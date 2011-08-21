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

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class CannotMoveAnymore extends L2GameClientPacket
{
	private static final String _C__36_STOPMOVE = "[C] 36 CannotMoveAnymore";

	private static Logger _log = Logger.getLogger(CannotMoveAnymore.class.getName());

	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Character player = getClient().getActiveChar();

		if(player == null)
			return;

		if(Config.DEBUG)
		{
			_log.fine("client: x:" + _x + " y:" + _y + " z:" + _z + " server x:" + player.getX() + " y:" + player.getY() + " z:" + player.getZ());
		}

		if(player.getAI() != null)
		{
			player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new L2CharPosition(_x, _y, _z, _heading));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__36_STOPMOVE;
	}
}
