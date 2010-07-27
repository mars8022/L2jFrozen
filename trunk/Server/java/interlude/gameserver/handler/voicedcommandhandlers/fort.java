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
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.Fort;
import interlude.gameserver.network.serverpackets.Ride;

/**
 *
 *
 */
public class fort implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "open doors", "close doors", "ride wyvern" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("open doors") && target.equals("fort") && activeChar.isClanLeader())
		{
			L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			Fort fort = FortManager.getInstance().getFortById(activeChar.getClan().getHasFort());
			if (door == null || fort == null) {
				return false;
			}
			if (fort.checkIfInZone(door.getX(), door.getY(), door.getZ()))
			{
				door.openMe();
			}
		}
		else if (command.startsWith("close doors") && target.equals("fort") && activeChar.isClanLeader())
		{
			L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			Fort fort = FortManager.getInstance().getFortById(activeChar.getClan().getHasFort());
			if (door == null || fort == null) {
				return false;
			}
			if (fort.checkIfInZone(door.getX(), door.getY(), door.getZ()))
			{
				door.closeMe();
			}
		}
		else if (command.startsWith("ride wyvern") && target.equals("fort"))
		{
			if (activeChar.getClan().getHasFort() > 0 && activeChar.isClanLeader())
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
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
