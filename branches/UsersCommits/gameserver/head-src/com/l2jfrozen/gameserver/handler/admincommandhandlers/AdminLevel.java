package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_add_level", "admin_set_level"
	};
	
	@Override
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

		L2Object targetChar = activeChar.getTarget();
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String val = "";
		if(st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if(actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if(targetChar instanceof L2PlayableInstance)
				{
					((L2PlayableInstance) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			}
			catch(NumberFormatException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("Wrong Number Format");
			}
		}
		else if(actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			try
			{
				if(targetChar == null || !(targetChar instanceof L2PlayableInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT)); // incorrect
					return false;
				}

				final L2PlayableInstance targetPlayer = (L2PlayableInstance) targetChar;

				final byte lvl = Byte.parseByte(val);
				int max_level = ExperienceData.getInstance().getMaxLevel();

				if(targetChar instanceof L2PcInstance && ((L2PcInstance) targetPlayer).isSubClassActive())
				{
					max_level = Config.MAX_SUBCLASS_LEVEL;
				}

				if(lvl >= 1 && lvl <= max_level)
				{
					final long pXp = targetPlayer.getStat().getExp();
					final long tXp = ExperienceData.getInstance().getExpForLevel(lvl);

					if(pXp > tXp)
					{
						targetPlayer.getStat().removeExpAndSp(pXp - tXp, 0);
					}
					else if(pXp < tXp)
					{
						targetPlayer.getStat().addExpAndSp(tXp - pXp, 0);
					}
				}
				else
				{
					activeChar.sendMessage("You must specify level between 1 and " + ExperienceData.getInstance().getMaxLevel() + ".");
					return false;
				}
			}
			catch(final NumberFormatException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				activeChar.sendMessage("You must specify level between 1 and " + ExperienceData.getInstance().getMaxLevel() + ".");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
