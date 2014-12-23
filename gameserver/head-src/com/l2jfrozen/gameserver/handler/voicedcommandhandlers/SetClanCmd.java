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
package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class SetClanCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"set name",
		"set home",
		"set group"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.startsWith("set privileges"))
		{
			final int n = Integer.parseInt(command.substring(15));
			L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
			
			if (pc != null)
			{
				if (activeChar.getClan().getClanId() == pc.getClan().getClanId() && activeChar.getClanPrivileges() > n || activeChar.isClanLeader())
				{
					pc.setClanPrivileges(n);
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Your clan privileges have been set to " + n + " by " + activeChar.getName());
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
			pc = null;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
