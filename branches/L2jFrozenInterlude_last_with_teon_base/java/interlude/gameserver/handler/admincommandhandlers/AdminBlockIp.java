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
import java.util.logging.Logger;

import interlude.gameserver.LoginServerThread;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>admin_unblockip</li>
 * </ul>
 *
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBlockIp implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
	private static final String[] ADMIN_COMMANDS = { "admin_banip", "admin_unblockip" };

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, interlude.gameserver.model.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_banip"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String ip = st.nextToken();
				String duration = st.nextToken();
				BanIp(activeChar, ip, Integer.parseInt(duration));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //banip ip duration");
			}
		}
		else if (command.startsWith("admin_unblockip "))
		{
			try
			{
				String ipAddress = command.substring(16);
				if (unblockIp(ipAddress, activeChar))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Removed IP " + ipAddress + " from blocklist!");
					activeChar.sendPacket(sm);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Send syntax to the user
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Usage mode: //unblockip <ip>");
				activeChar.sendPacket(sm);
			}
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean unblockIp(String ipAddress, L2PcInstance activeChar)
	{
		// LoginServerThread.getInstance().unBlockip(ipAddress);
		_log.warning("IP removed by GM " + activeChar.getName());
		return true;
	}

	private boolean BanIp(L2PcInstance activeChar, String ip, int duration)
	{
		LoginServerThread.getInstance().sendIpBan(ip, duration * 60000L);
		activeChar.sendMessage("Ip: " + ip + ". baned for " + duration / 60000L + " minutes.");
		return true;
	}
}
