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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jfrozen.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public class AdminEditChar implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	
	private static String[] ADMIN_COMMANDS =
	{
			"admin_changename", // changes char name
			"admin_edit_character",
			"admin_current_player",
			"admin_nokarma",
			"admin_setkarma",
			"admin_character_list", //same as character_info, kept for compatibility purposes
			"admin_character_info", //given a player name, displays an information window
			"admin_show_characters",
			"admin_find_character",
			"admin_find_dualbox",
			"admin_find_ip", // find all the player connections from a given IPv4 number
			"admin_find_account", //list all the characters from an account (useful for GMs w/o DB access)
			"admin_save_modifications", //consider it deprecated...
			"admin_rec",
			"admin_setclass",
			"admin_settitle",
			"admin_setsex",
			"admin_setcolor",
			"admin_fullfood",
			"admin_remclanwait",
			"admin_setcp",
			"admin_sethp",
			"admin_setmp",
			"admin_setchar_cp",
			"admin_setchar_hp",
			"admin_setchar_mp"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
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
		*/

		if(command.equals("admin_current_player"))
		{
			showCharacterInfo(activeChar, null);
		}
		else if(command.startsWith("admin_character_list") || command.startsWith("admin_character_info"))
		{
			try
			{
				String val = command.substring(21);
				L2PcInstance target = L2World.getInstance().getPlayer(val);

				if(target != null)
				{
					showCharacterInfo(activeChar, target);
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
				}

				val = null;
				target = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Usage: //character_info <player_name>");
			}
		}
		else if(command.startsWith("admin_show_characters"))
		{
			try
			{
				String val = command.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
				val = null;
			}
			catch(StringIndexOutOfBoundsException e)
			{
				//Case of empty page number
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if(command.startsWith("admin_find_character"))
		{
			try
			{
				String val = command.substring(21);
				findCharacter(activeChar, val);
				val = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				//Case of empty character name
				SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString("You didnt enter a character name to find.");
				activeChar.sendPacket(sm);
				sm = null;

				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_find_ip"))
		{
			try
			{
				String val = command.substring(14);
				findCharactersPerIp(activeChar, val);
				val = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				//Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				String val = command.substring(19);
				multibox = Integer.parseInt(val);
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			findDualbox(activeChar, multibox);
		}
		else if(command.startsWith("admin_find_account"))
		{
			try
			{
				String val = command.substring(19);
				findCharactersPerAccount(activeChar, val);
				val = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				//Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.equals("admin_edit_character"))
		{
			editCharacter(activeChar);
		}
		else if(command.equals("admin_nokarma"))
		{
			setTargetKarma(activeChar, 0);
		}
		else if(command.startsWith("admin_setkarma"))
		{
			try
			{
				String val = command.substring(15);
				int karma = Integer.parseInt(val);
				val = null;
				//if (activeChar == activeChar.getTarget()) L2EMU_DISABLE Visor123 fix
				setTargetKarma(activeChar, karma);
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
		else if(command.startsWith("admin_save_modifications"))
		{
			try
			{
				String val = command.substring(24);
				adminModifyCharacter(activeChar, val);
				val = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				//Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		}
		else if(command.equals("admin_rec"))
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;

			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
				return false;

			target = null;

			player.setRecomHave(player.getRecomHave() + 1);
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
			sm.addString("You have been recommended by a GM");
			player.sendPacket(sm);
			player.broadcastUserInfo();
			player = null;
			sm = null;
		}
		else if(command.startsWith("admin_rec"))
		{
			try
			{
				String val = command.substring(10);

				int recVal = Integer.parseInt(val);

				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;

				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
					return false;

				target = null;

				player.setRecomHave(player.getRecomHave() + recVal);
				SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString("You have been recommended by a GM");
				player.sendPacket(sm);
				player.broadcastUserInfo();
				player = null;
				sm = null;
				val = null;
			}
			catch(NumberFormatException nfe)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					nfe.printStackTrace();
				
				activeChar.sendMessage("You must specify the number of recommendations to add.");
			}
		}
		else if(command.startsWith("admin_setclass"))
		{
			try
			{
				String val = command.substring(15);

				int classidval = Integer.parseInt(val);

				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;

				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
					return false;

				boolean valid = false;

				for(ClassId classid : ClassId.values())
					if(classidval == classid.getId())
					{
						valid = true;
					}

				if(valid && player.getClassId().getId() != classidval)
				{
					player.setClassId(classidval);

					if(!player.isSubClassActive())
					{
						player.setBaseClass(classidval);
					}

					String newclass = player.getTemplate().className;
					player.store();

					if(player != activeChar)
					{
						player.sendMessage("A GM changed your class to " + newclass);
					}

					player.broadcastUserInfo();
					activeChar.sendMessage(player.getName() + " changed to " + newclass);

					newclass = null;
				}
				else
				{
					activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
				}
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				AdminHelpPage.showSubMenuPage(activeChar, "charclasses.htm");
			}
		}
		else if(command.startsWith("admin_settitle"))
		{
			String val = "";
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			L2NpcInstance npc = null;

			if(target == null)
			{
				player = activeChar;
			}
			else if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else if(target instanceof L2NpcInstance)
			{
				npc = (L2NpcInstance) target;
			}
			else
				return false;

			target = null;

			if(st.hasMoreTokens())
			{
				val = st.nextToken();
			}
			while(st.hasMoreTokens())
			{
				val += " " + st.nextToken();
			}

			st = null;

			if(player != null)
			{
				player.setTitle(val);
				if(player != activeChar)
				{
					player.sendMessage("Your title has been changed by a GM");
				}
				player.broadcastTitleInfo();
			}
			else if(npc != null)
			{
				npc.setTitle(val);
				npc.updateAbnormalEffect();
			}

			val = null;
		}
		else if(command.startsWith("admin_changename"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				String val = st.nextToken();
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;

				String oldName = null;

				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
					oldName = player.getName();

					L2World.getInstance().removeFromAllPlayers(player);
					player.setName(val);
					player.store();
					L2World.getInstance().addToAllPlayers(player);

					player.sendMessage("Your name has been changed by a GM.");
					player.broadcastUserInfo();

					if(player.isInParty())
					{
						// Delete party window for other party members
						player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
						for(L2PcInstance member : player.getParty().getPartyMembers())
						{
							// And re-add
							if(member != player)
							{
								member.sendPacket(new PartySmallWindowAll());
							}
						}
					}
					if(player.getClan() != null)
					{
						player.getClan().broadcastClanStatus();
					}

					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				else if(target instanceof L2NpcInstance)
				{
					L2NpcInstance npc = (L2NpcInstance) target;
					oldName = npc.getName();
					npc.setName(val);
					npc.updateAbnormalEffect();
				}
				if(oldName == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				}
				else
				{
					activeChar.sendMessage("Name changed from " + oldName + " to " + val);
				}

				st = null;
				target = null;
				player = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				//Case of empty character name
				activeChar.sendMessage("Usage: //changename new_name_for_target");
			}
		}
		else if(command.startsWith("admin_setsex"))
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;

			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
				return false;

			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed by a GM");
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe(player.getX(), player.getY(), player.getZ());

			player = null;
			target = null;
		}
		else if(command.startsWith("admin_setcolor"))
		{
			L2Object target = activeChar.getTarget();

			if(target == null)
			{
				activeChar.sendMessage("You have to select a player!");
				return false;
			}

			if(!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("Your target is not a player!");
				return false;
			}

			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				String val = st.nextToken();
				L2PcInstance player = (L2PcInstance) target;
				player.getAppearance().setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM");
				player.broadcastUserInfo();
				st = null;
				player = null;
			}
			catch(Exception e)
			{ //Case of empty color or invalid hex string
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Usage: //setcolor [color in HEX]");
			}

			target = null;
		}
		else if(command.startsWith("admin_fullfood"))
		{
			L2Object target = activeChar.getTarget();

			if(target instanceof L2PetInstance)
			{
				L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
				targetPet = null;
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			}

			target = null;
		}
		else if(command.equals("admin_remclanwait"))
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;

			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
				return false;

			target = null;

			if(player.getClan() == null)
			{
				player.setClanJoinExpiryTime(0);
				player.sendMessage("A GM Has reset your clan wait time, You may now join another clan.");
				activeChar.sendMessage("You have reset " + player.getName() + "'s wait time to join another clan.");
			}
			else
			{
				activeChar.sendMessage("Sorry, but " + player.getName() + " must not be in a clan. Player must leave clan before the wait limit can be reset.");
			}

			player = null;
		}

		StringTokenizer st2 = new StringTokenizer(command, " ");
		String cmand = st2.nextToken(); // get command

		if(cmand.equals("admin_setcp"))
		{
			int cp = 0;

			try
			{
				cp = Integer.parseInt(st2.nextToken());

			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}

			activeChar.getStatus().setCurrentCp(cp);
		}

		if(cmand.equals("admin_sethp"))
		{
			int hp = 0;

			try
			{
				hp = Integer.parseInt(st2.nextToken());
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}

			activeChar.getStatus().setCurrentHp(hp);
		}

		if(cmand.equals("admin_setmp"))
		{
			int mp = 0;

			try
			{
				mp = Integer.parseInt(st2.nextToken());

			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}
			activeChar.getStatus().setCurrentMp(mp);
		}

		if(cmand.equals("admin_setchar_cp"))
		{
			int cp = 0;

			try
			{
				cp = Integer.parseInt(st2.nextToken());
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}

			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentCp(cp);
				pc = null;
			}
			else if(activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentCp(cp);
				pet = null;
			}
			else
			{
				activeChar.getStatus().setCurrentCp(cp);
			}
		}

		if(cmand.equals("admin_setchar_hp"))
		{
			int hp = 0;

			try
			{
				hp = Integer.parseInt(st2.nextToken());
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}

			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentHp(hp);
				pc = null;
			}
			else if(activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentHp(hp);
				pet = null;
			}
			else
			{
				activeChar.getStatus().setCurrentHp(hp);
			}
		}

		if(cmand.equals("admin_setchar_mp"))
		{
			int mp = 0;

			try
			{
				mp = Integer.parseInt(st2.nextToken());
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "" + e.getMessage());
			}

			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentMp(mp);
				pc = null;
			}
			else if(activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentMp(mp);
				pet = null;
			}
			else
			{
				activeChar.getStatus().setCurrentMp(mp);
			}
		}

		st2 = null;

		return true;
	}

	private void listCharacters(L2PcInstance activeChar, int page)
	{
		Collection<L2PcInstance> allPlayers_with_offlines = L2World.getInstance().getAllPlayers();
		
		List<L2PcInstance> online_players_list = new ArrayList<L2PcInstance>();
		
		for(L2PcInstance actual_player: allPlayers_with_offlines)
			if(actual_player!=null && actual_player.isOnline()==1 && !actual_player.isOffline())
				online_players_list.add(actual_player);
			else if(actual_player==null)
				_log.log(Level.WARNING, "listCharacters: found player null into L2World Instance..");
			else if(actual_player.isOnline()==0)
				_log.log(Level.WARNING, "listCharacters: player "+actual_player.getName()+" not online into L2World Instance..");
			else if(actual_player.isOffline())
				_log.log(Level.WARNING, "listCharacters: player "+actual_player.getName()+" offline into L2World Instance..");
		
		L2PcInstance[] players = online_players_list.toArray(new L2PcInstance[online_players_list.size()]);
		online_players_list = null;

		int MaxCharactersPerPage = 20;
		int MaxPages = players.length / MaxCharactersPerPage;

		if(players.length > MaxCharactersPerPage * MaxPages)
		{
			MaxPages++;
		}

		//Check if number of users changed
		if(page > MaxPages)
		{
			page = MaxPages;
		}

		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.length;

		if(CharactersEnd - CharactersStart > MaxCharactersPerPage)
		{
			CharactersEnd = CharactersStart + MaxCharactersPerPage;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charlist.htm");
		TextBuilder replyMSG = new TextBuilder();

		for(int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}

		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG.clear();

		for(int i = CharactersStart; i < CharactersEnd; i++)
		{ //Add player info into new Table row
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_info " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
		}

		adminReply.replace("%players%", replyMSG.toString());
		activeChar.sendPacket(adminReply);

		replyMSG = null;
		adminReply = null;
		players = null;
	}

	/**
	 * @param activeChar
	 * @param player
	 */
	public static void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";

		try
		{
			StringTokenizer clientinfo = new StringTokenizer(player.getClient().toString(), " ]:-[");
			clientinfo.nextToken();
			clientinfo.nextToken();
			clientinfo.nextToken();
			account = clientinfo.nextToken();
			clientinfo.nextToken();
			ip = clientinfo.nextToken();
			clientinfo = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(ClanTable.getInstance().getClan(player.getClanId())));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo((float) player.getCurrentLoad() / (float) player.getMaxLoad() * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		activeChar.sendPacket(adminReply);

		adminReply = null;
		ip = null;
		account = null;
	}

	private void setTargetKarma(L2PcInstance activeChar, int newKarma)
	{
		// function to change karma of selected char
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;

		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
			return;

		target = null;

		if(newKarma >= 0)
		{
			// for display
			int oldKarma = player.getKarma();

			// update karma
			player.setKarma(newKarma);

			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.KARMA, newKarma);
			player.sendPacket(su);
			su = null;

			//Common character information
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
			sm.addString("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			player.sendPacket(sm);
			sm = null;

			//Admin information
			if(player != activeChar)
			{
				activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
			}
		}
		else
		{
			// tell admin of mistake
			activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
		}

		player = null;
	}

	private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		L2Object target = activeChar.getTarget();

		if(!(target instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) target;
		StringTokenizer st = new StringTokenizer(modifications);

		target = null;

		if(st.countTokens() != 6)
		{
			editCharacter(player);
			return;
		}

		String hp = st.nextToken();
		String mp = st.nextToken();
		String cp = st.nextToken();
		String pvpflag = st.nextToken();
		String pvpkills = st.nextToken();
		String pkkills = st.nextToken();

		st = null;

		int hpval = Integer.parseInt(hp);
		int mpval = Integer.parseInt(mp);
		int cpval = Integer.parseInt(cp);
		int pvpflagval = Integer.parseInt(pvpflag);
		int pvpkillsval = Integer.parseInt(pvpkills);
		int pkkillsval = Integer.parseInt(pkkills);

		//Common character information
		player.sendMessage("Admin has changed your stats." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);
		player.getStatus().setCurrentHp(hpval);
		player.getStatus().setCurrentMp(mpval);
		player.getStatus().setCurrentCp(cpval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);

		// Save the changed parameters to the database.
		player.store();

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);
		su = null;

		//Admin information
		player.sendMessage("Changed stats of " + player.getName() + "." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);

		if(Config.DEBUG)
		{
			_log.log(Level.WARNING, "[GM]" + activeChar.getName() + " changed stats of " + player.getName() + ". " + " HP: " + hpval + " MP: " + mpval + " CP: " + cpval + " PvP: " + pvpflagval + " / " + pvpkillsval);
		}

		showCharacterInfo(activeChar, null); //Back to start

		player.broadcastUserInfo();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.decayMe();
		player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		player = null;
	}

	private void editCharacter(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();

		if(!(target instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) target;
		gatherCharacterInfo(activeChar, player, "charedit.htm");
		target = null;
		player = null;
	}

	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		int CharactersFound = 0;

		String name;
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

		allPlayers = null;

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charfind.htm");
		TextBuilder replyMSG = new TextBuilder();

		for(L2PcInstance player : players)
		{ //Add player info into new Table row
			name = player.getName();

			if(name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}

			if(CharactersFound > 20)
			{
				break;
			}
		}

		name = null;
		players = null;

		adminReply.replace("%results%", replyMSG.toString());
		replyMSG.clear();

		if(CharactersFound == 0)
		{
			replyMSG.append("s. Please try again.");
		}
		else if(CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG.append("s.<br>Please refine your search to see all of the results.");
		}
		else if(CharactersFound == 1)
		{
			replyMSG.append(".");
		}
		else
		{
			replyMSG.append("s.");
		}

		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);

		adminReply = null;
		replyMSG = null;
	}

	private void findDualbox(L2PcInstance activeChar, int multibox) throws IllegalArgumentException
	{
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		Map<String, List<L2PcInstance>> ipMap = new HashMap<String, List<L2PcInstance>>();

		String ip = "0.0.0.0";
		L2GameClient client;

		final Map<String, Integer> dualboxIPs = new HashMap<String, Integer>();

		for(L2PcInstance player : players)
		{
			client = player.getClient();
			ip = client.getConnection().getInetAddress().getHostAddress();

			if(ipMap.get(ip) == null)
			{
				ipMap.put(ip, new ArrayList<L2PcInstance>());
			}
			ipMap.get(ip).add(player);

			if(ipMap.get(ip).size() >= multibox)
			{
				Integer count = dualboxIPs.get(ip);
				if(count == null)
				{
					dualboxIPs.put(ip, 0);
				}
				else
				{
					dualboxIPs.put(ip, count + 1);
				}
			}
		}

		List<String> keys = new ArrayList<String>(dualboxIPs.keySet());
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String left, String right)
			{
				return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
			}
		});
		Collections.reverse(keys);

		final StringBuilder results = new StringBuilder();
		for(String dualboxIP : keys)
		{
			results.append("<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + "</a><br1>");
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		activeChar.sendPacket(adminReply);

		keys = null;
		adminReply = null;
	}

	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(L2PcInstance activeChar, String IpAdress) throws IllegalArgumentException
	{
		if(!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			throw new IllegalArgumentException("Malformed IPv4 number");
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		allPlayers = null;

		int CharactersFound = 0;

		String name, ip = "0.0.0.0";
		TextBuilder replyMSG = new TextBuilder();
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/ipfind.htm");

		for(L2PcInstance player : players)
		{
			ip = player.getClient().getConnection().getInetAddress().getHostAddress();

			if(ip.equals(IpAdress))
			{
				name = player.getName();
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}

			if(CharactersFound > 20)
			{
				break;
			}
		}

		name = null;
		players = null;

		adminReply.replace("%results%", replyMSG.toString());
		replyMSG.clear();

		if(CharactersFound == 0)
		{
			replyMSG.append("s. Maybe they got d/c? :)");
		}
		else if(CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
			replyMSG.append("s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.");
		}
		else if(CharactersFound == 1)
		{
			replyMSG.append(".");
		}
		else
		{
			replyMSG.append("s.");
		}

		adminReply.replace("%ip%", ip);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);

		ip = null;
		replyMSG = null;
		adminReply = null;
	}

	/**
	 * @param activeChar
	 * @param characterName
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerAccount(L2PcInstance activeChar, String characterName) throws IllegalArgumentException
	{
		if(characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			L2PcInstance player = L2World.getInstance().getPlayer(characterName);

			if(player == null)
				throw new IllegalArgumentException("Player doesn't exist");

			chars = player.getAccountChars();
			account = player.getAccountName();

			TextBuilder replyMSG = new TextBuilder();
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile("data/html/admin/accountinfo.htm");

			for(String charname : chars.values())
			{
				replyMSG.append(charname + "<br1>");
			}

			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);

			account = null;
			chars = null;
			player = null;
			replyMSG = null;
			adminReply = null;
		}
		else
			throw new IllegalArgumentException("Malformed character name");
	}

	private void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if(player == null)
		{
			L2Object target = activeChar.getTarget();

			if(target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
				return;

			target = null;
		}
		else
		{
			activeChar.setTarget(player);
		}

		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
