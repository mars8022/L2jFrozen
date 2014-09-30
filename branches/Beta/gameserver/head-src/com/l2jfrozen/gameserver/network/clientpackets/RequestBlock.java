/* This program is free software; you can redistribute it and/or modify
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
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestBlock extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestBlock.class.getName());
	
	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;
	
	private String _name;
	private Integer _type;
	
	// private L2PcInstance _target;
	
	@Override
	protected void readImpl()
	{
		_type = readD(); // 0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock
		
		if (_type == BLOCK || _type == UNBLOCK)
		{
			_name = readS();
			// _target = L2World.getInstance().getPlayer(_name);
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		switch (_type)
		{
			case BLOCK:
			case UNBLOCK:
				
				L2PcInstance _target = L2World.getInstance().getPlayer(_name);
				
				if (_target == null)
				{
					// Incorrect player name.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
					return;
				}
				
				if (_target.isGM())
				{
					// Cannot block a GM character.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM));
					return;
				}
				
				if (_type == BLOCK)
				{
					
					if (activeChar.getBlockList().isInBlockList(_name))
					{
						// Player is already in your blocklist
						activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
						return;
					}
					
					activeChar.getBlockList().addToBlockList(_name);
					
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection(false);
						PreparedStatement statement = con.prepareStatement("SELECT * FROM character_friends WHERE char_id = ? AND friend_name = ?");
						statement.setInt(1, activeChar.getObjectId());
						statement.setString(2, _name);
						ResultSet rset = statement.executeQuery();
						
						if (rset.next())
						{
							
							statement = con.prepareStatement("UPDATE character_friends SET not_blocked = ? WHERE char_id = ? AND friend_name = ?");
							statement.setInt(1, _type);
							statement.setInt(2, activeChar.getObjectId());
							statement.setString(3, _name);
							statement.execute();
							
						}
						else
						{
							
							statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name, not_blocked) VALUES (?, ?, ?, ?)");
							statement.setInt(1, activeChar.getObjectId());
							statement.setInt(2, _target.getObjectId());
							statement.setString(3, _target.getName());
							statement.setInt(4, _type);
							statement.execute();
							
						}
						
						statement.close();
						
					}
					catch (Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						_log.warning("could not add blocked objectid: ");
						e.printStackTrace();
					}
					finally
					{
						CloseUtil.close(con);
						con = null;
					}
					
				}
				else
				{
					activeChar.getBlockList().removeFromBlockList(_name);
					
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection(false);
						PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? AND friend_name = ?");
						statement.setInt(1, activeChar.getObjectId());
						statement.setString(2, _name);
						statement.execute();
						statement.close();
						
					}
					catch (Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						_log.warning("could not add blocked objectid: ");
						e.printStackTrace();
					}
					finally
					{
						CloseUtil.close(con);
						con = null;
					}
					
				}
				
				break;
			case BLOCKLIST:
				
				activeChar.sendBlockList();
				
				break;
			case ALLBLOCK:
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));// Update by rocknow
				activeChar.getBlockList().setBlockAll(true);
				
				break;
			case ALLUNBLOCK:
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));// Update by rocknow
				activeChar.getBlockList().setBlockAll(false);
				
				break;
			default:
				_log.info("Unknown 0x0a block type: " + _type);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] A0 RequestBlock";
	}
}