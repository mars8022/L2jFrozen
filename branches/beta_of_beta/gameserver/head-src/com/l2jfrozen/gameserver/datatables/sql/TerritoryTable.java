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
/*
	coded by Balancer
	ported to L2JRU by Mr
	balancer@balancer.ru
	http://balancer.ru

	version 0.1.1, 2005-06-07
	version 0.1, 2005-03-16
*/

package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.model.L2Territory;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class TerritoryTable
{
	private final static Logger _log = Logger.getLogger(TradeController.class.getName());
	private static Map<Integer, L2Territory> _territory = new HashMap<Integer, L2Territory>();

	public static TerritoryTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public TerritoryTable()
	{
		_territory.clear();
		// load all data at server start
		reload_data();
	}

	public int[] getRandomPoint(Integer terr)
	{
		return _territory.get(terr).getRandomPoint();
	}

	public int getProcMax(Integer terr)
	{
		return _territory.get(terr).getProcMax();
	}

	public void reload_data()
	{
		_territory.clear();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT loc_id, loc_x, loc_y, loc_zmin, loc_zmax, proc FROM `locations`");
			final ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				//final String terr = "sql_terr_" + rset.getString("loc_id");
				final int terr = rset.getInt("loc_id");
				
				if(_territory.get(terr) == null)
				{
					L2Territory t = new L2Territory();
					_territory.put(terr, t);
				}
				_territory.get(terr).add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));
			}
			
			rset.close();
			statement.close();
		}
		catch(Exception e1)
		{
			_log.severe("locations couldnt be initialized "+ e1);
		}
		finally
		{
			CloseUtil.close(con);
		}

		_log.finest("TerritoryTable: Loaded {} locations "+ _territory.size());
	}
	
	private static class SingletonHolder
	{
		protected static final TerritoryTable _instance = new TerritoryTable();
	}
}
