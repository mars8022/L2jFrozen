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
package com.l2scoria.gameserver.handler.skillhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.handler.ISkillHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SummonInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Env;
import com.l2scoria.gameserver.skills.Formulas;
import com.l2scoria.gameserver.skills.Stats;
import com.l2scoria.gameserver.skills.funcs.Func;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

/**
 * @author programmos, sword dev
 */
public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = { SkillType.BLOW };

	private int _successChance;

	/*
	public final static int FRONT = 50;
	public final static int SIDE = 60;
	public final static int BEHIND = 70;
	*/

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
			return;

		for(L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			if(target.isAlikeDead())
				continue;

			// Calculate skill evasion
			Formulas.getInstance();
			if(Formulas.calcPhysicalSkillEvasion(target, skill))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
				continue;
			}
			// Calculate vengeance
			if(target.vengeanceSkill(skill))
			{
				target = activeChar;
			}

			if(activeChar.isBehindTarget())
				_successChance = Config.BLOW_ATTACK_BEHIND;
			else if(activeChar.isFrontTarget())
				_successChance = Config.BLOW_ATTACK_FRONT;
			else
				_successChance = Config.BLOW_ATTACK_SIDE;

			//If skill requires Crit or skill requires behind,
			//calculate chance based on DEX, Position and on self BUFF
			if(((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && _successChance == Config.BLOW_ATTACK_BEHIND || ((skill.getCondition() & L2Skill.COND_CRIT) != 0) && Formulas.getInstance().calcBlow(activeChar, target, _successChance))
			{
				if(skill.hasEffects())
				{
					if(target.reflectSkill(skill))
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(null, activeChar);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
						sm = null;
					}
				}
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER);
				boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);

				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = false;
				if(Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
					crit = true;

				double damage = (int) Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
				if(crit)
				{
					damage *= 2;
					// Vicious Stance is special after C5, and only for BLOW skills
					// Adds directly to damage
					L2Effect vicious = activeChar.getFirstEffect(312);
					if(vicious != null && damage > 1)
					{
						for(Func func : vicious.getStatFuncs())
						{
							Env env = new Env();
							env.player = activeChar;
							env.target = target;
							env.skill = skill;
							env.value = damage;
							func.calc(env);
							damage = (int) env.value;

							env = null;
						}
					}
				}

				if(soul && weapon != null)
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

				weapon = null;

				if(skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					if(!player.isInvul())
					{
						// Check and calculate transfered damage 
						L2Summon summon = player.getPet();
						if(summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
						{
							int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

							// Only transfer dmg up to current HP, it should not be killed 
							if(summon.getCurrentHp() < tDmg)
								tDmg = (int) summon.getCurrentHp() - 1;
							if(tDmg > 0)
							{
								summon.reduceCurrentHp(tDmg, activeChar);
								damage -= tDmg;
							}
							summon = null;
						}
						if(damage >= player.getCurrentHp())
						{
							if(player.isInDuel())
								player.setCurrentHp(1);
							else
							{
								player.setCurrentHp(0);
								if(player.isInOlympiadMode())
								{
									player.abortAttack();
									player.abortCast();
									player.getStatus().stopHpMpRegeneration();
								}
								else
									player.doDie(activeChar);
							}
						}
						else
							player.setCurrentHp(player.getCurrentHp() - damage);
					}
					SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
					smsg.addString(activeChar.getName());
					smsg.addNumber((int) damage);
					player.sendPacket(smsg);
					smsg = null;

					player = null;
				}
				else
					target.reduceCurrentHp(damage, activeChar);

				if(activeChar instanceof L2PcInstance)
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
				sm.addNumber((int) damage);
				activeChar.sendPacket(sm);
				sm = null;
			}
			//Possibility of a lethal strike
			if(!target.isRaid() && !(target instanceof L2DoorInstance) && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				//2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
				if(skill.getLethalChance2() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if(target instanceof L2NpcInstance)
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
					else if(target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
					{
						L2PcInstance player = (L2PcInstance) target;
						if(!player.isInvul())
						{
							player.setCurrentHp(1);
							player.setCurrentCp(1);
							player = null;
						}
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
				else if(skill.getLethalChance1() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if(target instanceof L2PcInstance)
					{
						L2PcInstance player = (L2PcInstance) target;
						if(!player.isInvul())
							player.setCurrentCp(1); // Set CP to 1
						player = null;
					}
					else if(target instanceof L2NpcInstance) // If is a monster remove first damage and after 50% of current hp
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
			}

			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			//Self Effect
			if(effect != null && effect.isSelfEffect())
				effect.exit();
			skill.getEffectsSelf(activeChar);

			effect = null;
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
