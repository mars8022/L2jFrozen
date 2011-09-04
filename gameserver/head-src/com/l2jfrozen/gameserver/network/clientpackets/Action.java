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
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class Action extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(Action.class.getName());

	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			_log.fine("Action:" + _actionId);
		if (Config.DEBUG)
			_log.fine("oid:" + _objectId);

		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if(activeChar == null)
			return;
		
		if (activeChar.inObserverMode())
		{
			getClient().sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object obj;
		
		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();
		else
			obj = L2World.getInstance().findObject(_objectId);
		
		// If object requested does not exist, add warn msg into logs
		if(obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			// _log.warning("Character: " + activeChar.getName() + " request action with non existent ObjectID:" + _objectId);
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Players can't interact with objects in the other instances
		// except from multiverse
		if (obj.getInstanceId() != activeChar.getInstanceId()
				&& activeChar.getInstanceId() != -1)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters
		if (obj instanceof L2PcInstance
				&& (((L2PcInstance)obj).getAppearance().getInvisible())
				&& !activeChar.isGM())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
	
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if(activeChar.getPrivateStoreType() == 0/* && activeChar.getActiveRequester() == null*/)
		{
			switch(_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if(obj instanceof L2Character && ((L2Character) obj).isAlikeDead())
					{
						obj.onAction(activeChar);
					}
					else
					{
						obj.onActionShift(getClient());
					}
					break;
				default:
					// Ivalid action detected (probably client cheating), log this
					_log.warning("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
		{
			// Actions prohibited when in trade
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 04 Action";
	}
}
