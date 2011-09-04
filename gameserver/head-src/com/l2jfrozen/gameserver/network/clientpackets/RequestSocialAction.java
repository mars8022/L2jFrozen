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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestSocialAction.class.getName());
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// You cannot do anything else while fishing
		if(activeChar.isFishing())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}

		// check if its the actionId is allowed
		if(_actionId < 2 || _actionId > 13)
		{
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " requested an internal Social Action.", Config.DEFAULT_PUNISH);
			return;
		}

		if(activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isAlikeDead() && (!activeChar.isAllSkillsDisabled() || activeChar.isInDuel()) && activeChar.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			if(Config.DEBUG)
			{
				_log.fine("Social Action:" + _actionId);
			}

			SocialAction atk = new SocialAction(activeChar.getObjectId(), _actionId);
			activeChar.broadcastPacket(atk);
			/*
			// Schedule a social task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SocialTask(this), 2600);
			activeChar.setIsParalyzed(true);
			*/
		}
	}

	/*
	class SocialTask implements Runnable
	{
		L2PcInstance _player;
		SocialTask(RequestSocialAction action)
		{
			_player = getClient().getActiveChar();
		}
		public void run()
		{
			_player.setIsParalyzed(false);
		}
	}
	*/

	@Override
	public String getType()
	{
		return "[C] 1B RequestSocialAction";
	}
}
