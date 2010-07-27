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
package interlude.gameserver.model.entity.RaidEngine;

import java.util.Vector;

import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.actor.instance.L2PcInstance;

public class L2EventChecks
{
	/**
	 * CheckIfOtherEvent --> Checks if the player is already inscribed in another event.
	 */
	private static boolean checkIfOtherEvent(L2PcInstance player)
	{
		if (player.inSoloEvent || player.inPartyEvent || player.inClanEvent)
		{
			player.sendMessage("You're alredy registered in another event.");
			return true;
		}
		return false;
	}

	/**
	 * Check if Player/Clan/Party is eligible for Event.<br>
	 * Documentation can be found in the method.<br>
	 *
	 * @param player
	 *            --> Basic Player Taking the action.
	 * @param eventType
	 *            --> Type of Event to check.
	 * @param points
	 *            --> Minimum Event Points Required to participate.
	 * @param minPeople
	 *            --> Minimum allowed People Required to participate.
	 * @return --> true for Eligible Players and false for UnEligible Players.
	 */
	public static boolean checkPlayer(L2PcInstance player, int eventType, int points, int minPeople, Vector<L2PcInstance> _eventPlayers)
	{
		int eventPoints = 0;
		// Let's avoid NPEs
		if (player == null) {
			return false;
		}
		// If there's not enough clan members online to fill the MinPeople
		// requirement
		// return false.
		if (_eventPlayers.size() <= minPeople && eventType == (2 | 3))
		{
			// Notify to the requester.
			player.sendMessage("Not enough " + eType(eventType) + " members of the connected at this mommtent, try again later.");
			return false;
		}
		for (L2PcInstance member : _eventPlayers)
		{
			/*
			 * In case of finding a disconnected player, we will continue the for statement.
			 */
			if (member == null) {
				continue;
			}
			// Let's check if any of the members is in another Event.
			if (checkIfOtherEvent(member))
			{
				/*
				 * If this is the case, we will notify the request instance about the inconvenience produced. We will also return a false.
				 */
				String badRequestor = member.getName();
				notifyBadRequestor(player, badRequestor, 2, _eventPlayers);
				return false;
			}
			// TODO: Add a Check asking members of the clan/party (ONLY)
			// ACTUALLY WANT TO PARTICIPATE or not.
			/*
			 * Let's count all the points for every one of the event members, only in the case that the request instance and the Clan Members are from the same clan
			 */
			switch (eventType)
			{
				case 2:
				{
					if (_eventPlayers.contains(player) && member.getClan().getName().equals(player.getClan().getName())) {
						eventPoints += member.getEventPoints();
					}
					break;
				}
				case 3:
				{
					// Let's add the points of each member to the Party General
					// Clan Score.
					eventPoints += member.getEventPoints();
					break;
				}
				default:
				{
					eventPoints = member.getEventPoints();
					break;
				}
			}
		}
		/*
		 * If the addition of all the points is bigger than the requested points, we will accept the Participation of this clan in the event
		 */
		if (eventPoints >= points)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				// Deletion of all the Buffs from all the Clan members
				for (L2Effect effect : member.getAllEffects())
				{
					if (effect != null) {
						effect.exit();
					}
				}
			}
			return true;
		}
		// Else The Clan doesn't have enough event points to participate.
		else if (eventType != 1)
		{
			player.sendMessage("The totality of your " + eType(eventType) + " members don't have enough Event Points to participate.");
			return false;
		}
		else
		{
			player.sendMessage("Not enough Event Points to participate into the Event.");
			return false;
		}
	}

	/**
	 * notifyOfBadRequestor --> Tell the members of the Clan/Party that the player is already inscribed in another event.
	 */
	private static void notifyBadRequestor(L2PcInstance player, String badRequestor, int type, Vector<L2PcInstance> _eventPlayers)
	{
		if (type == 2)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				member.sendMessage("You can't access the event while " + badRequestor + "is singed up for another event.");
			}
		}
		if (type == 3)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				member.sendMessage("You can't access the event while " + badRequestor + "is singed up for another event.");
			}
		}
	}

	public static boolean usualChecks(L2PcInstance player, int minLevel)
	{
		if (player.getLevel() < minLevel)
		{
			player.sendMessage("The minimum level to participate in this Event is " + minLevel + ". You cannot participate.");
			return false;
		}
		if (player.inClanEvent || player.inPartyEvent || player.inSoloEvent)
		{
			player.sendMessage("You're alredy registered in another Event.");
			return false;
		}
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("You can Not register while Having a Cursed Weapon.");
			return false;
		}
		if (player.isInStoreMode())
		{
			player.sendMessage("Cannot Participate while in Store Mode.");
			return false;
		}
		if (player.isInJail())
		{
			player.sendMessage("Cannot Participate while in Jail.");
			return false;
		}
		return true;
	}

	public static String eType(int type)
	{
		String sType;
		if (type == 1) {
			sType = "Single";
		} else if (type == 2) {
			sType = "Clan";
		} else if (type == 3) {
			sType = "Party";
		} else {
			sType = "error ocurred while getting type of Event.";
		}
		return sType;
	}
}