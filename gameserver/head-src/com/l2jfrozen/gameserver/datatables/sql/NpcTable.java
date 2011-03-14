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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.InfoCache;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2MinionData;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private final static Logger _log = LoggerFactory.getLogger(NpcTable.class);

	private static NpcTable _instance;

	private final Map<Integer, L2NpcTemplate> _npcs;
	private boolean _initialized = false;

	public static NpcTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new NpcTable();
		}

		return _instance;
	}

	private NpcTable()
	{
		_npcs = new FastMap<Integer, L2NpcTemplate>();

		restoreNpcData();
	}

	private void restoreNpcData()
	{
		Connection con = null;

		try
		{
			PreparedStatement statement;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
						"id",
						"idTemplate",
						"name",
						"serverSideName",
						"title",
						"serverSideTitle",
						"class",
						"collision_radius",
						"collision_height",
						"level",
						"sex",
						"type",
						"attackrange",
						"hp",
						"mp",
						"hpreg",
						"mpreg",
						"str",
						"con",
						"dex",
						"int",
						"wit",
						"men",
						"exp",
						"sp",
						"patk",
						"pdef",
						"matk",
						"mdef",
						"atkspd",
						"aggro",
						"matkspd",
						"rhand",
						"lhand",
						"armor",
						"walkspd",
						"runspd",
						"faction_id",
						"faction_range",
						"isUndead",
						"absorb_level",
						"absorb_type"
				}) + " FROM npc");
				final ResultSet npcdata = statement.executeQuery();
				fillNpcTable(npcdata);
				npcdata.close();
				statement.close();
			}
			catch(Exception e)
			{
				_log.error("NPCTable: Error creating NPC table", e);
			}

			if(Config.CUSTOM_NPC_TABLE)
				try
				{
					statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
							"id",
							"idTemplate",
							"name",
							"serverSideName",
							"title",
							"serverSideTitle",
							"class",
							"collision_radius",
							"collision_height",
							"level",
							"sex",
							"type",
							"attackrange",
							"hp",
							"mp",
							"hpreg",
							"mpreg",
							"str",
							"con",
							"dex",
							"int",
							"wit",
							"men",
							"exp",
							"sp",
							"patk",
							"pdef",
							"matk",
							"mdef",
							"atkspd",
							"aggro",
							"matkspd",
							"rhand",
							"lhand",
							"armor",
							"walkspd",
							"runspd",
							"faction_id",
							"faction_range",
							"isUndead",
							"absorb_level",
							"absorb_type"
					}) + " FROM custom_npc");
					final ResultSet npcdata = statement.executeQuery();
					fillNpcTable(npcdata);
					npcdata.close();
					statement.close();
				}
				catch(Exception e)
				{
					_log.error("NPCTable: Error creating custom NPC table", e);
				}
			try
			{
				statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				final ResultSet npcskills = statement.executeQuery();
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;

				while(npcskills.next())
				{
					int mobId = npcskills.getInt("npcid");
					npcDat = _npcs.get(mobId);

					if(npcDat == null)
					{
						continue;
					}

					int skillId = npcskills.getInt("skillid");
					int level = npcskills.getInt("level");

					if(npcDat.race == null && skillId == 4416)
					{
						npcDat.setRace(level);
						continue;
					}

					npcSkill = SkillTable.getInstance().getInfo(skillId, level);

					if(npcSkill == null)
					{
						continue;
					}

					npcDat.addSkill(npcSkill);
				}

				npcskills.close();
				statement.close();
			}
			catch(Exception e)
			{
				_log.error("NPCTable: Error reading NPC skills table", e);
			}

			if(Config.CUSTOM_DROPLIST_TABLE)
				try
				{
					statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
							"mobId", "itemId", "min", "max", "category", "chance"
					}) + " FROM custom_droplist ORDER BY mobId, chance DESC");
					ResultSet dropData = statement.executeQuery();

					int cCount = 0;

					while(dropData.next())
					{
						int mobId = dropData.getInt("mobId");

						L2NpcTemplate npcDat = _npcs.get(mobId);

						if(npcDat == null)
						{
							_log.warn("NPCTable: CUSTOM DROPLIST No npc correlating with id: {}", mobId);
							continue;
						}

						L2DropData dropDat = new L2DropData();
						dropDat.setItemId(dropData.getInt("itemId"));
						dropDat.setMinDrop(dropData.getInt("min"));
						dropDat.setMaxDrop(dropData.getInt("max"));
						dropDat.setChance(dropData.getInt("chance"));

						int category = dropData.getInt("category");

						npcDat.addDropData(dropDat, category);
						cCount++;
						//dropDat = null;
					}
					dropData.close();
					statement.close();
					_log.debug("CustomDropList : Added {} custom droplist", cCount);

					if(Config.ENABLE_CACHE_INFO)
					{
						FillDropList();
					}
				}
				catch(Exception e)
				{
					_log.error("NPCTable: Error reading NPC CUSTOM drop data", e);
				}

			try
			{
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
						"mobId", "itemId", "min", "max", "category", "chance"
				}) + " FROM droplist ORDER BY mobId, chance DESC");
				ResultSet dropData = statement.executeQuery();
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;

				while(dropData.next())
				{
					int mobId = dropData.getInt("mobId");

					npcDat = _npcs.get(mobId);

					if(npcDat == null)
					{
						_log.warn("NPCTable: No npc correlating with id: {}", mobId);
						continue;
					}

					dropDat = new L2DropData();

					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));

					int category = dropData.getInt("category");

					npcDat.addDropData(dropDat, category);
					//dropDat = null;
				}

				dropData.close();
				statement.close();
			}
			catch(Exception e)
			{
				_log.error("NPCTable: Error reading NPC drop data", e);
			}

			try
			{
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
						"npc_id", "class_id"
				}) + " FROM skill_learn");
				final ResultSet learndata = statement.executeQuery();

				while(learndata.next())
				{
					int npcId = learndata.getInt("npc_id");
					int classId = learndata.getInt("class_id");
					L2NpcTemplate npc = getTemplate(npcId);

					if(npc == null)
					{
						_log.warn("NPCTable: Error getting NPC template ID {} while trying to load skill trainer data.", npcId);
						continue;
					}

					npc.addTeachInfo(ClassId.values()[classId]);
				}

				learndata.close();
				statement.close();
			}
			catch(Exception e)
			{
				_log.error("NPCTable: Error reading NPC trainer data", e);
			}

			try
			{
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
						"boss_id", "minion_id", "amount_min", "amount_max"
				}) + " FROM minions");
				ResultSet minionData = statement.executeQuery();
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				int cnt = 0;

				while(minionData.next())
				{
					int raidId = minionData.getInt("boss_id");

					npcDat = _npcs.get(raidId);
					minionDat = new L2MinionData();
					minionDat.setMinionId(minionData.getInt("minion_id"));
					minionDat.setAmountMin(minionData.getInt("amount_min"));
					minionDat.setAmountMax(minionData.getInt("amount_max"));
					npcDat.addRaidData(minionDat);
					cnt++;
				}

				minionData.close();
				statement.close();
				_log.debug("NpcTable: Loaded {} Minions.", cnt);
			}
			catch(Exception e)
			{
				_log.error("Error loading minion data", e);
			}
		}
		finally
		{
			CloseUtil.close(con);
		}

		_initialized = true;
	}

	private void fillNpcTable(ResultSet NpcData) throws Exception
	{
		while(NpcData.next())
		{
			StatsSet npcDat = new StatsSet();

			int id = NpcData.getInt("id");

			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));

			//Level: for special bosses could be different
			int level = 0;
			float diff = 0; //difference between setted value and retail one
			boolean minion = false;

			switch(id)
			{
				case 29002: // and minions
				case 29003:
				case 29004:
				case 29005:
					minion = true;
				case 29001://queenAnt
				{
					if(Config.QA_LEVEL > 0)
					{
						diff = Config.QA_LEVEL - NpcData.getInt("level");
						level = Config.QA_LEVEL;
					}
					else
						level = NpcData.getInt("level");

				}
					break;
				case 29022:
				{ //zaken

					if(Config.ZAKEN_LEVEL > 0)
					{
						diff = Config.ZAKEN_LEVEL - NpcData.getInt("level");
						level = Config.ZAKEN_LEVEL;
					}
					else
						level = NpcData.getInt("level");

				}
					break;
				case 29015: //and minions
				case 29016:
				case 29017:
				case 29018:
					minion = true;
				case 29014://orfen 
				{

					if(Config.ORFEN_LEVEL > 0)
					{
						diff = Config.ORFEN_LEVEL - NpcData.getInt("level");
						level = Config.ORFEN_LEVEL;
					}
					else
						level = NpcData.getInt("level");

				}
					break;
				case 29007: //and minions
				case 29008:
				case 290011:
					minion = true;
				case 29006: //core
				{

					if(Config.CORE_LEVEL > 0)
					{
						diff = Config.CORE_LEVEL - NpcData.getInt("level");
						level = Config.CORE_LEVEL;
					}
					else
						level = NpcData.getInt("level");

				}
					break;
				default:
				{
					level = NpcData.getInt("level");
				}
			}

			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 38);

			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			//npcDat.set("name", "");
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));

			//BOSS POWER CHANGES
			double multi_value = 1;

			if(diff >= 15)
			{ //means that there is level customization
				multi_value = multi_value * (diff / 10);
			}
			else if(diff > 0 && diff < 15)
			{
				multi_value = multi_value + (diff / 10);
			}

			if(minion)
			{
				multi_value = multi_value * Config.LEVEL_DIFF_MULTIPLIER_MINION; //allow to increase the power of a value
				//that for example, at 40 diff levels is
				//equal to
				//    value = ((40/10)*0.8) = 3,2 --> 220 % more
			}
			else
			{

				switch(id)
				{
					case 29001:
					{//queenAnt

						if(Config.QA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.QA_POWER_MULTIPLIER;
						}

					}
						break;
					case 29022:
					{ //zaken

						if(Config.ZAKEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ZAKEN_POWER_MULTIPLIER;
						}

					}
						break;
					case 29014:
					{//orfen

						if(Config.ORFEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ORFEN_POWER_MULTIPLIER;
						}

					}
						break;
					case 29006:
					{ //core

						if(Config.CORE_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.CORE_POWER_MULTIPLIER;
						}

					}
						break;
					case 29019:
					{ //antharas

						if(Config.ANTHARAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ANTHARAS_POWER_MULTIPLIER;
						}

					}
						break;
					case 29028:
					{ //valakas

						if(Config.VALAKAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.VALAKAS_POWER_MULTIPLIER;
						}

					}
						break;
					case 29020:
					{ //baium

						if(Config.BAIUM_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.BAIUM_POWER_MULTIPLIER;
						}

					}
						break;
					case 29045:
					{ //frintezza

						if(Config.FRINTEZZA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.FRINTEZZA_POWER_MULTIPLIER;
						}

					}
						break;
					default:
					{
						level = NpcData.getInt("level");
					}
				}

			}

			npcDat.set("rewardExp", NpcData.getInt("exp") * multi_value);
			npcDat.set("rewardSp", NpcData.getInt("sp") * multi_value);
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd") * multi_value);
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd") * multi_value);
			npcDat.set("baseHpMax", NpcData.getInt("hp") * multi_value);
			npcDat.set("baseMpMax", NpcData.getInt("mp") * multi_value);
			npcDat.set("baseHpReg", (int) NpcData.getFloat("hpreg") * multi_value > 0 ? NpcData.getFloat("hpreg") : 1.5 + (level - 1) / 10.0);
			npcDat.set("baseMpReg", (int) NpcData.getFloat("mpreg") * multi_value > 0 ? NpcData.getFloat("mpreg") : 0.9 + 0.3 * (level - 1) / 10.0);
			npcDat.set("basePAtk", NpcData.getInt("patk") * multi_value);
			npcDat.set("basePDef", NpcData.getInt("pdef") * multi_value);
			npcDat.set("baseMAtk", NpcData.getInt("matk") * multi_value);
			npcDat.set("baseMDef", NpcData.getInt("mdef") * multi_value);

			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

			// constants, until we have stats in DB
			npcDat.set("baseSTR", NpcData.getInt("str"));
			npcDat.set("baseCON", NpcData.getInt("con"));
			npcDat.set("baseDEX", NpcData.getInt("dex"));
			npcDat.set("baseINT", NpcData.getInt("int"));
			npcDat.set("baseWIT", NpcData.getInt("wit"));
			npcDat.set("baseMEN", NpcData.getInt("men"));

			npcDat.set("baseCpMax", 0);

			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));

			npcDat.set("isUndead", NpcData.getString("isUndead"));

			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));
			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);

			_npcs.put(id, template);
		}

		_log.debug("NpcTable: Loaded {} Npc Templates.", _npcs.size());
	}

	public void reloadNpc(int id)
	{
		Connection con = null;

		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			Map<Integer, L2Skill> skills = new FastMap<Integer, L2Skill>();

			if(old.getSkills() != null)
			{
				skills.putAll(old.getSkills());
			}

			FastList<L2DropCategory> categories = new FastList<L2DropCategory>();

			if(old.getDropData() != null)
			{
				categories.addAll(old.getDropData());
			}
			ClassId[] classIds = null;

			if(old.getTeachInfo() != null)
			{
				classIds = old.getTeachInfo().clone();
			}

			List<L2MinionData> minions = new FastList<L2MinionData>();

			if(old.getMinionData() != null)
			{
				minions.addAll(old.getMinionData());
			}

			// reload the NPC base data
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"absorb_type"
			}) + " FROM npc WHERE id=?");
			st.setInt(1, id);
			final ResultSet rs = st.executeQuery();
			fillNpcTable(rs);
			rs.close();
			st.close();

			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);

			for(L2Skill skill : skills.values())
			{
				created.addSkill(skill);
			}

			if(classIds != null)
			{
				for(ClassId classId : classIds)
				{
					created.addTeachInfo(classId);
				}
			}

			for(L2MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
		}
		catch(Exception e)
		{
			_log.error("NPCTable: Could not reload data for NPC {}", id, e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	// just wrapper
	public void reloadAllNpc()
	{
		restoreNpcData();
	}

	public void saveNpc(StatsSet npc)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			Map<String, Object> set = npc.getSet();

			String name = "";
			String values = "";

			for(Object obj : set.keySet())
			{
				name = (String) obj;

				if(!name.equalsIgnoreCase("npcId"))
				{
					if(values != "")
					{
						values += ", ";
					}

					values += name + " = '" + set.get(name) + "'";
				}
			}

			final PreparedStatement statement = con.prepareStatement("UPDATE npc SET " + values + " WHERE id = ?");
			statement.setInt(1, npc.getInteger("npcId"));
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.error("NPCTable: Could not store new NPC data in database", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public void replaceTemplate(L2NpcTemplate npc)
	{
		_npcs.put(npc.npcId, npc);
	}

	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}

	public L2NpcTemplate getTemplateByName(String name)
	{
		for(L2NpcTemplate npcTemplate : _npcs.values())
			if(npcTemplate.name.equalsIgnoreCase(name))
				return npcTemplate;

		return null;
	}

	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new FastList<L2NpcTemplate>();

		for(L2NpcTemplate t : _npcs.values())
			if(t.level == lvl)
			{
				list.add(t);
			}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new FastList<L2NpcTemplate>();

		for(L2NpcTemplate t : _npcs.values())
			if(t.level == lvl && "L2Monster".equals(t.type))
			{
				list.add(t);
			}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		List<L2NpcTemplate> list = new FastList<L2NpcTemplate>();

		for(L2NpcTemplate t : _npcs.values())
			if(t.name.startsWith(letter) && "L2Npc".equals(t.type))
			{
				list.add(t);
			}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	/**
	 * @param classType
	 * @return
	 */
	public Set<Integer> getAllNpcOfClassType(String classType)
	{
		return null;
	}

	/**
	 * @param class1
	 * @return
	 */
	public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
	{
		return null;
	}

	/**
	 * @param aiType
	 * @return
	 */
	public Set<Integer> getAllNpcOfAiType(String aiType)
	{
		return null;
	}

	public Map<Integer, L2NpcTemplate> getAllTemplates()
	{
		return _npcs;
	}

	public void FillDropList()
	{
		for(L2NpcTemplate npc : _npcs.values())
		{
			InfoCache.addToDroplistCache(npc.npcId, npc.getAllDropData());
		}

		_log.info("Players droplist was cached");
	}

}
