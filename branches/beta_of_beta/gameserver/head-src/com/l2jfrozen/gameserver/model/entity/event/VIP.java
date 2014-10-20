/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class VIP
{
	public static String	_teamName = "", _joinArea = "";
	
	public static int		_time = 0, _winners = 0,
							_vipReward = 0, _vipRewardAmount = 0,
							_notVipReward = 0, _notVipRewardAmount = 0,
							_theVipReward = 0, _theVipRewardAmount = 0,
							_endNPC = 0, _joinNPC = 0,
							_delay = 0,
							_endX = 0, _endY = 0, _endZ = 0,
							_startX = 0, _startY = 0, _startZ = 0,
							_joinX = 0, _joinY = 0, _joinZ = 0,
							_team = 0; 	// Human = 1
										// Elf = 2
										// Dark = 3
										// Orc = 4
										// Dwarf = 5

	public static boolean 	_started = false,
							_joining = false,
							_inProgress= true,
							_sitForced = false;

	public static L2Spawn	_endSpawn, _joinSpawn;

	public static Vector<L2PcInstance> 	_playersVIP = new Vector<L2PcInstance>(),
										_playersNotVIP = new Vector<L2PcInstance>();

	public static void setTeam(String team, L2PcInstance activeChar)
	{
		if(team.compareToIgnoreCase("Human") == 0)
		{
			_team = 1;
			_teamName = "Human";
		}
		else if(team.compareToIgnoreCase("Elf") == 0)
		{
			_team = 2;
			_teamName = "Elf";
		}
		else if(team.compareToIgnoreCase("Dark") == 0)
		{
			_team = 3;
			_teamName = "Dark Elf";
		}
		else if(team.compareToIgnoreCase("Orc") == 0)
		{
			_team = 4;
			_teamName = "Orc";
		}
		else if(team.compareToIgnoreCase("Dwarf") == 0)
		{
			_team = 5;
			_teamName = "Dwarf";
		}
		else
		{
			activeChar.sendMessage("Invalid Team Name: //vip_setteam <human/elf/dark/orc/dwarf>");
			return;
		}
		setLoc();
	}

	public static void setRandomTeam(L2PcInstance activeChar)
	{
		Random generator = new Random();

		int random = generator.nextInt(5) + 1; // (0 - 4) + 1

		System.out.println("Random number generated in setRandomTeam(): " + random);

		switch(random)
		{
			case 1: 	_team = 1; _teamName = "Human"; setLoc(); break;
			case 2:		_team = 2; _teamName = "Elf"; setLoc(); break;
			case 3:		_team = 3; _teamName = "Dark"; setLoc(); break;
			case 4:		_team = 4; _teamName = "Orc"; setLoc(); break;
			case 5: 	_team = 5; _teamName = "Dwarf"; setLoc(); break;
			default: break;
		}
	}

	public static void setLoc()
	{
		java.sql.Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT endx,endy,endz FROM VIPinfo WHERE teamID = " + _team);
			ResultSet rset = statement.executeQuery();
			rset.next();

			_endX = rset.getInt("endx");
			_endY = rset.getInt("endy");
			_endZ = rset.getInt("endz");

			rset.close();
			statement.close();
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("Could not check End LOC for team" + _team + " got: " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT startx,starty,startz FROM VIPinfo WHERE teamID = " + _team);
			ResultSet rset = statement.executeQuery();
			rset.next();

			_startX = rset.getInt("startx");
			_startY = rset.getInt("starty");
			_startZ = rset.getInt("startz");

			rset.close();
			statement.close();
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("Could not check Start LOC for team" + _team + " got: " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	public static void endNPC(int npcId, L2PcInstance activeChar)
	{
		if(_team == 0)
		{
			activeChar.sendMessage("Please select a team first");
			return;
		}

		L2NpcTemplate npctmp = NpcTable.getInstance().getTemplate(npcId);
		_endNPC = npcId;

		try
		{
			_endSpawn = new L2Spawn(npctmp);
			_endSpawn.setLocx(_endX);
			_endSpawn.setLocy(_endY);
			_endSpawn.setLocz(_endZ);
			_endSpawn.setAmount(1);
			_endSpawn.setHeading(activeChar.getHeading());
			_endSpawn.setRespawnDelay(1);
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			activeChar.sendMessage("VIP Engine[endNPC(" + activeChar.getName() + ")]: exception: " + e.getMessage());
		}
	}

	public static void joinNPC(int npcId, L2PcInstance activeChar)
	{
		if(_joinX == 0)
		{
			activeChar.sendMessage("Please set a join x,y,z first");
			return;
		}

		L2NpcTemplate npctmp = NpcTable.getInstance().getTemplate(npcId);
		_joinNPC = npcId;

		try
		{
			_joinSpawn = new L2Spawn(npctmp);
			_joinSpawn.setLocx(_joinX);
			_joinSpawn.setLocy(_joinY);
			_joinSpawn.setLocz(_joinZ);
			_joinSpawn.setAmount(1);
			_joinSpawn.setHeading(activeChar.getHeading());
			_joinSpawn.setRespawnDelay(1);
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			activeChar.sendMessage("VIP Engine[joinNPC(" + activeChar.getName() + ")]: exception: " + e.getMessage());
		}
	}

	public static void spawnEndNPC()
	{
		try
		{
			SpawnTable.getInstance().addNewSpawn(_endSpawn, false);

			_endSpawn.init();
			_endSpawn.getLastSpawn().setCurrentHp(999999999);
			_endSpawn.getLastSpawn().setTitle("VIP Event Manager");
			_endSpawn.getLastSpawn().isAggressive();
			_endSpawn.getLastSpawn().decayMe();
			_endSpawn.getLastSpawn().spawnMe(_endSpawn.getLastSpawn().getX(), _endSpawn.getLastSpawn().getY(), _endSpawn.getLastSpawn().getZ());

			_endSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_endSpawn.getLastSpawn(), _endSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("VIP Engine[spawnEndNPC()]: exception: " + e.getMessage());
		}
	}

	public static void spawnJoinNPC()
	{
		try
		{
			SpawnTable.getInstance().addNewSpawn(_joinSpawn, false);

			_joinSpawn.init();
			_joinSpawn.getLastSpawn().setCurrentHp(999999999);
			_joinSpawn.getLastSpawn().setTitle("VIP Event Manager");
			_joinSpawn.getLastSpawn().isAggressive();
			_joinSpawn.getLastSpawn().decayMe();
			_joinSpawn.getLastSpawn().spawnMe(_joinSpawn.getLastSpawn().getX(), _joinSpawn.getLastSpawn().getY(), _joinSpawn.getLastSpawn().getZ());

			_joinSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_joinSpawn.getLastSpawn(), _joinSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("VIP Engine[spawnJoinNPC()]: exception: " + e.getMessage());
		}
	}

	public static String getNPCName(int id, L2PcInstance activeChar)
	{
		if(id == 0)
			return "";
		
		L2NpcTemplate npctmp = NpcTable.getInstance().getTemplate(id);
		return npctmp.name;
	}

	public static String getItemName(int id, L2PcInstance activeChar)
	{
		if(id == 0)
			return "";
		L2Item itemtmp = ItemTable.getInstance().getTemplate(id);
		return itemtmp.getName();
	}

	public static void setJoinLOC(String x, String y, String z)
	{
		_joinX = Integer.valueOf(x);
		_joinY = Integer.valueOf(y);
		_joinZ = Integer.valueOf(z);
	}

	public static void startJoin(L2PcInstance activeChar)
	{
		if(_time == 0 || _team == 0 || _endNPC == 0 || _delay == 0)
		{
			activeChar.sendMessage("Cannot initiate join status of event, not all the values are filled in");
			return;
		}

		if(_joining)
		{
			activeChar.sendMessage("Players are already allowed to join the event");
			return;
		}

		if(_started)
		{
			activeChar.sendMessage("Event already started. Please wait for it to finish or finish it manually");
			return;
		}

		_inProgress = true;
		_joining = true;
		Announcements.getInstance().gameAnnounceToAll("Vip event has started.Use .vipjoin to join or .vipleave to leave.");
		spawnJoinNPC();

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_joining = false;
				_started = true;
				startEvent();
			}
		}, _delay);
	}

	public static void startEvent()
	{
		Announcements.getInstance().gameAnnounceToAll("Registration for the VIP event involving " + _teamName + " has ended.");
		Announcements.getInstance().gameAnnounceToAll("Players will be teleported to their locations in 20 seconds.");

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				teleportPlayers();
				chooseVIP();
				setUserData();
				Announcements.getInstance().gameAnnounceToAll("Players have been teleported for the VIP event.");
				Announcements.getInstance().gameAnnounceToAll("VIP event will start in 20 seconds.");
				spawnEndNPC();

				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						Announcements.getInstance().gameAnnounceToAll("VIP event has started. " + _teamName + "'s VIP must get to the starter city and talk with " + getNPCName(_endNPC, null) + ". The opposing team must kill the VIP. All players except the VIP will respawn at their current locations.");
						Announcements.getInstance().gameAnnounceToAll("VIP event will end if the " + _teamName + " team makes it to their town or when " + _time/1000/60 + " mins have elapsed.");
						VIP.sit();

						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								endEventTime();
							}
						}, _time);
					}
				}, 20000);
			}
		}, 20000);
	}

	public static void vipDied()
	{
		if(!_started)
		{
			System.out.println("Could not finish the event. Event not started or event ended prematurly.");
			return;
		}

		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().gameAnnounceToAll("The VIP has died. The opposing team has won.");
		rewardNotVIP();
		teleportFinish();
	}

	public static void endEventTime()
	{
		if(!_started)
		{
			System.out.println("Could not finish the event. Event not started or event ended prematurly (VIP died)");
			return;
		}

		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().gameAnnounceToAll("The time has run out and the " + _teamName + "'s have not made it to their goal. Everybody on the opposing team wins.");
		rewardNotVIP();
		teleportFinish();
	}

	public static void unspawnEventNpcs()
	{
		if(_endSpawn != null)
		{
			_endSpawn.getLastSpawn().deleteMe();
			_endSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_endSpawn, true);
		}

		if(_joinSpawn != null)
		{
			_joinSpawn.getLastSpawn().deleteMe();
			_joinSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_joinSpawn, true);
		}
	}

	public static void showEndHTML(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			TextBuilder replyMSG = new TextBuilder("<html><head><body>");
			replyMSG.append("VIP (End NPC)<br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("Team:&nbsp;<font color=\"FFFFFF\">" + _teamName + "</font><br><br>");

			if(!_started)
				replyMSG.append("<center>Please wait until the admin/gm starts the joining period.</center>");
			else if(eventPlayer._isTheVIP)
			{
				replyMSG.append("You have made it to the end. All you have to do is hit the finish button to reward yourself and your team. Congrats!<br>");
				replyMSG.append("<center>");
				replyMSG.append("<button value=\"Finish\" action=\"bypass -h npc_" + objectId + "_vip_finishVIP\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				replyMSG.append("</center>");
			}
			else
			{
				replyMSG.append("I am the character the VIP has to reach in order to win the event.<br>");
			}

			replyMSG.append("</head></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);	
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("VIP(showJoinHTML(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

	public static void vipWin(L2PcInstance activeChar)
	{
		if(!_started)
		{
			System.out.println("Could not finish the event. Event not started or event ended prematurly");
			return;
		}

		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().gameAnnounceToAll("The VIP has made it to the goal. " + _teamName + " has won. Everybody on that team wins.");
		rewardVIP();
		teleportFinish();
	}

	public static void rewardNotVIP()
	{
		for(L2PcInstance player : _playersNotVIP)
		{
			if(player != null)
			{
				PcInventory inv = player.getInventory();
				
				if(ItemTable.getInstance().createDummyItem(_notVipReward).isStackable())
					inv.addItem("VIP Event: ", _notVipReward, _notVipRewardAmount, player, null);
				else
				{
					for(int i=0;i<=_notVipRewardAmount-1;i++)
						inv.addItem("VIP Event: ", _notVipReward, 1, player, null);
				}

				SystemMessage sm;

				if(_notVipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(_notVipReward);
					sm.addNumber(_notVipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(_notVipReward);
					player.sendPacket(sm);
				}

				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);

				NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				TextBuilder replyMSG = new TextBuilder("");

				replyMSG.append("<html><head><body>Your team has won the event. Your inventory now contains your reward.</body></html>");

				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
		}
	}

	public static void rewardVIP()
	{
		for(L2PcInstance player : _playersVIP)
		{
			if(player != null && !player._isTheVIP)
			{
				PcInventory inv = player.getInventory();

				if(ItemTable.getInstance().createDummyItem(_vipReward).isStackable())
					inv.addItem("VIP Event: ", _vipReward, _vipRewardAmount, player, null);
				else
				{
					for(int i=0;i<=_vipRewardAmount-1;i++)
						inv.addItem("VIP Event: ", _vipReward, 1, player, null);
				}

				SystemMessage sm;

				if(_vipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(_vipReward);
					sm.addNumber(_vipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(_vipReward);
					player.sendPacket(sm);
				}

				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);

				NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				TextBuilder replyMSG = new TextBuilder("");

				replyMSG.append("<html><head><body>Your team has won the event. Your inventory now contains your reward.</body></html>");

				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
			else if(player != null && player._isTheVIP)
			{
				PcInventory inv = player.getInventory();

				if(ItemTable.getInstance().createDummyItem(_theVipReward).isStackable())
					inv.addItem("VIP Event: ", _theVipReward, _theVipRewardAmount, player, null);
				else
				{
					for(int i=0;i<=_theVipRewardAmount-1;i++)
						inv.addItem("VIP Event: ", _theVipReward, 1, player, null);
				}

				SystemMessage sm;

				if(_theVipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(_theVipReward);
					sm.addNumber(_theVipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(_theVipReward);
					player.sendPacket(sm);
				}

				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);

				NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				TextBuilder replyMSG = new TextBuilder("");

				replyMSG.append("<html><head><body>You team have won the event. Your inventory now contains your reward.</body></html>");

				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
		}
	}

	public static void teleportFinish()
	{
		Announcements.getInstance().gameAnnounceToAll("Teleporting VIP players back to the Registration area in 20 seconds.");

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				for(L2PcInstance player : _playersVIP)
				{
					if(player !=  null)
						player.teleToLocation(_joinX, _joinY, _joinZ);
				}

				for(L2PcInstance player : _playersNotVIP)
				{
					if(player !=  null)
						player.teleToLocation(_joinX, _joinY, _joinZ);
				}

				VIP.clean();
			}
		}, 20000);
	}

	public static void clean()
	{
		_time = _winners = _endNPC = _joinNPC = _delay = _endX = _endY = _endZ = _startX = _startY = _startZ = _joinX = _joinY = _joinZ = _team = 0;
		_vipReward = _vipRewardAmount = _notVipReward = _notVipRewardAmount = _theVipReward = _theVipRewardAmount = 0;
		_started = _joining = _sitForced = false;
		_inProgress = false;
		_teamName = _joinArea = "";

		for(L2PcInstance player : _playersVIP)
		{
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
		}

		for(L2PcInstance player : _playersNotVIP)
		{
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
		}

		_playersVIP = new Vector<L2PcInstance>();
		_playersNotVIP = new Vector<L2PcInstance>();
	}

	public static void chooseVIP()
	{
		int size = _playersVIP.size();

		System.out.println("Size of players on VIP: " + size);

		Random generator = new Random();
		int random = generator.nextInt(size);

		System.out.println("Random number chosen in VIP: " + random);

		L2PcInstance VIP = _playersVIP.get(random);
		VIP._isTheVIP = true;
	}

	public static void teleportPlayers()
	{
		VIP.sit();

		for(L2PcInstance player : _playersVIP)
		{
			if(player !=  null) 
				player.teleToLocation(_startX, _startY, _startZ);
		}
		for(L2PcInstance player : _playersNotVIP)
		{
			if(player != null)
				player.teleToLocation(_endX, _endY, _endZ);
		}
	}

	public static void sit()
	{
		if(_sitForced)
			_sitForced = false;
		else
			_sitForced = true;

		for(L2PcInstance player : _playersVIP)
		{
			if(player != null)
			{
				if(_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();

					if(!player.isSitting())
						player.sitDown();
				}
				else
				{
					if(player.isSitting())
						player.standUp();
				}
			}
		}

		for(L2PcInstance player : _playersNotVIP)
		{
			if(player != null)
			{
				if(_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();
					
					if(!player.isSitting())
						player.sitDown();
				}
				else
				{
					if(player.isSitting())
						player.standUp();
				}
			}
		}
	}

	public static void setUserData()
	{
		for(L2PcInstance player : _playersVIP)
		{
			if(player._isTheVIP)
				player.getAppearance().setNameColor(255,255,0);
			else
				player.getAppearance().setNameColor(255,0,0);

			player.setKarma(0);
			player.broadcastUserInfo();
		}
		for(L2PcInstance player : _playersNotVIP)
		{
			player.getAppearance().setNameColor(0,255,0);
			player.setKarma(0);
			player.broadcastUserInfo();
		}
	}

	public static void showJoinHTML(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			TextBuilder replyMSG = new TextBuilder("<html><head><body>");
			replyMSG.append("VIP (Join NPC)<br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("	... Team:&nbsp;<font color=\"FFFFFF\">" + _teamName + "</font><br><br>");

			if(!_joining && !_started) // PreEvent
				replyMSG.append("<center>Please wait until the admin/gm starts the joining period.</center>");
			else if(_joining && !_started)
			{
				// Joining period
				if(_playersVIP.contains(eventPlayer) || _playersNotVIP.contains(eventPlayer))
				{
					replyMSG.append("You are already on a team<br><br>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					if(eventPlayer.getRace() == Race.human && _team == 1)
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if(eventPlayer.getRace() == Race.elf && _team == 2)
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if(eventPlayer.getRace() == Race.darkelf && _team == 3)
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if(eventPlayer.getRace() == Race.orc && _team == 4)
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if(eventPlayer.getRace() == Race.dwarf && _team == 5)
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else
					{
						replyMSG.append("It seems you are not on the part of the VIP race.<br>");
						replyMSG.append("When the event starts you will be teleported to the " + _teamName + " town<br1>");
						replyMSG.append("Be sure to cooperate with your team to destroy the VIP.<br1>");
						replyMSG.append("The VIP will be announced when the event starts.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinNotVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
				}
			}
			else if(_started) // Event already Started
				replyMSG.append("<center>The event is already taking place. Please sign up for the next event.</center>");

			replyMSG.append("</head></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);		
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("VIP(showJoinHTML(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}

	public static void addPlayerVIP(L2PcInstance activeChar)
	{
		activeChar._isVIP = true;
		_playersVIP.add(activeChar);
		activeChar._originalNameColourVIP = activeChar.getAppearance().getNameColor();
		activeChar._originalKarmaVIP = activeChar.getKarma();
		activeChar._inEventVIP = true;
	}

	public static void addPlayerNotVIP(L2PcInstance activeChar)
	{
		activeChar._isNotVIP = true;
		_playersNotVIP.add(activeChar);
		activeChar._originalNameColourVIP = activeChar.getAppearance().getNameColor();
		activeChar._originalKarmaVIP = activeChar.getKarma();
		activeChar._inEventVIP = true;
	}
	
	public static void onDisconnect(L2PcInstance player){
		
		if(player._inEventTvT){
			
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
			player.teleToLocation(_startX, _startY, _startZ);
		}
	}
}