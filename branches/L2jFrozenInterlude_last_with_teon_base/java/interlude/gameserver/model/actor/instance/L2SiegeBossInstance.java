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

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortResistSiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

/**
 * Author: Maxi
 */
public final class L2SiegeBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000;

	protected boolean _isInSocialAction = false;

	public L2SiegeBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home location at the time of this check, teleport it home
	 */
	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			public void run()
			{
				// teleport raid boss home if it's too far from home location
				L2Spawn bossSpawn = getSpawn();
				if (!isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), 5000, true, false))
				{
					teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
					healFull(); // prevents minor exploiting with it
				}
				_minionList.maintainMinions();
			}
		}, 60000, getMaintenanceInterval() + Rnd.get(5000));
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		super.reduceCurrentHp(damage, attacker, awake);
		if (this.getNpcId() == 35368)
		{
			if (attacker instanceof L2PcInstance 
					&& ((L2PcInstance) attacker).getClan() != null 
					&& FortResistSiegeManager.getInstance().getIsInProgress())
				FortResistSiegeManager.getInstance().addSiegeDamage(((L2PcInstance) attacker).getClan(), damage);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (getNpcId() == 35368 && FortResistSiegeManager.getInstance().getIsInProgress())
			FortResistSiegeManager.getInstance().endSiege(true);

		else if (getNpcId() == 35410 && DevastatedCastleManager.getInstance().getIsInProgress())
			DevastatedCastleManager.getInstance().endSiege(killer);

		else if (getNpcId() == 35629 && FortressofTheDeadManager.getInstance().getIsInProgress())
			FortressofTheDeadManager.getInstance().endSiege(killer);
		return true;
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();
	}

	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}

	public boolean IsInSocialAction()
	{
		return _isInSocialAction;
	}

	public void setIsInSocialAction(boolean value)
	{
		_isInSocialAction = value;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (!FortResistSiegeManager.getInstance().getIsInProgress() || FortressofTheDeadManager.getInstance().getIsInProgress() || DevastatedCastleManager.getInstance().getIsInProgress())
			return false;
		return true;
	}
}
