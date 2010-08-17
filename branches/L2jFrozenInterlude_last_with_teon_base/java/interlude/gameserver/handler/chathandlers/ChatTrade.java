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
package interlude.gameserver.handler.chathandlers;

import interlude.Config;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.handler.IChatHandler;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.CreatureSay;

/**
 * A chat handler
 * 
 * @author durgus
 */
public class ChatTrade implements IChatHandler {
	private static final int[] COMMAND_IDS = { 8 };

	/**
	 * Handle chat type 'trade'
	 * 
	 * @see interlude.gameserver.handler.IChatHandler#handleChat(int,
	 *      interlude.gameserver.model.actor.instance.L2PcInstance,
	 *      java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String target,
			String text) {
		if (!Config.ENABLE_FP && !activeChar.getFloodProtectors().getTrade().tryPerformAction("trade")) 
		{
			activeChar.sendMessage("You can not talk so fast wait a little.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type,
				activeChar.getName(), text);
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on")
				|| Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm")
				&& activeChar.isGM()) {
			for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
				player.sendPacket(cs);
			}
		} else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited")) {
			int region = MapRegionTable.getInstance().getMapRegion(
					activeChar.getX(), activeChar.getY());
			for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
				if (region == MapRegionTable.getInstance().getMapRegion(
						player.getX(), player.getY())) {
					player.sendPacket(cs);
				}
			}
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 * 
	 * @see interlude.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	public int[] getChatTypeList() {
		return COMMAND_IDS;
	}
}