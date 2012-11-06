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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * sample 5F 01 00 00 00 format cdd
 */
public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAnswerFriendInvite.class.getName());
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
				return;
			
			if (_response == 1)
			{
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(false);
					PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name, not_blocked) VALUES (?, ?, ?, ?), (?, ?, ?,?)");
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.setString(3, player.getName());
					statement.setInt(4, 1);
					statement.setInt(5, player.getObjectId());
					statement.setInt(6, requestor.getObjectId());
					statement.setString(7, requestor.getName());
					statement.setInt(8, 1);
					statement.execute();
					statement.close();
					SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
					requestor.sendPacket(msg);
					
					// Player added to your friendlist
					msg = new SystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
					requestor.getFriendList().add(player.getName());
					
					// has joined as friend.
					msg = new SystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
					msg.addString(requestor.getName());
					player.sendPacket(msg);
					player.getFriendList().add(requestor.getName());
					
					msg = null;
					
					// friend list rework ;)
					notifyFriends(player);
					notifyFriends(requestor);
					player.sendPacket(new FriendList(player));
					requestor.sendPacket(new FriendList(requestor));
				}
				catch (Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					_log.warning("could not add friend objectid: " + e);
				}
				finally
				{
					CloseUtil.close(con);
					con = null;
				}
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
				requestor.sendPacket(msg);
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
	
	private void notifyFriends(L2PcInstance cha)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=? AND not_blocked = 1 ");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();
			L2PcInstance friend;
			String friendName;
			
			while (rset.next())
			{
				friendName = rset.getString("friend_name");
				friend = L2World.getInstance().getPlayer(friendName);
				
				if (friend != null) // friend loggined.
				{
					friend.sendPacket(new FriendList(friend));
				}
			}
			
			rset.close();
			statement.close();
		}
		
		catch (Exception e)
		{
			_log.warning("could not restore friend data:" + e);
		}
		
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 5F RequestAnswerFriendInvite";
	}
}