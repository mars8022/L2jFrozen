package interlude.gameserver.model.quest.ai;

import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.quest.Quest;

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

	public Golkonda (int questId, String name, String descr)
	{
		super(questId,name,descr);
		int[] mobs = {GOLKONDA};
		
		for (int mob : mobs)
		{
			this.addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == GOLKONDA)
		{
			int z = npc.getZ();
			if (z > z2 || z < z1)
			{
				npc.teleToLocation(116313,15896,6999);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public void run()
	{
    	//new Golkonda(-1, "Golkonda", "ai");
    }
}