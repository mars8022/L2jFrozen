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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.thread.daemons.ItemsAutoDestroy;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class manage all items on ground
 * 
 * @version $Revision: $ $Date: $
 * @author DiezelMax - original ideea
 * @author Enforcer - actual build
 */
public class ItemsOnGroundManager
{
	static final Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());
	protected List<L2ItemInstance> _items = new FastList<L2ItemInstance>();

	private ItemsOnGroundManager()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if(!Config.SAVE_DROPPED_ITEM)
		{
			if(Config.CLEAR_DROPPED_ITEM_TABLE)
				emptyTable();
			
			return;
		}
		
		_log.info("Initializing ItemsOnGroundManager");
        		
		_items.clear();
		load();

		if(!Config.SAVE_DROPPED_ITEM)
			return;
		
		if(Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StoreInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
	}

	public static final ItemsOnGroundManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		// if DestroyPlayerDroppedItem was previously  false, items curently protected will be added to ItemsAutoDestroy
		if(Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			Connection con = null;
			try
			{
				String str = null;
				if(!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1 and equipable=0";
				}
				else if(Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1";
				}

				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
				str = null;
				statement = null;
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error while updating table ItemsOnGround " + e);
				e.printStackTrace();
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}

		//Add items to world
		Connection con = null;
		try
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				Statement s = con.createStatement();
				ResultSet result;
				int count = 0;
				result = s.executeQuery("select object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable from itemsonground");
				while(result.next())
				{
					L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
					L2World.getInstance().storeObject(item);
					if(item.isStackable() && result.getInt(3) > 1)
					{
						item.setCount(result.getInt(3));
					}

					if(result.getInt(4) > 0)
					{
						item.setEnchantLevel(result.getInt(4));
					}

					item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6), result.getInt(7));
					item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
					item.getPosition().getWorldRegion().addVisibleObject(item);
					item.setDropTime(result.getLong(8));
					if(result.getLong(8) == -1)
					{
						item.setProtected(true);
					}
					else
					{
						item.setProtected(false);
					}

					item.setIsVisible(true);
					L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
					_items.add(item);
					count++;
					// add to ItemsAutoDestroy only items not protected
					if(!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
					{
						if(result.getLong(8) > -1)
						{
							if(Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB)
							{
								ItemsAutoDestroy.getInstance().addItem(item);
							}
						}
					}
					item = null;
				}

				result.close();
				s.close();
				result = null;
				s = null;

				if(count > 0)
				{
					System.out.println("ItemsOnGroundManager: restored " + count + " items.");
				}
				else
				{
					System.out.println("Initializing ItemsOnGroundManager.");
				}
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error while loading ItemsOnGround " + e);
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		if(Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}

	public void save(L2ItemInstance item)
	{
		if(!Config.SAVE_DROPPED_ITEM)
			return;
		
		_items.add(item);
	}

	public void removeObject(L2Object item)
	{
		if(!Config.SAVE_DROPPED_ITEM)
			return;
		
		_items.remove(item);
	}

	public void saveInDb()
	{
		if(!Config.SAVE_DROPPED_ITEM)
			return;
		
		ThreadPoolManager.getInstance().executeTask(new StoreInDb());
	}

	public void cleanUp()
	{
		_items.clear();
	}

	public void emptyTable()
	{
		Connection conn = null;
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement del = conn.prepareStatement("delete from itemsonground");
			del.execute();
			del.close();
			del = null;
		}
		catch(Exception e1)
		{
			_log.log(Level.SEVERE, "error while cleaning table ItemsOnGround " + e1);
			e1.printStackTrace();
		}
		finally
		{
			CloseUtil.close(conn);
			conn = null;
		}
	}

	protected class StoreInDb extends Thread
	{
		@Override
		public void run()
		{
			emptyTable();

			if(_items.isEmpty())
			{
				if(Config.DEBUG)
				{
					_log.warning("ItemsOnGroundManager: nothing to save...");
				}
				return;
			}

			for(L2ItemInstance item : _items)
			{

				if(CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					continue; // Cursed Items not saved to ground, prevent double save
				}

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(false);
					PreparedStatement statement = con.prepareStatement("insert into itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) values(?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());

					if(item.isProtected())
					{
						statement.setLong(8, -1); //item will be protected
					}
					else
					{
						statement.setLong(8, item.getDropTime()); //item will be added to ItemsAutoDestroy
					}
					if(item.isEquipable())
					{
						statement.setLong(9, 1); //set equipable
					}
					else
					{
						statement.setLong(9, 0);
					}
					statement.execute();
					statement.close();
					statement = null;
				}
				catch(Exception e)
				{
					_log.log(Level.SEVERE, "error while inserting into table ItemsOnGround " + e);
					e.printStackTrace();
				}
				finally
				{
					CloseUtil.close(con);
				}
			}
			if(Config.DEBUG)
			{
				_log.warning("ItemsOnGroundManager: " + _items.size() + " items on ground saved");
			}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager _instance = new ItemsOnGroundManager();
	}
}
