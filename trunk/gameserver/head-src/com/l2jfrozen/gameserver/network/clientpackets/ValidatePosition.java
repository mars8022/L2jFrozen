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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2jfrozen.gameserver.thread.TaskPriority;

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
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null
				|| activeChar.isTeleporting()
				|| activeChar.inObserverMode())
			return;

		final int realX = activeChar.getX();
		final int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if (Config.DEVELOPER)
		{
			_log.fine("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
			_log.fine("server pos: "+ realX + " "+realY+ " "+realZ +" head "+activeChar.getHeading());
		}

		if (_x == 0 && _y == 0) 
		{
			if (realX != 0) // in this case this seems like a client error
				return;
		}

		int dx, dy, dz;
		double diffSq;

		if(activeChar.isInBoat()){
			
			dx = _x - realX;
			dy = _y - realY;
			diffSq = dx * dx + dy * dy;
			
			if((Config.COORD_SYNCHRONIZE & 2) == 2 && diffSq > 10000){
				sendPacket(new ValidateLocationInVehicle(activeChar));
			}else if (Config.COORD_SYNCHRONIZE == 4){
				dz = _z - realZ;
				activeChar.setXYZ(activeChar.getBoat().getX(), activeChar.getBoat().getY(), activeChar.getBoat().getZ());
			}else if(Config.COORD_SYNCHRONIZE == -1){
				
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

				sendPacket(new ValidateLocationInVehicle(activeChar));	
			}
			
			return;
			
		}
		
		/*
		if (activeChar.isInBoat())
		{
			if (Config.COORD_SYNCHRONIZE == 2)
			{
				dx = _x - activeChar.getInVehiclePosition().getX();
				dy = _y - activeChar.getInVehiclePosition().getY();
				dz = _z - activeChar.getInVehiclePosition().getZ();
				diffSq = (dx*dx + dy*dy);
				if (diffSq > 250000)
					sendPacket(new GetOnVehicle(activeChar.getObjectId(), _data, activeChar.getInVehiclePosition()));
			}
			return;
		}
		*/
		
		if (activeChar.isFalling(_z))
			return; // disable validations during fall to avoid "jumping"

		dx = _x - realX;
		dy = _y - realY;
		dz = _z - realZ;
		diffSq = (dx*dx + dy*dy);

		L2Party party = activeChar.getParty();
		if(party != null && activeChar.getLastPartyPositionDistance(_x, _y, _z) > 150)
		{
			activeChar.setLastPartyPosition(_x, _y, _z);
			party.broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
		}
		
		/*
		if (Config.ACCEPT_GEOEDITOR_CONN)
			if (GeoEditorListener.getInstance().getThread() != null  
					&& GeoEditorListener.getInstance().getThread().isWorking()  
					&& GeoEditorListener.getInstance().getThread().isSend(activeChar))
				GeoEditorListener.getInstance().getThread().sendGmPosition(_x,_y,(short)_z);
		 */
		
		if (activeChar.isFlying() || activeChar.isInsideZone(L2Character.ZONE_WATER))
		{
			activeChar.setXYZ(realX, realY, _z);
			if (diffSq > 90000) // validate packet, may also cause z bounce if close to land
				activeChar.sendPacket(new ValidateLocation(activeChar));
			
			if(Config.ALLOW_WATER)
			{
				activeChar.checkWaterState();
			}
			
		}
		else if (diffSq < 360000) // if too large, messes observation
		{
			if (Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synched to server, 
												// mainly used when no geodata but can be used also with geodata
			{
				activeChar.setXYZ(realX,realY,_z);
				return;
			}
			if (Config.COORD_SYNCHRONIZE == 1) // Trusting also client x,y coordinates (should not be used with geodata)
			{
				if (!activeChar.isMoving() 
						|| !activeChar.validateMovementHeading(_heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
						activeChar.setXYZ(realX, realY, _z);
					else
						activeChar.setXYZ(_x, _y, _z);
				}
				else
					activeChar.setXYZ(realX, realY, _z);

				activeChar.setHeading(_heading);
				return;
			}
			// Sync 2 (or other), 
			// intended for geodata. Sends a validation packet to client 
			// when too far from server calculated true coordinate.
			// Due to geodata/zone errors, some Z axis checks are made. (maybe a temporary solution)
			// Important: this code part must work together with L2Character.updatePosition
			if (Config.GEODATA > 0 && (diffSq > 250000 || Math.abs(dz) > 200))
			{
				//if ((_z - activeChar.getClientZ()) < 200 && Math.abs(activeChar.getLastServerPosition().getZ()-realZ) > 70)

				if (Math.abs(dz) > 200
						&& Math.abs(dz) < 1500
						&& Math.abs(_z - activeChar.getClientZ()) < 800 )
				{
					activeChar.setXYZ(realX, realY, _z);
					realZ = _z;
				}
				else
				{
					if (Config.DEVELOPER)
						_log.info(activeChar.getName() + ": Synchronizing position Server --> Client");

					activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
		}

		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading); // No real need to validate heading.
		activeChar.setLastServerPosition(realX, realY, realZ);
	}
	
	/*
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
								activeChar.isFalling((int)dz);
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
	*/
	
	/*
	private void clientToServer(L2PcInstance player)
	{
		_x = player.getX();
		_y = player.getY();
		_z = player.getZ();
	}
	*/

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