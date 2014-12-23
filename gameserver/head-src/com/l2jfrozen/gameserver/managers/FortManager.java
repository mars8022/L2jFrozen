/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos, scoria dev
 */

public class FortManager
{
	protected static final Logger LOGGER = Logger.getLogger(FortManager.class);
	
	public static final FortManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	// =========================================================
	// Data Field
	private final List<Fort> _forts = new FastList<>();
	
	// =========================================================
	// Constructor
	public FortManager()
	{
		LOGGER.info("Initializing FortManager");
		_forts.clear();
		load();
	}
	
	// =========================================================
	// Method - Public
	
	public final int findNearestFortIndex(final L2Object obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Fort fort;
			for (int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = i;
				}
			}
			fort = null;
		}
		return index;
	}
	
	// =========================================================
	// Method - Private
	private final void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			statement = con.prepareStatement("Select id from fort order by id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			
			LOGGER.info("Loaded: " + getForts().size() + " fortress");
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: loadFortData(): " + e.getMessage());
			e.printStackTrace();
		}
		
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// =========================================================
	// Property - Public
	public final Fort getFortById(final int fortId)
	{
		for (final Fort f : getForts())
		{
			if (f.getFortId() == fortId)
				return f;
		}
		return null;
	}
	
	public final Fort getFortByOwner(final L2Clan clan)
	{
		for (final Fort f : getForts())
		{
			if (f.getOwnerId() == clan.getClanId())
				return f;
		}
		return null;
	}
	
	public final Fort getFort(final String name)
	{
		for (final Fort f : getForts())
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
				return f;
		}
		return null;
	}
	
	public final Fort getFort(final int x, final int y, final int z)
	{
		for (final Fort f : getForts())
		{
			if (f.checkIfInZone(x, y, z))
				return f;
		}
		return null;
	}
	
	public final Fort getFort(final L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(final int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.getFortId() == fortId)
			{
				fort = null;
				return i;
			}
		}
		return -1;
	}
	
	public final int getFortIndex(final L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(final int x, final int y, final int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
			{
				fort = null;
				return i;
			}
		}
		return -1;
	}
	
	public final List<Fort> getForts()
	{
		return _forts;
	}
	
	private static class SingletonHolder
	{
		protected static final FortManager _instance = new FortManager();
	}
}
