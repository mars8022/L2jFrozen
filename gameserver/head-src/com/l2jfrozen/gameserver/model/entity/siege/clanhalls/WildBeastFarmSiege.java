/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

/*
 *  Author: Qwerty, Scoria dev.
 *  v 1.0
 */

package com.l2jfrozen.gameserver.model.entity.siege.clanhalls;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2DecoInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.siege.ClanHallSiege;
import com.l2jfrozen.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.taskmanager.ExclusiveTask;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class WildBeastFarmSiege extends ClanHallSiege
{
	protected static Logger LOGGER = Logger.getLogger(WildBeastFarmSiege.class);
	private static WildBeastFarmSiege _instance;
	private boolean _registrationPeriod = false;
	private int _clanCounter = 0;
	protected Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<>();
	public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(63);
	protected clanPlayersInfo _ownerClanInfo = new clanPlayersInfo();
	protected boolean _finalStage = false;
	protected ScheduledFuture<?> _midTimer;
	private L2ClanHallZone zone;
	
	public static final WildBeastFarmSiege getInstance()
	{
		if (_instance == null)
		{
			_instance = new WildBeastFarmSiege();
		}
		return _instance;
	}
	
	private WildBeastFarmSiege()
	{
		LOGGER.info("SiegeManager of Wild Beasts Farm");
		final long siegeDate = restoreSiegeDate(63);
		final Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 63, 22);
		// Schedule siege auto start
		_startSiegeTask.schedule(1000);
	}
	
	public void startSiege()
	{
		setRegistrationPeriod(false);
		if (_clansInfo.size() == 0)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() == 1 && clanhall.getOwnerClan() == null)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() == 1 && clanhall.getOwnerClan() != null)
		{
			L2Clan clan = null;
			for (final clanPlayersInfo a : _clansInfo.values())
			{
				clan = ClanTable.getInstance().getClanByName(a._clanName);
			}
			setIsInProgress(true);
			startSecondStep(clan);
			anonce("Take place at the siege of his headquarters.", 1);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 30);
			_endSiegeTask.schedule(1000);
			return;
		}
		setIsInProgress(true);
		spawnFlags();
		gateControl(1);
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5 * 60000);
		_midTimer = ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 25 * 60000);
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 60);
		_endSiegeTask.schedule(1000);
	}
	
	public void startSecondStep(final L2Clan winner)
	{
		final FastList<String> winPlayers = WildBeastFarmSiege.getInstance().getRegisteredPlayers(winner);
		unSpawnAll();
		_clansInfo.clear();
		final clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName = winner.getName();
		regPlayers._players = winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerClan().getClanId(), _ownerClanInfo);
		spawnFlags();
		gateControl(1);
		_finalStage = true;
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5 * 60000);
	}
	
	public void endSiege(final boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			final L2Clan winner = checkHaveWinner();
			if (winner != null)
			{
				ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
				anonce("Attention! Clan hall, farm beasts was conquered by the clan " + winner.getName(), 2);
			}
			else
			{
				anonce("Attention! Clan hall, farm wild animals did not get new owner", 2);
			}
		}
		setIsInProgress(false);
		unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 63, 22);
		_startSiegeTask.schedule(1000);
	}
	
	public void unSpawnAll()
	{
		for (final String clanName : getRegisteredClans())
		{
			final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			final L2MonsterInstance mob = getQuestMob(clan);
			final L2DecoInstance flag = getSiegeFlag(clan);
			if (mob != null)
			{
				mob.deleteMe();
			}
			if (flag != null)
			{
				flag.deleteMe();
			}
		}
	}
	
	public void gateControl(final int val)
	{
		if (val == 1)
		{
			DoorTable.getInstance().getDoor(21150003).openMe();
			DoorTable.getInstance().getDoor(21150004).openMe();
			DoorTable.getInstance().getDoor(21150001).closeMe();
			DoorTable.getInstance().getDoor(21150002).closeMe();
		}
		else if (val == 2)
		{
			DoorTable.getInstance().getDoor(21150001).closeMe();
			DoorTable.getInstance().getDoor(21150002).closeMe();
			DoorTable.getInstance().getDoor(21150003).closeMe();
			DoorTable.getInstance().getDoor(21150004).closeMe();
		}
	}
	
	public void teleportPlayers()
	{
		zone = clanhall.getZone();
		for (final L2Character cha : zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				final L2Clan clan = ((L2PcInstance) cha).getClan();
				if (!isPlayerRegister(clan, cha.getName()))
				{
					cha.teleToLocation(53468, -94092, -1634);
				}
			}
	}
	
	public L2Clan checkHaveWinner()
	{
		L2Clan res = null;
		int questMobCount = 0;
		for (final String clanName : getRegisteredClans())
		{
			final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan) != null)
			{
				res = clan;
				questMobCount++;
			}
		}
		if (questMobCount > 1)
			return null;
		return res;
	}
	
	protected class midSiegeStep implements Runnable
	{
		@Override
		public void run()
		{
			_mobControlTask.cancel();
			final L2Clan winner = checkHaveWinner();
			if (winner != null)
			{
				if (clanhall.getOwnerClan() == null)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
					anonce("Attention! Hall clan Fkrma wild animals was conquered by the clan " + winner.getName(), 2);
					endSiege(false);
				}
				else
				{
					startSecondStep(winner);
				}
			}
			else
			{
				endSiege(true);
			}
		}
	}
	
	protected class startFirstStep implements Runnable
	{
		@Override
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter = 1;
			for (final String clanName : getRegisteredClans())
			{
				L2NpcTemplate template;
				final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
				template = NpcTable.getInstance().getTemplate(35617 + mobCounter);
				/*
				 * template.setServerSideTitle(true); template.setTitle(clan.getName());
				 */
				final L2MonsterInstance questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter == 1)
				{
					questMob.spawnMe(57069, -91797, -1360);
				}
				else if (mobCounter == 2)
				{
					questMob.spawnMe(58838, -92232, -1354);
				}
				else if (mobCounter == 3)
				{
					questMob.spawnMe(57327, -93373, -1365);
				}
				else if (mobCounter == 4)
				{
					questMob.spawnMe(57820, -91740, -1354);
				}
				else if (mobCounter == 5)
				{
					questMob.spawnMe(58728, -93487, -1360);
				}
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob = questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle began. Kill the enemy NPC", 1);
		}
	}
	
	public void spawnFlags()
	{
		int flagCounter = 1;
		for (final String clanName : getRegisteredClans())
		{
			L2NpcTemplate template;
			final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (clan == clanhall.getOwnerClan())
			{
				template = NpcTable.getInstance().getTemplate(35422);
			}
			else
			{
				template = NpcTable.getInstance().getTemplate(35422 + flagCounter);
			}
			final L2DecoInstance flag = new L2DecoInstance(IdFactory.getInstance().getNextId(), template);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan == clanhall.getOwnerClan())
			{
				flag.spawnMe(58782, -93180, -1354);
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._flag = flag;
				continue;
			}
			
			if (flagCounter == 1)
			{
				flag.spawnMe(56769, -92097, -1360);
			}
			else if (flagCounter == 2)
			{
				flag.spawnMe(59138, -92532, -1354);
			}
			else if (flagCounter == 3)
			{
				flag.spawnMe(57027, -93673, -1365);
			}
			else if (flagCounter == 4)
			{
				flag.spawnMe(58120, -91440, -1354);
			}
			else if (flagCounter == 5)
			{
				flag.spawnMe(58428, -93787, -1360);
			}
			final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag = flag;
			flagCounter++;
		}
	}
	
	public void setRegistrationPeriod(final boolean par)
	{
		_registrationPeriod = par;
	}
	
	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}
	
	public boolean isPlayerRegister(final L2Clan playerClan, final String playerName)
	{
		if (playerClan == null)
			return false;
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				return true;
		return false;
	}
	
	public boolean isClanOnSiege(final L2Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
			return true;
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
			return false;
		return true;
	}
	
	public synchronized int registerClanOnSiege(final L2PcInstance player, final L2Clan playerClan)
	{
		if (_clanCounter == 5)
			return 2;
		final L2ItemInstance item = player.getInventory().getItemByItemId(8293);
		if (item != null && player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName = playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			}
		}
		else
			return 1;
		return 0;
	}
	
	public boolean unRegisterClan(final L2Clan playerClan)
	{
		if (_clansInfo.remove(playerClan.getClanId()) != null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}
	
	public FastList<String> getRegisteredClans()
	{
		final FastList<String> clans = new FastList<>();
		for (final clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}
	
	public FastList<String> getRegisteredPlayers(final L2Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
			return _ownerClanInfo._players;
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			return regPlayers._players;
		return null;
	}
	
	public L2DecoInstance getSiegeFlag(final L2Clan playerClan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(playerClan.getClanId());
		if (clanInfo != null)
			return clanInfo._flag;
		return null;
	}
	
	public L2MonsterInstance getQuestMob(final L2Clan clan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(clan.getClanId());
		if (clanInfo != null)
			return clanInfo._mob;
		return null;
	}
	
	public int getPlayersCount(final String playerClan)
	{
		for (final clanPlayersInfo a : _clansInfo.values())
			if (a._clanName.equalsIgnoreCase(playerClan))
				return a._players.size();
		return 0;
	}
	
	public void addPlayer(final L2Clan playerClan, final String playerName)
	{
		if (playerClan == clanhall.getOwnerClan())
			if (_ownerClanInfo._players.size() < 18)
				if (!_ownerClanInfo._players.contains(playerName))
				{
					_ownerClanInfo._players.add(playerName);
					return;
				}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.size() < 18)
				if (!regPlayers._players.contains(playerName))
				{
					regPlayers._players.add(playerName);
				}
	}
	
	public void removePlayer(final L2Clan playerClan, final String playerName)
	{
		if (playerClan == clanhall.getOwnerClan())
			if (_ownerClanInfo._players.contains(playerName))
			{
				_ownerClanInfo._players.remove(playerName);
				return;
			}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
			{
				regPlayers._players.remove(playerName);
			}
	}
	
	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			final Calendar siegeStart = Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining = registerTimeRemaining;
			if (registerTimeRemaining <= 0)
			{
				if (!isRegistrationPeriod())
				{
					if (clanhall.getOwnerClan() != null)
					{
						_ownerClanInfo._clanName = clanhall.getOwnerClan().getName();
					}
					else
					{
						_ownerClanInfo._clanName = "";
					}
					setRegistrationPeriod(true);
					anonce("Attention! The period of registration at the siege clan hall, farm wild animals.", 2);
					remaining = siegeTimeRemaining;
				}
			}
			if (siegeTimeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};
	
	public void anonce(final String text, final int type)
	{
		if (type == 1)
		{
			final CreatureSay cs = new CreatureSay(0, 1, "Journal", text);
			for (final String clanName : getRegisteredClans())
			{
				final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
				for (final String playerName : getRegisteredPlayers(clan))
				{
					final L2PcInstance cha = L2World.getInstance().getPlayer(playerName);
					if (cha != null)
					{
						cha.sendPacket(cs);
					}
				}
			}
		}
		else
		{
			final CreatureSay cs = new CreatureSay(0, 1, "Journal", text);
			/* L2MapRegion region = MapRegionManager.getInstance().getRegion(53508, -93776, -1584); */
			for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if /*
					 * (region == MapRegionManager.getInstance().getRegion(player.getX(), player.getY(), player.getZ()) &&
					 */(player.getInstanceId() == 0)/* ) */
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	protected final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};
	protected final ExclusiveTask _mobControlTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			int mobCount = 0;
			for (final clanPlayersInfo cl : _clansInfo.values())
				if (cl._mob.isDead())
				{
					final L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
					unRegisterClan(clan);
				}
				else
				{
					mobCount++;
				}
			teleportPlayers();
			if (mobCount < 2)
				if (_finalStage)
				{
					_siegeEndDate = Calendar.getInstance();
					_endSiegeTask.cancel();
					_endSiegeTask.schedule(5000);
				}
				else
				{
					_midTimer.cancel(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 5000);
				}
			else
			{
				schedule(3000);
			}
		}
	};
	
	protected class clanPlayersInfo
	{
		public String _clanName;
		public L2DecoInstance _flag = null;
		public L2MonsterInstance _mob = null;
		public FastList<String> _players = new FastList<>();
	}
}
