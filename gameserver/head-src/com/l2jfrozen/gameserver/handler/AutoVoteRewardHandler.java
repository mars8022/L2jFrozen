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
	private int votesCount = 0;
	private List<String> already_rewarded;
	
	private AutoVoteRewardHandler()
	{
		System.out.println("Vote Reward System Initiated.");
		int votes = getVotes();
		setVoteCount(votes);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoReward(), PowerPakConfig.VOTES_SYSYEM_INITIAL_DELAY, PowerPakConfig.VOTES_SYSYEM_STEP_DELAY);
	}

	private class AutoReward implements Runnable
	{
		public void run()
		{
			int votes = getVotes();
			System.out.println("Server Votes: " + votes);
			if (votes != 0 && getVoteCount() != 0 && votes >= getVoteCount() + PowerPakConfig.VOTES_FOR_REWARD)
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
				setVoteCount(getVoteCount() + PowerPakConfig.VOTES_FOR_REWARD);
			}
			
			int minutes = (PowerPakConfig.VOTES_SYSYEM_STEP_DELAY/1000)/60;
			
			Announcements.getInstance().gameAnnounceToAll("Actual HOPZONE Votes are " + votes + "...");
			Announcements.getInstance().gameAnnounceToAll("Next Reward in "+minutes+" minutes at " + (getVoteCount() + PowerPakConfig.VOTES_FOR_REWARD) + " Votes ;)");
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
	
	private int getVotes()
	{
		URL url = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(PowerPakConfig.VOTES_SITE_URL);
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

	private void setVoteCount(int voteCount)
	{
		votesCount = voteCount;
	}

	private int getVoteCount()
	{
		return votesCount;
	}

	public static AutoVoteRewardHandler getInstance()
	{
		if(PowerPakConfig.VOTES_SITE_URL != null && !PowerPakConfig.VOTES_SITE_URL.equals(""))
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