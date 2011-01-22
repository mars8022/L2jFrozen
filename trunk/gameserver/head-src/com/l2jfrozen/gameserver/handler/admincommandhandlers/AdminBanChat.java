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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - admin_banchat = Imposes a chat ban on the specified player/target. -
 * admin_unbanchat = Removes any chat ban on the specified player/target. Uses: admin_banchat [<player_name>]
 * [<ban_duration>] admin_unbanchat [<player_name>] If <player_name> is not specified, the current target player is
 * used.
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBanChat implements IAdminCommandHandler
{
	//private static Logger _log = Logger.getLogger(AdminBan.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_banchat", "admin_unbanchat"
	};
	
	private enum CommandEnum
	{
		admin_banchat,
		admin_unbanchat,
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
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if(comm == null)
			return false;
		
		String player = "";
		L2PcInstance plyr = null;
		
		switch(comm)
		{
			case admin_banchat:{
				
				String time_s = "";
				long time = 0;
				
				if(st.hasMoreTokens()){ //char_name specified
					
					player = st.nextToken();
					plyr = L2World.getInstance().getPlayer(player);
					
				}else{ //just called //banchat with target --> infinite chat ban
					
					L2Object target = activeChar.getTarget();

					if(target != null && target instanceof L2PcInstance)
					{
						plyr = (L2PcInstance) target;
					}
					else
					{
						activeChar.sendMessage("Usage: //banchat [char_name] (if none, target char gets banned) [TIME] (if none, infinite chat ban)");
						return false;
					}

					target = null;
					
				}
				
				if(st.hasMoreTokens()){ //time
					
					time_s = st.nextToken();
					
					try
					{
						time = Integer.parseInt(time_s) * 60L; // 60000L
					}
					catch(NumberFormatException nfe)
					{
						activeChar.sendMessage("TIME must be a number");
						return false;
						
					}
					
				}
				
				if(plyr != null && plyr.equals(activeChar))
				{
					plyr.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
					return false;
				}
				else if(plyr == null)
				{
					activeChar.sendMessage("Usage: //banchat [char_name] (if none, target char gets banned) [TIME] (if none, infinite chat ban)");
					return false;
					
				}
				else
				{
					activeChar.sendMessage(plyr.getName() + " is now chat banned for " + (time > 0 ? time + " seconds." : "ever!"));
					plyr.setChatBanned(true, time * 1000L);
					return true;
				}
				
			}
			case admin_unbanchat:{
				
				if(st.hasMoreTokens()){ //char_name specified
					
					player = st.nextToken();
					plyr = L2World.getInstance().getPlayer(player);
					
				}else{ //just called //unbanchat with target
					
					L2Object target = activeChar.getTarget();

					if(target != null && target instanceof L2PcInstance)
					{
						plyr = (L2PcInstance) target;
					}
					else
					{
						activeChar.sendMessage("Usage: //unbanchat [char_name] (if none, target char gets unbanned)");
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
					activeChar.sendMessage("Usage: //unbanchat [char_name] (if none, target char gets unbanned)");
					return false;
					
				}
				else
				{
					activeChar.sendMessage(plyr.getName() + "'s chat ban has now been lifted.");
					plyr.setChatBanned(false, 0);
					return true;
				}
				
			}
			default:{
				return false;
			}
		}
		
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
