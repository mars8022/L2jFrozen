package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseUtils
{

	public static void close(Connection conn)
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

	public static void close(PreparedStatement stmt)
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

	public static void close(ResultSet rs)
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
		close(rs);
		close(stmt);
		close(conn);
	}

	public static void closeDatabaseCS(Connection conn, PreparedStatement stmt)
	{
		close(stmt);
		close(conn);
	}

	public static void closeDatabaseSR(PreparedStatement stmt, ResultSet rs)
	{
		close(rs);
		close(stmt);
	}
}