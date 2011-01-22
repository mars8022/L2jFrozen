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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.CrestCache;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @version $Revision: 1.1 $
 * @author ProGramMoS
 */
public class AdminCache implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_cache_htm_rebuild",
			"admin_cache_htm_reload",
			"admin_cache_reload_path",
			"admin_cache_reload_file",
			"admin_cache_crest_rebuild",
			"admin_cache_crest_reload",
			"admin_cache_crest_fix"
	};

	private enum CommandEnum
	{
		admin_cache_htm_rebuild,
		admin_cache_htm_reload,
		admin_cache_reload_path,
		admin_cache_reload_file,
		admin_cache_crest_rebuild,
		admin_cache_crest_reload,
		admin_cache_crest_fix
	}

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){
			return false;
		}
		
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
		*/
		

		StringTokenizer st = new StringTokenizer(command," ");

		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if(comm == null)
			return false;
		
		switch(comm)
		{
			case admin_cache_htm_reload:
			case admin_cache_htm_rebuild:
				HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
				activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
				return true;

			case admin_cache_reload_path:
				if(st.hasMoreTokens()){
					String path = st.nextToken();
					HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
					path = null;
					return true;
				}else{
					
					activeChar.sendMessage("Usage: //cache_reload_path <path>");
					return false;
				}
			case admin_cache_reload_file:
				
				if(st.hasMoreTokens()){
					
					String path = st.nextToken();
					if(HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null)
					{
						activeChar.sendMessage("Cache[HTML]: file was loaded");
						path = null;
					}
					else
					{
						activeChar.sendMessage("Cache[HTML]: file can't be loaded");
						path = null;
					}
					return true;
				}else{
					activeChar.sendMessage("Usage: //cache_reload_file <relative_path/file>");
					return false;
				}
				
			case admin_cache_crest_rebuild:
			case admin_cache_crest_reload:
				CrestCache.getInstance().reload();
				activeChar.sendMessage("Cache[Crest]: " + String.format("%.3f", CrestCache.getInstance().getMemoryUsage()) + " megabytes on " + CrestCache.getInstance().getLoadedFiles() + " files loaded");
				return true;

			case admin_cache_crest_fix:
				CrestCache.getInstance().convertOldPedgeFiles();
				activeChar.sendMessage("Cache[Crest]: crests fixed");
				return true;
			default:{
				return false;
			}
		}

	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
