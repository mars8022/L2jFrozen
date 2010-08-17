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

import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.Ride;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{
	protected static final int COND_CLAN_OWNER = 3;
	private int _clanHallId = -1;

	public L2WyvernManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("RideWyvern"))
		{
			if (!player.isClanLeader())
			{
				player.sendMessage("Only clan leaders are allowed.");
				return;
			}
			if (player.getPet() == null)
			{
				if (player.isMounted())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Already Have a Pet or Mounted.");
					player.sendPacket(sm);
					return;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Summon your Strider.");
					player.sendPacket(sm);
					return;
				}
			}
			else if (player.getPet().getNpcId() == 12526 || player.getPet().getNpcId() == 12527 || player.getPet().getNpcId() == 12528)
			{
				if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
				{
					if (player.getPet().getLevel() < 55)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("Your Strider don't reach the required level.");
						player.sendPacket(sm);
						return;
					}
					else
					{
						if (!player.disarmWeapons()) return;

						player.getPet().unSummon(player);
						player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
						Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
						player.sendPacket(mount);
						player.broadcastPacket(mount);
						player.setMountType(mount.getMountType());
						player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("The Wyvern has been summoned successfully!");
						player.sendPacket(sm);
						return;
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("You need 10 Crystals: B Grade.");
					player.sendPacket(sm);
					return;
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Unsummon your pet.");
				player.sendPacket(sm);
				return;
			}
		} else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
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
				showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
		if (getClanHall() != null)
			filename = "data/html/wyvernmanager/wyvernmanager-clan-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_OWNER)
				// Owner message window
				filename = "data/html/wyvernmanager/wyvernmanager.htm";
			else if (condition == COND_CLAN_OWNER)
				filename = "data/html/wyvernmanager/wyvernmanager-clan.htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	/** Return the L2ClanHall this L2NpcInstance belongs to. */
	public final ClanHall getClanHall()
	{
		if (_clanHallId < 0)
		{
			ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
			if (temp != null)
				_clanHallId = temp.getId();

			if (_clanHallId < 0)
				return null;
		}
		return ClanHallManager.getInstance().getClanHallById(_clanHallId);
	}

	@Override
	protected int validateCondition(L2PcInstance player)
	{
		if (getClanHall() != null && player.getClan() != null)
		{
			ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (_clanHallId == 34 && DevastatedCastleManager.getInstance().getIsInProgress() 
					|| _clanHallId == 64 && FortressofTheDeadManager.getInstance().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			else if (getClanHall().getOwnerId() == player.getClanId() && player.isClanLeader())
				return COND_CLAN_OWNER; // Owner of the clanhall
		}
		else if (super.getCastle() != null && super.getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (super.getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if (super.getCastle().getOwnerId() == player.getClanId() // Clan owns castle
						&& player.isClanLeader())
					return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
