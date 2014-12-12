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
package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private static final String _S__73_ValidateLocationInVehicle = "[S] 73 ValidateLocationInVehicle";
	private int _boat = 1343225858;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _playerObj;
	
	public ValidateLocationInVehicle(final L2Character player)
	{
		_playerObj = player.getObjectId();
		_x = player.getX();
		_y = player.getY();
		_z = player.getZ();
		_heading = player.getHeading();
		_boat = ((L2PcInstance) player).getBoat().getId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x73);
		writeD(_playerObj);
		writeD(_boat);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
	
	@Override
	public String getType()
	{
		return _S__73_ValidateLocationInVehicle;
	}
}