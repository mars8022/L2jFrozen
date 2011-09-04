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
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocationInVehicle;

public final class ValidatePosition extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());

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
			return;
		
		if(_x == 0 && _y == 0 && activeChar.getX() != 0)
			return;
		
		if(activeChar.getX() == 0 && activeChar.getY() == 0) {
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			clientToServer(activeChar);
		}
		
		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		
		double dx = _x - realX;
		double dy = _y - realY;
		double dz = _z - realZ;
		double diffSq = dx * dx + dy * dy;
		
		int finalZ = _z;
		if (Math.abs(dz) <= 200){
			finalZ = realZ;
		}
		
		final int geoZ = GeoData.getInstance().getHeight(realX, realY, finalZ);
		
		
		/*
		if(activeChar.isMoving() && diffSq > activeChar.getStat().getMoveSpeed())
		{
			activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
		
		}
		else if(Config.COORD_SYNCHRONIZE > 0)
		{
			if(diffSq > 0 && diffSq < 250000) // if too large, messes observation
			{
				
				if((Config.COORD_SYNCHRONIZE & 1) == 1 
						&& (!activeChar.isMoving() // character is not moving, take coordinates from client
				|| !activeChar.validateMovementHeading(_heading))) // Heading changed on client = possible obstacle
				{
					if(Config.DEVELOPER)
						_log.info(activeChar.getName() + ": Synchronizing position Client --> Server" + (activeChar.isMoving() ? " (collision)" : " (stay sync)"));
					
					if(diffSq < 2500){
						
						if(MathLib.abs(dz) < 500){
							activeChar.setXYZ(realX, realY, realZ);
						}else{
							
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
						
					}else{
						
						if(MathLib.abs(dz) < 500){
							activeChar.setXYZ(realX, realY, realZ);
						}else{
							
							final int geoZ = GeoData.getInstance().getHeight(realX, realY, realZ);
							if((realZ - geoZ) > 0)
							{
								activeChar.setXYZ(_x, _y, realZ);
							}
							if(Config.FALL_DAMAGE)
							{
								activeChar.isFalling((int)dz);
							}
							
						}
						
					}
					
					activeChar.setHeading(_heading);
					
				} 
				else if((Config.COORD_SYNCHRONIZE & 2) == 2 
						&& diffSq > 10000) // more than can be considered to be result of latency
				{
					if(Config.DEVELOPER)
						_log.info(activeChar.getName() + ": Synchronizing position Server --> Client");

					if(activeChar.isInBoat())
						sendPacket(new ValidateLocationInVehicle(activeChar));
					else
						sendPacket(new ValidateLocation(activeChar));
				
				}
				else if(Config.COORD_SYNCHRONIZE == 4) //synchronization on Z
				{
					
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
					else if(_z < Config.WORLD_SIZE_MIN_Z || _z > Config.WORLD_SIZE_MAX_Z || diffSq > 1000000)
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
			if(diffSq < 250000){
				
				if(MathLib.abs(dz) < 500){
					activeChar.setXYZ(realX, realY, realZ);
				}else{
					
					
					int geoZ  = GeoData.getInstance().getHeight(realX, realY, realZ);
					
					if(MathLib.abs(_z - geoZ) > 0) //client Z is higher then GeoZ --> falling?
					{
						activeChar.setXYZ(realX, realY, _z);
					}
					
					
					
				}
				//activeChar.setXYZ(realX, realY, _z);
			}
			
			if(Config.DEBUG)
			{
				int realHeading = activeChar.getHeading();
				_log.fine("client pos: " + _x + " " + _y + " " + _z + " head " + _heading);
				_log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + realHeading);
			}
		}
		*/
		
		
		if(Config.DEBUG){
			
			int realHeading = activeChar.getHeading();
			_log.info("client pos: " + _x + " " + _y + " " + _z + " head " + _heading);
			_log.info("server pos: " + realX + " " + realY + " " + realZ + " head " + realHeading);
			_log.info("finalZ"+ finalZ + " geoZ: " + geoZ+" destZ: "+activeChar.getZdestination());
			
		}
		
		//COORD Client<-->Server synchronization
		switch(Config.COORD_SYNCHRONIZE){
			
			case 1:{ //full synchronization Client --> Server 
					 //only * using this option it is difficult 
					 //for players to bypass obstacles
			
				if (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500){ // 50*50 - attack won't work fluently if even small differences are corrected
						activeChar.getPosition().setXYZ(realX, realY, finalZ);
						
					}else{
						activeChar.getPosition().setXYZ(_x, _y, finalZ);
					}
				}
				else{
					activeChar.getPosition().setXYZ(realX, realY, finalZ);
					
				}
				
				activeChar.setHeading(_heading);
				
			}
			break;
			case 2:{ //full synchronization Server --> Client (bounces for validation)
				
				if (Config.GEODATA > 0 && (diffSq > 250000 || Math.abs(dz) > 200))
				{
					if (Math.abs(dz) > 200){
						
						if(Math.abs(finalZ - activeChar.getClientZ()) < 800){
							activeChar.getPosition().setXYZ(realX, realY, finalZ);
						}
						
					}
					else
					{
						if(!activeChar.isMoving()){
							
							if(activeChar.isInBoat())
								sendPacket(new ValidateLocationInVehicle(activeChar));
							else
								sendPacket(new ValidateLocation(activeChar));
							
							
						}else if(diffSq > activeChar.getStat().getMoveSpeed())
							activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
						
						
						finalZ = activeChar.getPosition().getZ();
					}
					
				}
				
			}
			break;
			case -1:{  // just (client-->server) Z coordination
			
				if (Math.abs(dz) > 200){
					
					if(Math.abs(_z - activeChar.getClientZ()) < 800)
						activeChar.getPosition().setXYZ(realX, realY, finalZ);
					
				}else
					finalZ = realZ;
				
			}
			break;
			default:
			case 0:{ //no synchronization at all
				//the server has the correct information
				finalZ = realZ;
			}
			break;
			
		}
		
		//check water
		if(Config.ALLOW_WATER)
			activeChar.checkWaterState();

		//check falling if previous client Z is less then
		if(Config.FALL_DAMAGE)
		{
			activeChar.isFalling(finalZ);
		}
		
		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading);

	}
	
	private void clientToServer(L2PcInstance player) {
		_x = player.getX();
		_y = player.getY();
		_z = player.getZ();
	}

	public boolean equal(ValidatePosition pos)
	{
		return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
	}
	
	@Override
	public String getType()
	{
		return "[C] 48 ValidatePosition";
	}
}
