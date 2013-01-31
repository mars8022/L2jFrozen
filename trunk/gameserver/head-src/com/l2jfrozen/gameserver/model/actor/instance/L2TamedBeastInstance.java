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
package com.l2jfrozen.gameserver.model.actor.instance;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.random.Rnd;

// While a tamed beast behaves a lot like a pet (ingame) and does have
// an owner, in all other aspects, it acts like a mob.
// In addition, it can be fed in order to increase its duration.
// This class handles the running tasks, AI, and feed of the mob.
// The (mostly optional) AI on feeding the spawn is handled by the datapack ai script
/**
 * The Class L2TamedBeastInstance.
 */
public final class L2TamedBeastInstance extends L2FeedableBeastInstance
{
	
	/** The _food skill id. */
	private int _foodSkillId;
	
	/** The Constant MAX_DISTANCE_FROM_HOME. */
	private static final int MAX_DISTANCE_FROM_HOME = 30000;
	
	/** The Constant MAX_DISTANCE_FROM_OWNER. */
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	
	/** The Constant MAX_DURATION. */
	private static final int MAX_DURATION = 1200000; // 20 minutes
	
	/** The Constant DURATION_CHECK_INTERVAL. */
	private static final int DURATION_CHECK_INTERVAL = 60000; // 1 minute
	
	/** The Constant DURATION_INCREASE_INTERVAL. */
	private static final int DURATION_INCREASE_INTERVAL = 20000; // 20 secs (gained upon feeding)
	
	/** The Constant BUFF_INTERVAL. */
	private static final int BUFF_INTERVAL = 5000; // 5 seconds
	
	/** The _remaining time. */
	private int _remainingTime = MAX_DURATION;
	
	/** The _home z. */
	private int _homeX, _homeY, _homeZ;
	
	/** The _owner. */
	private L2PcInstance _owner;
	
	/** The _buff task. */
	private Future<?> _buffTask = null;
	
	/** The _duration check task. */
	private Future<?> _durationCheckTask = null;

	/**
	 * Instantiates a new l2 tamed beast instance.
	 *
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setHome(this);
	}

	/**
	 * Instantiates a new l2 tamed beast instance.
	 *
	 * @param objectId the object id
	 * @param template the template
	 * @param owner the owner
	 * @param foodSkillId the food skill id
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int foodSkillId, int x, int y, int z)
	{
		super(objectId, template);

		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setOwner(owner);
		setFoodType(foodSkillId);
		setHome(x, y, z);
		this.spawnMe(x, y, z);
	}

	/**
	 * On receive food.
	 */
	public void onReceiveFood()
	{
		// Eating food extends the duration by 20secs, to a max of 20minutes
		_remainingTime = _remainingTime + DURATION_INCREASE_INTERVAL;
		if(_remainingTime > MAX_DURATION)
		{
			_remainingTime = MAX_DURATION;
		}
	}

	/**
	 * Gets the home.
	 *
	 * @return the home
	 */
	public Point3D getHome()
	{
		return new Point3D(_homeX, _homeY, _homeZ);
	}

	/**
	 * Sets the home.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setHome(int x, int y, int z)
	{
		_homeX = x;
		_homeY = y;
		_homeZ = z;
	}

	/**
	 * Sets the home.
	 *
	 * @param c the new home
	 */
	public void setHome(L2Character c)
	{
		setHome(c.getX(), c.getY(), c.getZ());
	}

	/**
	 * Gets the remaining time.
	 *
	 * @return the remaining time
	 */
	public int getRemainingTime()
	{
		return _remainingTime;
	}

	/**
	 * Sets the remaining time.
	 *
	 * @param duration the new remaining time
	 */
	public void setRemainingTime(int duration)
	{
		_remainingTime = duration;
	}

	/**
	 * Gets the food type.
	 *
	 * @return the food type
	 */
	public int getFoodType()
	{
		return _foodSkillId;
	}

	/**
	 * Sets the food type.
	 *
	 * @param foodItemId the new food type
	 */
	public void setFoodType(int foodItemId)
	{
		if(foodItemId > 0)
		{
			_foodSkillId = foodItemId;

			// start the duration checks
			// start the buff tasks
			if(_durationCheckTask != null)
			{
				_durationCheckTask.cancel(true);
			}

			_durationCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance#doDie(com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
			return false;

		getAI().stopFollow();
		cleanTasks();
		
		return true;
	}
	
	/**
	 * Clean tasks.
	 */
	private synchronized void cleanTasks(){
		
		if(_buffTask != null){
			_buffTask.cancel(true);
			_buffTask = null;
		}
		
		if(_durationCheckTask!=null){
			_durationCheckTask.cancel(true);
			_durationCheckTask = null;
		}
		
		// clean up variables
		if(_owner != null)
		{
			_owner.setTrainedBeast(null);
			_owner = null;
		}

		_foodSkillId = 0;
		_remainingTime = 0;
		
	}

	/**
	 * Gets the owner.
	 *
	 * @return the owner
	 */
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	/**
	 * Sets the owner.
	 *
	 * @param owner the new owner
	 */
	public void setOwner(L2PcInstance owner)
	{
		if(owner != null)
		{
			_owner = owner;
			setTitle(owner.getName());
			// broadcast the new title
			broadcastPacket(new NpcInfo(this, owner));

			owner.setTrainedBeast(this);

			// always and automatically follow the owner.
			getAI().startFollow(_owner, 100);

			// instead of calculating this value each time, let's get this now and pass it on
			int totalBuffsAvailable = 0;
			for(L2Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if(skill.getSkillType() == L2Skill.SkillType.BUFF)
				{
					totalBuffsAvailable++;
				}
			}

			// start the buff tasks
			if(_buffTask != null)
			{
				_buffTask.cancel(true);
			}
			_buffTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL, BUFF_INTERVAL);
		}
		else
		{
			doDespawn(); // despawn if no owner
		}
	}

	/**
	 * Checks if is too far from home.
	 *
	 * @return true, if is too far from home
	 */
	public boolean isTooFarFromHome()
	{
		return !this.isInsideRadius(_homeX, _homeY, _homeZ, MAX_DISTANCE_FROM_HOME, true, true);
	}

	/**
	 * Do despawn.
	 */
	public void doDespawn()
	{
		// stop running tasks
		getAI().stopFollow();
		stopHpMpRegeneration();
		setTarget(null);
		cleanTasks();
		onDecay();
	}

	// notification triggered by the owner when the owner is attacked.
	// tamed mobs will heal/recharge or debuff the enemy according to their skills
	/**
	 * On owner got attacked.
	 *
	 * @param attacker the attacker
	 */
	public void onOwnerGotAttacked(L2Character attacker)
	{
		// check if the owner is no longer around...if so, despawn
		if(_owner == null || _owner.isOnline() == 0)
		{
			doDespawn();
			return;
		}

		// if the owner is too far away, stop anything else and immediately run towards the owner.
		if(!_owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true))
		{
			getAI().startFollow(_owner);
			return;
		}

		// if the owner is dead, do nothing...
		if(_owner.isDead())
			return;

		// if the tamed beast is currently in the middle of casting, let it complete its skill...
		if(isCastingNow())
			return;

		float HPRatio = (float) _owner.getCurrentHp() / _owner.getMaxHp();

		// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
		// use of more than one debuff at this moment is acceptable
		if(HPRatio >= 0.8)
		{
			for(L2Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a debuff, check if the attacker has it already [  attacker.getEffect(L2Skill skill) ]
				if(skill.getSkillType() == L2Skill.SkillType.DEBUFF && Rnd.get(3) < 1 && attacker.getFirstEffect(skill) != null)
				{
					sitCastAndFollow(skill, attacker);
				}
			}
		}
		// for HP levels between 80% and 50%, do not react to attack events (so that MP can regenerate a bit)
		// for lower HP ranges, heal or recharge the owner with 1 skill use per attack.
		else if(HPRatio < 0.5)
		{
			int chance = 1;
			if(HPRatio < 0.25)
			{
				chance = 2;
			}

			// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
			for(L2Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if(Rnd.get(5) < chance && (skill.getSkillType() == L2Skill.SkillType.HEAL || skill.getSkillType() == L2Skill.SkillType.HOT || skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE || skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT || skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC || skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL || skill.getSkillType() == L2Skill.SkillType.COMBATPOINTPERCENTHEAL || skill.getSkillType() == L2Skill.SkillType.CPHOT || skill.getSkillType() == L2Skill.SkillType.MANAHEAL || skill.getSkillType() == L2Skill.SkillType.MANA_BY_LEVEL || skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT || skill.getSkillType() == L2Skill.SkillType.MANARECHARGE || skill.getSkillType() == L2Skill.SkillType.MPHOT))
				{
					sitCastAndFollow(skill, _owner);
					return;
				}
			}
		}
	}

	/**
	 * Prepare and cast a skill: First smoothly prepare the beast for casting, by abandoning other actions Next, call
	 * super.doCast(skill) in order to actually cast the spell Finally, return to auto-following the owner.
	 *
	 * @param skill the skill
	 * @param target the target
	 * @see com.l2jfrozen.gameserver.model.L2Character#doCast(com.l2jfrozen.gameserver.model.L2Skill)
	 */
	protected void sitCastAndFollow(L2Skill skill, L2Character target)
	{
		stopMove(null);
		broadcastPacket(new StopMove(this));
		getAI().setIntention(AI_INTENTION_IDLE);

		setTarget(target);
		doCast(skill);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
	}

	/**
	 * The Class CheckDuration.
	 */
	private class CheckDuration implements Runnable
	{
		
		/** The _tamed beast. */
		private L2TamedBeastInstance _tamedBeast;

		/**
		 * Instantiates a new check duration.
		 *
		 * @param tamedBeast the tamed beast
		 */
		CheckDuration(L2TamedBeastInstance tamedBeast)
		{
			_tamedBeast = tamedBeast;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			int foodTypeSkillId = _tamedBeast.getFoodType();
			L2PcInstance owner = _tamedBeast.getOwner();
			_tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL);

			// I tried to avoid this as much as possible...but it seems I can't avoid hardcoding
			// ids further, except by carrying an additional variable just for these two lines...
			// Find which food item needs to be consumed.
			L2ItemInstance item = null;

			if(foodTypeSkillId == 2188)
			{
				item = owner.getInventory().getItemByItemId(6643);
			}
			else if(foodTypeSkillId == 2189)
			{
				item = owner.getInventory().getItemByItemId(6644);
			}

			// if the owner has enough food, call the item handler (use the food and triffer all necessary actions)
			if(item != null && item.getCount() >= 1)
			{
				L2Object oldTarget = owner.getTarget();
				owner.setTarget(_tamedBeast);
				L2Object[] targets =
				{
					_tamedBeast
				};

				// emulate a call to the owner using food, but bypass all checks for range, etc
				// this also causes a call to the AI tasks handling feeding, which may call onReceiveFood as required.
				owner.callSkill(SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets);
				owner.setTarget(oldTarget);
				oldTarget = null;
				targets = null;
			}
			else
			{
				// if the owner has no food, the beast immediately despawns, except when it was only
				// newly spawned.  Newly spawned beasts can last up to 5 minutes
				if(_tamedBeast.getRemainingTime() < MAX_DURATION - 300000)
				{
					_tamedBeast.setRemainingTime(-1);
				}
			}

			/* There are too many conflicting reports about whether distance from home should
			 * be taken into consideration.  Disabled for now.
			 *
			if (_tamedBeast.isTooFarFromHome())
				_tamedBeast.setRemainingTime(-1);
			*/

			if(_tamedBeast.getRemainingTime() <= 0)
			{
				_tamedBeast.doDespawn();
			}

			item = null;
		}
	}

	/**
	 * The Class CheckOwnerBuffs.
	 */
	private class CheckOwnerBuffs implements Runnable
	{
		
		/** The _tamed beast. */
		private L2TamedBeastInstance _tamedBeast;
		
		/** The _num buffs. */
		private int _numBuffs;

		/**
		 * Instantiates a new check owner buffs.
		 *
		 * @param tamedBeast the tamed beast
		 * @param numBuffs the num buffs
		 */
		CheckOwnerBuffs(L2TamedBeastInstance tamedBeast, int numBuffs)
		{
			_tamedBeast = tamedBeast;
			_numBuffs = numBuffs;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			L2PcInstance owner = _tamedBeast.getOwner();

			// check if the owner is no longer around...if so, despawn
			if(owner == null || owner.isOnline() == 0)
			{
				doDespawn();
				return;
			}

			// if the owner is too far away, stop anything else and immediately run towards the owner.
			if(!isInsideRadius(owner, MAX_DISTANCE_FROM_OWNER, true, true))
			{
				getAI().startFollow(owner);
				return;
			}

			// if the owner is dead, do nothing...
			if(owner.isDead())
				return;

			// if the tamed beast is currently casting a spell, do not interfere (do not attempt to cast anything new yet).
			if(isCastingNow())
				return;

			int totalBuffsOnOwner = 0;
			int i = 0;
			int rand = Rnd.get(_numBuffs);
			L2Skill buffToGive = null;

			// get this npc's skills:  getSkills()
			for(L2Skill skill : _tamedBeast.getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if(skill.getSkillType() == L2Skill.SkillType.BUFF)
				{
					if(i == rand)
					{
						buffToGive = skill;
					}
					i++;
					if(owner.getFirstEffect(skill) != null)
					{
						totalBuffsOnOwner++;
					}
				}
			}
			// if the owner has less than 60% of this beast's available buff, cast a random buff
			if(_numBuffs * 2 / 3 > totalBuffsOnOwner)
			{
				_tamedBeast.sitCastAndFollow(buffToGive, owner);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _tamedBeast.getOwner());
			buffToGive = null;
		}
	}
}
