/*
 * This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private final static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());

	private static TeleportLocationTable _instance;

	private Map<Integer, L2TeleportLocation> _teleports;

	public static TeleportLocationTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new TeleportLocationTable();
		}

		return _instance;
	}

	private TeleportLocationTable()
	{
		reloadAll();
	}

	public void reloadAll()
	{
		_teleports = new FastMap<Integer, L2TeleportLocation>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			final ResultSet rset = statement.executeQuery();
			L2TeleportLocation teleport;

			while(rset.next())
			{
				teleport = new L2TeleportLocation();

				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble") == 1);

				_teleports.put(teleport.getTeleId(), teleport);
			}

			statement.close();
			rset.close();

			_log.finest("TeleportLocationTable: Loaded {} Teleport Location Templates. "+ _teleports.size());
		}
		catch(Exception e)
		{
			_log.severe("error while creating teleport table "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		if(Config.CUSTOM_TELEPORT_TABLE)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM custom_teleport");
				ResultSet rset = statement.executeQuery();
				L2TeleportLocation teleport;

				int _cTeleCount = _teleports.size();

				while(rset.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rset.getInt("id"));
					teleport.setLocX(rset.getInt("loc_x"));
					teleport.setLocY(rset.getInt("loc_y"));
					teleport.setLocZ(rset.getInt("loc_z"));
					teleport.setPrice(rset.getInt("price"));
					teleport.setIsForNoble(rset.getInt("fornoble") == 1);
					_teleports.put(teleport.getTeleId(), teleport);
				}

				statement.close();
				rset.close();

				_cTeleCount = _teleports.size() - _cTeleCount;

				if(_cTeleCount > 0)
				{
					_log.finest("TeleportLocationTable: Loaded {} Custom Teleport Location Templates. "+ _cTeleCount);
				}

			}
			catch(Exception e)
			{
				_log.severe("error while creating custom teleport table "+ e);
			}
			finally
			{
				CloseUtil.close(con);
			}
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}
