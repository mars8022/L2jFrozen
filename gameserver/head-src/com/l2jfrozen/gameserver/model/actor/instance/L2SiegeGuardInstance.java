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

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2SiegeGuardAI;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.SiegeGuardKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class represents all guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK's or custom interactions.
 * @version $Revision: 1.1.3 $ $Date: 2009/04/29 01:15:40 $
 * @author programmos
 */
public class L2SiegeGuardInstance extends L2Attackable
{
	private static Logger LOGGER = Logger.getLogger(L2GuardInstance.class);
	
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	
	public L2SiegeGuardInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // inits the knownlist
	}
	
	@Override
	public SiegeGuardKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof SiegeGuardKnownList))
		{
			setKnownList(new SiegeGuardKnownList(this));
		}
		
		return (SiegeGuardKnownList) super.getKnownList();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		synchronized (this)
		{
			if (_ai == null)
			{
				_ai = new L2SiegeGuardAI(new AIAccessor());
			}
		}
		return _ai;
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR>
	 * <BR>
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Attackable during siege by all except defenders ( Castle or Fort )
		return attacker != null && attacker instanceof L2PcInstance && (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && !getCastle().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan()) || DevastatedCastle.getInstance().getIsInProgress());
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
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
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			su = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					SocialAction sa = new SocialAction(getObjectId(), Rnd.nextInt(8));
					broadcastPacket(sa);
					sendPacket(sa);
					showChatWindow(player, 0);
					sa = null;
				}
			}
		}
	}
	
	@Override
	public void addDamageHate(final L2Character attacker, final int damage, final int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2SiegeGuardInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
