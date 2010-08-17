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

import interlude.gameserver.datatables.ExtractableItemsData;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.model.L2ExtractableItem;
import interlude.gameserver.model.L2ExtractableProductItem;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.model.item.PcInventory;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.util.Rnd;

public class ExtractableItems implements IItemHandler
{

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance)) {
			return;
		}
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(item.getItemId());
		if (exitem == null) {
			return;
		}

			int itemID = item.getItemId();
			int createItemID = 0, createAmount = 0, rndNum = Rnd.get(100), chanceFrom = 0;

			for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
			{
				int chance = expi.getChance();
				if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
				{
					createItemID = expi.getId();
					createAmount = expi.getAmmount();
					break;
				}
				chanceFrom += chance;
			}
			if (createItemID == 0)
			{
				activeChar.sendMessage("Nothing happend.");
				return;
			}
			PcInventory inv = activeChar.getInventory();
			if (createItemID > 0)
			{
				if (ItemTable.getInstance().createDummyItem(createItemID).isStackable()) {
					inv.addItem("Extract", createItemID, createAmount, activeChar, null);
				} else
				{
					for (int i = 0; i < createAmount; i++) {
						inv.addItem("Extract", createItemID, 1, activeChar, item);
					}
				}
				SystemMessage sm;
				if (createAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID);
					sm.addNumber(createAmount);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(createItemID);
				}
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.sendMessage("Item failed to open");

			}
			activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
		}

		public int[] getItemIds()
		{
			return ExtractableItemsData.getInstance().itemIDs();
		}
	}
