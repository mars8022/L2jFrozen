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

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSignetCasttime;

/**
 * @author L2jFrozen
 */
public final class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;
	
	public EffectSignet(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_EFFECT;
	}
	
	@Override
	public void onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill()).effectId, getLevel());
		}
		else if (getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
		}
		_actor = (L2EffectPointInstance) getEffected();
	}
	
	@Override
	public boolean onActionTime()
	{
		// if (getCount() == getTotalCount() - 1) return true; // do nothing first time
		if (_skill == null)
			return true;
		final int mpConsume = _skill.getMpConsume();
		final L2PcInstance caster = (L2PcInstance) getEffector();
		
		if (mpConsume > getEffector().getStatus().getCurrentMp())
		{
			getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		
		getEffector().reduceCurrentMp(mpConsume);
		
		for (final L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster || cha.isDead())
				continue;
			
			if (_skill.isOffensive())
			{
				if (cha instanceof L2PcInstance)
				{
					
					if ((((L2PcInstance) cha).getClanId() > 0 && caster.getClanId() > 0 && ((L2PcInstance) cha).getClanId() != caster.getClanId()) || (((L2PcInstance) cha).getAllyId() > 0 && caster.getAllyId() > 0 && ((L2PcInstance) cha).getAllyId() != caster.getAllyId()) || (cha.getParty() != null && caster.getParty() != null && !cha.getParty().equals(caster.getParty())))
					{
						_skill.getEffects(_actor, cha, false, false, false);
						continue;
					}
				}
			}
			else
			{
				if (cha instanceof L2PcInstance)
				{
					if ((cha.getParty() != null && caster.getParty() != null && cha.getParty().equals(caster.getParty())) || (((L2PcInstance) cha).getClanId() > 0 && caster.getClanId() > 0 && ((L2PcInstance) cha).getClanId() == caster.getClanId()) || (((L2PcInstance) cha).getAllyId() > 0 && caster.getAllyId() > 0 && ((L2PcInstance) cha).getAllyId() == caster.getAllyId()))
					{
						_skill.getEffects(_actor, cha, false, false, false);
						_skill.getEffects(_actor, caster, false, false, false); // Affect caster too.
						continue;
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}