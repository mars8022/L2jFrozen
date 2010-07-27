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
import interlude.gameserver.ai.L2CharacterAI;
import interlude.gameserver.ai.L2SiegeGuardAI;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.instancemanager.FortSiegeManager;
import interlude.gameserver.instancemanager.SiegeManager;
import interlude.gameserver.model.L2Attackable;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2SiegeClan;
import interlude.gameserver.model.actor.knownlist.SiegeGuardKnownList;
import interlude.gameserver.model.entity.FortSiege;
import interlude.gameserver.model.entity.Siege;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * This class represents all guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK's or custom interactions.
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2SiegeGuardInstance extends L2Attackable
{
		public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
		{
			super(objectId, template);
			getKnownList(); // inits the knownlist
		}

		@Override
		public final SiegeGuardKnownList getKnownList()
		{
			if(!(super.getKnownList() instanceof SiegeGuardKnownList))
				setKnownList(new SiegeGuardKnownList(this));
			return (SiegeGuardKnownList)super.getKnownList();
		}

		@Override
		public L2CharacterAI getAI()
		{
			L2CharacterAI ai = _ai;
			if (ai == null)
			{
				synchronized(this)
				{
					if (_ai == null)
						_ai = new L2SiegeGuardAI(new AIAccessor());
					return _ai;
				}
			}
			return ai;
		}

		/**
		 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.
		 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
		 */
		@Override
		public boolean isAutoAttackable(L2Character attacker)
		{
			// Summons and traps are attackable, too
			L2PcInstance player = attacker.getActingPlayer();
			if (player == null)
				return false;
			if (player.getClan() == null)
				return true;

			boolean isCastle = (getCastle() != null 
					&& getCastle().getSiege().getIsInProgress() 
					&& !getCastle().getSiege().checkIsDefender(player.getClan()));
			return isCastle;
		}

		@Override
		public boolean hasRandomAnimation()
		{
			return false;
		}

		/**
		 * This method forces guard to return to home location previously set
		 */
		@Override
		public void returnHome()
		{
			if (getStat().getWalkSpeed() <= 0)
				return;
			
			if (getSpawn() != null && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
			{
				setisReturningToSpawnPoint(true);
				clearAggroList();
				if (hasAI())
					getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
		}

		/**
		 * Custom onAction behaviour. Note that super() is not called because guards need
		 * extra check to see if a player should interact or ATTACK them when clicked.
		 */
		@Override
		public void onAction(L2PcInstance player)
		{
			if (!canTarget(player))
				return;

				boolean opp = false;
				Siege siege = SiegeManager.getInstance().getSiege(player);
				FortSiege fortSiege = FortSiegeManager.getInstance().getSiege(player);
				L2Clan oppClan = player.getClan();
				//Castle Sieges
				if (siege != null && siege.getIsInProgress() && oppClan != null)
				{
					for (L2SiegeClan clan : siege.getAttackerClans())
					{
						L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

						if (cl == oppClan || cl.getAllyId() == player.getAllyId())
						{
							opp = true;
							break;
						}
					}
					for (L2SiegeClan clan : siege.getDefenderClans())
					{
						L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

						if (cl == oppClan || cl.getAllyId() == player.getAllyId())
						{
							opp = true;
							break;
						}
					}
				}
				//Fort Sieges
				else if (fortSiege != null && fortSiege.getIsInProgress() && oppClan != null)
				{
					for (L2SiegeClan clan : fortSiege.getAttackerClans())
					{
						L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

						if (cl == oppClan || cl.getAllyId() == player.getAllyId())
						{
							opp = true;
							break;
						}
					}
				}
				if (!opp)
					return;

			// Check if the L2PcInstance already target the L2NpcInstance
			if (this != player.getTarget())
			{
				// Set the target of the L2PcInstance player
				player.setTarget(this);

				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);

				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);

				// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
				player.sendPacket(new ValidateLocation(this));
			}
			else
			{
				boolean AutoAT = isAutoAttackable(player);
				if (AutoAT && !isAlikeDead())
				{
					if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					else
						player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				if (!AutoAT)
				{
					if (!canInteract(player))
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
					else
						showChatWindow(player, 0);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}

		@Override
		public void addDamageHate(L2Character attacker, int damage, int aggro)
		{
			if (attacker == null)
				return;

			if (!(attacker instanceof L2SiegeGuardInstance))
				super.addDamageHate(attacker, damage, aggro);
		}
}
