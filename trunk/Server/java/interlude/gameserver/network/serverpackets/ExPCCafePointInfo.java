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
 * Format: ch ddcdc
 * @author KenM
 */
public class ExPCCafePointInfo extends L2GameServerPacket
{
    private static final String _S__FE_31_EXPCCAFEPOINTINFO = "[S] FE:31 ExPCCafePointInfo";

    private final int _points;
    private final int _mAddPoint;
    private int _mPeriodType;
    private int _remainTime;
    private int _pointType = 0;

    public ExPCCafePointInfo()
    {
        _points = 0;
        _mAddPoint = 0;
        _remainTime = 0;
        _mPeriodType = 0;
        _pointType = 0;
    }

    public ExPCCafePointInfo(final int points, final int modify_points, final boolean mod, final boolean _double, final int hours_left)
    {
        _points = points;
        _mAddPoint = modify_points;
        _remainTime = hours_left;
        if (mod && _double)
        {
            _mPeriodType = 1;
            _pointType = 0;
        }
        else if (mod)
        {
            _mPeriodType = 1;
            _pointType = 1;
        }
        else
        {
            _mPeriodType = 2;
            _pointType = 2;
        }
    }

    public ExPCCafePointInfo(final int val1, final int val2, final int val3, final int val4, final int val5, final int val6)
    {
        _points = val1;
        _mAddPoint = val2;
        _mPeriodType = val3;
        _remainTime = val4;
        _remainTime = val5;
        _pointType = val6;
    }

	@Override
	protected void writeImpl()
	{
        writeC(254);
        writeH(49);
        writeD(_points); // num points
        writeD(_mAddPoint); // points inc display
        writeC(_mPeriodType); // period(0=don't show window,1=acquisition,2=use points)
        writeD(_remainTime); // period hours left
        writeC(_pointType); // points inc display color(0=yellow,1=cyan-blue,2=red,all other black)
	}

	/**
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
        return _S__FE_31_EXPCCAFEPOINTINFO;
	}
}
