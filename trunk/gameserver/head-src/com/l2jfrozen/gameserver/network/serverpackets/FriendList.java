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
package com.l2jfrozen.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Support for "Chat with Friends" dialog. Format: ch (hdSdh) h: Total Friend Count h: Unknown d: Player Object ID S: Friend Name d: Online/Offline h: Unknown
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(FriendList.class);
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";
	
	private final L2PcInstance _activeChar;
	
	public FriendList(final L2PcInstance character)
	{
		_activeChar = character;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;
		
		Connection con = null;
		
		try
		{
			final String sqlQuery = "SELECT friend_id, friend_name FROM character_friends WHERE " + "char_id=" + _activeChar.getObjectId() + " AND not_blocked = 1 ORDER BY friend_name ASC";
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(sqlQuery);
			ResultSet rset = statement.executeQuery(sqlQuery);
			
			// Obtain the total number of friend entries for this player.
			rset.last();
			
			writeC(0xfa);
			writeD(rset.getRow());
			
			if (rset.getRow() > 0)
			{
				rset.beforeFirst();
				
				while (rset.next())
				{
					final int friendId = rset.getInt("friend_id");
					final String friendName = rset.getString("friend_name");
					
					if (friendId == _activeChar.getObjectId())
					{
						continue;
					}
					
					final L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
					
					// writeH(0); // ??
					writeD(friendId);
					writeS(friendName);
					
					if (friend == null)
					{
						writeD(0); // offline
						writeD(0x00);
					}
					else
					{
						writeD(1); // online
						writeD(friendId);
					}
					
				}
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			
			rset = null;
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Error found in " + _activeChar.getName() + "'s FriendList: " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}