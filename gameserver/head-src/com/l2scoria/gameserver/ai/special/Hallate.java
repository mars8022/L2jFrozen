package com.l2scoria.gameserver.ai.special;

import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Hallate extends Quest implements Runnable
{
	// Hallate NpcID
	private static final int HALLATE = 25220;
	// Hallate Z coords
	private static final int z1 = -2150;
	private static final int z2 = -1650;

	public Hallate(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(HALLATE, Quest.QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == HALLATE)
		{
			int z = npc.getZ();
			if(z > z2 || z < z1)
			{
				npc.teleToLocation(113548, 17061, -2125);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public void run()
	{}
}
