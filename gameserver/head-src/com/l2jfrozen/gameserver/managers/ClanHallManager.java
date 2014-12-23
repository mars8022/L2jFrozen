/* L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Steuf
 */
public class ClanHallManager
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallManager.class);
	
	private static final Map<Integer, ClanHall> _clanHall = new FastMap<>();
	private static final Map<Integer, ClanHall> _freeClanHall = new FastMap<>();
	private static boolean _loaded = false;
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public static boolean loaded()
	{
		return _loaded;
	}
	
	private ClanHallManager()
	{
		load();
	}
	
	/** Reload All Clan Hall */
	/*
	 * public final void reload() Cant reload atm - would loose zone info { _clanHall.clear(); _freeClanHall.clear(); load(); }
	 */
	
	/** Load All Clan Hall */
	private final void load()
	{
		LOGGER.info("Initializing ClanHallManager");
		Connection con = null;
		try
		{
			int id, ownerId, lease, grade;
			String Name, Desc, Location;
			long paidUntil;
			boolean paid = false;
			
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("id");
				Name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				Desc = rs.getString("desc");
				Location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");
				
				final ClanHall ch = new ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid);
				if (ownerId == 0)
				{
					_freeClanHall.put(id, ch);
				}
				else
				{
					final L2Clan clan = ClanTable.getInstance().getClan(ownerId);
					if (clan != null)
					{
						_clanHall.put(id, ch);
						clan.setHasHideout(id);
					}
					else
					{
						_freeClanHall.put(id, ch);
						ch.free();
						AuctionManager.getInstance().initNPC(id);
					}
				}
			}
			rs.close();
			DatabaseUtils.close(statement);
			
			LOGGER.info("Loaded: " + getClanHalls().size() + " clan halls");
			LOGGER.info("Loaded: " + getFreeClanHalls().size() + " free clan halls");
			_loaded = true;
		}
		catch (final Exception e)
		{
			LOGGER.warn("Exception: ClanHallManager.load(): " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * Get Map with all FreeClanHalls
	 * @return
	 */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	/**
	 * Get Map with all ClanHalls
	 * @return
	 */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}
	
	/**
	 * Check is free ClanHall
	 * @param chId
	 * @return
	 */
	public final boolean isFree(final int chId)
	{
		return _freeClanHall.containsKey(chId);
	}
	
	/**
	 * Free a ClanHall
	 * @param chId
	 */
	public final synchronized void setFree(final int chId)
	{
		_freeClanHall.put(chId, _clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/**
	 * Set ClanHallOwner
	 * @param chId
	 * @param clan
	 */
	public final synchronized void setOwner(final int chId, final L2Clan clan)
	{
		if (!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
		{
			_clanHall.get(chId).free();
		}
		
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	/**
	 * Get Clan Hall by Id
	 * @param clanHallId
	 * @return
	 */
	public final ClanHall getClanHallById(final int clanHallId)
	{
		if (_clanHall.containsKey(clanHallId))
			return _clanHall.get(clanHallId);
		if (_freeClanHall.containsKey(clanHallId))
			return _freeClanHall.get(clanHallId);
		
		return null;
	}
	
	/* Get Clan Hall by x,y,z */
	/*
	 * public final ClanHall getClanHall(int x, int y, int z) { for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet()) if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue(); for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet()) if
	 * (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue(); return null; }
	 */
	
	public final ClanHall getNearbyClanHall(final int x, final int y, final int maxDist)
	{
		
		for (final Integer ch_id : _clanHall.keySet())
		{
			
			final ClanHall ch = _clanHall.get(ch_id);
			
			if (ch == null)
			{
				LOGGER.warn("ATTENTION: Clah Hall " + ch_id + " is not defined.");
				_clanHall.remove(ch_id);
				continue;
			}
			
			if (ch.getZone().getDistanceToZone(x, y) < maxDist)
				return ch;
			
		}
		
		for (final Integer ch_id : _freeClanHall.keySet())
		{
			
			final ClanHall ch = _freeClanHall.get(ch_id);
			
			if (ch == null)
			{
				LOGGER.warn("ATTENTION: Clah Hall " + ch_id + " is not defined.");
				_freeClanHall.remove(ch_id);
				continue;
			}
			
			if (ch.getZone().getDistanceToZone(x, y) < maxDist)
				return ch;
			
		}
		
		return null;
	}
	
	/**
	 * Get Clan Hall by Owner
	 * @param clan
	 * @return
	 */
	public final ClanHall getClanHallByOwner(final L2Clan clan)
	{
		if (clan == null)
			return null;
		
		for (final Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			
			if (ch == null || ch.getValue() == null)
				return null;
			
			if (clan.getClanId() == ch.getValue().getOwnerId())
				return ch.getValue();
		}
		
		return null;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClanHallManager _instance = new ClanHallManager();
	}
}
