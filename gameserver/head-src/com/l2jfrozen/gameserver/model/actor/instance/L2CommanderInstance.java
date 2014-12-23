/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.CommanderKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author programmos
 */

public class L2CommanderInstance extends L2Attackable
{
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	
	public L2CommanderInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR>
	 * <BR>
	 * @param attacker The L2Character that the L2CommanderInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Attackable during siege by all except defenders
		return attacker != null && attacker instanceof L2PcInstance && getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && !getFort().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan());
	}
	
	@Override
	public final CommanderKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof CommanderKnownList))
		{
			setKnownList(new CommanderKnownList(this));
		}
		return (CommanderKnownList) super.getKnownList();
	}
	
	@Override
	public void addDamageHate(final L2Character attacker, final int damage, final int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2CommanderInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (getFort().getSiege().getIsInProgress())
		{
			getFort().getSiege().killedCommander(this);
		}
		
		return true;
	}
	
	/**
	 * Sets home location of guard. Guard will always try to return to this location after it has killed all PK's in range.
	 */
	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
		}
	}
	
	public int getHomeX()
	{
		return _homeX;
	}
	
	public int getHomeY()
	{
		return _homeY;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	public void returnHome()
	{
		if (!isInsideRadius(_homeX, _homeY, 40, false))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
			}
		}
	}
	
}
