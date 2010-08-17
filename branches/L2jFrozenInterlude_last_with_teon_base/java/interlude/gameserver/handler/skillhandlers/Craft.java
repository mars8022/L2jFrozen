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
package interlude.gameserver.handler.skillhandlers;

import interlude.gameserver.RecipeController;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class Craft implements ISkillHandler
{
	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IItemHandler#useItem(interlude.gameserver.model.L2PcInstance, interlude.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[] SKILL_IDS = { SkillType.COMMON_CRAFT, SkillType.DWARVEN_CRAFT };

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IItemHandler#useItem(interlude.gameserver.model.L2PcInstance, interlude.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance)) {
			return;
		}
		L2PcInstance player = (L2PcInstance) activeChar;
		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, skill.getSkillType() == SkillType.DWARVEN_CRAFT ? true : false);
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
