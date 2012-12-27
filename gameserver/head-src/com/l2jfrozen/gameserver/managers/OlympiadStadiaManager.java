/* This program is free software; you can redistribute it and/or modify
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

import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.zone.type.L2OlympiadStadiumZone;

import javolution.util.FastList;

public class OlympiadStadiaManager
{
    protected static Logger _log = Logger.getLogger(OlympiadStadiaManager.class.getName());

    // =========================================================
    private static OlympiadStadiaManager _instance;
    public static final OlympiadStadiaManager getInstance()
    {
        if (_instance == null)
        {
            System.out.println("Initializing OlympiadStadiaManager");
            _instance = new OlympiadStadiaManager();
        }
        return _instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private FastList<L2OlympiadStadiumZone> _olympiadStadias;
    
    // =========================================================
    // Constructor
    public OlympiadStadiaManager()
    {
    }

    // =========================================================
    // Property - Public
    
    public void addStadium(L2OlympiadStadiumZone arena)
    {
    	if (_olympiadStadias == null)
    		_olympiadStadias = new FastList<L2OlympiadStadiumZone>();
    	
    	_olympiadStadias.add(arena);
    }
    
    public final L2OlympiadStadiumZone getStadium(L2Character character)
    {
    	for (L2OlympiadStadiumZone temp : _olympiadStadias)
    		if (temp.isCharacterInZone(character)) return temp;
    	
    	return null;
    }
    
    public final L2OlympiadStadiumZone getStadiumByLoc(int x, int y, int z)
	{
		if(_olympiadStadias != null)
		{
			for(L2OlympiadStadiumZone temp : _olympiadStadias)
				if(temp.isInsideZone(x, y, z))
					return temp;
		}

		return null;
	}
    
    @Deprecated
    public final L2OlympiadStadiumZone getOlympiadStadiumById(int olympiadStadiumId)
    {
    	for (L2OlympiadStadiumZone temp : _olympiadStadias)
    		if (temp.getStadiumId() == olympiadStadiumId) return temp;
        return null;
    }

}
