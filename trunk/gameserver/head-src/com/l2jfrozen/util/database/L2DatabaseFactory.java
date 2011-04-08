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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2DatabaseFactory
{
	private static final Logger _log = LoggerFactory.getLogger(L2DatabaseFactory.class);
	
	public static enum ProviderType
	{
		MySql,
		MsSql
	}

	// =========================================================
	// Data Field
	private static L2DatabaseFactory _instance;
	private final ProviderType _providerType;
	private final BoneCP _source;

	// =========================================================
	// Constructor
	public L2DatabaseFactory() throws SQLException
	{
		if (Config.DATABASE_PARTITION_COUNT > 5){
			Config.DATABASE_PARTITION_COUNT = 5;
			_log.warn("max {} db connections partitions.", Config.DATABASE_PARTITION_COUNT);
		}
		
		if (Config.DATABASE_MAX_CONNECTIONS < 5) {
			Config.DATABASE_MAX_CONNECTIONS = 5;
			_log.warn("at least {} db connections are required.", Config.DATABASE_MAX_CONNECTIONS);
			
		}else if (Config.DATABASE_MAX_CONNECTIONS * Config.DATABASE_PARTITION_COUNT > 60){
			_log.warn("Max Connections number is higher then 60.. Using Partition 5 and Connection 12");
			Config.DATABASE_MAX_CONNECTIONS = 12;
			Config.DATABASE_PARTITION_COUNT = 5;
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
		config.setIdleMaxAge(60);
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
		_log.debug("Connection successful");
		final Statement statement = connect.createStatement();
		final ResultSet resultSet = statement.executeQuery("SHOW TABLES");
		resultSet.next();
		resultSet.close();
		statement.close();
		connect.close();
		_log.debug("Database Connection Working");
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
		_source.close();
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
				ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.DATABASE_CONNECTION_TIMEOUT);
			}
			catch(SQLException e)
			{
				_log.error("L2DatabaseFactory: getConnection() failed, trying again", e);
			}
		}

		return con;
	}
	
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
				_log.error("L2DatabaseFactory: getConnection() failed, trying again", e);
			}
		}

		return con;
	}
	
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
				
				_log.error("L2DatabaseFactory: getConnection() failed, trying again \n" + e);
			}
		}

		return con;
	}
	
	public int getBusyConnectionCount() {
		return _source.getTotalLeased();
	}

	public int getIdleConnectionCount() {
		return _source.getTotalFree();
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
			_log.error("Failed to close database connection!", e);
		}
	}

	private class ConnectionCloser implements Runnable
	{
		private final Connection c ;
		private final RuntimeException exp;
		
		public ConnectionCloser(Connection con, RuntimeException e)
		{
			c = con;
			exp = e;
		}
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				if (c!=null && !c.isClosed())
				{
					_log.error( "Unclosed connection! Trace: " + exp.getStackTrace()[1], exp);
					//c.close();
					
				}
			}
			catch (SQLException e)
			{
				//the close operation could generate exception, but there is not any problem
				//e.printStackTrace();
			}
			
		}
	}
}
