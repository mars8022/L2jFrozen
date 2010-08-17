/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.handler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.util.FastMap;
import interlude.Config;
import interlude.gameserver.handler.admincommandhandlers.*;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());
	private FastMap<String, IAdminCommandHandler> _datatable;
	// Alt privileges setting
	private static Logger _priviLog = Logger.getLogger("AltPrivilegesAdmin");
	private static FastMap<String, Integer> _privileges;
	public static final String GROUP_MANAGESERVER = "ManageServer";

	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new FastMap<String, IAdminCommandHandler>();
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminAutoAnnouncements());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminEventEngine());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminBanChat());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminEditPrivs());
		registerAdminCommandHandler(new AdminFightCalculator());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminFort());
		registerAdminCommandHandler(new AdminClanHallSieges());
		registerAdminCommandHandler(new AdminPathNode());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminBBS());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminTest());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminMobGroup());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminUnblockIp());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminRideWyvern());
		registerAdminCommandHandler(new AdminReload());
		registerAdminCommandHandler(new AdminLogin());
		registerAdminCommandHandler(new AdminCache());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminQuest());
		registerAdminCommandHandler(new AdminZone());
		registerAdminCommandHandler(new AdminDonator());
		registerAdminCommandHandler(new AdminTvTEngine());
		registerAdminCommandHandler(new AdminCTFEngine());
		registerAdminCommandHandler(new AdminDMEngine());
        registerAdminCommandHandler(new AdminFortressSiegeEngine());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminGeoEditor());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMassRecall());
		_log.config("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids) {
			if (Config.DEBUG) {
				_log.fine("Adding handler for command " + id);
			}
			_datatable.put(id, handler);
		}
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		if (Config.DEBUG) {
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		}
		return _datatable.get(command);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	public final void setPrivilegeValue(L2PcInstance player, String adminCommand, int newValue)
	{
		// Only a GM can execute an admin command
		if (!player.isGM()) {
			return;
		}
		if (_privileges == null) {
			_privileges = new FastMap<String, Integer>();
		}
		if (_privileges.containsKey(adminCommand))
		{
			_privileges.remove(adminCommand);
		}
		_privileges.put(adminCommand, newValue);
	}

	public final boolean checkPrivileges(L2PcInstance player, String adminCommand)
	{
		// Only a GM can execute a admin command
		if (!player.isGM()) {
			return false;
		}
		// Skip special privileges handler?
		if (!Config.ALT_PRIVILEGES_ADMIN || Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
			return true;
		}
		if (_privileges == null) {
			_privileges = new FastMap<String, Integer>();
		}
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		// The command not exists
		if (!_datatable.containsKey(command)) {
			return false;
		}
		int requireLevel = 0;
		if (!_privileges.containsKey(command))
		{
			// Try to loaded the command config
			boolean isLoaded = false;
			try
			{
				Properties Settings = new Properties();
				InputStream is = new FileInputStream(Config.COMMAND_PRIVILEGES_FILE);
				Settings.load(is);
				is.close();
				String stringLevel = Settings.getProperty(command);
				if (stringLevel != null)
				{
					isLoaded = true;
					requireLevel = Integer.parseInt(stringLevel);
				}
			}
			catch (Exception e)
			{
			}
			// Secure level?
			if (!isLoaded)
			{
				if (Config.ALT_PRIVILEGES_SECURE_CHECK)
				{
					_priviLog.info("The command '" + command + "' haven't got a entry in the configuration file. The command cannot be executed!!");
					return false;
				}
				requireLevel = Config.ALT_PRIVILEGES_DEFAULT_LEVEL;
			}
			_privileges.put(command, requireLevel);
		}
		else
		{
			requireLevel = _privileges.get(command);
		}
		if (player.getAccessLevel() < requireLevel)
		{
			_priviLog.warning("<GM>" + player.getName() + ": have not access level to execute the command '" + command + "'");
			return false;
		}
		return true;
	}

	private final static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}
