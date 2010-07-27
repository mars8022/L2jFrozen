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
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MagicSkillUser;

/**
 * This class ...
 *
 * @version $Revision: 1.0.0.0.0.0 $ $Date: 2005/09/02 19:41:13 $
 */
public class Firework implements IItemHandler
{
	// Modified by Baghak (Prograsso): Added Firework support
	private static final int[] ITEM_IDS = { 6403, 6406, 6407 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance)) {
			return; // prevent Class cast exception
		}
		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isAway())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isConfused())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isStunned())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isAlikeDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!Config.ENABLE_FP && !activeChar.getFloodProtectors().getFirework().tryPerformAction("firework"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		/*
		 * Elven Firecracker
		 */
		if (itemId == 6403) // elven_firecracker, xml: 2023
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2023, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			useFw(activeChar, 2023, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		/*
		 * Firework
		 */
		else if (itemId == 6406) // firework, xml: 2024
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2024, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			useFw(activeChar, 2024, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		/*
		 * Large Firework
		 */
		else if (itemId == 6407) // large_firework, xml: 2025
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2025, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			useFw(activeChar, 2025, 1);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}

	public void useFw(L2PcInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			activeChar.useMagic(skill, false, false);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}