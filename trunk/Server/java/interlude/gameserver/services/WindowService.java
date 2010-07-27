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
package interlude.gameserver.services;

import interlude.Config;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <FONT COLOR=#FF0000> WARNING: READ ONLY! </FONT> <br>
 * <br>
 * Only use this method to send a window, dont use for chatting or npc actions. <br>
 */
public class WindowService
{
	private final static Log _log = LogFactory.getLog(WindowService.class.getName());
	public static String _serviceName = ThreadService.class.getName().toString();

	/**
	 * method to send html only, replace dont work.
	 *
	 * @param target
	 * @param path
	 * @param filename
	 */
	public static void sendWindow(L2PcInstance target, String path, String filename)
	{
		String html = HtmCache.getInstance().getHtmForce(path + filename);
		NpcHtmlMessage reply = new NpcHtmlMessage(5);
		reply.setHtml(html);
		target.sendPacket(reply);
		// Send a Server->Client ActionFailed to the L2PcInstance in order to
		// avoid that the client wait another packet
		target.sendPacket(ActionFailed.STATIC_PACKET);
		if (Config.DEVELOPER) {
			_log.info("WindowService: Sending Window: " + filename + " for player: " + target.getName() + " in path: " + path);
		}
	}
}
