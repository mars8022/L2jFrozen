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

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author eX1steam L2JFrozen
 */
public class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see com.l2jfrozen.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TARGET_ME;
	}

	/**
	 * @see com.l2jfrozen.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public void onStart()
	{
		if(getEffected() instanceof L2PlayableInstance)
		{
			if(getEffected() instanceof L2SiegeSummonInstance)
				return;

			if(getEffected().getTarget() != getEffector())
			{
				// Target is different - stop autoattack and break cast
				//getEffected().abortAttack();
				//getEffected().abortCast();
				getEffected().setTarget(getEffector());
				MyTargetSelected my = new MyTargetSelected(getEffector().getObjectId(), 0);
				getEffected().sendPacket(my);
				getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
		}
	}

	/**
	 * @see com.l2jfrozen.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
	// nothing
	}

	/**
	 * @see com.l2jfrozen.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// nothing
		return false;
	}
}
