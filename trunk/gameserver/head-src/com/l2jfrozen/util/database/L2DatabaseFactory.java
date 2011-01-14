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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class L2DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
	
	public static enum ProviderType
	{
		MySql,
		MsSql
	}

	// =========================================================
	// Data Field
	private static L2DatabaseFactory _instance;
	private ProviderType _providerType;
	private ComboPooledDataSource _source;

	// =========================================================
	// Constructor
	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if(Config.DATABASE_MAX_CONNECTIONS < 10)
			{
				Config.DATABASE_MAX_CONNECTIONS = 10;
				_log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}
			_source = new ComboPooledDataSource();
			_source.setDebugUnreturnedConnectionStackTraces(Config.DEBUG); //debug func
			_source.setAutoCommitOnClose(true);

			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);

			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 miliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(Config.DATABASE_TIMEOUT); // 0 = wait indefinitely for new connection
			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.

			// this "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);

			// testing OnCheckin used with IdleConnectionTestPeriod is faster than  testing on checkout

			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(0); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour

			// enables statement caching,  there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(Config.DATABASE_STATEMENT);

			_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass(Config.DATABASE_DRIVER);
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);

			/* Test the connection */
			_source.getConnection().close();

			if(Config.DEBUG)
			{
				_log.fine("Database Connection Working");
			}

			if(Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
			{
				_providerType = ProviderType.MsSql;
			}
			else
			{
				_providerType = ProviderType.MySql;
			}
		}
		catch(SQLException x)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				x.printStackTrace();
			
			if(Config.DEBUG)
			{
				_log.fine("Database Connection FAILED");
			}
			// rethrow the exception
			throw x;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			if(Config.DEBUG)
			{
				_log.fine("Database Connection FAILED");
			}
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	// =========================================================
	// Method - Public
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if(returnOnlyTopRecord)
		{
			if(getProviderType() == ProviderType.MsSql)
			{
				msSqlTop1 = " Top 1 ";
			}

			if(getProviderType() == ProviderType.MySql)
			{
				mySqlTop1 = " Limit 1 ";
			}
		}
		String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
		return query;
	}

	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.INFO, "", e);
		}

		try
		{
			_source = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.INFO, "", e);
		}
	}

	public final String safetyString(String[] whatToCheck)
	{
		// NOTE: Use brace as a safty percaution just incase name is a reserved word
		String braceLeft = "`";
		String braceRight = "`";

		if(getProviderType() == ProviderType.MsSql)
		{
			braceLeft = "[";
			braceRight = "]";
		}

		String result = "";

		for(String word : whatToCheck)
		{
			if(result != "")
			{
				result += ", ";
			}
			result += braceLeft + word + braceRight;
		}
		return result;
	}

	// =========================================================
	// Property - Public
	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if(_instance == null)
		{
			_instance = new L2DatabaseFactory();
		}
		return _instance;
	}

	public Connection getConnection() throws SQLException 
	{ 
		Connection con = null;

		while(con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch(SQLException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.warning("L2DatabaseFactory: getConnection() failed, trying again \n" + e);
			}
		}

		return con;
	}

	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}

	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
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
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "Failed to close database connection!", e);
		}
	}
}
