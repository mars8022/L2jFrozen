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

import com.l2jfrozen.Config;
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
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			File fileData = new File("./data/sts/ExtTable.sts");
			
			reader = new FileReader(fileData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);

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
			e.printStackTrace();
			_log.warning("ExpTable is missing in sts folder");

		}
		catch(IOException e0)
		{
			e0.printStackTrace();
			_log.warning("Error while creating exp table: " + e0.getMessage());
		}
		finally
		{
			if(lnr != null)
				try
				{
					lnr.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(buff != null)
				try
				{
					buff.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(reader != null)
				try
				{
					reader.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
		}
	}
}
