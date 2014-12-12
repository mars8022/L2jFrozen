/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.zone.type.L2FortZone;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos
 */

public class Fort
{
	protected static final Logger LOGGER = Logger.getLogger(Fort.class);
	
	// =========================================================
	// Data Field
	private int _fortId = 0;
	private final List<L2DoorInstance> _doors = new FastList<>();
	private final List<String> _doorDefault = new FastList<>();
	private String _name = "";
	private int _ownerId = 0;
	private L2Clan _fortOwner = null;
	private FortSiege _siege = null;
	private Calendar _siegeDate;
	private int _siegeDayOfWeek = 7; // Default to saturday
	private int _siegeHourOfDay = 20; // Default to 8 pm server time
	private L2FortZone _zone;
	private L2Clan _formerOwner = null;
	
	// =========================================================
	// Constructor
	public Fort(final int fortId)
	{
		_fortId = fortId;
		load();
		loadDoor();
	}
	
	// =========================================================
	// Method - Public
	
	public void EndOfSiege(final L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
		
	}
	
	public void Engrave(final L2Clan clan, final int objId)
	{
		getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to raise the flag.", true);
		setOwner(clan);
	}
	
	// This method add to the treasury
	/**
	 * Add amount to fort instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(final int amount)
	{
		// TODO: Implement?
	}
	
	/**
	 * Add amount to fort instance's treasury (warehouse), no tax paying.
	 * @param amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(final int amount)
	{
		return true;
	}
	
	/**
	 * Move non clan members off fort area and to nearest town.<BR>
	 * <BR>
	 */
	public void banishForeigners()
	{
		_zone.banishForeigners(getOwnerId());
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Sets this forts zone
	 * @param zone
	 */
	public void setZone(final L2FortZone zone)
	{
		_zone = zone;
	}
	
	public L2FortZone getZone()
	{
		return _zone;
	}
	
	/**
	 * Get the objects distance to this fort
	 * @param obj
	 * @return
	 */
	public double getDistance(final L2Object obj)
	{
		return _zone.getDistanceToZone(obj);
	}
	
	public void closeDoor(final L2PcInstance activeChar, final int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}
	
	public void openDoor(final L2PcInstance activeChar, final int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}
	
	public void openCloseDoor(final L2PcInstance activeChar, final int doorId, final boolean open)
	{
		if (activeChar.getClanId() != getOwnerId())
			return;
		
		L2DoorInstance door = getDoor(doorId);
		
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
		
		door = null;
	}
	
	// This method is used to begin removing all fort upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	// This method updates the fort tax rate
	public void setOwner(final L2Clan clan)
	{
		// Remove old owner
		if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
		{
			// Try to find clan instance
			L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
			
			if (oldOwner != null)
			{
				if (_formerOwner == null)
				{
					_formerOwner = oldOwner;
				}
				
				// Unset has fort flag for old owner
				oldOwner.setHasFort(0);
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " fortress!");
			}
			
			oldOwner = null;
		}
		
		updateOwnerInDB(clan); // Update in database
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
		
		updateClansReputation();
		
		_fortOwner = clan;
	}
	
	public void removeOwner(final L2Clan clan)
	{
		if (clan != null)
		{
			_formerOwner = clan;
			
			clan.setHasFort(0);
			Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " fort");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		updateOwnerInDB(null);
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory();
		}
		
		updateClansReputation();
		
		_fortOwner = null;
	}
	
	// This method updates the fort tax rate
	public void setTaxPercent(final L2PcInstance activeChar, final int taxPercent)
	{
		int maxTax;
		
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			default: // no owner
				maxTax = 15;
		}
		
		if (taxPercent < 0 || taxPercent > maxTax)
		{
			activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
			return;
		}
		
		activeChar.sendMessage(getName() + " fort tax changed to " + taxPercent + "%.");
	}
	
	/**
	 * Respawn all doors on fort grounds<BR>
	 * <BR>
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on fort grounds
	 * @param isDoorWeak
	 */
	public void spawnDoor(final boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			
			if (door.getCurrentHp() >= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(_doorDefault.get(i));
				
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (!door.getOpen())
			{
				door.closeMe();
			}
			
			door = null;
		}
		
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(final int doorId, final int hp, final int pDef, final int mDef)
	{
		final L2DoorInstance door = getDoor(doorId);
		
		if (door == null)
			return;
		
		if (door.getDoorId() == doorId)
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	// =========================================================
	// Method - Private
	// This method loads fort
	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			statement = con.prepareStatement("Select * from fort where id = ?");
			statement.setInt(1, getFortId());
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_name = rs.getString("name");
				
				_siegeDate = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				
				_siegeDayOfWeek = rs.getInt("siegeDayOfWeek");
				
				if (_siegeDayOfWeek < 1 || _siegeDayOfWeek > 7)
				{
					_siegeDayOfWeek = 7;
				}
				
				_siegeHourOfDay = rs.getInt("siegeHourOfDay");
				if (_siegeHourOfDay < 0 || _siegeHourOfDay > 23)
				{
					_siegeHourOfDay = 20;
				}
				
				_ownerId = rs.getInt("owner");
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
			
			if (getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
				// ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
				if (clan != null)
				{
					clan.setHasFort(getFortId());
					_fortOwner = clan;
				}
				clan = null;
			}
			else
			{
				_fortOwner = null;
			}
			
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: loadFortData(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// This method loads fort door data from database
	private void loadDoor()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("Select * from fort_door where fortId = ?");
			statement.setInt(1, getFortId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				// Create list of the door default for use when respawning dead doors
				_doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef"));
				
				L2DoorInstance door = DoorTable.parseList(_doorDefault.get(_doorDefault.size() - 1));
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				
				_doors.add(door);
				
				DoorTable.getInstance().putDoor(door);
				door = null;
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: loadFortDoor(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// This method loads fort door upgrade data from database
	private void loadDoorUpgrade()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("Select * from fort_doorupgrade where doorId in (Select Id from fort_door where fortId = ?)");
			statement.setInt(1, getFortId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
			}
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			rs = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: loadFortDoorUpgrade(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void removeDoorUpgrade()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("delete from fort_doorupgrade where doorId in (select id from fort_door where fortId=?)");
			statement.setInt(1, getFortId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: removeDoorUpgrade(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void saveDoorUpgrade(final int doorId, final int hp, final int pDef, final int mDef)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void updateOwnerInDB(final L2Clan clan)
	{
		if (clan != null)
		{
			_ownerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			_ownerId = 0; // Remove owner
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;
			
			statement = con.prepareStatement("UPDATE fort SET owner=? where id = ?");
			statement.setInt(1, getOwnerId());
			statement.setInt(2, getFortId());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			// ============================================================================
			
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasFort(getFortId()); // Set has fort flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " fort!");
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				// ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	// =========================================================
	// Property
	public final int getFortId()
	{
		return _fortId;
	}
	
	public final L2Clan getOwnerClan()
	{
		return _fortOwner;
	}
	
	public final int getOwnerId()
	{
		return _ownerId;
	}
	
	public final L2DoorInstance getDoor(final int doorId)
	{
		if (doorId <= 0)
			return null;
		
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			
			if (door.getDoorId() == doorId)
				return door;
			
			door = null;
		}
		return null;
	}
	
	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public final FortSiege getSiege()
	{
		if (_siege == null)
		{
			_siege = new FortSiege(new Fort[]
			{
				this
			});
		}
		
		return _siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}
	
	public final void setSiegeDate(final Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}
	
	public final int getSiegeDayOfWeek()
	{
		return _siegeDayOfWeek;
	}
	
	public final int getSiegeHourOfDay()
	{
		return _siegeHourOfDay;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void updateClansReputation()
	{
		if (_formerOwner != null)
		{
			if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				final int maxreward = Math.max(0, _formerOwner.getReputationScore());
				
				L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				
				if (owner != null)
				{
					owner.setReputationScore(owner.getReputationScore() + Math.min(500, maxreward), true);
					owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
				}
				
				owner = null;
			}
			else
			{
				_formerOwner.setReputationScore(_formerOwner.getReputationScore() + 250, true);
			}
			
			_formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
		}
		else
		{
			L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			if (owner != null)
			{
				owner.setReputationScore(owner.getReputationScore() + 500, true);
				owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
			}
			
			owner = null;
		}
	}
	
	private class endFortressSiege implements Runnable
	{
		private final Fort _f;
		private final L2Clan _clan;
		
		public endFortressSiege(final Fort f, final L2Clan clan)
		{
			_f = f;
			_clan = clan;
		}
		
		@Override
		public void run()
		{
			_f.Engrave(_clan, 0);
		}
		
	}
}
