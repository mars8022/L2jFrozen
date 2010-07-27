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
package interlude.gameserver.skills.effects;

import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.EtcStatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.skills.Env;

/**
 * @author Forsaiken
 */
public class EffectRecoverForce extends L2Effect
{
	public EffectRecoverForce(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) getEffected();
			L2Skill skill = null;
			if (player.getSkillLevel(8) > 0) {
				skill = SkillTable.getInstance().getInfo(8, player.getSkillLevel(8));
			} else if (player.getSkillLevel(50) > 0) {
				skill = SkillTable.getInstance().getInfo(50, player.getSkillLevel(50));
			}
			if (skill != null)
			{
				EffectCharge effect = (EffectCharge) player.getFirstEffect(skill);
				if (effect != null)
				{
					if (effect.numCharges < skill.getNumCharges())
					{
						effect.numCharges++;
						player.sendPacket(new EtcStatusUpdate(player));
						SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
						sm.addNumber(effect.numCharges);
						player.sendPacket(sm);
					}
				} else {
					skill.getEffects(player, player);
				}
			}
		}
		return true;
	}
}