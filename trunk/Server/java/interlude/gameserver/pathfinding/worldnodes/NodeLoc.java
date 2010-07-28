/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.pathfinding.worldnodes;

import interlude.gameserver.pathfinding.AbstractNodeLoc;

/**
 * @author -Nemesiss-
 */
public class NodeLoc extends AbstractNodeLoc
{
	private final int _x;
	private final int _y;
	private short _z;

	public NodeLoc(int x, int y, short z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	/**
	 * @see interlude.gameserver.pathfinding.AbstractNodeLoc#getX()
	 */
	@Override
	public int getX()
	{
		return _x;
	}

	/**
	 * @see interlude.gameserver.pathfinding.AbstractNodeLoc#getY()
	 */
	@Override
	public int getY()
	{
		return _y;
	}

	/**
	 * @see interlude.gameserver.pathfinding.AbstractNodeLoc#getZ()
	 */
	@Override
	public short getZ()
	{
		return _z;
	}

	/**
	 * @see interlude.gameserver.pathfinding.AbstractNodeLoc#getNodeX()
	 */
	@Override
	public short getNodeX()
	{
		return (short)_x;
	}

	/**
	 * @see interlude.gameserver.pathfinding.AbstractNodeLoc#getNodeY()
	 */
	@Override
	public short getNodeY()
	{
		return (short)_y;
	}

	@Override
	public void setZ(short z) {
		_z = z;
	}
	
	
}
