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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.blocks.CompiledBlock;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.Point3D;

/**
 * 
 * @author -Nemesiss-
 * @author nameless
 * @since 0.3.0
 */

final class GeoEngine extends GeoData {
	private static final Log _log = LogFactory.getLog(GeoEngine.class);

	protected final static byte EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, ALL = 15,
								NONE = 0;

	private BufferedOutputStream _geoBugsOut;
	private final GeodataRepository geodata = new GeodataRepository();
	
	private static GeoEngine _instance;
	public static GeoEngine getInstance() {
		return _instance != null ? _instance : (_instance = new GeoEngine());
	}

	public GeoEngine() {
		nInitGeodata();
	}

	private static class GeoFile implements FileFilter {
		@Override
		public boolean accept(File f) {
			return !f.isHidden() && f.getName().endsWith(".l2j") && f.length() > 196608;
		}
	}

	private void nInitGeodata() {
		_log.info("Loading geodata...");
		File geoFolder = new File("./data/geodata");
		if (!geoFolder.exists()) {
			_log.warn("Geodata folder not found!");
			return;
		}
		for (File geo : geoFolder.listFiles(new GeoFile())) {
			final String name = geo.getName();
			_log.debug(name);
			final byte x = Byte.parseByte(name.substring(0, name.indexOf("_"))), y = Byte.parseByte(name.substring(name.indexOf("_") + 1, name.indexOf(".")));
			try {
				loadGeodataFile((short) ((x << 5) + y), geo);
			} catch (Exception e) {
				_log.warn("Failed to load geodata: " + x + " " + y, e);
			}
		}

		try {
			_geoBugsOut = new BufferedOutputStream(new FileOutputStream("./data/geodata/geo_bugs.txt", true));
		} catch (Exception e) {
			_log.error("Failed to Load geo_bugs.txt File.", e);
		}
	}

	/**
	 * @param region
	 * @param geo 
	 * @throws Exception
	 */
	private void loadGeodataFile(short region, File geo) throws Exception {
		FileChannel roChannel = null;
		try {
			// Create a read-only memory-mapped file
			roChannel = new RandomAccessFile(geo, "r").getChannel();
			MappedByteBuffer geoBuff = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size());
			geoBuff.order(ByteOrder.LITTLE_ENDIAN);
			geodata.load(geoBuff, region);
			geoBuff.clear();
		} finally {
			CloseUtil.close(roChannel);
		}
	}

	/**
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.GeoData#unloadGeodata(byte, byte)
	 */
	@Override
	public void unloadGeodata(byte x, byte y) {
		geodata.unload((short) (((x - 10) << 5) + (y - 10)));
	}

	/**
	 * Возвращает тип блока геодаты в данной ячейке:
	 * 0 - short,
	 * 1 - complex,
	 * 2 - multilevel
	 */
	@Override
	public int getType(int x, int y) {
		final int x1 = x - L2World.MAP_MIN_X >> 4, y1 = y - L2World.MAP_MIN_Y >> 4;
		return geodata.getBlock(getRegionOffset(x1, y1), getBlockOffset(x1, y1)).getType().ordinal();
	}

	/**
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.GeoData#getHeight(int, int, int)
	 */
	@Override
	public short getHeight(int x, int y, int z) {
		return nGetHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, (short) z);
	}

	/**
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.GeoData#getSpawnHeight(int, int, int, int)
	 */
	@Override
	public short getSpawnHeight(int x, int y, int zmin, int zmax) {
		return nGetSpawnHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, (short) zmin, (short) zmax);
	}

	@Override
	public String geoPosition(int x, int y) {
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;
		return "block offset: " + getBlockOffset(gx, gy) + " cell offset: " + getCellOffset(gx, gy) + "  region offset: "
				+ getRegionOffset(gx, gy) + " block offset: " + getBlockOffset(gx, gy);
	}

	@Override
	public boolean canSeeTarget(L2Object cha, Point3D target) {
//		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
//			return false;

		if (cha.getZ() >= target.getZ())
			return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
		return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
	}
	
	/**
	 * 
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.GeoData#canSeeTarget(com.l2scoria.gameserver.model.L2Object, com.l2scoria.gameserver.model.L2Object)
	 */
	@Override
	public boolean canSeeTarget(L2Object cha, L2Object target) {
		int z = cha.getZ() + 45;
		int z2 = target.getZ() + 45;

		if (cha instanceof L2SiegeGuardInstance || cha instanceof L2FortSiegeGuardInstance)
			z += 30;

//		if (!(target instanceof L2DoorInstance)
//				&& DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2))
//			return false;

		if (target instanceof L2DoorInstance)
			return true;

		if (target instanceof L2SiegeGuardInstance || target instanceof L2FortSiegeGuardInstance)
			z2 += 30; // well they don't move closer to balcony fence at the moment :(
		if (cha.getZ() >= target.getZ())
			return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
		return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
	}

	@Override
	public short getNSWE(int x, int y, int z) {
		return nGetNSWE(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
	}

	@Override
	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz) {
		Location destiny = moveCheck(x, y, z, tx, ty, tz);
		return (destiny.getX() == tx && destiny.getY() == ty && Math.abs(destiny.getZ() - tz) < 30);
	}

	@Override
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz) {
//		if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
//			return startpoint;

		return moveCheck(new Location(x, y, z), new Location(tx, ty, tz), x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z,
				tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}

	@Override
	public void addGeoDataBug(L2PcInstance gm, String comment) {
		int gx = gm.getX() - L2World.MAP_MIN_X >> 4;
		int gy = gm.getY() - L2World.MAP_MIN_Y >> 4;
		int boff = getBlockOffset(gx, gy);
		int coff = getCellOffset(gx, gy);
		int rx = (gx >> 11) + 15;
		int ry = (gy >> 11) + 10;
		String out = rx + ";" + ry + ";" + boff + ";" + coff + ";" + gm.getZ() + ";" + comment + "\n";
		try {
			_geoBugsOut.write(out.getBytes());
			_geoBugsOut.flush();
			gm.sendMessage("GeoData bug saved!");
		} catch (Exception e) {
			_log.error("", e);
			gm.sendMessage("GeoData bug save Failed!");
		}
	}

	@Override
	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz) {
		return canSee(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}

	@Override
	public boolean hasGeo(int x, int y) {
		return geodata.hasGeodata(getRegionOffset(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4));
	}
	
	@Override public Location[] findPath(int x, int y, int z, int tx, int ty, int tz, L2Character player) {
		if (!hasGeo(x, y) || !hasGeo(tx, ty)) return null;
		final int nx = x - L2World.MAP_MIN_X >> 4, ny = y - L2World.MAP_MIN_Y >> 4;
		final int ndx = tx - L2World.MAP_MIN_X >> 4, ndy = ty - L2World.MAP_MIN_Y >> 4;

		return nFindPath(new Node(nx, ny, z), new Node(ndx, ndy, tz), player);
	}
	
	@Override public void changeDoor(int x, int y, int z, boolean isOpen) {
		nChangeDoor(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, isOpen);
	}

	// Geodata Methods
	private static short getRegionOffset(int x, int y) {
		return (short) ((((x >> 11) + 16) << 5) + (y >> 11) + 10);
	}

	/**
	 * (getBlock(x) << 3) + getBlock(y)
	 */
	private static int getBlockOffset(int x, int y) {
		return (((x >> 3) % 256) << 8) + ((y >> 3) % 256);
	}

	private static int getCellOffset(int x, int y) {
		//any cell = 0-7
		return ((x % 8) << 3) + (y % 8);
	}

	private static int sign(int x) {
		return x >= 0 ? 1 : -1;
	}

	protected boolean canSee(int x, int y, int z, int tx, int ty, int tz) {
		int dx = (tx - x);
		int dy = (ty - y);
		final int dz = (tz - z);
		final int distance2 = dx * dx + dy * dy;

		if (distance2 > 90000)
			return false;
		else if (distance2 < 82)
			return !(dz * dz > 22500 && geodata.hasGeodata(getRegionOffset(x, y)));

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		final int inc_z_directionx = dz * dx / (distance2);
		final int inc_z_directiony = dz * dy / (distance2);

		// next_* are used in NLOS check from x,y
		int next_x = x;
		int next_y = y;

		// creates path to the target
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			final int delta_A = 2 * dy;
			final int delta_B = delta_A - 2 * dx;

			for (int i = 0, d = delta_A - dx; i < dx; i++) {
				x = next_x;
				y = next_y;
				next_x += inc_x;
				z += inc_z_directionx;

				if (!nLOS(x, y, z, inc_x, 0, inc_z_directionx, tz))
					return false;

				if (d > 0) {
					d += delta_B;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(next_x, y, z, 0, inc_y, inc_z_directiony, tz))
						return false;
				} else
					d += delta_A;
			}
		} else {
			final int delta_A = 2 * dx;
			final int delta_B = delta_A - 2 * dy;
			for (int i = 0, d = delta_A - dy; i < dy; i++) {
				x = next_x;
				y = next_y;
				next_y += inc_y;
				z += inc_z_directiony;

				if (!nLOS(x, y, z, 0, inc_y, inc_z_directiony, tz))
					return false;

				if (d > 0) {
					d += delta_B;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, next_y, z, inc_x, 0, inc_z_directionx, tz))
						return false;
				} else
					d += delta_A;
			}
		}
		return true;
	}

	private Location moveCheck(Location startpoint, Location destiny, int x, int y, int z, int tx, int ty, int tz) {
		int dx = (tx - x);
		int dy = (ty - y);
		final int distance2 = dx * dx + dy * dy;

		if (distance2 == 0)
			return destiny;
		if (distance2 > 102400) {
			// Avoid too long check
			// Currently we calculate a middle point
			// for wyvern users and otherwise for comfort
			final double divider = Math.sqrt((double) 30000 / distance2);
			tx = x + (int) (divider * dx);
			ty = y + (int) (divider * dy);
			int dz = (tz - startpoint.getZ());
			tz = startpoint.getZ() + (int) (divider * dz);
			dx = (tx - x);
			dy = (ty - y);
		}

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);

		// next_* are used in NcanMoveNext check from x,y
		int next_x = x;
		int next_y = y;
		int tempz = z;

		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			final int delta_A = 2 * dy;
			final int delta_B = delta_A - 2 * dx;
			int d = delta_A - dx;
			
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				next_x += inc_x;
				if (d > 0)
				{
					d += delta_B;
					tempz = nCanMoveNext(x, y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
					next_y += inc_y;
					tempz = nCanMoveNext(next_x, y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
				}
				else
				{
					d += delta_A;
					tempz = nCanMoveNext(x, y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
				}
			}
		} else {
			final int delta_A = 2 * dx;
			final int delta_B = delta_A - 2 * dy;
			int d = delta_A - dy;
			
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				next_y += inc_y;
				if (d > 0)
				{
					d += delta_B;
					tempz = nCanMoveNext(x, y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
					next_x += inc_x;
					tempz = nCanMoveNext(x, next_y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
				}
				else
				{
					d += delta_A;
					tempz = nCanMoveNext(x, y, z, next_x, next_y, tz);
					if (tempz == Short.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, z);
					z = tempz;
				}
			}
		}
		
		return new Location(destiny.getX(), destiny.getY(), z);
	}

	protected short nGetHeight(int x, int y, short z) {
		final short region = getRegionOffset(x, y);
		if (!geodata.hasGeodata(region))
			return z;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case FLAT:
			return block.getHeight();

		case COMPLEX:
			return block.getHeight(cell);

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			if(layers == 0) return block.getHeight(cell, layers);
			short height = Short.MIN_VALUE;
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (Math.abs(z - height) >= Math.abs(z - temp))
					height = temp;
			}
			
			return height;
		}
		return z;
	}
	
	/**
	 * @return One layer higher Z than parameter Z
	 */
	private short nGetUpperHeight(int x, int y, short z) {
		final short region = getRegionOffset(x, y);

		if (!geodata.hasGeodata(region))
			return z;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case FLAT:
			return block.getHeight();

		case COMPLEX:
			return block.getHeight(cell);

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			if(layers == 0) return block.getHeight(cell, layers);
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (temp < z)
					return temp;
			}
			return z;
		}
		return z;
	}

	/**
	 * @return Z betwen zmin and zmax
	 */
	private short nGetSpawnHeight(int x, int y, short zmin, short zmax) {
		final short region = getRegionOffset(x, y);

		if (!geodata.hasGeodata(region))
			return zmin;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));
		final int cell = getCellOffset(x, y);
		short height = Short.MIN_VALUE;
		switch (block.getType()) {
		case FLAT:
			height = block.getHeight();
			return height > zmax + 1000 || height < zmin - 1000 ? zmin : height;

		case COMPLEX:
			height = block.getHeight(cell);
			return height > zmax + 1000 || height < zmin - 1000 ? zmin : height;

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			if(layers == 0) return block.getHeight(cell, layers);
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (Math.abs(zmin - height) > Math.abs(zmin - temp))
					height = temp;
			}

			return height > zmax + 1000 || height < zmin - 1000 ? zmin : height;
		}

		return zmin;
	}

	/**
	 * Height if char can move to (tx,ty,tz), or Short.MIN_VALUE
	 * @return
	 */
	private int nCanMoveNext(int x, int y, int z, int tx, int ty, int tz) {
		final short region = getRegionOffset(x, y);

		if (!geodata.hasGeodata(region))
			return z;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));

		short height = Short.MIN_VALUE;
		byte NSWE = NONE;
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case FLAT:
			return block.getHeight();

		case COMPLEX:
			NSWE = block.getNSWE(cell);
			height = block.getHeight(cell);
			return checkNSWE(NSWE, x, y, tx, ty) ? height : Short.MIN_VALUE;

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if(Math.abs(z - height) > Math.abs(z - temp)) {
					height = temp;
					NSWE = block.getNSWE(cell, layers);
				}
			}
			return checkNSWE(NSWE, x, y, tx, ty) ? height : Short.MIN_VALUE;
		}

		return z;
	}

	/**
	 * @return True if Char can see target
	 */
	private boolean nLOS(int x, int y, int z, int inc_x, int inc_y, int inc_z, int tz) {
		final short region = getRegionOffset(x, y);

		if (!geodata.hasGeodata(region))
			return true;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));

		byte NSWE = ALL;
		short height = Short.MAX_VALUE;
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case FLAT:
			height = block.getHeight();
			return z > height ? z + inc_z > height : z + inc_z < height;

		case COMPLEX:
			height = block.getHeight(cell);
			NSWE = block.getNSWE(cell);
			return !checkNSWE(NSWE, x, y, x + inc_x, y + inc_y) ? !(z >= nGetUpperHeight(x + inc_x, y + inc_y, height)) : true;

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			short lowHeight = Short.MIN_VALUE;
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (z > temp) {
					lowHeight = temp;
					NSWE = block.getNSWE(cell, layers);
					break;
				}
				height = temp;
			}
			
			return ((z - height) < -10 && (z - height) > inc_z - 20 && (z - lowHeight) > 40) ? false : !checkNSWE(NSWE, x, y, x + inc_x, y + inc_y) ? !(z < nGetUpperHeight(x + inc_x, y + inc_y, lowHeight)) : true;
		}
		return true;
	}

	/**
	 * @return NSWE: 0-15
	 */
	protected byte nGetNSWE(int x, int y, int z) {
		final short region = getRegionOffset(x, y);

		if (!geodata.hasGeodata(region))
			return ALL;

		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case FLAT:
			return ALL;

		case COMPLEX:
			return block.getNSWE(cell);

		case MULTILEVEL:
			byte layers = block.getLayers(cell);
			if(layers == 0) return block.getNSWE(cell, layers);
			byte NSWE = NONE;
			short height = Short.MIN_VALUE;
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (Math.abs(z - height) > Math.abs(z - temp)) {
					height = temp;
					NSWE = block.getNSWE(cell, layers);
				}
			}

			return NSWE;
		}
		return NONE;
	}

	/**
	 * Change door open/close status
	 * @param x
	 * @param y
	 * @param z
	 * @param isOpen
	 */
	private void nChangeDoor(int x, int y, int z, boolean isOpen) {
		final short region = getRegionOffset(x, y);
		if(!geodata.hasGeodata(region)) return;
		final CompiledBlock block = geodata.getBlock(region, getBlockOffset(x, y));
		final int cell = getCellOffset(x, y);
		switch (block.getType()) {
		case COMPLEX:
			block.changeDoor(cell, isOpen);
			break;
			
		case MULTILEVEL:
			byte layers = block.getLayers(cell), currLayer = NONE;
			if(layers == 0) {
				block.changeDoor(cell, layers, isOpen);
				break;
			}
			short height = Short.MIN_VALUE;
			for (short temp; layers > -1; layers--) {
				temp = block.getHeight(cell, layers);
				if (Math.abs(z - height) > Math.abs(z - temp)) {
					height = temp;
					currLayer = layers;
				}
			}
			block.changeDoor(cell, currLayer, isOpen);
			break;
		}
	}

	/**
	 * Pathfinding by the modified algorithm A *. <br>
	 * Differences from classical A*:
	 * <li> Using the direction of wave
	 * <li> Used assignment of a priority to each point
	 * <li> Used diagonal points
	 * 
	 * @param start start node
	 * @param end end node
	 * @return path or null (path not found or path is not exist)
	 */
	private Location[] nFindPath(Node start, Node end, L2Character player) {
		final List<Node> visited = new ArrayList<Node>();
		final LinkedList<Node> to_visit = new LinkedList<Node>();
		to_visit.add(start);
		visited.add(start);
		final int targetx = end.getNodeX(),
		targety = end.getNodeY(),
		targetz = end.getZ();
	
		int dx, dy, dz;
		boolean added;
	
		Node node;
		for(int i = 0; i < Config.PATHFIND_DISTANCE; i++) {
			node = to_visit.pollFirst();
			if(node == null) return null;
			if (node.equals(end)) return constructPath(node, player);
			 
			node.attachNeighbors();
			
			for (Node n : node.getNeighbors()) {
				if(n == null) break;
				if (visited.contains(n)) continue;
				if(Math.abs(n.getZ() - n.getParent().getZ()) > 150) continue;
				
				added = false;
				dx = targetx - n.getNodeX();
				dy = targety - n.getNodeY();
				dz = targetz - n.getZ();
				n.setCost((int) Math.sqrt(dx * dx + dy * dy + dz * dz)); //distance and node priority, old priority math: short dx * dx + dy * dy + dz / 2 * dz
				for (int index = 0; index < to_visit.size(); index++)
					if(n.getCost() < to_visit.get(index).getCost()) {
						to_visit.add(index, n);
						added = true;
						break;
					}
				if (!added)
					to_visit.addLast(n);
				visited.add(n);
			}
		}
	
		return null;
	}

	/**
	 * @return True if NSWE dont block given direction
	 */
	protected static boolean checkNSWE(byte NSWE, int x, int y, int tx, int ty) {
		if (NSWE == ALL) return true;
		if(NSWE == NONE) return false;
		if(tx > x) {
			if ((NSWE & EAST) == 0) return false;
		} else if (tx < x) if((NSWE & WEST) == NONE)
			return false;
		if (ty > y) { 
			if((NSWE & SOUTH) == 0) return false;
		} else if (ty < y) if((NSWE & NORTH) == NONE)
			return false;
		return true;
	}
	
	private final static List<L2ItemInstance> debugPath = Config.DEBUG ? new ArrayList<L2ItemInstance>() : null;
	private Location[] constructPath(final Node node, L2Character player) {
		final LinkedList<Location> path = new LinkedList<Location>();
		int previousdirectionx = Integer.MIN_VALUE, previousdirectiony = Integer.MIN_VALUE;
		int directionx, directiony, directionz;
		
		boolean addNextNode = true;
		for(Node nextNode = node; nextNode.getParent() != null; nextNode = nextNode.getParent()) {
			if(!addNextNode) {
				addNextNode = true;
				continue;
			}
			if (!Node.ENABLE_DIAGONAL_FIND && nextNode.getParent().getParent() != null) {
				directionx = nextNode.getNodeX() - nextNode.getParent().getParent().getNodeX();
				directiony = nextNode.getNodeY() - nextNode.getParent().getParent().getNodeY();
				if(Math.abs(directiony) != Math.abs(directiony)) {
					directionx = nextNode.getNodeX() - nextNode.getParent().getNodeX();
					directiony = nextNode.getNodeY() - nextNode.getParent().getNodeY();
				}
			} else {
				directionx = nextNode.getNodeX() - nextNode.getParent().getNodeX();
				directiony = nextNode.getNodeY() - nextNode.getParent().getNodeY();
			}
			
			directionz = nextNode.getZ() - nextNode.getParent().getZ();
			
			if(nextNode.getParent().getParent() != null && 
				((nextNode.getNodeX() == nextNode.getParent().getNodeX() && nextNode.getNodeX() == nextNode.getParent().getParent().getX()) ||
				(nextNode.getNodeY() == nextNode.getParent().getNodeY() && nextNode.getNodeY() == nextNode.getParent().getParent().getY()))) {
				addNextNode = false;
			}
			
			if (directionx != previousdirectionx || directiony != previousdirectiony || directionz > 30) {
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				path.addFirst(nextNode.getLocation());
			} else addNextNode = true;
		}
		
		if(Config.DEBUG && player != null && player.charIsGM()) {
			for(L2ItemInstance item : debugPath)
				item.decayMe();
			debugPath.clear();
			for(Location loc : path) {
				final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 57);
				item.dropMe(null, loc.getX(), loc.getY(), loc.getZ());
				debugPath.add(item);
			}
		}
		
		if(path.size() > 2) pathSmoothing(path, player);
		
		if(Config.DEBUG && player != null && player.charIsGM()) {
			for(Location loc : path) {
				final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 735);
        		item.dropMe(null, loc.getX(), loc.getY(), loc.getZ());
        		debugPath.add(item);
			}
		}
		return path.toArray(new Location[path.size()]);
	}

	/**
	 * Check on passableness 3х points ({@link #canMoveFromToTarget(int, int, int, int, int, int) canMoveFromToTarget})
	 *  if its possible to pass - deleted 2 point.<br>
	 * 
	 * @param path
	 */
	private void pathSmoothing(List<Location> path, L2Character player) {
		Location start, check, end;
		boolean isSmoothind;
		int i = 0;
		do {
			isSmoothind = false;
    		for(int index = 0, size = path.size(); index < size - 2;) {
    			start = path.get(index);
    			check = path.get(index+1);
    			end = path.get(index+2);
    			if((start.getX() == end.getX() && check.getX() == end.getX()) || (start.getY() == end.getY() && check.getY() == end.getY())) {
    				isSmoothind = true;
        			path.remove(index+1);
        			size--;
            		continue;
        		}
        		if(!canMoveFromToTarget(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ())) {
        			index++;
        			continue;
        		}
        		isSmoothind = true;
    			path.remove(index+1);
    			size--;
    		}
		} while(player != null && i++ < Config.PATHFIND_SMOOTHING_LEVEL && path.size() > 2 && isSmoothind);
	}
}
