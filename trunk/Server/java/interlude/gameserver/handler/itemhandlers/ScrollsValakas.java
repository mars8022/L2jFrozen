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
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PetInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MagicSkillUser;

/**
 * @author Maxi [L2JTeon]
 */
public class ScrollsValakas implements IItemHandler
{
	private static final int[] ITEM_IDS = { 6653, 6654, 6655 };

	/*
	 * public ScrollsValakas() { ItemHandler.getInstance().registerItemHandler(this); }
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance) {
			activeChar = (L2PcInstance) playable;
		} else if (playable instanceof L2PetInstance) {
			activeChar = ((L2PetInstance) playable).getOwner();
		} else {
			return;
		}
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		int itemId = item.getItemId();
		if (itemId >= 6653 && itemId <= 6655)
		{
			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
				return;
			}
			switch (itemId)
			{
				case 6653: // Amulet: Flames of Valakas - xz nuzen li on vobwe
					activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2233, 1, 1, 0));
					useScroll(activeChar, 2233, 1);
					break;
				case 6654: // Amulet: Flames of Valakas
					activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2233, 1, 1, 0));
					useScroll(activeChar, 2233, 1);
					break;
				case 6655: // Amulet: Slay Valakas
					activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2232, 1, 1, 0));
					useScroll(activeChar, 2232, 1);
					break;
				default:
					break;
			}
			return;
		}
		// for the rest, there are no extra conditions
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
			return;
		}
	}

	public void useScroll(L2PcInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null) {
			activeChar.doCast(skill);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
