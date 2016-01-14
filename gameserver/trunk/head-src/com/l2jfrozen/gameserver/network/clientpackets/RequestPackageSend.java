/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.PcFreight;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;

/**
 * @author -Wooden-
 */
public final class RequestPackageSend extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
	private List<Item> _items = new FastList<Item>();
	private int _objectID;
	private int _count;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
		_count = readD();

		if(_count < 0 || _count > 500)
		{
			_count = -1;
			return;
		}

		for(int i = 0; i < _count; i++)
		{
			int id = readD(); //this is some id sent in PackageSendableList
			int count = readD();
			_items.add(new Item(id, count));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if(_count == -1 || _items == null)
			return;

		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
			return;

		if(player.getObjectId() == _objectID)
			return;

		L2PcInstance target = L2PcInstance.load(_objectID);

		if(player.getAccountChars().size() < 1)
		{
			return;
		}
		else if(!player.getAccountChars().containsKey(_objectID))
			return;
		
		if(L2World.getInstance().getPlayer(_objectID) != null)
		return;
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}


		PcFreight freight = target.getFreight();
		player.setActiveWarehouse(freight);
		target.deleteMe();
		ItemContainer warehouse = player.getActiveWarehouse();

		if(warehouse == null)
			return;

		L2FolkInstance manager = player.getLastFolkNPC();

		if((manager == null || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;

		if(warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Alt game - Karma punishment
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			return;

		// Freight price from config or normal price per item slot (30)
		int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
		int currentAdena = player.getAdena();
		int slots = 0;

		for(Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;

			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");

			// Check if item is null
			if(item == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
			}
					
			// Fix exploit for trade Augmented weapon with freight
			if(item.isAugmented())
			{
				_log.warning("Error depositing a warehouse object for char "+player.getName()+" (item is augmented)");
				return;
			}
			
			if(!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
				return;

			// Calculate needed adena and slots
			if(item.getItemId() == 57)
			{
				currentAdena -= count;
			}

			if(!item.isStackable())
			{
				slots += count;
			}
			else if(warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if(!warehouse.validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		// Check if enough adena and charge the fee
		if(currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;

			// check for an invalid item
			if(objectId == 0 && count == 0)
			{
				continue;
			}

			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);

			if(oldItem == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			int itemId = oldItem.getItemId();

			if(itemId >= 6611 && itemId <= 6621 || itemId == 6842)
			{
				continue;
			}

			L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());

			if(newItem == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}

			if(playerIU != null)
			{
				if(oldItem.getCount() > 0 && oldItem != newItem)
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}

		// Send updated item list to the player
		if(playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		player.setActiveWarehouse(null);
	}

	private class Item
	{
		public int id;
		public int count;

		public Item(int i, int c)
		{
			id = i;
			count = c;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 9F RequestPackageSend";
	}
}
