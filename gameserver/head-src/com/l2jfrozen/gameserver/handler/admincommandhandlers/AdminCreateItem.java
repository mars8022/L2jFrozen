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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items
 * with respective id, if num is not specified, assumes 1.
 * 
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_itemcreate", "admin_create_item"
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

		if(command.equals("admin_itemcreate"))
		{
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		else if(command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);

				if(st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					createItem(activeChar, idval, numval);
				}
				else if(st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, idval, 1);
				}

				val = null;
				st = null;
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //itemcreate <itemId> [amount]");
			}
			catch(NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}

			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void createItem(L2PcInstance activeChar, int id, int num)
	{
		if(num > 20)
		{
			L2Item template = ItemTable.getInstance().getTemplate(id);

			if(!template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}

			template = null;
		}

		L2PcInstance Player = null;

		if(activeChar.getTarget() != null)
		{
			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				Player = (L2PcInstance) activeChar.getTarget();
			}
			else
			{
				activeChar.sendMessage("You can add an item only to a character.");
				return;
			}
		}

		if(Player == null)
		{
			activeChar.setTarget(activeChar);
			Player = activeChar;
		}

		Player.getInventory().addItem("Admin", id, num, Player, null);
		ItemList il = new ItemList(Player, true);
		Player.sendPacket(il);
		if(activeChar.getName() == Player.getName())
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		else
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in " + Player.getName() + "'s inventory.");
			Player.sendMessage("Admin have spawned " + num + " item(s) number " + id + " in your inventory.");
		}

		Player = null;
		il = null;
	}
}
