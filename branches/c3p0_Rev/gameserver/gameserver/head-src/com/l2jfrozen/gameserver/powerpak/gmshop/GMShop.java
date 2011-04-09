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
package com.l2jfrozen.gameserver.powerpak.gmshop;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.handler.IBBSHandler;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.multisell.L2Multisell;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author L2JFrozen
 */
public class GMShop implements IVoicedCommandHandler, ICustomByPassHandler, IBBSHandler
{

	@Override
	public String[] getVoicedCommandList()
	{
		
		return new String[] {PowerPakConfig.GMSHOP_COMMAND};
	}

	private boolean checkAllowed(L2PcInstance activeChar)
	{
		String msg = null;
		if(activeChar.isSitting())
			msg = "GMShop is not available when you sit";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("ALL"))
			msg = "GMShop is not available in this area";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = "GMShop is not available with the cursed sword"; 
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = "GMShop is not available during the battle";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = "GMShop is not available in the catacombs and necropolis";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
				msg = "GMShop is not available in this area";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = "GMShop is not available in this area";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = "GMShop is not available in this area";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = "GMShop is not available in this area";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("OLYMPIAD") && (activeChar.isInOlympiadMode() ||
				activeChar.isInsideZone(L2Character.ZONE_OLY) || Olympiad.getInstance().isRegistered(activeChar) ||
				Olympiad.getInstance().isRegisteredInComp(activeChar))) 
			msg = "GMShop is not available at Olympiad";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("EVENT") && 
				(activeChar._inEvent))
			msg = "GMShop is not available at the opening event";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("TVT") && 
				activeChar._inEventTvT && TvT.is_started() )
			msg = "GMShop is not available in TVT";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("CTF") && 
				activeChar._inEventCTF && CTF.is_started() )
			msg = "GMShop is not available in CTF";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("DM") && 
				activeChar._inEventDM && DM.is_started() )
			msg = "GMShop is not available in DM";
		
		if(msg!=null)
			activeChar.sendMessage(msg);

		return msg==null;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if(activeChar==null) 
			return false;
		if(!checkAllowed(activeChar)) 
			return false;
		
		if(command.compareTo(PowerPakConfig.GMSHOP_COMMAND)==0)
		{
			String index = "";
			if(params!=null && params.length()!=0)
				if(!params.equals("0"))
					index= "-"+params; 
			String text = HtmCache.getInstance().getHtm("data/html/gmshop/gmshop"+index+".htm");
			NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
			htm.setHtml(text);
			activeChar.sendPacket(htm);
		}
		
		return false;
	}

	private static String [] _CMD =  {"gmshop"};
	@Override
	public String[] getByPassCommands()
	{
		return _CMD;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if(player==null) 
			return;
	 	if(parameters ==null || parameters.length()==0) 
	 		return;
	 	if(!checkAllowed(player)) 
	 		return;
	 	
	 	if(!PowerPakConfig.GMSHOP_USEBBS && !PowerPakConfig.GMSHOP_USECOMMAND){
			
			L2NpcInstance gmshopnpc = null; 
			
			if(player.getTarget()!=null)
				if(player.getTarget() instanceof L2NpcInstance)
				{
					gmshopnpc = (L2NpcInstance)player.getTarget();
					if(gmshopnpc.getTemplate().getNpcId()!=PowerPakConfig.GMSHOP_NPC)
						gmshopnpc=null;
				}
			
			//Possible fix to Buffer - 1
			if (gmshopnpc == null)
				return;

			//Possible fix to Buffer - 2
			if (!player.isInsideRadius(gmshopnpc, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				return;
			
		}//else (voice and bbs)
		
	 	if(parameters.startsWith("multisell")) {
	 		try {
	 			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(parameters.substring(9).trim()), player, false, 0);
	 		} catch(Exception e) {
	 			if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
	 			
	 			player.sendMessage("This list does not exist");
	 		}
	 	}
	 	else if(parameters.startsWith("Chat"))
	 		useVoicedCommand("",player,parameters.substring(4).trim());
	 			
	}

	@Override
	public String[] getBBSCommands()
	{
		return _CMD;
	}
}
