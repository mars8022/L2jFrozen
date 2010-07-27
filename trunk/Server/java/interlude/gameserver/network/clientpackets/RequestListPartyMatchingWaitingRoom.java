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
package interlude.gameserver.network.clientpackets;

/**
 * Format: (ch) this is just a trigger : no data
 *
 * @author -Wooden-
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static final String _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:16 RequestListPartyMatchingWaitingRoom";

	@Override
	protected void readImpl()
	{
		// trigger
	}

	/**
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		// TODO
		System.out.println("C5: RequestListPartyMatchingWaitingRoom");
	}

	/**
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM;
	}
}