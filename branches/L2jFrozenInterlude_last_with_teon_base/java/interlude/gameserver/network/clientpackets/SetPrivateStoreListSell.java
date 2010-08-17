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

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.TradeList;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.PrivateStoreManageListSell;
import interlude.gameserver.network.serverpackets.PrivateStoreMsgSell;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final String _C__74_SETPRIVATESTORELISTSELL = "[C] 74 SetPrivateStoreListSell";
	private static Logger _log = Logger.getLogger(SetPrivateStoreListSell.class.getName());
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
		if (player == null) {
			return;
		}
		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Transactions are disable for your Access Level");
			return;
		}
		
		// Prevents player to start selling inside a No Peace/Jail zone.
		if ((!player.isInsideZone(L2Character.ZONE_PEACE)) || (player.isInsideZone(L2Character.ZONE_JAIL)))
		{
			player.sendMessage("You cannot open a Private Workshop here.");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3 + 0];
			int count = _items[i * 3 + 1];
			int price = _items[i * 3 + 2];
			if (price <= 0)
			{
				/*
				String msgErr = "[SetPrivateStoreListSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit (use PHX), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				*/
				_count = 0;
				_items = null;
				//player.closeNetConnection(); // kick
				
				_log.warning("ATTENTION: Player "+player.getName()+" tried an overflow exploit (use PHX)!!");
				player.sendMessage("You are trying an overflow exploit (use PHX)! The Admin/GM will contact you soon.."); // message
				player.sendPacket(ActionFailed.STATIC_PACKET);
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
		// Check maximum number of allowed slots for pvt shops
		if (_count > player.GetPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		player.sitDown();
		if (_packageSale) {
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);
		} else {
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
		}
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}

	@Override
	public String getType()
	{
		return _C__74_SETPRIVATESTORELISTSELL;
	}
}
