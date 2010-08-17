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

import java.util.logging.Logger;

import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Julian
 */
public class DeluxeKey implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(BeastFeed.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.DELUXE_KEY_UNLOCK };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance)) {
			return;
		}
		L2Object[] targetList = skill.getTargetList(activeChar);
		if (targetList == null)
		{
			return;
		}
		_log.fine("Delux key casting succeded.");
		// This is just a dummy skill handler for the golden food and crystal
		// food skills,
		// since the AI responce onSkillUse handles the rest.
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isGM()) {
			((L2PcInstance) activeChar).sendMessage("Deluxe key casting succeeded with DeluxeKey skill handler.");
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
