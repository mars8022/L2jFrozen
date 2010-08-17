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
package com.l2scoria.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.text.TextBuilder;
import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.csv.DoorTable;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.datatables.sql.SpawnTable;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TvT
{
	private final static Log _log = LogFactory.getLog(TvT.class.getName());
	public static String _eventName = new String(), _eventDesc = new String(), _topTeam = new String(), _joiningLocationName = new String();
	public static Vector<String> _teams = new Vector<String>(), _savePlayers = new Vector<String>(), _savePlayerTeams = new Vector<String>();
	public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>(), _playersShuffle = new Vector<L2PcInstance>();
	public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(), _teamKillsCount = new Vector<Integer>(), _teamColors = new Vector<Integer>(), _teamsX = new Vector<Integer>(),
			_teamsY = new Vector<Integer>(), _teamsZ = new Vector<Integer>();
	public static boolean _joining = false, _teleport = false, _started = false, _sitForced = false;
	public static L2Spawn _npcSpawn;

	public static int _npcId = 0, _npcX = 0, _npcY = 0, _npcZ = 0, _npcHeading = 0, _rewardId = 0, _rewardAmount = 0, _topKills = 0, _minlvl = 0, _maxlvl = 0, _joinTime = 0, _eventTime = 0, _minPlayers = 0, _maxPlayers = 0;

	public static void kickPlayerFromTvt(L2PcInstance playerToKick)
	{
		if(playerToKick == null)
			return;
		
		if(_joining)
		{
			_playersShuffle.remove(playerToKick);
			_players.remove(playerToKick);
			playerToKick._inEventTvT = false;
			playerToKick._teamNameTvT = "";
			playerToKick._countTvTkills = 0;
		}
		if(_started || _teleport)
		{
			_playersShuffle.remove(playerToKick);
			playerToKick._inEventTvT = false;
			removePlayer(playerToKick);
			if(playerToKick.isOnline() != 0)
			{
				playerToKick.getAppearance().setNameColor(playerToKick._originalNameColorTvT);
				playerToKick.setKarma(playerToKick._originalKarmaTvT);
				playerToKick.setTitle(playerToKick._originalTitleTvT);
				playerToKick.broadcastUserInfo();
				playerToKick.sendMessage("You have been kicked from the TvT.");
				playerToKick.teleToLocation(_npcX, _npcY, _npcZ, false);
				playerToKick.teleToLocation(_npcX + Rnd.get(201) - 100, _npcY + Rnd.get(201) - 100, _npcZ, false);
			}
		}
	}

	public static void setNpcPos(L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
	}

	public static void setNpcPos(int x, int y, int z)
	{
		_npcX = x;
		_npcY = y;
		_npcZ = z;
	}

	public static void addTeam(String teamName)
	{
		if(!checkTeamOk())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}

		if(teamName.equals(" "))
			return;

		_teams.add(teamName);
		_teamPlayersCount.add(0);
		_teamKillsCount.add(0);
		_teamColors.add(0);
		_teamsX.add(0);
		_teamsY.add(0);
		_teamsZ.add(0);
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

	public static void removeTeam(String teamName)
	{
		if(!checkTeamOk() || _teams.isEmpty())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}

		if(teamPlayersCount(teamName) > 0)
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
			return;
		}

		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamsZ.remove(index);
		_teamsY.remove(index);
		_teamsX.remove(index);
		_teamColors.remove(index);
		_teamKillsCount.remove(index);
		_teamPlayersCount.remove(index);
		_teams.remove(index);
	}

	public static void setTeamPos(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamsX.set(index, activeChar.getX());
		_teamsY.set(index, activeChar.getY());
		_teamsZ.set(index, activeChar.getZ());
	}

	public static void setTeamPos(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamsX.set(index, x);
		_teamsY.set(index, y);
		_teamsZ.set(index, z);
	}

	public static void setTeamColor(String teamName, int color)
	{
		if(!checkTeamOk())
			return;

		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamColors.set(index, color);
	}

	public static boolean checkTeamOk()
	{
		if(_started || _teleport || _joining)
			return false;

		return true;
	}

	public static void startJoin(L2PcInstance activeChar)
	{
		if(!startJoinOk())
		{
			activeChar.sendMessage("Event not setted propertly.");
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			return;
		}

		_joining = true;
		spawnEventNpc(activeChar);
		Announcements.getInstance().announceToAll(_eventName + "!");
		if(Config.TVT_ANNOUNCE_REWARD)
			Announcements.getInstance().announceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().announceToAll("Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().announceToAll("Joinable in " + _joiningLocationName + " or by command .tvtjoin!");
		Announcements.getInstance().announceToAll("To leave .tvtleave! TvT Info .tvtinfo!");
	}

	public static void startJoin()
	{
		if(!startJoinOk())
		{
			_log.warn("Event not setted propertly.");
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[startJoin(startJoinOk() = false");
			return;
		}

		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + "!");
		if(Config.TVT_ANNOUNCE_REWARD)
			Announcements.getInstance().announceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().announceToAll("Recruiting levels " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().announceToAll("Joinable in " + _joiningLocationName + " or by command .tvtjoin!");
		Announcements.getInstance().announceToAll("To leave .tvtleave! TvT Info .tvtinfo!");
	}

	public static boolean startAutoJoin()
	{
		if(!startJoinOk())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[startJoin]: startJoinOk() = false");
			return false;
		}

		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + "!");
		if(Config.TVT_ANNOUNCE_REWARD)
			Announcements.getInstance().announceToAll("Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		Announcements.getInstance().announceToAll("Recruiting levels " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().announceToAll("Joinable in " + _joiningLocationName + " or by command .tvtjoin!");
		Announcements.getInstance().announceToAll("To leave .tvtleave! TvT Info .tvtinfo!");
		return true;
	}

	public static boolean startJoinOk()
	{
		if(_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") || _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 || _npcX == 0 || _npcY == 0
				|| _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 || _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0))
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
			_npcSpawn.getLastSpawn()._isEventMobTvT = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.error("TvT Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
		}
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
			_npcSpawn.getLastSpawn()._isEventMobTvT = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.error("TvT Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}

	public static void teleportStart()
	{
		if(!_joining || _started || _teleport)
			return;

		if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements.getInstance().announceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return;
		}

		_joining = false;
		Announcements.getInstance().announceToAll(_eventName + ": Teleport to team spot in 20 seconds!");

		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				TvT.sit();

				for(L2PcInstance player : _players)
				{
					if(player != null)
					{
						if(Config.TVT_ON_START_UNSUMMON_PET)
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

						if(Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
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

						player.setTitle("Kills: " + player._countTvTkills);
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201) - 100, _teamsY.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201) - 100, _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
	}

	public static boolean teleportAutoStart()
	{
		if(!_joining || _started || _teleport)
			return false;

		if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements.getInstance().announceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return false;
		}

		_joining = false;
		Announcements.getInstance().announceToAll(_eventName + ": Teleport to team spot in 20 seconds!");

		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				TvT.sit();

				for(L2PcInstance player : _players)
				{
					if(player != null)
					{
						if(Config.TVT_ON_START_UNSUMMON_PET)
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

						if(Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
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

						player.setTitle("Kills: " + player._countTvTkills);
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201) - 100, _teamsY.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201) - 100, _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
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
			activeChar.sendMessage("A TvT event is already in progress, try abort.");
			return;
		}
		if(!startEventOk())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			return;
		}

		_teleport = false;
		sit();

		if(Config.TVT_OPEN_FORT_DOORS)
		{
			openFortDoors();
		}

		Announcements.getInstance().announceToAll(_eventName + ": Started. Go to kill your enemies!");
		_started = true;
		_inProgress = true;
	}

	public static void setJoinTime(int time)
	{
		_joinTime = time;
	}

	public static void setEventTime(int time)
	{
		_eventTime = time;
	}

	public static boolean startAutoEvent()
	{
		if(!startEventOk())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[startEvent]: startEventOk() = false");
			return false;
		}

		_teleport = false;
		sit();

		if(Config.TVT_OPEN_FORT_DOORS)
		{
			openFortDoors();
		}

		Announcements.getInstance().announceToAll(_eventName + ": Started. Go to kill your enemies!");
		_started = true;
		return true;
	}

	public static void autoEvent()
	{
		if(startAutoJoin())
		{
			if(_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if(_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if(teleportAutoStart())
			{
				waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if(startAutoEvent())
				{
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if(!teleportAutoStart())
			{
				abortEvent();
			}
		}
	}

	private static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);

		while(startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--; // here because we don't want to see two time announce at the same time

			if(_joining || _started || _teleport)
			{
				switch(seconds)
				{
					case 3600: // 1 hour left
						if(_joining)
						{
							Announcements.getInstance().announceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().announceToAll("TvT: " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().announceToAll("TvT: " + seconds / 60 / 60 + " hour(s) till event finish!");

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
							Announcements.getInstance().announceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().announceToAll("TvT: " + seconds / 60 + " minute(s) till registration close!");
						}
						else if(_started)
							Announcements.getInstance().announceToAll("TvT: " + seconds / 60 + " minute(s) till event finish!");

						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						if(_joining)
							Announcements.getInstance().announceToAll("TvT: " + seconds + " second(s) till registration close!");
						else if(_teleport)
							Announcements.getInstance().announceToAll("TvT: " + seconds + " seconds(s) till start fight!");
						else if(_started)
							Announcements.getInstance().announceToAll("TvT: " + seconds + " second(s) till event finish!");

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

	private static boolean startEventOk()
	{
		if(_joining || !_teleport || _started)
			return false;

		if(Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			if(_teamPlayersCount.contains(0))
				return false;
		}
		else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
			int loopCount = 0;

			loopCount = _playersShuffle.size();

			for(int i = 0; i < loopCount; i++)
			{
				if(_playersShuffle != null)
					playersShuffleTemp.add(_playersShuffle.get(i));
			}

			_playersShuffle = playersShuffleTemp;
			playersShuffleTemp.clear();

			// if (_playersShuffle.size() < (_teams.size()*2)){
			// return false;
			// }
		}

		return true;
	}

	public static void shuffleTeams()
	{
		int teamCount = 0, playersCount = 0;

		for(;;)
		{
			if(_playersShuffle.isEmpty())
				break;

			int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
			L2PcInstance player = null;
			player = _playersShuffle.get(playerToAddIndex);
			player._originalNameColorTvT = player.getAppearance().getNameColor();
			player._originalTitleTvT = player.getTitle();
			player._originalKarmaTvT = player.getKarma();

			_players.add(player);
			_players.get(playersCount)._teamNameTvT = _teams.get(teamCount);
			_savePlayers.add(_players.get(playersCount).getName());
			_savePlayerTeams.add(_teams.get(teamCount));
			playersCount++;

			if(teamCount == _teams.size() - 1)
				teamCount = 0;
			else
				teamCount++;

			_playersShuffle.remove(playerToAddIndex);
		}
	}

	public static void setUserData()
	{
		for(L2PcInstance player : _players)
		{
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			if(Config.TVT_AURA)
			{
				if(_teams.size() >= 2)
					player.setTeam(_teams.indexOf(player._teamNameTvT) + 1);
			}
			player.broadcastUserInfo();
		}
	}

	public static void finishEvent()
	{
		if(!finishEventOk())
		{
			if(_log.isDebugEnabled())
				_log.debug("TvT Engine[finishEvent]: finishEventOk() = false");
			return;
		}

		_started = false;
		unspawnEventNpc();
		processTopTeam();
		L2PcInstance bestKiller = findBestKiller(_players);
		L2PcInstance looser = findLooser(_players);

		if(_topKills == 0)
			Announcements.getInstance().announceToAll(_eventName + ": No team wins the match(nobody killed).");
		else
		{
			Announcements.getInstance().announceToAll(_eventName + ": " + _topTeam + "'s win the match! " + _topKills + " kills.");
			rewardTeam(_topTeam, bestKiller, looser);
			playKneelAnimation(_topTeam);
		}

		if(Config.TVT_ANNOUNCE_TEAM_STATS)
		{
			Announcements.getInstance().announceToAll(_eventName + " Team Statistics:");
			for(String team : _teams)
			{
				int _kills = teamKillsCount(team);
				Announcements.getInstance().announceToAll("Team: " + team + " - Kills: " + _kills);
			}

			if(bestKiller != null)
			{
				Announcements.getInstance().announceToAll("Top killer: " + bestKiller.getName() + " - Kills: " + bestKiller._countTvTkills);
			}
			if((looser != null) && (!looser.equals(bestKiller)))
			{
				Announcements.getInstance().announceToAll("Top looser: " + looser.getName() + " - Dies: " + looser._countTvTdies);
			}
		}
		teleportFinish();
	}

	// show loosers and winners animations
	public static void playKneelAnimation(String teamName)
	{
		for(L2PcInstance player : _players)
		{
			if(player != null)
			{
				if(!player._teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
				}
				else if(player._teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}

	private static boolean finishEventOk()
	{
		if(!_started)
			return false;
		_inProgress = false;

		if(Config.TVT_CLOSE_FORT_DOORS)
		{
			closeFortDoors();
		}

		return true;
	}

	public static void processTopTeam()
	{
		for(String team : _teams)
		{
			if(teamKillsCount(team) > _topKills)
			{
				_topTeam = team;
				_topKills = teamKillsCount(team);
			}
		}
	}

	public static void rewardTeam(String teamName, L2PcInstance bestKiller, L2PcInstance looser)
	{
		for(L2PcInstance player : _players)
		{
			if((player != null) && (player.isOnline() != 0) && (player._inEventTvT == true) && (player._teamNameTvT.equals(teamName)) && (!player.equals(looser)) && (((player._countTvTkills > 0) || (Config.TVT_PRICE_NO_KILLS))))
			{
				if((bestKiller != null) && (bestKiller.equals(player)))
				{
					player.addItem("TvT Event: " + _eventName, _rewardId, _rewardAmount, player, true);
					player.addItem("TvT Event: " + _eventName, Config.TVT_TOP_KILLER_REWARD, Config.TVT_TOP_KILLER_QTY, player, true);
				}
				else
				{
					player.addItem("TvT Event: " + _eventName, _rewardId, _rewardAmount, player, true);

					NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("");

					replyMSG.append("<html><body>");
					replyMSG.append("<font color=\"FFFF00\">Your team wins the event. Look in your inventory for the reward.</font>");
					replyMSG.append("</body></html>");

					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);

					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(new ActionFailed());
				}
			}
		}
	}

	public static void abortEvent()
	{
		if(!_joining && !_teleport && !_started)
			return;
		if(_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanTvT();
			_joining = false;
			Announcements.getInstance().announceToAll(_eventName + ": Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		_inProgress = false;
		unspawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + ": Match aborted!");
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
		_log.info("");
		_log.info("");

		if(!_joining && !_teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (INACTIVE) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if(_joining && !_teleport && !_started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> TvT Engine infos dump (JOINING) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}
		else if(!_joining && _teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (TELEPORT) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if(!_joining && !_teleport && _started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> TvT Engine infos dump (STARTED) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}

		_log.info("Name: " + _eventName);
		_log.info("Desc: " + _eventDesc);
		_log.info("Join location: " + _joiningLocationName);
		_log.info("Min lvl: " + _minlvl);
		_log.info("Max lvl: " + _maxlvl);
		_log.info("");
		_log.info("##########################");
		_log.info("# _teams(Vector<String>) #");
		_log.info("##########################");

		for(String team : _teams)
			_log.info(team + " Kills Done :" + _teamKillsCount.get(_teams.indexOf(team)));

		if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			_log.info("");
			_log.info("#########################################");
			_log.info("# _playersShuffle(Vector<L2PcInstance>) #");
			_log.info("#########################################");

			for(L2PcInstance player : _playersShuffle)
			{
				if(player != null)
					_log.info("Name: " + player.getName());
			}
		}

		_log.info("");
		_log.info("##################################");
		_log.info("# _players(Vector<L2PcInstance>) #");
		_log.info("##################################");

		for(L2PcInstance player : _players)
		{
			if(player != null)
				_log.info("Name: " + player.getName() + "   Team: " + player._teamNameTvT + "  Kills Done:" + player._countTvTkills);
		}

		_log.info("");
		_log.info("#####################################################################");
		_log.info("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
		_log.info("#####################################################################");

		for(String player : _savePlayers)
			_log.info("Name: " + player + "	Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));

		_log.info("");
		_log.info("");
	}

	public static void loadData()
	{
		_eventName = new String();
		_eventDesc = new String();
		_topTeam = new String();
		_joiningLocationName = new String();
		_teams = new Vector<String>();
		_savePlayers = new Vector<String>();
		_savePlayerTeams = new Vector<String>();
		_players = new Vector<L2PcInstance>();
		_playersShuffle = new Vector<L2PcInstance>();
		_teamPlayersCount = new Vector<Integer>();
		_teamKillsCount = new Vector<Integer>();
		_teamColors = new Vector<Integer>();
		_teamsX = new Vector<Integer>();
		_teamsY = new Vector<Integer>();
		_teamsZ = new Vector<Integer>();
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
		_topKills = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;

		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select * from tvt");
			rs = statement.executeQuery();

			int teams = 0;

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
				teams = rs.getInt("teamsCount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
			}
			statement.close();

			int index = -1;
			if(teams > 0)
				index = 0;
			while(index < teams && index > -1)
			{
				statement = con.prepareStatement("Select * from tvt_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while(rs.next())
				{
					_teams.add(rs.getString("teamName"));
					_teamPlayersCount.add(0);
					_teamKillsCount.add(0);
					_teamColors.add(0);
					_teamsX.add(0);
					_teamsY.add(0);
					_teamsZ.add(0);
					_teamsX.set(index, rs.getInt("teamX"));
					_teamsY.set(index, rs.getInt("teamY"));
					_teamsZ.set(index, rs.getInt("teamZ"));
					_teamColors.set(index, rs.getInt("teamColor"));
				}
				index++;
				statement.close();
			}
		}
		catch(Exception e)
		{
			_log.error("Exception: TvT.loadData(): " + e.getMessage());
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

			statement = con.prepareStatement("Delete from tvt");
			statement.execute();
			statement.close();

			statement = con
					.prepareStatement("INSERT INTO tvt (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
			statement.setInt(13, _teams.size());
			statement.setInt(14, _joinTime);
			statement.setInt(15, _eventTime);
			statement.setInt(16, _minPlayers);
			statement.setInt(17, _maxPlayers);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("Delete from tvt_teams");
			statement.execute();
			statement.close();

			for(String teamName : _teams)
			{
				int index = _teams.indexOf(teamName);

				if(index == -1)
					return;
				statement = con.prepareStatement("INSERT INTO tvt_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, index);
				statement.setString(2, teamName);
				statement.setInt(3, _teamsX.get(index));
				statement.setInt(4, _teamsY.get(index));
				statement.setInt(5, _teamsZ.get(index));
				statement.setInt(6, _teamColors.get(index));
				statement.execute();
				statement.close();
			}
		}
		catch(Exception e)
		{
			_log.error("Exception: TvT.saveData(): " + e.getMessage());
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

			TextBuilder replyMSG = new TextBuilder("<html><title>Team vs Team</title><body>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
			replyMSG.append("<center><font color=\"3366CC\">Current event:</font></center><br1>");
			replyMSG.append("<center>Name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font></center><br1>");
			replyMSG.append("<center>Description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font></center><br><br>");

			if(!_started && !_joining)
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if(!TvT._started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + _playersShuffle.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if(eventPlayer.isCursedWeaponEquiped() && !Config.TVT_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if(!_started && _joining && eventPlayer.getLevel() >= _minlvl && eventPlayer.getLevel() < _maxlvl)
			{
				if(_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if(Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
						replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameTvT + "</font><br><br>");
					else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
						replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + _playersShuffle.size() + "</font></center><br>");

					replyMSG.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
					replyMSG.append("<center><button value=\"Remove\" action=\"bypass -h npc_" + objectId
							+ "_tvt_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
				}
				else
				{
					replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
					replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + _minlvl + "</font></center></td><br>");
					replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font></center></td><br><br>");
					replyMSG.append("<center><font color=\"3366CC\">Teams:</font></center><br>");

					if(Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");

						for(String team : _teams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team
									+ "\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
						}

						replyMSG.append("</table></center>");
					}
					else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center>");

						for(String team : _teams)
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font> &nbsp;</td>");

						replyMSG.append("</center><br>");

						replyMSG.append("<center><button value=\"Join Event\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
						replyMSG.append("<center><font color=\"3366CC\">Teams will be reandomly generated!</font></center><br>");
						replyMSG.append("<center>Joined Players:</font> <font color=\"LEVEL\">" + _playersShuffle.size() + "</center></font><br>");
						replyMSG.append("<center>Reward: <font color=\"LEVEL\">" + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName()+ "</center></font>");
					}
				}
			}
			else if(_started && !_joining)
				replyMSG.append("<center>TvT match is in progress.</center>");
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
			eventPlayer.sendPacket(new ActionFailed());
		}
		catch(Exception e)
		{
			_log.error("TvT Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

	public static void addPlayer(L2PcInstance player, String teamName)
	{
		if(!addPlayerOk(teamName, player))
			return;

		if(Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			player._teamNameTvT = teamName;
			_players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			_playersShuffle.add(player);

		player._inEventTvT = true;
		player._countTvTkills = 0;
	}

	public static void removeOfflinePlayers()
	{
		try
		{
			if(_playersShuffle == null)
				return;
			else if(_playersShuffle.isEmpty())
				return;
			else if(_playersShuffle.size() > 0)
			{
				for(L2PcInstance player : _playersShuffle)
				{
					if(player == null)
						_playersShuffle.remove(player);					
					else if(player.isOnline() == 0 || player.isInJail())
						removePlayer(player);
					if(_playersShuffle.size() == 0 || _playersShuffle.isEmpty())
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

	public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
	{
		try
		{
			for(L2PcInstance player : _playersShuffle)
			{
				if(player == null || player.isOnline() == 0)
				{
					_playersShuffle.remove(player);
					eventPlayer._inEventTvT = false;
					continue;
				}
				else if(player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
				//this 1 is incase player got new objectid after DC or reconnect
				else if(player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
			}
		}
		catch(Exception e)
		{
		}
		return false;
	}

	public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
	{
		if(checkShufflePlayers(eventPlayer) || eventPlayer._inEventTvT)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}

		for(L2PcInstance player : _players)
		{
			if(player.getObjectId() == eventPlayer.getObjectId())
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
			else if(player.getName() == eventPlayer.getName())
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

		if(Config.TVT_EVEN_TEAMS.equals("NO"))
			return true;
		else if(Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;

			for(int playersCount : _teamPlayersCount)
			{
				if(countBefore == -1)
					countBefore = playersCount;

				if(countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}

				countBefore = playersCount;
			}

			if(allTeamsEqual)
				return true;

			countBefore = Integer.MAX_VALUE;

			for(int teamPlayerCount : _teamPlayersCount)
			{
				if(teamPlayerCount < countBefore)
					countBefore = teamPlayerCount;
			}

			Vector<String> joinableTeams = new Vector<String>();

			for(String team : _teams)
			{
				if(teamPlayersCount(team) == countBefore)
					joinableTeams.add(team);
			}

			if(joinableTeams.contains(teamName))
				return true;
		}
		else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			return true;

		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}

	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		if((Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started))
				|| (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started)))
		{
			if(Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
			{
				for(L2Effect e : player.getAllEffects())
				{
					if(e != null)
						e.exit();
				}
			}

			player._teamNameTvT = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			for(L2PcInstance p : _players)
			{
				if(p == null)
				{
					continue;
				}
				//check by name incase player got new objectId
				else if(p.getName().equals(player.getName()))
				{
					player._originalNameColorTvT = player.getAppearance().getNameColor();
					player._originalTitleTvT = player.getTitle();
					player._originalKarmaTvT = player.getKarma();
					player._inEventTvT = true;
					player._countTvTkills = p._countTvTkills;
					_players.remove(p); //removing old object id from vector
					_players.add(player); //adding new objectId to vector
					break;
				}
			}

			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			player.broadcastUserInfo();
			if(Config.TVT_AURA)
			{
				if(_teams.size() >= 2)
					player.setTeam(_teams.indexOf(player._teamNameTvT) + 1);
			}
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams
					.indexOf(player._teamNameTvT)));
		}
	}

	public static void removePlayer(L2PcInstance player)
	{
		if(player._inEventTvT)
		{
			if(!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorTvT);
				player.setTitle(player._originalTitleTvT);
				player.setKarma(player._originalKarmaTvT);
				if(Config.TVT_AURA)
				{
					if(_teams.size() >= 2)
						player.setTeam(0);// clear aura :P
				}
				player.broadcastUserInfo();
			}
			player._teamNameTvT = new String();
			player._countTvTkills = 0;
			player._inEventTvT = false;

			if((Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
			{
				setTeamPlayersCount(player._teamNameTvT, teamPlayersCount(player._teamNameTvT) - 1);
				_players.remove(player);
			}
			else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
				_playersShuffle.remove(player);
		}
	}

	public static void cleanTvT()
	{
		for(L2PcInstance player : _players)
		{
			if(player != null)
			{
				removePlayer(player);
				if(_savePlayers.contains(player.getName()))
					_savePlayers.remove(player.getName());
				player._inEventTvT = false;
			}
		}
		if(_playersShuffle != null && !_playersShuffle.isEmpty())
		{
			for(L2PcInstance player : _playersShuffle)
			{
				if(player != null)
					player._inEventTvT = false;
			}
		}
		for(String team : _teams)
		{
			int index = _teams.indexOf(team);

			_teamPlayersCount.set(index, 0);
			_teamKillsCount.set(index, 0);
		}

		_topKills = 0;
		_topTeam = new String();
		_players = new Vector<L2PcInstance>();
		_playersShuffle = new Vector<L2PcInstance>();
		_savePlayers = new Vector<String>();
		_savePlayerTeams = new Vector<String>();

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
		Announcements.getInstance().announceToAll(_eventName + ": Teleport back to participation NPC in 20 seconds!");

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
				cleanTvT();
			}
		}, 20000);
	}

	public static L2PcInstance findBestKiller(Vector<L2PcInstance> players)
	{
		if(players == null)
		{
			return null;
		}
		L2PcInstance bestKiller = null;
		for(L2PcInstance player : players)
		{
			if((bestKiller == null) || (bestKiller._countTvTkills < player._countTvTkills))
				bestKiller = player;
		}
		return bestKiller;
	}

	public static L2PcInstance findLooser(Vector<L2PcInstance> players) 
	{
		if(players == null)
		{
			return null;
		}
		L2PcInstance looser = null;
		for(L2PcInstance player : players)
		{
			if((looser == null) || (looser._countTvTdies < player._countTvTdies))
				looser = player;
		}
		return looser;
	}

	public static int teamKillsCount(String teamName)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return -1;

		return _teamKillsCount.get(index);
	}

	public static void setTeamKillsCount(String teamName, int teamKillsCount)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamKillsCount.set(index, teamKillsCount);
	}

	public static int teamPlayersCount(String teamName)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return -1;

		return _teamPlayersCount.get(index);
	}

	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		int index = _teams.indexOf(teamName);

		if(index == -1)
			return;

		_teamPlayersCount.set(index, teamPlayersCount);
	}

	public static Vector<TvTTeam> getSotedTvTTeams()
	{
		Vector tvtTeams = new Vector();
		for(String name : _teams) 
		{
			tvtTeams.add(new TvTTeam(name, teamKillsCount(name)));
		}
		for(int i = 0; i < tvtTeams.size(); ++i)
		for(int j = tvtTeams.size() - 1; j >= i + 1; --j)
		if(((TvTTeam)tvtTeams.get(j)).killCount > ((TvTTeam)tvtTeams.get(j - 1)).killCount)
		{
			TvTTeam tmp = (TvTTeam)tvtTeams.get(j);
			tvtTeams.set(j, tvtTeams.get(j - 1));
			tvtTeams.set(j - 1, tmp);
		}
		return tvtTeams;
	}

	public static class TvTTeam
	{
		private int killCount = -1;
		private String name = null;

		TvTTeam(String name, int killCount)
		{
			this.killCount = killCount;
			this.name = name;
		}
		public int getKillCount()
		{
			return this.killCount;
		}
		public void setKillCount(int killCount)
		{
			this.killCount = killCount;
		}
		public String getName()
		{
			return this.name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
	}

	private static  boolean  _inProgress  = false;

	private static void closeFortDoors()
	{
		DoorTable.getInstance().getDoor(23170004).closeMe();
		DoorTable.getInstance().getDoor(23170005).closeMe();
		DoorTable.getInstance().getDoor(23170002).closeMe();
		DoorTable.getInstance().getDoor(23170003).closeMe();
		DoorTable.getInstance().getDoor(23170006).closeMe();
		DoorTable.getInstance().getDoor(23170007).closeMe();
		DoorTable.getInstance().getDoor(23170008).closeMe();
		DoorTable.getInstance().getDoor(23170009).closeMe();
		DoorTable.getInstance().getDoor(23170010).closeMe();
		DoorTable.getInstance().getDoor(23170011).closeMe();

		try
		{
			Thread.sleep(20);
		}
		catch(InterruptedException ie)
		{
			_log.fatal("Error, " + ie.getMessage());
		}
	}

	private static void openFortDoors()
	{
		DoorTable.getInstance().getDoor(23170004).openMe();
		DoorTable.getInstance().getDoor(23170005).openMe();
		DoorTable.getInstance().getDoor(23170002).openMe();
		DoorTable.getInstance().getDoor(23170003).openMe();
		DoorTable.getInstance().getDoor(23170006).openMe();
		DoorTable.getInstance().getDoor(23170007).openMe();
		DoorTable.getInstance().getDoor(23170008).openMe();
		DoorTable.getInstance().getDoor(23170009).openMe();
		DoorTable.getInstance().getDoor(23170010).openMe();
		DoorTable.getInstance().getDoor(23170011).openMe();

	}
}