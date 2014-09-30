/*
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

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author Faror
 */
final class EffectPhoenixBless extends L2Effect
{
	public EffectPhoenixBless(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.PHOENIX_BLESSING;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		if(getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).startPhoenixBlessing(this);
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2PlayableInstance)
		{
			((L2PlayableInstance) getEffected()).stopPhoenixBlessing(this);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
