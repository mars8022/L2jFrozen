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

import interlude.gameserver.GeoData;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.Location;
import interlude.gameserver.model.actor.instance.L2FolkInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2SiegeFlagInstance;
import interlude.gameserver.model.actor.instance.L2SiegeGuardInstance;
import interlude.gameserver.model.actor.instance.L2SiegeSummonInstance;
import interlude.gameserver.skills.Env;

/**
 * @author littlecrow Implementation of the Fear Effect
 */
final class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;

	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		if (!getEffected().isAfraid())
		{
			getEffected().startFear();
			onActionTime();
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().stopFear(this);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected() instanceof L2PcInstance && getEffector() instanceof L2PcInstance && getSkill().getId() != 1376 && getSkill().getId() != 1169 && getSkill().getId() != 65 && getSkill().getId() != 1092) return false;
		if(getEffected() instanceof L2FolkInstance) return false;
		if(getEffected() instanceof L2SiegeGuardInstance) return false;
		if(getEffected() instanceof L2SiegeFlagInstance) return false;
		if(getEffected() instanceof L2SiegeSummonInstance) return false;

		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		
		int signx=-1;
		int signy=-1;
		if (getEffected().getX()>getEffector().getX())
			signx=1;
		if (getEffected().getY()>getEffector().getY())
			signy=1;
		posX += signx*FEAR_RANGE;
		posY += signy*FEAR_RANGE;
		Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
		getEffected().setRunning();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destiny.getX(), destiny.getY(), destiny.getZ(), 0));
		return true;
	}
}