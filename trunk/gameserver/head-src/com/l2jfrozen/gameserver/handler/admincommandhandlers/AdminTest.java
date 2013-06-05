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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_test",
			"admin_stats",
			"admin_mcrit",
			"admin_addbufftest",
			"admin_skill_test",
			"admin_st",
			"admin_mp",
			"admin_known",
			"admin_oly_obs_mode",
			"admin_obs_mode"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(command.equals("admin_stats"))
		{
			for(String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		if(command.equals("admin_mcrit"))
		{
			L2Character target = (L2Character) activeChar.getTarget();
			
			activeChar.sendMessage("Activechar Mcrit "+activeChar.getMCriticalHit(null, null));
			activeChar.sendMessage("Activechar baseMCritRate "+activeChar.getTemplate().baseMCritRate);
			
			if(target != null)
			{
				activeChar.sendMessage("Target Mcrit "+target.getMCriticalHit(null, null));
			    activeChar.sendMessage("Target baseMCritRate "+target.getTemplate().baseMCritRate);
		    }
		}
		if(command.equals("admin_addbufftest"))
		{
			L2Character target = (L2Character) activeChar.getTarget();
			activeChar.sendMessage("cast");
			
			L2Skill skill = SkillTable.getInstance().getInfo(1085,3);

			if (target != null)
			{
				activeChar.sendMessage("target locked");
				
				for (int i = 0; i < 100;)
				{
					if (activeChar.isCastingNow())
						continue;
					
					activeChar.sendMessage("Casting "+i);
					activeChar.useMagic(skill, false, false);
					i++;
				}
			}
		}
		else if(command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();

				int id = Integer.parseInt(st.nextToken());

				adminTestSkill(activeChar, id);

				st = null;
			}
			catch(NumberFormatException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch(NoSuchElementException nsee)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					nsee.printStackTrace();
				
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if(command.equals("admin_mp on"))
		{
			//.startPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_mp off"))
		{
			//.stopPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_mp dump"))
		{
			//.dumpPacketHistory();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if(command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		else if(command.equals("admin_test"))
		{
			activeChar.sendMessage("Now the server will send a packet that client cannot read correctly");
			activeChar.sendMessage("generating a critical error..");
			
			int i = 5;
			while(i>0){

				activeChar.sendMessage("Client will crash in "+i+" seconds");
				
				try
				{
					Thread.sleep(1000);
					i--;
				}
				catch(InterruptedException e)
				{
				}
				
			}
			
			UserInfo ui = new UserInfo(activeChar);
			ui._critical_test = true;
			
			activeChar.sendPacket(ui);
			
			
		}
		else if (command.startsWith("admin_oly_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterOlympiadObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ(), -1);
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
		else if (command.startsWith("admin_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
		return true;
	}
	

	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id)
	{
		L2Character player;
		L2Object target = activeChar.getTarget();

		if(target == null || !(target instanceof L2Character))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}

		player.broadcastPacket(new MagicSkillUser(activeChar, player, id, 1, 1, 1));

		target = null;
		player = null;
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}