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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.crypt.nProtect.RestrictionType;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.CharSchemesTable;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.custom.CustomWorldHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.managers.CrownManager;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassLevel;
import com.l2jfrozen.gameserver.model.base.PlayerClass;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.Hero;
import com.l2jfrozen.gameserver.model.entity.Wedding;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.Disconnection;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ClientSetTime;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.Die;
import com.l2jfrozen.gameserver.network.serverpackets.Earthquake;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.HennaInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeStatusChanged;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfrozen.gameserver.network.serverpackets.SignsSky;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.thread.TaskPriority;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format cbdddd
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	private static final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	long _daysleft;
	SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
	
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
		//FIXME: Tyt kaka9ita xyeta, nado razobratsa c paketami pri logine chara

		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}

		// Register in flood protector
		//FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());

		if(L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if(Config.DEBUG)
			{
				_log.warning("User already exist in OID map! User " + activeChar.getName() + " is character clone");
				//activeChar.closeNetConnection();
			}
		}
		
		if(!activeChar.isGM())
		{		
		 if(activeChar.getName().length() < 3 || activeChar.getName().length() > 16 || !Util.isAlphaNumeric(activeChar.getName()) || !isValidName(activeChar.getName()))
		  {
			_log.fine("Charname: " + activeChar.getName() + " is invalid. EnterWorld failed.");
			getClient().closeNow();
			return;

		  }	
		}

		activeChar.setOnlineStatus(true);

		// engage and notify Partner
		if(Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}

		EnterGM(activeChar);

		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());

		if(Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		if(SevenSigns.getInstance().isSealValidationPeriod())
		{
			sendPacket(new SignsSky());
		}

		// buff and status icons
		if(Config.STORE_SKILL_COOLTIME)
		{
			activeChar.restoreEffects();
		}

		activeChar.sendPacket(new EtcStatusUpdate(activeChar));

		if(activeChar.getAllEffects() != null)
		{
			for(L2Effect e : activeChar.getAllEffects())
			{
				if(e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
				if(e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
			}
		}

		// apply augmentation boni for equipped items
		for(L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
		{
			if(temp != null && temp.isEquipped())
			{
				temp.getAugmentation().applyBoni(activeChar);
			}
		}

		if(L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
		{
			L2Event.restoreChar(activeChar);
		}
		else if(L2Event.connectionLossData.containsKey(activeChar.getName()))
		{
			L2Event.restoreAndTeleChar(activeChar);
		}

		//SECURE FIX - Anti Overenchant Cheat!!
		if(Config.MAX_ITEM_ENCHANT_KICK >0)
			for (L2ItemInstance i : activeChar.getInventory().getItems())
			{
				if (!activeChar.isGM())
				{	
					if (i.isEquipable())
					{	
						if (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
						{                        
							//Delete Item Over enchanted
							activeChar.getInventory().destroyItem(null, i, activeChar, null);
							//Message to Player
							activeChar.sendMessage("[Server]:You have over enchanted items you will be kicked from server!");
							activeChar.sendMessage("[Server]:Respect our server rules.");
							//Message with screen
							sendPacket(new ExShowScreenMessage(" You have an over enchanted item, you will be kicked from server! ", 6000));
							//Punishment e log in audit
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);                     
							//Logger in console
							_log.info("#### ATTENTION ####");
							_log.info(i+" item has been removed from "+activeChar);
						}

					}
				}
			}


		//restores custom status
		activeChar.restoreCustomStatus();

		ColorSystem(activeChar);

		//Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);

		activeChar.getMacroses().sendUpdate();

		sendPacket(new ClientSetTime()); // SetClientTime

		sendPacket(new UserInfo(activeChar));

		sendPacket(new HennaInfo(activeChar));

		sendPacket(new FriendList(activeChar));

		sendPacket(new ItemList(activeChar, false));

		sendPacket(new ShortCutInit(activeChar));

		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);
		
		//Credits to L2jfrozen
		activeChar.sendMessage("This server uses L2jFrozen, a project founded by L2Chef and");
		activeChar.sendMessage("developed by the L2jFrozen Dev Team at l2jfrozen.com");

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);

		loadTutorial(activeChar);
		
		// check for crowns
		CrownManager.getInstance().checkCrowns(activeChar);

		// check player skills
		if(Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
	    if(!activeChar.isAio())
	    		activeChar.checkAllowedSkills();

		PetitionManager.getInstance().checkPetitionMessages(activeChar);

		// send user info again .. just like the real client
		//sendPacket(ui);

		if(activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
		}

		if(activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		if(Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}

		if(Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
		{
			activeChar.setIsHero(true);
		}

		setPledgeClass(activeChar);

		SystemMessage sm1 = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm1.addString(activeChar.getName());
		for (String name : activeChar.getFriendList())
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(name);

			if(friend != null) //friend logged in.
				friend.sendPacket(sm1);
		}
		
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);

		activeChar.setTarget(activeChar);
		
		activeChar.onPlayerEnter();

		if(Config.PCB_ENABLE)
		{
			activeChar.showPcBangWindow();
		}

		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
				notifyCastleOwner(activeChar);
		}
		
		if(Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
		}

		if(DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}

		if(activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}

		if(activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));

			for(Siege siege : SiegeManager.getInstance().getSieges())
			{
				if(!siege.getIsInProgress())
				{
					continue;
				}

				if(siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if(siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}

			for(FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
			{
				if(!fortsiege.getIsInProgress())
				{
					continue;
				}

				if(fortsiege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if(fortsiege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}

			// Add message at connexion if clanHall not paid.
			// Possibly this is custom...
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());

			if(clanHall != null)
			{
				if(!clanHall.getPaid())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
				}
			}
		}

		if(!activeChar.isGM() && activeChar.getSiegeState() < 2 && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}

		RegionBBSManager.getInstance().changeCommunityBoard();
		CustomWorldHandler.getInstance().enterWorld(activeChar);

		if(TvT._savePlayers.contains(activeChar.getName()))
		{
			TvT.addDisconnectedPlayer(activeChar);
		}

		if(CTF._savePlayers.contains(activeChar.getName()))
		{
			CTF.addDisconnectedPlayer(activeChar);
		}

		if(DM._savePlayers.contains(activeChar.getName()))
		{
			DM.addDisconnectedPlayer(activeChar);
		}

		if(!activeChar.checkMultiBox()){ //means that it's not ok multiBox situation, so logout
			activeChar.sendMessage("I'm sorry, but multibox is not allowed here.");
			activeChar.logout();
		}
		
		Hellows(activeChar, sm);
		
		if(Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			ClassLevel lvlnow = PlayerClass.values()[activeChar.getClassId().getId()].getLevel();

			if(activeChar.getLevel() >= 20 && lvlnow == ClassLevel.First)
			{
				L2ClassMasterInstance.getInstance().onAction(activeChar);
			}
			else if(activeChar.getLevel() >= 40 && lvlnow == ClassLevel.Second)
			{
				L2ClassMasterInstance.getInstance().onAction(activeChar);
			}
			else if(activeChar.getLevel() >= 76 && lvlnow == ClassLevel.Third)
			{
				L2ClassMasterInstance.getInstance().onAction(activeChar);
			}
		}
		
		// NPCBuffer
		if (PowerPakConfig.BUFFER_ENABLED)
			CharSchemesTable.getInstance().onPlayerLogin(activeChar.getObjectId());
		
		
		if(!nProtect.getInstance().checkRestriction(activeChar, RestrictionType.RESTRICT_ENTER))
		{
			activeChar.setIsImobilised(true);
			activeChar.disableAllSkills();
			ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), 20000);

		}
	}

	private boolean isValidName(String text)
	{
		boolean result = true;

		String test = text;
		Pattern pattern;

		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch(PatternSyntaxException e) // case of illegal pattern
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();

			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}

		Matcher regexp = pattern.matcher(test);
		if(!regexp.matches())
		{
			result = false;
		}

		return result;
	}
	
	private void EnterGM(L2PcInstance activeChar)
	{
		if(activeChar.isGM())
		{
			if(Config.GM_SPECIAL_EFFECT)
			{
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 50, 4);
				activeChar.broadcastPacket(eq);
			}

			if(Config.SHOW_GM_LOGIN)
			{
				String gmname = activeChar.getName();
				String text = "GM " + gmname + " has logged on.";
				Announcements.getInstance().announceToAll(text);
			}

			if(Config.GM_STARTUP_INVULNERABLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}

			if(Config.GM_STARTUP_INVISIBLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.getAppearance().setInvisible();
			}

			if(Config.GM_STARTUP_SILENCE && AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setMessageRefusal(true);
			}

			if(Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}

			activeChar.updateGmNameTitleColor();
		}
	}

	private void Hellows(L2PcInstance activeChar, SystemMessage sm)
	{
		if(Config.ALT_Server_Name_Enabled)
		{
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Welcome to " + Config.ALT_Server_Name);
			sendPacket(sm);
		}

		if(Config.ONLINE_PLAYERS_ON_LOGIN)
		{
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("There are " + L2World.getInstance().getAllPlayers().size() + " players online.");
			sendPacket(sm);
		}

		if(activeChar.getFirstLog() && Config.NEW_PLAYER_EFFECT)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(2025,1);
			if(skill != null)
			{
				MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2025, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				activeChar.useMagic(skill, false, false);
			}
			activeChar.setFirstLog(false);
			activeChar.updateFirstLog();
		}

		 if(Config.WELCOME_HTM && isValidName(activeChar.getName()))
	      {
			String Welcome_Path = "data/html/welcome.htm";
			File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path);
			if(mainText.exists())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", activeChar.getName());
				sendPacket(html);
			}
		  }
		 
		  if ((activeChar.getClan() != null) && activeChar.getClan().isNoticeEnabled())
	       {
	         String clanNotice = "data/html/clanNotice.htm";
	         File mainText = new File(Config.DATAPACK_ROOT, clanNotice);
	         if(mainText.exists())
	         {
	            NpcHtmlMessage html = new NpcHtmlMessage(1);
	            html.setFile(clanNotice);
	            html.replace("%clan_name%", activeChar.getClan().getName());
	            html.replace("%notice_text%", activeChar.getClan().getNotice().replaceAll("\r\n", "<br>"));
	            sendPacket(html);
	         }
	      }

		if(Config.PM_MESSAGE_ON_START)
		{
			CreatureSay np = new CreatureSay(2, Say2.HERO_VOICE,Config.PM_TEXT1,Config.PM_SERVER_NAME);
			CreatureSay na = new CreatureSay(15, Say2.PARTYROOM_COMMANDER,activeChar.getName(),Config.PM_TEXT2);
			activeChar.sendPacket(np);
			activeChar.sendPacket(na);
		}
		
		if(Config.SERVER_TIME_ON_START) {
			activeChar.sendMessage("SVR time is " + fmt.format(new Date(System.currentTimeMillis())));
		}

	}

	private void ColorSystem(L2PcInstance activeChar)
	{
		// =================================================================================
		// Color System checks - Start =====================================================
		// Check if the custom PvP and PK color systems are enabled and if so ==============
		// check the character's counters and apply any color changes that must be done. ===
		//thank Kidzor
		/** KidZor: Ammount 1 **/
		if(activeChar.getPvpKills() >= Config.PVP_AMOUNT1 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if(activeChar.getPkKills() >= Config.PK_AMOUNT1 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 2 **/
		if(activeChar.getPvpKills() >= Config.PVP_AMOUNT2 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if(activeChar.getPkKills() >= Config.PK_AMOUNT2 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 3 **/
		if(activeChar.getPvpKills() >= Config.PVP_AMOUNT3 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if(activeChar.getPkKills() >= Config.PK_AMOUNT3 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 4 **/
		if(activeChar.getPvpKills() >= Config.PVP_AMOUNT4 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if(activeChar.getPkKills() >= Config.PK_AMOUNT4 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 5 **/
		if(activeChar.getPvpKills() >= Config.PVP_AMOUNT5 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if(activeChar.getPkKills() >= Config.PK_AMOUNT5 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
			// Color System checks - End =======================================================
			// =================================================================================
		}

		// Apply color settings to clan leader when entering  
		if(activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if(Config.CLAN_LEADER_COLORED == 1)
			{
				activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
			}
			else
			{
				activeChar.getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
			}
		}

		if(Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
			activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);

		if(Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
			activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);

		if(activeChar.isAio())
			onEnterAio(activeChar);

		
		
		
		activeChar.updateNameTitleColor();

		sendPacket(new UserInfo(activeChar));
		sendPacket(new HennaInfo(activeChar));
		sendPacket(new FriendList(activeChar));
		sendPacket(new ItemList(activeChar, false));
		sendPacket(new ShortCutInit(activeChar));
		activeChar.broadcastUserInfo();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
	}

	private void onEnterAio(L2PcInstance activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getAioEndTime();
		if(now > endDay)
    	{
    		activeChar.setAio(false);
    		activeChar.setAioEndTime(0);
    		activeChar.lostAioSkills();
    		activeChar.sendMessage("Removed your Aio stats... period ends ");
    	}
    	else
    	{
    		Date dt = new Date(endDay);
    		_daysleft = (endDay - now)/86400000;
    		if(_daysleft > 30)
    			activeChar.sendMessage("Aio period ends in " + df.format(dt) + ". enjoy the Game");
    		else if(_daysleft > 0)
    			activeChar.sendMessage("left " + (int)_daysleft + " for Aio period ends");
    		else if(_daysleft < 1)
    		{
    			long hour = (endDay - now)/3600000;
    			activeChar.sendMessage("left " + (int)hour + " hours to Aio period ends");
    		}
    	}
	}
		      
	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		for(Wedding cl : CoupleManager.getInstance().getCouples())
		{
			if(cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if(cl.getMaried())
				{
					cha.setMarried(true);
					cha.setmarriedType(cl.getType());
				}

				cha.setCoupleId(cl.getId());

				if(cl.getPlayer1Id() == _chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}

	/**
	 * @param activeChar partnerid
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if(cha.getPartnerId() != 0)
		{
			L2PcInstance partner = null;
			
			if(L2World.getInstance().findObject(cha.getPartnerId()) instanceof L2PcInstance){
				partner = (L2PcInstance) L2World.getInstance().findObject(cha.getPartnerId());
			}
			
			if(partner != null)
			{
				partner.sendMessage("Your Partner has logged in");
			}
		}
	}

	/**
	 * @param activeChar
	 */
	/*
	private void notifyFriends(L2PcInstance cha)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();

			L2PcInstance friend;
			String friendName;

			SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
			sm.addString(cha.getName());

			while(rset.next())
			{
				friendName = rset.getString("friend_name");

				friend = L2World.getInstance().getPlayer(friendName);

				if(friend != null) //friend logged in.
				{
					friend.sendPacket(new FriendList(friend));
					friend.sendPacket(sm);
				}
			}

			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("could not restore friend data:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
*/
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if(clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if(activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());

			if(sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if(activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());

			if(apprentice != null)
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
	/*private String getText(String string)
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
	}*/

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");

		if(qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}

	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;

		if(activeChar.getClan() != null)
		{
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
		}

		if(activeChar.isNoble() && pledgeClass < 5)
		{
			pledgeClass = 5;
		}

		if(activeChar.isHero())
		{
			pledgeClass = 8;
		}

		activeChar.setPledgeClass(pledgeClass);
	}
	private void notifyCastleOwner(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		
		if (clan != null)
		{
			if (clan.getHasCastle() > 0)
			{
			Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());
			if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
				Announcements.getInstance().announceToAll("Lord " + activeChar.getName() + " Ruler Of " + castle.getName() + " Castle is Now Online!");
			}
		}
	}
}
