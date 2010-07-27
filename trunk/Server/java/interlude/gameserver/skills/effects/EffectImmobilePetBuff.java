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

import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Summon;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.skills.Env;

/**
 * @author demonia TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
final class EffectImmobilePetBuff extends L2Effect
{
	private L2Summon _pet;

	public EffectImmobilePetBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		_pet = null;
		if (getEffected() instanceof L2Summon && getEffector() instanceof L2PcInstance && ((L2Summon) getEffected()).getOwner() == getEffector())
		{
			_pet = (L2Summon) getEffected();
			_pet.setIsImmobilized(true);
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		if (_pet != null) {
			_pet.setIsImmobilized(false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}