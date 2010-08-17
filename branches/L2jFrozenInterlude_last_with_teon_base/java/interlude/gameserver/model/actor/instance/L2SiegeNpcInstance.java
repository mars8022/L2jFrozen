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
package interlude.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.clanhallsiege.BanditStrongholdSiege;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortResistSiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.instancemanager.clanhallsiege.RainbowSpringSiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.WildBeastFarmSiege;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2ClanMember;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class L2SiegeNpcInstance extends L2FolkInstance
{
	public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
			{
				if (getNpcId() == 35437 
						|| getNpcId() == 35627 
						|| getNpcId() == 35604 
						|| getNpcId() == 35382)
					showChatWindow(player, 0);
				else if (getNpcId() == 35420)
					showSiegeInfoWindow(player,2);
				else if (getNpcId() == 35639)
					showSiegeInfoWindow(player,3);
				else
					showSiegeInfoWindow(player,1);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			if (quest.length() == 0)
				showQuestWindow(player);
			else
				showQuestWindow(player, quest);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("Registration"))
		{
			L2Clan playerClan = player.getClan();
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String str;
			str = "<html><body>Messenger!<br>";
			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showChatWindow(player, 3);
						return;
					}
					if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
					{
						showChatWindow(player, 1);
						return;
					}
					if (BanditStrongholdSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Your clan is already registered for the siege, what more do you want from me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add / remove a member of the siege</a><br>";
					}
					else
					{
						if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
						{
							str += "Your clan is already registered for the siege, what more do you want from me?<br>";
							str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">Unsubscribe</a><br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add / remove a member of the siege</a><br>";
						}
						else
						{
							int res = BanditStrongholdSiege.getInstance().registerClanOnSiege(player, playerClan);
							if (res == 0)
							{
								str += "Your clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully registered for the siege clan hall.<br>";
								str += "Now you need to select no more than 18 igokov who will take part in the siege, a member of your clan.<br>";
								str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Select members of the siege</a><br>";
							}
							else if (res == 1)
							{
								str += "You have not passed the test and did not qualify for participation in the siege of Robbers<br>";
								str += "Come back when you're done.";
							}
							else if (res == 2)
							{
								str += "Unfortunately, you are late. Five tribal leaders have already filed an application for registration.<br>";
								str += "Next time be more powerful";
							}
						}
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showChatWindow(player, 3);
						return;
					}
					if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
					{
						showChatWindow(player, 1);
						return;
					}
					if (WildBeastFarmSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Your clan is already registered for the siege, what more do you want from me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add / remove a member of the siege</a><br>";
					}
					else
					{
						if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
						{
							str += "Your clan is already registered for the siege, what more do you want from me?<br>";
							str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">Unsubscribe</a><br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add / remove a member of the siege</a><br>";
						}
						else
						{
							int res = WildBeastFarmSiege.getInstance().registerClanOnSiege(player, playerClan);
							if (res == 0)
							{
								str += "Your clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully registered for the siege clan hall.<br>";
								str += "Now you need to select no more than 18 igokov who will take part in the siege, a member of your clan.<br>";
								str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Select members of the siege</a><br>";
							}
							else if (res == 1)
							{
								str += "You have not passed the test and did not qualify for participation in the siege of Robbers<br>";
								str += "Come back when you're done.";
							}
							else if (res == 2)
							{
								str += "Unfortunately, you are late. Five tribal leaders have already filed an application for registration.<br>";
								str += "Next time be more raztoropny.";
							}
						}
					}
					break;
			case 35604:
				if(!RainbowSpringSiegeManager.getInstance().isRegistrationPeriod())
				{
					showChatWindow(player, 6);
					return;
				}
				if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				{
					showChatWindow(player, 4);
					return;
				}
				if (RainbowSpringSiegeManager.getInstance().clanhall.getOwnerClan() == playerClan)
					str += "Your clan is already registered for the siege, what more do you want from me?<br>";
				else if (RainbowSpringSiegeManager.getInstance().isClanOnSiege(playerClan))
					str += "Your clan has already applied to participate in the competition for the clan hall, that you still want from me?<br>";
				else
				{
					int res=RainbowSpringSiegeManager.getInstance().registerClanOnSiege(player,playerClan);
					if (res>0)
						str += "Your application for participation in the competition for the clan hall is accepted, you have made <font color=\"LEVEL\">"+res+" Certificate of Participation in the War of the clan hall hot spring</font>.<br>";
					else
						str += "To apply for participation in the competition for the clan hall must get as much as possible <font color=\"LEVEL\">Evidenced by participation in the War of the clan hall hot spring</font>.<br>";						
				}
				break;
			}
			str += "</body></html>";
			html.setHtml(str);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("UnRegister"))
		{
			L2Clan playerClan=player.getClan();
			NpcHtmlMessage html;
			String str;
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
			{
				_log.warning("Attention!!! player " + player.getName() + " use packet hack, try unregister clan.");
				return;
			}
			switch(getTemplate().getNpcId())
			{
			case 35437:			
				if(!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
				{
					showChatWindow(player, 3);
					return;
				}
				html = new NpcHtmlMessage(getObjectId());
				if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
				{
					if (BanditStrongholdSiege.getInstance().unRegisterClan(playerClan))
					{
						str = "<html><body>Messenger!<br>";
						str += "Your clan : <font color=\"LEVEL\"> " + player.getClan().getName() + " </font>, successfully removed from the register at the siege of the clan hall.<br>";
						str += "</body></html>";
						html.setHtml(str);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
				}
				else
					_log.warning("Attention!!! player " + player.getName() + " use packet hack, try unregister clan.");
				break;
			case 35627:
				if(!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
				{
					showChatWindow(player, 3);
					return;
				}
				html = new NpcHtmlMessage(getObjectId());
				if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
				{
					if (WildBeastFarmSiege.getInstance().unRegisterClan(playerClan))
					{
						str = "<html><body>Messenger!<br>";
						str += "Your clan : <font color=\"LEVEL\"> " + player.getClan().getName() + " </font>, successfully removed from the register at the siege of the clan hall.<br>";
						str += "</body></html>";
						html.setHtml(str);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
				}
				else
					_log.warning("Attention!!! player " + player.getName() + " use packet hack, try unregister clan.");
				break;
			case 35604:
				if(!RainbowSpringSiegeManager.getInstance().isRegistrationPeriod())
				{
					showChatWindow(player, 6);
					return;
				}
				html = new NpcHtmlMessage(getObjectId());
				if (RainbowSpringSiegeManager.getInstance().isClanOnSiege(playerClan))
				{
					if (RainbowSpringSiegeManager.getInstance().unRegisterClan(player))
					{
						str = "<html><body>Messenger!<br>";
						str += "Your clan : <font color=\"LEVEL\"> " + player.getClan().getName() + " </font>, successfully removed from the register at the siege of the clan hall.<br>";
						str += "</body></html>";
						html.setHtml(str);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
				}
				break;
			}
		}
		else if (command.startsWith("PlayerList"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;

			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showChatWindow(player, 3);
				return;
			}
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
				showPlayersList(playerClan, player);
		}
		else if (command.startsWith("addPlayer"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;

			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showChatWindow(player, 3);
				return;
			}
			String val = command.substring(10);
			if (playerClan.getClanMember(val) == null)
				return;

			BanditStrongholdSiege.getInstance().addPlayer(playerClan, val);
			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
				showPlayersList(playerClan, player);
		}
		else if (command.startsWith("removePlayer"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;

			if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
			{
				showChatWindow(player, 3);
				return;
			}
			String val = command.substring(13);
			if (playerClan.getClanMember(val) != null)
				BanditStrongholdSiege.getInstance().removePlayer(playerClan, val);

			if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
				showPlayersList(playerClan, player);
		}
	}

	public void showPlayersList(L2Clan playerClan, L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		str = "<html><body>Newspaper!<br>";
		str += "Your clan : <font color=\"LEVEL\"> " + player.getClan().getName() + " </font>. select participants for the siege.<br><br>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Register bathrooms</td><td width=110 align=center>Action</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		for (String temp : BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan))
		{
			str += "<tr><td width=170> " + temp + " </td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_removePlayer " + temp + "\"> Remove</a></td></tr>";
		}
		str += "</table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Clan Members</td><td width=110 align=center>Action</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		for (L2ClanMember temp : playerClan.getMembers())
		{
			if (!BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan).contains(temp.getName()))
				str += "<tr><td width=170> " + temp.getName() + " </td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_addPlayer " + temp.getName() + "\"> Add</a></td></tr>";
		}
		str += "</table>";
		str += "</body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		long startSiege = 0;
		int npcId = getTemplate().getNpcId();
		String filename;
		if (val==0)
			filename = "data/html/siege/" + npcId + ".htm";
		else
			filename = "data/html/siege/" + npcId +"-" +val+ ".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		if (npcId == 35382)
			startSiege = FortResistSiegeManager.getInstance().getSiegeDate().getTimeInMillis();

		else if (npcId == 35437 || npcId == 35627 || npcId == 35604)
		{
			ClanHall clanhall = null;
			String clans = "";
			clans += "<table width=280 border=0>";
			int clanCount = 0;
			switch (npcId)
			{
				case 35437:
					clanhall = ClanHallManager.getInstance().getClanHallById(35);
					startSiege = BanditStrongholdSiege.getInstance().getSiegeDate().getTimeInMillis();
					for (String a : BanditStrongholdSiege.getInstance().getRegisteredClans())
					{
						clanCount++;
						clans += "<tr><td><font color=\"LEVEL\"> " + a + " </font>  (Number : " + BanditStrongholdSiege.getInstance().getPlayersCount(a) + " people.)</td></tr>";
					}
					break;
				case 35627:
					clanhall = ClanHallManager.getInstance().getClanHallById(63);
					startSiege = WildBeastFarmSiege.getInstance().getSiegeDate().getTimeInMillis();
					for (String a : WildBeastFarmSiege.getInstance().getRegisteredClans())
					{
						clanCount++;
						clans += "<tr><td><font color=\"LEVEL\"> " + a + " </font>  (Number : " + BanditStrongholdSiege.getInstance().getPlayersCount(a) + " people.)</td></tr>";
					}
					break;
				case 35604:
					clanhall = ClanHallManager.getInstance().getClanHallById(62);
					startSiege = RainbowSpringSiegeManager.getInstance().getSiegeDate().getTimeInMillis();
					break;
			}
			while (clanCount < 5)
			{
				clans += "<tr><td><font color=\"LEVEL\">**Not logged**</font>  (Quantity : people.)</td></tr>";
				clanCount++;
			}
			clans += "</table>";
			html.replace("%clan%", String.valueOf(clans));
			L2Clan clan = clanhall.getOwnerClan();
			String clanName;
			if (clan == null)
				clanName = "NPC";
			else
				clanName = clan.getName();
			html.replace("%clanname%", String.valueOf(clanName));
		}
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		html.replace("%SiegeDate%", String.valueOf(format.format(startSiege)));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 *
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player, int index)
	{
		if (validateCondition(index))
		{
			if (index == 1)
				getCastle().getSiege().listRegisterClan(player);
			else if (index == 2)
				DevastatedCastleManager.getInstance().listRegisterClan(player);
			else if (index == 3)
				FortressofTheDeadManager.getInstance().listRegisterClan(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getTemplate().npcId + "-busy.htm");
			html.replace("%castlename%", getCastle().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private boolean validateCondition(int index)
	{
		if (index == 1)
		{
		if (getCastle().getSiege().getIsInProgress())
			return false; // Busy because of siege
		else if (index == 2)
			return !DevastatedCastleManager.getInstance().getIsInProgress();
		else if (index == 3)
			return !FortressofTheDeadManager.getInstance().getIsInProgress();
		}
		return true;
	}
}