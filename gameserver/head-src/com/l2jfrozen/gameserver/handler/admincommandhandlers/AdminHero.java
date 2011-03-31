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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 *L2Scoria
 **/
public class AdminHero implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_sethero"
	};

	protected static final Logger _log = Logger.getLogger(AdminHero.class.getName());
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){
			return false;
		}
		
		if(Config.GMAUDIT)
		{
			Logger _logAudit = Logger.getLogger("gmaudit");
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}
		*/

		if(activeChar == null)
			return false;

		if(command.startsWith("admin_sethero"))
		{
			String[] cmdParams = command.split(" ");

			long heroTime = 0;
			if(cmdParams.length > 1)
			{
				try
				{
					heroTime = Integer.parseInt(cmdParams[1]) * 24L * 60L * 60L * 1000L;
				}
				catch(NumberFormatException nfe)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						nfe.printStackTrace();
				}
			}
		
			L2Object target = activeChar.getTarget();

			if(target instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;
				boolean newHero = !targetPlayer.isHero();

				if(newHero)
				{
					targetPlayer.setIsHero(true);
					targetPlayer.sendMessage("You are now a hero.");
					updateDatabase(targetPlayer, true, heroTime);
					sendMessages(true, targetPlayer, activeChar, true, true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
					targetPlayer.broadcastUserInfo();
				}
				else
				{
					targetPlayer.setIsHero(false);
					targetPlayer.sendMessage("You are no longer a hero.");
					updateDatabase(targetPlayer, false, 0);
					sendMessages(false, targetPlayer, activeChar, true, true);
					targetPlayer.broadcastUserInfo();
				}

				targetPlayer = null;
			}
			else
			{
				activeChar.sendMessage("Impossible to set a non Player Target as hero.");
				_log.info("GM: " + activeChar.getName() + " is trying to set a non Player Target as hero.");

				return false;
			}

			target = null;
		}
		return true;
	}

	private void sendMessages(boolean fornewHero, L2PcInstance player, L2PcInstance gm, boolean announce, boolean notifyGmList)
	{
		if(fornewHero)
		{
			player.sendMessage(gm.getName() + " has granted Hero Status for you!");
			gm.sendMessage("You've granted Hero Status for " + player.getName());

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has set " + player.getName() + " as Hero !");
			}
		}
		else
		{
			player.sendMessage(gm.getName() + " has revoked Hero Status from you!");
			gm.sendMessage("You've revoked Hero Status from " + player.getName());

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has removed Hero Status of player" + player.getName());
			}
		}
	}

	/**
	 * @param activeChar
	 * @param newDonator
	 */
	private void updateDatabase(L2PcInstance player, boolean newHero, long heroTime)
	{
		Connection con = null;
		try
		{
			// prevents any NPE.
			// ----------------
			if(player == null)
				return;

			// Database Connection
			//--------------------------------
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement stmt = con.prepareStatement(newHero ? INSERT_DATA : DEL_DATA);

			// if it is a new donator insert proper data
			// --------------------------------------------
			if(newHero)
			{

				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, 1);
				stmt.setInt(4, 1);
				stmt.setInt(5, player.isDonator() ? 1 : 0);
				stmt.setLong(6, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			else
			// deletes from database
			{
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
				stmt.close();
				stmt = null;
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error: could not update database: ", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	// Updates That Will be Executed by MySQL
	// ----------------------------------------
	String INSERT_DATA = "REPLACE INTO characters_custom_data (obj_Id, char_name, hero, noble, donator, hero_end_date) VALUES (?,?,?,?,?,?)";
	String DEL_DATA = "UPDATE characters_custom_data SET hero = 0 WHERE obj_Id=?";

	/**
	 * @return
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
