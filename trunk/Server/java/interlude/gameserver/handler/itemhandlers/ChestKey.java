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

import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2ChestInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.SystemMessage;

public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	private static final int[] ITEM_IDS = { 6665, 6666, 6667, 6668, 6669, 6670, 6671, 6672
	// deluxe key
	};

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance)) {
			return;
		}
		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		L2Skill skill = SkillTable.getInstance().getInfo(2229, itemId - 6664);// box
		// key
		// skill
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2ChestInstance) || target == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			L2ChestInstance chest = (L2ChestInstance) target;
			if (chest.isDead() || chest.isInteracted())
			{
				if (activeChar.isGM())
				{
					activeChar.sendMessage("DEBUG: isDead(): " + (chest.isDead() ? "true" : "false"));
					activeChar.sendMessage("DEBUG: isInteracted(): " + (chest.isInteracted() ? "true" : "false"));
				}
				activeChar.sendMessage("The chest is empty.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			// activeChar.sendMessage("Opening Chest with ChestKey item
			// handler.");
			activeChar.useMagic(skill, false, false);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
