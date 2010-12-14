package com.l2jfrozen.gameserver.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.Config;

public class AutoVoteRewardHandler
{
	private int hopzoneVotesCount = 0;
	private int topzoneVotesCount = 0;
	private List<String> already_rewarded;
	
	private AutoVoteRewardHandler()
	{
		System.out.println("Vote Reward System Initiated.");
		int hopzone_votes = getHopZoneVotes();
		setHopZoneVoteCount(hopzone_votes);
		int topzone_votes = getTopZoneVotes();
		setTopZoneVoteCount(topzone_votes);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), PowerPakConfig.VOTES_SYSYEM_INITIAL_DELAY, PowerPakConfig.VOTES_SYSYEM_STEP_DELAY);
	}

	private class AutoReward implements Runnable
	{
		public void run()
		{
			int topzone_votes = getTopZoneVotes();
			int hopzone_votes = getHopZoneVotes();
			
			/*
			String topzone = "";
			if(PowerPakConfig.VOTES_SITE_URL.contains("l2topzone.com")){
				site = "TOPZONE";
				votes = getVotesTopZone();
			}else if(PowerPakConfig.VOTES_SITE_URL.contains("l2.hopzone.net")){
				site = "HOPZONE";
				votes = getVotes();
			}
			*/
			
			System.out.println("Server TOPZONE Votes: " + topzone_votes);
			System.out.println("Server HOPZONE Votes: " + hopzone_votes);
			
			if (hopzone_votes != 0 && hopzone_votes >= getHopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD)
			{
				already_rewarded = new ArrayList<String>();
				
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

				//L2ItemInstance item;
				for (L2PcInstance player : pls)
				{
					if (player != null)
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
			
			if (topzone_votes != 0 && topzone_votes >= getTopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD)
			{
				already_rewarded = new ArrayList<String>();
				
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();

				//L2ItemInstance item;
				for (L2PcInstance player : pls)
				{
					if (player != null)
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
			
			int minutes = (PowerPakConfig.VOTES_SYSYEM_STEP_DELAY/1000)/60;
			
			Announcements.getInstance().gameAnnounceToAll("Actual HOPZONE Votes are " + hopzone_votes + "...");
			Announcements.getInstance().gameAnnounceToAll("Actual TOPZONE Votes are " + topzone_votes + "...");
			Announcements.getInstance().gameAnnounceToAll("Next HOPZONE Reward in "+minutes+" minutes at " + (getHopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD) + " Votes ;)");
			Announcements.getInstance().gameAnnounceToAll("Next TOPZONE Reward in "+minutes+" minutes at " + (getTopZoneVoteCount() + PowerPakConfig.VOTES_FOR_REWARD) + " Votes ;)");
			/*if (getLastVoteCount() == 0)
			{
				setInitialVoteCount(votes);
			}*/
		}
	}

	private boolean checkSingleBox(L2PcInstance player){
		
		if(player.getClient()!=null && player.getClient().getConnection()!=null && !player.getClient().getConnection().isClosed()){
			
			String playerip = player.getClient().getConnection().getSocketChannel().socket().getInetAddress().getHostAddress();
			
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
			{}
			try
			{
				isr.close();
			}
			catch (IOException e)
			{}
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
			{}
			try
			{
				isr.close();
			}
			catch (IOException e)
			{}
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
		if(PowerPakConfig.VOTES_SITE_HOPZONE_URL != null && !PowerPakConfig.VOTES_SITE_HOPZONE_URL.equals("") &&
				PowerPakConfig.VOTES_SITE_TOPZONE_URL != null && !PowerPakConfig.VOTES_SITE_TOPZONE_URL.equals(""))
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