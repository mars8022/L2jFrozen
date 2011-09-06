/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfrozen.gameserver.network.clientpackets;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.netcore.ReceivablePacket;

/**
 * Packets received by the game server from clients
 * @author KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

	@Override
	protected boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (BufferOverflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();

			if (getClient() != null)
				getClient().closeNow();

			_log.severe("Client: " + getClient().toString() + " - Buffer overflow and has been kicked");
		}
		catch (BufferUnderflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			getClient().onBufferUnderflow();
		}
		catch (Throwable t)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);
			t.printStackTrace();
		}
		return false;
	}

	protected abstract void readImpl();

	@Override
	public void run()
	{
		try
		{	
			runImpl();
            if (this instanceof MoveBackwardToLocation || this instanceof AttackRequest || this instanceof RequestMagicSkillUse)
            	if (getClient().getActiveChar() != null)
            		getClient().getActiveChar().onActionRequest(); // Removes onSpawn Protection
		}
		catch (Throwable t)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);
			t.printStackTrace();

			if (this instanceof EnterWorld)
				getClient().closeNow();
		}
	}

	protected abstract void runImpl();

	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}

	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}