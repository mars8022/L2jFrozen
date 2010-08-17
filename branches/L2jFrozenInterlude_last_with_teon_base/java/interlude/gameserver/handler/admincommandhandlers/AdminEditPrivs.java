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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import interlude.Config;
import interlude.gameserver.handler.AdminCommandHandler;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.GmAudit;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Vice [L2JOneo]
 */
public class AdminEditPrivs implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	private static final String[] ADMIN_COMMANDS = { "admin_edit_priv", "admin_view_priv" };
	private static final int REQUIRED_LEVEL = Config.GM_PRIV_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_PRIV_VIEW;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM()) {
			return false;
		}
		new GmAudit(activeChar.getName(), activeChar.getObjectId(), (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), command);
		if (command.startsWith("admin_edit_priv"))
		{
			if (!Config.ALT_PRIVILEGES_ADMIN) {
				if (!checkLevel(activeChar.getAccessLevel())) {
					return false;
				}
			}
			try
			{
				if (Config.DEBUG) {
					_log.info(command);
				}
				savePrivs(activeChar, command);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_view_priv"))
		{
			if (!Config.ALT_PRIVILEGES_ADMIN) {
				if (!checkLevel2(activeChar.getAccessLevel())) {
					return false;
				}
			}
			try
			{
				String val = command.substring(16);
				int page = Integer.parseInt(val);
				Show_Privileges(activeChar, page);
			}
			catch (Exception e)
			{
				if (Config.DEBUG) {
					_log.info(e.getMessage());
				}
				activeChar.sendMessage("Wrong usage: //edit_priv pagenumber");
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void Show_Privileges(L2PcInstance activeChar, int page)
	{
		// Load the settings from file
		Properties Settings = new Properties();
		try
		{
			Settings = new Properties();
			InputStream is = new FileInputStream(Config.COMMAND_PRIVILEGES_FILE);
			Settings.load(is);
			is.close();
		}
		catch (Exception e)
		{
			return;
		}
		List<String> allPrivs = new ArrayList<String>(Settings.size());
		for (Enumeration e = Settings.propertyNames(); e.hasMoreElements();)
		{
			allPrivs.add(String.valueOf(e.nextElement()));
		}
		Collections.sort(allPrivs);
		String[] privs = allPrivs.toArray(new String[allPrivs.size()]);
		int MaxPrivsPerPage = 20;
		int MaxPages = Settings.size() / MaxPrivsPerPage;
		if (Settings.size() > MaxPrivsPerPage * MaxPages)
		{
			MaxPages++;
		}
		int PrivStart = MaxPrivsPerPage * page;
		int PrivEnd = Settings.size();
		if (PrivEnd - PrivStart > MaxPrivsPerPage)
		{
			PrivEnd = PrivStart + MaxPrivsPerPage;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/editpriv.htm");
		TextBuilder replyMSG = new TextBuilder();
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_view_priv " + x + "\">Page " + pagenr + "</a></center>");
		}
		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG.clear();
		for (int i = PrivStart; i < PrivEnd; i++)
		{
			replyMSG.append("<tr><td>" + privs[i].substring(6) + " (" + Settings.get(privs[i]) + "):</td>");
			replyMSG.append("<td><edit var=\"" + privs[i] + "\"></td>");
			replyMSG.append("<td><button value=\"Save\" width=35 action=\"bypass -h admin_edit_priv " + privs[i]);
			replyMSG.append(" \\$" + privs[i] + "\" height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		}
		adminReply.replace("%privlist%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void savePrivs(L2PcInstance activeChar, String command)
	{
		String[] commandSplit = command.split(" ");
		if (commandSplit.length < 3) {
			return;
		}
		if (replaceCommandPriv(commandSplit[1], Integer.parseInt(commandSplit[2])))
		{
			// Update also in memory
			AdminCommandHandler.getInstance().setPrivilegeValue(activeChar, commandSplit[1], Integer.parseInt(commandSplit[2]));
		}
	}

	private boolean replaceCommandPriv(String commandName, int newValue)
	{
		String line;
		FastList<String> fs = new FastList<String>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(Config.COMMAND_PRIVILEGES_FILE));
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith(commandName + " "))
				{
					line = commandName + " = " + newValue;
				}
				fs.add(line);
			} // end while
			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(Config.COMMAND_PRIVILEGES_FILE));
			for (String l : fs)
			{
				bw.write(l);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			return true;
		} // end try
		catch (IOException ex)
		{
			return false;
		}
	}

	private boolean checkLevel(int level)
	{
		return level >= REQUIRED_LEVEL;
	}

	private boolean checkLevel2(int level)
	{
		return level >= REQUIRED_LEVEL2;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
