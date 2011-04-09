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


/**
 * Flat-block.
 * Only block height.
 * 
 * @author nameless, 02.02.2011
 */
public final class FlatBlock extends CompiledBlock {
	private final static byte NSWE = 15;
	private final short height;

	public FlatBlock(short height) {
		this.height = height;
	}

	@Override
	public byte getNSWE(int cell, byte layer) {
		return NSWE;
	}

	@Override
	public short getHeight(int cell, byte layer) {
		return height;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.geo.blocks.CompiledBlock#getType()
	 */
	@Override
	public BlockType getType() {
		return BlockType.FLAT;
	}

	/**
	 * does exist ;)
	 */
	@Override
	public void changeDoor(int cell, byte layer, boolean isOpen) {}
}
