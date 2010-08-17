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
package interlude.gameserver.network.clientpackets;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.TradeController;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2TradeList;
import interlude.gameserver.model.actor.instance.L2MercManagerInstance;
import interlude.gameserver.model.actor.instance.L2MerchantInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.InventoryUpdate;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2Item;
import interlude.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestWearItem extends L2GameClientPacket
{
	private static final String _C__C6_REQUESTWEARITEM = "[C] C6 RequestWearItem";
	protected static final Logger _log = Logger.getLogger(RequestWearItem.class.getName());
	@SuppressWarnings("unchecked")
	protected Future _removeWearItemsTask;
	@SuppressWarnings("unused")
	private int _unknow;
	/** List of ItemID to Wear */
	private int _listId;
	/** Number of Item to Wear */
	private int _count;
	/** Table of ItemId containing all Item to Wear */
	private int[] _items;
	/** Player that request a Try on */
	protected L2PcInstance _activeChar;

	class RemoveWearItemsTask implements Runnable
	{
		public void run()
		{
			try
			{
				_activeChar.destroyWearedItems("Wear", null, true);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * Decrypt the RequestWearItem Client->Server Packet and Create _items table containing all ItemID to Wear.<BR>
	 * <BR>
	 */
	@Override
	protected void readImpl()
	{
		// Read and Decrypt the RequestWearItem Client->Server Packet
		_activeChar = getClient().getActiveChar();
		_unknow = readD();
		_listId = readD(); // List of ItemID to Wear
		_count = readD(); // Number of Item to Wear
		if (_count < 0) {
			_count = 0;
		}
		if (_count > 100) {
			_count = 0; // prevent too long lists
		}
		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];
		// Fill _items table with all ItemID to Wear
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i] = itemId;
		}
	}

	/**
	 * Launch Wear action.<BR>
	 * <BR>
	 */
	@Override
	protected void runImpl()
	{
		// Get the current player and return if null
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		// If Alternate rule Karma punishment is set to true, forbid Wear to
		// player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) {
			return;
		}
		// Check current target of the player and the INTERACTION_DISTANCE
		L2Object target = player.getTarget();
		if (!player.isGM() && (target == null // No target (ie GM Shop))
				|| !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance) // Target not a merchant and not mercmanager
		|| !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false))) {
			return;
		}
		L2TradeList list = null;
		// Get the current merchant targeted by the player
		L2MerchantInstance merchant = target != null && target instanceof L2MerchantInstance ? (L2MerchantInstance) target : null;
		List<L2TradeList> lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());
		if (lists == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			player.closeNetConnection(); // kick
			return;
		}
		for (L2TradeList tradeList : lists)
		{
			if (tradeList.getListId() == _listId)
			{
				list = tradeList;
			}
		}
		if (list == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			player.closeNetConnection(); // kick
			return;
		}
		_listId = list.getListId();
		// Check if the quantity of Item to Wear
		if (_count < 1 || _listId >= 1000000)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Total Price of the Try On
		long totalPrice = 0;
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				player.closeNetConnection(); // kick
				return;
			}
			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			weight += template.getWeight();
			slots++;
			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				player.closeNetConnection(); // kick
				return;
			}
		}
		// Check the weight
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		// Check the inventory capacity
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		// Charge buyer and add tax to castle treasury if not owned by npc clan
		// because a Try On is not Free
		if (totalPrice < 0 || !player.reduceAdena("Wear", (int) totalPrice, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		// Proceed the wear
		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				player.closeNetConnection(); // kick
				return;
			}
			// If player doesn't own this item : Add this L2ItemInstance to
			// Inventory and set properties lastchanged to ADDED and _wear
			// to
			// true
			// If player already own this item : Return its L2ItemInstance
			// (will
			// not be destroy because property _wear set to false)
			L2ItemInstance item = player.getInventory().addWearItem("Wear", itemId, player, merchant);
			// Equip player with this item (set its location)
			player.getInventory().equipItemAndRecord(item);
			// Add this Item in the InventoryUpdate Server->Client Packet
			playerIU.addItem(item);
		}
		// Send the InventoryUpdate Server->Client Packet to the player
		// Add Items in player inventory and equip them
		player.sendPacket(playerIU);
		// Send the StatusUpdate Server->Client Packet to the player with new
		// CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		// Send a Server->Client packet UserInfo to this L2PcInstance and
		// CharInfo to all L2PcInstance in its _KnownPlayers
		player.broadcastUserInfo();
		// All weared items should be removed in ALLOW_WEAR_DELAY sec.
		if (_removeWearItemsTask == null) {
			_removeWearItemsTask = ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__C6_REQUESTWEARITEM;
	}
}
