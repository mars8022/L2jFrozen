/* L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author la2 Lets drink to code!
 */
public class DecayTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(DecayTaskManager.class);
	protected Map<L2Character, Long> _decayTasks = new FastMap<L2Character, Long>().shared();
	
	private static DecayTaskManager _instance;
	
	public DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, 5000);
	}
	
	public static DecayTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new DecayTaskManager();
		}
		
		return _instance;
	}
	
	public void addDecayTask(final L2Character actor)
	{
		_decayTasks.put(actor, System.currentTimeMillis());
	}
	
	public void addDecayTask(final L2Character actor, final int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(final L2Character actor)
	{
		try
		{
			_decayTasks.remove(actor);
		}
		catch (final NoSuchElementException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}
	
	private class DecayScheduler implements Runnable
	{
		protected DecayScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			int delay;
			try
			{
				if (_decayTasks != null)
				{
					for (final L2Character actor : _decayTasks.keySet())
					{
						if (actor instanceof L2RaidBossInstance)
						{
							delay = 30000;
						}
						else
						{
							delay = 8500;
						}
						if (current - _decayTasks.get(actor) > delay)
						{
							actor.onDecay();
							_decayTasks.remove(actor);
						}
					}
				}
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				// TODO: Find out the reason for exception. Unless caught here, mob decay would stop.
				LOGGER.warn(e.toString());
			}
		}
	}
	
	@Override
	public String toString()
	{
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + _decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";
		
		final Long current = System.currentTimeMillis();
		for (final L2Character actor : _decayTasks.keySet())
		{
			ret += "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current - _decayTasks.get(actor)) + "\r\n";
		}
		
		return ret;
	}
}
