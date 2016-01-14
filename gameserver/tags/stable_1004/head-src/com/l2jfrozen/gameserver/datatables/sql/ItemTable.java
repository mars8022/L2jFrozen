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
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.Item;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.skills.SkillsEngine;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable
{
	private final static Logger _log = Logger.getLogger(ItemTable.class.getName());
	private final static java.util.logging.Logger _logItems = java.util.logging.Logger.getLogger("item");

	private static final Map<String, Integer> _crystalTypes = new FastMap<String, Integer>();
	private static final Map<String, L2WeaponType> _weaponTypes = new FastMap<String, L2WeaponType>();
	private static final Map<String, L2ArmorType> _armorTypes = new FastMap<String, L2ArmorType>();
	private static final Map<String, Integer> _slots = new FastMap<String, Integer>();

	private L2Item[] _allTemplates;

	private final boolean _initialized = true;

	static
	{
		_crystalTypes.put("s", L2Item.CRYSTAL_S);
		_crystalTypes.put("a", L2Item.CRYSTAL_A);
		_crystalTypes.put("b", L2Item.CRYSTAL_B);
		_crystalTypes.put("c", L2Item.CRYSTAL_C);
		_crystalTypes.put("d", L2Item.CRYSTAL_D);
		_crystalTypes.put("none", L2Item.CRYSTAL_NONE);

		_weaponTypes.put("blunt", L2WeaponType.BLUNT);
		_weaponTypes.put("bow", L2WeaponType.BOW);
		_weaponTypes.put("dagger", L2WeaponType.DAGGER);
		_weaponTypes.put("dual", L2WeaponType.DUAL);
		_weaponTypes.put("dualfist", L2WeaponType.DUALFIST);
		_weaponTypes.put("etc", L2WeaponType.ETC);
		_weaponTypes.put("fist", L2WeaponType.FIST);
		_weaponTypes.put("none", L2WeaponType.NONE); // these are shields !
		_weaponTypes.put("pole", L2WeaponType.POLE);
		_weaponTypes.put("sword", L2WeaponType.SWORD);
		_weaponTypes.put("bigsword", L2WeaponType.BIGSWORD); //Two-Handed Swords
		_weaponTypes.put("pet", L2WeaponType.PET); //Pet Weapon
		_weaponTypes.put("rod", L2WeaponType.ROD); //Fishing Rods
		_weaponTypes.put("bigblunt", L2WeaponType.BIGBLUNT); //Two handed blunt
		_armorTypes.put("none", L2ArmorType.NONE);
		_armorTypes.put("light", L2ArmorType.LIGHT);
		_armorTypes.put("heavy", L2ArmorType.HEAVY);
		_armorTypes.put("magic", L2ArmorType.MAGIC);
		_armorTypes.put("pet", L2ArmorType.PET);

		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("face", L2Item.SLOT_FACE);
		_slots.put("dhair", L2Item.SLOT_DHAIR);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear,lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger,lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("none", L2Item.SLOT_NONE);
		_slots.put("wolf", L2Item.SLOT_WOLF); // for wolf
		_slots.put("hatchling", L2Item.SLOT_HATCHLING); // for hatchling
		_slots.put("strider", L2Item.SLOT_STRIDER); // for strider
		_slots.put("babypet", L2Item.SLOT_BABYPET); // for babypet
	}

	private static ItemTable _instance;

	/** Table of SQL request in order to obtain items from tables [etcitem], [armor], [weapon] */
	private static final String[] SQL_ITEM_SELECTS =
	{
			"SELECT item_id, name, crystallizable, item_type, weight, consume_type, crystal_type, duration, price, crystal_count, sellable, dropable, destroyable, tradeable FROM etcitem",

			"SELECT item_id, name, bodypart, crystallizable, armor_type, weight," + " crystal_type, avoid_modify, duration, p_def, m_def, mp_bonus," + " price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl FROM armor",

			"SELECT item_id, name, bodypart, crystallizable, weight, soulshots, spiritshots," + " crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify," + " shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, duration, price, crystal_count," + " sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl,enchant4_skill_id,enchant4_skill_lvl, onCast_skill_id, onCast_skill_lvl," + " onCast_skill_chance, onCrit_skill_id, onCrit_skill_lvl, onCrit_skill_chance FROM weapon"
	};

	private static final String[] SQL_CUSTOM_ITEM_SELECTS =
	{
			"SELECT item_id, name, crystallizable, item_type, weight, consume_type, crystal_type, duration, price, crystal_count, sellable, dropable, destroyable, tradeable FROM custom_etcitem",

			"SELECT item_id, name, bodypart, crystallizable, armor_type, weight," + " crystal_type, avoid_modify, duration, p_def, m_def, mp_bonus," + " price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl FROM custom_armor",

			"SELECT item_id, name, bodypart, crystallizable, weight, soulshots, spiritshots," + " crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify," + " shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, duration, price, crystal_count," + " sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl,enchant4_skill_id,enchant4_skill_lvl, onCast_skill_id, onCast_skill_lvl," + " onCast_skill_chance, onCrit_skill_id, onCrit_skill_lvl, onCrit_skill_chance FROM custom_weapon"
	};

	/** List of etcItem */
	private static final Map<Integer, Item> itemData = new FastMap<Integer, Item>();
	/** List of weapons */
	private static final Map<Integer, Item> weaponData = new FastMap<Integer, Item>();
	/** List of armor */
	private static final Map<Integer, Item> armorData = new FastMap<Integer, Item>();

	/**
	 * Returns instance of ItemTable
	 * 
	 * @return ItemTable
	 */
	public static ItemTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new ItemTable();
		}
		return _instance;
	}

	/**
	 * Returns a new object Item
	 * 
	 * @return
	 */
	public Item newItem()
	{
		return new Item();
	}

	/**
	 * Constructor.
	 */
	public ItemTable()
	{
		Map<Integer, L2EtcItem> etcItems = new FastMap<Integer, L2EtcItem>();
		Map<Integer, L2Armor> armors = new FastMap<Integer, L2Armor>();
		Map<Integer, L2Weapon> weapons = new FastMap<Integer, L2Weapon>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			for(String selectQuery : SQL_ITEM_SELECTS)
			{
				final PreparedStatement statement = con.prepareStatement(selectQuery);
				final ResultSet rset = statement.executeQuery();

				// Add item in correct FastMap
				while(rset.next())
				{
					if(selectQuery.endsWith("etcitem"))
					{
						final Item newItem = readItem(rset);
						itemData.put(newItem.id, newItem);
					}
					else if(selectQuery.endsWith("armor"))
					{
						final Item newItem = readArmor(rset);
						armorData.put(newItem.id, newItem);
					}
					else if(selectQuery.endsWith("weapon"))
					{
						final Item newItem = readWeapon(rset);
						weaponData.put(newItem.id, newItem);
					}
				}

				statement.close();
				rset.close();
			}
		}
		catch(Exception e)
		{
			_log.severe("data error on item"+" "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}

		if(Config.CUSTOM_ITEM_TABLES)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				for(String selectQuery : SQL_CUSTOM_ITEM_SELECTS)
				{
					final PreparedStatement statement = con.prepareStatement(selectQuery);
					final ResultSet rset = statement.executeQuery();

					// Add item in correct FastMap
					while(rset.next())
					{
						if(selectQuery.endsWith("etcitem"))
						{
							final Item newItem = readItem(rset);

							if(itemData.containsKey(newItem.id))
							{
								itemData.remove(newItem.id);
							}

							itemData.put(newItem.id, newItem);
						}
						else if(selectQuery.endsWith("armor"))
						{
							final Item newItem = readArmor(rset);

							if(armorData.containsKey(newItem.id))
							{
								armorData.remove(newItem.id);
							}

							armorData.put(newItem.id, newItem);
						}
						else if(selectQuery.endsWith("weapon"))
						{
							final Item newItem = readWeapon(rset);

							if(weaponData.containsKey(newItem.id))
							{
								weaponData.remove(newItem.id);
							}

							weaponData.put(newItem.id, newItem);
						}
					}

					statement.close();
					rset.close();
				}
			}
			catch(Exception e)
			{
				_log.severe("data error on custom_item"+" "+ e);
			}
			finally
			{
				CloseUtil.close(con);
			}
		}

		for(L2Armor armor : SkillsEngine.getInstance().loadArmors(armorData))
		{
			armors.put(armor.getItemId(), armor);
		}
		_log.finest("ItemTable: Loaded " + armors.size() + " Armors.");

		for(L2EtcItem item : SkillsEngine.getInstance().loadItems(itemData))
		{
			etcItems.put(item.getItemId(), item);
		}
		_log.finest("ItemTable: Loaded " + etcItems.size() + " Items.");

		for(L2Weapon weapon : SkillsEngine.getInstance().loadWeapons(weaponData))
		{
			weapons.put(weapon.getItemId(), weapon);
		}
		_log.finest("ItemTable: Loaded " + weapons.size() + " Weapons.");

		//fillEtcItemsTable();
		//fillArmorsTable();
		//FillWeaponsTable();
		buildFastLookupTable(armors, weapons, etcItems);
	}

	/**
	 * Returns object Item from the record of the database
	 * 
	 * @param rset : ResultSet designating a record of the [weapon] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readWeapon(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		item.set = new StatsSet();
		item.type = _weaponTypes.get(rset.getString("weaponType"));
		item.id = rset.getInt("item_id");
		item.name = rset.getString("name");

		item.set.set("item_id", item.id);
		item.set.set("name", item.name);

		// lets see if this is a shield
		if(item.type == L2WeaponType.NONE)
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
		}
		else
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", L2Item.TYPE2_WEAPON);
		}

		item.set.set("bodypart", _slots.get(rset.getString("bodypart")));
		item.set.set("crystal_type", _crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
		item.set.set("weight", rset.getInt("weight"));
		item.set.set("soulshots", rset.getInt("soulshots"));
		item.set.set("spiritshots", rset.getInt("spiritshots"));
		item.set.set("p_dam", rset.getInt("p_dam"));
		item.set.set("rnd_dam", rset.getInt("rnd_dam"));
		item.set.set("critical", rset.getInt("critical"));
		item.set.set("hit_modify", rset.getDouble("hit_modify"));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("shield_def", rset.getInt("shield_def"));
		item.set.set("shield_def_rate", rset.getInt("shield_def_rate"));
		item.set.set("atk_speed", rset.getInt("atk_speed"));
		item.set.set("mp_consume", rset.getInt("mp_consume"));
		item.set.set("m_dam", rset.getInt("m_dam"));
		item.set.set("duration", rset.getInt("duration"));
		item.set.set("price", rset.getInt("price"));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));

		item.set.set("item_skill_id", rset.getInt("item_skill_id"));
		item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));

		item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
		item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));

		item.set.set("onCast_skill_id", rset.getInt("onCast_skill_id"));
		item.set.set("onCast_skill_lvl", rset.getInt("onCast_skill_lvl"));
		item.set.set("onCast_skill_chance", rset.getInt("onCast_skill_chance"));

		item.set.set("onCrit_skill_id", rset.getInt("onCrit_skill_id"));
		item.set.set("onCrit_skill_lvl", rset.getInt("onCrit_skill_lvl"));
		item.set.set("onCrit_skill_chance", rset.getInt("onCrit_skill_chance"));

		if(item.type == L2WeaponType.PET)
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);

			if(item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
			{
				item.set.set("type2", L2Item.TYPE2_PET_WOLF);
			}
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
			{
				item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
			}
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_BABYPET)
			{
				item.set.set("type2", L2Item.TYPE2_PET_BABY);
			}
			else
			{
				item.set.set("type2", L2Item.TYPE2_PET_STRIDER);
			}

			item.set.set("bodypart", L2Item.SLOT_R_HAND);
		}

		return item;
	}

	/**
	 * Returns object Item from the record of the database
	 * 
	 * @param rset : ResultSet designating a record of the [armor] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readArmor(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		item.set = new StatsSet();
		item.type = _armorTypes.get(rset.getString("armor_type"));
		item.id = rset.getInt("item_id");
		item.name = rset.getString("name");

		item.set.set("item_id", item.id);
		item.set.set("name", item.name);
		int bodypart = _slots.get(rset.getString("bodypart"));
		item.set.set("bodypart", bodypart);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));
		item.set.set("item_skill_id", rset.getInt("item_skill_id"));
		item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));

		if(bodypart == L2Item.SLOT_NECK || bodypart == L2Item.SLOT_HAIR || bodypart == L2Item.SLOT_FACE || bodypart == L2Item.SLOT_DHAIR || (bodypart & L2Item.SLOT_L_EAR) != 0 || (bodypart & L2Item.SLOT_L_FINGER) != 0)
		{
			item.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			item.set.set("type2", L2Item.TYPE2_ACCESSORY);
		}
		else
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
			item.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
		}

		item.set.set("weight", rset.getInt("weight"));
		item.set.set("crystal_type", _crystalTypes.get(rset.getString("crystal_type")));
		item.set.set("avoid_modify", rset.getInt("avoid_modify"));
		item.set.set("duration", rset.getInt("duration"));
		item.set.set("p_def", rset.getInt("p_def"));
		item.set.set("m_def", rset.getInt("m_def"));
		item.set.set("mp_bonus", rset.getInt("mp_bonus"));
		item.set.set("price", rset.getInt("price"));

		if(item.type == L2ArmorType.PET)
		{
			item.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);

			if(item.set.getInteger("bodypart") == L2Item.SLOT_WOLF)
			{
				item.set.set("type2", L2Item.TYPE2_PET_WOLF);
			}
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_HATCHLING)
			{
				item.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
			}
			else if(item.set.getInteger("bodypart") == L2Item.SLOT_BABYPET)
			{
				item.set.set("type2", L2Item.TYPE2_PET_BABY);
			}
			else
			{
				item.set.set("type2", L2Item.TYPE2_PET_STRIDER);
			}

			item.set.set("bodypart", L2Item.SLOT_CHEST);
		}

		return item;
	}

	/**
	 * Returns object Item from the record of the database
	 * 
	 * @param rset : ResultSet designating a record of the [etcitem] table of database
	 * @return Item : object created from the database record
	 * @throws SQLException
	 */
	private Item readItem(ResultSet rset) throws SQLException
	{
		Item item = new Item();
		item.set = new StatsSet();
		item.id = rset.getInt("item_id");

		item.set.set("item_id", item.id);
		item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
		item.set.set("type1", L2Item.TYPE1_ITEM_QUESTITEM_ADENA);
		item.set.set("type2", L2Item.TYPE2_OTHER);
		item.set.set("bodypart", 0);
		item.set.set("crystal_count", rset.getInt("crystal_count"));
		item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
		item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")));
		item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")));
		item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")));
		String itemType = rset.getString("item_type");

		if(itemType.equals("none"))
		{
			item.type = L2EtcItemType.OTHER; // only for default
		}
		else if(itemType.equals("castle_guard"))
		{
			item.type = L2EtcItemType.SCROLL; // dummy
		}
		else if(itemType.equals("pet_collar"))
		{
			item.type = L2EtcItemType.PET_COLLAR;
		}
		else if(itemType.equals("potion"))
		{
			item.type = L2EtcItemType.POTION;
		}
		else if(itemType.equals("recipe"))
		{
			item.type = L2EtcItemType.RECEIPE;
		}
		else if(itemType.equals("scroll"))
		{
			item.type = L2EtcItemType.SCROLL;
		}
		else if(itemType.equals("seed"))
		{
			item.type = L2EtcItemType.SEED;
		}
		else if(itemType.equals("shot"))
		{
			item.type = L2EtcItemType.SHOT;
		}
		else if(itemType.equals("spellbook"))
		{
			item.type = L2EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
		}
		else if(itemType.equals("herb"))
		{
			item.type = L2EtcItemType.HERB;
		}
		else if(itemType.equals("arrow"))
		{
			item.type = L2EtcItemType.ARROW;
			item.set.set("bodypart", L2Item.SLOT_L_HAND);
		}
		else if(itemType.equals("quest"))
		{
			item.type = L2EtcItemType.QUEST;
			item.set.set("type2", L2Item.TYPE2_QUEST);
		}
		else if(itemType.equals("lure"))
		{
			item.type = L2EtcItemType.OTHER;
			item.set.set("bodypart", L2Item.SLOT_L_HAND);
		}
		else
		{
			_log.finest("unknown etcitem type:" + itemType);
			item.type = L2EtcItemType.OTHER;
		}
		itemType = null;

		String consume = rset.getString("consume_type");
		if(consume.equals("asset"))
		{
			item.type = L2EtcItemType.MONEY;
			item.set.set("stackable", true);
			item.set.set("type2", L2Item.TYPE2_MONEY);
		}
		else if(consume.equals("stackable"))
		{
			item.set.set("stackable", true);
		}
		else
		{
			item.set.set("stackable", false);
		}

		int crystal = _crystalTypes.get(rset.getString("crystal_type"));
		item.set.set("crystal_type", crystal);

		int weight = rset.getInt("weight");
		item.set.set("weight", weight);
		item.name = rset.getString("name");
		item.set.set("name", item.name);

		item.set.set("duration", rset.getInt("duration"));
		item.set.set("price", rset.getInt("price"));

		return item;
	}

	/**
	 * Returns if ItemTable initialized
	 * 
	 * @return boolean
	 */
	public boolean isInitialized()
	{
		return _initialized;
	}

	/*
	private void fillEtcItemsTable()
	{
		for (Item itemInfo : itemData.values())
		{
			L2EtcItem item = SkillsEngine.getInstance().loadEtcItem(itemInfo.id, itemInfo.type, itemInfo.name, itemInfo.set);
			if (item == null)
			{
				item = new L2EtcItem((L2EtcItemType)itemInfo.type, itemInfo.set);
			}

			_etcItems.put(item.getItemId(), item);
		}
	}

	private void fillArmorsTable()
	{
		List<L2Armor> armorList = SkillsEngine.getInstance().loadArmors(armorData);

		/*for (Item itemInfo : armorData.values())
			{

			L2Armor armor = SkillsEngine.getInstance().loadArmor(itemInfo.id, itemInfo.type, itemInfo.name, itemInfo.set);
			if (armor == null)
				armor = new L2Armor((L2ArmorType)itemInfo.type, itemInfo.set);

			_armors.put(armor.getItemId(), armor);
		}*
	}

	private void FillWeaponsTable()
	{
		for (Item itemInfo : weaponData.values())
		{
			L2Weapon weapon = SkillsEngine.getInstance().loadWeapon(itemInfo.id, itemInfo.type, itemInfo.name, itemInfo.set);
				if (weapon == null)
					weapon = new L2Weapon((L2WeaponType)itemInfo.type, itemInfo.set);

				_weapons.put(weapon.getItemId(), weapon);
				}
	}*/

	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 * @param armors 
	 * @param weapons 
	 * @param etcItems 
	 */
	private void buildFastLookupTable(Map<Integer, L2Armor> armors, Map<Integer, L2Weapon> weapons, Map<Integer, L2EtcItem> etcItems)
	{
		int highestId = 0;

		// Get highest ID of item in armor FastMap, then in weapon FastMap, and finally in etcitem FastMap
		for(L2Armor item : armors.values())
		{
			if(item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}

		for(L2Weapon item : weapons.values())
		{
			if(item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}

		for(L2EtcItem item : etcItems.values())
		{
			if(item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}

		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		_log.finest("highest item id used: {}"+" "+ highestId);

		_allTemplates = new L2Item[highestId + 1];

		// Insert armor item in Fast Look Up Table
		for(int id : armors.keySet())
		{
			_allTemplates[id] = armors.get(id);
		}

		// Insert weapon item in Fast Look Up Table
		for(int id : weapons.keySet())
		{
			_allTemplates[id] = weapons.get(id);
		}

		// Insert etcItem item in Fast Look Up Table
		for(int id : etcItems.keySet())
		{
			_allTemplates[id] = etcItems.get(id);
		}
	}

	/**
	 * Returns the item corresponding to the item ID
	 * 
	 * @param id : int designating the item
	 * @return L2Item
	 */
	public L2Item getTemplate(int id)
	{
		if(id > _allTemplates.length)
			return null;
		return _allTemplates[id];
	}

	/**
	 * Create the L2ItemInstance corresponding to the Item Identifier and quantitiy add logs the activity.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create and Init the L2ItemInstance corresponding to the Item Identifier and quantity</li> <li>Add the
	 * L2ItemInstance object to _allObjects of L2world</li> <li>Logs Item creation according to log settings</li><BR>
	 * <BR>
	 * 
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be created
	 * @param count : int Quantity of items to be created for stackable items
	 * @param actor : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in
	 *            transformation
	 * @return L2ItemInstance corresponding to the new item
	 */
	public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		// Create and Init the L2ItemInstance corresponding to the Item Identifier
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);

		//create loot schedule also if autoloot is enabled
		if(process.equalsIgnoreCase("loot")/* && !Config.AUTO_LOOT*/)
		{
			ScheduledFuture<?> itemLootShedule;
			long delay = 0;
			// if in CommandChannel and was killing a World/RaidBoss
			if(reference instanceof L2GrandBossInstance || reference instanceof L2RaidBossInstance)
			{
				if(((L2Attackable) reference).getFirstCommandChannelAttacked() != null && ((L2Attackable) reference).getFirstCommandChannelAttacked().meetRaidWarCondition(reference))
				{
					item.setOwnerId(((L2Attackable) reference).getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					delay = 300000;
				}
				else
				{
					delay = 15000;
					item.setOwnerId(actor.getObjectId());
				}
			}
			else
			{
				item.setOwnerId(actor.getObjectId());
				delay = 15000;
			}
			itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new resetOwner(item), delay);
			item.setItemLootShedule(itemLootShedule);
		}

		_log.finest("ItemTable: Item created  oid: {} itemid: {}"+" "+ item.getObjectId()+" "+ itemId);

		// Add the L2ItemInstance object to _allObjects of L2world
		L2World.getInstance().storeObject(item);

		// Set Item parameters
		if(item.isStackable() && count > 1)
		{
			item.setCount(count);
		}

		if(Config.LOG_ITEMS)
		{
			LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
					item, actor, reference
			});
			_logItems.log(record);
		}

		return item;
	}

	public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}

	/**
	 * Returns a dummy (fr = factice) item.<BR>
	 * <BR>
	 * <U><I>Concept :</I></U><BR>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * 
	 * @param itemId : int designating the item
	 * @return L2ItemInstance designating the dummy item created
	 */
	public L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = getTemplate(itemId);

		if(item == null)
			return null;

		L2ItemInstance temp = new L2ItemInstance(0, item);

		try
		{
			temp = new L2ItemInstance(0, itemId);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			// this can happen if the item templates were not initialized
		}

		if(temp.getItem() == null)
		{
			_log.warning("ItemTable: Item Template missing for Id: {}"+" "+ itemId);
		}

		return temp;
	}

	/**
	 * Destroys the L2ItemInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Sets L2ItemInstance parameters to be unusable</li> <li>Removes the L2ItemInstance object to _allObjects of
	 * L2world</li> <li>Logs Item delettion according to log settings</li><BR>
	 * <BR>
	 * 
	 * @param process : String Identifier of process triggering this action
	 * @param item 
	 * @param actor : L2PcInstance Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in
	 *            transformation
	 */
	public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		synchronized (item)
		{
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);

			L2World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());

			if(Config.LOG_ITEMS)
			{
				LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
						item, actor, reference
				});
				_logItems.log(record);
			}

			// if it's a pet control item, delete the pet as well
			if(L2PetDataTable.isPetItem(item.getItemId()))
			{
				Connection con = null;
				try
				{
					// Delete the pet in db
					con = L2DatabaseFactory.getInstance().getConnection(false);
					final PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
					statement.setInt(1, item.getObjectId());
					statement.execute();
					statement.close();
				}
				catch(Exception e)
				{
					_log.severe("could not delete pet objectid"+" "+ e);
				}
				finally
				{
					CloseUtil.close(con);
				}
			}
		}
	}

	public void reload()
	{
		synchronized (_instance)
		{
			_instance = null;
			_instance = new ItemTable();
		}
	}

	protected class resetOwner implements Runnable
	{
		L2ItemInstance _item;

		public resetOwner(L2ItemInstance item)
		{
			_item = item;
		}

		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}

}
