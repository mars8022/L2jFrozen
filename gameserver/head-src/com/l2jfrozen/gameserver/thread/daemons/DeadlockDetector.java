/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.thread.daemons;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.thread.L2Thread;
import com.l2jfrozen.util.Util;

/**
 * @author ProGramMoS
 * @version 0.4 Stable
 */
public final class DeadlockDetector implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(DeadlockDetector.class);
	private final Set<Long> _logged = new FastSet<>();
	
	private static DeadlockDetector _instance;
	
	public static DeadlockDetector getInstance()
	{
		if (_instance == null)
		{
			_instance = new DeadlockDetector();
		}
		
		return _instance;
	}
	
	private DeadlockDetector()
	{
		LOGGER.info("DeadlockDetector daemon started.");
	}
	
	@Override
	public void run()
	{
		final long[] ids = findDeadlockedThreadIDs();
		
		if (ids == null)
			return;
		
		final List<Thread> deadlocked = new ArrayList<>();
		
		for (final long id : ids)
			if (_logged.add(id))
			{
				deadlocked.add(findThreadById(id));
			}
		
		if (!deadlocked.isEmpty())
		{
			Util.printSection("Deadlocked Thread(s)");
			
			for (final Thread thread : deadlocked)
			{
				for (final String line : L2Thread.getStats(thread))
				{
					LOGGER.error(line);
				}
			}
			
			Util.printSection("End");
		}
	}
	
	private long[] findDeadlockedThreadIDs()
	{
		if (ManagementFactory.getThreadMXBean().isSynchronizerUsageSupported())
			return ManagementFactory.getThreadMXBean().findDeadlockedThreads();
		return ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
	}
	
	private Thread findThreadById(final long id)
	{
		for (final Thread thread : Thread.getAllStackTraces().keySet())
			if (thread.getId() == id)
				return thread;
		
		throw new IllegalStateException("Deadlocked Thread not found!");
		
	}
	
}
