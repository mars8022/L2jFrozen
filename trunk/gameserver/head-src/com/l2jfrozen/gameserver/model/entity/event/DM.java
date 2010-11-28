
package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.text.TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.event.manager.EventTask;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class DM implements EventTask
{   
	private final static Log _log = LogFactory.getLog(DM.class.getName());
	
	private static String _eventName = new String(),
						 _eventDesc = new String(),
						 _joiningLocationName = new String();

	private static L2Spawn _npcSpawn;

	private static boolean _joining = false,
						  _teleport = false,
						  _started = false,
						  _aborted = false,
						  _sitForced = false,
						  _inProgress  = false;

	private static int _npcId = 0,
					  _npcX = 0,
					  _npcY = 0,
					  _npcZ = 0,
					  _npcHeading = 0,
					  _rewardId = 0,
					  _rewardAmount = 0,
					  _minlvl = 0,
					  _maxlvl = 0,
					  _joinTime = 0,
					  _eventTime = 0,
					  _minPlayers = 0,
					  _maxPlayers = 0,
					  _topKills = 0,
					  _playerColors = 0,
					  _playerX = 0,
					  _playerY = 0,
					  _playerZ = 0;
	
	private static long _intervalBetweenMatchs = 0;
	
	private String startEventTime;
	
	private static boolean _teamEvent = false; //TODO to be integrated

	public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>();  
	
	public static L2PcInstance _topPlayer;
	public static Vector<String> _savePlayers = new Vector<String>();
	
	private DM(){
	}
	
	public static DM getNewInstance(){
		return new DM();
	}
	
	/**
	 * @return the _eventName
	 */
	public static String get_eventName()
	{
		return _eventName;
	}

	/**
	 * @param _eventName the _eventName to set
	 */
	public static boolean set_eventName(String _eventName)
	{
		if(!is_inProgress()){
			DM._eventName = _eventName;
			return true;
		}
		else
			return false;
		
	}

	/**
	 * @return the _eventDesc
	 */
	public static String get_eventDesc()
	{
		return _eventDesc;
	}

	/**
	 * @param _eventDesc the _eventDesc to set
	 */
	public static boolean set_eventDesc(String _eventDesc)
	{
		if(!is_inProgress()){
			DM._eventDesc = _eventDesc;
			return true;
		}
		else
			return false;
		
	}

	/**
	 * @return the _joiningLocationName
	 */
	public static String get_joiningLocationName()
	{
		return _joiningLocationName;
	}

	/**
	 * @param _joiningLocationName the _joiningLocationName to set
	 */
	public static boolean set_joiningLocationName(String _joiningLocationName)
	{
		if(!is_inProgress()){
			DM._joiningLocationName = _joiningLocationName;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _npcId
	 */
	public static int get_npcId()
	{
		return _npcId;
	}

	/**
	 * @param _npcId the _npcId to set
	 */
	public static boolean set_npcId(int _npcId)
	{
		if(!is_inProgress()){
			DM._npcId = _npcId;
			return true;
		}
		else
			return false;
		
		
	}
	
	public static Location get_npcLocation()
	{
		Location npc_loc = new Location(_npcX,_npcY,_npcZ,_npcHeading);
		
		return npc_loc;
		
	}

	/**
	 * @return the _rewardId
	 */
	public static int get_rewardId()
	{
		return _rewardId;
	}

	/**
	 * @param _rewardId the _rewardId to set
	 */
	public static boolean set_rewardId(int _rewardId)
	{
		if(!is_inProgress()){
			DM._rewardId = _rewardId;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _rewardAmount
	 */
	public static int get_rewardAmount()
	{
		return _rewardAmount;
	}

	/**
	 * @param _rewardAmount the _rewardAmount to set
	 */
	public static boolean set_rewardAmount(int _rewardAmount)
	{
		if(!is_inProgress()){
			DM._rewardAmount = _rewardAmount;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _minlvl
	 */
	public static int get_minlvl()
	{
		return _minlvl;
	}

	/**
	 * @param _minlvl the _minlvl to set
	 */
	public static boolean set_minlvl(int _minlvl)
	{
		if(!is_inProgress()){
			DM._minlvl = _minlvl;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _maxlvl
	 */
	public static int get_maxlvl()
	{
		return _maxlvl;
	}

	/**
	 * @param _maxlvl the _maxlvl to set
	 */
	public static boolean set_maxlvl(int _maxlvl)
	{
		if(!is_inProgress()){
			DM._maxlvl = _maxlvl;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _joinTime
	 */
	public static int get_joinTime()
	{
		return _joinTime;
	}

	/**
	 * @param _joinTime the _joinTime to set
	 */
	public static boolean set_joinTime(int _joinTime)
	{
		if(!is_inProgress()){
			DM._joinTime = _joinTime;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _eventTime
	 */
	public static int get_eventTime()
	{
		return _eventTime;
	}

	/**
	 * @param _eventTime the _eventTime to set
	 */
	public static boolean set_eventTime(int _eventTime)
	{
		if(!is_inProgress()){
			DM._eventTime = _eventTime;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _minPlayers
	 */
	public static int get_minPlayers()
	{
		return _minPlayers;
	}

	/**
	 * @param _minPlayers the _minPlayers to set
	 */
	public static boolean set_minPlayers(int _minPlayers)
	{
		if(!is_inProgress()){
			DM._minPlayers = _minPlayers;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _maxPlayers
	 */
	public static int get_maxPlayers()
	{
		return _maxPlayers;
	}

	/**
	 * @param _maxPlayers the _maxPlayers to set
	 */
	public static boolean set_maxPlayers(int _maxPlayers)
	{
		if(!is_inProgress()){
			DM._maxPlayers = _maxPlayers;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _intervalBetweenMatchs
	 */
	public static long get_intervalBetweenMatchs()
	{
		return _intervalBetweenMatchs;
	}

	/**
	 * @param _intervalBetweenMatchs the _intervalBetweenMatchs to set
	 */
	public static boolean set_intervalBetweenMatchs(long _intervalBetweenMatchs)
	{
		if(!is_inProgress()){
			DM._intervalBetweenMatchs = _intervalBetweenMatchs;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the startEventTime
	 */
	public String getStartEventTime()
	{
		return startEventTime;
	}

	/**
	 * @param startEventTime the startEventTime to set
	 */
	public boolean setStartEventTime(String startEventTime)
	{
		if(!is_inProgress()){
			this.startEventTime = startEventTime;
			return true;
		}
		else
			return false;
		
		
	}

	/**
	 * @return the _joining
	 */
	public static boolean is_joining()
	{
		return _joining;
	}

	/**
	 * @return the _teleport
	 */
	public static boolean is_teleport()
	{
		return _teleport;
	}

	/**
	 * @return the _started
	 */
	public static boolean is_started()
	{
		return _started;
	}

	/**
	 * @return the _aborted
	 */
	public static boolean is_aborted()
	{
		return _aborted;
	}

	/**
	 * @return the _sitForced
	 */
	public static boolean is_sitForced()
	{
		return _sitForced;
	}

	/**
	 * @return the _inProgress
	 */
	public static boolean is_inProgress()
	{
		return _inProgress;
	}
	
	public static boolean checkMaxLevel(int maxlvl)
	{
		if(_minlvl >= maxlvl)
			return false;

		return true;
	}

	public static boolean checkMinLevel(int minlvl)
	{
		if(_maxlvl <= minlvl)
			return false;

		return true;
	}
	
	/** returns true if participated players is higher or equal then minimum needed players */
	public static boolean checkMinPlayers(int players)
	{
		if(_minPlayers <= players)
			return true;

		return false;
	}
	
	/** returns true if max players is higher or equal then participated players */
	public static boolean checkMaxPlayers(int players)
	{
		if(_maxPlayers > players)
			return true;

		return false;
	}
	
	public static boolean checkStartJoinOk()
	{
		if(_started || _teleport || _joining || _eventName.equals("") ||
			_joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 ||
			_npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0)
			return false;
		
		if(_teamEvent){
			if(!checkStartJoinTeamInfo())
				return false;
		}else{
			if(!checkStartJoinPlayerInfo())
				return false;
		}
		
		if(!Config.ALLOW_EVENTS_DURING_OLY && Olympiad.getInstance().inCompPeriod())
			return false;
		

		for(Castle castle:CastleManager.getInstance().getCastles()){
			if(castle!=null && castle.getSiege()!=null && castle.getSiege().getIsInProgress())
				return false;
		}
		
		if(!checkOptionalEventStartJoinOk())
			return false;
		
		return true;
		
	}

	private static boolean checkStartJoinTeamInfo(){
			
		//TODO be integrated
		return true;
		
	}
	
	private static boolean checkStartJoinPlayerInfo(){
		
		if(_playerX == 0 || _playerY == 0 || _playerZ == 0 || _playerColors == 0){
			return false;
		}
		
		return true;
		
	}

	private static boolean checkAutoEventStartJoinOk(){
		
		if(_joinTime == 0 || _eventTime == 0){
			return false;
		}
		
		return true;
	}
	
	private static boolean checkOptionalEventStartJoinOk(){
		
		//TODO be integrated
		return true;
		
	}
	
	public static void setNpcPos(L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
	}
	
	private static void spawnEventNpc()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);

		try
		{
			_npcSpawn = new L2Spawn(tmpl);

			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);

			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobDM = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.error(_eventName+" Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}

	private static void unspawnEventNpc()
	{
		if(_npcSpawn == null || _npcSpawn.getLastSpawn()==null)
			return;

		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}

	public static boolean startJoin()
	{
		if(!checkStartJoinOk())
		{
			if(_log.isDebugEnabled())
				_log.debug(_eventName+" Engine[startJoin]: startJoinOk() = false");
			return false;
		}

		_inProgress = true;
		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll("Event: " + _eventName + "!");
		if(Config.DM_ANNOUNCE_REWARD)
			Announcements.getInstance().gameAnnounceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().gameAnnounceToAll("Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().gameAnnounceToAll("Joinable in " + _joiningLocationName);
		
		if(Config.DM_COMMAND)
			Announcements.getInstance().gameAnnounceToAll(" or by command .dmjoin! To leave .dmleave! For Info .dminfo!");
		
		Announcements.getInstance().gameAnnounceToAll("FULL BUFF Event: be ready with your buffs, they won't be deleted!!!");
		
		return true;
	}

	public static boolean startTeleport()
	{
		if(!_joining || _started || _teleport)
			return false;

		removeOfflinePlayers();
		
		if(_teamEvent){
			
			/*if(Config.EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
			{
				shuffleTeams();
			}
			else if(Config.EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
			{
				Announcements.getInstance().gameAnnounceToAll("Not enough players for event. Min Requested : " + _minPlayers +", Participating : " + _playersShuffle.size());
				return false;
			}*/
			
		}else{
			
			if(!checkMinPlayers(_players.size()))
			{
				Announcements.getInstance().gameAnnounceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _players.size());
				return false;
			}
			
		}

		_joining = false;
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Teleport to team spot in 20 seconds!");

		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				sit();
				afterTeleportOperations();

				for(L2PcInstance player : _players)
				{
					if(player != null)
					{
						if(Config.DM_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if(player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for(L2Effect e : summon.getAllEffects())
									if(e != null)
										e.exit();

								if(summon instanceof L2PetInstance)
									summon.unSummon(player);
							}
						}

						if(Config.DM_ON_START_REMOVE_ALL_EFFECTS)
						{
							for(L2Effect e : player.getAllEffects())
							{
								if(e != null)
									e.exit();
							}
						}

						// Remove player from his party
						if(player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						player.setTitle("Kills: " + player._countDMkills);
						
						if(_teamEvent){
							//player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
							
						}else{
							player.teleToLocation(_playerX, _playerY, _playerZ);
						}
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
	}

	private static void afterTeleportOperations(){
		
	}
	
	public static boolean startEvent()
	{
		if(!startEventOk())
		{
			if(_log.isDebugEnabled())
				_log.debug(_eventName+" Engine[startEvent()]: startEventOk() = false");
			return false;
		}

		_teleport = false;
		
		sit();
		
		afterStartOperations();
		
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Started. Go to kill your enemies!");
		_started = true;
		
		return true;
	}

	private static void afterStartOperations(){
	
	}
	
	/**
	 * Restarts Event
	 * checks if event was aborted. and if true cancels restart task
	 */
	public synchronized static void restartEvent()
	{
		_log.info(_eventName+": Event has been restarted...");
		_joining = false;
		_started = false;
		_inProgress = false;
		_aborted = false;
		long delay = _intervalBetweenMatchs;

		Announcements.getInstance().gameAnnounceToAll(_eventName+": joining period will be avaible again in " + _intervalBetweenMatchs + " minute(s)!");

		waiter(delay);

		try
		{
			if(!_aborted)
				autoEvent(); //start a new event
			else
				Announcements.getInstance().gameAnnounceToAll(_eventName+": next event aborted!");
				
		}
		catch (Exception e)
		{
			_log.fatal(_eventName+": Error While Trying to restart Event...", e);
			e.printStackTrace();
		}
	}
	
	public static void finishEvent()
	{
		if(!finishEventOk())
		{
			if(_log.isDebugEnabled())
				_log.debug(_eventName+" Engine[finishEvent]: finishEventOk() = false");
			return;
		}

		_started = false;
		_aborted = false;
		unspawnEventNpc();
		
		afterFinishOperations();
		
		if(_teamEvent){
			processTopTeam();
			
		}else{
			processTopPlayer();
			
			if(_topKills != 0){
				
				Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + _topPlayer.getName() + " wins the match! " + _topKills + " kills.");
				rewardPlayer();
			}else{
				
				Announcements.getInstance().gameAnnounceToAll(_eventName + ": No players win the match(nobody killed).");
			
			}
			
		}
		
		teleportFinish();
	}
	
	private static void afterFinishOperations(){
		
	}
	
	public static void abortEvent()
	{
		if(!_joining && !_teleport && !_started)
			return;
		
		if(_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanDM();
			_joining = false;
			_inProgress = false;
			Announcements.getInstance().gameAnnounceToAll(_eventName + ": Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		_aborted = true;
		unspawnEventNpc();
		
		afterFinish();
		
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Match aborted!");
		teleportFinish();
	}
	
	private static void afterFinish(){
		
	}
	
	public static void teleportFinish()
	{
		Announcements.getInstance().gameAnnounceToAll(_eventName + ": Teleport back to participation NPC in 20 seconds!");

		removeUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				for(L2PcInstance player : _players)
				{
					if(player != null)
					{
						if(player.isOnline() != 0)
							player.teleToLocation(_npcX, _npcY, _npcZ, false);
						else
						{
							java.sql.Connection con = null;
							try
							{
								con = L2DatabaseFactory.getInstance().getConnection();

								PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
								statement.setInt(1, _npcX);
								statement.setInt(2, _npcY);
								statement.setInt(3, _npcZ);
								statement.setString(4, player.getName());
								statement.execute();
								statement.close();
							}
							catch(Exception e)
							{
								_log.error(e.getMessage(), e);
								return;
							}
							finally
							{
								try
								{
									if(con != null)
									{
										con.close();
									}
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				cleanDM();
			}
		}, 20000);
	}

	public static void autoEvent()
	{
		_log.info("Starting "+_eventName+"!");
		_log.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatchs() + " Minutes.");
		if (checkAutoEventStartJoinOk() && startJoin() && !_aborted)
		{
			if (_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if (_joinTime <= 0)
			{
				_log.info(_eventName+": join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (startTeleport() && !_aborted)
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startEvent() && !_aborted)
				{
					_log.debug(_eventName+": waiting.....minutes for event time " + _eventTime);

					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();

					_log.info(_eventName+": waiting... delay for final messages ");
					waiter(60000);//just a give a delay delay for final messages
					sendFinalMessages();

					if (!_started && !_aborted){ //if is not already started and it's not aborted
						
						_log.info(_eventName+": waiting.....delay for restart event  " + _intervalBetweenMatchs + " minutes.");
						waiter(60000);//just a give a delay to next restart

						try
						{
							if(!_aborted)
								restartEvent();
						}
						catch (Exception e)
						{
							_log.error("Error while tying to Restart Event", e);
							e.printStackTrace();
						}
						
					}
						
				}
			}
			else if(!_aborted){
			
				abortEvent();
				restartEvent();
				
			}
		}
	}
	
	//start without restart
	public static void eventOnceStart(){
		
		if(startJoin() && !_aborted)
		{
			if(_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if(_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if(startTeleport() && !_aborted)
			{
				waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if(startEvent() && !_aborted)
				{
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if(!_aborted)
			{
				abortEvent();
			}
		}
		
	}

	private static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);

		while(startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time

			if(_joining || _started || _teleport)
			{
				switch(seconds)
				{
					case 3600: // 1 hour left
						removeOfflinePlayers();
						
						if(_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 / 60 + " hour(s) till event finish!");

						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						removeOfflinePlayers();
						
						if(_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minute(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds / 60 + " minute(s) till event finish!");

						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						removeOfflinePlayers();
						
						if(_joining)
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " second(s) till registration close!");
						else if(_teleport)
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " seconds(s) till start fight!");
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": " + seconds + " second(s) till event finish!");

						break;
				}
			}

			long startOneSecondWaiterStartTime = System.currentTimeMillis();

			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while(startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch(InterruptedException ie)
				{
				}
			}
		}
	}
	
	public static void sit()
	{
		if(_sitForced)
			_sitForced = false;
		else
			_sitForced = true;

		for(L2PcInstance player : _players)
		{
			if(player != null)
			{
				if(_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();

					if(!player.isSitting())
						player.sitDown();
				}
				else
				{
					if(player.isSitting())
						player.standUp();
				}
			}
		}
	}

	public static void removeOfflinePlayers()
	{
		try
		{
			if(_players == null)
				return;
			else if(_players.isEmpty())
				return;
			else if(_players.size() > 0)
			{
				for(L2PcInstance player : _players)
				{
					if(player == null)
						_players.remove(player);					
					else if(player.isOnline() == 0 || player.isInJail())
						removePlayer(player);
					if(_players.size() == 0 || _players.isEmpty())
						break;
				}
			}
		}
		catch(Exception e)
		{
			_log.error(e.getMessage(), e);
			return;
		}
	}
	
	private static boolean startEventOk()
	{
		if(_joining || !_teleport || _started)
			return false;

		return true;
	}
	
	private static boolean finishEventOk()
	{
		if(!_started)
			return false;

		return true;
	}

	private static boolean addPlayerOk(L2PcInstance eventPlayer)
	{
		if(eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}

		if(eventPlayer._inEventTvT || eventPlayer._inEventCTF)
		{
			eventPlayer.sendMessage("You already participated to another event!"); 
			return false;
		}
		
		if(Olympiad.getInstance().isRegistered(eventPlayer) || eventPlayer.isInOlympiadMode())
		{
			eventPlayer.sendMessage("You already participated in Olympiad!"); 
			return false;
		}
		
		if(eventPlayer._active_boxes>1 && !Config.ALLOW_DUALBOX_EVENT){
			eventPlayer.sendMessage("Dual Box not allowed in Events");
			return false;
		}
		
		for(L2PcInstance player: _players)
		{
			if(player.getObjectId()==eventPlayer.getObjectId())
			{
				eventPlayer.sendMessage("You already participated in the event!"); 
				return false;
			}
			else if(player.getName()==eventPlayer.getName())
			{
				eventPlayer.sendMessage("You already participated in the event!"); 
				return false;
			}
		}

		if(_players.contains(eventPlayer))
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		return true;
	}
	
	public static void setUserData()
	{
		for(L2PcInstance player : _players)
		{
			player._originalNameColorDM = player.getAppearance().getNameColor();
			player._originalKarmaDM = player.getKarma();
			player._inEventDM = true;
			player._countDMkills = 0;
			player.getAppearance().setNameColor(_playerColors);
			player.setKarma(0);
			player.broadcastUserInfo();
		}
	}

	public static void dumpData()
	{
		_log.info("");
		_log.info("");

		if(!_joining && !_teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> "+_eventName + " Engine infos dump (INACTIVE) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if(_joining && !_teleport && !_started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> "+_eventName + " Engine infos dump (JOINING) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}
		else if(!_joining && _teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> "+_eventName + " Engine infos dump (TELEPORT) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if(!_joining && !_teleport && _started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> "+_eventName + " Engine infos dump (STARTED) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}

		_log.info("Name: " + _eventName);
		_log.info("Desc: " + _eventDesc);
		_log.info("Join location: " + _joiningLocationName);
		_log.info("Min lvl: " + _minlvl);
		_log.info("Max lvl: " + _maxlvl);

		System.out.println("");
		System.out.println("##################################");
		System.out.println("# _players(Vector<L2PcInstance>) #");
		System.out.println("##################################");

		System.out.println("Total Players : " + _players.size());

		for(L2PcInstance player : _players)
		{
			if(player != null)
				System.out.println("Name: " + player.getName()+ " kills :" + player._countDMkills);
		}

		System.out.println("");
		System.out.println("################################");
		System.out.println("# _savePlayers(Vector<String>) #");
		System.out.println("################################");

		for(String player : _savePlayers)
			System.out.println("Name: " + player );

		_log.info("");
		_log.info("");
		
		dumpLocalEventInfo();
		
	}

	private static void dumpLocalEventInfo(){
		
	}
	
	public static void loadData()
	{
		_eventName = new String();
		_eventDesc = new String();
		_joiningLocationName = new String();
		_savePlayers = new Vector<String>();
		_players = new Vector<L2PcInstance>();
		
		_topPlayer = null;
		_npcSpawn = null;
		_joining = false;
		_teleport = false;
		_started = false;
		_sitForced = false;
		_aborted = false;
		_inProgress = false;
		
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_rewardId = 0;
		_rewardAmount = 0;
		_topKills = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		_intervalBetweenMatchs = 0;
		_playerColors = 0;
		_playerX = 0;
		_playerY = 0;
		_playerZ = 0;
		
		
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select * from dm");
			rs = statement.executeQuery();

			while(rs.next())
			{
				_eventName = rs.getString("eventName");
				_eventDesc = rs.getString("eventDesc");
				_joiningLocationName = rs.getString("joiningLocation");
				_minlvl = rs.getInt("minlvl");
				_maxlvl = rs.getInt("maxlvl");
				_npcId = rs.getInt("npcId");
				_npcX = rs.getInt("npcX");
				_npcY = rs.getInt("npcY");
				_npcZ = rs.getInt("npcZ");
				_npcHeading = rs.getInt("npcHeading");
				_rewardId = rs.getInt("rewardId");
				_rewardAmount = rs.getInt("rewardAmount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
				_playerColors = rs.getInt("color");
				_playerX = rs.getInt("playerX");
				_playerY = rs.getInt("playerY");
				_playerZ = rs.getInt("playerZ");
				_intervalBetweenMatchs = rs.getInt("delayForNextEvent");
			}
			statement.close();

		}
		catch(Exception e)
		{
			_log.error("Exception: DM.loadData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception e)
			{
			}
		}
	}

	public static void saveData()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("Delete from dm");
			statement.execute();
			statement.close();

			statement = con.prepareStatement("INSERT INTO dm (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, joinTime, eventTime, minPlayers, maxPlayers, color, playerX, playerY, playerZ, delayForNextEvent ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");  
			statement.setString(1, _eventName);
			statement.setString(2, _eventDesc);
			statement.setString(3, _joiningLocationName);
			statement.setInt(4, _minlvl);
			statement.setInt(5, _maxlvl);
			statement.setInt(6, _npcId);
			statement.setInt(7, _npcX);
			statement.setInt(8, _npcY);
			statement.setInt(9, _npcZ);
			statement.setInt(10, _npcHeading);
			statement.setInt(11, _rewardId);
			statement.setInt(12, _rewardAmount);
			statement.setInt(13, _joinTime);
			statement.setInt(14, _eventTime);
			statement.setInt(15, _minPlayers);
			statement.setInt(16, _maxPlayers);
			statement.setInt(17, _playerColors);
			statement.setInt(18, _playerX);
			statement.setInt(19, _playerY);
			statement.setInt(20, _playerZ);
			statement.setLong(21, _intervalBetweenMatchs);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.error("Exception: DM.saveData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception e)
			{
			}
		}
	}

	public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			TextBuilder replyMSG = new TextBuilder("<html><title>"+_eventName+"</title><body>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
			replyMSG.append("<center><font color=\"3366CC\">Current event:</font></center><br1>");
			replyMSG.append("<center>Name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font></center><br1>");
			replyMSG.append("<center>Description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font></center><br><br>");

			if(!_started && !_joining)
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			else if(!checkMaxPlayers(_players.size()))
			{
				if(!_started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + _players.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if(eventPlayer.isCursedWeaponEquiped() && !Config.DM_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if(!_started && _joining && eventPlayer.getLevel()>=_minlvl && eventPlayer.getLevel()<=_maxlvl)
			{
				if(_players.contains(eventPlayer) )
				{
					replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						
					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait till event start or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_dmevent_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
					replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + _minlvl + "</font></center></td><br>");
					replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font></center></td><br><br>");
					replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_dmevent_player_join\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");

					
				}
			}
			else if(_started && !_joining)
				replyMSG.append("<center>"+_eventName+" match is in progress.</center>");
			else if(eventPlayer.getLevel() < _minlvl || eventPlayer.getLevel() > _maxlvl)
			{
				replyMSG.append("Your lvl: <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
				replyMSG.append("Min lvl: <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
			}

			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);

			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket( ActionFailed.STATIC_PACKET );
		}
		catch(Exception e)
		{
			_log.error(_eventName+" Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
/*
	public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			TextBuilder replyMSG = new TextBuilder("<html><body>");
			replyMSG.append("DM Match<br><br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("	... name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
			replyMSG.append("	... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br><br>");

			if(!_started && !_joining)
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			else if(!checkMaxPlayers(_players.size())){
				
				if(!DM._started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + _players.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
				
			}
			else if(!_started && _joining && eventPlayer.getLevel()>=_minlvl && eventPlayer.getLevel()<=_maxlvl)
			{
				if(_players.contains(eventPlayer))
				{
					replyMSG.append("You participated already!<br><br>");

					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait till event start or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_dmevent_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					replyMSG.append("<td width=\"200\">Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");

					replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_dmevent_player_join\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");

				}
			}
			else if(_started && !_joining)
				replyMSG.append("<center>"+_eventName+" match is in progress.</center>");
			else if(eventPlayer.getLevel() < _minlvl || eventPlayer.getLevel() > _maxlvl)
			{
				replyMSG.append("Your lvl: <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
				replyMSG.append("Min lvl: <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
			}

			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);

			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket( ActionFailed.STATIC_PACKET );
		}
		catch(Exception e)
		{
			_log.error(_eventName+" Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	*/
	
	public static void addPlayer(L2PcInstance player)
	{
		if(!addPlayerOk(player))
			return;
		_players.add(player);
		player._originalNameColorDM = player.getAppearance().getNameColor();
		player._originalKarmaDM = player.getKarma();
		player._inEventDM = true;
		player._countDMkills = 0;
		_savePlayers.add(player.getName());
		player.sendMessage("DM: You successfully registered for the DeathMatch event.");
	}

	public static void removePlayer(L2PcInstance player)
	{
		if(player != null && player._inEventDM)
		{
			player.getAppearance().setNameColor(player._originalNameColorDM);
			player.setTitle(player._originalTitleDM);
			player.setKarma(player._originalKarmaDM);
			player.broadcastUserInfo();
			
			player._countDMkills = 0;
			player._inEventDM = false;

			_players.remove(player);
			
			player.sendMessage("Your participation in the DeathMatch event has been removed.");
			
		}
	}

	public static void cleanDM()
	{
		for(L2PcInstance player : _players)
		{
			removePlayer(player);
		}
		_savePlayers = new Vector<String>();
		_topPlayer = null;
		_npcSpawn = null;
		_joining = false;
		_teleport = false;
		_started = false;
		_inProgress = false;
		_sitForced = false;
		_topKills = 0;
		_players = new Vector<L2PcInstance>();
	}
	
	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		if(!_players.contains(player) && _savePlayers.contains(player.getName()))
		{
			if(Config.DM_ON_START_REMOVE_ALL_EFFECTS)
			{
				for(L2Effect e : player.getAllEffects())
				{
					if(e != null)
						e.exit();
				}
			}

			_players.add(player);

			player._originalNameColorDM = player.getAppearance().getNameColor();
			player._originalTitleDM = player.getTitle();
			player._originalKarmaDM = player.getKarma();
			player._inEventDM = true;
			player._countDMkills = 0;
			if(_teleport || _started)
			{
				player.getAppearance().setNameColor(_playerColors);
				player.setKarma(0);
				player.broadcastUserInfo();
				player.teleToLocation(_playerX, _playerY , _playerZ);
			}
		}
	}


	
	/**
	 * @return the _playerColors
	 */
	public static int get_playerColors()
	{
		return _playerColors;
	}

	/**
	 * @param _playerColors the _playerColors to set
	 */
	public static boolean set_playerColors(int _playerColors)
	{
		if(!is_inProgress()){
			DM._playerColors = _playerColors;
			return true;
		}
		else
			return false;

	}

	public static void rewardPlayer()
	{
		if(_topPlayer != null)
		{
			_topPlayer.addItem("DM Event: " + _eventName, _rewardId, _rewardAmount, _topPlayer, true);

			StatusUpdate su = new StatusUpdate(_topPlayer.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, _topPlayer.getCurrentLoad());
			_topPlayer.sendPacket(su);

			NpcHtmlMessage nhm = new NpcHtmlMessage(5);
			TextBuilder replyMSG = new TextBuilder("");

			replyMSG.append("<html><body>You won the event. Look in your inventory for the reward.</body></html>");

			nhm.setHtml(replyMSG.toString());
			_topPlayer.sendPacket(nhm);

			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			_topPlayer.sendPacket( ActionFailed.STATIC_PACKET );
		}
	}
	
	private static void processTopPlayer()
	{
		for(L2PcInstance player : _players)
		{
			if(player._countDMkills > _topKills)
			{
				_topPlayer = player;
				_topKills = player._countDMkills;
			}
		}
	}
	
	private static void processTopTeam()
	{
		
	}

	public static Location get_playersSpawnLocation()
	{
		Location npc_loc = new Location(_playerX,_playerY,_playerZ,0);
		
		return npc_loc;
		
	}
	
	public static void setPlayersPos(L2PcInstance activeChar)
	{
		_playerX = activeChar.getX();
		_playerY = activeChar.getY();
		_playerZ = activeChar.getZ();
	}
	
	

	public static void removeUserData()
	{
		for(L2PcInstance player : _players)
		{
			player.getAppearance().setNameColor(player._originalNameColorDM);
			player.setKarma(player._originalKarmaDM);
			player._inEventDM = false;
			player._countDMkills = 0;
			player.broadcastUserInfo();
		}
	}

	/**
	 * just an announcer to send termination messages
	 *
	 */
	public static void sendFinalMessages()
	{
		if (!_started && !_aborted)
			Announcements.getInstance().gameAnnounceToAll("DM: Thank you For Participating At, " + "DM Event.");
	}

	/**
	 * returns the interval between each event
	 * @return
	 */
	public static int getIntervalBetweenMatchs()
	{
		long actualTime = System.currentTimeMillis();
		long totalTime = actualTime + _intervalBetweenMatchs;
		long interval = totalTime - actualTime;
		int seconds = (int) (interval / 1000);

		return Math.round(seconds / 60);
	}

	public void setEventStartTime(String newTime){
		startEventTime = newTime;
	}

	@Override
	public String getEventIdentifier()
	{
		return _eventName;
	}

	@Override
	public void run()
	{
		System.out.println("DM: Event notification start");
		eventOnceStart();
	}

	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	public static void onDisconnect(L2PcInstance player){
		
		if(player._inEventDM){
			
			removePlayer(player);
			if(player !=  null)
				player.teleToLocation(_npcX, _npcY, _npcZ);
			
		}
		
	}
	
}