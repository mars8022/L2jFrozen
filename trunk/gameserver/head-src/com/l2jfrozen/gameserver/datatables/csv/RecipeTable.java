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
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.RecipeController;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2RecipeInstance;

/**
 * @author programmos
 */
public class RecipeTable extends RecipeController
{
	private static final Logger _log = Logger.getLogger(RecipeTable.class.getName());
	private Map<Integer, L2RecipeList> _lists;

	private static RecipeTable instance;

	public static RecipeTable getInstance()
	{
		if(instance == null)
		{
			instance = new RecipeTable();
		}

		return instance;
	}

	private RecipeTable()
	{
		_lists = new FastMap<Integer, L2RecipeList>();
		String line = null;
		
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			File recipesData = new File(Config.DATAPACK_ROOT, "data/recipes.csv");
			
			reader = new FileReader(recipesData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);

			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}

				parseList(line);

			}
			_log.config("RecipeController: Loaded " + _lists.size() + " Recipes.");
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			if(lnr != null)
			{
				_log.log(Level.WARNING, "error while creating recipe controller in linenr: " + lnr.getLineNumber(), e);
			}
			else
			{
				_log.warning("No recipes were found in data folder");
			}

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

	//TODO XMLize the recipe list
	private void parseList(String line)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			List<L2RecipeInstance> recipePartList = new FastList<L2RecipeInstance>();

			//we use common/dwarf for easy reading of the recipes.csv file
			String recipeTypeString = st.nextToken();

			// now parse the string into a boolean
			boolean isDwarvenRecipe;

			if(recipeTypeString.equalsIgnoreCase("dwarven"))
			{
				isDwarvenRecipe = true;
			}
			else if(recipeTypeString.equalsIgnoreCase("common"))
			{
				isDwarvenRecipe = false;
			}
			else
			{ //prints a helpfull message
				_log.warning("Error parsing recipes.csv, unknown recipe type " + recipeTypeString);
				return;
			}

			recipeTypeString = null;

			String recipeName = st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			int recipeId = Integer.parseInt(st.nextToken());
			int level = Integer.parseInt(st.nextToken());

			//material
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
			while(st2.hasMoreTokens())
			{
				StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
				int rpItemId = Integer.parseInt(st3.nextToken());
				int quantity = Integer.parseInt(st3.nextToken());
				L2RecipeInstance rp = new L2RecipeInstance(rpItemId, quantity);
				recipePartList.add(rp);
				rp = null;
				st3 = null;
			}
			st2 = null;

			int itemId = Integer.parseInt(st.nextToken());
			int count = Integer.parseInt(st.nextToken());

			//npc fee
			/*String notdoneyet = */st.nextToken();

			int mpCost = Integer.parseInt(st.nextToken());
			int successRate = Integer.parseInt(st.nextToken());

			L2RecipeList recipeList = new L2RecipeList(id, level, recipeId, recipeName, successRate, mpCost, itemId, count, isDwarvenRecipe);

			for(L2RecipeInstance recipePart : recipePartList)
			{
				recipeList.addRecipe(recipePart);
			}
			_lists.put(new Integer(_lists.size()), recipeList);

			recipeList = null;
			recipeName = null;
			st = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.severe("Exception in RecipeController.parseList() - " + e);
		}
	}

	public int getRecipesCount()
	{
		return _lists.size();
	}

	public L2RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}

	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for(int i = 0; i < _lists.size(); i++)
		{
			L2RecipeList find = _lists.get(new Integer(i));
			if(find.getRecipeId() == itemId)
				return find;
		}
		return null;
	}

	public L2RecipeList getRecipeById(int recId)
	{
		for(int i = 0; i < _lists.size(); i++)
		{
			L2RecipeList find = _lists.get(new Integer(i));
			if(find.getId() == recId)
				return find;
		}
		return null;
	}
}
