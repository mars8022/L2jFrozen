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
package interlude.gameserver.model.actor.knownlist;

import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.actor.instance.L2CabaleBufferInstance;
import interlude.gameserver.model.actor.instance.L2FestivalGuideInstance;
import interlude.gameserver.model.actor.instance.L2FolkInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;

public class NpcKnownList extends CharKnownList
{
	// =========================================================
	// Data Field
	// =========================================================
	// Constructor
	public NpcKnownList(L2NpcInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	// =========================================================
	// Method - Private
	// =========================================================
	// Property - Public
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance) {
			return 4000;
		}
		if (object instanceof L2FolkInstance || !(object instanceof L2Character)) {
			return 0;
		}
		if (object instanceof L2CabaleBufferInstance) {
			return 900;
		}
		if (object instanceof L2PlayableInstance) {
			return 1500;
		}
		return 500;
	}
}
