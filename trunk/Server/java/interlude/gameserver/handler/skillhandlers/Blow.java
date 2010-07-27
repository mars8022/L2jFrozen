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
package interlude.gameserver.handler.skillhandlers;

import interlude.Config;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Summon;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2SummonInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.skills.Env;
import interlude.gameserver.skills.Formulas;
import interlude.gameserver.skills.Stats;
import interlude.gameserver.skills.funcs.Func;
import interlude.gameserver.templates.L2WeaponType;
import interlude.gameserver.util.Util;
import interlude.util.Rnd;

/**
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = { SkillType.BLOW };
	private int _successChance;
	public final static int FRONT = Config.FRONT_BLOW_SUCCESS;
	public final static int SIDE = Config.SIDE_BLOW_SUCCESS;
	public final static int BACK = Config.BACK_BLOW_SUCCESS;

	@SuppressWarnings("static-access")
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		for (L2Object target2 : targets) {
			L2Character target = (L2Character) target2;
			if (target.isAlikeDead())
			{
				continue;
			}
			// Check firstly if target dodges skill
			boolean skillIsEvaded = Formulas.getInstance().calcPhysicalSkillEvasion(target, skill);
			if (activeChar.isBehindTarget())
			{
				_successChance = BACK;
			}
			else if (activeChar.isFrontTarget())
			{
				_successChance = FRONT;
			}
			else
			{
				_successChance = SIDE;
			}
			// If skill requires Crit or skill requires behind,
			// calculate chance based on DEX, Position and on self BUFF
			if ((skill.getCondition() & L2Skill.COND_BACK) != 0 && _successChance == BACK && !skillIsEvaded || (skill.getCondition() & L2Skill.COND_CRIT) != 0 && Formulas.getInstance().calcBlow(activeChar, target, _successChance))
			{
				if (skill.hasEffects())
				{
					if (target.reflectSkill(skill))
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(null, activeChar);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
					}
				}
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER;
				boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = false;
				if (Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
				{
					crit = true;
				}
				double damage = (int) Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
				if (crit)
				{
					damage *= 2;
					// Vicious Stance is special after C5, and only for BLOW
					// skills
					// Adds directly to damage
					L2Effect vicious = activeChar.getFirstEffect(312);
					if (vicious != null && damage > 1)
					{
						for (Func func : vicious.getStatFuncs())
						{
							Env env = new Env();
							env.player = activeChar;
							env.target = target;
							env.skill = skill;
							env.value = damage;
							func.calc(env);
							damage = (int) env.value;
						}
					}
				}
				if (soul && weapon != null)
				{
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				}
				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					if (!player.isInvul())
					{
						// Check and calculate transfered damage
						L2Summon summon = player.getPet();
						if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
						{
							int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
							// Only transfer dmg up to current HP, it should not be killed
							if (summon.getCurrentHp() < tDmg) {
								tDmg = (int) summon.getCurrentHp() - 1;
							}
							if (tDmg > 0)
							{
								summon.reduceCurrentHp(tDmg, activeChar);
								damage -= tDmg;
							}
						}
						if (damage >= player.getCurrentHp())
						{
							if (player.isInDuel())
							{
								player.setCurrentHp(1);
							}
							else
							{
								player.setCurrentHp(0);
								if (player.isInOlympiadMode())
								{
									player.abortAttack();
									player.abortCast();
									player.getStatus().stopHpMpRegeneration();
								}
								else
								{
									player.doDie(activeChar);
								}
							}
						}
						else
						{
							player.setCurrentHp(player.getCurrentHp() - damage);
						}
					}
					SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
					smsg.addString(activeChar.getName());
					smsg.addNumber((int) damage);
					player.sendPacket(smsg);
				}
				else
				{
					target.reduceCurrentHp(damage, activeChar);
				}
				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
				}
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
				sm.addNumber((int) damage);
				activeChar.sendPacket(sm);
			}
			// Possibility of a lethal strike
			if (!target.isRaid() && !(target instanceof L2DoorInstance) && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				// 2nd lethal effect activate (cp,hp to 1 or if target is npc
				// then hp to 1)
				if (skill.getLethalChance2() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if (target instanceof L2NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
					}
					else if (target instanceof L2PcInstance) // If is a
					// active
					// player set his HP
					// and CP to 1
					{
						L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
						{
							player.setCurrentHp(1);
							player.setCurrentCp(1);
						}
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
				else if (skill.getLethalChance1() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if (target instanceof L2PcInstance)
					{
						L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
						{
							player.setCurrentCp(1); // Set CP to 1
						}
					}
					else if (target instanceof L2NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
			}
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			// Self Effect
			if (effect != null && effect.isSelfEffect())
			{
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
