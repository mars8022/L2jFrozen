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
package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Character.AIAccessor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;

public class L2SummonAI extends L2CharacterAI
{

	private boolean _thinking; // to prevent recursive thinking

	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		onIntentionActive();
	}

	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) _actor;

		if(summon.getFollowStatus())
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}

		summon = null;
	}

	private void thinkAttack()
	{
		if(checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}

		if(maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
			return;

		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
		return;
	}

	private void thinkCast()
	{
		L2Summon summon = (L2Summon) _actor;

		final L2Character target = getCastTarget();
		if(checkTargetLost(target))
		{
			setCastTarget(null);
			return;
		}

		final L2Skill skill = get_skill();
		if(maybeMoveToPawn(target, _actor.getMagicalAttackRange(skill)))
			return;

		clientStopMoving(null);
		summon.setFollowStatus(false);
		summon = null;
		setIntention(AI_INTENTION_IDLE);
		_accessor.doCast(skill);
		return;
	}

	private void thinkPickUp()
	{
		if(_actor.isAllSkillsDisabled())
			return;

		final L2Object target = getTarget();
		
		if(checkTargetLost(target))
			return;

		if(maybeMoveToPawn(target, 36))
			return;

		setIntention(AI_INTENTION_IDLE);
		((L2Summon.AIAccessor) _accessor).doPickupItem(target);

		return;
	}

	private void thinkInteract()
	{
		if(_actor.isAllSkillsDisabled())
			return;

		final L2Object target = getTarget();
		
		if(checkTargetLost(target))
			return;

		if(maybeMoveToPawn(target, 36))
			return;

		setIntention(AI_INTENTION_IDLE);

		return;
	}

	@Override
	protected void onEvtThink()
	{
		if(_thinking || _actor.isAllSkillsDisabled())
			return;

		_thinking = true;

		try
		{
			if(getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if(getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if(getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if(getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.ai.L2CharacterAI#onEvtFinishCasting()
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// TODO Auto-generated method stub
		super.onEvtFinishCasting();
		
		final L2Summon summon = (L2Summon) _actor;

		summon.setFollowStatus(true);
		
	}
	
	
}
