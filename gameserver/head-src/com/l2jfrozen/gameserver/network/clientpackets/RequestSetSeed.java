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
package com.l2jfrozen.gameserver.network.clientpackets;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.SeedProduction;

/**
 * Format: (ch) dd [ddd] d - manor id d - size [ d - seed id d - sales d - price ]
 * @author l3x
 */
public class RequestSetSeed extends L2GameClientPacket
{
	private int _size;
	private int _manorId;
	private int[] _items; // _size*3
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		
		if (_size * 12 > _buf.remaining() || _size > 500 || _size < 1)
		{
			_size = 0;
			return;
		}
		
		_items = new int[_size * 3];
		
		for (int i = 0; i < _size; i++)
		{
			final int itemId = readD();
			_items[i * 3 + 0] = itemId;
			final int sales = readD();
			_items[i * 3 + 1] = sales;
			final int price = readD();
			_items[i * 3 + 2] = price;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_size < 1)
			return;
		
		final FastList<SeedProduction> seeds = new FastList<>();
		
		for (int i = 0; i < _size; i++)
		{
			final int id = _items[i * 3 + 0];
			final int sales = _items[i * 3 + 1];
			final int price = _items[i * 3 + 2];
			if (id > 0)
			{
				final SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(_manorId).setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(_manorId).saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0A RequestSetSeed";
	}
}
