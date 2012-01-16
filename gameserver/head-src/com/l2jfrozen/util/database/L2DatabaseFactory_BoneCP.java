package com.l2jfrozen.util.database;
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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.jolbox.bonecp.BoneCPDataSource;
import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2DatabaseFactory_BoneCP extends L2DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2DatabaseFactory_BoneCP.class.getName());
	
	private BoneCPDataSource		_source;
	
	public L2DatabaseFactory_BoneCP()
	{
		_log.info("Initializing BoneCP [ databaseDriver -> " + Config.DATABASE_DRIVER + ", jdbcUrl -> "
				+ Config.DATABASE_URL + ", maxConnectionsPerPartition -> " + Config.DATABASE_MAX_CONNECTIONS + ", username -> " + Config.DATABASE_LOGIN
				+ ", password -> " + Config.DATABASE_PASSWORD + " ]");

		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 10)
			{
				Config.DATABASE_MAX_CONNECTIONS = 10;
				_log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}

			if (Config.DATABASE_PARTITION_COUNT > 4){
				Config.DATABASE_PARTITION_COUNT = 4;
				_log.warning("max {} db connections partitions. "+ Config.DATABASE_PARTITION_COUNT);
			}
			
			if (Config.DATABASE_MAX_CONNECTIONS * Config.DATABASE_PARTITION_COUNT > 200){
				_log.warning("Max Connections number is higher then 60.. Using Partition 2 and Connection 30");
				Config.DATABASE_MAX_CONNECTIONS = 50;
				Config.DATABASE_PARTITION_COUNT = 4;
			}
			
			_source = new BoneCPDataSource();
			// _source.setAutoCommitOnClose(true);
			_source.getConfig().setDefaultAutoCommit(true);

			// _source.setInitialPoolSize(10);
			_source.getConfig().setPoolAvailabilityThreshold(10);
			//_source.setMinPoolSize(10);
			_source.getConfig().setMinConnectionsPerPartition(10);
			//_source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);
			_source.getConfig().setMaxConnectionsPerPartition(Config.DATABASE_MAX_CONNECTIONS);

			_source.getConfig().setPartitionCount(Config.DATABASE_PARTITION_COUNT);

			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelayInMs(500); // 500 miliseconds wait before try to acquire connection again

			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.

			_source.setConnectionTimeoutInMs(Config.DATABASE_TIMEOUT);

			// this "connection_test_table" is automatically created if not already there
			//_source.setAutomaticTestTable("connection_test_table");
			//_source.setTestConnectionOnCheckin(false);

			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout

			_source.setIdleConnectionTestPeriodInMinutes(1); // test idle connection every 60 sec
			// _source.setMaxIdleTime(1800); // 0 = idle connections never expire
			_source.setIdleMaxAgeInSeconds(1800);
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour

			_source.setTransactionRecoveryEnabled(true);

			// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			//_source.setMaxStatementsPerConnection(100);

			//_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass(Config.DATABASE_DRIVER);
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUsername(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);

			/* Test the connection */
			_source.getConnection().close();

			if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
				_providerType = ProviderType.MsSql;
			else
				_providerType = ProviderType.MySql;

		}
		catch (Exception e)
		{
			throw new Error("L2DatabaseFactory: Failed to init database connections: " + e, e);
		}
	}
	
	/*
	private void testConnection() throws SQLException {
		final Connection connect = _source.getConnection();
		if(connect == null) throw new SQLException();
		_log.finest("Connection successful");
		final Statement statement = connect.createStatement();
		final ResultSet resultSet = statement.executeQuery("SHOW TABLES");
		resultSet.next();
		resultSet.close();
		statement.close();
		connect.close();
		_log.finest("Database Connection Working");
	}
	*/

	
	@Override
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.severe("Error shutdowning database pool "+ e);
		}
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Connection getConnection(boolean checkclose) 
	{ 
		Connection con = null;

		while(con == null)
		{
			try
			{
				con = _source.getConnection();
				if(checkclose)
					ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.DATABASE_CONNECTION_TIMEOUT);
			}
			catch(SQLException e)
			{
				_log.severe("L2DatabaseFactory: getConnection() failed, trying again "+ e);
			}
		}

		return con;
	}
	
	@Override
	public Connection getConnection(long max_connection_time) 
	{ 
		Connection con = null;

		while(con == null)
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
	public int getBusyConnectionCount()
	{
		return _source.getTotalLeased();
	}

}
