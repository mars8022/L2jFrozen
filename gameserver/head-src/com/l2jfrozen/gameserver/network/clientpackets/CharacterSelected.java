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
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelected;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends L2GameClientPacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());

	// cd
	private int _charSlot;

	@SuppressWarnings("unused")
	private int _unk1; // new in C4

	@SuppressWarnings("unused")
	private int _unk2; // new in C4

	@SuppressWarnings("unused")
	private int _unk3; // new in C4

	@SuppressWarnings("unused")
	private int _unk4; // new in C4

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
		// if there is a playback.dat file in the current directory, it will
		// be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to
		// be a  [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		//playLogFile(getConnection()); // try to play log file
		
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
			return;

		
		// we should always be abble to acquire the lock
		// but if we cant lock then nothing should be done (ie repeated packet)
		if(getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if(getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if(Config.DEBUG)
					{
						_log.fine("selected slot:" + _charSlot);
					}

					//load up character from disk
					L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);

					if(cha == null)
					{
						if(Config.DEBUG)
							_log.severe("Character could not be loaded (slot:" + _charSlot + ")");
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					if(cha.getAccessLevel().getLevel() < 0)
					{
						cha.deleteMe();
						return;
					}

					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					nProtect.getInstance().sendRequest(getClient());
					getClient().setState(GameClientState.IN_GAME);
					CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
					sendPacket(cs);

				}
			}
			catch(Exception e)
			{
				//never happen :)
				e.printStackTrace();
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
			}
		}
	}

	/*
	private void playLogFile(Connection connection)
	{
		long diff = 0;
		long first = -1;

		try
		{
			LineNumberReader lnr =
			new LineNumberReader(new FileReader("playback.dat"));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.length() > 0 && line.substring(0, 1).equals("1"))
				{
					String timestamp = line.substring(0, 13);
					long time = Long.parseLong(timestamp);
					if (first == -1)
					{
						long start = System.currentTimeMillis();
						first = time;
						diff = start - first;
					}

					String cs = line.substring(14, 15);
					// read packet definition
					ByteArrayOutputStream bais = new ByteArrayOutputStream();

					while (true)
					{
						String temp = lnr.readLine();
						if (temp.length() < 53)
						{
							break;
						}

						String bytes = temp.substring(6, 53);
						StringTokenizer st = new StringTokenizer(bytes);
						while (st.hasMoreTokens())
						{
							String b = st.nextToken();
							int number = Integer.parseInt(b, 16);
							bais.write(number);
						}
					}

					if (cs.equals("S"))
					{
						//wait for timestamp and send packet
						int wait =
						(int) (time + diff - System.currentTimeMillis());
						if (wait > 0)
						{
							if (Config.DEBUG) _log.fine("waiting"+ wait);
							Thread.sleep(wait);
						}
						if (Config.DEBUG) _log.fine("sending:"+ time);
						byte[] data = bais.toByteArray();
						if (data.length != 0)
						{
							//connection.sendPacket(data);
						}
						else
						{
							if (Config.DEBUG) _log.fine("skipping broken data");
						}

					}
					else
					{
						// skip packet
					}
				}

			}
		}
		catch (FileNotFoundException f)
		{
			// should not happen
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error:", e);
		}
	}
	 */

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}
}
