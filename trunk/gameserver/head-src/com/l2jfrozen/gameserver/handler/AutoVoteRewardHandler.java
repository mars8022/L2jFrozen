package com.l2jfrozen.gameserver.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class AutoVoteRewardHandler
{
	private int hopzoneVotesCount = 0;
	private int topzoneVotesCount = 0;
	private List<String> already_rewarded;
	
	private static boolean topzone = false;
	private static boolean hopzone = false;
	
	private AutoVoteRewardHandler()
	{
		System.out.println("Vote Reward System Initiated.");
		
		if(hopzone){
			int hopzone_votes = getHopZoneVotes();
			setHopZoneVoteCount(hopzone_votes);
		}
		
		if(topzone){
			int topzone_votes = getTopZoneVotes();
			setTopZoneVoteCount(topzone_votes);
		}
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), PowerPakConfig.VOTES_SYSYEM_INITIAL_DELAY, PowerPakConfig.VOTES_SYSYEM_STEP_DELAY);
	}

	private class AutoReward implements Runnable
	{
		public void run()
		{
			int minutes = (PowerPakConfig.VOTES_SYSYEM_STEP_DELAY/1000)/60;
			
			if(hopzone){
				int hopzone_votes = getHopZoneVotes();
				
				System.out.println("[AutoVoteReward] Server HOPZONE Votes: " + hopzone_votes);
				Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Actual HOPZONE Votes are " + hopzone_votes + "...");
				
				if (hopzone_votes != 0 && hopzone_votes >= getHopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD)
				{
					already_rewarded = new ArrayList<String>();
					
					Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

					Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Great Work! Check your inventory for Reward!!");
					
					//L2ItemInstance item;
					for (L2PcInstance player : pls)
					{
						if (player != null && !player.isOffline())
						{
							if(player._active_boxes<=1 || (player._active_boxes>1 && checkSingleBox(player))){
								
								Set<Integer> items = PowerPakConfig.VOTES_REWARDS_LIST.keySet();
								for (Integer i : items)
								{
									//item = player.getInventory().getItemByItemId(i);

									//TODO: check on maxstack for item
									player.addItem("reward", i, PowerPakConfig.VOTES_REWARDS_LIST.get(i), player, true);

								}
								
							}
						}
					}
					setHopZoneVoteCount(hopzone_votes);
				}
				Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Next HOPZONE Reward in "+minutes+" minutes at " + (getHopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD) + " Votes!!");
				//site web
				Announcements.getInstance().gameAnnounceToAll("[SiteWeb] "+PowerPakConfig.SERVER_WEB_SITE);
				
			}
			
			if(topzone && hopzone && PowerPakConfig.VOTES_SYSYEM_STEP_DELAY>0)
				try
				{
					Thread.sleep(PowerPakConfig.VOTES_SYSYEM_STEP_DELAY/2);
				}
				catch(InterruptedException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
				}
				
			if(topzone){
				int topzone_votes = getTopZoneVotes();
				
				System.out.println("[AutoVoteReward] Server TOPZONE Votes: " + topzone_votes);
				Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Actual TOPZONE Votes are " + topzone_votes + "...");
				
				if (topzone_votes != 0 && topzone_votes >= getTopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD)
				{
					already_rewarded = new ArrayList<String>();
					
					Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

					Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Great Work! Check your inventory for Reward!!");
					
					//L2ItemInstance item;
					for (L2PcInstance player : pls)
					{
						if (player != null && !player.isOffline())
						{
							if(player._active_boxes<=1 || (player._active_boxes>1 && checkSingleBox(player))){
								
								Set<Integer> items = PowerPakConfig.VOTES_REWARDS_LIST.keySet();
								for (Integer i : items)
								{
									//item = player.getInventory().getItemByItemId(i);

									//TODO: check on maxstack for item
									player.addItem("reward", i, PowerPakConfig.VOTES_REWARDS_LIST.get(i), player, true);

								}
								
							}
						}
					}
					setTopZoneVoteCount(topzone_votes);
				}
				
				Announcements.getInstance().gameAnnounceToAll("[AutoVoteReward] Next TOPZONE Reward in "+minutes+" minutes at " + (getTopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD) + " Votes!!");
				//site web
				Announcements.getInstance().gameAnnounceToAll("[SiteWeb] "+PowerPakConfig.SERVER_WEB_SITE);
				
			}
			
		}
	}

	private boolean checkSingleBox(L2PcInstance player){
		
		if(player.getClient()!=null && player.getClient().getConnection()!=null && !player.getClient().getConnection().isClosed() && !player.isOffline()){
			
			String playerip = player.getClient().getConnection().getInetAddress().getHostAddress();
			
			if(already_rewarded.contains(playerip))
				return false;
			else{
				already_rewarded.add(playerip);
				return true;
			}
		}
		
		//if no connection (maybe offline shop) dnt reward
		return false;
	}
	
	private int getHopZoneVotes()
	{
		URL url = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(PowerPakConfig.VOTES_SITE_HOPZONE_URL);
			isr = new InputStreamReader(url.openStream());
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("moreinfo_total_rank_text"))
				{
					return Integer.valueOf(inputLine.split(">")[2].replace("</div", ""));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			try
			{
				isr.close();
			}
			catch (IOException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
		return 0;
	}

	private int getTopZoneVotes()
	{
		URL url = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(PowerPakConfig.VOTES_SITE_TOPZONE_URL);
			isr = new InputStreamReader(url.openStream());
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("Votes"))
				{
					String votesLine = in.readLine() ;
					
					return Integer.valueOf(votesLine.split(">")[5].replace("</font", ""));
					
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			try
			{
				isr.close();
			}
			catch (IOException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
		return 0;
	}
	
	private void setHopZoneVoteCount(int voteCount)
	{
		hopzoneVotesCount = voteCount;
	}

	private int getHopZoneVoteCount()
	{
		return hopzoneVotesCount;
	}

	private void setTopZoneVoteCount(int voteCount)
	{
		topzoneVotesCount = voteCount;
	}

	private int getTopZoneVoteCount()
	{
		return topzoneVotesCount;
	}
	
	public static AutoVoteRewardHandler getInstance()
	{
		if(PowerPakConfig.VOTES_SITE_HOPZONE_URL != null && !PowerPakConfig.VOTES_SITE_HOPZONE_URL.equals("")){
			hopzone = true;
		}
		
		if(PowerPakConfig.VOTES_SITE_TOPZONE_URL != null && !PowerPakConfig.VOTES_SITE_TOPZONE_URL.equals("")){
			topzone = true;
		}
		
		if(topzone || hopzone)
			return SingletonHolder._instance;
		else
			return null;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoVoteRewardHandler    _instance       = new AutoVoteRewardHandler();
	}
}