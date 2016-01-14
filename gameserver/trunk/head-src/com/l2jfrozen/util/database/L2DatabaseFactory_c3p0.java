package com.l2jfrozen.util.database;
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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class L2DatabaseFactory_c3p0 extends L2DatabaseFactory
{
	static Logger _log = Logger.getLogger(L2DatabaseFactory_c3p0.class.getName());
	
	private ComboPooledDataSource _source;
	
	// =========================================================
	// Constructor
	public L2DatabaseFactory_c3p0() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.warning("A minimum of " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}
			
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);
			
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
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
			_source.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour
			
			// enables statement caching,  there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);
			
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
			
			if (Config.DEBUG)
				_log.fine("Database Connection Working");
			
			if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
				_providerType = ProviderType.MsSql;
			else
				_providerType = ProviderType.MySql;
		}
		catch (SQLException x)
		{
			if (Config.DEBUG)
				_log.fine("Database Connection FAILED");
			// re-throw the exception
			throw x;
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
				_log.fine("Database Connection FAILED");
			throw new SQLException("Could not init DB connection:" + e.getMessage());
		}
	}
	
	@Override
	public void shutdown()
	{
		try{
			//sleep 10 seconds before the final source shutdown
			Thread.sleep(10000);
		}catch(Exception e){
			//nothing
		}
		
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.log(Level.INFO, "", e);
		}
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.log(Level.INFO, "", e);
		}
	}
	
	@Override
	public Connection getConnection(boolean checkclose) 
	{ 
		Connection con = null;

		while(con == null && _source!=null)
		{
			try
			{
				con = _source.getConnection();
				if(checkclose)
					ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.DATABASE_CONNECTION_TIMEOUT);
			}
			catch(SQLException e)
			{
				_log.severe("L2DatabaseFactory: getConnection() failed, trying again" + e);
			}
		}

		return con;
	}
	
	@Override
	public Connection getConnection(long max_connection_time) 
	{ 
		Connection con = null;

		while(con == null && _source!=null)
		{
			try
			{
				con = _source.getConnection();
				ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), max_connection_time);
				
			}
			catch(SQLException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.severe("L2DatabaseFactory: getConnection() failed, trying again \n" + e);
			}
		}

		return con;
	}
	
	@Override
	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}
	
	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}

}
