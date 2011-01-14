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

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2ExtractableItem;
import com.l2jfrozen.gameserver.model.L2ExtractableProductItem;

public class ExtractableItemsData
{
	//Map<itemid, L2ExtractableItem>
	private FastMap<Integer, L2ExtractableItem> _items;

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
		_items = new FastMap<Integer, L2ExtractableItem>();

		Scanner s;

		try
		{
			s = new Scanner(new File("./data/extractable_items.csv"));
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("Extractable items data: Can not find './data/extractable_items.csv'");
			return;
		}

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

			boolean ok = true;
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
				ok = false;
			}

			if(!ok)
			{
				continue;
			}

			FastList<L2ExtractableProductItem> product_temp = new FastList<L2ExtractableProductItem>();

			for(int i = 0; i < lineSplit.length - 1; i++)
			{
				ok = true;

				String[] lineSplit2 = lineSplit[i + 1].split(",");

				if(lineSplit2.length != 3)
				{
					System.out.println("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
					System.out.println("		" + line);
					ok = false;
				}

				if(!ok)
				{
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
					ok = false;
				}

				if(!ok)
				{
					continue;
				}

				L2ExtractableProductItem product = new L2ExtractableProductItem(production, amount, chance);
				product_temp.add(product);
				product = null;
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
			L2ExtractableItem product = new L2ExtractableItem(itemID, product_temp);
			_items.put(itemID, product);

			line = null;
			lineSplit = null;
			product_temp = null;
			product = null;
		}

		s.close();
		s = null;
		System.out.println("Extractable items data: Loaded " + _items.size() + " extractable items!");
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
