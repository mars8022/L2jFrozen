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
 * 
 * @author Shyla
 */
public class AutoSaveManager
{
	private static final Logger _log = Logger.getLogger(AutoSaveManager.class.getName());
	private ScheduledFuture<?> _autoSaveInDB;
	
	public static final AutoSaveManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public AutoSaveManager()
	{
		_log.info("Initializing AutoSaveManager");
	}
	
	public void stopAutoSaveManager(){
		
		if(_autoSaveInDB!=null){
			_autoSaveInDB.cancel(true);
			_autoSaveInDB = null;
		}
		
	}
	
	public void startAutoSaveManager(){
		
		stopAutoSaveManager();
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		
	}

	protected class AutoSaveTask implements Runnable{
		
		@Override
		public void run()
		{
			
			_log.info("AutoSaveManager: saving players data..");
			
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			
			for(final L2PcInstance player:players){
				
				if(player != null)
				{
					try
					{
						player.store();
					}
					catch(Exception e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						_log.log(Level.SEVERE, "Error saving player character: " + player.getName(), e);
					}
				}
				
			}
			
			_log.info("AutoSaveManager: players data saved..");
			
		}
		
	}
	

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoSaveManager _instance = new AutoSaveManager();
	}
	
}
