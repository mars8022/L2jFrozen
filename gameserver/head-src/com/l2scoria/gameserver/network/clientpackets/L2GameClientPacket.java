/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.network.clientpackets;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.logging.Logger;


import com.l2scoria.Config;
import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2scoria.gameserver.util.FloodProtector;
import com.scoria.netcore.ReceivablePacket;

/**
 * Packets received by the game server from clients
 * 
 * @author KenM
 */

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

	@Override
	protected boolean read()
	{
		//System.out.println(this.getType());
		try
		{
			readImpl();
			return true;
		}
		catch(BufferOverflowException e)
		{
			if(getClient()!=null)
				getClient().closeNow();
			_log.severe("Client: " + getClient().toString() + " - Buffer overflow and has been kicked");
		}
		catch(BufferUnderflowException e)
		{
			if(getClient()!=null)
				getClient().closeNow();
			_log.severe("Client: " + getClient().toString() + " - Buffer underflow and has been kicked");
		}
		catch(Throwable t)
		{
			_log.severe("Client: " + getClient().toString() + " - Failed reading: " + getType() + " - L2Scoria Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
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
			// flood protection
			if(GameTimeController.getGameTicks() - getClient().packetsSentStartTick > 10)
			{
				getClient().packetsSentStartTick = GameTimeController.getGameTicks();
				getClient().packetsSentInSec = 0;
			}
			else
			{
				getClient().packetsSentInSec++;
				if(getClient().packetsSentInSec > FloodProtector.PROTECTED_ACTIVE_PACKETS)
				{
					if(getClient().packetsSentInSec < FloodProtector.PROTECTED_ACTIVE_PACKETS2)
					{
						sendPacket(ActionFailed.STATIC_PACKET);
					}
					return;
				}
			}

			runImpl();
			if(this instanceof MoveBackwardToLocation || this instanceof AttackRequest || this instanceof RequestMagicSkillUse)
			// could include pickup and talk too, but less is better
			{
				// Removes onspawn protection - player has faster computer than
				// average
				if(getClient().getActiveChar() != null)
				{
					getClient().getActiveChar().onActionRequest();
				}
			}
		}
		catch(Throwable t)
		{
			_log.severe("Client: " + getClient().toString() + " - Failed running: " + getType() + " - L2Scoria Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
			t.printStackTrace();
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
