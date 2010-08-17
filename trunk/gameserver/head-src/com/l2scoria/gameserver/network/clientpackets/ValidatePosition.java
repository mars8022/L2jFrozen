/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2scoria.gameserver.network.clientpackets;

import java.util.logging.Logger;

import javolution.lang.MathLib;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.geo.GeoData;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2scoria.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2scoria.gameserver.thread.TaskPriority;

public class ValidatePosition extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

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

		if(activeChar == null || activeChar.isTeleporting())
		{
			return;
		}
		
		if(_x == 0 && _y == 0 && activeChar.getX() != 0)
		{
			return;
		}
		
		if(activeChar.getX() == 0 && activeChar.getY() == 0 && activeChar.getZ() == 0)
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			clientToServer(activeChar);
		}
		
		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		double dx = _x - realX;
		double dy = _y - realY;
		double diffSq = dx * dx + dy * dy;

		if(Config.COORD_SYNCHRONIZE > 0)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading);

			if(diffSq > 0 && diffSq < 250000)
			{
				if((Config.COORD_SYNCHRONIZE & 1) == 1 && (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading)))
				{
					if(Config.DEVELOPER)
					{
						System.out.println(activeChar.getName() + ": Synchronizing position Client --> Server" + (activeChar.isMoving() ? " (collision)" : " (stay sync)"));
					}
					if(diffSq < 2500)
					{
						activeChar.setXYZ(realX, realY, _z);
					}
					else
					{
						activeChar.setXYZ(_x, _y, _z);
					}
					activeChar.setHeading(_heading);
				}
				else if((Config.COORD_SYNCHRONIZE & 2) == 2 && diffSq > 10000) // More than can be considered to be result of latency
				{
					if(Config.DEVELOPER)
					{
						System.out.println(activeChar.getName() + ": Synchronizing position Server --> Client");
					}

					if(activeChar.isInBoat())
					{
						sendPacket(new ValidateLocationInVehicle(activeChar));
					}
					else
					{
						activeChar.sendPacket(new ValidateLocation(activeChar));
					}
				}
				else if(Config.COORD_SYNCHRONIZE == 4)
				{
					double dz = _z - realZ;
					
					if(activeChar.isInBoat())
					{
						activeChar.setXYZ(activeChar.getBoat().getX(), activeChar.getBoat().getY(), activeChar.getBoat().getZ());
					}
					else if(activeChar.isFlying() || activeChar.isInWater())
					{
						if(MathLib.abs(dz) < 500)
						{
							realZ = _z;
						}
						else
						{
							_z = realZ;
						}
						
						activeChar.setXYZ(realX, realY, realZ);
						
						if(diffSq > 1000000)
						{
							clientToServer(activeChar);
						}
					}
					else if(_z < Config.WORLD_SIZE_MIN_Z || _z > Config.WORLD_SIZE_MAX_Z)
					{
						clientToServer(activeChar);
					}
					else if(diffSq > 1000000)
					{
						clientToServer(activeChar);
					}
					else if(diffSq < 250000)
					{
						if(dz < 0 && MathLib.abs(dz) > 500)
							clientToServer(activeChar);
						if(dz > 333)
						{
							final int geoZ = GeoData.getInstance().getHeight(realX, realY, realZ);
							if((_z - geoZ) > 0)
							{
								activeChar.setXYZ(realX, realY, _z);
							}
							if(Config.FALL_DAMAGE)
							{
								activeChar.Falling((int)dz);
							}
						}
						else if(activeChar.isMoving() && diffSq > activeChar.getStat().getMoveSpeed())
						{
							activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
						}
					}
					activeChar.broadcastPacket(new ValidateLocation(activeChar));
				}
			}
			activeChar.setLastClientPosition(_x, _y, _z);
			activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		}
		else if(Config.COORD_SYNCHRONIZE == -1)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading);

			if(diffSq < 250000)
			{
				activeChar.setXYZ(realX, realY, _z);
			}

			int realHeading = activeChar.getHeading();

			if(Config.DEBUG)
			{
				_log.fine("client pos: " + _x + " " + _y + " " + _z + " head " + _heading);
				_log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + realHeading);
			}

			if(Config.DEVELOPER)
				if(diffSq > 1000000)
				{
					if(Config.DEBUG)
					{
						_log.fine("client/server dist diff " + (int) Math.sqrt(diffSq));
					}
					if(activeChar.isInBoat())
					{
						sendPacket(new ValidateLocationInVehicle(activeChar));
					}
					else
					{
						activeChar.sendPacket(new ValidateLocation(activeChar));
					}
				}
		}

		if(activeChar.getParty() != null)
		{
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
		}

		if(Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}
	}
	
	private void clientToServer(L2PcInstance player)
	{
		_x = player.getX();
		_y = player.getY();
		_z = player.getZ();
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