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
package interlude.gameserver.model.entity.RaidEngine;

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.Announcements;
import interlude.gameserver.model.actor.instance.L2EventManagerInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;

public class RaidFightManager
{
	protected static final Logger _log = Logger.getLogger(RaidFightManager.class.getName());

	/**
	 * The task method to handle cycles of the event<br>
	 * <br>
	 *
	 * @see java.lang.Runnable#run()<br>
	 */
	public void run()
	{
		// TODO: Add initial breaks. And check performance
		for (;;)
		{
			waiter(Config.RAID_SYSTEM_FIGHT_TIME * 60); // in configuration
			// given as minutes
			for (L2NpcInstance eventMob : L2RaidEvent._eventMobList)
			{
				eventMob.decayMe();
				eventMob.deleteMe();
				L2EventManagerInstance._currentEvents -= 1;
			}
			_log.warning("Raid Engines: All the Members from the Event are now dead or Have Left The event. Event Finished.");
			break;
		}
	}

	void waiter(int seconds)
	{
		while (seconds > 1)
		{
			seconds--; // here because we don't want to see two time announce
			// at the same time
			if (L2RaidEvent.isParticipating())
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						L2RaidEvent.sysMsgToAllParticipants("You have One our left to kill the Raid Boss.");
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						Announcements.getInstance().announceToAll("L2Raid Event: " + seconds / 60 + " minute(s) untill Boss Disapears!");
						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					case 5: // 5 seconds left
					case 4: // 4 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						Announcements.getInstance().announceToAll("L2Raid Event: " + seconds + " second(s) untill Boss Disapears!");
						break;
				}
			}
			long oneSecWaitStart = System.currentTimeMillis();
			while (oneSecWaitStart + 1000L > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
}