/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;

public class AdminTownWar implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_townwar_start",
		"admin_townwar_end"
	};
	private L2Object _activeObject;
	public final L2Object getActiveObject()
	{
		return _activeObject;
	}
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
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
		
		if(command.startsWith("admin_townwar_start"))
		{
			startTW(activeChar);
		}
		if(command.startsWith("admin_townwar_end"))
		{
			endTW(activeChar);
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	private void startTW(L2PcInstance activeChar)
	{
		if(Config.TW_ALL_TOWNS)
		{
			TownManager.getInstance().getTown(1).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(2).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(3).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(4).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(5).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(6).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(7).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(8).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(9).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(10).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(11).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(12).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(13).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(14).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(15).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(16).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(17).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(19).setParameter("isPeaceZone", "false");
			TownManager.getInstance().getTown(20).setParameter("isPeaceZone", "false");
		}
		if(!Config.TW_ALL_TOWNS && Config.TW_TOWN_ID != 18 && Config.TW_TOWN_ID != 21 && Config.TW_TOWN_ID != 22)
		{
			TownManager.getInstance().getTown(Config.TW_TOWN_ID).setParameter("isPeaceZone", "false");
		}

		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		{
			int x,y,z;
			L2TownZone Town;
			byte zonaPaz = 1;

			for(L2PcInstance onlinePlayer : pls)
				if(onlinePlayer.isOnline() == 1 )
				{
					x = onlinePlayer.getX();
					y = onlinePlayer.getY();
					z = onlinePlayer.getZ();

					Town = TownManager.getInstance().getTown(x, y, z);
					if(Town != null)
					{
						if(Town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(zonaPaz, false);
							onlinePlayer.revalidateZone(true);
						}
						else if(Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(zonaPaz, false);
							onlinePlayer.revalidateZone(true);
						}
					}
						onlinePlayer.setInTownWar(true);
					}
		}

		if(Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().announceToAll("Town War Event!");
			Announcements.getInstance().announceToAll("All towns have been set to war zone by " + activeChar.getName() + ".");
		}
		if(!Config.TW_ALL_TOWNS) 
		{
			Announcements.getInstance().announceToAll("Town War Event!");
			Announcements.getInstance().announceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set to war zone by " + activeChar.getName() + ".");
		}
	}

	@SuppressWarnings("deprecation")
	private void endTW(L2PcInstance activeChar)
	{
		if(Config.TW_ALL_TOWNS)
		{
			TownManager.getInstance().getTown(1).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(2).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(3).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(4).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(5).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(6).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(7).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(8).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(9).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(10).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(11).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(12).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(13).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(14).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(15).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(16).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(17).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(19).setParameter("isPeaceZone", "true");
			TownManager.getInstance().getTown(20).setParameter("isPeaceZone", "true");
		}
		if(!Config.TW_ALL_TOWNS && Config.TW_TOWN_ID != 18 && Config.TW_TOWN_ID != 21 && Config.TW_TOWN_ID != 22)
		{
			TownManager.getInstance().getTown(Config.TW_TOWN_ID).setParameter("isPeaceZone", "true");
		}

		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		{
			int xx,yy,zz;
			L2TownZone Town;
			byte zonaPaz = 1;

			for(L2PcInstance onlinePlayer : pls)
				if(onlinePlayer.isOnline() == 1 )
				{
					xx = onlinePlayer.getX();
					yy = onlinePlayer.getY();
					zz = onlinePlayer.getZ();

					Town = TownManager.getInstance().getTown(xx,yy,zz);
					if(Town != null)
					{
						if(Town.getTownId() == Config.TW_TOWN_ID && !Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(zonaPaz, true);
							onlinePlayer.revalidateZone(true);
						}
						else if(Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(zonaPaz, true);
							onlinePlayer.revalidateZone(true);
						}
					}
						onlinePlayer.setInTownWar(false);
					}
		}

		if(Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().announceToAll("All towns have been set back to normal by " + activeChar.getName() + ".");	
		}
		if(!Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().announceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set back to normal by " + activeChar.getName() + ".");
		}
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}