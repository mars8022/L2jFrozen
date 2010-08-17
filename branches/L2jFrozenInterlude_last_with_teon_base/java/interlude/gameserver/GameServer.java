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
package interlude.gameserver;

import interlude.Config;
import interlude.ExternalConfig;
import interlude.L2DatabaseFactory;
import interlude.Server;
import interlude.gameserver.cache.CrestCache;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.communitybbs.Manager.ForumsBBSManager;
import interlude.gameserver.datatables.*;
import interlude.gameserver.geoeditorcon.GeoEditorListener;
import interlude.gameserver.handler.*;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.*;
import interlude.gameserver.instancemanager.clanhallsiege.*;
import interlude.gameserver.model.*;
import interlude.gameserver.model.entity.Hero;
import interlude.gameserver.model.entity.Npcbuffer;
import interlude.gameserver.model.olympiad.Olympiad;
import interlude.gameserver.network.L2GameClient;
import interlude.gameserver.network.L2GamePacketHandler;
import interlude.gameserver.pathfinding.geonodes.GeoPathFinding;
import interlude.gameserver.script.faenor.FaenorScriptEngine;
import interlude.gameserver.scripting.CompiledScriptCache;
import interlude.gameserver.scripting.L2ScriptEngineManager;
import interlude.gameserver.taskmanager.KnownListUpdateTaskManager;
import interlude.gameserver.taskmanager.TaskManager;
import interlude.gameserver.util.DynamicExtension;
import interlude.netcore.SelectorConfig;
import interlude.netcore.SelectorThread;
import interlude.status.Status;
import interlude.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class ...
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread<L2GameClient> _selectorThread;
	private final SkillTable _skillTable;
	private final ItemTable _itemTable;
	private final NpcTable _npcTable;
	private final HennaTable _hennaTable;
	private final IdFactory _idFactory;
	public static boolean _instanceOk = false;
	public static GameServer gameServer;
    private final Shutdown _shutdownHandler;
    @SuppressWarnings("unused")
	private final ThreadPoolManager _threadpools;
	private final DoorTable _doorTable;
	private final SevenSigns _sevenSignsEngine;
	private final AutoChatHandler _autoChatHandler;
	private final AutoSpawnHandler _autoSpawnHandler;
	private final LoginServerThread _loginThread;
	private final HelperBuffTable _helperBuffTable;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();


    public long getUsedMemoryMB()
	{
		//_log.finest("used mem:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + "MB");
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; //1024 * 1024 = 1048576;
	}

	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}

	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		// Prints General System Info+
		gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		Util.printSection("Database");
		L2DatabaseFactory.getInstance();
        _threadpools = ThreadPoolManager.getInstance();
        _idFactory = IdFactory.getInstance();
		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		Util.printSection("World");
		L2World.getInstance();
		// load script engines
		L2ScriptEngineManager.getInstance();
		// start game time control early
		GameTimeController.getInstance();
		Util.printSection("ID Factory");
		// keep the references of Singletons to prevent garbage collection
		CharNameTable.getInstance();
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		CharTemplateTable.getInstance();
		PcColorTable.getInstance();
		Util.printSection("Geodata - Path Finding");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			GeoPathFinding.getInstance();
		}
		Util.printSection("Skills");
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the skill table");
		}
		if (Config.ALLOW_NPC_WALKERS)
		{
			NpcWalkerRoutesTable.getInstance().load();
		}
		GmListTable.getInstance();
		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		NobleSkillTable.getInstance();
		HeroSkillTable.getInstance();
		Npcbuffer.getInstance().engineInit();
		NpcBufferSkillIdsTable.getInstance();
		/** NPC Buffer by House */
		if (Config.NPCBUFFER_FEATURE_ENABLED)
		{
			BufferSkillsTable.getInstance();
			CharSchemesTable.getInstance();
		}
		Util.printSection("Trade Controller");
		TradeController.getInstance();
		Util.printSection("Items");
		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the item table");
		}
		ExtractableItemsData.getInstance();
		SummonItemsData.getInstance();
		ArmorSetsTable.getInstance();
		FishTable.getInstance();
		Util.printSection("Henna");
		_hennaTable = HennaTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
			throw new Exception("Could not initialize the Henna Table");
		}
		HennaTreeTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
			throw new Exception("Could not initialize the Henna Tree Table");
		}
		Util.printSection("Npc");
		_npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}
		Util.printSection("Spawnlist");
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().notifyChangeMode();
		Util.printSection("Castle Sieges - Fortress Sieges");
		CastleManager.getInstance();
		SiegeManager.getInstance();
		FortManager.getInstance();
		FortSiegeManager.getInstance();
		// Load clan hall data before zone data and doors table
		ClanHallManager.getInstance();
		Util.printSection("Clan Hall Sieges");
		BanditStrongholdSiege.getInstance();
		DevastatedCastleManager.getInstance();
		FortResistSiegeManager.getInstance();
		FortressofTheDeadManager.getInstance();
		RainbowSpringSiegeManager.getInstance();
		WildBeastFarmSiege.getInstance();
		Util.printSection("Zones");
		ZoneManager.getInstance();
		MapRegionTable.getInstance();
		Util.printSection("Recipes");
		RecipeController.getInstance();
		Util.printSection("Cache");
		// Call to load caches
		HtmCache.getInstance();
		CrestCache.getInstance();
		Util.printSection("Clan");
		ClanTable.getInstance();
        PcCafePointsManager.getInstance();
		Util.printSection("Helper Buff Table");
		_helperBuffTable = HelperBuffTable.getInstance();
		/**
		 * NPCBUFFER: Import Table for NpcBuffer Core Side Buffer
		 */
		BuffTemplateTable.getInstance();
		_log.config("BuffTemplateTable Initialized");
		if (!_helperBuffTable.isInitialized())
		{
			throw new Exception("Could not initialize the Helper Buff Table");
		}
		Util.printSection("Teleport");
		TeleportLocationTable.getInstance();
		LevelUpData.getInstance();
		Util.printSection("Announcements");
		Announcements.getInstance();
		AutoAnnouncementHandler.getInstance();
		/** Load Manor data */
		Util.printSection("Manor");
		L2Manor.getInstance();
		CastleManorManager.getInstance();
		/** Load Manager */
		AuctionManager.getInstance();
		BoatManager.getInstance();
		MercTicketManager.getInstance();
		// PartyCommandManager.getInstance();
		PetitionManager.getInstance();
		// Init of a cursed weapon manager
		CursedWeaponsManager.getInstance();
		Util.printSection("Seven Signs Festival");
		_sevenSignsEngine = SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		// Spawn the Orators/Preachers if in the Seal Validation period.
		_sevenSignsEngine.spawnSevenSignsNPC();
		Util.printSection("Event Drop");
		EventsDropManager.getInstance();
		EventDroplist.getInstance();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		// Couple manager
		if (!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			// if ( _log.isDebugEnabled())_log.debug("CoupleManager initialized");
		}
		MonsterRace.getInstance();
		StaticObjects.getInstance();
		Util.printSection("Handlers");
		AdminCommandHandler.getInstance();
		ChatHandler.getInstance();
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		_autoChatHandler = AutoChatHandler.getInstance();
		_log.info("AutoChatHandler : Loaded " + _autoChatHandler.size() + " handlers in total.");
		_autoSpawnHandler = AutoSpawnHandler.getInstance();
		_log.info("AutoSpawnHandler : Loaded " + _autoSpawnHandler.size() + " handlers in total.");
		// read pet stats from db
		L2PetDataTable.getInstance().loadPetsData();
		Universe.getInstance();
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		Util.printSection("Doors");
		_doorTable = DoorTable.getInstance();
		_doorTable.parseData();
		try
		{
			_doorTable.getDoor(24190001).openMe();
			_doorTable.getDoor(24190002).openMe();
			_doorTable.getDoor(24190003).openMe();
			_doorTable.getDoor(24190004).openMe();
			_doorTable.getDoor(23180001).openMe();
			_doorTable.getDoor(23180002).openMe();
			_doorTable.getDoor(23180003).openMe();
			_doorTable.getDoor(23180004).openMe();
			_doorTable.getDoor(23180005).openMe();
			_doorTable.getDoor(23180006).openMe();
			_doorTable.checkAutoOpen();
		}
		catch (NullPointerException e)
		{
			_log.warning("There is errors in your Door.csv file. Update door.csv");
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
		Util.printSection("Augmentation Data");
		AugmentationData.getInstance();		
		Util.printSection("Quest Manager");
		QuestManager.getInstance();
		Util.printSection("Dimensional Rift");
		DimensionalRiftManager.getInstance();
		Util.printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		TaskManager.getInstance();
		GmListTable.getInstance();
		Util.printSection("RaidBosses - GrandBosses");
		RaidBossPointsManager.init();
		GrandBossManager.getInstance();
		FourSepulchersManager.getInstance().init();
//		VanHalterManager.getInstance().init(); // future implementation in DataPack
		Util.printSection("Quests - Scripts");
//		AILoader.init(); // temporarily disabled
		try
		{
			_log.info("Loading Server Scripts");
			File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
			if (!Config.ALT_DEV_NO_QUESTS)
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (IOException ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script going to be loaded");
		}
		try
		{
			CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
			{
				_log.info("Compiled Scripts Cache is disabled.");
			}
			else
			{
				compiledScriptCache.purge();
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					_log.info("Compiled Scripts Cache was saved.");
				}
				else
				{
					_log.info("Compiled Scripts Cache is up-to-date.");
				}
			}
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Failed to store Compiled Scripts Cache.", e);
		}
		QuestManager.getInstance().report();
		FaenorScriptEngine.getInstance();
		Util.printSection("Game Server");
		ForumsBBSManager.getInstance();
        KnownListUpdateTaskManager.getInstance();
		System.gc();
		_log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		// initialize the dynamic extension loader
		try
		{
			DynamicExtension.getInstance();
		}
		catch (Exception ex)
		{
			_log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
		}
		L2Manor.getInstance();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		// 1024 * 1024 = 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;

		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();

        final SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

        final L2GamePacketHandler gph = new L2GamePacketHandler();
        _selectorThread = new SelectorThread<L2GameClient>(sc, gph, gph, gph, null);

		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.severe("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: "+e1.getMessage());
				if (Config.DEVELOPER)
				{
					e1.printStackTrace();
				}
			}
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.severe("FATAL: Failed to open server socket. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		_selectorThread.start();
        _shutdownHandler = Shutdown.getInstance();
        Runtime.getRuntime().addShutdownHook(_shutdownHandler);
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			OnlinePlayers.getInstance();
		}
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + (serverLoadEnd - serverLoadStart) / 1000 + " Seconds");
		Util.printSection("Game Server Started");
	}

	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		/** * Main ** */
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		Util.printSection("Configs");
		// Initialize config
		Config.load();
		ExternalConfig.loadconfig();
		gameServer = new GameServer();
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
	}
}
