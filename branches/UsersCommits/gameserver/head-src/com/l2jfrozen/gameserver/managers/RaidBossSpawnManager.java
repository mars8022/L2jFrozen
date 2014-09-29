/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author godson
 */
public class RaidBossSpawnManager
{
	private static Logger _log = Logger.getLogger(RaidBossSpawnManager.class.getName());
	
	protected static Map<Integer, L2RaidBossInstance> _bosses = new FastMap<Integer, L2RaidBossInstance>();
	protected static Map<Integer, L2Spawn> _spawns = new FastMap<Integer, L2Spawn>();
	protected static Map<Integer, StatsSet> _storedInfo = new FastMap<Integer, StatsSet>();
	protected static Map<Integer, ScheduledFuture<?>> _schedules = new FastMap<Integer, ScheduledFuture<?>>();

	public static enum StatusEnum
	{
		ALIVE,
		DEAD,
		UNDEFINED
	}

	public RaidBossSpawnManager()
	{
		init();
	}

	public static RaidBossSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void init()
	{
		_bosses.clear();
		_schedules.clear();
		_storedInfo.clear();
		_spawns.clear();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);

			PreparedStatement statement = con.prepareStatement("SELECT * from raidboss_spawnlist ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template;
			long respawnTime;
			while(rset.next())
			{
				template = getValidTemplate(rset.getInt("boss_id"));
				if(template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setLocx(rset.getInt("loc_x"));
					spawnDat.setLocy(rset.getInt("loc_y"));
					spawnDat.setLocz(rset.getInt("loc_z"));
					spawnDat.setAmount(rset.getInt("amount"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
					spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
					respawnTime = rset.getLong("respawn_time");

					StatsSet info = new StatsSet();
					info.set("respawnTime", respawnTime);
					_storedInfo.put(rset.getInt("boss_id"), info);
					
					addNewSpawn(spawnDat, respawnTime, rset.getDouble("currentHP"), rset.getDouble("currentMP"), false);

					spawnDat = null;
					template = null;
				}
				else
				{
					_log.warning("RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
				}
			}

			_log.info("RaidBossSpawnManager: Loaded " + _bosses.size() + " Instances");
			_log.info("RaidBossSpawnManager: Scheduled " + _schedules.size() + " Instances");

			rset.close();
			statement.close();
			statement = null;
			rset = null;
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("RaidBossSpawnManager: Couldnt load raidboss_spawnlist table");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	private class spawnSchedule implements Runnable
	{
		private int bossId;

		public spawnSchedule(int npcId)
		{
			bossId = npcId;
		}

		@Override
		public void run()
		{
			L2RaidBossInstance raidboss = null;

			if(bossId == 25328)
			{
				raidboss = DayNightSpawnManager.getInstance().handleBoss(_spawns.get(bossId));
			}
			else
			{
				raidboss = (L2RaidBossInstance) _spawns.get(bossId).doSpawn();
			}

			if(raidboss != null)
			{
				raidboss.setRaidStatus(StatusEnum.ALIVE);

				StatsSet info = new StatsSet();
				info.set("currentHP", raidboss.getCurrentHp());
				info.set("currentMP", raidboss.getCurrentMp());
				info.set("respawnTime", 0L);

				_storedInfo.put(bossId, info);
				info = null;

				GmListTable.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());
				if(Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + raidboss.getName() + " spawned in world.");
				}
				_bosses.put(bossId, raidboss);
			}

			_schedules.remove(bossId);
		}
	}

	public void updateStatus(L2RaidBossInstance boss, boolean isBossDead)
	{
		if(!_storedInfo.containsKey(boss.getNpcId()))
			return;

		StatsSet info = _storedInfo.get(boss.getNpcId());

		if(isBossDead)
		{
			boss.setRaidStatus(StatusEnum.DEAD);

			long respawnTime;
			int RespawnMinDelay = boss.getSpawn().getRespawnMinDelay();
			int RespawnMaxDelay = boss.getSpawn().getRespawnMaxDelay();
			long respawn_delay = Rnd.get((int) (RespawnMinDelay * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER), (int) (RespawnMaxDelay * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER));
			respawnTime = Calendar.getInstance().getTimeInMillis() + respawn_delay;

			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);
			
			String text = "RaidBossSpawnManager: Updated " + boss.getName() + " respawn time to " + respawnTime;
			Log.add(text, "RaidBossSpawnManager");
			
			ScheduledFuture<?> futureSpawn;
			futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new spawnSchedule(boss.getNpcId()), respawn_delay);

			_schedules.put(boss.getNpcId(), futureSpawn);
			futureSpawn = null;
			if(Config.SAVE_RAIDBOSS_STATUS_INTO_DB)
			{
				updateDb();
			}
		}
		else
		{
			boss.setRaidStatus(StatusEnum.ALIVE);

			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0L);
		}

		_storedInfo.remove(boss.getNpcId());
		_storedInfo.put(boss.getNpcId(), info);

		info = null;
	}

	public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
	{
		if(spawnDat == null)
			return;

		if(_spawns.containsKey(spawnDat.getNpcid()))
			return;

		int bossId = spawnDat.getNpcid();
		long time = Calendar.getInstance().getTimeInMillis();

		SpawnTable.getInstance().addNewSpawn(spawnDat, false);

		if(respawnTime == 0L || time > respawnTime)
		{
			L2RaidBossInstance raidboss = null;

			if(bossId == 25328)
			{
				raidboss = DayNightSpawnManager.getInstance().handleBoss(spawnDat);
			}
			else
			{
				raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			}

			if(raidboss != null)
			{
				double bonus = raidboss.getStat().calcStat(Stats.MAX_HP, 1, raidboss, null);
				
				if(Config.DEBUG){
					System.out.println(" bossId: "+bossId );
					System.out.println(" 	maxHp: "+raidboss.getMaxHp() );
					System.out.println(" 	currHp: "+(int)currentHP );
					System.out.println(" 	bonusHp: "+bonus);
					System.out.println(" 	calculatedHp: "+(int)(bonus*currentHP));
				}
				
				//if new spawn, the currentHp is equal to maxHP/bonus, so set it to max
				if((int)(bonus*currentHP)==raidboss.getMaxHp()){
					currentHP = (raidboss.getMaxHp());
				}
				
				raidboss.setCurrentHp(currentHP);
				raidboss.setCurrentMp(currentMP);
				raidboss.setRaidStatus(StatusEnum.ALIVE);

				_bosses.put(bossId, raidboss);

				StatsSet info = new StatsSet();
				info.set("currentHP", currentHP);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0L);

				_storedInfo.put(bossId, info);

				raidboss = null;
			}
		}
		else
		{
			ScheduledFuture<?> futureSpawn;
			long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();

			futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new spawnSchedule(bossId), spawnTime);

			_schedules.put(bossId, futureSpawn);

			futureSpawn = null;
		}

		_spawns.put(bossId, spawnDat);

		if(storeInDb)
		{
			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?,?)");
				statement.setInt(1, spawnDat.getNpcid());
				statement.setInt(2, spawnDat.getAmount());
				statement.setInt(3, spawnDat.getLocx());
				statement.setInt(4, spawnDat.getLocy());
				statement.setInt(5, spawnDat.getLocz());
				statement.setInt(6, spawnDat.getHeading());
				statement.setLong(7, respawnTime);
				statement.setDouble(8, currentHP);
				statement.setDouble(9, currentMP);
				statement.execute();
				statement.close();
				statement = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				// problem with storing spawn
				_log.warning("RaidBossSpawnManager: Could not store raidboss #" + bossId + " in the DB:" + e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}

	public void deleteSpawn(L2Spawn spawnDat, boolean updateDb)
	{
		if(spawnDat == null)
			return;

		if(!_spawns.containsKey(spawnDat.getNpcid()))
			return;

		int bossId = spawnDat.getNpcid();

		SpawnTable.getInstance().deleteSpawn(spawnDat, false);
		_spawns.remove(bossId);

		if(_bosses.containsKey(bossId))
		{
			_bosses.remove(bossId);
		}

		if(_schedules.containsKey(bossId))
		{
			ScheduledFuture<?> f = _schedules.get(bossId);
			f.cancel(true);
			_schedules.remove(bossId);
		}

		if(_storedInfo.containsKey(bossId))
		{
			_storedInfo.remove(bossId);
		}

		if(updateDb)
		{
			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?");
				statement.setInt(1, bossId);
				statement.execute();
				statement.close();
				statement = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				// problem with deleting spawn
				_log.warning("RaidBossSpawnManager: Could not remove raidboss #" + bossId + " from DB: " + e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}

	private void updateDb()
	{
		for(Integer bossId : _storedInfo.keySet())
		{
			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);

				L2RaidBossInstance boss = _bosses.get(bossId);
				if(boss != null)
				{
					if(boss.getRaidStatus().equals(StatusEnum.ALIVE))
					{
						updateStatus(boss, false);
					}

					boss = null;

					StatsSet info = _storedInfo.get(bossId);
					if(info != null)
					{
						PreparedStatement statement = con.prepareStatement("UPDATE raidboss_spawnlist set respawn_time = ?, currentHP = ?, currentMP = ? where boss_id = ?");
						statement.setLong(1, info.getLong("respawnTime"));
						statement.setDouble(2, info.getDouble("currentHP"));
						statement.setDouble(3, info.getDouble("currentMP"));
						statement.setInt(4, bossId);
						statement.execute();

						statement.close();
						statement = null;
						info = null;
					}

				}
			}
			catch(SQLException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.warning("RaidBossSpawnManager: Couldnt update raidboss_spawnlist table");
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}

	public String[] getAllRaidBossStatus()
	{
		String[] msg = new String[_bosses == null ? 0 : _bosses.size()];

		if(_bosses == null)
		{
			msg[0] = "None";
			return msg;
		}

		int index = 0;

		for(int i : _bosses.keySet())
		{
			L2RaidBossInstance boss = _bosses.get(i);

			msg[index] = boss.getName() + ": " + boss.getRaidStatus().name();
			index++;

			boss = null;
		}

		return msg;
	}

	public String getRaidBossStatus(int bossId)
	{
		String msg = "RaidBoss Status....\n";

		if(_bosses == null)
		{
			msg += "None";
			return msg;
		}

		if(_bosses.containsKey(bossId))
		{
			L2RaidBossInstance boss = _bosses.get(bossId);

			msg += boss.getName() + ": " + boss.getRaidStatus().name();
		}

		return msg;
	}

	public StatusEnum getRaidBossStatusId(int bossId)
	{
		if(_bosses.containsKey(bossId))
			return _bosses.get(bossId).getRaidStatus();
		else if(_schedules.containsKey(bossId))
			return StatusEnum.DEAD;
		else
			return StatusEnum.UNDEFINED;
	}

	public L2NpcTemplate getValidTemplate(int bossId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if(template == null)
			return null;

		if(!template.type.equalsIgnoreCase("L2RaidBoss"))
			return null;

		return template;
	}

	public void notifySpawnNightBoss(L2RaidBossInstance raidboss)
	{
		StatsSet info = new StatsSet();
		info.set("currentHP", raidboss.getCurrentHp());
		info.set("currentMP", raidboss.getCurrentMp());
		info.set("respawnTime", 0L);

		raidboss.setRaidStatus(StatusEnum.ALIVE);

		_storedInfo.put(raidboss.getNpcId(), info);

		info = null;

		GmListTable.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());

		_bosses.put(raidboss.getNpcId(), raidboss);
	}

	public boolean isDefined(int bossId)
	{
		return _spawns.containsKey(bossId);
	}

	public Map<Integer, L2RaidBossInstance> getBosses()
	{
		return _bosses;
	}

	public Map<Integer, L2Spawn> getSpawns()
	{
		return _spawns;
	}

	public void reloadBosses()
	{
		init();
	}

	/**
	 * Saves all raidboss status and then clears all info from memory, including all schedules.
	 */

	public void cleanUp()
	{
		updateDb();

		_bosses.clear();

		if(_schedules != null)
		{
			for(Integer bossId : _schedules.keySet())
			{
				ScheduledFuture<?> f = _schedules.get(bossId);
				f.cancel(true);
			}
			_schedules.clear();
		}
		
		_storedInfo.clear();
		_spawns.clear();
	}
	
	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public L2RaidBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossSpawnManager _instance = new RaidBossSpawnManager();
	}
}
