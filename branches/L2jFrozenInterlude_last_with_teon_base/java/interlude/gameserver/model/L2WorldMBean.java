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
package interlude.gameserver.model;

/**
 * interface for JMX Administration Use to retrieve information about the L2World
 */
public interface L2WorldMBean
{
	/**
	 * Get the count of all visible objects in world.<br>
	 * <br>
	 *
	 * @return count off all L2World objects
	 */
	public int getAllVisibleObjectsCount();

	/**
	 * Return how many players are online.<BR>
	 * <BR>
	 *
	 * @return number of online players.
	 */
	public int getAllPlayersCount();
}
