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

import interlude.Config;
import interlude.gameserver.handler.voicedcommandhandlers.*;
import javolution.util.FastMap;

import java.util.logging.Logger;

public class VoicedCommandHandler
{
	private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
	private FastMap<String, IVoicedCommandHandler> _datatable;

	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private VoicedCommandHandler()
	{
		_datatable = new FastMap<String, IVoicedCommandHandler>();
		registerVoicedCommandHandler(new stats());
		if (Config.ALLOW_WEDDING) {
			registerVoicedCommandHandler(new Wedding());
		}
		if (Config.BANKING_SYSTEM_ENABLED) {
			registerVoicedCommandHandler(new Banking());
		}
		if (Config.ONLINE_VOICE_COMMAND) {
			registerVoicedCommandHandler(new OnlinePlayers());
		}
		if (Config.ALLOW_TRADEOFF_VOICE_COMMAND) {
			registerVoicedCommandHandler(new tradeoff());
		}
        if (Config.ALLOW_INFO_VOICE_COMMAND) {
			registerVoicedCommandHandler(new info());
		}
		if (Config.ALLOW_CFG_VOICE_COMMAND) {
			registerVoicedCommandHandler(new cfg());
		}
        if (Config.RES) {
			registerVoicedCommandHandler(new Res());
		}
        if (Config.BuyRec) {
			registerVoicedCommandHandler(new BuyRec());
		}
        if (Config.Stat) {
			registerVoicedCommandHandler(new Stat());
		}
        if (Config.ALLOW_LOC) {
			registerVoicedCommandHandler(new loc());
		}
         if (Config.ALLOW_LOC_RB) {
			registerVoicedCommandHandler(new locrb());
		}
        if (Config.ALLOW_SHOP_COMMAND) {
			registerVoicedCommandHandler(new shop());
		}
        if (Config.ALLOW_CL) {
			registerVoicedCommandHandler(new cl());
		}
        if (Config.ALLOW_BUFF) {
			registerVoicedCommandHandler(new buff());
		}
		registerVoicedCommandHandler(new version());
		_log.config("VoicedCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String id : ids) {
			if (Config.DEBUG) {
				_log.fine("Adding handler for command " + id);
			}
			_datatable.put(id, handler);
		}
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG) {
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		}
		return _datatable.get(command);
	}

	public int size()
	{
		return _datatable.size();
	}

	private final static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}
