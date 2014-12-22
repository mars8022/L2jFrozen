/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.lang.RandomStringUtils;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2PlayerAI;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.cache.WarehouseCacheManager;
import com.l2jfrozen.gameserver.communitybbs.BB.Forum;
import com.l2jfrozen.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.controllers.RecipeController;
import com.l2jfrozen.gameserver.datatables.AccessLevel;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.HeroSkillTable;
import com.l2jfrozen.gameserver.datatables.NobleSkillTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.FishTable;
import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.csv.RecipeTable;
import com.l2jfrozen.gameserver.datatables.sql.AccessLevels;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.handler.ItemHandler;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jfrozen.gameserver.handler.skillhandlers.SiegeFlag;
import com.l2jfrozen.gameserver.handler.skillhandlers.StrSiegeAssault;
import com.l2jfrozen.gameserver.handler.skillhandlers.TakeCastle;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.BlockList;
import com.l2jfrozen.gameserver.model.FishData;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.ItemContainer;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Fishing;
import com.l2jfrozen.gameserver.model.L2Macro;
import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Radar;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.L2Request;
import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.MacroList;
import com.l2jfrozen.gameserver.model.PartyMatchRoom;
import com.l2jfrozen.gameserver.model.PartyMatchRoomList;
import com.l2jfrozen.gameserver.model.PartyMatchWaitingList;
import com.l2jfrozen.gameserver.model.PcFreight;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.PcWarehouse;
import com.l2jfrozen.gameserver.model.PetInventory;
import com.l2jfrozen.gameserver.model.PlayerStatus;
import com.l2jfrozen.gameserver.model.ShortCuts;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.appearance.PcAppearance;
import com.l2jfrozen.gameserver.model.actor.knownlist.PcKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.actor.stat.PcStat;
import com.l2jfrozen.gameserver.model.actor.status.PcStatus;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.base.ClassLevel;
import com.l2jfrozen.gameserver.model.base.PlayerClass;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.model.base.SubClass;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.Duel;
import com.l2jfrozen.gameserver.model.entity.L2Rebirth;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.event.VIP;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingEnd;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingStart;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExSetCompassZoneCode;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.HennaInfo;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ObservationMode;
import com.l2jfrozen.gameserver.network.serverpackets.ObservationReturn;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreListBuy;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreListSell;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeShopSellList;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SendTradeDone;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfrozen.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jfrozen.gameserver.network.serverpackets.SkillList;
import com.l2jfrozen.gameserver.network.serverpackets.Snoop;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.TargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.TitleUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.TradePressOtherOk;
import com.l2jfrozen.gameserver.network.serverpackets.TradePressOwnOk;
import com.l2jfrozen.gameserver.network.serverpackets.TradeStart;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.thread.daemons.ItemsAutoDestroy;
import com.l2jfrozen.gameserver.util.Broadcast;
import com.l2jfrozen.gameserver.util.FloodProtectors;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class represents all player characters in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 * @version $Revision: 1.6.4 $ $Date: 2009/05/12 19:46:09 $
 * @author l2jfrozen dev
 */
public final class L2PcInstance extends L2PlayableInstance
{
	/** The Constant RESTORE_SKILLS_FOR_CHAR. */
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant ADD_NEW_SKILL. */
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
	
	/** The Constant UPDATE_CHARACTER_SKILL_LEVEL. */
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	
	/** The Constant DELETE_SKILL_FROM_CHAR. */
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	
	/** The Constant DELETE_CHAR_SKILLS. */
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant ADD_SKILL_SAVE. */
	// private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	/** The Constant RESTORE_SKILL_SAVE. */
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
	
	/** The Constant DELETE_SKILL_SAVE. */
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	
	/** The _is the vip. */
	public boolean _isVIP = false, _inEventVIP = false, _isNotVIP = false, _isTheVIP = false;
	
	/** The _original karma vip. */
	public int _originalNameColourVIP, _originalKarmaVIP;
	
	/** The _vote timestamp. */
	private long _voteTimestamp = 0;
	
	/** The _posticipate sit. */
	private boolean _posticipateSit;
	
	/** The sitting task launched. */
	protected boolean sittingTaskLaunched;
	
	/** The saved_status. */
	private PlayerStatus saved_status = null;
	
	/** The _instance login time. */
	private final long _instanceLoginTime;
	
	/** The _last teleport action. */
	private long _lastTeleportAction = 0;
	
	/** The TOGGLE_USE time. */
	protected long TOGGLE_USE = 0;
	
	/**
	 * Gets the actual status.
	 * @return the actual status
	 */
	public PlayerStatus getActualStatus()
	{
		
		saved_status = new PlayerStatus(this);
		return saved_status;
		
	}
	
	/**
	 * Gets the last saved status.
	 * @return the last saved status
	 */
	public PlayerStatus getLastSavedStatus()
	{
		
		return saved_status;
		
	}
	
	/**
	 * Gets the vote timestamp.
	 * @return the _voteTimestamp
	 */
	public long getVoteTimestamp()
	{
		return _voteTimestamp;
	}
	
	/**
	 * Sets the vote timestamp.
	 * @param timestamp the _voteTimestamp to set
	 */
	public void setVoteTimestamp(final long timestamp)
	{
		_voteTimestamp = timestamp;
	}
	
	/**
	 * Gets the vote points.
	 * @return the vote points
	 */
	public int getVotePoints()
	{
		Connection con = null;
		int votePoints = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("select votePoints from accounts where login=?");
			statement.setString(1, _accountName);
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				votePoints = rset.getInt("votePoints");
			}
			DatabaseUtils.close(rset);
			rset = null;
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		return votePoints;
	}
	
	/**
	 * Sets the vote points.
	 * @param points the new vote points
	 */
	public void setVotePoints(final int points)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("update accounts set votePoints=" + points + " where login='" + _accountName + "'");
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Gets the vote time.
	 * @return the vote time
	 */
	public int getVoteTime()
	{
		Connection con = null;
		int lastVote = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("select lastVote from accounts where login=?");
			statement.setString(1, _accountName);
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				lastVote = rset.getInt("lastVote");
			}
			DatabaseUtils.close(rset);
			rset = null;
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		return lastVote;
	}
	
	/** The _active_boxes. */
	public int _active_boxes = -1;
	
	/** The active_boxes_characters. */
	public List<String> active_boxes_characters = new ArrayList<>();
	
	/**
	 * UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=? ,face=?,hairStyle=?,hairColor =?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have =?,rec_left=?,clanid=?,maxload
	 * =?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs =?,wantspeace=?,base_class =?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date =?,lvl_joined_academy
	 * =?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=? ,char_name=?,death_penalty_level=?,good=?,evil=?,gve_kills=? WHERE obj_id=?.
	 */
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,pc_point=?,name_color=?,title_color=?,aio=?,aio_end=? WHERE obj_id=?";
	
	/**
	 * SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier,
	 * colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie,
	 * nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,good,evil,gve_kills FROM characters WHERE obj_id=?.
	 */
	// private static final String RESTORE_CHARACTER =
	// "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,pc_point,banchat_time,name_color,title_color,first_log,aio,aio_end FROM characters WHERE obj_id=?";
	private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon,punish_level,punish_timer," + /*
																																																																																																																																																													 * in_jail
																																																																																																																																																													 * ,
																																																																																																																																																													 * jail_timer
																																																																																																																																																													 * ,
																																																																																																																																																													 */"newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,pc_point" + /*
																																																																																																																																																																																																														 * ,
																																																																																																																																																																																																														 * banchat_time
																																																																																																																																																																																																														 */",name_color,title_color,first_log,aio,aio_end FROM characters WHERE obj_id=?";
	
	/** The Constant STATUS_DATA_GET. */
	private static final String STATUS_DATA_GET = "SELECT hero, noble, donator, hero_end_date FROM characters_custom_data WHERE obj_Id = ?";
	
	/** The Constant RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS. */
	private static final String RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? ORDER BY (skill_level+0)";
	
	// ---------------------- L2JFrozen Addons ---------------------------------- //
	/** The Constant RESTORE_CHAR_SUBCLASSES. */
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	
	/** The Constant ADD_CHAR_SUBCLASS. */
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	
	/** The Constant UPDATE_CHAR_SUBCLASS. */
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
	
	/** The Constant DELETE_CHAR_SUBCLASS. */
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant RESTORE_CHAR_HENNAS. */
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant ADD_CHAR_HENNA. */
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	
	/** The Constant DELETE_CHAR_HENNA. */
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	
	/** The Constant DELETE_CHAR_HENNAS. */
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant DELETE_CHAR_SHORTCUTS. */
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	
	/** The Constant RESTORE_CHAR_RECOMS. */
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	
	/** The Constant ADD_CHAR_RECOM. */
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	
	/** The Constant DELETE_CHAR_RECOMS. */
	private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
	
	/** The Constant REQUEST_TIMEOUT. */
	public static final int REQUEST_TIMEOUT = 15;
	
	/** The Constant STORE_PRIVATE_NONE. */
	public static final int STORE_PRIVATE_NONE = 0;
	
	/** The Constant STORE_PRIVATE_SELL. */
	public static final int STORE_PRIVATE_SELL = 1;
	
	/** The Constant STORE_PRIVATE_BUY. */
	public static final int STORE_PRIVATE_BUY = 3;
	
	/** The Constant STORE_PRIVATE_MANUFACTURE. */
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	
	/** The Constant STORE_PRIVATE_PACKAGE_SELL. */
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	
	/** The fmt. */
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	
	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S). */
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeTable.getInstance().getExpertiseLevel(1), // D
		SkillTreeTable.getInstance().getExpertiseLevel(2), // C
		SkillTreeTable.getInstance().getExpertiseLevel(3), // B
		SkillTreeTable.getInstance().getExpertiseLevel(4), // A
		SkillTreeTable.getInstance().getExpertiseLevel(5), // S
	};
	
	/** The Constant COMMON_CRAFT_LEVELS. */
	private static final int[] COMMON_CRAFT_LEVELS =
	{
		5,
		20,
		28,
		36,
		43,
		49,
		55,
		62
	};
	
	// private static Logger LOGGER = Logger.getLogger(L2PcInstance.class);
	
	/**
	 * The Class AIAccessor.
	 */
	public class AIAccessor extends L2Character.AIAccessor
	{
		
		/**
		 * Instantiates a new aI accessor.
		 */
		protected AIAccessor()
		{
		}
		
		/**
		 * Gets the player.
		 * @return the player
		 */
		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}
		
		/**
		 * Do pickup item.
		 * @param object the object
		 */
		public void doPickupItem(final L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
		}
		
		/**
		 * Do interact.
		 * @param target the target
		 */
		public void doInteract(final L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#doAttack(com.l2jfrozen.gameserver.model.L2Character)
		 */
		@Override
		public void doAttack(final L2Character target)
		{
			if (isInsidePeaceZone(L2PcInstance.this, target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// during teleport phase, players cant do any attack
			if ((TvT.is_teleport() && _inEventTvT) || (CTF.is_teleport() && _inEventCTF) || (DM.is_teleport() && _inEventDM))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Pk protection config
			if (!isGM() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 0 && ((L2PcInstance) target).getKarma() == 0 && (getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL || target.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL))
			{
				sendMessage("You can't hit a player that is lower level from you. Target's level: " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doAttack(target);
			
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			
			synchronized (_cubics)
			{
				for (final L2CubicInstance cubic : _cubics.values())
					if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					{
						cubic.doAction();
					}
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#doCast(com.l2jfrozen.gameserver.model.L2Skill)
		 */
		@Override
		public void doCast(final L2Skill skill)
		{
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			if (skill == null)
				return;
			
			// Like L2OFF you can use cupid bow skills on peace zone
			// Like L2OFF players can use TARGET_AURA skills on peace zone, all targets will be ignored.
			if (skill.isOffensive() && (isInsidePeaceZone(L2PcInstance.this, getTarget()) && skill.getTargetType() != SkillTargetType.TARGET_AURA) && (skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262)) // check limited to active target
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
				
			}
			
			// during teleport phase, players cant do any attack
			if ((TvT.is_teleport() && _inEventTvT) || (CTF.is_teleport() && _inEventCTF) || (DM.is_teleport() && _inEventDM))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doCast(skill);
			
			if (!skill.isOffensive())
				return;
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					L2Object mainTarget = skill.getFirstOfTargetList(L2PcInstance.this);
					if (mainTarget == null || !(mainTarget instanceof L2Character))
						return;
					
					synchronized (_cubics)
					{
						for (final L2CubicInstance cubic : _cubics.values())
							if (cubic != null && cubic.getId() != L2CubicInstance.LIFE_CUBIC)
							{
								cubic.doAction();
							}
					}
					
					mainTarget = null;
				}
					break;
			}
		}
	}
	
	/** The _client. */
	private L2GameClient _client;
	
	/** The _account name. */
	private String _accountName;
	
	/** The _delete timer. */
	private long _deleteTimer;
	
	/** The _is online. */
	private boolean _isOnline = false;
	
	/** The _online time. */
	private long _onlineTime;
	
	/** The _online begin time. */
	private long _onlineBeginTime;
	
	/** The _last access. */
	private long _lastAccess;
	
	/** The _uptime. */
	private long _uptime;
	
	/** The _base class. */
	protected int _baseClass;
	
	/** The _active class. */
	protected int _activeClass;
	
	/** The _class index. */
	protected int _classIndex = 0;
	
	/** Fireworks on first login. */
	private boolean _first_log;
	
	/** PC BANG POINT. */
	private int pcBangPoint = 0;
	
	/** The list of sub-classes this character has. */
	private Map<Integer, SubClass> _subClasses;
	
	/** The _appearance. */
	private PcAppearance _appearance;
	
	/** The Identifier of the L2PcInstance. */
	private int _charId = 0x00030b7a;
	
	/** The Experience of the L2PcInstance before the last Death Penalty. */
	private long _expBeforeDeath;
	
	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red). */
	private int _karma;
	
	/** The number of player killed during a PvP (the player killed was PvP Flagged). */
	private int _pvpKills;
	
	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed). */
	private int _pkKills;
	
	/** The _last kill. */
	private int _lastKill = 0;
	
	/** The count. */
	private int count = 0;
	
	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple). */
	private byte _pvpFlag;
	
	/** The Siege state of the L2PcInstance. */
	private byte _siegeState = 0;
	
	/** The _cur weight penalty. */
	private int _curWeightPenalty = 0;
	
	/** The _last compass zone. */
	private int _lastCompassZone; // the last compass zone update send to the client
	
	/** The _zone validate counter. */
	private byte _zoneValidateCounter = 4;
	
	/** The _is in7s dungeon. */
	private boolean _isIn7sDungeon = false;
	
	// private boolean _inJail = false;
	// private long _jailTimer = 0;
	// private ScheduledFuture<?> _jailTask;
	
	/** Special hero aura values. */
	private int heroConsecutiveKillCount = 0;
	
	/** The is pvp hero. */
	private boolean isPVPHero = false;
	
	/** character away mode *. */
	private boolean _awaying = false;
	
	/** The _is away. */
	private boolean _isAway = false;
	
	/** The _original title color away. */
	public int _originalTitleColorAway;
	
	/** The _original title away. */
	public String _originalTitleAway;
	
	/** The _is aio. */
	private boolean _isAio = false;
	
	/** The _aio_end time. */
	private long _aio_endTime = 0;
	
	/** Event parameters. */
	public int eventX;
	
	/** The event y. */
	public int eventY;
	
	/** The event z. */
	public int eventZ;
	
	/** The event karma. */
	public int eventKarma;
	
	/** The event pvp kills. */
	public int eventPvpKills;
	
	/** The event pk kills. */
	public int eventPkKills;
	
	/** The event title. */
	public String eventTitle;
	
	/** The kills. */
	public List<String> kills = new LinkedList<>();
	
	/** The event sit forced. */
	public boolean eventSitForced = false;
	
	/** The at event. */
	public boolean atEvent = false;
	
	/** TvT Engine parameters. */
	public String _teamNameTvT, _originalTitleTvT;
	
	/** The _original karma tv t. */
	public int _originalNameColorTvT = 0, _countTvTkills, _countTvTdies, _originalKarmaTvT;
	
	/** The _in event tv t. */
	public boolean _inEventTvT = false;
	
	/** CTF Engine parameters. */
	public String _teamNameCTF, _teamNameHaveFlagCTF, _originalTitleCTF;
	
	/** The _count ct fflags. */
	public int _originalNameColorCTF = 0, _originalKarmaCTF, _countCTFflags;
	
	/** The _have flag ctf. */
	public boolean _inEventCTF = false, _haveFlagCTF = false;
	
	/** The _pos checker ctf. */
	public Future<?> _posCheckerCTF = null;
	
	/** DM Engine parameters. */
	public String _originalTitleDM;
	
	/** The _original karma dm. */
	public int _originalNameColorDM = 0, _countDMkills, _originalKarmaDM;
	
	/** The _in event dm. */
	public boolean _inEventDM = false;
	
	/** The _correct word. */
	public int _correctWord = -1;
	
	/** The _stop kick bot task. */
	public boolean _stopKickBotTask = false;
	
	/** Event Engine parameters. */
	public int _originalNameColor, _countKills, _originalKarma, _eventKills;
	
	/** The _in event. */
	public boolean _inEvent = false;
	
	/** Olympiad. */
	private boolean _inOlympiadMode = false;
	
	/** The _ olympiad start. */
	private boolean _OlympiadStart = false;
	
	/** The _ olympiad position. */
	private int[] _OlympiadPosition;
	
	/** The _olympiad game id. */
	private int _olympiadGameId = -1;
	
	/** The _olympiad side. */
	private int _olympiadSide = -1;
	
	/** The dmg dealt. */
	// public int dmgDealt = 0;
	
	/** Duel. */
	private boolean _isInDuel = false;
	
	/** The _duel state. */
	private int _duelState = Duel.DUELSTATE_NODUEL;
	
	/** The _duel id. */
	private int _duelId = 0;
	
	/** The _no duel reason. */
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	/** Boat. */
	private boolean _inBoat;
	
	/** The _boat. */
	private L2BoatInstance _boat;
	
	/** The _in boat position. */
	private Point3D _inBoatPosition;
	
	/** The _mount type. */
	private int _mountType;
	
	/** Store object used to summon the strider you are mounting *. */
	private int _mountObjectID = 0;
	
	/** The _telemode. */
	public int _telemode = 0;
	
	/** The _is silent moving. */
	private int _isSilentMoving = 0;
	
	/** The _in crystallize. */
	private boolean _inCrystallize;
	
	/** The _in craft mode. */
	private boolean _inCraftMode;
	
	/** The table containing all L2RecipeList of the L2PcInstance. */
	private final Map<Integer, L2RecipeList> _dwarvenRecipeBook = new FastMap<>();
	
	/** The _common recipe book. */
	private final Map<Integer, L2RecipeList> _commonRecipeBook = new FastMap<>();
	
	/** True if the L2PcInstance is sitting. */
	private boolean _waitTypeSitting;
	
	/** True if the L2PcInstance is using the relax skill. */
	private boolean _relax;
	
	/** Location before entering Observer Mode. */
	private int _obsX;
	
	/** The _obs y. */
	private int _obsY;
	
	/** The _obs z. */
	private int _obsZ;
	
	/** The _observer mode. */
	private boolean _observerMode = false;
	
	/** Stored from last ValidatePosition *. */
	private Location _lastClientPosition = new Location(0, 0, 0);
	
	/** The _last server position. */
	private Location _lastServerPosition = new Location(0, 0, 0);
	
	/** The number of recommandation obtained by the L2PcInstance. */
	private int _recomHave; // how much I was recommended by others
	
	/** The number of recommandation that the L2PcInstance can give. */
	private int _recomLeft; // how many recomendations I can give to others
	
	/** Date when recom points were updated last time. */
	private long _lastRecomUpdate;
	
	/** List with the recomendations that I've give. */
	private final List<Integer> _recomChars = new FastList<>();
	
	/** The random number of the L2PcInstance. */
	// private static final Random _rnd = new Random();
	
	private final PcInventory _inventory = new PcInventory(this);
	
	/** The _warehouse. */
	private PcWarehouse _warehouse;
	
	/** The _freight. */
	private final PcFreight _freight = new PcFreight(this);
	
	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5). */
	private int _privatestore;
	
	/** The _active trade list. */
	private TradeList _activeTradeList;
	
	/** The _active warehouse. */
	private ItemContainer _activeWarehouse;
	
	/** The _create list. */
	private L2ManufactureList _createList;
	
	/** The _sell list. */
	private TradeList _sellList;
	
	/** The _buy list. */
	private TradeList _buyList;
	
	/** True if the L2PcInstance is newbie. */
	private boolean _newbie;
	
	/** The _noble. */
	private boolean _noble = false;
	
	/** The _hero. */
	private boolean _hero = false;
	
	/** The _donator. */
	private boolean _donator = false;
	
	/** The L2FolkInstance corresponding to the last Folk wich one the player talked. */
	private L2FolkInstance _lastFolkNpc = null;
	
	/** Last NPC Id talked on a quest. */
	private int _questNpcObject = 0;
	
	private int _party_find = 0;
	
	// summon friend
	/** The _summon request. */
	private final SummonRequest _summonRequest = new SummonRequest();
	
	/**
	 * The Class SummonRequest.
	 */
	protected static class SummonRequest
	{
		/** The _target. */
		private L2PcInstance _target = null;
		
		/** The _skill. */
		private L2Skill _skill = null;
		
		/**
		 * Sets the target.
		 * @param destination the destination
		 * @param skill the skill
		 */
		public void setTarget(final L2PcInstance destination, final L2Skill skill)
		{
			_target = destination;
			_skill = skill;
		}
		
		/**
		 * Gets the target.
		 * @return the target
		 */
		public L2PcInstance getTarget()
		{
			return _target;
		}
		
		/**
		 * Gets the skill.
		 * @return the skill
		 */
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	/** The table containing all Quests began by the L2PcInstance. */
	private final Map<String, QuestState> _quests = new FastMap<>();
	
	/** The list containing all shortCuts of this L2PcInstance. */
	private final ShortCuts _shortCuts = new ShortCuts(this);
	
	/** The list containing all macroses of this L2PcInstance. */
	private final MacroList _macroses = new MacroList(this);
	
	/** The _snoop listener. */
	private final List<L2PcInstance> _snoopListener = new FastList<>();
	
	/** The _snooped player. */
	private final List<L2PcInstance> _snoopedPlayer = new FastList<>();
	
	/** The _skill learning class id. */
	private ClassId _skillLearningClassId;
	
	// hennas
	/** The _henna. */
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
	
	/** The _henna str. */
	private int _hennaSTR;
	
	/** The _henna int. */
	private int _hennaINT;
	
	/** The _henna dex. */
	private int _hennaDEX;
	
	/** The _henna men. */
	private int _hennaMEN;
	
	/** The _henna wit. */
	private int _hennaWIT;
	
	/** The _henna con. */
	private int _hennaCON;
	
	/** The L2Summon of the L2PcInstance. */
	private L2Summon _summon = null;
	// apparently, a L2PcInstance CAN have both a summon AND a tamed beast at the same time!!
	/** The _tamed beast. */
	private L2TamedBeastInstance _tamedBeast = null;
	
	// client radar
	/** The _radar. */
	private L2Radar _radar;
	
	// Clan related attributes
	/** The Clan Identifier of the L2PcInstance. */
	private int _clanId = 0;
	
	/** The Clan object of the L2PcInstance. */
	private L2Clan _clan;
	
	/** Apprentice and Sponsor IDs. */
	private int _apprentice = 0;
	
	/** The _sponsor. */
	private int _sponsor = 0;
	
	/** The _clan join expiry time. */
	private long _clanJoinExpiryTime;
	
	/** The _clan create expiry time. */
	private long _clanCreateExpiryTime;
	
	/** The _power grade. */
	private int _powerGrade = 0;
	
	/** The _clan privileges. */
	private int _clanPrivileges = 0;
	
	/** L2PcInstance's pledge class (knight, Baron, etc.) */
	private int _pledgeClass = 0;
	
	/** The _pledge type. */
	private int _pledgeType = 0;
	
	/** Level at which the player joined the clan as an academy member. */
	private int _lvlJoinedAcademy = 0;
	
	/** The _wants peace. */
	private int _wantsPeace = 0;
	
	// Death Penalty Buff Level
	/** The _death penalty buff level. */
	private int _deathPenaltyBuffLevel = 0;
	
	// private int _ChatFilterCount = 0;
	
	// GM related variables
	// private boolean _isGm;
	/** The _access level. */
	private AccessLevel _accessLevel;
	
	// private boolean _chatBanned = false; // Chat Banned
	// private ScheduledFuture<?> _chatUnbanTask = null;
	/** The _message refusal. */
	private boolean _messageRefusal = false; // message refusal mode
	
	/** The _diet mode. */
	private boolean _dietMode = false; // ignore weight penalty
	
	/** The _exchange refusal. */
	private boolean _exchangeRefusal = false; // Exchange refusal
	
	/** The _party. */
	private L2Party _party;
	
	private long _lastAttackPacket = 0;
	
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	/** The _active requester. */
	private L2PcInstance _activeRequester;
	
	/** The _request expire time. */
	private long _requestExpireTime = 0;
	
	/** The _request. */
	private final L2Request _request = new L2Request(this);
	
	/** The _arrow item. */
	private L2ItemInstance _arrowItem;
	
	// Used for protection after teleport
	/** The _protect end time. */
	private long _protectEndTime = 0;
	
	public boolean isSpawnProtected()
	{
		return _protectEndTime > GameTimeController.getGameTicks();
	}
	
	private long _teleportProtectEndTime = 0;
	
	public boolean isTeleportProtected()
	{
		return _teleportProtectEndTime > GameTimeController.getGameTicks();
	}
	
	// protects a char from agro mobs when getting up from fake death
	/** The _recent fake death end time. */
	private long _recentFakeDeathEndTime = 0;
	
	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equiped). */
	private L2Weapon _fistsWeaponItem;
	
	/** The _chars. */
	private final Map<Integer, String> _chars = new FastMap<>();
	
	// private byte _updateKnownCounter = 0;
	
	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5). */
	private int _expertiseIndex; // index in EXPERTISE_LEVELS
	
	/** The _expertise penalty. */
	private int _expertisePenalty = 0;
	
	/** The _heavy_mastery. */
	private boolean _heavy_mastery = false;
	
	/** The _light_mastery. */
	private boolean _light_mastery = false;
	
	/** The _robe_mastery. */
	private boolean _robe_mastery = false;
	
	/** The _mastery penalty. */
	private int _masteryPenalty = 0;
	
	/** The _active enchant item. */
	private L2ItemInstance _activeEnchantItem = null;
	
	/** The _inventory disable. */
	protected boolean _inventoryDisable = false;
	
	/** The _cubics. */
	protected Map<Integer, L2CubicInstance> _cubics = new FastMap<>();
	
	/** Active shots. A FastSet variable would actually suffice but this was changed to fix threading stability... */
	protected Map<Integer, Integer> _activeSoulShots = new FastMap<Integer, Integer>().shared();
	
	/** The soul shot lock. */
	public final ReentrantLock soulShotLock = new ReentrantLock();
	
	/** The dialog. */
	public Quest dialog = null;
	
	/** new loto ticket *. */
	private final int _loto[] = new int[5];
	// public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	/** new race ticket *. */
	private final int _race[] = new int[2];
	
	/** The _block list. */
	private final BlockList _blockList = new BlockList(this);
	
	/** The _team. */
	private int _team = 0;
	
	/** lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra. */
	private int _alliedVarkaKetra = 0;
	
	/** ********************************************************************* Adventurers' coupon (0-no 1-NG 2-D 3-NG & D) 0 = No coupon 1 = coupon for No Grade 2 = coupon for D Grade 3 = coupon for No & D Grade ********************************************************************. */
	private int _hasCoupon = 0;
	
	/** The _fish combat. */
	private L2Fishing _fishCombat;
	
	/** The _fishing. */
	private boolean _fishing = false;
	
	/** The _fishx. */
	private int _fishx = 0;
	
	/** The _fishy. */
	private int _fishy = 0;
	
	/** The _fishz. */
	private int _fishz = 0;
	
	/** The _task rent pet. */
	private ScheduledFuture<?> _taskRentPet;
	
	/** The _task water. */
	private ScheduledFuture<?> _taskWater;
	
	/** Bypass validations. */
	private final List<String> _validBypass = new FastList<>();
	
	/** The _valid bypass2. */
	private final List<String> _validBypass2 = new FastList<>();
	
	/** The _valid link. */
	private final List<String> _validLink = new FastList<>();
	
	/** The _forum mail. */
	private Forum _forumMail;
	
	/** The _forum memo. */
	private Forum _forumMemo;
	
	/** Current skill in use. */
	private SkillDat _currentSkill;
	private SkillDat _currentPetSkill;
	
	/** Skills queued because a skill is already in progress. */
	private SkillDat _queuedSkill;
	
	/* Flag to disable equipment/skills while wearing formal wear * */
	/** The _ is wearing formal wear. */
	private boolean _IsWearingFormalWear = false;
	
	/** The _current skill world position. */
	private Point3D _currentSkillWorldPosition;
	
	/** The _cursed weapon equiped id. */
	private int _cursedWeaponEquipedId = 0;
	// private boolean _combatFlagEquippedId = false;
	
	/** The _revive requested. */
	private int _reviveRequested = 0;
	
	/** The _revive power. */
	private double _revivePower = 0;
	
	/** The _revive pet. */
	private boolean _revivePet = false;
	
	/** The _cp update inc check. */
	private double _cpUpdateIncCheck = .0;
	
	/** The _cp update dec check. */
	private double _cpUpdateDecCheck = .0;
	
	/** The _cp update interval. */
	private double _cpUpdateInterval = .0;
	
	/** The _mp update inc check. */
	private double _mpUpdateIncCheck = .0;
	
	/** The _mp update dec check. */
	private double _mpUpdateDecCheck = .0;
	
	/** The _mp update interval. */
	private double _mpUpdateInterval = .0;
	
	private long timerToAttack;
	
	// private boolean isInDangerArea;
	// //////////////////////////////////////////////////////////////////
	// START CHAT BAN SYSTEM
	// //////////////////////////////////////////////////////////////////
	// private long _chatBanTimer = 0L;
	// private ScheduledFuture<?> _chatBanTask = null;
	// //////////////////////////////////////////////////////////////////
	// END CHAT BAN SYSTEM
	// //////////////////////////////////////////////////////////////////
	
	/** The _is offline. */
	private boolean _isInOfflineMode = false;
	
	/** The _is trade off. */
	private boolean _isTradeOff = false;
	
	/** The _offline shop start. */
	private long _offlineShopStart = 0;
	
	/** The _original name color offline. */
	public int _originalNameColorOffline = 0xFFFFFF;
	
	/** Herbs Task Time *. */
	private int _herbstask = 0;
	
	/**
	 * Task for Herbs.
	 */
	public class HerbTask implements Runnable
	{
		
		/** The _process. */
		private final String _process;
		
		/** The _item id. */
		private final int _itemId;
		
		/** The _count. */
		private final int _count;
		
		/** The _reference. */
		private final L2Object _reference;
		
		/** The _send message. */
		private final boolean _sendMessage;
		
		/**
		 * Instantiates a new herb task.
		 * @param process the process
		 * @param itemId the item id
		 * @param count the count
		 * @param reference the reference
		 * @param sendMessage the send message
		 */
		HerbTask(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
		{
			_process = process;
			_itemId = itemId;
			_count = count;
			_reference = reference;
			_sendMessage = sendMessage;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void run()
		{
			try
			{
				addItem(_process, _itemId, _count, _reference, _sendMessage);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.warn("", t);
			}
		}
	}
	
	// L2JMOD Wedding
	/** The _married. */
	private boolean _married = false;
	
	/** The _married type. */
	private int _marriedType = 0;
	
	/** The _partner id. */
	private int _partnerId = 0;
	
	/** The _couple id. */
	private int _coupleId = 0;
	
	/** The _engagerequest. */
	private boolean _engagerequest = false;
	
	/** The _engageid. */
	private int _engageid = 0;
	
	/** The _marryrequest. */
	private boolean _marryrequest = false;
	
	/** The _marryaccepted. */
	private boolean _marryaccepted = false;
	
	/** Quake System. */
	private int quakeSystem = 0;
	
	/** The _is locked. */
	private boolean _isLocked = false;
	
	/** The _is stored. */
	private boolean _isStored = false;
	
	/**
	 * Skill casting information (used to queue when several skills are cast in a short time) *.
	 */
	public class SkillDat
	{
		
		/** The _skill. */
		private final L2Skill _skill;
		
		/** The _ctrl pressed. */
		private final boolean _ctrlPressed;
		
		/** The _shift pressed. */
		private final boolean _shiftPressed;
		
		/**
		 * Instantiates a new skill dat.
		 * @param skill the skill
		 * @param ctrlPressed the ctrl pressed
		 * @param shiftPressed the shift pressed
		 */
		protected SkillDat(final L2Skill skill, final boolean ctrlPressed, final boolean shiftPressed)
		{
			_skill = skill;
			_ctrlPressed = ctrlPressed;
			_shiftPressed = shiftPressed;
		}
		
		/**
		 * Checks if is ctrl pressed.
		 * @return true, if is ctrl pressed
		 */
		public boolean isCtrlPressed()
		{
			return _ctrlPressed;
		}
		
		/**
		 * Checks if is shift pressed.
		 * @return true, if is shift pressed
		 */
		public boolean isShiftPressed()
		{
			return _shiftPressed;
		}
		
		/**
		 * Gets the skill.
		 * @return the skill
		 */
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		/**
		 * Gets the skill id.
		 * @return the skill id
		 */
		public int getSkillId()
		{
			return getSkill() != null ? getSkill().getId() : -1;
		}
	}
	
	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a new L2PcInstance with an account name</li> <li>Set the name, the Hair Style, the Hair Color and the Face type of the L2PcInstance</li> <li>Add the player in the characters table of the database</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the L2PcInstance
	 * @param name The name of the L2PcInstance
	 * @param hairStyle The hair style Identifier of the L2PcInstance
	 * @param hairColor The hair color Identifier of the L2PcInstance
	 * @param face The face type Identifier of the L2PcInstance
	 * @param sex the sex
	 * @return The L2PcInstance added to the database or null
	 */
	public static L2PcInstance create(final int objectId, final L2PcTemplate template, final String accountName, final String name, final byte hairStyle, final byte hairColor, final byte face, final boolean sex)
	{
		// Create a new L2PcInstance with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		final L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);
		app = null;
		
		// Set the name of the L2PcInstance
		player.setName(name);
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
		{
			player.setNewbie(true);
		}
		
		// Add the player in the characters table of the database
		final boolean ok = player.createDb();
		
		if (!ok)
			return null;
		
		return player;
	}
	
	/**
	 * Creates the dummy player.
	 * @param objectId the object id
	 * @param name the name
	 * @return the l2 pc instance
	 */
	public static L2PcInstance createDummyPlayer(final int objectId, final String name)
	{
		// Create a new L2PcInstance with an account name
		final L2PcInstance player = new L2PcInstance(objectId);
		player.setName(name);
		
		return player;
	}
	
	/**
	 * Gets the account name.
	 * @return the account name
	 */
	public String getAccountName()
	{
		if (getClient() != null)
			return getClient().getAccountName();
		return _accountName;
	}
	
	/**
	 * Gets the account chars.
	 * @return the account chars
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	/**
	 * Gets the relation.
	 * @param target the target
	 * @return the relation
	 */
	public int getRelation(final L2PcInstance target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
		{
			result |= RelationChanged.RELATION_PVP_FLAG;
		}
		if (getKarma() > 0)
		{
			result |= RelationChanged.RELATION_HAS_KARMA;
		}
		
		if (isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
			{
				result |= RelationChanged.RELATION_ENEMY;
			}
			else
			{
				result |= RelationChanged.RELATION_ALLY;
			}
			if (getSiegeState() == 1)
			{
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}
		
		if (getClan() != null && target.getClan() != null)
		{
			if (target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && getPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
				{
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world (call restore method).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database</li> <li>Add the L2PcInstance object in _allObjects</li> <li>Set the x,y,z position of the L2PcInstance and make it invisible</li> <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @return The L2PcInstance loaded from the database
	 */
	public static L2PcInstance load(final int objectId)
	{
		return restore(objectId);
	}
	
	/**
	 * Inits the pc status update values.
	 */
	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance</li> <li>Set the name of the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 * @param app the app
	 */
	private L2PcInstance(final int objectId, final L2PcTemplate template, final String accountName, final PcAppearance app)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_accountName = accountName;
		_appearance = app;
		
		// Create an AI
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
		
		// Create a L2Radar object
		_radar = new L2Radar(this);
		
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills
		// Retrieve from the database all items of this L2PcInstance and add them to _inventory
		getInventory().restore();
		if (!Config.WAREHOUSE_CACHE)
		{
			getWarehouse();
		}
		getFreight().restore();
		
		_instanceLoginTime = System.currentTimeMillis();
	}
	
	/**
	 * Instantiates a new l2 pc instance.
	 * @param objectId the object id
	 */
	private L2PcInstance(final int objectId)
	{
		super(objectId, null);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_instanceLoginTime = System.currentTimeMillis();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance#getKnownList()
	 */
	@Override
	public final PcKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof PcKnownList))
		{
			setKnownList(new PcKnownList(this));
		}
		return (PcKnownList) super.getKnownList();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance#getStat()
	 */
	@Override
	public final PcStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof PcStat))
		{
			setStat(new PcStat(this));
		}
		return (PcStat) super.getStat();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance#getStatus()
	 */
	@Override
	public final PcStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof PcStatus))
		{
			setStatus(new PcStatus(this));
		}
		return (PcStatus) super.getStatus();
	}
	
	/**
	 * Gets the appearance.
	 * @return the appearance
	 */
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	/**
	 * Return the base L2PcTemplate link to the L2PcInstance.<BR>
	 * <BR>
	 * @return the base template
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass);
	}
	
	/**
	 * Return the L2PcTemplate link to the L2PcInstance.
	 * @return the template
	 */
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}
	
	/**
	 * Sets the template.
	 * @param newclass the new template
	 */
	public void setTemplate(final ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}
	
	public void setTimerToAttack(final long time)
	{
		timerToAttack = time;
	}
	
	public long getTimerToAttack()
	{
		return timerToAttack;
	}
	
	/**
	 * Return the AI of the L2PcInstance (create it if necessary).<BR>
	 * <BR>
	 * @return the aI
	 */
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
				}
			}
		}
		
		return _ai;
	}
	
	/**
	 * Calculate a destination to explore the area and set the AI Intension to AI_INTENTION_MOVE_TO.<BR>
	 * <BR>
	 * @return the level
	 */
	/*
	 * TODO public void explore() { if(!_exploring) return; if(getMountType() == 2) return; // Calculate the destination point (random) int x = getX() + Rnd.nextInt(6000) - 3000; int y = getY() + Rnd.nextInt(6000) - 3000; if(x > Universe.MAX_X) { x = Universe.MAX_X; } if(x < Universe.MIN_X) { x =
	 * Universe.MIN_X; } if(y > Universe.MAX_Y) { y = Universe.MAX_Y; } if(y < Universe.MIN_Y) { y = Universe.MIN_Y; } int z = getZ(); L2CharPosition pos = new L2CharPosition(x, y, z, 0); // Set the AI Intention to AI_INTENTION_MOVE_TO getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
	 * pos = null; }
	 */
	
	/** Return the Level of the L2PcInstance. */
	@Override
	public final int getLevel()
	{
		int level = getStat().getLevel();
		
		if (level == -1)
		{
			
			final L2PcInstance local_char = restore(this.getObjectId());
			
			if (local_char != null)
				level = local_char.getLevel();
			
		}
		
		if (level < 0)
			level = 1;
		
		return level;
	}
	
	/**
	 * Return the _newbie state of the L2PcInstance.<BR>
	 * <BR>
	 * @return true, if is newbie
	 */
	public boolean isNewbie()
	{
		return _newbie;
	}
	
	/**
	 * Set the _newbie state of the L2PcInstance.<BR>
	 * <BR>
	 * @param isNewbie The Identifier of the _newbie state<BR>
	 * <BR>
	 */
	public void setNewbie(final boolean isNewbie)
	{
		_newbie = isNewbie;
	}
	
	/**
	 * Sets the base class.
	 * @param baseClass the new base class
	 */
	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}
	
	/**
	 * Sets the base class.
	 * @param classId the new base class
	 */
	public void setBaseClass(final ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	/**
	 * Checks if is in store mode.
	 * @return true, if is in store mode
	 */
	public boolean isInStoreMode()
	{
		return getPrivateStoreType() > 0;
	}
	
	// public boolean isInCraftMode() { return (getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE); }
	
	/**
	 * Checks if is in craft mode.
	 * @return true, if is in craft mode
	 */
	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}
	
	/**
	 * Checks if is in craft mode.
	 * @param b the b
	 */
	public void isInCraftMode(final boolean b)
	{
		_inCraftMode = b;
	}
	
	/** The _kicked. */
	private boolean _kicked = false;
	
	/**
	 * Manage Logout Task.<BR>
	 * <BR>
	 * @param kicked the kicked
	 */
	public void logout(final boolean kicked)
	{
		// prevent from player disconnect when in Event
		if (atEvent)
		{
			sendMessage("A superior power doesn't allow you to leave the event.");
			sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		_kicked = kicked;
		
		closeNetConnection();
		
	}
	
	/**
	 * Checks if is kicked.
	 * @return true, if is kicked
	 */
	public boolean isKicked()
	{
		return _kicked;
	}
	
	/**
	 * Sets the kicked.
	 * @param value the new kicked
	 */
	public void setKicked(final boolean value)
	{
		_kicked = value;
	}
	
	/**
	 * Manage Logout Task.<BR>
	 * <BR>
	 */
	public void logout()
	{
		
		logout(false);
		/*
		 * if(_active_boxes!=-1){ //normal logout this.decreaseBoxes(); }
		 */
		/*
		 * _active_boxes = _active_boxes-1; if(getClient()!=null && !getClient().getConnection().isClosed()){ String thisip = getClient().getConnection().getSocketChannel().socket().getInetAddress().getHostAddress(); Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		 * L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]); for(L2PcInstance player : players) { if(player != null) { if(player.getClient()!=null && !player.getClient().getConnection().isClosed()){ String ip =
		 * player.getClient().getConnection().getSocketChannel().socket().getInetAddress().getHostAddress(); if(thisip.equals(ip) && this != player && player != null) { player._active_boxes = _active_boxes; } } } } }
		 */
		
	}
	
	/**
	 * Return a table containing all Common L2RecipeList of the L2PcInstance.<BR>
	 * <BR>
	 * @return the common recipe book
	 */
	public L2RecipeList[] getCommonRecipeBook()
	{
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}
	
	/**
	 * Return a table containing all Dwarf L2RecipeList of the L2PcInstance.<BR>
	 * <BR>
	 * @return the dwarven recipe book
	 */
	public L2RecipeList[] getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}
	
	/**
	 * Add a new L2RecipList to the table _commonrecipebook containing all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipe The L2RecipeList to add to the _recipebook
	 */
	public void registerCommonRecipeList(final L2RecipeList recipe)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipe The L2RecipeList to add to the _recipebook
	 */
	public void registerDwarvenRecipeList(final L2RecipeList recipe)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Checks for recipe list.
	 * @param recipeId the recipe id
	 * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(final int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			return true;
		else if (_commonRecipeBook.containsKey(recipeId))
			return true;
		else
			return false;
	}
	
	/**
	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2RecipeList of the L2PcInstance <BR>
	 * <BR>
	 * .
	 * @param recipeId the recipe id
	 */
	public void unregisterRecipeList(final int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
		{
			_dwarvenRecipeBook.remove(recipeId);
		}
		else if (_commonRecipeBook.containsKey(recipeId))
		{
			_commonRecipeBook.remove(recipeId);
		}
		else
		{
			LOGGER.warn("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		
		for (final L2ShortCut sc : allShortCuts)
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		allShortCuts = null;
	}
	
	/**
	 * Returns the Id for the last talked quest NPC.<BR>
	 * <BR>
	 * @return the last quest npc object
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	/**
	 * Sets the last quest npc object.
	 * @param npcId the new last quest npc object
	 */
	public void setLastQuestNpcObject(final int npcId)
	{
		_questNpcObject = npcId;
	}
	
	/**
	 * Return the QuestState object corresponding to the quest name.<BR>
	 * <BR>
	 * @param quest The name of the quest
	 * @return the quest state
	 */
	public QuestState getQuestState(final String quest)
	{
		return _quests.get(quest);
	}
	
	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR>
	 * <BR>
	 * @param qs The QuestState to add to _quest
	 */
	public void setQuestState(final QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}
	
	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR>
	 * <BR>
	 * @param quest The name of the quest
	 */
	public void delQuestState(final String quest)
	{
		_quests.remove(quest);
	}
	
	/**
	 * Adds the to quest state array.
	 * @param questStateArray the quest state array
	 * @param state the state
	 * @return the quest state[]
	 */
	private QuestState[] addToQuestStateArray(final QuestState[] questStateArray, final QuestState state)
	{
		final int len = questStateArray.length;
		final QuestState[] tmp = new QuestState[len + 1];
		for (int i = 0; i < len; i++)
		{
			tmp[i] = questStateArray[i];
		}
		tmp[len] = state;
		return tmp;
	}
	
	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR>
	 * <BR>
	 * @return the all active quests
	 */
	public Quest[] getAllActiveQuests()
	{
		final FastList<Quest> quests = new FastList<>();
		
		for (final QuestState qs : _quests.values())
		{
			if (qs != null)
			{
				if (qs.getQuest().getQuestIntId() >= 1999)
				{
					continue;
				}
				
				if (qs.isCompleted() && !Config.DEVELOPER)
				{
					continue;
				}
				
				if (!qs.isStarted() && !Config.DEVELOPER)
				{
					continue;
				}
				
				quests.add(qs.getQuest());
			}
		}
		
		return quests.toArray(new Quest[quests.size()]);
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR>
	 * <BR>
	 * @param npc the npc
	 * @return the quests for attacks
	 */
	public QuestState[] getQuestsForAttacks(final L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR>
	 * <BR>
	 * @param npc the npc
	 * @return the quests for kills
	 */
	public QuestState[] getQuestsForKills(final L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.<BR>
	 * <BR>
	 * @param npcId The Identifier of the NPC
	 * @return the quests for talk
	 */
	public QuestState[] getQuestsForTalk(final int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (final Quest quest : NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.QUEST_TALK))
		{
			if (quest != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (getQuestState(quest.getName()) != null)
				{
					if (states == null)
					{
						states = new QuestState[]
						{
							getQuestState(quest.getName())
						};
					}
					else
					{
						states = addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Process quest event.
	 * @param quest the quest
	 * @param event the event
	 * @return the quest state
	 */
	public QuestState processQuestEvent(final String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
		{
			event = "";
		}
		
		if (!_quests.containsKey(quest))
			return retval;
		
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return retval;
		
		if (qs == null)
		{
			Quest q = null;
			if (!Config.ALT_DEV_NO_QUESTS)
				q = QuestManager.getInstance().getQuest(quest);
			
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				final L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
				if (object instanceof L2NpcInstance && isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					final L2NpcInstance npc = (L2NpcInstance) object;
					final QuestState[] states = getQuestsForTalk(npc.getNpcId());
					
					if (states != null)
					{
						for (final QuestState state : states)
						{
							if (state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId() && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
								{
									showQuestWindow(quest, qs.getStateId());
								}
								
								retval = qs;
							}
						}
						sendPacket(new QuestList());
					}
				}
			}
			qs = null;
		}
		
		return retval;
	}
	
	/**
	 * Show quest window.
	 * @param questId the quest id
	 * @param stateId the state id
	 */
	private void showQuestWindow(final String questId, final String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content != null)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);
			}
			
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
			content = null;
			npcReply = null;
		}
		
		sendPacket(ActionFailed.STATIC_PACKET);
		path = null;
	}
	
	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR>
	 * <BR>
	 * @return the all short cuts
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * Return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).<BR>
	 * <BR>
	 * @param slot The slot in wich the shortCuts is equiped
	 * @param page The page of shortCuts containing the slot
	 * @return the short cut
	 */
	public L2ShortCut getShortCut(final int slot, final int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the L2PcInstance _shortCuts<BR>
	 * <BR>
	 * .
	 * @param shortcut the shortcut
	 */
	public void registerShortCut(final L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance _shortCuts.<BR>
	 * <BR>
	 * @param slot the slot
	 * @param page the page
	 */
	public void deleteShortCut(final int slot, final int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * Add a L2Macro to the L2PcInstance _macroses<BR>
	 * <BR>
	 * .
	 * @param macro the macro
	 */
	public void registerMacro(final L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance _macroses.<BR>
	 * <BR>
	 * @param id the id
	 */
	public void deleteMacro(final int id)
	{
		_macroses.deleteMacro(id);
	}
	
	/**
	 * Return all L2Macro of the L2PcInstance.<BR>
	 * <BR>
	 * @return the macroses
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}
	
	/**
	 * Set the siege state of the L2PcInstance.<BR>
	 * <BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @param siegeState the new siege state
	 */
	public void setSiegeState(final byte siegeState)
	{
		_siegeState = siegeState;
	}
	
	/**
	 * Get the siege state of the L2PcInstance.<BR>
	 * <BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @return the siege state
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	/**
	 * Set the PvP Flag of the L2PcInstance.<BR>
	 * <BR>
	 * @param pvpFlag the new pvp flag
	 */
	public void setPvpFlag(final int pvpFlag)
	{
		_pvpFlag = (byte) pvpFlag;
	}
	
	/**
	 * Gets the pvp flag.
	 * @return the pvp flag
	 */
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#updatePvPFlag(int)
	 */
	@Override
	public void updatePvPFlag(final int value)
	{
		if (getPvpFlag() == value)
			return;
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		
		// If this player has a pet update the pets pvp flag as well
		if (getPet() != null)
		{
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		}
		
		for (final L2PcInstance target : getKnownList().getKnownPlayers().values())
		{
			if (target == null)
				continue;
			
			target.sendPacket(new RelationChanged(this, getRelation(this), isAutoAttackable(target)));
			if (getPet() != null)
			{
				target.sendPacket(new RelationChanged(getPet(), getRelation(this), isAutoAttackable(target)));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#revalidateZone(boolean)
	 */
	@Override
	public void revalidateZone(final boolean force)
	{
		// Cannot validate if not in a world region (happens during teleport)
		if (getWorldRegion() == null)
			return;
		
		if (Config.ALLOW_WATER)
			checkWaterState();
		
		// This function is called very often from movement code
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
				return;
		}
		
		getWorldRegion().revalidateZones(this);
		
		if (isInsideZone(ZONE_SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				return;
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
			sendPacket(cz);
			cz = null;
		}
		else if (isInsideZone(ZONE_PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
			sendPacket(cz);
			cz = null;
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE);
			sendPacket(cz);
			cz = null;
		}
		else if (isInsideZone(ZONE_PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
			sendPacket(cz);
			cz = null;
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
				return;
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				updatePvPStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
			sendPacket(cz);
			cz = null;
		}
	}
	
	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	/**
	 * Gets the dwarven craft.
	 * @return the dwarven craft
	 */
	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	/**
	 * Gets the common craft.
	 * @return the common craft
	 */
	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	/**
	 * Return the PK counter of the L2PcInstance.<BR>
	 * <BR>
	 * @return the pk kills
	 */
	public int getPkKills()
	{
		return _pkKills;
	}
	
	/**
	 * Set the PK counter of the L2PcInstance.<BR>
	 * <BR>
	 * @param pkKills the new pk kills
	 */
	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}
	
	/**
	 * Return the _deleteTimer of the L2PcInstance.<BR>
	 * <BR>
	 * @return the delete timer
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	/**
	 * Set the _deleteTimer of the L2PcInstance.<BR>
	 * <BR>
	 * @param deleteTimer the new delete timer
	 */
	public void setDeleteTimer(final long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	/**
	 * Return the current weight of the L2PcInstance.<BR>
	 * <BR>
	 * @return the current load
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	/**
	 * Return date of las update of recomPoints.
	 * @return the last recom update
	 */
	public long getLastRecomUpdate()
	{
		return _lastRecomUpdate;
	}
	
	/**
	 * Sets the last recom update.
	 * @param date the new last recom update
	 */
	public void setLastRecomUpdate(final long date)
	{
		_lastRecomUpdate = date;
	}
	
	/**
	 * Return the number of recommandation obtained by the L2PcInstance.<BR>
	 * <BR>
	 * @return the recom have
	 */
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	/**
	 * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR>
	 * <BR>
	 */
	protected void incRecomHave()
	{
		if (_recomHave < 255)
		{
			_recomHave++;
		}
	}
	
	/**
	 * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR>
	 * <BR>
	 * @param value the new recom have
	 */
	public void setRecomHave(final int value)
	{
		if (value > 255)
		{
			_recomHave = 255;
		}
		else if (value < 0)
		{
			_recomHave = 0;
		}
		else
		{
			_recomHave = value;
		}
	}
	
	/**
	 * Return the number of recommandation that the L2PcInstance can give.<BR>
	 * <BR>
	 * @return the recom left
	 */
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	/**
	 * Increment the number of recommandation that the L2PcInstance can give.<BR>
	 * <BR>
	 */
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
		{
			_recomLeft--;
		}
	}
	
	/**
	 * Give recom.
	 * @param target the target
	 */
	public void giveRecom(final L2PcInstance target)
	{
		if (Config.ALT_RECOMMEND)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);
				statement.setInt(1, getObjectId());
				statement.setInt(2, target.getObjectId());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.warn("could not update char recommendations:" + e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
		target.incRecomHave();
		decRecomLeft();
		_recomChars.add(target.getObjectId());
	}
	
	/**
	 * Can recom.
	 * @param target the target
	 * @return true, if successful
	 */
	public boolean canRecom(final L2PcInstance target)
	{
		return !_recomChars.contains(target.getObjectId());
	}
	
	/**
	 * Set the exp of the L2PcInstance before a death.
	 * @param exp the new exp before death
	 */
	public void setExpBeforeDeath(final long exp)
	{
		_expBeforeDeath = exp;
	}
	
	/**
	 * Gets the exp before death.
	 * @return the exp before death
	 */
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	/**
	 * Return the Karma of the L2PcInstance.<BR>
	 * <BR>
	 * @return the karma
	 */
	public int getKarma()
	{
		return _karma;
	}
	
	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR>
	 * <BR>
	 * @param karma the new karma
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
		{
			karma = 0;
		}
		
		if (_karma == 0 && karma > 0)
		{
			for (final L2Object object : getKnownList().getKnownObjects().values())
			{
				if (object == null || !(object instanceof L2GuardInstance))
				{
					continue;
				}
				
				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);
		}
		
		_karma = karma;
		broadcastKarma();
	}
	
	/**
	 * Return the max weight that the L2PcInstance can load.<BR>
	 * <BR>
	 * @return the max load
	 */
	public int getMaxLoad()
	{
		
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		
		final int con = getCON();
		
		if (con < 1)
			return 31000;
		
		if (con > 59)
			return 176000;
		
		final double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
		return (int) calcStat(Stats.MAX_LOAD, baseLoad, this, null);
	}
	
	/**
	 * Gets the expertise penalty.
	 * @return the expertise penalty
	 */
	public int getExpertisePenalty()
	{
		return _expertisePenalty;
	}
	
	/**
	 * Gets the mastery penalty.
	 * @return the mastery penalty
	 */
	public int getMasteryPenalty()
	{
		return _masteryPenalty;
	}
	
	/**
	 * Gets the mastery weap penalty.
	 * @return the mastery weap penalty
	 */
	public int getMasteryWeapPenalty()
	{
		return _masteryWeapPenalty;
	}
	
	/**
	 * Gets the weight penalty.
	 * @return the weight penalty
	 */
	public int getWeightPenalty()
	{
		if (_dietMode)
			return 0;
		return _curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the L2PcInstance.<BR>
	 * <BR>
	 */
	public void refreshOverloaded()
	{
		if (Config.DISABLE_WEIGHT_PENALTY)
		{
			setIsOverloaded(false);
		}
		else if (_dietMode)
		{
			setIsOverloaded(false);
			_curWeightPenalty = 0;
			super.removeSkill(getKnownSkill(4270));
			sendPacket(new EtcStatusUpdate(this));
			Broadcast.toKnownPlayers(this, new CharInfo(this));
		}
		else
		{
			final int maxLoad = getMaxLoad();
			if (maxLoad > 0)
			{
				// setIsOverloaded(getCurrentLoad() > maxLoad);
				// int weightproc = getCurrentLoad() * 1000 / maxLoad;
				final long weightproc = (long) ((getCurrentLoad() - calcStat(Stats.WEIGHT_PENALTY, 1, this, null)) * 1000 / maxLoad);
				int newWeightPenalty;
				
				if (weightproc < 500)
				{
					newWeightPenalty = 0;
				}
				else if (weightproc < 666)
				{
					newWeightPenalty = 1;
				}
				else if (weightproc < 800)
				{
					newWeightPenalty = 2;
				}
				else if (weightproc < 1000)
				{
					newWeightPenalty = 3;
				}
				else
				{
					newWeightPenalty = 4;
				}
				
				if (_curWeightPenalty != newWeightPenalty)
				{
					_curWeightPenalty = newWeightPenalty;
					if (newWeightPenalty > 0)
					{
						super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
						sendSkillList(); // Fix visual bug
					}
					else
					{
						super.removeSkill(getKnownSkill(4270));
						sendSkillList(); // Fix visual bug
					}
					
					sendPacket(new EtcStatusUpdate(this));
					Broadcast.toKnownPlayers(this, new CharInfo(this));
				}
			}
		}
		
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Refresh mastery penality.
	 */
	public void refreshMasteryPenality()
	{
		if (!Config.MASTERY_PENALTY || this.getLevel() <= Config.LEVEL_TO_GET_PENALITY)
			return;
		
		_heavy_mastery = false;
		_light_mastery = false;
		_robe_mastery = false;
		
		final L2Skill[] char_skills = this.getAllSkills();
		
		for (final L2Skill actual_skill : char_skills)
		{
			if (actual_skill.getName().contains("Heavy Armor Mastery"))
			{
				_heavy_mastery = true;
			}
			
			if (actual_skill.getName().contains("Light Armor Mastery"))
			{
				_light_mastery = true;
			}
			
			if (actual_skill.getName().contains("Robe Mastery"))
			{
				_robe_mastery = true;
			}
		}
		
		int newMasteryPenalty = 0;
		
		if (!_heavy_mastery && !_light_mastery && !_robe_mastery)
		{
			// not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (final L2ItemInstance item : getInventory().getItems())
			{
				if (item != null && item.isEquipped() && item.getItem() instanceof L2Armor)
				{
					// No penality for formal wear
					if (item.getItemId() == 6408)
						continue;
					
					final L2Armor armor_item = (L2Armor) item.getItem();
					
					switch (armor_item.getItemType())
					{
						case HEAVY:
						{
							if (!_heavy_mastery)
								newMasteryPenalty++;
						}
							break;
						case LIGHT:
						{
							if (!_light_mastery)
								newMasteryPenalty++;
						}
							break;
						case MAGIC:
						{
							if (!_robe_mastery)
								newMasteryPenalty++;
						}
							break;
					}
				}
			}
		}
		
		if (_masteryPenalty != newMasteryPenalty)
		{
			final int penalties = _masteryWeapPenalty + _expertisePenalty + newMasteryPenalty;
			
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(4267, 1)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			_masteryPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Can interact.
	 * @param player the player
	 * @return true, if successful
	 */
	protected boolean canInteract(final L2PcInstance player)
	{
		if (!isInsideRadius(player, 50, false, false))
			return false;
		
		return true;
	}
	
	/** The _blunt_mastery. */
	private boolean _blunt_mastery = false;
	
	/** The _pole_mastery. */
	private boolean _pole_mastery = false;
	
	/** The _dagger_mastery. */
	private boolean _dagger_mastery = false;
	
	/** The _sword_mastery. */
	private boolean _sword_mastery = false;
	
	/** The _bow_mastery. */
	private boolean _bow_mastery = false;
	
	/** The _fist_mastery. */
	private boolean _fist_mastery = false;
	
	/** The _dual_mastery. */
	private boolean _dual_mastery = false;
	
	/** The _2hands_mastery. */
	private boolean _2hands_mastery = false;
	
	/** The _mastery weap penalty. */
	private int _masteryWeapPenalty = 0;
	
	/**
	 * Refresh mastery weap penality.
	 */
	public void refreshMasteryWeapPenality()
	{
		if (!Config.MASTERY_WEAPON_PENALTY || this.getLevel() <= Config.LEVEL_TO_GET_WEAPON_PENALITY)
			return;
		
		_blunt_mastery = false;
		_bow_mastery = false;
		_dagger_mastery = false;
		_fist_mastery = false;
		_dual_mastery = false;
		_pole_mastery = false;
		_sword_mastery = false;
		_2hands_mastery = false;
		
		final L2Skill[] char_skills = this.getAllSkills();
		
		for (final L2Skill actual_skill : char_skills)
		{
			
			if (actual_skill.getName().contains("Sword Blunt Mastery"))
			{
				_sword_mastery = true;
				_blunt_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Blunt Mastery"))
			{
				_blunt_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Bow Mastery"))
			{
				_bow_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Dagger Mastery"))
			{
				_dagger_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Fist Mastery"))
			{
				_fist_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Dual Weapon Mastery"))
			{
				_dual_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Polearm Mastery"))
			{
				_pole_mastery = true;
				continue;
			}
			
			if (actual_skill.getName().contains("Two-handed Weapon Mastery"))
			{
				_2hands_mastery = true;
				continue;
			}
		}
		
		int newMasteryPenalty = 0;
		
		if (!_bow_mastery && !_blunt_mastery && !_dagger_mastery && !_fist_mastery && !_dual_mastery && !_pole_mastery && !_sword_mastery && !_2hands_mastery)
		{ // not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (final L2ItemInstance item : getInventory().getItems())
			{
				if (item != null && item.isEquipped() && item.getItem() instanceof L2Weapon && !isCursedWeaponEquiped())
				{
					// No penality for cupid's bow
					if (item.isCupidBow())
						continue;
					
					final L2Weapon weap_item = (L2Weapon) item.getItem();
					
					switch (weap_item.getItemType())
					{
					
						case BIGBLUNT:
						case BIGSWORD:
						{
							if (!_2hands_mastery)
								newMasteryPenalty++;
						}
							break;
						case BLUNT:
						{
							if (!_blunt_mastery)
								newMasteryPenalty++;
						}
							break;
						case BOW:
						{
							if (!_bow_mastery)
								newMasteryPenalty++;
						}
							break;
						case DAGGER:
						{
							if (!_dagger_mastery)
								newMasteryPenalty++;
						}
							break;
						case DUAL:
						{
							if (!_dual_mastery)
								newMasteryPenalty++;
						}
							break;
						case DUALFIST:
						case FIST:
						{
							if (!_fist_mastery)
								newMasteryPenalty++;
						}
							break;
						case POLE:
						{
							if (!_pole_mastery)
								newMasteryPenalty++;
						}
							break;
						case SWORD:
						{
							if (!_sword_mastery)
								newMasteryPenalty++;
						}
							break;
					
					}
				}
			}
			
		}
		
		if (_masteryWeapPenalty != newMasteryPenalty)
		{
			final int penalties = _masteryPenalty + _expertisePenalty + newMasteryPenalty;
			
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(4267, 1)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			_masteryWeapPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Refresh expertise penalty.
	 */
	public void refreshExpertisePenalty()
	{
		if (!Config.EXPERTISE_PENALTY)
			return;
		
		// This code works on principle that first 1-5 levels of penalty is for weapon and 6-10levels are for armor
		int intensityW = 0; // Default value
		int intensityA = 5; // Default value.
		int intensity = 0; // Level of grade penalty.
		
		for (final L2ItemInstance item : getInventory().getItems())
		{
			if (item != null && item.isEquipped()) // Checks if items equipped
			{
				
				final int crystaltype = item.getItem().getCrystalType(); // Gets grade of item
				// Checks if item crystal levels is above character levels and also if last penalty for weapon was lower.
				if (crystaltype > getExpertiseIndex() && item.isWeapon() && crystaltype > intensityW)
				{
					intensityW = crystaltype - getExpertiseIndex();
				}
				// Checks if equiped armor, accesories are above character level and adds each armor penalty.
				if (crystaltype > getExpertiseIndex() && !item.isWeapon())
				{
					intensityA += crystaltype - getExpertiseIndex();
				}
			}
		}
		
		if (intensityA == 5)// Means that there isn't armor penalty.
		{
			intensity = intensityW;
		}
		
		else
		{
			intensity = intensityW + intensityA;
		}
		
		// Checks if penalty is above maximum and sets it to maximum.
		if (intensity > 10)
		{
			intensity = 10;
		}
		
		if (getExpertisePenalty() != intensity)
		{
			int penalties = _masteryPenalty + _masteryWeapPenalty + intensity;
			if (penalties > 10) // Checks if penalties are out of bounds for skill level on XML
			{
				penalties = 10;
			}
			
			_expertisePenalty = intensity;
			
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(4267, intensity));
				sendSkillList();
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
				sendSkillList();
				_expertisePenalty = 0;
			}
		}
	}
	
	public void checkIfWeaponIsAllowed()
	{
		// Override for Gamemasters
		if (isGM())
		{
			return;
		}
		// Iterate through all effects currently on the character.
		for (final L2Effect currenteffect : getAllEffects())
		{
			final L2Skill effectSkill = currenteffect.getSkill();
			// Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
			if (!effectSkill.isOffensive() && !(effectSkill.getTargetType() == SkillTargetType.TARGET_PARTY && effectSkill.getSkillType() == SkillType.BUFF))
			{
				// Check to rest to assure current effect meets weapon requirements.
				if (!effectSkill.getWeaponDependancy(this))
				{
					sendMessage(effectSkill.getName() + " cannot be used with this weapon.");
					if (Config.DEBUG)
					{
						LOGGER.info("   | Skill " + effectSkill.getName() + " has been disabled for (" + getName() + "); Reason: Incompatible Weapon Type.");
					}
					currenteffect.exit();
				}
			}
			continue;
		}
	}
	
	/**
	 * Check ss match.
	 * @param equipped the equipped
	 * @param unequipped the unequipped
	 */
	public void checkSSMatch(final L2ItemInstance equipped, final L2ItemInstance unequipped)
	{
		if (unequipped == null)
			return;
		
		if (unequipped.getItem().getType2() == L2Item.TYPE2_WEAPON && (equipped == null ? true : equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType()))
		// && getInventory().getItem() != null - must be fixed.
		{
			for (final L2ItemInstance ss : getInventory().getItems())
			{
				final int _itemId = ss.getItemId();
				
				if ((_itemId >= 2509 && _itemId <= 2514 || _itemId >= 3947 && _itemId <= 3952 || _itemId <= 1804 && _itemId >= 1808 || _itemId == 5789 || _itemId == 5790 || _itemId == 1835) && ss.getItem().getCrystalType() == unequipped.getItem().getCrystalType())
				{
					sendPacket(new ExAutoSoulShot(_itemId, 0));
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(ss.getItemName());
					sendPacket(sm);
				}
			}
		}
	}
	
	/**
	 * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR>
	 * <BR>
	 * @return the pvp kills
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR>
	 * <BR>
	 * @param pvpKills the new pvp kills
	 */
	public void setPvpKills(final int pvpKills)
	{
		_pvpKills = pvpKills;
		
		/*
		 * // Set hero aura if pvp kills > 100 if (pvpKills > 100) { isPermaHero = true; setHeroAura(true); }
		 */
	}
	
	/**
	 * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR>
	 * <BR>
	 * @return the class id
	 */
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}
	
	/**
	 * Set the template of the L2PcInstance.<BR>
	 * <BR>
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 */
	public void setClassId(final int Id)
	{
		
		if (getLvlJoinedAcademy() != 0 && _clan != null && PlayerClass.values()[Id].getLevel() == ClassLevel.Third)
		{
			if (getLvlJoinedAcademy() <= 16)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 400, true);
			}
			else if (getLvlJoinedAcademy() >= 39)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 170, true);
			}
			else
			{
				_clan.setReputationScore(_clan.getReputationScore() + 400 - (getLvlJoinedAcademy() - 16) * 10, true);
			}
			
			_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
			setLvlJoinedAcademy(0);
			// oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
			_clan.removeClanMember(getName(), 0);
			sendPacket(new SystemMessage(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED));
			msg = null;
			
			// receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
			getInventory().updateDatabase(); // update database
		}
		if (isSubClassActive())
		{
			getSubClasses().get(_classIndex).setClassId(Id);
		}
		doCast(SkillTable.getInstance().getInfo(5103, 1));
		setClassTemplate(Id);
	}
	
	/**
	 * Return the Experience of the L2PcInstance.
	 * @return the exp
	 */
	public long getExp()
	{
		return getStat().getExp();
	}
	
	/**
	 * Sets the active enchant item.
	 * @param scroll the new active enchant item
	 */
	public void setActiveEnchantItem(final L2ItemInstance scroll)
	{
		_activeEnchantItem = scroll;
	}
	
	/**
	 * Gets the active enchant item.
	 * @return the active enchant item
	 */
	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR>
	 * <BR>
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 */
	public void setFistsWeaponItem(final L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR>
	 * <BR>
	 * @return the fists weapon item
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance Class (used when no weapon is equiped).<BR>
	 * <BR>
	 * @param classId the class id
	 * @return the l2 weapon
	 */
	public L2Weapon findFistsWeaponItem(final int classId)
	{
		L2Weapon weaponItem = null;
		if (classId >= 0x00 && classId <= 0x09)
		{
			// human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x0a && classId <= 0x11)
		{
			// human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x12 && classId <= 0x18)
		{
			// elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x19 && classId <= 0x1e)
		{
			// elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x1f && classId <= 0x25)
		{
			// dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x26 && classId <= 0x2b)
		{
			// dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x2c && classId <= 0x30)
		{
			// orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x31 && classId <= 0x34)
		{
			// orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		else if (classId >= 0x35 && classId <= 0x39)
		{
			// dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
			temp = null;
		}
		
		return weaponItem;
	}
	
	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Level of the L2PcInstance</li> <li>If L2PcInstance Level is 5, remove beginner Lucky skill</li> <li>Add the Expertise skill corresponding to its Expertise level</li> <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR>
	 * <BR>
	 */
	public synchronized void rewardSkills()
	{
		rewardSkills(false);
	}
	
	public synchronized void rewardSkills(final boolean restore)
	{
		// Get the Level of the L2PcInstance
		final int lvl = getLevel();
		
		// Remove beginner Lucky skill
		if (lvl == 10)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
			
			if (Config.DEBUG && skill != null)
			{
				LOGGER.debug("Removed skill 'Lucky' from " + getName());
			}
			
			skill = null;
		}
		
		// Calculate the current higher Expertise of the L2PcInstance
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setExpertiseIndex(i);
			}
		}
		
		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill, !restore);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("Awarded " + getName() + " with new expertise.");
			}
			
			skill = null;
		}
		else
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("No skills awarded at lvl: " + lvl);
			}
		}
		
		// Active skill dwarven craft
		
		if (getSkillLevel(1321) < 1 && getRace() == Race.dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill, !restore);
			skill = null;
		}
		
		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill, !restore);
			skill = null;
		}
		
		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < i + 1)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i + 1));
				addSkill(skill, !restore);
				skill = null;
			}
		}
		
		// Auto-Learn skills if activated
		if (Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableSkills();
		}
		sendSkillList();
		
		if (_clan != null)
		{
			if (_clan.getLevel() > 3 && isClanLeader())
				SiegeManager.getInstance().addSiegeSkills(this);
		}
		
		// This function gets called on login, so not such a bad place to check weight
		refreshOverloaded(); // Update the overloaded status of the L2PcInstance
		
		refreshExpertisePenalty(); // Update the expertise status of the L2PcInstance
		
		refreshMasteryPenality();
		
		refreshMasteryWeapPenality();
		
	}
	
	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills<BR>
	 * <BR>
	 * .
	 */
	private synchronized void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load
		
		// Add noble skills if noble
		if (isNoble())
		{
			setNoble(true);
		}
		
		// Add Hero skills if hero
		if (isHero())
		{
			setHero(true);
		}
		
		/*
		 * // Add clan leader skills if clanleader if(isClanLeader()) { setClanLeader(true); }
		 */
		
		// Add clan skills
		if (getClan() != null && getClan().getReputationScore() >= 0)
		{
			L2Skill[] skills = getClan().getAllSkills();
			for (final L2Skill sk : skills)
			{
				if (sk.getMinPledgeClass() <= getPledgeClass())
				{
					addSkill(sk, false);
				}
			}
			skills = null;
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
	}
	
	/**
	 * Give all available skills to the player.<br>
	 * <br>
	 */
	public void giveAvailableSkills()
	{
		// int unLearnable = 0;
		int skillCounter = 0;
		
		// Get available skills
		// L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		// while(skills.length > unLearnable)
		// {
		// unLearnable = 0;
		// for(L2SkillLearn s : skills)
		Collection<L2Skill> skills = SkillTreeTable.getInstance().getAllAvailableSkills(this, getClassId());
		for (final L2Skill sk : skills)
		{
			// {
			// L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			// if(sk == null || (sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION))
			// {
			// unLearnable++;
			// continue;
			// }
			
			if (getSkillLevel(sk.getId()) == -1)
			{
				skillCounter++;
			}
			
			// Penality skill are not auto learn
			if (sk.getId() == 4267 || sk.getId() == 4270)
				continue;
			
			// fix when learning toggle skills
			if (sk.isToggle())
			{
				final L2Effect toggleEffect = getFirstEffect(sk.getId());
				if (toggleEffect != null)
				{
					// stop old toggle skill effect, and give new toggle skill effect back
					toggleEffect.exit(false);
					sk.getEffects(this, this, false, false, false);
				}
			}
			
			addSkill(sk, true);
		}
		
		// // Get new available skills
		// skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		// }
		
		sendMessage("You have learned " + skillCounter + " new skills.");
		skills = null;
	}
	
	/**
	 * Set the Experience value of the L2PcInstance.
	 * @param exp the new exp
	 */
	public void setExp(final long exp)
	{
		getStat().setExp(exp);
	}
	
	/**
	 * Return the Race object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the race
	 */
	public Race getRace()
	{
		if (!isSubClassActive())
			return getTemplate().race;
		
		final L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass);
		return charTemp.race;
	}
	
	/**
	 * Gets the radar.
	 * @return the radar
	 */
	public L2Radar getRadar()
	{
		return _radar;
	}
	
	/**
	 * Return the SP amount of the L2PcInstance.
	 * @return the sp
	 */
	public int getSp()
	{
		return getStat().getSp();
	}
	
	/**
	 * Set the SP amount of the L2PcInstance.
	 * @param sp the new sp
	 */
	public void setSp(final int sp)
	{
		super.getStat().setSp(sp);
	}
	
	/**
	 * Return true if this L2PcInstance is a clan leader in ownership of the passed castle.
	 * @param castleId the castle id
	 * @return true, if is castle lord
	 */
	public boolean isCastleLord(final int castleId)
	{
		L2Clan clan = getClan();
		
		// player has clan and is the clan leader, check the castle info
		if (clan != null && clan.getLeader().getPlayerInstance() == this)
		{
			// if the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId))
			{
				castle = null;
				return true;
			}
			castle = null;
		}
		clan = null;
		return false;
	}
	
	/**
	 * Return the Clan Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the clan id
	 */
	public int getClanId()
	{
		return _clanId;
	}
	
	/**
	 * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR>
	 * <BR>
	 * @return the clan crest id
	 */
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
			return _clan.getCrestId();
		
		return 0;
	}
	
	/**
	 * Gets the clan crest large id.
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if (_clan != null && _clan.hasCrestLarge())
			return _clan.getCrestLargeId();
		
		return 0;
	}
	
	/**
	 * Gets the clan join expiry time.
	 * @return the clan join expiry time
	 */
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	/**
	 * Sets the clan join expiry time.
	 * @param time the new clan join expiry time
	 */
	public void setClanJoinExpiryTime(final long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	/**
	 * Gets the clan create expiry time.
	 * @return the clan create expiry time
	 */
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	/**
	 * Sets the clan create expiry time.
	 * @param time the new clan create expiry time
	 */
	public void setClanCreateExpiryTime(final long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	/**
	 * Sets the online time.
	 * @param time the new online time
	 */
	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.<BR>
	 * <BR>
	 * @return the inventory
	 */
	public PcInventory getInventory()
	{
		return _inventory;
	}
	
	/**
	 * Delete a ShortCut of the L2PcInstance _shortCuts.<BR>
	 * <BR>
	 * @param objectId the object id
	 */
	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	// MOVING on attack TASK, L2OFF FIX
	/** The launched moving task. */
	protected MoveOnAttack launchedMovingTask = null;
	
	/** The _moving task defined. */
	protected Boolean _movingTaskDefined = false;
	
	/**
	 * MoveOnAttack Task.
	 */
	public class MoveOnAttack implements Runnable
	{
		
		/** The _player. */
		final L2PcInstance _player;
		
		/** The _pos. */
		L2CharPosition _pos;
		
		/**
		 * Instantiates a new move on attack.
		 * @param player the player
		 * @param pos the pos
		 */
		public MoveOnAttack(final L2PcInstance player, final L2CharPosition pos)
		{
			_player = player;
			_pos = pos;
			// launchedMovingTask = this;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			synchronized (_movingTaskDefined)
			{
				launchedMovingTask = null;
				_movingTaskDefined = false;
			}
			// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
			_player.getAI().changeIntention(AI_INTENTION_MOVE_TO, _pos, null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			_player.getAI().clientStopAutoAttack();
			
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			_player.abortAttack();
			
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			_player.getAI().moveTo(_pos.x, _pos.y, _pos.z);
		}
		
		/**
		 * Sets the new position.
		 * @param pos the new new position
		 */
		public void setNewPosition(final L2CharPosition pos)
		{
			_pos = pos;
		}
	}
	
	/**
	 * Checks if is moving task defined.
	 * @return true, if is moving task defined
	 */
	public boolean isMovingTaskDefined()
	{
		return _movingTaskDefined;
		// return launchedMovingTask != null;
	}
	
	public final void setMovingTaskDefined(final boolean value)
	{
		_movingTaskDefined = value;
	}
	
	/**
	 * Define new moving task.
	 * @param pos the pos
	 */
	public void defineNewMovingTask(final L2CharPosition pos)
	{
		synchronized (_movingTaskDefined)
		{
			launchedMovingTask = new MoveOnAttack(this, pos);
			_movingTaskDefined = true;
		}
	}
	
	/**
	 * Modify moving task.
	 * @param pos the pos
	 */
	public void modifyMovingTask(final L2CharPosition pos)
	{
		synchronized (_movingTaskDefined)
		{
			
			if (!_movingTaskDefined)
				return;
			
			launchedMovingTask.setNewPosition(pos);
		}
	}
	
	/**
	 * Start moving task.
	 */
	public void startMovingTask()
	{
		synchronized (_movingTaskDefined)
		{
			if (!_movingTaskDefined)
				return;
			
			if ((isMoving() && isAttackingNow()))
				return;
			
			ThreadPoolManager.getInstance().executeTask(launchedMovingTask);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is sitting.<BR>
	 * <BR>
	 * @return true, if is sitting
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting || sittingTaskLaunched;
	}
	
	/**
	 * Return True if the L2PcInstance is sitting task launched.<BR>
	 * <BR>
	 * @return true, if is sitting task launched
	 */
	public boolean isSittingTaskLaunched()
	{
		return sittingTaskLaunched;
	}
	
	/**
	 * Set _waitTypeSitting to given value.
	 * @param state the new checks if is sitting
	 */
	public void setIsSitting(final boolean state)
	{
		_waitTypeSitting = state;
	}
	
	/**
	 * Sets the posticipate sit.
	 * @param act the new posticipate sit
	 */
	public void setPosticipateSit(final boolean act)
	{
		_posticipateSit = act;
	}
	
	/**
	 * Gets the posticipate sit.
	 * @return the posticipate sit
	 */
	public boolean getPosticipateSit()
	{
		return _posticipateSit;
	}
	
	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR>
	 * <BR>
	 * .
	 */
	public void sitDown()
	{
		if (isFakeDeath())
		{
			stopFakeDeath(null);
		}
		
		if (isMoving()) // since you are moving and want sit down
		// the posticipate sitdown task will be always true
		{
			setPosticipateSit(true);
			return;
		}
		
		// we are going to sitdown, so posticipate is false
		setPosticipateSit(false);
		
		if (isCastingNow() && !_relax)
			return;
		
		if (sittingTaskLaunched) // if already started the task
			// just return
			return;
		
		if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImobilised())
		{
			breakAttack();
			setIsSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			sittingTaskLaunched = true;
			// Schedule a sit down task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500);
			setIsParalyzed(true);
		}
	}
	
	/**
	 * Sit down Task.
	 */
	class SitDownTask implements Runnable
	{
		
		/** The _player. */
		L2PcInstance _player;
		
		/** The this$0. */
		final L2PcInstance this$0;
		
		/**
		 * Instantiates a new sit down task.
		 * @param player the player
		 */
		SitDownTask(final L2PcInstance player)
		{
			this$0 = L2PcInstance.this;
			_player = player;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			setIsSitting(true);
			_player.setIsParalyzed(false);
			sittingTaskLaunched = false;
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	/**
	 * Stand up Task.
	 */
	class StandUpTask implements Runnable
	{
		
		/** The _player. */
		L2PcInstance _player;
		
		/**
		 * Instantiates a new stand up task.
		 * @param player the player
		 */
		StandUpTask(final L2PcInstance player)
		{
			_player = player;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			_player.setIsSitting(false);
			_player.setIsImobilised(false);
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR>
	 * <BR>
	 * .
	 */
	public void standUp()
	{
		if (isFakeDeath())
		{
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImobilised(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2000);
			stopFakeDeath(null);
		}
		
		if (sittingTaskLaunched)
		{
			return;
		}
		
		if (L2Event.active && eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
		}
		else if ((TvT.is_sitForced() && _inEventTvT) || (CTF.is_sitForced() && _inEventCTF) || (DM.is_sitForced() && _inEventDM))
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
		}
		else if (VIP._sitForced && _inEventVIP)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
		}
		else if (isAway())
		{
			sendMessage("You can't stand up if your Status is Away.");
		}
		else if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2Effect.EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImobilised(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
			
		}
	}
	
	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not.
	 * @param val the new relax
	 */
	public void setRelax(final boolean val)
	{
		_relax = val;
	}
	
	/**
	 * Return the PcWarehouse object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the warehouse
	 */
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return _warehouse;
	}
	
	/**
	 * Free memory used by Warehouse.
	 */
	public void clearWarehouse()
	{
		if (_warehouse != null)
		{
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}
	
	/**
	 * Return the PcFreight object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the freight
	 */
	public PcFreight getFreight()
	{
		return _freight;
	}
	
	/**
	 * Return the Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the char id
	 */
	public int getCharId()
	{
		return _charId;
	}
	
	/**
	 * Set the Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @param charId the new char id
	 */
	public void setCharId(final int charId)
	{
		_charId = charId;
	}
	
	/**
	 * Return the Adena amount of the L2PcInstance.<BR>
	 * <BR>
	 * @return the adena
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}
	
	/**
	 * Return the Item amount of the L2PcInstance.<BR>
	 * <BR>
	 * @param itemId the item id
	 * @param enchantLevel the enchant level
	 * @return the item count
	 */
	public int getItemCount(final int itemId, final int enchantLevel)
	{
		return _inventory.getInventoryItemCount(itemId, enchantLevel);
	}
	
	/**
	 * Return the Ancient Adena amount of the L2PcInstance.<BR>
	 * <BR>
	 * @return the ancient adena
	 */
	public int getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(final String process, int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > 0)
		{
			if (_inventory.getAdena() == Integer.MAX_VALUE)
			{
				return;
			}
			else if (_inventory.getAdena() >= Integer.MAX_VALUE - count)
			{
				count = Integer.MAX_VALUE - _inventory.getAdena();
				_inventory.addAdena(process, count, this, reference);
			}
			else if (_inventory.getAdena() < Integer.MAX_VALUE - count)
			{
				_inventory.addAdena(process, count, this, reference);
			}
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		// Game master don't need to pay
		if (isGM())
		{
			sendMessage("You are a Gm, you don't need to pay! reduceAdena = 0.");
			return true;
		}
		if (count > getAdena())
		{
			
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			_inventory.reduceAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			adenaItem = null;
		}
		
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addNumber(count);
			sendPacket(sm);
			sm = null;
		}
		
		if (count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(final String process, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			_inventory.reduceAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
				iu = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addNumber(count);
				sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
				sendPacket(sm);
				sm = null;
			}
			ancientAdenaItem = null;
		}
		
		return true;
	}
	
	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(final String process, final L2ItemInstance item, final L2Object reference, final boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					if (item.isStackable() && !Config.MULTIPLE_ITEM_DROP)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(item.getItemId());
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(item.getItemId());
						sm.addNumber(item.getCount());
						sendPacket(sm);
						sm = null;
					}
					
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					sendPacket(sm);
					sm = null;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item.getItemId());
					sendPacket(sm);
					sm = null;
				}
			}
			
			// Add the item to inventory
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
				playerIU = null;
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			su = null;
			
			// If over capacity, Drop the item
			if (!isGM() && !_inventory.validateCapacity(0))
			{
				dropItem("InvDrop", newitem, null, true, true);
			}
			else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
			newitem = null;
		}
		
		// If you pickup arrows.
		if (item.getItem().getItemType() == L2EtcItemType.ARROW)
		{
			// If a bow is equipped, try to equip them if no arrows is currently equipped.
			final L2Weapon currentWeapon = getActiveWeaponItem();
			if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
				checkAndEquipArrows();
		}
	}
	
	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
	{
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && (!isCastingNow() && ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB || ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				if (!isCastingNow() && !isCastingPotionNow())
				{
					L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(herb.getItemId());
					
					if (handler == null)
					{
						LOGGER.warn("No item handler registered for Herb - item ID " + herb.getItemId() + ".");
					}
					else
					{
						handler.useItem(this, herb);
						
						if (_herbstask >= 100)
						{
							_herbstask -= 100;
						}
						
						handler = null;
					}
					
					herb = null;
				}
				else
				{
					_herbstask += 100;
					ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
				}
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
				
				// Send inventory update packet
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(item);
					sendPacket(playerIU);
					playerIU = null;
				}
				else
				{
					sendPacket(new ItemList(this, false));
				}
				
				// Update current load as well
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				su = null;
				
				// If over capacity, drop the item
				if (!isGM() && !_inventory.validateCapacity(item))
				{
					dropItem("InvDrop", item, null, true, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
				
				item = null;
			}
		}
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(final String process, L2ItemInstance item, final L2Object reference, final boolean sendMessage)
	{
		item = _inventory.destroyItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(final String process, final int objectId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count || _inventory.destroyItem(process, objectId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(final String process, final int objectId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			return false;
		}
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCountWithoutTrace(process, -count, this, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				// could do also without saving, but let's save approx 1 of 10
				if (GameTimeController.getGameTicks() % 10 == 0)
				{
					item.updateDatabase();
				}
				_inventory.refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			_inventory.destroyItem(process, item, this, reference);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(final String process, final int itemId, final int count, final L2Object reference, final boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByItemId(itemId);
		
		if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		item = null;
		
		return true;
	}
	
	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void destroyWearedItems(final String process, final L2Object reference, final boolean sendMessage)
	{
		
		// Go through all Items of the inventory
		for (final L2ItemInstance item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
				{
					getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
				}
				
				if (_inventory.destroyItem(process, item, this, reference) == null)
				{
					LOGGER.warn("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}
				
				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				sm = null;
			}
		}
		
		// Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		ItemList il = new ItemList(getInventory().getItems(), true);
		sendPacket(il);
		il = null;
		
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers
		broadcastUserInfo();
		
		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");
		
	}
	
	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId the object id
	 * @param count : int Quantity of items to be transfered
	 * @param target the target
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(final String process, final int objectId, final int count, final Inventory target, final L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
			return null;
		
		final L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		playerSU = null;
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			}
			else
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
			targetPlayer = null;
			playerSU = null;
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
			petIU = null;
		}
		oldItem = null;
		
		return newItem;
	}
	
	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem the protect item
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(final String process, L2ItemInstance item, final L2Object reference, final boolean sendMessage, final boolean protectItem)
	{
		
		if (_freight.getItemByObjectId(item.getObjectId()) != null)
		{
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			this.sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + this.getName() + " of account " + this.getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return false;
			
		}
		
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return false;
		}
		
		item.dropMe(this, getClientX() + Rnd.get(50) - 25, getClientY() + Rnd.get(50) - 25, getClientZ() + 20);
		
		if (Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			
			if (Config.AUTODESTROY_ITEM_AFTER > 0)
			{ // autodestroy enabled
			
				if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
				{
					ItemsAutoDestroy.getInstance().addItem(item);
					item.setProtected(false);
				}
				else
				{
					item.setProtected(true);
				}
				
			}
			else
			{
				item.setProtected(true);
			}
			
		}
		else
		{
			item.setProtected(true);
			
		}
		
		if (protectItem)
			item.getDropProtection().protect(this);
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		
		return true;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem the protect item
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(final String process, final int objectId, final int count, final int x, final int y, final int z, final L2Object reference, final boolean sendMessage, final boolean protectItem)
	{
		
		if (_freight.getItemByObjectId(objectId) != null)
		{
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			this.sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + this.getName() + " of account " + this.getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return null;
			
		}
		
		L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		final L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
			{
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}
		
		if (protectItem)
			item.getDropProtection().protect(this);
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
			playerIU = null;
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		su = null;
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
			sm = null;
		}
		invitem = null;
		
		return item;
	}
	
	/**
	 * Check item manipulation.
	 * @param objectId the object id
	 * @param count the count
	 * @param action the action
	 * @return the l2 item instance
	 */
	public L2ItemInstance checkItemManipulation(final int objectId, final int count, final String action)
	{
		if (L2World.getInstance().findObject(objectId) == null)
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item not available in L2World");
			return null;
		}
		
		final L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if (count < 0 || count > 1 && !item.isStackable())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return null;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return null;
		}
		
		if (item.isWear())
			// cannot drop/trade wear-items
			return null;
		
		return item;
	}
	
	/**
	 * Set _protectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setProtection(final boolean protect)
	{
		if (Config.DEVELOPER && (protect || _protectEndTime > 0))
			LOGGER.info(getName() + ": Protection " + (protect ? "ON " + (GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) : "OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
		
		if (isInOlympiadMode())
			return;
		
		_protectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
		
		if (protect)
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportProtectionFinalizer(this), (Config.PLAYER_SPAWN_PROTECTION - 1) * 1000);
	}
	
	/**
	 * Set _teleportProtectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setTeleportProtection(final boolean protect)
	{
		if (Config.DEVELOPER && (protect || _teleportProtectEndTime > 0))
			LOGGER.warn(getName() + ": Tele Protection " + (protect ? "ON " + (GameTimeController.getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND) : "OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
		
		_teleportProtectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
		
		if (protect)
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportProtectionFinalizer(this), (Config.PLAYER_TELEPORT_PROTECTION - 1) * 1000);
	}
	
	static class TeleportProtectionFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		TeleportProtectionFinalizer(final L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_activeChar.isSpawnProtected())
					_activeChar.sendMessage("The effect of Spawn Protection has been removed.");
				else if (_activeChar.isTeleportProtected())
					_activeChar.sendMessage("The effect of Teleport Spawn Protection has been removed.");
				
				if (Config.PLAYER_SPAWN_PROTECTION > 0)
					_activeChar.setProtection(false);
				
				if (Config.PLAYER_TELEPORT_PROTECTION > 0)
					_activeChar.setTeleportProtection(false);
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
	}
	
	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 * @param protect the new recent fake death
	 */
	public void setRecentFakeDeath(final boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	/**
	 * Checks if is recent fake death.
	 * @return true, if is recent fake death
	 */
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Get the client owner of this char.<BR>
	 * <BR>
	 * @return the client
	 */
	public L2GameClient getClient()
	{
		return _client;
	}
	
	/**
	 * Sets the client.
	 * @param client the new client
	 */
	public void setClient(final L2GameClient client)
	{
		if (client == null && _client != null)
		{
			_client.stopGuardTask();
			nProtect.getInstance().closeSession(_client);
		}
		_client = client;
	}
	
	/**
	 * Close the active connection with the client.<BR>
	 * <BR>
	 */
	public void closeNetConnection()
	{
		if (_client != null)
		{
			_client.close(new LeaveWorld());
			setClient(null);
		}
	}
	
	/**
	 * Manage actions when a player click on this L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the target of the player</li> <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li> <li>If this L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li> <li>If this L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 * <li>If this L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * @param player The player that start an action on this L2PcInstance
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		// if ((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE))
		// no Interaction with not participant to events
		if (((TvT.is_started() || TvT.is_teleport()) && !Config.TVT_ALLOW_INTERFERENCE) || ((CTF.is_started() || CTF.is_teleport()) && !Config.CTF_ALLOW_INTERFERENCE) || ((DM.is_started() || DM.is_teleport()) && !Config.DM_ALLOW_INTERFERENCE))
		{
			if ((_inEventTvT && !player._inEventTvT) || (!_inEventTvT && player._inEventTvT))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if ((_inEventCTF && !player._inEventCTF) || (!_inEventCTF && player._inEventCTF))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if ((_inEventDM && !player._inEventDM) || (!_inEventDM && player._inEventDM))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		// Check if the L2PcInstance is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the player
			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
		}
		else
		{
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != 0)
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Calculate the distance between the L2PcInstance
				if (canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
			else
			{
				/*
				 * //during teleport phase, players cant do any attack if((TvT.is_teleport() && _inEventTvT) || (CTF.is_teleport() && _inEventCTF) || (DM.is_teleport() && _inEventDM)){ player.sendPacket(ActionFailed.STATIC_PACKET); return; } if (TvT.is_started()) { if ((_inEventTvT &&
				 * player._teamNameTvT.equals(_teamNameTvT))) { player.sendPacket(ActionFailed.STATIC_PACKET); return; } } if(CTF.is_started()){ if ((_inEventCTF && player._teamNameCTF.equals(_teamNameCTF))) { player.sendPacket(ActionFailed.STATIC_PACKET); return; } }
				 */
				// Check if this L2PcInstance is autoAttackable
				// if (isAutoAttackable(player) || (player._inEventTvT && TvT._started) || (player._inEventCTF && CTF._started) || (player._inEventDM && DM._started) || (player._inEventVIP && VIP._started))
				if (isAutoAttackable(player))
				{
					
					if (Config.ALLOW_CHAR_KILL_PROTECT)
					{
						Siege siege = SiegeManager.getInstance().getSiege(player);
						
						if (siege != null && siege.getIsInProgress())
						{
							if (player.getLevel() > 20 && ((L2Character) player.getTarget()).getLevel() < 20)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 40 && ((L2Character) player.getTarget()).getLevel() < 40)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 52 && ((L2Character) player.getTarget()).getLevel() < 52)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 61 && ((L2Character) player.getTarget()).getLevel() < 61)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() > 76 && ((L2Character) player.getTarget()).getLevel() < 76)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 20 && ((L2Character) player.getTarget()).getLevel() > 20)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 40 && ((L2Character) player.getTarget()).getLevel() > 40)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 52 && ((L2Character) player.getTarget()).getLevel() > 52)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 61 && ((L2Character) player.getTarget()).getLevel() > 61)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
							
							if (player.getLevel() < 76 && ((L2Character) player.getTarget()).getLevel() > 76)
							{
								player.sendMessage("Your target is not in your grade!");
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
						siege = null;
					}
					
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder can't attack players with lvl < 21
					if (isCursedWeaponEquiped() && player.getLevel() < 21 || player.isCursedWeaponEquiped() && getLevel() < 21)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (Config.GEODATA > 0)
						{
							if (GeoData.getInstance().canSeeTarget(player, this))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
								player.onActionRequest();
							}
						}
						else
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
							player.onActionRequest();
						}
					}
				}
				else
				{
					if (Config.GEODATA > 0)
					{
						if (GeoData.getInstance().canSeeTarget(player, this))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
						}
					}
					else
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#onActionShift(com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onActionShift(final L2PcInstance player)
	{
		final L2Weapon currentWeapon = player.getActiveWeaponItem();
		
		if (player.isGM())
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				AdminEditChar.gatherCharacterInfo(player, this, "charinfo.htm");
			}
		}
		else
		// Like L2OFF set the target of the L2PcInstance player
		{
			if (((TvT.is_started() || TvT.is_teleport()) && !Config.TVT_ALLOW_INTERFERENCE) || ((CTF.is_started() || CTF.is_teleport()) && !Config.CTF_ALLOW_INTERFERENCE) || ((DM.is_started() || DM.is_teleport()) && !Config.DM_ALLOW_INTERFERENCE))
			{
				if ((_inEventTvT && !player._inEventTvT) || (!_inEventTvT && player._inEventTvT))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				else if ((_inEventCTF && !player._inEventCTF) || (!_inEventCTF && player._inEventCTF))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				else if ((_inEventDM && !player._inEventDM) || (!_inEventDM && player._inEventDM))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			// Check if the L2PcInstance is confused
			if (player.isOutOfControl())
			{
				// Send a Server->Client packet ActionFailed to the player
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the player already target this L2PcInstance
			if (player.getTarget() != this)
			{
				// Set the target of the player
				player.setTarget(this);
				
				// Send a Server->Client packet MyTargetSelected to the player
				// The color to display in the select window is White
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
				// Check if this L2PcInstance has a Private Store
				if (getPrivateStoreType() != 0)
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
					
					// Calculate the distance between the L2PcInstance
					if (canInteract(player))
					{
						// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else
				{
					// Check if this L2PcInstance is autoAttackable
					// if (isAutoAttackable(player) || (player._inEventTvT && TvT._started) || (player._inEventCTF && CTF._started) || (player._inEventDM && DM._started) || (player._inEventVIP && VIP._started))
					if (isAutoAttackable(player))
					{
						
						if (Config.ALLOW_CHAR_KILL_PROTECT)
						{
							Siege siege = SiegeManager.getInstance().getSiege(player);
							
							if (siege != null && siege.getIsInProgress())
							{
								if (player.getLevel() > 20 && ((L2Character) player.getTarget()).getLevel() < 20)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 40 && ((L2Character) player.getTarget()).getLevel() < 40)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 52 && ((L2Character) player.getTarget()).getLevel() < 52)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 61 && ((L2Character) player.getTarget()).getLevel() < 61)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() > 76 && ((L2Character) player.getTarget()).getLevel() < 76)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 20 && ((L2Character) player.getTarget()).getLevel() > 20)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 40 && ((L2Character) player.getTarget()).getLevel() > 40)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 52 && ((L2Character) player.getTarget()).getLevel() > 52)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 61 && ((L2Character) player.getTarget()).getLevel() > 61)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
								if (player.getLevel() < 76 && ((L2Character) player.getTarget()).getLevel() > 76)
								{
									player.sendMessage("Your target is not in your grade!");
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
							}
							siege = null;
						}
						
						// Player with lvl < 21 can't attack a cursed weapon holder
						// And a cursed weapon holder can't attack players with lvl < 21
						if (isCursedWeaponEquiped() && player.getLevel() < 21 || player.isCursedWeaponEquiped() && getLevel() < 21)
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							if (Config.GEODATA > 0)
							{
								if (GeoData.getInstance().canSeeTarget(player, this))
								{
									// Calculate the distance between the L2PcInstance
									// Only archer can hit from long
									if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
									{
										player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
										player.onActionRequest();
									}
									else if (canInteract(player))
									{
										player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
										player.onActionRequest();
									}
									else
									{
										player.sendPacket(ActionFailed.STATIC_PACKET);
									}
								}
							}
							else
							{
								// Calculate the distance between the L2PcInstance
								// Only archer can hit from long
								if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
									player.onActionRequest();
								}
								else if (canInteract(player))
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
									player.onActionRequest();
								}
								else
								{
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
							}
						}
					}
					else
					{
						if (Config.GEODATA > 0)
						{
							if (GeoData.getInstance().canSeeTarget(player, this))
							{
								// Calculate the distance between the L2PcInstance
								// Only archer can hit from long
								if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
								}
								else if (canInteract(player))
								{
									player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
								}
								else
								{
									player.sendPacket(ActionFailed.STATIC_PACKET);
								}
								
							}
						}
						else
						{
							// Calculate the distance between the L2PcInstance
							// Only archer can hit from long
							if (currentWeapon != null && currentWeapon.getItemType() == L2WeaponType.BOW)
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
							}
							else if (canInteract(player))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
							}
							else
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance#isInFunEvent()
	 */
	@Override
	public boolean isInFunEvent()
	{
		return (atEvent || isInStartedTVTEvent() || isInStartedDMEvent() || isInStartedCTFEvent() || isInStartedVIPEvent());
	}
	
	public boolean isInStartedTVTEvent()
	{
		return (TvT.is_started() && _inEventTvT);
	}
	
	public boolean isRegisteredInTVTEvent()
	{
		return _inEventTvT;
	}
	
	public boolean isInStartedDMEvent()
	{
		return (DM.is_started() && _inEventDM);
	}
	
	public boolean isRegisteredInDMEvent()
	{
		return _inEventDM;
	}
	
	public boolean isInStartedCTFEvent()
	{
		return (CTF.is_started() && _inEventCTF);
	}
	
	public boolean isRegisteredInCTFEvent()
	{
		return _inEventCTF;
	}
	
	public boolean isInStartedVIPEvent()
	{
		return (VIP._started && _inEventVIP);
	}
	
	public boolean isRegisteredInVIPEvent()
	{
		return _inEventVIP;
	}
	
	/**
	 * Checks if is registered in fun event.
	 * @return true, if is registered in fun event
	 */
	public boolean isRegisteredInFunEvent()
	{
		return (atEvent || (_inEventTvT) || (_inEventDM) || (_inEventCTF) || (_inEventVIP) || Olympiad.getInstance().isRegistered(this));
	}
	
	// To Avoid Offensive skills when locked (during oly start or TODO other events start)
	/**
	 * Are player offensive skills locked.
	 * @return true, if successful
	 */
	public boolean arePlayerOffensiveSkillsLocked()
	{
		return isInOlympiadMode() && !isOlympiadStart();
	}
	
	/**
	 * Returns true if cp update should be done, false if not.
	 * @param barPixels the bar pixels
	 * @return boolean
	 */
	private boolean needCpUpdate(final int barPixels)
	{
		final double currentCp = getCurrentCp();
		
		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;
		
		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if mp update should be done, false if not.
	 * @param barPixels the bar pixels
	 * @return boolean
	 */
	private boolean needMpUpdate(final int barPixels)
	{
		final double currentMp = getCurrentMp();
		
		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;
		
		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance</li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		// We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		// super.broadcastStatusUpdate();
		
		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		if (Config.FORCE_COMPLETE_STATUS_UPDATE)
		{
			StatusUpdate su = new StatusUpdate(this);
			sendPacket(su);
			su = null;
		}
		else
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
			su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
			sendPacket(su);
			su = null;
		}
		
		// Check if a party is in progress and party window update is usefull
		if (isInParty() && (needCpUpdate(352) || super.needHpUpdate(352) || needMpUpdate(352)))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Send status for party window of " + getObjectId() + "(" + getName() + ") to his party. CP: " + getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
			}
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
			update = null;
		}
		
		if (isInOlympiadMode())
		{
			// TODO: implement new OlympiadUserInfo
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if (player.getOlympiadGameId() == getOlympiadGameId() && player.isOlympiadStart())
				{
					if (Config.DEBUG)
					{
						LOGGER.debug("Send status for Olympia window of " + getObjectId() + "(" + getName() + ") to " + player.getObjectId() + "(" + player.getName() + "). CP: " + getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
					}
					player.sendPacket(new ExOlympiadUserInfo(this, 1));
				}
			}
			if (Olympiad.getInstance().getSpectators(_olympiadGameId) != null && this.isOlympiadStart())
			{
				for (final L2PcInstance spectator : Olympiad.getInstance().getSpectators(_olympiadGameId))
				{
					if (spectator == null)
						continue;
					spectator.sendPacket(new ExOlympiadUserInfo(this, getOlympiadSide()));
				}
			}
		}
		if (isInDuel())
		{
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositTeam(this, update);
			update = null;
		}
	}
	
	// Custom PVP Color System - Start
	/**
	 * Update pvp color.
	 * @param pvpKillAmount the pvp kill amount
	 */
	public void updatePvPColor(final int pvpKillAmount)
	{
		if (Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be.
			if (isGM())
				return;
			
			// Check if the character is donator and if so, let them be.
			if (isDonator())
				return;
			
			if (pvpKillAmount >= Config.PVP_AMOUNT1 && pvpKillAmount < Config.PVP_AMOUNT2)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT1);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT2 && pvpKillAmount < Config.PVP_AMOUNT3)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT2);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT3 && pvpKillAmount < Config.PVP_AMOUNT4)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT3);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT4 && pvpKillAmount < Config.PVP_AMOUNT5)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT4);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT5)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT5);
			}
		}
	}
	
	// Custom PVP Color System - End
	
	// Custom Pk Color System - Start
	/**
	 * Update pk color.
	 * @param pkKillAmount the pk kill amount
	 */
	public void updatePkColor(final int pkKillAmount)
	{
		if (Config.PK_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be, like above.
			if (isGM())
				return;
			
			if (pkKillAmount >= Config.PK_AMOUNT1 && pkKillAmount < Config.PVP_AMOUNT2)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT1);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT2 && pkKillAmount < Config.PVP_AMOUNT3)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT2);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT3 && pkKillAmount < Config.PVP_AMOUNT4)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT3);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT4 && pkKillAmount < Config.PVP_AMOUNT5)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT4);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT5)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT5);
			}
		}
	}
	
	// Custom Pk Color System - End
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>. In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li> <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
	 * <BR>
	 */
	public final void broadcastUserInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] 03 CharInfo");
		}
		
		Broadcast.toKnownPlayers(this, new CharInfo(this));
	}
	
	/**
	 * Broadcast title info.
	 */
	public final void broadcastTitleInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet TitleUpdate to all L2PcInstance in _KnownPlayers of the L2PcInstance
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] cc TitleUpdate");
		}
		
		Broadcast.toKnownPlayers(this, new TitleUpdate(this));
	}
	
	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR>
	 * <BR>
	 * @return the ally id
	 */
	public int getAllyId()
	{
		if (_clan == null)
			return 0;
		return _clan.getAllyId();
	}
	
	/**
	 * Gets the ally crest id.
	 * @return the ally crest id
	 */
	public int getAllyCrestId()
	{
		if (getClanId() == 0 || getClan() == null)
			return 0;
		if (getClan().getAllyId() == 0)
			return 0;
		return getClan().getAllyCrestId();
	}
	
	/**
	 * Manage Interact Task with another L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li> <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li> <li>If the private store is a
	 * STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR>
	 * <BR>
	 * @param target The L2Character targeted
	 */
	public void doInteract(final L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
			{
				sendPacket(new PrivateStoreListSell(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreListBuy(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
			
			temp = null;
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
			{
				target.onAction(this);
			}
		}
	}
	
	/**
	 * Manage AutoLoot Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li> <li>Add the Item to the L2PcInstance inventory</li> <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li> <li>
	 * Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR>
	 * <BR>
	 * @param target The L2ItemInstance dropped
	 * @param item the item
	 */
	public void doAutoLoot(final L2Attackable target, final L2Attackable.RewardItem item)
	{
		if (isInParty())
		{
			getParty().distributeItem(this, item, false, target);
		}
		else if (item.getItemId() == 57)
		{
			addAdena("AutoLoot", item.getCount(), target, true);
		}
		else
		{
			addItem("AutoLoot", item.getItemId(), item.getCount(), target, true);
		}
	}
	
	/**
	 * Manage Pickup Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance</li> <li>Remove the L2ItemInstance from the world and send server->client GetItem packets</li> <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li> <li>Add the Item to the L2PcInstance
	 * inventory</li> <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li> <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR>
	 * <BR>
	 * @param object The L2ItemInstance to pick up
	 */
	protected void doPickupItem(final L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
			return;
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			LOGGER.warn(this + "trying to pickup wrong target." + getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		// Send a Server->Client packet ActionFailed to this L2PcInstance
		sendPacket(ActionFailed.STATIC_PACKET);
		
		// Send a Server->Client packet StopMove to this L2PcInstance
		StopMove sm = new StopMove(this);
		if (Config.DEBUG)
			LOGGER.debug("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
		sendPacket(sm);
		sm = null;
		
		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Like L2OFF you can't pickup items with private store opened
			if (getPrivateStoreType() != 0)
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this) && target.getItemId() != 8190 && target.getItemId() != 8689)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				final SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				return;
			}
			if ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
				return;
			}
			if (isInvul() && !isGM())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				smsg = null;
				return;
			}
			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
					smsg = null;
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
					smsg = null;
				}
				else
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target.getItemId());
					sendPacket(smsg);
					smsg = null;
				}
				return;
			}
			
			if (target.getItemId() == 57 && _inventory.getAdena() == Integer.MAX_VALUE)
			{
				sendMessage("You have reached the maximum amount of adena, please spend or deposit the adena so you may continue obtaining adena.");
				return;
			}
			
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}
			
			// Fixed it's not possible pick up the object if you exceed the maximum weight.
			if (_inventory.getTotalWeight() + target.getItem().getWeight() * target.getCount() > getMaxLoad())
			{
				sendMessage("You have reached the maximun weight.");
				return;
			}
			
			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
			if (Config.SAVE_DROPPED_ITEM)
				ItemsOnGroundManager.getInstance().removeObject(target);
		}
		
		// Auto use herbs - pick up
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
			if (handler == null)
				LOGGER.debug("No item handler registered for item ID " + target.getItemId() + ".");
			else
				handler.useItem(this, target);
			ItemTable.getInstance().destroyItem("Consume", target, this, null);
			handler = null;
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			/*
			 * Lineage2.com: When a player that controls Akamanah acquires Zariche, the newly-acquired Zariche automatically disappeared, and the equipped Akamanah's level increases by 1. The same rules also apply in the opposite instance.
			 */
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// if item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType || target.getItem() instanceof L2Armor || target.getItem() instanceof L2Weapon)
			{
				if (target.getEnchantLevel() > 0)
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3);
					msg.addString(getName());
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
				else
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2);
					msg.addString(getName());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
			}
			
			// Check if a Party is in progress
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
			{
				addItem("Pickup", target, null, true);
				
				// Like L2OFF Auto-Equip arrows if player has a bow and player picks up arrows.
				if (target.getItem() != null && target.getItem().getItemType() == L2EtcItemType.ARROW)
					checkAndEquipArrows();
			}
		}
		target = null;
	}
	
	/**
	 * Set a target.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character</li> <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character</li> <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and
	 * L2PcInstance to _KnownObject of the L2Object)</li><BR>
	 * <BR>
	 * @param newTarget The L2Object to target
	 */
	@Override
	public void setTarget(L2Object newTarget)
	{
		// Check if the new target is visible
		if (newTarget != null && !newTarget.isVisible())
		{
			newTarget = null;
		}
		
		// Prevents /target exploiting
		if (newTarget != null)
		{
			if (!(newTarget instanceof L2PcInstance) || !isInParty() || !((L2PcInstance) newTarget).isInParty() || getParty().getPartyLeaderOID() != ((L2PcInstance) newTarget).getParty().getPartyLeaderOID())
			{
				if (Math.abs(newTarget.getZ() - getZ()) > Config.DIFFERENT_Z_NEW_MOVIE)
				{
					newTarget = null;
				}
			}
		}
		
		if (!isGM())
		{
			// Can't target and attack festival monsters if not participant
			if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
			{
				newTarget = null;
			}
			else if (isInParty() && getParty().isInDimensionalRift())
			{
				final byte riftType = getParty().getDimensionalRift().getType();
				final byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
				
				if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				{
					newTarget = null;
				}
			}
		}
		
		// Get the current target
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
				return; // no target change
				
			// Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character
			if (oldTarget instanceof L2Character)
			{
				((L2Character) oldTarget).removeStatusListener(this);
			}
		}
		oldTarget = null;
		
		// Add the L2PcInstance to the _statusListener of the new target if it's a L2Character
		if (newTarget != null && newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
			TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			
			// Send packet just to me and to party, not to any other that does not use the information
			if (!this.isInParty())
			{
				this.sendPacket(my);
			}
			else
			{
				this._party.broadcastToPartyMembers(my);
			}
			
			my = null;
		}
		
		// Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR>
	 * <BR>
	 * @return the active weapon instance
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR>
	 * <BR>
	 * @return the active weapon item
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		final L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		return (L2Weapon) weapon.getItem();
	}
	
	/**
	 * Gets the chest armor instance.
	 * @return the chest armor instance
	 */
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	/**
	 * Gets the legs armor instance.
	 * @return the legs armor instance
	 */
	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	/**
	 * Gets the active chest armor item.
	 * @return the active chest armor item
	 */
	public L2Armor getActiveChestArmorItem()
	{
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
			return null;
		
		return (L2Armor) armor.getItem();
	}
	
	/**
	 * Gets the active legs armor item.
	 * @return the active legs armor item
	 */
	public L2Armor getActiveLegsArmorItem()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		
		if (legs == null)
			return null;
		
		return (L2Armor) legs.getItem();
	}
	
	/**
	 * Checks if is wearing heavy armor.
	 * @return true, if is wearing heavy armor
	 */
	public boolean isWearingHeavyArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.HEAVY && ((L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY))
				return true;
		}
		if (armor != null)
		{
			if ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing light armor.
	 * @return true, if is wearing light armor
	 */
	public boolean isWearingLightArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.LIGHT && ((L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT))
				return true;
		}
		if (armor != null)
		{
			if ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing magic armor.
	 * @return true, if is wearing magic armor
	 */
	public boolean isWearingMagicArmor()
	{
		final L2ItemInstance legs = getLegsArmorInstance();
		final L2ItemInstance armor = getChestArmorInstance();
		
		if (armor != null && legs != null)
		{
			if ((L2ArmorType) legs.getItemType() == L2ArmorType.MAGIC && ((L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC))
				return true;
		}
		if (armor != null)
		{
			if ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && (L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing formal wear.
	 * @return true, if is wearing formal wear
	 */
	public boolean isWearingFormalWear()
	{
		return _IsWearingFormalWear;
	}
	
	/**
	 * Sets the checks if is wearing formal wear.
	 * @param value the new checks if is wearing formal wear
	 */
	public void setIsWearingFormalWear(final boolean value)
	{
		_IsWearingFormalWear = value;
	}
	
	/**
	 * Checks if is married.
	 * @return true, if is married
	 */
	public boolean isMarried()
	{
		return _married;
	}
	
	/**
	 * Sets the married.
	 * @param state the new married
	 */
	public void setMarried(final boolean state)
	{
		_married = state;
	}
	
	/**
	 * Married type.
	 * @return the int
	 */
	public int marriedType()
	{
		return _marriedType;
	}
	
	/**
	 * Sets the married type.
	 * @param type the new married type
	 */
	public void setmarriedType(final int type)
	{
		_marriedType = type;
	}
	
	/**
	 * Checks if is engage request.
	 * @return true, if is engage request
	 */
	public boolean isEngageRequest()
	{
		return _engagerequest;
	}
	
	/**
	 * Sets the engage request.
	 * @param state the state
	 * @param playerid the playerid
	 */
	public void setEngageRequest(final boolean state, final int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}
	
	/**
	 * Sets the mary request.
	 * @param state the new mary request
	 */
	public void setMaryRequest(final boolean state)
	{
		_marryrequest = state;
	}
	
	/**
	 * Checks if is mary request.
	 * @return true, if is mary request
	 */
	public boolean isMaryRequest()
	{
		return _marryrequest;
	}
	
	/**
	 * Sets the marry accepted.
	 * @param state the new marry accepted
	 */
	public void setMarryAccepted(final boolean state)
	{
		_marryaccepted = state;
	}
	
	/**
	 * Checks if is marry accepted.
	 * @return true, if is marry accepted
	 */
	public boolean isMarryAccepted()
	{
		return _marryaccepted;
	}
	
	/**
	 * Gets the engage id.
	 * @return the engage id
	 */
	public int getEngageId()
	{
		return _engageid;
	}
	
	/**
	 * Gets the partner id.
	 * @return the partner id
	 */
	public int getPartnerId()
	{
		return _partnerId;
	}
	
	/**
	 * Sets the partner id.
	 * @param partnerid the new partner id
	 */
	public void setPartnerId(final int partnerid)
	{
		_partnerId = partnerid;
	}
	
	/**
	 * Gets the couple id.
	 * @return the couple id
	 */
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	/**
	 * Sets the couple id.
	 * @param coupleId the new couple id
	 */
	public void setCoupleId(final int coupleId)
	{
		_coupleId = coupleId;
	}
	
	/**
	 * Engage answer.
	 * @param answer the answer
	 */
	public void EngageAnswer(final int answer)
	{
		if (!_engagerequest)
			return;
		else if (_engageid == 0)
			return;
		else
		{
			L2PcInstance ptarget = (L2PcInstance) L2World.getInstance().findObject(_engageid);
			setEngageRequest(false, 0);
			if (ptarget != null)
			{
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				}
				else
				{
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
				
				ptarget = null;
			}
		}
	}
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR>
	 * <BR>
	 * @return the secondary weapon instance
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand) or the fists weapon.<BR>
	 * <BR>
	 * @return the secondary weapon item
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		final L2Item item = weapon.getItem();
		
		if (item instanceof L2Weapon)
			return (L2Weapon) item;
		
		weapon = null;
		return null;
	}
	
	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty</li> <li>If necessary, unsummon the Pet of the killed L2PcInstance</li> <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance</li> <li>If the killed L2PcInstance has Karma, manage
	 * Drop Item</li> <li>Kill the L2PcInstance</li><BR>
	 * <BR>
	 * @param killer the killer
	 * @return true, if successful
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (Config.TW_RESS_ON_DIE)
		{
			int x1, y1, z1;
			x1 = getX();
			y1 = getY();
			z1 = getZ();
			L2TownZone Town;
			Town = TownManager.getInstance().getTown(x1, y1, z1);
			if (Town != null && isinTownWar())
			{
				if (Town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
				{
					reviveRequest(this, null, false);
				}
				else if (Config.TW_ALL_TOWNS)
				{
					reviveRequest(this, null, false);
				}
			}
		}
		// Kill the L2PcInstance
		if (!super.doDie(killer))
			return false;
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
				castle = null;
			}
		}
		
		if (killer != null)
		{
			final L2PcInstance pk = killer.getActingPlayer();
			if (pk != null)
			{
				if (Config.ENABLE_PK_INFO)
				{
					doPkInfo(pk);
				}
				
				if (atEvent)
				{
					pk.kills.add(getName());
				}
				
				if (_inEventTvT && pk._inEventTvT)
				{
					if (TvT.is_teleport() || TvT.is_started())
					{
						if (!(pk._teamNameTvT.equals(_teamNameTvT)))
						{
							final PlaySound ps = new PlaySound(0, "ItemSound.quest_itemget", 1, getObjectId(), getX(), getY(), getZ());
							_countTvTdies++;
							pk._countTvTkills++;
							pk.setTitle("Kills: " + pk._countTvTkills);
							pk.sendPacket(ps);
							pk.broadcastUserInfo();
							TvT.setTeamKillsCount(pk._teamNameTvT, TvT.teamKillsCount(pk._teamNameTvT) + 1);
							pk.broadcastUserInfo();
						}
						else
						{
							pk.sendMessage("You are a teamkiller !!! Teamkills not counting.");
						}
						sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								teleToLocation(TvT._teamsX.get(TvT._teams.indexOf(_teamNameTvT)) + Rnd.get(201) - 100, TvT._teamsY.get(TvT._teams.indexOf(_teamNameTvT)) + Rnd.get(201) - 100, TvT._teamsZ.get(TvT._teams.indexOf(_teamNameTvT)), false);
								doRevive();
							}
						}, Config.TVT_REVIVE_DELAY);
					}
				}
				else if (_inEventTvT)
				{
					if (TvT.is_teleport() || TvT.is_started())
					{
						sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								teleToLocation(TvT._teamsX.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsY.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsZ.get(TvT._teams.indexOf(_teamNameTvT)), false);
								doRevive();
								broadcastPacket(new SocialAction(getObjectId(), 15));
							}
						}, Config.TVT_REVIVE_DELAY);
					}
				}
				else if (_inEventCTF)
				{
					if (CTF.is_teleport() || CTF.is_started())
					{
						sendMessage("You will be revived and teleported to team flag in 20 seconds!");
						if (_haveFlagCTF)
							removeCTFFlagOnDie();
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								teleToLocation(CTF._teamsX.get(CTF._teams.indexOf(_teamNameCTF)), CTF._teamsY.get(CTF._teams.indexOf(_teamNameCTF)), CTF._teamsZ.get(CTF._teams.indexOf(_teamNameCTF)), false);
								doRevive();
							}
						}, 20000);
					}
				}
				else if (_inEventDM && pk._inEventDM)
				{
					if (DM.is_teleport() || DM.is_started())
					{
						pk._countDMkills++;
						final PlaySound ps = new PlaySound(0, "ItemSound.quest_itemget", 1, getObjectId(), getX(), getY(), getZ());
						pk.setTitle("Kills: " + pk._countDMkills);
						pk.sendPacket(ps);
						pk.broadcastUserInfo();
						
						if (Config.DM_ENABLE_KILL_REWARD)
						{
							
							final L2Item reward = ItemTable.getInstance().getTemplate(Config.DM_KILL_REWARD_ID);
							pk.getInventory().addItem("DM Kill Reward", Config.DM_KILL_REWARD_ID, Config.DM_KILL_REWARD_AMOUNT, this, null);
							pk.sendMessage("You have earned " + Config.DM_KILL_REWARD_AMOUNT + " item(s) of ID " + reward.getName() + ".");
							
						}
						
						sendMessage("You will be revived and teleported to spot in 20 seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								final Location p_loc = DM.get_playersSpawnLocation();
								teleToLocation(p_loc._x, p_loc._y, p_loc._z, false);
								doRevive();
							}
						}, Config.DM_REVIVE_DELAY);
					}
				}
				else if (_inEventDM)
				{
					if (DM.is_teleport() || DM.is_started())
					{
						sendMessage("You will be revived and teleported to spot in 20 seconds!");
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								final Location players_loc = DM.get_playersSpawnLocation();
								teleToLocation(players_loc._x, players_loc._y, players_loc._z, false);
								doRevive();
							}
						}, 20000);
					}
				}
				else if (_inEventVIP && VIP._started)
				{
					if (_isTheVIP && !pk._inEventVIP)
					{
						Announcements.getInstance().announceToAll("VIP Killed by non-event character. VIP going back to initial spawn.");
						doRevive();
						teleToLocation(VIP._startX, VIP._startY, VIP._startZ);
						
					}
					else
					{
						if (_isTheVIP && pk._inEventVIP)
						{
							VIP.vipDied();
						}
						else
						{
							sendMessage("You will be revived and teleported to team spot in 20 seconds!");
							ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
							{
								@Override
								public void run()
								{
									doRevive();
									if (_isVIP)
										teleToLocation(VIP._startX, VIP._startY, VIP._startZ);
									else
										teleToLocation(VIP._endX, VIP._endY, VIP._endZ);
								}
							}, 20000);
						}
						
					}
					broadcastUserInfo();
				}
			}
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquiped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
			}
			else
			{
				if (pk == null || !pk.isCursedWeaponEquiped())
				{
					// if (getKarma() > 0)
					onDieDropItem(killer); // Check if any item should be dropped
					
					if (!(isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE)))
					{
						if ((pk != null) && pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember() && _clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getClanId()))
						{
							if (getClan().getReputationScore() > 0)
							{
								pk.getClan().setReputationScore(((L2PcInstance) killer).getClan().getReputationScore() + 2, true);
								pk.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(pk.getClan())); // Update status to all members
							}
							if (pk.getClan().getReputationScore() > 0)
							{
								_clan.setReputationScore(_clan.getReputationScore() - 2, true);
								_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan)); // Update status to all members
							}
						}
						if (Config.ALT_GAME_DELEVEL)
						{
							// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
							{
								deathPenalty((pk != null && getClan() != null && pk.getClan() != null && pk.getClan().isAtWarWith(getClanId())));
							}
						}
						else
						{
							onDieUpdateKarma(); // Update karma if delevel is not allowed
						}
					}
				}
			}
		}
		
		// Unsummon Cubics
		unsummonAllCubics();
		
		if (_forceBuff != null)
		{
			abortCast();
		}
		
		for (final L2Character character : getKnownList().getKnownCharacters())
			if (character.getTarget() == this)
			{
				if (character.isCastingNow())
					character.abortCast();
			}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		stopWaterTask();
		quakeSystem = 0;
		
		// leave war legend aura if enabled
		heroConsecutiveKillCount = 0;
		if (Config.WAR_LEGEND_AURA && !_hero && isPVPHero)
		{
			setHeroAura(false);
			this.sendMessage("You leaved War Legend State");
		}
		
		// Refresh focus force like L2OFF
		sendPacket(new EtcStatusUpdate(this));
		
		// After dead mob check if the killer got a moving task actived
		if (killer instanceof L2PcInstance)
		{
			if (((L2PcInstance) killer).isMovingTaskDefined())
			{
				((L2PcInstance) killer).startMovingTask();
			}
		}
		
		return true;
	}
	
	/**
	 * Removes the ctf flag on die.
	 */
	public void removeCTFFlagOnDie()
	{
		CTF._flagsTaken.set(CTF._teams.indexOf(_teamNameHaveFlagCTF), false);
		CTF.spawnFlag(_teamNameHaveFlagCTF);
		CTF.removeFlagFromPlayer(this);
		broadcastUserInfo();
		_haveFlagCTF = false;
		Announcements.getInstance().gameAnnounceToAll(CTF.get_eventName() + "(CTF): " + _teamNameHaveFlagCTF + "'s flag returned.");
	}
	
	/**
	 * On die drop item.
	 * @param killer the killer
	 */
	private void onDieDropItem(final L2Character killer)
	{
		if (atEvent || (TvT.is_started() && _inEventTvT) || (DM.is_started() && _inEventDM) || (CTF.is_started() && _inEventCTF) || (VIP._started && _inEventVIP) || killer == null)
			return;
		
		if (getKarma() <= 0 && killer instanceof L2PcInstance && ((L2PcInstance) killer).getClan() != null && getClan() != null && ((L2PcInstance) killer).getClan().isAtWarWith(getClanId()))
			// || this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId()))
			return;
		
		if (!isInsideZone(ZONE_PVP) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			final boolean isKillerNpc = killer instanceof L2NpcInstance;
			final int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
			int dropCount = 0;
			while (dropPercent > 0 && Rnd.get(100) < dropPercent && dropCount < dropLimit)
			{
				int itemDropPercent = 0;
				List<Integer> nonDroppableList = new FastList<>();
				List<Integer> nonDroppableListPet = new FastList<>();
				
				nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				
				for (final L2ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (itemDrop.isAugmented() || // Dont drop augmented items
					itemDrop.isShadowItem() || // Dont drop Shadow Items
					itemDrop.getItemId() == 57 || // Adena
					itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST || // Quest Items
					nonDroppableList.contains(itemDrop.getItemId()) || // Item listed in the non droppable item list
					nonDroppableListPet.contains(itemDrop.getItemId()) || // Item listed in the non droppable pet item list
					getPet() != null && getPet().getControlItemId() == itemDrop.getItemId() // Control Item of active pet
					)
					{
						continue;
					}
					
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
					}
					else
					{
						itemDropPercent = dropItem; // Item in inventory
					}
					
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						if (isKarmaDrop)
						{
							dropItem("DieDrop", itemDrop, killer, true, false);
							final String text = getName() + " has karma and dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount();
							Log.add(text, "karma_dieDrop");
						}
						else
						{
							dropItem("DieDrop", itemDrop, killer, true, true);
							final String text = getName() + " dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount();
							Log.add(text, "dieDrop");
						}
						
						dropCount++;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * On die update karma.
	 */
	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if (getKarma() > 0)
		{
			// this formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // multiply by char lvl
			karmaLost *= getLevel() / 100.0; // divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
				karmaLost = 1;
			
			// Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int) karmaLost);
		}
	}
	
	/**
	 * On kill update pvp karma.
	 * @param target the target
	 */
	public void onKillUpdatePvPKarma(final L2Character target)
	{
		if (target == null)
			return;
		
		if (!(target instanceof L2PlayableInstance))
			return;
		
		if ((_inEventCTF && CTF.is_started()) || (_inEventTvT && TvT.is_started()) || (_inEventVIP && VIP._started) || (_inEventDM && DM.is_started()))
			return;
		
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			// Custom message for time left
			// CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId);
			// SystemMessage msg = new SystemMessage(SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
			// int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
			// msg.addItemName(_cursedWeaponEquipedId);
			// msg.addNumber(timeLeftInHours);
			// sendPacket(msg);
			return;
		}
		
		L2PcInstance targetPlayer = null;
		
		if (target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
			return; // Target player is null
			
		if (targetPlayer == this)
		{
			targetPlayer = null;
			return; // Target player is self
		}
		
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			return;
		}
		
		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
			return;
		
		// If in Arena, do nothing
		if (isInsideZone(ZONE_PVP) || targetPlayer.isInsideZone(ZONE_PVP))
			return;
		
		// check anti-farm
		if (!checkAntiFarm(targetPlayer))
			return;
		
		if (Config.ANTI_FARM_SUMMON)
		{
			if (target instanceof L2SummonInstance)
				return;
		}
		
		// Check if it's pvp
		if (checkIfPvP(target) && targetPlayer.getPvpFlag() != 0 || isInsideZone(ZONE_PVP) && targetPlayer.isInsideZone(ZONE_PVP))
		{
			increasePvpKills();
		}
		else
		{
			// check about wars
			if (targetPlayer.getClan() != null && getClan() != null)
			{
				if (getClan().isAtWarWith(targetPlayer.getClanId()))
				{
					if (targetPlayer.getClan().isAtWarWith(getClanId()))
					{
						// 'Both way war' -> 'PvP Kill'
						increasePvpKills();
						if (target instanceof L2PcInstance && Config.ANNOUNCE_PVP_KILL)
						{
							Announcements.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
						}
						else if (target instanceof L2PcInstance && Config.ANNOUNCE_ALL_KILL)
						{
							Announcements.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
						}
						addItemReward(targetPlayer);
						return;
					}
				}
			}
			
			// 'No war' or 'One way war' -> 'Normal PK'
			if (!(_inEventTvT && TvT.is_started()) || !(_inEventCTF && CTF.is_started()) || !(_inEventVIP && VIP._started) || !(_inEventDM && DM.is_started()))
			{
				if (targetPlayer.getKarma() > 0) // Target player has karma
				{
					if (Config.KARMA_AWARD_PK_KILL)
					{
						increasePvpKills();
					}
					
					if (target instanceof L2PcInstance && Config.ANNOUNCE_PVP_KILL)
					{
						Announcements.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
					}
				}
				else if (targetPlayer.getPvpFlag() == 0) // Target player doesn't have karma
				{
					increasePkKillsAndKarma(targetPlayer.getLevel());
					if (target instanceof L2PcInstance && Config.ANNOUNCE_PK_KILL)
					{
						Announcements.getInstance().announceToAll("Player " + getName() + " has assassinated Player " + target.getName());
					}
				}
			}
		}
		if (target instanceof L2PcInstance && Config.ANNOUNCE_ALL_KILL)
		{
			Announcements.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
		}
		
		if (_inEventDM && DM.is_started())
		{
			return;
		}
		
		if (targetPlayer.getObjectId() == _lastKill)
		{
			count += 1;
		}
		else
		{
			count = 1;
			_lastKill = targetPlayer.getObjectId();
		}
		
		if (Config.REWARD_PROTECT == 0 || count <= Config.REWARD_PROTECT)
		{
			addItemReward(targetPlayer);
		}
	}
	
	/**
	 * Check anti farm.
	 * @param targetPlayer the target player
	 * @return true, if successful
	 */
	private boolean checkAntiFarm(final L2PcInstance targetPlayer)
	{
		
		if (Config.ANTI_FARM_ENABLED)
		{
			
			// Anti FARM Clan - Ally
			if (Config.ANTI_FARM_CLAN_ALLY_ENABLED && (getClanId() > 0 && targetPlayer.getClanId() > 0 && getClanId() == targetPlayer.getClanId()) || (getAllyId() > 0 && targetPlayer.getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId()))
			{
				this.sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + this.getName() + " and " + targetPlayer.getName() + ". CLAN or ALLY.");
				return false;
			}
			
			// Anti FARM level player < 40
			if (Config.ANTI_FARM_LVL_DIFF_ENABLED && targetPlayer.getLevel() < Config.ANTI_FARM_MAX_LVL_DIFF)
			{
				this.sendMessage("Farm is punishable with Ban! Don't kill new players! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + this.getName() + " and " + targetPlayer.getName() + ". LVL DIFF.");
				return false;
			}
			
			// Anti FARM pdef < 300
			if (Config.ANTI_FARM_PDEF_DIFF_ENABLED && targetPlayer.getPDef(targetPlayer) < Config.ANTI_FARM_MAX_PDEF_DIFF)
			{
				this.sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + this.getName() + " and " + targetPlayer.getName() + ". MAX PDEF DIFF.");
				return false;
			}
			
			// Anti FARM p atk < 300
			if (Config.ANTI_FARM_PATK_DIFF_ENABLED && targetPlayer.getPAtk(targetPlayer) < Config.ANTI_FARM_MAX_PATK_DIFF)
			{
				this.sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + this.getName() + " and " + targetPlayer.getName() + ". MAX PATK DIFF.");
				return false;
			}
			
			// Anti FARM Party
			if (Config.ANTI_FARM_PARTY_ENABLED && this.getParty() != null && targetPlayer.getParty() != null && this.getParty().equals(targetPlayer.getParty()))
			{
				this.sendMessage("Farm is punishable with Ban! Gm informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + this.getName() + " and " + targetPlayer.getName() + ". SAME PARTY.");
				return false;
			}
			
			// Anti FARM same Ip
			if (Config.ANTI_FARM_IP_ENABLED)
			{
				
				if (this.getClient() != null && targetPlayer.getClient() != null)
				{
					final String ip1 = this.getClient().getConnection().getInetAddress().getHostAddress();
					final String ip2 = targetPlayer.getClient().getConnection().getInetAddress().getHostAddress();
					
					if (ip1.equals(ip2))
					{
						this.sendMessage("Farm is punishable with Ban! Gm informed.");
						LOGGER.info("PVP POINT FARM ATTEMPT: " + this.getName() + " and " + targetPlayer.getName() + ". SAME IP.");
						return false;
					}
				}
			}
			return true;
		}
		return true;
	}
	
	/**
	 * Adds the item reword.
	 * @param targetPlayer the target player
	 */
	private void addItemReward(final L2PcInstance targetPlayer)
	{
		// IP check
		if (targetPlayer.getClient() != null && targetPlayer.getClient().getConnection() != null)
		{
			if (targetPlayer.getClient().getConnection().getInetAddress() != getClient().getConnection().getInetAddress())
			{
				
				if (targetPlayer.getKarma() > 0 || targetPlayer.getPvpFlag() > 0) // killing target pk or in pvp
				{
					if (Config.PVP_REWARD_ENABLED)
					{
						final int item = Config.PVP_REWARD_ID;
						final L2Item reward = ItemTable.getInstance().getTemplate(item);
						
						final int amount = Config.PVP_REWARD_AMOUNT;
						
						getInventory().addItem("Winning PvP", Config.PVP_REWARD_ID, Config.PVP_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(_inventory.getItemByItemId(Config.PVP_REWARD_ID));
						sendPacket(iu);
						iu = null;
					}
				}
				else
				// target is not pk and not in pvp ---> PK KILL
				{
					if (Config.PK_REWARD_ENABLED)
					{
						final int item = Config.PK_REWARD_ID;
						final L2Item reward = ItemTable.getInstance().getTemplate(item);
						final int amount = Config.PK_REWARD_AMOUNT;
						getInventory().addItem("Winning PK", Config.PK_REWARD_ID, Config.PK_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(_inventory.getItemByItemId(Config.PK_REWARD_ID));
						sendPacket(iu);
						iu = null;
					}
				}
			}
			else
			{
				this.sendMessage("Farm is punishable with Ban! Don't kill your Box!");
				LOGGER.warn("PVP POINT FARM ATTEMPT: " + this.getName() + " and " + targetPlayer.getName() + ". SAME IP.");
			}
		}
	}
	
	/**
	 * Increase the pvp kills count and send the info to the player.
	 */
	public void increasePvpKills()
	{
		int x, y, z;
		x = getX();
		y = getY();
		z = getZ();
		L2TownZone Town;
		Town = TownManager.getInstance().getTown(x, y, z);
		if (Town != null && isinTownWar())
		{
			if (Town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
			{
				getInventory().addItem("TownWar", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, this);
				sendMessage("You received your prize for a town war kill!");
			}
			else if (Config.TW_ALL_TOWNS)
			{
				getInventory().addItem("TownWar", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, this);
				sendMessage("You received your prize for a town war kill!");
			}
		}
		
		if ((TvT.is_started() && _inEventTvT) || (DM.is_started() && _inEventDM) || (CTF.is_started() && _inEventCTF) || (VIP._started && _inEventVIP))
			return;
		
		// Add karma to attacker and increase its PK counter
		setPvpKills(getPvpKills() + 1);
		
		// Increase the kill count for a special hero aura
		heroConsecutiveKillCount++;
		
		// If heroConsecutiveKillCount == 30 give hero aura
		if (heroConsecutiveKillCount == Config.KILLS_TO_GET_WAR_LEGEND_AURA && Config.WAR_LEGEND_AURA)
		{
			setHeroAura(true);
			Announcements.getInstance().gameAnnounceToAll(getName() + " becames War Legend with " + Config.KILLS_TO_GET_WAR_LEGEND_AURA + " PvP!!");
			
		}
		
		if (Config.PVPEXPSP_SYSTEM)
		{
			addExpAndSp(Config.ADD_EXP, Config.ADD_SP);
			{
				sendMessage("Earned Exp & SP for a pvp kill");
			}
		}
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's name color if they reached any of the 5 PvP levels.
		updatePvPColor(getPvpKills());
		broadcastUserInfo();
		
		if (Config.ALLOW_QUAKE_SYSTEM)
		{
			QuakeSystem();
		}
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Quake system.
	 */
	public void QuakeSystem()
	{
		quakeSystem++;
		switch (quakeSystem)
		{
			case 5:
				if (Config.ENABLE_ANTI_PVP_FARM_MSG)
				{
					final CreatureSay cs12 = new CreatureSay(0, 15, "", getName() + " 5 consecutive kill! Only Gm."); // 8D
					
					for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player != null)
							if (player.isOnline() != 0)
								if (player.isGM())
								{
									player.sendPacket(cs12);
								}
					}
				}
				break;
			case 6:
				final CreatureSay cs = new CreatureSay(0, 15, "", getName() + " is Dominating!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs);
				}
				break;
			case 9:
				final CreatureSay cs2 = new CreatureSay(0, 15, "", getName() + " is on a Rampage!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs2);
				}
				break;
			case 14:
				final CreatureSay cs3 = new CreatureSay(0, 15, "", getName() + " is on a Killing Spree!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs3);
				}
				break;
			case 18:
				final CreatureSay cs4 = new CreatureSay(0, 15, "", getName() + " is on a Monster Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs4);
				}
				break;
			case 22:
				final CreatureSay cs5 = new CreatureSay(0, 15, "", getName() + " is Unstoppable!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs5);
				}
				break;
			case 25:
				final CreatureSay cs6 = new CreatureSay(0, 15, "", getName() + " is on an Ultra Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs6);
				}
				break;
			case 28:
				final CreatureSay cs7 = new CreatureSay(0, 15, "", getName() + " God Blessed!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs7);
				}
				break;
			case 32:
				final CreatureSay cs8 = new CreatureSay(0, 15, "", getName() + " is Wicked Sick!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs8);
				}
				break;
			case 35:
				final CreatureSay cs9 = new CreatureSay(0, 15, "", getName() + " is on a Ludricrous Kill!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs9);
				}
				break;
			case 40:
				final CreatureSay cs10 = new CreatureSay(0, 15, "", getName() + " is GodLike!"); // 8D
				for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (player != null)
						if (player.isOnline() != 0)
							player.sendPacket(cs10);
				}
		}
	}
	
	/**
	 * Get info on pk's from pk table.
	 * @param PlayerWhoKilled the player who killed
	 */
	public void doPkInfo(final L2PcInstance PlayerWhoKilled)
	{
		String killer = PlayerWhoKilled.getName();
		String killed = getName();
		int kills = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT kills FROM pkkills WHERE killerId=? AND killedId=?");
			statement.setString(1, killer);
			statement.setString(2, killed);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				kills = rset.getInt("kills");
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
		if (kills >= 1)
		{
			kills++;
			String UPDATE_PKKILLS = "UPDATE pkkills SET kills=? WHERE killerId=? AND killedId=?";
			Connection conect = null;
			try
			{
				conect = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = conect.prepareStatement(UPDATE_PKKILLS);
				statement.setInt(1, kills);
				statement.setString(2, killer);
				statement.setString(3, killed);
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
				UPDATE_PKKILLS = null;
			}
			catch (final SQLException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info("Could not update pkKills, got: " + e.getMessage());
			}
			finally
			{
				CloseUtil.close(conect);
				conect = null;
			}
			sendMessage("You have been killed " + kills + " times by " + PlayerWhoKilled.getName() + ".");
			PlayerWhoKilled.sendMessage("You have killed " + getName() + " " + kills + " times.");
		}
		else
		{
			String ADD_PKKILLS = "INSERT INTO pkkills (killerId,killedId,kills) VALUES (?,?,?)";
			Connection conect2 = null;
			try
			{
				conect2 = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = conect2.prepareStatement(ADD_PKKILLS);
				statement.setString(1, killer);
				statement.setString(2, killed);
				statement.setInt(3, 1);
				statement.execute();
				DatabaseUtils.close(statement);
				ADD_PKKILLS = null;
				statement = null;
			}
			catch (final SQLException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info("Could not add pkKills, got: " + e.getMessage());
			}
			finally
			{
				CloseUtil.close(conect2);
				conect2 = null;
			}
			sendMessage("This is the first time you have been killed by " + PlayerWhoKilled.getName() + ".");
			PlayerWhoKilled.sendMessage("You have killed " + getName() + " for the first time.");
		}
		killer = null;
		killed = null;
	}
	
	/**
	 * Increase pk count, karma and send the info to the player.
	 * @param targLVL : level of the killed player
	 */
	public void increasePkKillsAndKarma(final int targLVL)
	{
		if ((TvT.is_started() && _inEventTvT) || (DM.is_started() && _inEventDM) || (CTF.is_started() && _inEventCTF) || (VIP._started && _inEventVIP))
			return;
		
		final int baseKarma = Config.KARMA_MIN_KARMA;
		int newKarma = baseKarma;
		final int karmaLimit = Config.KARMA_MAX_KARMA;
		
		final int pkLVL = getLevel();
		final int pkPKCount = getPkKills();
		
		int lvlDiffMulti = 0;
		int pkCountMulti = 0;
		
		// Check if the attacker has a PK counter greater than 0
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		
		// Calculate the level difference Multiplier between attacker and killed L2PcInstance
		if (pkLVL > targLVL)
		{
			lvlDiffMulti = pkLVL / targLVL;
		}
		else
		{
			lvlDiffMulti = 1;
		}
		
		if (lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}
		
		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		newKarma *= pkCountMulti;
		newKarma *= lvlDiffMulti;
		
		// Make sure newKarma is less than karmaLimit and higher than baseKarma
		if (newKarma < baseKarma)
		{
			newKarma = baseKarma;
		}
		
		if (newKarma > karmaLimit)
		{
			newKarma = karmaLimit;
		}
		
		// Fix to prevent overflow (=> karma has a max value of 2 147 483 647)
		if (getKarma() > Integer.MAX_VALUE - newKarma)
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		// Add karma to attacker and increase its PK counter
		int x, y, z;
		x = getX();
		y = getY();
		z = getZ();
		
		// get local town
		final L2TownZone Town = TownManager.getInstance().getTown(x, y, z);
		
		setPkKills(getPkKills() + 1);
		
		/*
		 * if(!Config.TW_ALLOW_KARMA && Town != null && isinTownWar()) { //nothing } else
		 */
		if (Town == null || (isinTownWar() && Config.TW_ALLOW_KARMA))
		{
			setKarma(getKarma() + newKarma);
		}
		/*
		 * else if() { setKarma(getKarma() + newKarma); }
		 */
		
		if (Town != null && isinTownWar())
		{
			if (Town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
			{
				getInventory().addItem("TownWar", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, this);
				sendMessage("You received your prize for a town war kill!");
			}
			else if (Config.TW_ALL_TOWNS && Town.getTownId() != 0)
			{
				getInventory().addItem("TownWar", Config.TW_ITEM_ID, Config.TW_ITEM_AMOUNT, this, this);
				sendMessage("You received your prize for a town war kill!");
			}
		}
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's title color if they reached any of the 5 PK levels.
		updatePkColor(getPkKills());
		broadcastUserInfo();
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Calculate karma lost.
	 * @param exp the exp
	 * @return the int
	 */
	public int calculateKarmaLost(final long exp)
	{
		// KARMA LOSS
		// When a PKer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level
		
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
			karmaLost = Integer.MAX_VALUE;
		else
			karmaLost = (int) expGained;
		
		if (karmaLost < Config.KARMA_LOST_BASE)
			karmaLost = Config.KARMA_LOST_BASE;
		if (karmaLost > getKarma())
			karmaLost = getKarma();
		
		return karmaLost;
	}
	
	/**
	 * Update pvp status.
	 */
	public void updatePvPStatus()
	{
		if ((TvT.is_started() && _inEventTvT) || (CTF.is_started() && _inEventCTF) || (DM.is_started() && _inEventDM) || (VIP._started && _inEventVIP))
			return;
		
		if (isInsideZone(ZONE_PVP))
			return;
		
		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
		{
			startPvPFlag();
		}
	}
	
	/**
	 * Update pvp status.
	 * @param target the target
	 */
	public void updatePvPStatus(final L2Character target)
	{
		L2PcInstance player_target = null;
		
		if (target instanceof L2PcInstance)
		{
			player_target = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			player_target = ((L2Summon) target).getOwner();
		}
		
		if (player_target == null)
			return;
		
		if ((TvT.is_started() && _inEventTvT && player_target._inEventTvT) || (DM.is_started() && _inEventDM && player_target._inEventDM) || (CTF.is_started() && _inEventCTF && player_target._inEventCTF) || (VIP._started && _inEventVIP && player_target._inEventVIP))
			return;
		
		if (isInDuel() && player_target.getDuelId() == getDuelId())
			return;
		
		if ((!isInsideZone(ZONE_PVP) || !player_target.isInsideZone(ZONE_PVP)) && player_target.getKarma() == 0)
		{
			if (checkIfPvP(player_target))
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			}
			else
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			}
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
			}
		}
		player_target = null;
	}
	
	/**
	 * Restore the specified % of experience this L2PcInstance has lost and sends a Server->Client StatusUpdate packet.<BR>
	 * <BR>
	 * @param restorePercent the restore percent
	 */
	public void restoreExp(final double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}
	
	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the Experience loss</li> <li>Set the value of _expBeforeDeath</li> <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary</li> <li>Send a Server->Client StatusUpdate packet with its new Experience</li><BR>
	 * <BR>
	 * @param atwar the atwar
	 */
	public void deathPenalty(final boolean atwar)
	{
		// Get the level of the L2PcInstance
		final int lvl = getLevel();
		
		// The death steal you some Exp
		double percentLost = 4.0; // standart 4% (lvl>20)
		
		if (getLevel() < 20)
		{
			percentLost = 10.0;
		}
		else if (getLevel() >= 20 && getLevel() < 40)
		{
			percentLost = 7.0;
		}
		else if (getLevel() >= 40 && getLevel() < 75)
		{
			percentLost = 4.0;
		}
		else if (getLevel() >= 75 && getLevel() < 81)
		{
			percentLost = 2.0;
		}
		
		if (getKarma() > 0)
		{
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		}
		
		if (isFestivalParticipant() || atwar || isInsideZone(ZONE_SIEGE))
		{
			percentLost /= 4.0;
		}
		
		// Calculate the Experience loss
		long lostExp = 0;
		if (!atEvent && !(_inEventTvT && TvT.is_started()) && !(_inEventDM && DM.is_started()) && !(_inEventCTF && CTF.is_started()) && !(_inEventVIP && VIP._started))
		{
			final byte maxLvl = ExperienceData.getInstance().getMaxLevel();
			if (lvl < maxLvl)
			{
				lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
			}
			else
			{
				lostExp = Math.round((getStat().getExpForLevel(maxLvl) - getStat().getExpForLevel(maxLvl - 1)) * percentLost / 100);
			}
		}
		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		if (getCharmOfCourage())
		{
			if (getSiegeState() > 0 && isInsideZone(ZONE_SIEGE))
			{
				lostExp = 0;
			}
			setCharmOfCourage(false);
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getName() + " died and lost " + lostExp + " experience.");
		}
		
		// Set the new Experience value of the L2PcInstance
		getStat().addExp(-lostExp);
	}
	
	/**
	 * Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client System Message to the L2PcInstance : YOU_INCREASED_YOUR_LEVEL</li> <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP</li> <li>Set the current HP and MP of the L2PcInstance, Launch/Stop a HP/MP/CP Regeneration Task and
	 * send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)</li> <li>Recalculate the party level</li> <li>Recalculate the number of Recommandation that the L2PcInstance can give</li> <li>Give Expertise skill of this level and remove beginner Lucky skill</li><BR>
	 * <BR>
	 */
	public void increaseLevel()
	{
		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(), getMaxMp());
		setCurrentCp(getMaxCp());
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the RegenActive flag to False</li> <li>Stop the HP/MP/CP Regeneration task</li><BR>
	 * <BR>
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopRentPet();
		stopPvpRegTask();
		stopPunishTask(true);
		stopBotChecker();
		quakeSystem = 0;
	}
	
	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR>
	 * <BR>
	 * @return the pet
	 */
	@Override
	public L2Summon getPet()
	{
		return _summon;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.<BR>
	 * <BR>
	 * @param summon the new pet
	 */
	public void setPet(final L2Summon summon)
	{
		_summon = summon;
	}
	
	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR>
	 * <BR>
	 * @return the trained beast
	 */
	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.<BR>
	 * <BR>
	 * @param tamedBeast the new trained beast
	 */
	public void setTrainedBeast(final L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}
	
	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @return the request
	 */
	public L2Request getRequest()
	{
		return _request;
	}
	
	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @param requester the new active requester
	 */
	public synchronized void setActiveRequester(final L2PcInstance requester)
	{
		_activeRequester = requester;
	}
	
	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @return the active requester
	 */
	public synchronized L2PcInstance getActiveRequester()
	{
		final L2PcInstance requester = _activeRequester;
		if (requester != null)
		{
			if (requester.isRequestExpired() && _activeTradeList == null)
				_activeRequester = null;
		}
		return _activeRequester;
	}
	
	/**
	 * Return True if a transaction is in progress.<BR>
	 * <BR>
	 * @return true, if is processing request
	 */
	public boolean isProcessingRequest()
	{
		return _activeRequester != null || _requestExpireTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if a transaction is in progress.<BR>
	 * <BR>
	 * @return true, if is processing transaction
	 */
	public boolean isProcessingTransaction()
	{
		return _activeRequester != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 * @param partner the partner
	 */
	public void onTransactionRequest(final L2PcInstance partner)
	{
		_requestExpireTime = GameTimeController.getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		if (partner != null)
		{
			partner.setActiveRequester(this);
		}
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR>
	 * <BR>
	 * @param warehouse the new active warehouse
	 */
	public void setActiveWarehouse(final ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	/**
	 * Return active Warehouse.<BR>
	 * <BR>
	 * @return the active warehouse
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	/**
	 * Select the TradeList to be used in next activity.<BR>
	 * <BR>
	 * @param tradeList the new active trade list
	 */
	public void setActiveTradeList(final TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	/**
	 * Return active TradeList.<BR>
	 * <BR>
	 * @return the active trade list
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	/**
	 * On trade start.
	 * @param partner the partner
	 */
	public void onTradeStart(final L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		SystemMessage msg = new SystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1);
		msg.addString(partner.getName());
		sendPacket(msg);
		sendPacket(new TradeStart(this));
		msg = null;
	}
	
	/**
	 * On trade confirm.
	 * @param partner the partner
	 */
	public void onTradeConfirm(final L2PcInstance partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessageId.S1_CONFIRMED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
		msg = null;
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	/**
	 * On trade cancel.
	 * @param partner the partner
	 */
	public void onTradeCancel(final L2PcInstance partner)
	{
		if (_activeTradeList == null)
			return;
		
		_activeTradeList.lock();
		_activeTradeList = null;
		
		sendPacket(new SendTradeDone(0));
		SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANCELED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
		msg = null;
	}
	
	/**
	 * On trade finish.
	 * @param successfull the successfull
	 */
	public void onTradeFinish(final boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
		{
			sendPacket(new SystemMessage(SystemMessageId.TRADE_SUCCESSFUL));
		}
	}
	
	/**
	 * Start trade.
	 * @param partner the partner
	 */
	public void startTrade(final L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	/**
	 * Cancel active trade.
	 */
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;
		
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null)
		{
			partner.onTradeCancel(this);
			partner = null;
		}
		onTradeCancel(this);
	}
	
	/**
	 * Return the _createList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the creates the list
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}
	
	/**
	 * Set the _createList object of the L2PcInstance.<BR>
	 * <BR>
	 * @param x the new creates the list
	 */
	public void setCreateList(final L2ManufactureList x)
	{
		_createList = x;
	}
	
	/**
	 * Return the _sellList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the sell list
	 */
	public TradeList getSellList()
	{
		if (_sellList == null)
		{
			_sellList = new TradeList(this);
		}
		return _sellList;
	}
	
	/**
	 * Return the _buyList object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the buy list
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null)
		{
			_buyList = new TradeList(this);
		}
		return _buyList;
	}
	
	/**
	 * Set the Private Store type of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>0 : STORE_PRIVATE_NONE</li> <li>1 : STORE_PRIVATE_SELL</li> <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 * @param type the new private store type
	 */
	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
		
		if (_privatestore == STORE_PRIVATE_NONE && (getClient() == null || isInOfflineMode()))
		{
			/*
			 * if(this._originalNameColorOffline!=0) getAppearance().setNameColor(this._originalNameColorOffline); else getAppearance().setNameColor(_accessLevel.getNameColor());
			 */
			this.store();
			if (Config.OFFLINE_DISCONNECT_FINISHED)
			{
				this.deleteMe();
				
				if (this.getClient() != null)
				{
					this.getClient().setActiveChar(null); // prevent deleteMe from being called a second time on disconnection
				}
			}
		}
	}
	
	/**
	 * Return the Private Store type of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>0 : STORE_PRIVATE_NONE</li> <li>1 : STORE_PRIVATE_SELL</li> <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 * @return the private store type
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	/**
	 * Set the _skillLearningClassId object of the L2PcInstance.<BR>
	 * <BR>
	 * @param classId the new skill learning class id
	 */
	public void setSkillLearningClassId(final ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	
	/**
	 * Return the _skillLearningClassId object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the skill learning class id
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.<BR>
	 * <BR>
	 * @param clan the new clan
	 */
	public void setClan(final L2Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		_clanId = clan.getClanId();
		
		// Add clan leader skills if clanleader
		if (isClanLeader() && clan.getLevel() >= 4)
		{
			addClanLeaderSkills(true);
		}
		else
		{
			addClanLeaderSkills(false);
		}
		
	}
	
	/**
	 * Return the _clan object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the clan
	 */
	public L2Clan getClan()
	{
		return _clan;
	}
	
	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR>
	 * <BR>
	 * @return true, if is clan leader
	 */
	public boolean isClanLeader()
	{
		if (getClan() == null)
			return false;
		return getObjectId() == getClan().getLeaderId();
	}
	
	/**
	 * Reduce the number of arrows owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR>
	 * <BR>
	 */
	@Override
	protected void reduceArrowCount()
	{
		L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, this, null);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("arrow count:" + (arrows == null ? 0 : arrows.getCount()));
		}
		
		if (arrows == null || arrows.getCount() == 0)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("removed arrows count");
			}
			
			sendPacket(new ItemList(this, false));
		}
		else
		{
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(arrows);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			arrows = null;
		}
	}
	
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				final ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equiped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
	/**
	 * Disarm the player's weapon and shield.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean disarmWeapons()
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquiped() && !getAccessLevel().isGm())
			return false;
		
		// Unequip the weapon
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		}
		
		if (wpn != null)
		{
			if (wpn.isWear())
				return false;
			
			// Remove augementation boni on unequip
			if (wpn.isAugmented())
			{
				wpn.getAugmentation().removeBoni(this);
			}
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			iu = null;
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
				sm = null;
			}
			wpn = null;
			unequiped = null;
		}
		
		// Unequip the shield
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
				return false;
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			iu = null;
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
				sm = null;
			}
			sld = null;
			unequiped = null;
		}
		return true;
	}
	
	/**
	 * Return True if the L2PcInstance use a dual weapon.<BR>
	 * <BR>
	 * @return true, if is using dual weapon
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		final L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
			return false;
		
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
			return true;
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
			return true;
		else if (weaponItem.getItemId() == 248) // orc fighter fists
			return true;
		else if (weaponItem.getItemId() == 252) // orc mage fists
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the uptime.
	 * @param time the new uptime
	 */
	public void setUptime(final long time)
	{
		_uptime = time;
	}
	
	/**
	 * Gets the uptime.
	 * @return the uptime
	 */
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	/**
	 * Return True if the L2PcInstance is invulnerable.<BR>
	 * <BR>
	 * @return true, if is invul
	 */
	@Override
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || _protectEndTime > GameTimeController.getGameTicks() || _teleportProtectEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2PcInstance has a Party in progress.<BR>
	 * <BR>
	 * @return true, if is in party
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	/**
	 * Set the _party object of the L2PcInstance (without joining it).<BR>
	 * <BR>
	 * @param party the new party
	 */
	public void setParty(final L2Party party)
	{
		_party = party;
	}
	
	/**
	 * Set the _party object of the L2PcInstance AND join it.<BR>
	 * <BR>
	 * @param party the party
	 */
	public void joinParty(final L2Party party)
	{
		if (party == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() == 9)
		{
			sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		
		if (party.getPartyMembers().contains(this))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() < 9)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			_party = party;
			party.addPartyMember(this);
		}
	}
	
	/**
	 * Return true if the L2PcInstance is a GM.<BR>
	 * <BR>
	 * @return true, if is gM
	 */
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	/**
	 * Return true if the L2PcInstance is a Administrator.<BR>
	 * <BR>
	 * @return true, if is administrator
	 */
	public boolean isAdministrator()
	{
		return getAccessLevel().getLevel() == Config.MASTERACCESS_LEVEL;
	}
	
	/**
	 * Return true if the L2PcInstance is a User.<BR>
	 * <BR>
	 * @return true, if is user
	 */
	public boolean isUser()
	{
		return getAccessLevel().getLevel() == Config.USERACCESS_LEVEL;
	}
	
	/**
	 * Checks if is normal gm.
	 * @return true, if is normal gm
	 */
	public boolean isNormalGm()
	{
		return !isAdministrator() && !isUser();
	}
	
	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR>
	 * <BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}
	
	/**
	 * Return the _party object of the L2PcInstance.<BR>
	 * <BR>
	 * @return the party
	 */
	@Override
	public L2Party getParty()
	{
		return _party;
	}
	
	/**
	 * Set the _isGm Flag of the L2PcInstance.<BR>
	 * <BR>
	 * @param first_log the new first LOGGER
	 */
	// public void setIsGM(boolean status)
	// {
	// _isGm = status;
	// }
	
	public void setFirstLog(final int first_log)
	{
		_first_log = false;
		if (first_log == 1)
		{
			_first_log = true;
		}
	}
	
	/**
	 * Sets the first LOGGER.
	 * @param first_log the new first LOGGER
	 */
	public void setFirstLog(final boolean first_log)
	{
		_first_log = first_log;
	}
	
	/**
	 * Gets the first LOGGER.
	 * @return the first LOGGER
	 */
	public boolean getFirstLog()
	{
		return _first_log;
	}
	
	/**
	 * Manage a cancel cast task for the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the Intention of the AI to AI_INTENTION_IDLE</li> <li>Enable all skills (set _allSkillsDisabled to False)</li> <li>Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)</li><BR>
	 * <BR>
	 */
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Enable all skills (set _allSkillsDisabled to False)
		enableAllSkills();
		
		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		
		// Broadcast the packet to self and known players.
		Broadcast.toSelfAndKnownPlayersInRadius(this, msc, 810000/* 900 */);
		msc = null;
	}
	
	/**
	 * Set the _accessLevel of the L2PcInstance.<BR>
	 * <BR>
	 * @param level the new access level
	 */
	public void setAccessLevel(final int level)
	{
		if (level == Config.MASTERACCESS_LEVEL)
		{
			LOGGER.warn("Admin Login at " + fmt.format(new Date(System.currentTimeMillis())) + " " + getName() + " logs in game with AccessLevel " + level + ".");
			_accessLevel = AccessLevels.getInstance()._masterAccessLevel;
		}
		else if (level == Config.USERACCESS_LEVEL)
		{
			_accessLevel = AccessLevels.getInstance()._userAccessLevel;
		}
		else
		{
			if (level > 0)
			{
				LOGGER.warn("GM Login at " + fmt.format(new Date(System.currentTimeMillis())) + " " + getName() + " logs in game with AccessLevel " + level + ".");
			}
			AccessLevel accessLevel = AccessLevels.getInstance().getAccessLevel(level);
			
			if (accessLevel == null)
			{
				if (level < 0)
				{
					AccessLevels.getInstance().addBanAccessLevel(level);
					_accessLevel = AccessLevels.getInstance().getAccessLevel(level);
				}
				else
				{
					LOGGER.warn("Tried to set unregistered access level " + level + " to character " + getName() + ". Setting access level without privileges!");
					_accessLevel = AccessLevels.getInstance()._userAccessLevel;
				}
			}
			else
			{
				_accessLevel = accessLevel;
			}
			
			accessLevel = null;
		}
		
		if (_accessLevel != AccessLevels.getInstance()._userAccessLevel)
		{
			// L2EMU_EDIT
			if (getAccessLevel().useNameColor())
			{
				getAppearance().setNameColor(_accessLevel.getNameColor());
			}
			if (getAccessLevel().useTitleColor())
			{
				getAppearance().setTitleColor(_accessLevel.getTitleColor());
			}
			// L2EMU_EDIT
			broadcastUserInfo();
		}
	}
	
	/**
	 * Sets the account accesslevel.
	 * @param level the new account accesslevel
	 */
	public void setAccountAccesslevel(final int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	/**
	 * Return the _accessLevel of the L2PcInstance.<BR>
	 * <BR>
	 * @return the access level
	 */
	public AccessLevel getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return AccessLevels.getInstance()._masterAccessLevel;
		else if (_accessLevel == null)
		{
			setAccessLevel(Config.USERACCESS_LEVEL);
		}
		return _accessLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getLevelMod()
	 */
	@Override
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).<BR>
	 * <BR>
	 * @param broadcastType the broadcast type
	 */
	public void updateAndBroadcastStatus(final int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		if (broadcastType == 1)
		{
			this.sendPacket(new UserInfo(this));
		}
		
		if (broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR>
	 * <BR>
	 * @param flag the new karma flag
	 */
	public void setKarmaFlag(final int flag)
	{
		sendPacket(new UserInfo(this));
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR>
	 * <BR>
	 */
	public void broadcastKarma()
	{
		sendPacket(new UserInfo(this));
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player == null)
				continue;
			
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR>
	 * <BR>
	 * @param isOnline the new online status
	 */
	public void setOnlineStatus(final boolean isOnline)
	{
		if (_isOnline != isOnline)
		{
			_isOnline = isOnline;
		}
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}
	
	/**
	 * Sets the checks if is in7s dungeon.
	 * @param isIn7sDungeon the new checks if is in7s dungeon
	 */
	public void setIsIn7sDungeon(final boolean isIn7sDungeon)
	{
		if (_isIn7sDungeon != isIn7sDungeon)
		{
			_isIn7sDungeon = isIn7sDungeon;
		}
		
		updateIsIn7sDungeonStatus();
	}
	
	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR>
	 * <BR>
	 */
	public void updateOnlineStatus()
	{
		
		if (isInOfflineMode()) // database online status must not change on offline mode
			return;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not set char online status:" + e);
		}
		finally
		{
			CloseUtil.close(con);
			
		}
	}
	
	/**
	 * Update is in7s dungeon status.
	 */
	public void updateIsIn7sDungeonStatus()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isIn7sDungeon() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not set char isIn7sDungeon status:" + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Update first LOGGER.
	 */
	public void updateFirstLog()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET first_log=? WHERE obj_id=?");
			
			int _fl;
			if (getFirstLog())
			{
				_fl = 1;
			}
			else
			{
				_fl = 0;
			}
			statement.setInt(1, _fl);
			statement.setInt(2, getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not set char first login:" + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Create a new player in the characters table of the database.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	private boolean createDb()
	{
		boolean output = false;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO characters " + "(account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp," + "acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd," + "str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex," + "movement_multiplier,attack_speed_multiplier,colRad,colHeight," + "exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime," + "cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace," + "base_class,newbie,nobless,power_grade,last_recom_date"/*
																																																																																																																																									 * ,
																																																																																																																																									 * banchat_time
																																																																																																																																									 * ,
																																																																																																																																									 */+ ",name_color,title_color,aio,aio_end) " + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setInt(20, getRunSpeed());
			statement.setInt(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getAppearance().getFace());
			statement.setInt(29, getAppearance().getHairStyle());
			statement.setInt(30, getAppearance().getHairColor());
			statement.setInt(31, getAppearance().getSex() ? 1 : 0);
			statement.setDouble(32, 1/* getMovementMultiplier() */);
			statement.setDouble(33, 1/* getAttackSpeedMultiplier() */);
			statement.setDouble(34, getTemplate().collisionRadius/* getCollisionRadius() */);
			statement.setDouble(35, getTemplate().collisionHeight/* getCollisionHeight() */);
			statement.setLong(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setLong(45, getDeleteTimer());
			statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAccessLevel().getLevel());
			statement.setInt(49, isOnline());
			statement.setInt(50, isIn7sDungeon() ? 1 : 0);
			statement.setInt(51, getClanPrivileges());
			statement.setInt(52, getWantsPeace());
			statement.setInt(53, getBaseClass());
			statement.setInt(54, isNewbie() ? 1 : 0);
			statement.setInt(55, isNoble() ? 1 : 0);
			statement.setLong(56, 0);
			statement.setLong(57, System.currentTimeMillis());
			
			statement.setString(58, StringToHex(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			statement.setInt(60, isAio() ? 1 : 0);
			statement.setLong(61, 0);
			
			statement.executeUpdate();
			DatabaseUtils.close(statement);
			statement = null;
			
			output = true;
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not insert char data", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
		
		if (output)
		{
			final String text = "Created new character : " + getName() + " for account: " + _accountName;
			Log.add(text, "New_chars");
		}
		
		return output;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database</li> <li>Add the L2PcInstance object in _allObjects</li> <li>Set the x,y,z position of the L2PcInstance and make it invisible</li> <li>Update the overloaded status of the L2PcInstance</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @return The L2PcInstance loaded from the database
	 */
	private static L2PcInstance restore(final int objectId)
	{
		L2PcInstance player = null;
		double curHp = 0;
		double curCp = 0;
		double curMp = 0;
		
		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			final PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex") != 0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
				
				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				
				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setWantsPeace(rset.getInt("wantspeace"));
				
				player.setHeading(rset.getInt("heading"));
				
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie") == 1);
				player.setNoble(rset.getInt("nobless") == 1);
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				player.setFirstLog(rset.getInt("first_log"));
				player.pcBangPoint = rset.getInt("pc_point");
				app = null;
				
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}
				
				final int clanId = rset.getInt("clanid");
				player.setPowerGrade((int) rset.getLong("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));
				player.setLastRecomUpdate(rset.getLong("last_recom_date"));
				// player.setApprentice(rset.getInt("apprentice"));
				
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPowerGrade() == 0)
						{
							player.setPowerGrade(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				
				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());
				
				curHp = rset.getDouble("curHp");
				curCp = rset.getDouble("curCp");
				curMp = rset.getDouble("curMp");
				
				/*
				 * player.setCurrentHp(rset.getDouble("curHp")); player.setCurrentCp(rset.getDouble("curCp")); player.setCurrentMp(rset.getDouble("curMp"));
				 */
				
				// Check recs
				player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));
				
				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					player.setBaseClass(activeClassId);
				}
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (final SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
							{
								player._classIndex = subClass.getClassIndex();
							}
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					LOGGER.warn("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player._activeClass = activeClassId;
				}
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1 ? true : false);
				
				player.setPunishLevel(rset.getInt("punish_level"));
				if (player.getPunishLevel() != PunishLevel.NONE)
					player.setPunishTimer(rset.getLong("punish_timer"));
				else
					player.setPunishTimer(0);
				/*
				 * player.setInJail(rset.getInt("in_jail") == 1 ? true : false); if(player.isInJail()) { player.setJailTimer(rset.getLong("jail_timer")); } else { player.setJailTimer(0); } player.setChatBanTimer(rset.getLong("banchat_time")); player.updateChatBanState();
				 */
				
				try
				{
					player.getAppearance().setNameColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("name_color")).toString()).intValue());
					player.getAppearance().setTitleColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("title_color")).toString()).intValue());
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					// leave them as default
				}
				
				CursedWeaponsManager.getInstance().checkPlayer(player);
				
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.setAio(rset.getInt("aio") == 1 ? true : false);
				player.setAioEndTime(rset.getLong("aio_end"));
				// Add the L2PcInstance object in _allObjects
				// L2World.getInstance().storeObject(player);
				
				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
				{
					final Integer charId = chars.getInt("obj_Id");
					final String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}
				
				chars.close();
				stmt.close();
				chars = null;
				stmt = null;
				
				break;
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			
			if (player == null)
			{
				// TODO: Log this!
				return null;
			}
			
			// Retrieve from the database all secondary data of this L2PcInstance
			// and reward expertise/lucky skills if necessary.
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			player.restoreCharData();
			// reward skill restore mode in order to avoid duplicate storage of already stored skills
			player.rewardSkills(true);
			
			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
			{
				player.getPet().setOwner(player);
			}
			
			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			
			player.restoreFriendList();
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not restore char data", e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		if (player != null)
		{
			player.fireEvent(EventType.LOAD.name, (Object[]) null);
			
			try
			{
				Thread.sleep(100);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			
			// once restored all the skill status, update current CP, MP and HP
			player.setCurrentHpDirect(curHp);
			player.setCurrentCpDirect(curCp);
			player.setCurrentMpDirect(curMp);
			// player.setCurrentCp(curCp);
			// player.setCurrentMp(curMp);
		}
		return player;
	}
	
	/**
	 * Gets the mail.
	 * @return the mail
	 */
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return _forumMail;
	}
	
	/**
	 * Sets the mail.
	 * @param forum the new mail
	 */
	public void setMail(final Forum forum)
	{
		_forumMail = forum;
	}
	
	/**
	 * Gets the memo.
	 * @return the memo
	 */
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		
		return _forumMemo;
	}
	
	/**
	 * Sets the memo.
	 * @param forum the new memo
	 */
	public void setMemo(final Forum forum)
	{
		_forumMemo = forum;
	}
	
	/**
	 * Restores sub-class data for the L2PcInstance, used to check the current class index for the character.
	 * @param player the player
	 * @return true, if successful
	 */
	private static boolean restoreSubClassData(final L2PcInstance player)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));
				
				// Enforce the correct indexing of _subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
			
			DatabaseUtils.close(statement);
			DatabaseUtils.close(rset);
			rset = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not restore classes for " + player.getName() + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		return true;
	}
	
	/**
	 * Restores secondary data for the L2PcInstance, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this L2PcInstance and add them to _macroses.
		_macroses.restore();
		
		// Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
		_shortCuts.restore();
		
		// Retrieve from the database all henna of this L2PcInstance and add them to _henna.
		restoreHenna();
		
		// Retrieve from the database all recom data of this L2PcInstance and add to _recomChars.
		if (Config.ALT_RECOMMEND)
		{
			restoreRecom();
		}
		
		// Retrieve from the database the recipe book of this L2PcInstance.
		if (!isSubClassActive())
		{
			restoreRecipeBook();
		}
	}
	
	/**
	 * Store recipe book data for this L2PcInstance, if not on an active sub-class.
	 */
	private synchronized void storeRecipeBook()
	{
		// If the player is on a sub-class don't even attempt to store a recipe book.
		if (isSubClassActive())
			return;
		
		if (getCommonRecipeBook().length == 0 && getDwarvenRecipeBook().length == 0)
			return;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			L2RecipeList[] recipes = getCommonRecipeBook();
			
			for (final L2RecipeList recipe : recipes)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, recipe.getId());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
			
			recipes = getDwarvenRecipeBook();
			for (final L2RecipeList recipe : recipes)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, recipe.getId());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
			recipes = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not store recipe book data: " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Restore recipe book data for this L2PcInstance.
	 */
	private void restoreRecipeBook()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			L2RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeTable.getInstance().getRecipeList(rset.getInt("id") - 1);
				
				if (rset.getInt("type") == 1)
				{
					registerDwarvenRecipeList(recipe);
				}
				else
				{
					registerCommonRecipeList(recipe);
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			statement = null;
			recipe = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not restore recipe book data:" + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Store.
	 * @param force the force
	 */
	public synchronized void store(final boolean force)
	{
		// update client coords, if these look like true
		if (!force && isInsideRadius(getClientX(), getClientY(), 1000, true))
		{
			setXYZ(getClientX(), getClientY(), getClientZ());
		}
		
		storeCharBase();
		storeCharSub();
		
		// Dont store effect if the char was on Offline trade
		if (!this.isStored())
			storeEffect();
		
		storeRecipeBook();
		fireEvent(EventType.STORE.name, (Object[]) null);
		
		// If char is in Offline trade, setStored must be true
		if (this.isInOfflineMode())
			setStored(true);
		else
			setStored(false);
	}
	
	/**
	 * Update L2PcInstance stats in the characters table of the database.<BR>
	 * <BR>
	 */
	public synchronized void store()
	{
		store(false);
	}
	
	/**
	 * Store char base.
	 */
	private synchronized void storeCharBase()
	{
		Connection con = null;
		
		try
		{
			// Get the exp, level, and sp of base class to store in base table
			final int currentClassIndex = getClassIndex();
			_classIndex = 0;
			final long exp = getStat().getExp();
			final int level = getStat().getLevel();
			final int sp = getStat().getSp();
			_classIndex = currentClassIndex;
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			
			// Update base class
			statement = con.prepareStatement(UPDATE_CHARACTER);
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getAppearance().getFace());
			statement.setInt(15, getAppearance().getHairStyle());
			statement.setInt(16, getAppearance().getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, _observerMode ? _obsX : getX());
			statement.setInt(19, _observerMode ? _obsY : getY());
			statement.setInt(20, _observerMode ? _obsZ : getZ());
			statement.setLong(21, exp);
			statement.setLong(22, getExpBeforeDeath());
			statement.setInt(23, sp);
			statement.setInt(24, getKarma());
			statement.setInt(25, getPvpKills());
			statement.setInt(26, getPkKills());
			statement.setInt(27, getRecomHave());
			statement.setInt(28, getRecomLeft());
			statement.setInt(29, getClanId());
			statement.setInt(30, getMaxLoad());
			statement.setInt(31, getRace().ordinal());
			
			// if (!isSubClassActive())
			
			// else
			// statement.setInt(30, getBaseTemplate().race.ordinal());
			
			statement.setInt(32, getClassId().getId());
			statement.setLong(33, getDeleteTimer());
			statement.setString(34, getTitle());
			statement.setInt(35, getAccessLevel().getLevel());
			
			if (_isInOfflineMode || isOnline() == 1)
			{ // in offline mode or online
				statement.setInt(36, 1);
			}
			else
				statement.setInt(36, isOnline());
			
			// statement.setInt(36, _isOffline ? 0 : isOnline());
			statement.setInt(37, isIn7sDungeon() ? 1 : 0);
			statement.setInt(38, getClanPrivileges());
			statement.setInt(39, getWantsPeace());
			statement.setInt(40, getBaseClass());
			
			long totalOnlineTime = _onlineTime;
			
			if (_onlineBeginTime > 0)
			{
				totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
			}
			
			statement.setLong(41, totalOnlineTime);
			// statement.setInt(42, isInJail() ? 1 : 0);
			// statement.setLong(43, getJailTimer());
			statement.setInt(42, getPunishLevel().value());
			statement.setLong(43, getPunishTimer());
			statement.setInt(44, isNewbie() ? 1 : 0);
			statement.setInt(45, isNoble() ? 1 : 0);
			statement.setLong(46, getPowerGrade());
			statement.setInt(47, getPledgeType());
			statement.setLong(48, getLastRecomUpdate());
			statement.setInt(49, getLvlJoinedAcademy());
			statement.setLong(50, getApprentice());
			statement.setLong(51, getSponsor());
			statement.setInt(52, getAllianceWithVarkaKetra());
			statement.setLong(53, getClanJoinExpiryTime());
			statement.setLong(54, getClanCreateExpiryTime());
			statement.setString(55, getName());
			statement.setLong(56, getDeathPenaltyBuffLevel());
			statement.setInt(57, getPcBangScore());
			
			statement.setString(58, StringToHex(Integer.toHexString(_originalNameColorOffline).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			
			// TODO allow different colors support to players store
			// statement.setString(58, StringToHex(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
			// statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			
			statement.setInt(60, isAio() ? 1 : 0);
			statement.setLong(61, getAioEndTime());
			
			statement.setInt(62, getObjectId());
			
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not store char base data: ");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Store char sub.
	 */
	private synchronized void storeCharSub()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			
			if (getTotalSubClasses() > 0)
			{
				for (final SubClass subClass : getSubClasses().values())
				{
					statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());
					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());
					
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not store sub class data for " + getName() + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	@SuppressWarnings("null")
	private synchronized void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Store all effect data along with calulated remaining
			// reuse delays for matching skills. 'restore_type'= 0.
			final L2Effect[] effects = getAllEffects();
			statement = con.prepareStatement(ADD_SKILL_SAVE);
			
			final List<Integer> storedSkills = new FastList<>();
			
			int buff_index = 0;
			
			for (final L2Effect effect : effects)
			{
				final int skillId = effect.getSkill().getId();
				
				if (storedSkills.contains(skillId))
					continue;
				storedSkills.add(skillId);
				
				if (effect != null && effect.getInUse() && !effect.getSkill().isToggle() && !effect.getStackType().equals("BattleForce") && !effect.getStackType().equals("SpellForce") && effect.getSkill().getSkillType() != SkillType.FORCE_BUFF)
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					if (ReuseTimeStamps.containsKey(effect.getSkill().getReuseHashCode()))
					{
						final TimeStamp t = ReuseTimeStamps.get(effect.getSkill().getReuseHashCode());
						statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
						statement.setLong(7, t.hasNotPassed() ? t.getStamp() : 0);
					}
					else
					{
						statement.setLong(6, 0);
						statement.setLong(7, 0);
					}
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
			// Store the reuse delays of remaining skills which
			// lost effect but still under reuse delay. 'restore_type' 1.
			for (final TimeStamp t : ReuseTimeStamps.values())
			{
				if (t.hasNotPassed())
				{
					final int skillId = t.getSkill().getId();
					final int skillLvl = t.getSkill().getLevel();
					if (storedSkills.contains(skillId))
						continue;
					storedSkills.add(skillId);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, skillLvl);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not store char effect data: ");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is on line.<BR>
	 * <BR>
	 * @return the int
	 */
	public int isOnline()
	{
		return _isOnline ? 1 : 0;
	}
	
	/**
	 * Checks if is in7s dungeon.
	 * @return true, if is in7s dungeon
	 */
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li> <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li> <li>Add Func objects of newSkill to the calculator set of the L2Character</li><BR>
	 * <BR>
	 */
	private boolean _learningSkill = false;
	
	/**
	 * Adds the skill.
	 * @param newSkill the new skill
	 * @param store the store
	 * @return the l2 skill
	 */
	public synchronized L2Skill addSkill(final L2Skill newSkill, final boolean store)
	{
		_learningSkill = true;
		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		final L2Skill oldSkill = super.addSkill(newSkill);
		
		// Add or update a L2PcInstance skill in the character_skills table of the database
		if (store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		
		_learningSkill = false;
		
		return oldSkill;
	}
	
	/**
	 * Checks if is learning skill.
	 * @return true, if is learning skill
	 */
	public boolean isLearningSkill()
	{
		return _learningSkill;
	}
	
	/**
	 * Removes the skill.
	 * @param skill the skill
	 * @param store the store
	 * @return the l2 skill
	 */
	public L2Skill removeSkill(final L2Skill skill, final boolean store)
	{
		if (store)
			return removeSkill(skill);
		return super.removeSkill(skill);
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the skill from the L2Character _skills</li> <li>Remove all its Func objects from the L2Character calculator set</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	@Override
	public L2Skill removeSkill(final L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		final L2Skill oldSkill = super.removeSkill(skill);
		
		Connection con = null;
		
		try
		{
			// Remove or update a L2PcInstance skill from the character_skills table of the database
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			
			if (oldSkill != null)
			{
				statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Error could not delete skill: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		
		for (final L2ShortCut sc : allShortCuts)
		{
			if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		allShortCuts = null;
		
		return oldSkill;
	}
	
	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database. <BR>
	 * <BR>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param newSkill the new skill
	 * @param oldSkill the old skill
	 * @param newClassIndex the new class index
	 */
	private void storeSkill(final L2Skill newSkill, final L2Skill oldSkill, final int newClassIndex)
	{
		int classIndex = _classIndex;
		
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = null;
			
			if (oldSkill != null && newSkill != null)
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				DatabaseUtils.close(statement);
			}
			else if (newSkill != null)
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setString(4, newSkill.getName());
				statement.setInt(5, classIndex);
				statement.execute();
				DatabaseUtils.close(statement);
			}
			else
			{
				LOGGER.warn("could not store new skill. its NULL");
			}
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Error could not store char skills: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * check player skills and remove unlegit ones (excludes hero, noblesse and cursed weapon skills).
	 */
	public void checkAllowedSkills()
	{
		boolean foundskill = false;
		if (!isGM())
		{
			Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(getClassId());
			// loop through all skills of player
			for (final L2Skill skill : getAllSkills())
			{
				final int skillid = skill.getId();
				// int skilllevel = skill.getLevel();
				
				foundskill = false;
				// loop through all skills in players skilltree
				for (final L2SkillLearn temp : skillTree)
				{
					// if the skill was found and the level is possible to obtain for his class everything is ok
					if (temp.getId() == skillid)
					{
						foundskill = true;
					}
				}
				
				// exclude noble skills
				if (isNoble() && skillid >= 325 && skillid <= 397)
				{
					foundskill = true;
				}
				
				if (isNoble() && skillid >= 1323 && skillid <= 1327)
				{
					foundskill = true;
				}
				
				// exclude hero skills
				if (isHero() && skillid >= 395 && skillid <= 396)
				{
					foundskill = true;
				}
				
				if (isHero() && skillid >= 1374 && skillid <= 1376)
				{
					foundskill = true;
				}
				
				// exclude cursed weapon skills
				if (isCursedWeaponEquiped() && skillid == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId).getSkillId())
				{
					foundskill = true;
				}
				
				// exclude clan skills
				if (getClan() != null && skillid >= 370 && skillid <= 391)
				{
					foundskill = true;
				}
				
				// exclude seal of ruler / build siege hq
				if (getClan() != null && (skillid == 246 || skillid == 247))
					if (getClan().getLeaderId() == getObjectId())
					{
						foundskill = true;
					}
				
				// exclude fishing skills and common skills + dwarfen craft
				if (skillid >= 1312 && skillid <= 1322)
				{
					foundskill = true;
				}
				
				if (skillid >= 1368 && skillid <= 1373)
				{
					foundskill = true;
				}
				
				// exclude sa / enchant bonus / penality etc. skills
				if (skillid >= 3000 && skillid < 7000)
				{
					foundskill = true;
				}
				
				// exclude Skills from AllowedSkills in options.properties
				if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
				{
					foundskill = true;
				}
				
				// exclude Donator character
				if (isDonator())
				{
					foundskill = true;
				}
				
				// exclude Aio character
				if (isAio())
				{
					foundskill = true;
				}
				
				// remove skill and do a lil LOGGER message
				if (!foundskill)
				{
					removeSkill(skill);
					
					if (Config.DEBUG)
					{
						// sendMessage("Skill " + skill.getName() + " removed and gm informed!");
						LOGGER.warn("Character " + getName() + " of Account " + getAccountName() + " got skill " + skill.getName() + ".. Removed!"/* + IllegalPlayerAction.PUNISH_KICK */);
						
					}
				}
			}
			
			// Update skill list
			sendSkillList();
			
			skillTree = null;
		}
	}
	
	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to _skills.<BR>
	 * <BR>
	 */
	public synchronized void restoreSkills()
	{
		Connection con = null;
		
		try
		{
			if (!Config.KEEP_SUBCLASS_SKILLS)
			{
				// Retrieve all skills of this L2PcInstance from the database
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);
				statement.setInt(1, getObjectId());
				statement.setInt(2, getClassIndex());
				ResultSet rset = statement.executeQuery();
				
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					final int id = rset.getInt("skill_id");
					final int level = rset.getInt("skill_level");
					
					if (id > 9000)
					{
						continue; // fake skills for base stats
					}
					
					// Create a L2Skill object for each record
					final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
					
					// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
					super.addSkill(skill);
				}
				
				DatabaseUtils.close(rset);
				DatabaseUtils.close(statement);
				rset = null;
				statement = null;
			}
			else
			{
				// Retrieve all skills of this L2PcInstance from the database
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS);
				statement.setInt(1, getObjectId());
				ResultSet rset = statement.executeQuery();
				
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					final int id = rset.getInt("skill_id");
					final int level = rset.getInt("skill_level");
					
					if (id > 9000)
					{
						continue; // fake skills for base stats
					}
					
					// Create a L2Skill object for each record
					final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
					
					// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
					super.addSkill(skill);
				}
				
				DatabaseUtils.close(rset);
				DatabaseUtils.close(statement);
				rset = null;
				statement = null;
			}
			
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Could not restore character skills: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public void restoreEffects()
	{
		restoreEffects(true);
	}
	
	/**
	 * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.<BR>
	 * <BR>
	 * @param activateEffects
	 */
	public void restoreEffects(final boolean activateEffects)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			ResultSet rset;
			
			/**
			 * Restore Type 0 These skill were still in effect on the character upon logout. Some of which were self casted and might still have had a long reuse delay which also is restored.
			 */
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 0);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLvl = rset.getInt("skill_level");
				final int effectCount = rset.getInt("effect_count");
				final int effectCurTime = rset.getInt("effect_cur_time");
				final long reuseDelay = rset.getLong("reuse_delay");
				
				// Just incase the admin minipulated this table incorrectly :x
				if (skillId == -1 || effectCount == -1 || effectCurTime == -1 || reuseDelay < 0)
				{
					continue;
				}
				
				if (activateEffects)
				{
					
					L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
					
					skill.getEffects(this, this, false, false, false);
					skill = null;
					
					for (final L2Effect effect : getAllEffects())
					{
						if (effect.getSkill().getId() == skillId)
						{
							effect.setCount(effectCount);
							effect.setFirstTime(effectCurTime);
						}
					}
				}
				
				if (reuseDelay > 10)
				{
					final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
					
					if (skill == null)
						continue;
					
					disableSkill(skill, reuseDelay);
					addTimeStamp(new TimeStamp(skill, reuseDelay));
				}
				
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			statement = null;
			
			/**
			 * Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
			 */
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 1);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLvl = rset.getInt("skill_level");
				final long reuseDelay = rset.getLong("reuse_delay");
				
				if (reuseDelay <= 0)
				{
					continue;
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				
				if (skill == null)
					continue;
				
				disableSkill(skill, reuseDelay);
				addTimeStamp(new TimeStamp(skill, reuseDelay));
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Could not restore active effect data: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		updateEffectIcons();
	}
	
	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR>
	 * <BR>
	 */
	private void restoreHenna()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			while (rset.next())
			{
				final int slot = rset.getInt("slot");
				
				if (slot < 1 || slot > 3)
				{
					continue;
				}
				
				final int symbol_id = rset.getInt("symbol_id");
				
				L2HennaInstance sym = null;
				
				if (symbol_id != 0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
					
					if (tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot - 1] = sym;
						tpl = null;
						sym = null;
					}
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not restore henna: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}
	
	/**
	 * Retrieve from the database all Recommendation data of this L2PcInstance, add to _recomChars and calculate stats of the L2PcInstance.<BR>
	 * <BR>
	 */
	private void restoreRecom()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_recomChars.add(rset.getInt("target_id"));
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not restore recommendations: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR>
	 * <BR>
	 * @return the henna empty slots
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		
		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
			{
				totalSlots--;
			}
		
		if (totalSlots <= 0)
			return 0;
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR>
	 * <BR>
	 * @param slot the slot
	 * @return true, if successful
	 */
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;
		
		slot--;
		
		if (_henna[slot] == null)
			return false;
		
		L2HennaInstance henna = _henna[slot];
		_henna[slot] = null;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not remove char henna: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));
		
		// Send Server->Client UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getItemIdDye());
		sm.addNumber(henna.getAmountDyeRequire() / 2);
		sendPacket(sm);
		sm = null;
		henna = null;
		
		return true;
	}
	
	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR>
	 * <BR>
	 * @param henna the henna
	 * @return true, if successful
	 */
	public boolean addHenna(final L2HennaInstance henna)
	{
		if (getHennaEmptySlots() == 0)
		{
			sendMessage("You may not have more than three equipped symbols at a time.");
			return false;
		}
		
		// int slot = 0;
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				Connection con = null;
				
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(false);
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					statement.execute();
					DatabaseUtils.close(statement);
					statement = null;
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					LOGGER.warn("could not save char henna: " + e);
				}
				finally
				{
					CloseUtil.close(con);
					con = null;
				}
				
				// Send Server->Client HennaInfo packet to this L2PcInstance
				HennaInfo hi = new HennaInfo(this);
				sendPacket(hi);
				hi = null;
				
				// Send Server->Client UserInfo packet to this L2PcInstance
				UserInfo ui = new UserInfo(this);
				sendPacket(ui);
				ui = null;
				
				getInventory().refreshWeight();
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR>
	 * <BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				continue;
			}
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}
		
		if (_hennaINT > 5)
		{
			_hennaINT = 5;
		}
		
		if (_hennaSTR > 5)
		{
			_hennaSTR = 5;
		}
		
		if (_hennaMEN > 5)
		{
			_hennaMEN = 5;
		}
		
		if (_hennaCON > 5)
		{
			_hennaCON = 5;
		}
		
		if (_hennaWIT > 5)
		{
			_hennaWIT = 5;
		}
		
		if (_hennaDEX > 5)
		{
			_hennaDEX = 5;
		}
	}
	
	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR>
	 * <BR>
	 * @param slot the slot
	 * @return the hennas
	 */
	public L2HennaInstance getHennas(final int slot)
	{
		if (slot < 1 || slot > 3)
			return null;
		
		return _henna[slot - 1];
	}
	
	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat int
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat str
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat con
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat men
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat wit
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR>
	 * <BR>
	 * @return the henna stat dex
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet</li> <li>Check if the attacker is L2MonsterInstance</li> <li>If the attacker is a L2PcInstance, check if it is not in the same party</li> <li>Check if the L2PcInstance has Karma</li> <li>If the attacker is a L2PcInstance, check if it is not
	 * in the same siege clan (Attacker, Defender)</li> <BR>
	 * <BR>
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == getPet())
			return false;
		
		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
			return true;
		
		// Check if the attacker is not in the same party, excluding duels like L2OFF
		if (getParty() != null && getParty().getPartyMembers().contains(attacker) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
			return false;
		
		// Check if the attacker is in olympia and olympia start
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInOlympiadMode())
		{
			if (isInOlympiadMode() && isOlympiadStart() && ((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId())
			{
				if (isFakeDeath())
					return false;
				return true;
			}
			return false;
		}
		
		// Check if the attacker is not in the same clan, excluding duels like L2OFF
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getName()) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
			return false;
		
		// Ally check
		if (attacker instanceof L2PlayableInstance)
		{
			L2PcInstance player = null;
			if (attacker instanceof L2PcInstance)
			{
				player = (L2PcInstance) attacker;
			}
			else if (attacker instanceof L2Summon)
			{
				player = ((L2Summon) attacker).getOwner();
			}
			
			// Check if the attacker is not in the same ally, excluding duels like L2OFF
			if (player != null && getAllyId() != 0 && player.getAllyId() != 0 && getAllyId() == player.getAllyId() && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == player.getDuelId()))
				return false;
		}
		
		if (attacker instanceof L2PlayableInstance && isInFunEvent())
		{
			
			L2PcInstance player = null;
			if (attacker instanceof L2PcInstance)
			{
				player = (L2PcInstance) attacker;
			}
			else if (attacker instanceof L2Summon)
			{
				player = ((L2Summon) attacker).getOwner();
			}
			
			if (player != null)
			{
				
				if (player.isInFunEvent())
				{
					
					// checks for events
					if ((_inEventTvT && player._inEventTvT && TvT.is_started() && !_teamNameTvT.equals(player._teamNameTvT)) || (_inEventCTF && player._inEventCTF && CTF.is_started() && !_teamNameCTF.equals(player._teamNameCTF)) || (_inEventDM && player._inEventDM && DM.is_started()) || (_inEventVIP && player._inEventVIP && VIP._started))
					{
						return true;
					}
					return false;
				}
				return false;
			}
		}
		
		if (L2Character.isInsidePeaceZone(attacker, this))
		{
			return false;
		}
		
		// Check if the L2PcInstance has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;
		
		// Check if the attacker is a L2PcInstance
		if (attacker instanceof L2PcInstance)
		{
			// is AutoAttackable if both players are in the same duel and the duel is still going on
			if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId())
				return true;
			// Check if the L2PcInstance is in an arena or a siege area
			if (isInsideZone(ZONE_PVP) && ((L2PcInstance) attacker).isInsideZone(ZONE_PVP))
				return true;
			
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(((L2PcInstance) attacker).getClan()) && siege.checkIsDefender(getClan()))
					{
						siege = null;
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && siege.checkIsAttacker(getClan()))
					{
						siege = null;
						return false;
					}
				}
				if (fortsiege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (fortsiege.checkIsDefender(((L2PcInstance) attacker).getClan()) && fortsiege.checkIsDefender(getClan()))
					{
						fortsiege = null;
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (fortsiege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && fortsiege.checkIsAttacker(getClan()))
					{
						fortsiege = null;
						return false;
					}
				}
				
				// Check if clan is at war
				if (getClan() != null && ((L2PcInstance) attacker).getClan() != null && getClan().isAtWarWith(((L2PcInstance) attacker).getClanId()) && getWantsPeace() == 0 && ((L2PcInstance) attacker).getWantsPeace() == 0 && !isAcademyMember())
					return true;
			}
			
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (getClan() != null)
			{
				final Siege siege = SiegeManager.getInstance().getSiege(this);
				return siege != null && siege.checkIsAttacker(getClan()) || DevastatedCastle.getInstance().getIsInProgress();
			}
		}
		else if (attacker instanceof L2FortSiegeGuardInstance)
		{
			if (getClan() != null)
			{
				final FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(this);
				return fortsiege != null && fortsiege.checkIsAttacker(getClan());
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the skill isn't toggle and is offensive</li> <li>Check if the target is in the skill cast range</li> <li>Check if the skill is Spoil type and if the target isn't already spoiled</li> <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill</li> <li>
	 * Check if the caster isn't sitting</li> <li>Check if all skills are enabled and this skill is enabled</li><BR>
	 * <BR>
	 * <li>Check if the caster own the weapon needed</li><BR>
	 * <BR>
	 * <li>Check if the skill is active</li><BR>
	 * <BR>
	 * <li>Check if all casting conditions are completed</li><BR>
	 * <BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	public void useMagic(final L2Skill skill, final boolean forceUse, final boolean dontMove)
	{
		if (isDead())
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (skill == null)
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int skill_id = skill.getId();
		int curr_skill_id = -1;
		SkillDat current = null;
		if ((current = getCurrentSkill()) != null)
		{
			curr_skill_id = current.getSkillId();
		}
		
		/*
		 * if (isWearingFormalWear() && !skill.isPotion()) { sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR)); sendPacket(ActionFailed.STATIC_PACKET); abortCast(); return; }
		 */
		if (inObserverMode())
		{
			sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster is sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Like L2OFF you can't use fake death if you are mounted
			if (skill.getId() == 60 && isMounted())
				return;
			
			// Get effects of the skill
			final L2Effect effect = getFirstEffect(skill);
			
			// Like L2OFF toogle skills have little delay
			if (TOGGLE_USE != 0 && TOGGLE_USE + 400 > System.currentTimeMillis())
			{
				TOGGLE_USE = 0;
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			TOGGLE_USE = System.currentTimeMillis();
			
			if (effect != null)
			{
				// fake death exception
				if (skill.getId() != 60)
					effect.exit(false);
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if skill is in reause time
		if (isSkillDisabled(skill))
		{
			if (!(skill.getId() == 2166))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(), skill.getLevel());
				sendPacket(sm);
				sm = null;
			}
			// Cp potion message like L2OFF
			else if ((skill.getId() == 2166))
			{
				if (skill.getLevel() == 2)
					sendMessage("Greater CP Potion is not available at this time: being prepared for reuse.");
				else if (skill.getLevel() == 1)
					sendMessage("CP Potion is not available at this time: being prepared for reuse.");
			}
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if it's ok to summon
		// siege golem (13), Wild Hog Cannon (299), Swoop Cannon (448)
		if ((skill_id == 13 || skill_id == 299 || skill_id == 448) && !SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
			return;
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used, queue this one if this is not the same
		// Note that this check is currently imperfect: getCurrentSkill() isn't always null when a skill has
		// failed to cast, or the casting is not yet in progress when this is rechecked
		if (curr_skill_id != -1 && (isCastingNow() || isCastingPotionNow()))
		{
			final SkillDat currentSkill = getCurrentSkill();
			// Check if new skill different from current skill in progress
			if (currentSkill != null && skill.getId() == currentSkill.getSkillId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (Config.DEBUG && getQueuedSkill() != null)
			{
				LOGGER.info(getQueuedSkill().getSkill().getName() + " is already queued for " + getName() + ".");
			}
			
			// Create a new SkillDat object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Create a new SkillDat object and set the player _currentSkill
		// This is used mainly to save & queue the button presses, since L2Character has
		// _lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);
		
		if (getQueuedSkill() != null) // wiping out previous values, after casting has been aborted
			setQueuedSkill(null, false, false);
		
		// triggered skills cannot be used directly
		if (_triggeredSkills.size() > 0)
		{
			
			if (Config.DEBUG)
			{
				LOGGER.info("Checking if Triggherable Skill: " + skill.getId());
				LOGGER.info("Saved Triggherable Skills");
				
				for (final Integer skillId : _triggeredSkills.keySet())
				{
					LOGGER.info(skillId);
				}
				
			}
			
			if (_triggeredSkills.get(skill.getId()) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// ************************************* Check Target *******************************************
		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		final SkillTargetType sklTargetType = skill.getTargetType();
		final SkillType sklType = skill.getSkillType();
		
		switch (sklTargetType)
		{
		// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
				if (isInOlympiadMode() && !isOlympiadStart())
					setTarget(this);
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			case TARGET_PET:
				target = getPet();
				break;
			default:
				target = getTarget();
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// skills can be used on Walls and Doors only durring siege
		// Ignore skill UNLOCK
		if (skill.isOffensive() && target instanceof L2DoorInstance)
		{
			final boolean isCastle = (((L2DoorInstance) target).getCastle() != null && ((L2DoorInstance) target).getCastle().getCastleId() > 0 && ((L2DoorInstance) target).getCastle().getSiege().getIsInProgress());
			final boolean isFort = (((L2DoorInstance) target).getFort() != null && ((L2DoorInstance) target).getFort().getFortId() > 0 && ((L2DoorInstance) target).getFort().getSiege().getIsInProgress());
			if ((!isCastle && !isFort))
				return;
		}
		
		// Like L2OFF you can't heal random purple people without using CTRL
		final SkillDat skilldat = getCurrentSkill();
		if (skilldat != null && skill.getSkillType() == SkillType.HEAL && !skilldat.isCtrlPressed() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 1 && this != target)
		{
			if ((getClanId() == 0 || ((L2PcInstance) target).getClanId() == 0) || (getClanId() != ((L2PcInstance) target).getClanId()))
			{
				if ((getAllyId() == 0 || ((L2PcInstance) target).getAllyId() == 0) || (getAllyId() != ((L2PcInstance) target).getAllyId()))
				{
					if ((getParty() == null || ((L2PcInstance) target).getParty() == null) || (!getParty().equals(((L2PcInstance) target).getParty())))
					{
						sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel())
		{
			if (!(target instanceof L2PcInstance && ((L2PcInstance) target).getDuelId() == getDuelId()) && !(target instanceof L2SummonInstance && ((L2Summon) target).getOwner().getDuelId() == getDuelId()))
			{
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Pk protection config
		if (skill.isOffensive() && !isGM() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 0 && ((L2PcInstance) target).getKarma() == 0 && (getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL || ((L2PcInstance) target).getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL))
		{
			sendMessage("You can't hit a player that is lower level from you. Target's level: " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		// if(isSkillDisabled(skill_id) /* && !getAccessLevel().allowPeaceAttack() */)
		// {
		// SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE);
		// sm.addString(skill.getName());
		// sendPacket(sm);
		//
		// Send a Server->Client packet ActionFailed to the L2PcInstance
		// sendPacket(ActionFailed.STATIC_PACKET);
		// return;
		// }
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && !getAccessLevel().allowPeaceAttack())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// prevent casting signets to peace zone
		if (skill.getSkillType() == SkillType.SIGNET || skill.getSkillType() == SkillType.SIGNET_CASTTIME)
		{
			if (isInsidePeaceZone(this))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill_id);
				sendPacket(sm);
				return;
			}
		}
		// ************************************* Check Consumables *******************************************
		
		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the spell consummes an Item
		if (skill.getItemConsume() > 0)
		{
			// Get the L2ItemInstance consummed by the spell
			final L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enought consummed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (sklType == L2Skill.SkillType.SUMMON)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				}
				return;
			}
		}
		
		// Like L2OFF if you are mounted on wyvern you can't use own skills
		if (isFlying())
		{
			if (skill_id != 327 && skill_id != 4289 && !skill.isPotion())
			{
				sendMessage("You cannot use skills while riding a wyvern.");
				return;
			}
		}
		
		// Like L2OFF if you have a summon you can't summon another one (ignore cubics)
		if (sklType == L2Skill.SkillType.SUMMON && skill instanceof L2SkillSummon && !((L2SkillSummon) skill).isCubic())
		{
			if (getPet() != null || isMounted())
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
				return;
			}
		}
		
		if (skill.getNumCharges() > 0 && skill.getSkillType() != SkillType.CHARGE && skill.getSkillType() != SkillType.CHARGEDAM && skill.getSkillType() != SkillType.CHARGE_EFFECT && skill.getSkillType() != SkillType.PDAM)
		{
			final EffectCharge effect = (EffectCharge) getFirstEffect(L2Effect.EffectType.CHARGE);
			if (effect == null || effect.numCharges < skill.getNumCharges())
			{
				sendPacket(new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE));
				return;
			}
			
			effect.numCharges -= skill.getNumCharges();
			sendPacket(new EtcStatusUpdate(this));
			
			if (effect.numCharges == 0)
			{
				effect.exit(false);
			}
		}
		// ************************************* Check Casting Conditions *******************************************
		
		// Check if the caster own the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Player State *******************************************
		
		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
		
		// Check if the player use "Fake Death" skill
		if (isAlikeDead() && !skill.isPotion() && skill.getSkillType() != L2Skill.SkillType.FAKE_DEATH)
		{
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isFishing() && sklType != SkillType.PUMPING && sklType != SkillType.REELING && sklType != SkillType.FISHING)
		{
			// Only fishing skills are available
			sendPacket(new SystemMessage(SystemMessageId.ONLY_FISHING_SKILLS_NOW));
			return;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			final Boolean peace = isInsidePeaceZone(this, target);
			
			if (peace && (skill.getId() != 3261 // Like L2OFF you can use cupid bow skills on peace zone
				&& skill.getId() != 3260 && skill.getId() != 3262 && sklTargetType != SkillTargetType.TARGET_AURA)) // Like L2OFF people can use TARGET_AURE skills on peace zone
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (isInOlympiadMode() && !isOlympiadStart() && sklTargetType != SkillTargetType.TARGET_AURA)
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!(target instanceof L2MonsterInstance) && sklType == SkillType.CONFUSE_MOB_ONLY)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			/*
			 * // Check if the target is attackable if(target instanceof L2PcInstance && !target.isAttackable() && !getAccessLevel().allowPeaceAttack() && (!(_inEventTvT && TvT.is_started()) || !(_inEventCTF && CTF.is_started()) || !(_inEventDM && DM.is_started()) || !(_inEventVIP && VIP._started)))
			 * { if(!isInFunEvent() || !((L2PcInstance)target).isInFunEvent()) { // If target is not attackable, send a Server->Client packet ActionFailed sendPacket(ActionFailed.STATIC_PACKET); return; } }
			 */
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			// if (!target.isAutoAttackable(this) && !forceUse && !(_inEventTvT && TvT._started) && !(_inEventDM && DM._started) && !(_inEventCTF && CTF._started) && !(_inEventVIP && VIP._started)
			if (!target.isAutoAttackable(this) && (!forceUse && (skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262)) && !(_inEventTvT && TvT.is_started()) && !(_inEventDM && DM.is_started()) && !(_inEventCTF && CTF.is_started()) && !(_inEventVIP && VIP._started) && sklTargetType != SkillTargetType.TARGET_AURA && sklTargetType != SkillTargetType.TARGET_CLAN && sklTargetType != SkillTargetType.TARGET_ALLY && sklTargetType != SkillTargetType.TARGET_PARTY && sklTargetType != SkillTargetType.TARGET_SELF && sklTargetType != SkillTargetType.TARGET_GROUND)
			
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				if (sklTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), (int) (skill.getCastRange() + getTemplate().getCollisionRadius()), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						
						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().collisionRadius, false, false)) // Calculate the distance between the L2PcInstance and the target
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (sklType == SkillType.SIGNET) // Check range for SIGNET skills
			{
				if (!isInsideRadius(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), (int) (skill.getCastRange() + getTemplate().getCollisionRadius()), false, false))
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		// Check if the skill is defensive
		if (!skill.isOffensive())
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			if (target instanceof L2MonsterInstance && !forceUse && sklTargetType != SkillTargetType.TARGET_PET && sklTargetType != SkillTargetType.TARGET_AURA && sklTargetType != SkillTargetType.TARGET_CLAN && sklTargetType != SkillTargetType.TARGET_SELF && sklTargetType != SkillTargetType.TARGET_PARTY && sklTargetType != SkillTargetType.TARGET_ALLY && sklTargetType != SkillTargetType.TARGET_CORPSE_MOB && sklTargetType != SkillTargetType.TARGET_AREA_CORPSE_MOB && sklTargetType != SkillTargetType.TARGET_GROUND && sklType != SkillType.BEAST_FEED && sklType != SkillType.DELUXE_KEY_UNLOCK && sklType != SkillType.UNLOCK)
			{
				// send the action failed so that the skill doens't go off.
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (sklType == SkillType.SPOIL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is Sweep type and if conditions not apply
		if (sklType == SkillType.SWEEP && target instanceof L2Attackable)
		{
			final int spoilerId = ((L2Attackable) target).getIsSpoiledBy();
			
			if (((L2Attackable) target).isDead())
			{
				if (!((L2Attackable) target).isSpoil())
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (getObjectId() != spoilerId && !isInLooterParty(spoilerId))
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.SWEEP_NOT_ALLOWED));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (sklType == SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		final Point3D worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			LOGGER.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SELF:
			case TARGET_GROUND:
				break;
			default:
				// if pvp skill is not allowed for given target
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack() && (skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262))
				{
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
		}
		
		if (sklTargetType == SkillTargetType.TARGET_HOLY && !TakeCastle.checkIfOkToCastSealOfRule(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		if (sklType == SkillType.SIEGEFLAG && !SiegeFlag.checkIfOkToPlaceFlag(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		else if (sklType == SkillType.STRSIEGEASSAULT && !StrSiegeAssault.checkIfOkToUseStriderSiegeAssault(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		/*
		 * TEMPFIX: Check client Z coordinate instead of server z to avoid exploit killing Zaken from others floor
		 */
		if ((target instanceof L2GrandBossInstance) && ((L2GrandBossInstance) target).getNpcId() == 29022)
		{
			if (Math.abs(this.getClientZ() - target.getZ()) > 200)
			{
				sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// GeoData Los Check here
		if (skill.getCastRange() > 0 && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If all conditions are checked, create a new SkillDat object and set the player _currentSkill
		setCurrentSkill(skill, forceUse, dontMove);
		
		// Check if the active L2Skill can be casted (ex : not sleeping...), Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		super.useMagic(skill);
	}
	
	/**
	 * Checks if is in looter party.
	 * @param LooterId the looter id
	 * @return true, if is in looter party
	 */
	public boolean isInLooterParty(final int LooterId)
	{
		final L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
		
		// if L2PcInstance is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
			return getParty().getCommandChannel().getMembers().contains(looter);
		
		if (isInParty() && looter != null)
			return getParty().getPartyMembers().contains(looter);
		
		return false;
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param target L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(final L2Object target, final L2Skill skill)
	{
		return checkPvpSkill(target, skill, false);
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param target L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @param srcIsSummon is L2Summon - caster?
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, final L2Skill skill, final boolean srcIsSummon)
	{
		// Check if player and target are in events and on the same team.
		if (target instanceof L2PcInstance)
		{
			if (skill.isOffensive() && (_inEventTvT && ((L2PcInstance) target)._inEventTvT && TvT.is_started() && !_teamNameTvT.equals(((L2PcInstance) target)._teamNameTvT)) || (_inEventCTF && ((L2PcInstance) target)._inEventCTF && CTF.is_started() && !_teamNameCTF.equals(((L2PcInstance) target)._teamNameCTF)) || (_inEventDM && ((L2PcInstance) target)._inEventDM && DM.is_started()) || (_inEventVIP && ((L2PcInstance) target)._inEventVIP && VIP._started))
			{
				return true;
			}
			else if (isInFunEvent() && skill.isOffensive()) // same team return false
			{
				return false;
			}
		}
		
		// check for PC->PC Pvp status
		if (target instanceof L2Summon)
			target = ((L2Summon) target).getOwner();
		
		if (target != null && // target not null and
		target != this && // target is not self and
		target instanceof L2PcInstance && // target is L2PcInstance and
		!(isInDuel() && ((L2PcInstance) target).getDuelId() == getDuelId()) && // self is not in a duel and attacking opponent
		!isInsideZone(ZONE_PVP) && // Pc is not in PvP zone
		!((L2PcInstance) target).isInsideZone(ZONE_PVP) // target is not in PvP zone
		)
		{
			final SkillDat skilldat = getCurrentSkill();
			// SkillDat skilldatpet = getCurrentPetSkill();
			if (skill.isPvpSkill()) // pvp skill
			{
				if (getClan() != null && ((L2PcInstance) target).getClan() != null)
				{
					if (getClan().isAtWarWith(((L2PcInstance) target).getClan().getClanId()) && ((L2PcInstance) target).getClan().isAtWarWith(getClan().getClanId()))
						return true; // in clan war player can attack whites even with sleep etc.
				}
				if (((L2PcInstance) target).getPvpFlag() == 0 && // target's pvp flag is not set and
				((L2PcInstance) target).getKarma() == 0 // target has no karma
				)
					return false;
			}
			else if ((skilldat != null && !skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon)
			/* || (skilldatpet != null && !skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon) */)
			{
				if (getClan() != null && ((L2PcInstance) target).getClan() != null)
				{
					if (getClan().isAtWarWith(((L2PcInstance) target).getClan().getClanId()) && ((L2PcInstance) target).getClan().isAtWarWith(getClan().getClanId()))
						return true; // in clan war player can attack whites even without ctrl
				}
				if (((L2PcInstance) target).getPvpFlag() == 0 && // target's pvp flag is not set and
				((L2PcInstance) target).getKarma() == 0 // target has no karma
				)
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Reduce Item quantity of the L2PcInstance Inventory and send it a Server->Client packet InventoryUpdate.<BR>
	 * <BR>
	 * @param itemConsumeId the item consume id
	 * @param itemCount the item count
	 */
	@Override
	public void consumeItem(final int itemConsumeId, final int itemCount)
	{
		if (itemConsumeId != 0 && itemCount != 0)
		{
			destroyItemByItemId("Consume", itemConsumeId, itemCount, null, true);
		}
	}
	
	/**
	 * Return True if the L2PcInstance is a Mage.<BR>
	 * <BR>
	 * @return true, if is mage class
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	/**
	 * Checks if is mounted.
	 * @return true, if is mounted
	 */
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean checkLandingState()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(ZONE_NOLANDING))
			return true;
		else
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if (isInsideZone(ZONE_SIEGE) && !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan().getLeader().getPlayerInstance()))
			return true;
		
		return false;
	}
	
	// returns false if the change of mount type fails.
	/**
	 * Sets the mount type.
	 * @param mountType the mount type
	 * @return true, if successful
	 */
	public boolean setMountType(final int mountType)
	{
		if (checkLandingState() && mountType == 2)
			return false;
		
		switch (mountType)
		{
			case 0:
				setIsFlying(false);
				setIsRiding(false);
				break; // Dismounted
			case 1:
				setIsRiding(true);
				if (isNoble())
				{
					final L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
					addSkill(striderAssaultSkill, false); // not saved to DB
				}
				break;
			case 2:
				setIsFlying(true);
				break; // Flying Wyvern
		}
		
		_mountType = mountType;
		
		// Send a Server->Client packet InventoryUpdate to the L2PcInstance in order to update speed
		UserInfo ui = new UserInfo(this);
		sendPacket(ui);
		ui = null;
		return true;
	}
	
	/**
	 * Return the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern).<BR>
	 * <BR>
	 * @return the mount type
	 */
	public int getMountType()
	{
		return _mountType;
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>. In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li> <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR>
	 * <BR>
	 */
	public void tempInvetoryDisable()
	{
		_inventoryDisable = true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}
	
	/**
	 * Return True if the Inventory is disabled.<BR>
	 * <BR>
	 * @return true, if is invetory disabled
	 */
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}
	
	/**
	 * The Class InventoryEnable.
	 */
	class InventoryEnable implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			_inventoryDisable = false;
		}
	}
	
	/**
	 * Gets the cubics.
	 * @return the cubics
	 */
	public Map<Integer, L2CubicInstance> getCubics()
	{
		synchronized (_cubics)
		{
			// clean cubics instances
			final Set<Integer> cubicsIds = _cubics.keySet();
			
			for (final Integer id : cubicsIds)
			{
				if (id == null || _cubics.get(id) == null)
					
					try
					{
						_cubics.remove(id);
					}
					catch (final NullPointerException e)
					{
						// FIXME: tried to remove a null key, to be found where this action has been performed (DEGUB)
					}
			}
			
			return _cubics;
		}
	}
	
	/**
	 * Add a L2CubicInstance to the L2PcInstance _cubics.<BR>
	 * <BR>
	 * @param id the id
	 * @param level the level
	 * @param matk the matk
	 * @param activationtime the activationtime
	 * @param activationchance the activationchance
	 * @param totalLifetime the total lifetime
	 * @param givenByOther the given by other
	 */
	/*
	 * public void addCubic(int id, int level, double d) { L2CubicInstance cubic = new L2CubicInstance(this, id, level,d); _cubics.put(id, cubic); cubic = null; }
	 */
	
	public void addCubic(final int id, final int level, final double matk, final int activationtime, final int activationchance, final int totalLifetime, final boolean givenByOther)
	{
		if (Config.DEBUG)
			LOGGER.info("L2PcInstance(" + getName() + "): addCubic(" + id + "|" + level + "|" + matk + ")");
		final L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther);
		
		synchronized (_cubics)
		{
			_cubics.put(id, cubic);
		}
		
	}
	
	/**
	 * Remove a L2CubicInstance from the L2PcInstance _cubics.<BR>
	 * <BR>
	 * @param id the id
	 */
	public void delCubic(final int id)
	{
		synchronized (_cubics)
		{
			_cubics.remove(id);
		}
		
	}
	
	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.<BR>
	 * <BR>
	 * @param id the id
	 * @return the cubic
	 */
	public L2CubicInstance getCubic(final int id)
	{
		synchronized (_cubics)
		{
			return _cubics.get(id);
		}
		
	}
	
	public void unsummonAllCubics()
	{
		
		// Unsummon Cubics
		synchronized (_cubics)
		{
			
			if (_cubics.size() > 0)
			{
				for (final L2CubicInstance cubic : _cubics.values())
				{
					cubic.stopAction();
					cubic.cancelDisappear();
				}
				
				_cubics.clear();
			}
			
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#toString()
	 */
	@Override
	public String toString()
	{
		return "player " + getName();
	}
	
	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR>
	 * <BR>
	 * @return the enchant effect
	 */
	public int getEnchantEffect()
	{
		final L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null)
			return 0;
		
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR>
	 * <BR>
	 * @param folkNpc the new last folk npc
	 */
	public void setLastFolkNPC(final L2FolkInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	/**
	 * Return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR>
	 * <BR>
	 * @return the last folk npc
	 */
	public L2FolkInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	/**
	 * Set the Silent Moving mode Flag.<BR>
	 * <BR>
	 * @param flag the new silent moving
	 */
	public void setSilentMoving(final boolean flag)
	{
		if (flag)
			_isSilentMoving++;
		else
			_isSilentMoving--;
	}
	
	/**
	 * Return True if the Silent Moving mode is active.<BR>
	 * <BR>
	 * @return true, if is silent moving
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving > 0;
	}
	
	/**
	 * Return True if L2PcInstance is a participant in the Festival of Darkness.<BR>
	 * <BR>
	 * @return true, if is festival participant
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isPlayerParticipant(this);
	}
	
	/**
	 * Adds the auto soul shot.
	 * @param itemId the item id
	 */
	public void addAutoSoulShot(final int itemId)
	{
		_activeSoulShots.put(itemId, itemId);
	}
	
	/**
	 * Removes the auto soul shot.
	 * @param itemId the item id
	 */
	public void removeAutoSoulShot(final int itemId)
	{
		_activeSoulShots.remove(itemId);
	}
	
	/**
	 * Gets the auto soul shot.
	 * @return the auto soul shot
	 */
	public Map<Integer, Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	/**
	 * Recharge auto soul shot.
	 * @param physical the physical
	 * @param magic the magic
	 * @param summon the summon
	 */
	public void rechargeAutoSoulShot(final boolean physical, final boolean magic, final boolean summon)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if (_activeSoulShots == null || _activeSoulShots.size() == 0)
			return;
		
		for (final int itemId : _activeSoulShots.values())
		{
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (!summon)
					{
						if (itemId == 2509 || itemId == 2510 || itemId == 2511 || itemId == 2512 || itemId == 2513 || itemId == 2514 || itemId == 3947 || itemId == 3948 || itemId == 3949 || itemId == 3950 || itemId == 3951 || itemId == 3952 || itemId == 5790)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6646 || itemId == 6647)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
				
				if (physical)
				{
					if (!summon)
					{
						if (itemId == 1463 || itemId == 1464 || itemId == 1465 || itemId == 1466 || itemId == 1467 || itemId == 1835 || itemId == 5789)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6645)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
		item = null;
		handler = null;
	}
	
	/**
	 * Recharge auto soul shot.
	 * @param physical the physical
	 * @param magic the magic
	 * @param summon the summon
	 * @param atkTime TODO
	 */
	public void rechargeAutoSoulShot(final boolean physical, final boolean magic, final boolean summon, final int atkTime)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				rechargeAutoSoulShot(physical, magic, summon);
			}
		}, atkTime);
	}
	
	/** The _task warn user take break. */
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	
	/**
	 * The Class WarnUserTakeBreak.
	 */
	class WarnUserTakeBreak implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			if (isOnline() == 1)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.PLAYING_FOR_LONG_TIME);
				L2PcInstance.this.sendPacket(msg);
				msg = null;
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}
	
	/** The _task bot checker. */
	private ScheduledFuture<?> _taskBotChecker;
	
	/** The _task kick bot. */
	protected ScheduledFuture<?> _taskKickBot;
	
	/**
	 * The Class botChecker.
	 */
	class botChecker implements Runnable
	{
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			/* Start bot checker if player is in combat online without shop and in a zone not peacefull */
			if (!isGM() && isOnline() == 1 && isInCombat() && getPrivateStoreType() == 0 && !isInsideZone(L2Character.ZONE_PEACE))
			{
				try
				{
					String text = HtmCache.getInstance().getHtm("data/html/custom/bot.htm");
					final String word = Config.QUESTION_LIST.get(Rnd.get(Config.QUESTION_LIST.size()));
					String output;
					_correctWord = Rnd.get(5) + 1;
					
					text = text.replace("%Time%", Integer.toString(Config.BOT_PROTECTOR_WAIT_ANSVER));
					for (int i = 1; i <= 5; i++)
					{
						if (i != _correctWord)
						{
							output = RandomStringUtils.random(word.length(), word);
						}
						else
						{
							output = word;
						}
						
						text = text.replace("%Word" + i + "%", output);
						if (i == _correctWord)
						{
							text = text.replace("%Word%", output);
						}
						
					}
					
					L2PcInstance.this.sendPacket(new TutorialShowHtml(text));
					
					if (_taskKickBot == null)
					{
						_stopKickBotTask = false;
						_taskKickBot = ThreadPoolManager.getInstance().scheduleGeneral(new kickBot(), 10);
					}
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				stopBotChecker();
			}
		}
	}
	
	/**
	 * The Class kickBot.
	 */
	class kickBot implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void run()
		{
			if (isOnline() == 1 && getPrivateStoreType() == 0 && !isGM())
			{
				
				for (int i = Config.BOT_PROTECTOR_WAIT_ANSVER; i >= 10; i -= 10)
				{
					if (_stopKickBotTask)
					{
						if (_taskKickBot != null)
						{
							_taskKickBot = null;
						}
						_stopKickBotTask = false;
						return;
					}
					
					L2PcInstance.this.sendMessage("You have " + i + " seconds to choose the answer.");
					
					try
					{
						Thread.sleep(10000);
					}
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				if (_stopKickBotTask)
				{
					if (_taskKickBot != null)
					{
						_taskKickBot = null;
					}
					_stopKickBotTask = false;
					return;
				}
				LOGGER.warn("Player " + L2PcInstance.this.getName() + " kicked from game, no/wrong answer on ANTI BOT!");
				L2PcInstance.this.closeNetConnection();
			}
			else
			{
				if (_taskKickBot != null)
				{
					_taskKickBot = null;
				}
				_stopKickBotTask = false;
			}
		}
	}
	
	/**
	 * The Class RentPetTask.
	 */
	class RentPetTask implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			stopRentPet();
		}
	}
	
	/** The _taskforfish. */
	public ScheduledFuture<?> _taskforfish;
	
	/**
	 * The Class WaterTask.
	 */
	class WaterTask implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;
			
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			reduceCurrentHp(reduceHp, L2PcInstance.this, false);
			// reduced hp, becouse not rest
			SystemMessage sm = new SystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
			sm.addNumber((int) reduceHp);
			sendPacket(sm);
			sm = null;
		}
	}
	
	/**
	 * The Class LookingForFishTask.
	 */
	class LookingForFishTask implements Runnable
	{
		
		/** The _is upper grade. */
		boolean _isNoob, _isUpperGrade;
		
		/** The _guts check time. */
		int _fishType, _fishGutsCheck, _gutsCheckTime;
		
		/** The _end task time. */
		long _endTaskTime;
		
		/**
		 * Instantiates a new looking for fish task.
		 * @param fishWaitTime the fish wait time
		 * @param fishGutsCheck the fish guts check
		 * @param fishType the fish type
		 * @param isNoob the is noob
		 * @param isUpperGrade the is upper grade
		 */
		protected LookingForFishTask(final int fishWaitTime, final int fishGutsCheck, final int fishType, final boolean isNoob, final boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				EndFishing(false);
				return;
			}
			if (_fishType == -1)
				return;
			final int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				StartFishCombat(_isNoob, _isUpperGrade);
			}
		}
		
	}
	
	/**
	 * Gets the clan privileges.
	 * @return the clan privileges
	 */
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	/**
	 * Sets the clan privileges.
	 * @param n the new clan privileges
	 */
	public void setClanPrivileges(final int n)
	{
		_clanPrivileges = n;
	}
	
	// baron etc
	/**
	 * Sets the pledge class.
	 * @param classId the new pledge class
	 */
	public void setPledgeClass(final int classId)
	{
		_pledgeClass = classId;
	}
	
	/**
	 * Gets the pledge class.
	 * @return the pledge class
	 */
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	/**
	 * Sets the pledge type.
	 * @param typeId the new pledge type
	 */
	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}
	
	/**
	 * Gets the pledge type.
	 * @return the pledge type
	 */
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	/**
	 * Gets the apprentice.
	 * @return the apprentice
	 */
	public int getApprentice()
	{
		return _apprentice;
	}
	
	/**
	 * Sets the apprentice.
	 * @param apprentice_id the new apprentice
	 */
	public void setApprentice(final int apprentice_id)
	{
		_apprentice = apprentice_id;
	}
	
	/**
	 * Gets the sponsor.
	 * @return the sponsor
	 */
	public int getSponsor()
	{
		return _sponsor;
	}
	
	/**
	 * Sets the sponsor.
	 * @param sponsor_id the new sponsor
	 */
	public void setSponsor(final int sponsor_id)
	{
		_sponsor = sponsor_id;
	}
	
	/**
	 * Send message.
	 * @param message the message
	 */
	public void sendMessage(final String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	/** The _was invisible. */
	private boolean _wasInvisible = false;
	
	/**
	 * Enter observer mode.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void enterObserverMode(final int x, final int y, final int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();
		
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
			getPet().unSummon(this);
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		_observerMode = true;
		setTarget(null);
		stopMove(null);
		setIsParalyzed(true);
		setIsInvul(true);
		
		_wasInvisible = getAppearance().getInvisible();
		getAppearance().setInvisible();
		
		sendPacket(new ObservationMode(x, y, z));
		getKnownList().removeAllKnownObjects(); // reinit knownlist
		setXYZ(x, y, z);
		teleToLocation(x, y, z, false);
		broadcastUserInfo();
	}
	
	/**
	 * Enter olympiad observer mode.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param id the id
	 */
	public void enterOlympiadObserverMode(final int x, final int y, final int z, final int id)
	{
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
			getPet().unSummon(this);
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		if (getParty() != null)
			getParty().removePartyMember(this);
		
		_olympiadGameId = id;
		
		if (isSitting())
			standUp();
		
		if (!_observerMode)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		
		_observerMode = true;
		setTarget(null);
		setIsInvul(true);
		_wasInvisible = getAppearance().getInvisible();
		getAppearance().setInvisible();
		
		teleToLocation(x, y, z, false);
		sendPacket(new ExOlympiadMode(3, this));
		broadcastUserInfo();
	}
	
	/**
	 * Leave observer mode.
	 */
	public void leaveObserverMode()
	{
		if (!_observerMode)
		{
			LOGGER.warn("Player " + L2PcInstance.this.getName() + " request leave observer mode when he not use it!");
			Util.handleIllegalPlayerAction(L2PcInstance.this, "Warning!! Character " + L2PcInstance.this.getName() + " tried to cheat in observer mode.", Config.DEFAULT_PUNISH);
		}
		setTarget(null);
		setXYZ(_obsX, _obsY, _obsZ);
		setIsParalyzed(false);
		
		if (_wasInvisible)
		{
			getAppearance().setInvisible();
		}
		else
			getAppearance().setVisible();
		
		setIsInvul(false);
		
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		teleToLocation(_obsX, _obsY, _obsZ, false);
		_observerMode = false;
		sendPacket(new ObservationReturn(this));
		
		if (!_wasInvisible)
			broadcastUserInfo();
	}
	
	/**
	 * Leave olympiad observer mode.
	 */
	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(_obsX, _obsY, _obsZ, true);
		getAppearance().setVisible();
		setIsInvul(false);
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		Olympiad.getInstance();
		Olympiad.removeSpectator(_olympiadGameId, this);
		_olympiadGameId = -1;
		_observerMode = false;
		
		if (!_wasInvisible)
			broadcastUserInfo();
		
	}
	
	/**
	 * Update name title color.
	 */
	public void updateNameTitleColor()
	{
		if (isMarried())
		{
			if (marriedType() == 1)
			{
				getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_LESBO);
			}
			else if (marriedType() == 2)
			{
				getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_GEY);
			}
			else
			{
				getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_NORMAL);
			}
		}
		/** Updates title and name color of a donator **/
		if (Config.DONATOR_NAME_COLOR_ENABLED && isDonator())
		{
			getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);
			getAppearance().setTitleColor(Config.DONATOR_TITLE_COLOR);
		}
	}
	
	/**
	 * Update gm name title color.
	 */
	public void updateGmNameTitleColor()// KidZor: needs to be finished when Acces levels system is complite
	{
		// if this is a GM but has disabled his gM status, so we clear name / title
		if (isGM() && !hasGmStatusActive())
		{
			getAppearance().setNameColor(0xFFFFFF);
			getAppearance().setTitleColor(0xFFFF77);
		}
		// this is a GM but has GM status enabled, so we must set proper values
		else if (isGM() && hasGmStatusActive())
		{
			// Nick Updates
			if (getAccessLevel().useNameColor())
			{
				// this is a normal GM
				if (isNormalGm())
				{
					getAppearance().setNameColor(getAccessLevel().getNameColor());
				}
				else if (isAdministrator())
				{
					getAppearance().setNameColor(Config.MASTERACCESS_NAME_COLOR);
				}
			}
			else
			{
				getAppearance().setNameColor(0xFFFFFF);
			}
			
			// Title Updates
			if (getAccessLevel().useTitleColor())
			{
				// this is a normal GM
				if (isNormalGm())
				{
					getAppearance().setTitleColor(getAccessLevel().getTitleColor());
				}
				else if (isAdministrator())
				{
					getAppearance().setTitleColor(Config.MASTERACCESS_TITLE_COLOR);
				}
			}
			else
			{
				getAppearance().setTitleColor(0xFFFF77);
			}
		}
	}
	
	/**
	 * Sets the olympiad side.
	 * @param i the new olympiad side
	 */
	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}
	
	/**
	 * Gets the olympiad side.
	 * @return the olympiad side
	 */
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	/**
	 * Sets the olympiad game id.
	 * @param id the new olympiad game id
	 */
	public void setOlympiadGameId(final int id)
	{
		_olympiadGameId = id;
	}
	
	/**
	 * Gets the olympiad game id.
	 * @return the olympiad game id
	 */
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	/**
	 * Gets the obs x.
	 * @return the obs x
	 */
	public int getObsX()
	{
		return _obsX;
	}
	
	/**
	 * Gets the obs y.
	 * @return the obs y
	 */
	public int getObsY()
	{
		return _obsY;
	}
	
	/**
	 * Gets the obs z.
	 * @return the obs z
	 */
	public int getObsZ()
	{
		return _obsZ;
	}
	
	/**
	 * In observer mode.
	 * @return true, if successful
	 */
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	/**
	 * set observer mode.
	 * @param mode
	 */
	public void setObserverMode(final boolean mode)
	{
		_observerMode = mode;
	}
	
	/**
	 * Gets the tele mode.
	 * @return the tele mode
	 */
	public int getTeleMode()
	{
		return _telemode;
	}
	
	/**
	 * Sets the tele mode.
	 * @param mode the new tele mode
	 */
	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}
	
	/**
	 * Sets the loto.
	 * @param i the i
	 * @param val the val
	 */
	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}
	
	/**
	 * Gets the loto.
	 * @param i the i
	 * @return the loto
	 */
	public int getLoto(final int i)
	{
		return _loto[i];
	}
	
	/**
	 * Sets the race.
	 * @param i the i
	 * @param val the val
	 */
	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}
	
	/**
	 * Gets the race.
	 * @param i the i
	 * @return the race
	 */
	public int getRace(final int i)
	{
		return _race[i];
	}
	
	/*
	 * public void setChatBanned(boolean isBanned) { _chatBanned = isBanned; if(isChatBanned()) { sendMessage("You have been chat banned by a server admin."); } else { sendMessage("Your chat ban has been lifted."); if(_chatUnbanTask != null) { _chatUnbanTask.cancel(false); } _chatUnbanTask = null; }
	 * sendPacket(new EtcStatusUpdate(this)); } public boolean isChatBanned() { return _chatBanned; } public void setChatUnbanTask(ScheduledFuture<?> task) { _chatUnbanTask = task; } public ScheduledFuture<?> getChatUnbanTask() { return _chatUnbanTask; }
	 */
	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR>
	 * <BR>
	 */
	@Override
	public void sendPacket(final L2GameServerPacket packet)
	{
		if (_client != null)
		{
			_client.sendPacket(packet);
		}
	}
	
	/**
	 * Send SystemMessage packet.<BR>
	 * <BR>
	 * @param id
	 */
	public void sendPacket(final SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * Gets the message refusal.
	 * @return the message refusal
	 */
	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}
	
	/**
	 * Sets the message refusal.
	 * @param mode the new message refusal
	 */
	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Sets the diet mode.
	 * @param mode the new diet mode
	 */
	public void setDietMode(final boolean mode)
	{
		_dietMode = mode;
	}
	
	/**
	 * Gets the diet mode.
	 * @return the diet mode
	 */
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	/**
	 * Sets the exchange refusal.
	 * @param mode the new exchange refusal
	 */
	public void setExchangeRefusal(final boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	/**
	 * Gets the exchange refusal.
	 * @return the exchange refusal
	 */
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	/**
	 * Gets the block list.
	 * @return the block list
	 */
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	/**
	 * Sets the hero aura.
	 * @param heroAura the new hero aura
	 */
	public void setHeroAura(final boolean heroAura)
	{
		isPVPHero = heroAura;
		return;
	}
	
	/**
	 * Gets the checks if is pvp hero.
	 * @return the checks if is pvp hero
	 */
	public boolean getIsPVPHero()
	{
		return isPVPHero;
	}
	
	/**
	 * Gets the count.
	 * @return the count
	 */
	public int getCount()
	{
		
		final String HERO_COUNT = "SELECT count FROM heroes WHERE char_name=?";
		int _count = 0;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(HERO_COUNT);
			statement.setString(1, getName());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_count = rset.getInt("count");
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			statement = null;
			rset = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		if (_count != 0)
			return _count;
		return 0;
	}
	
	/**
	 * Reload pvp hero aura.
	 */
	public void reloadPVPHeroAura()
	{
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Sets the checks if is hero.
	 * @param hero the new checks if is hero
	 */
	/*
	 * public void setIsHero(boolean hero) { if(hero && _baseClass == _activeClass) { for(L2Skill s : HeroSkillTable.getHeroSkills()) { addSkill(s, false); //Dont Save Hero skills to database } } else if(getCount() >= Config.HERO_COUNT && hero && Config.ALLOW_HERO_SUBSKILL) { for(L2Skill s :
	 * HeroSkillTable.getHeroSkills()) { addSkill(s, false); //Dont Save Hero skills to database } } else { for(L2Skill s : HeroSkillTable.getHeroSkills()) { super.removeSkill(s); //Just Remove skills from nonHero characters } } _hero = hero; sendSkillList(); }
	 */
	
	/**
	 * Sets the donator.
	 * @param value the new donator
	 */
	public void setDonator(final boolean value)
	{
		_donator = value;
	}
	
	/**
	 * Checks if is donator.
	 * @return true, if is donator
	 */
	public boolean isDonator()
	{
		return _donator;
	}
	
	/**
	 * Checks if is away.
	 * @return true, if is away
	 */
	public boolean isAway()
	{
		return _isAway;
	}
	
	/**
	 * Sets the checks if is away.
	 * @param state the new checks if is away
	 */
	public void setIsAway(final boolean state)
	{
		_isAway = state;
	}
	
	/**
	 * Sets the checks if is in olympiad mode.
	 * @param b the new checks if is in olympiad mode
	 */
	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}
	
	/**
	 * Sets the checks if is olympiad start.
	 * @param b the new checks if is olympiad start
	 */
	public void setIsOlympiadStart(final boolean b)
	{
		_OlympiadStart = b;
	}
	
	/**
	 * Checks if is olympiad start.
	 * @return true, if is olympiad start
	 */
	public boolean isOlympiadStart()
	{
		return _OlympiadStart;
	}
	
	/**
	 * Sets the olympiad position.
	 * @param pos the new olympiad position
	 */
	public void setOlympiadPosition(final int[] pos)
	{
		_OlympiadPosition = pos;
	}
	
	/**
	 * Gets the olympiad position.
	 * @return the olympiad position
	 */
	public int[] getOlympiadPosition()
	{
		return _OlympiadPosition;
	}
	
	/**
	 * Checks if is hero.
	 * @return true, if is hero
	 */
	public boolean isHero()
	{
		return _hero;
	}
	
	/**
	 * Checks if is in olympiad mode.
	 * @return true, if is in olympiad mode
	 */
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}
	
	/**
	 * Checks if is in duel.
	 * @return true, if is in duel
	 */
	public boolean isInDuel()
	{
		return _isInDuel;
	}
	
	/**
	 * Gets the duel id.
	 * @return the duel id
	 */
	public int getDuelId()
	{
		return _duelId;
	}
	
	/**
	 * Sets the duel state.
	 * @param mode the new duel state
	 */
	public void setDuelState(final int mode)
	{
		_duelState = mode;
	}
	
	/**
	 * Gets the duel state.
	 * @return the duel state
	 */
	public int getDuelState()
	{
		return _duelState;
	}
	
	/**
	 * Sets the coupon.
	 * @param coupon the new coupon
	 */
	public void setCoupon(final int coupon)
	{
		if (coupon >= 0 && coupon <= 3)
		{
			_hasCoupon = coupon;
		}
	}
	
	/**
	 * Adds the coupon.
	 * @param coupon the coupon
	 */
	public void addCoupon(final int coupon)
	{
		if (coupon == 1 || coupon == 2 && !getCoupon(coupon - 1))
		{
			_hasCoupon += coupon;
		}
	}
	
	/**
	 * Gets the coupon.
	 * @param coupon the coupon
	 * @return the coupon
	 */
	public boolean getCoupon(final int coupon)
	{
		return (_hasCoupon == 1 || _hasCoupon == 3) && coupon == 0 || (_hasCoupon == 2 || _hasCoupon == 3) && coupon == 1;
	}
	
	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setIsInDuel(final int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}
	
	/**
	 * This returns a SystemMessage stating why the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		final SystemMessage sm = new SystemMessage(_noDuelReason);
		sm.addString(getName());
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	/**
	 * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if (isDead() || isAlikeDead() || getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		if (isInOlympiadMode())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if (isCursedWeaponEquiped())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
			return false;
		}
		if (isInsideZone(ZONE_PVP) || isInsideZone(ZONE_PEACE) || isInsideZone(ZONE_SIEGE))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if is noble.
	 * @return true, if is noble
	 */
	public boolean isNoble()
	{
		return _noble;
	}
	
	/**
	 * Sets the noble.
	 * @param val the new noble
	 */
	public void setNoble(final boolean val)
	{
		if (val)
		{
			for (final L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
			{
				addSkill(s, false); // Dont Save Noble skills to Sql
			}
		}
		else
		{
			for (final L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
			{
				super.removeSkill(s); // Just Remove skills without deleting from Sql
			}
		}
		_noble = val;
		
		sendSkillList();
	}
	
	/**
	 * Adds the clan leader skills.
	 * @param val the val
	 */
	public void addClanLeaderSkills(final boolean val)
	{
		if (val)
		{
			SiegeManager.getInstance().addSiegeSkills(this);
			/*
			 * for(L2Skill s : ClanLeaderSkillTable.getInstance().GetClanLeaderSkills()) { addSkill(s, false); //Dont Save Noble skills to Sql }
			 */
		}
		else
		{
			SiegeManager.getInstance().removeSiegeSkills(this);
			/*
			 * for(L2Skill s : ClanLeaderSkillTable.getInstance().GetClanLeaderSkills()) { super.removeSkill(s); //Just Remove skills without deleting from Sql }
			 */
		}
		sendSkillList();
	}
	
	/**
	 * Sets the lvl joined academy.
	 * @param lvl the new lvl joined academy
	 */
	public void setLvlJoinedAcademy(final int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	/**
	 * Gets the lvl joined academy.
	 * @return the lvl joined academy
	 */
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	/**
	 * Checks if is academy member.
	 * @return true, if is academy member
	 */
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	/**
	 * Sets the team.
	 * @param team the new team
	 */
	public void setTeam(final int team)
	{
		_team = team;
	}
	
	/**
	 * Gets the team.
	 * @return the team
	 */
	public int getTeam()
	{
		return _team;
	}
	
	/**
	 * Sets the wants peace.
	 * @param wantsPeace the new wants peace
	 */
	public void setWantsPeace(final int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	/**
	 * Gets the wants peace.
	 * @return the wants peace
	 */
	public int getWantsPeace()
	{
		return _wantsPeace;
	}
	
	/**
	 * Checks if is fishing.
	 * @return true, if is fishing
	 */
	public boolean isFishing()
	{
		return _fishing;
	}
	
	/**
	 * Sets the fishing.
	 * @param fishing the new fishing
	 */
	public void setFishing(final boolean fishing)
	{
		_fishing = fishing;
	}
	
	/**
	 * Sets the alliance with varka ketra.
	 * @param sideAndLvlOfAlliance the new alliance with varka ketra
	 */
	public void setAllianceWithVarkaKetra(final int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	/**
	 * Gets the alliance with varka ketra.
	 * @return the alliance with varka ketra
	 */
	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}
	
	/**
	 * Checks if is allied with varka.
	 * @return true, if is allied with varka
	 */
	public boolean isAlliedWithVarka()
	{
		return _alliedVarkaKetra < 0;
	}
	
	/**
	 * Checks if is allied with ketra.
	 * @return true, if is allied with ketra
	 */
	public boolean isAlliedWithKetra()
	{
		return _alliedVarkaKetra > 0;
	}
	
	/**
	 * Send skill list.
	 */
	public void sendSkillList()
	{
		sendSkillList(this);
	}
	
	/**
	 * Send skill list.
	 * @param player the player
	 */
	public void sendSkillList(final L2PcInstance player)
	{
		SkillList sl = new SkillList();
		if (player != null)
		{
			for (final L2Skill s : player.getAllSkills())
			{
				if (s == null)
				{
					continue;
				}
				
				if (s.getId() > 9000)
				{
					continue; // Fake skills to change base stats
				}
				
				if (s.bestowed())
				{
					continue;
				}
				
				if (s.isChance())
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isChance());
				}
				else
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isPassive());
				}
			}
		}
		sendPacket(sl);
		sl = null;
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 * @param classId the class id
	 * @param classIndex the class index
	 * @return boolean subclassAdded
	 */
	public synchronized boolean addSubClass(final int classId, final int classIndex)
	{
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final L2ItemInstance rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		
		if (getTotalSubClasses() == Config.ALLOWED_SUBCLASS || classIndex == 0)
			return false;
		
		if (getSubClasses().containsKey(classIndex))
			return false;
		
		// Note: Never change _classIndex in any method other than setActiveClass().
		
		final SubClass newClass = new SubClass();
		newClass.setClassId(classId);
		newClass.setClassIndex(classIndex);
		
		boolean output = false;
		
		Connection con = null;
		
		try
		{
			// Store the basic info about this new sub-class.
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, newClass.getSp());
			statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex()); // <-- Added
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			output = true;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("WARNING: Could not add character sub class for " + getName() + ": " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		if (output)
		{
			
			// Commit after database INSERT incase exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			if (Config.DEBUG)
			{
				LOGGER.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
			}
			
			ClassId subTemplate = ClassId.values()[classId];
			Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
			subTemplate = null;
			
			if (skillTree == null)
				return true;
			
			Map<Integer, L2Skill> prevSkillList = new FastMap<>();
			
			for (final L2SkillLearn skillInfo : skillTree)
			{
				if (skillInfo.getMinLevel() <= 40)
				{
					final L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
					final L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());
					
					if (newSkill == null || prevSkill != null && prevSkill.getLevel() > newSkill.getLevel())
					{
						continue;
					}
					
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}
			skillTree = null;
			prevSkillList = null;
			
			if (Config.DEBUG)
			{
				LOGGER.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
			}
			
		}
		
		return output;
	}
	
	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 * @param classIndex the class index
	 * @param newClassId the new class id
	 * @return boolean subclassAdded
	 */
	public boolean modifySubClass(final int classIndex, final int newClassId)
	{
		final int oldClassId = getSubClasses().get(classIndex).getClassId();
		
		if (Config.DEBUG)
		{
			LOGGER.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");
		}
		
		boolean output = false;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			
			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Remove all effects info stored for this sub-class.
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SKILLS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			DatabaseUtils.close(statement);
			
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			output = true;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		getSubClasses().remove(classIndex);
		
		if (output)
		{
			return addSubClass(newClassId, classIndex);
		}
		return false;
	}
	
	/**
	 * Checks if is sub class active.
	 * @return true, if is sub class active
	 */
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	/**
	 * Gets the sub classes.
	 * @return the sub classes
	 */
	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
		{
			_subClasses = new FastMap<>();
		}
		
		return _subClasses;
	}
	
	/**
	 * Gets the total sub classes.
	 * @return the total sub classes
	 */
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	/**
	 * Gets the base class.
	 * @return the base class
	 */
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	/**
	 * Gets the active class.
	 * @return the active class
	 */
	public synchronized int getActiveClass()
	{
		return _activeClass;
	}
	
	/**
	 * Gets the class index.
	 * @return the class index
	 */
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	/**
	 * Sets the class template.
	 * @param classId the new class template
	 */
	private synchronized void setClassTemplate(final int classId)
	{
		_activeClass = classId;
		
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);
		
		if (t == null)
		{
			LOGGER.warn("Missing template for classId: " + classId);
			throw new Error();
		}
		
		// Set the template of the L2PcInstance
		setTemplate(t);
		t = null;
	}
	
	/**
	 * Changes the character's class based on the given class index. <BR>
	 * <BR>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
	 * @param classIndex the class index
	 * @return true, if successful
	 */
	public synchronized boolean setActiveClass(final int classIndex)
	{
		if (isInCombat() || this.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			sendMessage("Impossible switch class if in combat");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Delete a force buff upon class change.
		// thank l2j-arhid
		if (_forceBuff != null)
		{
			abortCast();
		}
		
		/**
		 * 1. Call store() before modifying _classIndex to avoid skill effects rollover. 2. Register the correct _classId against applied 'classIndex'.
		 */
		store();
		
		if (classIndex == 0)
		{
			setClassTemplate(getBaseClass());
		}
		else
		{
			try
			{
				setClassTemplate(getSubClasses().get(classIndex).getClassId());
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e);
				return false;
			}
		}
		_classIndex = classIndex;
		
		if (isInParty())
		{
			getParty().recalculatePartyLevel();
		}
		
		/*
		 * Update the character's change in class status. 1. Remove any active cubics from the player. 2. Renovate the characters table in the database with the new class info, storing also buff/effect data. 3. Remove all existing skills. 4. Restore all the learned skills for the current class from
		 * the database. 5. Restore effect/buff data for the new class. 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones. 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes. 8. Restore shortcut data related to
		 * this class. 9. Resend a class change animation effect to broadcast to all nearby players. 10.Unsummon any active servitor from the player.
		 */
		
		if (getPet() != null && getPet() instanceof L2SummonInstance)
		{
			getPet().unSummon(this);
		}
		
		unsummonAllCubics();
		
		/*
		 * for(L2Character character : getKnownList().getKnownCharacters()) { if(character.getForceBuff() != null && character.getForceBuff().getTarget() == this) { character.abortCast(); } }
		 */
		
		synchronized (getAllSkills())
		{
			
			for (final L2Skill oldSkill : getAllSkills())
			{
				super.removeSkill(oldSkill);
			}
			
		}
		
		// Yesod: Rebind CursedWeapon passive.
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().givePassive(_cursedWeaponEquipedId);
		}
		
		stopAllEffects();
		
		if (isSubClassActive())
		{
			_dwarvenRecipeBook.clear();
			_commonRecipeBook.clear();
		}
		else
		{
			restoreRecipeBook();
		}
		
		// Restore any Death Penalty Buff
		restoreDeathPenaltyBuffLevel();
		
		restoreSkills();
		regiveTemporarySkills();
		rewardSkills();
		restoreEffects(Config.ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE);
		
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final L2ItemInstance rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart());
				final InventoryUpdate iu = new InventoryUpdate();
				for (final L2ItemInstance element : unequipped)
					iu.addModifiedItem(element);
				sendPacket(iu);
			}
		}
		
		// Check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
			checkAllowedSkills();
		
		sendPacket(new EtcStatusUpdate(this));
		
		// if player has quest 422: Repent Your Sins, remove it
		QuestState st = getQuestState("422_RepentYourSins");
		
		if (st != null)
		{
			st.exitQuest(true);
			st = null;
		}
		
		for (int i = 0; i < 3; i++)
		{
			_henna[i] = null;
		}
		
		restoreHenna();
		sendPacket(new HennaInfo(this));
		
		if (getCurrentHp() > getMaxHp())
		{
			setCurrentHp(getMaxHp());
		}
		
		if (getCurrentMp() > getMaxMp())
		{
			setCurrentMp(getMaxMp());
		}
		
		if (getCurrentCp() > getMaxCp())
		{
			setCurrentCp(getMaxCp());
		}
		
		// Refresh player infos and update new status
		broadcastUserInfo();
		refreshOverloaded();
		refreshExpertisePenalty();
		refreshMasteryPenality();
		refreshMasteryWeapPenality();
		sendPacket(new UserInfo(this));
		sendPacket(new ItemList(this, false));
		getInventory().refreshWeight();
		
		// Clear resurrect xp calculation
		setExpBeforeDeath(0);
		_macroses.restore();
		_macroses.sendUpdate();
		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		
		// Rebirth Caller - if player has any skills, they will be granted them.
		if (Config.REBIRTH_ENABLE)
			L2Rebirth.getInstance().grantRebirthSkills(this);
		
		broadcastPacket(new SocialAction(getObjectId(), 15));
		sendPacket(new SkillCoolTime(this));
		
		if (getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		// decayMe();
		// spawnMe(getX(), getY(), getZ());
		
		return true;
	}
	
	/**
	 * Broadcast class icon.
	 */
	public void broadcastClassIcon()
	{
		// Update class icon in party and clan
		if (isInParty())
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		
		if (getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}
	
	/**
	 * Stop warn user take break.
	 */
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	/**
	 * Start warn user take break.
	 */
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}
	
	/**
	 * Start bot checker.
	 */
	public void startBotChecker()
	{
		if (_taskBotChecker == null)
		{
			if (Config.QUESTION_LIST.size() != 0)
			{
				_taskBotChecker = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new botChecker(), Config.BOT_PROTECTOR_FIRST_CHECK * 60000, Config.BOT_PROTECTOR_NEXT_CHECK * 60000);
			}
			else
			{
				LOGGER.warn("ATTENTION: Bot Checker is bad configured because config/questionwords.txt has 0 words of 6 to 15 keys");
			}
		}
	}
	
	/**
	 * Stop bot checker.
	 */
	public void stopBotChecker()
	{
		if (_taskBotChecker != null)
		{
			_taskBotChecker.cancel(true);
			_taskBotChecker = null;
		}
	}
	
	/**
	 * Check answer.
	 * @param id the id
	 */
	public void checkAnswer(final int id)
	{
		if (id - 100000 == _correctWord)
		{
			_stopKickBotTask = true;
		}
		else
		{
			closeNetConnection();
		}
	}
	
	/**
	 * Stop rent pet.
	 */
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && getMountType() == 2)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			
			if (setMountType(0)) // this should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
				sendPacket(dismount);
				broadcastPacket(dismount);
				dismount = null;
				_taskRentPet = null;
			}
		}
	}
	
	/**
	 * Start rent pet.
	 * @param seconds the seconds
	 */
	public void startRentPet(final int seconds)
	{
		if (_taskRentPet == null)
		{
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
		}
	}
	
	/**
	 * Checks if is rented pet.
	 * @return true, if is rented pet
	 */
	public boolean isRentedPet()
	{
		if (_taskRentPet != null)
			return true;
		
		return false;
	}
	
	/**
	 * Stop water task.
	 */
	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
			// for catacombs...
			broadcastUserInfo();
		}
	}
	
	/**
	 * Start water task.
	 */
	public void startWaterTask()
	{
		broadcastUserInfo();
		if (!isDead() && _taskWater == null)
		{
			final int timeinwater = 86000;
			
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}
	
	/**
	 * Checks if is in water.
	 * @return true, if is in water
	 */
	public boolean isInWater()
	{
		if (_taskWater != null)
			return true;
		
		return false;
	}
	
	/**
	 * Check water state.
	 */
	public void checkWaterState()
	{
		// checking if char is over base level of water (sea, rivers)
		if (getZ() > -3750)
		{
			stopWaterTask();
			return;
		}
		
		if (isInsideZone(ZONE_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
			return;
		}
	}
	
	/**
	 * On player enter.
	 */
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (Config.BOT_PROTECTOR)
		{
			startBotChecker();
		}
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
		
		// jail task
		updatePunishState();
		
		if (_isInvul)
		{
			sendMessage("Entering world in Invulnerable mode.");
		}
		
		if (getAppearance().getInvisible())
		{
			sendMessage("Entering world in Invisible mode.");
		}
		
		if (getMessageRefusal())
		{
			sendMessage("Entering world in Message Refusal mode.");
		}
		
		revalidateZone(true);
		
		notifyFriends(false);
		
		// Fix against exploit on anti-target on login
		decayMe();
		spawnMe();
		broadcastUserInfo();
		
	}
	
	/**
	 * Gets the last access.
	 * @return the last access
	 */
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	/**
	 * Check recom.
	 * @param recsHave the recs have
	 * @param recsLeft the recs left
	 */
	private void checkRecom(final int recsHave, final int recsLeft)
	{
		final Calendar check = Calendar.getInstance();
		check.setTimeInMillis(_lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		
		final Calendar min = Calendar.getInstance();
		
		_recomHave = recsHave;
		_recomLeft = recsLeft;
		
		if (getStat().getLevel() < 10 || check.after(min))
			return;
		
		restartRecom();
	}
	
	/**
	 * Restart recom.
	 */
	public void restartRecom()
	{
		if (Config.ALT_RECOMMEND)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS);
				statement.setInt(1, getObjectId());
				statement.execute();
				DatabaseUtils.close(statement);
				statement = null;
				
				_recomChars.clear();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.warn("could not clear char recommendations: " + e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
		
		if (getStat().getLevel() < 20)
		{
			_recomLeft = 3;
			_recomHave--;
		}
		else if (getStat().getLevel() < 40)
		{
			_recomLeft = 6;
			_recomHave -= 2;
		}
		else
		{
			_recomLeft = 9;
			_recomHave -= 3;
		}
		
		if (_recomHave < 0)
		{
			_recomHave = 0;
		}
		
		// If we have to update last update time, but it's now before 13, we should set it to yesterday
		final Calendar update = Calendar.getInstance();
		if (update.get(Calendar.HOUR_OF_DAY) < 13)
		{
			update.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		update.set(Calendar.HOUR_OF_DAY, 13);
		_lastRecomUpdate = update.getTimeInMillis();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#doRevive()
	 */
	@Override
	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_reviveRequested = 0;
		_revivePower = 0;
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
			{
				getParty().getDimensionalRift().memberRessurected(this);
			}
		}
		
		if ((_inEventTvT && TvT.is_started() && Config.TVT_REVIVE_RECOVERY) || (_inEventCTF && CTF.is_started() && Config.CTF_REVIVE_RECOVERY) || (_inEventDM && DM.is_started() && Config.DM_REVIVE_RECOVERY))
		{
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
			getStatus().setCurrentCp(getMaxCp());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#doRevive(double)
	 */
	@Override
	public void doRevive(final double revivePower)
	{
		// Restore the player's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	/**
	 * Revive request.
	 * @param Reviver the reviver
	 * @param skill the skill
	 * @param Pet the pet
	 */
	public void reviveRequest(final L2PcInstance Reviver, final L2Skill skill, final boolean Pet)
	{
		if (_reviveRequested == 1)
		{
			if (_revivePet == Pet)
			{
				Reviver.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
			}
			else
			{
				if (Pet)
				{
					Reviver.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_RES)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
				}
				else
				{
					Reviver.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				}
			}
			return;
		}
		if (Pet && getPet() != null && getPet().isDead() || !Pet && isDead())
		{
			_reviveRequested = 1;
			if (isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else if (skill != null)
			{
				_revivePower = Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), Reviver);
			}
			else
			{
				_revivePower = 0;
			}
			_revivePet = Pet;
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST.getId());
			dlg.addString(Reviver.getName());
			sendPacket(dlg);
			dlg = null;
		}
	}
	
	/**
	 * Revive answer.
	 * @param answer the answer
	 */
	public void reviveAnswer(final int answer)
	{
		if (_reviveRequested != 1 || !isDead() && !_revivePet || _revivePet && getPet() != null && !getPet().isDead())
			return;
		// If character refuse a PhoenixBlessed autoress, cancel all buffs he had
		if (answer == 0 && isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			stopAllEffects();
		}
		if (answer == 1)
		{
			if (!_revivePet)
			{
				if (_revivePower != 0)
				{
					doRevive(_revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (getPet() != null)
			{
				if (_revivePower != 0)
				{
					getPet().doRevive(_revivePower);
				}
				else
				{
					getPet().doRevive();
				}
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	/**
	 * Checks if is revive requested.
	 * @return true, if is revive requested
	 */
	public boolean isReviveRequested()
	{
		return _reviveRequested == 1;
	}
	
	/**
	 * Checks if is reviving pet.
	 * @return true, if is reviving pet
	 */
	public boolean isRevivingPet()
	{
		return _revivePet;
	}
	
	/**
	 * Removes the reviving.
	 */
	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	/**
	 * On action request.
	 */
	public void onActionRequest()
	{
		/*
		 * Important: dont send here a broadcast like removeAbnornalstatus cause they will create mass lag on pvp
		 */
		
		if (isSpawnProtected())
			sendMessage("The effect of Spawn Protection has been removed.");
		else if (isTeleportProtected())
			sendMessage("The effect of Teleport Spawn Protection has been removed.");
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			setProtection(false);
		
		if (Config.PLAYER_TELEPORT_PROTECTION > 0)
			setTeleportProtection(false);
	}
	
	/**
	 * Sets the expertise index.
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(final int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}
	
	/**
	 * Gets the expertise index.
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#onTeleported()
	 */
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		// Force a revalidation
		revalidateZone(true);
		
		if ((Config.PLAYER_TELEPORT_PROTECTION > 0) && !isInOlympiadMode())
		{
			setTeleportProtection(true);
			sendMessage("The effects of Teleport Spawn Protection flow through you.");
		}
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		// Modify the position of the tamed beast if necessary (normal pets are handled by super...though
		// L2PcInstance is the only class that actually has pets!!! )
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
			getTrainedBeast().getAI().startFollow(this);
		}
		
		// To be sure update also the pvp flag / war tag status
		if (!inObserverMode())
			broadcastUserInfo();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#updatePosition(int)
	 */
	@Override
	public final boolean updatePosition(final int gameTicks)
	{
		// Disables custom movement for L2PCInstance when Old Synchronization is selected
		if (Config.COORD_SYNCHRONIZE == -1)
			return super.updatePosition(gameTicks);
		
		// Get movement data
		final MoveData m = _move;
		
		if (_move == null)
			return true;
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == gameTicks)
			return false;
		
		final double dx = m._xDestination - getX();
		final double dy = m._yDestination - getY();
		final double dz = m._zDestination - getZ();
		final int distPassed = (int) getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		final double distFraction = distPassed / Math.sqrt(dx * dx + dy * dy + dz * dz);
		// if (Config.DEVELOPER) LOGGER.info("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1)
		{
			// Set the position of the L2Character to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			// Set the position of the L2Character to estimated after parcial move
			super.setXYZ(getX() + (int) (dx * distFraction + 0.5), getY() + (int) (dy * distFraction + 0.5), getZ() + (int) (dz * distFraction));
		}
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		
		revalidateZone(false);
		
		return distFraction > 1;
	}
	
	/**
	 * Sets the last client position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastClientPosition(final int x, final int y, final int z)
	{
		_lastClientPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last client position.
	 * @param loc the new last client position
	 */
	public void setLastClientPosition(final Location loc)
	{
		_lastClientPosition = loc;
	}
	
	/**
	 * Check last client position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean checkLastClientPosition(final int x, final int y, final int z)
	{
		return _lastClientPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last client distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last client distance
	 */
	public int getLastClientDistance(final int x, final int y, final int z)
	{
		final double dx = x - _lastClientPosition.getX();
		final double dy = y - _lastClientPosition.getY();
		final double dz = z - _lastClientPosition.getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Sets the last server position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastServerPosition(final int x, final int y, final int z)
	{
		_lastServerPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last server position.
	 * @param loc the new last server position
	 */
	public void setLastServerPosition(final Location loc)
	{
		_lastServerPosition = loc;
	}
	
	/**
	 * Check last server position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean checkLastServerPosition(final int x, final int y, final int z)
	{
		return _lastServerPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last server distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last server distance
	 */
	public int getLastServerDistance(final int x, final int y, final int z)
	{
		final double dx = x - _lastServerPosition.getX();
		final double dy = y - _lastServerPosition.getY();
		final double dz = z - _lastServerPosition.getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#addExpAndSp(long, int)
	 */
	@Override
	public void addExpAndSp(final long addToExp, final int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp);
	}
	
	/**
	 * Removes the exp and sp.
	 * @param removeExp the remove exp
	 * @param removeSp the remove sp
	 */
	public void removeExpAndSp(final long removeExp, final int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#reduceCurrentHp(double, com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public void reduceCurrentHp(final double i, final L2Character attacker)
	{
		getStatus().reduceHp(i, attacker);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	/*
	 * Function for skill summon friend or Gate Chant.
	 */
	/**
	 * Request Teleport *.
	 * @param requester the requester
	 * @param skill the skill
	 * @return true, if successful
	 */
	public boolean teleportRequest(final L2PcInstance requester, final L2Skill skill)
	{
		if (_summonRequest.getTarget() != null && requester != null)
			return false;
		_summonRequest.setTarget(requester, skill);
		return true;
	}
	
	/**
	 * Action teleport *.
	 * @param answer the answer
	 * @param requesterId the requester id
	 */
	public void teleportAnswer(final int answer, final int requesterId)
	{
		if (_summonRequest.getTarget() == null)
			return;
		if (answer == 1 && _summonRequest.getTarget().getObjectId() == requesterId)
		{
			teleToTarget(this, _summonRequest.getTarget(), _summonRequest.getSkill());
		}
		_summonRequest.setTarget(null, null);
	}
	
	/**
	 * Tele to target.
	 * @param targetChar the target char
	 * @param summonerChar the summoner char
	 * @param summonSkill the summon skill
	 */
	public static void teleToTarget(final L2PcInstance targetChar, final L2PcInstance summonerChar, final L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
			return;
		
		if (!checkSummonerStatus(summonerChar))
			return;
		if (!checkSummonTargetStatus(targetChar, summonerChar))
			return;
		
		final int itemConsumeId = summonSkill.getTargetConsumeId();
		final int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			// Delete by rocknow
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(summonSkill.getTargetConsumeId());
				targetChar.sendPacket(sm);
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(summonSkill.getTargetConsumeId());
			targetChar.sendPacket(sm);
		}
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
	}
	
	/**
	 * Check summoner status.
	 * @param summonerChar the summoner char
	 * @return true, if successful
	 */
	public static boolean checkSummonerStatus(final L2PcInstance summonerChar)
	{
		if (summonerChar == null)
			return false;
		
		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		if (summonerChar.inObserverMode())
		{
			return false;
		}
		
		if (summonerChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) || summonerChar.isFlying() || summonerChar.isMounted())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}
	
	/**
	 * Check summon target status.
	 * @param target the target
	 * @param summonerChar the summoner char
	 * @return true, if successful
	 */
	public static boolean checkSummonTargetStatus(final L2Object target, final L2PcInstance summonerChar)
	{
		if (target == null || !(target instanceof L2PcInstance))
			return false;
		
		final L2PcInstance targetChar = (L2PcInstance) target;
		
		if (targetChar.isAlikeDead())
		{
			return false;
		}
		
		if (targetChar.isInStoreMode())
		{
			return false;
		}
		
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			return false;
		}
		
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
			return false;
		}
		
		if (targetChar.isFestivalParticipant() || targetChar.isFlying())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.inObserverMode())
		{
			return false;
		}
		
		if (targetChar.isInCombat())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			return false;
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#reduceCurrentHp(double, com.l2jfrozen.gameserver.model.L2Character, boolean)
	 */
	@Override
	public void reduceCurrentHp(final double value, final L2Character attacker, final boolean awake)
	{
		getStatus().reduceHp(value, attacker, awake);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	public void broadcastSnoop(final int type, final String name, final String _text, final CreatureSay cs)
	{
		if (_snoopListener.size() > 0)
		{
			final Snoop sn = new Snoop(this, type, name, _text);
			for (final L2PcInstance pci : _snoopListener)
			{
				if (pci != null)
				{
					pci.sendPacket(cs);
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(final L2PcInstance pci)
	{
		if (!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(final L2PcInstance pci)
	{
		_snoopListener.remove(pci);
	}
	
	public void addSnooped(final L2PcInstance pci)
	{
		if (!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}
	
	public void removeSnooped(final L2PcInstance pci)
	{
		_snoopedPlayer.remove(pci);
	}
	
	/**
	 * Adds the bypass.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass(final String bypass)
	{
		if (bypass == null)
			return;
		_validBypass.add(bypass);
		// LOGGER.warn("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
	
	/**
	 * Adds the bypass2.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass2(final String bypass)
	{
		if (bypass == null)
			return;
		_validBypass2.add(bypass);
		// LOGGER.warn("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
	
	/**
	 * Validate bypass.
	 * @param cmd the cmd
	 * @return true, if successful
	 */
	public synchronized boolean validateBypass(final String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
			return true;
		
		for (final String bp : _validBypass)
		{
			if (bp == null)
			{
				continue;
			}
			
			// LOGGER.warn("[BypassValidation]"+getName()+" '"+bp+"'");
			if (bp.equals(cmd))
				return true;
		}
		
		for (final String bp : _validBypass2)
		{
			if (bp == null)
			{
				continue;
			}
			
			// LOGGER.warn("[BypassValidation]"+getName()+" '"+bp+"'");
			if (cmd.startsWith(bp))
				return true;
		}
		if (cmd.startsWith("npc_") && cmd.endsWith("_SevenSigns 7"))
			return true;
		
		final L2PcInstance player = getClient().getActiveChar();
		// We decided to put a kick because when a player is doing quest with a BOT he sends invalid bypass.
		Util.handleIllegalPlayerAction(player, "[L2PcInstance] player [" + player.getName() + "] sent invalid bypass '" + cmd + "'", Config.DEFAULT_PUNISH);
		return false;
	}
	
	/**
	 * Validate item manipulation by item id.
	 * @param itemId the item id
	 * @param action the action
	 * @return true, if successful
	 */
	public boolean validateItemManipulationByItemId(final int itemId, final String action)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getItemId() == itemId)
		{
			LOGGER.warn(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(itemId))
			// can not trade a cursed weapon
			return false;
		
		if (item.isWear())
			// cannot drop/trade wear-items
			return false;
		
		item = null;
		
		return true;
	}
	
	/**
	 * Validate item manipulation.
	 * @param objectId the object id
	 * @param action the action
	 * @return true, if successful
	 */
	public boolean validateItemManipulation(final int objectId, final String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			LOGGER.warn(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return false;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
			// can not trade a cursed weapon
			return false;
		
		if (item.isWear())
			// cannot drop/trade wear-items
			return false;
		
		item = null;
		
		return true;
	}
	
	/**
	 * Clear bypass.
	 */
	public synchronized void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	/**
	 * Validate link.
	 * @param cmd the cmd
	 * @return true, if successful
	 */
	public synchronized boolean validateLink(final String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
			return true;
		
		for (final String bp : _validLink)
		{
			if (bp == null)
				continue;
			
			if (bp.equals(cmd))
				return true;
		}
		LOGGER.warn("[L2PcInstance] player [" + getName() + "] sent invalid link '" + cmd + "', ban this player!");
		return false;
	}
	
	/**
	 * Clear links.
	 */
	public synchronized void clearLinks()
	{
		_validLink.clear();
	}
	
	/**
	 * Adds the link.
	 * @param link the link
	 */
	public synchronized void addLink(final String link)
	{
		if (link == null)
			return;
		_validLink.add(link);
	}
	
	/**
	 * Checks if is in boat.
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _inBoat;
	}
	
	/**
	 * Sets the in boat.
	 * @param inBoat The inBoat to set.
	 */
	public void setInBoat(final boolean inBoat)
	{
		_inBoat = inBoat;
	}
	
	/**
	 * Gets the boat.
	 * @return the boat
	 */
	public L2BoatInstance getBoat()
	{
		return _boat;
	}
	
	/**
	 * Sets the boat.
	 * @param boat the new boat
	 */
	public void setBoat(final L2BoatInstance boat)
	{
		_boat = boat;
	}
	
	/**
	 * Sets the in crystallize.
	 * @param inCrystallize the new in crystallize
	 */
	public void setInCrystallize(final boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	/**
	 * Checks if is in crystallize.
	 * @return true, if is in crystallize
	 */
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	/**
	 * Gets the in boat position.
	 * @return the in boat position
	 */
	public Point3D getInBoatPosition()
	{
		return _inBoatPosition;
	}
	
	/**
	 * Sets the in boat position.
	 * @param pt the new in boat position
	 */
	public void setInBoatPosition(final Point3D pt)
	{
		_inBoatPosition = pt;
	}
	
	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode</li> <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li> <li>Stop the HP/MP/CP Regeneration task</li> <li>
	 * Cancel Crafting, Attak or Cast</li> <li>Remove the L2PcInstance from the world</li> <li>Stop Party and Unsummon Pet</li> <li>Update database with items in its inventory and remove them from the world</li> <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then
	 * cancel Attak or Cast and notify AI</li> <li>Close the connection with the client</li><BR>
	 * <BR>
	 */
	public synchronized void deleteMe()
	{
		// Check if the L2PcInstance is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
		{
			setXYZ(_obsX, _obsY, _obsZ);
		}
		
		if (isTeleporting())
		{
			try
			{
				wait(2000);
			}
			catch (final InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			onTeleported();
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
			}
		}
		
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnlineStatus(false);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Stop crafting, if in progress
		try
		{
			RecipeController.getInstance().requestMakeItemAbort(this);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Cancel Attak or Cast
		try
		{
			abortAttack();
			abortCast();
			setTarget(null);
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		PartyMatchWaitingList.getInstance().removePlayer(this);
		if (_partyroom != 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
			if (room != null)
				room.deleteMember(this);
		}
		
		// Remove from world regions zones
		if (getWorldRegion() != null)
		{
			getWorldRegion().removeFromZones(this);
		}
		
		try
		{
			if (_forceBuff != null)
			{
				abortCast();
			}
			
			for (final L2Character character : getKnownList().getKnownCharacters())
				if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
				{
					character.abortCast();
				}
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Remove the L2PcInstance from the world
		if (isVisible())
		{
			try
			{
				decayMe();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// If a Party is in progress, leave it
		if (isInParty())
		{
			try
			{
				leaveParty();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// If the L2PcInstance has Pet, unsummon it
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.error("deleteMe()", t);
			}// returns pet to control item
		}
		
		if (getClanId() != 0 && getClan() != null)
		{
			// set the status for pledge member list to OFFLINE
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}
				clanMember = null;
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		if (getActiveRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setActiveRequester(null);
		}
		
		if (getOlympiadGameId() != -1)
		{
			Olympiad.getInstance().removeDisconnectedCompetitor(this);
		}
		
		// If the L2PcInstance is a GM, remove it from the GM List
		if (isGM())
		{
			try
			{
				GmListTable.getInstance().deleteGm(this);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				LOGGER.error("deleteMe()", t);
			}
		}
		
		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}
		
		// Update database with items in its freight and remove them from the world
		try
		{
			getFreight().deleteMe();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("deleteMe()", t);
		}
		
		// Close the connection with the client
		closeNetConnection();
		
		// remove from flood protector
		// FloodProtector.getInstance().removePlayer(getObjectId());
		
		if (getClanId() > 0)
		{
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			// ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
		}
		
		for (final L2PcInstance player : _snoopedPlayer)
		{
			player.removeSnooper(this);
		}
		
		for (final L2PcInstance player : _snoopListener)
		{
			player.removeSnooped(this);
		}
		
		if (_chanceSkills != null)
		{
			_chanceSkills.setOwner(null);
			_chanceSkills = null;
		}
		
		notifyFriends(true);
		
		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);
		L2World.getInstance().removeFromAllPlayers(this); // force remove in case of crash during teleport
		
	}
	
	/** ShortBuff clearing Task */
	private ScheduledFuture<?> _shortBuffTask = null;
	
	private class ShortBuffTask implements Runnable
	{
		private L2PcInstance _player = null;
		
		public ShortBuffTask(final L2PcInstance activeChar)
		{
			_player = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
				return;
			
			_player.sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
		}
	}
	
	/**
	 * @param magicId
	 * @param level
	 * @param time
	 */
	public void shortBuffStatusUpdate(final int magicId, final int level, final int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(this), 15000);
		
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	/** list of character friends. */
	private final List<String> _friendList = new FastList<>();
	
	/**
	 * Gets the friend list.
	 * @return the friend list
	 */
	public List<String> getFriendList()
	{
		return _friendList;
	}
	
	/**
	 * Restore friend list.
	 */
	public void restoreFriendList()
	{
		_friendList.clear();
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name,not_blocked FROM character_friends WHERE char_id=?");
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final String friendName = rset.getString("friend_name");
				
				if (friendName.equals(getName()))
					continue;
				
				final Integer blockedType = rset.getInt("not_blocked");
				
				if (blockedType == 1)
				{
					
					_friendList.add(friendName);
					
				}
				else
				{
					
					_blockList.getBlockList().add(friendName);
					
				}
				
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("could not restore friend data:" + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Notify friends.
	 * @param closing the closing
	 */
	private void notifyFriends(final boolean closing)
	{
		for (final String friendName : _friendList)
		{
			final L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			
			if (friend != null) // friend logged in.
			{
				friend.sendPacket(new FriendList(friend));
			}
		}
	}
	
	/*
	 * private void notifyFriends2(L2PcInstance cha) { Connection con = null; try { con = L2DatabaseFactory.getInstance().getConnection(false); PreparedStatement statement; statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?"); statement.setInt(1,
	 * cha.getObjectId()); ResultSet rset = statement.executeQuery(); while(rset.next()) { String friendName = rset.getString("friend_name"); L2PcInstance friend = L2World.getInstance().getPlayer(friendName); if(friend != null) //friend logged in. { friend.sendPacket(new FriendList(friend));
	 * friend.sendMessage("Friend: " + cha.getName() + " has logged off."); } } DatabaseUtils.close(rset); DatabaseUtils.close(statement); } catch(Exception e) { if(Config.ENABLE_ALL_EXCEPTIONS) e.printStackTrace(); LOGGER.warn("could not restore friend data:" + e); } finally { CloseUtil.close(con);
	 * con = null; } }
	 */
	
	/** The _fish. */
	private FishData _fish;
	
	/*
	 * startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check. Also worthy of note is the fact the code to find the hook landing position was also striped. The stripped code was moved into fishing.java. In my opinion it makes more sense for it to be there since
	 * all other skill related checks were also there. Last but not least, moving the zone check there, fixed a bug where baits would always be consumed no matter if fishing actualy took place. startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing
	 * coordinates.
	 */
	/**
	 * Start fishing.
	 * @param _x the _x
	 * @param _y the _y
	 * @param _z the _z
	 */
	public void startFishing(final int _x, final int _y, final int _z)
	{
		stopMove(null);
		setIsImobilised(true);
		_fishing = true;
		_fishx = _x;
		_fishy = _y;
		_fishz = _z;
		broadcastUserInfo();
		// Starts fishing
		final int lvl = GetRandomFishLvl();
		final int group = GetRandomGroup();
		final int type = GetRandomFishType(group);
		List<FishData> fishs = FishTable.getInstance().getfish(lvl, type, group);
		if (fishs == null || fishs.size() == 0)
		{
			sendMessage("Error - Fishes are not definied");
			EndFishing(false);
			return;
		}
		final int check = Rnd.get(fishs.size());
		// Use a copy constructor else the fish data may be over-written below
		_fish = new FishData(fishs.get(check));
		fishs.clear();
		fishs = null;
		sendPacket(new SystemMessage(SystemMessageId.CAST_LINE_AND_START_FISHING));
		ExFishingStart efs = null;
		
		if (!GameTimeController.getInstance().isNowNight() && _lure.isNightLure())
		{
			_fish.setType(-1);
		}
		
		// sendMessage("Hook x,y: " + _x + "," + _y + " - Water Z, Player Z:" + _z + ", " + getZ()); //debug line, uncoment to show coordinates used in fishing.
		efs = new ExFishingStart(this, _fish.getType(), _x, _y, _z, _lure.isNightLure());
		broadcastPacket(efs);
		efs = null;
		StartLookingForFishTask();
	}
	
	/**
	 * Stop looking for fish task.
	 */
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	/**
	 * Start looking for fish task.
	 */
	public void StartLookingForFishTask()
	{
		if (!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				final int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.33));
				}
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.00));
				}
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 0.66));
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	/**
	 * Gets the random group.
	 * @return the int
	 */
	private int GetRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}
	
	/**
	 * Gets the random fish type.
	 * @param group the group
	 * @return the int
	 */
	private int GetRandomFishType(final int group)
	{
		final int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch (_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	/**
	 * Gets the random fish lvl.
	 * @return the int
	 */
	private int GetRandomFishLvl()
	{
		L2Effect[] effects = getAllEffects();
		int skilllvl = getSkillLevel(1315);
		for (final L2Effect e : effects)
		{
			if (e.getSkill().getId() == 2274)
			{
				skilllvl = (int) e.getSkill().getPower(this);
			}
		}
		if (skilllvl <= 0)
			return 1;
		int randomlvl;
		final int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		effects = null;
		
		return randomlvl;
	}
	
	/**
	 * Start fish combat.
	 * @param isNoob the is noob
	 * @param isUpperGrade the is upper grade
	 */
	public void StartFishCombat(final boolean isNoob, final boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}
	
	/**
	 * End fishing.
	 * @param win the win
	 */
	public void EndFishing(final boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		efe = null;
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		broadcastUserInfo();
		
		if (_fishCombat == null)
		{
			sendPacket(new SystemMessage(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY));
		}
		
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		sendPacket(new SystemMessage(SystemMessageId.REEL_LINE_AND_STOP_FISHING));
		setIsImobilised(false);
		stopLookingForFishTask();
	}
	
	/**
	 * Gets the fish combat.
	 * @return the l2 fishing
	 */
	public L2Fishing GetFishCombat()
	{
		return _fishCombat;
	}
	
	/**
	 * Gets the fishx.
	 * @return the int
	 */
	public int GetFishx()
	{
		return _fishx;
	}
	
	/**
	 * Gets the fishy.
	 * @return the int
	 */
	public int GetFishy()
	{
		return _fishy;
	}
	
	/**
	 * Gets the fishz.
	 * @return the int
	 */
	public int GetFishz()
	{
		return _fishz;
	}
	
	public void SetPartyFind(final int find)
	{
		_party_find = find;
	}
	
	public int GetPartyFind()
	{
		return _party_find;
	}
	
	/**
	 * Sets the lure.
	 * @param lure the lure
	 */
	public void SetLure(final L2ItemInstance lure)
	{
		_lure = lure;
	}
	
	/**
	 * Gets the lure.
	 * @return the l2 item instance
	 */
	public L2ItemInstance GetLure()
	{
		return _lure;
	}
	
	/**
	 * Gets the inventory limit.
	 * @return the int
	 */
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
		
		return ivlim;
	}
	
	/**
	 * Gets the ware house limit.
	 * @return the int
	 */
	public int GetWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
		
		return whlim;
	}
	
	/**
	 * Gets the private sell store limit.
	 * @return the int
	 */
	public int GetPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.dwarf)
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		
		else
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
		
		return pslim;
	}
	
	/**
	 * Gets the private buy store limit.
	 * @return the int
	 */
	public int GetPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.dwarf)
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
		
		return pblim;
	}
	
	/**
	 * Gets the freight limit.
	 * @return the int
	 */
	public int GetFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}
	
	/**
	 * Gets the dwarf recipe limit.
	 * @return the int
	 */
	public int GetDwarfRecipeLimit()
	{
		int recdlim = Config.DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	/**
	 * Gets the common recipe limit.
	 * @return the int
	 */
	public int GetCommonRecipeLimit()
	{
		int recclim = Config.COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	/**
	 * Sets the mount object id.
	 * @param newID the new mount object id
	 */
	public void setMountObjectID(final int newID)
	{
		_mountObjectID = newID;
	}
	
	/**
	 * Gets the mount object id.
	 * @return the mount object id
	 */
	public int getMountObjectID()
	{
		return _mountObjectID;
	}
	
	/** The _lure. */
	private L2ItemInstance _lure = null;
	
	/**
	 * Get the current skill in use or return null.<BR>
	 * <BR>
	 * @return the current skill
	 */
	public SkillDat getCurrentSkill()
	{
		return _currentSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player _currentSkill.<BR>
	 * <BR>
	 * @param currentSkill the current skill
	 * @param ctrlPressed the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setCurrentSkill(final L2Skill currentSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Setting current skill: NULL for " + getName() + ".");
			}
			
			_currentSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		}
		
		_currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the queued skill.
	 * @return the queued skill
	 */
	public SkillDat getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	/**
	 * Create a new SkillDat object and queue it in the player _queuedSkill.<BR>
	 * <BR>
	 * @param queuedSkill the queued skill
	 * @param ctrlPressed the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setQueuedSkill(final L2Skill queuedSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Setting queued skill: NULL for " + getName() + ".");
			}
			
			_queuedSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " + getName() + ".");
		}
		
		_queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the power grade.
	 * @return the power grade
	 */
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	/**
	 * Sets the power grade.
	 * @param power the new power grade
	 */
	public void setPowerGrade(final int power)
	{
		_powerGrade = power;
	}
	
	/**
	 * Checks if is cursed weapon equiped.
	 * @return true, if is cursed weapon equiped
	 */
	public boolean isCursedWeaponEquiped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	/**
	 * Sets the cursed weapon equiped id.
	 * @param value the new cursed weapon equiped id
	 */
	public void setCursedWeaponEquipedId(final int value)
	{
		_cursedWeaponEquipedId = value;
	}
	
	/**
	 * Gets the cursed weapon equiped id.
	 * @return the cursed weapon equiped id
	 */
	public int getCursedWeaponEquipedId()
	{
		return _cursedWeaponEquipedId;
	}
	
	/** The _charm of courage. */
	private boolean _charmOfCourage = false;
	
	/**
	 * Gets the charm of courage.
	 * @return the charm of courage
	 */
	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}
	
	/**
	 * Sets the charm of courage.
	 * @param val the new charm of courage
	 */
	public void setCharmOfCourage(final boolean val)
	{
		_charmOfCourage = val;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Gets the death penalty buff level.
	 * @return the death penalty buff level
	 */
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	/**
	 * Sets the death penalty buff level.
	 * @param level the new death penalty buff level
	 */
	public void setDeathPenaltyBuffLevel(final int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	/**
	 * Calculate death penalty buff level.
	 * @param killer the killer
	 */
	public void calculateDeathPenaltyBuffLevel(final L2Character killer)
	{
		if (Rnd.get(100) <= Config.DEATH_PENALTY_CHANCE && !(killer instanceof L2PcInstance) && !isGM() && !(getCharmOfLuck() && (killer instanceof L2GrandBossInstance || killer instanceof L2RaidBossInstance)) && !(isInsideZone(L2Character.ZONE_PVP) || isInsideZone(L2Character.ZONE_SIEGE)))
		{
			increaseDeathPenaltyBuffLevel();
		}
	}
	
	/**
	 * Increase death penalty buff level.
	 */
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15) // maximum level reached
			return;
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null)
			{
				removeSkill(skill, true);
				skill = null;
			}
		}
		
		_deathPenaltyBuffLevel++;
		
		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
		sm = null;
		sendSkillList();
	}
	
	/**
	 * Reduce death penalty buff level.
	 */
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
			skill = null;
			sendSkillList();
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
			sm = null;
			sendSkillList();
		}
		else
		{
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(new SystemMessage(SystemMessageId.DEATH_PENALTY_LIFTED));
		}
	}
	
	/**
	 * restore all Custom Data hero/noble/donator.
	 */
	public void restoreCustomStatus()
	{
		if (Config.DEVELOPER)
		{
			LOGGER.info("Restoring character status " + getName() + " from database...");
		}
		
		int hero = 0;
		int noble = 0;
		int donator = 0;
		long hero_end = 0;
		
		Connection con = null;
		
		try
		{
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(STATUS_DATA_GET);
			statement.setInt(1, getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				hero = rset.getInt("hero");
				noble = rset.getInt("noble");
				donator = rset.getInt("donator");
				hero_end = rset.getLong("hero_end_date");
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			statement = null;
			rset = null;
			
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Error: could not restore char custom data info: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		if (hero > 0 && (hero_end == 0 || hero_end > System.currentTimeMillis()))
		{
			setHero(true);
		}
		else
		{
			// delete wings of destiny
			destroyItem("HeroEnd", 6842, 1, null, false);
		}
		
		if (noble > 0)
		{
			setNoble(true);
		}
		
		if (donator > 0)
		{
			setDonator(true);
		}
	}
	
	/**
	 * Restore death penalty buff level.
	 */
	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
			skill = null;
		}
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
			sm = null;
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/** The Reuse time stamps. */
	private final FastMap<Integer, TimeStamp> ReuseTimeStamps = new FastMap<Integer, TimeStamp>().shared();
	
	/**
	 * Simple class containing all neccessary information to maintain valid timestamps and reuse for skills upon relog. Filter this carefully as it becomes redundant to store reuse for small delays.
	 * @author Yesod public class TimeStamp { private int skill; private long reuse; private Date stamp; public TimeStamp(int _skill, long _reuse) { skill = _skill; reuse = _reuse; stamp = new Date(new Date().getTime() + reuse); } public int getSkill() { return skill; } public long getReuse() {
	 *         return reuse; } public boolean hasNotPassed() { Date d = new Date(); if(d.before(stamp)) { reuse -= d.getTime() - (stamp.getTime() - reuse); return true; } return false; } }
	 */
	
	public static class TimeStamp
	{
		
		public long getStamp()
		{
			return stamp;
		}
		
		public L2Skill getSkill()
		{
			return skill;
		}
		
		public long getReuse()
		{
			return reuse;
		}
		
		public long getRemaining()
		{
			return Math.max(stamp - System.currentTimeMillis(), 0L);
		}
		
		protected boolean hasNotPassed()
		{
			return System.currentTimeMillis() < stamp;
		}
		
		private final L2Skill skill;
		private final long reuse;
		private final long stamp;
		
		protected TimeStamp(final L2Skill _skill, final long _reuse)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = System.currentTimeMillis() + reuse;
		}
		
		protected TimeStamp(final L2Skill _skill, final long _reuse, final long _systime)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = _systime;
		}
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param s the s
	 * @param r the r
	 */
	@Override
	public void addTimeStamp(final L2Skill s, final int r)
	{
		ReuseTimeStamps.put(s.getReuseHashCode(), new TimeStamp(s, r));
	}
	
	/**
	 * Index according to skill this TimeStamp instance for restoration purposes only.
	 * @param T the t
	 */
	private void addTimeStamp(final TimeStamp T)
	{
		ReuseTimeStamps.put(T.getSkill().getId(), T);
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param s the s
	 */
	@Override
	public void removeTimeStamp(final L2Skill s)
	{
		ReuseTimeStamps.remove(s.getReuseHashCode());
	}
	
	public Collection<TimeStamp> getReuseTimeStamps()
	{
		return ReuseTimeStamps.values();
	}
	
	public void resetSkillTime(final boolean ssl)
	{
		final L2Skill arr$[] = getAllSkills();
		for (final L2Skill skill : arr$)
		{
			if (skill != null && skill.isActive() && skill.getId() != 1324)
				enableSkill(skill);
		}
		
		if (ssl)
			sendSkillList();
		sendPacket(new SkillCoolTime(this));
	}
	
	/*
	 * public boolean isInDangerArea() { return isInDangerArea; } public void enterDangerArea() { L2Skill skill = SkillTable.getInstance().getInfo(4268, 1); if(skill != null) { removeSkill(skill, true); skill = null; } addSkill(skill, false); isInDangerArea = true; sendPacket(new
	 * EtcStatusUpdate(this)); SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); sm.addString("You have entered a danger area"); sendPacket(sm); sm = null; } public void exitDangerArea() { L2Skill skill = SkillTable.getInstance().getInfo(4268, 1); if(skill != null) { removeSkill(skill,
	 * true); skill = null; } isInDangerArea = false; sendPacket(new EtcStatusUpdate(this)); SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2); sm.addString("You have left a danger area"); sendPacket(sm); sm = null; }
	 */
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#sendDamageMessage(com.l2jfrozen.gameserver.model.L2Character, int, boolean, boolean, boolean)
	 */
	@Override
	public final void sendDamageMessage(final L2Character target, final int damage, final boolean mcrit, final boolean pcrit, final boolean miss)
	{
		// Check if hit is missed
		if (miss)
		{
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
			return;
		}
		
		// Check if hit is critical
		if (pcrit)
		{
			sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
			
		}
		
		if (mcrit)
		{
			sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
			
		}
		
		if (isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == getOlympiadGameId())
		{
			Olympiad.getInstance().notifyCompetitorDamage(this, damage, getOlympiadGameId());
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
		sm.addNumber(damage);
		sendPacket(sm);
		sm = null;
	}
	
	/**
	 * Update title.
	 */
	public void updateTitle()
	{
		setTitle(Config.PVP_TITLE_PREFIX + getPvpKills() + Config.PK_TITLE_PREFIX + getPkKills() + " ");
	}
	
	/**
	 * Return true if last request is expired.
	 * @return true, if is request expired
	 */
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getGameTicks());
	}
	
	/** The _gm status. */
	boolean _gmStatus = true; // true by default sincce this is used by GMS
	
	// private Object _BanChatTask;
	
	// private long _banchat_timer;
	
	/**
	 * Sets the gm status active.
	 * @param state the new gm status active
	 */
	public void setGmStatusActive(final boolean state)
	{
		_gmStatus = state;
	}
	
	/**
	 * Checks for gm status active.
	 * @return true, if successful
	 */
	public boolean hasGmStatusActive()
	{
		return _gmStatus;
	}
	
	/*
	 * ////////////////////////////////////////////////////////////////// //START CHAT BAN SYSTEM ////////////////////////////////////////////////////////////////// public void setChatBanTimer(long time) { _chatBanTimer = time; } private void updateChatBanState() { if(_chatBanTimer > 0L) {
	 * _chatBanned = true; _chatBanTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChatBanTask(this), _chatBanTimer); sendPacket(new EtcStatusUpdate(this)); } } public void stopChatBanTask(boolean save) { if(_chatBanTask != null) { if(save) { long delay =
	 * _chatBanTask.getDelay(TimeUnit.MILLISECONDS); if(delay < 0L) { delay = 0L; } setChatBanTimer(delay); } _chatBanTask.cancel(false); _chatBanned = false; _chatBanTask = null; sendPacket(new EtcStatusUpdate(this)); } } public void setChatBanned(boolean state, long delayInSec) { _chatBanned =
	 * state; _chatBanTimer = 0L; stopChatBanTask(false); if(_chatBanned && delayInSec > 0) { _chatBanTimer = delayInSec; _chatBanTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChatBanTask(this), _chatBanTimer);
	 * sendMessage("\u0412\u0430\u0448 \u0447\u0430\u0442 \u0437\u0430\u0431\u0430\u043D\u0435\u043D \u043D\u0430 " + _chatBanTimer / 60 / 1000 + " \u043C\u0438\u043D\u0443\u0442."); sendPacket(new EtcStatusUpdate(this)); } storeCharBase(); } public long getChatBanTimer() { if(_chatBanned &&
	 * _chatBanTask!=null) { long delay = _chatBanTask.getDelay(TimeUnit.MILLISECONDS); if(delay >= 0L) { _chatBanTimer = delay; } } return _chatBanTimer; } private class ChatBanTask implements Runnable { L2PcInstance _player; //protected long _startedAt; protected ChatBanTask(L2PcInstance player) {
	 * _player = player; //_startedAt = System.currentTimeMillis(); } public void run() { _player.setChatBanned(false, 0); } }
	 */
	// ////////////////////////////////////////////////////////////////
	// END CHAT BAN SYSTEM
	// ////////////////////////////////////////////////////////////////
	
	/** The _saymode. */
	public L2Object _saymode = null;
	
	/**
	 * Gets the say mode.
	 * @return the say mode
	 */
	public L2Object getSayMode()
	{
		return _saymode;
	}
	
	/**
	 * Sets the say mode.
	 * @param say the new say mode
	 */
	public void setSayMode(final L2Object say)
	{
		_saymode = say;
	}
	
	/**
	 * Save event stats.
	 */
	public void saveEventStats()
	{
		_originalNameColor = getAppearance().getNameColor();
		_originalKarma = getKarma();
		_eventKills = 0;
	}
	
	/**
	 * Restore event stats.
	 */
	public void restoreEventStats()
	{
		getAppearance().setNameColor(_originalNameColor);
		setKarma(_originalKarma);
		_eventKills = 0;
	}
	
	/**
	 * Gets the current skill world position.
	 * @return the current skill world position
	 */
	public Point3D getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	/**
	 * Sets the current skill world position.
	 * @param worldPosition the new current skill world position
	 */
	public void setCurrentSkillWorldPosition(final Point3D worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}
	
	// //////////////////////////////////////////////
	/**
	 * Checks if is cursed weapon equipped.
	 * @return true, if is cursed weapon equipped
	 */
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	// public void setCombatFlagEquipped(boolean value)
	// {
	// _combatFlagEquippedId = value;
	// }
	
	/**
	 * Dismount.
	 * @return true, if successful
	 */
	public boolean dismount()
	{
		if (setMountType(0))
		{
			if (isFlying())
			{
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			}
			
			Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
			broadcastPacket(dismount);
			dismount = null;
			setMountObjectID(0);
			
			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the pc bang score.
	 * @return the pc bang score
	 */
	public int getPcBangScore()
	{
		return pcBangPoint;
	}
	
	/**
	 * Reduce pc bang score.
	 * @param to the to
	 */
	public void reducePcBangScore(final int to)
	{
		pcBangPoint -= to;
		updatePcBangWnd(to, false, false);
	}
	
	/**
	 * Adds the pc bang score.
	 * @param to the to
	 */
	public void addPcBangScore(final int to)
	{
		pcBangPoint += to;
	}
	
	/**
	 * Update pc bang wnd.
	 * @param score the score
	 * @param add the add
	 * @param duble the duble
	 */
	public void updatePcBangWnd(final int score, final boolean add, final boolean duble)
	{
		final ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, score, add, 24, duble);
		sendPacket(wnd);
	}
	
	/**
	 * Show pc bang window.
	 */
	public void showPcBangWindow()
	{
		final ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, 0, false, 24, false);
		sendPacket(wnd);
	}
	
	/**
	 * String to hex.
	 * @param color the color
	 * @return the string
	 */
	private String StringToHex(String color)
	{
		switch (color.length())
		{
			case 1:
				color = new StringBuilder().append("00000").append(color).toString();
				break;
			
			case 2:
				color = new StringBuilder().append("0000").append(color).toString();
				break;
			
			case 3:
				color = new StringBuilder().append("000").append(color).toString();
				break;
			
			case 4:
				color = new StringBuilder().append("00").append(color).toString();
				break;
			
			case 5:
				color = new StringBuilder().append('0').append(color).toString();
				break;
		}
		return color;
	}
	
	/**
	 * Checks if is offline.
	 * @return true, if is offline
	 */
	public boolean isInOfflineMode()
	{
		return _isInOfflineMode;
	}
	
	/**
	 * Sets the offline.
	 * @param set the new offline
	 */
	public void setOfflineMode(final boolean set)
	{
		_isInOfflineMode = set;
	}
	
	/**
	 * Checks if is trade disabled.
	 * @return true, if is trade disabled
	 */
	public boolean isTradeDisabled()
	{
		return _isTradeOff || isCastingNow();
	}
	
	/**
	 * Sets the trade disabled.
	 * @param set the new trade disabled
	 */
	public void setTradeDisabled(final boolean set)
	{
		_isTradeOff = set;
	}
	
	/**
	 * Show teleport html.
	 */
	public void showTeleportHtml()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title></title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Your party leader, " + getParty().getLeader().getName() + ", requested a group teleport to raidboss. You have 30 seconds from this popup to teleport, or the teleport windows will close</td></tr></table><br>");
		text.append("<a action=\"bypass -h rbAnswear\">Port with my party</a><br>");
		text.append("<a action=\"bypass -h rbAnswearDenied\">Don't port</a><br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/** The Dropzor. */
	String Dropzor = "Coin of Luck";
	
	/**
	 * Show raidboss info level40.
	 */
	public void showRaidbossInfoLevel40()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (40-45)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Leto Chief Talkin (40)<br1>");
		text.append("Water Spirit Lian (40) <br1>");
		text.append("Shaman King Selu (40) <br1>");
		text.append("Gwindorr (40) <br1>");
		text.append("Icarus Sample 1 (40) <br1>");
		text.append("Fafurion's Page Sika (40) <br1>");
		text.append("Nakondas (40) <br1>");
		text.append("Road Scavenger Leader (40)<br1>");
		text.append("Wizard of Storm Teruk (40) <br1>");
		text.append("Water Couatle Ateka (40)<br1>");
		text.append("Crazy Mechanic Golem (43) <br1>");
		text.append("Earth Protector Panathen (43) <br1>");
		text.append("Thief Kelbar (44) <br1>");
		text.append("Timak Orc Chief Ranger (44) <br1>");
		text.append("Rotten Tree Repiro (44) <br1>");
		text.append("Dread Avenger Kraven (44) <br1>");
		text.append("Biconne of Blue Sky (45)<br1>");
		text.append("Evil Spirit Cyrion (45) <br1>");
		text.append("Iron Giant Totem (45) <br1>");
		text.append("Timak Orc Gosmos (45) <br1>");
		text.append("Shacram (45) <br1>");
		text.append("Fafurion's Henchman Istary (45) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level45.
	 */
	public void showRaidbossInfoLevel45()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (45-50)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Necrosentinel Royal Guard (47) <br1>");
		text.append("Barion (47) <br1>");
		text.append("Orfen's Handmaiden (48) <br1>");
		text.append("King Tarlk (48) <br1>");
		text.append("Katu Van Leader Atui (49) <br1>");
		text.append("Mirror of Oblivion (49) <br1>");
		text.append("Karte (49) <br1>");
		text.append("Ghost of Peasant Leader (50) <br1>");
		text.append("Cursed Clara (50) <br1>");
		text.append("Carnage Lord Gato (50) <br1>");
		text.append("Fafurion's Henchman Istary (45) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level50.
	 */
	public void showRaidbossInfoLevel50()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (50-55)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Verfa (51) <br1>");
		text.append("Deadman Ereve (51) <br1>");
		text.append("Captain of Red Flag Shaka (52) <br1>");
		text.append("Grave Robber Kim (52) <br1>");
		text.append("Paniel the Unicorn (54) <br1>");
		text.append("Bandit Leader Barda (55) <br1>");
		text.append("Eva's Spirit Niniel (55) <br1>");
		text.append("Beleth's Seer Sephia (55) <br1>");
		text.append("Pagan Watcher Cerberon (55) <br1>");
		text.append("Shaman King Selu (55) <br1>");
		text.append("Black Lily (55) <br1>");
		text.append("Ghost Knight Kabed (55) <br1>");
		text.append("Sorcerer Isirr (55) <br1>");
		text.append("Furious Thieles (55) <br1>");
		text.append("Enchanted Forest Watcher Ruell (55) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level55.
	 */
	public void showRaidbossInfoLevel55()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (55-60)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Fairy Queen Timiniel (56) <br1>");
		text.append("Harit Guardian Garangky (56) <br1>");
		text.append("Refugee Hopeful Leo (56) <br1>");
		text.append("Timak Seer Ragoth (57) <br1>");
		text.append("Soulless Wild Boar (59) <br1>");
		text.append("Abyss Brukunt (59) <br1>");
		text.append("Giant Marpanak (60) <br1>");
		text.append("Ghost of the Well Lidia (60) <br1>");
		text.append("Guardian of the Statue of Giant Karum (60) <br1>");
		text.append("The 3rd Underwater Guardian (60) <br1>");
		text.append("Taik High Prefect Arak (60) <br1>");
		text.append("Ancient Weird Drake (60) <br1>");
		text.append("Lord Ishka (60) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level60.
	 */
	public void showRaidbossInfoLevel60()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (60-65)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Roaring Lord Kastor (62) <br1>");
		text.append("Gorgolos (64) <br1>");
		text.append("Hekaton Prime (65) <br1>");
		text.append("Gargoyle Lord Tiphon (65) <br1>");
		text.append("Fierce Tiger King Angel (65) <br1>");
		text.append("Enmity Ghost Ramdal (65) <br1>");
		text.append("Rahha (65) <br1>");
		text.append("Shilen's Priest Hisilrome (65) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level65.
	 */
	public void showRaidbossInfoLevel65()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (65-70)</title>");
		text.append("<br><br>");
		text.append("<center>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("</center>");
		text.append("Demon's Agent Falston (66) <br1>");
		text.append("Last Titan utenus (66) <br1>");
		text.append("Kernon's Faithful Servant Kelone (67) <br1>");
		text.append("Spirit of Andras, the Betrayer (69) <br1>");
		text.append("Bloody Priest Rudelto (69) <br1>");
		text.append("Shilen's Messenger Cabrio (70) <br1>");
		text.append("Anakim's Nemesis Zakaron (70) <br1>");
		text.append("Flame of Splendor Barakiel (70) <br1>");
		text.append("Roaring Skylancer (70) <br1>");
		text.append("Beast Lord Behemoth (70) <br1>");
		text.append("Palibati Queen Themis (70) <br1>");
		text.append("Fafurion''s Herald Lokness (70) <br1>");
		text.append("Meanas Anor (70) <br1>");
		text.append("Korim (70) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/**
	 * Show raidboss info level70.
	 */
	public void showRaidbossInfoLevel70()
	{
		final TextBuilder text = new TextBuilder();
		text.append("<html>");
		text.append("<body>");
		text.append("<title>Raidboss Level (70-75)</title>");
		text.append("<center>");
		text.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		text.append("</center>");
		text.append("<br><br>");
		text.append("<table width=\"85%\"><tr><td>Drop: " + Dropzor + "</td></tr></table>");
		text.append("Immortal Savior Mardil (71) <br1>");
		text.append("Vanor Chief Kandra (72) <br1>");
		text.append("Water Dragon Seer Sheshark (72) <br1>");
		text.append("Doom Blade Tanatos (72) <br1>");
		text.append("Death Lord Hallate (73) <br1>");
		text.append("Plague Golem (73) <br1>");
		text.append("Icicle Emperor Bumbalump (74) <br1>");
		text.append("Antharas Priest Cloe (74) <br1>");
		text.append("Krokian Padisha Sobekk (74) <br1>");
		text.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"1\">");
		text.append("<font color=\"999999\">Gates of Fire</font></center>");
		text.append("</body>");
		text.append("</html>");
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(text.toString());
		sendPacket(html);
	}
	
	/** The isintwtown. */
	private boolean isintwtown = false;
	
	/**
	 * Checks if is inside tw town.
	 * @return true, if is inside tw town
	 */
	public boolean isInsideTWTown()
	{
		if (isintwtown)
			return true;
		return false;
	}
	
	/**
	 * Sets the inside tw town.
	 * @param b the new inside tw town
	 */
	public void setInsideTWTown(final boolean b)
	{
		isintwtown = true;
	}
	
	/**
	 * check if local player can make multibox and also refresh local boxes instances number.
	 * @return true, if successful
	 */
	public boolean checkMultiBox()
	{
		
		boolean output = true;
		
		int boxes_number = 0; // this one
		final List<String> active_boxes = new ArrayList<>();
		
		if (getClient() != null && getClient().getConnection() != null && !getClient().getConnection().isClosed() && getClient().getConnection().getInetAddress() != null)
		{
			
			final String thisip = getClient().getConnection().getInetAddress().getHostAddress();
			final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
			for (final L2PcInstance player : allPlayers)
			{
				if (player != null)
				{
					if (player.isOnline() == 1 && player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && player.getClient().getConnection().getInetAddress() != null && !player.getName().equals(this.getName()))
					{
						
						final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && this != player)
						{
							if (!Config.ALLOW_DUALBOX)
							{
								
								output = false;
								break;
								
							}
							
							if (boxes_number + 1 > Config.ALLOWED_BOXES)
							{ // actual count+actual player one
								output = false;
								break;
							}
							boxes_number++;
							active_boxes.add(player.getName());
						}
					}
				}
			}
		}
		
		if (output)
		{
			_active_boxes = boxes_number + 1; // current number of boxes+this one
			if (!active_boxes.contains(this.getName()))
			{
				active_boxes.add(this.getName());
				
				this.active_boxes_characters = active_boxes;
			}
			refreshOtherBoxes();
		}
		/*
		 * LOGGER.info("Player "+getName()+" has this boxes"); for(String name:active_boxes_characters){ LOGGER.info("*** "+name+" ***"); }
		 */
		return output;
	}
	
	/**
	 * increase active boxes number for local player and other boxer for same ip.
	 */
	public void refreshOtherBoxes()
	{
		
		if (getClient() != null && getClient().getConnection() != null && !getClient().getConnection().isClosed() && getClient().getConnection().getInetAddress() != null)
		{
			
			final String thisip = getClient().getConnection().getInetAddress().getHostAddress();
			final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
			final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
			
			for (final L2PcInstance player : players)
			{
				if (player != null && player.isOnline() == 1)
				{
					if (player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && !player.getName().equals(this.getName()))
					{
						
						final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && this != player)
						{
							player._active_boxes = _active_boxes;
							player.active_boxes_characters = active_boxes_characters;
							/*
							 * LOGGER.info("Player "+player.getName()+" has this boxes"); for(String name:player.active_boxes_characters){ LOGGER.info("*** "+name+" ***"); }
							 */
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * descrease active boxes number for local player and other boxer for same ip.
	 */
	public void decreaseBoxes()
	{
		
		_active_boxes = _active_boxes - 1;
		active_boxes_characters.remove(this.getName());
		
		refreshOtherBoxes();
		/*
		 * if(getClient()!=null && !getClient().getConnection().isClosed()){ String thisip = getClient().getConnection().getSocketChannel().socket().getInetAddress().getHostAddress(); Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers(); L2PcInstance[] players =
		 * allPlayers.toArray(new L2PcInstance[allPlayers.size()]); for(L2PcInstance player : players) { if(player != null) { if(player.getClient()!=null && !player.getClient().getConnection().isClosed()){ String ip =
		 * player.getClient().getConnection().getSocketChannel().socket().getInetAddress().getHostAddress(); if(thisip.equals(ip) && this != player && player != null) { player._active_boxes = _active_boxes; player.active_boxes_characters = active_boxes_characters;
		 * LOGGER.info("Player "+player.getName()+" has this boxes"); for(String name:player.active_boxes_characters){ LOGGER.info("*** "+name+" ***"); } } } } } }
		 */
		/*
		 * LOGGER.info("Player "+getName()+" has this boxes"); for(String name:active_boxes_characters){ LOGGER.info("*** "+name+" ***"); }
		 */
	}
	
	/**
	 * Aio System Start.
	 * @return true, if is aio
	 */
	public boolean isAio()
	{
		return _isAio;
	}
	
	/**
	 * Sets the aio.
	 * @param val the new aio
	 */
	public void setAio(final boolean val)
	{
		_isAio = val;
		
	}
	
	/**
	 * Reward aio skills.
	 */
	public void rewardAioSkills()
	{
		L2Skill skill;
		for (final Integer skillid : Config.AIO_SKILLS.keySet())
		{
			final int skilllvl = Config.AIO_SKILLS.get(skillid);
			skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			if (skill != null)
			{
				addSkill(skill, true);
			}
		}
		sendMessage("GM give to you Aio's skills");
	}
	
	/**
	 * Lost aio skills.
	 */
	public void lostAioSkills()
	{
		L2Skill skill;
		for (final Integer skillid : Config.AIO_SKILLS.keySet())
		{
			final int skilllvl = Config.AIO_SKILLS.get(skillid);
			skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			removeSkill(skill);
		}
	}
	
	/**
	 * Sets the aio end time.
	 * @param val the new aio end time
	 */
	public void setAioEndTime(final long val)
	{
		_aio_endTime = val;
	}
	
	/**
	 * Sets the end time.
	 * @param process the process
	 * @param val the val
	 */
	public void setEndTime(final String process, int val)
	{
		if (val > 0)
		{
			long end_day;
			final Calendar calendar = Calendar.getInstance();
			if (val >= 30)
			{
				while (val >= 30)
				{
					if (calendar.get(Calendar.MONTH) == 11)
						calendar.roll(Calendar.YEAR, true);
					calendar.roll(Calendar.MONTH, true);
					val -= 30;
				}
			}
			if (val < 30 && val > 0)
			{
				while (val > 0)
				{
					if (calendar.get(Calendar.DATE) == 28 && calendar.get(Calendar.MONTH) == 1)
						calendar.roll(Calendar.MONTH, true);
					if (calendar.get(Calendar.DATE) == 30)
					{
						if (calendar.get(Calendar.MONTH) == 11)
							calendar.roll(Calendar.YEAR, true);
						calendar.roll(Calendar.MONTH, true);
						
					}
					calendar.roll(Calendar.DATE, true);
					val--;
				}
			}
			
			end_day = calendar.getTimeInMillis();
			if (process.equals("aio"))
				_aio_endTime = end_day;
			
			else
			{
				LOGGER.info("process " + process + "no Known while try set end date");
				return;
			}
			final Date dt = new Date(end_day);
			LOGGER.info("" + process + " end time for player " + getName() + " is " + dt);
		}
		else
		{
			if (process.equals("aio"))
				_aio_endTime = 0;
			
			else
			{
				LOGGER.info("process " + process + "no Known while try set end date");
				return;
			}
		}
	}
	
	/**
	 * Gets the aio end time.
	 * @return the aio end time
	 */
	public long getAioEndTime()
	{
		return _aio_endTime;
	}
	
	/**
	 * Gets the offline start time.
	 * @return the offline start time
	 */
	public long getOfflineStartTime()
	{
		return _offlineShopStart;
	}
	
	/**
	 * Sets the offline start time.
	 * @param time the new offline start time
	 */
	public void setOfflineStartTime(final long time)
	{
		_offlineShopStart = time;
	}
	
	// during fall validations will be disabled for 10 ms.
	/** The Constant FALLING_VALIDATION_DELAY. */
	private static final int FALLING_VALIDATION_DELAY = 10000;
	
	/** The _falling timestamp. */
	private long _fallingTimestamp = 0;
	
	/**
	 * Return true if character falling now On the start of fall return false for correct coord sync !.
	 * @param z the z
	 * @return true, if is falling
	 */
	public final boolean isFalling(final int z)
	{
		if (isDead() || isFlying() || isInvul() || isInFunEvent() || isInsideZone(ZONE_WATER))
			return false;
		
		if (System.currentTimeMillis() < _fallingTimestamp)
			return true;
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getFallHeight())
			return false;
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false);
			sendPacket(new SystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}
		
		setFalling();
		
		return false;
	}
	
	/**
	 * Set falling timestamp.
	 */
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	/** Previous coordinate sent to party in ValidatePosition *. */
	private final Point3D _lastPartyPosition = new Point3D(0, 0, 0);
	
	/**
	 * Sets the last party position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastPartyPosition(final int x, final int y, final int z)
	{
		_lastPartyPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Gets the last party position distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last party position distance
	 */
	public int getLastPartyPositionDistance(final int x, final int y, final int z)
	{
		final double dx = (x - _lastPartyPosition.getX());
		final double dy = (y - _lastPartyPosition.getY());
		final double dz = (z - _lastPartyPosition.getZ());
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Checks if is awaying.
	 * @return the _awaying
	 */
	public boolean isAwaying()
	{
		return _awaying;
	}
	
	/**
	 * Sets the _awaying.
	 * @param _awaying the _awaying to set
	 */
	public void set_awaying(final boolean _awaying)
	{
		this._awaying = _awaying;
	}
	
	/**
	 * Checks if is locked.
	 * @return true, if is locked
	 */
	public boolean isLocked()
	{
		return _isLocked;
	}
	
	/**
	 * Sets the locked.
	 * @param a the new locked
	 */
	public void setLocked(final boolean a)
	{
		_isLocked = a;
	}
	
	/**
	 * Checks if is stored.
	 * @return true, if is stored
	 */
	public boolean isStored()
	{
		return _isStored;
	}
	
	/**
	 * Sets the stored.
	 * @param a the new stored
	 */
	public void setStored(final boolean a)
	{
		_isStored = a;
	}
	
	/** The _punish level. */
	private PunishLevel _punishLevel = PunishLevel.NONE;
	
	/** The _punish timer. */
	private long _punishTimer = 0;
	
	/** The _punish task. */
	private ScheduledFuture<?> _punishTask;
	
	/**
	 * The Enum PunishLevel.
	 */
	public enum PunishLevel
	{
		
		/** The NONE. */
		NONE(0, ""),
		
		/** The CHAT. */
		CHAT(1, "chat banned"),
		
		/** The JAIL. */
		JAIL(2, "jailed"),
		
		/** The CHAR. */
		CHAR(3, "banned"),
		
		/** The ACC. */
		ACC(4, "banned");
		
		/** The pun value. */
		private final int punValue;
		
		/** The pun string. */
		private final String punString;
		
		/**
		 * Instantiates a new punish level.
		 * @param value the value
		 * @param string the string
		 */
		PunishLevel(final int value, final String string)
		{
			punValue = value;
			punString = string;
		}
		
		/**
		 * Value.
		 * @return the int
		 */
		public int value()
		{
			return punValue;
		}
		
		/**
		 * String.
		 * @return the string
		 */
		public String string()
		{
			return punString;
		}
	}
	
	// open/close gates
	@SuppressWarnings("synthetic-access")
	private final GatesRequest _gatesRequest = new GatesRequest();
	
	private static class GatesRequest
	{
		private L2DoorInstance _target = null;
		
		public void setTarget(final L2DoorInstance door)
		{
			_target = door;
		}
		
		public L2DoorInstance getDoor()
		{
			return _target;
		}
	}
	
	public void gatesRequest(final L2DoorInstance door)
	{
		_gatesRequest.setTarget(door);
	}
	
	public void gatesAnswer(final int answer, final int type)
	{
		if (_gatesRequest.getDoor() == null)
			return;
		
		if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 1)
			_gatesRequest.getDoor().openMe();
		else if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 0)
			_gatesRequest.getDoor().closeMe();
		
		_gatesRequest.setTarget(null);
	}
	
	/**
	 * returns punishment level of player.
	 * @return the punish level
	 */
	public PunishLevel getPunishLevel()
	{
		return _punishLevel;
	}
	
	/**
	 * Checks if is in jail.
	 * @return True if player is jailed
	 */
	public boolean isInJail()
	{
		return _punishLevel == PunishLevel.JAIL;
	}
	
	/**
	 * Checks if is chat banned.
	 * @return True if player is chat banned
	 */
	public boolean isChatBanned()
	{
		return _punishLevel == PunishLevel.CHAT;
	}
	
	/**
	 * Sets the punish level.
	 * @param state the new punish level
	 */
	public void setPunishLevel(final int state)
	{
		switch (state)
		{
			case 0:
			{
				_punishLevel = PunishLevel.NONE;
				break;
			}
			case 1:
			{
				_punishLevel = PunishLevel.CHAT;
				break;
			}
			case 2:
			{
				_punishLevel = PunishLevel.JAIL;
				break;
			}
			case 3:
			{
				_punishLevel = PunishLevel.CHAR;
				break;
			}
			case 4:
			{
				_punishLevel = PunishLevel.ACC;
				break;
			}
		}
	}
	
	/**
	 * Sets the punish level.
	 * @param state the state
	 * @param delayInMinutes the delay in minutes
	 */
	public void setPunishLevel(final PunishLevel state, final int delayInMinutes)
	{
		final long delayInMilliseconds = delayInMinutes * 60000L;
		setPunishLevel(state, delayInMilliseconds);
		
	}
	
	/**
	 * Sets punish level for player based on delay.
	 * @param state the state
	 * @param delayInMilliseconds 0 - Indefinite
	 */
	public void setPunishLevel(final PunishLevel state, final long delayInMilliseconds)
	{
		switch (state)
		{
			case NONE: // Remove Punishments
			{
				switch (_punishLevel)
				{
					case CHAT:
					{
						_punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Your Chat ban has been lifted");
						break;
					}
					case JAIL:
					{
						_punishLevel = state;
						// Open a Html message to inform the player
						final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
						final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
						if (jailInfos != null)
							htmlMsg.setHtml(jailInfos);
						else
							htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
						sendPacket(htmlMsg);
						stopPunishTask(true);
						teleToLocation(17836, 170178, -3507, true); // Floran
						break;
					}
				}
				break;
			}
			case CHAT: // Chat Ban
			{
				// not allow player to escape jail using chat ban
				if (_punishLevel == PunishLevel.JAIL)
					break;
				_punishLevel = state;
				_punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					final int minutes = (int) (delayInMilliseconds / 60000);
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), _punishTimer);
					sendMessage("You are chat banned for " + minutes + " minutes.");
				}
				else
					sendMessage("You have been chat banned");
				break;
				
			}
			case JAIL: // Jail Player
			{
				_punishLevel = state;
				_punishTimer = 0;
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					_punishTimer = delayInMilliseconds; // Delay in milliseconds
					
					// start the countdown
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), _punishTimer);
					sendMessage("You are in jail for " + delayInMilliseconds / 60000 + " minutes.");
				}
				
				if (_inEventCTF)
				{
					CTF.onDisconnect(this);
				}
				else if (_inEventDM)
				{
					DM.onDisconnect(this);
				}
				else if (_inEventTvT)
				{
					TvT.onDisconnect(this);
				}
				else if (_inEventVIP)
				{
					VIP.onDisconnect(this);
				}
				if (Olympiad.getInstance().isRegisteredInComp(this))
					Olympiad.getInstance().removeDisconnectedCompetitor(this);
				
				// Open a Html message to inform the player
				final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
				final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
				if (jailInfos != null)
					htmlMsg.setHtml(jailInfos);
				else
					htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
				sendPacket(htmlMsg);
				setInstanceId(0);
				setIsIn7sDungeon(false);
				
				teleToLocation(-114356, -249645, -2984, false); // Jail
				break;
			}
			case CHAR: // Ban Character
			{
				setAccessLevel(-100);
				logout();
				break;
			}
			case ACC: // Ban Account
			{
				setAccountAccesslevel(-100);
				logout();
				break;
			}
			default:
			{
				_punishLevel = state;
				break;
			}
		}
		
		// store in database
		storeCharBase();
	}
	
	/**
	 * Gets the punish timer.
	 * @return the punish timer
	 */
	public long getPunishTimer()
	{
		return _punishTimer;
	}
	
	/**
	 * Sets the punish timer.
	 * @param time the new punish timer
	 */
	public void setPunishTimer(final long time)
	{
		_punishTimer = time;
	}
	
	/**
	 * Update punish state.
	 */
	private void updatePunishState()
	{
		if (getPunishLevel() != PunishLevel.NONE)
		{
			// If punish timer exists, restart punishtask.
			if (_punishTimer > 0)
			{
				_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), _punishTimer);
				sendMessage("You are still " + getPunishLevel().string() + " for " + (_punishTimer / 60000) + " minutes.");
			}
			if (getPunishLevel() == PunishLevel.JAIL)
			{
				// If player escaped, put him back in jail
				if (!isInsideZone(ZONE_JAIL))
					teleToLocation(-114356, -249645, -2984, true);
			}
		}
	}
	
	/**
	 * Stop punish task.
	 * @param save the save
	 */
	public void stopPunishTask(final boolean save)
	{
		if (_punishTask != null)
		{
			if (save)
			{
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
					delay = 0;
				setPunishTimer(delay);
			}
			_punishTask.cancel(false);
			ThreadPoolManager.getInstance().removeGeneral((Runnable) _punishTask);
			_punishTask = null;
		}
	}
	
	/**
	 * The Class PunishTask.
	 */
	private class PunishTask implements Runnable
	{
		
		/** The _player. */
		L2PcInstance _player;
		
		// protected long _startedAt;
		
		/**
		 * Instantiates a new punish task.
		 * @param player the player
		 */
		protected PunishTask(final L2PcInstance player)
		{
			_player = player;
			// _startedAt = System.currentTimeMillis();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			_player.setPunishLevel(PunishLevel.NONE, 0);
		}
	}
	
	private final HashMap<Integer, Long> confirmDlgRequests = new HashMap<>();
	
	public void addConfirmDlgRequestTime(final int requestId, final int time)
	
	{
		confirmDlgRequests.put(requestId, System.currentTimeMillis() + time + 2000);
	}
	
	public Long getConfirmDlgRequestTime(final int requestId)
	{
		return confirmDlgRequests.get(requestId);
	}
	
	public void removeConfirmDlgRequestTime(final int requestId)
	{
		confirmDlgRequests.remove(requestId);
	}
	
	/**
	 * Gets the flood protectors.
	 * @return the flood protectors
	 */
	public FloodProtectors getFloodProtectors()
	{
		return getClient().getFloodProtectors();
	}
	
	/**
	 * Test if player inventory is under 80% capaity.
	 * @return true, if is inventory under80
	 */
	public boolean isInventoryUnder80()
	{
		if (getInventory().getSize() <= (getInventoryLimit() * 0.8))
		{
			return true;
		}
		return false;
	}
	
	// Multisell
	/** The _current multi sell id. */
	private int _currentMultiSellId = -1;
	
	/**
	 * Gets the multi sell id.
	 * @return the multi sell id
	 */
	public final int getMultiSellId()
	{
		return _currentMultiSellId;
	}
	
	/**
	 * Sets the multi sell id.
	 * @param listid the new multi sell id
	 */
	public final void setMultiSellId(final int listid)
	{
		_currentMultiSellId = listid;
	}
	
	/**
	 * Checks if is party waiting.
	 * @return true, if is party waiting
	 */
	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	// these values are only stored temporarily
	/** The _partyroom. */
	private int _partyroom = 0;
	
	/**
	 * Sets the party room.
	 * @param id the new party room
	 */
	public void setPartyRoom(final int id)
	{
		_partyroom = id;
	}
	
	/**
	 * Gets the party room.
	 * @return the party room
	 */
	public int getPartyRoom()
	{
		return _partyroom;
	}
	
	/**
	 * Checks if is in party match room.
	 * @return true, if is in party match room
	 */
	public boolean isInPartyMatchRoom()
	{
		return _partyroom > 0;
	}
	
	/**
	 * Checks if is item equipped by item id.
	 * @param item_id the item_id
	 * @return true, if is item equipped by item id
	 */
	public boolean isItemEquippedByItemId(final int item_id)
	{
		if (_inventory == null)
			return false;
		
		if (_inventory.getAllItemsByItemId(item_id) == null || _inventory.getAllItemsByItemId(item_id).length == 0)
			return false;
		
		return _inventory.checkIfEquipped(item_id);
	}
	
	/**
	 * Gets the _instance login time.
	 * @return the _instanceLoginTime
	 */
	public long get_instanceLoginTime()
	{
		return _instanceLoginTime;
	}
	
	/**
	 * Sets the sex db.
	 * @param player the player
	 * @param mode the mode
	 */
	public static void setSexDB(final L2PcInstance player, final int mode)
	{
		Connection con;
		if (player == null)
			return;
		con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET sex=? WHERE obj_Id=?");
			statement.setInt(1, player.getAppearance().getSex() ? 1 : 0);
			statement.setInt(2, player.getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("SetSex:  Could not store data:" + e);
		}
		finally
		{
			CloseUtil.close(con);
			
		}
	}
	
	public boolean checkTeleportOverTime()
	{
		
		if (!isTeleporting())
			return false;
		
		if (System.currentTimeMillis() - _lastTeleportAction > Config.CHECK_TELEPORT_ZOMBIE_DELAY_TIME)
		{
			
			LOGGER.warn("Player " + getName() + " has been in teleport more then " + Config.CHECK_TELEPORT_ZOMBIE_DELAY_TIME / 1000 + " seconds.. --> Kicking it");
			
			return true;
			
		}
		
		return false;
		
	}
	
	@Override
	public void setIsTeleporting(final boolean value)
	{
		super.setIsTeleporting(value);
		if (value)
		{
			_lastTeleportAction = System.currentTimeMillis();
		}
		
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return this;
	}
	
	public void sendBlockList()
	{
		sendMessage("======<Ignore List>======");
		
		int i = 1;
		final Iterator<String> blockListIt = getBlockList().getBlockList().iterator();
		while (blockListIt.hasNext())
		{
			final String playerId = blockListIt.next();
			sendMessage((new StringBuilder()).append(i++).append(". ").append(playerId).toString());
			
		}
		
		sendMessage("========================");
		
	}
	
	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}
	
	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			final L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if (equippedItem != null && !equippedItem.checkOlympCondition())
			{
				if (equippedItem.isAugmented())
					equippedItem.getAugmentation().removeBoni(this);
				final L2ItemInstance[] items = getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
					continue;
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem.getItemId());
				}
				sendPacket(sm);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				sendPacket(iu);
				broadcastUserInfo();
			}
		}
	}
	
	public void enterOlympiadObserverMode(final int x, final int y, final int z, final int id, final boolean storeCoords)
	{
		if (getPet() != null)
			getPet().unSummon(this);
		
		unsummonAllCubics();
		
		_olympiadGameId = id;
		if (isSitting())
			standUp();
		if (storeCoords)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		// sendPacket(new GMHide(1));
		teleToLocation(x, y, z, true);
		sendPacket(new ExOlympiadMode(3, this));
		_observerMode = true;
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode(final boolean olymp)
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(_obsX, _obsY, _obsZ, true);
		if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invis", getAccessLevel()))
			getAppearance().setVisible();
		if (!AdminCommandAccessRights.getInstance().hasAccess("admin_invul", getAccessLevel()))
			setIsInvul(false);
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		if (!olymp)
			Olympiad.removeSpectator(_olympiadGameId, this);
		_olympiadGameId = -1;
		_observerMode = false;
		broadcastUserInfo();
	}
	
	public void setHero(final boolean hero)
	{
		_hero = hero;
		if (_hero && _baseClass == _activeClass)
		{
			giveHeroSkills();
		}
		else if (getCount() >= Config.HERO_COUNT && _hero && Config.ALLOW_HERO_SUBSKILL)
		{
			giveHeroSkills();
		}
		else
		{
			removeHeroSkills();
		}
	}
	
	public void giveHeroSkills()
	{
		for (final L2Skill s : HeroSkillTable.getHeroSkills())
		{
			addSkill(s, false); // Dont Save Hero skills to database
		}
		sendSkillList();
	}
	
	public void removeHeroSkills()
	{
		for (final L2Skill s : HeroSkillTable.getHeroSkills())
		{
			super.removeSkill(s); // Just Remove skills from nonHero characters
		}
		sendSkillList();
	}
	
	/**
	 * Get the current pet skill in use or return null.<br>
	 * <br>
	 * @return
	 */
	public SkillDat getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player _currentPetSkill.<br>
	 * <br>
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentPetSkill(final L2Skill currentSkill, final boolean ctrlPressed, final boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
				LOGGER.info("Setting current pet skill: NULL for " + getName() + ".");
			
			_currentPetSkill = null;
			return;
		}
		
		if (Config.DEBUG)
			LOGGER.info("Setting current Pet skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		
		_currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
}