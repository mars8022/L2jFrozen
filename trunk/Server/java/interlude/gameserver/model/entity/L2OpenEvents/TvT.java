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
/**
 *
 * @author FBIagent / fixed and moded for l2jfree by SqueezeD
 *
 */
package interlude.gameserver.model.entity.L2OpenEvents;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.Announcements;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.lib.Rnd;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Party;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2Summon;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PetInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TvT
{
	private final static Log _log = LogFactory.getLog(TvT.class.getName());
	public static String _eventName = new String(), _eventDesc = new String(), _topTeam = new String(), _joiningLocationName = new String();
	public static Vector<String> _teams = new Vector<String>(), _savePlayers = new Vector<String>(), _savePlayerTeams = new Vector<String>();
	public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>(), _playersShuffle = new Vector<L2PcInstance>();
	public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(), _teamKillsCount = new Vector<Integer>(), _teamColors = new Vector<Integer>(), _teamsX = new Vector<Integer>(), _teamsY = new Vector<Integer>(), _teamsZ = new Vector<Integer>();
	public static boolean _joining = false, _teleport = false, _started = false, _sitForced = false;
	public static L2Spawn _npcSpawn;
	public static int _npcId = 0, _npcX = 0, _npcY = 0, _npcZ = 0, _npcHeading = 0, _rewardId = 0, _rewardAmount = 0, _topKills = 0, _minlvl = 0, _maxlvl = 0, _joinTime = 0, _eventTime = 0, _minPlayers = 0, _maxPlayers = 0;

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
		if (!checkTeamOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
			}
			return;
		}
		if (teamName.equals(" ")) {
			return;
		}
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
		if (_minlvl >= maxlvl) {
			return false;
		}
		return true;
	}

	public static boolean checkMinLevel(int minlvl)
	{
		if (_maxlvl <= minlvl) {
			return false;
		}
		return true;
	}

	/** returns true if participated players is higher or equal then minimum needed players */
	public static boolean checkMinPlayers(int players)
	{
		if (_minPlayers <= players) {
			return true;
		}
		return false;
	}

	/** returns true if max players is higher or equal then participated players */
	public static boolean checkMaxPlayers(int players)
	{
		if (_maxPlayers > players) {
			return true;
		}
		return false;
	}

	public static void removeTeam(String teamName)
	{
		if (!checkTeamOk() || _teams.isEmpty())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
			}
			return;
		}
		if (teamPlayersCount(teamName) > 0)
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
			}
			return;
		}
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return;
		}
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
		if (index == -1) {
			return;
		}
		_teamsX.set(index, activeChar.getX());
		_teamsY.set(index, activeChar.getY());
		_teamsZ.set(index, activeChar.getZ());
	}

	public static void setTeamPos(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return;
		}
		_teamsX.set(index, x);
		_teamsY.set(index, y);
		_teamsZ.set(index, z);
	}

	public static void setTeamColor(String teamName, int color)
	{
		if (!checkTeamOk()) {
			return;
		}
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return;
		}
		_teamColors.set(index, color);
	}

	public static boolean checkTeamOk()
	{
		if (_started || _teleport || _joining) {
			return false;
		}
		return true;
	}

	public static void startJoin(L2PcInstance activeChar)
	{
		if (!startJoinOk())
		{
			activeChar.sendMessage("Event not setted propertly.");
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			}
			return;
		}
		_joining = true;
		spawnEventNpc(activeChar);
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
	}

	public static void startJoin()
	{
		if (!startJoinOk())
		{
			_log.warn("Event not setted propertly.");
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[startJoin(startJoinOk() = false");
			}
			return;
		}
		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
	}

	public static boolean startAutoJoin()
	{
		if (!startJoinOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[startJoin]: startJoinOk() = false");
			}
			return false;
		}
		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
		return true;
	}

	public static boolean startJoinOk()
	{
		if (_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") || _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 || _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 || _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0)) {
			return false;
		}
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
		catch (Exception e)
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
		catch (Exception e)
		{
			_log.error("TvT Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}

	public static void teleportStart()
	{
		if (!_joining || _started || _teleport) {
			return;
		}
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements.getInstance().announceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return;
		}
		_joining = false;
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Teleport to team spot in 20 seconds!");
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				TvT.sit();
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (Config.TVT_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if (player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for (L2Effect e : summon.getAllEffects()) {
									if (e != null) {
										e.exit();
									}
								}
								if (summon instanceof L2PetInstance) {
									summon.unSummon(player);
								}
							}
						}
						if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (L2Effect e : player.getAllEffects())
							{
								if (e != null) {
									e.exit();
								}
							}
						}
						// Remove player from his party
						if (player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
	}

	public static boolean teleportAutoStart()
	{
		if (!_joining || _started || _teleport) {
			return false;
		}
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements.getInstance().announceToAll("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return false;
		}
		_joining = false;
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Teleport to team spot in 20 seconds!");
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				TvT.sit();
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (Config.TVT_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if (player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for (L2Effect e : summon.getAllEffects()) {
									if (e != null) {
										e.exit();
									}
								}
								if (summon instanceof L2PetInstance) {
									summon.unSummon(player);
								}
							}
						}
						if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (L2Effect e : player.getAllEffects())
							{
								if (e != null) {
									e.exit();
								}
							}
						}
						// Remove player from his party
						if (player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
	}

	public static void startEvent(L2PcInstance activeChar)
	{
		if (_inProgress)
		{
			activeChar.sendMessage("A TvT event is already in progress, try abort.");
			return;
		}
		if (!startEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			}
			return;
		}
		_teleport = false;
		sit();
		
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			closeColiseumDoors();

		Announcements.getInstance().announceToAll(_eventName + "(TvT): Started. Go to kill your enemies!");
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
		if (!startEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[startEvent]: startEventOk() = false");
			}
			return false;
		}
		_teleport = false;
		sit();
		
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			closeColiseumDoors();
		
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Started. Go to kill your enemies!");
		_started = true;
		return true;
	}

	public static synchronized void autoEvent()
	{
		_log.info("Starting TvT!");
		_log.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatchs() + " Minutes.");
		if (startAutoJoin())
		{
			_eventType = 2;

			if (_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if (_joinTime <= 0)
			{
				_log.info("TvT: join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (teleportAutoStart())
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startAutoEvent())
				{
					_log.debug("TvT: waiting.....minutes for event time " + TvT._eventTime);

					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();

					_log.info("TvT: waiting... delay for final messages ");
					waiter(60000);//just a give a delay delay for final messages
					sendFinalMessages();

					if (_finished && _eventType == 2)
						_log.info("TVT: waiting.....delay for restart event  " + TvT.getIntervalBetweenMatchs() + " minutes.");
					waiter(60000);//just a give a delay to next restart

					try
					{
						restartEvent();
					}
					catch (Exception e)
					{
						_log.error("Error while tying to Restart Event", e);
						e.printStackTrace();
					}
				}
			}
			else if (!teleportAutoStart())
			{
				abortEvent();
				restartEvent();
			}
		}
	}

	private static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		while (startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--; // here because we don't want to see two time announce at the same time
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if (_started) {
							Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 / 60 + " hour(s) till event finish!");
						}
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						if (_joining)
						{
							removeOfflinePlayers();
							Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 + " minute(s) till registration close!");
						}
						else if (_started) {
							Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 + " minute(s) till event finish!");
						}
						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 5: // 5 seconds left
					case 4: // 4 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						if (_joining) {
							Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) till registration close!");
						} else if (_teleport) {
							Announcements.getInstance().announceToAll("TvT Event: " + seconds + " seconds(s) till start fight!");
						} else if (_started) {
							Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) till event finish!");
						}
						break;
				}
			}
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}

	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started) {
			return false;
		}
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			if (_teamPlayersCount.contains(0)) {
				return false;
			}
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
			int loopCount = 0;
			loopCount = _playersShuffle.size();
			for (int i = 0; i < loopCount; i++)
			{
				if (_playersShuffle != null) {
					playersShuffleTemp.add(_playersShuffle.get(i));
				}
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
		for (;;)
		{
			if (_playersShuffle.isEmpty()) {
				break;
			}
			int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
			L2PcInstance player = null;
			player = _playersShuffle.get(playerToAddIndex);
			player._originalNameColorTvT = player.getAppearance().getNameColor();
			player._originalKarmaTvT = player.getKarma();
			_players.add(player);
			_players.get(playersCount)._teamNameTvT = _teams.get(teamCount);
			_savePlayers.add(_players.get(playersCount).getName());
			_savePlayerTeams.add(_teams.get(teamCount));
			playersCount++;
			if (teamCount == _teams.size() - 1) {
				teamCount = 0;
			} else {
				teamCount++;
			}
			_playersShuffle.remove(playerToAddIndex);
		}
	}

	public static void setUserData()
	{
		for (L2PcInstance player : _players)
		{
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			player.broadcastUserInfo();
		}
	}

	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("TvT Engine[finishEvent]: finishEventOk() = false");
			}
			return;
		}
		_started = false;
		unspawnEventNpc();
		processTopTeam();
		if (_topKills == 0) {
			Announcements.getInstance().announceToAll(_eventName + "(TvT): No team wins the match(nobody killed).");
		} else
		{
			Announcements.getInstance().announceToAll(_eventName + "(TvT): " + _topTeam + "'s win the match! " + _topKills + " kills.");
			rewardTeam(_topTeam);
			playKneelAnimation(_topTeam);
		}
		if (Config.TVT_ANNOUNCE_TEAM_STATS)
		{
			Announcements.getInstance().announceToAll(_eventName + " Team Statistics:");
			for (String team : _teams)
			{
				int _kills = teamKillsCount(team);
				Announcements.getInstance().announceToAll("Team: " + team + " - Kills: " + _kills);
			}
		}
		teleportFinish();
	}

	// show loosers and winners animations
	public static void playKneelAnimation(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (!player._teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
				}
				else if (player._teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}

	private static boolean finishEventOk()
	{
		if (!_started)
			return false;

		_inProgress = false;
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			openColiseumDoors();

		return true;
	}

	public static void processTopTeam()
	{
		for (String team : _teams)
		{
			if (teamKillsCount(team) > _topKills)
			{
				_topTeam = team;
				_topKills = teamKillsCount(team);
			}
		}
	}

	public static void rewardTeam(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (player._teamNameTvT.equals(teamName) && (player._countTvTkills > 0 || Config.TVT_PRICE_NO_KILLS))
				{
					player.addItem("TvT Event: " + _eventName, _rewardId, _rewardAmount, player, true);
					NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("");
					replyMSG.append("<html><body>Your team wins the event. Look in your inventory for the reward.</body></html>");
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}

	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started) {
			return;
		}
		if (_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanTvT();
			_joining = false;
			Announcements.getInstance().announceToAll(_eventName + "(TvT): Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		_inProgress = false;
		unspawnEventNpc();
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Match aborted!");
		teleportFinish();
	}

	public static void sit()
	{
		if (_sitForced) {
			_sitForced = false;
		} else {
			_sitForced = true;
		}
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();
					if (!player.isSitting()) {
						player.sitDown();
					}
				}
				else
				{
					if (player.isSitting()) {
						player.standUp();
					}
				}
			}
		}
	}

	public static void dumpData()
	{
		_log.info("");
		_log.info("");
		if (!_joining && !_teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (INACTIVE) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (_joining && !_teleport && !_started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> TvT Engine infos dump (JOINING) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}
		else if (!_joining && _teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (TELEPORT) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (!_joining && !_teleport && _started)
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
		for (String team : _teams) {
			_log.info(team + " Kills Done :" + _teamKillsCount.get(_teams.indexOf(team)));
		}
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			_log.info("");
			_log.info("#########################################");
			_log.info("# _playersShuffle(Vector<L2PcInstance>) #");
			_log.info("#########################################");
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null) {
					_log.info("Name: " + player.getName());
				}
			}
		}
		_log.info("");
		_log.info("##################################");
		_log.info("# _players(Vector<L2PcInstance>) #");
		_log.info("##################################");
		for (L2PcInstance player : _players)
		{
			if (player != null) {
				_log.info("Name: " + player.getName() + "   Team: " + player._teamNameTvT + "  Kills Done:" + player._countTvTkills);
			}
		}
		_log.info("");
		_log.info("#####################################################################");
		_log.info("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
		_log.info("#####################################################################");
		for (String player : _savePlayers) {
			_log.info("Name: " + player + "	Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
		}
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
		_intervalBetweenMatchs = 0;
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Select * from tvt");
			rs = statement.executeQuery();
			int teams = 0;
			while (rs.next())
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
				_intervalBetweenMatchs = rs.getLong("delayForNextEvent");
			}
			statement.close();
			int index = -1;
			if (teams > 0) {
				index = 0;
			}
			while (index < teams && index > -1)
			{
				statement = con.prepareStatement("Select * from tvt_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
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
		catch (Exception e)
		{
			_log.error("Exception: TvT.loadData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
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
			statement = con.prepareStatement("INSERT INTO tvt (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers, delayForNextEvent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
			statement.setLong(18, _intervalBetweenMatchs);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("Delete from tvt_teams");
			statement.execute();
			statement.close();
			for (String teamName : _teams)
			{
				int index = _teams.indexOf(teamName);
				if (index == -1) {
					return;
				}
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
		catch (Exception e)
		{
			_log.error("Exception: TvT.saveData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
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
			replyMSG.append("TvT Match<br><br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("	... name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
			replyMSG.append("	... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br><br>");
			if (!_started && !_joining) {
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			} else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!TvT._started)
				{
					replyMSG.append("Currently participated : <font color=\"00FF00\">" + _playersShuffle.size() + ".</font><br>");
					replyMSG.append("Admin set max players : <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.TVT_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if (!_started && _joining && eventPlayer.getLevel() >= _minlvl && eventPlayer.getLevel() <= _maxlvl)
			{
				if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) {
						replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameTvT + "</font><br><br>");
					} else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE")) {
						replyMSG.append("You participated already!<br><br>");
					}
					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait till event start or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_tvt_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					replyMSG.append("<td width=\"200\">Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						for (String team : _teams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
						}
						replyMSG.append("</table></center>");
					}
					else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						for (String team : _teams) {
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
						}
						replyMSG.append("</table></center><br>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("Teams will be reandomly generated!");
					}
				}
			}
			else if (_started && !_joining) {
				replyMSG.append("<center>TvT match is in progress.</center>");
			} else if (eventPlayer.getLevel() < _minlvl || eventPlayer.getLevel() > _maxlvl)
			{
				replyMSG.append("Your lvl : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
				replyMSG.append("Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (Exception e)
		{
			_log.error("TvT Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

	public static void addPlayer(L2PcInstance player, String teamName)
	{
		if (!addPlayerOk(teamName, player)) {
			return;
		}
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			player._teamNameTvT = teamName;
			_players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE")) {
			_playersShuffle.add(player);
		}
		player._inEventTvT = true;
		player._countTvTkills = 0;
	}

	public static void removeOfflinePlayers()
	{
		try
		{
			if (_playersShuffle == null || _playersShuffle.isEmpty()) {
				return;
			}
			if (_playersShuffle != null && !_playersShuffle.isEmpty())
			{
				for (L2PcInstance player : _playersShuffle)
				{
					if (player == null || player.isOnline() == 0) {
						_playersShuffle.remove(player);
					}
				}
			}
		}
		catch (Exception e)
		{
			return;
		}
	}

	public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
	{
		try
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player == null  || player.isOnline() == 0 )
				{
					_playersShuffle.remove(player);
					continue;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
				// this 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}

	public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
	{
		if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventTvT)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		if (eventPlayer._inEventFOS || eventPlayer._inEventCTF || eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		for (L2PcInstance player : _players)
		{
			if (player.getObjectId() == eventPlayer.getObjectId())
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
			else if (player.getName() == eventPlayer.getName())
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
		}
		if (_players.contains(eventPlayer))
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		if (CTF._savePlayers.contains(eventPlayer.getName()))
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		if (Config.TVT_EVEN_TEAMS.equals("NO")) {
			return true;
		} else if (Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			for (int playersCount : _teamPlayersCount)
			{
				if (countBefore == -1) {
					countBefore = playersCount;
				}
				if (countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}
				countBefore = playersCount;
			}
			if (allTeamsEqual) {
				return true;
			}
			countBefore = Integer.MAX_VALUE;
			for (int teamPlayerCount : _teamPlayersCount)
			{
				if (teamPlayerCount < countBefore) {
					countBefore = teamPlayerCount;
				}
			}
			Vector<String> joinableTeams = new Vector<String>();
			for (String team : _teams)
			{
				if (teamPlayersCount(team) == countBefore) {
					joinableTeams.add(team);
				}
			}
			if (joinableTeams.contains(teamName)) {
				return true;
			}
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE")) {
			return true;
		}
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}

	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started) || Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started))
		{
			if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
			{
				for (L2Effect e : player.getAllEffects())
				{
					if (e != null) {
						e.exit();
					}
				}
			}
			player._teamNameTvT = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			if (!_players.contains(player.getName())) {
				_players.add(player);
			}
			player._originalNameColorTvT = player.getAppearance().getNameColor();
			player._originalKarmaTvT = player.getKarma();
			player._inEventTvT = true;
			player._countTvTkills = 0;
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			player.broadcastUserInfo();
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
		}
	}

	public static void removePlayer(L2PcInstance player)
	{
		if (player._inEventTvT)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorTvT);
				player.setKarma(player._originalKarmaTvT);
				player.broadcastUserInfo();
			}
			player._teamNameTvT = new String();
			player._countTvTkills = 0;
			player._inEventTvT = false;
			if ((Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
			{
				setTeamPlayersCount(player._teamNameTvT, teamPlayersCount(player._teamNameTvT) - 1);
				_players.remove(player);
			}
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !_playersShuffle.isEmpty() && _playersShuffle.contains(player)) {
				_playersShuffle.remove(player);
			}
		}
	}

	public static void cleanTvT()
	{
		_log.info("TvT : Cleaning players.");
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				removePlayer(player);
				if (_savePlayers.contains(player.getName())) {
					_savePlayers.remove(player.getName());
				}
				player._inEventTvT = false;
			}
		}
		if (_playersShuffle != null && !_playersShuffle.isEmpty())
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null) {
					player._inEventTvT = false;
				}
			}
		}
		_log.info("TvT : Cleaning teams.");
		for (String team : _teams)
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
		_log.info("Cleaning TvT done.");
	}

	public static void unspawnEventNpc()
	{
		if (_npcSpawn == null) {
			return;
		}
		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}

	public static void teleportFinish()
	{
		Announcements.getInstance().announceToAll(_eventName + "(TvT): Teleport back to participation NPC in 20 seconds!");
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				for (L2PcInstance player : _players)
				{
					if (player != null) {
						player.teleToLocation(_npcX, _npcY, _npcZ);
					}
				}
				cleanTvT();
			}
		}, 20000);
	}

	public static int teamKillsCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return -1;
		}
		return _teamKillsCount.get(index);
	}

	public static void setTeamKillsCount(String teamName, int teamKillsCount)
	{
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return;
		}
		_teamKillsCount.set(index, teamKillsCount);
	}

	public static int teamPlayersCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return -1;
		}
		return _teamPlayersCount.get(index);
	}

	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		int index = _teams.indexOf(teamName);
		if (index == -1) {
			return;
		}
		_teamPlayersCount.set(index, teamPlayersCount);
	}
	public static long _intervalBetweenMatchs = 0;

	/**
	 * The type of TvT Event
	 * 1 = Manual
	 * 2 = Automatic
	 */
	public static int _eventType = 0;

	private static boolean _inProgress = false;
	private static boolean _finished = false;
	private static boolean _aborted	= false;

	/**
	 * Opens All Coliseum Doors
	 */
	private static void closeColiseumDoors()
	{
		Announcements.getInstance().announceToAll("Closing Coliseum Doors, TvT event has just started !");
		DoorTable.getInstance().getDoor(24190001).closeMe();//west gate out
		DoorTable.getInstance().getDoor(24190002).closeMe();//west gate in
		DoorTable.getInstance().getDoor(24190003).closeMe();//east gate out
		DoorTable.getInstance().getDoor(24190004).closeMe();//east gate in

		try
		{
			//just to give a lil delay :P
			Thread.sleep(20);
		}
		catch (InterruptedException ie)
		{
			_log.fatal("Error, " + ie.getMessage());
		}
	}

	/**
	 * Open all Coliseum Doors
	 */
	private static void openColiseumDoors()
	{
		Announcements.getInstance().announceToAll("Opening Coliseum Doors, TvT event has finished!");
		DoorTable.getInstance().getDoor(24190001).openMe();
		DoorTable.getInstance().getDoor(24190002).openMe();
		DoorTable.getInstance().getDoor(24190003).openMe();
		DoorTable.getInstance().getDoor(24190004).openMe();

	}

	/**
	 * Restarts Event
	 * checks if event was aborted. and if true cancels restart task
	 */
	public synchronized static void restartEvent()
	{
		if (_aborted)
		{
			_log.debug("TvT: restart skipped, event was aborted.");
			return;
		}
		_log.info("TvT: Event has been restarted...");
		_joining = false;
		_started = false;
		_finished = false;
		_inProgress = false;
		long delay = _intervalBetweenMatchs;

		Announcements.getInstance().announceToAll("TvT: joining period will be avaible again in " + getIntervalBetweenMatchs() + " minute(s)!");

		waiter(delay);

		try
		{
			autoEvent(); //start a new event
		}
		catch (Exception e)
		{
			_log.fatal("TvT: Error While Trying to restart Event...", e);
			e.printStackTrace();
		}
	}

	/**
	 * Returns the event type by name.
	 * @param value
	 * @return
	 */
	public static String getEventTypeByName(int value)
	{
		String type = String.valueOf(value);

		switch (value)
		{
		case 0:
			type = ("None");
			break;

		case 1:
			type = ("Manual");
			break;

		case 2:
			type = ("Automatic");
			break;
		}
		return type;
	}

	/**
	 * just an announcer to send termination messages
	 *
	 */
	public static void sendFinalMessages()
	{
		if (_finished && !_aborted)
			Announcements.getInstance().announceToAll("TvT: Thank you For Participating At, " + "TVT Event.");
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
}