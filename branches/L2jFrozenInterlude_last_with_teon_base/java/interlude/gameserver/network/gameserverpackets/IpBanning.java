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
package interlude.gameserver.network.gameserverpackets;

import java.io.IOException;

/**
 *@author KidZor
 */
public class IpBanning extends GameServerBasePacket
{
	public IpBanning(String ip, long duration)
	{
		writeC(0x07);
		writeS(ip);
		writeD((int) duration);
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.gameserverpackets.GameServerBasePacket#getContent()
	 */
	@Override
	public byte[] getContent() throws IOException
	{
		return getBytes();
	}
}