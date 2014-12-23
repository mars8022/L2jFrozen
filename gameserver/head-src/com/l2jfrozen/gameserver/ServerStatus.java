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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.Memory;
import com.l2jfrozen.util.Util;

/**
 * Server status
 * @author Nefer
 * @version 1.0
 */
public class ServerStatus
{
	protected static final Logger LOGGER = Logger.getLogger("Loader");
	protected ScheduledFuture<?> _scheduledTask;
	
	protected ServerStatus()
	{
		_scheduledTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ServerStatusTask(), 1800000, 3600000);
	}
	
	protected class ServerStatusTask implements Runnable
	{
		protected final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
		
		@Override
		public void run()
		{
			int ActivePlayers = 0;
			int OfflinePlayers = 0;
			
			for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player.isInOfflineMode())
					OfflinePlayers++;
				else
					ActivePlayers++;
			}
			
			Util.printSection("Server Status");
			LOGGER.info("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
			LOGGER.info("Active Players Online: " + ActivePlayers);
			LOGGER.info("Offline Players Online: " + OfflinePlayers);
			LOGGER.info("Threads: " + Thread.activeCount());
			LOGGER.info("Free Memory: " + Memory.getFreeMemory() + " MB");
			LOGGER.info("Used memory: " + Memory.getUsedMemory() + " MB");
			Util.printSection("Server Status");
		}
	}
	
	public static ServerStatus getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerStatus _instance = new ServerStatus();
	}
}