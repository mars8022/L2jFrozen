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
package com.l2jfrozen.gameserver.geo.pathfinding.utils;

import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.util.L2FastSet;
import com.l2jfrozen.gameserver.geo.util.ObjectPool;

/**
 * @author Sami
 */
public final class CellNodeMap
{
	protected final L2FastSet<Node> _cellIndex = new L2FastSet<>(4096);
	
	protected CellNodeMap()
	{
		
	}
	
	public void add(final Node n)
	{
		_cellIndex.add(n);
	}
	
	public boolean contains(final Node n)
	{
		return _cellIndex.contains(n);
	}
	
	public static CellNodeMap newInstance()
	{
		return POOL.get();
	}
	
	public static void recycle(final CellNodeMap map)
	{
		POOL.store(map);
	}
	
	private static final ObjectPool<CellNodeMap> POOL = new ObjectPool<CellNodeMap>()
	{
		@Override
		protected void reset(final CellNodeMap map)
		{
			map._cellIndex.clear();
		}
		
		@Override
		protected CellNodeMap create()
		{
			return new CellNodeMap();
		}
	};
}