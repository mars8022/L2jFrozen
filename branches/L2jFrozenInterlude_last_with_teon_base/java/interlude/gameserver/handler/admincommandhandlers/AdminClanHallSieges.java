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
package interlude.gameserver.handler.admincommandhandlers;

import interlude.Config;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.instancemanager.clanhallsiege.BanditStrongholdSiege;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortResistSiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.instancemanager.clanhallsiege.WildBeastFarmSiege;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maxi
 */
public class AdminClanHallSieges implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_startfortresist", "admin_endfortresist", "admin_startdevastated", "admin_enddevastated", "admin_startbandit", "admin_endbandit", "admin_startwildbeastfarm", "admin_endwildbeastfarm", "admin_startfortress", "admin_endfortress" };
	private static final int REQUIRED_LEVEL = Config.GM_FORTSIEGE;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		}
		if (command.startsWith("admin_startfortresist"))
		{
			FortResistSiegeManager.getInstance().startSiege();
			validateStartSiege(activeChar,1);
		}
		else if (command.startsWith("admin_endfortresist"))
		{
			FortResistSiegeManager.getInstance().endSiege(true);
			getSendMessage(activeChar,1);
		}
		else if (command.startsWith("admin_startdevastated"))
		{
			DevastatedCastleManager.getInstance().startSiege();
			validateStartSiege(activeChar,2);
		}
		else if (command.startsWith("admin_enddevastated"))
		{
			DevastatedCastleManager.getInstance().endSiege(activeChar);
			getSendMessage(activeChar,2);
		}
		else if (command.startsWith("admin_startbandit"))
		{
			BanditStrongholdSiege.getInstance().startSiege();
			validateStartSiege(activeChar,3);
		}
		else if (command.startsWith("admin_endbandit"))
		{
			BanditStrongholdSiege.getInstance().endSiege(true);
			getSendMessage(activeChar,3);
		}
		else if (command.startsWith("admin_startwildbeastfarm"))
		{
			WildBeastFarmSiege.getInstance().startSiege();
			validateStartSiege(activeChar,4);
		}
		else if (command.startsWith("admin_endwildbeastfarm"))
		{
			WildBeastFarmSiege.getInstance().endSiege(true);
			getSendMessage(activeChar,4);
		}
		else if (command.startsWith("admin_startfortress"))
		{
			FortressofTheDeadManager.getInstance().startSiege();
			validateStartSiege(activeChar,5);
		}
		else if (command.startsWith("admin_endfortress"))
		{
			FortressofTheDeadManager.getInstance().endSiege(activeChar);
			getSendMessage(activeChar,5);
		}
		return true;
	}
	
	public boolean getSendMessage(L2PcInstance activeChar,int val)
	{
		if (val ==1)
			if (!FortResistSiegeManager.getInstance().getIsInProgress())
				activeChar.sendMessage("End siege Fortress of Resistance");
			else
				activeChar.sendMessage("Failed end siege Fortress of Resistance");
		if (val ==2)
			if (!DevastatedCastleManager.getInstance().getIsInProgress())
				activeChar.sendMessage("End Siege Devastated Castle");
			else
				activeChar.sendMessage("Failed end siege Devastated Castle");
		if (val ==3)
			if (!BanditStrongholdSiege.getInstance().getIsInProgress())
				activeChar.sendMessage("End siege Bandit Stronghold");
			else
				activeChar.sendMessage("Failed end siege Bandit Stronghold");
		if (val ==4)
			if (!WildBeastFarmSiege.getInstance().getIsInProgress())
				activeChar.sendMessage("End siege Wild Beast Farm");
			else
				activeChar.sendMessage("Failed end siege Wild Beast Farm");
		if (val ==5)
			if (!FortressofTheDeadManager.getInstance().getIsInProgress())
				activeChar.sendMessage("End Siege Fortress of The Dead");
			else
				activeChar.sendMessage("Failed end siege Fortress of The Dead");
		return true;
	}

	public boolean validateStartSiege(L2PcInstance activeChar,int val)
	{
		if (val ==1)
			if (FortResistSiegeManager.getInstance().getIsInProgress())
				activeChar.sendMessage("Start Siege Fortress of Resistance");
			else
				activeChar.sendMessage("Failed Start Siege Fortress of Resistance");
		if (val ==2)
			if (DevastatedCastleManager.getInstance().getIsInProgress())
				activeChar.sendMessage("Start Siege Devastated Castle");
			else
				activeChar.sendMessage("Failed start siege Devastated Castle");
		if (val ==3)
			if (BanditStrongholdSiege.getInstance().getIsInProgress())
				activeChar.sendMessage("Start siege Bandit Stronghold");
			else
				activeChar.sendMessage("Failed start siege Bandit Stronghlod");
		if (val ==4)
			if (WildBeastFarmSiege.getInstance().getIsInProgress())
				activeChar.sendMessage("Start siege Wild Beast Farm");
			else
				activeChar.sendMessage("Failed start siege Wild Beast Farm");
		if (val ==5)
			if (FortressofTheDeadManager.getInstance().getIsInProgress())
				activeChar.sendMessage("Start siege Fortress of The Dead");
			else
				activeChar.sendMessage("Failed start siege Fortress of The Dead");
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return level >= REQUIRED_LEVEL;
	}
}
