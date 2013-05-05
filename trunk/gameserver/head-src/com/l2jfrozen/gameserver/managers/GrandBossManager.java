/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles all Grand Bosses:
 * <ul>
 * <li>22215-22217  Tyrannosaurus</li>
 * <li>25333-25338  Anakazel</li>
 * <li>29001        Queen Ant</li>
 * <li>29006        Core</li>
 * <li>29014        Orfen</li>
 * <li>29019        Antharas</li>
 * <li>29020        Baium</li>
 * <li>29022        Zaken</li>
 * <li>29028        Valakas</li>
 * <li>29045        Frintezza</li>
 * <li>29046-29047  Scarlet van Halisha</li>
 * </ul>
 *
 * It handles the saving of hp, mp, location, and status of all Grand Bosses. It also manages the zones associated with the Grand Bosses.
 * NOTE: The current version does NOT spawn the Grand Bosses, it just stores and retrieves the values on reboot/startup, for AI scripts to utilize as needed.
 * @author DaRkRaGe Revised by Emperorc
 */
public class GrandBossManager
{
	protected static final Logger _log = Logger.getLogger(GrandBossManager.class.getName());
	
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";
	
	private static GrandBossManager _instance;

	protected static Map<Integer, L2GrandBossInstance> _bosses;

	protected static Map<Integer, StatsSet> _storedInfo;

	private Map<Integer, Integer> _bossStatus;

	private List<L2BossZone> _zones;

	public static GrandBossManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing GrandBossManager");
			_instance = new GrandBossManager();
		}
		return _instance;
	}

	public GrandBossManager()
	{
		init();
	}

	private void init()
	{
		_zones = new CopyOnWriteArrayList<L2BossZone>();
		_bosses = new ConcurrentHashMap<Integer, L2GrandBossInstance>();
		_storedInfo = new ConcurrentHashMap<Integer, StatsSet>();
		_bossStatus = new ConcurrentHashMap<Integer, Integer>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);

			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				//Read all info from DB, and store it for AI to read and decide what to do
				//faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); //jython doesn't recognize doubles
				int true_HP = (int) HP; //so use java's ability to type cast
				info.set("currentHP", true_HP); //to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				_bossStatus.put(bossId, rset.getInt("status"));

				_storedInfo.put(bossId, info);
				info = null;
			}

			_log.info("GrandBossManager: Loaded " + _storedInfo.size() + " Instances");

			rset.close();
			statement.close();
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("GrandBossManager: Could not load grandboss_data table");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void initZones()
	{
		if(_zones == null)
		{
			_log.warning("GrandBossManager: Could not read Grand Boss zone data");
			return;
		}

		Map<Integer, List<Integer>> zones = new ConcurrentHashMap<Integer, List<Integer>>();
		for(L2BossZone zone : _zones)
		{
			if(zone == null)
			{
				continue;
			}
			zones.put(zone.getId(), new CopyOnWriteArrayList<Integer>());
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				zones.get(rset.getInt("zone")).add(rset.getInt("player_id"));
			}
			rset.close();
			statement.close();
			_log.info("GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch(SQLException e)
		{
			_log.warning("GrandBossManager: Could not load grandboss_list table");
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}

		for(L2BossZone zone : _zones)
		{
			if(zone == null)
			{
				continue;
			}
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}
		zones.clear();
	}

	public void addZone(L2BossZone zone)
	{
		if(_zones != null)
		{
			_zones.add(zone);
		}
	}

	public final L2BossZone getZone(L2Character character)
	{
		if(_zones != null)
		{
			for(L2BossZone temp : _zones)
			{
				if(temp.isCharacterInZone(character))
					return temp;
			}
		}
		return null;
	}

	public final L2BossZone getZone(int x, int y, int z)
	{
		if(_zones != null)
		{
			for(L2BossZone temp : _zones)
			{
				if(temp.isInsideZone(x, y, z))
					return temp;
			}
		}
		return null;
	}

	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if(temp == null)
			return false;
		return temp.getZoneName().equalsIgnoreCase(zoneType);
	}

	/*
	 * The rest
	 */

	public Integer getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}

	/*
	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.remove(bossId);
		_bossStatus.put(bossId, status);
	}
	*/
	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		_log.info(getClass().getSimpleName()+": Updated "+NpcTable.getInstance().getTemplate(bossId).getName()+"(" +bossId+ ") status to " +status);
		updateDb(bossId, true);
	}
	
	public void addBoss(L2GrandBossInstance boss)
	{
		if(boss != null)
		{
			_bosses.put(boss.getNpcId(), boss);
		}
	}

	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	public L2GrandBossInstance deleteBoss(int bossId)
	{
		return _bosses.remove(bossId);
	}

	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}

	public void setStatsSet(int bossId, StatsSet info)
	{
		if(_storedInfo.containsKey(bossId))
		{
			_storedInfo.remove(bossId);
		}
		_storedInfo.put(bossId, info);
		//Update immediately status in Database.
		fastStoreToDb();
	}

	private void fastStoreToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			for(Integer bossId : _storedInfo.keySet())
			{
				L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if(boss == null || info == null)
				{
					PreparedStatement update2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
					update2.setInt(1, _bossStatus.get(bossId));
					update2.setInt(2, bossId);
					update2.executeUpdate();
					update2.close();
				}
				else
				{
					PreparedStatement update1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					update1.setInt(1, boss.getX());
					update1.setInt(2, boss.getY());
					update1.setInt(3, boss.getZ());
					update1.setInt(4, boss.getHeading());
					update1.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if(boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					update1.setDouble(6, hp);
					update1.setDouble(7, mp);
					update1.setInt(8, _bossStatus.get(bossId));
					update1.setInt(9, bossId);
					update1.executeUpdate();
					update1.close();
				}
			}
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			_log.warning("GrandBossManager[fastStoreToDb]: Couldn't store grandbosses to database:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	private void storeToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);

			PreparedStatement delete = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			delete.executeUpdate();
			delete.close();

			for(L2BossZone zone : _zones)
			{
				if(zone == null)
				{
					continue;
				}
				Integer id = zone.getId();
				List<Integer> list = zone.getAllowedPlayers();
				if(list == null || list.isEmpty())
				{
					continue;
				}
				for(Integer player : list)
				{
					PreparedStatement insert = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
					insert.setInt(1, player);
					insert.setInt(2, id);
					insert.executeUpdate();
					insert.close();
				}
			}

			for(Integer bossId : _storedInfo.keySet())
			{
				L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if(boss == null || info == null)
				{
					PreparedStatement update2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
					update2.setInt(1, _bossStatus.get(bossId));
					update2.setInt(2, bossId);
					update2.executeUpdate();
					update2.close();
				}
				else
				{
					PreparedStatement update1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					update1.setInt(1, boss.getX());
					update1.setInt(2, boss.getY());
					update1.setInt(3, boss.getZ());
					update1.setInt(4, boss.getHeading());
					update1.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if(boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					update1.setDouble(6, hp);
					update1.setDouble(7, mp);
					update1.setInt(8, _bossStatus.get(bossId));
					update1.setInt(9, bossId);
					update1.executeUpdate();
					update1.close();
				}
			}
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("GrandBossManager: Couldn't store grandbosses to database:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	private void updateDb(int bossId, boolean statusOnly)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);
			
			if (statusOnly || boss == null || info == null)
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
				statement.setInt(1, _bossStatus.get(bossId));
				statement.setInt(2, bossId);
			}
			else
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
				statement.setInt(1, boss.getX());
				statement.setInt(2, boss.getY());
				statement.setInt(3, boss.getZ());
				statement.setInt(4, boss.getHeading());
				statement.setLong(5, info.getLong("respawn_time"));
				double hp = boss.getCurrentHp();
				double mp = boss.getCurrentMp();
				if (boss.isDead())
				{
					hp = boss.getMaxHp();
					mp = boss.getMaxMp();
				}
				statement.setDouble(6, hp);
				statement.setDouble(7, mp);
				statement.setInt(8, _bossStatus.get(bossId));
				statement.setInt(9, bossId);
			}
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "GrandBossManager: Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	

	/**
	 * Saves all Grand Boss info and then clears all info from memory, including all schedules.
	 */
	public void cleanUp()
	{
		storeToDb();

		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}
	
	public L2NpcTemplate getValidTemplate(int bossId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if(template == null)
			return null;

		if(!template.type.equalsIgnoreCase("L2GrandBoss"))
			return null;

		return template;
	}
	
	public boolean isDefined(int bossId) //into database
	{
		return _bossStatus.get(bossId)!=null;
	}
}
