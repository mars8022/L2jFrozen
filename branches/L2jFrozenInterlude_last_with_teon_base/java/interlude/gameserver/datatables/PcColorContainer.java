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
/**
 * @author DaRkRaGe
 */
package interlude.gameserver.datatables;

public class PcColorContainer
{
	private int _color;
	private long _regTime;
	private long _time;

	public PcColorContainer(int color, long regTime, long time)
	{
		_color = color;
		_regTime = regTime;
		_time = time;
	}

	/**
	 * Returns the color
	 *
	 * @return int
	 */
	public int getColor()
	{
		return _color;
	}

	/**
	 * Returns the time when the color was registered
	 *
	 * @return long
	 */
	public long getRegTime()
	{
		return _regTime;
	}

	/**
	 * Returns the time when the color should be deleted
	 *
	 * @return long
	 */
	public long getTime()
	{
		return _time;
	}
}