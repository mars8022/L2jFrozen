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

import java.util.List;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.multisell.L2Multisell;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExHeroList;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public class L2OlympiadManagerInstance extends L2FolkInstance
{
	private static Logger _logOlymp = Logger.getLogger(L2OlympiadManagerInstance.class.getName());

	private static final int GATE_PASS = Config.ALT_OLY_COMP_RITEM;

	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(player == null)
			return;

		if(command.startsWith("OlympiadDesc"))
		{
			int val = Integer.parseInt(command.substring(13, 14));
			String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
			suffix = null;
		}
		else if(command.startsWith("OlympiadNoble"))
		{
			if(!player.isNoble() || player.getClassId().getId() < 88)
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage reply;
			TextBuilder replyMSG;

			switch(val)
			{
				case 1:
					Olympiad.getInstance().unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					int[] array = Olympiad.getInstance().getWaitingList();

					if(array != null)
					{
						classed = array[0];
						nonClassed = array[1];
					}

					reply = new NpcHtmlMessage(getObjectId());
					replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("The number of people on the waiting list for " + "Grand Olympiad" + "<center>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<table width=270 border=0 bgcolor=\"000000\">" + "<tr>" + "<td align=\"left\">General</td>" + "<td align=\"right\">" + classed + "</td>" + "</tr>" + "<tr>" + "<td align=\"left\">Not class-defined</td>" + "<td align=\"right\">" + nonClassed + "</td>" + "</tr>" + "</table><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<button value=\"Back\" action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\" " + "width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");

					replyMSG.append("</body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 3:
					int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
					if(points >= 0)
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new TextBuilder("<html><body>");
						replyMSG.append("There are " + points + " Grand Olympiad " + "points granted for this event.<br><br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\">Return</a>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 4:
					if(player._active_boxes>1 && !Config.ALLOW_DUALBOX_OLY){
						
						boolean already_in_oly = false;
						
						List<String> players_in_boxes = player.active_boxes_characters;
						
						if(players_in_boxes!=null && players_in_boxes.size()>1)
							for(String character_name: players_in_boxes){
								L2PcInstance actual_player = L2World.getInstance().getPlayer(character_name);
								
								if(actual_player!=null && actual_player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(actual_player)){
									already_in_oly = true;
									break;
								}
							}
						
						if(already_in_oly)
							player.sendMessage("Dual Box not allowed in Olympiad Event");
						else
							Olympiad.getInstance().registerNoble(player, false);
						break;
					}else
						Olympiad.getInstance().registerNoble(player, false);
					break;
				case 5:
					if(player._active_boxes>1 && !Config.ALLOW_DUALBOX_OLY){
						
						boolean already_in_oly = false;
						
						List<String> players_in_boxes = player.active_boxes_characters;
						
						if(players_in_boxes!=null && players_in_boxes.size()>1)
							for(String character_name: players_in_boxes){
								L2PcInstance actual_player = L2World.getInstance().getPlayer(character_name);
								
								if(actual_player!=null && actual_player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(actual_player)){
									already_in_oly = true;
									break;
								}
							}
						
						if(already_in_oly)
							player.sendMessage("Dual Box not allowed in Olympiad Event");
						else
							Olympiad.getInstance().registerNoble(player, true);
						break;
					}else
						Olympiad.getInstance().registerNoble(player, true);
					break;
				case 6:
					int passes = Olympiad.getInstance().getNoblessePasses(player.getObjectId());
					if(passes > 0)
					{
						L2ItemInstance item = player.getInventory().addItem("Olympiad", GATE_PASS, passes, player, this);

						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendPacket(iu);
						iu = null;

						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addNumber(passes);
						sm.addItemName(item.getItemId());
						player.sendPacket(sm);
						item = null;
						sm = null;
					}
					else
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new TextBuilder("<html><body>");
						replyMSG.append("Not enough olympiad points, or not currently in Valdation Period");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 7:
					L2Multisell.getInstance().SeparateAndSend(102, player, false, getCastle().getTaxRate());
					break;
				default:
					_logOlymp.warning("Olympiad System: Couldnt send packet for request " + val);
					break;

			}
			reply = null;
			replyMSG = null;
		}
		else if(command.startsWith("Olympiad"))
		{
			int val = Integer.parseInt(command.substring(9, 10));

			NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
			TextBuilder replyMSG = new TextBuilder("<html><body>");

			switch(val)
			{
				case 1:
					if(player.isInFunEvent())
					{
						player.sendMessage("You can't do that while in a event");
						return;
					}
					String[] matches = Olympiad.getInstance().getMatchList();
					int stad;
					int showbattle;

					replyMSG.append("Grand Olympiad Games Overview<br><br>" + "* Caution: Please note, if you watch an Olympiad " + "game, the summoning of your Servitors or Pets will be " + "cancelled. Be careful.<br>");

					if(matches == null)
					{
						replyMSG.append("<br>There are no matches at the moment");
					}
					else
					{
						for(String matche : matches)
						{
							showbattle = Integer.parseInt(matche.substring(1, 2));
							stad = Integer.parseInt(matche.substring(4, 5));
							if(showbattle == 1)
							{
								replyMSG.append("<br><a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + stad + "\">" + matche + "</a>");
							}
						}
					}
					replyMSG.append("</body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 2:
					// for example >> Olympiad 1_88
					int classId = Integer.parseInt(command.substring(11));
					if(classId >= 88)
					{
						replyMSG.append("<center>Grand Olympiad Ranking");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");

						List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
						if(names.size() != 0)
						{
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");

							int index = 1;

							for(String name : names)
							{
								replyMSG.append("<tr>");
								replyMSG.append("<td align=\"left\">" + index + "</td>");
								replyMSG.append("<td align=\"right\">" + name + "</td>");
								replyMSG.append("</tr>");
								index++;
							}

							replyMSG.append("</table>");
						}

						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						replyMSG.append("</center>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 3:
					int id = Integer.parseInt(command.substring(11));
					if(player.isInFunEvent())
					{
						player.sendMessage("You can't do that while in a event");
					}
					else
					{
						Olympiad.getInstance().addSpectator(id, player);
					}
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				default:
					_logOlymp.warning("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
			reply = null;
			replyMSG = null;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_FILE;

		filename += "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";

		if(filename.equals(Olympiad.OLYMPIAD_HTML_FILE + "noble_desc0.htm"))
		{
			filename = Olympiad.OLYMPIAD_HTML_FILE + "noble_main.htm";
		}

		showChatWindow(player, filename);
		filename = null;
	}
}
