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
package interlude;

import interlude.gameserver.util.FloodProtectorConfig;
import interlude.gameserver.util.StringUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import java.util.Properties;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

public final class Config
{
	protected static final Logger _log = Logger.getLogger(Config.class.getName());
	// Configuration files
	public static final String CONFIGURATION_FILE = "./config/system/Server.ini";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/system/LoginServer.ini";
	public static final String ALT_SETTINGS_FILE = "./config/main/AltSettings.ini";
	public static final String CLAN_SETTINGS_FILE = "./config/main/Clan.ini";
	public static final String CLANHALL_CONFIG_FILE = "./config/main/ClanHall.ini";
	public static final String COMMAND_PRIVILEGES_FILE = "./config/system/Command-Privileges.ini";
	public static final String FLOODPROTECTOR_CONFIG_FILE = "./config/system/FloodProtector.ini";
	public static final String GM_ACCESS_FILE = "./config/system/GMAccess.ini";
	public static final String ENCHANT_CONFIG_FILE = "./config/main/Enchant.ini";
	public static final String ID_CONFIG_FILE = "./config/system/IDFactory.ini";
	public static final String OPTIONS_FILE = "./config/system/Options.ini";
	public static final String OTHER_CONFIG_FILE = "./config/custom/Other.ini";
	public static final String RATES_CONFIG_FILE = "./config/main/Rates.ini";
	public static final String PVP_CONFIG_FILE = "./config/main/PvP.ini";
	public static final String TELNET_FILE = "./config/system/Telnet.ini";
	public static final String MMO_CONFIG_FILE = "./config/system/MMO.ini";
	public static final String SERVER_VERSION_FILE = "./config/L2Open-version.ini";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/Siege.ini";
	public static final String HEXID_FILE = "./config/system//hexid.txt";
	public static final String CHAT_FILTER_FILE = "./config/system/ChatFilter.txt";
	public static final String SEVENSIGNS_FILE = "./config/main/SevenSigns.ini";
	public static final String FS_CONFIG_FILE = "./config/main/FourSepulchers.ini";
	public static final String OPEN_CUSTOM = "./config/custom/OpenCustom.ini";
	public static final String OPEN_MODS = "./config/custom/OpenMods.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/custom/Feature.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/custom/General.ini";
	public static final String BALANCE_CONFIG_FILE = "./config/custom/BalanceClasses.ini";
	public static final String CUSTOM_TABLES_FILE = "./config/custom/CustomTables.ini";
	public static final String OLYMPIAD_FILE = "./config/main/Olympiad.ini";
	public static final String AUGMENT_CONFIG_FILE = "./config/main/Augment.ini";
	public static final String DEV_CONFIG_FILE = "./config/system/Dev.ini";

	// Server and Datapack version
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	public static int MAX_ITEM_IN_PACKET;
	
	// FloodProtector configs
	public static final FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_UNK_PACKETS = new FloodProtectorConfig("UnkPacketsFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_BUFFER = new FloodProtectorConfig("BufferFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_CRAFT = new FloodProtectorConfig("CraftFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_BANKING_SYSTEM = new FloodProtectorConfig("BankingSystemFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_WEREHOUSE = new FloodProtectorConfig("WerehouseFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_MISC = new FloodProtectorConfig("MiscFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_CHAT = new FloodProtectorConfig("ChatFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL = new FloodProtectorConfig("GlobalFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_TRADE = new FloodProtectorConfig("TradeFloodProtector");
	public static final FloodProtectorConfig FLOOD_PROTECTOR_POTION = new FloodProtectorConfig("PotionFloodProtector");

	// Auto loots configs
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAID;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LEARN_SKILLS;
    public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static double ALT_WEIGHT_LIMIT;
	public static boolean ALT_RECOMMEND;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static boolean ALT_GAME_MOB_ATTACK_AI;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static boolean ALT_GAME_TIREDNESS;

	// Karma Punishment
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	
	// Crafting and Recipes Configs
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	
	// Balance Classes START
	public static boolean ENABLE_BALANCE;
	public static float FIGHT_P_DMG;
	public static float KNIGHT_P_DMG;
	public static float ROGUE_P_DMG;
	public static float MAGE_INI_P_DMG;
	public static float WIZARD_P_DMG;
	public static float FIGHT_M_DMG;
	public static float KNIGHT_M_DMG;
	public static float ROGUE_M_DMG;
	public static float MAGE_INI_M_DMG;
	public static float WIZARD_M_DMG;
	public static float DAGGER_P_DMG;
	public static float ARCHER_P_DMG;
	public static float TANKER_P_DMG;
	public static float DUAL_P_DMG;
	public static float POLE_P_DMG;
	public static float MAGE_P_DMG;
	public static float ORC_MONK_P_DMG;
	public static float ORC_RAIDER_P_DMG;
	public static float DWARF_P_DMG;
	public static float DAGGER_M_DMG;
	public static float ARCHER_M_DMG;
	public static float TANKER_M_DMG;
	public static float DUAL_M_DMG;
	public static float POLE_M_DMG;
	public static float MAGE_M_DMG;
	public static float ORC_MONK_M_DMG;
	public static float ORC_RAIDER_M_DMG;
	public static float DWARF_M_DMG;
	public static boolean ALLOW_DAGGERS_WEAR_HEAVY;
	public static boolean ALLOW_ARCHERS_WEAR_HEAVY;

	// multiples damages Pet's and Mobs
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
    // MeGaPk Options
    //.cl
    public static boolean ALLOW_CL; 
    public static int     ClItemId;
    public static int     ClItemCount;
    public static String  ClItemName;
    // .shop
    public static boolean ALLOW_SHOP_COMMAND;
    public static String MULTISELL_VOICE;
    public static FastList<Integer> MULTISELL_VOICE_LIST = new FastList<Integer>();
    // use .loc
    public static boolean ALLOW_LOC;
    public static boolean ALLOW_LOC_RB;
    // use .loc town
    public static int     LOC_ITEM_ID;
    public static int     LOC_ITEM_COUNT;
    public static String  LOC_ITEM_NAME;
    // use .loc epic
    public static int     LOCRB_ITEM_ID;
    public static int     LOCRB_ITEM_COUNT;
    public static String  LOCRB_ITEM_NAME;

    public static boolean WareHouse;
    public static boolean REM_BUFF;
    public static boolean CS_PVP_SYSTEM;
    public static boolean Stat;
    public static boolean StartNoble;
    public static boolean StartHero;
    public static boolean ALLOW_INFO_VOICE_COMMAND;
    public static boolean ALLOW_BUFF;
    public static byte MAX_LEVEL;
    public static byte SubLevel;
    // .res
    public static boolean RES;
    public static int RES_CMD_CONSUME_ID;
    public static String RES_ITEM_NAME;
    // .buyrec
    public static boolean BuyRec;
    public static int REC_ITEM_ID;
    public static int REC_ITEM_COUNT;
    public static int REC_REWARD;

	// V.C.
	public static boolean ALLOW_CFG_VOICE_COMMAND;
	
	// Allow use Event Managers for change occupation ?
	public static boolean ALLOW_CLASS_MASTERS;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean RESTORE_EFFECTS_ON_SUBCLASS_CHANGE;
	public static byte BUFFS_MAX_AMOUNT;
	
	// Clan and Ally configs
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	
	// Olympiad time configs
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_BWAIT;
	public static long ALT_OLY_IWAIT;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static String ALT_OLY_RESTRICTED_ITEMS;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new FastList<Integer>();
	public static boolean ALT_OLY_SAME_IP;
	
	// Lottery configs
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	
	// Chat Filter Configs
	public static int     CHAT_FILTER_PUNISHMENT_PARAM1;
	public static int     CHAT_FILTER_PUNISHMENT_PARAM2;
	public static boolean USE_SAY_FILTER;
	public static String  CHAT_FILTER_CHARS;
	public static String  CHAT_FILTER_PUNISHMENT;
	public static ArrayList<String> FILTER_LIST = new ArrayList<String>();
	
	// For development
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;

	// Clan Hall function related configs
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;

	// GM access level
	public static int GM_ACCESSLEVEL;
	public static int GM_MIN;
	public static int GM_ALTG_MIN_LEVEL;
	public static int GM_ANNOUNCE;
	public static int GM_BAN;
	public static int GM_BAN_CHAT;
	public static int GM_CREATE_ITEM;
	public static int GM_DELETE;
	public static int GM_KICK;
	public static int GM_MENU;
	public static int GM_GODMODE;
	public static int GM_CHAR_EDIT;
	public static int GM_CHAR_EDIT_OTHER;
	public static int GM_CHAR_VIEW;
	public static int GM_NPC_EDIT;
	public static int GM_NPC_VIEW;
	public static int GM_PRIV_EDIT;
	public static int GM_PRIV_VIEW;
	public static int GM_TELEPORT;
	public static int GM_TELEPORT_OTHER;
	public static int GM_RESTART;
	public static int GM_MONSTERRACE;
	public static int GM_RIDER;
	public static int GM_ESCAPE;
	public static int GM_FIXED;
	public static int GM_CREATE_NODES;
	public static int GM_ENCHANT;
	public static int GM_DOOR;
	public static int GM_RES;
	public static int GM_PEACEATTACK;
	public static int GM_HEAL;
	public static int GM_UNBLOCK;
	public static int GM_CACHE;
	public static int GM_TALK_BLOCK;
	public static int GM_TEST;
	public static int GM_FORTSIEGE;
	public static int GM_CLAN_PANEL;
	public static int GM_REPAIR = 75;
	
	// Disable transaction
	public static boolean GM_DISABLE_TRANSACTION;
	public static int GM_TRANSACTION_MIN;
	public static int GM_TRANSACTION_MAX;
	public static int GM_CAN_GIVE_DAMAGE;
	public static int GM_DONT_TAKE_EXPSP;
	public static int GM_DONT_TAKE_AGGRO;
    public static boolean OLOLO;

	public static enum ObjectMapType
	{
		L2ObjectHashMap, WorldObjectMap
	}

	public static enum ObjectSetType
	{
		L2ObjectHashSet, WorldObjectSet
	}

	public static enum IdFactoryType
	{
		Compaction, BitSet, Stack
	}

	// ID Factory type and Check for bad ID
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	// Type of map and set object
	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;

	// Debug/ assertions / code 'in progress' mode
	public static boolean DEBUG;
	public static boolean ASSERT;
	public static boolean DEVELOPER;
	
	// Setting for serverList
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_GMONLY;
	
	// Set if this server is a test server used for development
	public static boolean TEST_SERVER;
	
	// For test servers - everybody has admin rights
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	
	// Global and Trade chat state
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	
	// Default punishment for illegal actions
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;

	public static int COORD_SYNCHRONIZE;
	public static int ZONE_TOWN;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static int DELETE_DAYS;
	public static int FLOODPROTECTOR_INITIALSIZE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean LAZY_CACHE;
	public static int MAX_DRIFT_RANGE;
	public static boolean ACTIVATE_POSITION_RECORDER;
	public static int PACKET_LIFETIME;
	public static String PROTECTED_ITEMS;
	public static List<Integer> LIST_PROTECTED_ITEMS = new FastList<Integer>();
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static boolean SHOW_NPC_LVL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_FISHING;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_FREIGHT;
	
	// Logging Chat and Item Window
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean LOG_TRADES;
	public static boolean LOG_PDAM;
	public static boolean LOG_MDAM;
	
	// Community Board
	public static String COMMUNITY_TYPE;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	
	// Thread pools size
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	
	// Grid Options
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	
	// GeoData Options
	public static int GEODATA;
	public static boolean FORCE_GEODATA;
    public static boolean MOVE_BASED_KNOWNLIST;
    public static long KNOWNLIST_UPDATE_INTERVAL;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static String PATHFIND_BUFFERS;
	public static boolean DEBUG_PATH;
	public static int MAX_POSTFILTER_PASSES;
	
	// buff multiplayer
	public static float BUFF_TIME_MULTIPLIER;
	public static float SPIRIT_TIME_MULTIPLIER;
	public static float DANCE_TIME_MULTIPLIER;


	// Amount of adenas when starting a new character
	public static int STARTING_ADENA;
	public static int STARTING_AA;
	public static byte STARTING_LEVEL;
	
	// Pets
	public static int WYVERN_SPEED;
	public static int STRIDER_SPEED;
	public static boolean ALLOW_WYVERN_UPGRADER;

	public static boolean EFFECT_CANCELING;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	
	// Character multipliers
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	
	// Raid Boss multipliers
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_DEFENCE_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	
	public static int UNSTUCK_INTERVAL;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;

	public static String PARTY_XP_CUTOFF_METHOD;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static boolean STORE_SKILL_COOLTIME;
	public static String PET_RENT_NPC;
	public static List<Integer> LIST_PET_RENT_NPC = new FastList<Integer>();
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_PRIVILEGES_ADMIN;
	public static boolean ALT_PRIVILEGES_SECURE_CHECK;
	public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
	public static boolean GM_NAME_COLOR_ENABLED;
	public static int GM_NAME_COLOR;
	public static int ADMIN_NAME_COLOR;
	public static int MASTERACCESS_LEVEL;
	public static int MASTERACCESS_NAME_COLOR;
	public static int MASTERACCESS_TITLE_COLOR;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	
	// Jail config
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	
	// Maximum and Minimum karma gain/loss
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	public static String NONDROPPABLE_ITEMS;
	public static List<Integer> LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	
	// Pvp times
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;

	public static boolean ALLOW_SOE_IN_PVP;
	
	// Rate control
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	// ADENA BOSS
	public static float ADENA_BOSS;
	public static float ADENA_RAID;
	public static float ADENA_MINON;
	// ITEMS BOSS
	public static float ITEMS_BOSS;
	public static float ITEMS_RAID;
	public static float ITEMS_MINON;
	// SPOIL BOSS
	public static float SPOIL_BOSS;
	public static float SPOIL_RAID;
	public static float SPOIL_MINON;
	public static float RATE_DROP_QUEST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	// Rate herbs
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	// Player Drop Rate control
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	// Pet Rates (Multipliers)
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	// Karma Drop Rate control
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;

	// SevenSigns
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;

	public static boolean IS_TELNET_ENABLED;
	public static int PORT_GAME;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAXSTATEMENTS;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MIN_POOLSIZE;
	public static int DATABASE_MAX_POOLSIZE;
	public static int DATABASE_ACQUIREINCREMENT;
	public static int DATABASE_IDLECONNECTIONTEST;
	public static int DATABASE_MAXIDLETIME;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;

	// TVT Event Engine
	public static String TVT_EVEN_TEAMS;
	public static boolean TVT_ALLOW_INTERFERENCE;
	public static boolean TVT_ALLOW_POTIONS;
	public static boolean TVT_ALLOW_SUMMON;
	public static boolean TVT_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean TVT_ON_START_UNSUMMON_PET;
	public static boolean TVT_REVIVE_RECOVERY;
	public static boolean TVT_ANNOUNCE_TEAM_STATS;
	public static boolean TVT_PRICE_NO_KILLS;
	public static boolean TVT_JOIN_CURSED;
	public static boolean TVT_CLOSE_COLISEUM_DOORS;
	// CTF Event Engine
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ANNOUNCE_TEAM_STATS;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_REVIVE_RECOVERY;
	// DM Event Engine
	public static boolean DM_ALLOW_INTERFERENCE;
	public static boolean DM_ALLOW_POTIONS;
	public static boolean DM_ALLOW_SUMMON;
	public static boolean DM_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean DM_ON_START_UNSUMMON_PET;
	// Fortress Siege Event Engine
    public static String FortressSiege_EVEN_TEAMS;
    public static boolean FortressSiege_SAME_IP_PLAYERS_ALLOWED;
    public static boolean FortressSiege_ALLOW_INTERFERENCE;
    public static boolean FortressSiege_ALLOW_POTIONS;
    public static boolean FortressSiege_ALLOW_SUMMON;
    public static boolean FortressSiege_ON_START_REMOVE_ALL_EFFECTS;
    public static boolean FortressSiege_ON_START_UNSUMMON_PET;
    public static boolean FortressSiege_ANNOUNCE_TEAM_STATS;
    public static boolean FortressSiege_JOIN_CURSED;
    public static boolean FortressSiege_REVIVE_RECOVERY;
    public static boolean FortressSiege_PRICE_NO_KILLS;
	// Raid Event Engine
	public static boolean RAID_SYSTEM_ENABLED;
	public static int RAID_SYSTEM_MAX_EVENTS;
	public static boolean RAID_SYSTEM_GIVE_BUFFS;
	public static boolean RAID_SYSTEM_RESURRECT_PLAYER;
	public static int RAID_SYSTEM_FIGHT_TIME;
	// Wedding System
	public static boolean ALLOW_WEDDING;
	public static boolean ALLOW_TRADEOFF_VOICE_COMMAND;
	public static boolean ONLINE_VOICE_COMMAND;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	// Champion Mobs
	public static boolean CHAMPION_ENABLE;
	public static int CHAMPION_FREQUENCY;
	public static int CHAMPION_MIN_LVL;
	public static int CHAMPION_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static int CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static boolean ENABLE_FP;

	// PvP and PK Reward
	public static boolean ALLOW_PVP_REWARD;
	public static int PVP_REWARD_ITEM;
	public static int PVP_REWARD_COUNT;
	public static boolean ALLOW_PK_REWARD;
	public static int PK_REWARD_ITEM;
	public static int PK_REWARD_COUNT;
	// Colored pvp name system
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;

	// Character Customizations
	public static int MAX_RCRIT;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static boolean KEEP_SUBCLASS_SKILLS;
	public static int MAX_SUBCLASSES;
	public static boolean NPC_ATTACKABLE;
	public static List<Integer> INVUL_NPC_LIST;
	public static int PLAYER_PROTECTION_SYSTEM;
	public static float DAGGER_RECUDE_DMG_VS_ROBE;
	public static float DAGGER_RECUDE_DMG_VS_LIGHT;
	public static float DAGGER_RECUDE_DMG_VS_HEAVY;
	public static int FRONT_BLOW_SUCCESS;
	public static int BACK_BLOW_SUCCESS;
	public static int SIDE_BLOW_SUCCESS;
	public static boolean DISABLE_GRADE_PENALTIES;
	public static boolean DISABLE_WEIGHT_PENALTIES;

	public static boolean MASTERY_PENALTY;
	public static int LEVEL_TO_GET_PENALITY;
	
	public static boolean DONATOR_DELETE_RENTED_ITEMS;
	public static int DONATOR_NAME_COLOR;
	public static boolean DONATOR_ITEMS;
	public static boolean DONATORS_REVIVE;
	public static boolean ALLOW_HERO_CUSTOM_ITEM;
	public static int HERO_CUSTOM_ITEM_ID;
	public static boolean ALLOW_DONATORS_UNLEGIT_SKILLS;
	public static int KOOFS_NAME_COLOR;
	public static int NOOBS_NAME_COLOR;
	public static boolean ENABLE_FACTION_KOOFS_NOOBS;
	public static int FACTION_ANNOUNCE_TIME;
	public static String KOOFS_NAME_TEAM;
	public static String NOOBS_NAME_TEAM;

	public static int REBIRTH_ITEM;
	public static int REBIRTH_SKILL1;
	public static int REBIRTH_SKILL1_LVL;
	public static int REBIRTH_SKILL2;
	public static int REBIRTH_SKILL2_LVL;
	public static int REBIRTH_SKILL3;
	public static int REBIRTH_SKILL3_LVL;
	public static int REBIRTH_SKILL4;
	public static int REBIRTH_SKILL4_LVL;
	public static int REBIRTH_SKILL5;
	public static int REBIRTH_SKILL5_LVL;
	public static int REBIRTH_SKILL6;
	public static int REBIRTH_SKILL6_LVL;
	public static int REBIRTH_SKILL7;
	public static int REBIRTH_SKILL7_LVL;
	public static int REBIRTH_SKILL8;
	public static int REBIRTH_SKILL8_LVL;
	public static int REBIRTH_SKILL9;
	public static int REBIRTH_SKILL9_LVL;
	public static int REBIRTH_SKILL10;
	public static int REBIRTH_SKILL10_LVL;
	public static boolean ALLOW_REMOTE_CLASS_MASTERS;
	public static boolean ALLOW_L2WALKER_PROTECTION;
	public static boolean PVP_SAME_IP;

	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static int CUSTOM_RUN_SPEED;
	public static double MULTIPLE_MCRIT;
	public static int DEATH_PENALTY_CHANCE;
	public static boolean ALT_PLAYER_CAN_DROP_AA;
	public static int PLAYER_DROP_AA;
	public static boolean ALLOW_ADENA_REWARD;
	public static int ADENA_NUMBER_REWARD_ON_PVP;
	public static boolean LOOSE_ADENA_ON_DIE;
	public static int ADENA_NUMBER_LOST_ON_DIE;
	public static boolean CUSTOM_STARTER_ITEMS_ENABLED;
	public static boolean DISABLE_OFFICIAL_STARTER_ITEMS;
	public static List<int[]> CUSTOM_STARTER_ITEMS = new FastList<int[]>();
	
	// NPC Customizations
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	
	// Limits
	public static int MAX_RUN_SPEED;
	public static int MAX_EVASION;
	public static int MAX_MCRIT_RATE;
	public static boolean ALLOW_MANOR;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static boolean ALLOW_NPC_WALKERS;
	
	// Player Commands
	public static boolean ALLOW_AUTOHERBS_CMD;
	public static boolean SHOW_HTML_WELCOME;
	public static boolean SHOW_GM_LOGIN;
	public static boolean ONLINE_PLAYERS_AT_STARTUP;
	public static int PLAYERS_ONLINE_TRICK;
	public static int ONLINE_PLAYERS_ANNOUNCE_INTERVAL;
	public static boolean ANNOUNCE_CASTLE_LORDS;
	public static boolean ALT_ANNOUNCE_PK;
	public static boolean ENABLE_PK_INFO;
	public static int NPC_ANNOUNCER_PRICE_PER_ANNOUNCE;
	public static int NPC_ANNOUNCER_MAX_ANNOUNCES_PER_DAY;
	public static int NPC_ANNOUNCER_MIN_LVL_TO_ANNOUNCE;
	public static int NPC_ANNOUNCER_MAX_LVL_TO_ANNOUNCE;
	public static boolean ALLOW_NPC_ANNOUNCER;
	public static boolean NPC_ANNOUNCER_DONATOR_ONLY;
	public static boolean ALLOW_OFFLINE_TRADE; 
	public static boolean ALLOW_OFFLINE_CRAFT;
	public static boolean OFFLINE_TARGET_COLOR; 
    public static int OFFLINE_COLOR; 
	
	// Dimensional Rift
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	
	// Clan Customizations
	public static int CLAN_RAISE_FIRST_COST;
	public static int CLAN_RAISE_SEC_COST;
	public static int CLAN_MEMBERS_FIRST;
	public static int CLAN_MEMBERS_SEC;
	public static int CLAN_MEMBERS_THIRD;
	public static int CLAN_REPUTATION_FIRST;
	public static int CLAN_REPUTATION_SEC;
	public static int CLAN_REPUTATION_THIRD;
	public static int CLAN_SP_FIRST;
	public static int CLAN_SP_SEC;
	public static int CLAN_SP_THIRD;
	public static int CLAN_SP_FORTH;
	public static int CLAN_SP_FIFTH;
	
	// Server Customizations
	public static boolean SAFE_SIGTERM;
	public static int GM_OVER_ENCHANT;
	public static int ENCHANT_MAX_ALLOWED_WEAPON;
	public static int ENCHANT_MAX_ALLOWED_ARMOR;
	public static int ENCHANT_MAX_ALLOWED_JEWELRY;
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static String ALLOWED_SKILLS;
	public static FastList<Integer> ALLOWED_SKILLS_LIST = new FastList<Integer>();
	
	// Custom 
	public static int MANA_POTION_RES;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static int ENCHANT_CHANCE_WEAPON_CRYSTAL;
	public static int ENCHANT_CHANCE_ARMOR_CRYSTAL;
	public static int ENCHANT_CHANCE_JEWELRY_CRYSTAL;
	public static int ENCHANT_CHANCE_WEAPON_BLESSED;
	public static int ENCHANT_CHANCE_ARMOR_BLESSED;
	public static int ENCHANT_CHANCE_JEWELRY_BLESSED;
	public static boolean ENABLE_DWARF_ENCHANT_BONUS;
	public static int DWARF_ENCHANT_MIN_LEVEL;
	public static int DWARF_ENCHANT_BONUS;
	public static boolean ENABLE_FIREWORK_ENCH;
	public static int FIREWORK_ENCH_VAL;
	public static int RESET_ENCHANT_VAL;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean ENABLE_PACKET_PROTECTION;
	public static int MAX_UNKNOWN_PACKETS;
	public static int UNKNOWN_PACKETS_PUNISHMENT;
	public static boolean GMAUDIT;
	
	// Miscellaneous Customizations
	public static boolean ENCHANT_HERO_WEAPONS;
	public static boolean SUBCLASS_WITH_ITEM_AND_NO_QUEST;
	public static boolean FLYING_WYVERN_DURING_SIEGE;
	public static boolean ENDLESS_SOUL_SHOTS;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static int ELIXIRS_REUSE_DELAY;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static boolean ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean ENABLE_WAREHOUSESORTING_FREIGHT;
	public static boolean DISABLE_SUMMON_IN_COMBAT;
	public static boolean DISABLE_ATTACK_NPC_TYPE;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static String ALLOWED_NPC_TYPES;
	public static FastList<String> LIST_ALLOWED_NPC_TYPES = new FastList<String>();
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean DELETE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_ETCITEM_TABLE;
	public static boolean CUSTOM_ARMOR_TABLE;
	public static boolean CUSTOM_ARMORSETS_TABLE;
	public static boolean CUSTOM_WEAPON_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_MERCHANT_TABLES;
	public static int OLY_ENCHANT_LIMIT;
	public static boolean OLYMPIAD_ALLOW_AUTO_SS;
	public static boolean OLYMPIAD_GIVE_ACUMEN_MAGES;
	public static boolean OLYMPIAD_GIVE_HASTE_FIGHTERS;
	public static int OLYMPIAD_ACUMEN_LVL;
	public static int OLYMPIAD_HASTE_LVL;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static Integer MODIFIED_SKILL_COUNT;
	public static boolean ENABLE_NO_AUTOLEARN_LIST;
	public static FastList<Integer> NO_AUTOLEARN_LIST;
	public static boolean CASTLE_SHIELD;
	public static boolean CLANHALL_SHIELD;
	public static boolean APELLA_ARMORS;
	public static boolean OATH_ARMORS;
	public static boolean CASTLE_CROWN;
	public static boolean CASTLE_CIRCLETS;
	
	// Banking System 
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_ADENA;
	public static int BANKING_SYSTEM_GOLDBARS;

	// Fortress Settings
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;

	// Augment
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static float SK_FIG;
	public static float SK_MAG;
	public static float AP_FIG;
	public static float CP_MAG;
	public static float M_TK;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	
	// Four Sepulchers
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;

	// General Settings
	public static boolean FAILD_FAKEDEATH;
	public static boolean NPCBUFFER_FEATURE_ENABLED;
	public static int NPCBUFFER_MAX_SCHEMES;
	public static int NPCBUFFER_MAX_SKILLS;
	public static boolean NPCBUFFER_STORE_SCHEMES;
	public static int NPCBUFFER_STATIC_BUFF_COST;
	public static File DATAPACK_ROOT;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;

    // MMO Settings
    public static int MMO_SELECTOR_SLEEP_TIME;
    public static int MMO_MAX_SEND_PER_PASS;
    public static int MMO_MAX_READ_PER_PASS;
    public static int MMO_HELPER_BUFFER_COUNT;
    public static int MMO_IO_SELECTOR_THREAD_COUNT;

	public static boolean USE_3D_MAP;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int PATH_NODE_RADIUS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static boolean TOGGLE_WEAPON_ALLOWED;
	public static boolean NOT_WEAPON_CHANCE_SUBCLASS;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static int IP_UPDATE_TIME;
	
	// Packet information
	public static boolean COUNT_PACKETS = false;
	public static boolean DUMP_PACKET_COUNTS = false;
	public static int DUMP_INTERVAL_SECONDS = 60;
	public static boolean SHOW_LICENCE;
	public static boolean FORCE_GGAUTH;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static int MINIMUM_UPDATE_DISTANCE;
	public static int KNOWNLIST_FORGET_DELAY;
	public static int MINIMUN_UPDATE_TIME;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;

	public static boolean DATABASE_AUTO_ANALYZE;
	public static boolean DATABASE_AUTO_CHECK;
	public static boolean DATABASE_AUTO_OPTIMIZE;
	public static boolean DATABASE_AUTO_REPAIR;
	public static boolean RWHO_LOG;
	public static int RWHO_FORCE_INC;
	public static int RWHO_KEEP_STAT;
	public static int RWHO_MAX_ONLINE;
	public static boolean RWHO_SEND_TRASH;
	public static int RWHO_ONLINE_INCREMENT;
	public static float RWHO_PRIV_STORE_FACTOR;
	public static int RWHO_ARRAY[] = new int[13];
	         
	public static int WORLD_X_MIN;
	public static int WORLD_X_MAX;
	public static int WORLD_Y_MIN;
	public static int WORLD_Y_MAX;
	public static boolean GEODATA_CELLFINDING;
	
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			_log.info("loading gameserver config");
			InputStream is = null;
			try
			{
				try
				{
					Properties serverSettings = new Properties();
					is = new FileInputStream(new File(CONFIGURATION_FILE));
					serverSettings.load(is);
					GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
					PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
					EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
					INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
					REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
					ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2open");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "50"));
					DATABASE_MAXSTATEMENTS = Integer.parseInt(serverSettings.getProperty("MaximumStateMents", "100"));
					DATABASE_MIN_POOLSIZE = Integer.parseInt(serverSettings.getProperty("MinPoolSize", "50"));
					DATABASE_MAX_POOLSIZE = Integer.parseInt(serverSettings.getProperty("MaxPoolSize", "10"));
					DATABASE_ACQUIREINCREMENT = Integer.parseInt(serverSettings.getProperty("AquireIncrement", "1"));
					DATABASE_IDLECONNECTIONTEST = Integer.parseInt(serverSettings.getProperty("IdleConnectionTest", "10800"));
					DATABASE_MAXIDLETIME = Integer.parseInt(serverSettings.getProperty("MaxIdleTime", "0"));
					DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
					CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
					PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
					MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
					MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
					MIN_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "660"));
					MAX_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "665"));
					ENABLE_PACKET_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("PacketProtection", "True"));
					MAX_UNKNOWN_PACKETS = Integer.parseInt(serverSettings.getProperty("UnknownPacketsBeforeBan", "5"));
					UNKNOWN_PACKETS_PUNISHMENT = Integer.parseInt(serverSettings.getProperty("UnknownPacketsPunishment", "2"));
					if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
					{
						throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
					}
					DATABASE_AUTO_ANALYZE = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoAnalyze", "False"));
					DATABASE_AUTO_CHECK = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoCheck", "False"));
					DATABASE_AUTO_OPTIMIZE = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoOptimize", "False"));
					DATABASE_AUTO_REPAIR = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoRepair", "False"));
					//Add Z3r0C00L - Emulation OFF Core (packet SendStatus)
					Random ppc = new Random();
					int z = ppc.nextInt(6);
					if(z == 0)
					{
						z += 2;
					}
					for(int x = 0; x < 8; x++)
					{
						if(x == 4)
						{
							RWHO_ARRAY[x] = 44;
						}
						else
						{
							RWHO_ARRAY[x] = 51 + ppc.nextInt(z);
						}
					}
					RWHO_ARRAY[11] = 37265 + ppc.nextInt(z * 2 + 3);
					RWHO_ARRAY[8] = 51 + ppc.nextInt(z);
					z = 36224 + ppc.nextInt(z * 2);
					RWHO_ARRAY[9] = z;
					RWHO_ARRAY[10] = z;
					RWHO_ARRAY[12] = 1;
					RWHO_LOG = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoLog", "False"));
					RWHO_SEND_TRASH = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoSendTrash", "False"));
					RWHO_MAX_ONLINE = Integer.parseInt(serverSettings.getProperty("RemoteWhoMaxOnline", "0"));
					RWHO_KEEP_STAT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineKeepStat", "5"));
					RWHO_ONLINE_INCREMENT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineIncrement", "0"));
					RWHO_PRIV_STORE_FACTOR = Float.parseFloat(serverSettings.getProperty("RemotePrivStoreFactor", "0"));
					RWHO_FORCE_INC = Integer.parseInt(serverSettings.getProperty("RemoteWhoForceInc", "0"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
				}
				// alternative settings
				try
				{
					Properties altSettings = new Properties();
					is = new FileInputStream(new File(ALT_SETTINGS_FILE));
					altSettings.load(is);
					ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "False"));
					ALT_GAME_CREATION = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "False"));
					ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
					ALT_GAME_CREATION_XP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
					ALT_GAME_CREATION_SP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
					ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(altSettings.getProperty("AltBlacksmithUseRecipes", "True"));
					ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "False"));
					AUTO_LEARN_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "False"));
	                AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(altSettings.getProperty("AutoLearnDivineInspiration", "False"));
					ALT_GAME_CANCEL_BOW = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					ALT_GAME_CANCEL_CAST = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "False"));
					ALT_GAME_DELEVEL = Boolean.parseBoolean(altSettings.getProperty("Delevel", "True"));
					ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "False"));
					GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "True"));
					ALT_GAME_EXPONENT_XP = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
					ALT_GAME_EXPONENT_SP = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
					ALLOW_CLASS_MASTERS = Boolean.parseBoolean(altSettings.getProperty("AllowClassMasters", "False"));
					ALT_GAME_FREIGHTS = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "False"));
					ALT_GAME_FREIGHT_PRICE = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
					ALT_PARTY_RANGE = Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
					ALT_PARTY_RANGE2 = Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
					ALT_WEIGHT_LIMIT = Double.parseDouble(altSettings.getProperty("AltWeightLimit", "1"));
					IS_CRAFTING_ENABLED = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "True"));
					AUTO_LOOT = Boolean.parseBoolean(altSettings.getProperty("AutoLoot", "True"));
					AUTO_LOOT_RAID = Boolean.valueOf(altSettings.getProperty("AutoLootRaid", "True"));
					AUTO_LOOT_HERBS = Boolean.parseBoolean(altSettings.getProperty("AutoLootHerbs", "True"));
					ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "False"));
					ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanShop", "True"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanUseGK", "False"));
					ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanTeleport", "True"));
					ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanTrade", "True"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "True"));
					ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
					ALT_RECOMMEND = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
					ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
					SUBCLASS_WITH_ITEM_AND_NO_QUEST = Boolean.parseBoolean(altSettings.getProperty("SubclassWithItemAndNoQuest", "False"));
					MAX_SUBCLASSES = Integer.parseInt(altSettings.getProperty("MaxSubClasses", "3"));
					KEEP_SUBCLASS_SKILLS = Boolean.parseBoolean(altSettings.getProperty("KeepSubClassSkills", "False"));
					RESTORE_EFFECTS_ON_SUBCLASS_CHANGE = Boolean.parseBoolean(altSettings.getProperty("RestoreEffectsOnSubClassChange", "False"));
					DWARF_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit", "50"));
					COMMON_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit", "50"));
					ALLOW_MANOR = Boolean.parseBoolean(altSettings.getProperty("AllowManor", "False"));
					ALT_MANOR_REFRESH_TIME = Integer.parseInt(altSettings.getProperty("AltManorRefreshTime", "20"));
					ALT_MANOR_REFRESH_MIN = Integer.parseInt(altSettings.getProperty("AltManorRefreshMin", "00"));
					ALT_MANOR_APPROVE_TIME = Integer.parseInt(altSettings.getProperty("AltManorApproveTime", "6"));
					ALT_MANOR_APPROVE_MIN = Integer.parseInt(altSettings.getProperty("AltManorApproveMin", "00"));
					ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(altSettings.getProperty("AltManorMaintenancePreiod", "360000"));
					ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(altSettings.getProperty("AltManorSaveAllActions", "False"));
					ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(altSettings.getProperty("AltManorSavePeriodRate", "2"));
					RIFT_MIN_PARTY_SIZE = Integer.parseInt(altSettings.getProperty("RiftMinPartySize", "5"));
					RIFT_MAX_JUMPS = Integer.parseInt(altSettings.getProperty("MaxRiftJumps", "4"));
					RIFT_SPAWN_DELAY = Integer.parseInt(altSettings.getProperty("RiftSpawnDelay", "10000"));
					RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMin", "480"));
					RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMax", "600"));
					RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(altSettings.getProperty("BossRoomTimeMultiply", "1.5"));
					RIFT_ENTER_COST_RECRUIT = Integer.parseInt(altSettings.getProperty("RecruitCost", "18"));
					RIFT_ENTER_COST_SOLDIER = Integer.parseInt(altSettings.getProperty("SoldierCost", "21"));
					RIFT_ENTER_COST_OFFICER = Integer.parseInt(altSettings.getProperty("OfficerCost", "24"));
					RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(altSettings.getProperty("CaptainCost", "27"));
					RIFT_ENTER_COST_COMMANDER = Integer.parseInt(altSettings.getProperty("CommanderCost", "30"));
					RIFT_ENTER_COST_HERO = Integer.parseInt(altSettings.getProperty("HeroCost", "33"));
					CASTLE_SHIELD = Boolean.parseBoolean(altSettings.getProperty("CastleShieldRestriction", "True"));
					CLANHALL_SHIELD = Boolean.parseBoolean(altSettings.getProperty("ClanHallShieldRestriction", "True"));
					APELLA_ARMORS = Boolean.parseBoolean(altSettings.getProperty("ApellaArmorsRestriction", "True"));
					OATH_ARMORS = Boolean.parseBoolean(altSettings.getProperty("OathArmorsRestriction", "True"));
					CASTLE_CROWN = Boolean.parseBoolean(altSettings.getProperty("CastleLordsCrownRestriction", "True"));
					CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("CastleCircletsRestriction", "True"));
					ALT_OLY_START_TIME = Integer.parseInt(altSettings.getProperty("AltOlyStartTime", "18"));
					ALT_OLY_MIN = Integer.parseInt(altSettings.getProperty("AltOlyMin", "00"));
					ALT_OLY_CPERIOD = Long.parseLong(altSettings.getProperty("AltOlyCPeriod", "21600")) * 1000;
					ALT_OLY_BATTLE = Long.parseLong(altSettings.getProperty("AltOlyBattle", "360")) * 1000;
					ALT_OLY_BWAIT = Long.parseLong(altSettings.getProperty("AltOlyBWait", "600")) * 1000;
					ALT_OLY_IWAIT = Long.parseLong(altSettings.getProperty("AltOlyIWait", "300")) * 1000;
					ALT_OLY_WPERIOD = Long.parseLong(altSettings.getProperty("AltOlyWPeriod", "604800")) * 1000;
					ALT_OLY_VPERIOD = Long.parseLong(altSettings.getProperty("AltOlyVPeriod", "86400")) * 1000;
					ALT_OLY_CLASSED = Integer.parseInt(altSettings.getProperty("AltOlyClassedParticipants", "5"));
					ALT_OLY_NONCLASSED = Integer.parseInt(altSettings.getProperty("AltOlyNonClassedParticipants", "9"));
					ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(altSettings.getProperty("AltOlyBattleRewItem", "6651"));
					ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(altSettings.getProperty("AltOlyClassedRewItemCount", "50"));
					ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(altSettings.getProperty("AltOlyNonClassedRewItemCount", "30"));
					ALT_OLY_COMP_RITEM = Integer.parseInt(altSettings.getProperty("AltOlyCompRewItem", "6651"));
					ALT_OLY_GP_PER_POINT = Integer.parseInt(altSettings.getProperty("AltOlyGPPerPoint", "1000"));
					ALT_OLY_MIN_POINT_FOR_EXCH = Integer.parseInt(altSettings.getProperty("AltOlyMinPointForExchange", "50"));
					ALT_OLY_HERO_POINTS = Integer.parseInt(altSettings.getProperty("AltOlyHeroPoints", "300"));
					ALT_OLY_RESTRICTED_ITEMS = altSettings.getProperty("AltOlyRestrictedItems", "0");
					LIST_OLY_RESTRICTED_ITEMS = new FastList<Integer>();
					for (String id : ALT_OLY_RESTRICTED_ITEMS.split(","))
					{
						LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
					}
					ALT_OLY_SAME_IP = Boolean.parseBoolean(altSettings.getProperty("AltOlySameIp", "True"));
					ALT_LOTTERY_PRIZE = Integer.parseInt(altSettings.getProperty("AltLotteryPrize", "50000"));
					ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice", "2000"));
					ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate", "0.6"));
					ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate", "0.2"));
					ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate", "0.2"));
					ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize", "200"));
					USE_SAY_FILTER = Boolean.parseBoolean(altSettings.getProperty("UseChatFilter", "false"));
					CHAT_FILTER_CHARS = altSettings.getProperty("ChatFilterChars", "***");				CHAT_FILTER_PUNISHMENT = altSettings.getProperty("ChatFilterPunishment", "off");
					CHAT_FILTER_PUNISHMENT_PARAM1 = Integer.parseInt(altSettings.getProperty("ChatFilterPunishmentParam1", "1"));
					CHAT_FILTER_PUNISHMENT_PARAM2 = Integer.parseInt(altSettings.getProperty("ChatFilterPunishmentParam2", "1"));
					BUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("MaxBuffAmount", "20"));
					ALT_DEV_NO_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoQuests", "False"));
					ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoSpawns", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + ALT_SETTINGS_FILE + " File.");
				}
				// clan settings
				try
				{
					Properties clanSettings = new Properties();
					is = new FileInputStream(new File(CLAN_SETTINGS_FILE));
					clanSettings.load(is);

					ALT_CLAN_JOIN_DAYS = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAClan", "5"));
					ALT_CLAN_CREATE_DAYS = Integer.parseInt(clanSettings.getProperty("DaysBeforeCreateAClan", "10"));
					ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(clanSettings.getProperty("DaysToPassToDissolveAClan", "7"));
					ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
					ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
					ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(clanSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
					ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(clanSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
					ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(clanSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
					ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(clanSettings.getProperty("AltClanMembersForWar", "15"));
					ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(clanSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
					ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(clanSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));

					// ********************//
					/* Clan. Customizes. */
					// ********************//
					CLAN_RAISE_FIRST_COST = Integer.parseInt(clanSettings.getProperty("ClanFirstCost", "650000"));
					CLAN_RAISE_SEC_COST = Integer.parseInt(clanSettings.getProperty("ClanSecondCOst", "2500000"));
					CLAN_MEMBERS_FIRST = Integer.parseInt(clanSettings.getProperty("ClanMembersNeedSix", "30"));
					CLAN_MEMBERS_SEC = Integer.parseInt(clanSettings.getProperty("ClanMembersNeedSeven", "80"));
					CLAN_MEMBERS_THIRD = Integer.parseInt(clanSettings.getProperty("ClanMembersNeedEight", "120"));
					CLAN_REPUTATION_FIRST = Integer.parseInt(clanSettings.getProperty("ClanReputationSix", "10000"));
					CLAN_REPUTATION_SEC = Integer.parseInt(clanSettings.getProperty("ClanReputationSeven", "20000"));
					CLAN_REPUTATION_THIRD = Integer.parseInt(clanSettings.getProperty("ClanReputationEight", "40000"));
					CLAN_SP_FIRST = Integer.parseInt(clanSettings.getProperty("ClanSpFirst", "30000"));
					CLAN_SP_SEC = Integer.parseInt(clanSettings.getProperty("ClanSpSecond", "150000"));
					CLAN_SP_THIRD = Integer.parseInt(clanSettings.getProperty("ClanSpThird", "500000"));
					CLAN_SP_FORTH = Integer.parseInt(clanSettings.getProperty("ClanSpForth", "1400000"));
					CLAN_SP_FIFTH = Integer.parseInt(clanSettings.getProperty("ClanSpFifth", "3500000"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CLAN_SETTINGS_FILE + " File.");
				}
				// clanhall settings
				try
				{
					Properties clanhallSettings = new Properties();
					is = new FileInputStream(new File(CLANHALL_CONFIG_FILE));
					clanhallSettings.load(is);
					CH_TELE_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
					CH_TELE1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
					CH_TELE2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
					CH_SUPPORT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
					CH_SUPPORT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
					CH_SUPPORT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
					CH_SUPPORT3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
					CH_SUPPORT4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
					CH_SUPPORT5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
					CH_SUPPORT6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
					CH_SUPPORT7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
					CH_SUPPORT8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
					CH_MPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
					CH_MPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
					CH_MPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
					CH_MPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
					CH_MPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
					CH_MPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
					CH_HPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
					CH_HPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
					CH_HPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
					CH_HPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
					CH_HPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
					CH_HPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
					CH_HPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
					CH_HPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
					CH_HPREG8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
					CH_HPREG9_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
					CH_HPREG10_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
					CH_HPREG11_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
					CH_HPREG12_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
					CH_HPREG13_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
					CH_EXPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
					CH_EXPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
					CH_EXPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
					CH_EXPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
					CH_EXPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
					CH_EXPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
					CH_EXPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
					CH_EXPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
					CH_ITEM_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
					CH_ITEM1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
					CH_ITEM2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
					CH_ITEM3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
					CH_CURTAIN_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
					CH_CURTAIN1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
					CH_CURTAIN2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
					CH_FRONT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
					CH_FRONT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
					CH_FRONT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CLANHALL_CONFIG_FILE + " File.");
				}
				// Enchant File
				try
				{
					Properties Enchant = new Properties();
					is = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
					Enchant.load(is);

					/* chance to enchant an item over +3 */
					ENCHANT_CHANCE_WEAPON = Integer.parseInt(Enchant.getProperty("EnchantChanceWeapon", "68"));
					ENCHANT_CHANCE_ARMOR = Integer.parseInt(Enchant.getProperty("EnchantChanceArmor", "52"));
					ENCHANT_CHANCE_JEWELRY = Integer.parseInt(Enchant.getProperty("EnchantChanceJewelry", "54"));
					ENCHANT_CHANCE_WEAPON_CRYSTAL = Integer.parseInt(Enchant.getProperty("EnchantChanceWeaponCrystal", "85"));
					ENCHANT_CHANCE_ARMOR_CRYSTAL = Integer.parseInt(Enchant.getProperty("EnchantChanceArmorCrystal", "85"));
					ENCHANT_CHANCE_JEWELRY_CRYSTAL = Integer.parseInt(Enchant.getProperty("EnchantChanceJewelryCrystal", "85"));
					ENCHANT_CHANCE_WEAPON_BLESSED = Integer.parseInt(Enchant.getProperty("EnchantChanceWeaponBlessed", "55"));
					ENCHANT_CHANCE_ARMOR_BLESSED = Integer.parseInt(Enchant.getProperty("EnchantChanceArmorBlessed", "55"));
					ENCHANT_CHANCE_JEWELRY_BLESSED = Integer.parseInt(Enchant.getProperty("EnchantChanceJewelryBlessed", "55"));
					ENABLE_DWARF_ENCHANT_BONUS = Boolean.parseBoolean(Enchant.getProperty("EnableDwarfEnchantBonus", "False"));
					DWARF_ENCHANT_MIN_LEVEL = Integer.parseInt(Enchant.getProperty("DwarfEnchantMinLevel", "80"));
					DWARF_ENCHANT_BONUS = Integer.parseInt(Enchant.getProperty("DwarfEnchantBonus", "15"));
					/* firework */
					ENABLE_FIREWORK_ENCH = Boolean.parseBoolean(Enchant.getProperty("EnableFireworkEnchant", "False"));
					FIREWORK_ENCH_VAL = Integer.parseInt(Enchant.getProperty("FireworkEnchantVal", "3"));
					RESET_ENCHANT_VAL = Integer.parseInt(Enchant.getProperty("ResetEnchVal", "0"));
					ENCHANT_HERO_WEAPONS = Boolean.parseBoolean(Enchant.getProperty("EnchantHeroWeapons", "False"));
					/* limit on enchant */
					ENCHANT_MAX_WEAPON = Integer.parseInt(Enchant.getProperty("EnchantMaxWeapon", "255"));
					ENCHANT_MAX_ARMOR = Integer.parseInt(Enchant.getProperty("EnchantMaxArmor", "255"));
					ENCHANT_MAX_JEWELRY = Integer.parseInt(Enchant.getProperty("EnchantMaxJewelry", "255"));
					/* limit of safe enchant normal */
					ENCHANT_SAFE_MAX = Integer.parseInt(Enchant.getProperty("EnchantSafeMax", "3"));
					/* limit of safe enchant full */
					ENCHANT_SAFE_MAX_FULL = Integer.parseInt(Enchant.getProperty("EnchantSafeMaxFull", "4"));

					GM_OVER_ENCHANT = Integer.parseInt(Enchant.getProperty("GMOverEnchant", "25"));
					ENCHANT_MAX_ALLOWED_WEAPON = Integer.parseInt(Enchant.getProperty("EnchantMaxAllowedWeapon", "25"));
					ENCHANT_MAX_ALLOWED_ARMOR = Integer.parseInt(Enchant.getProperty("EnchantMaxAllowedArmor", "25"));
					ENCHANT_MAX_ALLOWED_JEWELRY = Integer.parseInt(Enchant.getProperty("EnchantMaxAllowedJewelry", "25"));

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + ENCHANT_CONFIG_FILE + " File.");
				}
				// Chat Filter File
				try
				{
					Properties ChatFilter = new Properties();
					is = new FileInputStream(new File(CHAT_FILTER_FILE));
					ChatFilter.load(is);

					LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(CHAT_FILTER_FILE))));
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().length() == 0 || line.startsWith("#")) {
							continue;
						}

						FILTER_LIST.add(line.trim());
					}
					_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CHAT_FILTER_FILE + " File.");
				}
				// FloodProtector
				try
				{
					Properties FloodProtector = new Properties();
					is = new FileInputStream(new File(FLOODPROTECTOR_CONFIG_FILE));
					FloodProtector.load(is);
					ENABLE_FP = Boolean.parseBoolean(FloodProtector.getProperty("FloodProtectorEnable", "False"));
					loadFloodProtectorConfigs(FloodProtector);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + FLOODPROTECTOR_CONFIG_FILE + " File.");
				}
				try
				{
					Properties gmSettings = new Properties();
					is = new FileInputStream(new File(GM_ACCESS_FILE));
					gmSettings.load(is);
					GM_ACCESSLEVEL = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
					GM_MIN = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
					GM_ALTG_MIN_LEVEL = Integer.parseInt(gmSettings.getProperty("GMCanAltG", "100"));
					GM_ANNOUNCE = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
					GM_BAN = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
					GM_BAN_CHAT = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
					GM_CREATE_ITEM = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
					GM_DELETE = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
					GM_KICK = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
					GM_MENU = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
					GM_GODMODE = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
					GM_CHAR_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
					GM_CHAR_EDIT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
					GM_CHAR_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
					GM_NPC_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
					GM_NPC_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
					GM_PRIV_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditPriv", "100"));
					GM_PRIV_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewPriv", "100"));
					GM_TELEPORT = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
					GM_TELEPORT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
					GM_RESTART = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
					GM_MONSTERRACE = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
					GM_RIDER = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
					GM_ESCAPE = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
					GM_FIXED = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
					GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
					GM_ENCHANT = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
					GM_DOOR = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
					GM_RES = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
					GM_PEACEATTACK = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
					GM_HEAL = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
					GM_UNBLOCK = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
					GM_CACHE = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
					GM_TALK_BLOCK = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
					GM_TEST = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));
					GM_FORTSIEGE = Integer.parseInt(gmSettings.getProperty("GMFortSiege", "100"));
					GM_CLAN_PANEL = Integer.parseInt(gmSettings.getProperty("GMClanPanel", "100"));
					String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");
					if (!gmTrans.equalsIgnoreCase("False"))
					{
						String[] params = gmTrans.split(",");
						GM_DISABLE_TRANSACTION = true;
						GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
						GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
					}
					else
					{
						GM_DISABLE_TRANSACTION = false;
					}
					GM_ADMIN_MENU_STYLE = gmSettings.getProperty("GMAdminMenuStyle", "modern");
					GM_CAN_GIVE_DAMAGE = Integer.parseInt(gmSettings.getProperty("GMCanGiveDamage", "90"));
					GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
					GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + GM_ACCESS_FILE + " File.");
				}
				// id factory
				try
				{
					Properties idSettings = new Properties();
					is = new FileInputStream(new File(ID_CONFIG_FILE));
					idSettings.load(is);
					MAP_TYPE = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
					SET_TYPE = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
					IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
					BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "True"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + ID_CONFIG_FILE + " File.");
				}
				// Options Config File
				try
				{
					Properties optionsSettings = new Properties();
					is = new FileInputStream(new File(OPTIONS_FILE));
					optionsSettings.load(is);
					EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "False"));
					DEBUG = Boolean.parseBoolean(optionsSettings.getProperty("Debug", "False"));
					ASSERT = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "False"));
					DEVELOPER = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "False"));
					TEST_SERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "False"));
					SERVER_LIST_TESTSERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "False"));
					SERVER_LIST_BRACKET = Boolean.parseBoolean(optionsSettings.getProperty("ServerListBrackets", "False"));
					SERVER_LIST_CLOCK = Boolean.parseBoolean(optionsSettings.getProperty("ServerListClock", "False"));
					SERVER_GMONLY = Boolean.parseBoolean(optionsSettings.getProperty("ServerGMOnly", "False"));
					AUTODESTROY_ITEM_AFTER = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
					HERB_AUTO_DESTROY_TIME = Integer.parseInt(optionsSettings.getProperty("AutoDestroyHerbTime", "15")) * 1000;
					PROTECTED_ITEMS = optionsSettings.getProperty("ListOfProtectedItems");
					LIST_PROTECTED_ITEMS = new FastList<Integer>();
					for (String id : PROTECTED_ITEMS.split(","))
					{
						LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
					}
					DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(optionsSettings.getProperty("DestroyPlayerDroppedItem", "False"));
					DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(optionsSettings.getProperty("DestroyEquipableItem", "False"));
					SAVE_DROPPED_ITEM = Boolean.parseBoolean(optionsSettings.getProperty("SaveDroppedItem", "False"));
					EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "False"));
					SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
					CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(optionsSettings.getProperty("ClearDroppedItemTable", "False"));
					PRECISE_DROP_CALCULATION = Boolean.parseBoolean(optionsSettings.getProperty("PreciseDropCalculation", "True"));
					MULTIPLE_ITEM_DROP = Boolean.parseBoolean(optionsSettings.getProperty("MultipleItemDrop", "True"));
					COORD_SYNCHRONIZE = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));
					ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(optionsSettings.getProperty("OnlyGMItemsFree", "True"));
					ALLOW_WAREHOUSE = Boolean.parseBoolean(optionsSettings.getProperty("AllowWarehouse", "True"));
					WAREHOUSE_CACHE = Boolean.parseBoolean(optionsSettings.getProperty("WarehouseCache", "False"));
					WAREHOUSE_CACHE_TIME = Integer.parseInt(optionsSettings.getProperty("WarehouseCacheTime", "15"));
					ALLOW_FREIGHT = Boolean.parseBoolean(optionsSettings.getProperty("AllowFreight", "True"));
					ALLOW_WEAR = Boolean.parseBoolean(optionsSettings.getProperty("AllowWear", "False"));
					WEAR_DELAY = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
					WEAR_PRICE = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
					ALLOW_LOTTERY = Boolean.parseBoolean(optionsSettings.getProperty("AllowLottery", "False"));
					ALLOW_RACE = Boolean.parseBoolean(optionsSettings.getProperty("AllowRace", "False"));
					ALLOW_WATER = Boolean.parseBoolean(optionsSettings.getProperty("AllowWater", "False"));
					ALLOW_FISHING = Boolean.parseBoolean(optionsSettings.getProperty("AllowFishing_2", "False"));
					ALLOW_RENTPET = Boolean.parseBoolean(optionsSettings.getProperty("AllowRentPet", "False"));
					FLOODPROTECTOR_INITIALSIZE = Integer.parseInt(optionsSettings.getProperty("FloodProtectorInitialSize", "50"));
					ALLOW_DISCARDITEM = Boolean.parseBoolean(optionsSettings.getProperty("AllowDiscardItem", "True"));
					ALLOWFISHING = Boolean.parseBoolean(optionsSettings.getProperty("AllowFishing", "False"));
					ALLOW_BOAT = Boolean.parseBoolean(optionsSettings.getProperty("AllowBoat", "False"));
					ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(optionsSettings.getProperty("AllowCursedWeapons", "False"));
					ALLOW_NPC_WALKERS = Boolean.parseBoolean(optionsSettings.getProperty("AllowNpcWalkers", "False"));
					ACTIVATE_POSITION_RECORDER = Boolean.parseBoolean(optionsSettings.getProperty("ActivatePositionRecorder", "False"));
					DEFAULT_GLOBAL_CHAT = optionsSettings.getProperty("GlobalChat", "ON");
					DEFAULT_TRADE_CHAT = optionsSettings.getProperty("TradeChat", "ON");
					LOG_CHAT = Boolean.parseBoolean(optionsSettings.getProperty("LogChat", "False"));
					LOG_ITEMS = Boolean.parseBoolean(optionsSettings.getProperty("LogItems", "False"));
					LOG_TRADES = Boolean.parseBoolean(optionsSettings.getProperty("LogTrades", "False"));
					LOG_PDAM = Boolean.parseBoolean(optionsSettings.getProperty("LogPdam", "False"));
					LOG_MDAM = Boolean.parseBoolean(optionsSettings.getProperty("LogMdam", "False"));
					GMAUDIT = Boolean.parseBoolean(optionsSettings.getProperty("GMAudit", "False"));
					COMMUNITY_TYPE = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
					BBS_DEFAULT = optionsSettings.getProperty("BBSDefault", "_bbshome");
					SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
					SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
					NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
					NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
					ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
					MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));
					MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
					MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
					SHOW_NPC_LVL = Boolean.parseBoolean(optionsSettings.getProperty("ShowNpcLevel", "False"));
					FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(optionsSettings.getProperty("ForceInventoryUpdate", "False"));
					AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
					THREAD_P_EFFECTS = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "6"));
					THREAD_P_GENERAL = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "15"));
					GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
					IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
					AI_MAX_THREAD = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
					GENERAL_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "4"));
					DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
					DEFAULT_PUNISH = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
					DEFAULT_PUNISH_PARAM = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));
					LAZY_CACHE = Boolean.parseBoolean(optionsSettings.getProperty("LazyCache", "False"));
					PACKET_LIFETIME = Integer.parseInt(optionsSettings.getProperty("PacketLifeTime", "0"));
					BYPASS_VALIDATION = Boolean.parseBoolean(optionsSettings.getProperty("BypassValidation", "True"));
					GAMEGUARD_ENFORCE = Boolean.parseBoolean(optionsSettings.getProperty("GameGuardEnforce", "False"));
					GAMEGUARD_PROHIBITACTION = Boolean.parseBoolean(optionsSettings.getProperty("GameGuardProhibitAction", "False"));
					GRIDS_ALWAYS_ON = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
					GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "1"));
					GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "90"));
					GEODATA = Integer.parseInt(optionsSettings.getProperty("GeoData", "0"));
					FORCE_GEODATA = Boolean.parseBoolean(optionsSettings.getProperty("ForceGeoData", "True"));
	                MOVE_BASED_KNOWNLIST = Boolean.parseBoolean(optionsSettings.getProperty("MoveBasedKnownlist", "False"));
	                KNOWNLIST_UPDATE_INTERVAL = Long.parseLong(optionsSettings.getProperty("KnownListUpdateInterval", "1250"));
					ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(optionsSettings.getProperty("AcceptGeoeditorConn", "False"));
					// ---------------------------------------------------
					// Configuration values not found in config files
					// ---------------------------------------------------
					USE_3D_MAP = Boolean.parseBoolean(optionsSettings.getProperty("Use3DMap", "False"));
					PATH_NODE_RADIUS = Integer.parseInt(optionsSettings.getProperty("PathNodeRadius", "50"));
					NEW_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
					SELECTED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
					LINKED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
					NEW_NODE_TYPE = optionsSettings.getProperty("NewNodeType", "npc");
					COUNT_PACKETS = Boolean.parseBoolean(optionsSettings.getProperty("CountPacket", "False"));
					DUMP_PACKET_COUNTS = Boolean.parseBoolean(optionsSettings.getProperty("DumpPacketCounts", "False"));
					DUMP_INTERVAL_SECONDS = Integer.parseInt(optionsSettings.getProperty("PacketDumpInterval", "60"));
					MINIMUM_UPDATE_DISTANCE = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
					MINIMUN_UPDATE_TIME = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
					CHECK_KNOWN = Boolean.parseBoolean(optionsSettings.getProperty("CheckKnownList", "False"));
					KNOWNLIST_FORGET_DELAY = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));
				
					WORLD_X_MIN = Integer.parseInt(optionsSettings.getProperty("WorldXMin", "11"));
					WORLD_X_MAX = Integer.parseInt(optionsSettings.getProperty("WorldXMax", "26"));
					WORLD_Y_MIN = Integer.parseInt(optionsSettings.getProperty("WorldYMin", "10"));
					WORLD_Y_MAX = Integer.parseInt(optionsSettings.getProperty("WorldYMax", "26"));
					GEODATA_CELLFINDING = Boolean.parseBoolean(optionsSettings.getProperty("CellPathFinding", "False"));
					
					PATHFIND_BUFFERS = optionsSettings.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
					LOW_WEIGHT = Float.parseFloat(optionsSettings.getProperty("LowWeight", "0.5"));
					MEDIUM_WEIGHT = Float.parseFloat(optionsSettings.getProperty("MediumWeight", "2"));
					HIGH_WEIGHT = Float.parseFloat(optionsSettings.getProperty("HighWeight", "3"));
					ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(optionsSettings.getProperty("AdvancedDiagonalStrategy", "True"));
					DIAGONAL_WEIGHT = Float.parseFloat(optionsSettings.getProperty("DiagonalWeight", "0.707"));
					MAX_POSTFILTER_PASSES = Integer.parseInt(optionsSettings.getProperty("MaxPostfilterPasses", "3"));
					DEBUG_PATH = Boolean.parseBoolean(optionsSettings.getProperty("DebugPath", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + OPTIONS_FILE + " File.");
				}
				// other
				try
				{
					Properties otherSettings = new Properties();
					is = new FileInputStream(new File(OTHER_CONFIG_FILE));
					otherSettings.load(is);
					DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
					EFFECT_CANCELING = Boolean.parseBoolean(otherSettings.getProperty("CancelLesserEffect", "True"));
					WYVERN_SPEED = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));
					STRIDER_SPEED = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
					ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(otherSettings.getProperty("AllowWyvernUpgrader", "False"));
					/* Inventory slots limits */
					INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
					INVENTORY_MAXIMUM_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
					INVENTORY_MAXIMUM_GM = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
					MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
					/* Inventory slots limits */
					WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
					WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
					WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
					FREIGHT_SLOTS = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));
					/*
					 * if different from 100 (ie 100%) heal rate is modified acordingly
					 */
					HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100")) / 100;
					MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100")) / 100;
					CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100")) / 100;
					RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
					RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
					RAID_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "100")) / 100;
					RAID_MINION_RESPAWN_TIMER = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
					RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
					RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
					STARTING_ADENA = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
					STARTING_AA = Integer.parseInt(otherSettings.getProperty("StartingAA", "0"));
					STARTING_LEVEL = Byte.parseByte(otherSettings.getProperty("StartingLevel", "1"));
					UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));
					/* Player protection after teleport or login */
					PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
					/*
					 * Player protection after recovering from fake death (works against mobs only)
					 */
					PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));
					/* Defines some Party XP related values */
					PARTY_XP_CUTOFF_METHOD = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
					PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
					PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
					/* Amount of HP, MP, and CP is restored */
					RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
					RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
					RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
					RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
					RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));
					/* Maximum number of available slots for pvt stores */
					MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
					MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
					STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "True"));
					PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "30827");
					LIST_PET_RENT_NPC = new FastList<Integer>();
					for (String id : PET_RENT_NPC.split(","))
					{
						LIST_PET_RENT_NPC.add(Integer.parseInt(id));
					}
					NONDROPPABLE_ITEMS = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");
					LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
					for (String id : NONDROPPABLE_ITEMS.split(","))
					{
						LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
					}
					ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
					ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
					ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
					ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));
					GM_NAME_COLOR_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("GMNameColorEnabled", "False"));
					GM_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("GMNameColor", "FFFF00"));
					ADMIN_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("AdminNameColor", "00FF00"));
					GM_HERO_AURA = Boolean.parseBoolean(otherSettings.getProperty("GMHeroAura", "True"));
					GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvulnerable", "True"));
					GM_STARTUP_INVISIBLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvisible", "True"));
					MASTERACCESS_LEVEL = Integer.parseInt(otherSettings.getProperty("MasterAccessLevel", "1"));
					MASTERACCESS_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("MasterNameColor", "00FF00"));
					MASTERACCESS_TITLE_COLOR = Integer.decode("0x" + otherSettings.getProperty("MasterTitleColor", "00FF00"));
					GM_STARTUP_SILENCE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupSilence", "True"));
					GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(otherSettings.getProperty("GMStartupAutoList", "True"));
					PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
					MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
					MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));
					JAIL_IS_PVP = Boolean.parseBoolean(otherSettings.getProperty("JailIsPvp", "True"));
					JAIL_DISABLE_CHAT = Boolean.parseBoolean(otherSettings.getProperty("JailDisableChat", "True"));
                    //MeGaPk Options
                    // .cl
                    ALLOW_CL = Boolean.parseBoolean(otherSettings.getProperty("AllowCl","False"));
                    ClItemId = Integer.parseInt(otherSettings.getProperty("ClItemId", "4037"));
                    ClItemCount = Integer.parseInt(otherSettings.getProperty("ClItemCount", "1"));
                    ClItemName = otherSettings.getProperty("ClItemName", "Coin of Luck");
                    // .shop
                    ALLOW_SHOP_COMMAND = Boolean.parseBoolean(otherSettings.getProperty("AllowShopCommand","False"));
                    MULTISELL_VOICE = otherSettings.getProperty("AllowedVoiceMultisell", "9998,9999");
                    MULTISELL_VOICE_LIST = new FastList<Integer>();
                    for (String id : MULTISELL_VOICE.trim().split(","))
                    {
                        MULTISELL_VOICE_LIST.add(Integer.parseInt(id.trim()));
                    }
                    // .loc
                    ALLOW_LOC_RB = Boolean.parseBoolean(otherSettings.getProperty("AllowLocRB", "False"));
                    ALLOW_LOC = Boolean.parseBoolean(otherSettings.getProperty("AllowLoc", "False"));
                    // use .loc town
                    LOC_ITEM_ID = Integer.parseInt(otherSettings.getProperty("LocItemId", "4037"));
                    LOC_ITEM_COUNT = Integer.parseInt(otherSettings.getProperty("LocItemCount", "1"));
                    LOC_ITEM_NAME = otherSettings.getProperty("LocItemName", "Coin of Luck");
                    // use .loc epic
                    LOCRB_ITEM_ID = Integer.parseInt(otherSettings.getProperty("LocRBItemId", "4037"));
                    LOCRB_ITEM_COUNT = Integer.parseInt(otherSettings.getProperty("LocRBItemCount", "1"));
                    LOCRB_ITEM_NAME = otherSettings.getProperty("LocRBItemName", "Coin of Luck");

                    WareHouse = Boolean.parseBoolean(otherSettings.getProperty("WareHouse", "False"));
                    REM_BUFF = Boolean.parseBoolean(otherSettings.getProperty("RemoveBuff", "True"));
                    CS_PVP_SYSTEM = Boolean.parseBoolean(otherSettings.getProperty("CsPvpSystem", "False"));
                     // .res
                    RES = Boolean.parseBoolean(otherSettings.getProperty("Res", "False"));
                    RES_CMD_CONSUME_ID = Integer.parseInt(otherSettings.getProperty("ResItem", "4037"));
                    RES_ITEM_NAME = otherSettings.getProperty("ResItemName", "Coin of Luck");
                    // .buyrec
                    BuyRec = Boolean.parseBoolean(otherSettings.getProperty("BuyRec", "False"));
                    REC_ITEM_ID  = Integer.parseInt(otherSettings.getProperty("RecItemId", "4037"));
                    REC_ITEM_COUNT  = Integer.parseInt(otherSettings.getProperty("RecItemCount", "1"));
                    REC_REWARD  = Integer.parseInt(otherSettings.getProperty("RecReward", "100"));
                    
                    Stat = Boolean.parseBoolean(otherSettings.getProperty("Stat", "False"));
                    MAX_LEVEL = Byte.parseByte(otherSettings.getProperty("MaxLevel", "81"));
                    SubLevel = Byte.parseByte(otherSettings.getProperty("SubLevel", "40"));
                    StartNoble = Boolean.parseBoolean(otherSettings.getProperty("StartNoble", "False"));
                    StartHero = Boolean.parseBoolean(otherSettings.getProperty("StartHero", "False"));
                    ALLOW_INFO_VOICE_COMMAND = Boolean.parseBoolean(otherSettings.getProperty("InfoVoice", "False"));
                    ALLOW_BUFF = Boolean.parseBoolean(otherSettings.getProperty("AllowBuff", "False"));
					// V.C.
					ALLOW_CFG_VOICE_COMMAND = Boolean.parseBoolean(otherSettings.getProperty("CfgVoice", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + OTHER_CONFIG_FILE + " File.");
				}
				// General Config File
				try
				{
					Properties General = new Properties();
					is = new FileInputStream(new File(GENERAL_CONFIG_FILE));
					General.load(is);
					TOGGLE_WEAPON_ALLOWED = Boolean.parseBoolean(General.getProperty("ToggleWeaponIsAllowed", "True"));
					NOT_WEAPON_CHANCE_SUBCLASS = Boolean.parseBoolean(General.getProperty("NotWeaponChanceSubClass", "False"));
					FAILD_FAKEDEATH = Boolean.parseBoolean(General.getProperty("FaildFakeDeath", "True"));
					/** L2J NPC Buffer by House */
					NPCBUFFER_FEATURE_ENABLED = Boolean.parseBoolean(General.getProperty("NPCBufferEnabled", "False"));
					NPCBUFFER_MAX_SCHEMES = Integer.parseInt(General.getProperty("NPCBufferMaxSchemesPerChar", "4"));
					NPCBUFFER_MAX_SKILLS = Integer.parseInt(General.getProperty("NPCBufferMaxSkllsperScheme", "24"));
					NPCBUFFER_STORE_SCHEMES = Boolean.parseBoolean(General.getProperty("NPCBufferStoreSchemes", "True"));
					NPCBUFFER_STATIC_BUFF_COST = Integer.parseInt(General.getProperty("NPCBufferStaticCostPerBuff", "-1"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + GENERAL_CONFIG_FILE + " File.");
				}
				// CustomTables Config File
				try
				{
					Properties CustomTables = new Properties();
					is = new FileInputStream(new File(CUSTOM_TABLES_FILE));
					CustomTables.load(is);

					CUSTOM_SPAWNLIST_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomSpawnlistTable", "False"));
					SAVE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(CustomTables.getProperty("SaveGmSpawnOnCustom", "False"));
					DELETE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(CustomTables.getProperty("DeleteGmSpawnOnCustom", "False"));
					CUSTOM_NPC_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomNpcTable", "False"));
					CUSTOM_ETCITEM_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomEtcitemTable", "False"));
					CUSTOM_ARMOR_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomArmorTable", "False"));
					CUSTOM_ARMORSETS_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomArmorSetsTable", "False"));
					CUSTOM_WEAPON_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomWeaponTable", "False"));
					CUSTOM_TELEPORT_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomTeleportTable", "False"));
					CUSTOM_DROPLIST_TABLE = Boolean.parseBoolean(CustomTables.getProperty("CustomDroplistTable", "False"));
					CUSTOM_MERCHANT_TABLES = Boolean.parseBoolean(CustomTables.getProperty("CustomMerchantTables", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CUSTOM_TABLES_FILE + " File.");
				}
				// Balance Config File
				try
				{
					Properties Balance = new Properties();
					is = new FileInputStream(new File(BALANCE_CONFIG_FILE));
					Balance.load(is);

					ENABLE_BALANCE = Boolean.parseBoolean(Balance.getProperty("EnableBalanceClasses", "False"));

					FIGHT_P_DMG = Float.parseFloat(Balance.getProperty("PDamageFightInitial", "1.0"));
					FIGHT_M_DMG = Float.parseFloat(Balance.getProperty("MDamageFightInitial", "1.0"));

					KNIGHT_P_DMG = Float.parseFloat(Balance.getProperty("PDamageKnight", "1.0"));
					KNIGHT_M_DMG = Float.parseFloat(Balance.getProperty("MDamageKnight", "1.0"));

					ROGUE_P_DMG = Float.parseFloat(Balance.getProperty("PDamageRogue", "1.0"));
					ROGUE_M_DMG = Float.parseFloat(Balance.getProperty("MDamageRogue", "1.0"));

					MAGE_INI_P_DMG = Float.parseFloat(Balance.getProperty("PDamageMageInitial", "1.0"));
					MAGE_INI_M_DMG = Float.parseFloat(Balance.getProperty("MDamageMageInitial", "1.0"));

					WIZARD_P_DMG = Float.parseFloat(Balance.getProperty("PDamageWizard", "1.0"));
					WIZARD_M_DMG = Float.parseFloat(Balance.getProperty("MDamageWizard", "1.0"));

					DAGGER_P_DMG = Float.parseFloat(Balance.getProperty("PDamageDagger", "1.0"));
					DAGGER_M_DMG = Float.parseFloat(Balance.getProperty("MDamageDagger", "1.0"));

					ARCHER_P_DMG = Float.parseFloat(Balance.getProperty("PDamageArcher", "1.0"));
					ARCHER_M_DMG = Float.parseFloat(Balance.getProperty("MDamageArcher", "1.0"));

					TANKER_P_DMG = Float.parseFloat(Balance.getProperty("PDamageTanker", "1.0"));
					TANKER_M_DMG = Float.parseFloat(Balance.getProperty("MDamageTanker", "1.0"));

					DUAL_P_DMG = Float.parseFloat(Balance.getProperty("PDamageDual", "1.0"));
					DUAL_M_DMG = Float.parseFloat(Balance.getProperty("MDamageDual", "1.0"));

					POLE_P_DMG = Float.parseFloat(Balance.getProperty("PDamagePole", "1.0"));
					POLE_M_DMG = Float.parseFloat(Balance.getProperty("MDamagePole", "1.0"));

					MAGE_P_DMG = Float.parseFloat(Balance.getProperty("PDamageMage", "1.0"));
					MAGE_M_DMG = Float.parseFloat(Balance.getProperty("MDamageMage", "1.0"));

					ORC_MONK_P_DMG = Float.parseFloat(Balance.getProperty("PDamageOrcMonk", "1.0"));
					ORC_MONK_M_DMG = Float.parseFloat(Balance.getProperty("MDamageOrcMonk", "1.0"));

					ORC_RAIDER_P_DMG = Float.parseFloat(Balance.getProperty("PDamageOrcRaider", "1.0"));
					ORC_RAIDER_M_DMG = Float.parseFloat(Balance.getProperty("MDamageOrcRaider", "1.0"));

					DWARF_P_DMG = Float.parseFloat(Balance.getProperty("PDamageDwarf", "1.0"));
					DWARF_M_DMG = Float.parseFloat(Balance.getProperty("MDamageDwarf", "1.0"));

					ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Balance.getProperty("AltPDamagePets", "1.00"));
					ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Balance.getProperty("AltMDamagePets", "1.00"));
					ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Balance.getProperty("AltPDamageNpc", "1.00"));
					ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Balance.getProperty("AltMDamageNpc", "1.00"));

					MAX_RUN_SPEED = Integer.parseInt(Balance.getProperty("MaxRunSpeed", "250"));
					MAX_EVASION = Integer.parseInt(Balance.getProperty("MaxEvasion", "200"));
					MAX_MCRIT_RATE = Integer.parseInt(Balance.getProperty("MaxMCritRate", "150"));
					MAX_RCRIT = Integer.parseInt(Balance.getProperty("MaxCritical", "500"));
					MAX_PATK_SPEED = Integer.parseInt(Balance.getProperty("MaxPAtkSpeed", "1800"));
					MAX_MATK_SPEED = Integer.parseInt(Balance.getProperty("MaxMAtkSpeed", "2500"));

					DAGGER_RECUDE_DMG_VS_ROBE = Float.parseFloat(Balance.getProperty("DaggerReduceDmgVSRobe", "1.00"));
					DAGGER_RECUDE_DMG_VS_LIGHT = Float.parseFloat(Balance.getProperty("DaggerReduceDmgVSLight", "1.00"));
					DAGGER_RECUDE_DMG_VS_HEAVY = Float.parseFloat(Balance.getProperty("DaggerReduceDmgVSHeavy", "1.00"));
					FRONT_BLOW_SUCCESS = Integer.parseInt(Balance.getProperty("FrontBlow", "50"));
					BACK_BLOW_SUCCESS = Integer.parseInt(Balance.getProperty("BackBlow", "70"));
					SIDE_BLOW_SUCCESS = Integer.parseInt(Balance.getProperty("SideBlow", "60"));

					CUSTOM_RUN_SPEED = Integer.parseInt(Balance.getProperty("CustomRunSpeed", "0"));

					MULTIPLE_MCRIT = Double.parseDouble(Balance.getProperty("MultipleMCrit", "4.0"));

		            ALLOW_DAGGERS_WEAR_HEAVY = Boolean.parseBoolean(Balance.getProperty("AllowDaggersUseHeavy", "True"));
		            ALLOW_ARCHERS_WEAR_HEAVY = Boolean.parseBoolean(Balance.getProperty("AllowArchersUseHeavy", "True"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + BALANCE_CONFIG_FILE + " File.");
				}
				// Olympiad Config File
				try
				{
					Properties OlympiadSettings = new Properties();
					is = new FileInputStream(new File(OLYMPIAD_FILE));
					OlympiadSettings.load(is);
					OLYMPIAD_ALLOW_AUTO_SS = Boolean.parseBoolean(OlympiadSettings.getProperty("OlympiadAllowAutoSS", "True"));
					OLYMPIAD_GIVE_ACUMEN_MAGES = Boolean.parseBoolean(OlympiadSettings.getProperty("OlympiadGiveAcumenMages", "False"));
					OLYMPIAD_GIVE_HASTE_FIGHTERS = Boolean.parseBoolean(OlympiadSettings.getProperty("OlympiadGiveHasteFighters", "True"));
					OLYMPIAD_ACUMEN_LVL = Integer.parseInt(OlympiadSettings.getProperty("OlympiadAcumenLvl", "1"));
					OLYMPIAD_HASTE_LVL = Integer.parseInt(OlympiadSettings.getProperty("OlympiadHasteLvl", "2"));
					OLY_ENCHANT_LIMIT = Integer.parseInt(OlympiadSettings.getProperty("OlyMaxEnchant", "-1"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + OLYMPIAD_FILE + " File.");
				}
				// Feature Config File
				try
				{
					Properties Feature = new Properties();
					is = new FileInputStream(new File(FEATURE_CONFIG_FILE));
					Feature.load(is);
					INVUL_NPC_LIST = new FastList<Integer>();
					String t = Feature.getProperty("InvulNpcList", "30001-32132,35092-35103,35142-35146,35176-35187,35218-35232,35261-35278,35308-35319,35352-35367,35382-35407,35417-35427,35433-35469,35497-35513,35544-35587,35600-35617,35623-35628,35638-35640,35644,35645,50007,70010,99999");
					String as[];
					int k = (as = t.split(",")).length;
					for (int j = 0; j < k; j++)
					{
						String t2 = as[j];
						if (t2.contains("-"))
						{
							int a1 = Integer.parseInt(t2.split("-")[0]);
							int a2 = Integer.parseInt(t2.split("-")[1]);
							for (int i = a1; i <= a2; i++) {
								INVUL_NPC_LIST.add(Integer.valueOf(i));
							}
						}
						else
						{
							INVUL_NPC_LIST.add(Integer.valueOf(Integer.parseInt(t2)));
						}
					}
					DISABLE_ATTACK_NPC_TYPE = Boolean.parseBoolean(Feature.getProperty("DisableAttackToNpcs", "False"));
					ALLOWED_NPC_TYPES = Feature.getProperty("AllowedNPCTypes");
					LIST_ALLOWED_NPC_TYPES = new FastList<String>();
					for (String npc_type : ALLOWED_NPC_TYPES.split(","))
					{
						LIST_ALLOWED_NPC_TYPES.add(npc_type);
					}
					FS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressTeleportFunctionFeeRatio", "604800000"));
					FS_TELE1_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl1", "1000"));
					FS_TELE2_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl2", "10000"));
					FS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressSupportFunctionFeeRatio", "86400000"));
					FS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl1", "7000"));
					FS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl2", "17000"));
					FS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressMpRegenerationFunctionFeeRatio", "86400000"));
					FS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl1", "6500"));
					FS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl2", "9300"));
					FS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressHpRegenerationFunctionFeeRatio", "86400000"));
					FS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl1", "2000"));
					FS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl2", "3500"));
					FS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressExpRegenerationFunctionFeeRatio", "86400000"));
					FS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl1", "9000"));
					FS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl2", "10000"));
					NPC_ATTACKABLE = Boolean.parseBoolean(Feature.getProperty("NpcAttackable", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + FEATURE_CONFIG_FILE + " File.");
				}
				// Augment CONFIG... AUGMENT_CONFIG_FILE
				try
				{
					Properties Augment = new Properties();
					is = new FileInputStream(new File(AUGMENT_CONFIG_FILE));
					Augment.load(is);
					// chances skills and glow
					AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationBaseStatChance", "1"));
					AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationNGSkillChance", "15"));
					AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationNGGlowChance", "0"));
					AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationMidSkillChance", "30"));
					AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationMidGlowChance", "40"));
					AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationHighSkillChance", "45"));
					AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationHighGlowChance", "70"));
					AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationTopSkillChance", "60"));
					AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(Augment.getProperty("AugmentationTopGlowChance", "100"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + AUGMENT_CONFIG_FILE + " File.");
				}
				try
				{
					Properties Dev = new Properties();
					is = new FileInputStream(new File(DEV_CONFIG_FILE));
					Dev.load(is);
					SK_FIG = Float.parseFloat(Dev.getProperty("Skfig", "1.0"));
					SK_MAG = Float.parseFloat(Dev.getProperty("Skmag", "1.0"));
					AP_FIG = Float.parseFloat(Dev.getProperty("Apfig", "1.0"));
					CP_MAG = Float.parseFloat(Dev.getProperty("Cpmag", "1.0"));
					M_TK = Float.parseFloat(Dev.getProperty("Mtk", "1.0"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + DEV_CONFIG_FILE + " File.");
				}
				/*
				 * Load L2J Server Version Properties file (if exists)
				 */
				try
				{
					Properties serverVersion = new Properties();
					is = new FileInputStream(new File(SERVER_VERSION_FILE));
					serverVersion.load(is);
					SERVER_VERSION = serverVersion.getProperty("version", "Unsupported Custom Version.");
					SERVER_BUILD_DATE = serverVersion.getProperty("builddate", "Undefined Date.");
				}
				catch (Exception e)
				{
					// Ignore Properties file if it doesnt exist
					SERVER_VERSION = "Unsupported Custom Version.";
					SERVER_BUILD_DATE = "Undefined Date.";
				}
				// telnet
				try
				{
					Properties telnetSettings = new Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + TELNET_FILE + " File.");
				}
	            // MMO
	            try
	            {
	                Properties mmoSettings = new Properties();
	                is = new FileInputStream(new File(MMO_CONFIG_FILE));
	                mmoSettings.load(is);
	                MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
	                MMO_IO_SELECTOR_THREAD_COUNT = Integer.parseInt(mmoSettings.getProperty("IOSelectorThreadCount", "2"));
	                MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
	                MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
	                MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
	            }
	            catch (Exception e)
	            {
	                e.printStackTrace();
	                throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
	            }
				// rates
				try
				{
					Properties ratesSettings = new Properties();
					is = new FileInputStream(new File(RATES_CONFIG_FILE));
					ratesSettings.load(is);
					RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
					RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
					RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
					RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
					RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
					RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
					RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
					RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
					RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
					RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
					// ADENA BOSS
					ADENA_BOSS = Float.parseFloat(ratesSettings.getProperty("AdenaBoss", "1."));
					ADENA_RAID = Float.parseFloat(ratesSettings.getProperty("AdenaRaid", "1."));
					ADENA_MINON = Float.parseFloat(ratesSettings.getProperty("AdenaMinon", "1."));
					// ITEMS BOSS
					ITEMS_BOSS = Float.parseFloat(ratesSettings.getProperty("ItemsBoss", "1."));
					ITEMS_RAID = Float.parseFloat(ratesSettings.getProperty("ItemsRaid", "1."));
					ITEMS_MINON = Float.parseFloat(ratesSettings.getProperty("ItemsMinon", "1."));
					// SPOIL BOSS
					SPOIL_BOSS = Float.parseFloat(ratesSettings.getProperty("SpoilBoss", "1."));
					SPOIL_RAID = Float.parseFloat(ratesSettings.getProperty("SpoilRaid", "1."));
					SPOIL_MINON = Float.parseFloat(ratesSettings.getProperty("SpoilMinon", "1."));
					RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
					RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
					RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
					RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15."));
					RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10."));
					RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4."));
					RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8")) * 10;
					RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2")) * 10;
					PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
					PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
					PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
					PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
					PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
					PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
					PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
					SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));
					KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
					KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
					KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
					KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
					KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
				}

				try
				{
					Properties OpenEventMods = new Properties();
					is = new FileInputStream(new File(OPEN_MODS));
					OpenEventMods.load(is);
					// -------------------- //
					// TVT Event Engine //
					// -------------------- //
					TVT_EVEN_TEAMS = OpenEventMods.getProperty("TvTEvenTeams", "BALANCE");
					TVT_ALLOW_INTERFERENCE = Boolean.parseBoolean(OpenEventMods.getProperty("TvTAllowInterference", "False"));
					TVT_ALLOW_POTIONS = Boolean.parseBoolean(OpenEventMods.getProperty("TvTAllowPotions", "False"));
					TVT_ALLOW_SUMMON = Boolean.parseBoolean(OpenEventMods.getProperty("TvTAllowSummon", "False"));
					TVT_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(OpenEventMods.getProperty("TvTOnStartRemoveAllEffects", "True"));
					TVT_ON_START_UNSUMMON_PET = Boolean.parseBoolean(OpenEventMods.getProperty("TvTOnStartUnsummonPet", "True"));
					TVT_REVIVE_RECOVERY = Boolean.parseBoolean(OpenEventMods.getProperty("TvTReviveRecovery", "False"));
					TVT_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(OpenEventMods.getProperty("TvtAnnounceTeamStats", "False"));
					TVT_PRICE_NO_KILLS = Boolean.parseBoolean(OpenEventMods.getProperty("TvtPriceNoKills", "False"));
					TVT_JOIN_CURSED = Boolean.parseBoolean(OpenEventMods.getProperty("TvtJoinWithCursedWeapon", "True"));
					TVT_CLOSE_COLISEUM_DOORS = Boolean.parseBoolean(OpenEventMods.getProperty("TvTCloseColiseumDoors", "False"));
					// -------------------- //
					// CTF Event Engine //
					// -------------------- //
					CTF_EVEN_TEAMS = OpenEventMods.getProperty("CTFEvenTeams", "BALANCE");
					CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(OpenEventMods.getProperty("CTFAllowInterference", "False"));
					CTF_ALLOW_POTIONS = Boolean.parseBoolean(OpenEventMods.getProperty("CTFAllowPotions", "False"));
					CTF_ALLOW_SUMMON = Boolean.parseBoolean(OpenEventMods.getProperty("CTFAllowSummon", "False"));
					CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(OpenEventMods.getProperty("CTFOnStartRemoveAllEffects", "True"));
					CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(OpenEventMods.getProperty("CTFOnStartUnsummonPet", "True"));
					CTF_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(OpenEventMods.getProperty("CTFAnnounceTeamStats", "False"));
					CTF_JOIN_CURSED = Boolean.parseBoolean(OpenEventMods.getProperty("CTFJoinWithCursedWeapon", "True"));
					CTF_REVIVE_RECOVERY = Boolean.parseBoolean(OpenEventMods.getProperty("CTFReviveRecovery", "False"));
					// -------------------- //
					// DM Event Engine //
					// -------------------- //
					DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(OpenEventMods.getProperty("DMAllowInterference", "False"));
					DM_ALLOW_POTIONS = Boolean.parseBoolean(OpenEventMods.getProperty("DMAllowPotions", "False"));
					DM_ALLOW_SUMMON = Boolean.parseBoolean(OpenEventMods.getProperty("DMAllowSummon", "False"));
					DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(OpenEventMods.getProperty("DMOnStartRemoveAllEffects", "True"));
					DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(OpenEventMods.getProperty("DMOnStartUnsummonPet", "True"));
					// -------------------- //
					// Fortress Siege Event Engine //
					// -------------------- //
			           FortressSiege_EVEN_TEAMS = OpenEventMods.getProperty("FortressSiegeEvenTeams", "BALANCE");
			           FortressSiege_SAME_IP_PLAYERS_ALLOWED = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeSameIPPlayersAllowed", "False"));
			           FortressSiege_ALLOW_INTERFERENCE = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeAllowInterference", "False"));
			           FortressSiege_ALLOW_POTIONS = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeAllowPotions", "False"));
			           FortressSiege_ALLOW_SUMMON = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeAllowSummon", "False"));
			           FortressSiege_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeOnStartRemoveAllEffects", "True"));
			           FortressSiege_ON_START_UNSUMMON_PET = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeOnStartUnsummonPet", "True"));
			           FortressSiege_REVIVE_RECOVERY = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeReviveRecovery", "False"));
			           FortressSiege_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeAnnounceTeamStats", "False"));
			           FortressSiege_PRICE_NO_KILLS = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegePriceNoKills", "False"));
			           FortressSiege_JOIN_CURSED = Boolean.parseBoolean(OpenEventMods.getProperty("FortressSiegeJoinWithCursedWeapon", "True"));
					// -------------------- //
					// KvN Engine //
					// -------------------- //
					ENABLE_FACTION_KOOFS_NOOBS = Boolean.parseBoolean(OpenEventMods.getProperty("FactionKoofsNoobs", "False"));
					KOOFS_NAME_TEAM = OpenEventMods.getProperty("KoofsTeamName", "koofs");
					NOOBS_NAME_TEAM = OpenEventMods.getProperty("NoobsTeamName", "noobs");
					KOOFS_NAME_COLOR = Integer.decode("0x" + OpenEventMods.getProperty("KoofsColorName", "00FFFF"));
					NOOBS_NAME_COLOR = Integer.decode("0x" + OpenEventMods.getProperty("NoobsColorName", "00FF00"));
					ALT_PLAYER_CAN_DROP_AA = Boolean.parseBoolean(OpenEventMods.getProperty("PlayerCanDropAncientAdena", "False"));
					PLAYER_DROP_AA = Integer.parseInt(OpenEventMods.getProperty("DropAncientAdena", "1"));
					ALLOW_ADENA_REWARD = Boolean.parseBoolean(OpenEventMods.getProperty("PlayerGetAdenaByPvP", "False"));
					ADENA_NUMBER_REWARD_ON_PVP = Integer.parseInt(OpenEventMods.getProperty("AmmountAdenaGetByPvP", "1"));
					LOOSE_ADENA_ON_DIE = Boolean.parseBoolean(OpenEventMods.getProperty("PlayerLooseAdena", "False"));
					ADENA_NUMBER_LOST_ON_DIE = Integer.parseInt(OpenEventMods.getProperty("AmmountAdenaLostWhenDies", "1"));
					ALT_ANNOUNCE_PK = Boolean.parseBoolean(OpenEventMods.getProperty("NoticePK", "False"));
					FACTION_ANNOUNCE_TIME = Integer.parseInt(OpenEventMods.getProperty("AnnounceTimeFaction", "0"));
					// -------------------- //
					// Rebith System //
					// -------------------- //
					REBIRTH_ITEM = Integer.parseInt(OpenEventMods.getProperty("RebirthItemId", "0"));
					REBIRTH_SKILL1 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill1", "0"));
					REBIRTH_SKILL1_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL1", "0"));
					REBIRTH_SKILL2 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill2", "0"));
					REBIRTH_SKILL2_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL2", "0"));
					REBIRTH_SKILL3 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill3", "0"));
					REBIRTH_SKILL3_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL3", "0"));
					REBIRTH_SKILL4 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill4", "0"));
					REBIRTH_SKILL4_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL4", "0"));
					REBIRTH_SKILL5 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill5", "0"));
					REBIRTH_SKILL5_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL5", "0"));
					REBIRTH_SKILL6 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill6", "0"));
					REBIRTH_SKILL6_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL6", "0"));
					REBIRTH_SKILL7 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill7", "0"));
					REBIRTH_SKILL7_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL7", "0"));
					REBIRTH_SKILL8 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill8", "0"));
					REBIRTH_SKILL8_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL8", "0"));
					REBIRTH_SKILL9 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill9", "0"));
					REBIRTH_SKILL9_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL9", "0"));
					REBIRTH_SKILL10 = Integer.parseInt(OpenEventMods.getProperty("RewardSKill10", "0"));
					REBIRTH_SKILL10_LVL = Integer.parseInt(OpenEventMods.getProperty("RewardSKillLvL10", "0"));
					// -------------------- //
					// RAID Event Engine //
					// -------------------- //
					RAID_SYSTEM_ENABLED = Boolean.parseBoolean(OpenEventMods.getProperty("RaidEnginesEnabled", "False"));
					RAID_SYSTEM_GIVE_BUFFS = Boolean.parseBoolean(OpenEventMods.getProperty("RaidGiveBuffs", "True"));
					RAID_SYSTEM_RESURRECT_PLAYER = Boolean.parseBoolean(OpenEventMods.getProperty("RaidResurrectPlayer", "True"));
					RAID_SYSTEM_MAX_EVENTS = Integer.parseInt(OpenEventMods.getProperty("RaidMaxNumEvents", "3"));
					RAID_SYSTEM_FIGHT_TIME = Integer.parseInt(OpenEventMods.getProperty("RaidSystemFightTime", "60"));
					if (RAID_SYSTEM_MAX_EVENTS == 0)
					{
						RAID_SYSTEM_ENABLED = false;
						System.out.println("Raid Engine[Config.load()]: Invalid config property: Max Events = 0?!");
					}
					// -------------------- //
					// Wedding System //
					// -------------------- //
					ALLOW_WEDDING = Boolean.parseBoolean(OpenEventMods.getProperty("AllowWedding", "True"));
					WEDDING_PRICE = Integer.parseInt(OpenEventMods.getProperty("WeddingPrice", "500000"));
					WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(OpenEventMods.getProperty("WeddingPunishInfidelity", "True"));
					WEDDING_TELEPORT = Boolean.parseBoolean(OpenEventMods.getProperty("WeddingTeleport", "True"));
					WEDDING_TELEPORT_PRICE = Integer.parseInt(OpenEventMods.getProperty("WeddingTeleportPrice", "500000"));
					WEDDING_TELEPORT_INTERVAL = Integer.parseInt(OpenEventMods.getProperty("WeddingTeleportInterval", "120"));
					WEDDING_SAMESEX = Boolean.parseBoolean(OpenEventMods.getProperty("WeddingAllowSameSex", "False"));
					// -------------------- //
					// Champion Mods //
					// -------------------- //
					CHAMPION_ENABLE = Boolean.parseBoolean(OpenEventMods.getProperty("ChampionEnable", "False"));
					CHAMPION_FREQUENCY = Integer.parseInt(OpenEventMods.getProperty("ChampionFrequency", "0"));
					CHAMPION_MIN_LVL = Integer.parseInt(OpenEventMods.getProperty("ChampionMinLevel", "20"));
					CHAMPION_MAX_LVL = Integer.parseInt(OpenEventMods.getProperty("ChampionMaxLevel", "60"));
					CHAMPION_HP = Integer.parseInt(OpenEventMods.getProperty("ChampionHp", "7"));
					CHAMPION_HP_REGEN = Float.parseFloat(OpenEventMods.getProperty("ChampionHpRegen", "1."));
					CHAMPION_REWARDS = Integer.parseInt(OpenEventMods.getProperty("ChampionRewards", "8"));
					CHAMPION_ADENAS_REWARDS = Integer.parseInt(OpenEventMods.getProperty("ChampionAdenasRewards", "1"));
					CHAMPION_ATK = Float.parseFloat(OpenEventMods.getProperty("ChampionAtk", "1."));
					CHAMPION_SPD_ATK = Float.parseFloat(OpenEventMods.getProperty("ChampionSpdAtk", "1."));
					CHAMPION_REWARD = Integer.parseInt(OpenEventMods.getProperty("ChampionRewardItem", "0"));
					CHAMPION_REWARD_ID = Integer.parseInt(OpenEventMods.getProperty("ChampionRewardItemID", "6393"));
					CHAMPION_REWARD_QTY = Integer.parseInt(OpenEventMods.getProperty("ChampionRewardItemQty", "1"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + OPEN_MODS + " File.");
				}
				/** ************************************************** */
				/** Mods Properties File -End * */
				/** ************************************************** */
				/** ************************************************** */
				/** Custom Properties File -Begin * */
				/** ************************************************** */
				try
				{
					Properties OpenCustom = new Properties();
					is = new FileInputStream(new File(OPEN_CUSTOM));
					OpenCustom.load(is);
					/** Remote Class Master by Danielmwx **/
					ALLOW_REMOTE_CLASS_MASTERS = Boolean.parseBoolean(OpenCustom.getProperty("AllowRemoteClassMasters", "False"));
					/** L2Walker Protection by Danielmwx **/
					ALLOW_L2WALKER_PROTECTION = Boolean.parseBoolean(OpenCustom.getProperty("L2WalkerProtection", "False"));
					PVP_SAME_IP = Boolean.parseBoolean(OpenCustom.getProperty("PvPSameIP", "False"));
					ALLOW_TRADEOFF_VOICE_COMMAND = Boolean.parseBoolean(OpenCustom.getProperty("TradeOffCommand", "False"));
					ONLINE_VOICE_COMMAND = Boolean.parseBoolean(OpenCustom.getProperty("OnlineVCommand", "False"));
					// ********************//
					/* Character Statistics */
					// ********************//
					PLAYER_PROTECTION_SYSTEM = Integer.parseInt(OpenCustom.getProperty("PlayerProtectionLevel", "0"));
					DISABLE_GRADE_PENALTIES = Boolean.parseBoolean(OpenCustom.getProperty("DisableGradePenalties", "False"));
					DISABLE_WEIGHT_PENALTIES = Boolean.parseBoolean(OpenCustom.getProperty("DisableWeightPenalties", "False"));
					DONATOR_DELETE_RENTED_ITEMS = Boolean.parseBoolean(OpenCustom.getProperty("DonatorDeleteRentedItems", "False"));
					DONATOR_NAME_COLOR = Integer.decode("0x" + OpenCustom.getProperty("DonatorColorName", "00FFFF"));
					DONATOR_ITEMS = Boolean.parseBoolean(OpenCustom.getProperty("AllowDonatorItems", "False"));
					DONATORS_REVIVE = Boolean.parseBoolean(OpenCustom.getProperty("AllowDonatorAutoRevive", "False"));
					ALLOW_HERO_CUSTOM_ITEM = Boolean.parseBoolean(OpenCustom.getProperty("AllowHeroCustomItem", "False"));
					HERO_CUSTOM_ITEM_ID = Integer.parseInt(OpenCustom.getProperty("HeroCustomItemID", "7196"));
					Config.ALLOW_DONATORS_UNLEGIT_SKILLS = Boolean.parseBoolean(OpenCustom.getProperty("AllowDonatorsUnlegit", "False"));
					CHAR_TITLE = Boolean.parseBoolean(OpenCustom.getProperty("CharTitle", "False"));
					ADD_CHAR_TITLE = OpenCustom.getProperty("CharAddTitle", "L2OpenDevTeam");
					DEATH_PENALTY_CHANCE = Integer.parseInt(OpenCustom.getProperty("DeathPenaltyChance", "20"));
					CUSTOM_STARTER_ITEMS_ENABLED = Boolean.parseBoolean(OpenCustom.getProperty("CustomStarterItemsEnabled", "False"));
					DISABLE_OFFICIAL_STARTER_ITEMS = Boolean.parseBoolean(OpenCustom.getProperty("DisableOfficialStarterItems", "False"));
					if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
					{
						String[] propertySplit = OpenCustom.getProperty("CustomStarterItems", "0,0").split(";");
						for (String starteritems : propertySplit)
						{
							String[] starteritemsSplit = starteritems.split(",");
							if (starteritemsSplit.length != 2)
							{
								CUSTOM_STARTER_ITEMS_ENABLED = false;
								System.out.println("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
							}
							else
							{
								try
								{
									CUSTOM_STARTER_ITEMS.add(new int[] { Integer.parseInt(starteritemsSplit[0]), Integer.parseInt(starteritemsSplit[1]) });
								}
								catch (NumberFormatException nfe)
								{
									if (!starteritems.equals(""))
									{
										CUSTOM_STARTER_ITEMS_ENABLED = false;
										System.out.println("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
									}
								}
							}
						}
					}
					// ********************//
					/* NPC Customizations */
					// ********************//
					MIN_MONSTER_ANIMATION = Integer.parseInt(OpenCustom.getProperty("MinMonsterAnimation", "0"));
					MAX_MONSTER_ANIMATION = Integer.parseInt(OpenCustom.getProperty("MaxMonsterAnimation", "0"));
					// ********************//
					/* Player Command */
					// ********************//
					ALLOW_AUTOHERBS_CMD = Boolean.parseBoolean(OpenCustom.getProperty("AllowAutoHerbsCommand", "False"));
					// ********************//
					/* Announcements */
					// ********************//
					SHOW_HTML_WELCOME = Boolean.parseBoolean(OpenCustom.getProperty("ShowTeonInfo", "True"));
					SHOW_GM_LOGIN = Boolean.parseBoolean(OpenCustom.getProperty("ShowGMLogin", "False"));
					ONLINE_PLAYERS_AT_STARTUP = Boolean.parseBoolean(OpenCustom.getProperty("ShowOnlinePlayersAtStartup", "False"));
					PLAYERS_ONLINE_TRICK = Integer.parseInt(OpenCustom.getProperty("OnlinePlayerCountTrick", "0"));
					ONLINE_PLAYERS_ANNOUNCE_INTERVAL = Integer.parseInt(OpenCustom.getProperty("OnlinePlayersAnnounceInterval", "900000"));
					ANNOUNCE_CASTLE_LORDS = Boolean.parseBoolean(OpenCustom.getProperty("AnnounceCastleLords", "False"));
					ENABLE_PK_INFO = Boolean.parseBoolean(OpenCustom.getProperty("EnablePkInfo", "False"));
					NPC_ANNOUNCER_DONATOR_ONLY = Boolean.parseBoolean(OpenCustom.getProperty("NpcAnnouncerDonatorOnly", "False"));
					ALLOW_NPC_ANNOUNCER = Boolean.parseBoolean(OpenCustom.getProperty("AllowNpcAnnouncer", "False"));
					NPC_ANNOUNCER_PRICE_PER_ANNOUNCE = Integer.parseInt(OpenCustom.getProperty("PricePerAnnounce", "10000"));
					NPC_ANNOUNCER_MAX_ANNOUNCES_PER_DAY = Integer.parseInt(OpenCustom.getProperty("AnnouncesPerDay", "20"));
					NPC_ANNOUNCER_MIN_LVL_TO_ANNOUNCE = Integer.parseInt(OpenCustom.getProperty("MinLevelToAnnounce", "0"));
					NPC_ANNOUNCER_MAX_LVL_TO_ANNOUNCE = Integer.parseInt(OpenCustom.getProperty("MaxLevelToAnnounce", "80"));
					// ************************** //
					// *Offline Trade & Crafting* //
					// ************************** //
	                ALLOW_OFFLINE_TRADE = Boolean.parseBoolean(OpenCustom.getProperty("AllowOfflineTrade", "False")); 
	                ALLOW_OFFLINE_CRAFT = Boolean.parseBoolean(OpenCustom.getProperty("AllowOfflineCraft", "False")); 
	                OFFLINE_TARGET_COLOR = Boolean.parseBoolean(OpenCustom.getProperty("OfflineTargetColor", "False")); 
	                OFFLINE_COLOR = Integer.decode("0x" + OpenCustom.getProperty("OfflineColor", "00FFFF")); 
					// ********************//
					/* Serv. Customizes. */
					// ********************//
					SAFE_SIGTERM = Boolean.parseBoolean(OpenCustom.getProperty("SafeSigterm", "False"));
					CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(OpenCustom.getProperty("CheckSkillsOnEnter", "False"));
					ALLOWED_SKILLS = OpenCustom.getProperty("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619");
					ALLOWED_SKILLS_LIST = new FastList<Integer>();
					for (String id : ALLOWED_SKILLS.trim().split(","))
					{
						ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
					}
					MANA_POTION_RES = Integer.parseInt(OpenCustom.getProperty("ManaPotionMPRes", "200")); // Mana Potion Custom Regeneration
					SPAWN_CHAR = Boolean.parseBoolean(OpenCustom.getProperty("CustomSpawn", "False"));
					SPAWN_X = Integer.parseInt(OpenCustom.getProperty("SpawnX", ""));
					SPAWN_Y = Integer.parseInt(OpenCustom.getProperty("SpawnY", ""));
					SPAWN_Z = Integer.parseInt(OpenCustom.getProperty("SpawnZ", ""));
					// ********************//
					/* Misc. Customizes. */
					// ********************//
					FLYING_WYVERN_DURING_SIEGE = Boolean.parseBoolean(OpenCustom.getProperty("FlyingWyvernDuringSiege", "False"));
					ENDLESS_SOUL_SHOTS = Boolean.parseBoolean(OpenCustom.getProperty("EndlessSoulShots", "False"));
					SPIRIT_TIME_MULTIPLIER = Float.parseFloat(OpenCustom.getProperty("SpiritMultiplier", "1"));
					BUFF_TIME_MULTIPLIER = Float.parseFloat(OpenCustom.getProperty("BuffMultiplier", "1"));
					DANCE_TIME_MULTIPLIER = Float.parseFloat(OpenCustom.getProperty("DanceTimeMultiplier", "1"));					
					ES_SP_BOOK_NEEDED = Boolean.parseBoolean(OpenCustom.getProperty("EnchantSkillSpBookNeeded", "True"));
					LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(OpenCustom.getProperty("LifeCrystalNeeded", "True"));
					ELIXIRS_REUSE_DELAY = Integer.parseInt(OpenCustom.getProperty("ElixirsDelay", "0")) * 1000;
					REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(OpenCustom.getProperty("RemoveCastleCirclets", "True"));
					ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(OpenCustom.getProperty("EnableWarehouseSortingClan", "False"));
					ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(OpenCustom.getProperty("EnableWarehouseSortingPrivate", "False"));
					ENABLE_WAREHOUSESORTING_FREIGHT = Boolean.parseBoolean(OpenCustom.getProperty("EnableWarehouseSortingFreight", "False"));
					DISABLE_SUMMON_IN_COMBAT = Boolean.parseBoolean(OpenCustom.getProperty("DisableSummonInCombat", "True"));
					ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(OpenCustom.getProperty("AltPerfectShieldBlockRate", "5"));
					ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(OpenCustom.getProperty("EnableModifySkillDuration", "False"));
					// Create Map only if enabled
					if (ENABLE_MODIFY_SKILL_DURATION)
					{
						SKILL_DURATION_LIST = new FastMap<Integer, Integer>();
						String[] propertySplit;
						propertySplit = OpenCustom.getProperty("SkillDurationList", "").split(";");
						for (String skill : propertySplit)
						{
							String[] skillSplit = skill.split(",");
							if (skillSplit.length != 2)
							{
								System.out.println("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
							}
							else
							{
								try
								{
									SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!skill.equals(""))
									{
										System.out.println("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
									}
								}
							}
						}
					}
					ENABLE_NO_AUTOLEARN_LIST = Boolean.parseBoolean(OpenCustom.getProperty("EnableNoAutoLearnList", "False"));
					if (ENABLE_NO_AUTOLEARN_LIST)
					{
						NO_AUTOLEARN_LIST = new FastList<Integer>();
						String[] propertySplit;
						propertySplit = OpenCustom.getProperty("NoAutoLearnList", "").split(";");
						for (String skill : propertySplit)
						{
							try
							{
								NO_AUTOLEARN_LIST.add(Integer.parseInt(skill));
							}
							catch (NumberFormatException nfe)
							{
								if (!skill.equals(""))
								{
									System.out.println("[NoAutoLearnList]: invalid config property -> NoAutoLearnList \"" + skill);
								}
							}
						}
					}
					// ************************//
					/* Banking System */
					// **********************//
					BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(OpenCustom.getProperty("BankingSystemEnabled", "False"));
					BANKING_SYSTEM_ADENA = Integer.parseInt(OpenCustom.getProperty("BankingSystemAdena", "0"));
					BANKING_SYSTEM_GOLDBARS = Integer.parseInt(OpenCustom.getProperty("BankingSystemGoldBars", "0"));
					// ************************//
					/* PvP E Pk Color System */
					// **********************//
					PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(OpenCustom.getProperty("EnablePvPColorSystem", "False"));
					PVP_AMOUNT1 = Integer.parseInt(OpenCustom.getProperty("PvpAmount1", "500"));
					PVP_AMOUNT2 = Integer.parseInt(OpenCustom.getProperty("PvpAmount2", "1000"));
					PVP_AMOUNT3 = Integer.parseInt(OpenCustom.getProperty("PvpAmount3", "1500"));
					PVP_AMOUNT4 = Integer.parseInt(OpenCustom.getProperty("PvpAmount4", "2500"));
					PVP_AMOUNT5 = Integer.parseInt(OpenCustom.getProperty("PvpAmount5", "5000"));
					NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + OpenCustom.getProperty("ColorForAmount1", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + OpenCustom.getProperty("ColorForAmount2", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + OpenCustom.getProperty("ColorForAmount3", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + OpenCustom.getProperty("ColorForAmount4", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + OpenCustom.getProperty("ColorForAmount4", "00FF00"));
					PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(OpenCustom.getProperty("EnablePkColorSystem", "False"));
					PK_AMOUNT1 = Integer.parseInt(OpenCustom.getProperty("PkAmount1", "500"));
					PK_AMOUNT2 = Integer.parseInt(OpenCustom.getProperty("PkAmount2", "1000"));
					PK_AMOUNT3 = Integer.parseInt(OpenCustom.getProperty("PkAmount3", "1500"));
					PK_AMOUNT4 = Integer.parseInt(OpenCustom.getProperty("PkAmount4", "2500"));
					PK_AMOUNT5 = Integer.parseInt(OpenCustom.getProperty("PkAmount5", "5000"));
					TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + OpenCustom.getProperty("TitleForAmount1", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + OpenCustom.getProperty("TitleForAmount2", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + OpenCustom.getProperty("TitleForAmount3", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + OpenCustom.getProperty("TitleForAmount4", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + OpenCustom.getProperty("TitleForAmount5", "00FF00"));
					// PvP and PK Reward
					ALLOW_PVP_REWARD = Boolean.parseBoolean(OpenCustom.getProperty("AllowPvpRewardSystem", "False"));
					PVP_REWARD_ITEM = Integer.parseInt(OpenCustom.getProperty("PvpRewardItem", "57"));
					PVP_REWARD_COUNT = Integer.parseInt(OpenCustom.getProperty("PvpRewardAmount", "1"));
					ALLOW_PK_REWARD = Boolean.parseBoolean(OpenCustom.getProperty("AllowPkRewardSystem", "False"));
					PK_REWARD_ITEM = Integer.parseInt(OpenCustom.getProperty("PkRewardItem", "57"));
					PK_REWARD_COUNT = Integer.parseInt(OpenCustom.getProperty("PkRewardAmount", "1"));
				
					MASTERY_PENALTY = Boolean.parseBoolean(OpenCustom.getProperty("EnableMasteryPenalties", "True"));
					LEVEL_TO_GET_PENALITY = Integer.parseInt(OpenCustom.getProperty("LevelToGetMasteryPenalities", "20"));
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + OPEN_CUSTOM + " File.");
				}
				/** ************************************************** */
				/** L2J Teon Custom Properties File -End * */
				/** ************************************************** */
				// Seven Signs Config
				try
				{
					Properties SevenSettings = new Properties();
					is = new FileInputStream(new File(SEVENSIGNS_FILE));
					SevenSettings.load(is);
					ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
					ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
					ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
					ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
					ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
					ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
					ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
					ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
					ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
					ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
					ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
					ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + SEVENSIGNS_FILE + " File.");
				}
				// pvp config
				try
				{
					Properties pvpSettings = new Properties();
					is = new FileInputStream(new File(PVP_CONFIG_FILE));
					pvpSettings.load(is);
					/* KARMA SYSTEM */
					KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
					KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
					KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
					KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
					KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "False"));
					KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "True"));
					KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
					KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
					KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
					KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
					for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
					{
						KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
					}
					KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
					for (String id : KARMA_NONDROPPABLE_ITEMS.split(","))
					{
						KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
					}
					PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
					PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
					
					ALLOW_SOE_IN_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowSoeInPVP", "True"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File.");
				}
				try
				{
					Properties Settings = new Properties();
					is = new FileInputStream(HEXID_FILE);
					Settings.load(is);
					SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
					HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
				}
				catch (Exception e)
				{
					_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
				}
				// sepulche Custom Setting
				try
				{
					Properties Settings = new Properties();
					is = new FileInputStream(FS_CONFIG_FILE);
					Settings.load(is);
					FS_TIME_ATTACK = Integer.parseInt(Settings.getProperty("TimeOfAttack", "50"));
					FS_TIME_COOLDOWN = Integer.parseInt(Settings.getProperty("TimeOfCoolDown", "5"));
					FS_TIME_ENTRY = Integer.parseInt(Settings.getProperty("TimeOfEntry", "3"));
					FS_TIME_WARMUP = Integer.parseInt(Settings.getProperty("TimeOfWarmUp", "2"));
					FS_PARTY_MEMBER_COUNT = Integer.parseInt(Settings.getProperty("NumberOfNecessaryPartyMembers", "4"));
					if (FS_TIME_ATTACK <= 0) {
						FS_TIME_ATTACK = 50;
					}
					if (FS_TIME_COOLDOWN <= 0) {
						FS_TIME_COOLDOWN = 5;
					}
					if (FS_TIME_ENTRY <= 0) {
						FS_TIME_ENTRY = 3;
					}
					if (FS_TIME_ENTRY <= 0) {
						FS_TIME_ENTRY = 3;
					}
					if (FS_TIME_ENTRY <= 0) {
						FS_TIME_ENTRY = 3;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + FS_CONFIG_FILE + " File.");
				}
			}
            finally
            {
            	try
            	{
            		is.close();
            	}
            	catch(Exception e)
            	{
            	}
            }
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			_log.info("loading login config");
			InputStream is = null;
			try
			{
				try
				{
					Properties serverSettings = new Properties();
					is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));
					serverSettings.load(is);
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname", "*");
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9013"));
					LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
					PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
					DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "False"));
					DEVELOPER = Boolean.parseBoolean(serverSettings.getProperty("Developer", "False"));
					ASSERT = Boolean.parseBoolean(serverSettings.getProperty("Assert", "False"));
					ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));
					REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
					ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
					LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
					LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
					GM_MIN = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));
					DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile(); // FIXME:
					
					// in login?
					INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "localhost");
					EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "localhost");
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2open");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "50"));
					DATABASE_MAXSTATEMENTS = Integer.parseInt(serverSettings.getProperty("MaximumStateMents", "100"));
					DATABASE_MIN_POOLSIZE = Integer.parseInt(serverSettings.getProperty("MinPoolSize", "50"));
					DATABASE_MAX_POOLSIZE = Integer.parseInt(serverSettings.getProperty("MaxPoolSize", "10"));
					DATABASE_ACQUIREINCREMENT = Integer.parseInt(serverSettings.getProperty("AquireIncrement", "1"));
					DATABASE_IDLECONNECTIONTEST = Integer.parseInt(serverSettings.getProperty("IdleConnectionTest", "10800"));
					DATABASE_MAXIDLETIME = Integer.parseInt(serverSettings.getProperty("MaxIdleTime", "0"));
					SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "True"));
					IP_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("IpUpdateTime", "0"));
					FORCE_GGAUTH = Boolean.parseBoolean(serverSettings.getProperty("ForceGGAuth", "False"));
					AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "True"));
					FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
					FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
					NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
					FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
					MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));

					DATABASE_AUTO_ANALYZE = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoAnalyze", "False"));
					DATABASE_AUTO_CHECK = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoCheck", "False"));
					DATABASE_AUTO_OPTIMIZE = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoOptimize", "False"));
					DATABASE_AUTO_REPAIR = Boolean.parseBoolean(serverSettings.getProperty("DatabaseAutoRepair", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
				}
	            // MMO
	            try
	            {
	                Properties mmoSettings = new Properties();
	                is = new FileInputStream(new File(MMO_CONFIG_FILE));
	                mmoSettings.load(is);
	                MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
	                MMO_IO_SELECTOR_THREAD_COUNT = Integer.parseInt(mmoSettings.getProperty("IOSelectorThreadCount", "2"));
	                MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
	                MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
	                MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
	            }
	            catch (Exception e)
	            {
	                e.printStackTrace();
	                throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
	            }
				// telnet
				try
				{
					Properties telnetSettings = new Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + TELNET_FILE + " File.");
				}
			}
            finally
            {
            	try
            	{
            		is.close();
            	}
            	catch(Exception e)
            	{
            	}
            }
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
	}

	/**
	 * Set a new value to a game parameter from the admin console.
	 *
	 * @param pName
	 *            (String) : name of the parameter to change
	 * @param pValue
	 *            (String) : new value of the parameter
	 * @return boolean : True if modification has been made
	 * @link useAdminCommand
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		// Server settings
		if (pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSp"))
		{
			RATE_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartyXp"))
		{
			RATE_PARTY_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartySp"))
		{
			RATE_PARTY_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestsReward"))
		{
			RATE_QUESTS_REWARD = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropAdena"))
		{
			RATE_DROP_ADENA = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateConsumableCost"))
		{
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropItems"))
		{
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropSpoil"))
		{
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropManor"))
		{
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AdenaBoss"))
		{
			ADENA_BOSS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AdenaRaid"))
		{
			ADENA_RAID = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("LifeCrystalNeeded"))
		{
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AdenaMinon"))
		{
			ADENA_MINON = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ItemsBoss"))
		{
			ITEMS_BOSS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ItemsRaid"))
		{
			ITEMS_RAID = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ItemsMinon"))
		{
			ITEMS_MINON = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("SpoilBoss"))
		{
			SPOIL_BOSS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("SpoilRaid"))
		{
			SPOIL_RAID = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("SpoilMinon"))
		{
			SPOIL_MINON = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropQuest"))
		{
			RATE_DROP_QUEST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateKarmaExpLost"))
		{
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
		{
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerDropLimit"))
		{
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDrop"))
		{
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropItem"))
		{
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip"))
		{
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
		{
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaDropLimit"))
		{
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDrop"))
		{
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropItem"))
		{
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip"))
		{
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
		{
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem"))
		{
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyEquipableItem"))
		{
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItem"))
		{
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad"))
		{
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItemInterval"))
		{
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClearDroppedItemTable"))
		{
			CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("PreciseDropCalculation"))
		{
			PRECISE_DROP_CALCULATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MultipleItemDrop"))
		{
			MULTIPLE_ITEM_DROP = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CoordSynchronize"))
		{
			COORD_SYNCHRONIZE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays"))
		{
			DELETE_DAYS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowDiscardItem"))
		{
			ALLOW_DISCARDITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowFreight"))
		{
			ALLOW_FREIGHT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWarehouse"))
		{
			ALLOW_WAREHOUSE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWear"))
		{
			ALLOW_WEAR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WearDelay"))
		{
			WEAR_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WearPrice"))
		{
			WEAR_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWater"))
		{
			ALLOW_WATER = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRentPet"))
		{
			ALLOW_RENTPET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowBoat"))
		{
			ALLOW_BOAT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowCursedWeapons"))
		{
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowManor"))
		{
			ALLOW_MANOR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("BypassValidation"))
		{
			BYPASS_VALIDATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CommunityType"))
		{
			COMMUNITY_TYPE = pValue.toLowerCase();
		}
		else if (pName.equalsIgnoreCase("BBSDefault"))
		{
			BBS_DEFAULT = pValue;
		}
		else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard"))
		{
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard"))
		{
			SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard"))
		{
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard"))
		{
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowNpcLevel"))
		{
			SHOW_NPC_LVL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ForceInventoryUpdate"))
		{
			FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
		{
			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumOnlineUsers"))
		{
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PacketProtection"))
		{
			ENABLE_PACKET_PROTECTION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("UnknownPacketsBeforeBan"))
		{
			MAX_UNKNOWN_PACKETS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UnknownPacketsPunishment"))
		{
			UNKNOWN_PACKETS_PUNISHMENT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ZoneTown"))
		{
			ZONE_TOWN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowGMLogin"))
		{
			SHOW_GM_LOGIN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumUpdateDistance"))
		{
			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumUpdateTime"))
		{
			MINIMUN_UPDATE_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CheckKnownList"))
		{
			CHECK_KNOWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("KnownListForgetDelay"))
		{
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules"))
		{
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CancelLesserEffect"))
		{
			EFFECT_CANCELING = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WyvernSpeed"))
		{
			WYVERN_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StriderSpeed"))
		{
			STRIDER_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
		{
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
		{
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
		{
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
		{
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
		{
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
		{
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumFreightSlots"))
		{
			FREIGHT_SLOTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceWeapon"))
		{
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceArmor"))
		{
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceJewelry"))
		{
			ENCHANT_CHANCE_JEWELRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceWeaponCrystal"))
		{
			ENCHANT_CHANCE_WEAPON_CRYSTAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceArmorCrystal"))
		{
			ENCHANT_CHANCE_ARMOR_CRYSTAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceWeaponBlessed"))
		{
			ENCHANT_CHANCE_WEAPON_BLESSED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceArmorBlessed"))
		{
			ENCHANT_CHANCE_ARMOR_BLESSED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxWeapon"))
		{
			ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxArmor"))
		{
			ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxJewelry"))
		{
			ENCHANT_MAX_JEWELRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMax"))
		{
			ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMaxFull"))
		{
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GMOverEnchant"))
		{
			GM_OVER_ENCHANT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("HpRegenMultiplier"))
		{
			HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("MpRegenMultiplier"))
		{
			MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("CpRegenMultiplier"))
		{
			CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
		{
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
		{
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidDefenceMultiplier"))
		{
			RAID_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime"))
		{
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAdena"))
		{
			STARTING_ADENA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingLevel"))
		{
			STARTING_LEVEL = Byte.parseByte(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAA"))
		{
			STARTING_AA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UnstuckInterval"))
		{
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerSpawnProtection"))
		{
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
		{
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod"))
		{
			PARTY_XP_CUTOFF_METHOD = pValue;
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent"))
		{
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel"))
		{
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreCP"))
		{
			RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreHP"))
		{
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreMP"))
		{
			RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf"))
		{
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther"))
		{
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StoreSkillCooltime"))
		{
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn"))
		{
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameTiredness"))
		{
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreation"))
		{
			ALT_GAME_CREATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed"))
		{
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate"))
		{
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate"))
		{
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltWeightLimit"))
		{
			ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes"))
		{
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameSkillLearn"))
		{
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPAtkSpeed"))
		{
			MAX_PATK_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxMAtkSpeed"))
		{
			MAX_MATK_SPEED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSkillSpBookNeeded"))
		{
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("FrontBlow"))
		{
			FRONT_BLOW_SUCCESS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BackBlow"))
		{
			BACK_BLOW_SUCCESS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("SideBlow"))
		{
			SIDE_BLOW_SUCCESS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		else if (pName.equalsIgnoreCase("AltShieldBlocks"))
		{
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
		{
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("Delevel"))
		{
			ALT_GAME_DELEVEL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MagicFailures"))
		{
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone"))
		{
			GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentXp"))
		{
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentSp"))
		{
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowClassMaster"))
		{
			ALLOW_CLASS_MASTERS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreights"))
		{
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreightPrice"))
		{
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange"))
		{
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange2"))
		{
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CraftingEnabled"))
		{
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLoot"))
		{
			AUTO_LOOT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLootHerbs"))
		{
			AUTO_LOOT_HERBS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowAutoHerbsCommand"))
		{
			ALLOW_AUTOHERBS_CMD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DisableGradePenalties"))
		{
			DISABLE_GRADE_PENALTIES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DisableWeightPenalties"))
		{
			DISABLE_GRADE_PENALTIES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DisableWeightPenalties"))
		{
			DONATOR_NAME_COLOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DonatorColorName"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireCastleForDawn"))
		{
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireClanCastle"))
		{
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltFreeTeleporting"))
		{
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
		{
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie"))
		{
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH"))
		{
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit"))
		{
			DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CommonRecipeLimit"))
		{
			COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionEnable"))
		{
			CHAMPION_ENABLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionFrequency"))
		{
			CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMinLevel"))
		{
			CHAMPION_MIN_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMaxLevel"))
		{
			CHAMPION_MAX_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHp"))
		{
			CHAMPION_HP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHpRegen"))
		{
			CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewards"))
		{
			CHAMPION_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards"))
		{
			CHAMPION_ADENAS_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAtk"))
		{
			CHAMPION_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionSpdAtk"))
		{
			CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItem"))
		{
			CHAMPION_REWARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemID"))
		{
			CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty"))
		{
			CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWedding"))
		{
			ALLOW_WEDDING = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPrice"))
		{
			WEDDING_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPunishInfidelity"))
		{
			WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleport"))
		{
			WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportPrice"))
		{
			WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportInterval"))
		{
			WEDDING_TELEPORT_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingAllowSameSex"))
		{
			WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingFormalWear"))
		{
			WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingDivorceCosts"))
		{
			WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFEvenTeams"))
		{
			CTF_EVEN_TEAMS = pValue;
		}
		else if (pName.equalsIgnoreCase("CTFAllowInterference"))
		{
			CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFAllowPotions"))
		{
			CTF_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFAllowSummon"))
		{
			CTF_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFOnStartRemoveAllEffects"))
		{
			CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CTFOnStartUnsummonPet"))
		{
			CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DMAllowInterference")) {
			DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("DMAllowPotions")) {
			DM_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("DMAllowSummon")) {
			DM_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("DMOnStartRemoveAllEffects")) {
			DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("DMOnStartUnsummonPet")) {
			DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		} else if (pName.equalsIgnoreCase("MinKarma"))
		{
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxKarma"))
		{
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("XPDivider"))
		{
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BaseKarmaLost"))
		{
			KARMA_LOST_BASE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CanGMDropEquipment"))
		{
			KARMA_DROP_GM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
		{
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
		{
			KARMA_PK_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsNormalTime"))
		{
			PVP_NORMAL_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsPvPTime"))
		{
			PVP_PVP_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GlobalChat"))
		{
			DEFAULT_GLOBAL_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("TradeChat"))
		{
			DEFAULT_TRADE_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("TimeOfAttack"))
		{
			FS_TIME_ATTACK = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TimeOfCoolDown"))
		{
			FS_TIME_COOLDOWN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TimeOfEntry"))
		{
			FS_TIME_ENTRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TimeOfWarmUp"))
		{
			FS_TIME_WARMUP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("NumberOfNecessaryPartyMembers"))
		{
			FS_PARTY_MEMBER_COUNT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MenuStyle"))
		{
			GM_ADMIN_MENU_STYLE = pValue;
		}
		else if (pName.equalsIgnoreCase("AugmentationNGSkillChance"))
		{
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationNGGlowChance"))
		{
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidSkillChance"))
		{
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidGlowChance"))
		{
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighSkillChance"))
		{
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighGlowChance"))
		{
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopSkillChance"))
		{
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopGlowChance"))
		{
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else
		{
			return false;
		}
		return true;
	}

	private static void loadFloodProtectorConfigs(final Properties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "4");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_UNK_PACKETS, "UnkPackets", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_BUFFER, "Buffer", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CRAFT, "Craft", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_WEREHOUSE, "Werehouse", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MISC, "Misc", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHAT, "Chat", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL, "Global", "500");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRADE, "Trade", "500");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_POTION, "Potion", "10");
	}

	private static void loadFloodProtectorConfig(final Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 *
	 * @param string
	 *            (String) : hexadecimal ID of the server to store
	 * @see HEXID_FILE
	 * @see saveHexid(String string, String fileName)
	 * @link LoginServerThread
	 */
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 *
	 * @param hexId
	 *            (String) : hexadecimal ID of the server to store
	 * @param fileName
	 *            (String) : name of the properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("Failed to save hex id to " + fileName + " File.");
			e.printStackTrace();
		}
	}
	/**
	 * Clear all buffered filter words on memory.
	 */
	public static void unallocateFilterBuffer()
	{
		_log.info("Cleaning Chat Filter..");
		FILTER_LIST.clear();
	}
}