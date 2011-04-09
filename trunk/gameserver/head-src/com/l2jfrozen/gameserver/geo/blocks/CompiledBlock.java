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
 * 
 * 
 * @author nameless, 02.02.2011
 */
public abstract class CompiledBlock {
	//заглушка ^_^
	protected final static byte NULL = 0;
	
	public abstract byte getNSWE(int cell, byte layer);
	public abstract short getHeight(int cell, byte layer);
	public abstract void changeDoor(int cell, byte layer, boolean isOpen);
	public abstract BlockType getType();

	public byte getLayers(int cell) {
		return -1;
	}
	
	public final short getHeight() {
		return getHeight(NULL, NULL);
	}
	
	public final short getHeight(int cell) {
		return getHeight(cell, NULL);
	}
	
	public final byte getNSWE(int cell) {
		return getNSWE(cell, NULL);
	}
	
	public final void changeDoor(int cell, boolean isOpen) {
		changeDoor(cell, NULL, isOpen);
	}
	
	public final void changeDoor(boolean isOpen) {
		changeDoor(NULL, NULL, isOpen);
	}
}
