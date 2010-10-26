/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.event.manager.EventTask;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DM implements EventTask
{   
	private final static Log _log = LogFactory.getLog(DM.class.getName());
	public static String _eventName = new String(),
						 _eventDesc = new String(),
						 _joiningLocationName = new String();
	public static Vector<String> _savePlayers = new Vector<String>();
	public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>();  
	public static boolean _joining = false,
						  _teleport = false,
						  _started = false,
						  _aborted = false,
						  _inProgress  = false,
						  _sitForced = false;
	public static L2Spawn _npcSpawn;
	public static L2PcInstance _topPlayer;
	public static int _npcId = 0,
					  _npcX = 0,
					  _npcY = 0,
					  _npcZ = 0,
					  _npcHeading = 0,
					  _rewardId = 0,
					  _rewardAmount = 0,
					  _joinTime = 0,
					  _eventTime = 0,
					  _topKills = 0,
					  _minlvl = 0,
					  _maxlvl = 0,
					  _playerColors = 0,
					  _playerX = 0,
					  _playerY = 0,
					  _playerZ = 0,
					  _minPlayers = 0,
					  _maxPlayers = 0;
	
	public static long _intervalBetweenMatchs = 0;

	private String startEventTime;

	private DM(){
		
	}
	
	public static DM getNewInstance(){
		return new DM();
	}
	
	public static void setNpcPos(L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
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

	public static void setPlayersPos(L2PcInstance activeChar)
	{
		_playerX = activeChar.getX();
		_playerY = activeChar.getY();
		_playerZ = activeChar.getZ();
	}

	public static boolean checkPlayerOk()
	{
		if(_started || _teleport || _joining)
			return false;

		return true;
	}

	public static void startJoin(L2PcInstance activeChar)
	{
		if(!startJoinOk())
		{
			if(_log.isDebugEnabled())_log.debug("DM Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			return;
		}

		_joining = true;
		spawnEventNpc(activeChar);
		Announcements.getInstance().gameAnnounceToAll("Death Match!");
		Announcements.getInstance().gameAnnounceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().gameAnnounceToAll("Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().gameAnnounceToAll("Joinable in " + _joiningLocationName + "!");
	}
	
	public static boolean startAutoJoin()
	{
		if(!startJoinOk())
		{
			if(_log.isDebugEnabled())_log.debug("DM Engine[startAutoJoin()]: startAutoJoin() = false");
			return false;
		}

		_joining = true;
		spawnEventNpc(null);
		Announcements.getInstance().gameAnnounceToAll("Death Match!");
		Announcements.getInstance().gameAnnounceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().gameAnnounceToAll("Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().gameAnnounceToAll("Joinable in " + _joiningLocationName + " or by command .dmjoin!");
		Announcements.getInstance().gameAnnounceToAll("To leave .dmleave! DM Info .dminfo!");
		return true;
	}

	public static boolean startJoinOk()
	{
		if(_started || _teleport || _joining || _eventName.equals("") ||
			_joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 ||
			_npcX == 0 || _npcY == 0 || _npcZ == 0 || _npcHeading == 0 || _rewardId == 0 || _rewardAmount == 0 ||
			 _joinTime == 0 || _eventTime == 0 || _playerX == 0 || _playerY == 0 || _playerZ == 0)
			return false;

		return true;
	}

	private static void spawnEventNpc(L2PcInstance activeChar)
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
			if(_log.isDebugEnabled())
				if(activeChar!=null)
					_log.debug("DM Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
				else
					_log.debug("DM Engine[spawnEventNpc(null)]: exception: " + e.getMessage());
		}
	}

	public static void teleportStart()
	{
		if(!_joining || _started || _teleport)
			return;

		if(checkMinPlayers(_players.size()))
		{
			removeOfflinePlayers();
		}
		else if(!checkMinPlayers(_players.size()))
		{
			Announcements.getInstance().gameAnnounceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _players.size());
			return;
		}
		
		_joining = false;
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Teleport to team spot in 20 seconds!");

		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				DM.sit();

				for(L2PcInstance player : DM._players)
				{
					if(player !=  null)
					{
						if(Config.DM_ON_START_UNSUMMON_PET)
						{
							//Remove Summon's buffs
							if(player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for(L2Effect e : summon.getAllEffects())
									e.exit();

								if(summon instanceof L2PetInstance)
									summon.unSummon(player);
							}
						}

						if(Config.DM_ON_START_REMOVE_ALL_EFFECTS)
						{
							for(L2Effect e : player.getAllEffects())
							{
								if(e != null) e.exit();
							}
						}

						// Remove player from his party
						if(player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.teleToLocation(_playerX, _playerY, _playerZ);
					}
				}
			}
		}, 20000);
		_teleport = true;
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
	
	public static boolean teleportAutoStart()
	{
		if(!_joining || _started || _teleport)
			return true;
		
		if(checkMinPlayers(_players.size()))
		{
			removeOfflinePlayers();
		}
		else if(!checkMinPlayers(_players.size()))
		{
			Announcements.getInstance().gameAnnounceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _players.size());
			return false;
		}

		_joining = false;
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Teleport to team spot in 20 seconds!");

		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				DM.sit();

				for(L2PcInstance player : DM._players)
				{
					if(player !=  null)
					{
						if(Config.DM_ON_START_UNSUMMON_PET)
						{
							//Remove Summon's buffs
							if(player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for(L2Effect e : summon.getAllEffects())
									e.exit();

								if(summon instanceof L2PetInstance)
									summon.unSummon(player);
							}
						}

						if(Config.DM_ON_START_REMOVE_ALL_EFFECTS)
						{
							for(L2Effect e : player.getAllEffects())
							{
								if(e != null) e.exit();
							}
						}

						// Remove player from his party
						if(player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.teleToLocation(_playerX, _playerY, _playerZ);
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
	}
	
	public static void startEvent(L2PcInstance activeChar)
	{
		if(_inProgress)
		{
			if(activeChar!=null)
				activeChar.sendMessage("A DM event is already in progress, try abort.");
			return;
		}
		
		if(!startEventOk())
		{
			if(_log.isDebugEnabled())_log.debug("DM Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			return;
		}

		_teleport = false;
		sit();
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Started. Go to kill your enemies!");
		_started = true;
		_inProgress = true;
	}
	
	public static boolean startAutoEvent()
	{
		
		if(!startEventOk())
		{
			if(_log.isDebugEnabled())_log.debug("DM Engine[startAutoEvent()]: startEventOk() = false");
			return true;
		}

		_teleport = false;
		sit();
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Started. Go to kill your enemies!");
		_started = true;
		_inProgress = true;
		return true;
	}

	public static void autoEvent()
	{
		
		_log.info("Starting DM!");
		_log.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatchs() + " Minutes.");
		if (startAutoJoin() && !_aborted)
		{
			if (_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if (_joinTime <= 0)
			{
				_log.info("DM: join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (teleportAutoStart() && !_aborted)
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startAutoEvent() && !_aborted)
				{
					_log.debug("DM: waiting.....minutes for event time " + DM._eventTime);

					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();

					_log.info("DM: waiting... delay for final messages ");
					waiter(60000);//just a give a delay delay for final messages
					sendFinalMessages();

					if (!_started && !_aborted){ //if is not already started and it's not aborted
						
						_log.info("DM: waiting.....delay for restart event  " + DM.getIntervalBetweenMatchs() + " minutes.");
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
		
		if(startAutoJoin() && !_aborted)
		{
			if(_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if(_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if(teleportAutoStart() && !_aborted)
			{
				waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if(startAutoEvent() && !_aborted)
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
			seconds--; // here because we don't want to see two time announce at the same time

			if(_joining || _started || _teleport)
			{
				switch(seconds)
				{
					case 3600: // 1 hour left
						if(_joining)
						{
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds / 60 / 60 + " hour(s) till event finish!");

						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						if(_joining)
						{
							removeOfflinePlayers();
							Announcements.getInstance().gameAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds / 60 + " minute(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds / 60 + " minute(s) till event finish!");

						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						if(_joining)
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds + " second(s) till registration close!");
						else if(_teleport)
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds + " seconds(s) till start fight!");
						else if(_started)
							Announcements.getInstance().gameAnnounceToAll("DM: " + seconds + " second(s) till event finish!");

						break;
				}
			}

			long startOneSecondWaiterStartTime = System.currentTimeMillis();

			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
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
	/**
	 * Restarts Event
	 * checks if event was aborted. and if true cancels restart task
	 */
	public synchronized static void restartEvent()
	{
		_log.info("DM: Event has been restarted...");
		_joining = false;
		_started = false;
		_inProgress = false;
		_aborted = false;
		long delay = _intervalBetweenMatchs;

		Announcements.getInstance().gameAnnounceToAll("DM: joining period will be avaible again in " + getIntervalBetweenMatchs() + " minute(s)!");

		waiter(delay);

		try
		{
			if(!_aborted)
				autoEvent(); //start a new event
			else
				Announcements.getInstance().gameAnnounceToAll("DM: next event aborted!");
				
		}
		catch (Exception e)
		{
			_log.fatal("DM: Error While Trying to restart Event...", e);
			e.printStackTrace();
		}
	}

	
	public static void setJoinTime(int time)
	{
		_joinTime = time;
	}

	public static void setEventTime(int time)
	{
		_eventTime = time;
	}

	private static boolean startEventOk()
	{
		if(_joining || !_teleport || _started)
			return false;

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

	public static void finishEvent()
	{
		if(!finishEventOk())
		{
			if(_log.isDebugEnabled())_log.debug("DM Engine[finishEvent()]: finishEventOk() = false");
			return;
		}

		_started = false;
		_aborted = false;
		unspawnEventNpc();
		processTopPlayer();

		if(_topKills == 0)
			Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): No players win the match(nobody killed).");
		else
		{
			Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): " + _topPlayer.getName() + " wins the match! " + _topKills + " kills.");
			rewardPlayer();
		}

		teleportFinish();
	}

	private static boolean finishEventOk()
	{
		if(!_started)
			return false;
		
		_inProgress = false;

		return true;
	}

	public static void processTopPlayer()
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

	public static void abortEvent()
	{
		if(!_joining && !_teleport && !_started)
			return;

		_joining = false;
		_teleport = false;
		_started = false;
		_inProgress = false;
		_aborted = true;
		unspawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Match aborted!");
		teleportFinish();
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

	public static void dumpData()
	{
		System.out.println("");
		System.out.println("");

		if(!_joining && !_teleport && !_started)
		{
			System.out.println("<<---------------------------------->>");
			System.out.println(">> DM Engine infos dump (INACTIVE) <<");
			System.out.println("<<--^----^^-----^----^^------^^----->>");
		}
		else if(_joining && !_teleport && !_started)
		{
			System.out.println("<<--------------------------------->>");
			System.out.println(">> DM Engine infos dump (JOINING) <<");
			System.out.println("<<--^----^^-----^----^^------^----->>");
		}
		else if(!_joining && _teleport && !_started)
		{
			System.out.println("<<---------------------------------->>");
			System.out.println(">> DM Engine infos dump (TELEPORT) <<");
			System.out.println("<<--^----^^-----^----^^------^^----->>");
		}
		else if(!_joining && !_teleport && _started)
		{
			System.out.println("<<--------------------------------->>");
			System.out.println(">> DM Engine infos dump (STARTED) <<");
			System.out.println("<<--^----^^-----^----^^------^----->>");
		}

		System.out.println("Name: " + _eventName);
		System.out.println("Desc: " + _eventDesc);
		System.out.println("Join location: " + _joiningLocationName);
		System.out.println("Min lvl: " + _minlvl);
		System.out.println("Max lvl: " + _maxlvl);

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

		System.out.println("");
		System.out.println("");
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
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_rewardId = 0;
		_rewardAmount = 0;
		_joinTime = 0;
		_eventTime = 0;
		_topKills = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_playerColors = 0;
		_playerX = 0;
		_playerY = 0;
		_playerZ = 0;
		_intervalBetweenMatchs = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		
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
				replyMSG.append("<center>DM match is in progress.</center>");
			else if(eventPlayer.getLevel()<_minlvl || eventPlayer.getLevel()>_maxlvl )
			{
				replyMSG.append("Your lvl : <font color=\"00FF00\">" + eventPlayer.getLevel() +"</font><br>");
				replyMSG.append("Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
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
			_log.error("DM Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

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

	public static boolean addPlayerOk(L2PcInstance eventPlayer)
	{
		if(eventPlayer._inEventTvT)
		{
			eventPlayer.sendMessage("You already participated to another event!"); 
			return false;
		}

		if(eventPlayer._inEventCTF)
		{
			eventPlayer.sendMessage("You already participated to another event!"); 
			return false;
		}

		if(eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in the event!"); 
			return false;
		}
		return true;
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

	
	public static void removePlayer(L2PcInstance player)
	{
		if(player != null && player._inEventDM)
		{
			player.getAppearance().setNameColor(player._originalNameColorDM);
			player.setKarma(player._originalKarmaDM);
			player.broadcastUserInfo();
			
			player._countDMkills = 0;
			player._inEventDM = false;

			_players.remove(player);
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
		_sitForced = false;
		_topKills = 0;
		_players = new Vector<L2PcInstance>();
	}

	public static void unspawnEventNpc()
	{
		if(_npcSpawn == null)
			return;

		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}
	
	public static void teleportFinish()
	{
		Announcements.getInstance().gameAnnounceToAll(_eventName + "(DM): Teleport back to participation NPC in 20 seconds!");

		removeUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				for(L2PcInstance player : _players)
				{
					if(player !=  null)
						player.teleToLocation(_npcX, _npcY, _npcZ);
				} 
				cleanDM();
			}
		}, 20000);
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