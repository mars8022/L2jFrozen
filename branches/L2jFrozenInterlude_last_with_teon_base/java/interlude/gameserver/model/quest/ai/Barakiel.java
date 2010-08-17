package interlude.gameserver.model.quest.ai;

import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Barakiel extends Quest implements Runnable
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;
	// Barakiel Z coords
	private static final int x1 = 89800;
	private static final int x2 = 93200;
	private static final int y1 = -87038;

	public Barakiel (int questId, String name, String descr)
	{
		super(questId,name,descr);
		int[] mobs = {BARAKIEL};
		for (int mob : mobs)
		{
			this.addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}

	@Override
	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			if (x < x1 || x > x2 || y < y1)
			{
				npc.teleToLocation(91008,-85904,-2736);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public void run()
	{
    	//new Barakiel(-1, "Barakiel", "ai");
    }
}