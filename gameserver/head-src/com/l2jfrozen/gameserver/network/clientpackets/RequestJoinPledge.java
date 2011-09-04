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

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.AskJoinPledge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
	private int _target;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(!(L2World.getInstance().findObject(_target) instanceof L2PcInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return;
		}

		L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_target);
		L2Clan clan = activeChar.getClan();

		if(!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
			return;

		if(!activeChar.getRequest().setRequest(target, this))
			return;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2);
		sm.addString(activeChar.getName());
		sm.addString(activeChar.getClan().getName());
		target.sendPacket(sm);
		sm = null;
		AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName());
		target.sendPacket(ap);
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	@Override
	public String getType()
	{
		return "[C] 24 RequestJoinPledge";
	}
}
