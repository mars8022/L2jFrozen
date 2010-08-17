package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author ProGramMoS
 */

public class AdminBuffs implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_getbuffs", "admin_stopbuff", "admin_stopallbuffs", "admin_areacancel"
	};

	private enum CommandEnum
	{
		admin_getbuffs,
		admin_stopbuff,
		admin_stopallbuffs,
		admin_areacancel
	}

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

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

		StringTokenizer st = new StringTokenizer(command, " ");

		String[] wordList = command.split(" ");
		CommandEnum comm;

		try
		{
			comm = CommandEnum.valueOf(wordList[0]);
		}
		catch(Exception e)
		{
			return false;
		}

		CommandEnum commandEnum = comm;

		switch(commandEnum)
		{
			case admin_getbuffs:
				st = new StringTokenizer(command, " ");
				command = st.nextToken();

				if(st.hasMoreTokens())
				{
					L2PcInstance player = null;
					String playername = st.nextToken();
					st = null;

					try
					{
						player = L2World.getInstance().getPlayer(playername);
					}
					catch(Exception e)
					{
						//ignore
					}

					if(player != null)
					{
						showBuffs(player, activeChar);
						playername = null;
						player = null;
						return true;
					}
					else
					{
						activeChar.sendMessage("The player " + playername + " is not online");
						playername = null;
						player = null;
						return false;
					}
				}
				else if(activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
				{
					showBuffs((L2PcInstance) activeChar.getTarget(), activeChar);
					return true;
				}
				else
					return true;

			case admin_stopbuff:
				try
				{
					st = new StringTokenizer(command, " ");

					st.nextToken();
					String playername = st.nextToken();

					int SkillId = Integer.parseInt(st.nextToken());

					removeBuff(activeChar, playername, SkillId);

					st = null;
					playername = null;

					return true;
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Failed removing effect: " + e.getMessage());
					activeChar.sendMessage("Usage: //stopbuff <playername> [skillId]");
					return false;
				}

			case admin_stopallbuffs:
				st = new StringTokenizer(command, " ");
				st.nextToken();
				String playername = st.nextToken();

				if(playername != null)
				{
					removeAllBuffs(activeChar, playername);
					playername = null;
					st = null;

					return true;
				}
				else
				{
					playername = null;
					st = null;

					return false;
				}

			case admin_areacancel:
				st = new StringTokenizer(command, " ");
				st.nextToken();
				String val = st.nextToken();

				try
				{
					int radius = Integer.parseInt(val);

					for(L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					{
						if(knownChar instanceof L2PcInstance && !knownChar.equals(activeChar))
						{
							knownChar.stopAllEffects();
						}
					}

					activeChar.sendMessage("All effects canceled within raidus " + radius);
					st = null;
					val = null;

					return true;
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Usage: //areacancel <radius>");
					st = null;
					val = null;

					return false;
				}
		}

		wordList = null;
		comm = null;
		commandEnum = null;
		st = null;

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	public void showBuffs(L2PcInstance player, L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder();

		html.append("<html><center><font color=\"LEVEL\">Effects of " + player.getName() + "</font><center><br>");
		html.append("<table>");
		html.append("<tr><td width=200>Skill</td><td width=70>Action</td></tr>");

		L2Effect[] effects = player.getAllEffects();

		for(L2Effect e : effects)
		{
			if(e != null)
			{
				html.append("<tr><td>" + e.getSkill().getName() + "</td><td><button value=\"Remove\" action=\"bypass -h admin_stopbuff " + player.getName() + " " + String.valueOf(e.getSkill().getId()) + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			}
		}

		html.append("</table><br>");
		html.append("<button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs " + player.getName() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		html.append("</html>");

		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());

		activeChar.sendPacket(ms);

		html = null;
		ms = null;
		effects = null;
	}

	private void removeBuff(L2PcInstance remover, String playername, int SkillId)
	{
		L2PcInstance player = null;

		try
		{
			player = L2World.getInstance().getPlayer(playername);
		}
		catch(Exception e)
		{
			//ignore
		}

		if(player != null && SkillId > 0)
		{
			L2Effect[] effects = player.getAllEffects();

			for(L2Effect e : effects)
			{
				if(e != null && e.getSkill().getId() == SkillId)
				{
					e.exit();
					remover.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + playername);
				}
			}
			showBuffs(player, remover);

			player = null;
			effects = null;
		}
	}

	private void removeAllBuffs(L2PcInstance remover, String playername)
	{
		L2PcInstance player = null;

		try
		{
			player = L2World.getInstance().getPlayer(playername);
		}
		catch(Exception e)
		{
			//ignore
		}

		if(player != null)
		{
			player.stopAllEffects();
			remover.sendMessage("Removed all effects from " + playername);
			showBuffs(player, remover);

			player = null;
		}
		else
		{
			remover.sendMessage("Can not remove effects from " + playername + ". Player appears offline.");
			showBuffs(player, remover);

			player = null;
		}
	}

}
