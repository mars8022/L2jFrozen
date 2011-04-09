package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Golkonda extends Quest implements Runnable
{
	// Golkonda NpcID
	private static final int GOLKONDA = 25126;
	// Golkonda Z coords
	private static final int z1 = 6900;
	private static final int z2 = 7500;

	public Golkonda(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(GOLKONDA, Quest.QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == GOLKONDA)
		{
			int z = npc.getZ();
			if(z > z2 || z < z1)
			{
				npc.teleToLocation(116313, 15896, 6999);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public void run()
	{}
}
