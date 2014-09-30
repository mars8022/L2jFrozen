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
package com.l2jfrozen.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class stores players' buff schemes into FastMap. On player login, his scheme is loaded and on server shutdown all modified schemes are saved to DataBase. This avoids too many unnecessary DataBase connections and queries. If server crashes, nothing important is lost :)
 *
 * @author House
 */
public class CharSchemesTable
{
	private static FastMap<Integer, FastMap<String, FastList<L2Skill>>> _schemesTable = new FastMap<Integer, FastMap<String, FastList<L2Skill>>>();
	private static CharSchemesTable _instance = null;
	private static Logger _log = Logger.getLogger(CharSchemesTable.class.getName());
	private static final String SQL_LOAD_SCHEME = "SELECT * FROM mods_buffer_schemes WHERE ownerId=?";
	private static final String SQL_DELETE_SCHEME = "DELETE FROM mods_buffer_schemes WHERE ownerId=?";
	private static final String SQL_INSERT_SCHEME = "INSERT INTO mods_buffer_schemes (ownerId, id, level, scheme) VALUES (?,?,?,?)";

	public CharSchemesTable()
	{
		_schemesTable.clear();
	}

	/**
	 * This method loads player scheme and put into _schemesTable map.
	 *
	 * @param objectId
	 *            : player's objectId
	 */
	public void loadScheme(int objectId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement(SQL_LOAD_SCHEME);
			statement.setInt(1, objectId);
			ResultSet rs = statement.executeQuery();
			FastMap<String, FastList<L2Skill>> map = new FastMap<String, FastList<L2Skill>>();
			while (rs.next())
			{
				int skillId = rs.getInt("id");
				int skillLevel = rs.getInt("level");
				String scheme = rs.getString("scheme");
				if (!map.containsKey(scheme) && map.size() <= PowerPakConfig.NPCBUFFER_MAX_SCHEMES)
					map.put(scheme, new FastList<L2Skill>());
				if (map.get(scheme) != null && map.get(scheme).size() < PowerPakConfig.NPCBUFFER_MAX_SKILLS)
					map.get(scheme).add(SkillTable.getInstance().getInfo(skillId, skillLevel));
			}
			if (!map.isEmpty())
				_schemesTable.put(objectId, map);
			statement.close();
			rs.close();
		}
		catch (Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("Error trying to load buff scheme from object id: " + objectId);
		}
		finally
		{
			CloseUtil.close(con);
			
		}
	}

	public void onPlayerLogin(int playerId)
	{
		if (_schemesTable.get(playerId) == null)
			loadScheme(playerId);
	}

	/**
	 * Do necessary task when server is shutting down or restarting:<br>
	 * <li>Clears DataBase</li> <li>Saves new info</li>
	 */
	public void onServerShutdown()
	{
		if (PowerPakConfig.NPCBUFFER_STORE_SCHEMES)
		{
			clearDB();
			saveDataToDB();
		}
	}

	public void clearDB()
	{
		if (_schemesTable.isEmpty())
			return;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			for (FastMap.Entry<Integer, FastMap<String, FastList<L2Skill>>> e = _schemesTable.head(), end = _schemesTable.tail(); (e = e.getNext()) != end;)
			{
				PreparedStatement statement = con.prepareStatement(SQL_DELETE_SCHEME);
				statement.setInt(1, e.getKey());
				statement.execute();
			}
		}
		catch (Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("CharSchemesTable: Error while trying to delete schemes");
		}
		finally
		{
			CloseUtil.close(con);
			
		}
	}

	public void saveDataToDB()
	{
		if (_schemesTable.isEmpty())
			return;
		Connection con = null;
		int count = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			// _schemesTable
			for (FastMap.Entry<Integer, FastMap<String, FastList<L2Skill>>> e = _schemesTable.head(), end = _schemesTable.tail(); (e = e.getNext()) != end;)
			{
				// each profile
				if (e.getValue() == null || e.getValue().isEmpty())
					continue;
				for (FastMap.Entry<String, FastList<L2Skill>> a = e.getValue().head(), enda = e.getValue().tail(); (a = a.getNext()) != enda;)
				{
					if (a.getValue() == null || a.getValue().isEmpty())
						continue;
					// each skill
					for (L2Skill sk : a.getValue())
					{
						PreparedStatement statement = con.prepareStatement(SQL_INSERT_SCHEME);
						statement.setInt(1, e.getKey());
						statement.setInt(2, sk.getId());
						statement.setInt(3, sk.getLevel());
						statement.setString(4, a.getKey());
						statement.execute();
					}
				}
				count++;
			}
		}
		catch (Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("CharSchemesTable: Error while trying to delete schemes");
		}
		finally
		{
			CloseUtil.close(con);
			
			System.out.println("CharSchemeTable: Saved " + String.valueOf(count + " scheme(s)"));
		}
	}

	public FastList<L2Skill> getScheme(int playerid, String scheme_key)
	{
		if (_schemesTable.get(playerid) == null)
			return null;
		return _schemesTable.get(playerid).get(scheme_key);
	}

	public boolean getSchemeContainsSkill(int playerId, String scheme_key, int skillId)
	{
		for (L2Skill sk : getScheme(playerId, scheme_key))
			if (sk.getId() == skillId)
				return true;
		return false;
	}

	public void setScheme(int playerId, String schemeKey, FastList<L2Skill> list)
	{
		_schemesTable.get(playerId).put(schemeKey, list);
	}

	public FastMap<String, FastList<L2Skill>> getAllSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}

	public FastMap<Integer, FastMap<String, FastList<L2Skill>>> getSchemesTable()
	{
		return _schemesTable;
	}

	public static CharSchemesTable getInstance()
	{
		if (_instance == null)
			_instance = new CharSchemesTable();
		return _instance;
	}
}