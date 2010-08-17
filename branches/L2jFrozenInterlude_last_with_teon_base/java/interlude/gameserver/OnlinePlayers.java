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
package interlude.gameserver;

import interlude.Config;
import interlude.gameserver.model.L2World;

public class OnlinePlayers
{
	private static OnlinePlayers _instance;

	class AnnounceOnline implements Runnable
	{
		public void run()
		{
			int PLAYERS_ONLINE = L2World.getInstance().getAllPlayers().size() + Config.PLAYERS_ONLINE_TRICK;
			Announcements.getInstance().announceToAll("Online Players: " + PLAYERS_ONLINE);
			if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceOnline(), Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
			}
		}
	}

	public static OnlinePlayers getInstance()
	{
		if (_instance == null)
		{
			_instance = new OnlinePlayers();
		}
		return _instance;
	}

	private OnlinePlayers()
	{
		if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceOnline(), Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
		}
	}
}