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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2EnchantSkillLearn;
import com.l2jfrozen.gameserver.model.L2PledgeSkillLearn;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.skills.holders.ISkillsHolder;
import com.l2jfrozen.gameserver.skills.holders.PlayerSkillHolder;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.2.2.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTreeTable
{
	private final static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static SkillTreeTable _instance;

	private Map<ClassId, Map<Integer, L2SkillLearn>> _skillTrees;
	private List<L2SkillLearn> _fishingSkillTrees; //all common skills (teached by Fisherman)
	private List<L2SkillLearn> _expandDwarfCraftSkillTrees; //list of special skill for dwarf (expand dwarf craft) learned by class teacher
	private List<L2PledgeSkillLearn> _pledgeSkillTrees; //pledge skill list
	private List<L2EnchantSkillLearn> _enchantSkillTrees; //enchant skill list

	public static SkillTreeTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new SkillTreeTable();
		}

		return _instance;
	}

	/**
	 * Return the minimum level needed to have this Expertise.<BR>
	 * <BR>
	 * 
	 * @param grade The grade level searched
	 * @return 
	 */
	public int getExpertiseLevel(int grade)
	{
		if(grade <= 0)
			return 0;

		// since expertise comes at same level for all classes we use paladin for now
		Map<Integer, L2SkillLearn> learnMap = getSkillTrees().get(ClassId.paladin);

		int skillHashCode = SkillTable.getSkillHashCode(239, grade);

		if(learnMap.containsKey(skillHashCode))
			return learnMap.get(skillHashCode).getMinLevel();
		
		return 0;
	}

	/**
	 * Each class receives new skill on certain levels, this methods allow the retrieval of the minimum character level
	 * of given class required to learn a given skill
	 * 
	 * @param skillId The iD of the skill
	 * @param classId The classId of the character
	 * @param skillLvl The SkillLvl
	 * @return The min level
	 */
	public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
	{
		Map<Integer, L2SkillLearn> map = getSkillTrees().get(classId);

		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

		if(map.containsKey(skillHashCode))
			return map.get(skillHashCode).getMinLevel();

		return 0;
	}

	public int getMinSkillLevel(int skillId, int skillLvl)
	{
		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

		// Look on all classes for this skill (takes the first one found)
		for(Map<Integer, L2SkillLearn> map : getSkillTrees().values())
		{
			// checks if the current class has this skill
			if(map.containsKey(skillHashCode))
				return map.get(skillHashCode).getMinLevel();
		}

		return 0;
	}

	private SkillTreeTable()
	{
		int classId = 0;
		int count = 0;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			final ResultSet classlist = statement.executeQuery();

			Map<Integer, L2SkillLearn> map;
			int parentClassId;
			L2SkillLearn skillLearn;

			while(classlist.next())
			{
				map = new FastMap<Integer, L2SkillLearn>();
				parentClassId = classlist.getInt("parent_id");
				classId = classlist.getInt("id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
				statement2.setInt(1, classId);
				final ResultSet skilltree = statement2.executeQuery();

				if(parentClassId != -1)
				{
					Map<Integer, L2SkillLearn> parentMap = getSkillTrees().get(ClassId.values()[parentClassId]);
					map.putAll(parentMap);
				}

				int prevSkillId = -1;

				while(skilltree.next())
				{
					int id = skilltree.getInt("skill_id");
					int lvl = skilltree.getInt("level");
					String name = skilltree.getString("name");
					int minLvl = skilltree.getInt("min_level");
					int cost = skilltree.getInt("sp");

					if(prevSkillId != id)
					{
						prevSkillId = id;
					}

					skillLearn = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
					map.put(SkillTable.getSkillHashCode(id, lvl), skillLearn);
				}

				getSkillTrees().put(ClassId.values()[classId], map);
				skilltree.close();
				statement2.close();

				count += map.size();
				_log.finest("SkillTreeTable: skill tree for class {} has {} skills "+ classId+" "+ map.size());
			}

			classlist.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.severe("Error while creating skill tree (Class ID {}): "+ classId+" "+ e);
		}
		

		_log.finest("SkillTreeTable: Loaded {} skills."+" "+ count);

		//Skill tree for fishing skill (from Fisherman)
		int count2 = 0;
		int count3 = 0;

		try
		{
			_fishingSkillTrees = new FastList<L2SkillLearn>();
			_expandDwarfCraftSkillTrees = new FastList<L2SkillLearn>();
			if (con == null)
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
			}
			final PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, sp, min_level, costid, cost, isfordwarf FROM fishing_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree2 = statement.executeQuery();

			int prevSkillId = -1;

			while(skilltree2.next())
			{
				int id = skilltree2.getInt("skill_id");
				int lvl = skilltree2.getInt("level");
				String name = skilltree2.getString("name");
				int minLvl = skilltree2.getInt("min_level");
				int cost = skilltree2.getInt("sp");
				int costId = skilltree2.getInt("costid");
				int costCount = skilltree2.getInt("cost");
				int isDwarven = skilltree2.getInt("isfordwarf");

				if(prevSkillId != id)
				{
					prevSkillId = id;
				}

				L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);

				if(isDwarven == 0)
				{
					_fishingSkillTrees.add(skill);
				}
				else
				{
					_expandDwarfCraftSkillTrees.add(skill);
				}
			}

			skilltree2.close();
			statement.close();

			count2 = _fishingSkillTrees.size();
			count3 = _expandDwarfCraftSkillTrees.size();
		}
		catch(Exception e)
		{
			_log.severe("Error while creating fishing skill table"+" "+ e);
		}

		int count4 = 0;
		try
		{
			_enchantSkillTrees = new FastList<L2EnchantSkillLearn>();
			if (con == null)
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
			}
			final PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, base_lvl, sp, min_skill_lvl, exp, success_rate76, success_rate77, success_rate78 FROM enchant_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree3 = statement.executeQuery();

			int prevSkillId = -1;

			while(skilltree3.next())
			{
				int id = skilltree3.getInt("skill_id");
				int lvl = skilltree3.getInt("level");
				String name = skilltree3.getString("name");
				int baseLvl = skilltree3.getInt("base_lvl");
				int minSkillLvl = skilltree3.getInt("min_skill_lvl");
				int sp = skilltree3.getInt("sp");
				int exp = skilltree3.getInt("exp");
				byte rate76 = skilltree3.getByte("success_rate76");
				byte rate77 = skilltree3.getByte("success_rate77");
				byte rate78 = skilltree3.getByte("success_rate78");

				if(prevSkillId != id)
				{
					prevSkillId = id;
				}

				_enchantSkillTrees.add(new L2EnchantSkillLearn(id, lvl, minSkillLvl, baseLvl, name, sp, exp, rate76, rate77, rate78));
			}

			skilltree3.close();
			statement.close();

			count4 = _enchantSkillTrees.size();
		}
		catch(Exception e)
		{
			_log.severe("Error while creating enchant skill table"+" "+ e);
		}

		int count5 = 0;
		try
		{
			_pledgeSkillTrees = new FastList<L2PledgeSkillLearn>();
			if (con == null)
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
			}
			final PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, clan_lvl, repCost, itemId FROM pledge_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree4 = statement.executeQuery();

			int prevSkillId = -1;

			while(skilltree4.next())
			{
				int id = skilltree4.getInt("skill_id");
				int lvl = skilltree4.getInt("level");
				String name = skilltree4.getString("name");
				int baseLvl = skilltree4.getInt("clan_lvl");
				int sp = skilltree4.getInt("repCost");
				int itemId = skilltree4.getInt("itemId");

				if(prevSkillId != id)
				{
					prevSkillId = id;
				}

				_pledgeSkillTrees.add(new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId));
			}

			skilltree4.close();
			statement.close();

			count5 = _pledgeSkillTrees.size();
		}
		catch(Exception e)
		{
			_log.severe("Error while creating fishing skill table"+" "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}

		_log.finest("FishingSkillTreeTable: Loaded {} general skills."+" "+ count2);
		_log.finest("FishingSkillTreeTable: Loaded {} dwarven skills."+" "+ count3);
		_log.finest("EnchantSkillTreeTable: Loaded {} enchant skills."+" "+ count4);
		_log.finest("PledgeSkillTreeTable: Loaded {} pledge skills"+" "+ count5);
	}

	private Map<ClassId, Map<Integer, L2SkillLearn>> getSkillTrees()
	{
		if(_skillTrees == null)
		{
			_skillTrees = new FastMap<ClassId, Map<Integer, L2SkillLearn>>();
		}

		return _skillTrees;
	}

	public L2SkillLearn[] getAvailableSkills(L2PcInstance player, ClassId classId)
	{
		List<L2SkillLearn> result = getAvailableSkills(player, classId, player);
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	/**
	 * Gets the available skills.
	 * @param player the learning skill player.
	 * @param classId the learning skill class ID.
	 * @param includeByFs if {@code true} skills from Forgotten Scroll will be included.
	 * @param includeAutoGet if {@code true} Auto-Get skills will be included.
	 * @param holder
	 * @return all available skills for a given {@code player}, {@code classId}, {@code includeByFs} and {@code includeAutoGet}.
	 */
	/*
	private List<L2SkillLearn> getAvailableSkills(L2PcInstance cha, ClassId classId, ISkillsHolder holder)
	{
		
		List<L2SkillLearn> result = new FastList<L2SkillLearn>();
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();

		L2Skill[] oldSkills = cha.getAllSkills();

		for(L2SkillLearn temp : skills)
		{
			if(temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for(int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if(!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result;
	}
	*/
	/**
	 * Gets the available skills.
	 * @param player the learning skill player.
	 * @param classId the learning skill class ID.
	 * @param holder
	 * @return all available skills for a given {@code player}, {@code classId}, {@code includeByFs} and {@code includeAutoGet}.
	 */
	private List<L2SkillLearn> getAvailableSkills(L2PcInstance player, ClassId classId, ISkillsHolder holder)
	{
		final List<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();

		if (skills.isEmpty())
		{
			// The Skill Tree for this class is undefined.
			_log.warning(getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
			return result;
		}
		
		for (L2SkillLearn skill : skills)
		{
			if (skill.getMinLevel() <= player.getLevel())
			{
				final L2Skill oldSkill = holder.getKnownSkill(skill.getId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}

	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
		List<L2SkillLearn> result = new FastList<L2SkillLearn>();
		List<L2SkillLearn> skills = new FastList<L2SkillLearn>();

		skills.addAll(_fishingSkillTrees);

		if(cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}

		L2Skill[] oldSkills = cha.getAllSkills();

		for(L2SkillLearn temp : skills)
		{
			if(temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for(int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if(!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public L2EnchantSkillLearn[] getAvailableEnchantSkills(L2PcInstance cha)
	{
		List<L2EnchantSkillLearn> result = new FastList<L2EnchantSkillLearn>();
		List<L2EnchantSkillLearn> skills = new FastList<L2EnchantSkillLearn>();

		skills.addAll(_enchantSkillTrees);

		L2Skill[] oldSkills = cha.getAllSkills();

		for(L2EnchantSkillLearn temp : skills)
		{
			if(76 <= cha.getLevel())
			{
				boolean knownSkill = false;

				for(int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if(oldSkills[j].getLevel() == temp.getMinSkillLevel())
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

			}
		}

		//cha.sendMessage("loaded "+ result.size()+" enchant skills for this char(You)");
		return result.toArray(new L2EnchantSkillLearn[result.size()]);
	}

	public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2PcInstance cha)
	{
		List<L2PledgeSkillLearn> result = new FastList<L2PledgeSkillLearn>();
		List<L2PledgeSkillLearn> skills = _pledgeSkillTrees;

		if(skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("No clan skills defined!");
			return new L2PledgeSkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getClan().getAllSkills();

		for(L2PledgeSkillLearn temp : skills)
		{
			if(temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;

				for(int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if(oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if(oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if(!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2PledgeSkillLearn[result.size()]);
	}

	/**
	 * Returns all allowed skills for a given class.
	 * 
	 * @param classId
	 * @return all allowed skills for a given class.
	 */
	public Collection<L2SkillLearn> getAllowedSkills(ClassId classId)
	{
		return getSkillTrees().get(classId).values();
	}

	public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
	{
		int minLevel = 0;
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();

		if(skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class {} is not defined !"+" "+ classId);
			return minLevel;
		}

		for(L2SkillLearn temp : skills)
		{
			if(temp.getMinLevel() > cha.getLevel() && temp.getSpCost() != 0)
				if(minLevel == 0 || temp.getMinLevel() < minLevel)
				{
					minLevel = temp.getMinLevel();
				}
		}

		return minLevel;
	}

	public int getMinLevelForNewSkill(L2PcInstance cha)
	{
		int minLevel = 0;
		List<L2SkillLearn> skills = new FastList<L2SkillLearn>();

		skills.addAll(_fishingSkillTrees);

		if(cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}

		for(L2SkillLearn s : skills)
		{
			if(s.getMinLevel() > cha.getLevel())
				if(minLevel == 0 || s.getMinLevel() < minLevel)
				{
					minLevel = s.getMinLevel();
				}
		}

		return minLevel;
	}

	public int getSkillCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		ClassId classId = player.getSkillLearningClassId();
		int skillHashCode = SkillTable.getSkillHashCode(skill);

		if(getSkillTrees().get(classId).containsKey(skillHashCode))
		{
			L2SkillLearn skillLearn = getSkillTrees().get(classId).get(skillHashCode);
			if(skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if(!player.getClassId().equalsOrChildOf(classId))
				{
					if(skill.getCrossLearnAdd() < 0)
						return skillCost;

					skillCost += skill.getCrossLearnAdd();
					skillCost *= skill.getCrossLearnMul();
				}

				if(classId.getRace() != player.getRace() && !player.isSubClassActive())
				{
					skillCost *= skill.getCrossLearnRace();
				}

				if(classId.isMage() != player.getClassId().isMage())
				{
					skillCost *= skill.getCrossLearnProf();
				}
			}
		}

		return skillCost;
	}

	public int getSkillSpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for(L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if(enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if(enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			if(76 > player.getLevel())
			{
				continue;
			}

			skillCost = enchantSkillLearn.getSpCost();
		}
		return skillCost;
	}

	public int getSkillExpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for(L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if(enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if(enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			if(76 > player.getLevel())
			{
				continue;
			}

			skillCost = enchantSkillLearn.getExp();
		}

		return skillCost;
	}

	public byte getSkillRate(L2PcInstance player, L2Skill skill)
	{
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for(L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if(enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if(enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			return enchantSkillLearn.getRate(player);
		}

		return 0;
	}

	/**
	 * @param player
	 * @param classId
	 * @return
	 */
	public Collection<L2Skill> getAllAvailableSkills(L2PcInstance player, ClassId classId)
	{
		// Get available skills
		int unLearnable = 0;
		PlayerSkillHolder holder = new PlayerSkillHolder(player.getSkills());
		List<L2SkillLearn> learnable = getAvailableSkills(player, classId, holder);
		while (learnable.size() > unLearnable)
		{
			for (L2SkillLearn s : learnable)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if ((sk == null) || ((sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION) && !Config.AUTO_LEARN_DIVINE_INSPIRATION && !player.isGM()))
				{
					unLearnable++;
					continue;
				}
				
				holder.addSkill(sk);
			}
			
			// Get new available skills, some skills depend of previous skills to be available.
			learnable = getAvailableSkills(player, classId, holder);
		}
		return holder.getSkills().values();
	}
}