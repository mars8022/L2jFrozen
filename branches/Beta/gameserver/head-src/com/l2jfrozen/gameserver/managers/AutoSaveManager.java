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
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * <b>AutoSave</b> only
 * @author Shyla
 */
public class AutoSaveManager
{
	protected static final Logger LOGGER = Logger.getLogger(AutoSaveManager.class);
	private ScheduledFuture<?> _autoSaveInDB;
	private ScheduledFuture<?> _autoCheckConnectionStatus;
	private ScheduledFuture<?> _autoCleanDatabase;
	
	public static final AutoSaveManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public AutoSaveManager()
	{
		LOGGER.info("Initializing AutoSaveManager");
	}
	
	public void stopAutoSaveManager()
	{
		if (_autoSaveInDB != null)
		{
			_autoSaveInDB.cancel(true);
			_autoSaveInDB = null;
		}
		
		if (_autoCheckConnectionStatus != null)
		{
			_autoCheckConnectionStatus.cancel(true);
			_autoCheckConnectionStatus = null;
		}
		if (_autoCleanDatabase != null)
		{
			_autoCleanDatabase.cancel(true);
			_autoCleanDatabase = null;
		}
	}
	
	public void startAutoSaveManager()
	{
		
		stopAutoSaveManager();
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		_autoCheckConnectionStatus = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConnectionCheckTask(), Config.CHECK_CONNECTION_INITIAL_TIME, Config.CHECK_CONNECTION_DELAY_TIME);
		_autoCleanDatabase = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoCleanDBTask(), Config.CLEANDB_INITIAL_TIME, Config.CLEANDB_DELAY_TIME);
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			int playerscount = 0;
			
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					try
					{
						playerscount++;
						player.store();
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						LOGGER.error("Error saving player character: " + player.getName(), e);
					}
				}
			}
			LOGGER.info("[AutoSaveManager] AutoSaveTask, " + playerscount + " players data saved.");
		}
	}
	
	protected class ConnectionCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			
			for (final L2PcInstance player : players)
			{
				if (player != null && !player.isInOfflineMode())
				{
					if (player.getClient() == null || player.isOnline() == 0)
					{
						LOGGER.info("[AutoSaveManager] Player " + player.getName() + " status == 0 ---> Closing Connection..");
						player.store();
						player.deleteMe();
					}
					else if (!player.getClient().isConnectionAlive())
					{
						try
						{
							LOGGER.info("[AutoSaveManager] Player " + player.getName() + " connection is not alive ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (final Exception e)
						{
							LOGGER.error("[AutoSaveManager] Error saving player character: " + player.getName(), e);
						}
					}
					else if (player.checkTeleportOverTime())
					{
						try
						{
							LOGGER.info("[AutoSaveManager] Player " + player.getName() + " has a teleport overtime ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (final Exception e)
						{
							LOGGER.error("[AutoSaveManager] Error saving player character: " + player.getName(), e);
						}
					}
				}
			}
			LOGGER.info("[AutoSaveManager] ConnectionCheckTask, players connections checked.");
		}
	}
	
	protected class AutoCleanDBTask implements Runnable
	{
		
		@Override
		public void run()
		{
			int erased = 0;
			
			/*
			 * Perform the clean here instead of every time that the skills are saved in order to do it in once step because if skill have 0 reuse delay doesn't affect the game, just makes the table grows bigger
			 */
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_skills_save WHERE reuse_delay=0 && restore_type=1");
				erased = statement.executeUpdate();
				DatabaseUtils.close(statement);
				statement = null;
			}
			catch (final Exception e)
			{
				LOGGER.info("[AutoSaveManager] Error while cleaning skill with 0 reuse time from table.");
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			finally
			{
				CloseUtil.close(con);
			}
			
			LOGGER.info("[AutoSaveManager] AutoCleanDBTask, " + erased + " entries cleaned from db.");
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSaveManager _instance = new AutoSaveManager();
	}
}