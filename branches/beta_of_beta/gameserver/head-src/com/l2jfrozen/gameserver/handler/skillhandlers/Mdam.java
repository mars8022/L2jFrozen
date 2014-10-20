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
package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.logs.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.8.2.9 $ $Date: 2005/04/05 19:41:23 $
 */

public class Mdam implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Mdam.class.getName());

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IItemHandler#useItem(com.l2jfrozen.gameserver.model.L2PcInstance, com.l2jfrozen.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.MDAM,
		SkillType.DEATHLINK
	};

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IItemHandler#useItem(com.l2jfrozen.gameserver.model.L2PcInstance, com.l2jfrozen.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
			return;

		boolean bss = activeChar.checkBss();
		boolean sps = activeChar.checkSps();
		
		for(L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;

			if(activeChar instanceof L2PcInstance && target instanceof L2PcInstance
				&& target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if(target.isAlikeDead())
			{
				if(skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB && target instanceof L2NpcInstance)
				{
					((L2NpcInstance) target).endDecayTask();
				}
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));

			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, sps, bss, mcrit);

			if(damage > 5000 && Config.LOG_HIGH_DAMAGES && activeChar instanceof L2PcInstance)
			{
				String name = "";
				if(target instanceof L2RaidBossInstance) name = "RaidBoss ";
				if(target instanceof L2NpcInstance)
					name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
				if(target instanceof L2PcInstance)
					name = target.getName() + "(" + target.getObjectId() + ") ";
				name += target.getLevel() + " lvl";
				Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") "
					+ activeChar.getLevel() + " lvl did damage " + damage + " with skill "
					+ skill.getName() + "(" + skill.getId() + ") to " + name, "damage_mdam");
			}

			// Why are we trying to reduce the current target HP here?
			// Why not inside the below "if" condition, after the effects processing as it should be?
			// It doesn't seem to make sense for me. I'm moving this line inside the "if" condition, right after the effects processing...
			// [changed by nexus - 2006-08-15]
			//target.reduceCurrentHp(damage, activeChar);

			if(damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if(!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if(skill.hasEffects())
				{
					if(target.reflectSkill(skill))
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(null, activeChar,false,sps,bss);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
						sm = null;
					}
					else
					{
						// activate attacked effects, if any
						if(Formulas.getInstance().calcSkillSuccess(activeChar, target, skill,false,sps,bss))
						{
							// Like L2OFF must remove the first effect only if the second effect is successful
							target.stopSkillEffects(skill.getId());
							skill.getEffects(activeChar, target,false,sps,bss);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
							sm = null;
						}
					}
				}

				target.reduceCurrentHp(damage, activeChar);
			}
			target = null;
		}
		
		if (bss){
			activeChar.removeBss();
		}else if(sps){
			activeChar.removeSps();
		}
		
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if(effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit(false);
		}
		effect = null;
		skill.getEffectsSelf(activeChar);

		if(skill.isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.setCurrentHp(0);
		}
		
		activeChar = null;
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
