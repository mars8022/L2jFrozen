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
package teleports.GrandBossTeleporters;

import interlude.ExternalConfig;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.instancemanager.GrandBossManager;
import interlude.gameserver.instancemanager.QuestManager;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2GrandBossInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.model.quest.QuestState;
import interlude.gameserver.model.zone.type.L2BossZone;
import interlude.util.Rnd;

public class GrandBossTeleporters extends Quest
{
	private final static int[] NPCs =
	{
		13001,31859,31384,31385,31540,31686,31687,31759
	};

	private Quest antharasAI()
	{
		return QuestManager.getInstance().getQuest("antharas");
	}

	private Quest valakasAI()
	{
		return QuestManager.getInstance().getQuest("valakas");
	}

	private int count = 0;

	public GrandBossTeleporters(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}

		if (event.equalsIgnoreCase("31540"))
		{
			if (st.getQuestItemsCount(7267) > 0)
			{
				st.takeItems(7267, 1);
				player.teleToLocation(183813, -115157, -3303);
				st.set("allowEnter","1");
			}
			else
				htmltext = "31540-06.htm";
		}

		return htmltext;
	}

	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		int npcId = npc.getNpcId();
		if (npcId == 13001)
		{
			if (antharasAI() != null)
			{
				int status = GrandBossManager.getInstance().getBossStatus(29019);
				if (status == 0 || status == 1)
				{
					if (st.getQuestItemsCount(3865) > 0)
					{
						st.takeItems(3865, 1);
						L2BossZone zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
						if (zone != null)
							zone.allowPlayerEntry(player, 30);
						int x = 179700 + Rnd.get(700);
						int y = 113800 + Rnd.get(2100);
						player.teleToLocation(x, y, -7709);
						if (status == 0)
						{
							L2GrandBossInstance antharas = GrandBossManager.getInstance().getBoss(29019);
							antharasAI().startQuestTimer("waiting", ExternalConfig.Antharas_Wait_Time, antharas, null);
							GrandBossManager.getInstance().setBossStatus(29019, 1);
						}
					}
					else
						htmltext = "13001-03.htm";
				}
				else if (status == 2)
					htmltext = "13001-02.htm";
				else
					htmltext = "13001-01.htm";
			}
			else
				htmltext = "13001-01.htm";
		}
		else if (npcId == 31859)
		{
			int x = 79800 + Rnd.get(600);
			int y = 151200 + Rnd.get(1100);
			player.teleToLocation(x, y, -3534);
		}
		else if (npcId == 31385)
		{
			if (valakasAI() != null)
			{
				int status = GrandBossManager.getInstance().getBossStatus(29028);
				if (status == 0 || status == 1)
				{
					if (count >= 200)
						htmltext = "31385-03.htm";
					else if (st.getInt("allowEnter") == 1)
					{
						st.unset("allowEnter");
						L2BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
						if (zone != null)
							zone.allowPlayerEntry(player, 30);
						int x = 204328 + Rnd.get(600);
						int y = -111874 + Rnd.get(600);
						player.teleToLocation(x, y, 70);
						count++;
						if (status == 0)
						{
							L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
							valakasAI().startQuestTimer("1001", ExternalConfig.Valakas_Wait_Time, valakas, null);
							GrandBossManager.getInstance().setBossStatus(29028, 1);
						}
					}
					else //player cheated, wasn't ported via npc Klein
						htmltext = "31385-04.htm";
				}
				else if (status == 2)
					htmltext = "31385-02.htm";
				else
					htmltext = "31385-01.htm";
			}
			else
				htmltext = "31385-01.htm";
		}
		else if (npcId == 31384)
			DoorTable.getInstance().getDoor(24210004).openMe();
		else if (npcId == 31686)
			DoorTable.getInstance().getDoor(24210006).openMe();
		else if (npcId == 31687)
			DoorTable.getInstance().getDoor(24210005).openMe();
		else if (npcId == 31540)
		{
			if (count < 50)
				htmltext = "31540-01.htm";
			else if (count < 100)
				htmltext = "31540-02.htm";
			else if (count < 150)
				htmltext = "31540-03.htm";
			else if (count < 200)
				htmltext = "31540-04.htm";
			else
				htmltext = "31540-05.htm";
		}
		else if (npcId == 31759)
		{
			int x = 150037 + Rnd.get(500);
			int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new GrandBossTeleporters(-1, "GrandBossTeleporters", "teleports");
	}
}