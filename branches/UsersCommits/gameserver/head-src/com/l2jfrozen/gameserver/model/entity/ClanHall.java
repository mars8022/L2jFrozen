/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.AuctionManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * The Class ClanHall.
 */
public class ClanHall
{
	/** The Constant _log. */
	protected static final Logger _log = Logger.getLogger(ClanHall.class.getName());

	/** The _clan hall id. */
	private int _clanHallId;
	
	/** The _doors. */
	private List<L2DoorInstance> _doors = new FastList<L2DoorInstance>();
	
	/** The _door default. */
	private List<String> _doorDefault = new FastList<String>();
	
	/** The _name. */
	private String _name;
	
	/** The _owner id. */
	private int _ownerId;
	
	/** The _owner clan. */
	private L2Clan _ownerClan;
	
	/** The _lease. */
	private int _lease;
	
	/** The _desc. */
	private String _desc;
	
	/** The _location. */
	private String _location;
	
	/** The _paid until. */
	protected long _paidUntil;
	
	/** The _zone. */
	private L2ClanHallZone _zone;
	
	/** The _grade. */
	private int _grade;
	
	/** The _ch rate. */
	protected final int _chRate = 604800000;
	
	/** The _is free. */
	protected boolean _isFree = true;
	
	/** The _functions. */
	private Map<Integer, ClanHallFunction> _functions;
	
	/** The _paid. */
	protected boolean _paid;

	/** Clan Hall Functions. */
	public static final int FUNC_TELEPORT = 1;
	
	/** The Constant FUNC_ITEM_CREATE. */
	public static final int FUNC_ITEM_CREATE = 2;
	
	/** The Constant FUNC_RESTORE_HP. */
	public static final int FUNC_RESTORE_HP = 3;
	
	/** The Constant FUNC_RESTORE_MP. */
	public static final int FUNC_RESTORE_MP = 4;
	
	/** The Constant FUNC_RESTORE_EXP. */
	public static final int FUNC_RESTORE_EXP = 5;
	
	/** The Constant FUNC_SUPPORT. */
	public static final int FUNC_SUPPORT = 6;
	
	/** The Constant FUNC_DECO_FRONTPLATEFORM. */
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	
	/** The Constant FUNC_DECO_CURTAINS. */
	public static final int FUNC_DECO_CURTAINS = 8;

	/**
	 * The Class ClanHallFunction.
	 */
	public class ClanHallFunction
	{
		
		/** The _type. */
		private int _type;
		
		/** The _lvl. */
		private int _lvl;
		
		/** The _fee. */
		protected int _fee;
		
		/** The _temp fee. */
		protected int _tempFee;
		
		/** The _rate. */
		private long _rate;
		
		/** The _end date. */
		private long _endDate;
		
		/** The _in debt. */
		protected boolean _inDebt;

		/**
		 * Instantiates a new clan hall function.
		 *
		 * @param type the type
		 * @param lvl the lvl
		 * @param lease the lease
		 * @param tempLease the temp lease
		 * @param rate the rate
		 * @param time the time
		 */
		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask();
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public int getType()
		{
			return _type;
		}

		/**
		 * Gets the lvl.
		 *
		 * @return the lvl
		 */
		public int getLvl()
		{
			return _lvl;
		}

		/**
		 * Gets the lease.
		 *
		 * @return the lease
		 */
		public int getLease()
		{
			return _fee;
		}

		/**
		 * Gets the rate.
		 *
		 * @return the rate
		 */
		public long getRate()
		{
			return _rate;
		}

		/**
		 * Gets the end time.
		 *
		 * @return the end time
		 */
		public long getEndTime()
		{
			return _endDate;
		}

		/**
		 * Sets the lvl.
		 *
		 * @param lvl the new lvl
		 */
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}

		/**
		 * Sets the lease.
		 *
		 * @param lease the new lease
		 */
		public void setLease(int lease)
		{
			_fee = lease;
		}

		/**
		 * Sets the end time.
		 *
		 * @param time the new end time
		 */
		public void setEndTime(long time)
		{
			_endDate = time;
		}

		/**
		 * Initialize task.
		 */
		private void initializeTask()
		{
			if(_isFree)
				return;

			long currentTime = System.currentTimeMillis();

			if(_endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), _endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), 0);
			}
		}

		/**
		 * The Class FunctionTask.
		 */
		private class FunctionTask implements Runnable
		{
			
			/**
			 * Instantiates a new function task.
			 */
			public FunctionTask()
			{}

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				try
				{
					if(_isFree)
						return;

					if(getOwnerClan().getWarehouse().getAdena() >= _fee)
					{
						int fee = _fee;
						boolean newfc = true;

						if(getEndTime() == 0 || getEndTime() == -1)
						{
							if(getEndTime() == -1)
							{
								newfc = false;
								fee = _tempFee;
							}
						}
						else
						{
							newfc = false;
						}

						setEndTime(System.currentTimeMillis() + getRate());
						dbSave(newfc);
						getOwnerClan().getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);

						if(Config.DEBUG)
						{
							_log.warning("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
						}

						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch(Throwable t)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						t.printStackTrace();
				}
			}
		}

		/**
		 * Db save.
		 *
		 * @param newFunction the new function
		 */
		public void dbSave(boolean newFunction)
		{
			Connection con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection(false);
				if(newFunction)
				{
					statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
					statement.setInt(1, getId());
					statement.setInt(2, getType());
					statement.setInt(3, getLvl());
					statement.setInt(4, getLease());
					statement.setLong(5, getRate());
					statement.setLong(6, getEndTime());
				}
				else
				{
					statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?");
					statement.setInt(1, getLvl());
					statement.setInt(2, getLease());
					statement.setLong(3, getEndTime());
					statement.setInt(4, getId());
					statement.setInt(5, getType());
				}
				statement.execute();
				statement.close();
				statement = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}

	/**
	 * Instantiates a new clan hall.
	 *
	 * @param clanHallId the clan hall id
	 * @param name the name
	 * @param ownerId the owner id
	 * @param lease the lease
	 * @param desc the desc
	 * @param location the location
	 * @param paidUntil the paid until
	 * @param Grade the grade
	 * @param paid the paid
	 */
	public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, int Grade, boolean paid)
	{
		_clanHallId = clanHallId;
		_name = name;
		_ownerId = ownerId;

		if(Config.DEBUG)
		{
			_log.warning("Init Owner : " + _ownerId);
		}

		_lease = lease;
		_desc = desc;
		_location = location;
		_paidUntil = paidUntil;
		_grade = Grade;
		_paid = paid;
		loadDoor();
		_functions = new FastMap<Integer, ClanHallFunction>();

		if(ownerId != 0)
		{
			_isFree = false;

			initialyzeTask(false);
			loadFunctions();
		}
	}

	/**
	 * Return if clanHall is paid or not.
	 *
	 * @return the paid
	 */
	public final boolean getPaid()
	{
		return _paid;
	}

	/**
	 * Return Id Of Clan hall.
	 *
	 * @return the id
	 */
	public final int getId()
	{
		return _clanHallId;
	}

	/**
	 * Return name.
	 *
	 * @return the name
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Return OwnerId.
	 *
	 * @return the owner id
	 */
	public final int getOwnerId()
	{
		return _ownerId;
	}

	/**
	 * Return lease.
	 *
	 * @return the lease
	 */
	public final int getLease()
	{
		return _lease;
	}

	/**
	 * Return Desc.
	 *
	 * @return the desc
	 */
	public final String getDesc()
	{
		return _desc;
	}

	/**
	 * Return Location.
	 *
	 * @return the location
	 */
	public final String getLocation()
	{
		return _location;
	}

	/**
	 * Return PaidUntil.
	 *
	 * @return the paid until
	 */
	public final long getPaidUntil()
	{
		return _paidUntil;
	}

	/**
	 * Return Grade.
	 *
	 * @return the grade
	 */
	public final int getGrade()
	{
		return _grade;
	}

	/**
	 * Return all DoorInstance.
	 *
	 * @return the doors
	 */
	public final List<L2DoorInstance> getDoors()
	{
		//if (_doors == null)
		//	_doors = new FastList<L2DoorInstance>();
		return _doors;
	}

	/**
	 * Return Door.
	 *
	 * @param doorId the door id
	 * @return the door
	 */
	public final L2DoorInstance getDoor(int doorId)
	{
		if(doorId <= 0)
			return null;

		for(int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);

			if(door.getDoorId() == doorId)
				return door;

			door = null;
		}
		return null;
	}

	/**
	 * Return function with id.
	 *
	 * @param type the type
	 * @return the function
	 */
	public ClanHallFunction getFunction(int type)
	{
		if(_functions.get(type) != null)
			return _functions.get(type);

		return null;
	}

	/**
	 * Sets this clan halls zone.
	 *
	 * @param zone the new zone
	 */
	public void setZone(L2ClanHallZone zone)
	{
		_zone = zone;
	}

	/**
	 * Returns the zone of this clan hall.
	 *
	 * @return the zone
	 */
	public L2ClanHallZone getZone()
	{
		return _zone;
	}

	/**
	 * Free this clan hall.
	 */
	public void free()
	{
		_ownerId = 0;
		_isFree = true;

		for(Map.Entry<Integer, ClanHallFunction> fc : _functions.entrySet())
		{
			removeFunction(fc.getKey());
		}

		_functions.clear();
		_paidUntil = 0;
		_paid = false;
		updateDb();
	}

	/**
	 * Set owner if clan hall is free.
	 *
	 * @param clan the new owner
	 */
	public void setOwner(L2Clan clan)
	{
		// Verify that this ClanHall is Free and Clan isn't null
		if(_ownerId > 0 || clan == null)
			return;

		_ownerId = clan.getClanId();
		_isFree = false;
		_paidUntil = System.currentTimeMillis();
		initialyzeTask(true);

		// Annonce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}

	/**
	 * Gets the owner clan.
	 *
	 * @return the owner clan
	 */
	public L2Clan getOwnerClan()
	{
		if(_ownerId == 0)
			return null;

		if(_ownerClan == null)
		{
			_ownerClan = ClanTable.getInstance().getClan(getOwnerId());
		}

		return _ownerClan;
	}

	/**
	 * Respawn all doors.
	 */
	public void spawnDoor()
	{
		for(int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);

			if(door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(_doorDefault.get(i));

				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if(door.getOpen())
			{
				door.closeMe();
			}

			door.setCurrentHp(door.getMaxHp());

			door = null;
		}
	}

	/**
	 * Open or Close Door.
	 *
	 * @param activeChar the active char
	 * @param doorId the door id
	 * @param open the open
	 */
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoor(doorId, open);
		}
	}

	/**
	 * Open close door.
	 *
	 * @param doorId the door id
	 * @param open the open
	 */
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	/**
	 * Open close door.
	 *
	 * @param door the door
	 * @param open the open
	 */
	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if(door != null)
			if(open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
	}

	/**
	 * Open close doors.
	 *
	 * @param activeChar the active char
	 * @param open the open
	 */
	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoors(open);
		}
	}

	/**
	 * Open close doors.
	 *
	 * @param open the open
	 */
	public void openCloseDoors(boolean open)
	{
		for(L2DoorInstance door : getDoors())
		{
			if(door != null)
				if(open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
		}
	}

	/**
	 * Banish Foreigner.
	 */
	public void banishForeigners()
	{
		_zone.banishForeigners(getOwnerId());
	}

	/**
	 * Load All Functions.
	 */
	private void loadFunctions()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();

			while(rs.next())
			{
				_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime")));
			}

			rs.close();
			statement.close();
			statement = null;
			rs = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	/**
	 * Remove function In List and in DB.
	 *
	 * @param functionType the function type
	 */
	public void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	/**
	 * Update Function.
	 *
	 * @param type the type
	 * @param lvl the lvl
	 * @param lease the lease
	 * @param rate the rate
	 * @param addNew the add new
	 * @return true, if successful
	 */
	public boolean updateFunctions(int type, int lvl, int lease, long rate, boolean addNew)
	{
		if(Config.DEBUG)
		{
			_log.warning("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
		}

		if(addNew)
		{
			if(ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() < lease)
				return false;
			_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0));
		}
		else
		{
			if(lvl == 0 && lease == 0)
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - _functions.get(type).getLease();

				if(Config.DEBUG)
				{
					_log.warning("Called ClanHall.updateFunctions diffLease : " + diffLease);
				}

				if(diffLease > 0)
				{
					if(ClanTable.getInstance().getClan(_ownerId).getWarehouse().getAdena() < diffLease)
						return false;

					_functions.remove(type);
					_functions.put(type, new ClanHallFunction(type, lvl, lease, diffLease, rate, -1));
				}
				else
				{
					_functions.get(type).setLease(lease);
					_functions.get(type).setLvl(lvl);
					_functions.get(type).dbSave(false);
				}
			}
		}
		return true;
	}

	/**
	 * Update DB.
	 */
	public void updateDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, _ownerId);
			statement.setLong(2, _paidUntil);
			statement.setInt(3, _paid ? 1 : 0);
			statement.setInt(4, _clanHallId);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	/**
	 * Initialize Fee Task.
	 *
	 * @param forced the forced
	 */
	private void initialyzeTask(boolean forced)
	{
		long currentTime = System.currentTimeMillis();

		if(_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		}
		else if(!_paid && !forced)
		{
			if(System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= _paidUntil + _chRate)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil + _chRate - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}

	/**
	 * Fee Task.
	 */
	protected class FeeTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(_isFree)
					return;

				L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());

				if(ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					if(_paidUntil != 0)
					{
						while(_paidUntil < System.currentTimeMillis())
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = System.currentTimeMillis() + _chRate;
					}

					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);

					if(Config.DEBUG)
					{
						_log.warning("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall _paidUntil" + _paidUntil);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - System.currentTimeMillis());
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if(System.currentTimeMillis() > _paidUntil + _chRate)
					{
						if(ClanHallManager.loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addNumber(getLease());
						Clan.broadcastToOnlineMembers(sm);
						sm = null;

						if(System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= _paidUntil + _chRate)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil + _chRate - System.currentTimeMillis());
						}

					}
				}

				Clan = null;
			}
			catch(Exception t)
			{
				t.printStackTrace();
			}
		}
	}

	/**
	 * Load door.
	 */
	private void loadDoor()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
			statement.setInt(1, getId());
			ResultSet rs = statement.executeQuery();

			while(rs.next())
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
			statement.close();
			statement = null;
			rs = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
}
