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
package interlude.gameserver.network.clientpackets;

import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.JoinPledge;
import interlude.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import interlude.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import interlude.gameserver.network.serverpackets.PledgeShowMemberListAll;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
	// private static Logger _log =
	// Logger.getLogger(RequestAnswerJoinPledge.class.getName());
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
		{
			return;
		}
		L2PcInstance requestor = activeChar.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}
		if (_answer == 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION);
			sm.addString(requestor.getName());
			activeChar.sendPacket(sm);
			sm = null;
			sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
			sm = null;
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
			{
				return; // hax
			}
			RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			L2Clan clan = requestor.getClan();
			// we must double check this cause during response time
			// conditions
			// can be changed, i.e. another player could join clan
			if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType()))
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
					activeChar.setPowerGrade(5); // new member starts at
					// 5,
					// not confirmed
				}
				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ENTERED_THE_CLAN));
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
				sm.addString(activeChar.getName());
				clan.broadcastToOnlineMembers(sm);
				sm = null;
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				// this activates the clan tab on the new member
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
				activeChar.setCanWithdrawCWH(0);
			}
		}
		activeChar.getRequest().onRequestResponse();
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__25_REQUESTANSWERJOINPLEDGE;
	}
}
