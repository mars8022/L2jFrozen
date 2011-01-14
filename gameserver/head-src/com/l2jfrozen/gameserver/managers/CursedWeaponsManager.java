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
package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.CursedWeapon;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2CommanderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Micht
 */
public class CursedWeaponsManager
{
	private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());

	// =========================================================
	private static CursedWeaponsManager _instance;

	public static final CursedWeaponsManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new CursedWeaponsManager();
		}
		return _instance;
	}

	// =========================================================
	// Data Field
	private Map<Integer, CursedWeapon> _cursedWeapons;

	// =========================================================
	// Constructor
	public CursedWeaponsManager()
	{
		_log.info("Initializing CursedWeaponsManager");
		_cursedWeapons = new FastMap<Integer, CursedWeapon>();

		if(!Config.ALLOW_CURSED_WEAPONS)
			return;

		load();
		restore();
		controlPlayers();

		_log.info("Loaded : " + _cursedWeapons.size() + " cursed weapon(s).");
	}

	// =========================================================
	// Method - Private
	public final void reload()
	{
		_instance = new CursedWeaponsManager();
	}

	private final void load()
	{
		if(Config.DEBUG)
		{
			System.out.print("  Parsing ... ");
		}
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File(Config.DATAPACK_ROOT + "/data/cursedWeapons.xml");
			if(!file.exists())
			{
				if(Config.DEBUG)
				{
					System.out.println("NO FILE");
				}
				return;
			}

			Document doc = factory.newDocumentBuilder().parse(file);

			factory = null;
			file = null;

			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if("list".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();

							CursedWeapon cw = new CursedWeapon(id, skillId, name);
							name = null;

							int val;
							for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if("dropRate".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDropRate(val);
								}
								else if("duration".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDuration(val);
								}
								else if("durationLost".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDurationLost(val);
								}
								else if("disapearChance".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDisapearChance(val);
								}
								else if("stageKills".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setStageKills(val);
								}
							}

							// Store cursed weapon
							_cursedWeapons.put(id, cw);

							attrs = null;
							cw = null;
						}
					}
				}
			}

			doc = null;

			if(Config.DEBUG)
			{
				System.out.println("OK");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error parsing cursed weapons file.", e);

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			return;
		}
	}

	private final void restore()
	{
		if(Config.DEBUG)
		{
			System.out.print("  Restoring ... ");
		}

		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
			ResultSet rset = statement.executeQuery();

			if(rset.next())
			{
				int itemId = rset.getInt("itemId");
				int playerId = rset.getInt("playerId");
				int playerKarma = rset.getInt("playerKarma");
				int playerPkKills = rset.getInt("playerPkKills");
				int nbKills = rset.getInt("nbKills");
				long endTime = rset.getLong("endTime");

				CursedWeapon cw = _cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate();

				cw = null;
			}

			rset.close();
			statement.close();
			rset = null;
			statement = null;

			if(Config.DEBUG)
			{
				System.out.println("OK");
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not restore CursedWeapons data: " + e);

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			return;
		}
		finally
		{
			try { con.close(); } catch(Exception e) { 
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			con = null;
		}
	}

	private final void controlPlayers()
	{
		if(Config.DEBUG)
		{
			System.out.print("  Checking players ... ");
		}

		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			ResultSet rset = null;

			// TODO: See comments below...
			// This entire for loop should NOT be necessary, since it is already handled by
			// CursedWeapon.endOfLife().  However, if we indeed *need* to duplicate it for safety,
			// then we'd better make sure that it FULLY cleans up inactive cursed weapons!
			// Undesired effects result otherwise, such as player with no zariche but with karma
			// or a lost-child entry in the cursedweapons table, without a corresponding one in items...
			for(CursedWeapon cw : _cursedWeapons.values())
			{
				if(cw.isActivated())
				{
					continue;
				}

				// Do an item check to be sure that the cursed weapon isn't hold by someone
				int itemId = cw.getItemId();
				try
				{
					statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					rset = statement.executeQuery();

					if(rset.next())
					{
						// A player has the cursed weapon in his inventory ...
						int playerId = rset.getInt("owner_id");
						_log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

						// Delete the item
						statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						if(statement.executeUpdate() != 1)
						{
							_log.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}
						statement.close();

						// Delete the skill
						/*
						statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=");
						statement.setInt(1, playerId);
						statement.setInt(2, cw.getSkillId());
						if (statement.executeUpdate() != 1)
						{
							_log.warning("Error while deleting cursed weapon "+itemId+" skill from userId "+playerId);
						}
						*/
						// Restore the player's old karma and pk count
						statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
						statement.setInt(1, cw.getPlayerKarma());
						statement.setInt(2, cw.getPlayerPkKills());
						statement.setInt(3, playerId);
						if(statement.executeUpdate() != 1)
						{
							_log.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
						}
						// clean up the cursedweapons table.
						removeFromDb(itemId);
					}
					rset.close();
					statement.close();
					rset = null;
					statement = null;

				}
				catch(SQLException sqlE)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						sqlE.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not check CursedWeapons data: " + e);

			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			if(Config.DEBUG)
			{
				System.out.println("ERROR");
			}
			return;
		}
		finally
		{
			try
			{
				try { con.close(); } catch(Exception e) {
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
				}
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}

		if(Config.DEBUG)
		{
			System.out.println("DONE");
		}
	}

	// =========================================================
	// Properties - Public
	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if(attackable instanceof L2SiegeGuardInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2FestivalMonsterInstance || attackable instanceof L2GrandBossInstance || attackable instanceof L2FortSiegeGuardInstance || attackable instanceof L2CommanderInstance)
			return;

		if(player.isCursedWeaponEquiped())
			return;

		for(CursedWeapon cw : _cursedWeapons.values())
		{
			if(cw.isActive())
			{
				continue;
			}

			if(cw.checkDrop(attackable, player))
			{
				break;
			}
		}
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if(player.isCursedWeaponEquiped()) // cannot own 2 cursed swords
		{
			CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquipedId());
			/* TODO: give the bonus level in a more appropriate manner.
			 *  The following code adds "_stageKills" levels.  This will also show in the char status.
			 * I do not have enough info to know if the bonus should be shown in the pk count, or if it
			 * should be a full "_stageKills" bonus or just the remaining from the current count till the
			 * of the current stage...
			 * This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as
			 * little change in the code as possible, until proper info arises.
			 */
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();

			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
		}
		else
		{
			cw.activate(player, item);
		}

		cw = null;
	}

	public void drop(int itemId, L2Character killer)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		cw.dropIt(killer);
		cw = null;
	}

	public void increaseKills(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		cw.increaseKills();
		cw = null;
	}

	public int getLevel(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		return cw.getLevel();
	}

	public static void announce(SystemMessage sm)
	{
		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if(player == null)
			{
				continue;
			}

			player.sendPacket(sm);
		}
		if(Config.DEBUG)
		{
			_log.info("MessageID: " + sm.getMessageID());
		}
	}

	public void checkPlayer(L2PcInstance player)
	{
		if(player == null)
			return;

		for(CursedWeapon cw : _cursedWeapons.values())
		{
			if(cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquipedId(cw.getItemId());

				SystemMessage sm = new SystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
				sm.addString(cw.getName());
				//sm.addItemName(cw.getItemId());
				sm.addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
				sm = null;
			}
		}
	}

	public static void removeFromDb(int itemId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete datas
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();

			statement.close();
			statement = null;
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.severe("CursedWeaponsManager: Failed to remove data: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) {
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
	}

	public void saveData()
	{
		for(CursedWeapon cw : _cursedWeapons.values())
		{
			cw.saveData();
		}
	}

	// =========================================================
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}

	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}

	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}

	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}

	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch(Exception e)
		{
			/***/
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}
}
