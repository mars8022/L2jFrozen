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

import javolution.util.FastMap;

/**
 * @author Julian
 */
public class L2FastMap<K extends Object, V extends Object> extends FastMap<K, V>
{
	static final long serialVersionUID = 1L;

	public interface I2ForEach<K, V>
	{
		public boolean forEach(K key, V obj);

		public FastMap.Entry<K, V> getNext(FastMap.Entry<K, V> priv);
	}

	public final boolean ForEach(I2ForEach<K, V> func, boolean sync)
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

	private boolean forEachP(I2ForEach<K, V> func)
	{
		for (FastMap.Entry<K, V> e = head(), end = tail(); (e = func.getNext(e)) != end;) {
			if (!func.forEach(e.getKey(), e.getValue())) {
				return false;
			}
		}
		return true;
	}
}
