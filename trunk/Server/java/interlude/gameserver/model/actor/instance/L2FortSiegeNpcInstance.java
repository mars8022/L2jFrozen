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
package interlude.gameserver.model.actor.instance;

import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * @author Vice [L2JOneo]
 */
public class L2FortSiegeNpcInstance extends L2FolkInstance
{
	// private static Logger _log =
	// Logger.getLogger(L2SiegeNpcInstance.class.getName());
	public L2FortSiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null) {
			return;
		}
		super.onBypassFeedback(player, command);
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), -15));
		if (isInsideRadius(player, INTERACTION_DISTANCE, false, false)) {
			showSiegeInfoWindow(player);
		}
	}

	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 *
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if (validateCondition(player)) {
			getFort().getSiege().listRegisterClan(player);
		} else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/fortsiege/" + getTemplate().npcId + "-busy.htm");
			html.replace("%fortname%", getFort().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private boolean validateCondition(L2PcInstance player)
	{
		if (getFort().getSiege().getIsInProgress()) {
			return false; // Busy because of siege
		}
		return true;
	}
}