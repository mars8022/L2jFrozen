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
package ai.group_template;

import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author  Maxi
 * to java Kidzor
 */
public class AncientEGG extends L2AttackableAIScript
{
	private int EGG = 18344;

	public AncientEGG(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(EGG);
	}

	public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet)
	{
		player.setTarget(player);
		player.doCast(SkillTable.getInstance().getInfo(5088,1));
		return super.onAttack(npc, player, damage, isPet);
	}

	public static void main(String[] args)
	{
		new AncientEGG(-1, "AncientEGG", "ai");
	}
}