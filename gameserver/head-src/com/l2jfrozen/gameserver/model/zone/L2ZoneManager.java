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
package com.l2jfrozen.gameserver.model.zone;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class manages all zones for a given world region
 * @author durgus
 */
public class L2ZoneManager
{
	private final Logger LOGGER = Logger.getLogger(L2ZoneManager.class);
	private final FastList<L2ZoneType> _zones;
	
	/**
	 * The Constructor creates an initial zone list use registerNewZone() / unregisterZone() to change the zone list
	 */
	public L2ZoneManager()
	{
		_zones = new FastList<>();
	}
	
	/**
	 * Register a new zone object into the manager
	 * @param zone
	 */
	public void registerNewZone(final L2ZoneType zone)
	{
		_zones.add(zone);
	}
	
	/**
	 * Unregister a given zone from the manager (e.g. dynamic zones)
	 * @param zone
	 */
	public void unregisterZone(final L2ZoneType zone)
	{
		_zones.remove(zone);
	}
	
	public void revalidateZones(final L2Character character)
	{
		if (Config.ZONE_DEBUG && character != null && character instanceof L2PcInstance && character.getName() != null)
			LOGGER.debug("ZONE: Revalidating Zone for character: " + character.getName());
		
		for (final L2ZoneType e : _zones)
		{
			if (e != null)
			{
				e.revalidateInZone(character);
			}
		}
	}
	
	public void removeCharacter(final L2Character character)
	{
		for (final L2ZoneType e : _zones)
		{
			if (e != null)
			{
				e.removeCharacter(character);
			}
		}
	}
	
	public void onDeath(final L2Character character)
	{
		for (final L2ZoneType e : _zones)
		{
			if (e != null)
			{
				e.onDieInside(character);
			}
		}
	}
	
	public void onRevive(final L2Character character)
	{
		for (final L2ZoneType e : _zones)
		{
			if (e != null)
			{
				e.onReviveInside(character);
			}
		}
	}
	
	/**
	 * @return
	 */
	public FastList<L2ZoneType> getZones()
	{
		return _zones;
	}
}
