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
package com.l2jfrozen.gameserver.datatables.sts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.base.Experience;

/**
 * @author programmos
 */
public class ExpTable
{
	private static Logger _log = Logger.getLogger(ExpTable.class.getName());
	private static ExpTable instance;

	public static ExpTable getInstance()
	{
		if(instance == null)
		{
			instance = new ExpTable();
		}

		return instance;
	}

	private ExpTable()
	{
		load();
	}

	public void load()
	{
		LineNumberReader lnr = null;

		try
		{
			File fileData = new File("./data/sts/ExtTable.sts");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(fileData)));

			String line = null;

			int level = 0;

			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("//"))
				{
					continue;
				}

				StringTokenizer st = new StringTokenizer(line, ",");

				long exp = Long.parseLong(st.nextToken());

				Experience.setExp(level, exp);
				level++;
			}

		}
		catch(FileNotFoundException e)
		{
			_log.warning("ExpTable is missing in sts folder");
			System.exit(1);
		}
		catch(IOException e0)
		{
			_log.warning("Error while creating exp table: " + e0.getMessage());
			System.exit(1);
		}
		finally
		{
			try
			{
				lnr.close();
				lnr = null;
			}
			catch(Exception e1)
			{
				//ignore
			}
		}
	}
}
