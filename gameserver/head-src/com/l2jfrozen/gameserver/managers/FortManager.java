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
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos, scoria dev
 */

public class FortManager
{
	protected static final Logger _log = Logger.getLogger(FortManager.class.getName());
	
	public static final FortManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================
	// Data Field
	private List<Fort> _forts = new FastList<Fort>();

	// =========================================================
	// Constructor
	public FortManager()
	{
		_log.info("Initializing FortManager");
		_forts.clear();
		load();
	}

	// =========================================================
	// Method - Public

	public final int findNearestFortIndex(L2Object obj)
	{
		int index = getFortIndex(obj);
		if(index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Fort fort;
			for(int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				if(fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if(closestDistance > distance)
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

			while(rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}

			rs.close();
			statement.close();

			_log.info("Loaded: " + getForts().size() + " fortress");
		}
		catch(Exception e)
		{
			_log.warning("Exception: loadFortData(): " + e.getMessage());
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
	public final Fort getFortById(int fortId)
	{
		for(Fort f : getForts())
		{
			if(f.getFortId() == fortId)
				return f;
		}
		return null;
	}

	public final Fort getFortByOwner(L2Clan clan)
	{
		for(Fort f : getForts())
		{
			if(f.getOwnerId() == clan.getClanId())
				return f;
		}
		return null;
	}

	public final Fort getFort(String name)
	{
		for(Fort f : getForts())
		{
			if(f.getName().equalsIgnoreCase(name.trim()))
				return f;
		}
		return null;
	}

	public final Fort getFort(int x, int y, int z)
	{
		for(Fort f : getForts())
		{
			if(f.checkIfInZone(x, y, z))
				return f;
		}
		return null;
	}

	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for(int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if(fort != null && fort.getFortId() == fortId)
			{
				fort = null;
				return i;
			}
		}
		return -1;
	}

	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for(int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if(fort != null && fort.checkIfInZone(x, y, z))
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
