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
package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.sql.TerritoryTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages AI of L2Attackable.<BR>
 * <BR>
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	
	// protected static final Logger LOGGER = Logger.getLogger(L2AttackableAI.class);
	
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method) */
	private Future<?> _aiTask;
	
	/** The delay after wich the attacked is stopped */
	private int _attackTimeout;
	
	/** The L2Attackable aggro counter */
	private int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress */
	private boolean _thinking; // to prevent recursive thinking
	
	/**
	 * Constructor of L2AttackableAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2AttackableAI(final L2Character.AIAccessor accessor)
	{
		super(accessor);
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR>
	 * <BR>
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>The L2PcInstance target has karma (=PK)</li> <li>The L2MonsterInstance
	 * target is aggressive</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>A siege is in progress</li> <li>The L2PcInstance target isn't a Defender</li>
	 * <BR>
	 * <BR>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>The L2PcInstance target has karma (=PK)</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>The actor is Aggressive</li><BR>
	 * <BR>
	 * @param target The targeted L2Object
	 * @return
	 */
	private boolean autoAttackCondition(final L2Character target)
	{
		if (target == null || !(_actor instanceof L2Attackable))
			return false;
		
		final L2Attackable me = (L2Attackable) _actor;
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			// However EffectInvincible requires to check GMs specially
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isGM())
				return false;
			
			if (target instanceof L2Summon && ((L2Summon) target).getOwner().isGM())
				return false;
		}
		
		// Check if the target isn't a Folk or a Door
		if (target instanceof L2FolkInstance || target instanceof L2DoorInstance)
			return false;
		
		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || Math.abs(_actor.getZ() - target.getZ()) > 300)
			return false;
		
		// Check if the target is a L2PcInstance
		if (target instanceof L2PcInstance)
		{
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (((L2PcInstance) target).isGM() && ((L2PcInstance) target).getAccessLevel().canTakeAggro())
				return false;
			
			// Check if the AI isn't a Raid Boss and the target isn't in silent move mode
			if (!(me instanceof L2RaidBossInstance) && ((L2PcInstance) target).isSilentMoving())
				return false;
			
			// if in offline mode
			if (((L2PcInstance) target).isInOfflineMode())
				return false;
			
			// Check if player is an ally //TODO! [Nemesiss] it should be rather boolean or smth like that
			// Comparing String isnt good idea!
			if (me.getFactionId() != null && me.getFactionId().equals("varka") && ((L2PcInstance) target).isAlliedWithVarka())
				return false;
			
			if (me.getFactionId() != null && me.getFactionId().equals("ketra") && ((L2PcInstance) target).isAlliedWithKetra())
				return false;
			
			// check if the target is within the grace period for JUST getting up from fake death
			if (((L2PcInstance) target).isRecentFakeDeath())
				return false;
			
			// check player is in away mod
			if (((L2PcInstance) target).isAway() && !Config.AWAY_PLAYER_TAKE_AGGRO)
				return false;
			
			if (target.isInParty() && target.getParty().isInDimensionalRift())
			{
				final byte riftType = target.getParty().getDimensionalRift().getType();
				final byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();
				
				if (me instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
					return false;
			}
		}
		
		// Check if the target is a L2Summon
		if (target instanceof L2Summon)
		{
			final L2PcInstance owner = ((L2Summon) target).getOwner();
			if (owner != null)
			{
				// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
				if (owner.isGM() && (owner.isInvul() || !owner.getAccessLevel().canTakeAggro()))
					return false;
				// Check if player is an ally (comparing mem addr)
				if (me.getFactionId() != null && me.getFactionId() == "varka" && owner.isAlliedWithVarka())
					return false;
				if (me.getFactionId() != null && me.getFactionId() == "ketra" && owner.isAlliedWithKetra())
					return false;
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				// Los Check
				return GeoData.getInstance().canSeeTarget(me, target);
			
			// if (target instanceof L2Summon)
			// return ((L2Summon)target).getKarma() > 0;
			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof L2MonsterInstance)
				return ((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target);
			
			return false;
		}
		else if (_actor instanceof L2FriendlyMobInstance)
		{
			// the actor is a L2FriendlyMobInstance
			
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
				return false;
			
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				// Los Check
				return GeoData.getInstance().canSeeTarget(me, target);
			return false;
		}
		else
		{
			// The actor is a L2MonsterInstance
			
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
				return false;
			
			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (L2Character.isInsidePeaceZone(me, target))
				return false;
			
			// Check if the actor is Aggressive
			return me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target);
		}
	}
	
	public synchronized void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
		
	}
	
	public synchronized void stopAITask()
	{
		
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}
	
	/**
	 * Set the Intention of this L2CharacterAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	public void changeIntention(CtrlIntention intention, final Object arg0, final Object arg1)
	{
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			// Check if actor is not dead
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) _actor;
				
				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (npc.getKnownList().getKnownPlayers().size() > 0)
				{
					intention = AI_INTENTION_ACTIVE;
				}
				
				npc = null;
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				stopAITask();
				
				// Cancel the AI
				_accessor.detachAI();
				
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR>
	 * <BR>
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(final L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li> <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li> <li>If the actor is a L2GuardInstance that can't attack,
	 * order to it to return to its home location</li> <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR>
	 * <BR>
	 */
	private void thinkActive()
	{
		L2Attackable npc = (L2Attackable) _actor;
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			// L2Object[] objects = L2World.getInstance().getVisibleObjects(_actor, ((L2NpcInstance)_actor).getAggroRange());
			// Go through visible objects
			for (final L2Object obj : npc.getKnownList().getKnownObjects().values())
			{
				if (obj == null || !(obj instanceof L2Character))
				{
					continue;
				}
				
				L2Character target = (L2Character) obj;
				
				/*
				 * Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
				 */
				if (_actor instanceof L2FestivalMonsterInstance && obj instanceof L2PcInstance)
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;
					if (!targetPlayer.isFestivalParticipant())
					{
						continue;
					}
					
					targetPlayer = null;
				}
				
				if (obj instanceof L2PcInstance || obj instanceof L2Summon)
				{
					if (!target.isAlikeDead() && !npc.isInsideRadius(obj, npc.getAggroRange(), true, false))
					{
						final L2PcInstance targetPlayer = obj instanceof L2PcInstance ? (L2PcInstance) obj : ((L2Summon) obj).getOwner();
						
						for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
						{
							quest.notifyAggroRangeEnter(npc, targetPlayer, obj instanceof L2Summon);
						}
					}
				}
				
				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					final int hating = npc.getHating(target);
					
					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 1);
					}
				}
				
				target = null;
			}
			
			// Chose a target from its aggroList
			L2Character hated;
			
			// Force mobs to attak anybody if confused
			if (_actor.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getMostHated();
			}
			
			// Order to the L2Attackable to attack the target
			if (hated != null)
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				final int aggro = npc.getHating(hated);
				if (aggro + _globalAggro > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}
					
					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				
				return;
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			((L2GuardInstance) _actor).returnHome();
		}
		
		// If this is a festival monster, then it remains in the same location.
		if (_actor instanceof L2FestivalMonsterInstance)
			return;
		
		// Check if the mob should not return to spawn point
		if (!npc.canReturnToSpawnPoint())
			return;
		
		// Minions following leader
		if (_actor instanceof L2MinionInstance && ((L2MinionInstance) _actor).getLeader() != null)
		{
			int offset;
			
			// for Raids - need correction
			if (_actor.isRaid())
			{
				offset = 500;
			}
			else
			{
				// for normal minions - need correction :)
				offset = 200;
			}
			
			if (((L2MinionInstance) _actor).getLeader().isRunning())
			{
				_actor.setRunning();
			}
			else
			{
				_actor.setWalking();
			}
			
			if (_actor.getPlanDistanceSq(((L2MinionInstance) _actor).getLeader()) > offset * offset)
			{
				int x1, y1, z1;
				
				x1 = ((L2MinionInstance) _actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				y1 = ((L2MinionInstance) _actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				z1 = ((L2MinionInstance) _actor).getLeader().getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
				return;
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if (!(npc instanceof L2ChestInstance) && npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0)
		{
			int x1, y1, z1;
			
			// If NPC with random coord in territory
			if (npc.getSpawn().getLocx() == 0 && npc.getSpawn().getLocy() == 0)
			{
				// If NPC with random fixed coord, don't move
				if (TerritoryTable.getInstance().getProcMax(npc.getSpawn().getLocation()) > 0)
					return;
				
				// Calculate a destination point in the spawn area
				final int p[] = TerritoryTable.getInstance().getRandomPoint(npc.getSpawn().getLocation());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];
				
				// Calculate the distance between the current position of the L2Character and the target (x,y)
				final double distance2 = _actor.getPlanDistanceSq(x1, y1);
				
				if (distance2 > Config.MAX_DRIFT_RANGE * Config.MAX_DRIFT_RANGE)
				{
					npc.setisReturningToSpawnPoint(true);
					final float delay = (float) Math.sqrt(distance2) / Config.MAX_DRIFT_RANGE;
					x1 = _actor.getX() + (int) ((x1 - _actor.getX()) / delay);
					y1 = _actor.getY() + (int) ((y1 - _actor.getY()) / delay);
				}
				else
				{
					npc.setisReturningToSpawnPoint(false);
				}
				
			}
			else
			{
				if (Config.MONSTER_RETURN_DELAY > 0 && npc instanceof L2MonsterInstance && !npc.isAlikeDead() && !npc.isDead() && npc.getSpawn() != null && !npc.isInsideRadius(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), Config.MAX_DRIFT_RANGE, false))
				{
					((L2MonsterInstance) _actor).returnHome();
				}
				
				// If NPC with fixed coord
				x1 = npc.getSpawn().getLocx() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;
				y1 = npc.getSpawn().getLocy() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;
				z1 = npc.getZ();
			}
			
			// LOGGER.config("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
		
		npc = null;
		
		return;
		
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update the attack timeout if actor is running</li> <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li> <li>Call all L2Object of its Faction inside the Faction Range</li> <li>Chose a target and order to attack it with magic skill
	 * or physical attack</li><BR>
	 * <BR>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		if ((_actor == null) || _actor.isCastingNow())
		{
			return;
		}
		if (_attackTimeout < GameTimeController.getGameTicks())
		{
			// Check if the actor is running
			if (_actor.isRunning())
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				_actor.setWalking();
				
				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}
		
		final L2Character originalAttackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() || (originalAttackTarget instanceof L2PcInstance && (((L2PcInstance) originalAttackTarget).isInOfflineMode() || ((L2PcInstance) originalAttackTarget).isOnline() == 0)) || _attackTimeout < GameTimeController.getGameTicks())
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
				((L2Attackable) _actor).stopHating(originalAttackTarget);
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			
			_actor.setWalking();
			return;
		}
		
		/*
		 * // Handle all L2Object of its Faction inside the Faction Range if (_actor instanceof L2NpcInstance && ((L2NpcInstance) _actor).getFactionId() != null) { String faction_id = ((L2NpcInstance) _actor).getFactionId(); // Go through all L2Object that belong to its faction Collection<L2Object>
		 * objs = _actor.getKnownList().getKnownObjects().values(); //synchronized (_actor.getKnownList().getKnownObjects()) try { for (L2Object obj : objs) { if (obj instanceof L2NpcInstance) { L2NpcInstance npc = (L2NpcInstance) obj; //Handle SevenSigns mob Factions String npcfaction =
		 * npc.getFactionId(); boolean sevenSignFaction = false; // TODO: Unhardcode this by AI scripts (DrHouse) //Catacomb mobs should assist lilim and nephilim other than dungeon if ("c_dungeon_clan".equals(faction_id) && ("c_dungeon_lilim".equals(npcfaction) ||
		 * "c_dungeon_nephi".equals(npcfaction))) sevenSignFaction = true; //Lilim mobs should assist other Lilim and catacomb mobs else if ("c_dungeon_lilim".equals(faction_id) && "c_dungeon_clan".equals(npcfaction)) sevenSignFaction = true; //Nephilim mobs should assist other Nephilim and catacomb
		 * mobs else if ("c_dungeon_nephi".equals(faction_id) && "c_dungeon_clan".equals(npcfaction)) sevenSignFaction = true; if (!faction_id.equals(npc.getFactionId()) && !sevenSignFaction) continue; // Check if the L2Object is inside the Faction Range of // the actor if
		 * (_actor.isInsideRadius(npc, npc.getFactionRange() + npc.getTemplate().collisionRadius, true, false) && npc.getAI() != null) { if (Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600 && _actor.getAttackByList().contains(originalAttackTarget) && (npc.getAI()._intention ==
		 * CtrlIntention.AI_INTENTION_IDLE || npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE) && GeoData.getInstance().canSeeTarget(_actor, npc)) { if ((originalAttackTarget instanceof L2PcInstance) || (originalAttackTarget instanceof L2Summon)) { if
		 * (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null) { L2PcInstance player = (originalAttackTarget instanceof L2PcInstance) ? (L2PcInstance) originalAttackTarget : ((L2Summon) originalAttackTarget).getOwner(); for (Quest quest :
		 * npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL)) quest.notifyFactionCall(npc, (L2NpcInstance) _actor, player, (originalAttackTarget instanceof L2Summon)); } } else if (npc instanceof L2Attackable && getAttackTarget() != null && npc.getAI()._intention !=
		 * CtrlIntention.AI_INTENTION_ATTACK) { ((L2Attackable) npc).addDamageHate(getAttackTarget(), 0, ((L2Attackable) _actor).getHating(getAttackTarget())); npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget()); } } } } } } catch (NullPointerException e) { LOGGER.warn(
		 * "L2AttackableAI: thinkAttack() faction call failed: " + e.getMessage(), e); } }
		 */
		
		// Call all L2Object of its Faction inside the Faction Range
		if (((L2NpcInstance) _actor).getFactionId() != null)
		{
			// Go through all L2Object that belong to its faction
			for (final L2Object obj : _actor.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2NpcInstance)
				{
					L2NpcInstance npc = (L2NpcInstance) obj;
					String faction_id = ((L2NpcInstance) _actor).getFactionId();
					
					if (!faction_id.equalsIgnoreCase(npc.getFactionId()) || npc.getFactionRange() == 0)
					{
						faction_id = null;
						continue;
					}
					
					// Check if the L2Object is inside the Faction Range of the actor
					if (_actor.getAttackByList() != null && _actor.isInsideRadius(npc, npc.getFactionRange(), true, false) && npc.getAI() != null && _actor.getAttackByList().contains(originalAttackTarget))
					{
						if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
						{
							
							if (GeoData.getInstance().canSeeTarget(_actor, npc) && Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600)
							{
								
								if (originalAttackTarget instanceof L2PcInstance && originalAttackTarget.isInParty() && originalAttackTarget.getParty().isInDimensionalRift())
								{
									final byte riftType = originalAttackTarget.getParty().getDimensionalRift().getType();
									final byte riftRoom = originalAttackTarget.getParty().getDimensionalRift().getCurrentRoom();
									
									if (_actor instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
									{
										continue;
									}
								}
								// Notify the L2Object AI with EVT_AGGRESSION
								npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
								
							}
							
						}
						
						if (GeoData.getInstance().canSeeTarget(_actor, npc) && Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 500)
						{
							
							if (originalAttackTarget instanceof L2PcInstance || originalAttackTarget instanceof L2Summon)
							{
								final L2PcInstance player = originalAttackTarget instanceof L2PcInstance ? (L2PcInstance) originalAttackTarget : ((L2Summon) originalAttackTarget).getOwner();
								for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL))
								{
									quest.notifyFactionCall(npc, (L2NpcInstance) _actor, player, (originalAttackTarget instanceof L2Summon));
								}
							}
							
						}
						
					}
					
					npc = null;
				}
			}
		}
		
		if (_actor.isAttackingDisabled())
			return;
		
		// Get all information needed to chose between physical or magical attack
		L2Skill[] skills = null;
		double dist2 = 0;
		int range = 0;
		
		try
		{
			_actor.setTarget(originalAttackTarget);
			skills = _actor.getAllSkills();
			dist2 = _actor.getPlanDistanceSq(originalAttackTarget.getX(), originalAttackTarget.getY());
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + originalAttackTarget.getTemplate().collisionRadius;
		}
		catch (final NullPointerException e)
		{
			// LOGGER.warn("AttackableAI: Attack target is NULL.");
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		L2Weapon weapon = _actor.getActiveWeaponItem();
		final int collision = _actor.getTemplate().collisionRadius;
		final int combinedCollision = collision + originalAttackTarget.getTemplate().collisionRadius;
		
		// ------------------------------------------------------
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		// Note from Gnacik:
		// On l2js because of that sometimes mobs don't attack player only running
		// around player without any sense, so decrease chance for now
		if (!_actor.isMovementDisabled() && Rnd.nextInt(100) <= 3)
		{
			for (final L2Object nearby : _actor.getKnownList().getKnownObjects().values())
			{
				if (nearby instanceof L2Attackable && _actor.isInsideRadius(nearby, collision, false, false) && nearby != originalAttackTarget)
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newX = originalAttackTarget.getX() + newX;
					else
						newX = originalAttackTarget.getX() - newX;
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newY = originalAttackTarget.getY() + newY;
					else
						newY = originalAttackTarget.getY() - newY;
					
					if (!_actor.isInsideRadius(newX, newY, collision, false))
					{
						final int newZ = _actor.getZ() + 30;
						if (Config.GEODATA == 0 || GeoData.getInstance().canMoveFromToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), newX, newY, newZ))
							moveTo(newX, newY, newZ);
					}
					return;
				}
			}
		}
		
		if (weapon != null && weapon.getItemType() == L2WeaponType.BOW)
		{
			// Micht: kepping this one otherwise we should do 2 sqrt
			final double distance2 = _actor.getPlanDistanceSq(originalAttackTarget.getX(), originalAttackTarget.getY());
			if (Math.sqrt(distance2) <= 60 + combinedCollision)
			{
				// double distance2 = _actor.getPlanDistanceSq(originalAttackTarget.getX(), originalAttackTarget.getY());
				// if(distance2 <= 10000)
				// {
				final int chance = 5;
				if (chance >= Rnd.get(100))
				{
					int posX = _actor.getX();
					int posY = _actor.getY();
					final int posZ = _actor.getZ();
					final double distance = Math.sqrt(distance2); // This way, we only do the sqrt if we need it
					
					int signx = -1;
					int signy = -1;
					if (_actor.getX() > originalAttackTarget.getX())
					{
						signx = 1;
					}
					if (_actor.getY() > originalAttackTarget.getY())
					{
						signy = 1;
					}
					
					posX += Math.round((float) (signx * (range / 2 + Rnd.get(range)) - distance));
					posY += Math.round((float) (signy * (range / 2 + Rnd.get(range)) - distance));
					setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
					return;
				}
			}
		}
		weapon = null;
		
		// Force mobs to attack anybody if confused
		L2Character hated;
		if (_actor.isConfused())
		{
			hated = originalAttackTarget;
		}
		else
		{
			hated = ((L2Attackable) _actor).getMostHated();
		}
		
		if (hated == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		if (hated != originalAttackTarget)
		{
			setAttackTarget(hated);
		}
		// We should calculate new distance cuz mob can have changed the target
		dist2 = _actor.getPlanDistanceSq(hated.getX(), hated.getY());
		
		if (hated.isMoving())
		{
			range += 50;
		}
		
		// Check if the actor isn't far from target
		if (dist2 > range * range)
		{
			// check for long ranged skills and heal/buff skills
			if (!_actor.isMuted() && (!Config.ALT_GAME_MOB_ATTACK_AI || _actor instanceof L2MonsterInstance && Rnd.nextInt(100) <= 5))
			{
				for (final L2Skill sk : skills)
				{
					final int castRange = sk.getCastRange();
					
					boolean _inRange = false;
					if (dist2 >= castRange * castRange / 9.0 && dist2 <= castRange * castRange && castRange > 70)
					{
						_inRange = true;
					}
					
					if ((sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL || _inRange) && !_actor.isSkillDisabled(sk) && _actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk) && !sk.isPassive() && Rnd.nextInt(100) <= 5)
					{
						
						if (sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL)
						{
							boolean useSkillSelf = true;
							
							if (sk.getSkillType() == L2Skill.SkillType.HEAL && _actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
							{
								useSkillSelf = false;
								break;
							}
							
							if (sk.getSkillType() == L2Skill.SkillType.BUFF)
							{
								L2Effect[] effects = _actor.getAllEffects();
								
								for (int i = 0; effects != null && i < effects.length; i++)
								{
									final L2Effect effect = effects[i];
									
									if (effect.getSkill() == sk)
									{
										useSkillSelf = false;
										break;
									}
								}
								
								effects = null;
							}
							if (useSkillSelf)
							{
								_actor.setTarget(_actor);
							}
						}
						
						L2Object OldTarget = _actor.getTarget();
						
						clientStopMoving(null);
						
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						OldTarget = null;
						
						return;
					}
				}
			}
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			if (hated.isMoving())
			{
				range -= 100;
			}
			if (range < 5)
			{
				range = 5;
			}
			
			moveToPawn(originalAttackTarget, range);
			
			return;
		}
		// Else, if this is close enough to attack
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// check for close combat skills && heal/buff skills
		if (!_actor.isMuted() /* && _rnd.nextInt(100) <= 5 */)
		{
			boolean useSkillSelf = true;
			
			for (final L2Skill sk : skills)
			{
				if (/* sk.getCastRange() >= dist && sk.getCastRange() <= 70 && */!sk.isPassive() && _actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk) && !_actor.isSkillDisabled(sk) && (Rnd.nextInt(100) <= 8 || _actor instanceof L2PenaltyMonsterInstance && Rnd.nextInt(100) <= 20))
				{
					if (sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL)
					{
						useSkillSelf = true;
						
						if (sk.getSkillType() == L2Skill.SkillType.HEAL && _actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
						{
							useSkillSelf = false;
							break;
						}
						
						if (sk.getSkillType() == L2Skill.SkillType.BUFF)
						{
							L2Effect[] effects = _actor.getAllEffects();
							
							for (int i = 0; effects != null && i < effects.length; i++)
							{
								final L2Effect effect = effects[i];
								
								if (effect.getSkill() == sk)
								{
									useSkillSelf = false;
									break;
								}
							}
							
							effects = null;
						}
						if (useSkillSelf)
						{
							_actor.setTarget(_actor);
						}
					}
					// GeoData Los Check here
					if (!useSkillSelf && !GeoData.getInstance().canSeeTarget(_actor, _actor.getTarget()))
						return;
					
					L2Object OldTarget = _actor.getTarget();
					
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					OldTarget = null;
					
					return;
				}
			}
		}
		
		// Finally, physical attacks
		clientStopMoving(null);
		_accessor.doAttack(hated);
		skills = null;
		hated = null;
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking || _actor.isAllSkillsDisabled())
			return;
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li> <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li> <li>Set the Intention to AI_INTENTION_ATTACK</li>
	 * <BR>
	 * <BR>
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(final L2Character attacker)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		// Add the attacker to the _aggroList of the actor
		((L2Attackable) _actor).addDamageHate(attacker, 0, 1);
		
		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}
		
		if (!((_actor instanceof L2NpcInstance && !(_actor instanceof L2Attackable)) && !(_actor instanceof L2PlayableInstance)))
		{
			
			// Set the Intention to AI_INTENTION_ATTACK
			if (getIntention() != AI_INTENTION_ATTACK)
			{
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
			else if (((L2Attackable) _actor).getMostHated() != getAttackTarget())
			{
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li> <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR>
	 * <BR>
	 * @param target the L2Character that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(final L2Character target, final int aggro)
	{
		L2Attackable me = (L2Attackable) _actor;
		
		// To avoid lag issue
		if (me.isDead())
			return;
		
		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already
			// present
			me.addDamageHate(target, 0, aggro);
			
			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send
				// Server->Client packet ChangeMoveType to all others
				// L2PcInstance
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}
				
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		me = null;
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}
	
	public void setGlobalAggro(final int value)
	{
		_globalAggro = value;
	}
}
