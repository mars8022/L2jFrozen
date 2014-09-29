package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseUtils
{

	public static void closeConnection(Connection conn)
	{
		if(conn != null)
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
			}
	}

	public static void closeStatement(PreparedStatement stmt)
	{
		if(stmt != null)
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
			}
	}

	public static void closeResultSet(ResultSet rs)
	{
		if(rs != null)
			try
			{
				rs.close();
			}
			catch(SQLException e)
			{}
	}

	public static void closeDatabaseCSR(Connection conn, PreparedStatement stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(conn);
	}

	public static void closeDatabaseCS(Connection conn, PreparedStatement stmt)
	{
		closeStatement(stmt);
		closeConnection(conn);
	}

	public static void closeDatabaseSR(PreparedStatement stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
	}
}