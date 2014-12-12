/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.ai.special;

import java.util.Collection;

import javolution.util.FastList;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.random.Rnd;

public class Monastery extends Quest implements Runnable
{
	static final int[] mobs1 =
	{
		22124,
		22125,
		22126,
		22127,
		22129
	};
	static final int[] mobs2 =
	{
		22134,
		22135
	};
	// TODO: npcstring
	static final String[] text =
	{
		"You cannot carry a weapon without authorization!",
		"name, why would you choose the path of darkness?!",
		"name! How dare you defy the will of Einhasad!"
	};
	
	public Monastery(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		registerMobs(mobs1, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(mobs2, QuestEventType.ON_SPELL_FINISHED);
	}
	
	@Override
	public String onAggroRangeEnter(final L2NpcInstance npc, final L2PcInstance player, final boolean isPet)
	{
		if (Util.contains(mobs1, npc.getNpcId()) && !npc.isInCombat() && npc.getTarget() == null)
		{
			if (player.getActiveWeaponInstance() != null && !player.isSilentMoving())
			{
				npc.setTarget(player);
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), text[0]));
				
				switch (npc.getNpcId())
				{
					case 22124:
					case 22126:
					{
						final L2Skill skill = SkillTable.getInstance().getInfo(4589, 8);
						npc.doCast(skill);
						break;
					}
					default:
					{
						npc.setIsRunning(true);
						((L2Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
					}
				}
			}
			else if (((L2Attackable) npc).getMostHated() == null)
				return null;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(final L2NpcInstance npc)
	{
		if (Util.contains(mobs1, npc.getNpcId()))
		{
			final FastList<L2PlayableInstance> result = new FastList<>();
			final Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			for (final L2Object obj : objs)
			{
				if (obj instanceof L2PcInstance || obj instanceof L2PetInstance)
				{
					if (Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((L2Character) obj).isDead())
						result.add((L2PlayableInstance) obj);
				}
			}
			if (!result.isEmpty() && result.size() != 0)
			{
				final Object[] characters = result.toArray();
				for (final Object obj : characters)
				{
					final L2PlayableInstance target = (L2PlayableInstance) (obj instanceof L2PcInstance ? obj : ((L2Summon) obj).getOwner());
					
					if (target.getActiveWeaponInstance() == null || (target instanceof L2PcInstance && ((L2PcInstance) target).isSilentMoving()) || (target instanceof L2Summon && ((L2Summon) target).getOwner().isSilentMoving()))
					{
						continue;
					}
					
					if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
					{
						npc.setTarget(target);
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), text[0]));
						switch (npc.getNpcId())
						{
							case 22124:
							case 22126:
							case 22127:
							{
								final L2Skill skill = SkillTable.getInstance().getInfo(4589, 8);
								npc.doCast(skill);
								break;
							}
							default:
							{
								npc.setIsRunning(true);
								((L2Attackable) npc).addDamageHate(target, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								break;
							}
						}
					}
				}
			}
		}
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(final L2NpcInstance npc, final L2PcInstance player, final L2Skill skill)
	{
		if (Util.contains(mobs1, npc.getNpcId()) && skill.getId() == 4589)
		{
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		
		if (Util.contains(mobs2, npc.getNpcId()))
		{
			if (skill.getSkillType() == SkillType.AGGDAMAGE)
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), text[Rnd.get(2) + 1].replace("name", player.getName())));
				((L2Attackable) npc).addDamageHate(player, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				
			}
		}
		
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public void run()
	{
	}
	
}