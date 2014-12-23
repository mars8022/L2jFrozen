/* L2jFrozen Project - www.l2jfrozen.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.skills.effects;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @author kombat
 */
public class EffectForce extends L2Effect
{
	protected static final Logger LOGGER = Logger.getLogger(EffectForce.class);
	
	public int forces = 0;
	private int _range = -1;
	
	public EffectForce(final Env env, final EffectTemplate template)
	{
		super(env, template);
		forces = getSkill().getLevel();
		_range = getSkill().getCastRange();
	}
	
	@Override
	public boolean onActionTime()
	{
		return Util.checkIfInRange(_range, getEffector(), getEffected(), true);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	public void increaseForce()
	{
		forces++;
		updateBuff();
	}
	
	public void decreaseForce()
	{
		forces--;
		if (forces < 1)
		{
			exit(false);
		}
		else
		{
			updateBuff();
		}
	}
	
	public void updateBuff()
	{
		exit(false);
		final L2Skill newSkill = SkillTable.getInstance().getInfo(getSkill().getId(), forces);
		if (newSkill != null)
			newSkill.getEffects(getEffector(), getEffected(), false, false, false);
	}
	
	@Override
	public void onExit()
	{
		// try
		// {
		// getEffector().abortCast();
		// if(getEffector().getForceBuff() != null)
		// getEffector().getForceBuff().delete();
		// }
		// catch(Exception e)
		// {
		// null
		// }
	}
}
