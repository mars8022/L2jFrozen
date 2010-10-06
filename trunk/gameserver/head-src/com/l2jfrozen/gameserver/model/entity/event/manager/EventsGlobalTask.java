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
package com.l2jfrozen.gameserver.model.entity.event.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author Shyla
 */
public class EventsGlobalTask implements Runnable
{
	private final static Log _log = LogFactory.getLog(EventsGlobalTask.class.getName());
	
	private static EventsGlobalTask instance;
	
	private Thread local_thread;
	private boolean destroy = false;
	
	private Hashtable<String, ArrayList<EventTask>> time_to_events = new Hashtable<String,ArrayList<EventTask>>();  //time is in hour-minutes 
	private Hashtable<String, String> event_to_time = new Hashtable<String,String>(); //time is in hour-minutes

	
	
	private EventsGlobalTask(){
		
		local_thread = new Thread(this);
		local_thread.start();
		
	}
	
	public static EventsGlobalTask getInstance(){
		
		if(instance==null){
			instance = new EventsGlobalTask();
		}
		
		return instance;
		
	}
	
	public void registerNewEventTask(EventTask event){
		
		if(event==null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || 
				event.getEventStartTime()==null || event.getEventStartTime().equals("")){
			
			_log.error("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
			
		}
		
		//remove previous save
		if(event_to_time.get(event.getEventIdentifier())!=null){
			
			String old_time = event_to_time.remove(event.getEventIdentifier());
			
			ArrayList<EventTask> savedTaks = time_to_events.get(old_time);
			
			for(EventTask actual:savedTaks){
				if(actual.getEventIdentifier().equals(event.getEventIdentifier())){
						savedTaks.remove(actual);
						break;
				}
			}
			
			time_to_events.put(old_time, savedTaks);
			
		}
		
		ArrayList<EventTask> savedTaks;
		
		if(time_to_events.get(event.getEventStartTime())==null){
			savedTaks = new ArrayList<EventTask>();
		}else{
			savedTaks = time_to_events.get(event.getEventStartTime());
		}
		
		savedTaks.add(event);
		
		time_to_events.put(event.getEventStartTime(), savedTaks);
		event_to_time.put(event.getEventIdentifier(), event.getEventStartTime());
		
		System.out.println("Added Event: "+event.getEventIdentifier());
		
	}
	
	public void registerEventTasks(EventTask event){ //register more then 1 task for event, based on multitimes
		
		if(event==null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || 
				event.getEventStartTime()==null || event.getEventStartTime().equals("")){
			
			_log.error("registerEventTasks: eventTask must be not null as its identifier and startTime ");
			return;
			
		}
		/*
		//remove previous save
		if(event_to_time.get(event.getEventIdentifier())!=null){
			
			String old_time = event_to_time.remove(event.getEventIdentifier());
			
			ArrayList<EventTask> savedTaks = time_to_events.get(old_time);
			
			for(EventTask actual:savedTaks){
				if(actual.getEventIdentifier().equals(event.getEventIdentifier())){
						savedTaks.remove(actual);
						break;
				}
			}
			
			time_to_events.put(old_time, savedTaks);
			
		}
		
		ArrayList<EventTask> savedTaks;
		
		if(time_to_events.get(event.getEventStartTime())==null){
			savedTaks = new ArrayList<EventTask>();
		}else{
			savedTaks = time_to_events.get(event.getEventStartTime());
		}
		
		savedTaks.add(event);
		
		time_to_events.put(event.getEventStartTime(), savedTaks);
		event_to_time.put(event.getEventIdentifier(), event.getEventStartTime());
		*/
	}
	
	public void deleteEventTask(EventTask event){
		
		if(event==null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || 
				event.getEventStartTime() == null || event.getEventStartTime().equals("")){
			
			_log.error("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
			
		}
		
		if(this.time_to_events.size()<0){
			return;
		}
		
		String oldNotificationTime = event_to_time.remove(event.getEventIdentifier());
		if(oldNotificationTime!=null)
			time_to_events.remove(oldNotificationTime);
		
	}
	
	public void deleteEventsAtTime(String time){
		
		if(this.time_to_events.size()<0){
			return;
		}
		
		ArrayList<EventTask> registeredEventsAtTime = time_to_events.get(time);
		
		if(registeredEventsAtTime!=null){
			for(EventTask actualEvent: registeredEventsAtTime){
				deleteEventTask(actualEvent);
			}
			
		}
		
	}
	
	private void checkRegisteredEvents(){
		
		if(this.time_to_events.size()<0){
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		
		String hourStr = "";
		String minStr = "";
		
		if(hour<10){
			hourStr = "0"+hour;
		}else
			hourStr = ""+hour;
		
		if(min<10){
			minStr = "0"+min;
		}else
			minStr = ""+min;
		
		String currentTime = hourStr+":"+minStr;
		
		System.out.println("Current Time: "+currentTime);
		ArrayList<EventTask> registeredEventsAtCurrentTime = time_to_events.get(currentTime);
		
		if(registeredEventsAtCurrentTime!=null){
			for(EventTask actualEvent: registeredEventsAtCurrentTime){
				
				actualEvent.notifyEventStart();
				
			}
		}
	}
	
	public void destroyLocalInstance(){
		destroy = true;
		local_thread.getThreadGroup().destroy();
		instance = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		
		while(!destroy){//start time checker
			
			checkRegisteredEvents();
			
			try
			{
				Thread.sleep(60000); //1 minute
			}
			catch(InterruptedException e)
			{
				//ignore
			}
			
		}
	}
	
}
