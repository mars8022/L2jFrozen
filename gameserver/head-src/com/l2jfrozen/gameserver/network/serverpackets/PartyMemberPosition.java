/*
 * L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;

import javolution.util.FastMap;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	Map<Integer, Location> locations = new FastMap<>();
	
	public PartyMemberPosition(final L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(final L2Party party)
	{
		locations.clear();
		for (final L2PcInstance member : party.getPartyMembers())
		{
			if (member == null)
				continue;
			locations.put(member.getObjectId(), new Location(member));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa7);
		writeD(locations.size());
		
		for (final Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			final Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] a7 PartyMemberPosition";
	}
	
}
