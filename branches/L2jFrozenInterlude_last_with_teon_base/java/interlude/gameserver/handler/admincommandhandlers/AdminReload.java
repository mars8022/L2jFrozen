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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import interlude.Config;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.NpcWalkerRoutesTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.TeleportLocationTable;
import interlude.gameserver.datatables.dbmanager;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.instancemanager.Manager;
import interlude.gameserver.instancemanager.QuestManager;
import interlude.gameserver.model.L2Multisell;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.script.faenor.FaenorScriptEngine;
import interlude.gameserver.scripting.CompiledScriptCache;
import interlude.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author KidZor
 */
public class AdminReload implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_reload" };

	@SuppressWarnings("static-access")
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_reload"))
		{
			sendReloadPage(activeChar);
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				if (type.equals("multisell"))
				{
					L2Multisell.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Multisell reloaded.");
				}
				else if (type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Teleport location table reloaded.");
				}
				else if (type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Skills reloaded.");
				}
				else if (type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Npcs reloaded.");
				}
				else if (type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
				}
				else if (type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Item templates reloaded");
				}
				else if (type.startsWith("instancemanager"))
				{
					Manager.reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All instance manager has been reloaded");
				}
				else if (type.startsWith("npcwalkers"))
				{
					NpcWalkerRoutesTable.getInstance().load();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All NPC walker routes have been reloaded");
				}
				else if (type.startsWith("npcbuffers"))
				{
					dbmanager.reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All Buffer skills tables have been reloaded");
				}
				else if (type.equals("configs"))
				{
					Config.load();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Server Config Reloaded.");
				}
				else if (type.equals("dbs"))
				{
					dbmanager.reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("ItemTable reloaded.");
					activeChar.sendMessage("SkillTable reloaded.");
					activeChar.sendMessage("BufferSkillsTable reloaded.");
					activeChar.sendMessage("NpcBufferSkillIdsTable reloaded.");
					activeChar.sendMessage("GmListTable reloaded.");
					activeChar.sendMessage("ClanTable reloaded.");
					activeChar.sendMessage("AugmentationData reloaded.");
					activeChar.sendMessage("HelperBuffTable reloaded.");
				}
				else if (type.startsWith("scripts"))
				{
					try
					{
						File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
						if (!Config.ALT_DEV_NO_QUESTS)
							L2ScriptEngineManager.getInstance().executeScriptList(scripts);
					}
					catch (IOException ioe)
					{
						activeChar.sendMessage("Failed loading scripts.cfg, no script going to be loaded");
						ioe.printStackTrace();
					}
					try
					{
						CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
						if (compiledScriptCache == null)
						{
							activeChar.sendMessage("Compiled Scripts Cache is disabled.");
						}
						else
						{
							compiledScriptCache.purge();
							if (compiledScriptCache.isModified())
							{
								compiledScriptCache.save();
								activeChar.sendMessage("Compiled Scripts Cache was saved.");
							}
							else
							{
								activeChar.sendMessage("Compiled Scripts Cache is up-to-date.");
							}
						}
					}
					catch (IOException e)
					{
						activeChar.sendMessage( "Failed to store Compiled Scripts Cache.");
						e.printStackTrace();
					}
					QuestManager.getInstance().reloadAllQuests();
					QuestManager.getInstance().report();
					FaenorScriptEngine.getInstance().reloadPackages();
					
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage:  //reload <type>");
			}
		}
		return true;
	}

	/**
	 * send reload page
	 *
	 * @param admin
	 */
	private void sendReloadPage(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "reload_menu.htm");
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}