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
package interlude.gameserver.services;

/**
 * Service to Manage Threads Sleep Timer Here we Work with seconds conversor auto converts to Milis.
 */
public class ThreadService
{
	// private final static Log _log =
	// LogFactory.getLog(ThreadMilisService.class.getName());
	// converts from seconds to milis
	static int Conversor = 60000;
	// default delay timer to sleep threads.
	public static final int DEFAULT_THREAD_DELAY_TIMER = (int) (0.017 * Conversor - 20);// 1000
	// ms
	// the sleep between npc buffer casts
	public final static int BUFFER_CAST_DELAY_TIMER = (int) (0.025 * Conversor); // 1500

	// ms
	/**
	 * x1000
	 *
	 * @param multipler
	 */
	public static void processSleep(int multipler)
	{
		try
		{
			Thread.sleep(DEFAULT_THREAD_DELAY_TIMER * multipler);
		}
		catch (InterruptedException ex)
		{
		}
	}
}
