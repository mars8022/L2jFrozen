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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2ExtractableItem;
import com.l2jfrozen.gameserver.model.L2ExtractableProductItem;

/**
 * @author FBIagent
 */
public class ExtractableItemsData
{
	//Map<itemid, L2ExtractableItem>
	private Map<Integer, L2ExtractableItem> _items;

	private static ExtractableItemsData _instance = null;

	public static ExtractableItemsData getInstance()
	{
		if(_instance == null)
		{
			_instance = new ExtractableItemsData();
		}

		return _instance;
	}

	public ExtractableItemsData()
	{
		_items = new HashMap<Integer, L2ExtractableItem>();

		Scanner s = null;
		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT+"/data/extractable_items.csv"));
			
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
				int itemID = 0;
				try
				{
					itemID = Integer.parseInt(lineSplit[0]);
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					System.out.println("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
					System.out.println("		" + line);
					return;
				}
				
				List<L2ExtractableProductItem> product_temp = new ArrayList<L2ExtractableProductItem>(lineSplit.length);
				for(int i = 0; i < lineSplit.length - 1; i++)
				{
					String[] lineSplit2 = lineSplit[i + 1].split(",");
					if(lineSplit2.length != 3)
					{
						System.out.println("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
						System.out.println("		" + line);
						continue;
					}
					
					int production = 0, amount = 0, chance = 0;

					try
					{
						production = Integer.parseInt(lineSplit2[0]);
						amount = Integer.parseInt(lineSplit2[1]);
						chance = Integer.parseInt(lineSplit2[2]);
						lineSplit2 = null;
					}
					catch(Exception e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						System.out.println("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
						System.out.println("		" + line);
						continue;
					}
					
					product_temp.add(new L2ExtractableProductItem(production, amount, chance));
				}

				int fullChances = 0;
				for(L2ExtractableProductItem Pi : product_temp)
				{
					fullChances += Pi.getChance();
				}

				if(fullChances > 100)
				{
					System.out.println("Extractable items data: Error in line " + lineCount + " -> all chances together are more then 100!");
					System.out.println("		" + line);
					continue;
				}
				
				_items.put(itemID, new L2ExtractableItem(itemID, product_temp));
			}

			System.out.println("Extractable items data: Loaded " + _items.size() + " extractable items!");
		}
		catch(Exception e)
		{
			//if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("Extractable items data: Can not find './data/extractable_items.csv'");
			
		}finally{
			
			if(s != null)
				try
				{
					s.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
		}
	}

	public L2ExtractableItem getExtractableItem(int itemID)
	{
		return _items.get(itemID);
	}

	public int[] itemIDs()
	{
		int size = _items.size();
		int[] result = new int[size];
		int i = 0;
		for(L2ExtractableItem ei : _items.values())
		{
			result[i] = ei.getItemId();
			i++;
		}
		return result;
	}
}
