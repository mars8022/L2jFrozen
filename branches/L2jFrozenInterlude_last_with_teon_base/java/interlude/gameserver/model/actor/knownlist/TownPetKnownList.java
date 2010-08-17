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

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2TownPetInstance;

public class TownPetKnownList extends AttackableKnownList
{
	// =========================================================
	// Data Field
	// =========================================================
	// Constructor
	public TownPetKnownList(L2TownPetInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return addKnownObject(object, null);
	}

	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper)) {
			return false;
		}
		if (getActiveChar().getHomeX() == 0) {
			getActiveChar().getHomeLocation();
		}
		// Set the L2TownPetInstance Intention to AI_INTENTION_ACTIVE if the
		// state was AI_INTENTION_IDLE
		if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
			getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}
		return true;
	}

	// =========================================================
	// Method - Private
	// =========================================================
	// Property - Public
	@Override
	public final L2TownPetInstance getActiveChar()
	{
		return (L2TownPetInstance) super.getActiveChar();
	}
}