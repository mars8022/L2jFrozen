/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jfrozen.gameserver.model.L2MaxPolyModel;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 *
 * @author  Velvet
 */
public class MaxCheatersTable
{
	private final FastMap<Integer, L2MaxPolyModel> _map;
	private final static Logger _log = LoggerFactory.getLogger(MaxCheatersTable.class);
	private static MaxCheatersTable _instance;

	private final String SQL_SELECT = "SELECT * from max_poly";

	public MaxCheatersTable()
	{
		_map = new FastMap<Integer, L2MaxPolyModel>();
	}

	public static MaxCheatersTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new MaxCheatersTable();
			_instance.load();
		}
		return _instance;
	}

	private void load()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement st = con.prepareStatement(SQL_SELECT);
			final ResultSet rs = st.executeQuery();
			
			restore(rs);
			
			rs.close();
			st.close();
		}
		catch(Exception e)
		{
			_log.error(e.getMessage(), e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	private void restore(ResultSet data) throws SQLException
	{
		StatsSet set = new StatsSet();

		while(data.next())
		{
			set.set("name", data.getString("name"));
			set.set("title", data.getString("title"));
			set.set("sex", data.getInt("sex"));
			set.set("hair", data.getInt("hair"));
			set.set("hairColor", data.getInt("hairColor"));
			set.set("face", data.getInt("face"));
			set.set("classId", data.getInt("classId"));
			set.set("npcId", data.getInt("npcId"));
			set.set("weaponIdRH", data.getInt("weaponIdRH"));
			set.set("weaponIdLH", data.getInt("weaponIdLH"));
			set.set("weaponIdEnc", data.getInt("weaponIdEnc"));
			set.set("armorId", data.getInt("armorId"));
			set.set("head", data.getInt("head"));
			set.set("hats", data.getInt("hats"));
			set.set("faces", data.getInt("faces"));
			set.set("chest", data.getInt("chest"));
			set.set("legs", data.getInt("legs"));
			set.set("gloves", data.getInt("gloves"));
			set.set("feet", data.getInt("feet"));
			set.set("abnormalEffect", data.getInt("abnormalEffect"));
			set.set("pvpFlag", data.getInt("pvpFlag"));
			set.set("karma", data.getInt("karma"));
			set.set("recom", data.getInt("recom"));
			set.set("clan", data.getInt("clan"));
			set.set("isHero", data.getInt("isHero"));
			set.set("pledge", data.getInt("pledge"));
			set.set("nameColor", data.getInt("nameColor"));
			set.set("titleColor", data.getInt("titleColor"));

			final L2MaxPolyModel poly = new L2MaxPolyModel(set);
			_map.put(poly.getNpcId(), poly);// xD
		}
		_log.debug("MaxCheatersTable Loaded: {} npc to pc entry(s)", _map.size());
	}

	public L2MaxPolyModel getModelForID(int key)
	{
		return _map.get(key);
	}
}
