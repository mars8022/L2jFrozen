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
package com.l2jfrozen.loginserver.network.loginserverpackets;

import com.l2jfrozen.loginserver.L2LoginServer;
import com.l2jfrozen.loginserver.network.serverpackets.ServerBasePacket;

/**
 * @author -Wooden-
 */
public class InitLS extends ServerBasePacket
{
	// ID 0x00
	// format
	// d proto rev
	// d key size
	// b key
	
	public InitLS(final byte[] publickey)
	{
		writeC(0x00);
		writeD(L2LoginServer.PROTOCOL_REV);
		writeD(publickey.length);
		writeB(publickey);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
