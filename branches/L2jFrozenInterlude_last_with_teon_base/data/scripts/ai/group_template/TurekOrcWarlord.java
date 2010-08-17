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

import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.NpcSay;
import interlude.util.Rnd;

/**
 * @author Maxi
 */
public class TurekOrcWarlord extends L2AttackableAIScript
{
	private static final int TUREKW = 20495;

	private static boolean _FirstAttacked;

	public TurekOrcWarlord(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {TUREKW};
		registerMobs(mobs);
		_FirstAttacked = false;
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
        if (npc.getNpcId() == TUREKW)
        {
            if (_FirstAttacked)
            {
               if (Rnd.get(100) == 40)
            	   npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),"You wont take me down easily."));
            }
            else
            {
               _FirstAttacked = true;
           npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),"The battle has just begun!"));
		}
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
        int npcId = npc.getNpcId();
        if (npcId == TUREKW)
        {
            _FirstAttacked = false;
        }
        return super.onKill(npc,killer,isPet);
    }

	public static void main(String[] args)
	{
		new TurekOrcWarlord(-1, "TurekOrcWarlord", "ai");
	}
}