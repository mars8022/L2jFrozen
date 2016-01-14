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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestFriendDel extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		try
		{
			_name = readS();
		}
		catch (Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_name = null;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_name == null)
			return;
		
		SystemMessage sm;
		Connection con = null;
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!activeChar.getFriendList().contains(_name))
		{
			sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}
		
		try
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(_name);
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			PreparedStatement statement;
			ResultSet rset;
			
			int objectId = -1;
			
			if (friend != null)
			{
				objectId = friend.getObjectId();
				/*
				 * statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=? and friend_id=?"); statement.setInt(1, activeChar.getObjectId()); statement.setInt(2, friend.getObjectId()); rset = statement.executeQuery(); if(!rset.next()) { statement.close(); // Player
				 * is not in your friendlist sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST); sm.addString(_name); activeChar.sendPacket(sm); CloseUtil.close(con); con = null; return; }
				 */
			}
			else
			{
				statement = con.prepareStatement("SELECT friend_id FROM character_friends, characters WHERE char_id=? AND friend_id=obj_id AND char_name=? AND not_blocked = 1");
				statement.setInt(1, activeChar.getObjectId());
				statement.setString(2, _name);
				rset = statement.executeQuery();
				
				if (!rset.next())
				{
					statement.close();
					rset.close();
					
					// Player is not in your friendlist
					sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
					sm.addString(_name);
					activeChar.sendPacket(sm);
					CloseUtil.close(con);
					con = null;
					return;
					
				}
				objectId = rset.getInt("friend_id");
			}
			
			statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, objectId);
			statement.setInt(3, objectId);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			// Player deleted from your friendlist
			sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			
			statement.close();
			
			activeChar.getFriendList().remove(_name);
			activeChar.sendPacket(new FriendList(activeChar));
			
			if (friend != null)
			{
				friend.getFriendList().remove(activeChar.getName());
				friend.sendPacket(new FriendList(friend));
			}
		}
		catch (Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "could not del friend objectid: ", e);
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
		return "[C] 61 RequestFriendDel";
	}
}