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

import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.Castle;
import interlude.gameserver.model.entity.Fort;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author KenM
 */
public final class RequestJoinSiege extends L2GameClientPacket
{
	private static final String _C__A4_RequestJoinSiege = "[C] a4 RequestJoinSiege";

	private int _castleId;
	private int _isAttacker;
	private int _isJoining;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (!activeChar.isClanLeader())
			return;
		{
		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null && _castleId != 34 && _castleId != 64)
			return;
			
		if (castle==null && _isAttacker == 0) // ClanHall have no defender clans
			return;

		if (_isJoining == 1)
		{
			if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
				return;
			}
			if (_isAttacker == 1)
				if (_castleId == 34)
					DevastatedCastleManager.getInstance().registerClan(activeChar);
				else if (_castleId == 64)
					FortressofTheDeadManager.getInstance().registerClan(activeChar);
				else
					castle.getSiege().registerAttacker(activeChar);
			else
				if (castle != null)
				castle.getSiege().registerDefender(activeChar);
			}
			else if (_castleId == 34)
					DevastatedCastleManager.getInstance().removeSiegeClan(activeChar);
				else if (_castleId == 64)
					FortressofTheDeadManager.getInstance().removeSiegeClan(activeChar);
			else
				castle.getSiege().removeSiegeClan(activeChar);
				if (_castleId == 34)
					DevastatedCastleManager.getInstance().listRegisterClan(activeChar);
				else if (_castleId == 64)
					FortressofTheDeadManager.getInstance().listRegisterClan(activeChar);
				else
			castle.getSiege().listRegisterClan(activeChar);
		}
			Fort fort = FortManager.getInstance().getFortById(_castleId);
			if (fort == null)
				return;

			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
					return;
				}
				if (_isAttacker == 1)
					fort.getSiege().registerAttacker(activeChar);
				else
					fort.getSiege().registerDefender(activeChar);
			}
			else
				fort.getSiege().removeSiegeClan(activeChar);
					fort.getSiege().listRegisterClan(activeChar);
	}

	@Override
	public String getType()
	{
		return _C__A4_RequestJoinSiege;
	}
}
