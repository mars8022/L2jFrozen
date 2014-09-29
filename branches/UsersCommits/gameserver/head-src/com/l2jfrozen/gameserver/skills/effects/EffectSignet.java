/* This program is free software; you can redistribute it and/or modify
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
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
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
	
	public EffectSignet(Env env, EffectTemplate template)
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
		int mpConsume = _skill.getMpConsume();
		L2PcInstance caster = (L2PcInstance) getEffector();
		
		if (mpConsume > getEffector().getStatus().getCurrentMp())
		{
			getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		
		getEffector().reduceCurrentMp(mpConsume);
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster || cha.isDead())
				continue;
			
			if (_skill.isOffensive())
			{
				/*
				 * Like L2OFF if the skill is offensive must not effect the caster, clan, ally, party
				 */
				
				if (cha instanceof L2PlayableInstance)
				{
					if ((cha instanceof L2Summon && ((L2Summon) cha).getOwner() == caster))
						continue;
				}
				
				if (cha instanceof L2Summon)
					cha = ((L2Summon) cha).getOwner();
				
				if (cha instanceof L2PcInstance)
				{
					if (((L2PcInstance) cha).getClanId() > 0 && ((L2PcInstance) getEffector()).getClanId() > 0 && ((L2PcInstance) cha).getClanId() == ((L2PcInstance) getEffector()).getClanId())
						continue;
					
					if (((L2PcInstance) cha).getAllyId() > 0 && ((L2PcInstance) getEffector()).getAllyId() > 0 && ((L2PcInstance) cha).getAllyId() == ((L2PcInstance) getEffector()).getAllyId())
						continue;
					
					if ((getEffector().getParty() != null && cha.getParty() != null && getEffector().getParty().equals(cha.getParty())))
						continue;
				}
				_skill.getEffects(_actor, cha, false, false, false);
			}
			else
			{
				/*
				 * Like L2OFF if the skill is not offensive must effect only the caster, clan, ally, party
				 */
				
				if (cha instanceof L2PlayableInstance)
				{
					if (!(cha instanceof L2Summon && ((L2Summon) cha).getOwner() == caster))
						continue;
				}
				
				if (cha instanceof L2Summon)
					cha = ((L2Summon) cha).getOwner();
				
				if (cha instanceof L2PcInstance)
				{
					if (((L2PcInstance) cha).getClanId() > 0 && ((L2PcInstance) getEffector()).getClanId() > 0 && ((L2PcInstance) cha).getClanId() != ((L2PcInstance) getEffector()).getClanId())
						continue;
					
					if (((L2PcInstance) cha).getAllyId() > 0 && ((L2PcInstance) getEffector()).getAllyId() > 0 && ((L2PcInstance) cha).getAllyId() != ((L2PcInstance) getEffector()).getAllyId())
						continue;
					
					if ((getEffector().getParty() != null && cha.getParty() != null && !getEffector().getParty().equals(cha.getParty())))
						continue;
				}
				_skill.getEffects(_actor, cha, false, false, false);
			}
			
			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			_actor.broadcastPacket(new MagicSkillUser(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0));
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