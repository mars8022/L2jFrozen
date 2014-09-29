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
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.JoinPledge;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		L2PcInstance requestor = activeChar.getRequest().getPartner();
		
		if (requestor == null)
			return;
		
		if (_answer == 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION);
			sm.addString(requestor.getName());
			activeChar.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
				return; // hax
				
			RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			L2Clan clan = requestor.getClan();
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if (clan != null && clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType()))
			{
				JoinPledge jp = new JoinPledge(requestor.getClanId());
				activeChar.sendPacket(jp);
				
				activeChar.setPledgeType(requestPacket.getPledgeType());
				
				if (requestPacket.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
				{
					activeChar.setPowerGrade(9); // adademy
					activeChar.setLvlJoinedAcademy(activeChar.getLevel());
				}
				else
				{
					activeChar.setPowerGrade(5); // new member starts at 5, not confirmed
				}
				
				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
				
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ENTERED_THE_CLAN));
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
				sm.addString(activeChar.getName());
				clan.broadcastToOnlineMembers(sm);
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				
				// this activates the clan tab on the new member
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
			}
		}
		
		activeChar.getRequest().onRequestResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] 25 RequestAnswerJoinPledge";
	}
}