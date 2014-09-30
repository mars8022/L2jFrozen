package com.l2jfrozen.gameserver.powerpak.engrave;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.State;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;

public class EngraveQuest extends Quest
{
	private static String qn = "8008_Engrave";

	public EngraveQuest()
	{
		super(8008, qn, "Engrave");
		setInitialState(new State("Start", this));
		if(PowerPakConfig.SPAWN_ENGRAVER)
		{
			addSpawn(50018, PowerPakConfig.ENGRAVER_X, PowerPakConfig.ENGRAVER_Y, PowerPakConfig.ENGRAVER_Z, 0, false, 0);
			System.out.println("...spawned engraver");
		}
		addStartNpc(50018);
		addTalkId(50018);
	}

	private interface CondChecker
	{
		public boolean check(L2ItemInstance item, L2PcInstance player);
	}

	private String buildList(L2PcInstance player, int startWith, String baseAction, String action, CondChecker checker)
	{
		String htm = "<table width=300>";
		int i = 0;
		int numadded = 0;
		boolean endreached = true;
		for(L2ItemInstance it : player.getInventory().getItems())
		{
			if(i++ < startWith)
			{
				continue;
			}
			if(numadded == 20)
			{
				endreached = false;
				break;
			}
			if(checker.check(it, player))
			{
				numadded++;
				htm += "<tr><td><a action=\"bypass -h Quest 8008_Engrave " + action + "_" + it.getObjectId() + "\">" + it.getItemName() + "</a></td></tr>";
			}
		}

		htm += "</table>";
		if(!endreached)
		{
			htm += "<br1><center><a action=\"bypass -h Quest 8008_Engrave " + baseAction + "_" + i + "\">more...</a><center>";
		}
		return htm;
	}

	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htm = HtmCache.getInstance().getHtm("data/html/default/50018-3.htm");
		if(event.startsWith("mark") || event.startsWith("clear"))
		{
			int iPos = event.lastIndexOf("_");
			int objectId = 0;
			if(iPos > 0)
			{
				try
				{
					objectId = Integer.parseInt(event.substring(iPos + 1));
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					return htm;
				}
			}
			else
				return htm;
			L2ItemInstance it = player.getInventory().getItemByObjectId(objectId);
			if(it != null)
			{
				if(PowerPakConfig.ENGRAVE_PRICE > 0 && PowerPakConfig.ENGRAVE_PRICE_ITEM > 0 && event.startsWith("mark"))
				{
					L2ItemInstance pit = player.getInventory().getItemByItemId(PowerPakConfig.ENGRAVE_PRICE_ITEM);
					if(pit == null || pit.getCount() < PowerPakConfig.ENGRAVE_PRICE)
					{
						htm = HtmCache.getInstance().getHtm("data/html/default/50018-6.htm");
						htm = htm.replace("%itemname%", ItemTable.getInstance().getTemplate(PowerPakConfig.ENGRAVE_PRICE_ITEM).getName());
						htm = htm.replace("%count%", String.valueOf(PowerPakConfig.ENGRAVE_PRICE));
						return htm;
					}
					player.destroyItemByItemId("use", PowerPakConfig.ENGRAVE_PRICE_ITEM, PowerPakConfig.ENGRAVE_PRICE, npc, true);
				}

				if(event.startsWith("mark"))
				{
					EngraveManager.getInstance().engraveItem(it, player);
					EngraveManager.getInstance().logAction(it, player, null, "Engrave");
					htm = HtmCache.getInstance().getHtm("data/html/default/50018-5.htm");
				}
				else
				{
					if(EngraveManager.getInstance().getEngraver(it) == player.getObjectId())
					{
						EngraveManager.getInstance().removeEngravement(it);
						htm = HtmCache.getInstance().getHtm("data/html/default/50018-8.htm");
					}

				}
				htm = htm.replace("%item%", it.getItemName());
			}

		}
		else if(event.startsWith("cleanup"))
		{
			npc.setBusy(true);
			EngraveManager.getInstance().cleanup(player.getObjectId());
			htm = HtmCache.getInstance().getHtm("data/html/default/50018-9.htm");
			npc.setBusy(false);
		}
		else if(event.startsWith("engrave"))
		{
			if(PowerPakConfig.MAX_ENGRAVED_ITEMS_PER_CHAR != 0)
			{
				if(EngraveManager.getInstance().getMyEngravement(player).size() >= PowerPakConfig.MAX_ENGRAVED_ITEMS_PER_CHAR)
				{
					htm = HtmCache.getInstance().getHtm("data/html/default/50018-7.htm");
					htm = htm.replace("%cnt%", String.valueOf(PowerPakConfig.MAX_ENGRAVED_ITEMS_PER_CHAR));
					return htm;
				}
			}
			int iPos = event.lastIndexOf("_");
			int startwith = 0;
			if(iPos > 0)
			{
				try
				{
					startwith = Integer.parseInt(event.substring(iPos + 1));
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					return htm;
				}
			}
			htm = HtmCache.getInstance().getHtm("data/html/default/50018-4.htm");
			String caption = "Select a subject for drawing engraving";
			if(PowerPakConfig.ENGRAVE_PRICE > 0 && PowerPakConfig.ENGRAVE_PRICE_ITEM > 0)
			{
				caption += "<br1>It will cost you <font color=\"LEVEL\">" + PowerPakConfig.ENGRAVE_PRICE + " " + ItemTable.getInstance().getTemplate(PowerPakConfig.ENGRAVE_PRICE_ITEM).getName() + "</font>";

				L2ItemInstance it = player.getInventory().getItemByItemId(PowerPakConfig.ENGRAVE_PRICE_ITEM);
				if(it == null || it.getCount() < PowerPakConfig.ENGRAVE_PRICE)
				{
					htm = HtmCache.getInstance().getHtm("data/html/default/50018-6.htm");
					htm = htm.replace("%itemname%", ItemTable.getInstance().getTemplate(PowerPakConfig.ENGRAVE_PRICE_ITEM).getName());
					htm = htm.replace("%count%", String.valueOf(PowerPakConfig.ENGRAVE_PRICE));
					return htm;
				}
			}
			htm = htm.replace("%caption%", caption);
			htm = htm.replace("%list%", buildList(player, startwith, "engrave", "mark", new CondChecker() {
				@Override
				public boolean check(L2ItemInstance item, L2PcInstance player)
				{
					synchronized (PowerPakConfig.ENGRAVE_EXCLUDED_ITEMS)
					{

						return !item.isEquipped() && item.isEquipable() && !item.isShadowItem() && !EngraveManager.getInstance().isEngraved(item.getObjectId()) && !PowerPakConfig.ENGRAVE_EXCLUDED_ITEMS.contains(item.getItemId()) && PowerPakConfig.ENGRAVE_ALLOW_GRADE.contains(item.getItem().getCrystalType());
					}
				}
			}));
		}
		else if(event.startsWith("remove"))
		{
			int iPos = event.lastIndexOf("_");
			int startwith = 0;
			if(iPos > 0)
			{
				try
				{
					startwith = Integer.parseInt(event.substring(iPos + 1));
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					return htm;
				}
			}
			htm = HtmCache.getInstance().getHtm("data/html/default/50018-4.htm");
			htm = htm.replace("%caption%", "Select the item to remove the engraving:");
			htm = htm.replace("%list%", buildList(player, startwith, "remove", "clear", new CondChecker() {
				@Override
				public boolean check(L2ItemInstance item, L2PcInstance player)
				{
					return !item.isEquipped() && EngraveManager.getInstance().getEngraver(item) == player.getObjectId();
				}
			}));

		}
		else if(event.startsWith("look"))
		{
			int iPos = event.lastIndexOf("_");
			int objectId = 0;
			if(iPos > 0)
			{
				try
				{
					objectId = Integer.parseInt(event.substring(iPos + 1));
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					return htm;
				}
			}
			else
				return htm;

			int[] iinfo = EngraveManager.getInstance().getItemInfo(objectId);
			if(iinfo == null)
				return htm;
			htm = HtmCache.getInstance().getHtm("data/html/default/50018-4.htm");
			htm = htm.replace("%caption%", "History <font color=\"LEVEL\">" + ItemTable.getInstance().getTemplate(iinfo[1]).getName() + "</font>");
			String list = "<table width=300><tr><td>Date</td><td>Action</td><td>From</td><td>Who</td></tr>";
			for(String s : EngraveManager.getInstance().getLog(objectId))
			{
				list += s;
			}
			list += "</table>";
			htm = htm.replace("%list%", list);
		}
		else if(event.startsWith("trace"))
		{
			int iPos = event.lastIndexOf("_");
			int startwith = 0;
			int numadded = 0;
			if(iPos > 0)
			{
				try
				{
					startwith = Integer.parseInt(event.substring(iPos + 1));
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					return htm;
				}
			}
			htm = HtmCache.getInstance().getHtm("data/html/default/50018-4.htm");
			int i = 0;
			boolean endreaced = true;
			String list = "<table width=300>";
			for(int[] item : EngraveManager.getInstance().getMyEngravement(player))
			{
				if(i++ < startwith)
				{
					continue;
				}
				list += "<tr><td><a action=\"bypass -h Quest 8008_Engrave look_" + item[0] + "\">" + ItemTable.getInstance().getTemplate(item[1]).getName() + "</a></td></tr>";
				numadded++;
				if(numadded == 20)
				{
					endreaced = false;
					break;
				}
			}
			list += "</table>";
			if(!endreaced)
			{
				list += "<br><center><a action=\"bypass -h Quest 8008_Engrave trace_" + i + "\">More</a></center>";
			}
			htm = htm.replace("%caption%", "Objects, engraved by you<br1>");
			htm = htm.replace("%list%", list);
		}
		return htm;
	}

	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		if(player.getQuestState(qn) == null)
		{
			newQuestState(player);
		}
		return HtmCache.getInstance().getHtm("data/html/default/50018-2.htm");

	}

}
