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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Shyla
 */
public class EventsGlobalTask implements Runnable
{
	protected static final Logger _log = Logger.getLogger(EventsGlobalTask.class.getName());
	
	private static EventsGlobalTask instance;
	
	private boolean destroy = false;
	
	private Hashtable<String, ArrayList<EventTask>> time_to_tasks = new Hashtable<String,ArrayList<EventTask>>();  //time is in hh:mm 
	private Hashtable<String, ArrayList<EventTask>> eventid_to_tasks = new Hashtable<String,ArrayList<EventTask>>();
	
	
	private EventsGlobalTask(){
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
	
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
			
			_log.log(Level.SEVERE, "registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
			
		}
		
		ArrayList<EventTask> savedTasksForTime = time_to_tasks.get(event.getEventStartTime());
		ArrayList<EventTask> savedTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		
		if(savedTasksForTime!=null){
			
			if(!savedTasksForTime.contains(event)){
				savedTasksForTime.add(event);
			}
			
		}else{
		
			savedTasksForTime = new ArrayList<EventTask>();
			savedTasksForTime.add(event);
			
		}
		
		time_to_tasks.put(event.getEventStartTime(), savedTasksForTime);
		
		if(savedTasksForId!=null){
			
			if(!savedTasksForId.contains(event)){
				savedTasksForId.add(event);
			}
			
		}else{
		
			savedTasksForId = new ArrayList<EventTask>();
			savedTasksForId.add(event);
			
		}
		
		eventid_to_tasks.put(event.getEventIdentifier(), savedTasksForId);
		
		if(Config.DEBUG){
			System.out.println("Added Event: "+event.getEventIdentifier());
			
			//check Info
			for(String time:time_to_tasks.keySet()){
				
				//System.out.println("--Time: "+time);
				ArrayList<EventTask> tasks = time_to_tasks.get(time);
				
				Iterator<EventTask> taskIt = tasks.iterator();
				
				while(taskIt.hasNext()){
					EventTask actual_event = taskIt.next();
					System.out.println("	--Registered Event: "+actual_event.getEventIdentifier());
				}
				
			}
			
			for(String event_id:eventid_to_tasks.keySet()){
				
				System.out.println("--Event: "+event_id);
				ArrayList<EventTask> times = eventid_to_tasks.get(event_id);
				
				Iterator<EventTask> timesIt = times.iterator();
				
				while(timesIt.hasNext()){
					EventTask actual_time = timesIt.next();
					System.out.println("	--Registered Time: "+actual_time.getEventStartTime());
				}
				
			}
		}
		
		
	}
	
	public void clearEventTasksByEventName(String eventId){
		
		if(eventId==null){
			_log.log(Level.SEVERE, "registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if(eventId.equalsIgnoreCase("all")){
			
			time_to_tasks.clear();
			eventid_to_tasks.clear();
			
		}else{
			
			ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(eventId);
			
			if(oldTasksForId!=null){
				
				for(EventTask actual:oldTasksForId){
					
					ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(actual.getEventStartTime());
					
					if(oldTasksForTime!=null){
						
						oldTasksForTime.remove(actual);
						
						time_to_tasks.put(actual.getEventStartTime(),oldTasksForTime);
						
					}
					
					
				}
				
				eventid_to_tasks.remove(eventId);
				
			}
		
		}
		
	}
	
	public void deleteEventTask(EventTask event){
		
		if(event==null || event.getEventIdentifier() == null || event.getEventIdentifier().equals("") || 
				event.getEventStartTime() == null || event.getEventStartTime().equals("")){
			
			_log.log(Level.SEVERE, "registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
			
		}
		
		if(this.time_to_tasks.size()<0){
			return;
		}
		
		ArrayList<EventTask> oldTasksForId = eventid_to_tasks.get(event.getEventIdentifier());
		ArrayList<EventTask> oldTasksForTime = time_to_tasks.get(event.getEventStartTime());
		
		if(oldTasksForId!=null){
			oldTasksForId.remove(event);
			eventid_to_tasks.put(event.getEventIdentifier(), oldTasksForId);
		}
		
		if(oldTasksForTime!=null){
			oldTasksForTime.remove(event);
			time_to_tasks.put(event.getEventStartTime(), oldTasksForTime);
		}
		
		
	}
	
	private void checkRegisteredEvents(){
		
		if(this.time_to_tasks.size()<0){
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
		
		//System.out.println("Current Time: "+currentTime);
		ArrayList<EventTask> registeredEventsAtCurrentTime = time_to_tasks.get(currentTime);
		
		if(registeredEventsAtCurrentTime!=null){
			for(EventTask actualEvent: registeredEventsAtCurrentTime){
				
				ThreadPoolManager.getInstance().scheduleGeneral(actualEvent, 5000);
				
			}
		}
	}
	
	public void destroyLocalInstance(){
		destroy = true;
		instance = null;
	}

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
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			
		}
	}
	
}
