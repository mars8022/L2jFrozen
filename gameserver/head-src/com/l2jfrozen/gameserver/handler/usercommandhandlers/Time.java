/* * L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 *
 *
 */
public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};
	
	/*
	 * (non-Javadoc)
	 */
	@Override
	public boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		if (COMMAND_IDS[0] != id)
			return false;
		
		final int t = GameTimeController.getInstance().getGameTime();
		final String h = "" + t / 60 % 24;
		String m;
		if (t % 60 < 10)
		{
			m = "0" + t % 60;
		}
		else
		{
			m = "" + t % 60;
		}
		
		SystemMessage sm;
		if (GameTimeController.getInstance().isNowNight())
		{
			sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
			sm.addString(h);
			sm.addString(m);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
			sm.addString(h);
			sm.addString(m);
		}
		activeChar.sendPacket(sm);
		
		sm = null;
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
