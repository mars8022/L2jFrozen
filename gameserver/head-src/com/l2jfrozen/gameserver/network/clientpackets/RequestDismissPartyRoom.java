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

import java.util.logging.Logger;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestDismissPartyRoom.class.getName());
	private int _data1;
	private int _data2;

	@Override
	protected void readImpl()
	{
		_data1 = readD();
		_data2 = readD();
	}

	@Override
	protected void runImpl()
	{
		// TODO Auto-generated method stub
		_log.info("This packet is not well known : RequestDismissPartyRoom");
		_log.info("Data received: d:" + _data1 + " d:" + _data2);

	}

	@Override
	public String getType()
	{
		return "[C] D0:02 RequestDismissPartyRoom";
	}

}
