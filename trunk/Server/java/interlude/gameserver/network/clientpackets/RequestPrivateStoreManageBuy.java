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

import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.PrivateStoreManageListBuy;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreManageBuy extends L2GameClientPacket
{
	private static final String _C__90_REQUESTPRIVATESTOREMANAGEBUY = "[C] 90 RequestPrivateStoreManageBuy";

	// private static Logger _log =
	// Logger.getLogger(RequestPrivateStoreBuyManage.class.getName());
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		// Player shouldn't be able to set stores if he/she is alike dead (dead
		// or fake death)
		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isInOlympiadMode())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getMountType() != 0)
		{
			return;
		}
		if (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY || player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY + 1)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
		}
		if (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
		{
			if (player.isSitting())
			{
				player.standUp();
			}
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
			player.sendPacket(new PrivateStoreManageListBuy(player));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__90_REQUESTPRIVATESTOREMANAGEBUY;
	}
}
