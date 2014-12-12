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
package com.l2jfrozen.gameserver.ai.special;

import java.util.List;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
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
	private L2MonsterInstance _larva = null;
	private L2MonsterInstance _queen = null;
	private final List<L2MonsterInstance> _Minions = new FastList<>();
	private final List<L2MonsterInstance> _Nurses = new FastList<>();
	
	// L2GrandBossInstance queen = null;
	
	enum Event
	{
		QUEEN_SPAWN, /* CHECK_QA_ZONE, */
		CHECK_MINIONS_ZONE,
		CHECK_NURSE_ALIVE,
		ACTION,
		DESPAWN_MINIONS,
		SPAWN_ROYAL,
		NURSES_SPAWN,
		RESPAWN_ROYAL,
		RESPAWN_NURSE,
		LARVA_DESPAWN,
		HEAL
	}
	
	public QueenAnt(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		final int[] mobs =
		{
			QUEEN,
			LARVA,
			NURSE,
			GUARD,
			ROYAL
		};
		for (final int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
		
		_Zone = GrandBossManager.getInstance().getZone(-21610, 181594, -5734);
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		switch (status)
		{
			case DEAD:
			{
				final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
				if (temp > 0)
				{
					startQuestTimer("QUEEN_SPAWN", temp, null, null);
				}
				else
				{
					final L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
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
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				final L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
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
				final L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
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
	
	private void spawnBoss(final L2GrandBossInstance npc)
	{
		startQuestTimer("ACTION", 10000, npc, null, true);
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		startQuestTimer("SPAWN_ROYAL", 1000, npc, null);
		startQuestTimer("NURSES_SPAWN", 1000, npc, null);
		startQuestTimer("CHECK_MINIONS_ZONE", 30000, npc, null, true);
		startQuestTimer("HEAL", 1000, null, null, true);
		_queen = npc;
		_larva = (L2MonsterInstance) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
		_larva.setIsUnkillable(true);
		_larva.setIsImobilised(true);
		_larva.setIsAttackDisabled(true);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		final Event event_enum = Event.valueOf(event);
		
		switch (event_enum)
		{
			case QUEEN_SPAWN:
			{
				final L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				spawnBoss(queen);
			}
				break;
			case LARVA_DESPAWN:
			{
				_larva.decayMe();
			}
				break;
			case NURSES_SPAWN:
			{
				final int radius = 400;
				for (int i = 0; i < 6; i++)
				{
					final int x = (int) (radius * Math.cos(i * 1.407)); // 1.407~2pi/6
					final int y = (int) (radius * Math.sin(i * 1.407));
					_Nurses.add((L2MonsterInstance) addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
					_Nurses.get(i).setIsAttackDisabled(true);
				}
			}
				break;
			case SPAWN_ROYAL:
			{
				final int radius = 400;
				for (int i = 0; i < 8; i++)
				{
					final int x = (int) (radius * Math.cos(i * .7854)); // .7854~2pi/8
					final int y = (int) (radius * Math.sin(i * .7854));
					_Minions.add((L2MonsterInstance) addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
				}
			}
				break;
			case RESPAWN_ROYAL:
			{
				_Minions.add((L2MonsterInstance) addSpawn(ROYAL, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
			}
			case RESPAWN_NURSE:
			{
				_Nurses.add((L2MonsterInstance) addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
			}
				break;
			case DESPAWN_MINIONS:
			{
				for (int i = 0; i < _Minions.size(); i++)
				{
					final L2Attackable mob = _Minions.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				for (int k = 0; k < _Nurses.size(); k++)
				{
					final L2MonsterInstance _nurse = _Nurses.get(k);
					if (_nurse != null)
						_nurse.decayMe();
				}
				_Nurses.clear();
				_Minions.clear();
			}
				break;
			case CHECK_MINIONS_ZONE:
			{
				for (int i = 0; i < _Minions.size(); i++)
				{
					final L2Attackable mob = _Minions.get(i);
					
					if (mob != null && !mob.isInsideRadius(npc.getX(), npc.getY(), 700, false))/* !_Zone.isInsideZone(mob)) */
					{
						mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					}
				}
			}
				break;
			case CHECK_NURSE_ALIVE:
			{
				int deadNurses = 0;
				for (final L2MonsterInstance nurse : _Nurses)
				{
					if (nurse.isDead())
						deadNurses++;
				}
				if (deadNurses == _Nurses.size())
					startQuestTimer("RESPAWN_NURSE", Config.QA_RESP_NURSE * 1000, npc, null);
			}
				break;
			/*
			 * case CHECK_QA_ZONE:{ int loc_x = -21610; int loc_y = 181594; int loc_z = -5734; if(!npc.isInsideRadius(loc_x,loc_y,3000,false)){ npc.teleToLocation(loc_x, loc_y, loc_z); } startQuestTimer("CHECK_MINIONS_ZONE", 1000, npc, null); } break;
			 */
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
			case HEAL:
			{
				boolean notCasting;
				final boolean larvaNeedHeal = _larva != null && _larva.getCurrentHp() < _larva.getMaxHp();
				final boolean queenNeedHeal = _queen != null && _queen.getCurrentHp() < _queen.getMaxHp();
				boolean nurseNeedHeal = false;
				for (final L2MonsterInstance nurse : _Nurses)
				{
					nurseNeedHeal = nurse != null && nurse.getCurrentHp() < nurse.getMaxHp();
					if (nurse == null || nurse.isDead() || nurse.isCastingNow())
						continue;
					notCasting = nurse.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST;
					if (larvaNeedHeal)
					{
						if (nurse.getTarget() != _larva || notCasting)
						{
							getIntoPosition(nurse, _larva);
							nurse.setTarget(_larva);
							nurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
							nurse.doCast(SkillTable.getInstance().getInfo(4024, 1));
						}
						continue;
					}
					if (queenNeedHeal)
					{
						if (nurse.getTarget() != _queen || notCasting)
						{
							getIntoPosition(nurse, _queen);
							nurse.setTarget(_queen);
							nurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
						}
						continue;
					}
					if (nurseNeedHeal)
					{
						if (nurse.getTarget() != nurse || notCasting)
						{
							for (int k = 0; k < _Nurses.size(); k++)
							{
								getIntoPosition(_Nurses.get(k), nurse);
								_Nurses.get(k).setTarget(nurse);
								_Nurses.get(k).doCast(SkillTable.getInstance().getInfo(4020, 1));
							}
							
						}
					}
					if (notCasting && nurse.getTarget() != null)
						nurse.setTarget(null);
				}
			}
				break;
			default:
			{
				LOGGER.info("QUEEN: Not defined event: " + event + "!");
			}
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (npcId == NURSE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getNpcId();
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		if (npcId == QUEEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			
			if (!npc.getSpawn().is_customBossInstance())
			{
				GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
				// time is 36hour +/- 17hour
				final long respawnTime = (Config.QA_RESP_FIRST + Rnd.get(Config.QA_RESP_SECOND)) * 3600000;
				startQuestTimer("QUEEN_SPAWN", respawnTime, null, null);
				startQuestTimer("LARVA_DESPAWN", 4 * 60 * 60 * 1000, null, null);
				cancelQuestTimer("ACTION", npc, null);
				cancelQuestTimer("SPAWN_ROYAL", npc, null);
				cancelQuestTimer("CHECK_MINIONS_ZONE", npc, null);
				cancelQuestTimer("CHECK_NURSE_ALIVE", npc, null);
				cancelQuestTimer("HEAL", null, null);
				// cancelQuestTimer("CHECK_QA_ZONE", npc, null);
				// also save the respawn time so that the info is maintained past reboots
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(QUEEN, info);
			}
			
			startQuestTimer("DESPAWN_MINIONS", 10000, null, null);
		}
		else if (status == LIVE)
		{
			if (npcId == ROYAL || npcId == NURSE)
			{
				npc.decayMe();
				if (_Minions.contains(npc))
				{
					_Minions.remove(npc);
				}
				else
				{
					_Nurses.remove(npc);
				}
				
				if (npcId == ROYAL)
				{
					startQuestTimer("RESPAWN_ROYAL", (Config.QA_RESP_ROYAL + Rnd.get(40)) * 1000, npc, null);
				}
				else if (npcId == NURSE)
				{
					startQuestTimer("CHECK_NURSE_ALIVE", 1000, npc, null);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public void getIntoPosition(final L2MonsterInstance nurse, final L2MonsterInstance caller)
	{
		if (!nurse.isInsideRadius(caller, 300, false, false))
			nurse.getAI().moveToPawn(caller, 300);
	}
	
	@Override
	public void run()
	{
	}
}