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

import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2NpcTemplate;

public class ChristmasTree implements IItemHandler
{
	private static final int[] ITEM_IDS = { 5560, /* x-mas tree */
	5561
	/* Special x-mas tree */
	};
	private static final int[] NPC_IDS = { 13006, /*
												 * Christmas tree w. flashing lights and snow
												 */
	13007 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2NpcTemplate template1 = null;
		int itemId = item.getItemId();
		for (int i = 0; i < ITEM_IDS.length; i++)
		{
			if (ITEM_IDS[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
				break;
			}
		}
		if (template1 == null) {
			return;
		}
		L2Object target = activeChar.getTarget();
		if (target == null) {
			target = activeChar;
		}
		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			L2World.getInstance().storeObject(spawn.spawnOne());
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Created " + template1.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Target is not ingame.");
			activeChar.sendPacket(sm);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
