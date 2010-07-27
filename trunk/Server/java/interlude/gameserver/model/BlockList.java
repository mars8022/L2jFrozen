/*
 * $Header: BlockList.java, 21/11/2005 14:53:53 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/11/2005 14:53:53 $
 * $Revision: 1 $
 * $Log: BlockList.java,v $
 * Revision 1  21/11/2005 14:53:53  luisantonioa
 * Added copyright notice
 *
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
package interlude.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.logging.Logger;

import javolution.util.FastSet;
import interlude.L2DatabaseFactory;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class BlockList
{
	private static final Logger _log = Logger.getLogger(BlockList.class.getName());
	private final Set<String> _blockSet;
	@SuppressWarnings("unused")
	private boolean _blockAll;
	private L2PcInstance _owner;

	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockSet = new FastSet<String>();
		_blockAll = false;
		restoreBlocksFromDb();
	}

	private boolean addToBlockList(L2PcInstance character)
	{
		return addToBlockList(character.getName());
	}

	private boolean addToBlockList(String name)
	{
		if (saveBlockIntoDb(name))
		{
			_blockSet.add(name);
			return true;
		}
		return false;
	}

	private boolean removeFromBlockList(L2PcInstance character)
	{
		return removeFromBlockList(character.getName());
	}

	private boolean removeFromBlockList(String name)
	{
		if (deleteBlockFromDb(name))
		{
			_blockSet.remove(name);
			return true;
		}
		return false;
	}

	private boolean isInBlockList(L2PcInstance character)
	{
		return _blockSet.contains(character.getName());
	}

	private boolean isInBlockList(String name)
	{
		return _blockSet.contains(name);
	}

	// private boolean isBlockAll()
	// {
	// return _blockAll;
	// }
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance character)
	{
		// BlockList blockList = listOwner.getBlockList();
		// return blockList.isBlockAll() || blockList.isInBlockList(character);
		return listOwner.getBlockList().isInBlockList(character);
	}

	// private void setBlockAll(boolean state)
	// {
	// _blockAll = state;
	// }
	private Set<String> getBlockList()
	{
		return _blockSet;
	}

	private boolean saveBlockIntoDb(String blockedName)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_blocklist (blocker,blocked) values (?,?)");
			statement.setString(1, _owner.getName());
			statement.setString(2, blockedName);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not store block: " + _owner.getName() + " blocked " + blockedName + ": " + e);
			return false;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	private void restoreBlocksFromDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT blocked FROM character_blocklist WHERE blocker = ?");
			statement.setString(1, _owner.getName());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_blockSet.add(rset.getString("blocked"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore blocklist for: " + _owner.getName() + " - " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private boolean deleteBlockFromDb(String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_blocklist WHERE blocker = ? AND blocked = ?");
			statement.setString(1, _owner.getName());
			statement.setString(2, name);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not delete block: " + _owner.getName() + " unblocked " + name + ": " + e);
			return false;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	public static int getOfflineCharacterACL(String name) throws IllegalArgumentException
	{
		int acl = 0;
		boolean found = false;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT accesslevel FROM characters WHERE char_name = ?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				acl = rset.getInt("accesslevel");
				found = true;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not get char objId for: " + name + " - " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		if (!found) {
			throw new IllegalArgumentException();
		}
		return acl;
	}

	public static void addToBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		if (listOwner.getBlockList().addToBlockList(character))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			character.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
			sm.addString(character.getName());
			listOwner.sendPacket(sm);
		}
		else
		{
			listOwner.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
		}
	}

	public static void addToBlockList(L2PcInstance listOwner, String name)
	{
		if (listOwner.getBlockList().addToBlockList(name))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
			sm.addString(name);
			listOwner.sendPacket(sm);
		}
		else
		{
			listOwner.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
		}
	}

	public static void removeFromBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		if (listOwner.getBlockList().removeFromBlockList(character))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
			sm.addString(character.getName());
			listOwner.sendPacket(sm);
		}
		else
		{
			listOwner.sendMessage("Failed to unblock " + character.getName() + ".");
		}
	}

	public static void removeFromBlockList(L2PcInstance listOwner, String name)
	{
		if (listOwner.getBlockList().removeFromBlockList(name))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
			sm.addString(name);
			listOwner.sendPacket(sm);
		}
		else
		{
			listOwner.sendMessage("Failed to unblock " + name + ".");
		}
	}

	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		return listOwner.getBlockList().isInBlockList(character);
	}

	public static boolean isInBlockList(L2PcInstance listOwner, String name)
	{
		return listOwner.getBlockList().isInBlockList(name);
	}

	// public static boolean isBlockAll(L2PcInstance listOwner)
	// {
	// return listOwner.getBlockList().isBlockAll();
	// }
	// public static void setBlockAll(L2PcInstance listOwner, boolean
	// newValue)
	// {
	// listOwner.getBlockList().setBlockAll(newValue);
	// }
	public static void sendListToOwner(L2PcInstance listOwner)
	{
		listOwner.sendPacket(new SystemMessage(SystemMessageId.BLOCK_LIST_HEADER));
		for (String playerName : listOwner.getBlockList().getBlockList())
		{
			listOwner.sendMessage(playerName);
		}
		listOwner.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
	}
}
