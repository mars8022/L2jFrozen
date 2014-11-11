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
package com.l2jfrozen.gameserver.model.actor.appearance;

public class PcAppearance
{
	// =========================================================
	// Data Field
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	private boolean _sex; // Female true(1)
	/** true if the player is invisible */
	private boolean _invisible = false;
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _nameColor = 0xFFFFFF;
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _titleColor = 0xFFFF77;
	
	// =========================================================
	// Constructor
	public PcAppearance(final byte Face, final byte HColor, final byte HStyle, final boolean Sex)
	{
		_face = Face;
		_hairColor = HColor;
		_hairStyle = HStyle;
		_sex = Sex;
	}
	
	// =========================================================
	// Method - Public
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	public final byte getFace()
	{
		return _face;
	}
	
	/**
	 * @param value
	 */
	public final void setFace(final int value)
	{
		_face = (byte) value;
	}
	
	public final byte getHairColor()
	{
		return _hairColor;
	}
	
	/**
	 * @param value
	 */
	public final void setHairColor(final int value)
	{
		_hairColor = (byte) value;
	}
	
	public final byte getHairStyle()
	{
		return _hairStyle;
	}
	
	/**
	 * @param value
	 */
	public final void setHairStyle(final int value)
	{
		_hairStyle = (byte) value;
	}
	
	public final boolean getSex()
	{
		return _sex;
	}
	
	/**
	 * @param isfemale
	 */
	public final void setSex(final boolean isfemale)
	{
		_sex = isfemale;
	}
	
	public void setInvisible()
	{
		_invisible = true;
	}
	
	public void setVisible()
	{
		_invisible = false;
	}
	
	public boolean getInvisible()
	{
		return _invisible;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(final int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(final int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public void setTitleColor(final int red, final int green, final int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
}
