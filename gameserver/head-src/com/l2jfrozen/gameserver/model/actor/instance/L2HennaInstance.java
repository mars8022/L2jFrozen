/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.templates.L2Henna;

/**
 * This class represents a Non-Player-Character in the world. it can be a monster or a friendly character. it also uses a template to fetch some static values. the templates are hardcoded in the client, so we can rely on them.
 * @version $Revision$ $Date$
 */

public class L2HennaInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2HennaInstance.class);
	
	private final L2Henna _template;
	private int _symbolId;
	private int _itemIdDye;
	private int _price;
	private int _statINT;
	private int _statSTR;
	private int _statCON;
	private int _statMEM;
	private int _statDEX;
	private int _statWIT;
	private int _amountDyeRequire;
	
	public L2HennaInstance(final L2Henna template)
	{
		_template = template;
		_symbolId = _template.symbolId;
		_itemIdDye = _template.dye;
		_amountDyeRequire = _template.amount;
		_price = _template.price;
		_statINT = _template.statINT;
		_statSTR = _template.statSTR;
		_statCON = _template.statCON;
		_statMEM = _template.statMEM;
		_statDEX = _template.statDEX;
		_statWIT = _template.statWIT;
	}
	
	public String getName()
	{
		String res = "";
		if (_statINT > 0)
		{
			res = res + "INT +" + _statINT;
		}
		else if (_statSTR > 0)
		{
			res = res + "STR +" + _statSTR;
		}
		else if (_statCON > 0)
		{
			res = res + "CON +" + _statCON;
		}
		else if (_statMEM > 0)
		{
			res = res + "MEN +" + _statMEM;
		}
		else if (_statDEX > 0)
		{
			res = res + "DEX +" + _statDEX;
		}
		else if (_statWIT > 0)
		{
			res = res + "WIT +" + _statWIT;
		}
		
		if (_statINT < 0)
		{
			res = res + ", INT " + _statINT;
		}
		else if (_statSTR < 0)
		{
			res = res + ", STR " + _statSTR;
		}
		else if (_statCON < 0)
		{
			res = res + ", CON " + _statCON;
		}
		else if (_statMEM < 0)
		{
			res = res + ", MEN " + _statMEM;
		}
		else if (_statDEX < 0)
		{
			res = res + ", DEX " + _statDEX;
		}
		else if (_statWIT < 0)
		{
			res = res + ", WIT " + _statWIT;
		}
		
		return res;
	}
	
	public L2Henna getTemplate()
	{
		return _template;
	}
	
	public int getSymbolId()
	{
		return _symbolId;
	}
	
	public void setSymbolId(final int SymbolId)
	{
		_symbolId = SymbolId;
	}
	
	public int getItemIdDye()
	{
		return _itemIdDye;
	}
	
	public void setItemIdDye(final int ItemIdDye)
	{
		_itemIdDye = ItemIdDye;
	}
	
	public int getAmountDyeRequire()
	{
		return _amountDyeRequire;
	}
	
	public void setAmountDyeRequire(final int AmountDyeRequire)
	{
		_amountDyeRequire = AmountDyeRequire;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public void setPrice(final int Price)
	{
		_price = Price;
	}
	
	public int getStatINT()
	{
		return _statINT;
	}
	
	public void setStatINT(final int StatINT)
	{
		_statINT = StatINT;
	}
	
	public int getStatSTR()
	{
		return _statSTR;
	}
	
	public void setStatSTR(final int StatSTR)
	{
		_statSTR = StatSTR;
	}
	
	public int getStatCON()
	{
		return _statCON;
	}
	
	public void setStatCON(final int StatCON)
	{
		_statCON = StatCON;
	}
	
	public int getStatMEM()
	{
		return _statMEM;
	}
	
	public void setStatMEM(final int StatMEM)
	{
		_statMEM = StatMEM;
	}
	
	public int getStatDEX()
	{
		return _statDEX;
	}
	
	public void setStatDEX(final int StatDEX)
	{
		_statDEX = StatDEX;
	}
	
	public int getStatWIT()
	{
		return _statWIT;
	}
	
	public void setStatWIT(final int StatWIT)
	{
		_statWIT = StatWIT;
	}
}
