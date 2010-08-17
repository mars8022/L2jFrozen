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

import interlude.Config;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * VoicedCommand ".autoherbs" Handler Allow player to select: use autoloot of Herbs or not Syntax : .autoherbs_on/off Author : Sergey V Chursin
 */
public class AutoLootHerbs implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "autoherbs_on", "autoherbs_off" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		// is command enabled?
		if (!Config.ALLOW_AUTOHERBS_CMD) {
			return false;
		}
		// check command syntax and do work
		if (command.startsWith("autoherbs_on"))
		{
			activeChar.setAutoLootHerbs(1);
		}
		else if (command.startsWith("autoherbs_off"))
		{ // auto loot off
			activeChar.setAutoLootHerbs(0);
		}
		else
		{ // show cmd syntax
			activeChar.sendMessage("AutoHerbs Syntax:");
			activeChar.sendMessage("  Enable auto loot herbs: .autoherbs_on");
			activeChar.sendMessage("  Disable auto loot herbs: .autoherbs_off");
		}
		// work's done - exit
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
