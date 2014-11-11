package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

/*
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * <B><U>User Character .repair voicecommand - SL2 L2JEmu</U></B><BR>
 * <BR>
 * <U>NOTICE:</U> Voice command .repair that when used, allows player to try to repair any of characters on his account, by setting spawn to Floran, removing all shortcuts and moving everything equipped to that char warehouse.<BR>
 * <BR>
 * (solving client crashes on character entering world)<BR>
 * <BR>
 * @author szponiasty
 * @version $Revision: 0.17.2.95.2.9 $ $Date: 2010/03/03 9:07:11 $
 */
public class Repair implements IVoicedCommandHandler, ICustomByPassHandler
{
	static final Logger LOGGER = Logger.getLogger(Repair.class);
	
	private static final String[] _voicedCommands =
	{
		"repair",
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (activeChar == null)
			return false;
		
		// Send activeChar HTML page
		if (command.startsWith("repair"))
		{
			final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair.htm");
			final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		// Command for enter repairFunction from html
		
		// LOGGER.warn("Repair Attempt: Failed. ");
		return false;
	}
	
	private String getCharList(final L2PcInstance activeChar)
	{
		String result = "";
		final String repCharAcc = activeChar.getAccountName();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, repCharAcc);
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (activeChar.getName().compareTo(rset.getString(1)) != 0)
					result += rset.getString(1) + ";";
			}
			// LOGGER.warn("Repair Attempt: Output Result for searching characters on account:"+result);
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
			// return result;
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		return result;
	}
	
	private boolean checkAcc(final L2PcInstance activeChar, final String repairChar)
	{
		boolean result = false;
		String repCharAcc = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				repCharAcc = rset.getString(1);
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		if (activeChar.getAccountName().compareTo(repCharAcc) == 0)
			result = true;
		return result;
	}
	
	private boolean checkPunish(final L2PcInstance activeChar, final String repairChar)
	{
		boolean result = false;
		int accessLevel = 0;
		int repCharJail = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			final PreparedStatement statement = con.prepareStatement("SELECT accesslevel,punish_level FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				accessLevel = rset.getInt(1);
				repCharJail = rset.getInt(2);
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		if (repCharJail == 1 || accessLevel < 0) // 0 norm, 1 chat ban, 2 jail, 3....
			result = true;
		return result;
	}
	
	private boolean checkKarma(final L2PcInstance activeChar, final String repairChar)
	{
		boolean result = false;
		int repCharKarma = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT karma FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				repCharKarma = rset.getInt(1);
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		if (repCharKarma > 0)
			result = true;
		return result;
	}
	
	private boolean checkChar(final L2PcInstance activeChar, final String repairChar)
	{
		boolean result = false;
		if (activeChar.getName().compareTo(repairChar) == 0)
			result = true;
		return result;
	}
	
	private void repairBadCharacter(final String charName)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			final ResultSet rset = statement.executeQuery();
			
			int objId = 0;
			if (rset.next())
			{
				objId = rset.getInt(1);
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			if (objId == 0)
			{
				CloseUtil.close(con);
				con = null;
				return;
			}
			statement = con.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3503 WHERE obj_Id=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.warn("GameServer: could not repair character:" + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
			
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	private static final String[] _BYPASSCMD =
	{
		"repair",
		"repair_close_win"
	};
	
	private enum CommandEnum
	{
		repair,
		repair_close_win
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.ICustomByPassHandler#handleCommand(java.lang.String, com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public void handleCommand(final String command, final L2PcInstance activeChar, final String repairChar)
	{
		
		final CommandEnum comm = CommandEnum.valueOf(command);
		
		if (comm == null)
			return;
		
		switch (comm)
		{
			case repair:
			{
				
				if (repairChar == null || repairChar.equals(""))
					return;
				
				if (checkAcc(activeChar, repairChar))
				{
					if (checkChar(activeChar, repairChar))
					{
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-self.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkPunish(activeChar, repairChar))
					{
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-jail.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkKarma(activeChar, repairChar))
					{
						activeChar.sendMessage("Selected Char has Karma,Cannot be repaired!");
						return;
					}
					else
					{
						repairBadCharacter(repairChar);
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-done.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
				}
				
				final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-error.htm");
				final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
				activeChar.sendPacket(npcHtmlMessage);
				return;
			}
			case repair_close_win:
			{
				return;
			}
		}
		
	}
}
