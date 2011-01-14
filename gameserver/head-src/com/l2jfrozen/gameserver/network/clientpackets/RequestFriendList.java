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
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendList extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestFriendList.class.getName());
	private static final String _C__60_REQUESTFRIENDLIST = "[C] 60 RequestFriendList";

	@Override
	protected void readImpl()
	{
	// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		SystemMessage sm;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT friend_id, friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, activeChar.getObjectId());

			ResultSet rset = statement.executeQuery();

			//======<Friend List>======
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_HEAD));

			L2PcInstance friend = null;
			while(rset.next())
			{
				// int friendId = rset.getInt("friend_id");
				String friendName = rset.getString("friend_name");
				friend = L2World.getInstance().getPlayer(friendName);

				if(friend == null)
				{
					//	(Currently: Offline)
					sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
					sm.addString(friendName);
				}
				else
				{
					//(Currently: Online)
					sm = new SystemMessage(SystemMessageId.S1_ONLINE);
					sm.addString(friendName);
				}

				activeChar.sendPacket(sm);
			}

			//=========================
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
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
			
			_log.warning("Error in /friendlist for " + activeChar + ": " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { 
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			con = null;
		}
	}

	@Override
	public String getType()
	{
		return _C__60_REQUESTFRIENDLIST;
	}
}
