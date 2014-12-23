/* L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.managers;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.zone.type.L2ArenaZone;

public class ArenaManager
{
	// =========================================================
	private static ArenaManager _instance;
	private static final Logger LOGGER = Logger.getLogger(ArenaManager.class);
	
	public static final ArenaManager getInstance()
	{
		if (_instance == null)
		{
			LOGGER.info("Initializing ArenaManager");
			_instance = new ArenaManager();
		}
		return _instance;
	}
	
	// =========================================================
	
	// =========================================================
	// Data Field
	private FastList<L2ArenaZone> _arenas;
	
	// =========================================================
	// Constructor
	public ArenaManager()
	{
	}
	
	// =========================================================
	// Property - Public
	
	public void addArena(final L2ArenaZone arena)
	{
		if (_arenas == null)
		{
			_arenas = new FastList<>();
		}
		
		_arenas.add(arena);
	}
	
	public final L2ArenaZone getArena(final L2Character character)
	{
		if (_arenas != null)
		{
			for (final L2ArenaZone temp : _arenas)
				if (temp.isCharacterInZone(character))
					return temp;
		}
		
		return null;
	}
	
	public final L2ArenaZone getArena(final int x, final int y, final int z)
	{
		if (_arenas != null)
		{
			for (final L2ArenaZone temp : _arenas)
				if (temp.isInsideZone(x, y, z))
					return temp;
		}
		
		return null;
	}
}
