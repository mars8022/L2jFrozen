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
package com.l2jfrozen.gameserver.geo;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;

/**
 * 
 * 
 * @author -Nemesiss-
 * @author nameless
 */
final class Node {
	protected final static boolean ENABLE_DIAGONAL_FIND = Config.PATHFIND_DIAGONAL_FACTOR > 1.00D;
	private final static int STEP = 1;
	private final Location loc;
	private final int _x;
	private final int _y;
	private final boolean diagonal;
	private final Node _parent;
	private Node[] _neighbors;
	private int _cost;
	private byte NSWE = GeoEngine.NONE;
	
	public Node(int x, int y, int z) {
		this(x, y, z, false, null);
	}
	
	private Node(int x, int y, int z, Node parent) {
		this(x, y, z, false, parent);
	}
	
	private Node(int x, int y, int z, boolean diagonal, Node parent) {
		_x = x;
		_y = y;
		_parent = parent;
		loc = new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, GeoEngine.getInstance().nGetHeight(x, y, (short) z));
		this.diagonal = diagonal;
	}

	public void setCost(int cost) {
		_cost = (int) (diagonal ? cost * Config.PATHFIND_DIAGONAL_FACTOR : cost);
	}
	
	public byte getNSWE() {
		return NSWE != GeoEngine.NONE ? NSWE : (NSWE = GeoEngine.getInstance().nGetNSWE(getNodeX(), getNodeY(), getZ()));
	}

	public void attachNeighbors() {
		_neighbors = new Node[8];
		NSWE = getNSWE(); //if action goes in one cell/layer there is no sense each time to search NSWE, it is necessary to think
		if(NSWE == GeoEngine.NONE) return;
		
		int parentdirection = 0;
		if(_parent != null) { //we do not add the parent
			if (_parent.getNodeX() > _x) parentdirection = 1;
			if (_parent.getNodeX() < _x) parentdirection = -1;
			if (_parent.getNodeY() > _y) parentdirection = 2;
			if (_parent.getNodeY() < _y) parentdirection = -2;
		}
		
		if(NSWE != GeoEngine.ALL && parentdirection != 0) return;
		
		Node n = null, s = null, we;
		int index = 0; //check the correct direction, and add neighbors
		if(parentdirection != -2 && GeoEngine.checkNSWE(NSWE, _x, _y, _x, _y - STEP))
			n = _neighbors[index++] = new Node(_x, _y - STEP, loc.getZ(), this);
		if(parentdirection != 2 && GeoEngine.checkNSWE(NSWE, _x, _y, _x, _y + STEP))
			s = _neighbors[index++] = new Node(_x, _y + STEP, loc.getZ(), this);
		
		if(parentdirection != -1 && GeoEngine.checkNSWE(NSWE, _x, _y, _x - STEP, _y)) {
			we = _neighbors[index++] = new Node(_x - STEP, _y, loc.getZ(), this);
			if(ENABLE_DIAGONAL_FIND) {
    			if(n != null && (we.getNSWE() & GeoEngine.NORTH) != 0 && (n.getNSWE() & GeoEngine.WEST) != 0)
    				_neighbors[index++] = new Node(_x - STEP, _y - STEP, loc.getZ(), true, this);
    			if(s != null && (we.getNSWE() & GeoEngine.SOUTH) != 0 && (s.getNSWE() & GeoEngine.WEST) != 0)
    				_neighbors[index++] = new Node(_x - STEP, _y + STEP, loc.getZ(), true, this);
			}
		}
		if(parentdirection != 1 && GeoEngine.checkNSWE(NSWE, _x, _y, _x + STEP, _y)) {
			we = _neighbors[index++] = new Node(_x + STEP, _y, loc.getZ(), this);
			if(ENABLE_DIAGONAL_FIND) {
    			if(n != null && (we.getNSWE() & GeoEngine.NORTH) != 0 && (n.getNSWE() & GeoEngine.EAST) != 0)
    				_neighbors[index++] = new Node(_x + STEP, _y - STEP, loc.getZ(), true, this);
    			if(s != null && (we.getNSWE() & GeoEngine.SOUTH) != 0 && (s.getNSWE() & GeoEngine.EAST) != 0)
    				_neighbors[index++] = new Node(_x + STEP, _y + STEP, loc.getZ(), true, this);
			}
		}
	}

	public Node[] getNeighbors() {
		return _neighbors;
	}

	public Node getParent() {
		return _parent;
	}

	protected int getCost() {
		return _cost;
	}

	public int getX() {
		return loc.getX();
	}

	public int getY() {
		return loc.getY();
	}

	public int getZ() {
		return loc.getZ();
	}

	public int getNodeX() {
		return _x;
	}

	public int getNodeY() {
		return _y;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) return true;
		final Node n = (Node)obj;
		return Math.abs(_x - n._x) < STEP && Math.abs(_y - n._y) < STEP;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * loc.hashCode();
	}
	
	
}