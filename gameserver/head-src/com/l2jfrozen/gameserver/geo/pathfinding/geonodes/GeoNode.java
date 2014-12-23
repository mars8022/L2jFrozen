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
package com.l2jfrozen.gameserver.geo.pathfinding.geonodes;

import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.model.L2World;

public final class GeoNode extends Node
{
	private final short _x;
	private final short _y;
	private final short _z;
	
	public GeoNode(final short x, final short y, final short z, final int neighborsIdx)
	{
		super(neighborsIdx);
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public int getX()
	{
		return L2World.MAP_MIN_X + _x * 128 + 48;
	}
	
	@Override
	public int getY()
	{
		return L2World.MAP_MIN_Y + _y * 128 + 48;
	}
	
	@Override
	public short getZ()
	{
		return _z;
	}
	
	@Override
	public void setZ(final short z)
	{
		//
	}
	
	@Override
	public int getNodeX()
	{
		return _x;
	}
	
	@Override
	public int getNodeY()
	{
		return _y;
	}
}
