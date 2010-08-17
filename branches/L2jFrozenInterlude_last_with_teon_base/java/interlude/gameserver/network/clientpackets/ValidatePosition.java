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
package interlude.gameserver.network.clientpackets;

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.TaskPriority;
import interlude.gameserver.Universe;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.geoeditorcon.GeoEditorListener;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.PartyMemberPosition;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.network.serverpackets.ValidateLocationInVehicle;

/**
 * This class ...
 *
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends L2GameClientPacket
{
    private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

	/** urgent messages, execute immediatly */
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	@SuppressWarnings("unused")
	private int _data;

	// private double diff;
	/**
	 * packet type id 0x48 format: cddddd
	 *
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		_data = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isTeleporting())
			return;

		if (Config.COORD_SYNCHRONIZE > 0)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading); // No real need to validate heading.
			int realX = activeChar.getX();
			int realY = activeChar.getY();
			// int realZ = activeChar.getZ();
			double dx = _x - realX;
			double dy = _y - realY;
			double diffSq = dx * dx + dy * dy;

/*			if (Config.DEVELOPER && false)
			{
				int dxs = (_x - activeChar._lastClientPosition.x);
				int dys = (_y - activeChar._lastClientPosition.y);
				int dist = (int)Math.sqrt(dxsdxs + dysdys);
				int heading = dist > 0 ? (int)(Math.atan2(-dys/dist, -dxs/dist) 10430.378350470452724949566316381) + 32768 : 0; System.out.println("Client X:" + _x + ", Y:" + _y + ", Z:" + _z + ", H:" + _heading + ", Dist:" + activeChar.getLastClientDistance(_x, _y, _z));
				System.out.println("Server X:" + realX + ", Y:" + realY + ", Z:" + realZ + ", H:" + activeChar.getHeading() + ", Dist:" + activeChar.getLastServerDistance(realX, realY, realZ));
			}*/
		
			if (diffSq > 0 && diffSq < 250000) // if too large, messes observation
			{
				if ((Config.COORD_SYNCHRONIZE & 1) == 1 
						&& (!activeChar.isMoving() // character is not moving, take coordinates from client
						|| !activeChar.validateMovementHeading(_heading))) // Heading changed on client = possible obstacle
				{
					if (diffSq < 2500)
						activeChar.setXYZ(realX, realY, _z);
					else
						activeChar.setXYZ(_x, _y, _z);

					activeChar.setHeading(_heading);
				}
				else if ((Config.COORD_SYNCHRONIZE & 2) == 2 && diffSq > 10000) // more than can be considered to be result of latency
				{
					if (activeChar.isInBoat())
						sendPacket(new ValidateLocationInVehicle(activeChar));
					else
						activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
			activeChar.setLastClientPosition(_x, _y, _z);
			activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		}
		else if (Config.COORD_SYNCHRONIZE == -1)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading);
			int realX = activeChar.getX();
			int realY = activeChar.getY();
			int realZ = activeChar.getZ();
			double dx = _x - realX;
			double dy = _y - realY;
			double diffSq = dx * dx + dy * dy;

	        if (Config.DEVELOPER)
	        {
	            _log.fine("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
	            _log.fine("server pos: "+realX+" "+realY+" "+realZ+" head "+activeChar.getHeading());
	        }
			if (diffSq < 250000)
				activeChar.setXYZ(realX, realY, _z);

			if (Config.ACTIVATE_POSITION_RECORDER && !activeChar.isFlying() 
					&& Universe.getInstance().shouldLog(activeChar.getObjectId()))
				Universe.getInstance().registerHeight(realX, realY, _z);

			if (Config.DEVELOPER)
			{
				if (diffSq > 1000000)
				{
					if (activeChar.isInBoat())
						sendPacket(new ValidateLocationInVehicle(activeChar));
					else
						activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
		}
		if (activeChar.getParty() != null)
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));

		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			if (GeoEditorListener.getInstance().getThread() != null 
					&& GeoEditorListener.getInstance().getThread().isWorking() 
					&& GeoEditorListener.getInstance().getThread().isSend(activeChar))
				GeoEditorListener.getInstance().getThread().sendGmPosition(_x, _y, (short) _z);
		}
		if (activeChar.getPet() != null)
			activeChar.getPet().setInRange(true);

		if (!Config.FLYING_WYVERN_DURING_SIEGE && activeChar.getMountType() == 2)
		{
			if (activeChar.isInsideZone(L2Character.ZONE_SIEGE) 
					&& !(activeChar.getClan() != null 
					&& CastleManager.getInstance().getCastle(activeChar) == CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) 
					&& activeChar == activeChar.getClan().getLeader().getPlayerInstance()))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("You entered into a no-fly zone.");
				activeChar.sendPacket(sm);
				activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}

	@Override
	public String getType()
	{
		return _C__48_VALIDATEPOSITION;
	}

	@Deprecated
	public boolean equal(ValidatePosition pos)
	{
		return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
	}
}
