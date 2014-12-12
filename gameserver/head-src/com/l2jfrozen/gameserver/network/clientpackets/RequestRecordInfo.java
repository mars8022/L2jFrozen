/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.DoorInfo;
import com.l2jfrozen.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetItemList;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.SpawnItem;
import com.l2jfrozen.gameserver.network.serverpackets.SpawnItemPoly;
import com.l2jfrozen.gameserver.network.serverpackets.StaticObject;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleInfo;
import com.l2jfrozen.gameserver.thread.TaskPriority;

public class RequestRecordInfo extends L2GameClientPacket
{
	/**
	 * urgent messages, execute immediately
	 * @return
	 */
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_NORMAL;
	}
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
			return;
		
		_activeChar.getKnownList().updateKnownObjects();
		_activeChar.sendPacket(new UserInfo(_activeChar));
		
		for (final L2Object object : _activeChar.getKnownList().getKnownObjects().values())
		{
			if (object == null)
			{
				continue;
			}
			
			if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
			{
				_activeChar.sendPacket(new SpawnItemPoly(object));
			}
			else
			{
				if (object instanceof L2ItemInstance)
				{
					_activeChar.sendPacket(new SpawnItem((L2ItemInstance) object));
				}
				else if (object instanceof L2DoorInstance)
				{
					_activeChar.sendPacket(new DoorInfo((L2DoorInstance) object, false));
					_activeChar.sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
				}
				else if (object instanceof L2BoatInstance)
				{
					if (!_activeChar.isInBoat() && object != _activeChar.getBoat())
					{
						_activeChar.sendPacket(new VehicleInfo((L2BoatInstance) object));
						((L2BoatInstance) object).sendVehicleDeparture(_activeChar);
					}
				}
				else if (object instanceof L2StaticObjectInstance)
				{
					_activeChar.sendPacket(new StaticObject((L2StaticObjectInstance) object));
				}
				else if (object instanceof L2NpcInstance)
				{
					_activeChar.sendPacket(new NpcInfo((L2NpcInstance) object, _activeChar));
				}
				else if (object instanceof L2Summon)
				{
					final L2Summon summon = (L2Summon) object;
					
					// Check if the L2PcInstance is the owner of the Pet
					if (_activeChar.equals(summon.getOwner()))
					{
						_activeChar.sendPacket(new PetInfo(summon));
						
						if (summon instanceof L2PetInstance)
						{
							_activeChar.sendPacket(new PetItemList((L2PetInstance) summon));
						}
					}
					else
					{
						_activeChar.sendPacket(new NpcInfo(summon, _activeChar));
					}
					
					// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
					summon.updateEffectIcons(true);
				}
				else if (object instanceof L2PcInstance)
				{
					final L2PcInstance otherPlayer = (L2PcInstance) object;
					
					if (otherPlayer.isInBoat())
					{
						otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(_activeChar);
						
						if (otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != relation)
						{
							_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
						}
						
						_activeChar.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
					}
					else
					{
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(_activeChar);
						
						if (otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(_activeChar.getObjectId()) != relation)
						{
							_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
						}
					}
				}
				
				if (object instanceof L2Character)
				{
					// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
					final L2Character obj = (L2Character) object;
					obj.getAI().describeStateToPlayer(_activeChar);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[0] CF RequestRecordInfo";
	}
}
