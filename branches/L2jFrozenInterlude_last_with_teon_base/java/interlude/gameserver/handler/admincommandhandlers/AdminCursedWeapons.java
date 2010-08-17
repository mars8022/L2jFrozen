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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import interlude.Config;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.instancemanager.CursedWeaponsManager;
import interlude.gameserver.model.CursedWeapon;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - cw_info = displays cursed weapon status - cw_remove = removes a cursed
 * weapon from the world, item id or name must be provided - cw_add = adds a cursed weapon into the world, item id or
 * name must be provided. Target will be the weilder - cw_goto = teleports GM to the specified cursed weapon - cw_reload
 * = reloads instance manager
 * 
 * @version $Revision: 1.2 $
 * @author ProGramMoS
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_reload", "admin_cw_add", "admin_cw_info_menu"
	};

	private enum CommandEnum
	{
		admin_cw_info,
		admin_cw_remove,
		admin_cw_goto,
		admin_cw_reload,
		admin_cw_add,
		admin_cw_info_menu
	}
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!Config.ALT_PRIVILEGES_ADMIN) 
		{
			if (!checkLevel(activeChar.getAccessLevel())) 
			{
				return false;
			}
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

		CursedWeapon cw1 = null;
		CursedWeaponsManager cwm = null;

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		int id = 0;

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
			case admin_cw_info:
				cwm = CursedWeaponsManager.getInstance();
				activeChar.sendMessage("====== Cursed Weapons: ======");

				for(CursedWeapon cw : cwm.getCursedWeapons())
				{
					activeChar.sendMessage("> " + cw.getName() + " (" + cw.getItemId() + ")");

					if(cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
						activeChar.sendMessage("  Player holding: " + pl == null ? "null" : pl.getName());
						activeChar.sendMessage("    Player karma: " + cw.getPlayerKarma());
						activeChar.sendMessage("    Time Remaining: " + cw.getTimeLeft() / 60000 + " min.");
						activeChar.sendMessage("    Kills : " + cw.getNbKills());
						pl = null;
					}
					else if(cw.isDropped())
					{
						activeChar.sendMessage("  Lying on the ground.");
						activeChar.sendMessage("    Time Remaining: " + cw.getTimeLeft() / 60000 + " min.");
						activeChar.sendMessage("    Kills : " + cw.getNbKills());
					}
					else
					{
						activeChar.sendMessage("  Don't exist in the world.");
					}

					activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
				}

				cwm = null;
				break;

			case admin_cw_info_menu:
				cwm = CursedWeaponsManager.getInstance();
				TextBuilder replyMSG = new TextBuilder();
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				adminReply.setFile("data/html/admin/cwinfo.htm");

				for(CursedWeapon cw : cwm.getCursedWeapons())
				{
					int itemId = cw.getItemId();
					replyMSG.append("<table width=270><tr><td>Name:</td><td>" + cw.getName() + "</td></tr>");

					if(cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
						replyMSG.append("<tr><td>Weilder:</td><td>" + (pl == null ? "null" : pl.getName()) + "</td></tr>");
						replyMSG.append("<tr><td>Karma:</td><td>" + String.valueOf(cw.getPlayerKarma()) + "</td></tr>");
						replyMSG.append("<tr><td>Kills:</td><td>" + String.valueOf(cw.getPlayerPkKills()) + "/" + String.valueOf(cw.getNbKills()) + "</td></tr>");
						replyMSG.append("<tr><td>Time remaining:</td><td>" + String.valueOf(cw.getTimeLeft() / 60000) + " min.</td></tr>");
						replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
						replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
					}
					else if(cw.isDropped())
					{
						replyMSG.append("<tr><td>Position:</td><td>Lying on the ground</td></tr>");
						replyMSG.append("<tr><td>Time remaining:</td><td>" + String.valueOf(cw.getTimeLeft() / 60000) + " min.</td></tr>");
						replyMSG.append("<tr><td>Kills:</td><td>" + String.valueOf(cw.getNbKills()) + "</td></tr>");
						replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
						replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + String.valueOf(itemId) + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
					}
					else
					{
						replyMSG.append("<tr><td>Position:</td><td>Doesn't exist.</td></tr>");
						replyMSG.append("<tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add " + String.valueOf(itemId) + "\" width=99 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
					}

					replyMSG.append("</table>");
					replyMSG.append("<br>");
				}

				adminReply.replace("%cwinfo%", replyMSG.toString());
				activeChar.sendPacket(adminReply);

				replyMSG = null;
				adminReply = null;
				cwm = null;
				break;

			case admin_cw_reload:
				cwm = CursedWeaponsManager.getInstance();
				cwm.reload();
				cwm = null;
				break;

			case admin_cw_remove:
				cwm = CursedWeaponsManager.getInstance();

				try
				{
					String parameter = st.nextToken();

					if(parameter.matches("[0-9]*"))
					{
						id = Integer.parseInt(parameter);
					}
					else
					{
						parameter = parameter.replace('_', ' ');

						for(CursedWeapon cwp : cwm.getCursedWeapons())
						{
							if(cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
							{
								id = cwp.getItemId();
								break;
							}
						}
					}

					parameter = null;
					cw1 = cwm.getCursedWeapon(id);

					if(cw1 == null)
					{
						activeChar.sendMessage("Не найден ID.");
						return false;
					}
				}
				catch(Exception e)
				{}
				cw1.endOfLife();cw1 = null;
				cwm = null;
				break;
				
			case admin_cw_goto:
				cwm = CursedWeaponsManager.getInstance();

				try
				{
					String parameter = st.nextToken();

					if(parameter.matches("[0-9]*"))
					{
						id = Integer.parseInt(parameter);
					}
					else
					{
						parameter = parameter.replace('_', ' ');

						for(CursedWeapon cwp : cwm.getCursedWeapons())
						{
							if(cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
							{
								id = cwp.getItemId();
								break;
							}
						}
					}

					parameter = null;
					cw1 = cwm.getCursedWeapon(id);

					if(cw1 == null)
					{
						activeChar.sendMessage("Не найден ID.");
						return false;
					}
				}
				catch(Exception e)
				{
					//ignore
				}

				cw1.goTo(activeChar);

				cw1 = null;
				cwm = null;
				break;

			case admin_cw_add:
				cwm = CursedWeaponsManager.getInstance();

				try
				{
					String parameter = st.nextToken();

					if(parameter.matches("[0-9]*"))
					{
						id = Integer.parseInt(parameter);
					}
					else
					{
						parameter = parameter.replace('_', ' ');

						for(CursedWeapon cwp : cwm.getCursedWeapons())
						{
							if(cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
							{
								id = cwp.getItemId();
								break;
							}
						}
					}

					parameter = null;
					cw1 = cwm.getCursedWeapon(id);

					if(cw1 == null)
					{
						activeChar.sendMessage("Не найден ID.");
						return false;
					}
				}
				catch(Exception e)
				{
					//ignore
				}

				if(cw1 == null)
				{
					activeChar.sendMessage("Пример: //cw_add <ид или имя>");
					return false;
				}
				else if(cw1.isActive())
				{
					activeChar.sendMessage("Проклятое оружее активно.");
				}
				else
				{
					L2Object target = activeChar.getTarget();

					if(target != null && target instanceof L2PcInstance)
					{
						((L2PcInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					}
					else
					{
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
					}

					target = null;
				}

				cw1 = null;
				cwm = null;
				break;
		}

		st = null;
		cwm = null;

		return true;
	}
		private boolean checkLevel(int level)
		{
			return level >= REQUIRED_LEVEL;
		}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
