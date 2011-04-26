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
package com.l2jfrozen.gameserver.model.actor.instance;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 *
 */

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.TeleportLocationTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());

	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;

	/**
	 * @param template
	 */
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if(condition <= COND_BUSY_BECAUSE_OF_SIEGE)
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if(actualCommand.equalsIgnoreCase("clan_gate"))
		{
			L2PcInstance leader;
			leader = (L2PcInstance) L2World.getInstance().findObject(player.getClan().getLeaderId());
			Castle castle = getCastle();

			if(!castle.isGateOpen())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/teleporter/castleteleporter-nogate.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				html = null;
				return;
			}
			else if(leader.atEvent)
			{
				player.sendMessage("Your leader is in an event.");
				return;
			}/*
			else if(leader == null)
			{
				player.sendMessage("Your Leader is not online.");
				return;
			}*/
			else if(leader.isInJail())
			{
				player.sendMessage("Your leader is in Jail.");
				return;
			}
			else if(leader.isInOlympiadMode())
			{
				player.sendMessage("Your leader is in the Olympiad now.");
				return;
			}
			else if(leader.inObserverMode())
			{
				player.sendMessage("Your leader is in Observ Mode.");
				return;
			}
			else if(leader.isInDuel())
			{
				player.sendMessage("Your leader is in a duel.");
				return;
			}
			else if(leader.isFestivalParticipant())
			{
				player.sendMessage("Your leader is in a festival.");
				return;
			}
			else if(leader.isInParty() && leader.getParty().isInDimensionalRift())
			{
				player.sendMessage("Your leader is in dimensional rift.");
				return;
			}
			else if(leader.getClan() != null && CastleManager.getInstance().getCastleByOwner(leader.getClan()) != null && CastleManager.getInstance().getCastleByOwner(leader.getClan()).getSiege().getIsInProgress())
			{
				player.sendMessage("Your leader is in siege, you can't go to your leader.");
				return;
			}
			else if(player.isClanLeader())
			{
				player.sendMessage("Your are The Leader.");
				return;
			}

			player.teleToLocation(castle.getGateX(), castle.getGateY(), castle.getGateZ());
			player.sendMessage("You have been teleported to your leader.");
			player.stopMove(new L2CharPosition(castle.getGateX(), castle.getGateY(), castle.getGateZ(), player.getHeading()));
			player.sendPacket(ActionFailed.STATIC_PACKET);

			leader = null;
			castle = null;
		}

		if(actualCommand.equalsIgnoreCase("goto"))
		{
			if(st.countTokens() <= 0)
				return;
			int whereTo = Integer.parseInt(st.nextToken());
			if(condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if(condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if(st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				if(10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}
				return;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}

		st = null;
		actualCommand = null;
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if(val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "data/html/teleporter/" + pom + ".htm";
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";

		int condition = validateCondition(player);
		if(condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if(condition > COND_ALL_FALSE)
		{
			if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			}
			else if(condition == COND_OWNER)
			{
				filename = "data/html/teleporter/" + getNpcId() + ".htm"; // Owner message window
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);

		filename = null;
		html = null;
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if(list != null)
		{
			if(player.reduceAdena("Teleport", list.getPrice(), player.getLastFolkNPC(), true))
			{
				if(Config.DEBUG)
				{
					_log.fine("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}

				// teleport
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
				player.stopMove(new L2CharPosition(list.getLocX(), list.getLocY(), list.getLocZ(), player.getHeading()));
			}
			list = null;
		}
		else
		{
			_log.warning("No teleport destination with id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private int validateCondition(L2PcInstance player)
	{
		if(player.getClan() != null && getCastle() != null)
		{
			//if (getCastle().getSiege().getIsInProgress())
			//	return COND_BUSY_BECAUSE_OF_SIEGE;                    // Busy because of siege
			//else
			if(getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				return COND_OWNER; // Owner
		}

		return COND_ALL_FALSE;
	}
}
