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
		//Connection con = null;
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		//_name = Util.capitalizeFirst(_name); //FIXME: is it right to capitalize a nickname?

		if(friend == null)
		{
			//Target is not found in the game.
			sm = new SystemMessage(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if(friend == activeChar)
		{
			//You cannot add yourself to your own friend list.
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if(activeChar.isInCombat() || friend.isInCombat())
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

		if(!friend.isProcessingRequest())
		{
			//requets to become friend
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
		
		/*
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT char_id FROM character_friends WHERE char_id=? AND friend_id=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, friend.getObjectId());
			ResultSet rset = statement.executeQuery();

			if(rset.next())
			{
				//Player already is in your friendlist
				sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
				sm.addString(_name);
			}
			else
			{
				if(!friend.isProcessingRequest())
				{
					//requets to become friend
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
			}

			friend.sendPacket(sm);
			sm = null;
			rset.close();
			statement.close();

			rset = null;
			statement = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "could not add friend objectid: ", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		*/
	}

	@Override
	public String getType()
	{
		return "[C] 5E RequestFriendInvite";
	}
}
