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
package interlude.gameserver.model.entity;

import interlude.Config;
import interlude.gameserver.Announcements;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.model.L2World;

/**
 * @author DaRkRaGe
 */
public class FactionPlayers
{
	private static FactionPlayers _instance;

	class AnnounceFaction implements Runnable
	{
		public void run()
		{
			Announcements.getInstance().announceToAll(Config.KOOFS_NAME_TEAM + L2World.getInstance().getAllkoofPlayersCount() + " || " + Config.NOOBS_NAME_TEAM + L2World.getInstance().getAllnoobPlayersCount());
			ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceFaction(), Config.FACTION_ANNOUNCE_TIME);
		}
	}

	public static FactionPlayers getInstance()
	{
		if (_instance == null)
		{
			_instance = new FactionPlayers();
		}
		return _instance;
	}

	private FactionPlayers()
	{
		if (Config.FACTION_ANNOUNCE_TIME > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceFaction(), Config.FACTION_ANNOUNCE_TIME);
		}
	}
}