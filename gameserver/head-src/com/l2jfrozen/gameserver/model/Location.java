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
package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location
{
	public int _x;
	public int _y;
	public int _z;
	public int _heading;
	
	public Location(final int x, final int y, final int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location(final int x, final int y, final int z, final int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
	public Location(final L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
	}
	
	public Location(final L2Character obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void setX(final int x)
	{
		_x = x;
	}
	
	public void setY(final int y)
	{
		_y = y;
	}
	
	public void setZ(final int z)
	{
		_z = z;
	}
	
	public void setHeading(final int head)
	{
		_heading = head;
	}
	
	public void setXYZ(final int x, final int y, final int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public boolean equals(final int x, final int y, final int z)
	{
		if (_x == x && _y == y && _z == z)
			return true;
		return false;
	}
}
