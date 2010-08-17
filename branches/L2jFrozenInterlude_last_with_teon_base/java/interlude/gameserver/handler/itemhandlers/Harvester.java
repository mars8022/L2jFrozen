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
import interlude.gameserver.instancemanager.CastleManorManager;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2MonsterInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler
{
	private static final int[] ITEM_IDS = { /* Harvester */5125 };
	L2PcInstance _activeChar;
	L2MonsterInstance _target;

	public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
	{
		if (!(playable instanceof L2PcInstance)) {
			return;
		}
		if (CastleManorManager.getInstance().isDisabled()) {
			return;
		}
		_activeChar = (L2PcInstance) playable;
		if (_activeChar.getTarget() == null || !(_activeChar.getTarget() instanceof L2MonsterInstance))
		{
			_activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		_target = (L2MonsterInstance) _activeChar.getTarget();
		if (_target == null || !_target.isDead())
		{
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); // harvesting skill
		_activeChar.useMagic(skill, false, false);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}