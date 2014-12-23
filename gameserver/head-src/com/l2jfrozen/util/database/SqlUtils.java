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
package com.l2jfrozen.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.CloseUtil;

public class SqlUtils
{
	private static Logger LOGGER = Logger.getLogger(SqlUtils.class);
	
	// =========================================================
	// Data Field
	private static SqlUtils _instance;
	
	// =========================================================
	// Property - Public
	public static SqlUtils getInstance()
	{
		if (_instance == null)
		{
			_instance = new SqlUtils();
		}
		
		return _instance;
	}
	
	// =========================================================
	// Method - Public
	public static Integer getIntValue(final String resultField, final String tableName, final String whereClause)
	{
		String query = "";
		Integer res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		Connection con = null;
		try
		{
			query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[]
			{
				resultField
			}, tableName, whereClause, true);
			
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				res = rset.getInt(1);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					DatabaseUtils.close(rset);
					rset = null;
				}
				
				if (statement != null)
				{
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			
			CloseUtil.close(con);
			con = null;
			
		}
		
		return res;
	}
	
	public static Integer[] getIntArray(final String resultField, final String tableName, final String whereClause)
	{
		String query = "";
		Integer[] res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		Connection con = null;
		try
		{
			query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[]
			{
				resultField
			}, tableName, whereClause, false);
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			
			int rows = 0;
			
			while (rset.next())
			{
				rows++;
			}
			
			if (rows == 0)
			{
				CloseUtil.close(con);
				con = null;
				return new Integer[0];
			}
			res = new Integer[rows - 1];
			
			rset.first();
			
			final int row = 0;
			
			while (rset.next())
			{
				res[row] = rset.getInt(1);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("mSGI: Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					DatabaseUtils.close(rset);
					rset = null;
				}
				
				if (statement != null)
				{
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			
			CloseUtil.close(con);
			con = null;
			
		}
		
		return res;
	}
	
	public static Integer[][] get2DIntArray(final String[] resultFields, final String usedTables, final String whereClause)
	{
		final long start = System.currentTimeMillis();
		
		String query = "";
		
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		Integer res[][] = null;
		
		Connection con = null;
		try
		{
			query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			
			int rows = 0;
			
			while (rset.next())
			{
				rows++;
			}
			
			res = new Integer[rows - 1][resultFields.length];
			
			rset.first();
			
			int row = 0;
			
			while (rset.next())
			{
				for (int i = 0; i < resultFields.length; i++)
				{
					res[row][i] = rset.getInt(i + 1);
				}
				
				row++;
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					DatabaseUtils.close(rset);
					rset = null;
				}
				
				if (statement != null)
				{
					DatabaseUtils.close(statement);
					statement = null;
				}
				
				query = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			
			CloseUtil.close(con);
			con = null;
			
		}
		
		LOGGER.error("Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
		
		return res;
	}
	
	public static void OpzGame()
	{
		final String GAME_QUICK_OPTIMIZE = "OPTIMIZE TABLE auction, auction_bid, " + "auction_watch, augmentations, castle, castle_door, castle_doorupgrade, " + "castle_manor_procure, castle_manor_production, castle_siege_guards, char_templates, character_friends, " + "character_hennas, character_macroses, character_quests, character_recipebook, character_recommends, character_shortcuts, " + "character_skills, character_skills_save, character_subclasses, characters, characters_custom_data, clan_data, clan_privs, " + "clan_skills, clan_subpledges, clan_wars, clanhall, clanhall_functions, cursed_weapons, " + "dimensional_rift, " + "global_tasks, heroes, " + "minions, olympiad_nobles, pets, pets_stats, pkkills, " + "quest_global_data, raidboss_spawnlist, random_spawn, random_spawn_loc, rebirth_manager, seven_signs, " + "seven_signs_festival, seven_signs_status, siege_clans, " + "fort_siege_guards, fort_spawnlist";
		
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			LOGGER.info("Optimization gameserver tables...");
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(GAME_QUICK_OPTIMIZE);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.info("Optimization failed");
		}
		finally
		{
			CloseUtil.close(con);
		}
		LOGGER.info("Optimization gameserver tables completed.");
	}
	
	public static void OpzLogin()
	{
		final String LOGIN_QUICK_OPTIMIZE = "OPTIMIZE TABLE accounts, gameservers";
		
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			LOGGER.info("Optimization loginserver tables...");
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(LOGIN_QUICK_OPTIMIZE);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.info("Optimization failed");
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		LOGGER.info("Optimization loginserver tables completed.");
	}
}
