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

import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author -Nemesiss-
 */
public class CPperHeal implements ISkillHandler
{
	// private final static Log _log = LogFactory.getLog(CombatPointHeal.class.getName());
	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IItemHandler#useItem(interlude.gameserver.model.L2PcInstance, interlude.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[] SKILL_IDS = { SkillType.COMBATPOINTPERHEAL };

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.handler.IItemHandler#useItem(interlude.gameserver.model.L2PcInstance, interlude.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target;
		for (L2Object element : targets)
		{
			target = (L2Character) element;
			double percp = target.getMaxCp() * skill.getPower();
			target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() - percp);
			StatusUpdate sucp = new StatusUpdate(target.getObjectId());
			sucp.addAttribute(StatusUpdate.CUR_CP, (int) target.getStatus().getCurrentCp());
			target.sendPacket(sucp);
			// Missing system message??
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}