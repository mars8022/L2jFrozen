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
public class CatsEyeBandit extends L2AttackableAIScript
{
	private static final int BANDIT = 27038;

	private static boolean _FirstAttacked;

	public CatsEyeBandit(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {BANDIT};
		registerMobs(mobs);
		_FirstAttacked = false;
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
        if (npc.getNpcId() == BANDIT)
        {
            if (_FirstAttacked)
            {
               if (Rnd.get(100) == 40)
            	   npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),"You childish fool, do you think you can catch me?"));
            }
            else
            {
               _FirstAttacked = true;
		}
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
        int npcId = npc.getNpcId();
        if (npcId == BANDIT)
        {
		int objId = npc.getObjectId();
               if (Rnd.get(100) == 80)
            npc.broadcastPacket(new NpcSay(objId,0,npcId,"I must do something about this shameful incident..."));
            _FirstAttacked = false;
        }
        return super.onKill(npc,killer,isPet);
    }

	public static void main(String[] args)
	{
		new CatsEyeBandit(-1, "CatsEyeBandit", "ai");
	}
}