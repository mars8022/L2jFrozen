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

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _objectId;

	@SuppressWarnings("unused")
	private int _unk1;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		//TODO analyse value unk1
		_unk1 = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO: this packet is currently for starting auto join
		L2PcInstance player = (L2PcInstance) L2World.getInstance().findObject(_objectId);
		if(player == null)
			return;

		PartyMatchDetail details = new PartyMatchDetail(player);
		sendPacket(details);
	}

	@Override
	public String getType()
	{
		return "[C] 71 RequestPartyMatchDetail";
	}
}
