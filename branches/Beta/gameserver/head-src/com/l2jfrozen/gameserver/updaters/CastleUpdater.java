/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.updaters;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.logs.Log;

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

	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if(warehouse != null && _clan.getHasCastle() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
				if(!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if(_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						String text = "Manor System: all data for " + castle.getName() + " saved";
						Log.add(text, "Manor_system");
					}
				}

				_runCount++;
				CastleUpdater cu = new CastleUpdater(_clan, _runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
				warehouse = null;
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
