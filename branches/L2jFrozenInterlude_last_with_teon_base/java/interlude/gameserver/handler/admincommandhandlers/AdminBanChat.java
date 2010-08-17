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

import interlude.Config;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.GmAudit;

/**
 * This class handles following admin commands: - admin_banchat = Imposes a chat ban on the specified player. - admin_unbanchat = Removes any chat ban on the specified player. Uses: admin_banchat [<player_name>] [<time_in_seconds>] admin_banchat [<player_name>] [<time_in_seconds>] [<ban_chat_reason>] admin_unbanchat [<player_name>]
 *
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBanChat implements IAdminCommandHandler
{
	// private static Logger _log =
	// Logger.getLogger(AdminBan.class.getName());
	private static final String[] ADMIN_COMMANDS = { "admin_banchat", "admin_unbanchat" };
	private static final int REQUIRED_LEVEL = Config.GM_BAN_CHAT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!checkLevel(activeChar.getAccessLevel()))
			{
				System.out.println("Not required level.");
				return false;
			}
		}
		String[] cmdParams = command.split(" ");
		// checking syntax
		if (cmdParams.length < 3 && command.startsWith("admin_banchat"))
		{
			activeChar.sendMessage("BanChat Syntax:");
			activeChar.sendMessage("  //banchat [<player_name>] [<time_in_seconds>]");
			activeChar.sendMessage("  //banchat [<player_name>] [<time_in_seconds>] [<ban_chat_reason>]");
			return false;
		}
		else if (cmdParams.length < 2 && command.startsWith("admin_unbanchat"))
		{
			activeChar.sendMessage("UnBanChat Syntax:");
			activeChar.sendMessage("  //unbanchat [<player_name>]");
			return false;
		}
		// void vars
		long banLength = -1;
		String banReason = "";
		L2PcInstance targetPlayer = null;
		// chat instance
		targetPlayer = L2World.getInstance().getPlayer(cmdParams[1]);
		if (targetPlayer == null)
		{
			activeChar.sendMessage("Incorrect parameter or target.");
			return false;
		}
		// what is our actions?
		if (command.startsWith("admin_banchat"))
		{
			// ban chat length (seconds)
			try
			{
				banLength = Integer.parseInt(cmdParams[2]);
			}
			catch (NumberFormatException nfe)
			{
			}
			// ban chat reason
			if (cmdParams.length > 3)
			{
				banReason = cmdParams[3];
			}
			// apply ban chat
			activeChar.sendMessage(targetPlayer.getName() + "'s chat is banned for " + banLength + " seconds.");
			targetPlayer.setChatBanned(true, banLength, banReason);
		}
		else if (command.startsWith("admin_unbanchat"))
		{
			activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
			targetPlayer.setChatBanned(false, 0, "");
		}
		new GmAudit(activeChar.getName(), activeChar.getObjectId(), targetPlayer.getName(), command);
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return level >= REQUIRED_LEVEL;
	}
}
