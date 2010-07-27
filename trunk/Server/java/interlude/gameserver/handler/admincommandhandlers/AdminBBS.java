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
import interlude.gameserver.communitybbs.Manager.AdminBBSManager;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;

public class AdminBBS implements IAdminCommandHandler
{
	// private static Logger _log =
	// Logger.getLogger(AdminKick.class.getName());
	private static final String[] ADMIN_COMMANDS = { "admin_bbs" };
	private static final int REQUIRED_LEVEL = Config.GM_MIN;

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, interlude.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				// System.out.println("Not required level");
				return false;
			}
		}
		AdminBBSManager.getInstance().parsecmd(command, activeChar);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return level >= REQUIRED_LEVEL;
	}
}