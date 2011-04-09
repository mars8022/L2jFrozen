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
package com.l2jfrozen.gameserver.geo.blocks;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Multilevel-block.
 * Includes 64 cells, each of which is divided in turn into layers.
 * Typically, the maximum number of layers is 125, but "heavy" units, this limit is 255.
 * Each layer consists of individual values of passableness (NSWE) in one direction or another light, as well as height.
 * 
 * @author nameless, 02.02.2011
 */
public final class MultilevelBlock extends CompiledBlock {
	// cell
	private final byte[] layers = new byte[64];
	// cell, layer
	private final short height[][] = new short[64][];
	private final byte NSWE[][] = new byte[64][];
	private final AtomicBoolean[][] doors = new AtomicBoolean[64][];
	
	public MultilevelBlock(ByteBuffer buffer) {
		byte layers;
		for (int cell = 0; cell < 64; cell++) {
			layers = buffer.get();
			height[cell] = new short[layers];
			NSWE[cell] = new byte[layers];
			doors[cell] = new AtomicBoolean[layers];
			for(int i = 0; i < layers; i++) doors[cell][i] = new AtomicBoolean(true);
			for (int i = 0, index; i < layers; i++) {
				index = buffer.position();
				height[cell][i] = (short)((short)(buffer.getShort(index) & 0x0fff0) >> 1);
				NSWE[cell][i] = (byte) (buffer.getShort(index) & 0x0F);
				buffer.position(index+2);
			}
			this.layers[cell] = --layers;
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getNSWE()
	 */
	@Override
	public byte getNSWE(int cell, byte layer) {
		if(doors[cell][layer].get())
			return NSWE[cell][layer];
		return NULL;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getHeight()
	 */
	@Override
	public short getHeight(int cell, byte layer) {
		return height[cell][layer];
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getLayers()
	 */
	@Override
	public byte getLayers(int cell) {
		return layers[cell];
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getType()
	 */
	@Override
	public BlockType getType() {
		return BlockType.MULTILEVEL;
	}

	/**
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#changeDoor(boolean)
	 */
	@Override
	public void changeDoor(int cell, byte layer, boolean isOpen) {
		doors[cell][layer].set(isOpen);
	}
}
