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

import java.util.logging.Logger;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.model.L2Clan;

/**
 * @author Vice [L2jOneo]
 */
public class FortUpdater implements Runnable
{
	protected static Logger _log = Logger.getLogger(FortUpdater.class.getName());
	private L2Clan _clan;
	private int _runCount = 0;

	public FortUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}

	public void run()
	{
		try
		{
			// Increment Reputation every 6 hour
			if (_clan.getHasFort() > 0)
			{
				if (_runCount % 6 == 0)
					_clan.setReputationScore(_clan.getReputationScore() + 1, true);

				_runCount++;
				FortUpdater cu = new FortUpdater(_clan, _runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}
