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
package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author Kerberos
 */
public final class EffectFusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;

	public EffectFusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = 10;
	}
	
	@Override
	public boolean onActionTime()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FUSION;
	}

	public void increaseEffect()
	{
		if(_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}

	public void decreaseForce()
	{
		_effect--;
		if(_effect < 1)
		{
			exit(false);
		}
		else
		{
			updateBuff();
		}
	}

	private void updateBuff()
	{
		exit(false);
		SkillTable.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected(),false,false,false);
	}
}
