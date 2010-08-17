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
package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.Castle;
import interlude.gameserver.model.entity.Fort;
import interlude.gameserver.network.serverpackets.Ride;

/**
 *
 *
 */
public class castle implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "opendoor", "closedoor", "ridewyvern" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("opendoor") && activeChar.getClan() != null)
		{
			// Can the player opene/close the doors ?
			if ((activeChar.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) != L2Clan.CP_CS_OPEN_DOOR) {
				return false;
			}
			// Is a door the target ?
			L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			if (door == null) {
				return false;
			}
			Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if (castle != null)
			{
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				{
					door.openMe();
					return true;
				}
			}
			Fort fort = FortManager.getInstance().getFortById(activeChar.getClan().getHasFort());
			if (fort != null && door.getFort() != null)
			{
				if (fort.getFortId() == door.getFort().getFortId())
				{
					door.openMe();
					return true;
				}
			}
			return false;
		}
		else if (command.startsWith("closedoor") && activeChar.getClan() != null)
		{
			// Can the player open/close the doors ?
			if ((activeChar.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) != L2Clan.CP_CS_OPEN_DOOR) {
				return false;
			}
			L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			if (door == null) {
				return false;
			}
			Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if (castle != null)
			{
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				{
					door.closeMe();
				}
			}
			Fort fort = FortManager.getInstance().getFortById(activeChar.getClan().getHasFort());
			if (fort != null && door.getFort() != null)
			{
				if (fort.getFortId() == door.getFort().getFortId())
				{
					door.closeMe();
					return true;
				}
			}
			return false;
		}
		else if (command.startsWith("ridewyvern") && activeChar.getClan() != null)
		{
			Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if (castle != null && activeChar.isClanLeader())
			{
				if (castle.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()))
				{
					if (!activeChar.disarmWeapons()) {
						return false;
					}
					Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, 12621);
					activeChar.sendPacket(mount);
					activeChar.broadcastPacket(mount);
					activeChar.setMountType(mount.getMountType());
				}
			}
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
