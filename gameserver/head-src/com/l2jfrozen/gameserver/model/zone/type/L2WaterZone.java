/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.model.zone.type;

import java.util.Collection;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, true);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2NpcInstance)
		{
			final Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			// synchronized (character.getKnownList().getKnownPlayers())
			{
				for (final L2PcInstance player : plrs)
				{
					player.sendPacket(new NpcInfo((L2NpcInstance) character, player));
				}
			}
		}
		
		/*
		 * if (character instanceof L2PcInstance) { ((L2PcInstance)character).sendMessage("You entered water!");}
		 */
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, false);
		
		/*
		 * if (character instanceof L2PcInstance) { ((L2PcInstance)character).sendMessage("You exited water!"); }
		 */
		
		// TODO: update to only send speed status when that packet is known
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2NpcInstance)
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			// synchronized (character.getKnownList().getKnownPlayers())
			{
				for (final L2PcInstance player : plrs)
				{
					player.sendPacket(new NpcInfo((L2NpcInstance) character, player));
				}
			}
			
			plrs = null;
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
