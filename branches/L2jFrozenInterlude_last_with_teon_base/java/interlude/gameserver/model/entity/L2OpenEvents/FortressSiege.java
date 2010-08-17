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
 * @author FBIagent / fixed and moded for l2jfree by SqueezeD & Darki699
 *
 */

package interlude.gameserver.model.entity.L2OpenEvents;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlEvent;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.handler.admincommandhandlers.AdminFortressSiegeEngine;
import interlude.gameserver.lib.Rnd;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Party;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2Summon;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PetInstance;
import interlude.gameserver.model.item.PcInventory;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.CreatureSay;
import interlude.gameserver.network.serverpackets.ItemList;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.RelationChanged;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fortress Siege Event
 * @author Darki699
 * @comment: So many fortresses, I hate to see them go to waste ;]
 */
public class FortressSiege
{
	private final static Log _log = LogFactory.getLog(FortressSiege.class.getName());
	public static boolean 					_joining = false,
											_teleport = false,
											_started = false,
											_sitForced = false;
	public static int 						_FlagNPC = 35062,
											_npcId = 0,
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
											_flagX = 0,
											_flagY = 0,
											_flagZ = 0,
											_topScore = 0,
											eventCenterX=0,
											eventCenterY=0,
											eventCenterZ=0,
											_door[] = new int[6];
	public static String 					_eventName = new String(),
						 					_eventDesc = new String(),
						 					_joiningLocationName = new String(),
											_topTeam = new String();
	public static Vector<Integer> 			_teamPlayersCount = new Vector<Integer>(),
								  			_teamColors = new Vector<Integer>(2),
								  			_teamsX = new Vector<Integer>(2),
								  			_teamsY = new Vector<Integer>(2),
								  			_teamsZ = new Vector<Integer>(2),
								  			_teamPointsCount = new Vector<Integer>(2);
	public static Vector<String> 			_teams = new Vector<String>(2),
											_savePlayers = new Vector<String>(),
											_savePlayerTeams = new Vector<String>();
	public static Vector<L2PcInstance> 		_players = new Vector<L2PcInstance>(),
											_playersShuffle = new Vector<L2PcInstance>();
	public static L2Spawn 					_npcSpawn,
											_flagSpawn;
	/** Constructor for FortressSiege Class. Calls an Immediate wipe of all Siege data.*/
	FortressSiege(){
		resetData();
	}

	/**
	 * Resets ALL The Event Siege data.
	 **/
	public static void resetData()
	{
		healDoors();
		closeDoors();
		eventCenterX=0;
		eventCenterY=0;
		eventCenterZ=0;
		_topTeam = new String();
		_door = new int[6];
		_eventName = new String();
		_eventDesc = new String();
		_joiningLocationName = new String();
		_teams = new Vector<String>();
		_teams.add("");
		_teams.add("");
		_savePlayers = new Vector<String>();
		_savePlayerTeams = new Vector<String>();

		if (_players != null && !_players.isEmpty())
		{
			for (L2PcInstance player: _players)
			{
				if (player == null) {
					continue;
				}
				player._countFOSKills=0;
				removeSealOfRuler(player);
				player._inEventFOS=false;
				if (player.getKarma()== player._originalKarmaFOS) {
					player.setKarma(0);
				} else {
					player.setKarma(player._originalKarmaFOS);
				}
				if (player.getAppearance().getNameColor() ==  player._originalNameColorFOS) {
					player.getAppearance().setNameColor(0xFFFFFF);
				} else {
					player.getAppearance().setNameColor(player._originalNameColorFOS);
				}
				player.setSiegeState((byte)0);
				player.sendPacket(new UserInfo(player));
				for (L2PcInstance p : _players) {
					p.sendPacket(new RelationChanged(player, player.getRelation(p), player.isAutoAttackable(p)));
				}
				player.broadcastUserInfo();
			}
		}

		if (_playersShuffle!=null && !_playersShuffle.isEmpty())
		{
			for (L2PcInstance player: _playersShuffle)
			{
				if (player == null) {
					continue;
				}
				player._countFOSKills=0;
				player._inEventFOS=false;
			}
		}

		_players = new Vector<L2PcInstance>();
		_playersShuffle = new Vector<L2PcInstance>();
		_teamPlayersCount = new Vector<Integer>();
		_teamPlayersCount.add(0);
		_teamPlayersCount.add(0);
		_teamPointsCount = new Vector<Integer>();
		_teamPointsCount.add(0);
		_teamPointsCount.add(0);
		_teamColors = new Vector<Integer>();
		_teamColors.add(0xffffff);
		_teamColors.add(0xffffff);
		_teamsX = new Vector<Integer>();
		_teamsX.add(0);
		_teamsX.add(0);
		_teamsY = new Vector<Integer>();
		_teamsY.add(0);
		_teamsY.add(0);
		_teamsZ = new Vector<Integer>();
		_teamsZ.add(0);
		_teamsZ.add(0);
		_flagSpawn = null;
		_flagX = 0;
		_flagY = 0;
		_flagZ = 0;
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
		_topScore = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
	}

	public static void autoLoadData()
	{
		AdminFortressSiegeEngine a = new AdminFortressSiegeEngine();
		a.showSiegeLoadPage(null,true);
	}

	/**
	 * Loads Fortress Siege data from MySql, depending on which siege we want
	 * @param fortressSiege String containing the String siege name which will be loaded
	 */
	public static void loadData(String fortressSiege)
	{
		resetData();
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Select * from fortress_siege");
			rs = statement.executeQuery();
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
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
				eventCenterX = rs.getInt("centerX");
				eventCenterY = rs.getInt("centerY");
				eventCenterZ = rs.getInt("centerZ");
				_teams.set(0,rs.getString("team1Name"));
				_teamsX.set(0,rs.getInt("team1X"));
				_teamsY.set(0,rs.getInt("team1Y"));
				_teamsZ.set(0,rs.getInt("team1Z"));
				_teamColors.set(0,rs.getInt("team1Color"));
				_teams.set(1,rs.getString("team2Name"));
				_teamsX.set(1,rs.getInt("team2X"));
				_teamsY.set(1,rs.getInt("team2Y"));
				_teamsZ.set(1,rs.getInt("team2Z"));
				_teamColors.set(1,rs.getInt("team2Color"));
				_flagX = rs.getInt("flagX");
				_flagY = rs.getInt("flagY");
				_flagZ = rs.getInt("flagZ");
				_door[0] = rs.getInt("innerDoor1");
				_door[1] = rs.getInt("innerDoor2");
				_door[2] = rs.getInt("innerDoor3");
				_door[3] = rs.getInt("innerDoor4");
				_door[4] = rs.getInt("outerDoor1");
				_door[5] = rs.getInt("outerDoor2");
				if(_eventName.equalsIgnoreCase(fortressSiege)) {
					break;
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: FortressSiege.loadData(): " + e.toString());
		}
		finally {try { con.close(); } catch (Exception e) {}}
	}

	/** Saves Fortress Siege Data to MySQL*/
	public static void saveData()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO fortress_siege (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, joinTime, eventTime, minPlayers, maxPlayers, centerX, centerY, centerZ, team1Name, team1X, team1Y, team1Z, team1Color, team2Name, team2X, team2Y, team2Z, team2Color, flagX, flagY, flagZ, innerDoor1, innerDoor2, innerDoor3, innerDoor4, outerDoor1, outerDoor2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
			statement.setInt(17, eventCenterX);
			statement.setInt(18, eventCenterY);
			statement.setInt(19, eventCenterZ);
			statement.setString(20, _teams.get(0));
			statement.setInt(21, _teamsX.get(0));
			statement.setInt(22, _teamsY.get(0));
			statement.setInt(23, _teamsZ.get(0));
			statement.setInt(24, _teamColors.get(0));
			statement.setString(25, _teams.get(1));
			statement.setInt(26, _teamsX.get(1));
			statement.setInt(27, _teamsY.get(1));
			statement.setInt(28, _teamsZ.get(1));
			statement.setInt(29, _teamColors.get(1));
			statement.setInt(30, _flagX);
			statement.setInt(31, _flagY);
			statement.setInt(32, _flagZ);
			statement.setInt(33, _door[0]);
			statement.setInt(34, _door[1]);
			statement.setInt(35, _door[2]);
			statement.setInt(36, _door[3]);
			statement.setInt(37, _door[4]);
			statement.setInt(38, _door[5]);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: FortressSiege.saveData(): " + e.getMessage());
		}
		finally {try { con.close(); } catch (Exception e) {}}
	}

	/** Checks if Minimum level does'nt exceed Maximum level, so Max lvl should be set first*/
	public static boolean checkMinLevel(int minlvl)
	{
		return _maxlvl >= minlvl;
	}

	/** Checks if Maximum Level is Greater than or Equal to Minimum Level*/
	public static boolean checkMaxLevel(int maxlvl)
	{
		return _minlvl <= maxlvl;
	}

	/**
	 * Sets team position to the player position.
	 * @param teamName - String name of the team who's positions are set.
	 * @param activeChar - The L2PcInstance that sets the position.
	 */
	public static void setTeamPos(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);
		if (index > -1)
		{
			_teamsX.set(index, activeChar.getX());
			_teamsY.set(index, activeChar.getY());
			_teamsZ.set(index, activeChar.getZ());
		} else {
			activeChar.sendMessage("No such team name.");
		}
	}

	public static void setTeamColor(String teamName, int color)
	{
		int index = _teams.indexOf(teamName);
		if (index > -1) {
			_teamColors.set(index, color);
		}
	}

	public static void dumpData()
	{
		_log.info("");
		if (!_joining && !_teleport && !_started)
		{
			_log.info("<<--------------------------------------------->>");
			_log.info(">> Fortress Siege Engine infos dump (INACTIVE) <<");
			_log.info("<<-----^-------^^--------^-------^^------^----->>");
		}
		else if (_joining && !_teleport && !_started)
		{
			_log.info("<<-------------------------------------------->>");
			_log.info(">> Fortress Siege Engine infos dump (JOINING) <<");
			_log.info("<<----^-------^^--------^-------^^------^----->>");
		}
		else if (!_joining && _teleport && !_started)
		{
			_log.info("<<--------------------------------------------->>");
			_log.info(">> Fortress Siege Engine infos dump (TELEPORT) <<");
			_log.info("<<-----^-------^^--------^-------^^------^----->>");
		}
		else if (!_joining && !_teleport && _started)
		{
			_log.info("<<-------------------------------------------->>");
			_log.info(">> Fortress Siege Engine infos dump (STARTED) <<");
			_log.info("<<----^-------^^--------^-------^^------^----->>");
		}
		_log.info("Fortress Name: " + _eventName);
		_log.info("Description  : " + _eventDesc);
		_log.info("Join location: " + _joiningLocationName);
		_log.info("Minimum level: " + _minlvl);
		_log.info("Maximum level: " + _maxlvl);
		_log.info("");
		_log.info("##########################");
		_log.info("# _teams(Vector<String>) #");
		_log.info("##########################");
		for (String team : _teams) {
			_log.info(team + " Siege Flags Taken :" + _teamPointsCount.get(_teams.indexOf(team)));
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE"))
		{
			_log.info("");
			_log.info("#########################################");
			_log.info("# _playersShuffle(Vector<L2PcInstance>) #");
			_log.info("#########################################");
			for (L2PcInstance player : _playersShuffle) {
				if (player != null) {
					_log.info("Name: " + player.getName());
				}
			}
		}
		_log.info("");
		_log.info("##################################");
		_log.info("# _players(Vector<L2PcInstance>) #");
		_log.info("##################################");
		for (L2PcInstance player : _players) {
			if (player != null) {
				_log.info("Name: " + player.getName() + "   Team: " + player._teamNameFOS);
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
		System.out.println("**********==Fortress Siege==************");
		System.out.println("FortressSiege._teamPointsCount:"+_teamPointsCount.toString());
		System.out.println("FortressSiege._flagSpawn:  "+_flagSpawn.toString());
		System.out.println("FortressSiege._flagsX:	 "+_flagX);
		System.out.println("FortressSiege._flagsY:	 "+_flagY);
		System.out.println("FortressSiege._flagsZ:	 "+_flagZ);
		System.out.println("************EOF**************\n");
		System.out.println("");
	}

	public static void startJoin(L2PcInstance activeChar)
	{
		if (!startJoinOk())
		{
			activeChar.sendMessage("Event not setted propertly.");
			if (_log.isDebugEnabled()) {
				_log.debug("Fortress Siege Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			}
				return;
		}
		_joining = true;
		spawnEventNpc();
		Announcements(_eventName + "(FOS): Joinable in " + _joiningLocationName + "!");
	}

	public static void startJoin()
	{
		if (!startJoinOk())
		{
			_log.warn("Event not setted propertly.");
			if (_log.isDebugEnabled()) {
				_log.debug("Fortress Siege Engine[startJoin(startJoinOk() = false");
			}
				return;
		}
		_joining = true;
		spawnEventNpc();
		Announcements(_eventName + "(FOS): Joinable in " + _joiningLocationName + "!");
	}

	public static boolean startJoinOk()
	{
		if (_started || _teleport || _joining || _teams.size() > 2 || _teams.size() < 1 || _eventName.equals("") ||
			_joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 || _flagX == 0 || _flagY == 0 || _flagZ == 0 ||
			_npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 || _door[0]==0 || _door[1] == 0 || _door[2]==0 ||
			_door[3]==0 || _door[4]==0 || _door[5]==0 || _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0)) {
			return false;
		}
		return true;
	}

	//New color Announcements 8D for FOS
	public static void Announcements(String announce)
	{
		CreatureSay cs = new CreatureSay(0, 18, "", "Announcements: "+announce);
		if (!_started && !_teleport)
		{
			for (L2PcInstance player: L2World.getInstance().getAllPlayers())
			{
				if (player != null) {
					if (player.isOnline()!=0) {
						player.sendPacket(cs);
					}
				}
			}
		}
		else
		{
			if (_players!=null && !_players.isEmpty())
			{
				for (L2PcInstance player: _players)
				{
					if (player != null) {
						if (player.isOnline()!=0) {
							player.sendPacket(cs);
						}
					}
				}
			}
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
			_npcSpawn.getLastSpawn()._isEventMobFOS = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.error("Fortress Siege Engine[spawnEventNpc(exception: " + e.getMessage());
		}
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

	/** returns true if max players is higher than, or equal to, participated players */
	public static boolean checkMaxPlayers(int players)
	{
		return _maxPlayers >= players;
	}

	/** returns true if there are more players participated than the minimum required */
	public static boolean checkMinPlayers(int players)
	{
		return _minPlayers <= players;
	}

	public static void showArtifactHtml(L2PcInstance eventPlayer, String objectId)
	{
		if (eventPlayer == null) {
			return;
		}
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			TextBuilder replyMSG = new TextBuilder("<html><head><body><center>");
			replyMSG.append("Sacred Artifact<br><br>");
			replyMSG.append("<font color=\"00FF00\">" + _eventName + " Artifact</font><br1>");
			if (eventPlayer._teamNameFOS!=null && eventPlayer._teamNameFOS.equals(_teams.get(1))) {
				replyMSG.append("<font color=\"LEVEL\">This is your Sacred Artifact. Defend it!</font><br1>");
			} else {
				replyMSG.append("<font color=\"LEVEL\">Use the Seal Of Ruler Skill to Complete this Siege!</font><br1>");
			}
			if (!_started) {
				replyMSG.append("The Siege is not in progress yet.<br>Wait for a Admin/GM to start the event.<br>");
			}
			replyMSG.append("</center></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			_log.error("FOS Engine[showArtifactHtml(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
		}
	}
	/** Shows the Event Html to the player, depending on the stage of the event, minimum/maximum players, and more variables */
	public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			TextBuilder replyMSG = new TextBuilder("<html><body>");
			replyMSG.append("Fortress Siege Event<br><br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
			replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br><br>");
			if (!_started && !_joining) {
				replyMSG.append("<center>Wait till the admin/gm starts the participation.</center>");
			} else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!_started)
				{
					replyMSG.append("Currently participated : <font color=\"00FF00\">" + _playersShuffle.size() +".</font><br>");
					replyMSG.append("Admin set max players : <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate in this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.FortressSiege_JOIN_CURSED) {
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			} else if (!_started && _joining && eventPlayer.getLevel()>=_minlvl && eventPlayer.getLevel()<_maxlvl){
				if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE")) {
						replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameFOS + "</font><br><br>");
					} else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE")) {
						replyMSG.append("You participated already!<br><br>");
					}
					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait util the event starts or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"Remove\" action=\"bypass -h npc_" + objectId + "_fos_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					replyMSG.append("<td width=\"200\">Admin set Minimum level : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Admin set Maximum level : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
					if (Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						for (String team : _teams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_fos_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
						}
						replyMSG.append("</table></center>");
					}
					else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						for (String team : _teams) {
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
						}
						replyMSG.append("</table></center><br>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_fos_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("Teams will be generated randomly!");
					}
				}
			}
			else if (_started && !_joining) {
				replyMSG.append("<center>The Fortress Siege has already begun.</center>");
			} else if (eventPlayer.getLevel()<_minlvl || eventPlayer.getLevel()>_maxlvl )
			{
				replyMSG.append("You are level : <font color=\"00FF00\">" + eventPlayer.getLevel() +"</font><br>");
				replyMSG.append("Admin set Minimum level : <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Admin set Maximum level : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can not participate in this event.</font><br>");
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			eventPlayer.sendPacket( ActionFailed.STATIC_PACKET );
		}
		catch (Exception e)
		{
			_log.error("FortressSiege Engine[showEventHtml(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

	/** Searches the list of shuffled players to find if the eventPlayer is one of them */
	public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
	{
		try
		{
			for(L2PcInstance player: _playersShuffle)
			{
				if(player==null)
				{
					_playersShuffle.remove(player);
					continue;
				}
				else if(player.getObjectId()==eventPlayer.getObjectId())
				{
					eventPlayer._inEventFOS = true;
					eventPlayer._countFOSKills = 0;
					return true;
				}
				//Just incase a player got a new objectid after DC or reconnect
				else if(player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventFOS = true;
					eventPlayer._countFOSKills = 0;
					return true;
				}
			}
		}
		catch (Throwable t)
		{
			_log.error("Error: FortressSiege.checkShufflePlayers: "+t.toString());
		}
		return false;
	}

	public static int teamPointsCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		return index == -1 ? -1 : _teamPointsCount.get(index);
	}

	public static void setTeamPointsCount(String teamName, int teamPointCount)
	{
		int index = _teams.indexOf(teamName);
		if (index > -1) {
			_teamPointsCount.set(index, teamPointCount);
		}
	}

	public static int teamPlayersCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		return index == -1 ? -1 : _teamPlayersCount.get(index);
	}

	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		int index = _teams.indexOf(teamName);
		if (index > -1) {
			_teamPlayersCount.set(index, teamPlayersCount);
		}
	}

	public static void addPlayer(L2PcInstance player, String teamName)
	{
		if (!_joining || !addPlayerOk(teamName, player)) {
			return;
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE"))
		{
			player._teamNameFOS = teamName;
			_players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName)+1);
		}
		else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE")) {
			_playersShuffle.add(player);
		}
		player._inEventFOS = true;
		player._countFOSKills = 0;
	}

	public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
	{
		if (teamName == null || eventPlayer == null) {
			return false;
		}
		if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventFOS)
		{
			eventPlayer.sendMessage("You already participated in this event!");
			return false;
		}
		if (eventPlayer._inEventTvT || eventPlayer._inEventCTF || eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		for(L2PcInstance player: _players)
		{
			if(player.getObjectId()==eventPlayer.getObjectId() || player.getName().equals(eventPlayer.getName()))
			{
				eventPlayer.sendMessage("You already participated in this event!");
				return false;
			}
		}
		if(_players.contains(eventPlayer))
		{
			eventPlayer.sendMessage("You already participated in this event!");
			return false;
		}
		if (TvT._savePlayers.contains(eventPlayer.getName()) || CTF._savePlayers.contains(eventPlayer.getName()))
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("NO")) {
			return true;
		} else if (Config.FortressSiege_EVEN_TEAMS.equals("BALANCE"))
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
		else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE")) {
			return true;
		}
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}

	public static void removePlayer(L2PcInstance player)
	{
		if(player._inEventFOS)
		{
			if(!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorFOS);
				player.setKarma(player._originalKarmaFOS);
				player.broadcastUserInfo();
			}
			player._teamNameFOS = new String();
			player._countFOSKills = 0;
			player._inEventFOS = false;
			if ((Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
			{
				setTeamPlayersCount(player._teamNameFOS, teamPlayersCount(player._teamNameFOS)-1);
				_players.remove(player);
			}
			else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && !_playersShuffle.isEmpty() && _playersShuffle.contains(player)) {
				_playersShuffle.remove(player);
			}
		}
	}

	public static void removeOfflinePlayers()
	{
		try
		{
			if(_playersShuffle== null || _playersShuffle.isEmpty()) {
				return;
			}
			for(L2PcInstance player: _playersShuffle) {
				if(player==null || player.isOnline()==0) {
					_playersShuffle.remove(player);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	public static void teleportStart()
	{
		if (!_joining || _started || _teleport) {
			return;
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements("Not enough players for this event. Minimum Requested : " + _minPlayers +", Participated : " + _playersShuffle.size());
			return;
		}
		_joining = false;
		Announcements(_eventName + "(FOS): Teleport to team spot in 10 seconds!");
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				FortressSiege.sit();
				FortressSiege.spawnFlag();
				for (L2PcInstance player : _players)
				{
					if (player !=  null){
						FortressSiege.setSealOfRuler(player);
						if (Config.FortressSiege_ON_START_UNSUMMON_PET)
						{
							//Remove Summon's buffs
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
						if (Config.FortressSiege_ON_START_REMOVE_ALL_EFFECTS) {
							for (L2Effect e : player.getAllEffects()) {
								if (e != null) {
									e.exit();
								}
							}
						}
						//Remove player from his party
						L2Party party = player.getParty();
						if (party != null) {
							party.removePartyMember(player);
						}
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameFOS)), _teamsY.get(_teams.indexOf(player._teamNameFOS)), _teamsZ.get(_teams.indexOf(player._teamNameFOS)));
					}
				}
			}
		}, 10000);
		_teleport = true;
	}

	/** Sets the Player name color and karma, and adds siege properties*/
	public static void setUserData()
	{
		if (_players==null || _players.isEmpty()) {
			return;
		}
		for (L2PcInstance player : _players)
		{
			if (player==null) {
				continue;
			}
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameFOS)));
			player.setKarma(0);
			player.broadcastUserInfo();
			setTitleSiegeFlags(player);
		}
	}

	public static void setTitleSiegeFlags(L2PcInstance player)
	{
		if (player==null) {
			return;
		}
		if (player._teamNameFOS.equals(_teams.get(0))) // attacking team = attackers siege flag sign
		{
			player.setSiegeState((byte)1);
			player.sendPacket(new UserInfo(player));
			for (L2PcInstance p : _players) {
				p.sendPacket(new RelationChanged(player, player.getRelation(p), player.isAutoAttackable(p)));
			}
		}
		if (player._teamNameFOS.equals(_teams.get(1))) // defending team = defender siege flag sign
		{
			player.setSiegeState((byte)2);
			player.sendPacket(new UserInfo(player));
			for (L2PcInstance p : _players) {
				p.sendPacket(new RelationChanged(player, player.getRelation(p), player.isAutoAttackable(p)));
			}
		}
	}

	public static boolean checkIfOkToCastSealOfRule(L2PcInstance player)
	{
		if (!_started) {
			return false;
		} else if (Math.abs(player.getZ() - _flagZ) > 50) {
			return false;
		}

		if (player.getTarget() instanceof L2NpcInstance &&
			((L2NpcInstance)player.getTarget())._isFOS_Artifact &&
			player._inEventFOS && player._teamNameFOS.equals(_teams.get(0))) {
			return true;
		}

		return false;
	}

	public static void setSealOfRuler(L2PcInstance player)
	{
		try
		{
			L2Skill sealOfRuler = SkillTable.getInstance().getInfo(246, 1);
			if (!player.returnSkills().containsValue(sealOfRuler)) {
				player.addSkill(sealOfRuler, false);
			} else {
				player._FOSRulerSkills = true;
			}
			player.sendSkillList();
			player.sendMessage("You have been given the Seal Of Ruler skill for this event.");
		}catch (Throwable t){return;}
	}

	public static void removeSealOfRuler(L2PcInstance player)
	{
		try
		{
			L2Skill sealOfRuler = SkillTable.getInstance().getInfo(246, 1);
			if (player.returnSkills().containsValue(sealOfRuler) && !player._FOSRulerSkills)
			{
				player.removeSkill(sealOfRuler, false);
				player.sendSkillList();
			} else {
				player._FOSRulerSkills = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void shuffleTeams()
	{
		int teamCount = 0;
		_teamPlayersCount.set(0,0);
		_teamPlayersCount.set(1,0);
		while(true)
		{
			if (_playersShuffle.isEmpty() || _playersShuffle==null) {
				break;
			}
			int randomIndex = Rnd.nextInt(_playersShuffle.size());
			L2PcInstance player = _playersShuffle.get(randomIndex);
			player._originalNameColorFOS = player.getAppearance().getNameColor();
			player._originalKarmaFOS = player.getKarma();
			player._teamNameFOS = _teams.get(teamCount);
			_players.add(player);
			_playersShuffle.remove(randomIndex);
			_savePlayers.add(player.getName());
			_savePlayerTeams.add(_teams.get(teamCount));
			_teamPlayersCount.set(teamCount,_teamPlayersCount.get(teamCount)+1);
			checkForSameIP(player, teamCount); // Checks for more players from the same IP and puts them in the same team
			if (teamCount == _teams.size()-1) {
				teamCount = 0;
			} else {
				teamCount++;
			}
		}
		//Since we add same IPs to same teams this may cause the teams to be uneven in numbers.
		//so we shift amount of players until the teams are even.
		while (_teamPlayersCount.get(0)>_teamPlayersCount.get(1)+1) {
			movePlayerFromTeamToTeam(0,1);
		}
		while (_teamPlayersCount.get(1)>_teamPlayersCount.get(0)+1){
			movePlayerFromTeamToTeam(1,0);
		}
	}

	/**
	 * Moves a player from fromTeam team to toTeam team
	 * @param fromTeam - index of the team to move a player from
	 * @param toTeam - index of the team to move a player to
	 */
	private static void movePlayerFromTeamToTeam(int fromTeam, int toTeam)
	{
		int index = 0;
		for (L2PcInstance p : _players)
		{
			if (p._teamNameFOS.equals(_teams.get(fromTeam)))
			{
				index = _players.indexOf(p);
				break;
			}
		}
		L2PcInstance player = _players.get(index);
		player._teamNameFOS = _teams.get(toTeam);
		_savePlayerTeams.set(index,_teams.get(toTeam));
		_teamPlayersCount.set(fromTeam,_teamPlayersCount.get(fromTeam)-1);
		_teamPlayersCount.set(toTeam,_teamPlayersCount.get(toTeam)+1);
	}

	/**
	 * Finds all players from the same IP and places them in the same teams, or if FortressSiege_SAME_IP_PLAYERS_ALLOWED reached, throws them from the event ;]
	 * @param player L2PcInstance of the player that has already been removed from the queue
	 */
	private static void checkForSameIP(L2PcInstance player, int teamNumber)
	{
		try
		{
			String playerIP = getIP(player);
			if (playerIP == null) {
				return;
			}
			for (L2PcInstance same : _playersShuffle)
			{
				if (same == null)
				{
					_playersShuffle.remove(same);
					continue;
				}
				String sameIP = getIP(same);
				if (sameIP == null) {
					continue;
				}
				if (!sameIP.equals(playerIP)) {
					continue;
				}
				//Now we are left with equal IPs:
				if (!Config.FortressSiege_SAME_IP_PLAYERS_ALLOWED)
				{
					String msg = "Admin does not allow players from the same IP to participate. Player "+player.getName()+" from IP "+playerIP+" is already joined. So player "+same.getName()+" may not join this event!";
					player.sendMessage(msg);
					same.sendMessage(msg);
					removePlayer(same);
					continue;
				}
				//So we allow players from the same IP to join the event:
				same._originalNameColorFOS = same.getAppearance().getNameColor();
				same._originalKarmaFOS = same.getKarma();
				same._teamNameFOS = _teams.get(teamNumber);
				_players.add(same);
				_playersShuffle.remove(same);
				_savePlayers.add(same.getName());
				_savePlayerTeams.add(_teams.get(teamNumber));
				_teamPlayersCount.set(teamNumber,_teamPlayersCount.get(teamNumber)+1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String getIP(L2PcInstance player)
	{
		try
		{
			return player.getClient().getConnection().getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
			return "";
		}
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
				else if (player.isSitting()) {
					player.standUp();
				}
			}
		}
	}

	/** It's not alway random, only for the Artifacts that I didn't know where they should go =P */
	public static int getRandomFlagId()
	{
		int[] flagId = {31508,31509,31541,35514,35515,
						35322,35323,35469,31512};
		if (_eventName.contains("Ketra")) {
			return 31558;
		} else if (_eventName.contains("Varka")) {
			return 31560;
		} else if (_eventName.contains("Saint")) {
			return 31510;
		}
		return flagId[Rnd.get(flagId.length)];
	}

	public static void spawnFlag()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(getRandomFlagId());
		try
		{
			_flagSpawn = new L2Spawn(tmpl);
			_flagSpawn.setLocx(_flagX);
			_flagSpawn.setLocy(_flagY);
			_flagSpawn.setLocz(_flagZ);
			_flagSpawn.setAmount(1);
			_flagSpawn.setHeading(0);
			_flagSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_flagSpawn, false);
			_flagSpawn.init();
			_flagSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_flagSpawn.getLastSpawn().setTitle(_eventName);
			_flagSpawn.getLastSpawn().decayMe();
			_flagSpawn.getLastSpawn().spawnMe(_flagSpawn.getLastSpawn().getX(), _flagSpawn.getLastSpawn().getY(), _flagSpawn.getLastSpawn().getZ());
			_flagSpawn.getLastSpawn()._isFOS_Artifact = true;
		}
		catch(Exception e)
		{
			_log.error("Fortress Siege Engine[spawnAllFlags()]: exception: " + e.getStackTrace());
		}
	}

	public static void unspawnFlag()
	{
		try
		{
			if (_flagSpawn == null || _teams == null) {
				return;
			}
			_flagSpawn.getLastSpawn().deleteMe();
			_flagSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_flagSpawn, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void startEvent(L2PcInstance activeChar)
	{
		if (!startEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("Fortress Siege Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			}
			return;
		}
		_teleport = false;
		sit();
		_started = true;
		_teamPointsCount.set(1, 1); // Start with 1 point for defenders, then 2 points for each successful siege
		Announcements(_eventName + "(FOS): Started. Let the battles begin!");
		try
		{
			for (int x=0 ; x<4 ; x++) {
				if (DoorTable.getInstance().getDoor(_door[x])!=null) {
					DoorTable.getInstance().getDoor(_door[x]).openMe();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean startAutoEvent()
	{
		if (!startEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("Fortress Siege Engine[startEvent]: startEventOk() = false");
			}
			return false;
		}
		_teleport = false;
		sit();
		_started = true;
		Announcements(_eventName + "(FOS): Started. Let the battles begin!");
		try
		{
			for (int x=0 ; x<4 ; x++) {
				if (DoorTable.getInstance().getDoor(_door[x])!=null) {
					DoorTable.getInstance().getDoor(_door[x]).openMe();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started) {
			return false;
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE")){
			if (_teamPlayersCount.contains(0)) {
				return false;
			}
		}
		return true;
	}

	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started) {
			return;
		}
		if (_joining && !_teleport && !_started){
			unspawnEventNpc();
			resetData();
			_joining = false;
			Announcements(_eventName + "(FOS): Siege aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		unspawnEventNpc();
		unspawnFlag();
		Announcements(_eventName + "(FOS): Match aborted!");
		teleportFinish();
	}

	public static void teleportFinish()
	{
		Announcements(_eventName + "(FOS): Teleport back to participation NPC in 10 seconds!");
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				for (L2PcInstance player : _players)
				{
					if (player !=  null && player.isOnline()!=0) {
						player.teleToLocation(_npcX, _npcY, _npcZ);
					}
				}
				resetData();
			}
		}, 10000);
	}

	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("FortressSiege Engine[finishEvent]: finishEventOk() = false");
			}
			return;
		}
		_started = false;
		unspawnEventNpc();
		unspawnFlag();
		processTopTeam(); // and also divides team points to get the number of successful sieges
		if (_topScore != 0) {
			playKneelAnimation(_topTeam);
		}
		if(Config.FortressSiege_ANNOUNCE_TEAM_STATS)
		{
			Announcements(_eventName + " Team Statistics:");
			Announcements("Team: " + _teams.get(0) + " - Successful Sieges: " + _teamPointsCount.get(0));
			Announcements("Team: " + _teams.get(1) + " - Successful Sieges: " + _teamPointsCount.get(1));
		}
		teleportFinish();
	}

	private static boolean finishEventOk()
	{
		return _started;
	}

	public static void processTopTeam()
	{
		if (_teamPointsCount.get(0)<2 && _teamPointsCount.get(1)<2) // 2 loosing teams
		{
			_teamPointsCount.set(0,0);
			_teamPointsCount.set(1,0);
			Announcements(_eventName + "(FOS): No Successful Engravings were made. "+_teams.get(1)+" succeeded to protect the Fortress!");
			Announcements(_eventName + "(FOS): "+_teams.get(1)+" wins!");
			rewardTeam(_teams.get(1));
			return;
		}
		else
		{
			_teamPointsCount.set(0,(_teamPointsCount.get(0)/2)); // Needed to decrease the attacker point
			_teamPointsCount.set(1,(_teamPointsCount.get(1)/2)); // Remember that they get 2 points for each siege!
		}
		if (_teamPointsCount.get(0)>_teamPointsCount.get(1))
		{
			Announcements(_eventName + "(FOS): Team " + _teams.get(0) + " wins the match, with " + _teamPointsCount.get(0) + " successful sieges!");
			rewardTeam(_teams.get(0));
		}
		else if (_teamPointsCount.get(1)>_teamPointsCount.get(0))
		{
			Announcements(_eventName + "(FOS): Team " + _teams.get(1) + " wins the match, with " + _teamPointsCount.get(1) + " successful sieges!");
			rewardTeam(_teams.get(1));
		}
		else
		{
			Announcements(_eventName + "(FOS): Maximum Successful sieges : " + _teamPointsCount.get(0) + " Sieges! It's a tie.");
			rewardTeam(null);
		}
	}

	/** In this event if both team made EQUAL successful sieges, prize is divided into 2 ONLY if it's stackable */
	public static void rewardTeam(String teamName)
	{
		if (!ItemTable.getInstance().createDummyItem(_rewardId).isStackable() && teamName == null) {
			return; // If the prize is not stackable, and no Winning team, return
		}
		if (teamName == null) {
			Announcements(_eventName + "(FOS): The prize will be divided between both teams.");
		}
		int stackableCount = _rewardAmount;
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if ((teamName == null || player._teamNameFOS.equals(teamName)) && (player._countFOSKills>0 || Config.FortressSiege_PRICE_NO_KILLS))
				{
					_rewardAmount = stackableCount;
					if (teamName==null && _rewardAmount>1) {
						_rewardAmount = (_rewardAmount+1)/2;
					}
					PcInventory inv = player.getInventory();
					if (ItemTable.getInstance().createDummyItem(_rewardId).isStackable()) {
						inv.addItem("FortressSiege: " + _eventName, _rewardId, _rewardAmount, player, null);
					} else {
						for (int i=0;i<=_rewardAmount-1;i++) {
							inv.addItem("FortressSiege: " + _eventName, _rewardId, 1, player, null);
						}
					}
					SystemMessage sm;
					if (_rewardAmount > 1)
					{
						sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(_rewardId);
						sm.addNumber(_rewardAmount);
						player.sendPacket(sm);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(_rewardId);
						player.sendPacket(sm);
					}
					StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("");
					replyMSG.append("<html><body>Your team did a good job. Look in your inventory for the reward.</body></html>");
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					ItemList il = new ItemList(player, true);
					player.sendPacket(il);
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket( ActionFailed.STATIC_PACKET );
				}
			}
		}
	}

	public static void playKneelAnimation(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null && player.isOnline()!=0)
			{
				if (!player._teamNameFOS.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
					player.broadcastPacket(new SocialAction(player.getObjectId(), 13));
				}
				else{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}

	public static void doSwap()
	{
		sit();//stop everything;
		healDoors(); // restore all doors
		closeDoors(); // close all inner doors
		Announcements(_eventName + "(FOS): Teleport to team spots. In 20 seconds the Siege continues!");//announce to players
		_teamPointsCount.set(0, _teamPointsCount.get(0)+2);//give points to the attacking side
		String team = _teams.get(0);//swap teams
		_teams.set(0,_teams.get(1));
		_teams.set(1,team);

		int points = _teamPointsCount.get(0);//swap points!
		_teamPointsCount.set(0,_teamPointsCount.get(1));
		_teamPointsCount.set(1,points);

		for (L2PcInstance player : _players) {
			setTitleSiegeFlags(player);
		}

		try
		{
			for (int x=0 ; x<4 ; x++)
			{
				if (DoorTable.getInstance().getDoor(_door[x])!=null) {
					DoorTable.getInstance().getDoor(_door[x]).closeMe();
				}
			}
		}
		catch(Throwable t){}

		for (L2PcInstance player : _players) {
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameFOS)), _teamsY.get(_teams.indexOf(player._teamNameFOS)), _teamsZ.get(_teams.indexOf(player._teamNameFOS)), false);
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() //teleport players back to reverse positions
		{
			public void run()
			{
				try
				{
					for (int x=0 ; x<4 ; x++) {
						if (DoorTable.getInstance().getDoor(_door[x])!=null) {
							DoorTable.getInstance().getDoor(_door[x]).openMe();
						}
					}
				}
				catch(Throwable t){}
				sit();
				if (Rnd.get(30)<11) {
					Announcements(_eventName + "(FOS): Let the sieges continue!");//announce to players
				} else if (Rnd.get(30)<11) {
					Announcements(_eventName + "(FOS): ...and the battles begin again!");//announce to players
				} else {
					Announcements(_eventName + "(FOS): May the best team win!");//announce to players
				}
			}
		}, 20000);
	}

	private static void closeDoors()
	{
		try
		{
			for (int x=0 ; x<6 ; x++)
			{
				if (_door[x]<=0) {
					continue;
				} else if (DoorTable.getInstance().getDoor(_door[x])!=null)
				{
					DoorTable.getInstance().getDoor(_door[x]).closeMe();
				}
			}
		}
		catch(Throwable t){}
	}

	private static void healDoors()
	{
		try
		{
			for (int x=0 ; x<6 ; x++)
			{
				if (_door[x]<=0) {
					continue;
				} else if (DoorTable.getInstance().getDoor(_door[x])!=null)
				{
					DoorTable.getInstance().getDoor(_door[x]).doRevive();
					DoorTable.getInstance().getDoor(_door[x]).spawnMe();
					DoorTable.getInstance().getDoor(_door[x]).getStatus().setCurrentHp(DoorTable.getInstance().getDoor(_door[x]).getMaxHp());
				}
			}
		}catch(Throwable t){}
	}

	public static boolean isDoorAttackable(int id,L2Character attacker)
	{
		 if (!_started) {
			return false;
		}
		for (int doorId : _door)
		{
			if (doorId!=id) {
				continue;
			}
			if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker)._inEventFOS) {
				return true;
			} else if (attacker instanceof L2Summon && ((L2Summon)attacker).getOwner()._inEventFOS) {
				return true;
			} else if (attacker instanceof L2PetInstance && ((L2PetInstance)attacker).getOwner()._inEventFOS) {
				return true;
			}
		}
		return false;
	}

	/** Returns true if the L2Character is in the protected siege spawn zone (both attacker/defender spawns are protected) */
	public static boolean inProtectedZone(L2Character cha, L2PcInstance attacker)
	{
		if (cha == null || attacker == null) {
			return false;
		}
		//This is the function: isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck); (corners are left unchecked)
		if (cha.isInsideRadius(eventCenterX, eventCenterY, eventCenterZ, 584, true, true) ||
			cha.isInsideRadius(_teamsX.get(0), _teamsY.get(0), _teamsZ.get(0), 300, true, true) )
		{
			attacker.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
			attacker.getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			attacker.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}

	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started) || Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE")  && (_teleport || _started))
		{
			if (Config.FortressSiege_ON_START_REMOVE_ALL_EFFECTS) {
				for (L2Effect e : player.getAllEffects()) {
					if (e != null) {
						e.exit();
					}
				}
			}

			player._teamNameFOS = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameFOS)), _teamsY.get(_teams.indexOf(player._teamNameFOS)), _teamsZ.get(_teams.indexOf(player._teamNameFOS)));
			if(!_players.contains(player.getName())) {
				_players.add(player);
			}

			player._originalNameColorFOS = player.getAppearance().getNameColor();
			player._originalKarmaFOS = player.getKarma();
			player._inEventFOS = true;
			player._countFOSKills = 0;
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameFOS)));
			player.setKarma(0);

			setSealOfRuler(player);
			setTitleSiegeFlags(player);
			player.broadcastUserInfo();
		}
	}

	/**************************-- Auto Events Engine --******************************/

	/** Starts the autoevent engine, generates a random event and runs it */
	public static void autoEvent()
	{
		if(startAutoJoin())
		{
			if(_joinTime > 0) {
				waiter(_joinTime * 60 * 1000); // minutes for join event
			} else if(_joinTime <= 0)
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
			else if (!teleportAutoStart()) {
				abortEvent();
			}
		}
	}

	public static boolean teleportAutoStart()
	{
		if (!_joining || _started || _teleport) {
			return false;
		}
		if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements("Not enough players for event. Min Requested : " + _minPlayers +", Participating : " + _playersShuffle.size());
			return false;
		}
		_joining = false;
		Announcements(_eventName + "(FOS): Teleport to team spot in 10 seconds!");
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				sit();
				spawnFlag();
				for (L2PcInstance player : _players)
				{
					if (player !=  null){
						setSealOfRuler(player);
						if (Config.FortressSiege_ON_START_UNSUMMON_PET)
						{
							//Remove Summon's buffs
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
						if (Config.FortressSiege_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (L2Effect e : player.getAllEffects()) {
								if (e != null) {
									e.exit();
								}
							}
						}
						//Remove player from his party
						if (player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameFOS)), _teamsY.get(_teams.indexOf(player._teamNameFOS)), _teamsZ.get(_teams.indexOf(player._teamNameFOS)));
					}
				}
			}
		}, 10000);
		_teleport = true;
		return true;
	}

	public static boolean startAutoJoin()
	{
		if (!startJoinOk())
		{
			if (_log.isDebugEnabled()) {
				_log.debug("FortressSiege Engine[startJoin]: startJoinOk() = false");
			}
				return false;
		}
		_joining = true;
		spawnEventNpc();
		Announcements(_eventName + "(FOS): Joinable in " + _joiningLocationName + "!");
		return true;
	}

	private static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int)(interval / 1000);
		while (startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--; // here because we don't want to see two time announce at the same time
			if (!_joining && !_started && !_teleport) {
				break;
			}
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							Announcements(_eventName + "(FOS): Joinable in " + _joiningLocationName + "!");
							Announcements("Fortress Siege Event: " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if (_started) {
							Announcements("Fortress Siege Event: " + seconds / 60 / 60 + " hour(s) till event finish!");
						}
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: //  10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						if (_joining)
						{
							removeOfflinePlayers();
							Announcements(_eventName + "(FOS): Joinable in " + _joiningLocationName + "!");
							Announcements("Fortress Siege Event: " + seconds / 60 + " minute(s) till registration close!");
						}
						else if (_started) {
							Announcements("Fortress Siege Event: " + seconds / 60 + " minute(s) till event finish!");
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
							Announcements("Fortress Siege Event: " + seconds + " second(s) till registration close!");
						} else if (_teleport) {
							Announcements("Fortress Siege Event: " + seconds + " seconds(s) till start fight!");
						} else if (_started) {
							Announcements("Fortress Siege Event: " + seconds + " second(s) till event finish!");
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
				catch (InterruptedException ie)	{}
			}
		}
	}
}
