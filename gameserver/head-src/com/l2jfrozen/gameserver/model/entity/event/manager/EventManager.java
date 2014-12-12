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
package com.l2jfrozen.gameserver.model.entity.event.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;

/**
 * @author Shyla
 */
public class EventManager
{
	protected static final Logger LOGGER = Logger.getLogger(EventManager.class);
	
	private final static String EVENT_MANAGER_CONFIGURATION_FILE = "./config/frozen/eventmanager.properties";
	
	public static boolean TVT_EVENT_ENABLED;
	public static ArrayList<String> TVT_TIMES_LIST;
	
	public static boolean CTF_EVENT_ENABLED;
	public static ArrayList<String> CTF_TIMES_LIST;
	
	public static boolean DM_EVENT_ENABLED;
	public static ArrayList<String> DM_TIMES_LIST;
	
	private static EventManager instance = null;
	
	private EventManager()
	{
		loadConfiguration();
	}
	
	public static EventManager getInstance()
	{
		
		if (instance == null)
		{
			instance = new EventManager();
		}
		return instance;
		
	}
	
	public static void loadConfiguration()
	{
		
		InputStream is = null;
		try
		{
			final Properties eventSettings = new Properties();
			is = new FileInputStream(new File(EVENT_MANAGER_CONFIGURATION_FILE));
			eventSettings.load(is);
			
			// ============================================================
			
			TVT_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TVTEventEnabled", "false"));
			TVT_TIMES_LIST = new ArrayList<>();
			
			String[] propertySplit;
			propertySplit = eventSettings.getProperty("TVTStartTime", "").split(";");
			
			for (final String time : propertySplit)
			{
				TVT_TIMES_LIST.add(time);
			}
			
			CTF_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("CTFEventEnabled", "false"));
			CTF_TIMES_LIST = new ArrayList<>();
			
			propertySplit = eventSettings.getProperty("CTFStartTime", "").split(";");
			
			for (final String time : propertySplit)
			{
				CTF_TIMES_LIST.add(time);
			}
			
			DM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("DMEventEnabled", "false"));
			DM_TIMES_LIST = new ArrayList<>();
			
			propertySplit = eventSettings.getProperty("DMStartTime", "").split(";");
			
			for (final String time : propertySplit)
			{
				DM_TIMES_LIST.add(time);
			}
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void startEventRegistration()
	{
		
		if (TVT_EVENT_ENABLED)
		{
			registerTvT();
		}
		
		if (CTF_EVENT_ENABLED)
		{
			registerCTF();
		}
		
		if (DM_EVENT_ENABLED)
		{
			registerDM();
		}
		
	}
	
	private static void registerTvT()
	{
		
		TvT.loadData();
		if (!TvT.checkStartJoinOk())
		{
			LOGGER.error("registerTvT: TvT Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(TvT.get_eventName());
		
		for (final String time : TVT_TIMES_LIST)
		{
			
			final TvT newInstance = TvT.getNewInstance();
			// LOGGER.info("registerTvT: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
			
		}
		
	}
	
	private static void registerCTF()
	{
		
		CTF.loadData();
		if (!CTF.checkStartJoinOk())
		{
			LOGGER.error("registerCTF: CTF Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(CTF.get_eventName());
		
		for (final String time : CTF_TIMES_LIST)
		{
			
			final CTF newInstance = CTF.getNewInstance();
			// LOGGER.info("registerCTF: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
			
		}
		
	}
	
	private static void registerDM()
	{
		DM.loadData();
		if (!DM.checkStartJoinOk())
		{
			LOGGER.error("registerDM: DM Event is not setted Properly");
		}
		
		// clear all tvt
		EventsGlobalTask.getInstance().clearEventTasksByEventName(DM.get_eventName());
		
		for (final String time : DM_TIMES_LIST)
		{
			
			final DM newInstance = DM.getNewInstance();
			// LOGGER.info("registerDM: reg.time: "+time);
			newInstance.setEventStartTime(time);
			EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
			
		}
	}
	
}
