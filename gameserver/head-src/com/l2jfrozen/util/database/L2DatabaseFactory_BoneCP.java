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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2DatabaseFactory_BoneCP extends L2DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2DatabaseFactory_BoneCP.class.getName());
	
	private BoneCP _source;

	// =========================================================
	// Constructor
	public L2DatabaseFactory_BoneCP() throws SQLException
	{
		if (Config.DATABASE_PARTITION_COUNT > 4){
			Config.DATABASE_PARTITION_COUNT = 4;
			_log.warning("max {} db connections partitions. "+ Config.DATABASE_PARTITION_COUNT);
		}
		
		if (Config.DATABASE_MAX_CONNECTIONS < 10) {
			Config.DATABASE_MAX_CONNECTIONS = 10;
			_log.warning("at least {} db connections are required. "+ Config.DATABASE_MAX_CONNECTIONS);
			
		}else if (Config.DATABASE_MAX_CONNECTIONS * Config.DATABASE_PARTITION_COUNT > 60){
			_log.warning("Max Connections number is higher then 60.. Using Partition 2 and Connection 30");
			Config.DATABASE_MAX_CONNECTIONS = 30;
			Config.DATABASE_PARTITION_COUNT = 2;
		}
		
		
		try {
			Class.forName(Config.DATABASE_DRIVER);
		} catch(Throwable e) {
			throw new SQLException("Database driver not found!");
		}
		
		//hard configure, maybe used config.setConfigFile(String)?
		final BoneCPConfig config = new BoneCPConfig();
		config.setJdbcUrl(Config.DATABASE_URL);
		config.setUsername(Config.DATABASE_LOGIN);
		config.setPassword(Config.DATABASE_PASSWORD);
		
		config.setMinConnectionsPerPartition(5);
		config.setMaxConnectionsPerPartition(Config.DATABASE_MAX_CONNECTIONS);
		config.setAcquireIncrement(5);
		config.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
		config.setAcquireRetryDelay(500); // 500 miliseconds wait before try to acquire connection again
		config.setIdleConnectionTestPeriod(0);
		config.setIdleMaxAge(43200); //12 hours
		config.setStatementReleaseHelperThreads(4);
		config.setStatementsCacheSize(0); //min value
		config.setPartitionCount(Config.DATABASE_PARTITION_COUNT);
		config.setDisableConnectionTracking(true);
		// if pool is exhausted, get 5 more connections at a time cause there is a "long" delay on acquire connection
		// so taking more than one connection at once will make connection pooling more effective.
		config.setConnectionTimeout(Config.DATABASE_TIMEOUT);
		
		
		try {
			_source = new BoneCP(config);
			testConnection();
		} catch (SQLException x) {
			throw x;
		} catch (Exception e) {
			throw new SQLException("could not init DB connection", e);
		}
		
		if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
			_providerType = ProviderType.MsSql;
		else
			_providerType = ProviderType.MySql;
	}
	
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
	public Connection getConnection(boolean checkclose) throws SQLException 
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
	public Connection getConnection(long max_connection_time) throws SQLException 
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
	public int getBusyConnectionCount() throws SQLException {
		return _source.getTotalLeased();
	}

	@Override
	public int getIdleConnectionCount() throws SQLException {
		return _source.getTotalFree();
	}
}
