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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.gameserver.model.L2ArmorSet;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ArmorSetsTable
{
	private final static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
	private static ArmorSetsTable _instance;

	public FastMap<Integer, L2ArmorSet> _armorSets;
	private final FastMap<Integer, ArmorDummy> _cusArmorSets;

	public static ArmorSetsTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new ArmorSetsTable();
		}

		return _instance;
	}

	private ArmorSetsTable()
	{
		_armorSets = new FastMap<Integer, L2ArmorSet>();
		_cusArmorSets = new FastMap<Integer, ArmorDummy>();
		loadData();
	}

	private void loadData()
	{
		Connection con = null;
		try
		{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				final PreparedStatement statement = con.prepareStatement("SELECT id, chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets");
				final ResultSet rset = statement.executeQuery();

				while(rset.next())
				{
					int id = rset.getInt("id");
					int chest = rset.getInt("chest");
					int legs  = rset.getInt("legs");
					int head  = rset.getInt("head");
					int gloves = rset.getInt("gloves");
					int feet  = rset.getInt("feet");
					int skill_id = rset.getInt("skill_id");
					int shield = rset.getInt("shield");
					int shield_skill_id = rset.getInt("shield_skill_id");
					int enchant6skill = rset.getInt("enchant6skill");

					_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet,skill_id, shield, shield_skill_id, enchant6skill));
					_cusArmorSets.put(id, new ArmorDummy(chest, legs, head, gloves, feet, skill_id, shield));
				}

				_log.finest("Loaded: {} armor sets."+" "+ _armorSets.size());

				rset.close();
				statement.close();
		}
		catch(Exception e)
		{
			_log.severe(e.getMessage()+" "+ e);
		} finally {
			CloseUtil.close(con);
		}
	}

	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}

	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}

	public void addObj(int v, L2ArmorSet s)
	{
		_armorSets.put(v, s);
	}

	public ArmorDummy getCusArmorSets(int id)
	{
		return _cusArmorSets.get(id);
	}

	public class ArmorDummy
	{
		private final int _chest;
		private final int _legs;
		private final int _head;
		private final int _gloves;
		private final int _feet;
		private final int _skill_id;
		private final int _shield;

		public ArmorDummy(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield)
		{
			_chest = chest;
			_legs = legs;
			_head = head;
			_gloves = gloves;
			_feet = feet;
			_skill_id = skill_id;
			_shield = shield;
		}

		public int getChest()
		{
			return _chest;
		}

		public int getLegs()
		{
			return _legs;
		}

		public int getHead()
		{
			return _head;
		}

		public int getGloves()
		{
			return _gloves;
		}

		public int getFeet()
		{
			return _feet;
		}

		public int getSkill_id()
		{
			return _skill_id;
		}

		public int getShield()
		{
			return _shield;
		}
	}
}
