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
package interlude.gameserver.instancemanager;

import javolution.util.FastList;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.zone.type.L2ArenaZone;

public class ArenaManager
{
	// =========================================================
	private static ArenaManager _instance;

	public static final ArenaManager getInstance()
	{
		if (_instance == null)
		{
			System.out.println("Initializing ArenaManager");
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
	public void addArena(L2ArenaZone arena)
	{
		if (_arenas == null) {
			_arenas = new FastList<L2ArenaZone>();
		}
		_arenas.add(arena);
	}

	public final L2ArenaZone getArena(L2Character character)
	{
		for (L2ArenaZone temp : _arenas) {
			if (temp.isCharacterInZone(character)) {
				return temp;
			}
		}
		return null;
	}
}
