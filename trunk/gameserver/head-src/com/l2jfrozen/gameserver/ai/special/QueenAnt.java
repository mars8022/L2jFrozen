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

import java.util.List;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

public class QueenAnt extends Quest implements Runnable
{
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	// QUEEN Status Tracking :
	private static final int LIVE = 0; // Queen Ant is spawned.
	private static final int DEAD = 1; // Queen Ant has been killed.
	
	@SuppressWarnings("unused")
	private static L2BossZone _Zone;
	private static List<L2Attackable> _Minions = new FastList<L2Attackable>();
	private static List<L2Attackable> _Larva_minions = new FastList<L2Attackable>();
	
	// L2GrandBossInstance queen = null;
	
	enum Event
	{
		QUEEN_SPAWN, /* CHECK_QA_ZONE, */
		CHECK_MINIONS_ZONE,
		ACTION,
		DESPAWN_MINIONS,
		SPAWN_ROYAL,
		SPAWN_QUEEN_NURSE,
		SPAWN_LARVA_NURSE,
		LARVA_SPAWN,
		LARVA_DESPAWN
	}
	
	public QueenAnt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		int[] mobs =
		{
			QUEEN,
			LARVA,
			NURSE,
			GUARD,
			ROYAL
		};
		for (int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
			addEventId(mob, Quest.QuestEventType.ON_FACTION_CALL);
		}
		
		_Zone = GrandBossManager.getInstance().getZone(-21610, 181594, -5734);
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
		
		Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		switch (status)
		{
			case DEAD:
			{			
				long temp = info.getLong("respawn_time") - System.currentTimeMillis();
				if (temp > 0)
				{
					startQuestTimer("QUEEN_SPAWN", temp, null, null);
					startQuestTimer("LARVA_SPAWN", 1000, null, null);
				}
				else
				{
					L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
					if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
					{
						Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
					}
					GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
					GrandBossManager.getInstance().addBoss(queen);
					spawnBoss(queen);
				}
			}
				break;
			case LIVE:
			{				
				/*
				 * int loc_x = info.getInteger("loc_x"); int loc_y = info.getInteger("loc_y"); int loc_z = info.getInteger("loc_z"); int heading = info.getInteger("heading");
				 */
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().addBoss(queen);
				queen.setCurrentHpMp(hp, mp);
				spawnBoss(queen);
			}
				break;
			default:
			{				
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				spawnBoss(queen);				
			}
		}		
	}
	
	private void spawnBoss(L2GrandBossInstance npc)
	{
		startQuestTimer("ACTION", 10000, npc, null, true);
		startQuestTimer("LARVA_DESPAWN", 1000, null, null);
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// Spawn minions
		int radius = 400;
		for (int i = 0; i < 6; i++)
		{
			int x = (int) (radius * Math.cos(i * 1.407)); // 1.407~2pi/6
			int y = (int) (radius * Math.sin(i * 1.407));
			_Minions.add((L2Attackable) addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
		}
		for (int i = 0; i < 8; i++)
		{
			int x = (int) (radius * Math.cos(i * .7854)); // .7854~2pi/8
			int y = (int) (radius * Math.sin(i * .7854));
			_Minions.add((L2Attackable) addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
		}
		// startQuestTimer("CHECK_MINIONS_ZONE", 30000, npc, null,true);
		startQuestTimer("CHECK_MINIONS_ZONE", 30000, npc, null, true);		
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		Event event_enum = Event.valueOf(event);
		
		switch (event_enum)
		{
			case QUEEN_SPAWN:
			{				
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				spawnBoss(queen);				
			}
				break;
			case LARVA_SPAWN:
			{				
				L2Attackable larva = (L2Attackable) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
				larva.setIsInvul(true);
				_Larva_minions.add(larva);
				_Larva_minions.add((L2Attackable) addSpawn(NURSE, -22000, 179482, -5846, 0, false, 0));
				_Larva_minions.add((L2Attackable) addSpawn(NURSE, -21200, 179482, -5846, 0, false, 0));				
			}
				break;
			case LARVA_DESPAWN:
			{			
				for (int i = 0; i < _Larva_minions.size(); i++)
				{
					L2Attackable mob = _Larva_minions.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				_Larva_minions.clear();				
			}
				break;
			case DESPAWN_MINIONS:
			{				
				for (int i = 0; i < _Minions.size(); i++)
				{
					L2Attackable mob = _Minions.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				_Minions.clear();			
			}
				break;
			case CHECK_MINIONS_ZONE:
			{			
				for (int i = 0; i < _Minions.size(); i++)
				{
					L2Attackable mob = _Minions.get(i);
					
					if (mob != null && !mob.isInsideRadius(npc.getX(), npc.getY(), 300, false))/* !_Zone.isInsideZone(mob)) */
					{
						mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					}
				}			
			}
				break;
			/*
			 * case CHECK_QA_ZONE:{ int loc_x = -21610; int loc_y = 181594; int loc_z = -5734; if(!npc.isInsideRadius(loc_x,loc_y,3000,false)){ npc.teleToLocation(loc_x, loc_y, loc_z); } startQuestTimer("CHECK_MINIONS_ZONE", 1000, npc, null); } break;
			 */
			case SPAWN_ROYAL:
			{			
				_Minions.add((L2Attackable) addSpawn(ROYAL, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));			
			}
				break;
			case SPAWN_QUEEN_NURSE:
			{			
				_Minions.add((L2Attackable) addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));			
			}
				break;
			case SPAWN_LARVA_NURSE:
			{			
				_Larva_minions.add((L2Attackable) addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));			
			}
				break;
			case ACTION:
			{				
				if (Rnd.get(3) == 0)
				{
					if (Rnd.get(2) == 0)
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
					}
					else
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
					}
				}
				/*
				 * if(Math.abs(npc.getX() + 21610) > 1000 || Math.abs(npc.getY() - 181594) > 2500) { ((L2Attackable) npc).clearAggroList(); npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE); npc.teleToLocation(-21610, 181594, -5740); }
				 */
				// startQuestTimer("ACTION", 10000, npc, null);
				
			}
				break;
			default:
			{
				_log.info("QUEEN: Not defined event: " + event + "!");
			}			
		}
		
		return super.onAdvEvent(event, npc, player);		
	}
	
	@Override
	public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
	{
		if (caller == null || npc == null)
			return super.onFactionCall(npc, caller, attacker, isPet);
		
		int npcId = npc.getNpcId();
		int callerId = caller.getNpcId();
		if (npcId == NURSE)
		{
			if (callerId == LARVA)
			{
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
				npc.doCast(SkillTable.getInstance().getInfo(4024, 1));
				return null;
			}
			else if (callerId == NURSE) // Like L2OFF NURSE must heal each others
			{
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
				return null;
			}
			else if (callerId == QUEEN)
			{
				if (npc.getTarget() != null && npc.getTarget() instanceof L2NpcInstance)
				{
					if (((L2NpcInstance) npc.getTarget()).getNpcId() == LARVA)
						return null;
				}
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
				return null;
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == NURSE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		
		Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		if (npcId == QUEEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			
			if (!npc.getSpawn().is_customBossInstance())
			{
				GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
				// time is 36hour +/- 17hour
				long respawnTime = (Config.QA_RESP_FIRST + Rnd.get(Config.QA_RESP_SECOND)) * 3600000;
				startQuestTimer("QUEEN_SPAWN", respawnTime, null, null);
				cancelQuestTimer("ACTION", npc, null);
				cancelQuestTimer("SPAWN_ROYAL", npc, null);
				cancelQuestTimer("SPAWN_QUEEN_NURSE", npc, null);
				cancelQuestTimer("CHECK_MINIONS_ZONE", npc, null);
				// cancelQuestTimer("CHECK_QA_ZONE", npc, null);
				// also save the respawn time so that the info is maintained past reboots
				StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(QUEEN, info);
				startQuestTimer("LARVA_SPAWN", 10000, null, null);				
			}
			
			startQuestTimer("DESPAWN_MINIONS", 10000, null, null);			
		}
		else if (status == LIVE)
		{			
			if (npcId == ROYAL || npcId == NURSE)
			{				
				boolean queen = false;
				
				npc.decayMe();
				if (_Minions.contains(npc))
				{
					_Minions.remove(npc);
					queen = true;
				}
				else
				{
					_Larva_minions.remove(npc);
				}
				
				if (npcId == ROYAL)
				{
					startQuestTimer("SPAWN_ROYAL", (Config.QA_RESP_ROYAL + Rnd.get(40)) * 1000, npc, null);
				}
				else if (npcId == NURSE)
				{
					if (queen)
					{
						startQuestTimer("SPAWN_QUEEN_NURSE", Config.QA_RESP_NURSE * 1000, npc, null);
					}
					else
						startQuestTimer("SPAWN_LARVA_NURSE", Config.QA_RESP_NURSE * 1000, npc, null);					
				}
			}			
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
}