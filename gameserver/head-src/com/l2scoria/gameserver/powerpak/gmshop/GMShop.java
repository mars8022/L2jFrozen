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
package com.l2scoria.gameserver.powerpak.gmshop;

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.handler.IBBSHandler;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.multisell.L2Multisell;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import com.l2scoria.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * 
 * 
 * @author Nick
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
			msg = "GMShop не доступен когда вы сидите";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("ALL"))
			msg = "GMShop не доступен в данной зоне";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = "GMShop не доступен с проклятым оружием"; 
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = "GMShop не доступен во время боя";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = "GMShop не доступен в катакомбах и некрополисах";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
				msg = "GMShop не доступен в данной зоне";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = "GMShop не доступен в данной зоне";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = "GMShop не доступен в данной зоне";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = "GMShop не доступен в данной зоне";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("OLYMPIAD") && (activeChar.isInOlympiadMode() ||
				activeChar.isInsideZone(L2Character.ZONE_OLY) || Olympiad.getInstance().isRegistered(activeChar) ||
				Olympiad.getInstance().isRegisteredInComp(activeChar))) 
			msg = "GMShop не доступен на Великой Олимпиаде";
		else if(PowerPakConfig.GMSHOP_EXCLUDE_ON.contains("EVENT") && 
				(activeChar._inEvent))
			msg = "GMShop не доступен на эвенте";
		
		if(msg!=null)
			activeChar.sendMessage(msg);

		return msg==null;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if(activeChar==null) return false;
		if(!checkAllowed(activeChar)) return false;
		String index = "";
		if(params!=null && params.length()!=0)
			if(!params.equals("0"))
				index= "-"+params; 
		String text = HtmCache.getInstance().getHtm("data/html/gmshop/gmshop"+index+".htm");
		activeChar.sendPacket(new NpcHtmlMessage(5,text)); 
		return false;
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.ICustomByPassHandler#getByPassCommands()
	 */
	private static String [] _CMD =  {"gmshop"};
	@Override
	public String[] getByPassCommands()
	{
		return _CMD;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if(player==null) return;
	 	if(parameters ==null || parameters.length()==0) return;
	 	if(!checkAllowed(player)) return;
	 	if(parameters.startsWith("multisell")) {
	 		try {
	 			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(parameters.substring(9).trim()), player, false, 0);
	 		} catch(Exception e) {
	 			player.sendMessage("Данный список не существует");
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
