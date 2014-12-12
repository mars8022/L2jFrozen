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
package com.l2jfrozen.gameserver.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController
{
	private static Logger LOGGER = Logger.getLogger(TradeController.class);
	private static TradeController _instance;
	
	private int _nextListId;
	private final Map<Integer, L2TradeList> _lists;
	private final Map<Integer, L2TradeList> _listsTaskItem;
	
	/** Task launching the function for restore count of Item (Clan Hall) */
	public class RestoreCount implements Runnable
	{
		private final int _timer;
		
		public RestoreCount(final int time)
		{
			_timer = time;
		}
		
		@Override
		public void run()
		{
			try
			{
				restoreCount(_timer);
				dataTimerSave(_timer);
				ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(_timer), (long) _timer * 60 * 60 * 1000);
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					t.printStackTrace();
			}
		}
	}
	
	public static TradeController getInstance()
	{
		if (_instance == null)
		{
			_instance = new TradeController();
		}
		return _instance;
	}
	
	private TradeController()
	{
		_lists = new FastMap<>();
		_listsTaskItem = new FastMap<>();
		final File buylistData = new File(Config.DATAPACK_ROOT, "data/buylists.csv");
		
		if (buylistData.exists())
		{
			LOGGER.warn("Do, please, remove buylists from data folder and use SQL buylist instead");
			String line = null;
			int dummyItemCount = 0;
			
			FileReader reader = null;
			BufferedReader buff = null;
			LineNumberReader lnr = null;
			
			try
			{
				reader = new FileReader(buylistData);
				buff = new BufferedReader(reader);
				lnr = new LineNumberReader(buff);
				
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().length() == 0 || line.startsWith("#"))
					{
						continue;
					}
					dummyItemCount += parseList(line);
				}
				
				if (Config.DEBUG)
				{
					LOGGER.debug("created " + dummyItemCount + " Dummy-Items for buylists");
				}
				
				LOGGER.info("TradeController: Loaded " + _lists.size() + " Buylists.");
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.warn("error while creating trade controller in line: " + (lnr == null ? 0 : lnr.getLineNumber()), e);
				
			}
			finally
			{
				
				if (lnr != null)
					try
					{
						lnr.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
				if (buff != null)
					try
					{
						buff.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
				if (reader != null)
					try
					{
						reader.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
			}
			
		}
		else
		{
			LOGGER.debug("No buylists were found in data folder, using SQL buylist instead");
			Connection con = null;
			
			/**
			 * Initialize Shop buylist
			 */
			int dummyItemCount = 0;
			boolean LimitedItem = false;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement1 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"shop_id",
					"npc_id"
				}) + " FROM merchant_shopids");
				
				ResultSet rset1 = statement1.executeQuery();
				
				while (rset1.next())
				{
					PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
						"item_id",
						"price",
						"shop_id",
						"order",
						"count",
						"time",
						"currentCount"
					}) + " FROM merchant_buylists WHERE shop_id=? ORDER BY " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
						"order"
					}) + " ASC");
					
					statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
					ResultSet rset = statement.executeQuery();
					if (rset.next())
					{
						LimitedItem = false;
						dummyItemCount++;
						L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
						
						int itemId = rset.getInt("item_id");
						int price = rset.getInt("price");
						int count = rset.getInt("count");
						int currentCount = rset.getInt("currentCount");
						int time = rset.getInt("time");
						
						L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
						
						if (item == null)
						{
							DatabaseUtils.close(rset);
							DatabaseUtils.close(statement);
							
							rset = null;
							statement = null;
							continue;
						}
						
						if (count > -1)
						{
							item.setCountDecrease(true);
							LimitedItem = true;
						}
						
						if (!rset1.getString("npc_id").equals("gm") && price < (item.getReferencePrice() / 2))
						{
							
							LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
							price = item.getReferencePrice();
						}
						
						item.setPriceToSell(price);
						item.setTime(time);
						item.setInitCount(count);
						
						if (currentCount > -1)
						{
							item.setCount(currentCount);
						}
						else
						{
							item.setCount(count);
						}
						
						buy1.addItem(item);
						item = null;
						buy1.setNpcId(rset1.getString("npc_id"));
						
						try
						{
							while (rset.next()) // TODO aici
							{
								dummyItemCount++;
								itemId = rset.getInt("item_id");
								price = rset.getInt("price");
								count = rset.getInt("count");
								time = rset.getInt("time");
								currentCount = rset.getInt("currentCount");
								final L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
								
								if (item2 == null)
								{
									continue;
								}
								
								if (count > -1)
								{
									item2.setCountDecrease(true);
									LimitedItem = true;
								}
								
								if (!rset1.getString("npc_id").equals("gm") && price < item2.getReferencePrice() / 2)
								{
									
									LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
									price = item2.getReferencePrice();
								}
								
								item2.setPriceToSell(price);
								
								item2.setTime(time);
								item2.setInitCount(count);
								if (currentCount > -1)
								{
									item2.setCount(currentCount);
								}
								else
								{
									item2.setCount(count);
								}
								buy1.addItem(item2);
							}
						}
						catch (final Exception e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
								e.printStackTrace();
							
							LOGGER.warn("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
						}
						if (LimitedItem)
						{
							_listsTaskItem.put(new Integer(buy1.getListId()), buy1);
						}
						else
						{
							_lists.put(new Integer(buy1.getListId()), buy1);
						}
						
						_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
						buy1 = null;
					}
					
					DatabaseUtils.close(rset);
					DatabaseUtils.close(statement);
					
					rset = null;
					statement = null;
				}
				rset1.close();
				statement1.close();
				
				rset1 = null;
				statement1 = null;
				
				if (Config.DEBUG)
				{
					LOGGER.debug("created " + dummyItemCount + " Dummy-Items for buylists");
				}
				
				LOGGER.info("TradeController: Loaded " + _lists.size() + " Buylists.");
				LOGGER.info("TradeController: Loaded " + _listsTaskItem.size() + " Limited Buylists.");
				/*
				 * Restore Task for reinitialyze count of buy item
				 */
				try
				{
					int time = 0;
					long savetimer = 0;
					final long currentMillis = System.currentTimeMillis();
					
					PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM merchant_buylists WHERE time <> 0 ORDER BY time");
					ResultSet rset2 = statement2.executeQuery();
					
					while (rset2.next())
					{
						time = rset2.getInt("time");
						savetimer = rset2.getLong("savetimer");
						if (savetimer - currentMillis > 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), savetimer - System.currentTimeMillis());
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), 0);
						}
					}
					rset2.close();
					statement2.close();
					
					rset2 = null;
					statement2 = null;
				}
				catch (final Exception e)
				{
					LOGGER.warn("TradeController: Could not restore Timer for Item count.");
					e.printStackTrace();
				}
			}
			catch (final Exception e)
			{
				// problem with initializing spawn, go to next one
				LOGGER.warn("TradeController: Buylists could not be initialized.");
				e.printStackTrace();
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
			/*
			 * If enabled, initialize the custom buylist
			 */
			if (Config.CUSTOM_MERCHANT_TABLES)// Custom merchat Tabels
			{
				try
				{
					final int initialSize = _lists.size();
					con = L2DatabaseFactory.getInstance().getConnection(false);
					
					PreparedStatement statement1 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
					{
						"shop_id",
						"npc_id"
					}) + " FROM custom_merchant_shopids");
					
					ResultSet rset1 = statement1.executeQuery();
					
					while (rset1.next())
					{
						PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
						{
							"item_id",
							"price",
							"shop_id",
							"order",
							"count",
							"time",
							"currentCount"
						}) + " FROM custom_merchant_buylists WHERE shop_id=? ORDER BY " + L2DatabaseFactory.getInstance().safetyString(new String[]
						{
							"order"
						}) + " ASC");
						
						statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
						ResultSet rset = statement.executeQuery();
						
						if (rset.next())
						{
							LimitedItem = false;
							dummyItemCount++;
							L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
							int itemId = rset.getInt("item_id");
							int price = rset.getInt("price");
							int count = rset.getInt("count");
							int currentCount = rset.getInt("currentCount");
							int time = rset.getInt("time");
							L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
							if (item == null)
							{
								DatabaseUtils.close(rset);
								DatabaseUtils.close(statement);
								
								rset = null;
								statement = null;
								continue;
							}
							
							if (count > -1)
							{
								item.setCountDecrease(true);
								LimitedItem = true;
							}
							
							if (!rset1.getString("npc_id").equals("gm") && price < (item.getReferencePrice() / 2))
							{
								
								LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
								price = item.getReferencePrice();
							}
							
							item.setPriceToSell(price);
							item.setTime(time);
							item.setInitCount(count);
							
							if (currentCount > -1)
							{
								item.setCount(currentCount);
							}
							else
							{
								item.setCount(count);
							}
							
							buy1.addItem(item);
							item = null;
							buy1.setNpcId(rset1.getString("npc_id"));
							
							try
							{
								while (rset.next())
								{
									dummyItemCount++;
									itemId = rset.getInt("item_id");
									price = rset.getInt("price");
									count = rset.getInt("count");
									time = rset.getInt("time");
									currentCount = rset.getInt("currentCount");
									L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
									if (item2 == null)
									{
										continue;
									}
									if (count > -1)
									{
										item2.setCountDecrease(true);
										LimitedItem = true;
									}
									
									if (!rset1.getString("npc_id").equals("gm") && price < item2.getReferencePrice() / 2)
									{
										
										LOGGER.warn("L2TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
										price = item2.getReferencePrice();
									}
									
									item2.setPriceToSell(price);
									item2.setTime(time);
									item2.setInitCount(count);
									if (currentCount > -1)
									{
										item2.setCount(currentCount);
									}
									else
									{
										item2.setCount(count);
									}
									buy1.addItem(item2);
									
									item2 = null;
								}
							}
							catch (final Exception e)
							{
								if (Config.ENABLE_ALL_EXCEPTIONS)
									e.printStackTrace();
								
								LOGGER.warn("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
							}
							if (LimitedItem)
							{
								_listsTaskItem.put(new Integer(buy1.getListId()), buy1);
							}
							else
							{
								_lists.put(new Integer(buy1.getListId()), buy1);
							}
							_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
							
							buy1 = null;
						}
						
						DatabaseUtils.close(rset);
						DatabaseUtils.close(statement);
						
						rset = null;
						statement = null;
					}
					rset1.close();
					statement1.close();
					
					rset1 = null;
					statement1 = null;
					
					if (Config.DEBUG)
					{
						LOGGER.debug("created " + dummyItemCount + " Dummy-Items for buylists");
					}
					
					LOGGER.info("TradeController: Loaded " + (_lists.size() - initialSize) + " Custom Buylists.");
					
					/**
					 * Restore Task for reinitialyze count of buy item
					 */
					try
					{
						int time = 0;
						long savetimer = 0;
						final long currentMillis = System.currentTimeMillis();
						
						PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM custom_merchant_buylists WHERE time <> 0 ORDER BY time");
						ResultSet rset2 = statement2.executeQuery();
						
						while (rset2.next())
						{
							time = rset2.getInt("time");
							savetimer = rset2.getLong("savetimer");
							if (savetimer - currentMillis > 0)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), savetimer - System.currentTimeMillis());
							}
							else
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), 0);
							}
						}
						rset2.close();
						statement2.close();
						
						rset2 = null;
						statement2 = null;
						
					}
					catch (final Exception e)
					{
						LOGGER.warn("TradeController: Could not restore Timer for Item count.");
						e.printStackTrace();
					}
				}
				catch (final Exception e)
				{
					// problem with initializing spawn, go to next one
					LOGGER.warn("TradeController: Buylists could not be initialized.");
					e.printStackTrace();
				}
				finally
				{
					CloseUtil.close(con);
					con = null;
				}
			}
		}
	}
	
	private int parseList(final String line)
	{
		int itemCreated = 0;
		StringTokenizer st = new StringTokenizer(line, ";");
		
		final int listId = Integer.parseInt(st.nextToken());
		L2TradeList buy1 = new L2TradeList(listId);
		while (st.hasMoreTokens())
		{
			final int itemId = Integer.parseInt(st.nextToken());
			int price = Integer.parseInt(st.nextToken());
			final L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
			
			if (price < (item.getReferencePrice() / 2))
			{
				
				LOGGER.warn("L2TradeList " + listId + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
				price = item.getReferencePrice();
			}
			
			item.setPriceToSell(price);
			buy1.addItem(item);
			itemCreated++;
		}
		st = null;
		
		_lists.put(new Integer(buy1.getListId()), buy1);
		buy1 = null;
		return itemCreated;
	}
	
	public L2TradeList getBuyList(final int listId)
	{
		if (_lists.get(new Integer(listId)) != null)
			return _lists.get(new Integer(listId));
		
		return _listsTaskItem.get(new Integer(listId));
	}
	
	public List<L2TradeList> getBuyListByNpcId(final int npcId)
	{
		final List<L2TradeList> lists = new FastList<>();
		
		for (final L2TradeList list : _lists.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		for (final L2TradeList list : _listsTaskItem.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		return lists;
	}
	
	protected void restoreCount(final int time)
	{
		if (_listsTaskItem == null)
			return;
		
		for (final L2TradeList list : _listsTaskItem.values())
		{
			list.restoreCount(time);
		}
	}
	
	protected void dataTimerSave(final int time)
	{
		Connection con = null;
		final long timerSave = System.currentTimeMillis() + (long) time * 60 * 60 * 1000;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer =? WHERE time =?");
			statement.setLong(1, timerSave);
			statement.setInt(2, time);
			statement.executeUpdate();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.error("TradeController: Could not update Timer save in Buylist");
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public void dataCountStore()
	{
		Connection con = null;
		PreparedStatement statement;
		
		int listId;
		
		if (_listsTaskItem == null)
			return;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			
			for (final L2TradeList list : _listsTaskItem.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				
				for (final L2ItemInstance Item : list.getItems())
				{
					if (Item.getCount() < Item.getInitCount()) // needed?
					{
						statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?");
						statement.setInt(1, Item.getCount());
						statement.setInt(2, Item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
						DatabaseUtils.close(statement);
						statement = null;
					}
				}
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			LOGGER.error("TradeController: Could not store Count Item");
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * @return
	 */
	public synchronized int getNextId()
	{
		return _nextListId++;
	}
	
	/**
	 * This will reload buylists info from DataBase
	 */
	public static void reload()
	{
		_instance = new TradeController();
	}
}
