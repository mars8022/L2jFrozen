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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeReceivePowerInfo;

/**
 * Format: (ch) dS
 * @author -Wooden-
 */
public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unk1;
	private String _player;
	
	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_player = readS();
	}
	
	@Override
	protected void runImpl()
	{
		// LOGGER.info("C5: RequestPledgeMemberPowerInfo d:"+_unk1);
		// LOGGER.info("C5: RequestPledgeMemberPowerInfo S:"+_player);
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		// do we need powers to do that??
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		final L2ClanMember member = clan.getClanMember(_player);
		if (member == null)
			return;
		activeChar.sendPacket(new PledgeReceivePowerInfo(member));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:1B RequestPledgeMemberPowerInfo";
	}
}
