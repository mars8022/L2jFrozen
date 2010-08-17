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

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.instancemanager.RaidBossPointsManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2Summon;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

/**
 * This class manages all Grand Bosses.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2GrandBossInstance extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
    protected static final Map<Integer, Integer> _radius = new FastMap<Integer, Integer>();
    protected ScheduledFuture<?> _locationMaintainTask = null;

	protected boolean _isInSocialAction = false;

	public boolean IsInSocialAction()
	{
		return _isInSocialAction;
	}

	public void setIsInSocialAction(boolean value)
	{
		_isInSocialAction = value;
	}

	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * @param objectIdID of the instance
	 * @param templateL2NpcTemplate of the instance
	 */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		if(_radius.size() == 0)
			_radius.put(29001, 4000);	// Queen Ant
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}

	@Override
	public void onSpawn()
	{
		setIsRaid(true);
    	if (getNpcId() == 29020 || getNpcId() == 29028 || getNpcId() == 29019 
    			|| getNpcId() == 29046 || getNpcId() == 29047) // baium and valakas are all the time in passive mode, theirs attack AI handled in AI scripts
    		super.disableCoreAI(true);
		if (getNpcId() == 35368)
			super.disableCoreAI(false);

		super.onSpawn();
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		super.reduceCurrentHp(damage, attacker, awake);
	}

	/**
	 * @see interlude.gameserver.model.actor.instance.L2MonsterInstance#doDie(interlude.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		L2PcInstance player = null;
		if (killer instanceof L2PcInstance)
			player = (L2PcInstance) killer;

		else if (killer instanceof L2Summon)
			player = ((L2Summon) killer).getOwner();

		if (player != null)
		{
			broadcastPacket(new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
				}
			} else
				RaidBossPointsManager.addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
		}
		return true;
	}

	@Override
	public void doAttack(L2Character target)
	{
		if (_isInSocialAction)
			return;
		else
			super.doAttack(target);
	}

	@Override
	public void doCast(L2Skill skill)
	{
		if (_isInSocialAction)
			return;
		else
			super.doCast(skill);
	}

    protected void manageLocation()
    {
    	if(!_radius.containsKey(getNpcId()))
    		return;

    	_locationMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
    	{
    		public void run()
    		{
    			// teleport raid boss home if it's too far from home location
    			L2Spawn bossSpawn = getSpawn();
    			if(!isInsideRadius(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), _radius.get(getNpcId()), true, false))
    				teleToLocation(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), true);
    		}
    	}, 20000, getMaintenanceInterval() + Rnd.get(5000));
    }
}
