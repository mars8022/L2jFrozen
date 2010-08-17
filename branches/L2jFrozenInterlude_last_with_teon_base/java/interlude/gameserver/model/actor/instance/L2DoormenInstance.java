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
package interlude.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.StringTokenizer;

import javolution.util.FastList;

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2NpcInstance
{
	private ClanHall _clanHall;
	private static int COND_ALL_FALSE = 0;
	private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static int COND_CASTLE_OWNER = 2;
	private static int COND_HALL_OWNER = 3;
	private static int COND_FORT_OWNER = 4;

	/**
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	public final ClanHall getClanHall()
	{
		if (_clanHall == null)
			_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);

		return _clanHall;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;

		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;

		else if (condition == COND_CASTLE_OWNER || condition == COND_HALL_OWNER || condition == COND_FORT_OWNER)
		{
			if (command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if (command.startsWith("open_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(true);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"FF9955\">opened</font> the clan hall door.<br>Outsiders may enter the clan hall while the door is open. Please close it when you've finished your business.<br><center><button value=\"Close\" action=\"bypass -h npc_" + getObjectId()
							+ "_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					// DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					if (condition == 2)
					{
						while (st.hasMoreTokens())
						{
							getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
				else if (condition == COND_FORT_OWNER)
				{
					// DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					if (condition == 4)
					{
						while (st.hasMoreTokens())
						{
							getFort().openDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
			}
			else if (command.startsWith("close_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(), "<html><body>You have <font color=\"FF9955\">closed</font> the clan hall door.<br>Good day!<br><center><button value=\"To Beginning\" action=\"bypass -h npc_" + getObjectId() + "_Chat\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					// DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					// L2Clan playersClan = player.getClan();
					if (condition == 2)
					{
						while (st.hasMoreTokens())
						{
							getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
				else if (condition == COND_FORT_OWNER)
				{
					// DoorTable doorTable = DoorTable.getInstance();
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid
					// L2Clan playersClan = player.getClan();
					if (condition == 4)
					{
						while (st.hasMoreTokens())
						{
							getFort().closeDoor(player, Integer.parseInt(st.nextToken()));
						}
						return;
					}
				}
			}
			else if (condition == COND_FORT_OWNER && command.startsWith("open_near_doors"))
			{
				for (L2DoorInstance door : getKnownDoors(player))
					door.openMe();
			}
			else if (condition == COND_FORT_OWNER && command.startsWith("close_near_doors"))
			{
				for (L2DoorInstance door : getKnownDoors(player))
					door.closeMe();
			}
		}
		super.onBypassFeedback(player, command);
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) {
			return;
		}
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";
		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE) {
			filename = "data/html/doormen/" + getTemplate().npcId + "-busy.htm"; // Busy because of siege
		} else if (condition == COND_CASTLE_OWNER || condition == COND_FORT_OWNER) {
			filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window
		}
		// Prepare doormen for clan hall
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		if (getClanHall() != null)
		{
			if (condition == COND_HALL_OWNER)
			{
				str = "<html><body>Greetings!<br><br><font color=\"00FFFF\">" + ClanTable.getInstance().getClan(getClanHall().getOwnerId()).getName() + "</font> I am honored to serve your clan.<br>How may i assist you?<br>";
				str += "<center><br><button value=\"Open Door\" action=\"bypass -h npc_%objectId%_open_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>";
				str += "<button value=\"Close Door\" action=\"bypass -h npc_%objectId%_close_doors\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
			}
			else
			{
				L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
				if (owner != null && owner.getLeader() != null)
				{
					str = "<html><body>Hello there!<br>This clan hall is owned by <font color=\"55FFFF\">" + owner.getLeader().getName() + " who is the Lord of the ";
					str += owner.getName() + "</font> clan.<br>";
					str += "I am sorry, but only the clan members who belong to the " + owner.getName() + " clan can enter the clan hall.</body></html>";
				}
				else
				{
					int ClanHallID = _clanHall.getId();
					if (ClanHallID == 21 || ClanHallID == 34 || ClanHallID == 35 || ClanHallID == 62 || ClanHallID == 63 || ClanHallID == 64) {
						str = "<html><body>Clan Hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> has no owner. <br> To get this clan hall must visit the messenger</body></html>";
					} else {
						str = "<html><body>Clan Hall <font color=\"LEVEL\">" + getClanHall().getName() + "</font> has no owner. <br> To purchase, go to the Auction.</body></html>";
					}
				}
			}
			html.setHtml(str);
		} else {
			html.setFile(filename);
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (player.getClan() != null)
		{
			// Prepare doormen for clan hall
			if (getClanHall() != null)
			{
				if (player.getClanId() == getClanHall().getOwnerId()) {
					return COND_HALL_OWNER;
				} else {
					return COND_ALL_FALSE;
				}
			}
			if (getCastle() != null && getCastle().getCastleId() > 0)
			{
				// if (getCastle().getSiege().getIsInProgress())
				// return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				// else
				if (getCastle().getOwnerId() == player.getClanId()) {
					return COND_CASTLE_OWNER; // Owner
				}
			}
			if (getFort() != null && getFort().getFortId() > 0)
			{
				// if (getFort().getSiege().getIsInProgress())
				// return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				// else
				if (getFort().getOwnerId() == player.getClanId()) {
					return COND_FORT_OWNER; // Owner
				}
			}
		}
		return COND_ALL_FALSE;
	}
	
	/**
	 * Funtion get the doors near the Doormen
	 * can be open by the player
	 * @param player
	 */
	private Collection<L2DoorInstance> getKnownDoors(L2PcInstance player)
	{
		//Container
		FastList<L2DoorInstance> _doors = new FastList<L2DoorInstance>();

		// Get all objects in the doorman knownlist and select the doors
		for (L2Object object : getKnownList().getKnownObjects().values())
		{
			if (object instanceof L2DoorInstance)
			{
				L2DoorInstance door = null;
				door = (L2DoorInstance) object;

				if (door != null)
				{
					if (door.getCastle() != null && door.getCastle().getOwnerId() == player.getClanId() || door.getFort() != null
							&& door.getFort().getOwnerId() == player.getClanId())
						_doors.add(door);
				}
			}
		}
		return _doors;
	}
}