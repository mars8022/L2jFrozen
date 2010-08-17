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
package interlude.gameserver.skills.effects;

import interlude.gameserver.model.ChanceCondition;
import interlude.gameserver.model.IChanceSkillTrigger;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.skills.Env;

public class EffectChanceSkillTrigger extends L2Effect implements IChanceSkillTrigger
{
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final ChanceCondition _chanceCondition;

	public EffectChanceSkillTrigger(Env env, EffectTemplate template)
    {
		super(env, template);

		_triggeredId = template.triggeredId;
	    _triggeredLevel = template.triggeredLevel;
	    _chanceCondition = template.chanceCondition;
    }

	@Override
    public EffectType getEffectType()
    {
	    return EffectType.CHANCE_SKILL_TRIGGER;
    }

	@Override
	public void onStart()
	{
		getEffected().addChanceEffect(this);
	}

	@Override
    public boolean onActionTime()
    {
	    return false;
    }

	@Override
	public void onExit()
	{
        getEffected().removeChanceEffect(this);
	}

	@Override
    public int getTriggeredChanceId()
    {
	    return _triggeredId;
    }

	@Override
    public int getTriggeredChanceLevel()
    {
	    return _triggeredLevel;
    }

	@Override
    public boolean triggersChanceSkill()
    {
	    return _triggeredId > 1;
    }

	@Override
    public ChanceCondition getTriggeredChanceCondition()
    {
	    return _chanceCondition;
    }

}