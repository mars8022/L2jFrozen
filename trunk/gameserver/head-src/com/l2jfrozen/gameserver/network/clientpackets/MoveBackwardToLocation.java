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

import java.nio.BufferUnderflowException;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.thread.TaskPriority;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	// cdddddd
	private int _targetX; 
 	private int _targetY; 
 	private int _targetZ;

	private int _originX;
	private int _originY;
	private int _originZ;

	private int _moveMovement;

	//For geodata
	private int _curX;
	private int _curY;
	@SuppressWarnings("unused")
	private int _curZ;

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		
		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
		}
		catch(BufferUnderflowException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			// ignore for now
			if(Config.L2WALKER_PROTEC)
			{
				L2PcInstance activeChar = getClient().getActiveChar();
				activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use l2walker!", IllegalPlayerAction.PUNISH_KICK);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;
		
		//Move flood protection
		if (!getClient().getFloodProtectors().getMoveAction().tryPerformAction("MoveBackwardToLocation"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ) 
		{ 
			activeChar.sendPacket(new StopMove(activeChar)); 
				return; 
		}

		// Correcting targetZ from floor level to head level 
		_targetZ += activeChar.getTemplate().collisionHeight;

		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();

		if(activeChar.getTeleMode() > 0)
		{
			if(activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}

			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}

		if(_moveMovement == 0 && !Config.ALLOW_USE_CURSOR_FOR_WALK)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			double dx = _targetX - _curX;
			double dy = _targetY - _curY;

			// Can't move if character is confused, or trying to move a huge distance
			if(activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000) // 9900*9900
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] 01 MoveBackwardToLoc";
	}
}
