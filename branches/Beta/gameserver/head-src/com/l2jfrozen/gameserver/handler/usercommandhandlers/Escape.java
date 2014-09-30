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
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.event.VIP;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 *
 *
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jfrozen.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{

		int unstuckTimer = activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000;

		// Check to see if the current player is in Festival.
		if(activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape command in a festival.");
			return false;
		}

		// Check to see if the current player is in TVT Event.
		if(activeChar._inEventTvT && TvT.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in TvT.");
			return false;
		}

		// Check to see if the current player is in CTF Event.
		if(activeChar._inEventCTF && CTF.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in CTF.");
			return false;
		}

		// Check to see if the current player is in DM Event.
		if(activeChar._inEventDM && DM.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in DM.");
			return false;
		}

		// Check to see if the current player is in Vip Event.
		if(activeChar._inEventVIP && VIP._started)
		{
			activeChar.sendMessage("You may not use an escape skill in VIP.");
			return false;
		}

		// Check to see if the current player is in Grandboss zone.
		if(GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
		{
			activeChar.sendMessage("You may not use an escape command in Grand boss zone.");
			return false;
		}

		// Check to see if the current player is in jail.
		if(activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}

		// Check to see if the current player is in fun event.
		if(activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You may not escape from an Event.");
			return false;
		}
		
		// Check to see if the current player is in Observer Mode.
		if(activeChar.inObserverMode())
		{
			activeChar.sendMessage("You may not escape during Observer mode.");
			return false;
		}
		
		// Check to see if the current player is sitting.
		if(activeChar.isSitting())
		{
		    activeChar.sendMessage("You may not escape when you sitting.");
		    return false;
		}
		
		// Check player status.
		if(activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.isAwaying())
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		
		if(unstuckTimer<60000)
	         sm.addString("You use Escape: "+ unstuckTimer / 1000 +" seconds.");
	    else
	    	 sm.addString("You use Escape: "+ unstuckTimer / 60000 +" minutes.");
		
		activeChar.sendPacket(sm);
		sm = null;

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();

		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, unstuckTimer, 0);
		activeChar.setTarget(null); // Like retail we haven't self target
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		msk = null;
		sg = null;
		//End SoE Animation section
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);

		ef = null;

		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;

		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			if(_activeChar.isDead())
				return;

			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();

			try
			{
				if(_activeChar.getKarma()>0 && Config.ALT_KARMA_TELEPORT_TO_FLORAN){
					_activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
					return;
				}
				
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			catch(Throwable e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
