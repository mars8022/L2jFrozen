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
package interlude.gameserver.instancemanager.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import interlude.L2DatabaseFactory;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.ZoneManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2WorldRegion;
import interlude.gameserver.model.actor.instance.L2MonsterInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2SiegeFlagInstance;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.model.zone.type.L2ClanHallSiegeZone;
import interlude.gameserver.network.clientpackets.Say2;
import interlude.gameserver.network.serverpackets.CreatureSay;
import interlude.gameserver.taskmanager.ExclusiveTask;
import interlude.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Author: MHard
 */
public class BanditStrongholdSiege
{
        protected static Log _log = LogFactory.getLog(BanditStrongholdSiege.class);
        private static BanditStrongholdSiege _instance;
    	private Calendar _siegeDate;
    	public Calendar _siegeEndDate;
    	private boolean _isInProgress = false;
        private boolean _registrationPeriod = false;
        private int _clanCounter = 0;
        private Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<Integer, clanPlayersInfo>();
        private L2ZoneType zone = ZoneManager.getInstance().getZoneById(11114);
        public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(35);
        private clanPlayersInfo _ownerClanInfo = new clanPlayersInfo();
        private boolean _finalStage = false;
        private ScheduledFuture<?> _midTimer;

	public static final BanditStrongholdSiege getInstance()
	{
		if (_instance == null)
			_instance = new BanditStrongholdSiege();
		return _instance;
	}

	private BanditStrongholdSiege()
	{
    	_isInProgress = false;
		_log.info("SiegeManager of Bandits Stronghold");
		long siegeDate = restoreSiegeDate(35);
		Calendar tmpDate=Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate,35,22);
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

		if (_clansInfo.size() == 1 && clanhall.getOwnerId() == 0)
		{
			endSiege(false);
			return;
		}

		if (_clansInfo.size() == 1 && clanhall.getOwnerId() != 0)
		{
			L2Clan clan=null;
			for (clanPlayersInfo a : _clansInfo.values())
				clan=ClanTable.getInstance().getClanByName(a._clanName);
			setIsInProgress(true);
			((L2ClanHallSiegeZone)zone).updateSiegeStatus();
			startSecondStep(clan);
			anonce("Take place at the siege of his headquarters.",1);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 30);
			_endSiegeTask.schedule(1000);
			return;
		}

		setIsInProgress(true);
		((L2ClanHallSiegeZone)zone).updateSiegeStatus();
		spawnFlags();
		gateControl(1);
		anonce("Take place at the siege of his headquarters.",1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5*60000);
		_midTimer=ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 25*60000);
		
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 60);
		_endSiegeTask.schedule(1000);
	}

	public void startSecondStep(L2Clan winner)
	{
		FastList<String> winPlayers = BanditStrongholdSiege.getInstance().getRegisteredPlayers(winner);
		unSpawnAll();
		_clansInfo.clear();
		clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName=winner.getName();
		regPlayers._players=winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerId(),_ownerClanInfo);
		spawnFlags();
		gateControl(1);
		_finalStage = true;
		anonce("Take place at the siege of his headquarters.",1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5*60000);
	}

	public void endSiege(boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			L2Clan winner=checkHaveWinner();
			if(winner!=null)
			{
				ClanHallManager.getInstance().setOwner(clanhall.getId(),winner);
				anonce("Attention! Clan hall, Bandit Stronghold was conquered by the clan " + winner.getName(),2);
			}
			else
				anonce("Attention! Clan hall, Bandit Stronghold did not get a new owner",2);
		}
		setIsInProgress(false);
		((L2ClanHallSiegeZone)zone).updateSiegeStatus();
		unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(),35,22);
		_startSiegeTask.schedule(1000);
	}

	public void unSpawnAll()
	{
		for(String clanName : getRegisteredClans())
		{
			L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
			L2MonsterInstance mob=getQuestMob(clan);
			L2SiegeFlagInstance flag = getSiegeFlag(clan);
			if (mob!=null)
				mob.deleteMe();
			if (flag!=null)
				flag.deleteMe();
		}
	}

	public void gateControl(int val)
	{
		if (val==1)
		{
			DoorTable.getInstance().getDoor(22170001).openMe();
			DoorTable.getInstance().getDoor(22170002).openMe();
			DoorTable.getInstance().getDoor(22170003).closeMe();
			DoorTable.getInstance().getDoor(22170004).closeMe();
		}
		else if (val==2)
		{
			DoorTable.getInstance().getDoor(22170001).closeMe();
			DoorTable.getInstance().getDoor(22170002).closeMe();
			DoorTable.getInstance().getDoor(22170003).closeMe();
			DoorTable.getInstance().getDoor(22170004).closeMe();
		}
	}

	public void teleportPlayers()
	{
		for(L2Character cha : zone.getCharactersInside().values())
		{
			if (cha instanceof L2PcInstance)
			{
				L2Clan clan=((L2PcInstance)cha).getClan();
				if (!isPlayerRegister(clan,cha.getName()))
					cha.teleToLocation(88404, -21821, -2276);
			}
		}
	}

	public L2Clan checkHaveWinner()
	{
		L2Clan res=null;
		int questMobCount=0;
		for(String clanName:getRegisteredClans())
		{
			L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan)!=null)
			{
				res=clan;
				questMobCount++;
			}
		}
		if (questMobCount>1)
			return null;
		return res;
	}

	private class midSiegeStep implements Runnable 
	{
		public void run()
		{
			_mobControlTask.cancel();
			L2Clan winner=checkHaveWinner();
			if(winner!=null)
			{
				if (clanhall.getOwnerId() == 0)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(),winner);
					anonce("Attention! Clan hall, Bandit Stronghold was conquered by the clan " + winner.getName(),2);
					endSiege(false);
				}
				else
					startSecondStep(winner);
			}
			else
			{
				endSiege(true);
			}
		}
	}

	private class startFirstStep implements Runnable 
	{
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter=1;
			for(String clanName : getRegisteredClans())
			{
				L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(35427+mobCounter);
				L2MonsterInstance questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
				questMob.setServerSideTitle(true);
				questMob.setTitle(clan.getName());
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter==1)
					questMob.spawnMe(83752,-17354,-1828);
				else if (mobCounter==2)
					questMob.spawnMe(82018,-15126,-1829);
				else if (mobCounter==3)
					questMob.spawnMe(85320,-16191,-1823);
				else if (mobCounter==4)
					questMob.spawnMe(81522,-16503,-1829);
				else if (mobCounter==5)
					questMob.spawnMe(83786,-15369,-1828);
				clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob=questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle began. Kill the enemy NPC",1);
		}
	}

	public void spawnFlags()
	{
		int flagCounter=1;
		for(String clanName : getRegisteredClans())
		{
			L2NpcTemplate template;
			L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (clan.getClanId() == clanhall.getOwnerId())
				template = NpcTable.getInstance().getTemplate(35422);
			else
				template = NpcTable.getInstance().getTemplate(35422+flagCounter);
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(null, IdFactory.getInstance().getNextId(), template);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan.getClanId() == clanhall.getOwnerId())
				flag.spawnMe(81700,-16300,-1828);
			else
			{
				if (flagCounter==1)
					flag.spawnMe(83452,-17654,-1828);
				else if (flagCounter==2)
					flag.spawnMe(81718,-14826,-1829);
				else if (flagCounter==3)
					flag.spawnMe(85020,-15891,-1823);
				else if (flagCounter==4)
					flag.spawnMe(81222,-16803,-1829);
				else if (flagCounter==5)
					flag.spawnMe(83486,-15069,-1828);
			}
			clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag=flag;
			flagCounter++;
		}
	}

	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}

	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}

	public boolean isPlayerRegister(L2Clan playerClan,String playerName)
	{
		if (playerClan==null)
			return false;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				return true;
		return false;
	}

	public boolean isClanOnSiege(L2Clan playerClan)
	{
		if (playerClan.getClanId() == clanhall.getOwnerId())
			return true;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
		{
			return false;
		}
		return true;
	}

	public synchronized int registerClanOnSiege(L2PcInstance player,L2Clan playerClan)
	{
		if (_clanCounter==5)
			return 2;
		L2ItemInstance item=player.getInventory().getItemByItemId(5009);
		if ((item!=null)&&(player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false)))
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName=playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			}
		}
		else
			return 1;
		return 0;
	}

	public boolean unRegisterClan(L2Clan playerClan)
	{
		if(_clansInfo.remove(playerClan.getClanId())!=null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}

	public FastList<String> getRegisteredClans()
	{
		FastList<String> clans=new FastList<String>();
		for (clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}

	public FastList<String> getRegisteredPlayers(L2Clan playerClan)
	{
		if (playerClan.getClanId() == clanhall.getOwnerId())
			return _ownerClanInfo._players;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			return regPlayers._players;
		return null;
	}

	public L2SiegeFlagInstance getSiegeFlag(L2Clan playerClan)
	{
		clanPlayersInfo clanInfo=_clansInfo.get(playerClan.getClanId());
		if(clanInfo!=null)
			return clanInfo._flag;
		return null;
	}

	public L2MonsterInstance getQuestMob(L2Clan clan)
	{
		clanPlayersInfo clanInfo=_clansInfo.get(clan.getClanId());
		if(clanInfo!=null)
			return clanInfo._mob;
		return null;
	}

	public int getPlayersCount(String playerClan)
	{
		for (clanPlayersInfo a : _clansInfo.values())
			if(a._clanName.equals(playerClan))
				return a._players.size();
		return 0;
	}

	public void addPlayer(L2Clan playerClan,String playerName)
	{
		if (playerClan.getClanId() == clanhall.getOwnerId() && _ownerClanInfo._players.size() < 18 && !_ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.add(playerName);
			return;
		}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.size()<18)
				if (!regPlayers._players.contains(playerName))
					regPlayers._players.add(playerName);
	}

	public void removePlayer(L2Clan playerClan,String playerName)
	{
		if (playerClan.getClanId() == clanhall.getOwnerId() && _ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.remove(playerName);
			return;
		}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				regPlayers._players.remove(playerName);
	}

	private final ExclusiveTask _startSiegeTask = new ExclusiveTask(){
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			Calendar siegeStart=Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining=registerTimeRemaining;
			if (registerTimeRemaining <= 0)
			{
				if (!isRegistrationPeriod())
				{
					if (clanhall.getOwnerId() != 0)
						_ownerClanInfo._clanName=clanhall.getOwnerClan().getName();
					else
						_ownerClanInfo._clanName="";
					setRegistrationPeriod(true);
					anonce("Attention! The period of registration at the siege clan hall, Bandit Stronghold.",2);
					remaining=siegeTimeRemaining;
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

	public void anonce(String text,int type)
	{
		CreatureSay cs = new CreatureSay(0, Say2.SHOUT, "Messenger", text);
		if (type==1)
		{
			for(String clanName:getRegisteredClans())
			{
				L2Clan clan=ClanTable.getInstance().getClanByName(clanName);
				for(String playerName:getRegisteredPlayers(clan))
				{
					L2PcInstance cha = L2World.getInstance().getPlayer(playerName);
					if (cha!=null)
						cha.sendPacket(cs);
				}
			}
		}
		else
		{
			L2WorldRegion region = L2World.getInstance().getRegion(88404, -21821);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (region == L2World.getInstance().getRegion(player.getX(), player.getY())	&& player.getInstanceId() == 0)
					player.sendPacket(cs);
			}
		}
	}

	private final ExclusiveTask _endSiegeTask = new ExclusiveTask() {
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

	private final ExclusiveTask _mobControlTask = new ExclusiveTask() {
		@Override
		protected void onElapsed()
		{
			int mobCount=0;
			for(clanPlayersInfo cl:_clansInfo.values())
				if (cl._mob.isDead())
				{
					L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
					unRegisterClan(clan);
				}
				else
					mobCount++;
			teleportPlayers();
			if (mobCount<2)
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
				schedule(3000);
		}
	};

	private class clanPlayersInfo
	{
		public String _clanName;
		public L2SiegeFlagInstance _flag = null;
		public L2MonsterInstance _mob = null;
		public FastList<String> _players	= new FastList<String>();
	}

	public long restoreSiegeDate(int ClanHallId)
	{
		long res = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT siege_data FROM clanhall_siege WHERE id=?");
			statement.setInt(1, ClanHallId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
				res = rs.getLong("siege_data");
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: can't get clanhall siege date: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	public void setNewSiegeDate(long siegeDate, int ClanHallId, int hour)
	{
		Calendar tmpDate = Calendar.getInstance();
		if (siegeDate <= System.currentTimeMillis())
		{
			tmpDate.setTimeInMillis(System.currentTimeMillis());
			tmpDate.add(Calendar.DAY_OF_MONTH, 3);
			tmpDate.set(Calendar.DAY_OF_WEEK, 6);
			tmpDate.set(Calendar.HOUR_OF_DAY, hour);
			tmpDate.set(Calendar.MINUTE, 0);
			tmpDate.set(Calendar.SECOND, 0);
			setSiegeDate(tmpDate);
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clanhall_siege SET siege_data=? WHERE id = ?");
				statement.setLong(1, getSiegeDate().getTimeInMillis());
				statement.setInt(2, ClanHallId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Exception: can't save clanhall siege date: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					if (con != null)
						con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public final void setSiegeDate(Calendar par)
	{
		_siegeDate = par;
	}

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public final void setIsInProgress(boolean par)
	{
		_isInProgress = par;
	}
}
