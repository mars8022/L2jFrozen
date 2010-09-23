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
package com.l2jfrozen.gameserver.powerpak;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.CommunityBoard;
import com.l2jfrozen.gameserver.handler.VoicedCommandHandler;
import com.l2jfrozen.gameserver.handler.custom.CustomBypassHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.Buffer.BuffHandler;
import com.l2jfrozen.gameserver.powerpak.Buffer.BuffTable;
import com.l2jfrozen.gameserver.powerpak.RaidInfo.RaidInfoHandler;
import com.l2jfrozen.gameserver.powerpak.Servers.WebServer;
import com.l2jfrozen.gameserver.powerpak.engrave.EngraveManager;
import com.l2jfrozen.gameserver.powerpak.globalGK.GKHandler;
import com.l2jfrozen.gameserver.powerpak.gmshop.GMShop;
import com.l2jfrozen.gameserver.powerpak.vote.L2TopDeamon;
import com.l2jfrozen.gameserver.powerpak.xmlrpc.XMLRPCServer;
import com.l2jfrozen.util.Util;

public class PowerPak
{
	private static PowerPak _instance = null;

	public static PowerPak getInstance()
	{
		if(_instance == null)
		{
			_instance = new PowerPak();
		}
		return _instance;
	}

	private PowerPak()
	{
		if(Config.POWERPAK_ENABLED)
		{
			PowerPakConfig.load();
			Util.printSection("PowerPak");
			if(PowerPakConfig.BUFFER_ENABLED)
			{
				System.out.println("Buffer:");
				BuffTable.getInstance();
				
				if((PowerPakConfig.BUFFER_COMMAND != null && PowerPakConfig.BUFFER_COMMAND.length() > 0) || PowerPakConfig.BUFFER_USEBBS){	
					
					BuffHandler handler = new BuffHandler();
					if(PowerPakConfig.BUFFER_COMMAND != null && PowerPakConfig.BUFFER_COMMAND.length() > 0)
					{
						VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handler);
					}
	
					if(PowerPakConfig.BUFFER_USEBBS)
					{
						CommunityBoard.getInstance().registerBBSHandler(handler);
					}
					CustomBypassHandler.getInstance().registerCustomBypassHandler(handler);
					
				}
				
				
				
				System.out.println("...Enabled");
			}

			if(PowerPakConfig.GLOBALGK_ENABDLED)
			{
				System.out.println("Global Gatekeeper:");
				GKHandler handler = new GKHandler();
				if(PowerPakConfig.GLOBALGK_COMMAND != null && PowerPakConfig.GLOBALGK_COMMAND.length() > 0)
				{
					VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handler);
				}

				if(PowerPakConfig.GLOBALGK_USEBBS)
				{
					CommunityBoard.getInstance().registerBBSHandler(handler);
				}
				CustomBypassHandler.getInstance().registerCustomBypassHandler(handler);
				System.out.println("...Enabled");
			}

			if(PowerPakConfig.GMSHOP_ENABLED)
			{
				System.out.println("GMShop:");
				GMShop gs = new GMShop();
				CustomBypassHandler.getInstance().registerCustomBypassHandler(gs);
				if(PowerPakConfig.GLOBALGK_COMMAND!=null && PowerPakConfig.GLOBALGK_COMMAND.length()>0)
				{
					VoicedCommandHandler.getInstance().registerVoicedCommandHandler(gs);
				}

				if(PowerPakConfig.GMSHOP_USEBBS)
				{
					CommunityBoard.getInstance().registerBBSHandler(gs);
				}
				System.out.println("...Enabled");
			}

			if(PowerPakConfig.ENGRAVER_ENABLED)
			{
				System.out.println("Engrave system:");
				EngraveManager.getInstance();
				System.out.println("...Enabled");
			}

			if(PowerPakConfig.L2TOPDEMON_ENABLED)
			{
				L2TopDeamon.getInstance();
			}

			if(PowerPakConfig.WEBSERVER_ENABLED)
			{
				WebServer.getInstance();
			}
			
			if(PowerPakConfig.XMLRPC_ENABLED)
			{
				XMLRPCServer.getInstance();
			}
			
			System.out.println("Raid Info:");
			RaidInfoHandler handler = new RaidInfoHandler();
			CustomBypassHandler.getInstance().registerCustomBypassHandler(handler);
			System.out.println("...Enabled");
		}
	}

	public void chatHandler(L2PcInstance sayer, int chatType, String message)
	{}
}