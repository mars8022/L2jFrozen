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
package com.l2scoria.gameserver.ai.special;

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.model.L2Attackable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

public class Monastery extends Quest implements Runnable
{
	public Monastery(int questId, String name, String descr)
	{
		super(questId, name, descr);

		int[] mobs =
		{
				22124, 22125, 22126, 22127, 22129
		};
		for(int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}

	private static boolean _isAttacked = false;

	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == 22129 && _isAttacked == false && Rnd.get(100) < 50)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Brother " + attacker.getName() + ", move your weapon away!!"));
		}

		_isAttacked = true;

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		if(Util.calculateDistance(npc.getX(), npc.getY(), npc.getZ(), player.getX(), player.getY()) < 300)
		{
			L2Character target = isPet ? player.getPet().getOwner() : player;
			if(target.getActiveWeaponItem() != null && npc.getCurrentHp() > 1)
			{
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else
			{
				((L2Attackable) npc).getAggroListRP().remove(target);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public void run()
	{}
}
