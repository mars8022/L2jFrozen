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

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.knownlist.GuardNoHTMLKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all Guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK and aggressive L2MonsterInstance.<BR>
 * <BR>
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2GuardNoHTMLInstance extends L2Attackable
{
	private static Logger _log = Logger.getLogger(L2GuardNoHTMLInstance.class.getName());
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private static final int RETURN_INTERVAL = 60000;

	public class ReturnTask implements Runnable
	{
		@Override
		public void run()
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
				returnHome();
			}
		}
	}

	/**
	 * Constructor of L2GuardInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the L2GuardInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the L2GuardInstance</li> <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li> <BR>
	 * <BR>
	 *
	 * @param objectId
	 *            Identifier of the object to initialized
	 * @param L2NpcTemplate
	 *            Template to apply to the NPC
	 */
	public L2GuardNoHTMLInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.nextInt(60000));
	}

	@Override
	public final GuardNoHTMLKnownList getKnownList()
	{
		if(super.getKnownList() == null || !(super.getKnownList() instanceof GuardNoHTMLKnownList)) {
			setKnownList(new GuardNoHTMLKnownList(this));
		}
		return(GuardNoHTMLKnownList) super.getKnownList();
	}

	/**
	 * Return true if hte attacker is a L2MonsterInstance.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}

	/**
	 * Set home location of the L2GuardInstance.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Guard will always try to return to this location after it has killed all PK's in range <BR>
	 * <BR>
	 */
	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		if(Config.DEBUG) {
			_log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
		}
	}

	public int getHomeX()
	{
		return _homeX;
	}

	/**
	 * Notify the L2GuardInstance to return to its home location (AI_INTENTION_MOVE_TO) and clear its _aggroList.<BR>
	 * <BR>
	 */
	public void returnHome()
	{
		if(!isInsideRadius(_homeX, _homeY, 150, false))
		{
			if(Config.DEBUG) {
				_log.fine(getObjectId() + ": moving hometo" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
			}
			clearAggroList();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
		}
	}

	/**
	 * Set the home location of its L2GuardInstance.<BR>
	 * <BR>
	 */
	@Override
	public void onSpawn()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		if(Config.DEBUG) {
			_log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
		}
		// check the region where this mob is, do not activate the AI if region is inactive.
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if(region != null && !region.isActive()) {
			((L2AttackableAI) getAI()).stopAITask();
		}
	}

	/**
	 * Manage actions when a player click on the L2GuardInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2GuardInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2GuardInstance as target of the L2PcInstance player (if necessary)</li> <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li> <li>Set the L2PcInstance Intention to AI_INTENTION_IDLE</li> <li>Send a Server->Client packet ValidateLocation to correct the L2GuardInstance position and heading on the client</li> <BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2GuardInstance (Attack it/Interact with it)</U> :</B><BR>
	 * <BR>
	 * <li>If L2PcInstance is in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_ATTACK</li> <li>If L2PcInstance is NOT in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li> <BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li> <BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance that start an action on the L2GuardInstance
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		// Check if the L2PcInstance already target the L2GuardInstance
		if(getObjectId() != player.getTargetId())
		{
			// Set the L2PcInstance Intention to AI_INTENTION_IDLE
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			if(Config.DEBUG) {
				_log.fine(player.getObjectId() + ": Targetted guard " + getObjectId());
			}
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance
			// player The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			// Send a Server->Client packet ValidateLocation to correct the
			// L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Check if the L2PcInstance is in the _aggroList of the L2GuardInstance
			if(containsTarget(player))
			{
				if(Config.DEBUG) {
					_log.fine(player.getObjectId() + ": Attacked guard " + getObjectId());
				}
				// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if(!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
				{
					// Set the L2PcInstance Intention to AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
					// Set the L2PcInstance Intention to AI_INTENTION_IDLE
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, null);
				}
			}
		}
	}
}
