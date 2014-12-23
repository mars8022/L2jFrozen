/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelected;

@SuppressWarnings("unused")
public class CharacterSelected extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(CharacterSelected.class);
	private int _charSlot;
	private int _unk1, _unk2, _unk3, _unk4; // new in C4
		
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to be a [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		// playLogFile(getConnection()); // try to play LOGGER file
		
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
			return;
		
		// we should always be abble to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
		if (getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null but if not then this is repeated packet and nothing should be done here
				if (getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if (Config.DEBUG)
						LOGGER.debug("DEBUG " + getType() + ": selected slot:" + _charSlot);
					
					// Load up character from disk
					final L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
					
					if (cha == null)
					{
						LOGGER.warn(getType() + ": Character could not be loaded (slot:" + _charSlot + ")");
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					if (cha.getAccessLevel().getLevel() < 0)
					{
						cha.deleteMe();
						return;
					}
					
					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					nProtect.getInstance().sendRequest(getClient());
					getClient().setState(GameClientState.IN_GAME);
					sendPacket(new CharSelected(cha, getClient().getSessionId().playOkID1));
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 0D CharacterSelected";
	}
}