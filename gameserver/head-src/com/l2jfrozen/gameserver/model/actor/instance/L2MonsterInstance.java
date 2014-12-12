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
package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.MonsterKnownList;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.MinionList;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all Monsters. L2MonsterInstance :<BR>
 * <BR>
 * <li>L2MinionInstance</li> <li>L2RaidBossInstance</li> <li>L2GrandBossInstance</li>
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	// private static Logger LOGGER = Logger.getLogger(L2MonsterInstance.class);
	
	/** The _minion list. */
	protected final MinionList _minionList;
	
	/** The _minion maintain task. */
	protected ScheduledFuture<?> _minionMaintainTask = null;
	
	/** The Constant MONSTER_MAINTENANCE_INTERVAL. */
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the L2MonsterInstance</li> <li>Create a RandomAnimation Task that will be launched after the calculated
	 * delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2MonsterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_minionList = new MinionList(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Attackable#getKnownList()
	 */
	@Override
	public final MonsterKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof MonsterKnownList))
		{
			setKnownList(new MonsterKnownList(this));
		}
		return (MonsterKnownList) super.getKnownList();
	}
	
	/**
	 * Return home.
	 */
	public void returnHome()
	{
		ThreadPoolManager.getInstance().scheduleAi(new Runnable()
		{
			@Override
			public void run()
			{
				L2Spawn mobSpawn = getSpawn();
				if (!isInCombat() && !isAlikeDead() && !isDead() && mobSpawn != null && !isInsideRadius(mobSpawn.getLocx(), mobSpawn.getLocy(), Config.MAX_DRIFT_RANGE, false))
				{
					teleToLocation(mobSpawn.getLocx(), mobSpawn.getLocy(), mobSpawn.getLocz(), false);
				}
				mobSpawn = null;
			}
		}, Config.MONSTER_RETURN_DELAY * 1000);
	}
	
	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR>
	 * <BR>
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;
		
		return !isEventMob;
	}
	
	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR>
	 * <BR>
	 * @return true, if is aggressive
	 */
	@Override
	public boolean isAggressive()
	{
		return getTemplate().aggroRange > 0 && !isEventMob;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Attackable#onSpawn()
	 */
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getTemplate().getMinionData() != null)
		{
			try
			{
				for (final L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null)
					{
						continue;
					}
					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();
				
				manageMinions();
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			
			switch (getTemplate().npcId)
			{
				case 12372: // baium
				{
					SocialAction sa = new SocialAction(getObjectId(), 2);
					broadcastPacket(sa);
					sa = null;
				}
			}
		}
	}
	
	/**
	 * Gets the maintenance interval.
	 * @return the maintenance interval
	 */
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	/**
	 * Spawn all minions at a regular interval.
	 */
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval());
	}
	
	/**
	 * Call minions.
	 */
	public void callMinions()
	{
		if (_minionList.hasMinions())
		{
			for (final L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				// Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Get the coords of the master to use as a base to move the minion to
					final int masterX = getX();
					final int masterY = getY();
					final int masterZ = getZ();
					
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = masterX + Rnd.nextInt(401) - 200;
					int minionY = masterY + Rnd.nextInt(401) - 200;
					final int minionZ = masterZ;
					while (minionX != masterX + 30 && minionX != masterX - 30 || minionY != masterY + 30 && minionY != masterY - 30)
					{
						minionX = masterX + Rnd.nextInt(401) - 200;
						minionY = masterY + Rnd.nextInt(401) - 200;
					}
					
					// Move the minion to the new coords
					if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
					{
						minion.moveToLocation(minionX, minionY, minionZ, 0);
					}
				}
			}
		}
	}
	
	/**
	 * Call minions to assist.
	 * @param attacker the attacker
	 */
	public void callMinionsToAssist(final L2Character attacker)
	{
		if (_minionList.hasMinions())
		{
			List<L2MinionInstance> spawnedMinions = _minionList.getSpawnedMinions();
			if (spawnedMinions != null && spawnedMinions.size() > 0)
			{
				final Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
				L2MinionInstance minion;
				while (itr.hasNext())
				{
					minion = itr.next();
					// Trigger the aggro condition of the minion
					if (minion != null && !minion.isDead())
					{
						if (this instanceof L2RaidBossInstance)
						{
							minion.addDamage(attacker, 100);
						}
						else
						{
							minion.addDamage(attacker, 1);
						}
					}
				}
				spawnedMinions = null;
				minion = null;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Attackable#doDie(com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_minionMaintainTask != null)
		{
			_minionMaintainTask.cancel(true); // doesn't do it?
		}
		
		if (this instanceof L2RaidBossInstance)
		{
			deleteSpawnedMinions();
		}
		return true;
	}
	
	/**
	 * Gets the spawned minions.
	 * @return the spawned minions
	 */
	public List<L2MinionInstance> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions instances.
	 * @return the total spawned minions instances
	 */
	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions groups.
	 * @return the total spawned minions groups
	 */
	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}
	
	/**
	 * Notify minion died.
	 * @param minion the minion
	 */
	public void notifyMinionDied(final L2MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}
	
	/**
	 * Notify minion spawned.
	 * @param minion the minion
	 */
	public void notifyMinionSpawned(final L2MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}
	
	/**
	 * Checks for minions.
	 * @return true, if successful
	 */
	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Attackable#addDamageHate(com.l2jfrozen.gameserver.model.L2Character, int, int)
	 */
	@Override
	public void addDamageHate(final L2Character attacker, final int damage, final int aggro)
	{
		if (!(attacker instanceof L2MonsterInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance#deleteMe()
	 */
	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (_minionMaintainTask != null)
			{
				_minionMaintainTask.cancel(true);
			}
			
			deleteSpawnedMinions();
		}
		super.deleteMe();
	}
	
	/**
	 * Delete spawned minions.
	 */
	public void deleteSpawnedMinions()
	{
		for (final L2MinionInstance minion : getSpawnedMinions())
		{
			if (minion == null)
			{
				continue;
			}
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			getSpawnedMinions().remove(minion);
		}
		_minionList.clearRespawnList();
	}
}
