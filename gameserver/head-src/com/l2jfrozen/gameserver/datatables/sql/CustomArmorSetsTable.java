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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfrozen.gameserver.datatables.sql.ArmorSetsTable;
import com.l2jfrozen.gameserver.model.L2ArmorSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * 
 * 
 * @author ProGramMoS
 */
public final class CustomArmorSetsTable
{
	private static final Log _log = LogFactory.getLog(CustomArmorSetsTable.class);
	private static CustomArmorSetsTable _instance;
	public static CustomArmorSetsTable getInstance() {
		if(_instance == null)
			_instance = new CustomArmorSetsTable();
		return _instance;
	}
	
	public CustomArmorSetsTable() {
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM custom_armorsets");
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				int chest = rset.getInt("chest");
				int legs = rset.getInt("legs");
				int head = rset.getInt("head");
				int gloves = rset.getInt("gloves");
				int feet = rset.getInt("feet");
				int skill_id = rset.getInt("skill_id");
				int shield = rset.getInt("shield");
				int shield_skill_id = rset.getInt("shield_skill_id");
				int enchant6skill = rset.getInt("enchant6skill");
				ArmorSetsTable.getInstance().addObj(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
			}
			_log.info("ArmorSetsTable: Loaded custom armor sets.");

			statement.close();
			rset.close();
			statement = null;
			rset = null;
		}
		catch(Exception e)
		{
			_log.error("ArmorSetsTable: Error reading Custom ArmorSets table: " + e);
		}
		finally {
			if(con != null)
				try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

}
