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
 * Complex-block.
 * Includes 64 cells, each of which contains the individual values passableness (NSWE) and heights.
 * 
 * @author nameless, 02.02.2011
 */
public final class ComplexBlock extends CompiledBlock {
	private final byte NSWE[] = new byte[64];
	private final short height[] = new short[64];
	private final AtomicBoolean[] doors = new AtomicBoolean[64];
	public ComplexBlock(ByteBuffer buffer) {
		int index;
		for(int i = 0; i < doors.length; i++) doors[i] = new AtomicBoolean(true);
		for (int cell = 0; cell < 64; cell++) {
			index = buffer.position();
			height[cell] = (short)((short)(buffer.getShort(index) & 0x0fff0) >> 1);
			NSWE[cell] = (byte) (buffer.getShort(index) & 0x0F);
			buffer.position(index+2);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getNSWE()
	 */
	@Override
	public byte getNSWE(int cell, byte layer) {
		if(doors[cell].get())
			return NSWE[cell];
		return NULL;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getHeight()
	 */
	@Override
	public short getHeight(int cell, byte layer) {
		return height[cell];
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getType()
	 */
	@Override
	public BlockType getType() {
		return BlockType.COMPLEX;
	}

	/**
	 * (non-Javadoc)
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#changeDoor(boolean)
	 */
	@Override
	public void changeDoor(int cell, byte layer, boolean isOpen) {
		doors[cell].set(isOpen);
	}
}
