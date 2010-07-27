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
package interlude.gameserver.model;

import java.util.Iterator;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Just a quick scratch
 *
 * @author NB4L1
 */
public class L2SkillZone
{
	@SuppressWarnings("unused")
	private static final Log _log = LogFactory.getLog(L2SkillZone.class.getName());
	private final L2PcInstance _caster;
	private final L2WorldRegion _region;
	private final L2Skill _triggerSkill;
	@SuppressWarnings("unused")
	private final L2Skill _triggeredSkill;
	private final int _triggeredCount;
	private final int _triggeredDelay;
	private final boolean _standAlone;

	public L2SkillZone(L2PcInstance caster, L2Skill skill1, L2Skill skill2, int count, int delay, boolean standAlone)
	{
		_caster = caster;
		_region = _caster.getWorldRegion();
		_triggerSkill = skill1;
		_triggeredSkill = skill2;
		_triggeredCount = count;
		_triggeredDelay = delay;
		// does the caster have to cast druing the whole time, or it just makes the zone?
		// symbol of ... - just makes the zone
		// aoe spells - casting during the whole time...
		_standAlone = standAlone;
		ThreadPoolManager.getInstance().scheduleGeneral(new ZoneCheck(_triggeredCount), _triggeredDelay);
	}

	private class ZoneCheck implements Runnable
	{
		private int _count;

		public ZoneCheck(int count)
		{
			@SuppressWarnings("unused")
			int _count = count;
		}

		private void regionCheck(L2WorldRegion reg)
		{
			Iterator<L2PlayableInstance> playables = reg.iterateAllPlayers();
			while (playables.hasNext())
			{
				L2PlayableInstance activeChar = playables.next();
				checkOnPlayer(activeChar);
			}
		}

		private void checkOnPlayer(L2PlayableInstance activeChar)
		{
			// Check for the actual position... to apply/remove effect... or to deal damage, etc
		}

		private boolean shouldContinue()
		{
			// more check needed for conditions about caster... etc
			_count--;
			if (!_standAlone)
			{
				if (!_caster.isCastingNow() 
						|| _caster.getCurrentSkill() == null 
						|| _triggerSkill.getId() != _caster.getCurrentSkill().getSkillId())
					return false;
			}
			return _count > 0;
		}

		public void run()
		{
			// global things effects
			regionCheck(_region);
			for (L2WorldRegion neighbour : _region.getSurroundingRegions())
			{
				regionCheck(neighbour);
			}
			// scheduling
			if (shouldContinue())
				ThreadPoolManager.getInstance().scheduleGeneral(new ZoneCheck(_count), _triggeredDelay);
			else
			{
				// destroy of the zone
			}
		}
	}
}