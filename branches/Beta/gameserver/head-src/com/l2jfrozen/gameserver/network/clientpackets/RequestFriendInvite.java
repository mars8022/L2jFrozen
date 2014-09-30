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

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.AskJoinFriend;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		SystemMessage sm;
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		
		// _name = Util.capitalizeFirst(_name); //FIXME: is it right to capitalize a nickname?
		
		if (friend == null)
		{
			// Target is not found in the game.
			sm = new SystemMessage(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (friend == activeChar)
		{
			// You cannot add yourself to your own friend list.
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getBlockList().isInBlockList(_name))
		{
			sm = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (friend.getBlockList().isInBlockList(activeChar.getName()))
		{
			sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(friend.getName());
			activeChar.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.isInCombat() || friend.isInCombat())
		{
			sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getFriendList().contains(friend.getName()))
		{
			// Player already is in your friendlist
			sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (!friend.isProcessingRequest())
		{
			// requets to become friend
			activeChar.onTransactionRequest(friend);
			sm = new SystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS);
			sm.addString(_name);
			AskJoinFriend ajf = new AskJoinFriend(activeChar.getName());
			friend.sendPacket(ajf);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
		}
		
		friend.sendPacket(sm);
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 5E RequestFriendInvite";
	}
}