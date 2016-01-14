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
package com.l2jfrozen.gameserver.powerpak.xmlrpc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Server
{
	/**
	 * Retained for compatibility with PowerPak
	 * @param key 
	 * @param charName 
	 * @param itemId 
	 * @param count 
	 * @param message 
	 * @return 
	 */
	public int addItem(String key, String charName, String itemId, String count, String message)
	{
		return addItemToCharacter(charName, itemId, count, message);
	}

	/**
	 * Add item to the player<br>
	 * 
	 * @param charName as String - character name <br>
	 * @param itemId as String - ID subject <br>
	 * @param count as String - the number of <br>
	 * @param message as String - message <br>
	 * @return as Integer - 0 - satisfied, -1 SQL error -2 -3 Character no other errors
	 */
	public int addItemToCharacter(String charName, String itemId, String count, String message)
	{
		int output = 0;
		
		L2PcInstance player = L2World.getInstance().getPlayer(charName);
		if(player == null)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement stm = con.prepareStatement("select obj_id from characters where char_name like ?");
				stm.setString(1, charName);
				ResultSet r = stm.executeQuery();

				if(r.next())
				{
					player = L2PcInstance.load(r.getInt(1));
				}

				r.close();
				stm.close();
				
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				output = -1;
			}finally{
				CloseUtil.close(con);
				con=null;
			}
		}

		if(player == null)
		{
			output = -2;
		}
		else
		{
			try
			{
				player.addItem("Web", Integer.parseInt(itemId), Integer.parseInt(count), null, player.isOnline() != 0);
				if(player.isOnline() == 0)
				{
					player.store();
				}
				else if(message != null && message.length() > 0)
				{
					player.sendMessage(message);
				}
				output = 0;
			}
			catch (Exception e)
			{
				output = -3;
			}
		}
		return output;
	}

	/**
	 * Get a list of all the players on-line<br>
	 * <br>
	 * 
	 * @return as String - A list of all online players<br>
	 */
	public String getOnLine()
	{
		String result = "<online>";
		for(L2PcInstance p : L2World.getInstance().getAllPlayers())
		{
			if(p!=null)
				result += "<player id=\"" + p.getObjectId() + "\" name=\"" + p.getName() + "\" level=\"" + p.getLevel() + "\" class=\"" + p.getActiveClass()/* + "\" clan=\"" + p.getClan() == null ? "" : p.getClan().getName()*/ + "\" />";
		}
		return result;
	}
}
