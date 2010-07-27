/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.network.clientpackets;

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.InventoryUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2Item;

/**
 * This class ...
 *
 * @version $Revision: 1.8.2.3.2.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUnEquipItem extends L2GameClientPacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
	private static Logger _log = Logger.getLogger(RequestUnEquipItem.class.getName());
	// cd
	private int _slot;

	/**
	 * packet type id 0x11 format: cd
	 *
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG) {
			_log.fine("request unequip slot " + _slot);
		}
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		if (activeChar._haveFlagCTF)
		{
			activeChar.sendMessage("You can't unequip a CTF flag.");
			return;
		}
		L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		if (item != null && item.isWear())
		{
			// Wear-items are not to be unequipped
			return;
		}
		// Prevent of unequiping a cursed weapon
		if (_slot == L2Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquiped())
		{
			// Message ?
			return;
		}
		// Prevent player from unequipping items in special conditions
		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
		{
			activeChar.sendMessage("Your status does not allow you to do that.");
			return;
		}
		if (activeChar.isCastingNow()) {
			return;
		}
		// Remove augmentation bonus
		if (item != null && item.isAugmented())
		{
			item.getAugmentation().removeBonus(activeChar);
		}
		// remove skill of cupid's bow
		if (item != null && item.isCupidBow())
		{
			if (item.getItemId() == 9140)
			{
				activeChar.removeSkill(SkillTable.getInstance().getInfo(3261, 1));
			}
			else
			{
				activeChar.removeSkill(SkillTable.getInstance().getInfo(3260, 0));
				activeChar.removeSkill(SkillTable.getInstance().getInfo(3262, 0));
			}
		}
		L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		// show the update in the inventory
		InventoryUpdate iu = new InventoryUpdate();
		for (L2ItemInstance element : unequiped) {
			activeChar.checkSSMatch(null, element);
			iu.addModifiedItem(element);
		}
		activeChar.sendPacket(iu);
		// On retail you don't stop hitting if unequip something. REOMVED: activeChar.abortAttack();
		activeChar.broadcastUserInfo();
		// this can be 0 if the user pressed the right mousebutton twice very fast
		if (unequiped.length > 0)
		{
			SystemMessage sm = null;
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0].getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0].getItemId());
			}
			activeChar.sendPacket(sm);
			sm = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}
