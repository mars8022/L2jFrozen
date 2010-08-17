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
package interlude.gameserver.skills.l2skills;

import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.templates.StatsSet;

public final class L2SkillMagicOnGround extends L2Skill
{
	public int effectNpcId;
	public int triggerEffectId;

	public L2SkillMagicOnGround(StatsSet set)
	{
		super(set);
		effectNpcId = set.getInteger("effectNpcId", -1);
		triggerEffectId = set.getInteger("triggerEffectId", -1);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead()) {
			return;
		}
		getEffectsSelf(caster);
	}
}