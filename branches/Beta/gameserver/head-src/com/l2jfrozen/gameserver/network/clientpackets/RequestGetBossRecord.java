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

import com.l2jfrozen.gameserver.managers.RaidBossPointsManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ExGetBossRecord;

/**
 * Format: (ch) d
 * @author -Wooden-
 */
public class RequestGetBossRecord extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _bossId;

	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		int points = RaidBossPointsManager.getPointsByOwnerId(activeChar.getObjectId());
		int ranking = RaidBossPointsManager.calculateRanking(activeChar.getObjectId());

		// trigger packet
		activeChar.sendPacket(new ExGetBossRecord(ranking, points, RaidBossPointsManager.getList(activeChar)));
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return "[C] D0:18 RequestGetBossRecord";
	}

}
