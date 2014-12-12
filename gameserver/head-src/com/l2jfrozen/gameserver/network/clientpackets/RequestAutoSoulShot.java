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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAutoSoulShot.class);
	
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		// Like L2OFF you can't use soulshots while sitting
		final int[] shots_ids =
		{
			5789,
			1835,
			1463,
			1464,
			1465,
			1466,
			1467,
			5790,
			2509,
			2510,
			2511,
			2512,
			2513,
			2514,
			3947,
			3948,
			3949,
			3950,
			3951,
			3952
		};
		if (activeChar.isSitting() && Arrays.toString(shots_ids).contains(String.valueOf(_itemId)))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_AUTO_USE_LACK_OF_S1);
			sm.addItemName(_itemId);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoSoulShot:" + _itemId);
			}
			
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			
			if (item != null)
			{
				if (_type == 1)
				{
					
					// Fishingshots are not automatic on retail
					if (_itemId < 6535 || _itemId > 6540)
					{
						activeChar.addAutoSoulShot(_itemId);
						
						// Attempt to charge first shot on activation
						if (_itemId == 6645 || _itemId == 6646 || _itemId == 6647)
						{
							// Like L2OFF you can active automatic SS only if you have a pet
							if (activeChar.getPet() != null)
							{
								// activeChar.addAutoSoulShot(_itemId);
								// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
								// activeChar.sendPacket(atk);
								
								// start the auto soulshot use
								final SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								
								activeChar.rechargeAutoSoulShot(true, true, true, 0);
							}
							else
							{
								final SystemMessage sm = new SystemMessage(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								return;
							}
						}
						else
						{
							if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType())
							{
								if (_itemId >= 3947 && _itemId <= 3952 && activeChar.isInOlympiadMode())
								{
									final SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
									sm.addString(item.getItemName());
									activeChar.sendPacket(sm);
								}
								else
								{
									// activeChar.addAutoSoulShot(_itemId);
									
									// start the auto soulshot use
									final SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
									sm.addString(item.getItemName());
									activeChar.sendPacket(sm);
									
								}
								
							}
							else
							{
								if (_itemId >= 2509 && _itemId <= 2514 || _itemId >= 3947 && _itemId <= 3952 || _itemId == 5790)
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
								}
								else
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
								}
							}
							
							activeChar.rechargeAutoSoulShot(true, true, false, 0);
							
						}
					}
					
				}
				else if (_type == 0)
				{
					activeChar.removeAutoSoulShot(_itemId);
					// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
					// activeChar.sendPacket(atk);
					
					// cancel the auto soulshot use
					final SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItemName());
					activeChar.sendPacket(sm);
				}
				
				final ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
				activeChar.sendPacket(atk);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] CF RequestAutoSoulShot";
	}
}
