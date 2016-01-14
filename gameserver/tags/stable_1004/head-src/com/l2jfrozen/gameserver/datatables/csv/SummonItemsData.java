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

/**
 *
 * @author FBIagent
 *
 */

package com.l2jfrozen.gameserver.datatables.csv;

import java.io.File;
import java.util.Scanner;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2SummonItem;

public class SummonItemsData
{
	private FastMap<Integer, L2SummonItem> _summonitems;

	private static SummonItemsData _instance;

	public static SummonItemsData getInstance()
	{
		if(_instance == null)
		{
			_instance = new SummonItemsData();
		}

		return _instance;
	}

	public SummonItemsData()
	{
		_summonitems = new FastMap<Integer, L2SummonItem>();

		Scanner s = null;

		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT+"/data/summon_items.csv"));
			
			int lineCount = 0;

			while(s.hasNextLine())
			{
				lineCount++;

				String line = s.nextLine();

				if(line.startsWith("#"))
				{
					continue;
				}
				else if(line.equals(""))
				{
					continue;
				}

				String[] lineSplit = line.split(";");
				line = null;

				boolean ok = true;
				int itemID = 0, npcID = 0;
				byte summonType = 0;

				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
					npcID = Integer.parseInt(lineSplit[1]);
					summonType = Byte.parseByte(lineSplit[2]);
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					System.out.println("Summon items data: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
					System.out.println("		" + line);
					ok = false;
				}

				if(!ok)
				{
					continue;
				}

				L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType);
				_summonitems.put(itemID, summonitem);
				summonitem = null;
			}

		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("Summon items data: Can not find './data/summon_items.csv'");
		}finally{
			
			if(s!=null)
				s.close();
		}

		System.out.println("Summon items data: Loaded " + _summonitems.size() + " summon items.");
	}

	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}

	public int[] itemIDs()
	{
		int size = _summonitems.size();
		int[] result = new int[size];
		int i = 0;

		for(L2SummonItem si : _summonitems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
}
