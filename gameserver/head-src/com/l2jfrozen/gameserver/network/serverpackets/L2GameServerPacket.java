/* L2jFrozen Project - www.l2jfrozen.com 
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

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.netcore.SendablePacket;

/**
 * The Class L2GameServerPacket.
 * @author ProGramMoS
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(L2GameServerPacket.class);
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (final Throwable t)
		{
			LOGGER.error("Client: " + getClient().toString() + " - Failed writing: " + getType() + " - L2J Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION, t);
			t.printStackTrace();
		}
	}
	
	/**
	 * Run impl.
	 */
	public void runImpl()
	{
		
	}
	
	/**
	 * Write impl.
	 */
	protected abstract void writeImpl();
	
	/**
	 * Gets the type.
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}
