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
package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.templates.L2ArmorType;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * The Class ConditionUsingItemType.
 * @author mkizub
 */
public final class ConditionUsingItemType extends Condition
{
	private final boolean _armor;
	private final int _mask;
	
	/**
	 * Instantiates a new condition using item type.
	 * @param mask the mask
	 */
	public ConditionUsingItemType(final int mask)
	{
		_mask = mask;
		_armor = (_mask & (L2ArmorType.MAGIC.mask() | L2ArmorType.LIGHT.mask() | L2ArmorType.HEAVY.mask())) != 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.skills.conditions.Condition#testImpl(com.l2jfrozen.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(final Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		final Inventory inv = ((L2PcInstance) env.player).getInventory();
		
		// If ConditionUsingItemType is one between Light, Heavy or Magic
		if (_armor)
		{
			// Get the itemMask of the weared chest (if exists)
			final L2ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null)
				return false;
			final int chestMask = chest.getItem().getItemMask();
			
			// If chest armor is different from the condition one return false
			if ((_mask & chestMask) == 0)
				return false;
			
			// So from here, chest armor matches conditions
			
			final int chestBodyPart = chest.getItem().getBodyPart();
			// return True if chest armor is a Full Armor
			if (chestBodyPart == L2Item.SLOT_FULL_ARMOR)
				return true;
			
			final L2ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs == null)
				return false;
			final int legMask = legs.getItem().getItemMask();
			// return true if legs armor matches too
			return (_mask & legMask) != 0;
		}
		return (_mask & inv.getWearedMask()) != 0;
	}
}
