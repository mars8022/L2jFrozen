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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class handles following admin commands: - show_moves - show_teleport - teleport_to_character - move_to -
 * teleport_character
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
			"admin_show_moves",
			"admin_show_moves_other",
			"admin_show_teleport",
			"admin_teleport_to_character",
			"admin_teleportto",
			"admin_move_to",
			"admin_teleport_character",
			"admin_recall",
			"admin_walk",
			"admin_recall_npc",
			"admin_gonorth",
			"admin_gosouth",
			"admin_goeast",
			"admin_gowest",
			"admin_goup",
			"admin_godown",
			"admin_instant_move",
			"admin_sendhome",
			"admin_tele",
			"admin_teleto",
			"admin_recall_party",
	};

	private enum CommandEnum
	{
		admin_show_moves,
		admin_show_moves_other,
		admin_show_teleport,
		admin_teleport_to_character,
		admin_teleportto,
		admin_move_to,
		admin_teleport_character,
		admin_recall,
		admin_walk,
		admin_recall_npc,
		admin_gonorth,
		admin_gosouth,
		admin_goeast,
		admin_gowest,
		admin_goup,
		admin_godown,
		admin_instant_move,
		admin_sendhome,
		admin_tele,
		admin_teleto,
		admin_recall_party
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());		
		//Alt+g window (instant move)
		if (command.equals("admin_instant_move"))
			{
				activeChar.setTeleMode(1);
			}
		//Send player to town (alt+g)
		else if (command.startsWith("admin_sendhome"))
							       {
							           try
							           {
							               String[] param = command.split(" ");
							               if (param.length != 2)
							               {
							                   activeChar.sendMessage("Usage: //sendhome <playername>");
							                   return false;
							               }
							               String targetName = param[1];
						               L2PcInstance player = L2World.getInstance().getPlayer(targetName);
						               if (player != null)
							               {
							                   Location loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Town);
						                   player.setInstanceId(0);
							                   player.teleToLocation(loc, true);
							               }
							               else
							                   activeChar.sendMessage("User is not online.");
							           }
							           catch (StringIndexOutOfBoundsException e)
							           {
		      }
		   }
		else if(comm == null)
			return false;
		
		switch(comm)
		{
			case admin_show_moves:{
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
				return true;
			} 
			case admin_show_moves_other:{
				AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
				return true;
			} 
			case admin_show_teleport:{
				showTeleportCharWindow(activeChar);
				return true;
			} 
			case admin_teleport_to_character:{
				teleportToCharacter(activeChar, activeChar.getTarget());
				return true;
			} 
			case admin_teleportto:{
				
				String val = "";
				
				if(st.hasMoreTokens()){
					
					val = st.nextToken();
					
				}else{
					activeChar.sendMessage("Usage: //teleportto <char_name>");
					return false;
				}
				
				L2PcInstance player = L2World.getInstance().getPlayer(val);
				
				if(player==null){
					activeChar.sendMessage("ATTENTION: char_name must be valid character");
					activeChar.sendMessage("Usage: //teleportto <char_name>");
					return false;
				}
				
				teleportToCharacter(activeChar, player);
				val = null;
				player = null;
				return true;
				
			} 
			case admin_recall_party:{
			
				if (activeChar.isGM() && !(activeChar.getAccessLevel().getLevel() == 1))
				{return false;}
				
				String val = "";
				
				if(st.hasMoreTokens()){
					
					val = st.nextToken();
					
				}else{
					activeChar.sendMessage("Usage: //recall_party <party_leader_name>");
					return false;
				}
				
				if(val.equals("")){
					activeChar.sendMessage("Usage: //recall_party <party_leader_name>");
					return false;
				}

				L2PcInstance player = L2World.getInstance().getPlayer(val);	
				
				if(player==null){
					activeChar.sendMessage("ATTENTION: party_leader_name must be valid character");
					activeChar.sendMessage("//recall_party <party_leader_name>");
					return false;
				}
				
				if(player.getParty() == null){
					activeChar.sendMessage("The player must have a party");
					activeChar.sendMessage("//recall_party <party_leader_name>");
					return false;
				}

				if(!player.getParty().isLeader(player)){
					activeChar.sendMessage("The player must be the party_leader");
					activeChar.sendMessage("//recall_party <party_leader_name>");
					return false;
				}				
				
				for(L2PcInstance partyMember : player.getParty().getPartyMembers())
				{					
					partyMember.sendMessage("You party teleported by Admin.");
					teleportTo(partyMember, activeChar.getX(), activeChar.getY(), activeChar.getZ()); 				
				}
				return true;
		
			}
			case admin_move_to:{
				
				int x = 0;
				int y = 0;
				int z = 0;
				
				
				if(st.countTokens()==3){
					
					try{
						
						String x_str = st.nextToken();
						String y_str = st.nextToken();
						String z_str = st.nextToken();
						
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
						
					}catch(NumberFormatException e){
						activeChar.sendMessage("Usage: //move_to <coordinates>");
						AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
						return false;
					}
					
				}else{
					activeChar.sendMessage("Usage: //move_to <coordinates>");
					AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
					return false;
				}
				
				if(x == 0 && y == 0){
					activeChar.sendMessage("Usage: //move_to <valid_coordinates>");
					AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
					return false;
				}
				
				teleportTo(activeChar, x,y,z);
				return true;
				
			} 
			case admin_teleport_character:{
				
				int x = 0;
				int y = 0;
				int z = 0;
				
				if(st.countTokens()==3){
					
					try{
						
						String x_str = st.nextToken();
						String y_str = st.nextToken();
						String z_str = st.nextToken();
						
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
						
					}catch(NumberFormatException e){
						activeChar.sendMessage("Usage: //teleport_character <coordinates>");
						showTeleportCharWindow(activeChar);
						return false;
					}
					
				}else{
					activeChar.sendMessage("Usage: //teleport_character <coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				
				if(x == 0 && y == 0){
					activeChar.sendMessage("Usage: //teleport_character <valid_coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				
				L2Object target = null;
				L2PcInstance player = null;
				
				if((target = activeChar.getTarget()) != null && target instanceof L2PcInstance){
					
					player = (L2PcInstance) target ;
					
				}
				
				if(player==null){
					activeChar.sendMessage("Select valid player");
					activeChar.sendMessage("Usage: //teleport_character <valid_coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				
				teleportTo(player, x,y,z);
				return true;
				
			} 
			case admin_recall:{
				
				String val = "";
				
				if(st.hasMoreTokens()){
					
					val = st.nextToken();
					
				}else{
					activeChar.sendMessage("Usage: //recall <char_name>");
					return false;
				}
				
				if(val.equals("")){
					activeChar.sendMessage("Usage: //recall <char_name>");
					return false;
				}
				
				L2PcInstance player = L2World.getInstance().getPlayer(val);
				
				if(player==null){
					activeChar.sendMessage("ATTENTION: char_name must be valid character");
					activeChar.sendMessage("Usage: //recall <char_name>");
					return false;
				}
				
				teleportTo(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				return true;
				
			} 
			case admin_walk:{
				
				int x = 0;
				int y = 0;
				int z = 0;
				
				if(st.countTokens()==3){
					
					try{
						
						String x_str = st.nextToken();
						String y_str = st.nextToken();
						String z_str = st.nextToken();
						
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
						
					}catch(NumberFormatException e){
						activeChar.sendMessage("Usage: //walk <coordinates>");
						return false;
					}
					
				}else{
					activeChar.sendMessage("Usage: //walk <coordinates>");
					return false;
				}
				
				if(x == 0 && y == 0){
					activeChar.sendMessage("Usage: //walk <valid_coordinates>");
					return false;
				}
				
				L2CharPosition pos = new L2CharPosition(x, y, z, 0);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
				return true;
				
			} 
			case admin_recall_npc:{
				recallNPC(activeChar);
			} 
			break;
			case admin_gonorth:
			case admin_gosouth:
			case admin_goeast:
			case admin_gowest:
			case admin_goup:
			case admin_godown:{
				
				int intVal = 150;
				int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();

				try
				{
					String val = command.substring(8);
					st = new StringTokenizer(val);
					String dir = st.nextToken();

					if(st.hasMoreTokens())
					{
						intVal = Integer.parseInt(st.nextToken());
					}

					if(dir.equals("east"))
					{
						x += intVal;
					}
					else if(dir.equals("west"))
					{
						x -= intVal;
					}
					else if(dir.equals("north"))
					{
						y -= intVal;
					}
					else if(dir.equals("south"))
					{
						y += intVal;
					}
					else if(dir.equals("up"))
					{
						z += intVal;
					}
					else if(dir.equals("down"))
					{
						z -= intVal;
					}

					activeChar.teleToLocation(x, y, z, false);
					showTeleportWindow(activeChar);

					dir = null;
					st = null;
					val = null;
					
					return true;
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
					return false;
				}
			}
			case admin_tele:{
				showTeleportWindow(activeChar);
			} 
			break;
			case admin_teleto:{
				
				String val = "";
				
				if(st.hasMoreTokens()){
					
					val = st.nextToken();
					
				}

				if(val.equals("")){
					activeChar.setTeleMode(1);
				}else if(val.equals("r")){
					activeChar.setTeleMode(2);
				}else if(val.equals("end")){
					activeChar.setTeleMode(0);
				}else{
					activeChar.sendMessage("Defined mode not allowed..");
					return false;
				}
				
				return true;
				
			} 
		
		}

		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void teleportTo(L2PcInstance activeChar, int x, int y, int z)
	//private void teleportTo(L2PcInstance activeChar, String Cords)
	{
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.teleToLocation(x, y, z, false);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		sm.addString("You have been teleported to " + x+" "+y+" "+z);
		activeChar.sendPacket(sm);

		sm = null;
		
	}

	private void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "move.htm");
	}

	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;

		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		replyMSG.append("Co-ordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Co-ordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Co-ordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);

		adminReply = null;
		replyMSG = null;
		player = null;
		target = null;
	}

	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		L2PcInstance player = null;
		//L2NpcInstance npc = null;
		
		if(target != null && target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}/*else if(target != null && target instanceof L2NpcInstance){
			npc = (L2NpcInstance) target;
		}*/else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}

		if(player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			int x = player.getX();
			int y = player.getY();
			int z = player.getZ();

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);

			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}/*
		else if(npc!=null)
		{
			int x = npc.getX();
			int y = npc.getY();
			int z = npc.getZ();

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);

			activeChar.sendMessage("You have teleported to npc " + npc.getName() + ".");
		}*/
	}

	private void recallNPC(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();

		if(obj != null && obj instanceof L2NpcInstance)
		{
			L2NpcInstance target = (L2NpcInstance) obj;

			int monsterTemplate = target.getTemplate().npcId;

			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);

			if(template1 == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}

			L2Spawn spawn = target.getSpawn();

			if(spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}

			int respawnTime = spawn.getRespawnDelay()/1000;
			boolean custom_boss_spawn = spawn.is_customBossInstance();
				
			target.deleteMe();
			spawn.stopRespawn();
			
			//check to avoid that recalling a custom raid it will be saved into database
			SpawnTable.getInstance().deleteSpawn(spawn, !custom_boss_spawn);

			try
			{
				//L2MonsterInstance mob = new L2MonsterInstance(monsterTemplate, template1);

				spawn = new L2Spawn(template1);
				spawn.setLocx(activeChar.getX());
				spawn.setLocy(activeChar.getY());
				spawn.setLocz(activeChar.getZ());
				spawn.setAmount(1);
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				SpawnTable.getInstance().addNewSpawn(spawn, !custom_boss_spawn);
				spawn.init();

				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Created " + template1.name + " on " + target.getObjectId() + ".");
				activeChar.sendPacket(sm);
				sm = null;

				if(Config.DEBUG)
				{
					_log.fine("Spawn at X=" + spawn.getLocx() + " Y=" + spawn.getLocy() + " Z=" + spawn.getLocz());
					_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") moved NPC " + target.getObjectId());
				}

				spawn = null;
				template1 = null;
				target = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Target is not in game.");
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}

		obj = null;
	}
}
