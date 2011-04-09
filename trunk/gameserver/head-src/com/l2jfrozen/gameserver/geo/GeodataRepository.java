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

import java.nio.ByteBuffer;

import com.l2jfrozen.gameserver.geo.blocks.CompiledBlock;
import com.l2jfrozen.gameserver.geo.blocks.ComplexBlock;
import com.l2jfrozen.gameserver.geo.blocks.FlatBlock;
import com.l2jfrozen.gameserver.geo.blocks.MultilevelBlock;

/**
 * 
 * 
 * @author nameless, 29.01.2011
 */
final class GeodataRepository {
	// region, block
	private final CompiledBlock[][] geodata = new CompiledBlock[857][];
	public void load(final ByteBuffer buffer, final int regionoffset) {
		CompiledBlock[] blocks = new CompiledBlock[65536];

		byte type;
		for (int blockCount = 0; blockCount < 65536; blockCount++) {
			type = buffer.get();
			if (type == 0) // 1x short, flat block
				blocks[blockCount] = new FlatBlock((short) (buffer.getShort() & 0x0fff0));
			else if (type == 1) // 64x short, complex block
				blocks[blockCount] = new ComplexBlock(buffer);
			else // 64x-8192x short, multilevel block
				blocks[blockCount] = new MultilevelBlock(buffer);
		}
		
		geodata[regionoffset] = blocks;
	}

	public CompiledBlock getBlock(final short region, final int block) {
		return geodata[region][block];
	}

	public boolean hasGeodata(final int region) {
		try {
			return region <= geodata.length && geodata[region][0] != null;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/**
	 * Выгрузка геодаты из памяти.
	 * 
	 * @param region
	 */
	public void unload(final int region) {
		geodata[region] = null;
	}
}
