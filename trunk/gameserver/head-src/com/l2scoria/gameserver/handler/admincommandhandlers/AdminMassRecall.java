package com.l2scoria.gameserver.handler.admincommandhandlers;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.datatables.sql.ClanTable;
import com.l2scoria.gameserver.handler.IAdminCommandHandler;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - recallparty - recallclan - recallally
 * 
 * @author Yamaneko
 */
public class AdminMassRecall implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
			"admin_recallclan", "admin_recallparty", "admin_recallally"
	};

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

		if(command.startsWith("admin_recallclan"))
		{
			try
			{
				String val = command.substring(17).trim();

				L2Clan clan = ClanTable.getInstance().getClanByName(val);

				if(clan == null)
				{
					activeChar.sendMessage("This clan doesn't exists.");
					return true;
				}

				val = null;
				L2PcInstance[] m = clan.getOnlineMembers("");

				for(L2PcInstance element : m)
				{
					Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}

				clan = null;
				m = null;
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Error in recallclan command.");
			}
		}
		else if(command.startsWith("admin_recallally"))
		{
			try
			{
				String val = command.substring(17).trim();
				L2Clan clan = ClanTable.getInstance().getClanByName(val);

				if(clan == null)
				{
					activeChar.sendMessage("This clan doesn't exists.");
					return true;
				}

				int ally = clan.getAllyId();

				if(ally == 0)
				{

					L2PcInstance[] m = clan.getOnlineMembers("");

					for(L2PcInstance element : m)
					{
						Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
					}

					m = null;
				}
				else
				{
					for(L2Clan aclan : ClanTable.getInstance().getClans())
					{
						if(aclan.getAllyId() == ally)
						{
							L2PcInstance[] m = aclan.getOnlineMembers("");

							for(L2PcInstance element : m)
							{
								Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
							}

							m = null;
						}
					}
				}

				clan = null;
				val = null;
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Error in recallally command.");
			}
		}
		else if(command.startsWith("admin_recallparty"))
		{
			try
			{
				String val = command.substring(18).trim();
				L2PcInstance player = L2World.getInstance().getPlayer(val);

				if(player == null)
				{
					activeChar.sendMessage("Target error.");
					return true;
				}

				if(!player.isInParty())
				{
					activeChar.sendMessage("Player is not in party.");
					return true;
				}

				L2Party p = player.getParty();

				for(L2PcInstance ppl : p.getPartyMembers())
				{
					Teleport(ppl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}

				p = null;
				val = null;
				player = null;

			}
			catch(Exception e)
			{
				activeChar.sendMessage("Error in recallparty command.");
			}
		}
		return true;
	}

	private void Teleport(L2PcInstance player, int X, int Y, int Z, String Message)
	{
		player.sendMessage(Message);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(X, Y, Z, true);
	}

	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}
