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

import java.util.logging.Logger;

import javolution.util.FastMap;
import interlude.Config;
import interlude.gameserver.handler.usercommandhandlers.*;

public class UserCommandHandler
{
	private static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());
	private FastMap<Integer, IUserCommandHandler> _datatable;

	public static UserCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private UserCommandHandler()
	{
		_datatable = new FastMap<Integer, IUserCommandHandler>();
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new DisMount());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new Mount());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new Time());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new ChannelLeave());
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelListUpdate());
		_log.config("UserCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int id : ids) {
			if (Config.DEBUG) {
				_log.fine("Adding handler for user command " + id);
			}
			_datatable.put(Integer.valueOf(id), handler);
		}
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		if (Config.DEBUG) {
			_log.fine("getting handler for user command: " + userCommand);
		}
		return _datatable.get(Integer.valueOf(userCommand));
	}

	public int size()
	{
		return _datatable.size();
	}

	private final static class SingletonHolder
	{
		protected static final UserCommandHandler _instance = new UserCommandHandler();
	}
}
