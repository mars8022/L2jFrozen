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
package com.l2jfrozen.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.FService;
import com.l2jfrozen.L2Frozen;
import com.l2jfrozen.ServerType;
import com.l2jfrozen.gameserver.datatables.GameServerTable;
import com.l2jfrozen.netcore.SelectorConfig;
import com.l2jfrozen.netcore.SelectorThread;
import com.l2jfrozen.status.Status;
import com.l2jfrozen.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.database.SqlUtils;

public class L2LoginServer
{
	public static final int PROTOCOL_REV = 0x0102;

	private static L2LoginServer _instance;
	private Logger _log = Logger.getLogger(L2LoginServer.class.getName());
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;
	
	public static void main(String[] args)
	{
		_instance = new L2LoginServer();
	}

	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	public L2LoginServer()
	{
		ServerType.serverMode = ServerType.MODE_LOGINSERVER;
		//      Local Constants
		final String LOG_FOLDER_BASE = "log"; // Name of folder for log base file
		File logFolderBase = new File(LOG_FOLDER_BASE);
		logFolderBase.mkdir();
		
		final String LOG_FOLDER = "log/login"; // Name of folder for log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = null;
		try
		{
			//check for legacy Implementation
			File log_conf_file = new File(FService.LOG_CONF_FILE);
			if(!log_conf_file.exists()){
				//old file position
				log_conf_file = new File(FService.LEGACY_LOG_CONF_FILE);
			}
			
			is = new FileInputStream(log_conf_file);
			LogManager.getLogManager().readConfiguration(is);

		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(is != null){
				try
				{
					
					is.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
		}

		// Team info
		Util.printSection("Team");
		L2Frozen.info();

		// Load LoginServer Configs
		Config.load();

		Util.printSection("Database");
		// Prepare Database
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch(SQLException e)
		{
			_log.severe("FATAL: Failed initializing database. Reason: " + e.getMessage());

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}

		try
		{
			LoginController.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed initializing LoginController. Reason: " + e.getMessage());
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}

		try
		{
			GameServerTable.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}
		catch(Exception e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.exit(1);
		}

		InetAddress bindAddress = null;
		if(!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch(UnknownHostException e1)
			{
				_log.severe("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage());

				if(Config.ENABLE_ALL_EXCEPTIONS)
					e1.printStackTrace();
				
			}
		}		
		// Load telnet status
		if (Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new Status(ServerType.serverMode);
				_statusServer.start();
			}
			catch (IOException e)
			{
				_log.log(Level.WARNING, "Failed to start the Telnet Server. Reason: " + e.getMessage(), e);
			}
		}

		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = com.l2jfrozen.netcore.Config.getInstance().MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = com.l2jfrozen.netcore.Config.getInstance().MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = com.l2jfrozen.netcore.Config.getInstance().MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = com.l2jfrozen.netcore.Config.getInstance().MMO_HELPER_BUFFER_COUNT;
		
		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<L2LoginClient>(sc, sh, lph, sh, sh);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open Selector. Reason: " + e.getMessage());

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}

		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage());

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
			_selectorThread.start();
			_log.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);

		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open server socket. Reason: " + e.getMessage());
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			

			System.exit(1);
		}
		
		
		//load bannedIps
		Config.loadBanFile();
		
		logFolder = null;
		bindAddress = null;
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}

	public void shutdown(boolean restart)
	{
		LoginController.getInstance().shutdown();
		SqlUtils.OpzLogin();
		System.gc();
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
