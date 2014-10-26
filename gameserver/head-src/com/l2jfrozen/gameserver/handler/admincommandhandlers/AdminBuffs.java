package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;

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
		"admin_getbuffs",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel"
	};
	
	private enum CommandEnum
	{
		admin_getbuffs,
		admin_stopbuff,
		admin_stopallbuffs,
		admin_areacancel
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		StringTokenizer st = new StringTokenizer(command, " ");
		
		CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
			return false;
		
		switch (comm)
		{
			case admin_getbuffs:
				if (st.hasMoreTokens())
				{
					L2PcInstance player = null;
					String playername = st.nextToken();
					st = null;
					
					player = L2World.getInstance().getPlayer(playername);
					
					if (player != null)
					{
						showBuffs(player, activeChar);
						playername = null;
						return true;
					}
					activeChar.sendMessage("The player " + playername + " is not online");
					playername = null;
					return false;
				}
				else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
				{
					showBuffs((L2PcInstance) activeChar.getTarget(), activeChar);
					return true;
				}
				else
					return true;
				
			case admin_stopbuff:
				if (st.hasMoreTokens())
				{
					String playername = st.nextToken();
					
					if (st.hasMoreTokens())
					{
						
						int SkillId = 0;
						
						try
						{
							SkillId = Integer.parseInt(st.nextToken());
							
						}
						catch (final NumberFormatException e)
						{
							
							activeChar.sendMessage("Usage: //stopbuff <playername> [skillId] (skillId must be a number)");
							return false;
							
						}
						
						if (SkillId > 0)
							removeBuff(activeChar, playername, SkillId);
						else
						{
							activeChar.sendMessage("Usage: //stopbuff <playername> [skillId] (skillId must be a number > 0)");
							return false;
						}
						
						st = null;
						playername = null;
						
						return true;
						
					}
					activeChar.sendMessage("Usage: //stopbuff <playername> [skillId]");
					return false;
				}
				activeChar.sendMessage("Usage: //stopbuff <playername> [skillId]");
				return false;
				
			case admin_stopallbuffs:
				
				if (st.hasMoreTokens())
				{
					String playername = st.nextToken();
					
					if (playername != null)
					{
						removeAllBuffs(activeChar, playername);
						playername = null;
						st = null;
						return true;
					}
					activeChar.sendMessage("Usage: //stopallbuffs <playername>");
					
					st = null;
					return false;
				}
				activeChar.sendMessage("Usage: //stopallbuffs <playername>");
				return false;
			case admin_areacancel:
				
				if (st.hasMoreTokens())
				{
					String val = st.nextToken();
					
					int radius = 0;
					
					try
					{
						radius = Integer.parseInt(val);
						
					}
					catch (final NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //areacancel <radius> (integer value > 0)");
						st = null;
						val = null;
						
						return false;
					}
					
					if (radius > 0)
					{
						for (final L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if (knownChar instanceof L2PcInstance && !knownChar.equals(activeChar))
							{
								knownChar.stopAllEffects();
							}
						}
						
						activeChar.sendMessage("All effects canceled within raidus " + radius);
						st = null;
						val = null;
						return true;
					}
					activeChar.sendMessage("Usage: //areacancel <radius> (integer value > 0)");
					st = null;
					val = null;
					return false;
					
				}
				activeChar.sendMessage("Usage: //areacancel <radius>");
				return false;
		}
		
		comm = null;
		st = null;
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public void showBuffs(final L2PcInstance player, final L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder();
		
		html.append("<html><center><font color=\"LEVEL\">Effects of " + player.getName() + "</font><center><br>");
		html.append("<table>");
		html.append("<tr><td width=200>Skill</td><td width=70>Action</td></tr>");
		
		L2Effect[] effects = player.getAllEffects();
		
		for (final L2Effect e : effects)
		{
			if (e != null)
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
	
	private void removeBuff(final L2PcInstance remover, final String playername, final int SkillId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(playername);
		
		if (player != null && SkillId > 0)
		{
			L2Effect[] effects = player.getAllEffects();
			
			for (final L2Effect e : effects)
			{
				if (e != null && e.getSkill().getId() == SkillId)
				{
					e.exit(true);
					remover.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + playername);
				}
			}
			showBuffs(player, remover);
			
			player = null;
			effects = null;
		}
	}
	
	private void removeAllBuffs(final L2PcInstance remover, final String playername)
	{
		final L2PcInstance player = L2World.getInstance().getPlayer(playername);
		
		if (player != null)
		{
			player.stopAllEffects();
			remover.sendMessage("Removed all effects from " + playername);
			showBuffs(player, remover);
		}
		else
		{
			remover.sendMessage("Can not remove effects from " + playername + ". Player appears offline.");
		}
		
	}
	
}
