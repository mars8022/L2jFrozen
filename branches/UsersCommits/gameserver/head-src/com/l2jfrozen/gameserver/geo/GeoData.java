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
package com.l2jfrozen.gameserver.geo;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.Point3D;


public class GeoData
{
	protected static final Logger _log = Logger.getLogger(GeoData.class.getName());
	
	private static final class SingletonHolder
	{
		static
		{
			_log.info("Geodata Engine: Disabled.");
		}

		protected static final GeoData INSTANCE = new GeoData();
	}
	
	protected GeoData()
	{
	}
	
	public static GeoData getInstance()
	{
		if(Config.GEODATA > 0)
			return GeoEngine.getInstance();
		return SingletonHolder.INSTANCE;
	}

	// Public Methods
	/**
	 * @param x
	 * @param y
	 * @return Geo Block Type
	 */
	public short getType(int x, int y)
	{
		return 0;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return Nearles Z
	 */
	public short getHeight(int x, int y, int z)
	{
		return (short) z;
	}

	/**
	 * @param x
	 * @param y
	 * @param zmin
	 * @param zmax
	 * @param spawnid
	 * @return
	 */
	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
	{
		return (short) zmin;
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public String geoPosition(int x, int y)
	{
		return "";
	}

	/**
	 * @param cha
	 * @param target
	 * @return True if cha can see target (LOS)
	 */
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		//If geo is off do simple check :]
		//Don't allow casting on players on different dungeon lvls etc
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(L2Object cha, Point3D worldPosition)
	{
		//If geo is off do simple check :]
		//Don't allow casting on players on different dungeon lvls etc
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		// If geo is off do simple check :]
		// Don't allow casting on players on different dungeon lvls etc
		return (Math.abs(z - tz) < 1000);
	}

	/**
	 * @param gm
	 * @param target
	 * @return True if cha can see target (LOS) and send usful info to PC
	 */
	public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
	{
		return true;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return Geo NSWE (0-15)
	 */
	public short getNSWE(int x, int y, int z)
	{
		return 15;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return Last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		return new Location(tx, ty, tz);
	}

	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		return true;
	}

	/**
	 * @param gm
	 * @param comment
	 */
	public void addGeoDataBug(L2PcInstance gm, String comment)
	{
		//Do Nothing
	}

	public void unloadGeodata(byte rx, byte ry)
	{
	}
	
	public boolean loadGeodataFile(byte rx, byte ry)
	{
		return false;
	}

	public boolean hasGeo(int x, int y)
	{
		return false;
	}

	public Node[] getNeighbors(Node n)
	{
		return null;
	}
}
