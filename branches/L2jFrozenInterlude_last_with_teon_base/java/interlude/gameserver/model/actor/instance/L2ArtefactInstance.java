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
package interlude.gameserver.model.actor.instance;

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all Castle Siege Artefacts.
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2ArtefactInstance extends L2NpcInstance
{
	/**
	 * Constructor of L2ArtefactInstance (use L2Character and L2NpcInstance constructor).
	 * Actions :
	 * Call the L2Character constructor to set the _template of the L2ArtefactInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) Set the name of the L2ArtefactInstance Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2ArtefactInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	/**
	 * Return False.
	 */
    @Override
    public boolean isAutoAttackable(L2Character attacker)
	{
        return false;
    }

    @Override
	public boolean isAttackable()
    {
        return false;
    }

	/**
	 * Manage actions when a player click on the L2ArtefactInstance.
	 *  Actions :
	 * Set the L2NpcInstance as target of the L2PcInstance player (if necessary) Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window) Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
	 *  Example of use :
	 * Client packet : Action, AttackRequest
	 * @param player The L2PcInstance that start an action on the L2ArtefactInstance
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (_isFOS_Artifact && player._inEventFOS)
		{
			super.onAction(player);
			return;
		}

		if (!canTarget(player)) return;

		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker)
	{
	}
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
	}
}