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
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author l2jserver
 */
public class SkillSpellbookTable
{
	private final static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static SkillSpellbookTable _instance;

	private static Map<Integer, Integer> _skillSpellbooks;

	public static SkillSpellbookTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new SkillSpellbookTable();
		}

		return _instance;
	}

	private SkillSpellbookTable()
	{
		_skillSpellbooks = new FastMap<Integer, Integer>();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT skill_id, item_id FROM skill_spellbooks");
			ResultSet spbooks = statement.executeQuery();

			while(spbooks.next())
			{
				_skillSpellbooks.put(spbooks.getInt("skill_id"), spbooks.getInt("item_id"));
			}

			spbooks.close();
			statement.close();

			_log.finest("SkillSpellbookTable: Loaded {} Spellbooks."+" "+ _skillSpellbooks.size());
		}
		catch(Exception e)
		{
			_log.severe("Error while loading spellbook data"+" "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	public int getBookForSkill(int skillId, int level)
	{
		if(skillId == L2Skill.SKILL_DIVINE_INSPIRATION && level != -1)
		{
			switch(level)
			{
				case 1:
					return 8618; // Ancient Book - Divine Inspiration (Modern Language Version)
				case 2:
					return 8619; // Ancient Book - Divine Inspiration (Original Language Version)
				case 3:
					return 8620; // Ancient Book - Divine Inspiration (Manuscript)
				case 4:
					return 8621; // Ancient Book - Divine Inspiration (Original Version)
				default:
					return -1;
			}
		}

		if(!_skillSpellbooks.containsKey(skillId))
			return -1;

		return _skillSpellbooks.get(skillId);
	}

	public int getBookForSkill(L2Skill skill)
	{
		return getBookForSkill(skill.getId(), -1);
	}
	
	public int getBookForSkill(L2Skill skill, int level)
	{
		return getBookForSkill(skill.getId(), level);
	}
}
