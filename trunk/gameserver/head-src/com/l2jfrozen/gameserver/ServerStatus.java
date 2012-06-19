package com.l2jfrozen.gameserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.Memory;
import com.l2jfrozen.util.Util;

public class ServerStatus
{
	protected static final Logger _log = Logger.getLogger("Loader");
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
			Util.printSection("Server Status");
			_log.info("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
			_log.info("Players Online: " + L2World.getInstance().getAllPlayers().size());
			_log.info("Threads: " + Thread.activeCount());
			_log.info("Free Memory: " + Memory.getFreeMemory() + " MB");
			_log.info("Used memory: " + Memory.getUsedMemory() + " MB");
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