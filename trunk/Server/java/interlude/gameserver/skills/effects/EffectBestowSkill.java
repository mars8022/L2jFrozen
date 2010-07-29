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

import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.skills.Env;

/**
* @author kombat
*/
final class EffectBestowSkill extends L2Effect
{
public EffectBestowSkill(Env env, EffectTemplate template)
{
super(env, template);
}

/**
* @see net.sf.l2j.gameserver.model.L2Effect#getEffectType()
*/
@Override
public EffectType getEffectType()
{
return EffectType.BUFF;
}

/**
* @see net.sf.l2j.gameserver.model.L2Effect#onstart()
*/
@Override
public void onStart()
{
L2Skill tempSkill = SkillTable.getInstance().getInfo(getSkill().getTriggeredId(), getSkill().getTriggeredLevel());
if (tempSkill != null)
{
	System.out.println(getEffected().getName()+" Triggered Skill: "+tempSkill.getName());
getEffected().addSkill(tempSkill);
return;
}
return;
}

/**
* @see net.sf.l2j.gameserver.model.L2Effect#onExit()
*/
@Override
public void onExit()
{
getEffected().removeSkill(getSkill().getTriggeredId());
System.out.println(getEffected().getName()+" Removed Triggered Skill: "+getSkill().getName());
}

/**
* @see net.sf.l2j.gameserver.model.L2Effect#onActionTime()
*/
@Override
public boolean onActionTime()
{
return false;
}
}