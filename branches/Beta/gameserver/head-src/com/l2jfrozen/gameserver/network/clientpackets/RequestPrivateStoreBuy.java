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

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.ItemRequest;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.TradeList.TradeItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPrivateStoreBuy.class.getName());
	
	private int _storePlayerId;
	private int _count;
	private ItemRequest[] _items;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		
		// count*12 is the size of a for iteration of each item
		if (_count < 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}
		
		_items = new ItemRequest[_count];
		
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			long count = readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			int price = readD();
			
			_items[i] = new ItemRequest(objectId, (int) count, price);
		}
		
		if (Config.DEBUG)
		{
			
			_log.info("Player " + getClient().getActiveChar().getName() + " requested to buy to storeId " + _storePlayerId + " Items Number: " + _count);
			
			for (int i = 0; i < _count; i++)
			{
				_log.info("Requested Item ObjectID: " + _items[i].getObjectId());
				_log.info("Requested Item Id: " + _items[i].getItemId());
				_log.info("Requested Item count: " + _items[i].getCount());
				_log.info("Requested Item price: " + _items[i].getPrice());
				
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You buying items too fast.");
			return;
		}
		
		L2Object object = L2World.getInstance().findObject(_storePlayerId);
		if (object == null || !(object instanceof L2PcInstance))
			return;
		
		L2PcInstance storePlayer = (L2PcInstance) object;
		if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		TradeList storeList = storePlayer.getSellList();
		
		if (storeList == null)
			return;
		
		// Check if player didn't choose any items
		if (_items == null || _items.length == 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		long priceTotal = 0;
		for (ItemRequest ir : _items)
		{
			if (ir.getCount() > Integer.MAX_VALUE || ir.getCount() < 0)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			TradeItem sellersItem = storeList.getItem(ir.getObjectId());
			
			if (sellersItem == null)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			if (ir.getPrice() != sellersItem.getPrice())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			L2ItemInstance iEnchant = storePlayer.getInventory().getItemByObjectId(ir.getObjectId());
			int enchant = 0;
			if (iEnchant == null)
			{
				enchant = 0;
			}
			else
			{
				enchant = iEnchant.getEnchantLevel();
			}
			ir.setEnchant(enchant);
			
			priceTotal += ir.getPrice() * ir.getCount();
		}
		
		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		if (priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (player.getItemCount(Config.SELL_ITEM, -1) < priceTotal)
			{
				sendPacket(SystemMessage.sendString("You do not have needed items to buy"));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
		}
		else
		{
			if (player.getAdena() < priceTotal)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _count)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		if (!storeList.PrivateStoreBuy(player, _items, (int) priceTotal))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			// Punishment e log in audit
			Util.handleIllegalPlayerAction(storePlayer, "PrivateStore buy has failed due to invalid list or request. Player: " + player.getName(), Config.DEFAULT_PUNISH);
			_log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
		
		/*
		 * Lease holders are currently not implemented else if (_seller != null) { // lease shop sell L2MerchantInstance seller = (L2MerchantInstance)_seller; L2ItemInstance ladena = seller.getLeaseAdena(); for (TradeItem ti : buyerlist) { L2ItemInstance li =
		 * seller.getLeaseItemByObjectId(ti.getObjectId()); if (li == null) { if (ti.getObjectId() == ladena.getObjectId()) { buyer.addAdena(ti.getCount()); ladena.setCount(ladena.getCount()-ti.getCount()); ladena.updateDatabase(); } continue; } int cnt = li.getCount(); if (cnt < ti.getCount())
		 * ti.setCount(cnt); if (ti.getCount() <= 0) continue; L2ItemInstance inst = ItemTable.getInstance().createItem(li.getItemId()); inst.setCount(ti.getCount()); inst.setEnchantLevel(li.getEnchantLevel()); buyer.getInventory().addItem(inst); li.setCount(li.getCount()-ti.getCount());
		 * li.updateDatabase(); ladena.setCount(ladena.getCount()+ti.getCount()*ti.getOwnersPrice()); ladena.updateDatabase(); } }
		 */
	}
	
	@Override
	public String getType()
	{
		return "[C] 79 RequestPrivateStoreBuy";
	}
}