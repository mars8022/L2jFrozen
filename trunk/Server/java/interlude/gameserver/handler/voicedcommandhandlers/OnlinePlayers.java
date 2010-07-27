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
package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * this class... shows the amount of online players to anyone who calls it.
 */
public class OnlinePlayers implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "online" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("online"))
		{
			showPlayers(activeChar, target);
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}

	public void showPlayers(L2PcInstance player, String target)
	{
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("======<Players Online>======");
			player.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Players online:");
			sm.addNumber(L2World.getInstance().getAllPlayers().size());
			player.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("=======================");
			player.sendPacket(sm);
		}
	}
}