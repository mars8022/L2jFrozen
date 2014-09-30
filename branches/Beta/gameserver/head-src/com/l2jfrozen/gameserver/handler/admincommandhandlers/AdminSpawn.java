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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.datatables.sql.TeleportLocationTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.DayNightSpawnManager;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class handles following admin commands: - show_spawns = shows menu - spawn_index lvl = shows menu for monsters
 * with respective level - spawn_monster id = spawns monster id on target
 * 
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS =
	{
			"admin_show_spawns",
			"admin_spawn",
			"admin_spawn_monster",
			"admin_spawn_index",
			"admin_unspawnall",
			"admin_respawnall",
			"admin_spawn_reload",
			"admin_npc_index",
			"admin_spawn_once",
			"admin_show_npcs",
			"admin_teleport_reload",
			"admin_spawnnight",
			"admin_spawnday"
	};

	public static Logger _log = Logger.getLogger(AdminSpawn.class.getName());

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(command.equals("admin_show_spawns"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		}
		else if(command.startsWith("admin_spawn_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");

			try
			{
				st.nextToken();

				int level = Integer.parseInt(st.nextToken());
				int from = 0;

				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch(NoSuchElementException nsee)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						nsee.printStackTrace();
				}

				showMonsters(activeChar, level, from);
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}

			st = null;
		}
		else if(command.equals("admin_show_npcs"))
		{
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		}
		else if(command.startsWith("admin_npc_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");

			try
			{
				st.nextToken();
				String letter = st.nextToken();

				int from = 0;

				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch(NoSuchElementException nsee)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						nsee.printStackTrace();
				}

				showNpcs(activeChar, letter, from);

				letter = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}

			st = null;
		}
		// Command spawn '//spawn name numberSpawn respawnTime'.
		// With command '//spawn name' the respawnTime will be 10 seconds.
		else if(command.startsWith("admin_spawn") || command.startsWith("admin_spawn_monster"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				String cmd = st.nextToken();
				String id = st.nextToken();
				int mobCount = 1;
				int respawnTime = 10;
				if(st.hasMoreTokens())
					mobCount = Integer.parseInt(st.nextToken());
				if(st.hasMoreTokens())
					respawnTime = Integer.parseInt(st.nextToken());

				if(cmd.equalsIgnoreCase("admin_spawn_once"))
					spawnMonster(activeChar, id, respawnTime, mobCount, false);
				else
					spawnMonster(activeChar, id, respawnTime, mobCount, true);

				cmd = null;
				id = null;
			}
			catch(Exception e)
			{ // Case of wrong or missing monster data
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}

			st = null;
		}
		// Command for unspawn all Npcs on Server, use //repsawnall to respawn the npc
		else if(command.startsWith("admin_unspawnall"))
		{
			for(L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
			}
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");
		}
		else if(command.startsWith("admin_spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnDayCreatures();
		}
		else if(command.startsWith("admin_spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnNightCreatures();
		}
		else if(command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make sure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			// now respawn all
			NpcTable.getInstance().reloadAllNpc();
			SpawnTable.getInstance().reloadAll();
			RaidBossSpawnManager.getInstance().reloadBosses();
			GmListTable.broadcastMessageToGMs("NPC Respawn completed!");
		}
		else if(command.startsWith("admin_teleport_reload"))
		{
			TeleportLocationTable.getInstance().reloadAll();
			GmListTable.broadcastMessageToGMs("Teleport List Table reloaded.");
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount, boolean permanent)
	{
		L2Object target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		if(target != activeChar && activeChar.getAccessLevel().isGm())
			target = activeChar;
		
		L2NpcTemplate template1;
		if(monsterId.matches("[0-9]*"))
		{
			//First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			//First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcTable.getInstance().getTemplateByName(monsterId);
		}

		if(template1 == null){
			activeChar.sendMessage("Attention, wrong NPC ID/Name");
			return;
		}
		
		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			if(Config.SAVE_GMSPAWN_ON_CUSTOM)
				spawn.setCustom(true);
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			
			if(RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid()) || GrandBossManager.getInstance().isDefined(spawn.getNpcid()))
			{
				activeChar.sendMessage("Another instance of " + template1.name + " already present into database:");
				activeChar.sendMessage("It will be spawned but not saved on Database");
				activeChar.sendMessage("After server restart or raid dead, the spawned npc will desappear");
				permanent=false;
				spawn.set_customBossInstance(true); //for raids, this value is used in order to segnalate to not save respawn time - status for custom instance

			}
			//else
			//{
				if(RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null)
				{
					RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getStatsSet().getDouble("baseHpMax"), template1.getStatsSet().getDouble("baseMpMax"), permanent);
				}
				else
				{
					SpawnTable.getInstance().addNewSpawn(spawn, permanent);
				}

				spawn.init();

				if(!permanent)
				{
					spawn.stopRespawn();
				}

				activeChar.sendMessage("Created " + template1.name + " on " + target.getObjectId());
			//}

			spawn = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
		}

		template1 = null;
		target = null;
	}

	private void showMonsters(L2PcInstance activeChar, int level, int from)
	{
		TextBuilder tb = new TextBuilder();

		L2NpcTemplate[] mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);

		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> Level " + level + ":<br>Total Npc's : " + mobs.length + "<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index " + level + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";

		// Loop
		boolean ended = true;
		for(int i = from; i < mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";

			if(tb.length() + txt.length() + end2.length() > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;

				break;
			}

			tb.append(txt);
			txt = null;
		}

		// End
		if(ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));

		end1 = null;
		end2 = null;
		mobs = null;
		tb = null;
	}

	private void showNpcs(L2PcInstance activeChar, String starting, int from)
	{
		TextBuilder tb = new TextBuilder();
		L2NpcTemplate[] mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);

		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> There are " + mobs.length + " Npcs whose name starts with " + starting + ":<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index " + starting + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";

		// Loop
		boolean ended = true;
		for(int i = from; i < mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";

			if(tb.length() + txt.length() + end2.length() > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;

				break;
			}
			tb.append(txt);
			txt = null;
		}
		// End
		if(ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));

		tb = null;
		mobs = null;
		end1 = null;
		end2 = null;
	}
}
