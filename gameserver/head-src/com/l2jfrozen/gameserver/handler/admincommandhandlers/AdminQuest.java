/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class AdminQuest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_quest_reload"
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, com.l2jfrozen.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		// syntax will either be:
		// quest_reload <id>
		// quest_reload <questName>
		// The questName MUST start with a non-numeric character for this to work,
		// regardless which of the two formats is used.
		// Example: //quest_reload orc_occupation_change_1
		// Example: //quest_reload chests
		// Example: //quest_reload SagasSuperclass
		// Example: //quest_reload 12
		if (command.startsWith("admin_quest_reload"))
		{
			String[] parts = command.split(" ");
			
			if (parts.length < 2)
			{
				activeChar.sendMessage("Syntax: //quest_reload <questFolder>.<questSubFolders...>.questName> or //quest_reload <id>");
			}
			else
			{
				// try the first param as id
				try
				{
					final int questId = Integer.parseInt(parts[1]);
					
					if (QuestManager.getInstance().reload(questId))
					{
						activeChar.sendMessage("Quest Reloaded Successfully.");
					}
					else
					{
						activeChar.sendMessage("Quest Reloaded Failed");
					}
				}
				catch (final NumberFormatException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					if (QuestManager.getInstance().reload(parts[1]))
					{
						activeChar.sendMessage("Quest Reloaded Successfully.");
					}
					else
					{
						activeChar.sendMessage("Quest Reloaded Failed");
					}
				}
			}
			
			parts = null;
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
