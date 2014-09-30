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
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.L2CommandChannel;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		96
	};

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(activeChar==null)
			return false;
		
		if(activeChar.isInParty())
		{
			if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel())
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();
				L2Party party = activeChar.getParty();
				channel.removeParty(party);

				party.getLeader().sendPacket(new SystemMessage(SystemMessageId.LEFT_COMMAND_CHANNEL));
				channel.broadcastToChannelMembers(new SystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addString(party.getLeader().getName()));

				party = null;
				channel = null;
				return true;
			}
		}
		return false;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
