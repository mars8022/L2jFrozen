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

/**
 * Format: (ch)ddddd
 */
public class ExConfirmVariationRefiner extends L2GameServerPacket
{
	private static final String _S__FE_53_EXCONFIRMVARIATIONREFINER = "[S] FE:53 ExConfirmVariationRefiner";
	private int _refinerItemObjId;
	private int _lifestoneItemId;
	private int _gemstoneItemId;
	private int _gemstoneCount;
	private int _unk2;

	public ExConfirmVariationRefiner(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, int gemstoneCount)
	{
		_refinerItemObjId = refinerItemObjId;
		_lifestoneItemId = lifeStoneId;
		_gemstoneItemId = gemstoneItemId;
		_gemstoneCount = gemstoneCount;
		_unk2 = 1;
	}

	/**
	 * @see interlude.gameserver.network.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x53);
		writeD(_refinerItemObjId);
		writeD(_lifestoneItemId);
		writeD(_gemstoneItemId);
		writeD(_gemstoneCount);
		writeD(_unk2);
	}

	/**
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_53_EXCONFIRMVARIATIONREFINER;
	}
}
