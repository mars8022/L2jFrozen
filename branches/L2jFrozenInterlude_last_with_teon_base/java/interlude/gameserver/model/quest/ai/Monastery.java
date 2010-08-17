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
package interlude.gameserver.model.quest.ai;

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2Attackable;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.network.serverpackets.CreatureSay;
import interlude.gameserver.util.Util;
import interlude.util.Rnd;

public class Monastery extends Quest implements Runnable
{
	public Monastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {22124, 22125, 22126, 22127, 22129};
		for (int mob : mobs)
		{
			this.addEventId(mob, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
			this.addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}

	private static boolean _isAttacked = false;

	@Override
	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 22129 && _isAttacked == false && Rnd.get(100) < 50)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Brother " + attacker.getName() + ", move your weapon away!!"));

		_isAttacked = true;

		return super.onAttack (npc, attacker, damage, isPet);
	}

	@Override
	public String onAggroRangeEnter (L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		if (Util.calculateDistance(npc.getX(), npc.getY(), npc.getZ(), player.getX(), player.getY()) < 300)
		{
			L2Character target = isPet ? player.getPet().getOwner() : player;
			if (target.getActiveWeaponItem() != null && npc.getCurrentHp() > 1)
			{
				if (npc.getNpcId() == 22129)
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Brother " + target.getName() + ", move your weapon away!!"));
				else
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "You cannot carry a weapon without authorization!"));
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else
			{
				((L2Attackable) npc).getAggroList().remove(target);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			}
		}
		return super.onAggroRangeEnter (npc, player, isPet);
	}

	@Override
	public void run()
	{
		//new Monastery(-1, "Monastery", "ai");
	}
}