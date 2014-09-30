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
package com.l2jfrozen.gameserver.model.multisell;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MultiSellList;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;

/**
 * Multisell list manager
 * 
 * @author programmos
 */
public class L2Multisell
{
	private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
	private List<MultiSellListContainer> _entries = new FastList<MultiSellListContainer>();
	private static L2Multisell _instance;/* = new L2Multisell();*/

	public MultiSellListContainer getList(int id)
	{
		synchronized (_entries)
		{
			for(MultiSellListContainer list : _entries)
			{
				if(list.getListId() == id)
					return list;
			}
		}

		_log.warning("[L2Multisell] can't find list with id: " + id);
		return null;
	}

	private L2Multisell()
	{
		parseData();
	}

	public void reload()
	{
		parseData();
	}

	public static L2Multisell getInstance()
	{
		if(_instance==null)
			_instance = new L2Multisell();
		return _instance;
	}

	private void parseData()
	{
		_entries.clear();
		parse();
	}

	/**
	 * This will generate the multisell list for the items. There exist various parameters in multisells that affect the
	 * way they will appear: 1) inventory only: * if true, only show items of the multisell for which the "primary"
	 * ingredients are already in the player's inventory. By "primary" ingredients we mean weapon and armor. * if false,
	 * show the entire list. 2) maintain enchantment: presumably, only lists with "inventory only" set to true should
	 * sometimes have this as true. This makes no sense otherwise... * If true, then the product will match the
	 * enchantment level of the ingredient. if the player has multiple items that match the ingredient list but the
	 * enchantment levels differ, then the entries need to be duplicated to show the products and ingredients for each
	 * enchantment level. For example: If the player has a crystal staff +1 and a crystal staff +3 and goes to exchange
	 * it at the mammon, the list should have all exchange possibilities for the +1 staff, followed by all possibilities
	 * for the +3 staff. * If false, then any level ingredient will be considered equal and product will always be at +0
	 * 3) apply taxes: Uses the "taxIngredient" entry in order to add a certain amount of adena to the ingredients
	 * @param listId 
	 * @param inventoryOnly 
	 * @param player 
	 * @param taxRate 
	 * @return 
	 */
	private MultiSellListContainer generateMultiSell(int listId, boolean inventoryOnly, L2PcInstance player, double taxRate)
	{
		MultiSellListContainer listTemplate = L2Multisell.getInstance().getList(listId);
		MultiSellListContainer list = new MultiSellListContainer();

		if(listTemplate == null)
			return list;

		list = new MultiSellListContainer();
		list.setListId(listId);

		if(inventoryOnly)
		{
			if(player == null)
				return list;

			L2ItemInstance[] items;

			if(listTemplate.getMaintainEnchantment())
			{
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false, true);
			}
			else
			{
				items = player.getInventory().getUniqueItems(false, false, false, true);
			}

			int enchantLevel;
			for(L2ItemInstance item : items)
			{
				// only do the matchup on equipable items that are not currently equipped
				// so for each appropriate item, produce a set of entries for the multisell list.
				if(!item.isWear() && (item.getItem() instanceof L2Armor || item.getItem() instanceof L2Weapon))
				{
					enchantLevel = listTemplate.getMaintainEnchantment() ? item.getEnchantLevel() : 0;
					// loop through the entries to see which ones we wish to include
					for(MultiSellEntry ent : listTemplate.getEntries())
					{
						boolean doInclude = false;

						// check ingredients of this entry to see if it's an entry we'd like to include.
						for(MultiSellIngredient ing : ent.getIngredients())
						{
							if(item.getItemId() == ing.getItemId())
							{
								doInclude = true;
								break;
							}
						}

						// manipulate the ingredients of the template entry for this particular instance shown
						// i.e: Assign enchant levels and/or apply taxes as needed.
						if(doInclude)
						{
							list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.getMaintainEnchantment(), enchantLevel, taxRate));
						}
					}
				}
			} // end for each inventory item.

			items = null;
		} // end if "inventory-only"
		else
		// this is a list-all type
		{
			// if no taxes are applied, no modifications are needed
			for(MultiSellEntry ent : listTemplate.getEntries())
			{
				list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0, taxRate));
			}
		}

		listTemplate = null;

		return list;
	}

	// Regarding taxation, the following is the case:
	// a) The taxes come out purely from the adena TaxIngredient
	// b) If the entry has no adena ingredients other than the taxIngredient, the resulting
	//    amount of adena is appended to the entry
	// c) If the entry already has adena as an entry, the taxIngredient is used in order to increase
	//	  the count for the existing adena ingredient
	private MultiSellEntry prepareEntry(MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel, double taxRate)
	{
		MultiSellEntry newEntry = new MultiSellEntry();
		newEntry.setEntryId(templateEntry.getEntryId() * 100000 + enchantLevel);

		int adenaAmount = 0;

		for(MultiSellIngredient ing : templateEntry.getIngredients())
		{
			// load the ingredient from the template
			MultiSellIngredient newIngredient = new MultiSellIngredient(ing);

			// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
			if(ing.getItemId() == 57 && ing.isTaxIngredient())
			{
				if(applyTaxes)
				{
					adenaAmount += (int) Math.round(ing.getItemCount() * taxRate);
				}
				continue; // do not adena yet, as non-taxIngredient adena entries might occur next (order not guaranteed)
			}
			else if(ing.getItemId() == 57) // && !ing.isTaxIngredient()
			{
				adenaAmount += ing.getItemCount();
				continue; // do not adena yet, as taxIngredient adena entries might occur next (order not guaranteed)
			}
			// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
			else if(maintainEnchantment)
			{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
				if(tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
				}

				tempItem = null;
			}

			// finally, add this ingredient to the entry
			newEntry.addIngredient(newIngredient);
			newIngredient = null;
		}

		// now add the adena, if any.
		if(adenaAmount > 0)
		{
			newEntry.addIngredient(new MultiSellIngredient(57, adenaAmount, 0, false, false));
		}

		// Now modify the enchantment level of products, if necessary
		for(MultiSellIngredient ing : templateEntry.getProducts())
		{
			// load the ingredient from the template
			MultiSellIngredient newIngredient = new MultiSellIngredient(ing);

			if(maintainEnchantment)
			{
				// if it is an armor/weapon, modify the enchantment level appropriately
				// (note, if maintain enchantment is "false" this modification will result to a +0)
				L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();

				if(tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
			}

			newEntry.addProduct(newIngredient);
			newIngredient = null;
		}

		return newEntry;
	}

	public void SeparateAndSend(int listId, L2PcInstance player, boolean inventoryOnly, double taxRate)
	{
		MultiSellListContainer list = generateMultiSell(listId, inventoryOnly, player, taxRate);
		MultiSellListContainer temp = new MultiSellListContainer();

		int page = 1;

		temp.setListId(list.getListId());

		for(MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == 40)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				page++;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}

			temp.addEntry(e);
		}

		player.setMultiSellId(listId);
		
		player.sendPacket(new MultiSellList(temp, page, 1));

		list = null;
		temp = null;
	}

	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);

		if(!dir.exists())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}

		File[] files = dir.listFiles();

		for(File f : files)
		{
			if(f.getName().endsWith(".xml"))
			{
				hash.add(f);
			}
		}

	}

	private void parse()
	{
		Document doc = null;

		int id = 0;

		List<File> files = new FastList<File>();
		hashFiles("multisell", files);

		for(File f : files)
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
			try
			{

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
				factory = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "Error loading file " + f, e);
			}
			try
			{
				MultiSellListContainer list = parseDocument(doc);
				list.setListId(id);
				_entries.add(list);
				list = null;
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.SEVERE, "Error in file " + f, e);
			}
		}

		files = null;
		doc = null;
	}

	protected MultiSellListContainer parseDocument(Document doc)
	{
		MultiSellListContainer list = new MultiSellListContainer();

		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;
				attribute = n.getAttributes().getNamedItem("applyTaxes");

				if(attribute == null)
				{
					list.setApplyTaxes(false);
				}
				else
				{
					list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
				}

				attribute = n.getAttributes().getNamedItem("maintainEnchantment");

				if(attribute == null)
				{
					list.setMaintainEnchantment(false);
				}
				else
				{
					list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
				}

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						MultiSellEntry e = parseEntry(d);
						list.addEntry(e);
					}
				}

				attribute = null;
			}
			else if("item".equalsIgnoreCase(n.getNodeName()))
			{
				MultiSellEntry e = parseEntry(n);
				list.addEntry(e);
			}
		}

		return list;
	}

	protected MultiSellEntry parseEntry(Node n)
	{
		int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());

		Node first = n.getFirstChild();
		MultiSellEntry entry = new MultiSellEntry();

		for(n = first; n != null; n = n.getNextSibling())
		{
			if("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;

				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				boolean isTaxIngredient = false, mantainIngredient = false;

				attribute = n.getAttributes().getNamedItem("isTaxIngredient");

				if(attribute != null)
				{
					isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}

				attribute = n.getAttributes().getNamedItem("mantainIngredient");

				if(attribute != null)
				{
					mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}

				MultiSellIngredient e = new MultiSellIngredient(id, count, isTaxIngredient, mantainIngredient);
				entry.addIngredient(e);
				e = null;
				attribute = null;
			}
			else if("production".equalsIgnoreCase(n.getNodeName()))
			{
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0;
				// By Azagthtot support enchantment in multisell
				if(n.getAttributes().getNamedItem("enchant") != null)
				{
					enchant = Integer.parseInt(n.getAttributes().getNamedItem("enchant").getNodeValue());
				}
				MultiSellIngredient e = new MultiSellIngredient(id, count, enchant, false, false);
				entry.addProduct(e);
				e = null;
			}
		}

		entry.setEntryId(entryId);

		first = null;

		return entry;
	}

}
