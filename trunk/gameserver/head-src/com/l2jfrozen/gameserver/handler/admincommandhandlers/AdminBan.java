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
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles following admin commands: - ban account_name = changes account access level to -100 and logs him
 * off. If no account is specified, target's account is used. - unban account_name = changes account access level to 0.
 * - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified. -
 * unjail charname = Unjails player, teleport him to Floran.
 * 
 * @version $Revision: 1.2 $
 * @author ProGramMoS
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_ban", "admin_unban", "admin_jail", "admin_unjail"
	};

	private enum CommandEnum
	{
		admin_ban,
		admin_unban,
		admin_jail,
		admin_unjail
	}

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
		
		StringTokenizer st = new StringTokenizer(command);
		
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;

		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if(comm == null)
			return false;
		
		switch(comm)
		{
			case admin_ban:
				
				if(st.hasMoreTokens()){
					
					player = st.nextToken();
					plyr = L2World.getInstance().getPlayer(player);
					
				}else{
					
					L2Object target = activeChar.getTarget();

					if(target != null && target instanceof L2PcInstance)
					{
						plyr = (L2PcInstance) target;
					}
					else
					{
						activeChar.sendMessage("Usage: //ban [account_name or char_name] (if none, target char's account gets banned)");
						return false;
					}

					target = null;
					
				}
				
				if(plyr != null && plyr.equals(activeChar))
				{
					plyr.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
					return false;
					
				}
				else if(plyr == null)
				{
					account_name = player;
					LoginServerThread.getInstance().sendAccessLevel(account_name, -1);
					activeChar.sendMessage("Ban request sent for account " + account_name + ". If you need a playername based commmand, see //ban_menu");
					
					activeChar.sendMessage("Account " + account_name + " banned.");
					
					return true;
					
				}
				else
				{
					plyr.setAccountAccesslevel(-1);
					account_name = plyr.getAccountName();
					RegionBBSManager.getInstance().changeCommunityBoard();
					//plyr.logout();
					plyr.closeNetConnection();
					activeChar.sendMessage("Account " + account_name + " banned.");
					if(Config.ANNOUNCE_TRY_BANNED_ACCOUNT) {
						Announcements.getInstance().announceToAll("Administrator has banned player " + plyr.getName() + "forever");
					}
					return true;
					
				}
				
			case admin_unban:
				
				if(st.hasMoreTokens()){
					
					account_name = st.nextToken();
					LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
					activeChar.sendMessage("Unban request sent for account " + account_name + ". If you need a playername based commmand, see //unban_menu");
				
					return true;
					
				}else{
					
					activeChar.sendMessage("Usage: //unban <account_name>");

					return false;
				}
				
			case admin_jail:
				
				if(st.hasMoreTokens()){
					
					player = st.nextToken();
					
					L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

					int delay = 0;
					
					if(st.hasMoreTokens()){
						
						try
						{
							delay = Integer.parseInt(st.nextToken());
						}
						catch(NumberFormatException nfe)
						{
							activeChar.sendMessage("Specified delay must be a number");
							return false;
						}
						
					}
					
					if(playerObj != null)
					{
						playerObj.setInJail(true, delay);
						activeChar.sendMessage("Character " + player + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
						return true;
						
					}
					else
					{
						jailOfflinePlayer(activeChar, player, delay);
						return true;
					}

					
				}else{
					activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
					return false;
				}
				
			case admin_unjail:
				if(st.hasMoreTokens()){
					
					player = st.nextToken();
					L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

					if(playerObj != null)
					{
						playerObj.setInJail(false, 0);
						activeChar.sendMessage("Character " + player + " removed from jail");
						return true;
					}
					else
					{
						unjailOfflinePlayer(activeChar, player);
						return true;
					}

				}else{
					activeChar.sendMessage("Usage: //unjail <charname>");
					return false;
				}
				
		}

		st = null;
		account_name = null;
		player = null;
		plyr = null;
		comm = null;
		
		return false;
	}

	private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, 1);
			statement.setLong(5, delay * 60000L);
			statement.setString(6, name);

			statement.execute();

			int count = statement.getUpdateCount();

			statement.close();
			statement = null;

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch(SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");

			if(Config.ENABLE_ALL_EXCEPTIONS)
				se.printStackTrace();
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

	private void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();

			int count = statement.getUpdateCount();

			statement.close();
			statement = null;

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " removed from jail");
			}
		}
		catch(SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");

			if(Config.ENABLE_ALL_EXCEPTIONS)
				se.printStackTrace();
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

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
