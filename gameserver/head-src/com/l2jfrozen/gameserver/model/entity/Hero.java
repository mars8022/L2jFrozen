/*
 * L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author godson
 */
public class Hero
{
	private static final Logger LOGGER = Logger.getLogger(Hero.class);
	
	private static Hero _instance;
	private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
	private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
	private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
	private static final String INSERT_HERO = "INSERT INTO heroes VALUES (?,?,?,?,?)";
	private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ?" + " WHERE charId = ?";
	private static final String GET_CLAN_ALLY = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid " + " WHERE characters.obj_Id = ?";
	private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
	private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN " + "(6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) " + "AND owner_id NOT IN (SELECT obj_id FROM characters WHERE accesslevel > 0)";
	private static final List<Integer> _heroItems = Arrays.asList(6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621);
	private static Map<Integer, StatsSet> _heroes;
	private static Map<Integer, StatsSet> _completeHeroes;
	
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	
	public static Hero getInstance()
	{
		if (_instance == null)
		{
			_instance = new Hero();
		}
		return _instance;
	}
	
	public Hero()
	{
		init();
	}
	
	private void init()
	{
		_heroes = new FastMap<>();
		_completeHeroes = new FastMap<>();
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement(GET_HEROES);
			rset = statement.executeQuery();
			while (rset.next())
			{
				final StatsSet hero = new StatsSet();
				final int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();
				if (rset2.next())
				{
					final int clanId = rset2.getInt("clanid");
					final int allyId = rset2.getInt("allyId");
					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;
					if (clanId > 0)
					{
						clanName = ClanTable.getInstance().getClan(clanId).getName();
						clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
						if (allyId > 0)
						{
							allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
							allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
						}
					}
					hero.set(CLAN_CREST, clanCrest);
					hero.set(CLAN_NAME, clanName);
					hero.set(ALLY_CREST, allyCrest);
					hero.set(ALLY_NAME, allyName);
				}
				rset2.close();
				statement2.close();
				_heroes.put(charId, hero);
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();
			while (rset.next())
			{
				final StatsSet hero = new StatsSet();
				final int charId = rset.getInt(Olympiad.CHAR_ID);
				
				final String charName = rset.getString(Olympiad.CHAR_NAME);
				
				hero.set(Olympiad.CHAR_NAME, charName);
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();
				if (rset2.next())
				{
					final int clanId = rset2.getInt("clanid");
					final int allyId = rset2.getInt("allyId");
					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;
					if (clanId > 0)
					{
						
						final L2Clan clan = ClanTable.getInstance().getClan(clanId);
						if (clan != null)
						{
							
							clanName = clan.getName();
							clanCrest = clan.getCrestId();
							if (allyId > 0)
							{
								allyName = clan.getAllyName();
								allyCrest = clan.getAllyCrestId();
							}
							
						}
						else
						{
							
							LOGGER.error("Hero System: Player " + charName + " has clan id " + clanId + " that is not present inside clanTable..");
							
						}
						
					}
					hero.set(CLAN_CREST, clanCrest);
					hero.set(CLAN_NAME, clanName);
					hero.set(ALLY_CREST, allyCrest);
					hero.set(ALLY_NAME, allyName);
				}
				rset2.close();
				statement2.close();
				_completeHeroes.put(charId, hero);
			}
		}
		catch (final SQLException e)
		{
			LOGGER.warn("Hero System: Couldnt load Heroes");
			if (Config.DEBUG)
				e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement2, rset2);
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		LOGGER.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		LOGGER.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
	}
	
	public void putHero(final L2PcInstance player, final boolean isComplete)
	{
		try
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Adding new hero");
				LOGGER.info("Name:" + player.getName());
				LOGGER.info("ClassId:" + player.getClassId().getId());
			}
			final StatsSet newHero = new StatsSet();
			newHero.set(Olympiad.CHAR_NAME, player.getName());
			newHero.set(Olympiad.CLASS_ID, player.getClassId().getId());
			newHero.set(COUNT, 1);
			newHero.set(PLAYED, 1);
			_heroes.put(player.getObjectId(), newHero);
			if (isComplete)
				_completeHeroes.put(player.getObjectId(), newHero);
		}
		catch (final Exception e)
		{
			/*   */
		}
	}
	
	public void deleteHero(final L2PcInstance player, final boolean isComplete)
	{
		final int objId = player.getObjectId();
		if (_heroes.containsKey(objId))
			_heroes.remove(objId);
		if (isComplete)
			if (_completeHeroes.containsKey(objId))
				_completeHeroes.remove(objId);
	}
	
	public Map<Integer, StatsSet> getHeroes()
	{
		return _heroes;
	}
	
	public synchronized void computeNewHeroes(final List<StatsSet> newHeroes)
	{
		updateHeroes(true);
		L2ItemInstance[] items;
		InventoryUpdate iu;
		if (_heroes.size() != 0)
		{
			for (final StatsSet hero : _heroes.values())
			{
				final String name = hero.getString(Olympiad.CHAR_NAME);
				final L2PcInstance player = L2World.getInstance().getPlayer(name);
				if (player == null)
					continue;
				try
				{
					player.setHero(false);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
					iu = new InventoryUpdate();
					for (final L2ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
					iu = new InventoryUpdate();
					for (final L2ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_HAIR);
					iu = new InventoryUpdate();
					for (final L2ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_FACE);
					iu = new InventoryUpdate();
					for (final L2ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_DHAIR);
					iu = new InventoryUpdate();
					for (final L2ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					for (final L2ItemInstance item : player.getInventory().getAvailableItems(false))
					{
						if (item == null)
							continue;
						if (!_heroItems.contains(item.getItemId()))
							continue;
						player.destroyItem("Hero", item, null, true);
						iu = new InventoryUpdate();
						iu.addRemovedItem(item);
						player.sendPacket(iu);
					}
					player.sendPacket(new UserInfo(player));
					player.broadcastUserInfo();
				}
				catch (final NullPointerException e)
				{
					/**/
				}
			}
		}
		if (newHeroes.size() == 0)
		{
			_heroes.clear();
			return;
		}
		final Map<Integer, StatsSet> heroes = new FastMap<>();
		for (final StatsSet hero : newHeroes)
		{
			final int charId = hero.getInteger(Olympiad.CHAR_ID);
			if (_completeHeroes != null && _completeHeroes.containsKey(charId))
			{
				final StatsSet oldHero = _completeHeroes.get(charId);
				final int count = oldHero.getInteger(COUNT);
				oldHero.set(COUNT, count + 1);
				oldHero.set(PLAYED, 1);
				heroes.put(charId, oldHero);
			}
			else
			{
				final StatsSet newHero = new StatsSet();
				newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
				newHero.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
				newHero.set(COUNT, 1);
				newHero.set(PLAYED, 1);
				heroes.put(charId, newHero);
			}
		}
		deleteItemsInDb();
		_heroes.clear();
		_heroes.putAll(heroes);
		heroes.clear();
		updateHeroes(false);
		for (final StatsSet hero : _heroes.values())
		{
			final String name = hero.getString(Olympiad.CHAR_NAME);
			final L2PcInstance player = L2World.getInstance().getPlayer(name);
			if (player != null)
			{
				player.setHero(true);
				final L2Clan clan = player.getClan();
				if (clan != null)
				{
					clan.setReputationScore(clan.getReputationScore() + 1000, true);
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
					sm.addString(name);
					sm.addNumber(1000);
					clan.broadcastToOnlineMembers(sm);
				}
				player.sendPacket(new UserInfo(player));
				player.broadcastUserInfo();
			}
			else
			{
				Connection con = null;
				PreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(GET_CLAN_NAME);
					statement.setString(1, name);
					rset = statement.executeQuery();
					if (rset.next())
					{
						final String clanName = rset.getString("clan_name");
						if (clanName != null)
						{
							final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
							if (clan != null)
							{
								clan.setReputationScore(clan.getReputationScore() + 1000, true);
								clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
								final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
								sm.addString(name);
								sm.addNumber(1000);
								clan.broadcastToOnlineMembers(sm);
							}
						}
					}
				}
				catch (final Exception e)
				{
					LOGGER.warn("could not get clan name of " + name + ": " + e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCSR(con, statement, rset);
				}
			}
		}
	}
	
	public void updateHeroes(final boolean setDefault)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset2 = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if (setDefault)
			{
				statement = con.prepareStatement(UPDATE_ALL);
				statement.execute();
				DatabaseUtils.close(statement);
			}
			else
			{
				for (final Integer heroId : _heroes.keySet())
				{
					final StatsSet hero = _heroes.get(heroId);
					if (_completeHeroes == null || !_completeHeroes.containsKey(heroId))
					{
						statement = con.prepareStatement(INSERT_HERO);
						statement.setInt(1, heroId);
						statement.setString(2, hero.getString(Olympiad.CHAR_NAME));
						statement.setInt(3, hero.getInteger(Olympiad.CLASS_ID));
						statement.setInt(4, hero.getInteger(COUNT));
						statement.setInt(5, hero.getInteger(PLAYED));
						statement.execute();
						statement2 = con.prepareStatement(GET_CLAN_ALLY);
						statement2.setInt(1, heroId);
						rset2 = statement2.executeQuery();
						if (rset2.next())
						{
							final int clanId = rset2.getInt("clanid");
							final int allyId = rset2.getInt("allyId");
							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;
							if (clanId > 0)
							{
								clanName = ClanTable.getInstance().getClan(clanId).getName();
								clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
								if (allyId > 0)
								{
									allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
									allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
								}
							}
							hero.set(CLAN_CREST, clanCrest);
							hero.set(CLAN_NAME, clanName);
							hero.set(ALLY_CREST, allyCrest);
							hero.set(ALLY_NAME, allyName);
						}
						rset2.close();
						statement2.close();
						_heroes.remove(heroId);
						_heroes.put(heroId, hero);
						_completeHeroes.put(heroId, hero);
					}
					else
					{
						statement = con.prepareStatement(UPDATE_HERO);
						statement.setInt(1, hero.getInteger(COUNT));
						statement.setInt(2, hero.getInteger(PLAYED));
						statement.setInt(3, heroId);
						statement.execute();
					}
					DatabaseUtils.close(statement);
				}
			}
		}
		catch (final SQLException e)
		{
			LOGGER.warn("Hero System: Couldnt update Heroes");
			if (Config.DEBUG)
				e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement2, rset2);
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
	
	public List<Integer> getHeroItems()
	{
		return _heroItems;
	}
	
	private void deleteItemsInDb()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_ITEMS);
			statement.execute();
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}