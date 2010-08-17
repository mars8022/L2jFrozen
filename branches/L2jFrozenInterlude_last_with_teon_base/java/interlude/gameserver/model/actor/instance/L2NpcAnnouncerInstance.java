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

import java.util.Calendar;

import interlude.Config;
import interlude.gameserver.Announcements;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.services.HtmlPathService;
import interlude.gameserver.services.WindowService;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * an Special Custom Instance to Make an Announcer Npc for Players :)
 *
 * @author Rayan for L2EmuProject date 20/08/07
 */
public class L2NpcAnnouncerInstance extends L2NpcInstance
{
	public L2NpcAnnouncerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		return HtmlPathService.ANNOUNCER_NPC_HTML_PATH + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// Get the distance between the L2PcInstance and the
		// L2NpcAnnouncerInstance
		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
		if (command.equalsIgnoreCase("request_announce"))
		{
			int playerlevel = player.getLevel();
			// prevents player with lower level than config to announce
			if (playerlevel < Config.NPC_ANNOUNCER_MIN_LVL_TO_ANNOUNCE)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
				// add
				// proper
				// code
				// soon
				// was
				// too
				// lazy
				// for
				// atm
				// :P
				String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "min_lvl.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%min_lvl_player%", String.valueOf(Config.NPC_ANNOUNCER_MIN_LVL_TO_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}
			// prevents player with higher level than config to announce
			if (playerlevel > Config.NPC_ANNOUNCER_MAX_LVL_TO_ANNOUNCE)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
				// add
				// proper
				// code
				// soon
				// was
				// too
				// lazy
				// for
				// atm
				// :P
				String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "max_lvl.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%max_lvl_player%", String.valueOf(Config.NPC_ANNOUNCER_MAX_LVL_TO_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}
			// checks if donator mode is active
			if (Config.NPC_ANNOUNCER_DONATOR_ONLY)
			{
				if (!player.isDonator() && !player.isGM())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
					// add
					// proper
					// code
					// soon
					// was
					// too
					// lazy
					// for
					// atm
					// :P
					String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "donator_only.htm";
					html.setFile(filename);
					if (filename != null)
					{
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%npcname%", String.valueOf(getName()));
						player.sendPacket(html);
						return;
					}
				}
			}
			// if player has already announce the count of max announces per
			// day
			if (player.getAnnounceCount() == Config.NPC_ANNOUNCER_MAX_ANNOUNCES_PER_DAY)
			{
				if (player.getLastAnnounceDate() == Calendar.DAY_OF_WEEK)// player
				// is
				// in
				// the
				// same
				// day
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
					// add
					// proper
					// code
					// soon
					// was
					// too
					// lazy
					// for
					// atm
					// :P
					String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "max_announce.htm";
					html.setFile(filename);
					if (filename != null)
					{
						html.replace("%max_announces_per_day%", String.valueOf(Config.NPC_ANNOUNCER_MAX_ANNOUNCES_PER_DAY));
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%npcname%", String.valueOf(getName()));
						player.sendPacket(html);
						return;
					}
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
			// add
			// proper
			// code
			// soon
			// was
			// too
			// lazy
			// for
			// atm
			// :P
			String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "announce.htm";
			html.setFile(filename);
			if (filename != null)
			{
				html.replace("%price_per_announce%", String.valueOf(Config.NPC_ANNOUNCER_PRICE_PER_ANNOUNCE));
				html.replace("%max_announce_per_day%", String.valueOf(Config.NPC_ANNOUNCER_MAX_ANNOUNCES_PER_DAY));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", String.valueOf(getName()));
				player.sendPacket(html);
			}
		}
		else if (command.equalsIgnoreCase("main_window"))
		{
			int npcID = getNpcId();
			WindowService.sendWindow(player, HtmlPathService.ANNOUNCER_NPC_HTML_PATH + npcID, ".htm");
		}
		else if (command.startsWith("make_announce") && Config.ALLOW_NPC_ANNOUNCER)
		{
			String playerName = player.getName();
			String msg = command.substring(14).toLowerCase();
			// checks adena
			if (!player.reduceAdena("NpcAnnouncer: announce", Config.NPC_ANNOUNCER_PRICE_PER_ANNOUNCE, player, true) && !player.isGM())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
				// add
				// proper
				// code
				// soon
				// was
				// too
				// lazy
				// for
				// atm
				// :P
				String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "missing_adena.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%price_per_announce%", String.valueOf(Config.NPC_ANNOUNCER_PRICE_PER_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}
			Announcements.getInstance().announceToAll(playerName + ": " + msg);
			player.increaseAnnounces();
			player.setLastAnnounceDate();
			// TODO: player.setDelayForNextAnnounce();
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());// will
			// add
			// proper
			// code
			// soon
			// was
			// too
			// lazy
			// for
			// atm
			// :P
			String filename = HtmlPathService.ANNOUNCER_NPC_HTML_PATH + "announce_complete.htm";
			html.setFile(filename);
			if (filename != null)
			{
				html.replace("%remaining_announces%", String.valueOf(player.getRemainingAnnounces()));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", String.valueOf(getName()));
				player.sendPacket(html);
			}
		}
		super.onBypassFeedback(player, command);
	}
}