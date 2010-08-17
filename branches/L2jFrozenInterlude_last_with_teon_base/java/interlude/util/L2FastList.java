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
package interlude.util;

import javolution.util.FastList;

/**
 * @author Julian
 */
public class L2FastList<T extends Object> extends FastList<T>
{
	static final long serialVersionUID = 1L;

	public interface I2ForEach<T>
	{
		public boolean ForEach(T obj);

		public FastList.Node<T> getNext(FastList.Node<T> priv);
	}

	public final boolean forEach(I2ForEach<T> func, boolean sync)
	{
		if (sync) {
			synchronized (this)
			{
				return forEachP(func);
			}
		} else {
			return forEachP(func);
		}
	}

	private boolean forEachP(I2ForEach<T> func)
	{
		for (FastList.Node<T> e = head(), end = tail(); (e = func.getNext(e)) != end;) {
			if (!func.ForEach(e.getValue())) {
				return false;
			}
		}
		return true;
	}
}
