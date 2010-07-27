/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2Attackable;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.knownlist.MonsterKnownList;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.gameserver.util.MinionList;
import interlude.util.Rnd;

/**
 * This class manages all Monsters. L2MonsterInstance :<BR>
 * <BR>
 * <li>L2MinionInstance</li> <li>L2RaidBossInstance</li>
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	protected final MinionList _minionList;
	@SuppressWarnings( { "unchecked" })
	protected ScheduledFuture _minionMaintainTask = null;
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;

	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the L2MonsterInstance</li> <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li> <BR>
	 * <BR>
	 *
	 * @param objectId
	 *            Identifier of the object to initialized
	 * @param L2NpcTemplate
	 *            Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_minionList = new MinionList(this);
	}

	@Override
	public final MonsterKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof MonsterKnownList))
			setKnownList(new MonsterKnownList(this));
		
		return (MonsterKnownList) super.getKnownList();
	}

	/**
	 * Return true if the attacker is not another L2MonsterInstance.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;
		
		return !isEventMob;
	}

	/**
	 * Return true if the L2MonsterInstance is Agressive (aggroRange > 0).
	 */
	@Override
	public boolean isAggressive()
	{
		switch (getTemplate().npcId)
        {
	        case 35633:
	        case 35634:
	        case 35635:
	        case 35636:
	        case 35637:
	        case 35411:
	        case 35412:
	        case 35413:
	        case 35414:
	        case 35415:
	        case 35416:
				break;
	        default:
	        	return getTemplate().aggroRange > 1200;
        }
		return getTemplate().aggroRange > 0 && !isEventMob;
	}

	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		switch (getTemplate().npcId)
        {
	        case 35633:
	        case 35634:
	        case 35635:
	        case 35636:
	        case 35637:
	        case 35411:
	        case 35412:
	        case 35413:
	        case 35414:
	        case 35415:
	        case 35416:
				break;
	        default:
	    		if (getStat().getWalkSpeed() <= 0)
	    			return;

			if (getSpawn() != null && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
			{
				setisReturningToSpawnPoint(true);
				clearAggroList();
				if (hasAI())
					getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
        }
	}

    @Override
	public boolean hasRandomAnimation()
	{
		switch (getTemplate().npcId)
        {
	        case 35633:
	        case 35634:
	        case 35635:
	        case 35636:
	        case 35637:
	        case 35411:
	        case 35412:
	        case 35413:
	        case 35414:
	        case 35415:
	        case 35416:
				break;
	        default:
	    		return false;
        }
		return true;
	}

	@Override
	public void onSpawn()
	{
			switch (getTemplate().npcId)
	        {
		        case 35633:
		        case 35634:
		        case 35635:
		        case 35636:
		        case 35637:
		        case 35411:
		        case 35412:
		        case 35413:
		        case 35414:
		        case 35415:
		        case 35416:
					super.disableCoreAI(true);
					break;
		        default:
	        }
		super.onSpawn();
		if (getTemplate().getMinionData() != null)
		{
			try
			{
				for (L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null) continue;
					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();
				manageMinions();
			}
			catch (NullPointerException e)
			{
			}
		}
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval
	 */
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval());
	}

	public void callMinions()
	{
		if (_minionList.hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
			{
				// Get actual coords of the minion and check to see if it's too
				// far away from this L2MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Get the coords of the master to use as a base to move the minion to
					int masterX = getX();
					int masterY = getY();
					int masterZ = getZ();
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = masterX + Rnd.nextInt(401) - 200;
					int minionY = masterY + Rnd.nextInt(401) - 200;
					int minionZ = masterZ;
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

	public void callMinionsToAssist(L2Character attacker)
	{
		if (_minionList.hasMinions())
		{
			List<L2MinionInstance> spawnedMinions = _minionList.getSpawnedMinions();
			if (spawnedMinions != null && spawnedMinions.size() > 0)
			{
				Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
				L2MinionInstance minion;
				while (itr.hasNext())
				{
					minion = itr.next();
					// Trigger the aggro condition of the minion
					if (minion != null && !minion.isDead())
					{
						if (isRaid() && !isRaidMinion())
							minion.addDamage(attacker, 100);
						else
							minion.addDamage(attacker, 1);
					}
				}
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (_minionMaintainTask != null)
			_minionMaintainTask.cancel(true); // doesn't do it?

		if (isRaid() && !isRaidMinion())
			deleteSpawnedMinions();

		return true;
	}

	public List<L2MinionInstance> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}

	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}

	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}

	public void notifyMinionDied(L2MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}

	public void notifyMinionSpawned(L2MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}

	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!(attacker instanceof L2MonsterInstance))
			super.addDamageHate(attacker, damage, aggro);
	}

	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (_minionMaintainTask != null)
				_minionMaintainTask.cancel(true);
			deleteSpawnedMinions();
		}
		super.deleteMe();
	}

	public void deleteSpawnedMinions()
	{
		for (L2MinionInstance minion : getSpawnedMinions())
		{
			if (minion == null) continue;

			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			getSpawnedMinions().remove(minion);
		}
		_minionList.clearRespawnList();
	}
}
