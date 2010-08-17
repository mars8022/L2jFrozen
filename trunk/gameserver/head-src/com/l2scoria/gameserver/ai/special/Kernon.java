package com.l2scoria.gameserver.ai.special;

import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Kernon extends Quest implements Runnable
{
	// Kernon NpcID
	private static final int KERNON = 25054;
	// Kernon Z coords
	private static final int z1 = 3900;
	private static final int z2 = 4300;

	public Kernon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(KERNON, Quest.QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == KERNON)
		{
			int z = npc.getZ();
			if(z > z2 || z < z1)
			{
				npc.teleToLocation(113420, 16424, 3969);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public void run()
	{}
}
