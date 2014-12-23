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
package com.l2jfrozen.gameserver;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.datatables.CharSchemesTable;
import com.l2jfrozen.gameserver.datatables.OfflineTradeTable;
import com.l2jfrozen.gameserver.managers.AutoSaveManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.ItemsOnGroundManager;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfrozen.gameserver.network.serverpackets.ServerClose;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.sql.SQLQueue;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.database.SqlUtils;

/**
 * This class provides the functions for shutting down and restarting the server It closes all open client connections and saves all data.
 * @version $Revision: 1.2.4.6 $ $Date: 2009/05/12 19:45:09 $
 */
public class Shutdown extends Thread
{
	public enum ShutdownModeType1
	{
		SIGTERM("Terminating"),
		SHUTDOWN("Shutting down"),
		RESTART("Restarting"),
		ABORT("Aborting"),
		TASK_SHUT("Shuting down"),
		TASK_RES("Restarting"),
		TELL_SHUT("Shuting down"),
		TELL_RES("Restarting");
		
		private final String _modeText;
		
		ShutdownModeType1(final String modeText)
		{
			_modeText = modeText;
		}
		
		public String getText()
		{
			return _modeText;
		}
	}
	
	protected static final Logger LOGGER = Logger.getLogger(Shutdown.class);
	
	private static Shutdown _instance;
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	
	private int _shutdownMode;
	
	private boolean _shutdownStarted;
	
	/** 0 */
	public static final int SIGTERM = 0;
	/** 1 */
	public static final int GM_SHUTDOWN = 1;
	/** 2 */
	public static final int GM_RESTART = 2;
	/** 3 */
	public static final int ABORT = 3;
	/** 4 */
	public static final int TASK_SHUTDOWN = 4;
	/** 5 */
	public static final int TASK_RESTART = 5;
	/** 6 */
	public static final int TELL_SHUTDOWN = 6;
	/** 7 */
	public static final int TELL_RESTART = 7;
	
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting", // standart
		"shutting down",
		"restarting", // task
		"shutting down",
		"restarting"
	}; // telnet
	
	/**
	 * This function starts a shutdown count down from Telnet (Copied from Function startShutdown())
	 * @param IP Which Issued shutdown command
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	
	public void startTelnetShutdown(final String IP, final int seconds, final boolean restart)
	{
		final Announcements _an = Announcements.getInstance();
		LOGGER.warn("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		_an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (restart)
		{
			_shutdownMode = TELL_RESTART;
		}
		else
		{
			_shutdownMode = TELL_SHUTDOWN;
		}
		
		if (_shutdownMode > 0)
		{
			_an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
			_an.announceToAll("Please exit game now!!");
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, restart, false, true);
		_counterInstance.start();
	}
	
	/**
	 * This function aborts a running countdown
	 * @param IP IP Which Issued shutdown command
	 */
	public void telnetAbort(final String IP)
	{
		Announcements _an = Announcements.getInstance();
		LOGGER.warn("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		_an = null;
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
	}
	
	/**
	 * Default constructor is only used internal to create the shutdown-hook instance
	 */
	public Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
		_shutdownStarted = false;
	}
	
	/**
	 * This creates a count down instance of Shutdown.
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 * @param task
	 * @param telnet
	 */
	public Shutdown(int seconds, final boolean restart, final boolean task, final boolean telnet)
	{
		if (seconds < 0)
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		if (restart)
		{
			if (!task)
			{
				_shutdownMode = GM_RESTART;
			}
			else if (telnet)
			{
				_shutdownMode = TELL_RESTART;
			}
			else
			{
				_shutdownMode = TASK_RESTART;
			}
		}
		else
		{
			if (!task)
			{
				_shutdownMode = GM_SHUTDOWN;
			}
			else if (telnet)
			{
				_shutdownMode = TELL_SHUTDOWN;
			}
			else
			{
				_shutdownMode = TASK_SHUTDOWN;
			}
		}
		
		_shutdownStarted = false;
		
	}
	
	/**
	 * get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it has to be registered externally.
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		if (_instance == null)
		{
			_instance = new Shutdown();
		}
		return _instance;
	}
	
	public boolean isShutdownStarted()
	{
		boolean output = _shutdownStarted;
		
		// if a counter is started, the value of shutdownstarted is of counterinstance
		if (_counterInstance != null)
		{
			output = _counterInstance._shutdownStarted;
		}
		
		return output;
	}
	
	/**
	 * this function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients. after this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a
	 * countdown thread. we start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{
		/*
		 * // disallow new logins try { //Doesnt actually do anything //Server.gameServer.getLoginController().setMaxAllowedOnlinePlayers(0); } catch(Throwable t) { if(Config.ENABLE_ALL_EXCEPTIONS) t.printStackTrace(); }
		 */
		
		if (this == _instance)
		{
			closeServer();
		}
		else
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			
			if (_shutdownMode != ABORT)
			{
				// last point where logging is operational :(
				LOGGER.warn("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
				
				closeServer();
				
			}
			
		}
	}
	
	/**
	 * This functions starts a shutdown countdown
	 * @param activeChar GM who issued the shutdown command
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startShutdown(final L2PcInstance activeChar, final int seconds, final boolean restart)
	{
		Announcements _an = Announcements.getInstance();
		
		if (activeChar != null)
			LOGGER.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		else
			LOGGER.warn("External Service issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		if (_shutdownMode > 0)
		{
			_an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
			_an.announceToAll("Please exit game now!!");
			_an = null;
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		
		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart, false, false);
		_counterInstance.start();
	}
	
	public int getCountdown()
	{
		return _secondsShut;
	}
	
	/**
	 * This function aborts a running countdown
	 * @param activeChar GM who issued the abort command
	 */
	public void abort(final L2PcInstance activeChar)
	{
		Announcements _an = Announcements.getInstance();
		
		if (activeChar != null)
			LOGGER.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		else
			LOGGER.warn("External Service issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		
		_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		_an = null;
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
	}
	
	/**
	 * set the shutdown mode
	 * @param mode what mode shall be set
	 */
	/*
	 * private void setMode(final int mode) { _shutdownMode = mode; }
	 */
	
	/**
	 * set shutdown mode to ABORT
	 */
	private void _abort()
	{
		_shutdownMode = ABORT;
	}
	
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		
		try
		{
			while (_secondsShut > 0)
			{
				
				int _seconds;
				int _minutes;
				int _hours;
				
				_seconds = _secondsShut;
				_minutes = _seconds / 60;
				_hours = _seconds / 3600;
				
				// announce only every minute after 10 minutes left and every second after 20 seconds
				if ((_seconds <= 20 || _seconds == _minutes * 10) && _seconds <= 600 && _hours <= 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
					sm.addString(Integer.toString(_seconds));
					Announcements.getInstance().announceToAll(sm);
					sm = null;
				}
				
				try
				{
					if (_seconds <= 60)
					{
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
					}
				}
				catch (final Exception e)
				{
					// do nothing, we maybe are not connected to LS anymore
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
				}
				
				_secondsShut--;
				
				final int delay = 1000; // milliseconds
				Thread.sleep(delay);
				
				if (_shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch (final InterruptedException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}
	
	private void closeServer()
	{
		
		// last byebye, save all data and quit this server
		// logging doesnt work here :(
		_shutdownStarted = true;
		
		try
		{
			LoginServerThread.getInstance().interrupt();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		AutoSaveManager.getInstance().stopAutoSaveManager();
		
		// ensure all services are stopped
		SQLQueue.getInstance().shutdown();
		
		// saveData sends messages to exit players, so shutdown selector after it
		saveData();
		
		try
		{
			GameTimeController.getInstance().stopTimer();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		try
		{
			// GameServer.getSelectorThread().setDaemon(true);
			GameServer.getSelectorThread().shutdown();
			
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		// stop all threadpolls
		try
		{
			ThreadPoolManager.getInstance().shutdown();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		try
		{
			SqlUtils.OpzGame();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		try
		{
			SqlUtils.OpzLogin();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		LOGGER.info("Committing all data, last chance...");
		
		// commit data, last chance
		try
		{
			L2DatabaseFactory.getInstance().shutdown();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		LOGGER.info("All database data committed.");
		
		System.runFinalization();
		System.gc();
		
		LOGGER.info("Memory cleanup, recycled unused objects.");
		
		LOGGER.info("[STATUS] Server shutdown successfully.");
		
		// server will quit, when this function ends.
		/*
		 * switch (_shutdownMode) { case GM_SHUTDOWN: _instance.setMode(GM_SHUTDOWN); System.exit(0); break; case GM_RESTART: _instance.setMode(GM_RESTART); System.exit(2); break; case TASK_SHUTDOWN: _instance.setMode(TASK_SHUTDOWN); System.exit(4); break; case TASK_RESTART:
		 * _instance.setMode(TASK_RESTART); System.exit(5); break; case TELL_SHUTDOWN: _instance.setMode(TELL_SHUTDOWN); System.exit(6); break; case TELL_RESTART: _instance.setMode(TELL_RESTART); System.exit(7); break; }
		 */
		
		if (_instance._shutdownMode == GM_RESTART)
		{
			Runtime.getRuntime().halt(2);
		}
		else if (_instance._shutdownMode == TASK_RESTART)
		{
			Runtime.getRuntime().halt(5);
		}
		else if (_instance._shutdownMode == TASK_SHUTDOWN)
		{
			Runtime.getRuntime().halt(4);
		}
		else if (_instance._shutdownMode == TELL_RESTART)
		{
			Runtime.getRuntime().halt(7);
		}
		else if (_instance._shutdownMode == TELL_SHUTDOWN)
		{
			Runtime.getRuntime().halt(6);
		}
		else
		{
			Runtime.getRuntime().halt(0);
		}
		
	}
	
	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private synchronized void saveData()
	{
		Announcements _an = Announcements.getInstance();
		switch (_shutdownMode)
		{
			case SIGTERM:
				LOGGER.info("SIGTERM received. Shutting down NOW!");
				break;
			
			case GM_SHUTDOWN:
				LOGGER.info("GM shutdown received. Shutting down NOW!");
				break;
			
			case GM_RESTART:
				LOGGER.info("GM restart received. Restarting NOW!");
				break;
			
			case TASK_SHUTDOWN:
				LOGGER.info("Auto task shutdown received. Shutting down NOW!");
				break;
			
			case TASK_RESTART:
				LOGGER.info("Auto task restart received. Restarting NOW!");
				break;
			
			case TELL_SHUTDOWN:
				LOGGER.info("Telnet shutdown received. Shutting down NOW!");
				break;
			
			case TELL_RESTART:
				LOGGER.info("Telnet restart received. Restarting NOW!");
				break;
		
		}
		try
		{
			_an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " NOW!");
			_an = null;
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
		}
		
		try
		{
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
				OfflineTradeTable.storeOffliners();
		}
		catch (final Throwable t)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				t.printStackTrace();
			
			LOGGER.error("Error saving offline shops.", t);
		}
		
		try
		{
			wait(1000);
		}
		catch (final InterruptedException e1)
		{
		}
		
		// Disconnect all the players from the server
		disconnectAllCharacters();
		
		try
		{
			wait(5000);
		}
		catch (final InterruptedException e1)
		{
		}
		
		// Save players data!
		saveAllPlayers();
		
		try
		{
			wait(10000);
		}
		catch (final InterruptedException e1)
		{
		}
		
		// Seven Signs data is now saved along with Festival data.
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
		}
		
		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData(null, true);
		LOGGER.info("SevenSigns: All info saved!!");
		
		// Save all raidboss status
		RaidBossSpawnManager.getInstance().cleanUp();
		LOGGER.info("RaidBossSpawnManager: All raidboss info saved!!");
		
		// Save all Grandboss status
		GrandBossManager.getInstance().cleanUp();
		LOGGER.info("GrandBossManager: All Grand Boss info saved!!");
		
		// Save data CountStore
		TradeController.getInstance().dataCountStore();
		LOGGER.info("TradeController: All count Item Saved");
		
		// Save Olympiad status
		try
		{
			Olympiad.getInstance().saveOlympiadStatus();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		LOGGER.info("Olympiad System: Data saved!!");
		
		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();
		
		// Save all manor data
		CastleManorManager.getInstance().save();
		
		// Save all global (non-player specific) Quest data that needs to persist after reboot
		if (!Config.ALT_DEV_NO_QUESTS)
			QuestManager.getInstance().save();
		
		CharSchemesTable.getInstance().onServerShutdown();
		
		// Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			LOGGER.info("ItemsOnGroundManager: All items on ground saved!!");
		}
		
		try
		{
			wait(5000);
		}
		catch (final InterruptedException e)
		{
			// never happens :p
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}
	
	private void saveAllPlayers()
	{
		LOGGER.info("Saving all players data...");
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
				continue;
			
			// Logout Character
			try
			{
				// Save player status
				player.store();
				
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
			}
		}
		
	}
	
	/**
	 * this disconnects all clients from the server
	 */
	private void disconnectAllCharacters()
	{
		LOGGER.info("Disconnecting all players from the Server...");
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
				continue;
			
			try
			{
				// Player Disconnect
				if (player.getClient() != null)
				{
					player.getClient().sendPacket(ServerClose.STATIC_PACKET);
					player.getClient().close(0);
					player.getClient().setActiveChar(null);
					player.setClient(null);
					
				}
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
			}
		}
		
	}
	
}
