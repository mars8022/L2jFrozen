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
package com.l2jfrozen.gameserver.script.faenor;

import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2PetData;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.script.DateRange;
import com.l2jfrozen.gameserver.script.EngineInterface;
import com.l2jfrozen.gameserver.script.EventDroplist;
import com.l2jfrozen.gameserver.script.Expression;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{
	protected static final Logger LOGGER = Logger.getLogger(FaenorInterface.class);
	
	public static FaenorInterface getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private FaenorInterface()
	{
	}
	
	public List<?> getAllPlayers()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Adds a new Quest Drop to an NPC
	 * @see com.l2jfrozen.gameserver.script.EngineInterface#addQuestDrop(int, int, int, int, int, String, String[])
	 */
	@Override
	public void addQuestDrop(final int npcID, final int itemID, final int min, final int max, final int chance, final String questID, final String[] states)
	{
		final L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
		{
			LOGGER.info("FeanorInterface: Npc " + npcID + " is null..");
			return;
		}
		final L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		drop.setQuestID(questID);
		drop.addStates(states);
		addDrop(npc, drop, false);
	}
	
	/**
	 * Adds a new Drop to an NPC
	 * @param npcID
	 * @param itemID
	 * @param min
	 * @param max
	 * @param sweep
	 * @param chance
	 * @throws NullPointerException
	 * @see com.l2jfrozen.gameserver.script.EngineInterface#addQuestDrop(int, int, int, int, int, String, String[])
	 */
	public void addDrop(final int npcID, final int itemID, final int min, final int max, final boolean sweep, final int chance) throws NullPointerException
	{
		final L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("Npc doesnt Exist");
			}
			throw new NullPointerException();
		}
		final L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		
		addDrop(npc, drop, sweep);
	}
	
	/**
	 * Adds a new drop to an NPC. If the drop is sweep, it adds it to the NPC's Sweep category If the drop is non-sweep, it creates a new category for this drop.
	 * @param npc
	 * @param drop
	 * @param sweep
	 */
	public void addDrop(final L2NpcTemplate npc, final L2DropData drop, final boolean sweep)
	{
		if (sweep)
		{
			addDrop(npc, drop, -1);
		}
		else
		{
			int maxCategory = -1;
			
			if (npc.getDropData() != null)
			{
				for (final L2DropCategory cat : npc.getDropData())
				{
					if (maxCategory < cat.getCategoryType())
					{
						maxCategory = cat.getCategoryType();
					}
				}
			}
			maxCategory++;
			npc.addDropData(drop, maxCategory);
		}
		
	}
	
	/**
	 * Adds a new drop to an NPC, in the specified category. If the category does not exist, it is created.
	 * @param npc
	 * @param drop
	 * @param category
	 */
	public void addDrop(final L2NpcTemplate npc, final L2DropData drop, final int category)
	{
		npc.addDropData(drop, category);
	}
	
	/**
	 * @param npcID
	 * @return Returns the _questDrops.
	 */
	public List<L2DropData> getQuestDrops(final int npcID)
	{
		final L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
			return null;
		final List<L2DropData> questDrops = new FastList<>();
		if (npc.getDropData() != null)
		{
			for (final L2DropCategory cat : npc.getDropData())
			{
				for (final L2DropData drop : cat.getAllDrops())
				{
					if (drop.getQuestID() != null)
					{
						questDrops.add(drop);
					}
				}
			}
		}
		return questDrops;
	}
	
	@Override
	public void addEventDrop(final int[] items, final int[] count, final double chance, final DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int) (chance * L2DropData.MAX_CHANCE), range);
	}
	
	@Override
	public void onPlayerLogin(final String[] message, final DateRange validDateRange)
	{
		Announcements.getInstance().addEventAnnouncement(validDateRange, message);
	}
	
	public void addPetData(final ScriptContext context, final int petID, final int levelStart, final int levelEnd, final Map<String, String> stats)
	{
		final L2PetData[] petData = new L2PetData[levelEnd - levelStart + 1];
		int value = 0;
		for (int level = levelStart; level <= levelEnd; level++)
		{
			petData[level - 1] = new L2PetData();
			petData[level - 1].setPetID(petID);
			petData[level - 1].setPetLevel(level);
			
			context.setAttribute("level", new Double(level), ScriptContext.ENGINE_SCOPE);
			for (final String stat : stats.keySet())
			{
				value = ((Number) Expression.eval(context, "beanshell", stats.get(stat))).intValue();
				petData[level - 1].setStat(stat, value);
			}
			context.removeAttribute("level", ScriptContext.ENGINE_SCOPE);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FaenorInterface _instance = new FaenorInterface();
	}
}
