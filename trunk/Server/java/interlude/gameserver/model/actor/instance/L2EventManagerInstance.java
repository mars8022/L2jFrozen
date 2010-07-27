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

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import interlude.Config;
import interlude.gameserver.model.entity.RaidEngine.L2EventChecks;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.ConfirmDlg;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This Class manages all the Requests to join a Raid Event.
 *
 * @author polbat02
 */
public class L2EventManagerInstance extends L2NpcInstance
{
	// Local Variables Definition
	// --------------------------
	/** Number of Current Events */
	public static int _currentEvents = 0;
	/** Players from which we're waiting an answer */
	public static Vector<L2PcInstance> _awaitingplayers = new Vector<L2PcInstance>();
	/** Players that will finally get inside the Event */
	public static Vector<L2PcInstance> _finalPlayers = new Vector<L2PcInstance>();

	public L2EventManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		_finalPlayers = new Vector<L2PcInstance>();
		if (actualCommand.equalsIgnoreCase("iEvent"))
		{
			try
			{
				/* Type:1- Single //2- Clan //3- Party */
				int type = Integer.parseInt(st.nextToken());
				/* Required Event Points needed to participate */
				int eventPoints = Integer.parseInt(st.nextToken());
				/* NpcId of the Event mobs */
				int npcId = Integer.parseInt(st.nextToken());
				/* Number of NPcs */
				int npcAm = Integer.parseInt(st.nextToken());
				/* Minimum number of needed persons players to participate */
				int minPeople = Integer.parseInt(st.nextToken());
				/* Minimum level to participate */
				int minLevel = Integer.parseInt(st.nextToken());
				/* Buff List to apply */
				int bufflist = Integer.parseInt(st.nextToken());
				/* Level of The Prize to Hand out */
				int prizeLevel = Integer.parseInt(st.nextToken());
				if (player == null)
				{
					return;
				}
				setTarget(player);
				if (_currentEvents >= Config.RAID_SYSTEM_MAX_EVENTS)
				{
					player.sendMessage("There's alredy " + _currentEvents + " events in progress. " + "Wait untill one of them ends to get into another one.");
					return;
				}
				if (L2EventChecks.usualChecks(player, minLevel)) {
					_finalPlayers.add(player);
				} else {
					return;
				}
				// If the player has passed the checks, then continue.
				switch (type)
				{
					// Case Clan Events.
					case 2:
					{
						if (player.getClan() == null)
						{
							player.sendMessage("You Don't have a Clan!");
							return;
						}
						L2PcInstance[] onlineclanMembers = player.getClan().getOnlineMembers("");
						for (L2PcInstance member : onlineclanMembers)
						{
							boolean eligible = true;
							if (member == null) {
								continue;
							}
							if (!L2EventChecks.usualChecks(member, minLevel)) {
								eligible = false;
							}
							if (eligible && !_finalPlayers.contains(member)) {
								_finalPlayers.add(member);
							}
						}
						if (_finalPlayers.size() > 1 && _finalPlayers.size() >= minPeople)
						{
							player.setRaidParameters(player, type, eventPoints, npcId, npcAm, minPeople, bufflist, prizeLevel, this, _finalPlayers);
							_awaitingplayers.add(player);
							ConfirmDlg dlg = new ConfirmDlg(614).addString(player.getName() + " A total of " + _finalPlayers.size() + " members of your " + " clan are Eligible for the event. Do you want to continue?");
							player.sendPacket(dlg);
							// player.sendPacket(new ConfirmDlg(614, " A total of " + _finalPlayers.size() + " members of your " + " clan are Eligible for the event. Do you want to continue?"));
						}
						else
						{
							String reason;
							if (_finalPlayers.size() > 1) {
								reason = ": Only 1 Clan Member Online.";
							} else if (_finalPlayers.size() < minPeople) {
								reason = ": Not enough members online to participate.";
							} else {
								reason = ".";
							}
							player.sendMessage("Cannot participate" + reason);
						}
						break;
					}
						// Case Party Events.
					case 3:
					{
						if (player.getParty() == null)
						{
							player.sendMessage("You DON'T have a Party!");
							return;
						}
						List<L2PcInstance> partyMembers = player.getParty().getPartyMembers();
						for (L2PcInstance member : partyMembers)
						{
							boolean eligible = true;
							if (member == null) {
								continue;
							}
							if (!L2EventChecks.usualChecks(member, minLevel)) {
								eligible = false;
							}
							if (eligible && !_finalPlayers.contains(member)) {
								_finalPlayers.add(member);
							}
						}
						if (_finalPlayers.size() > 1 && _finalPlayers.size() >= minPeople)
						{
							player.setRaidParameters(player, type, eventPoints, npcId, npcAm, minPeople, bufflist, prizeLevel, this, _finalPlayers);
							_awaitingplayers.add(player);
							ConfirmDlg dlg = new ConfirmDlg(614).addString(player.getName() + " A total of " + _finalPlayers.size() + " members of your " + "party are Eligible for the event. Do you want to continue?");
							player.sendPacket(dlg);
							// player.sendPacket(new ConfirmDlg(614, " A total of " + _finalPlayers.size() + " members of your " + "party are Eligible for the event. Do you want to continue?"));
						}
						else
						{
							String reason;
							if (_finalPlayers.size() > 1) {
								reason = ": Only 1 Party Member.";
							} else if (_finalPlayers.size() < minPeople) {
								reason = ": Not enough members to participate.";
							} else {
								reason = ".";
							}
							player.sendMessage("Cannot participate" + reason);
						}
						break;
					}
					default:
					{
						player.setRaidParameters(player, type, eventPoints, npcId, npcAm, minPeople, bufflist, prizeLevel, this, _finalPlayers);
						player.setRaidAnswear(1);
					}
				}
				return;
			}
			catch (Exception e)
			{
				_log.warning("L2EventManagerInstance: Error while getting html command");
				e.printStackTrace();
			}
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/mods/raidevent/" + pom + ".htm";
	}

	public static boolean addEvent()
	{
		if (_currentEvents >= Config.RAID_SYSTEM_MAX_EVENTS) {
			return false;
		} else
		{
			_currentEvents += 1;
			return true;
		}
	}

	public static boolean removeEvent()
	{
		if (_currentEvents > 0)
		{
			_currentEvents -= 1;
			return true;
		} else {
			return false;
		}
	}
}