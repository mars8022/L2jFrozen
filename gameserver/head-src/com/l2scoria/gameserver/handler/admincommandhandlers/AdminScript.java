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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.IAdminCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author KidZor
 */

public class AdminScript implements IAdminCommandHandler
{
	private static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
	private static final Logger _log = Logger.getLogger(AdminScript.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_load_script"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

		if(Config.GMAUDIT)
		{
			Logger _logAudit = Logger.getLogger("gmaudit");
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}

		if(command.startsWith("admin_load_script"))
		{
			File file;
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String line = st.nextToken();

			try
			{
				file = new File(SCRIPT_FOLDER, line);

				if(file.isFile())
				{
					try
					{
						L2ScriptEngineManager.getInstance().executeScript(file);
					}
					catch(ScriptException e)
					{
						L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
					}
				}
				else
				{
					_log.warning("Failed loading: (" + file.getCanonicalPath() + " - Reason: doesnt exists or is not a file.");
				}
			}
			catch(Exception e)
			{
				//null
			}
			st = null;
		}

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
