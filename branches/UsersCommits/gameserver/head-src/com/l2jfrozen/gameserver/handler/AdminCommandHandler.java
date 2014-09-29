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
package com.l2jfrozen.gameserver.handler;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminAio;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminBBS;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminBuffs;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminCache;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminCharSupervision;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminChristmas;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminDMEngine;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminDonator;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminEventEngine;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminFortSiege;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminGeodata;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminKill;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminLogin;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMassControl;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMassRecall;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMobGroup;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminNoble;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminQuest;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminReload;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminRepairChar;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminScript;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminShutdown;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminTest;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminTownWar;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminTvTEngine;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminVIPEngine;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminWho;
import com.l2jfrozen.gameserver.handler.admincommandhandlers.AdminZone;

/**
 * This class ...
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());
	
	private static AdminCommandHandler _instance;
	
	private FastMap<String, IAdminCommandHandler> _datatable;
	
	public static AdminCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AdminCommandHandler();
		}
		return _instance;
	}
	
	private AdminCommandHandler()
	{
		_datatable = new FastMap<String, IAdminCommandHandler>();
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminCTFEngine());
		registerAdminCommandHandler(new AdminVIPEngine());
		registerAdminCommandHandler(new AdminDMEngine());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminScript());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminEventEngine());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminChristmas());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminPolymorph());
		// registerAdminCommandHandler(new AdminBanChat());
		registerAdminCommandHandler(new AdminReload());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminFightCalculator());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminFortSiege());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminBBS());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminTest());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminMassRecall());
		registerAdminCommandHandler(new AdminMassControl());
		registerAdminCommandHandler(new AdminMobGroup());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminUnblockIp());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminRideWyvern());
		registerAdminCommandHandler(new AdminLogin());
		registerAdminCommandHandler(new AdminCache());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminQuest());
		registerAdminCommandHandler(new AdminZone());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminTownWar());
		registerAdminCommandHandler(new AdminTvTEngine());
		registerAdminCommandHandler(new AdminDonator());
		registerAdminCommandHandler(new AdminNoble());
		registerAdminCommandHandler(new AdminBuffs());
		registerAdminCommandHandler(new AdminAio());
		registerAdminCommandHandler(new AdminCharSupervision());
		registerAdminCommandHandler(new AdminWho()); // L2OFF command
		// ATTENTION: adding new command handlers, you have to change the
		// sql file containing the access levels rights
		
		_log.info("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
		
		if (Config.DEBUG)
		{
			String[] commands = new String[_datatable.keySet().size()];
			
			commands = _datatable.keySet().toArray(commands);
			
			Arrays.sort(commands);
			
			for (String command : commands)
			{
				if (AdminCommandAccessRights.getInstance().accessRightForCommand(command) < 0)
				{
					_log.info("ATTENTION: admin command " + command + " has not an access right");
				}
			}
			
		}
		
	}
	
	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String element : ids)
		{
			if (Config.DEBUG)
			{
				_log.info("Adding handler for command " + element);
			}
			
			if (_datatable.keySet().contains(new String(element)))
			{
				_log.log(Level.WARNING, "Duplicated command \"" + element + "\" definition in " + handler.getClass().getName() + ".");
			}
			else
			{
				_datatable.put(element, handler);
			}
		}
		ids = null;
	}
	
	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		
		if (Config.DEBUG)
		{
			_log.info("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		}
		
		return _datatable.get(command);
	}
}