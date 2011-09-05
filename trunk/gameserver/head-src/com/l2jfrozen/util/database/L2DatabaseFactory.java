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
package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jfrozen.Config;

public abstract class L2DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
	
	protected enum ProviderType
	{
		MySql,
		MsSql
	}

	// =========================================================
	// Data Field
	protected static L2DatabaseFactory _instance;
	protected ProviderType _providerType;
	
	// =========================================================
	// Property - Public
	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if(_instance == null)
		{
			if(Config.DATABASE_POOL_TYPE.equals("BoneCP")){
				_instance = new L2DatabaseFactory_BoneCP();
			}else{
				_instance = new L2DatabaseFactory_c3p0();
			}
			
		}
		return _instance;
	}

	
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord)
		{
			if (getProviderType() == ProviderType.MsSql)
				msSqlTop1 = " Top 1 ";
			if (getProviderType() == ProviderType.MySql)
				mySqlTop1 = " Limit 1 ";
		}
		String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
		return query;
	}
	
	public final String safetyString(String... whatToCheck)
	{
		// NOTE: Use brace as a safty precaution just incase name is a reserved word
		final char braceLeft;
		final char braceRight;
		
		if (getProviderType() == ProviderType.MsSql)
		{
			braceLeft = '[';
			braceRight = ']';
		}
		else
		{
			braceLeft = '`';
			braceRight = '`';
		}
		
		int length = 0;
		
		for (String word : whatToCheck)
		{
			length += word.length() + 4;
		}
		
		final StringBuilder sbResult = new StringBuilder(length);
		
		for (String word : whatToCheck)
		{
			if (sbResult.length() > 0)
			{
				sbResult.append(", ");
			}
			
			sbResult.append(braceLeft);
			sbResult.append(word);
			sbResult.append(braceRight);
		}
		
		return sbResult.toString();
	}
	
	public Connection getConnection() throws SQLException 
	{ 
		return getConnection(true);
	}
	
	public final ProviderType getProviderType()
	{
		return _providerType;
	}
	
	public static void close(Connection con)
	{
		if (con == null)
			return;
		
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			_log.severe("Failed to close database connection! "+ e);
		}
	}
	
	public abstract void shutdown();
	
	
	public abstract Connection getConnection(boolean checkclose) throws SQLException;
	
	public abstract Connection getConnection(long max_connection_time) throws SQLException;
	
	public abstract int getBusyConnectionCount() throws SQLException;
	
}
