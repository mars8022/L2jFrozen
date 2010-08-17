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
package interlude.gameserver.skills.l2skills;

import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.EtcStatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.skills.effects.EffectCharge;
import interlude.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill
{
	final int numCharges;

	public L2SkillCharge(StatsSet set)
	{
		super(set);
		numCharges = set.getInteger("num_charges", getLevel());
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead()) {
			return;
		}
		// get the effect
		EffectCharge effect = (EffectCharge) caster.getFirstEffect(this);
		if (effect != null)
		{
			if (effect.numCharges < numCharges)
			{
				effect.numCharges++;
				if (caster instanceof L2PcInstance)
				{
					caster.sendPacket(new EtcStatusUpdate((L2PcInstance) caster));
					SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
					sm.addNumber(effect.numCharges);
					caster.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXIMUM);
				caster.sendPacket(sm);
			}
			return;
		}
		getEffects(caster, caster);
		// effect self :]
		// L2Effect seffect = caster.getEffect(getId());
		// TODO ?? this is always null due to a return in the if block above!
		// if (effect != null && seffect.isSelfEffect())
		// {
		// Replace old effect with new one.
		// seffect.exit();
		// }
		// cast self effect if any
		getEffectsSelf(caster);
	}
}
