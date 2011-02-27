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
package com.l2jfrozen.gameserver.powerpak.RaidInfo;

import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * @author Enzo
 */
public class RaidInfoHandler implements ICustomByPassHandler
{
	private static Logger _log = Logger.getLogger(RaidInfoHandler.class.getName());

	private static final int NPC_ID = 93000;

	private static final String [] _BYPASSCMD = {"raidinfo"};
	
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		
		if(player==null)
			return;

		if(!(player.getTarget() instanceof L2NpcInstance))
			return;
		
		if( ((L2NpcInstance)player.getTarget()).getNpcId()!=NPC_ID)
			return;
		
		sendInfo(player);
		
	}
	
	private static void sendInfo(L2PcInstance activeChar)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Grand Boss Info</title><body><br><center>");
		tb.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>");

		for(int boss : Config.RAID_INFO_IDS_LIST)
		{
			String name = "";
			L2NpcTemplate template = null;
			if((template = NpcTable.getInstance().getTemplate(boss)) != null){
				name = template.getName();
			}else{
				_log.warning("[RaidInfoHandler][sendInfo] Raid Boss with ID "+boss+" is not defined into NpcTable");
				continue;
			}
			 
			StatsSet actual_boss_stat = null;
			GrandBossManager.getInstance().getStatsSet(boss);
			long delay = 0;
			
			if(NpcTable.getInstance().getTemplate(boss).type.equals("L2RaidBoss")){
				actual_boss_stat=RaidBossSpawnManager.getInstance().getStatsSet(boss);
				if(actual_boss_stat!=null)
					delay = actual_boss_stat.getLong("respawnTime");
			}else if(NpcTable.getInstance().getTemplate(boss).type.equals("L2GrandBoss")){
				actual_boss_stat=GrandBossManager.getInstance().getStatsSet(boss);
				if(actual_boss_stat!=null)
					delay = actual_boss_stat.getLong("respawn_time");
			}else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
				tb.append("<font color=\"00C3FF\">" + name + "</color>: " + "<font color=\"9CC300\">Is Alive</color>"+"<br1>");
			}
			else
			{
				int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
				int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
				int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
				tb.append("<font color=\"00C3FF\">" + name + "</color>" + "<font color=\"FFFFFF\">" +" " + "Respawn in :</color>" + " " + " <font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color><br1>");
			}
		}

		tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
		tb.append("</center></body></html>");
		
		
		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		
		activeChar.sendPacket(msg);
		
	}
}
