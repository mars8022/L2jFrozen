/*
 * L2jFrozen Project - www.l2jfrozen.com 
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

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

final class EffectParalyze extends L2Effect
{
	
	public EffectParalyze(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PARALYZE;
	}
	
	@Override
	public void onStart()
	{
		getEffected().stopMove(null);
		getEffected().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_HOLD_1);
		getEffected().setIsParalyzed(true);
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_HOLD_1);
		getEffected().setIsParalyzed(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
