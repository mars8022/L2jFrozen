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
package interlude.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.Fort;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Vice [L2JOneo]
 */
public class AdminFort implements IAdminCommandHandler
{
	// private static Logger _log =
	// Logger.getLogger(AdminSiege.class.getName());
	private static final String[] ADMIN_COMMANDS = { "admin_fort", "admin_add_fort_attacker", "admin_add_fort_defender", "admin_add_fort_guard", "admin_list_fortsiege_clans", "admin_clear_fortsiege_list", "admin_move_fort_defenders", "admin_spawn_fort_doors", "admin_endfortsiege", "admin_startfortsiege", "admin_setfort", "admin_removefort" };
	private static final int REQUIRED_LEVEL = Config.GM_FORTSIEGE;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN) {
			if (activeChar.getAccessLevel() < REQUIRED_LEVEL || !activeChar.isGM())
			{
				return false;
			}
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		// Get fort
		Fort fort = null;
		if (st.hasMoreTokens()) {
			fort = FortManager.getInstance().getFort(st.nextToken());
		}
		// Get fort
		String val = "";
		if (st.hasMoreTokens()) {
			val = st.nextToken();
		}
		if (fort == null || fort.getFortId() < 0) {
			// No fort specified
			showFortSelectPage(activeChar);
		} else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance) {
				player = (L2PcInstance) target;
			}
			if (command.equalsIgnoreCase("admin_add_fort_attacker"))
			{
				if (player == null) {
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				} else {
					fort.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_fort_defender"))
			{
				if (player == null) {
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				} else {
					fort.getSiege().registerDefender(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_fort_guard"))
			{
				try
				{
					int npcId = Integer.parseInt(val);
					fort.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //add_fort_guard npcId");
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				fort.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_fort_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if (player == null || player.getClan() == null) {
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				} else {
					fort.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				L2Clan clan = ClanTable.getInstance().getClan(fort.getOwnerId());
				if (clan != null) {
					fort.removeOwner(clan);
				} else {
					activeChar.sendMessage("Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fort_doors"))
			{
				fort.spawnDoor();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			showSiegePage(activeChar, fort.getName());
		}
		return true;
	}

	private void showFortSelectPage(L2PcInstance activeChar)
	{
		int i = 0;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/forts.htm");
		TextBuilder cList = new TextBuilder();
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort != null)
			{
				String name = fort.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_fort " + name + "\">" + name + "</a></td>");
				i++;
			}
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%forts%", cList.toString());
		cList.clear();
		activeChar.sendPacket(adminReply);
	}

	private void showSiegePage(L2PcInstance activeChar, String fortName)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fortName);
		activeChar.sendPacket(adminReply);
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
