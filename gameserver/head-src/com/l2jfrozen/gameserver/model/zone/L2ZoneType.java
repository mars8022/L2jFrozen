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
package com.l2jfrozen.gameserver.model.zone;

import javolution.util.FastMap;

import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Abstract base class for any zone type Handles basic operations
 * 
 * @author durgus
 */
public abstract class L2ZoneType
{
	private final int _id;
	protected L2ZoneForm _zone;
	protected FastMap<Integer, L2Character> _characterList;

	/** Parameters to affect specific characters */
	private boolean _checkAffected;

	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;

	protected L2ZoneType(int id)
	{
		_id = id;
		_characterList = new FastMap<Integer, L2Character>().shared();

		_checkAffected = false;

		_minLvl = 0;
		_maxLvl = 0xFF;

		_classType = 0;

		_race = null;
		_class = null;
	}

	public int getId()
	{
		return _id;
	}

	/**
	 * Setup new parameters for this zone
	 * 
	 * @param type
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;

		// Minimum leve
		if(name.equals("affectedLvlMin"))
		{
			_minLvl = Integer.parseInt(value);
		}
		// Maximum level
		else if(name.equals("affectedLvlMax"))
		{
			_maxLvl = Integer.parseInt(value);
		}
		// Affected Races
		else if(name.equals("affectedRace"))
		{
			// Create a new array holding the affected race
			if(_race == null)
			{
				_race = new int[1];
				_race[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_race.length + 1];

				int i = 0;

				for(; i < _race.length; i++)
				{
					temp[i] = _race[i];
				}

				temp[i] = Integer.parseInt(value);

				_race = temp;
			}
		}
		// Affected classes
		else if(name.equals("affectedClassId"))
		{
			// Create a new array holding the affected classIds
			if(_class == null)
			{
				_class = new int[1];
				_class[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_class.length + 1];

				int i = 0;

				for(; i < _class.length; i++)
				{
					temp[i] = _class[i];
				}

				temp[i] = Integer.parseInt(value);

				_class = temp;
			}
		}
		// Affected class type
		else if(name.equals("affectedClassType"))
		{
			if(value.equals("Fighter"))
			{
				_classType = 1;
			}
			else
			{
				_classType = 2;
			}
		}
	}

	public void setSpawnLocs(Node node1)
	{}

	/**
	 * Checks if the given character is affected by this zone
	 * 
	 * @param character
	 * @return
	 */
	private boolean isAffected(L2Character character)
	{
		// Check lvl
		if(character.getLevel() < _minLvl || character.getLevel() > _maxLvl)
			return false;

		if(character instanceof L2PcInstance)
		{
			// Check class type
			if(_classType != 0)
			{
				if(((L2PcInstance) character).isMageClass())
				{
					if(_classType == 1)
						return false;
				}
				else if(_classType == 2)
					return false;
			}

			// Check race
			if(_race != null)
			{
				boolean ok = false;

				for(int element : _race)
				{
					if(((L2PcInstance) character).getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}

				if(!ok)
					return false;
			}

			// Check class
			if(_class != null)
			{
				boolean ok = false;

				for(int clas : _class)
				{
					if(((L2PcInstance) character).getClassId().ordinal() == clas)
					{
						ok = true;
						break;
					}
				}

				if(!ok)
					return false;
			}
		}
		return true;
	}

	/**
	 * Set the zone for this L2ZoneType Instance
	 * 
	 * @param zone
	 */
	public void setZone(L2ZoneForm zone)
	{
		_zone = zone;
	}

	/**
	 * Returns this zones zone form
	 * 
	 * @param zone
	 * @return
	 */
	public L2ZoneForm getZone()
	{
		return _zone;
	}

	/**
	 * Checks if the given coordinates are within the zone
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}

	/**
	 * Checks if the given obejct is inside the zone.
	 * 
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return _zone.isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public double getDistanceToZone(int x, int y)
	{
		return _zone.getDistanceToZone(x, y);
	}

	public double getDistanceToZone(L2Object object)
	{
		return _zone.getDistanceToZone(object.getX(), object.getY());
	}

	public void revalidateInZone(L2Character character)
	{
		// If the character cant be affected by this zone return
		if(_checkAffected)
		{
			if(!isAffected(character))
				return;
		}
		
		if(Config.DEBUG &&  character !=null && character.getName()!=null && character.getName().equalsIgnoreCase("pippo")){
			
			System.out.println("Character "+character.getName() +" has coords: ");
			System.out.println("	X: "+character.getX());
			System.out.println("	Y: "+character.getY());
			System.out.println("	Z: "+character.getZ());
			System.out.println(" -  is inside zone "+_id+"?: "+_zone.isInsideZone(character.getX(), character.getY(), character.getZ()));
			
		}
		

		// If the object is inside the zone...
		if(_zone.isInsideZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if(!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if(_characterList.containsKey(character.getObjectId()))
			{
				_characterList.remove(character.getObjectId());
				onExit(character);
			}
		}
		
		if(Config.DEBUG){
			System.out.println(" -  players inside zone "+_id+": ");
			for(L2Character actual: _characterList.values()){
				if(actual instanceof L2PcInstance)
					System.out.println("	 -  "+actual.getName());
			}
		}
		
		
	}

	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 * 
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if(_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			onExit(character);
		}
	}

	/**
	 * Will scan the zones char list for the character
	 * 
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}

	protected abstract void onEnter(L2Character character);

	protected abstract void onExit(L2Character character);

	protected abstract void onDieInside(L2Character character);

	protected abstract void onReviveInside(L2Character character);

	/**
	 * Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (_characterList.isEmpty())
			return;
		
		for (L2Character character : _characterList.values())
		{
			if (character instanceof L2PcInstance)
				character.sendPacket(packet);
		}
	}
	
	public FastMap<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}
	
}
