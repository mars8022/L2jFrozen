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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */

/**
 * Fortress Siege Event
 * @author Darki699
 * @comment: So many fortresses, I hate to see them go to waste ;]
 */

package interlude.gameserver.handler.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.lib.Rnd;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.L2OpenEvents.FortressSiege;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Fortress Siege Event
 * @author Darki699
 * @comment: So many fortresses, I hate to see them go to waste ;]
 */

public class AdminFortressSiegeEngine implements IAdminCommandHandler {

 private static final String[] ADMIN_COMMANDS = {"admin_fos","admin_fos_pg2","admin_fos_pg3",
                                           "admin_fos_name", "admin_fos_desc", "admin_fos_join_loc","admin_fos_tele1","admin_fos_tele2",
                                           "admin_fos_minlvl", "admin_fos_maxlvl","admin_fos_door6","admin_fos_tele3","admin_fos_tele4",
                                           "admin_fos_npc", "admin_fos_npc_pos", "admin_fos_door1","admin_fos_door2","admin_fos_door3",
                                           "admin_fos_reward", "admin_fos_reward_amount","admin_fos_door4","admin_fos_door5",
                                           "admin_fos_teamname", "admin_fos_team_pos", "admin_fos_team_color","admin_fos_team_flag","admin_fos_team_cent",
                                           "admin_fos_join", "admin_fos_teleport", "admin_fos_start", "admin_fos_abort", "admin_fos_finish",
                                           "admin_fos_sit", "admin_fos_dump", "admin_fos_save", "admin_fos_load", "admin_fos_jointime",
                                           "admin_fos_eventtime", "admin_fos_autoevent","admin_fos_minplayers","admin_fos_maxplayers"};

 private static final int REQUIRED_LEVEL = 100;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        try{
    	if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) {
			return false;
		}
        if (command.equals("admin_fos")) {
			showMainPage(activeChar);
		} else if (command.startsWith("admin_fos_name ")){
            FortressSiege._eventName = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_tele1")){
        	if (!(FortressSiege._teamsX.get(0)==0 && FortressSiege._teamsY.get(0)==0 && FortressSiege._teamsZ.get(0)==0)) {
				activeChar.teleToLocation(FortressSiege._teamsX.get(0), FortressSiege._teamsY.get(0), FortressSiege._teamsZ.get(0));
			}
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_tele2")){
        	if (!(FortressSiege._teamsX.get(1)==0 && FortressSiege._teamsY.get(1)==0 && FortressSiege._teamsZ.get(1)==0)) {
				activeChar.teleToLocation(FortressSiege._teamsX.get(1), FortressSiege._teamsY.get(1), FortressSiege._teamsZ.get(1));
			}
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_tele3")){
        	if (!(FortressSiege._flagX==0 && FortressSiege._flagY==0 && FortressSiege._flagZ==0)) {
				activeChar.teleToLocation(FortressSiege._flagX, FortressSiege._flagY, FortressSiege._flagZ);
			}
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_tele4")){
        	if (!(FortressSiege._npcX==0 && FortressSiege._npcY==0 && FortressSiege._npcZ==0)) {
				activeChar.teleToLocation(FortressSiege._npcX, FortressSiege._npcY, FortressSiege._npcZ);
			}
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_desc ")){
            FortressSiege._eventDesc = command.substring(15);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_minlvl ")){
            if (!FortressSiege.checkMinLevel(Integer.valueOf(command.substring(17)))) {
				return false;
			}
            FortressSiege._minlvl = Integer.valueOf(command.substring(17));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_door")){
            L2Object target = activeChar.getTarget();
            if (target==null) {
				activeChar.sendMessage("Nothing targeted!");
			} else if (target instanceof L2DoorInstance){
            	int doorId = ((L2DoorInstance)target).getDoorId();
            	if (doorId>0) {
					FortressSiege._door[Integer.valueOf(command.substring(14))-1]=doorId;
				}
            } else {
				activeChar.sendMessage("Incorrect target.");
			}
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_team_flag")){
            FortressSiege._flagX = activeChar.getX();
            FortressSiege._flagY = activeChar.getY();
            FortressSiege._flagZ = activeChar.getZ();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_team_cent")){
            FortressSiege.eventCenterX = activeChar.getX();
            FortressSiege.eventCenterY = activeChar.getY();
            FortressSiege.eventCenterZ = activeChar.getZ();
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_maxlvl ")){
            if (!FortressSiege.checkMaxLevel(Integer.valueOf(command.substring(17)))) {
				return false;
			}
            FortressSiege._maxlvl = Integer.valueOf(command.substring(17));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_minplayers ")){
            FortressSiege._minPlayers = Integer.valueOf(command.substring(21));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_maxplayers ")){
            FortressSiege._maxPlayers = Integer.valueOf(command.substring(21));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_join_loc ")){
            FortressSiege._joiningLocationName = command.substring(19);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_npc ")){
            FortressSiege._npcId = Integer.valueOf(command.substring(14));
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_npc_pos")){
            FortressSiege._npcX = activeChar.getX();
            FortressSiege._npcY = activeChar.getY();
            FortressSiege._npcZ = activeChar.getZ();
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_reward ")){
            FortressSiege._rewardId = Integer.valueOf(command.substring(17));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_reward_amount ")){
            FortressSiege._rewardAmount = Integer.valueOf(command.substring(24));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_jointime ")){
            FortressSiege._joinTime = Integer.valueOf(command.substring(19));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_eventtime ")){
            FortressSiege._eventTime = Integer.valueOf(command.substring(20));
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_teamname ")){
            String[] params;
            params = command.split(" ");
            if (params.length < 3){
            	activeChar.sendMessage("Wrong usage: //fos_teamname <1 or 2> <team name>");
            	return false;
            }
            FortressSiege._teams.set(Integer.valueOf(params[1])-1, params[2]);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_team_pos ")){
            String teamName = command.substring(19);
            FortressSiege.setTeamPos(teamName, activeChar);
            showMainPage(activeChar);
        }
        else if (command.startsWith("admin_fos_team_color ")){
            String[] params;
            params = command.split(" ");
            if (params.length < 3){
                activeChar.sendMessage("Wrong usage: //fos_team_color <colorHex> <team name>");
                return false;
            }
            FortressSiege.setTeamColor(command.substring(params[0].length()+params[1].length()+2), Integer.decode("0x" + params[1]));
            showMainPage(activeChar);
        }
        else if(command.equals("admin_fos_join"))
        {
        	FortressSiege.startJoin(activeChar);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_teleport"))
        {
            if (FortressSiege._joining) {
				FortressSiege.teleportStart();
			}
            showMainPage(activeChar);
        }
        else if(command.equals("admin_fos_start"))
        {
        	if (FortressSiege._joining) {
				FortressSiege.teleportStart();
			} else if (FortressSiege._teleport) {
				FortressSiege.startEvent(activeChar);
			}
            showMainPage(activeChar);
        }
        else if(command.equals("admin_fos_abort"))
        {
            activeChar.sendMessage("Aborting event");
            FortressSiege.abortEvent();
            showMainPage(activeChar);
        }
        else if(command.equals("admin_fos_finish"))
        {
            FortressSiege.finishEvent();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_sit"))
        {
            FortressSiege.sit();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_load")){
        	showSiegeLoadPage(activeChar,false);
        }
        else if (command.startsWith("admin_fos_load ")){
        	String siegeName = command.substring(15);
        	FortressSiege.loadData(siegeName);
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_autoevent"))
        {
        	if (FortressSiege._joining || FortressSiege._started || FortressSiege._teleport){
        		activeChar.sendMessage("Event is already in progress. Wait until the event ends or Abort it.");
        		return false;
        	}
        	if(FortressSiege._joinTime>0 && FortressSiege._eventTime>0){
        		FortressSiege.autoEvent();
        	} else {
				activeChar.sendMessage("Wrong usage: join time or event time invallid.");
			}
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_save"))
        {
            FortressSiege.saveData();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_dump")){
            FortressSiege.dumpData();
            showMainPage(activeChar);
        }
        else if (command.equals("admin_fos_pg2")){
        	showEditEventPage(activeChar);
        }
        else if (command.equals("admin_fos_pg3")){
        	showControlEventPage(activeChar);
        }
        return true;
        }catch(Throwable t){
        	activeChar.sendMessage("The command was not used correctly:"+t.toString());
        	return false;
        }
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return level >= REQUIRED_LEVEL;
    }

    public void showSiegeLoadPage(L2PcInstance activeChar,boolean autoLoad){
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("<center><font color=\"LEVEL\">[FortressSiege Engine by Darki699]</font></center><br><br><br>");
    	java.sql.Connection con = null;
        try{
            PreparedStatement statement;
            ResultSet rs;
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("Select * from fortress_siege");
            rs = statement.executeQuery();
            String _eventName="";
            while(rs.next()){
                _eventName = rs.getString("eventName");
                if (autoLoad && Rnd.get(100)<10){
                	statement.close();
                	break;
                }
                rs.getString("eventDesc");rs.getString("joiningLocation");rs.getInt("minlvl");
                rs.getInt("maxlvl");rs.getInt("npcId");rs.getInt("npcX");rs.getInt("npcY");
                rs.getInt("npcZ");rs.getInt("npcHeading");rs.getInt("rewardId");rs.getInt("rewardAmount");
                rs.getInt("joinTime");rs.getInt("eventTime");rs.getInt("minPlayers");rs.getInt("maxPlayers");
                rs.getInt("centerX");rs.getInt("centerY");rs.getInt("centerZ");rs.getString("team1Name");
                rs.getInt("team1X");rs.getInt("team1Y");rs.getInt("team1Z");rs.getInt("team1Color");
                rs.getString("team2Name");rs.getInt("team2X");rs.getInt("team2Y");rs.getInt("team2Z");
                rs.getInt("team2Color");rs.getInt("flagX");rs.getInt("flagY");rs.getInt("flagZ");
                if (FortressSiege._eventName!=null && FortressSiege._eventName.equals(_eventName)) {
					replyMSG.append("<a action=\"bypass -h admin_fos_load "+_eventName+"\"><font color=\"FF0000\">"+_eventName+"</font><font color=\"LEVEL\"><-- *loaded*</font></a><br1>");
				} else {
					replyMSG.append("<a action=\"bypass -h admin_fos_load "+_eventName+"\"><font color=\"00FF00\">"+_eventName+"</font></a><br1>");
				}
            }
            statement.close();
            if (autoLoad){
            	FortressSiege.loadData(_eventName);
                if (activeChar!=null) {
					showMainPage(activeChar);
				}
            	return;
            }
            replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_fos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            if (activeChar!=null) {
				activeChar.sendPacket(adminReply);
			}
        }catch (Exception e){
            System.out.println("Exception: AdminFortressSiegeEngine.showSiegeLoadPage: " + e.getMessage());
        }finally {try { con.close(); } catch (Exception e) {}}

    }

    public void showEditEventPage(L2PcInstance activeChar){
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        try{
            replyMSG.append("<center><font color=\"LEVEL\">[FortressSiege Engine by Darki699]</font></center><br><br><br>");
            replyMSG.append("<table><tr><td><edit var=\"input1\" width=\"125\"></td><td><edit var=\"input2\" width=\"125\"></td></tr></table>");
            replyMSG.append("<table border=\"0\"><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Name\" action=\"bypass -h admin_fos_name $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Description\" action=\"bypass -h admin_fos_desc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Join Location\" action=\"bypass -h admin_fos_join_loc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Max level\" action=\"bypass -h admin_fos_maxlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Min level\" action=\"bypass -h admin_fos_minlvl $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Max players\" action=\"bypass -h admin_fos_maxplayers $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Min players\" action=\"bypass -h admin_fos_minplayers $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"NPC\" action=\"bypass -h admin_fos_npc $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"NPC Pos\" action=\"bypass -h admin_fos_npc_pos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Reward\" action=\"bypass -h admin_fos_reward $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Reward Amount\" action=\"bypass -h admin_fos_reward_amount $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Join Time\" action=\"bypass -h admin_fos_jointime $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Event Time\" action=\"bypass -h admin_fos_eventtime $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table>Position:<br1><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Siege Flag\" action=\"bypass -h admin_fos_team_flag\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Central\" action=\"bypass -h admin_fos_team_cent\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Team Name\" action=\"bypass -h admin_fos_teamname $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Team Color\" action=\"bypass -h admin_fos_team_color $input1 $input2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Team Pos\" action=\"bypass -h admin_fos_team_pos $input1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td></td></tr></table><br>");
            replyMSG.append("Doors: (target a door)<br1><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Outer #1\" action=\"bypass -h admin_fos_door5\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Outer #2\" action=\"bypass -h admin_fos_door6\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Inner #1\" action=\"bypass -h admin_fos_door1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Inner #2\" action=\"bypass -h admin_fos_door2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Inner #3\" action=\"bypass -h admin_fos_door3\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Inner #4\" action=\"bypass -h admin_fos_door4\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br>");
            replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_fos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
            }catch(Throwable t){
            	try{
                    replyMSG.append("</body></html>");
                    adminReply.setHtml(replyMSG.toString());
                    activeChar.sendPacket(adminReply);
            	}
            	catch(Throwable e){return;}
            }
    }

    public void showControlEventPage(L2PcInstance activeChar){
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        try{
            replyMSG.append("<center><font color=\"LEVEL\">[FortressSiege Engine by Darki699]</font></center><br><br><br>");
            replyMSG.append("<table border=\"0\"><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Join\" action=\"bypass -h admin_fos_join\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Teleport\" action=\"bypass -h admin_fos_teleport\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Start\" action=\"bypass -h admin_fos_start\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Abort\" action=\"bypass -h admin_fos_abort\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Finish\" action=\"bypass -h admin_fos_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Sit Force\" action=\"bypass -h admin_fos_sit\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Dump\" action=\"bypass -h admin_fos_dump\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Save\" action=\"bypass -h admin_fos_save\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Load\" action=\"bypass -h admin_fos_load\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Auto Event\" action=\"bypass -h admin_fos_autoevent\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br><br><table><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Tele>Team1\" action=\"bypass -h admin_fos_tele1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Tele>Team2\" action=\"bypass -h admin_fos_tele2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=\"100\"><button value=\"Tele>Artif\" action=\"bypass -h admin_fos_tele3\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr><tr>");
            replyMSG.append("<td width=\"100\"><button value=\"Tele>NPC\" action=\"bypass -h admin_fos_tele4\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table><br><center><button value=\"Back\" action=\"bypass -h admin_fos\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        catch(Throwable t){
        	try{
                replyMSG.append("</body></html>");
                adminReply.setHtml(replyMSG.toString());
                activeChar.sendPacket(adminReply);
        	}
        	catch(Throwable e){return;}
        }
    }

    public void showMainPage(L2PcInstance activeChar){
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        try{
        replyMSG.append("<center><font color=\"LEVEL\">[FortressSiege Engine by Darki699]</font></center><br><br><br>");
        replyMSG.append("<table border=\"0\"><tr>");
        replyMSG.append("<td width=\"100\"><button value=\"Edit\" action=\"bypass -h admin_fos_pg2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td>");
        replyMSG.append("<td width=\"100\"><button value=\"Control\" action=\"bypass -h admin_fos_pg3\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table><br><br>");
        replyMSG.append("Current event...<br1>");
        replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + FortressSiege._eventName + "</font><br1>");
        replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + FortressSiege._eventDesc + "</font><br1>");
        replyMSG.append("    ... joining location name:&nbsp;<font color=\"00FF00\">" + FortressSiege._joiningLocationName + "</font><br1>");
        replyMSG.append("    ... joining NPC ID:&nbsp;<font color=\"00FF00\">" + FortressSiege._npcId + " on pos " + FortressSiege._npcX + "," + FortressSiege._npcY + "," + FortressSiege._npcZ + "</font>");
        replyMSG.append("<td width=\"100\"><button value=\"Tele>NPC\" action=\"bypass -h admin_fos_tele4\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><br1>");
        replyMSG.append("    ... reward ID  :&nbsp;<font color=\"00FF00\">" + FortressSiege._rewardId + "</font><br1>");
        if ( ItemTable.getInstance().getTemplate(FortressSiege._rewardId) != null) {
			replyMSG.append("    ... reward Item:&nbsp;<font color=\"00FF00\">" +  ItemTable.getInstance().getTemplate(FortressSiege._rewardId).getName() + "</font><br1>");
		} else {
			replyMSG.append("    ... reward Item:&nbsp;<font color=\"00FF00\">(unknown)</font><br1>");
		}
        replyMSG.append("    ... reward Amount:&nbsp;<font color=\"00FF00\">" + FortressSiege._rewardAmount + "</font><br><br>");
        replyMSG.append("    ... Min lvl:&nbsp;<font color=\"00FF00\">" + FortressSiege._minlvl + "</font><br1>");
        replyMSG.append("    ... Max lvl:&nbsp;<font color=\"00FF00\">" + FortressSiege._maxlvl + "</font><br>");
        replyMSG.append("    ... Min Players:&nbsp;<font color=\"00FF00\">" + FortressSiege._minPlayers + "</font><br1>");
        replyMSG.append("    ... Max Players:&nbsp;<font color=\"00FF00\">" + FortressSiege._maxPlayers + "</font><br>");
        replyMSG.append("    ... Joining Time:&nbsp;<font color=\"00FF00\">" + FortressSiege._joinTime + "</font><br1>");
        replyMSG.append("    ... Event Time  :&nbsp;<font color=\"00FF00\">" + FortressSiege._eventTime + "</font><br>");
        replyMSG.append("Current teams:<br1>");
        replyMSG.append("<center><table border=\"0\">");

        if (FortressSiege._teams!=null && !FortressSiege._teams.isEmpty()) {
			for (String team : FortressSiege._teams){
			    replyMSG.append("<tr><td width=\"100\">Name:<font color=\"LEVEL\">" + team + "</font>");
			    if (Config.FortressSiege_EVEN_TEAMS.equals("NO") || Config.FortressSiege_EVEN_TEAMS.equals("BALANCE")) {
					replyMSG.append(" (" + FortressSiege._teamPlayersCount.get(FortressSiege._teams.indexOf(team)) + " joined)");
				} else if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE")){
			        if (FortressSiege._teleport || FortressSiege._started) {
						replyMSG.append(" (" + FortressSiege._teamPlayersCount.get(FortressSiege._teams.indexOf(team)) + " in)");
					}
			    }
			    replyMSG.append("</td></tr><tr><td>");
			    String c = Integer.toHexString(FortressSiege._teamColors.get(FortressSiege._teams.indexOf(team)));
			    while (c.length()<6) {
					c="0"+c;
				}
			    replyMSG.append("Color: <font color=\"00FF00\">0x"+c.toUpperCase()+"</font><font color=\""+c+"\"> 8D </font>");
			    replyMSG.append("</td></tr><tr><td>");
			    replyMSG.append("Position: <font color=\"00FF00\">("+FortressSiege._teamsX.get(FortressSiege._teams.indexOf(team)) + ", " + FortressSiege._teamsY.get(FortressSiege._teams.indexOf(team)) + ", " + FortressSiege._teamsZ.get(FortressSiege._teams.indexOf(team))+")</font>");
			    replyMSG.append("</td></tr><tr>");
			    if (team.equals(FortressSiege._teams.get(0))) {
					replyMSG.append("<td width=\"100\"><button value=\"Tele>Team1\" action=\"bypass -h admin_fos_tele1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
			    if (team.equals(FortressSiege._teams.get(1))) {
					replyMSG.append("<td width=\"100\"><button value=\"Tele>Team2\" action=\"bypass -h admin_fos_tele2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
			}
		}
        replyMSG.append("<tr><td>Artifact:  <font color=\"00FF00\">("+FortressSiege._flagX + ", " + FortressSiege._flagY+ ", " + FortressSiege._flagZ+")</font></td></tr>");
        replyMSG.append("<tr><td width=\"100\"><button value=\"Tele>Artif\" action=\"bypass -h admin_fos_tele3\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<tr><td>Center Room: <font color=\"00FF00\">("+FortressSiege.eventCenterX + ", " + FortressSiege.eventCenterY+ ", " + FortressSiege.eventCenterZ+")</font></td></tr>");
        replyMSG.append("<tr><td>Fortress Door Ids: <font color=\"00FF00\">("+FortressSiege._door[4] + ", " + FortressSiege._door[5]+")</font></td></tr>");
        replyMSG.append("<tr><td>Inner Door Ids : </td></tr>");
        replyMSG.append("<tr><td><font color=\"00FF00\">("+FortressSiege._door[0] + ", " + FortressSiege._door[1] + "),</td></tr>");
        replyMSG.append("<tr><td>("+FortressSiege._door[2] + ", " + FortressSiege._door[3]+")</font></td></tr>");
        replyMSG.append("</table></center>");
        if (Config.FortressSiege_EVEN_TEAMS.equals("SHUFFLE")){
            if (FortressSiege._joining){
                replyMSG.append("<br1>");
                replyMSG.append(FortressSiege._playersShuffle.size() + " players participating. Waiting to shuffle teams (done on teleport)");
                replyMSG.append("<br><br>");
            }
        }
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
        }catch(Throwable t){
        	try{
                replyMSG.append("</body></html>");
                adminReply.setHtml(replyMSG.toString());
                activeChar.sendPacket(adminReply);
        	}
        	catch(Throwable e){return;}
        }
    }
}
