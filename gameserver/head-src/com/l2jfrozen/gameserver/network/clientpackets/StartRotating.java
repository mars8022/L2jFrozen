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

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.BeginRotation;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class StartRotating extends L2GameClientPacket
{
	private static final String _C__4A_STARTROTATING = "[C] 4A StartRotating";

	private int _degree;
	private int _side;

	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	protected void runImpl()
	{
		if(getClient().getActiveChar() == null)
			return;
		
		if(!Config.ALLOW_USE_CURSOR_FOR_WALK){
			getClient().getActiveChar().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		//Move flood protection
		if (!getClient().getFloodProtectors().getMoveAction().tryPerformAction("StartRotating"))
		{
			getClient().getActiveChar().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		BeginRotation br = new BeginRotation(getClient().getActiveChar(), _degree, _side, 0);
		getClient().getActiveChar().broadcastPacket(br);
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__4A_STARTROTATING;
	}
}
