package com.l2jfrozen.gameserver.powerpak.Buffer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class BuffTable
{
	private final Logger LOGGER = Logger.getLogger(BuffTable.class);
	
	public class Buff
	{
		public int _skillId;
		public int _skillLevel;
		public boolean _force;
		public int _minLevel;
		public int _maxLevel;
		public int _price;
		public L2Skill _skill;
		
		public Buff(final ResultSet r) throws SQLException
		{
			_skillId = r.getInt(2);
			_skillLevel = r.getInt(3);
			_force = r.getInt(4) == 1;
			_minLevel = r.getInt(5);
			_maxLevel = r.getInt(6);
			_price = r.getInt(7);
			if (_price == -1)
			{
				_price = PowerPakConfig.BUFFER_PRICE;
			}
			
			_skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
		}
		
		/**
		 * @param player
		 * @return Returns the result of level check
		 */
		public boolean checkLevel(final L2PcInstance player)
		{
			return (_minLevel == 0 || player.getLevel() >= _minLevel) && (_maxLevel == 0 || player.getLevel() <= _maxLevel);
		}
		
		/**
		 * @param player
		 * @return Returns the result of price check
		 */
		public boolean checkPrice(final L2PcInstance player)
		{
			return (_price == 0 || player.getInventory().getAdena() >= _price);
		}
	}
	
	private static BuffTable _instance = null;
	private final Map<String, ArrayList<Buff>> _buffs;
	private final Map<Integer, ArrayList<Buff>> _buffs_by_id;
	
	public static BuffTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new BuffTable();
		}
		return _instance;
	}
	
	private BuffTable()
	{
		_buffs = new FastMap<>();
		_buffs_by_id = new FastMap<>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement stm = con.prepareStatement("select name,skill_id,skill_level,skill_force,char_min_level,char_max_level,price_adena,id from buff_templates");
			final ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				if (_buffs.get(rs.getString(1)) == null)
				{
					_buffs.put(rs.getString(1), new ArrayList<Buff>());
					
				}
				
				if (_buffs_by_id.get(rs.getInt(8)) == null)
				{
					_buffs_by_id.put(rs.getInt(8), new ArrayList<Buff>());
				}
				
				final ArrayList<Buff> a = _buffs.get(rs.getString(1));
				final ArrayList<Buff> b = _buffs_by_id.get(rs.getInt(8));
				final Buff new_buff = new Buff(rs);
				a.add(new_buff);
				b.add(new_buff);
				
			}
			rs.close();
			stm.close();
			LOGGER.info("Loaded " + _buffs_by_id.size() + " buff templates");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			LOGGER.info("...Error while loading buffs. Please, check buff_templates table");
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public ArrayList<Buff> getBuffsForName(final String name)
	{
		ArrayList<Buff> output = new ArrayList<>();
		if ((name == null) || name.equals("all"))
		{
			for (final ArrayList<Buff> actual : _buffs.values())
			{
				output.addAll(actual);
			}
		}
		else
		{
			if (_buffs.get(name) != null)
			{
				output = _buffs.get(name);
			}
		}
		return output;
	}
	
	public ArrayList<Buff> getBuffsForID(final Integer id)
	{
		return _buffs_by_id.get(id);
	}
	
	public Iterator<String> skill_groups()
	{
		return _buffs.keySet().iterator();
	}
}
