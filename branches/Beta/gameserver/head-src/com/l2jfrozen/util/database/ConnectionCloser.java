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


/**
 * 
 * 
 * @author Enzo
 */
public class ConnectionCloser implements Runnable
{
	private static final Logger _log = Logger.getLogger(ConnectionCloser.class.getName());
	
	
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
				_log.severe( "Unclosed connection! Trace: " + exp.getStackTrace()[1]+" "+ exp);
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
