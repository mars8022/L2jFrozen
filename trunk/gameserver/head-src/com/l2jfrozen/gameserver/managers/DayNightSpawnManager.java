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
package com.l2jfrozen.gameserver.managers;

import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author godson
 */
public class DayNightSpawnManager
{
	
	private static Logger LOGGER = Logger.getLogger(DayNightSpawnManager.class);
	
	private static DayNightSpawnManager _instance;
	private static Map<L2Spawn, L2NpcInstance> _dayCreatures;
	private static Map<L2Spawn, L2NpcInstance> _nightCreatures;
	private static Map<L2Spawn, L2RaidBossInstance> _bosses;
	
	// private static int _currentState; // 0 = Day, 1 = Night
	
	public static DayNightSpawnManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new DayNightSpawnManager();
		}
		
		return _instance;
	}
	
	private DayNightSpawnManager()
	{
		_dayCreatures = new FastMap<>();
		_nightCreatures = new FastMap<>();
		_bosses = new FastMap<>();
		
		LOGGER.info("DayNightSpawnManager: Day/Night handler initialised");
	}
	
	public void addDayCreature(final L2Spawn spawnDat)
	{
		if (_dayCreatures.containsKey(spawnDat))
		{
			LOGGER.warn("DayNightSpawnManager: Spawn already added into day map");
			return;
		}
		_dayCreatures.put(spawnDat, null);
	}
	
	public void addNightCreature(final L2Spawn spawnDat)
	{
		if (_nightCreatures.containsKey(spawnDat))
		{
			LOGGER.warn("DayNightSpawnManager: Spawn already added into night map");
			return;
		}
		_nightCreatures.put(spawnDat, null);
	}
	
	/*
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/*
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/*
	 * Manage Spawn/Respawn Arg 1 : Map with L2NpcInstance must be unspawned Arg 2 : Map with L2NpcInstance must be spawned Arg 3 : String for LOGGER info for unspawned L2NpcInstance Arg 4 : String for LOGGER info for spawned L2NpcInstance
	 */
	private void spawnCreatures(final Map<L2Spawn, L2NpcInstance> UnSpawnCreatures, final Map<L2Spawn, L2NpcInstance> SpawnCreatures, final String UnspawnLogInfo, final String SpawnLogInfo)
	{
		try
		{
			if (UnSpawnCreatures.size() != 0)
			{
				int i = 0;
				for (final L2NpcInstance dayCreature : UnSpawnCreatures.values())
				{
					if (dayCreature == null)
					{
						continue;
					}
					
					dayCreature.getSpawn().stopRespawn();
					dayCreature.deleteMe();
					i++;
				}
				LOGGER.info("DayNightSpawnManager: Deleted " + i + " " + UnspawnLogInfo + " creatures");
			}
			
			int i = 0;
			L2NpcInstance creature = null;
			
			for (final L2Spawn spawnDat : SpawnCreatures.keySet())
			{
				if (SpawnCreatures.get(spawnDat) == null)
				{
					creature = spawnDat.doSpawn();
					if (creature == null)
					{
						continue;
					}
					
					SpawnCreatures.remove(spawnDat);
					SpawnCreatures.put(spawnDat, creature);
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					creature = SpawnCreatures.get(spawnDat);
					creature.getSpawn().startRespawn();
				}
				else
				{
					creature = SpawnCreatures.get(spawnDat);
					if (creature == null)
					{
						continue;
					}
					
					creature.getSpawn().startRespawn();
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					creature.spawnMe();
				}
				
				i++;
			}
			
			creature = null;
			
			LOGGER.info("DayNightSpawnManager: Spawning " + i + " " + SpawnLogInfo + " creatures");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void changeMode(final int mode)
	{
		if (_nightCreatures.size() == 0 && _dayCreatures.size() == 0)
			return;
		
		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				ShadowSenseMsg(0);
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				ShadowSenseMsg(1);
				break;
			default:
				LOGGER.warn("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}
	
	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeController.getInstance().isNowNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(final int mode)
	{
		try
		{
			for (final L2Spawn spawn : _bosses.keySet())
			{
				L2RaidBossInstance boss = _bosses.get(spawn);
				
				if (boss == null && mode == 1)
				{
					boss = (L2RaidBossInstance) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.remove(spawn);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if (boss == null && mode == 0)
				{
					continue;
				}
				
				if ((boss != null) && (boss.getNpcId() == 25328) && boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				
				boss = null;
				return;
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleHellmans(final L2RaidBossInstance boss, final int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				LOGGER.info("DayNightSpawnManager: Deleting Hellman raidboss");
				break;
			case 1:
				boss.spawnMe();
				LOGGER.info("DayNightSpawnManager: Spawning Hellman raidboss");
				break;
		}
	}
	
	private void ShadowSenseMsg(final int mode)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(294, 1);
		if (skill == null)
			return;
		
		final SystemMessageId msg = (mode == 1 ? SystemMessageId.NIGHT_EFFECT_APPLIES : SystemMessageId.DAY_EFFECT_DISAPPEARS);
		final Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		for (final L2PcInstance onlinePlayer : pls)
		{
			if (onlinePlayer.getRace().ordinal() == 2 && onlinePlayer.getSkillLevel(294) > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(msg);
				sm.addSkillName(294);
				onlinePlayer.sendPacket(sm);
				sm = null;
			}
		}
	}
	
	public L2RaidBossInstance handleBoss(final L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
			return _bosses.get(spawnDat);
		
		if (GameTimeController.getInstance().isNowNight())
		{
			final L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		_bosses.put(spawnDat, null);
		return null;
	}
}
