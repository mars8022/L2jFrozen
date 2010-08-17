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

import java.util.StringTokenizer;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.model.L2World;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.NpcSay;
import interlude.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2FolkInstance
{
	private boolean _currentTask = false;

	/**
	 * @param template
	 */
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equalsIgnoreCase("tele"))
		{
			int delay;
			if (!getTask())
			{
				if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0) {
					delay = 480000;
				} else {
					delay = 30000;
				}
				setTask(true);
				ThreadPoolManager.getInstance().scheduleGeneral(new oustAllPlayers(), delay);
			}
			String filename = "data/html/castleteleporter/MassGK-1.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			player.sendPacket(html);
			return;
		} else {
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename;
		if (!getTask())
		{
			if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0) {
				filename = "data/html/castleteleporter/MassGK-2.htm";
			} else {
				filename = "data/html/castleteleporter/MassGK.htm";
			}
		} else {
			filename = "data/html/castleteleporter/MassGK-1.htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}

	class oustAllPlayers implements Runnable
	{
		public void run()
		{
			try
			{
				NpcSay cs = new NpcSay(getObjectId(), 1, getNpcId(), "The defenders of " + getCastle().getName() + " castle will be teleported to the inner castle.");
				int region = MapRegionTable.getInstance().getMapRegion(getX(), getY());
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
					{
						player.sendPacket(cs);
					}
				}
				oustAllPlayers();
				setTask(false);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) {
			return;
		}
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showChatWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public boolean getTask()
	{
		return _currentTask;
	}

	public void setTask(boolean state)
	{
		_currentTask = state;
	}
}