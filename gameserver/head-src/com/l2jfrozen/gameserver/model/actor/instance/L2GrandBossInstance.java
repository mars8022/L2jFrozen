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
package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossPointsManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all Grand Bosses.
 * 
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2GrandBossInstance extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 20000;

	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * 
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
			return false;

		L2PcInstance player = null;

		if(killer instanceof L2PcInstance)
			player = (L2PcInstance) killer;
		else if(killer instanceof L2Summon)
			player = ((L2Summon) killer).getOwner();

		if(player != null)
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
			broadcastPacket(msg);
			msg = null;
			if(player.getParty() != null)
			{
				for(L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				}
			}
			else
				RaidBossPointsManager.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
		}
		return true;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if(!this.getSpawn().is_customBossInstance())
			GrandBossManager.getInstance().addBoss(this);
	}

	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				// Teleport raid boss home if it's too far from home location
				L2Spawn bossSpawn = getSpawn();
				
				int rb_lock_range = Config.RBLOCKRAGE;
				if(Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcid())!=null){
					rb_lock_range = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcid());
				}
				
				if(rb_lock_range>=100 && !isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), rb_lock_range, true, false))
				{
					teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
					//healFull(); // Prevents minor exploiting with it
				}
				
				_minionList.maintainMinions();
			}
		}, 60000, getMaintenanceInterval());
	}
	
	@Override
	public boolean isRaid()
	{
		return true;
	}

	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
}
