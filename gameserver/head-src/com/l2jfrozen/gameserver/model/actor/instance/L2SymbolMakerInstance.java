/*
 * $Header$
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
package com.l2jfrozen.gameserver.model.actor.instance;

import javolution.text.TextBuilder;

import com.l2jfrozen.gameserver.datatables.sql.HennaTreeTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.network.serverpackets.HennaEquipList;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class L2SymbolMakerInstance extends L2FolkInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2SymbolMakerInstance.class);
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.equals("Draw"))
		{
			final L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
			final HennaEquipList hel = new HennaEquipList(player, henna);
			player.sendPacket(hel);
			
			player.sendPacket(new ItemList(player, false));
		}
		else if (command.equals("RemoveList"))
		{
			showRemoveChat(player);
		}
		else if (command.startsWith("Remove "))
		{
			if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaRemove"))
				return;
			
			final int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
			
			player.sendPacket(new ItemList(player, false));
			
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showRemoveChat(final L2PcInstance player)
	{
		TextBuilder html1 = new TextBuilder("<html><body>");
		html1.append("Select symbol you would like to remove:<br><br>");
		boolean hasHennas = false;
		
		for (int i = 1; i <= 3; i++)
		{
			final L2HennaInstance henna = player.getHennas(i);
			
			if (henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
			}
		}
		if (!hasHennas)
		{
			html1.append("You don't have any symbol to remove!");
		}
		
		html1.append("</body></html>");
		insertObjectIdAndShowChatWindow(player, html1.toString());
		html1 = null;
	}
	
	public L2SymbolMakerInstance(final int objectID, final L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public String getHtmlPath(final int npcId, final int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
