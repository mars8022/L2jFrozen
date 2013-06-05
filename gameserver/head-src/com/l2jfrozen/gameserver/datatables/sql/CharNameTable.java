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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.1 $ $Date: 2005/03/27 15:29:18 $
 */
public class CharNameTable
{
	private final static Logger _log = Logger.getLogger(CharNameTable.class.getName());

	private static CharNameTable _instance;

	public static CharNameTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new CharNameTable();
		}
		return _instance;
	}

	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = true;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, name);
			final ResultSet rset = statement.executeQuery();
			result = rset.next();

			statement.close();
			rset.close();
		}
		catch(SQLException e)
		{
			_log.severe("could not check existing charname"+" "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		return result;
	}

	public int accountCharNumber(String account)
	{
		Connection con = null;
		int number = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			final ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				number = rset.getInt(1);
			}

			statement.close();
			rset.close();
		}
		catch(SQLException e)
		{
			_log.severe("could not check existing char number");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}

		return number;
	}
	
	public int ipCharNumber(String ip)
	{
		Connection con = null;
		int number = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT count(char_name) FROM "+Config.LOGINSERVER_DB+".accounts a, "+Config.GAMESERVER_DB+".characters c where a.login = c.account_name and a.lastIP=?");
			statement.setString(1, ip);
			final ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				number = rset.getInt(1);
			}

			statement.close();
			rset.close();
		}
		catch(SQLException e)
		{
			_log.severe("could not check existing char number");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}

		return number;
	}
}
