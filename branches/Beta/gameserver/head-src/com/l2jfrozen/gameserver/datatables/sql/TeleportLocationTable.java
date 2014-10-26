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

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private final static Logger LOGGER = Logger.getLogger(TeleportLocationTable.class);
	
	private static TeleportLocationTable _instance;
	
	private Map<Integer, L2TeleportLocation> teleports;
	
	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
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
		teleports = new FastMap<>();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			final ResultSet rset = statement.executeQuery();
			L2TeleportLocation teleport;
			
			while (rset.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble") == 1);
				
				teleports.put(teleport.getTeleId(), teleport);
			}
			
			DatabaseUtils.close(statement);
			DatabaseUtils.close(rset);
			
			LOGGER.info("TeleportLocationTable: Loaded " + teleports.size() + " Teleport Location Templates");
		}
		catch (final Exception e)
		{
			LOGGER.error("Error while creating teleport table ", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM custom_teleport");
				final ResultSet rset = statement.executeQuery();
				L2TeleportLocation teleport;
				
				int _cTeleCount = teleports.size();
				
				while (rset.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rset.getInt("id"));
					teleport.setLocX(rset.getInt("loc_x"));
					teleport.setLocY(rset.getInt("loc_y"));
					teleport.setLocZ(rset.getInt("loc_z"));
					teleport.setPrice(rset.getInt("price"));
					teleport.setIsForNoble(rset.getInt("fornoble") == 1);
					teleports.put(teleport.getTeleId(), teleport);
				}
				
				DatabaseUtils.close(statement);
				DatabaseUtils.close(rset);
				
				_cTeleCount = teleports.size() - _cTeleCount;
				
				if (_cTeleCount > 0)
				{
					LOGGER.info("TeleportLocationTable: Loaded {} Custom Teleport Location Templates. " + _cTeleCount);
				}
				
			}
			catch (final Exception e)
			{
				LOGGER.error("Error while creating custom teleport table ", e);
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
	public L2TeleportLocation getTemplate(final int id)
	{
		return teleports.get(id);
	}
}
