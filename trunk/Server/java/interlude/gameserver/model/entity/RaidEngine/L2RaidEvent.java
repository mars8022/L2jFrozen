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
package interlude.gameserver.model.entity.RaidEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.instancemanager.RaidBossSpawnManager;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.actor.instance.L2BufferOpenInstance;
import interlude.gameserver.model.actor.instance.L2EventManagerInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.item.PcInventory;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ItemList;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This Class implements and Manages All Raid Events.<br>
 *
 * @author polbat02
 */
public class L2RaidEvent
{
	// Local Variables Definition
	// --------------------------
	protected static final Logger _log = Logger.getLogger(L2RaidEvent.class.getName());
	/**
	 * Definition of the Event Mob Spawn
	 */
	private static L2PcInstance _player;
	private static L2Spawn _npcSpawn = null;
	/**
	 * Definition of the Spawn as a L2NpcInstance
	 */
	private static L2NpcInstance _lastNpcSpawn = null;
	/**
	 * Custom Management of Experience upon NPC death.
	 */
	public static int exp = 0;
	/**
	 * Custom Management of SP upon NPC death.
	 */
	public static int sp = 0;
	/**
	 * <b>Event Type:</b><br>
	 * 1- Solo Event (Single player)<br>
	 * 2- Clan Event<br>
	 * 3- Party Event<br>
	 */
	public static int _eventType;
	/**
	 * Number Of Event Mobs.
	 */
	private static int _eventMobs = 0;
	/**
	 * Reward Level: According to this reward level the players will be congratulated with different prizes.
	 */
	private static int _rewardLevel;
	/**
	 * Transport Locations
	 */
	private static int _locX, _locY, _locZ, _pX, _pY, _pZ;
	/**
	 * NPC spawn positions
	 */
	private static int _npcX, _npcY, _npcZ;
	/**
	 * DataBase Prize Parameters
	 */
	private static int _first_id, _first_ammount, _second_id, _second_ammount, _event_ammount;
	/** Event points Required */
	private static int _points;
	/** NPC ID */
	private static int _npcId;
	/** NPC Ammount */
	private static int _npcAm;
	/** BuffList */
	private static int _bufflist;
	/** BUFFER */
	private static L2Object _effector;
	/**
	 * Vector Created to add Single/Party/Clan Players onto the event. TODO: Use this vector also to add another kind of event --> free Event with any player that may want to participate.
	 */
	public static Vector<L2PcInstance> _participatingPlayers = new Vector<L2PcInstance>();
	/**
	 * Players from whom we're waiting for an answer in order to know it they want to join the event.
	 */
	public static Vector<L2PcInstance> _awaitingplayers = new Vector<L2PcInstance>();
	/**
	 * Vector Created to track all the Event Mobs and Delete them if needed.
	 */
	public static Vector<L2NpcInstance> _eventMobList = new Vector<L2NpcInstance>();
	/** The state of the Event<br> */
	private static EventState _state = EventState.INACTIVE;

	enum EventState
	{
		INACTIVE, STARTING, STARTED, PARTICIPATING, REWARDING, INACTIVATING
	}

	/**
	 * CONSTRUCTOR:<br>
	 * This is the start of the Event, defined from HTM files.<br>
	 * Documentation can be found in the method.<br>
	 *
	 * @param player
	 *            --> Player taking the action on the Event Manager.
	 * @param type
	 *            --> Type of Event: 1: Single Event || 2: Clan Event || 3: Party Event
	 * @param points
	 *            --> Event Points Required to start event.
	 * @param npcId
	 *            --> Id of the Event Raid/Mob
	 * @param npcAm
	 *            --> Amount of Mobs
	 * @param minPeople
	 *            --> Minimum People required to run event (Only functional on Clan and Party Events)
	 * @param bufflist
	 *            --> BuffList to apply to the player. Defined in the SQL table buff_templates
	 * @param rewardLevel
	 *            --> Reward level to apply upon player's victory.
	 * @param effector
	 *            --> Effector of the Buffs (Previously defined in L2EventMAnagerInstance.java)
	 * @param participatingPlayers
	 *            --> Players Enrolled in the Event.
	 */
	public L2RaidEvent(L2PcInstance player, int type, int points, int npcId, int npcAm, int bufflist, int rewardLevel, L2Object effector, Vector<L2PcInstance> participatingPlayers)
	{
		// Define the actual coordinates of the Player.
		_player = player;
		_pX = player.getClientX();
		_pY = player.getClientY();
		_pZ = player.getClientZ();
		_eventType = type;
		_points = points;
		_npcId = npcId;
		_npcAm = npcAm;
		_bufflist = bufflist;
		_rewardLevel = rewardLevel;
		_effector = effector;
		_participatingPlayers = participatingPlayers;
	}

	/** Event Initialization given the Constructor defined variables. */
	public void init()
	{
		setState(EventState.STARTING);
		// Increase the number of Current Events.
		if (!L2EventManagerInstance.addEvent()) {
			return;
		}
		// Set the coordinates for the Event.
		if (setCoords(_player)) {
			;
		} else
		{
			L2EventManagerInstance.removeEvent();
			return;
		}
		_log.warning("RaidEngine [setCoords]: Players: " + _locX + ", " + _locY + ", " + _locZ);
		// Set Player inEvent
		setInEvent(_player);
		// Initialize event.
		startEvent(_player, _npcId, _npcAm);
		// Call the Function required to buff the player.
		buffEventMembers(_player, _points, _bufflist, _effector);
		return;
	}

	/**
	 * Sets the spawn positions for the players in each event
	 */
	private static boolean setCoords(L2PcInstance player)
	{
		int _ce = L2EventManagerInstance._currentEvents;
		if (_ce == 0 || _ce > Config.RAID_SYSTEM_MAX_EVENTS)
		{
			String reason = null;
			if (_ce == 0) {
				reason = "Current Events = 0.";
			} else if (_ce > Config.RAID_SYSTEM_MAX_EVENTS) {
				reason = "Too many Events going on";
			}
			player.sendMessage("Raid Engines [setCoords()]: Error while setting spawn positions for players and Monsters. Reason: " + reason);
			return false;
		}
		else
		{
			loadSpawns(_ce);
			return true;
		}
	}

	/**
	 * We will set the player/party Member in an Event Status.<br>
	 * This way we will also make sure they don't enroll in any other event.<br>
	 *
	 * @param player
	 *            --> Player to set in an Event Status
	 * @param type
	 *            --> Type of event to be set In.
	 */
	private synchronized void setInEvent(L2PcInstance player)
	{
		// Check if the type of event is defined.
		if (_eventType != 1 && _eventType != 2 && _eventType != 3)
		{
			player.sendMessage("Debug: Error in The event type [Function: setInEvent]");
			_log.warning("Event Manager: Error! Event not defined! [Function setInEvent]");
			return;
		}
		for (L2PcInstance member : _participatingPlayers)
		{
			if (member == null) {
				continue;
			}
			switch (_eventType)
			{
				case 1:
				{
					member.inSoloEvent = true;
					break;
				}
				case 2:
				{
					member.inClanEvent = true;
					break;
				}
				case 3:
				{
					member.inPartyEvent = true;
					break;
				}
				default:
					return;
			}
			member.sendMessage("Event Manager: You are now enroled in a " + L2EventChecks.eType(_eventType) + " Type of Event.");
		}
	}

	/**
	 * <b>Let's Apply the Buffs to the Event Members</b> <li>We don't need to check if the player can or can not have access to the buffing state since it has previously been checked. <li>We assign a value of previousEventPoints to notify the player. <li>Apply the buffs. <li>Notify the player once he/she has gotten the Buffs. <br>
	 * More Documentation can Be found inside the method's code.<br>
	 * We will apply the buffs previous to the Event following the parameters:
	 *
	 * @param player
	 *            --> Player participating in the Event.
	 * @param eventPoints
	 *            --> Event points to be deduced once the buffing takes place.
	 * @param buffList
	 *            --> Buff list from where the buffs will be taken.
	 * @param efector
	 *            --> Eefector taking the action (in this case NPC).
	 * @param eventType
	 *            --> Type of Event.
	 */
	private synchronized static void buffEventMembers(L2PcInstance player, int eventPoints, int buffList, L2Object efector)
	{
		/*
		 * Check if the event type has been defined. Once the event is fully functional this checks will be taken out
		 */
		if (_eventType != 1 && _eventType != 2 && _eventType != 3)
		{
			player.sendMessage("Debug: Error in The event type [Function: bufEventMembers]");
			_log.warning("Se corta la funcion de entrega de buffs.");
			return;
		}
		// Single event --> Direct Buffing.
		if (_eventType == 1)
		{
			int previousPoints = player.getEventPoints();
			if (Config.RAID_SYSTEM_GIVE_BUFFS) {
				L2BufferOpenInstance.makeBuffs(player, buffList, efector, false);
			}
			player.setEventPoints(player.getEventPoints() - eventPoints);
			player.sendMessage("Event Manager: " + eventPoints + " Event Points have Been used. " + "You had " + previousPoints + " and now you have " + player.getEventPoints() + "Event Points.");
		}
		// Clan Event: Let's buff all the clan members...
		// TODO: Check if the distance of other clan members is important upon
		// member buffing.
		if (_eventType == 2)
		{
			// Define HOW many players are online at this very moment.
			int cmCount = _participatingPlayers.size();
			// Define the individual Event Points Price for every player.
			int individualPrice = eventPoints / cmCount;
			// Round up the price
			// individualPrice = Math.round(individualPrice);
			// Start the Buffing.
			for (L2PcInstance member : _participatingPlayers)
			{
				// Define the previous points for each member of the clan.
				int previousPoints;
				if (member == null) {
					continue;
				}
				// Apply the Buffs if allowed
				if (Config.RAID_SYSTEM_GIVE_BUFFS) {
					L2BufferOpenInstance.makeBuffs(member, buffList, efector, false);
				}
				/*
				 * In this case we will generate an HTML to notify the member of the action taken. 1. In the first case, we will check if the subject has enough Event Points as to pay the Buffs, and enroll into the event. 2. If that's not the case we will proceed into the first IF: 2a. The even points will be replaced by 0 since the player doesn't have enough event points to pay the normal quota.
				 * 2b. We will notify him of this situation. We will also deduce the missing points from other Clan Members. (Sharing is good right? xD) 3. If 1 is Affirmative we will proceed onto the second IF: 3a. Deduction of event points = to what's needed to participate in the event/online clan members. 3b. Notify this situation and inform the player of the amount of points that he/she has at
				 * this very moment.
				 */
				if (individualPrice > member.getEventPoints())
				{
					previousPoints = member.getEventPoints();
					member.setEventPoints(0);
					NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("<tr><td>A total of " + eventPoints + " points have been deduced from your party TOTAL Event Point Score.</td></tr><br>");
					replyMSG.append("<tr><td>You didn't have enough Event Points, so we've used all of your points.</td></tr><br>");
					replyMSG.append("<tr><td>You had " + previousPoints + ", and we needed " + individualPrice + " points.</td></tr><br><br><br>");
					replyMSG.append("<tr><td>Developed by: polbat02 for the L2J community.</td></tr>");
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					member.sendPacket(adminReply);
				}
				else
				{
					previousPoints = member.getEventPoints();
					member.setEventPoints(member.getEventPoints() - individualPrice);
					NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("<tr><td>A total of " + eventPoints + " points have been deduced from your party TOTAL Event Point Score.</td></tr><br>");
					replyMSG.append("<tr><td>You had " + previousPoints + ", and now you have " + (previousPoints - individualPrice) + " points.</td></tr><br><br><br>");
					replyMSG.append("<tr><td>Developed by: polbat02 for the L2J community.</td></tr>");
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					member.sendPacket(adminReply);
				}
			}
		}
		// Party Event --> The same action as in Clan Events Will be taken.
		if (_eventType == 3)
		{
			int pmCount = player.getParty().getMemberCount();
			int individualPrice = eventPoints / pmCount;
			// individualPrice = Math.round(individualPrice);
			for (L2PcInstance member : _participatingPlayers)
			{
				if (member == null) {
					continue;
				}
				if (Config.RAID_SYSTEM_GIVE_BUFFS) {
					L2BufferOpenInstance.makeBuffs(member, buffList, efector, false);
				}
				member.inPartyEvent = true;
				if (individualPrice > member.getEventPoints())
				{
					int previousPoints = member.getEventPoints();
					member.setEventPoints(0);
					NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("<tr><td>A total of " + eventPoints + " points have been deduced from your party TOTAL Event Point Score.</td></tr><br>");
					replyMSG.append("<tr><td>You didn't have enough Event Points, so we've used all of your points.</td></tr><br>");
					replyMSG.append("<tr><td>You had " + previousPoints + ", and we needed " + individualPrice + " points.</td></tr><br><br><br>");
					replyMSG.append("<tr><td>Developed by: Polbat02 //Dragonlance Server.</td></tr>");
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					member.sendPacket(adminReply);
				}
				else
				{
					int previousPoints = member.getEventPoints();
					member.setEventPoints(member.getEventPoints() - individualPrice);
					NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("<tr><td>A total of " + eventPoints + " points have been deduced from your party TOTAL Event Point Score.</td></tr><br>");
					replyMSG.append("<tr><td>You had " + previousPoints + ", and now you have " + (previousPoints - individualPrice) + " points.</td></tr><br><br><br>");
					replyMSG.append("<tr><td>Developed by: Polbat02 //Dragonlance Server.</td></tr>");
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					member.sendPacket(adminReply);
				}
			}
		}
	}

	/**
	 * <b>Starting of the Event</b><br>
	 * This method checks it the total amount of events in process is > than the allowed and acts according to that and the parameters given.<br>
	 * In case X events are already taking place, the void returns and won't let us continue with the event.<br>
	 * This check is not needed since we already check this in L2EventManagerInstance.java, but i'll leave it in here for now since this is a very early stage of developing for now.<br>
	 * More documentation can be found in the Method.<br>
	 *
	 * @param player
	 *            --> Player taking the action.
	 * @param npcId
	 *            --> Event Monster ID.
	 * @param ammount
	 *            --> Amount of Event Monsters
	 * @param type
	 *            --> type of Event.
	 */
	private static void startEvent(L2PcInstance player, int npcId, int ammount)
	{
		if (player == null) {
			return;
		}
		int currentEvents = L2EventManagerInstance._currentEvents;
		if (currentEvents >= Config.RAID_SYSTEM_MAX_EVENTS) {
			return;
		}
		if (currentEvents == 0) {
			return;
		}
		setState(EventState.STARTED);
		// Teleport Player or Members depending on the Event Type.
		doTeleport(player, _locX, _locY, _locZ, 10, false);
		// Spawn The NPC Monster for the Event.
		spawnMonster(npcId, 60, ammount, _npcX, _npcY, _npcZ);
	}

	/**
	 * Teleport the event participants to where the event is going to take place<br>
	 * A function has been created to make it easier for us to teleport the players every time we need them to teleport.<br>
	 * Added suport for different kind of events.
	 *
	 * @param player
	 *            --> Player being teleported.
	 * @param cox
	 *            --> Coord X
	 * @param coy
	 *            --> Coord Y
	 * @param coz
	 *            --> Coord Z
	 * @param delay
	 *            --> Delay to be teleported in
	 * @param removeBuffs
	 *            --> Boolean to removeBuffs uponTeleport or not.
	 */
	private static void doTeleport(L2PcInstance player, int cox, int coy, int coz, int delay, boolean removeBuffs)
	{
		for (L2PcInstance member : _participatingPlayers)
		{
			new L2EventTeleporter(member, cox, coy, coz, delay, removeBuffs);
			member.sendMessage("You will be teleported in 10 Seconds.");
		}
	}

	/**
	 * Spawning function of Event Monsters.<br>
	 * Added Support for multiple spawns and for each one of them being defined as Event Mob.
	 *
	 * @param monsterId
	 *            --> Npc Id
	 * @param respawnTime
	 *            --> Respawn Delay (in most cases this will be 0 as we're gonna cut the respawning of the Mobs upon death).
	 * @param mobCount
	 *            --> MobCount to be spawned.
	 * @param locX
	 *            --> Coordinate X for the mob to be spawned in.
	 * @param locY
	 *            --> Coordinate Y for the mob to be spawned in.
	 * @param locZ
	 *            --> Coordinate Z for the mob to be spawned in.
	 */
	private static void spawnMonster(int monsterId, int respawnDelay, int mobCount, int locX, int locY, int locZ)
	{
		L2NpcTemplate template;
		int monsterTemplate = monsterId;
		template = NpcTable.getInstance().getTemplate(monsterTemplate);
		if (template == null) {
			return;
		}
		_eventMobs = mobCount;
		// Support for multiple spawns.
		if (mobCount > 1)
		{
			int n = 1;
			while (n <= mobCount)
			{
				try
				{
					L2Spawn spawn = new L2Spawn(template);
					// TODO: Add support for different spawning zones.
					spawn.setLocx(locX);
					spawn.setLocy(locY);
					spawn.setLocz(locZ);
					spawn.setAmount(1);
					spawn.setHeading(0);
					spawn.setRespawnDelay(respawnDelay);
					if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null) {
						RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template.getStatsSet().getDouble("baseHpMax"), template.getStatsSet().getDouble("baseMpMax"), false);
					} else {
						SpawnTable.getInstance().addNewSpawn(spawn, false);
					}
					spawn.init();
					/*
					 * Define the properties of every spawn. TODO: Change the Mob statistics according on Event Participants and Server Rates.
					 */
					_lastNpcSpawn = spawn.getLastSpawn();
					_npcSpawn = spawn;
					_lastNpcSpawn.isPrivateEventMob = true;
					_lastNpcSpawn.setChampion(false);
					_lastNpcSpawn.setTitle("Event Monster");
					// Stop the Respawn of the Mob.
					_npcSpawn.stopRespawn();
					_eventMobList.add(_lastNpcSpawn);
					n++;
				}
				catch (Exception e)
				{
					_log.warning("L2EventManager: Exception Upon MULTIPLE NPC SPAWN.");
					e.printStackTrace();
				}
			}
			setState(EventState.PARTICIPATING);
		}
		else
		{
			try
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setLocx(locX);
				spawn.setLocy(locY);
				spawn.setLocz(locZ);
				spawn.setAmount(1);
				spawn.setHeading(0);
				spawn.setRespawnDelay(respawnDelay);
				if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null) {
					RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template.getStatsSet().getDouble("baseHpMax"), template.getStatsSet().getDouble("baseMpMax"), false);
				} else {
					SpawnTable.getInstance().addNewSpawn(spawn, false);
				}
				spawn.init();
				_lastNpcSpawn = spawn.getLastSpawn();
				_npcSpawn = spawn;
				_lastNpcSpawn.isPrivateEventMob = true;
				_lastNpcSpawn.setChampion(false);
				_lastNpcSpawn.setTitle("Event Monster");
				_npcSpawn.stopRespawn();
				_eventMobList.add(_lastNpcSpawn);
			}
			catch (Exception e)
			{
				_log.warning("L2EventManager: Exception Upon SINGLE NPC SPAWN.");
				e.printStackTrace();
			}
			setState(EventState.PARTICIPATING);
		}
		new RaidFightManager();
	}

	/**
	 * Delete the mob from the Event.
	 */
	private static void unSpawnNPC()
	{
		try
		{
			_lastNpcSpawn.deleteMe();
			_npcSpawn.stopRespawn();
			_npcSpawn = null;
			_lastNpcSpawn = null;
		}
		catch (Exception e)
		{
			_log.warning("L2EventManager: Eception Upon NPC UNSPAWN.");
		}
	}

	/**
	 * Function launched at every player death (if he/she's enrolled in any Raid event)
	 *
	 * @param player
	 */
	public static void onPlayerDeath(L2PcInstance player)
	{
		/*
		 * TODO: Add support for: - Configurable Death rebirth system including: - Automatic respawn (Done) - Track deaths for player. - doRevive? (Done)
		 */
		new L2EventTeleporter(player, _locX, _locY, _locZ, 0, false);
		player.setTarget(null);
		player.breakAttack();
		player.breakCast();
		player.doRevive();
	}

	/**
	 * This is the place where we define all the actions that take place after one Event Mob dies. a. Check if that was the last event mob of this instance. b. If not, decrease the number by one. c. Else return true.
	 */
	public static boolean checkPossibleReward()
	{
		if (_eventMobs == 0) {
			return false;
		}
		if (_eventMobs < 1)
		{
			_eventMobs = 0;
			return false;
		}
		if (_eventMobs > 1)
		{
			_eventMobs = _eventMobs - 1;
			return false;
		}
		setState(EventState.REWARDING);
		return true;
	}

	/**
	 * This void picks the rewards and launches the hand out system. It also Ends the event. Added database support for this.
	 *
	 * @param player
	 *            --> Player taking the action.
	 */
	public static void chooseReward(L2PcInstance player)
	{
		if (_eventMobs == 1) {
			_eventMobs = 0;
		} else {
			return;
		}
		loadData(_rewardLevel);
		// Case Single Event
		if (_eventType == 1)
		{
			// Hand Out Items
			handOutItems(player, _first_id, _first_ammount, _second_id, _second_ammount, _event_ammount);
			// Genearal Clean-Up of the Event.
			unSpawnNPC();
			clearFromEvent(player);
			// Teleport back to previous-event location.
			doTeleport(player, _pX, _pY, _pZ, 10, true);
			if (L2EventManagerInstance._currentEvents != 0) {
				L2EventManagerInstance._currentEvents = L2EventManagerInstance._currentEvents - 1;
			}
		}
		// Case Clan Event
		if (_eventType == 2)
		{
			for (L2PcInstance member : _participatingPlayers)
			{
				if (member == null) {
					continue;
				}
				handOutItems(member, _first_id, _first_ammount, _second_id, _second_ammount, _event_ammount);
				doTeleport(member, _pX, _pY, _pZ, 10, true);
			}
			unSpawnNPC();
			clearFromEvent(player);
			if (L2EventManagerInstance._currentEvents != 0) {
				L2EventManagerInstance._currentEvents = L2EventManagerInstance._currentEvents - 1;
			}
		}
		// Case Party Event.
		if (_eventType == 3)
		{
			if (player.getParty() != null)
			{
				for (L2PcInstance member : _participatingPlayers)
				{
					handOutItems(member, _first_id, _first_ammount, _second_id, _second_ammount, _event_ammount);
					doTeleport(member, _pX, _pY, _pZ, 10, true);
				}
			}
			else
			{
				player.sendMessage("You don't have a party anymore?! Well then the rewards go for you only.");
				// Hand Out Items
				handOutItems(player, _first_id, _first_ammount, _second_id, _second_ammount, _event_ammount);
				// General Clean-Up of the Event.
				unSpawnNPC();
				clearFromEvent(player);
				// Teleport back to previous-event location.
				doTeleport(player, _pX, _pY, _pZ, 10, true);
				if (L2EventManagerInstance._currentEvents != 0) {
					L2EventManagerInstance._currentEvents = L2EventManagerInstance._currentEvents - 1;
				}
				return;
			}
			unSpawnNPC();
			clearFromEvent(player);
			if (L2EventManagerInstance._currentEvents != 0) {
				L2EventManagerInstance._currentEvents = L2EventManagerInstance._currentEvents - 1;
			}
		}
		return;
	}

	/**
	 * Custom Definition of the Experience. TODO: Add custom definitions of Experience for different prize lists.
	 */
	public static void expHandOut()
	{
		exp += exp;
		sp += sp;
	}

	/**
	 * Clean the eventStatus from the players.
	 */
	private synchronized static void clearFromEvent(L2PcInstance player)
	{
		setState(EventState.INACTIVATING);
		if (_eventType != 1 && _eventType != 2 && _eventType != 3) {
			return;
		}
		if (_eventType == 1)
		{
			player.inSoloEvent = false;
		}
		if (_eventType == 2)
		{
			if (_participatingPlayers.size() != 0)
			{
				for (L2PcInstance member : _participatingPlayers)
				{
					if (member == null) {
						continue;
					}
					member.inClanEvent = false;
				}
				// Clear Clan Members from event.
				if (_participatingPlayers.size() != 0) {
					_participatingPlayers.clear();
				}
			}
		}
		if (_eventType == 3)
		{
			if (player.getParty() != null)
			{
				player.inPartyEvent = false;
				for (L2PcInstance member : _participatingPlayers)
				{
					if (member == null) {
						continue;
					}
					member.inPartyEvent = false;
				}
			} else {
				player.inPartyEvent = false;
			}
		}
		setState(EventState.INACTIVE);
	}

	/**
	 * Function with which we will hand out event Items.
	 *
	 * @param player
	 * @param item1
	 * @param ammount1
	 * @param item2
	 * @param ammount2
	 * @param eventPoints
	 */
	private static void handOutItems(L2PcInstance player, int item1, int ammount1, int item2, int ammount2, int eventPoints)
	{
		boolean hasItem1 = false;
		boolean hasItem2 = false;
		boolean hasEventPoints = false;
		if (item1 == 0 && item2 == 0 && eventPoints == 0) {
			return;
		}
		if (item1 != 0) {
			hasItem1 = true;
		}
		if (item2 != 0) {
			hasItem2 = true;
		}
		if (eventPoints != 0) {
			hasEventPoints = true;
		}
		PcInventory inv = player.getInventory();
		if (hasItem1)
		{
			if (item1 == 57)
			{
				inv.addAdena("Event - Adena", ammount1, player, player);
				SystemMessage smAdena;
				smAdena = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smAdena.addItemName(57);
				smAdena.addNumber(ammount1);
				player.sendPacket(smAdena);
			}
			else
			{
				if (ItemTable.getInstance().createDummyItem(item1).isStackable()) {
					inv.addItem("Event", item1, ammount1, player, player);
				} else
				{
					for (int i = 0; i <= ammount1 - 1; i++) {
						inv.addItem("Event", item1, ammount1, player, player);
					}
				}
				SystemMessage smItem;
				smItem = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smItem.addItemName(item1);
				smItem.addNumber(ammount1);
				player.sendPacket(smItem);
			}
		}
		if (hasItem2)
		{
			if (item2 == 57)
			{
				inv.addAdena("Event - Adena", ammount2, player, player);
				SystemMessage smAdena;
				smAdena = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smAdena.addItemName(57);
				smAdena.addNumber(ammount2);
				player.sendPacket(smAdena);
			}
			else
			{
				if (ItemTable.getInstance().createDummyItem(item2).isStackable()) {
					inv.addItem("Event", item2, ammount2, player, player);
				} else
				{
					for (int i = 0; i <= ammount2 - 1; i++) {
						inv.addItem("Event", item2, ammount2, player, player);
					}
				}
				SystemMessage smItem;
				smItem = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smItem.addItemName(item2);
				smItem.addNumber(ammount2);
				player.sendPacket(smItem);
			}
		}
		if (hasEventPoints)
		{
			player.setEventPoints(player.getEventPoints() + eventPoints);
			SystemMessage smp;
			smp = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			smp.addString("Event Points ");
			smp.addNumber(2);
			player.sendPacket(smp);
		}
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<tr><td>You won the event!</td></tr><br>");
		replyMSG.append("<tr><td>You have Earned:</td></tr><br>");
		if (hasItem1)
		{
			String item1name = ItemTable.getInstance().createDummyItem(item1).getItemName();
			replyMSG.append("<tr><td>- " + ammount1 + " " + item1name + ".</td></tr><br>");
		}
		if (hasItem2)
		{
			String item2name = ItemTable.getInstance().createDummyItem(item2).getItemName();
			replyMSG.append("<tr><td>- " + ammount2 + " " + item2name + ".</td></tr><br>");
		}
		if (hasEventPoints)
		{
			replyMSG.append("<tr><td>- " + eventPoints + " Event Points.</td></tr><br>");
		}
		replyMSG.append("<br><tr><td>Congratulations!!</td></tr><br><br><br>");
		replyMSG.append("<tr><td>Developed by: Polbat02.</td></tr>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		player.sendPacket(adminReply);
	}

	/**
	 * Hard Finish Event (Case every Body dies)
	 */
	public static void hardFinish()
	{
		for (L2NpcInstance eventMob : _eventMobList)
		{
			eventMob.decayMe();
			eventMob.deleteMe();
			L2EventManagerInstance._currentEvents -= 1;
		}
		_log.warning("Raid Engines: All the Members from the Event are now dead or Have Left The event. Event Finished.");
	}

	/**
	 * Load Data of the prizes for each event. Added DataBase support for this.
	 *
	 * @param prizePackage
	 */
	private static void loadData(int prizePackage)
	{
		Connection con;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT first_prize_id, first_prize_ammount, second_prize_id, second_prize_ammount, event_points_ammount FROM raid_prizes WHERE `prize_package_id` = '" + prizePackage + "'");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_first_id = rset.getInt("first_prize_id");
				_first_ammount = rset.getInt("first_prize_ammount");
				_second_id = rset.getInt("second_prize_id");
				_second_ammount = rset.getInt("second_prize_ammount");
				_event_ammount = rset.getInt("event_points_ammount");
			}
			rset.close();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.severe("Error While loading Raids prizes." + e);
		}
	}

	/**
	 * Sets the Event state<br>
	 * <br>
	 *
	 * @param state
	 * <br>
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}

	/**
	 * Is Event inactive?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isInactive()
	{
		boolean isInactive;
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		return isInactive;
	}

	/**
	 * Is Event in inactivating?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		return isInactivating;
	}

	/**
	 * Is Event in participation?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		return isParticipating;
	}

	/**
	 * Is Event starting?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isStarting()
	{
		boolean isStarting;
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		return isStarting;
	}

	/**
	 * Is Event started?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isStarted()
	{
		boolean isStarted;
		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}
		return isStarted;
	}

	/**
	 * Is Event rewarding?<br>
	 * <br>
	 *
	 * @return boolean<br>
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		return isRewarding;
	}

	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two<br>
	 * <br>
	 *
	 * @param message
	 * <br>
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		for (L2PcInstance player : _participatingPlayers)
		{
			if (player != null) {
				player.sendMessage(message);
			}
		}
	}

	private static void loadSpawns(int eventNum)
	{
		Connection con;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT raid_locX, raid_locY, raid_locZ, player_locX, player_locY, player_locZ " + "FROM raid_event_spawnlist WHERE `id` = '" + eventNum + "'");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_npcX = rset.getInt("raid_locX");
				_npcY = rset.getInt("raid_locY");
				_npcZ = rset.getInt("raid_locZ");
				_locX = rset.getInt("player_locX");
				_locY = rset.getInt("player_locY");
				_locZ = rset.getInt("player_locZ");
			}
			rset.close();
			statement.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.severe("Error While loading Raids Spawn Positions." + e);
		}
	}
}
