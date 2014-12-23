/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;

@SuppressWarnings("unused")
public final class AttackRequest extends L2GameClientPacket
{
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click - 1 for shift-click
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (System.currentTimeMillis() - activeChar.getLastAttackPacket() < 500)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		activeChar.setLastAttackPacket();
		
		// avoid using expensive operations if not needed
		final L2Object target;
		
		if (activeChar.getTargetId() == _objectId)
			target = activeChar.getTarget();
		else
			target = L2World.getInstance().findObject(_objectId);
		
		if (target == null)
			return;
		
		// Like L2OFF
		if (activeChar.isAttackingNow() && activeChar.isMoving())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't attack objects in the other instances except from multiverse
		if (target.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
			return;
		
		// Only GMs can directly attack invisible characters
		if (target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().getInvisible() && !activeChar.isGM())
			return;
		
		// During teleport phase, players cant do any attack
		if ((TvT.is_teleport() && activeChar._inEventTvT) || (CTF.is_teleport() && activeChar._inEventCTF) || (DM.is_teleport() && activeChar._inEventDM))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// No attacks to same team in Event
		if (TvT.is_started())
		{
			if (target instanceof L2PcInstance)
			{
				if ((activeChar._inEventTvT && ((L2PcInstance) target)._inEventTvT) && activeChar._teamNameTvT.equals(((L2PcInstance) target)._teamNameTvT))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof L2SummonInstance)
			{
				if ((activeChar._inEventTvT && ((L2SummonInstance) target).getOwner()._inEventTvT) && activeChar._teamNameTvT.equals(((L2SummonInstance) target).getOwner()._teamNameTvT))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// No attacks to same team in Event
		if (CTF.is_started())
		{
			if (target instanceof L2PcInstance)
			{
				if ((activeChar._inEventCTF && ((L2PcInstance) target)._inEventCTF) && activeChar._teamNameCTF.equals(((L2PcInstance) target)._teamNameCTF))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof L2SummonInstance)
			{
				if ((activeChar._inEventCTF && ((L2SummonInstance) target).getOwner()._inEventCTF) && activeChar._teamNameCTF.equals(((L2SummonInstance) target).getOwner()._teamNameCTF))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (activeChar.getTarget() != target)
			target.onAction(activeChar);
		else
		{
			if ((target.getObjectId() != activeChar.getObjectId()) && activeChar.getPrivateStoreType() == 0
			/* && activeChar.getActiveRequester() ==null */)
				target.onForcedAttack(activeChar);
			else
				sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 0A AttackRequest";
	}
}