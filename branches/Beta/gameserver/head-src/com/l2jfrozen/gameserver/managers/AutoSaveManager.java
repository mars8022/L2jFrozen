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
package com.l2jfrozen.gameserver.managers;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * <b>AutoSave</b> only
 * @author Shyla
 */
public class AutoSaveManager
{
	protected static final Logger _log = Logger.getLogger(AutoSaveManager.class.getName());
	private ScheduledFuture<?> _autoSaveInDB;
	private ScheduledFuture<?> _autoCheckConnectionStatus;
	
	public static final AutoSaveManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public AutoSaveManager()
	{
		_log.info("Initializing AutoSaveManager");
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
	}
	
	public void startAutoSaveManager()
	{
		
		stopAutoSaveManager();
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		_autoCheckConnectionStatus = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PlayersSaveTask(), Config.CHECK_CONNECTION_INITIAL_TIME, Config.CHECK_CONNECTION_DELAY_TIME);
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("AutoSaveManager: saving players data..");
			
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			
			for (final L2PcInstance player : players)
			{
				
				if (player != null)
				{
					try
					{
						player.store();
					}
					catch (Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						_log.log(Level.SEVERE, "Error saving player character: " + player.getName(), e);
					}
				}
			}
			_log.info("AutoSaveManager: players data saved..");
		}
	}
	
	protected class PlayersSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.info("AutoSaveManager: checking players connection..");
			
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			
			for (final L2PcInstance player : players)
			{			
				if (player != null && !player.isOffline())
				{
					if (player.getClient() == null || player.isOnline() == 0)
					{
						_log.info("AutoSaveManager: player " + player.getName() + " status == 0 ---> Closing Connection..");
						player.store();
						player.deleteMe();
					}
					else if (!player.getClient().isConnectionAlive())
					{
						try
						{
							_log.info("AutoSaveManager: player " + player.getName() + " connection is not alive ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (Exception e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
								e.printStackTrace();
							
							_log.log(Level.SEVERE, "Error saving player character: " + player.getName(), e);
						}
					}
					else if (player.checkTeleportOverTime())
					{
						try
						{
							_log.info("AutoSaveManager: player " + player.getName() + " has a teleport overtime ---> Closing Connection..");
							player.getClient().onDisconnection();
						}
						catch (Exception e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
								e.printStackTrace();
							
							_log.log(Level.SEVERE, "Error saving player character: " + player.getName(), e);
						}
					}
				}
			}
			
			if (Config.DEBUG)
				_log.info("AutoSaveManager: players connections checked..");
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSaveManager _instance = new AutoSaveManager();
	}
}