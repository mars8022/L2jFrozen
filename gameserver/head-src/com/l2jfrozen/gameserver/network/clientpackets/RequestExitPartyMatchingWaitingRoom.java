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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch) this is just a trigger : no data
 * @author -Wooden-
 */
public final class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		if (_activeChar == null)
			return;
		
		PartyMatchWaitingList.getInstance().removePlayer(_activeChar);
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:17 RequestExitPartyMatchingWaitingRoom";
	}
	
}
