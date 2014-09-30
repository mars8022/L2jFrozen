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
package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStart;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfrozen.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.Die;
import com.l2jfrozen.gameserver.network.serverpackets.MoveToLocationInVehicle;
import com.l2jfrozen.gameserver.network.serverpackets.MoveToPawn;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.network.serverpackets.StopRotation;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * Mother class of all objects AI in the world.<BR>
 * <BR>
 * AbastractAI :<BR>
 * <BR>
 * <li>L2CharacterAI</li><BR>
 * <BR>
 */
abstract class AbstractAI implements Ctrl
{

	protected static final Logger _log = Logger.getLogger(AbstractAI.class.getName());

	class FollowTask implements Runnable
	{
		protected int _range = 60;
		protected boolean newtask = true;

		public FollowTask()
		{
		//null
		}

		public FollowTask(int range)
		{
			_range = range;
		}

		@Override
		public void run()
		{
			try
			{
				if(_followTask == null)
					return;
				
				final L2Character follow = getFollowTarget();

				if(follow == null)
				{
					stopFollow();
					return;
				}
				if(!_actor.isInsideRadius(follow, _range, true, false))
				{
					moveToPawn(follow, _range);
				}
				else if(newtask)
				{
					newtask = false;
					_actor.broadcastPacket(new MoveToPawn(_actor, follow, _range));
				}
			}
			catch(Throwable t)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
				
				_log.log(Level.WARNING, "", t);
			}
		}
	}

	/** The character that this AI manages */
	protected final L2Character _actor;

	/** An accessor for private methods of the actor */
	protected final L2Character.AIAccessor _accessor;

	/** Current long-term intention */
	private CtrlIntention _intention = AI_INTENTION_IDLE;
	/** Current long-term intention parameter */
	private Object _intentionArg0 = null;
	/** Current long-term intention parameter */
	private Object _intentionArg1 = null;

	/** Flags about client's state, in order to know which messages to send */
	protected boolean _clientMoving;
	/** Flags about client's state, in order to know which messages to send */
	protected boolean _clientAutoAttacking;
	/** Flags about client's state, in order to know which messages to send */
	protected int _clientMovingToPawnOffset;

	/** Different targets this AI maintains */
	private L2Object _target;
	private L2Character _castTarget;
	private L2Character _attackTarget;
	private L2Character _followTarget;

	
	/** Diferent internal state flags */
	private int _moveToPawnTimeout;

	protected Future<?> _followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;

	/**
	 * Constructor of AbstractAI.<BR>
	 * <BR>
	 * 
	 * @param accessor The AI accessor of the L2Character
	 */
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;

		// Get the L2Character managed by this Accessor AI
		_actor = accessor.getActor();
	}

	/**
	 * Return the L2Character managed by this Accessor AI.<BR>
	 * <BR>
	 */
	@Override
	public L2Character getActor()
	{
		return _actor;
	}

	/**
	 * Set the Intention of this AbstractAI.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR>
	 * <BR>
	 * <B><U> Overriden in </U> : </B><BR>
	 * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR>
	 * <BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	public synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
		 if (Config.DEBUG)
		 _log.warning("AbstractAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 * @param arg1 The second parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(!_actor.isVisible() || !_actor.hasAI())
			return;

		/*
		 if (Config.DEBUG)
		 _log.warning("AbstractAI: setIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		// Stop the follow mode if necessary
		if(intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK)
		{
			stopFollow();
		}

		// Launch the onIntention method of the L2CharacterAI corresponding to the new Intention
		switch(intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((L2CharPosition) arg0);
				break;
			case AI_INTENTION_MOVE_TO_IN_A_BOAT:
				onIntentionMoveToInABoat((L2CharPosition) arg0, (L2CharPosition) arg1);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
		_actor.fireEvent(EventType.SETINTENTION.name, new Object[]
		{
			intention
		});
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character
	 * attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * 
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character
	 * attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * 
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character
	 * attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * 
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if(!_actor.isVisible() || !_actor.hasAI() || (_actor instanceof L2PcInstance && ((L2PcInstance)_actor).isOffline()))
			return;

		/*
		 if (Config.DEBUG)
		 _log.warning("AbstractAI: notifyEvent -> " + evt + " " + arg0 + " " + arg1);
		 */

		switch(evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				onEvtReadyToAct();
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				onEvtArrived();
				break;
			case EVT_ARRIVED_REVALIDATE:
				onEvtArrivedRevalidate();
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((L2CharPosition) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(L2Character target);

	protected abstract void onIntentionCast(L2Skill skill, L2Object target);

	protected abstract void onIntentionMoveTo(L2CharPosition destination);

	protected abstract void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin);

	protected abstract void onIntentionFollow(L2Character target);

	protected abstract void onIntentionPickUp(L2Object item);

	protected abstract void onIntentionInteract(L2Object object);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(L2Character attacker);

	protected abstract void onEvtAggression(L2Character target, int aggro);

	protected abstract void onEvtStunned(L2Character attacker);

	protected abstract void onEvtSleeping(L2Character attacker);

	protected abstract void onEvtRooted(L2Character attacker);

	protected abstract void onEvtConfused(L2Character attacker);

	protected abstract void onEvtMuted(L2Character attacker);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtUserCmd(Object arg0, Object arg1);

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedRevalidate();

	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);

	protected abstract void onEvtForgetObject(L2Object object);

	protected abstract void onEvtCancel();

	protected abstract void onEvtDead();

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientActionFailed()
	{
		if(_actor instanceof L2PcInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn
	 * <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param pawn 
	 * @param offset 
	 */
	public void moveToPawn(L2Object pawn, int offset)
	{
		// Chek if actor can move
		if(!_actor.isMovementDisabled())
		{
			if(offset < 10)
			{
				offset = 10;
			}

			// prevent possible extra calls to this function (there is none?), 
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if(_clientMoving && getTarget() == pawn)
			{
				if(_clientMovingToPawnOffset == offset)
				{
					if(GameTimeController.getGameTicks() < _moveToPawnTimeout)
						return;

					sendPacket = false;
				}
				else if(_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if(GameTimeController.getGameTicks() < _moveToPawnTimeout + 10)
						return;
				}
			}

			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			
			setTarget(pawn);
			
			_moveToPawnTimeout = GameTimeController.getGameTicks();
			_moveToPawnTimeout += /*1000*/200 / GameTimeController.MILLIS_IN_TICK;

			if(pawn == null || _accessor == null)
				return;

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);

			if(!_actor.isMoving())
			{
				_actor.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			if(pawn instanceof L2Character)
			{
				if(_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new CharMoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else if(sendPacket)
				{
					_actor.broadcastPacket(new MoveToPawn(_actor, (L2Character) pawn, offset));
				}
			}
			else
			{
				_actor.broadcastPacket(new CharMoveToLocation(_actor));
			}
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet
	 * CharMoveToLocation <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param x 
	 * @param y 
	 * @param z 
	 */
	public void moveTo(int x, int y, int z)
	{
		// Chek if actor can move
		if(!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(x, y, z);

			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			CharMoveToLocation msg = new CharMoveToLocation(_actor);
			_actor.broadcastPacket(msg);
			msg = null;

		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	protected void moveToInABoat(L2CharPosition destination, L2CharPosition origin)
	{
		// Chek if actor can move
		if(!_actor.isMovementDisabled())
		{
			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			//CharMoveToLocation msg = new CharMoveToLocation(_actor);
			if(((L2PcInstance) _actor).getBoat() != null)
			{
				MoveToLocationInVehicle msg = new MoveToLocationInVehicle(_actor, destination, origin);
				_actor.broadcastPacket(msg);
				msg = null;
			}

		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation
	 * <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param pos 
	 */
	protected void clientStopMoving(L2CharPosition pos)
	{
		/*
		if (true && _actor instanceof L2PcInstance){
			_log.warning("clientStopMoving();");
			Thread.dumpStack();
		}
		*/

		// Stop movement of the L2Character
		if(_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		_clientMovingToPawnOffset = 0;

		if(_clientMoving || pos != null)
		{
			_clientMoving = false;

			// Send a Server->Client packet StopMove to the actor and all L2PcInstance in its _knownPlayers
			StopMove msg = new StopMove(_actor);
			_actor.broadcastPacket(msg);
			msg = null;

			if(pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all L2PcInstance in its _knownPlayers
				StopRotation sr = new StopRotation(_actor, pos.heading, 0);
				_actor.sendPacket(sr);
				_actor.broadcastPacket(sr);
				sr = null;
			}
		}
	}

	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if(_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			StopMove msg = new StopMove(_actor);
			_actor.broadcastPacket(msg);
			msg = null;
		}
		_clientMoving = false;
	}

	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	/*
	public void clientStartAutoAttack()
	{
		if(!isAutoAttacking())
		{
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}
	*/

	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	/*
	public void clientStopAutoAttack()
	{
		if(_actor instanceof L2PcInstance)
		{
			if(!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor) && isAutoAttacking())
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if(isAutoAttacking())
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}
		setAutoAttacking(false);
	}
	*/
	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 *
	 */
	public void clientStartAutoAttack()
	{
		if(((_actor instanceof L2NpcInstance
			&& !(_actor instanceof L2Attackable))
			&& !(_actor instanceof L2PlayableInstance))){
			return;
		}
		
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
				summon.getOwner().getAI().clientStartAutoAttack();
			return;
		}
		if (!isAutoAttacking())
		{
			if (_actor instanceof L2PcInstance && ((L2PcInstance)_actor).getPet() != null)
				((L2PcInstance)_actor).getPet().broadcastPacket(new AutoAttackStart(((L2PcInstance)_actor).getPet().getObjectId()));
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}
	
	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 *
	 */
	public void clientStopAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
				summon.getOwner().getAI().clientStopAutoAttack();
			return;
		}
		
		final boolean isAutoAttacking = isAutoAttacking();
		
		if (_actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor) && isAutoAttacking)
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
		}
		else if (isAutoAttacking)
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}
	

	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die
	 * <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientNotifyDead()
	{
		// Send a Server->Client packet Die to the actor and all L2PcInstance in its _knownPlayers
		Die msg = new Die(_actor);
		_actor.broadcastPacket(msg);
		msg = null;

		// Init AI
		setIntention(AI_INTENTION_IDLE);
		setTarget(null);
		setAttackTarget(null);
		setCastTarget(null);
		
		// Cancel the follow task if necessary
		stopFollow();
	}

	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and
	 * AutoAttackStart to the L2PcInstance player.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * 
	 * @param player The L2PcIstance to notify with state of this L2Character
	 */
	public void describeStateToPlayer(L2PcInstance player)
	{
		if(_clientMoving)
		{
			final L2Character follow = getFollowTarget();
			
			if(_clientMovingToPawnOffset != 0 &&  follow!= null)
			{
				// Send a Server->Client packet MoveToPawn to the actor and all L2PcInstance in its _knownPlayers
				MoveToPawn msg = new MoveToPawn(_actor, follow, _clientMovingToPawnOffset);
				player.sendPacket(msg);
				msg = null;
			}
			else
			{
				// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
				CharMoveToLocation msg = new CharMoveToLocation(_actor);
				player.sendPacket(msg);
				msg = null;
			}
		}
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 1s.<BR>
	 * <BR>
	 * 
	 * @param target The L2Character to follow
	 */
	public synchronized void startFollow(L2Character target)
	{
		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		// Create and Launch an AI Follow Task to execute every 1s
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.<BR>
	 * <BR>
	 * 
	 * @param target The L2Character to follow
	 * @param range 
	 */
	public synchronized void startFollow(L2Character target, int range)
	{
		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}

	/**
	 * Stop an AI Follow Task.<BR>
	 * <BR>
	 */
	public synchronized void stopFollow()
	{
		if(_followTask != null)
		{
			// Stop the Follow Task
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = null;
	}

	protected synchronized L2Character getFollowTarget()
	{
		return _followTarget;
	}

	protected synchronized L2Object getTarget()
	{
		return _target;
	}

	protected synchronized void setTarget(L2Object target)
	{
		_target = target;
	}
	
	protected synchronized void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}

	/**
	 * @return the current cast target.
	 */
	public synchronized L2Character getCastTarget()
	{
		return _castTarget;
	}

	protected synchronized void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}

	/**
	 * Return current attack target.<BR>
	 * <BR>
	 */
	@Override
	public synchronized L2Character getAttackTarget()
	{
		return _attackTarget;
	}

	public synchronized  boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}

	public synchronized void setAutoAttacking(boolean isAutoAttacking)
	{
		_clientAutoAttacking = isAutoAttacking;
	}

	
	/**
	 * @return the _intentionArg0
	 */
	public synchronized Object get_intentionArg0()
	{
		return _intentionArg0;
	}

	/**
	 * @param _intentionArg0 the _intentionArg0 to set
	 */
	public synchronized void set_intentionArg0(Object _intentionArg0)
	{
		this._intentionArg0 = _intentionArg0;
	}

	/**
	 * @return the _intentionArg1
	 */
	public synchronized Object get_intentionArg1()
	{
		return _intentionArg1;
	}

	/**
	 * @param _intentionArg1 the _intentionArg1 to set
	 */
	public synchronized void set_intentionArg1(Object _intentionArg1)
	{
		this._intentionArg1 = _intentionArg1;
	}

	/**
	 * Return the current Intention.<BR>
	 * <BR>
	 */
	@Override
	public synchronized CtrlIntention getIntention()
	{
		return _intention;
	}

	
}
