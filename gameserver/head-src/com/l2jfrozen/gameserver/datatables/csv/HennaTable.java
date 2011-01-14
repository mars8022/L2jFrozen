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
package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class HennaTable
{
	private static Logger _log = Logger.getLogger(HennaTable.class.getName());

	private static HennaTable _instance;

	private Map<Integer, L2Henna> _henna;
	private boolean _initialized = true;

	public static HennaTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new HennaTable();
		}

		return _instance;
	}

	private HennaTable()
	{
		_henna = new FastMap<Integer, L2Henna>();
		restoreHennaData();
	}

	private void restoreHennaData()
	{
		LineNumberReader lnr = null;

		try
		{
			File fileData = new File("./data/csv/henna.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(fileData)));

			String line = null;

			while((line = lnr.readLine()) != null)
			{
				//ignore comments
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}

				StringTokenizer st = new StringTokenizer(line, ";");

				StatsSet hennaDat = new StatsSet();
				int id = Integer.parseInt(st.nextToken());
				hennaDat.set("symbol_id", id);
				st.nextToken(); //next token...ignore name
				hennaDat.set("dye", Integer.parseInt(st.nextToken()));
				hennaDat.set("amount", Integer.parseInt(st.nextToken()));
				hennaDat.set("price", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_INT", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_STR", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_CON", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_MEM", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_DEX", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_WIT", Integer.parseInt(st.nextToken()));

				L2Henna template = new L2Henna(hennaDat);
				_henna.put(id, template);
				hennaDat = null;
				template = null;
			}

			_log.config("HennaTable: Loaded " + _henna.size() + " Templates.");
		}
		catch(FileNotFoundException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("./data/csv/henna.csv is missing in data folder");
		}
		catch(IOException e0)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e0.printStackTrace();
			
			_log.warning("Error while creating table: " + e0.getMessage() + "\n" + e0);
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
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e1.printStackTrace();
				
			}
		}
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public L2Henna getTemplate(int id)
	{
		return _henna.get(id);
	}

}
