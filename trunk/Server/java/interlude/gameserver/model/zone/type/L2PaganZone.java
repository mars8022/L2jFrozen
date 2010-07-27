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
package interlude.gameserver.model.zone.type;

import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

public class L2PaganZone extends L2ZoneType
{
	L2ItemInstance fadedMark;

	public L2PaganZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			L2ItemInstance item = player.getInventory().getItemByItemId(8064);
			if (item != null)
			{
				player.destroyItemByItemId("Mark", 8064, 1, player, true);
				L2ItemInstance fadedMark = player.getInventory().addItem("Faded Mark", 8065, 1, player, player);
				SystemMessage ms = new SystemMessage(SystemMessageId.EARNED_ITEM);
				ms.addItemName(fadedMark);
				player.sendPacket(ms);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}