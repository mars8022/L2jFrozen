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

import com.l2jfrozen.gameserver.model.L2CommandChannel;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 */
public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
			return;
		
		if (_response == 1)
		{
			boolean newCc = false;
			if (!requestor.getParty().isInCommandChannel())
			{
				new L2CommandChannel(requestor); // Create new CC
				newCc = true;
			}
			
			requestor.getParty().getCommandChannel().addParty(player.getParty());
			if (!newCc)
				player.sendPacket(new SystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		}
		else
			requestor.sendPacket(new SystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION).addString(player.getName()));
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0E RequestExAcceptJoinMPCC";
	}
	
}
