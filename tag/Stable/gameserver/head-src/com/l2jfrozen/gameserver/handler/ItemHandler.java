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
package com.l2jfrozen.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.handler.itemhandlers.BeastSoulShot;
import com.l2jfrozen.gameserver.handler.itemhandlers.BeastSpice;
import com.l2jfrozen.gameserver.handler.itemhandlers.BeastSpiritShot;
import com.l2jfrozen.gameserver.handler.itemhandlers.BlessedSpiritShot;
import com.l2jfrozen.gameserver.handler.itemhandlers.Book;
import com.l2jfrozen.gameserver.handler.itemhandlers.BreakingArrow;
import com.l2jfrozen.gameserver.handler.itemhandlers.CharChangePotions;
import com.l2jfrozen.gameserver.handler.itemhandlers.ChestKey;
import com.l2jfrozen.gameserver.handler.itemhandlers.ChristmasTree;
import com.l2jfrozen.gameserver.handler.itemhandlers.CrystalCarol;
import com.l2jfrozen.gameserver.handler.itemhandlers.Crystals;
import com.l2jfrozen.gameserver.handler.itemhandlers.CustomPotions;
import com.l2jfrozen.gameserver.handler.itemhandlers.EnchantScrolls;
import com.l2jfrozen.gameserver.handler.itemhandlers.EnergyStone;
import com.l2jfrozen.gameserver.handler.itemhandlers.ExtractableItems;
import com.l2jfrozen.gameserver.handler.itemhandlers.Firework;
import com.l2jfrozen.gameserver.handler.itemhandlers.FishShots;
import com.l2jfrozen.gameserver.handler.itemhandlers.Harvester;
import com.l2jfrozen.gameserver.handler.itemhandlers.HeroCustomItem;
import com.l2jfrozen.gameserver.handler.itemhandlers.JackpotSeed;
import com.l2jfrozen.gameserver.handler.itemhandlers.MOSKey;
import com.l2jfrozen.gameserver.handler.itemhandlers.MapForestOfTheDead;
import com.l2jfrozen.gameserver.handler.itemhandlers.Maps;
import com.l2jfrozen.gameserver.handler.itemhandlers.MercTicket;
import com.l2jfrozen.gameserver.handler.itemhandlers.MysteryPotion;
import com.l2jfrozen.gameserver.handler.itemhandlers.Nectar;
import com.l2jfrozen.gameserver.handler.itemhandlers.NobleCustomItem;
import com.l2jfrozen.gameserver.handler.itemhandlers.PaganKeys;
import com.l2jfrozen.gameserver.handler.itemhandlers.Potions;
import com.l2jfrozen.gameserver.handler.itemhandlers.Recipes;
import com.l2jfrozen.gameserver.handler.itemhandlers.Remedy;
import com.l2jfrozen.gameserver.handler.itemhandlers.RollingDice;
import com.l2jfrozen.gameserver.handler.itemhandlers.ScrollOfEscape;
import com.l2jfrozen.gameserver.handler.itemhandlers.ScrollOfResurrection;
import com.l2jfrozen.gameserver.handler.itemhandlers.Scrolls;
import com.l2jfrozen.gameserver.handler.itemhandlers.Seed;
import com.l2jfrozen.gameserver.handler.itemhandlers.SevenSignsRecord;
import com.l2jfrozen.gameserver.handler.itemhandlers.SoulCrystals;
import com.l2jfrozen.gameserver.handler.itemhandlers.SoulShots;
import com.l2jfrozen.gameserver.handler.itemhandlers.SpecialXMas;
import com.l2jfrozen.gameserver.handler.itemhandlers.SpiritShot;
import com.l2jfrozen.gameserver.handler.itemhandlers.SummonItems;

/**
 * This class manages handlers of items
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class ItemHandler
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private static ItemHandler _instance;

	private Map<Integer, IItemHandler> _datatable;

	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 * 
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new ItemHandler();
		}

		return _instance;
	}

	/**
	 * Returns the number of elements contained in datatable
	 * 
	 * @return int : Size of the datatable
	 */
	public int size()
	{
		return _datatable.size();
	}

	/**
	 * Constructor of ItemHandler
	 */
	private ItemHandler()
	{
		_datatable = new TreeMap<Integer, IItemHandler>();
		registerItemHandler(new ScrollOfEscape());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new ChestKey());
		registerItemHandler(new CustomPotions());
		registerItemHandler(new PaganKeys());
		registerItemHandler(new Maps());
		registerItemHandler(new MapForestOfTheDead());
		registerItemHandler(new Potions());
		registerItemHandler(new Recipes());
		registerItemHandler(new RollingDice());
		registerItemHandler(new MysteryPotion());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new EnergyStone());
		registerItemHandler(new Book());
		registerItemHandler(new Remedy());
		registerItemHandler(new Scrolls());
		registerItemHandler(new CrystalCarol());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new CharChangePotions());
		registerItemHandler(new Firework());
		registerItemHandler(new Seed());
		registerItemHandler(new Harvester());
		registerItemHandler(new MercTicket());
		registerItemHandler(new Nectar());
		registerItemHandler(new FishShots());
		registerItemHandler(new ExtractableItems());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new SummonItems());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new JackpotSeed());
		registerItemHandler(new NobleCustomItem());
		registerItemHandler(new HeroCustomItem());
		registerItemHandler(new MOSKey());
		registerItemHandler(new BreakingArrow());
		registerItemHandler(new ChristmasTree());
		registerItemHandler(new Crystals());
		_log.config("ItemHandler: Loaded " + _datatable.size() + " handlers.");
	}

	/**
	 * Adds handler of item type in <I>datatable</I>.<BR>
	 * <BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item
	 * type (existing in classes of package itemhandlers) sets as key of the Map.
	 * 
	 * @param handler (IItemHandler)
	 */
	public void registerItemHandler(IItemHandler handler)
	{
		// Get all ID corresponding to the item type of the handler
		int[] ids = handler.getItemIds();

		// Add handler for each ID found
		for(int id : ids)
		{
			_datatable.put(new Integer(id), handler);
		}
	}

	/**
	 * Returns the handler of the item
	 * 
	 * @param itemId : int designating the itemID
	 * @return IItemHandler
	 */
	public IItemHandler getItemHandler(int itemId)
	{
		return _datatable.get(new Integer(itemId));
	}
}
