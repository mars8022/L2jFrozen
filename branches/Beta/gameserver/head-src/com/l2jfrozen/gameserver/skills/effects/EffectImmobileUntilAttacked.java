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

import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author programmos
 */
public class EffectImmobileUntilAttacked extends L2Effect
{
	static final Logger _log = Logger.getLogger(EffectImmobileUntilAttacked.class.getName());

	public EffectImmobileUntilAttacked(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.IMMOBILEUNTILATTACKED;
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().stopImmobileUntilAttacked(this);
		// just stop this effect
		return false;
	}

	/** Notify exited */

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopImmobileUntilAttacked(this);
	}

	/** Notify started */

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startImmobileUntilAttacked();
	}
}
