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

/**
 *
 * @author: FBIagent / fixed by SqueezeD
 *
 */

package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;


public class AdminTvTEngine implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS =
	{ "admin_tvt", "admin_tvt_name", "admin_tvt_desc", "admin_tvt_join_loc", "admin_tvt_minlvl", "admin_tvt_maxlvl", "admin_tvt_npc", "admin_tvt_npc_pos", "admin_tvt_reward",
			"admin_tvt_reward_amount", "admin_tvt_team_add", "admin_tvt_team_remove", "admin_tvt_team_pos", "admin_tvt_team_color", "admin_tvt_join", "admin_tvt_teleport", "admin_tvt_start",
			"admin_tvt_abort", "admin_tvt_finish", "admin_tvt_sit", "admin_tvt_dump", "admin_tvt_save", "admin_tvt_load", "admin_tvt_jointime", "admin_tvt_eventtime", "admin_tvt_autoevent",
			"admin_tvt_minplayers", "admin_tvt_maxplayers", "admin_tvtkick" };

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){
			return false;
		}
		
		if(Config.GMAUDIT)
		{
			Logger _logAudit = Logger.getLogger("gmaudit");
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}
		
		if(command.equals("admin_tvt"))
			showMainPage(activeChar);
		else if(command.startsWith("admin_tvt_name "))
		{
			TvT._eventName = command.substring(15);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_desc "))
		{
			TvT._eventDesc = command.substring(15);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_minlvl "))
		{
			if(!TvT.checkMinLevel(Integer.valueOf(command.substring(17))))
				return false;
			TvT._minlvl = Integer.valueOf(command.substring(17));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_maxlvl "))
		{
			if(!TvT.checkMaxLevel(Integer.valueOf(command.substring(17))))
				return false;
			TvT._maxlvl = Integer.valueOf(command.substring(17));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_minplayers "))
		{
			TvT._minPlayers = Integer.valueOf(command.substring(21));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_maxplayers "))
		{
			TvT._maxPlayers = Integer.valueOf(command.substring(21));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_join_loc "))
		{
			TvT._joiningLocationName = command.substring(19);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_npc "))
		{
			TvT._npcId = Integer.valueOf(command.substring(14));
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_npc_pos"))
		{
			TvT.setNpcPos(activeChar);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_reward "))
		{
			TvT._rewardId = Integer.valueOf(command.substring(17));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_reward_amount "))
		{
			TvT._rewardAmount = Integer.valueOf(command.substring(24));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_jointime "))
		{
			TvT._joinTime = Integer.valueOf(command.substring(19));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_eventtime "))
		{
			TvT._eventTime = Integer.valueOf(command.substring(20));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_interval "))
		{
			TvT._intervalBetweenMatchs= Integer.valueOf(command.substring(20));
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_team_add "))
		{
			String teamName = command.substring(19);

			TvT.addTeam(teamName);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_team_remove "))
		{
			String teamName = command.substring(22);

			TvT.removeTeam(teamName);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_team_pos "))
		{
			String teamName = command.substring(19);

			TvT.setTeamPos(teamName, activeChar);
			showMainPage(activeChar);
		}
		else if(command.startsWith("admin_tvt_team_color "))
		{
			String[] params;

			params = command.split(" ");

			if(params.length != 3)
			{
				activeChar.sendMessage("Wrong usege: //tvt_team_color <colorHex> <teamName>");
				return false;
			}

			TvT.setTeamColor(command.substring(params[0].length() + params[1].length() + 2), Integer.decode("0x" + params[1]));
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_join"))
		{
			TvT.startJoin(activeChar);
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_teleport"))
		{
			TvT.teleportStart();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_start"))
		{
			TvT.startEvent(activeChar);
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_abort"))
		{
			activeChar.sendMessage("Aborting event");
			TvT.abortEvent();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_finish"))
		{
			TvT.finishEvent();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_sit"))
		{
			TvT.sit();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_load"))
		{
			TvT.loadData();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_autoevent"))
		{
			if(TvT._joinTime > 0 && TvT._eventTime > 0)
				TvT.autoEvent();
			else
				activeChar.sendMessage("Wrong usege: join time or event time invallid.");
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_save"))
		{
			TvT.saveData();
			showMainPage(activeChar);
		}
		else if(command.equals("admin_tvt_dump"))
			TvT.dumpData();
		else if(command.startsWith("admin_tvtkick"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String plyr = st.nextToken();
				L2PcInstance playerToKick = L2World.getInstance().getPlayer(plyr);
				if(playerToKick != null)
				{
					TvT.kickPlayerFromTvt(playerToKick);
					activeChar.sendMessage("You kicked " + playerToKick.getName() + " from the TvT.");
				}
				else
					activeChar.sendMessage("Wrong usege: //tvtkick <player>");
			}
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	public void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><title>Team vs Team</title><body>");

		replyMSG.append("<center><font color=\"LEVEL\">[TvT Engine]</font></center><br><br><br>");
		replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
		replyMSG.append("<table border=\"0\"><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Name\" action=\"bypass -h admin_tvt_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Description\" action=\"bypass -h admin_tvt_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Join Location\" action=\"bypass -h admin_tvt_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Max lvl\" action=\"bypass -h admin_tvt_maxlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Min lvl\" action=\"bypass -h admin_tvt_minlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Max players\" action=\"bypass -h admin_tvt_maxplayers $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Min players\" action=\"bypass -h admin_tvt_minplayers $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"NPC\" action=\"bypass -h admin_tvt_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"NPC Pos\" action=\"bypass -h admin_tvt_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Reward\" action=\"bypass -h admin_tvt_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Reward Amount\" action=\"bypass -h admin_tvt_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Join Time\" action=\"bypass -h admin_tvt_jointime $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Event Time\" action=\"bypass -h admin_tvt_eventtime $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Interval Time\" action=\"bypass -h admin_tvt_interval $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Team Add\" action=\"bypass -h admin_tvt_team_add $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Team Color\" action=\"bypass -h admin_tvt_team_color $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Team Pos\" action=\"bypass -h admin_tvt_team_pos $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Team Remove\" action=\"bypass -h admin_tvt_team_remove $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Join\" action=\"bypass -h admin_tvt_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Teleport\" action=\"bypass -h admin_tvt_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Start\" action=\"bypass -h admin_tvt_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Abort\" action=\"bypass -h admin_tvt_abort\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Finish\" action=\"bypass -h admin_tvt_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Sit Force\" action=\"bypass -h admin_tvt_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Dump\" action=\"bypass -h admin_tvt_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><br><table><tr>");
		replyMSG.append("<td width=\"100\"><button value=\"Save\" action=\"bypass -h admin_tvt_save\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Load\" action=\"bypass -h admin_tvt_load\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=\"100\"><button value=\"Auto Event\" action=\"bypass -h admin_tvt_autoevent\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table><br><br>");
		replyMSG.append("Current event:<br1>");
		replyMSG.append("Name:&nbsp;<font color=\"00FF00\">" + TvT._eventName + "</font><br1>");
		replyMSG.append("Description:&nbsp;<font color=\"00FF00\">" + TvT._eventDesc + "</font><br1>");
		replyMSG.append("Joining location name:&nbsp;<font color=\"00FF00\">" + TvT._joiningLocationName + "</font><br1>");
		replyMSG.append("Joining NPC ID:&nbsp;<font color=\"00FF00\">" + TvT._npcId + " on pos " + TvT._npcX + "," + TvT._npcY + "," + TvT._npcZ + "</font><br1>");
		replyMSG.append("Reward ID:&nbsp;<font color=\"00FF00\">" + TvT._rewardId + "</font><br1>");
		replyMSG.append("Reward Amount:&nbsp;<font color=\"00FF00\">" + TvT._rewardAmount + "</font><br><br>");
		replyMSG.append("Min lvl:&nbsp;<font color=\"00FF00\">" + TvT._minlvl + "</font><br>");
		replyMSG.append("Max lvl:&nbsp;<font color=\"00FF00\">" + TvT._maxlvl + "</font><br><br>");
		replyMSG.append("Min Players:&nbsp;<font color=\"00FF00\">" + TvT._minPlayers + "</font><br>");
		replyMSG.append("Max Players:&nbsp;<font color=\"00FF00\">" + TvT._maxPlayers + "</font><br><br>");
		replyMSG.append("Joining Time:&nbsp;<font color=\"00FF00\">" + TvT._joinTime + "</font><br>");
		replyMSG.append("Event Timer:&nbsp;<font color=\"00FF00\">" + TvT._eventTime + "</font><br><br>");
		replyMSG.append("Interval Time:&nbsp;<font color=\"00FF00\">" + TvT._intervalBetweenMatchs + "</font><br><br>");
		replyMSG.append("Current teams:<br1>");
		replyMSG.append("<center><table border=\"0\">");

		for(String team : TvT._teams)
		{
			replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>");

			if(Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
				replyMSG.append("&nbsp;(" + TvT.teamPlayersCount(team) + " joined)");
			else if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			{
				if(TvT._teleport || TvT._started)
					replyMSG.append("&nbsp;(" + TvT.teamPlayersCount(team) + " in)");
			}

			replyMSG.append("</td></tr><tr><td>");
			replyMSG.append(TvT._teamColors.get(TvT._teams.indexOf(team)));
			replyMSG.append("</td></tr><tr><td>");
			replyMSG.append(TvT._teamsX.get(TvT._teams.indexOf(team)) + ", " + TvT._teamsY.get(TvT._teams.indexOf(team)) + ", " + TvT._teamsZ.get(TvT._teams.indexOf(team)));
			replyMSG.append("</td></tr><tr><td width=\"60\"><button value=\"Remove\" action=\"bypass -h admin_tvt_team_remove " + team
					+ "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		}

		replyMSG.append("</table></center>");

		if(Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			if(!TvT._started)
			{
				replyMSG.append("<br1>");
				replyMSG.append(TvT._playersShuffle.size() + " players participating. Waiting to shuffle in teams(done on teleport)!");
				replyMSG.append("<br><br>");
			}
		}

		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}
