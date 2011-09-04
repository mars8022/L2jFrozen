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

import com.l2jfrozen.gameserver.datatables.sql.HennaTreeTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.HennaEquipList;

/**
 * RequestHennaList - 0xba
 * @author Tempy
 */
public final class RequestHennaList extends L2GameClientPacket
{
	// This is just a trigger packet...
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId());
		HennaEquipList he = new HennaEquipList(activeChar, henna);
		activeChar.sendPacket(he);
	}

	@Override
	public String getType()
	{
		return "[C] ba RequestHennaList";
	}
}
