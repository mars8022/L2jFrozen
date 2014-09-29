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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private int _count;
	private int[] _items; // count * 3
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		
		if (_count <= 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
			_items = null;
			return;
		}
		
		_items = new int[_count * 4];
		
		for (int x = 0; x < _count; x++)
		{
			int itemId = readD();
			_items[x * 4 + 0] = itemId;
			_items[(x * 4 + 3)] = readH();
			// readH();//it's the enchant value, but the interlude client has a bug, so it dnt send back the correct enchant value
			readH();// TODO analyse this
			long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			
			_items[x * 4 + 1] = (int) cnt;
			int price = readD();
			_items[x * 4 + 2] = price;
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow() || player.isMovementDisabled() || player.inObserverMode() || player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		int cost = 0;
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 4 + 0];
			int count = _items[i * 4 + 1];
			int price = _items[i * 4 + 2];
			int enchant = _items[i * 4 + 3];
			
			tradeList.addItemByItemId(itemId, count, price, enchant);
			cost += count * price;
			
			if (cost > Integer.MAX_VALUE)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
		}
		
		if (_count <= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendMessage("Store mode are disable while trading.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_count > player.GetPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check for available funds
		if (Config.SELL_BY_ITEM)
		{
			if (cost > player.getItemCount(Config.SELL_ITEM, -1) || cost <= 0)
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
		}
		else
		{
			if (cost > player.getAdena() || cost <= 0)
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
		}
		
		player.sitDown();
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	@Override
	public String getType()
	{
		return "[C] 91 SetPrivateStoreListBuy";
	}
	
}