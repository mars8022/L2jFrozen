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
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private int _count;
	private boolean _packageSale;
	private int[] _items; // count * 3
	
	@Override
	protected void readImpl()
	{
		_packageSale = readD() == 1;
		_count = readD();
		
		if (_count <= 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
			_items = null;
			return;
		}
		
		_items = new int[_count * 3];
		
		for (int x = 0; x < _count; x++)
		{
			int objectId = readD();
			_items[x * 3 + 0] = objectId;
			long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			
			_items[x * 3 + 1] = (int) cnt;
			int price = readD();
			_items[x * 3 + 2] = price;
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
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow() || player.isMovementDisabled() || player.inObserverMode() || player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);
		
		long totalCost = player.getAdena();
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3 + 0];
			int count = _items[i * 3 + 1];
			int price = _items[i * 3 + 2];
			
			if (price <= 0)
			{
				String msgErr = "[SetPrivateStoreListSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit (use PHX), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				_count = 0;
				_items = null;
				return;
			}
			
			totalCost += price;
			if (totalCost > Integer.MAX_VALUE)
			{
				player.sendPacket(new PrivateStoreManageListSell(player));
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			tradeList.addItem(objectId, count, price);
		}
		
		if (_count <= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendMessage("Store mode are disable while trading.");
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_count > player.GetPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		player.sitDown();
		if (_packageSale)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);
		}
		else
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
		}
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}
	
	@Override
	public String getType()
	{
		return "[C] 74 SetPrivateStoreListSell";
	}
}