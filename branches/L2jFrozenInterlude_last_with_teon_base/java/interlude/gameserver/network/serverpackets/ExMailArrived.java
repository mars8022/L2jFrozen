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
package interlude.gameserver.network.serverpackets;

import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * Fromat: (ch)
 *
 * @author -Specialwolf-
 */
public class ExMailArrived extends L2GameServerPacket
{
	private static final String _S__FE_2D_EXMAILARRIVED = "[S] FE:2D ExMailArrived";
	@SuppressWarnings("unused")
	private L2PcInstance _activeChar;

	public ExMailArrived(L2PcInstance character)
	{
		_activeChar = character;
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2d);
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_2D_EXMAILARRIVED;
	}
}
