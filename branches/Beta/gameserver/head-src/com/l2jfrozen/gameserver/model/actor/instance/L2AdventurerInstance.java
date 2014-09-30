/*
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
package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ExQuestInfo;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 * @author LBaldi
 */
public class L2AdventurerInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2AdventurerInstance.class.getName());

	public L2AdventurerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(command.startsWith("npcfind_byid"))
		{
			try
			{
				int bossId = Integer.parseInt(command.substring(12).trim());
				switch(RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId))
				{
					case ALIVE:
					case DEAD:
						L2Spawn spawn = RaidBossSpawnManager.getInstance().getSpawns().get(bossId);
						player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
						spawn = null;
						break;
					case UNDEFINED:
						player.sendMessage("This Boss isn't in game - notify L2JFrozen Datapack Dev Team");
						break;
				}
			}
			catch(NumberFormatException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.warning("Invalid Bypass to Server command parameter.");
			}
		}
		else if(command.startsWith("raidInfo"))
		{
			int bossLevel = Integer.parseInt(command.substring(9).trim());
			String filename = "data/html/adventurer_guildsman/raid_info/info.htm";

			if(bossLevel != 0)
			{
				filename = "data/html/adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
			}

			showChatWindow(player, bossLevel, filename);
			filename = null;
		}
		else if(command.equalsIgnoreCase("questlist"))
		{
			player.sendPacket(new ExQuestInfo());
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if(val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "data/html/adventurer_guildsman/" + pom + ".htm";
	}

	private void showChatWindow(L2PcInstance player, int bossLevel, String filename)
	{
		showChatWindow(player, filename);
	}
}
