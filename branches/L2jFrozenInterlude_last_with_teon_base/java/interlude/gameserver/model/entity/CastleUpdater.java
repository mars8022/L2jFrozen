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

import interlude.Config;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.item.ItemContainer;

/**
 * Thorgrim - 2005 Class managing periodical events with castle
 */
public class CastleUpdater implements Runnable
{
	protected static Logger _log = Logger.getLogger(CastleUpdater.class.getName());
	private L2Clan _clan;
	private int _runCount = 0;

	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}

	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if (warehouse != null && _clan.getHasCastle() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if (_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						_log.info("Manor System: all data for " + castle.getName() + " saved");
					}
				}
				if (_runCount % 3 == 0)
					warehouse.addItem("Castle", 5126, 1, null, null);

				if (_runCount % 24 == 0)
				{
					if (castle.getCastleId() >= 1 && castle.getCastleId() < 5 || castle.getCastleId() == 6)
						warehouse.addItem("Castle", 6622, 1, null, null);
					else
						warehouse.addItem("Castle", 6622, 2, null, null);
				}
				if (_runCount % 6 == 0)
					_clan.setReputationScore(_clan.getReputationScore() + 1, true);

				_runCount++;
				CastleUpdater cu = new CastleUpdater(_clan, _runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}
