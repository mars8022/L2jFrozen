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
package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Shyla
 * @author L2jfrozen
 */
public class Orfen extends Quest implements Runnable
{
	private static final int ORFEN = 29014;
	private static final int LIVE = 0;
	private static final int DEAD = 1;
	
	private boolean FirstAttacked = false;
	private boolean Teleported = false;
	
	L2GrandBossInstance orfen = null;
	
	enum Event
	{
		ORFEN_SPAWN,
		ORFEN_REFRESH,
		ORFEN_RETURN
	}
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Orfen(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);		
		Integer status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		
		addEventId(ORFEN, Quest.QuestEventType.ON_KILL);
		addEventId(ORFEN, Quest.QuestEventType.ON_ATTACK);
		
		switch (status)
		{
			case DEAD:
			{			
				long temp = info.getLong("respawn_time") - System.currentTimeMillis();
				if (temp > 0)
				{
					startQuestTimer("ORFEN_SPAWN", temp, null, null);
				}
				else
				{
					int loc_x = 55024;
					int loc_y = 17368;
					int loc_z = -5412;
					int heading = 0;
					
					orfen = (L2GrandBossInstance) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
					if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
					{
						Announcements.getInstance().announceToAll("Raid boss " + orfen.getName() + " spawned in world.");
					}
					GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
					GrandBossManager.getInstance().addBoss(orfen);
				}
			}
				break;
			case LIVE:
			{				
				/*
				 * int loc_x = info.getInteger("loc_x"); int loc_y = info.getInteger("loc_y"); int loc_z = info.getInteger("loc_z"); int heading = info.getInteger("heading");
				 */
				
				int loc_x = 55024;
				int loc_y = 17368;
				int loc_z = -5412;
				int heading = 0;
				
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				orfen = (L2GrandBossInstance) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().addBoss(orfen);
				orfen.setCurrentHpMp(hp, mp);
			}
				break;
			default:
			{			
				int loc_x = 55024;
				int loc_y = 17368;
				int loc_z = -5412;
				int heading = 0;
				
				orfen = (L2GrandBossInstance) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
				GrandBossManager.getInstance().addBoss(orfen);			
			}
		}		
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		Event event_enum = Event.valueOf(event.toUpperCase());
		
		switch (event_enum)
		{
			case ORFEN_SPAWN:
			{				
				int loc_x = 55024;
				int loc_y = 17368;
				int loc_z = -5412;
				int heading = 0;
				
				orfen = (L2GrandBossInstance) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
				GrandBossManager.getInstance().addBoss(orfen);				
			}
				break;
			case ORFEN_REFRESH:
			{			
				if (npc == null || npc.getSpawn() == null)
				{				
					cancelQuestTimer("ORFEN_REFRESH", npc, null);
					break;
				}
				
				double saved_hp = -1;
				
				if (npc.getNpcId() == ORFEN && !npc.getSpawn().is_customBossInstance())
				{					
					saved_hp = GrandBossManager.getInstance().getStatsSet(ORFEN).getDouble("currentHP");
					
					if (saved_hp < npc.getCurrentHp())
					{
						npc.setCurrentHp(saved_hp);
						GrandBossManager.getInstance().getStatsSet(ORFEN).set("currentHP", npc.getMaxHp());
					}					
				}
				
				if ((Teleported && npc.getCurrentHp() > npc.getMaxHp() * 0.95))
				{				
					cancelQuestTimer("ORFEN_REFRESH", npc, null);
					startQuestTimer("ORFEN_RETURN", 10000, npc, null);					
				}
				else
				{ // restart the refresh scheduling				
					startQuestTimer("ORFEN_REFRESH", 10000, npc, null);					
				}
				
			}
				break;
			case ORFEN_RETURN:
			{				
				if (npc == null || npc.getSpawn() == null)
				{
					break;
				}
				
				this.Teleported = false;
				this.FirstAttacked = false;
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				npc.getSpawn().setLocx(55024);
				npc.getSpawn().setLocy(17368);
				npc.getSpawn().setLocz(-5412);
				npc.teleToLocation(55024, 17368, -5412, false);				
			}
				break;
			default:
			{
				_log.info("ORFEN: Not defined event: " + event + "!");
			}			
		}
		
		return super.onAdvEvent(event, npc, player);		
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == ORFEN)
		{
			if (FirstAttacked)
			{
				if ((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2) && !Teleported)
				{
					GrandBossManager.getInstance().getStatsSet(ORFEN).set("currentHP", npc.getCurrentHp());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					Teleported = true;
					npc.getSpawn().setLocx(43577);
					npc.getSpawn().setLocy(15985);
					npc.getSpawn().setLocz(-4396);
					npc.teleToLocation(43577, 15985, -4396, false);
					startQuestTimer("ORFEN_REFRESH", 10000, npc, null);
				}
				else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && Rnd.get(10) == 0)
				{
					attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
				}
			}
			else
			{
				FirstAttacked = true;
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);		
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == ORFEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			
			if (!npc.getSpawn().is_customBossInstance())
			{			
				GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
				// time is 48hour +/- 20hour
				long respawnTime = (long) (Config.ORFEN_RESP_FIRST + Rnd.get(Config.ORFEN_RESP_SECOND)) * 3600000;
				cancelQuestTimer("ORFEN_REFRESH", npc, null);
				startQuestTimer("ORFEN_SPAWN", respawnTime, null, null);
				// also save the respawn time so that the info is maintained past reboots
				StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(ORFEN, info);				
			}			
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}	
}