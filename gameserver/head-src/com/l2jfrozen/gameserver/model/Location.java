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


/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location
{
	public int _x;
	public int _y;
	public int _z;
	public int _heading;

	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
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
	
	public void setX(int x) {
		_x = x;
	}
	
	public void setY(int y) {
		_y = y;
	}
	
	public void setZ(int z) {
		_z = z;
	}
	
	public void setHeading(int head) {
		_heading = head;
	}
	
	public void setXYZ(int x, int y, int z) {
		_x = x;
		_y = y;
		_z = z;
	}

	public boolean equals(int x, int y, int z) {
		if(_x == x && _y == y && _z == z)
			return true;
		return false;
	}
	
	@Override public boolean equals(Object obj) {
		if(super.equals(obj)) return true;
		if(!(obj instanceof Location)) return false;
		final Location loc = (Location) obj;
		return _x == loc._x && _y == loc._y && _z == loc._z;
	}
	
	@Override public int hashCode() {
		final int prime = 31;
	    int result = 1;
	    result = prime * result + _x;
	    result = prime * result + _y;
	    result = prime * result + _z;
		return result;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "x: " + _x + ", y: " + _y + ", z: " + _z;
	}
}
