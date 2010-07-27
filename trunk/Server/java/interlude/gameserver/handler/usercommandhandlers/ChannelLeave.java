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
package interlude.gameserver.handler.usercommandhandlers;

import interlude.gameserver.handler.IUserCommandHandler;
import interlude.gameserver.model.L2CommandChannel;
import interlude.gameserver.model.L2Party;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 96 };

	/**
	 * @see interlude.gameserver.handler.IUserCommandHandler#useUserCommand(int, interlude.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0]) {
			return false;
		}
		if (activeChar.isInParty())
		{
			if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel())
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();
				L2Party party = activeChar.getParty();
				channel.removeParty(party);
				SystemMessage sm = SystemMessage.sendString("Your party has left the CommandChannel.");
				party.broadcastToPartyMembers(sm);
				sm = SystemMessage.sendString(party.getPartyMembers().get(0).getName() + "'s party has left the CommandChannel.");
				channel.broadcastToChannelMembers(sm);
				return true;
			}
		}
		return false;
	}

	/**
	 * @see interlude.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
