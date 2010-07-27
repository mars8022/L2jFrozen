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
package interlude.gameserver.handler.itemhandlers;

import interlude.Config;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author DaRkRaGe [L2JOneo]
 */
public class DonatorItems implements IItemHandler
{
	private static final int[] ITEM_IDS = { 6392, 6393 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (Config.DONATOR_ITEMS)
		{
			if (!(playable instanceof L2PcInstance))
			{
				return;
			}
			L2PcInstance activeChar = (L2PcInstance) playable;
			int itemId = item.getItemId();
			String filename = "data/html/mods/donator/" + itemId + ".htm";
			String content = HtmCache.getInstance().getHtm(filename);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			if (content == null)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml("<html><head><body>Donator Items Are Disabled</body></html>");
				activeChar.sendPacket(html);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
				itemReply.setHtml(content);
				activeChar.sendPacket(itemReply);
			}
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
