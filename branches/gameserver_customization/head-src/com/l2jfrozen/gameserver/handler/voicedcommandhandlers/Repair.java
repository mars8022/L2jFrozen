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
package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * <B><U>User Character .repair voicecommand - SL2 L2JEmu</U></B><BR><BR>
 *
 * 
 * <U>NOTICE:</U> Voice command .repair that when used, allows player to
 * try to repair any of characters on his account, by setting spawn
 * to Floran, removing all shortcuts and moving everything equipped to
 * that char warehouse.<BR><BR>
 *
 *
 * (solving client crashes on character entering world)<BR><BR>
 *
 *
 * @author szponiasty
 * @version $Revision: 0.17.2.95.2.9 $ $Date: 2010/03/03 9:07:11 $
 */

public class Repair implements IVoicedCommandHandler
{
	static final Logger _log = Logger.getLogger(Repair.class.getName());
	
	private static final String[]	 _voicedCommands	=
		{
		"repair",
		"startrepair"
		};

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{		
		if(activeChar==null)
			return false;

		String repairChar=null;

		try
		{
			if(target != null)
				if(target.length() > 1)
				  {
				   String[] cmdParams = target.split(" ");
				   repairChar=cmdParams[0];
				  }
		}
		catch(Exception e)
		{
			repairChar = null;
		}

		if(command.startsWith("repair"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/mods/repair.htm");
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(content);
			npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		if(command.startsWith("startrepair") && (repairChar != null))
		{
				if(checkAcc(activeChar,repairChar))
				{
					if(checkChar(activeChar,repairChar))
					{
						String content = HtmCache.getInstance().getHtm("data/html/mods/repair-self.htm");
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(content);
						activeChar.sendPacket(npcHtmlMessage);
						return false;
					}
					else
					{
						repairBadCharacter(repairChar);
						String content = HtmCache.getInstance().getHtm("data/html/mods/repair-done.htm");
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(content);
						activeChar.sendPacket(npcHtmlMessage);
						return true;
					}
				}
				else
				{
					String content = HtmCache.getInstance().getHtm("data/html/mods/repair-error.htm");
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					npcHtmlMessage.setHtml(content);
					activeChar.sendPacket(npcHtmlMessage);
					return false;
				}
		}
		return false;
	}

	private String getCharList(L2PcInstance activeChar)
	{
		String result="";
		String repCharAcc=activeChar.getAccountName();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, repCharAcc);
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				if(activeChar.getName().compareTo(rset.getString(1)) != 0)
					result += rset.getString(1)+";";
			}
			rset.close();
			statement.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			try
			{
				if(con != null)
					con.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return result;	
	}

	private boolean checkAcc(L2PcInstance activeChar,String repairChar)
	{
		boolean result=false;
		String repCharAcc="";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			ResultSet rset = statement.executeQuery();
			if(rset.next())
			{
				repCharAcc = rset.getString(1);
			}
			rset.close();
			statement.close();

		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			try
			{
				if(con != null)
					con.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		if(activeChar.getAccountName().compareTo(repCharAcc)==0)
			result=true;
		return result;
	}

	private boolean checkChar(L2PcInstance activeChar,String repairChar)
	{
		boolean result=false;
		if(activeChar.getName().compareTo(repairChar)==0)
			result=true;
		return result;
	}

	private void repairBadCharacter(String charName)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement;
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			ResultSet rset = statement.executeQuery();

			int objId = 0;
			if(rset.next())
			{
				objId = rset.getInt(1);
			}
			rset.close();
			statement.close();
			if(objId == 0)
			{
				con.close();
				return;
			}
			statement = con.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3503 WHERE obj_Id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning("GameServer: could not repair character:" + e);
		}
		finally
		{
			try
			{
				if(con != null)
					con.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}