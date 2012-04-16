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
package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2RecipeInstance;

/**
 * This class describes a Recipe used by Dwarf to craft Item. All L2RecipeList are made of L2RecipeInstance (1 line of
 * the recipe : Item-Quantity needed).<BR>
 * <BR>
 */
public class L2RecipeList
{
	/** The table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList */
	private L2RecipeInstance[] _recipes;

	/** The Identifier of the Instance */
	private int _id;

	/** The crafting level needed to use this L2RecipeList */
	private int _level;

	/** The Identifier of the L2RecipeList */
	private int _recipeId;

	/** The name of the L2RecipeList */
	private String _recipeName;

	/** The crafting succes rate when using the L2RecipeList */
	private int _successRate;

	/** The crafting MP cost of this L2RecipeList */
	private int _mpCost;

	/** The Identifier of the Item crafted with this L2RecipeList */
	private int _itemId;

	/** The quantity of Item crafted when using this L2RecipeList */
	private int _count;

	/** If this a common or a dwarven recipe */
	private boolean _isDwarvenRecipe;

	/**
	 * Constructor of L2RecipeList (create a new Recipe).
	 * @param id 
	 * @param level 
	 * @param recipeId 
	 * @param recipeName 
	 * @param successRate 
	 * @param mpCost 
	 * @param itemId 
	 * @param count 
	 * @param isDwarvenRecipe 
	 */
	public L2RecipeList(int id, int level, int recipeId, String recipeName, int successRate, int mpCost, int itemId, int count, boolean isDwarvenRecipe)
	{
		_id = id;
		_recipes = new L2RecipeInstance[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_isDwarvenRecipe = isDwarvenRecipe;
	}

	/**
	 * Add a L2RecipeInstance to the L2RecipeList (add a line Item-Quantity needed to the Recipe).
	 * @param recipe 
	 */
	public void addRecipe(L2RecipeInstance recipe)
	{
		int len = _recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
		tmp = null;
	}

	/**
	 * @return the Identifier of the Instance.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return the crafting level needed to use this L2RecipeList.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return the Identifier of the L2RecipeList.
	 */
	public int getRecipeId()
	{
		return _recipeId;
	}

	/**
	 * @return the name of the L2RecipeList.
	 */
	public String getRecipeName()
	{
		return _recipeName;
	}

	/**
	 * @return the crafting success rate when using the L2RecipeList.
	 */
	public int getSuccessRate()
	{
		return _successRate;
	}

	/**
	 * @return the crafting MP cost of this L2RecipeList.
	 */
	public int getMpCost()
	{
		return _mpCost;
	}

	/**
	 * @return true if the Item crafted with this L2RecipeList is consumable (shot, arrow,...).
	 */
	public boolean isConsumable()
	{
		return _itemId >= 1463 && _itemId <= 1467 || _itemId >= 2509 && _itemId <= 2514 || _itemId >= 3947 && _itemId <= 3952 || _itemId >= 1341 && _itemId <= 1345;
	}

	/**
	 * @return the Identifier of the Item crafted with this L2RecipeList.
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * @return the quantity of Item crafted when using this L2RecipeList.
	 */
	public int getCount()
	{
		return _count;
	}

	/**
	 * @return true if this a Dwarven recipe or false if its a Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}

	/**
	 * @return the table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList.
	 */
	public L2RecipeInstance[] getRecipes()
	{
		return _recipes;
	}
}
