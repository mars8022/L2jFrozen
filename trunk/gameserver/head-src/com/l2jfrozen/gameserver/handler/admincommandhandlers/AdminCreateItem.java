/*
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

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminCreateItem.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_l2jfrozen",
		"admin_itemcreate",
		"admin_create_item",
		"admin_mass_create",
		"admin_clear_inventory"
	};
	
	private enum CommandEnum
	{
		admin_l2jfrozen,
		admin_itemcreate,
		admin_create_item,
		admin_mass_create,
		admin_clear_inventory
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
			return false;
		
		switch (comm)
		{
		
		/*
		 * Command //l2jfrozen, it gives useful items to Gm character Crystals, Gemstones, Bss-ss, scrolls, elixirs, etc To be complete...
		 */
			case admin_l2jfrozen:
				
				// Command usable only by Administrator
				if (activeChar.getAccessLevel().getLevel() != 1 || !activeChar.isGM())
				{
					activeChar.sendMessage("Only Administrators can use this command!");
					return false;
				}
				
				L2PcInstance Player = null;
				// Items can be added only to self Gm
				if (Player == null)
				{
					activeChar.setTarget(activeChar);
					Player = activeChar;
				}
				
				Player.getInventory().addItem("Admin", 1458, 5000, Player, activeChar); // Cry d
				Player.getInventory().addItem("Admin", 1459, 5000, Player, activeChar); // Cry c
				Player.getInventory().addItem("Admin", 1460, 5000, Player, activeChar); // Cry b
				Player.getInventory().addItem("Admin", 1461, 5000, Player, activeChar); // Cry a
				Player.getInventory().addItem("Admin", 1462, 5000, Player, activeChar); // Cry s
				
				Player.getInventory().addItem("Admin", 2130, 200, Player, activeChar); // Gem d
				Player.getInventory().addItem("Admin", 2131, 200, Player, activeChar); // Gem c
				Player.getInventory().addItem("Admin", 2132, 200, Player, activeChar); // Gem b
				Player.getInventory().addItem("Admin", 2133, 200, Player, activeChar); // Gem a
				Player.getInventory().addItem("Admin", 2134, 200, Player, activeChar); // Gem s
				
				Player.getInventory().addItem("Admin", 736, 10, Player, activeChar); // Scroll of Escape
				Player.getInventory().addItem("Admin", 737, 10, Player, activeChar); // Scroll of Resurrection
				Player.getInventory().addItem("Admin", 1538, 10, Player, activeChar); // Blessed Scroll of Escape
				Player.getInventory().addItem("Admin", 1829, 10, Player, activeChar); // Scroll of Escape: Clan Hall
				Player.getInventory().addItem("Admin", 1830, 10, Player, activeChar); // Scroll of Escape: Castle
				Player.getInventory().addItem("Admin", 3936, 10, Player, activeChar); // Blessed Scroll of Resurrection
				Player.getInventory().addItem("Admin", 5858, 10, Player, activeChar); // Blessed Scroll of Escape: Clan Hall
				Player.getInventory().addItem("Admin", 5859, 10, Player, activeChar); // Blessed Scroll of Escape: Castle
				
				Player.getInventory().addItem("Admin", 1467, 1000, Player, activeChar); // Soulshot: S-grade
				Player.getInventory().addItem("Admin", 2514, 1000, Player, activeChar); // Spiritshot: S-grade
				Player.getInventory().addItem("Admin", 3952, 1000, Player, activeChar); // Blessed Spiritshot: S Grade
				
				Player.getInventory().addItem("Admin", 8627, 10, Player, activeChar); // Elixir of Life (S-Grade)
				Player.getInventory().addItem("Admin", 8633, 10, Player, activeChar); // Elixir of Mental Strength (S-Grade)
				Player.getInventory().addItem("Admin", 8639, 10, Player, activeChar); // Elixir of CP (S-Grade)
				
				Player.getInventory().addItem("Admin", 8874, 10, Player, activeChar); // Einhasad's Holy Water
				
				final ItemList il = new ItemList(Player, true);
				Player.sendPacket(il);
				activeChar.sendMessage("Items added successfully!");
				
				activeChar.addSkill(SkillTable.getInstance().getInfo(7029, 4), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7041, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7042, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7043, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7044, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7045, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7046, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7047, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7048, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7049, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7050, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7051, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7052, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7053, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7054, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7055, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7056, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7057, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7058, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7059, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7058, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7059, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7060, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7061, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7062, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7063, 1), true);
				activeChar.addSkill(SkillTable.getInstance().getInfo(7064, 1), true);
				
				activeChar.sendSkillList();
				activeChar.sendMessage("Gm skills added successfully!");
				
				return true;
				
			case admin_itemcreate:
				
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				return true;
				
			case admin_create_item:
				
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						
						int idval = 0;
						int numval = 0;
						
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (final NumberFormatException e)
						{
							
							activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						
						if (idval > 0 && numval > 0)
						{
							createItem(activeChar, idval, numval);
							return true;
						}
						activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						
						try
						{
							idval = Integer.parseInt(id);
							
						}
						catch (final NumberFormatException e)
						{
							
							activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						
						if (idval > 0)
						{
							createItem(activeChar, idval, 1);
							return true;
						}
						activeChar.sendMessage("Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
					// activeChar.sendMessage("Usage: //itemcreate <itemId> [amount]");
					return true;
				}
				return false;
				
			case admin_mass_create:
				
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						
						int idval = 0;
						int numval = 0;
						
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (final NumberFormatException e)
						{
							activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
							return false;
						}
						
						if (idval > 0 && numval > 0)
						{
							massCreateItem(activeChar, idval, numval);
							return true;
						}
						activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						
						try
						{
							idval = Integer.parseInt(id);
							
						}
						catch (final NumberFormatException e)
						{
							activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
							return false;
						}
						
						if (idval > 0)
						{
							massCreateItem(activeChar, idval, 1);
							return true;
						}
						activeChar.sendMessage("Usage: //mass_create <itemId> <amount>");
						return false;
					}
				}
				return false;
				
			case admin_clear_inventory:
				
				removeAllItems(activeChar);
				return true;
				
			default:
				return false;
		}
		
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(final L2PcInstance activeChar, final int id, final int num)
	{
		if (num > 20)
		{
			L2Item template = ItemTable.getInstance().getTemplate(id);
			
			if (template != null && !template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}
			
			template = null;
		}
		
		L2PcInstance Player = null;
		
		if (activeChar.getTarget() != null)
		{
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				if (activeChar.getAccessLevel().getLevel() > 0 && activeChar.getAccessLevel().getLevel() < 3)
				{
					Player = (L2PcInstance) activeChar.getTarget();
				}
				else
				{
					activeChar.sendMessage("You have not right to create item on another player");
					return;
				}
			}
			else
			{
				activeChar.sendMessage("You can add an item only to a character.");
				return;
			}
		}
		
		if (Player == null)
		{
			activeChar.setTarget(activeChar);
			Player = activeChar;
		}
		
		Player.getInventory().addItem("Admin", id, num, Player, null);
		ItemList il = new ItemList(Player, true);
		Player.sendPacket(il);
		if (activeChar.getName().equalsIgnoreCase(Player.getName()))
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		else
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in " + Player.getName() + "'s inventory.");
			Player.sendMessage("Admin has spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		
		Player = null;
		il = null;
	}
	
	private void massCreateItem(final L2PcInstance activeChar, final int id, final int num)
	{
		if (num > 20)
		{
			final L2Item template = ItemTable.getInstance().getTemplate(id);
			if (template != null && !template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}
		}
		
		int i = 0;
		L2ItemInstance item = null;
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendMessage("Admin is rewarding all online players.");
			item = player.getInventory().addItem("Admin", id, num, null, null);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendPacket(iu);
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
			sm.addItemName(item.getItemId());
			sm.addNumber(num);
			player.sendPacket(sm);
			i++;
		}
		activeChar.sendMessage("Mass-created items in the inventory of " + i + " player(s).");
		LOGGER.info("GM " + activeChar.getName() + " mass_created item Id: " + id + " (" + num + ")");
	}
	
	private void removeAllItems(final L2PcInstance activeChar)
	{
		for (final L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (item.getLocation() == L2ItemInstance.ItemLocation.INVENTORY)
				activeChar.getInventory().destroyItem("Destroy", item.getObjectId(), item.getCount(), activeChar, null);
		}
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendMessage("Your inventory has been cleared.");
	}
}
