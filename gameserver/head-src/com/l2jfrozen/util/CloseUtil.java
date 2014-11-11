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
package com.l2jfrozen.util;

import java.io.Closeable;
import java.sql.Connection;

import org.apache.log4j.Logger;

/**
 * little 'hack' :)
 * @author ProGramMoS
 */
public final class CloseUtil
{
	private final static Logger LOGGER = Logger.getLogger(CloseUtil.class);
	
	public static void close(Connection con)
	{
		if (con != null)
			try
			{
				con.close();
				con = null;
			}
			catch (final Throwable e)
			{
				LOGGER.error(e);
			}
	}
	
	public static void close(final Closeable closeable)
	{
		if (closeable != null)
			try
			{
				closeable.close();
			}
			catch (final Throwable e)
			{
				LOGGER.error(e);
			}
	}
}
