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
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.Point3D;

public class GeoData {
	private static GeoData _instance;

	public static GeoData getInstance() {
		return _instance != null ? _instance : Config.GEODATA > 0 ? (_instance = GeoEngine.getInstance()) : (_instance = new GeoData());
	}

	public int getType(int x, int y) {
		return 0;
	}

	public short getHeight(int x, int y, int z) {
		return (short) z;
	}

	public short getSpawnHeight(int x, int y, int zmin, int zmax) {
		return (short) zmin;
	}

	public String geoPosition(int x, int y) {
		return "";
	}

	public boolean canSeeTarget(L2Object cha, L2Object target) {
		return (Math.abs(target.getZ() - cha.getZ()) < 1000);
	}

	public boolean canSeeTarget(L2Object cha, Point3D worldPosition) {
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz) {
		return (Math.abs(z - tz) < 1000);
	}

	public short getNSWE(int x, int y, int z) {
		return 15;
	}

	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz) {
		return new Location(tx, ty, tz);
	}

	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz) {
		return true;
	}

	public void addGeoDataBug(L2PcInstance gm, String comment) {
	}

	public void unloadGeodata(byte rx, byte ry) {
	}

	public boolean loadGeodataFile(byte rx, byte ry) {
		return false;
	}

	public boolean hasGeo(int x, int y) {
		return false;
	}
	
	public Location[] findPath(int x, int y, int z, int tx, int ty, int tz, L2Character player) {
		return null;
	}
	
	public void changeDoor(int x, int y, int z, boolean isOpen) {}
}