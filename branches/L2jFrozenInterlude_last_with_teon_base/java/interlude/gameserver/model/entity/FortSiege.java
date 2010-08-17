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
package interlude.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;
import interlude.L2DatabaseFactory;
import interlude.gameserver.Announcements;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.FortSiegeManager;
import interlude.gameserver.instancemanager.MercTicketManager;
import interlude.gameserver.instancemanager.SiegeGuardManager;
import interlude.gameserver.instancemanager.FortSiegeManager.SiegeSpawn;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2SiegeClan;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2SiegeClan.SiegeClanType;
import interlude.gameserver.model.actor.instance.L2ArtefactInstance;
import interlude.gameserver.model.actor.instance.L2ControlTowerInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.FortSiegeInfo;
import interlude.gameserver.network.serverpackets.RelationChanged;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * @author Vice [L2JOneo]
 */
public class FortSiege
{
	public static enum TeleportWhoType
	{
		All, Attacker, DefenderNotOwner, Owner, Spectator
	}

	// ===============================================================
	// Schedule task
	public class ScheduleEndSiegeTask implements Runnable
	{
		private Fort _fortInst;

		public ScheduleEndSiegeTask(Fort pFort)
		{
			_fortInst = pFort;
		}

		public void run()
		{
			if (!getIsInProgress())
			{
				return;
			}
			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 3600000); // Prepare
					// task
					// for
					// 1 hr
					// left.
				}
				else if (timeRemaining <= 3600000 && timeRemaining > 600000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 600000); // Prepare
					// task
					// for
					// 10 minute left.
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 300000); // Prepare
					// task
					// for 5 minute
					// left.
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 10000); // Prepare
					// task
					// for
					// 10
					// seconds
					// count
					// down
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getFort().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) left!", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_fortInst), timeRemaining); // Prepare
					// task for
					// second
					// count
					// down
				}
				else
				{
					_fortInst.getSiege().endSiege();
				}
			}
			catch (Throwable t)
			{
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		private Fort _fortInst;

		public ScheduleStartSiegeTask(Fort pFort)
		{
			_fortInst = pFort;
		}

		public void run()
		{
			if (getIsInProgress())
			{
				return;
			}
			if (!getIsScheduled())
			{
				return;
			}
			try
			{
				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 86400000); // Prepare
					// task
					// for
					// 24
					// before siege start to
					// end registration
				}
				else if (timeRemaining <= 86400000 && timeRemaining > 13600000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 13600000); // Prepare
					// task
					// for
					// 1 hr left before
					// siege start.
				}
				else if (timeRemaining <= 13600000 && timeRemaining > 600000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 600000); // Prepare
					// task
					// for 10 minute
					// left.
				}
				else if (timeRemaining <= 600000 && timeRemaining > 300000)
				{
					announceToPlayer("The registration term for " + getFort().getName() + " has ended.", false);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 300000); // Prepare
					// task
					// for
					// 5
					// minute
					// left.
				}
				else if (timeRemaining <= 300000 && timeRemaining > 10000)
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 10000); // Prepare
					// task
					// for
					// 10
					// seconds
					// count
					// down
				}
				else if (timeRemaining <= 10000 && timeRemaining > 0)
				{
					announceToPlayer(getFort().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) to start!", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_fortInst), timeRemaining); // Prepare
					// task
					// for
					// second
					// count
					// down
				}
				else
				{
					_fortInst.getSiege().startSiege();
				}
			}
			catch (Throwable t)
			{
			}
		}
	}

	// =========================================================
	// Data Field
	// Attacker and Defender
	private List<L2SiegeClan> _attackerClans = new FastList<L2SiegeClan>(); // L2SiegeClan
	private List<L2SiegeClan> _defenderClans = new FastList<L2SiegeClan>(); // L2SiegeClan
	private List<L2SiegeClan> _defenderWaitingClans = new FastList<L2SiegeClan>(); // L2SiegeClan
	private int _defenderRespawnDelayPenalty;
	// Fort setting
	private List<L2ArtefactInstance> _artifacts = new FastList<L2ArtefactInstance>();
	private List<L2ControlTowerInstance> _controlTowers = new FastList<L2ControlTowerInstance>();
	private Fort[] _fort;
	private boolean _isInProgress = false;
	private boolean _isScheduled = false;
	private boolean _isNormalSide = true; // true = Atk is Atk, false =
	// Atk is Def
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	private SiegeGuardManager _siegeGuardManager;
	protected Calendar _siegeRegistrationEndDate;
	private boolean _hasCastle = false;

	// =========================================================
	// Constructor
	public FortSiege(Fort[] fort)
	{
		_fort = fort;
		// ***_siegeGuardManager = new SiegeGuardManager(getFort());
		checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
	}

	public void setHasCastle()
	{
		_hasCastle = true;
	}

	// =========================================================
	// Siege phases
	/**
	 * When siege ends<BR>
	 * <BR>
	 */
	public void endSiege()
	{
		if (getIsInProgress())
		{
			announceToPlayer("The siege of " + getFort().getName() + " has finished!", false);
			if (getFort().getOwnerId() <= 0 && !_hasCastle)
			{
				announceToPlayer("The siege of " + getFort().getName() + " has ended in a draw.", false);
			}
			removeFlags(); // Removes all flags. Note: Remove flag before
			// teleporting players
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town); // Teleport
			// to
			// the
			// second closest
			// town
			teleportPlayer(FortSiege.TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.Town); // Teleport
			// to
			// the
			// second closest
			// town
			teleportPlayer(FortSiege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport
			// to
			// the
			// second closest
			// town
			_isInProgress = false; // Flag so that siege instance can be started
			_isScheduled = false;
			updatePlayerSiegeStateFlags(true);
			saveFortSiege(); // Save fort specific data
			clearSiegeClan(); // Clear siege clan from db
			removeArtifact(); // Remove artifact from this fort
			removeControlTower(); // Remove all control tower from this fort
			// Remove all spawned siege guard from this fort
			// ***_siegeGuardManager.unspawnSiegeGuard();
			if (getFort().getOwnerId() > 0)
			{
				// ***_siegeGuardManager.removeMercs();
			}
			getFort().spawnDoor(); // Respawn door to fort
			getFort().getZone().updateZoneStatusForCharactersInside();
			FortSiegeManager.getInstance().removeSiege(this);
		}
		else if (getIsScheduled())
		{
			saveFortSiege(); // Save fort specific data
			clearSiegeClan(); // Clear siege clan from db
			announceToPlayer("The schedule for siege of " + getFort().getName() + " has be cancelled!", false);
		}
	}

	private void removeDefender(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}

	private void removeAttacker(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}

	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
		{
			return;
		}
		sc.setType(type);
		getDefenderClans().add(sc);
	}

	private void addAttacker(L2SiegeClan sc)
	{
		if (sc == null)
		{
			return;
		}
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}

	/**
	 * When control of fort changed during siege<BR>
	 * <BR>
	 */
	public void midVictory()
	{
		if (getIsInProgress()) // Siege still in progress
		{
			if (getFort().getOwnerId() > 0)
			{
				// Remove all merc entry from db
				// _siegeGuardManager.removeMercs();
			}
			// If defender doesn't exist (Pc vs Npc)
			if (getDefenderClans().size() == 0 && getAttackerClans().size() == 1)
			{
				L2SiegeClan sc_newowner = getAttackerClan(getFort().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if (getFort().getOwnerId() > 0)
			{
				int allyId = ClanTable.getInstance().getClan(getFort().getOwnerId()).getAllyId();
				if (getDefenderClans().size() == 0) // If defender doesn't
				// exist
				// (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans())
						{
							if (sc != null)
							{
								if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
								{
									allinsamealliance = false;
								}
							}
						}
						if (allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getFort().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}
				}
				for (L2SiegeClan sc : getDefenderClans())
				{
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				L2SiegeClan sc_newowner = getAttackerClan(getFort().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				// The player's clan is in an alliance
				if (allyId != 0)
				{
					L2Clan[] clanList = ClanTable.getInstance().getClans();
					for (L2Clan clan : clanList)
					{
						if (clan.getAllyId() == allyId)
						{
							L2SiegeClan sc = getAttackerClan(clan.getClanId());
							if (sc != null)
							{
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
						}
					}
				}
				teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.SiegeFlag); // Teleport
				// to
				// the
				// second
				// closest
				// town
				teleportPlayer(FortSiege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport
				// to
				// the second
				// closest town
				removeDefenderFlags(); // Removes defenders' flags
				getFort().removeUpgrade(); // Remove all fort upgrade
				getFort().spawnDoor(true); // Respawn door to fort but
				// make them weaker (50% hp)
				updatePlayerSiegeStateFlags(false);
			}
		}
	}

	/**
	 * When siege starts<BR>
	 * <BR>
	 */
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getAttackerClans().size() <= 0)
			{
				SystemMessage sm;
				if (getFort().getOwnerId() <= 0)
				{
					sm = new SystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
				}
				sm.addString(getFort().getName());
				Announcements.getInstance().announceToAll(sm);
				return;
			}
			// Atk is now atk
			_isNormalSide = true;
			// Flag so that same siege instance cannot be started again
			_isInProgress = true;
			_isScheduled = false;
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town); // Teleport
			// to
			// the
			// closest town
			// teleportPlayer(Siege.TeleportWhoType.Spectator,
			// MapRegionTable.TeleportWhereType.Town); // Teleport to the
			// second
			// closest town
			spawnArtifact(getFort().getFortId()); // Spawn artifact
			spawnControlTower(getFort().getFortId()); // Spawn control
			// tower
			getFort().spawnDoor(); // Spawn door
			// ***spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getFort().getFortId()); // remove
			// the
			// tickets
			// from
			// the
			// ground
			_defenderRespawnDelayPenalty = 0; // Reset respawn delay
			getFort().getZone().updateZoneStatusForCharactersInside();
			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getFort()), 1000); // Prepare
			// auto end
			// task
			announceToPlayer("The siege of " + getFort().getName() + " has started!", false);
			saveFortSiege();
			FortSiegeManager.getInstance().addSiege(this);
		}
	}

	// =========================================================
	// Method - Public
	/**
	 * Announce to player.<BR>
	 * <BR>
	 *
	 * @param message
	 *            The String of the message to send to player
	 * @param inAreaOnly
	 *            The boolean flag to show message to players in area only.
	 */
	public void announceToPlayer(String message, boolean inAreaOnly)
	{
		if (inAreaOnly)
		{
			getFort().getZone().announceToPlayers(message);
			return;
		}
		// Get all players
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendMessage(message);
		}
	}

	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(""))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 1);
				}
				member.sendPacket(new UserInfo(member));
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(""))
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 2);
				}
				member.sendPacket(new UserInfo(member));
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
	}

	/**
	 * Approve clan as defender for siege<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of player's clan id
	 */
	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), 0, true);
		loadSiegeClan();
	}

	/** Return true if object is inside the zone */
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}

	/** Return true if object is inside the zone */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getIsInProgress() && getFort().checkIfInZone(x, y, z); // Fort
		// zone
		// during
		// siege
	}

	/**
	 * Return true if clan is attacker<BR>
	 * <BR>
	 *
	 * @param clan
	 *            The L2Clan of the player
	 */
	public boolean checkIsAttacker(L2Clan clan)
	{
		return getAttackerClan(clan) != null;
	}

	/**
	 * Return true if clan is defender<BR>
	 * <BR>
	 *
	 * @param clan
	 *            The L2Clan of the player
	 */
	public boolean checkIsDefender(L2Clan clan)
	{
		return getDefenderClan(clan) != null;
	}

	/**
	 * Return true if clan is defender waiting approval<BR>
	 * <BR>
	 *
	 * @param clan
	 *            The L2Clan of the player
	 */
	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}

	/** Clear all registered siege clans from database for fort */
	public void clearSiegeClan()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();
			if (getFort().getOwnerId() > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
				statement2.setInt(1, getFort().getOwnerId());
				statement2.execute();
				statement2.close();
			}
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			System.out.println("Exception: clearSiegeClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/** Clear all siege clans waiting for approval from database for fort */
	public void clearSiegeWaitingClan()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and type = 2");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			System.out.println("Exception: clearSiegeWaitingClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/** Return list of L2PcInstance registered as attacker in the zone. */
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new FastList<L2PcInstance>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		return players;
	}

	/**
	 * Return list of L2PcInstance registered as defender but not owner in the zone.
	 */
	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<L2PcInstance>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() == getFort().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		return players;
	}

	/** Return list of L2PcInstance in the zone. */
	public List<L2PcInstance> getPlayersInZone()
	{
		return getFort().getZone().getAllPlayers();
	}

	/** Return list of L2PcInstance owning the fort in the zone. */
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<L2PcInstance>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() != getFort().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(""))
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		return players;
	}

	/**
	 * Return list of L2PcInstance not registered as attacker or defender in the zone.
	 */
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new FastList<L2PcInstance>();
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			// quick check from player states, which don't include siege
			// number
			// however
			if (!player.isInsideZone(L2Character.ZONE_SIEGE) || player.getSiegeState() != 0)
			{
				continue;
			}
			if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				players.add(player);
			}
		}
		return players;
	}

	/** Control Tower was skilled */
	public void killedCT(L2NpcInstance ct)
	{
		_defenderRespawnDelayPenalty += FortSiegeManager.getInstance().getControlTowerLosePenalty(); // Add
		// respawn
		// penalty
		// to
		// defenders for each control
		// tower lose
	}

	/** Remove the flag that was killed */
	public void killedFlag(L2NpcInstance flag)
	{
		if (flag == null)
		{
			return;
		}
		for (int i = 0; i < getAttackerClans().size(); i++)
		{
			if (getAttackerClan(i).removeFlag(flag))
			{
				return;
			}
		}
	}

	/** Display list of registered clans */
	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new FortSiegeInfo(getFort()));
	}

	/**
	 * Register clan as attacker<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance of the player trying to register
	 */
	public void registerAttacker(L2PcInstance player)
	{
		registerAttacker(player, false);
	}

	public void registerAttacker(L2PcInstance player, boolean force)
	{
		if (player.getClan() == null)
		{
			return;
		}
		int allyId = 0;
		if (getFort().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getFort().getOwnerId()).getAllyId();
		}
		if (allyId != 0)
		{
			if (player.getClan().getAllyId() == allyId && !force)
			{
				player.sendMessage("You cannot register as an attacker because your alliance owns the fort");
				return;
			}
		}
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 1, false); // Save to database
			// if the first registering we start the timer
			if (getAttackerClans().size() == 1) {
				startAutoTask();
			}
		}
	}

	/**
	 * Register clan as defender<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance of the player trying to register
	 */
	public void registerDefender(L2PcInstance player)
	{
		registerDefender(player, false);
	}

	public void registerDefender(L2PcInstance player, boolean force)
	{
		if (getFort().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getFort().getName() + " is owned by NPC.");
		}
		else if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 2, false); // Save to database
		}
	}

	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and clan_id=?");
			statement.setInt(1, getFort().getFortId());
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
			loadSiegeClan();
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of player's clan id
	 */
	public void removeAllSiegeClan()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? ");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();
			loadSiegeClan();
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(L2Clan clan)
	{
		if (clan == null || clan.getHasFort() == getFort().getFortId() || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))
		{
			return;
		}
		removeSiegeClan(clan.getClanId());
	}

	/**
	 * Remove clan from siege<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance of player/clan being removed
	 */
	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}

	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 */
	public void startAutoTask()
	{
		setSiegeDateTime();
		System.out.println("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
		setIsScheduled(true);
		loadSiegeClan();
		// Schedule registration end
		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(getFort().getSiegeDate().getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.MINUTE, -10);
		// Schedule siege auto start
		ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(getFort()), 1000);
	}

	/**
	 * Start the auto tasks<BR>
	 * <BR>
	 */
	public void checkAutoTask()
	{
		if (getFort().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			clearSiegeDate();
			saveSiegeDate();
			removeAllSiegeClan();
			return;
		}
		System.out.println("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
		setIsScheduled(true);
		loadSiegeClan();
		// Schedule registration end
		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(getFort().getSiegeDate().getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.MINUTE, -10);
		// Schedule siege auto start
		ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(getFort()), 1000);
	}

	/**
	 * Teleport players
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			case DefenderNotOwner:
				players = getDefendersButNotOwnersInZone();
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		;
		for (L2PcInstance player : players)
		{
			if (player.isGM() || player.isInJail())
			{
				continue;
			}
			player.teleToLocation(teleportWhere);
		}
	}

	// =========================================================
	// Method - Private
	/**
	 * Add clan as attacker<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add
		// registered
		// attacker
		// to
		// attacker
		// list
	}

	/**
	 * Add clan as defender<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of clan's id
	 */
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add
		// registered
		// defender
		// to
		// defender
		// list
	}

	/**
	 * <p>
	 * Add clan as defender with the specified type
	 * </p>
	 *
	 * @param clanId
	 *            The int of clan's id
	 * @param type
	 *            the type of the clan
	 */
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}

	/**
	 * Add clan as defender waiting approval<BR>
	 * <BR>
	 *
	 * @param clanId
	 *            The int of clan's id
	 */
	private void addDefenderWaiting(int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add
		// registered
		// defender
		// to
		// defender
		// list
	}

	/**
	 * Return true if the player can register.<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance of the player trying to register
	 */
	private boolean checkIfCanRegister(L2PcInstance player)
	{
		if (getIsRegistrationOver())
		{
			player.sendMessage("The deadline to register for the siege of " + getFort().getName() + " has passed.");
		}
		else if (getIsInProgress())
		{
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		}
		else if (player.getClan() == null || player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel())
		{
			player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fort siege.");
		}
		else if (player.getClan().getHasFort() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a fort.");
		}
		else if (player.getClan().getClanId() == getFort().getOwnerId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
		}
		else if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), getFort().getFortId()))
		{
			player.sendMessage("You are already registered in a Siege.");
		}
		else
		{
			return true;
		}
		return false;
	}

	/**
	 * Return the correct siege date as Calendar.<BR>
	 * <BR>
	 *
	 * @param siegeDate
	 *            The Calendar siege date and time
	 */
	private void setSiegeDateTime()
	{
		Calendar newDate = Calendar.getInstance();
		newDate.add(Calendar.MINUTE, 60);
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}

	/** Load siege clans. */
	private void loadSiegeClan()
	{
		java.sql.Connection con = null;
		try
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			// Add fort owner as defender (add owner first so that they
			// are on
			// the top of the defender list)
			if (getFort().getOwnerId() > 0)
			{
				addDefender(getFort().getOwnerId(), SiegeClanType.OWNER);
			}
			PreparedStatement statement = null;
			ResultSet rs = null;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id,type FROM fortsiege_clans where fort_id=?");
			statement.setInt(1, getFort().getFortId());
			rs = statement.executeQuery();
			int typeId;
			while (rs.next())
			{
				typeId = rs.getInt("type");
				if (typeId == 0)
				{
					addDefender(rs.getInt("clan_id"));
				}
				else if (typeId == 1)
				{
					addAttacker(rs.getInt("clan_id"));
				}
				else if (typeId == 2)
				{
					addDefenderWaiting(rs.getInt("clan_id"));
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception: loadSiegeClan(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/** Remove artifacts spawned. */
	private void removeArtifact()
	{
		if (_artifacts != null)
		{
			// Remove all instance of artifact for this fort
			for (L2ArtefactInstance art : _artifacts)
			{
				if (art != null)
				{
					art.decayMe();
				}
			}
			_artifacts = null;
		}
	}

	/** Remove all control tower spawned. */
	private void removeControlTower()
	{
		if (_controlTowers != null)
		{
			// Remove all instance of control tower for this fort
			for (L2ControlTowerInstance ct : _controlTowers)
			{
				if (ct != null)
				{
					ct.decayMe();
				}
			}
			_controlTowers = null;
		}
	}

	/** Remove all flags. */
	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}

	/** Remove flags from defenders. */
	private void removeDefenderFlags()
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}

	/** Save fort siege related to database. */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		saveSiegeDate(); // Save the date
		setIsScheduled(false);
		// startAutoTask(); // Prepare auto start siege and end registration
	}

	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update fort set siegeDate = ? where id = ?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setInt(2, getFort().getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception: saveSiegeDate(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Save registration to database.<BR>
	 * <BR>
	 *
	 * @param clan
	 *            The L2Clan of player
	 * @param typeId
	 *            -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 */
	private void saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration)
	{
		if (clan.getHasFort() > 0)
		{
			return;
		}
		java.sql.Connection con = null;
		try
		{
			if (typeId == 0 || typeId == 2 || typeId == -1)
			{
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= FortSiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}
			}
			else
			{
				if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
				{
					return;
				}
			}
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			if (!isUpdateRegistration)
			{
				statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id,type,fort_owner) values (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, typeId);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("Update fortsiege_clans set type = ? where fort_id = ? and clan_id = ?");
				statement.setInt(1, typeId);
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
				statement.close();
			}
			if (typeId == 0 || typeId == -1)
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getFort().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getFort().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getFort().getName(), false);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/** Set the date for the next siege. */
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
		_isRegistrationOver = false; // Allow registration for next siege
	}

	/** Spawn artifact. */
	private void spawnArtifact(int Id)
	{
		// Set artefact array size if one does not exist
		if (_artifacts == null)
		{
			_artifacts = new FastList<L2ArtefactInstance>();
		}
		for (SiegeSpawn _sp : FortSiegeManager.getInstance().getArtefactSpawnList(Id))
		{
			L2ArtefactInstance art;
			art = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
			art.setCurrentHpMp(art.getMaxHp(), art.getMaxMp());
			art.setHeading(_sp.getLocation().getHeading());
			art.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);
			_artifacts.add(art);
		}
	}

	/** Spawn control tower. */
	private void spawnControlTower(int Id)
	{
		// Set control tower array size if one does not exist
		if (_controlTowers == null)
		{
			_controlTowers = new FastList<L2ControlTowerInstance>();
		}
		for (SiegeSpawn _sp : FortSiegeManager.getInstance().getControlTowerSpawnList(Id))
		{
			L2ControlTowerInstance ct;
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());
			template.getStatsSet().set("baseHpMax", _sp.getHp());
			// TODO: Check/confirm if control towers have any special weapon
			// resistances/vulnerabilities
			// template.addVulnerability(Stats.BOW_WPN_VULN,0);
			// template.addVulnerability(Stats.BLUNT_WPN_VULN,0);
			// template.addVulnerability(Stats.DAGGER_WPN_VULN,0);
			ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
			ct.setCurrentHpMp(ct.getMaxHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
			_controlTowers.add(ct);
		}
	}

	/**
	 * Spawn siege guard.<BR>
	 * <BR>
	 */
	@SuppressWarnings("unused")
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
		// Register guard to the closest Control Tower
		// When CT dies, so do all the guards that it controls
		if (getSiegeGuardManager().getSiegeGuardSpawn().size() > 0 && _controlTowers.size() > 0)
		{
			L2ControlTowerInstance closestCt;
			double distance, x, y, z;
			double distanceClosest = 0;
			for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}
				closestCt = null;
				distanceClosest = 0;
				for (L2ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
					{
						continue;
					}
					x = spawn.getLocx() - ct.getX();
					y = spawn.getLocy() - ct.getY();
					z = spawn.getLocz() - ct.getZ();
					distance = x * x + y * y + z * z;
					if (closestCt == null || distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				if (closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
		}
	}

	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getAttackerClan(clan.getClanId());
	}

	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		return null;
	}

	public final List<L2SiegeClan> getAttackerClans()
	{
		if (_isNormalSide)
		{
			return _attackerClans;
		}
		return _defenderClans;
	}

	public final int getAttackerRespawnDelay()
	{
		return FortSiegeManager.getInstance().getAttackerRespawnDelay();
	}

	public final Fort getFort()
	{
		if (_fort == null || _fort.length <= 0)
		{
			return null;
		}
		return _fort[0];
	}

	public final L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderClan(clan.getClanId());
	}

	public final L2SiegeClan getDefenderClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		return null;
	}

	public final List<L2SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
		{
			return _defenderClans;
		}
		return _attackerClans;
	}

	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderWaitingClan(clan.getClanId());
	}

	public final L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderWaitingClans())
		{
			if (sc != null && sc.getClanId() == clanId)
			{
				return sc;
			}
		}
		return null;
	}

	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}

	public final int getDefenderRespawnDelay()
	{
		return FortSiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
	}

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public final boolean getIsScheduled()
	{
		return _isScheduled;
	}

	public final void setIsScheduled(boolean isScheduled)
	{
		_isScheduled = isScheduled;
	}

	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}

	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
	}

	public List<L2NpcInstance> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}
		return null;
	}

	public final SiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			// ***_siegeGuardManager = new SiegeGuardManager(getFort());
		}
		return _siegeGuardManager;
	}
}