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
package com.l2jfrozen.gameserver.model.zone.type;

import javolution.util.FastList;

import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.util.random.Rnd;

/**
 * A Town zone
 * @author durgus
 */
public class L2TownZone extends L2ZoneType
{
	private String _townName;
	private int _townId;
	private int _redirectTownId;
	private int _taxById;
	private boolean _noPeace;
	private final FastList<int[]> _spawnLoc;
	
	public L2TownZone(final int id)
	{
		super(id);
		
		_taxById = 0;
		_spawnLoc = new FastList<>();
		
		// Default to Giran
		_redirectTownId = 9;
		
		// Default peace zone
		_noPeace = false;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		if (name.equals("name"))
		{
			_townName = value;
		}
		else if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("redirectTownId"))
		{
			_redirectTownId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
		}
		else if (name.equals("noPeace"))
		{
			_noPeace = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	public void setSpawnLocs(final Node node)
	{
		final int ai[] = new int[3];
		
		Node node1 = node.getAttributes().getNamedItem("X");
		
		if (node1 != null)
		{
			ai[0] = Integer.parseInt(node1.getNodeValue());
		}
		
		node1 = node.getAttributes().getNamedItem("Y");
		
		if (node1 != null)
		{
			ai[1] = Integer.parseInt(node1.getNodeValue());
		}
		
		node1 = node.getAttributes().getNamedItem("Z");
		
		if (node1 != null)
		{
			ai[2] = Integer.parseInt(node1.getNodeValue());
		}
		_spawnLoc.add(ai);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if (((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
				return;
			
			if (Config.ZONE_DEBUG)
				((L2PcInstance) character).sendMessage("You entered " + _townName);
		}
		
		if (!_noPeace && Config.ZONE_TOWN != 2)
		{
			character.setInsideZone(L2Character.ZONE_PEACE, true);
		}
		
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (!_noPeace)
		{
			character.setInsideZone(L2Character.ZONE_PEACE, false);
		}
		
		if (Config.ZONE_DEBUG)
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage("You left " + _townName);
			}
		
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
	
	/**
	 * Returns this town zones name
	 * @return
	 */
	@Deprecated
	public String getName()
	{
		return _townName;
	}
	
	/**
	 * Returns this zones town id (if any)
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/**
	 * Gets the id for this town zones redir town
	 * @return
	 */
	@Deprecated
	public int getRedirectTownId()
	{
		return _redirectTownId;
	}
	
	/**
	 * Returns this zones spawn location
	 * @return
	 */
	public final int[] getSpawnLoc()
	{
		int ai[] = new int[3];
		
		ai = _spawnLoc.get(Rnd.get(_spawnLoc.size()));
		
		return ai;
	}
	
	/**
	 * Returns this town zones castle id
	 * @return
	 */
	public final int getTaxById()
	{
		return _taxById;
	}
}
