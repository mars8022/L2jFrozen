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
package interlude.gameserver.network.clientpackets;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import interlude.Base64;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.Announcements;
import interlude.gameserver.GmListTable;
import interlude.gameserver.SevenSigns;
import interlude.gameserver.TaskPriority;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.communitybbs.Manager.RegionBBSManager;
import interlude.gameserver.datatables.CharSchemesTable;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.datatables.PcColorTable;
import interlude.gameserver.handler.AdminCommandHandler;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.CoupleManager;
import interlude.gameserver.instancemanager.CrownManager;
import interlude.gameserver.instancemanager.CursedWeaponsManager;
import interlude.gameserver.instancemanager.DimensionalRiftManager;
import interlude.gameserver.instancemanager.FortSiegeManager;
import interlude.gameserver.instancemanager.PetitionManager;
import interlude.gameserver.instancemanager.QuestManager;
import interlude.gameserver.instancemanager.RaidBossPointsManager;
import interlude.gameserver.instancemanager.SiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2ClassMasterInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.base.ClassLevel;
import interlude.gameserver.model.base.PlayerClass;
import interlude.gameserver.model.entity.Castle;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.model.entity.Couple;
import interlude.gameserver.model.entity.Hero;
import interlude.gameserver.model.entity.L2Event;
import interlude.gameserver.model.entity.Siege;
import interlude.gameserver.model.olympiad.Olympiad;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.model.quest.QuestState;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.Die;
import interlude.gameserver.network.serverpackets.EtcStatusUpdate;
import interlude.gameserver.network.serverpackets.ExPCCafePointInfo;
import interlude.gameserver.network.serverpackets.ExStorageMaxCount;
import interlude.gameserver.network.serverpackets.FriendList;
import interlude.gameserver.network.serverpackets.HennaInfo;
import interlude.gameserver.network.serverpackets.ItemList;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.PledgeShowMemberListAll;
import interlude.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import interlude.gameserver.network.serverpackets.PledgeSkillList;
import interlude.gameserver.network.serverpackets.PledgeStatusChanged;
import interlude.gameserver.network.serverpackets.QuestList;
import interlude.gameserver.network.serverpackets.ShortCutInit;
import interlude.gameserver.network.serverpackets.SignsSky;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.gameserver.services.WindowService;
import interlude.gameserver.templates.L2Item;
import interlude.gameserver.util.Util;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev656 cbdddd
 * <p>
 *
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}

	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().getConnection().close(null);
			return;
		}
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
				_log.warning("User already exist in OID map! User " + activeChar.getName() + " is character clone");
			// activeChar.closeNetConnection();
		}
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))
				activeChar.setIsInvul(true);

			if (Config.GM_STARTUP_INVISIBLE && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))
				activeChar.getAppearance().setInvisible();

			if (Config.GM_STARTUP_SILENCE && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))
				activeChar.setMessageRefusal(true);

			if (Config.GM_STARTUP_AUTO_LIST && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston")))
				GmListTable.getInstance().addGm(activeChar, false);
			else
				GmListTable.getInstance().addGm(activeChar, true);

			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (activeChar.getAccessLevel() >= 100)
					activeChar.getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
				else if (activeChar.getAccessLevel() >= 75)
					activeChar.getAppearance().setNameColor(Config.GM_NAME_COLOR);
			}
			if (Config.SHOW_GM_LOGIN)
			{
				String name = activeChar.getName();
				String text = "GameMaster " + name + " Is Currently Online.";
				Announcements.getInstance().announceToAll(text);
			}
		}
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		if (activeChar.getZ() < -15000 || activeChar.getZ() > 15000)
		{
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.setTarget(activeChar);
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
			L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
			L2Event.restoreAndTeleChar(activeChar);

		if (SevenSigns.getInstance().isSealValidationPeriod())
			sendPacket(new SignsSky());

		if (Config.STORE_SKILL_COOLTIME)
			activeChar.restoreEffects();

		// engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
			// Check if player is maried and remove if necessary Cupid's Bow
			if (!activeChar.isMaried())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByItemId(9140);
				// Remove Cupid's Bow
				if (item != null)
				{
					activeChar.destroyItem("Removing Cupid's Bow", item, activeChar, true);
					activeChar.getInventory().updateDatabase();
					// Log it
					_log.info("Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " got Cupid's Bow removed.");
				}
			}
		}
		if (activeChar.getAllEffects() != null)
		{
			for (L2Effect e : activeChar.getAllEffects())
			{
				if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
				if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
			}
		}
		// apply augmentation bonus for equipped items
		for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
				temp.getAugmentation().applyBonus(activeChar);
		}
		activeChar.getMacroses().sendUpdate();
		// checkup and delete delayed donator rented items
		if (Config.DONATOR_DELETE_RENTED_ITEMS)
			activeChar.donatorDeleteDelayedRentedItems();

		// sends general packets
		activeChar.getMacroses().sendUpdate();
		sendPacket(new UserInfo(activeChar));
		sendPacket(new HennaInfo(activeChar));
		sendPacket(new FriendList(activeChar));
		sendPacket(new ItemList(activeChar, false));
		sendPacket(new ShortCutInit(activeChar));

		activeChar.updateEffectIcons();

		// Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));

        if (activeChar.getPcBangPoints() > 0)
            activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), 0, false, false, 1));
        else
            activeChar.sendPacket(new ExPCCafePointInfo());

		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);
		
		activeChar.sendMessage(getText("0KHQv9Cw0YHQuNCx0L4g0LfQsCDQuNGB0L/QvtC70YzQt9C+0LLQsNC90LjQtSDRgdCx0L7RgNC60LggT3BlbiBUZWFtIQ==\n"));
		activeChar.sendMessage(getText("0KDQsNC30LvQuNGH0L3Ri9C1INC00L7Qv9C+0LvQvdC10L3QuNGPINC00LvRjyDRgdC10YDQstC10YDQsCDQvNC+0LbQvdC+INC90LDQudGC0Lgg0L3QsCDRgdCw0LnRgtC1IHd3dy5sMm1heGkucnU=\n"));
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		CrownManager.getInstance().checkCrowns(activeChar);
		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());
		loadTutorial(activeChar);
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
				quest.notifyEnterWorld(activeChar);
		}
		// restore info about chat ban
		activeChar.checkBanChat(false);
		// restore info about auto herbs loot
		// Color System checks - Start =====================================================
		// Check if the custom PvP and PK color systems are enabled and if so ==============
		// check the character's counters and apply any color changes that must be done. ===
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT1 && Config.PVP_COLOR_SYSTEM_ENABLED)
			activeChar.updatePvPColor(activeChar.getPvpKills());

		if (activeChar.getPkKills() >= Config.PK_AMOUNT1 && Config.PK_COLOR_SYSTEM_ENABLED)
			activeChar.updatePkColor(activeChar.getPkKills());

		// Color System checks - End =======================================================
		if (Config.ALLOW_AUTOHERBS_CMD)
			activeChar.getAutoLootHerbs();


		// donator's "Hello!"
		if (activeChar.isDonator())
		{
			activeChar.getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);
			activeChar.sendMessage("Welcome " + activeChar.getName() + " to our L][ Server!");
			activeChar.sendMessage("Enjoy your Stay Donator!");
		}
		// Faction Engine by DaRkRaGe
		if (activeChar.isKoof() && Config.ENABLE_FACTION_KOOFS_NOOBS)
		{
			activeChar.getAppearance().setNameColor(Config.KOOFS_NAME_COLOR);
			activeChar.sendMessage("Welcome " + activeChar.getName() + " u Are fighiting for " + Config.KOOFS_NAME_TEAM + "  Faction");
		}
		if (activeChar.isNoob() && Config.ENABLE_FACTION_KOOFS_NOOBS)
		{
			activeChar.getAppearance().setNameColor(Config.NOOBS_NAME_COLOR);
			activeChar.sendMessage("Welcome " + activeChar.getName() + " u Are fighiting for " + Config.NOOBS_NAME_TEAM + " Faction");
		}
		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			int PLAYERS_ONLINE = L2World.getInstance().getAllPlayers().size() + Config.PLAYERS_ONLINE_TRICK;
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Players online: ");
			sm.addNumber(PLAYERS_ONLINE);
			sendPacket(sm);
		}
		// check any poending petitions
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		// sends welcome htm if enabled.
		if (Config.SHOW_HTML_WELCOME)
			WindowService.sendWindow(activeChar, "data/html/", "Info.htm");

		// check player for unlegit skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
			activeChar.checkAllowedSkills();

		// send user info again .. just like the real client sendPacket(ui);
		if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
		}
		if (activeChar.isAlikeDead())
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
			activeChar.setHero(true);

		setPledgeClass(activeChar);
		activeChar.setOnlineStatus(true);
		notifyFriends(activeChar);
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);
		notifyCastleOwner(activeChar);
		activeChar.onPlayerEnter();
		PcColorTable.getInstance().process(activeChar);
		checkCrown(activeChar);
		// NPCBuffer
		if (Config.NPCBUFFER_FEATURE_ENABLED)
			CharSchemesTable.getInstance().onPlayerLogin(activeChar.getObjectId());
		// load points for that character
		RaidBossPointsManager.loadPoints(activeChar);
		if (activeChar.isCursedWeaponEquiped())
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquipedId()).cursedOnLogin();

		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
		}
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));

		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
			SiegeManager.getInstance().onEnterWorld(activeChar);
			FortSiegeManager.getInstance().onEnterWorld(activeChar);
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;

				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte) 1);
				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte) 2);
			}
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
			}
			if(DevastatedCastleManager.getInstance().getIsInProgress())
				if(DevastatedCastleManager.getInstance().checkIsRegistered(activeChar.getClan()))
					activeChar.setSiegeState((byte) 1);
			if(FortressofTheDeadManager.getInstance().getIsInProgress())
				if(FortressofTheDeadManager.getInstance().checkIsRegistered(activeChar.getClan()))
					activeChar.setSiegeState((byte) 1);
		}
		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}
		RegionBBSManager.getInstance().changeCommunityBoard();
		if (Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			ClassLevel lvlnow = PlayerClass.values()[activeChar.getClassId().getId()].getLevel();
			if (activeChar.getLevel() >= 20 && lvlnow == ClassLevel.First)
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
			else if (activeChar.getLevel() >= 40 && lvlnow == ClassLevel.Second)
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
			else if (activeChar.getLevel() >= 76 && lvlnow == ClassLevel.Third)
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
		}
		// check for over enchant
		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isEquipable() && !activeChar.isGM() || !i.isEquipable() && !activeChar.isGM())
			{
				int itemType2 = i.getItem().getType2();
				if (itemType2 == L2Item.TYPE2_WEAPON)
				{
					if (i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_WEAPON)
					{
						// Delete Item Over enchanted
						activeChar.getInventory().destroyItem(null, i, activeChar, null);
						// Message to Player
						activeChar.sendMessage("[Server]:You have Items over enchanted you will be kikked!");
						// If Audit is only a Kick, with this the player goes in Jail for 1.200 minutes
						activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, 1200);
						// Punishment e log in audit
						Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " have item Overenchanted ", Config.DEFAULT_PUNISH);
						activeChar.closeNetConnection(); // kick
						// Log in console
						_log.info("#### ATTENCTION ####");
						_log.info(i + " item has been removed from player.");
						return;
					}
				}
				if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR)
				{
					if (i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_ARMOR)
					{
						// Delete Item Over enchanted
						activeChar.getInventory().destroyItem(null, i, activeChar, null);
						// Message to Player
						activeChar.sendMessage("[Server]:You have Items over enchanted you will be kikked!");
						// If Audit is only a Kick, with this the player goes in Jail for 1.200 minutes
						activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, 1200);
						// Punishment e log in audit
						Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " have item Overenchanted ", Config.DEFAULT_PUNISH);
						activeChar.closeNetConnection(); // kick
						// Log in console
						_log.info("#### ATTENCTION ####");
						_log.info(i + " item has been removed from player.");
						return;
					}
				}
				if (itemType2 == L2Item.TYPE2_ACCESSORY)
				{
					if (i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_JEWELRY)
					{
						// Delete Item Over enchanted
						activeChar.getInventory().destroyItem(null, i, activeChar, null);
						// Message to Player
						activeChar.sendMessage("[Server]:You have Items over enchanted you will be kikked!");
						// If Audit is only a Kick, with this the player goes in Jail for 1.200 minutes
						activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, 1200);
						// Punishment e log in audit
						Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " have item Overenchanted ", Config.DEFAULT_PUNISH);
						activeChar.closeNetConnection(); // kick
						// Log in console
						_log.info("#### ATTENCTION ####");
						_log.info(i + " item has been removed from player.");
						return;
					}
				}
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();
		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
					cha.setMaried(true);

				cha.setCoupleId(cl.getId());
				if (cl.getPlayer1Id() == _chaid)
					cha.setPartnerId(cl.getPlayer2Id());
				else
					cha.setPartnerId(cl.getPlayer1Id());
			}
		}
	}

	/**
	 * @param activeChar
	 *            partnerid
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			L2PcInstance partner;
			partner = (L2PcInstance) L2World.getInstance().findObject(cha.getPartnerId());
			if (partner != null)
				partner.sendMessage("Your Partner has logged in");
			partner = null;
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();
			L2PcInstance friend;
			String friendName;
			SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
			sm.addString(cha.getName());
			while (rset.next())
			{
				friendName = rset.getString("friend_name");
				friend = L2World.getInstance().getPlayer(friendName);
				if (friend != null) // friend logged in.
				{
					friend.sendPacket(new FriendList(friend));
					friend.sendPacket(sm);
				}
			}
			sm = null;
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore friend data:" + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
			if (clan.isNoticeEnabled())
				sendPacket(new NpcHtmlMessage(1, "<html><title>Clan Announcements</title><body><br><center><font color=\"CCAA00\">" + activeChar.getClan().getName() + "</font> <font color=\"6655FF\">Clan Alert Message</font></center><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><br>" + activeChar.getClan().getNotice().replaceAll("\r\n", "<br>") + "</body></html>"));
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyCastleOwner(L2PcInstance activeChar)
	{
		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
			L2Clan clan = activeChar.getClan();
			if (clan != null)
			{
				if (clan.getHasCastle() > 0)
				{
					Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());
					if (castle != null && activeChar.getObjectId() == clan.getLeaderId())
						Announcements.getInstance().announceToAll("Castle Lord " + activeChar.getName() + " Of " + castle.getName() + " Castle Is Currently Online.");
				}
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void checkCrown(L2PcInstance activeChar)
	{
		if (activeChar.isClanLeader() && activeChar.getClan().getHasCastle() != 0)
		{
			if (activeChar.getInventory().getItemByItemId(6841) == null && activeChar.getInventory().validateCapacity(1))
			{
				activeChar.getInventory().addItem("Crown", 6841, 1, activeChar, null);
				activeChar.getInventory().updateDatabase();
			}
		}
		else
		{
			if (activeChar.getInventory().getItemByItemId(6841) != null)
				activeChar.getInventory().destroyItemByItemId("Crown", 6841, 1, activeChar, null);
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());
			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());
			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getText(String string)
	{
		try
		{
			String result = new String(Base64.decode(string), "UTF-8");
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			// huh, UTF-8 is not supported? :)
			return null;
		}
	}

	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;
		if (activeChar.getClan() != null)
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);

		if (activeChar.isNoble() && pledgeClass < 5)
			pledgeClass = 5;

		if (activeChar.isHero())
			pledgeClass = 8;

		activeChar.setPledgeClass(pledgeClass);
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}