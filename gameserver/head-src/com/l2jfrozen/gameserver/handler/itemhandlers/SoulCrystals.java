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
package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.2.4 $ $Date: 2005/08/14 21:31:07 $
 */

public class SoulCrystals implements IItemHandler
{
	// First line is for Red Soul Crystals, second is Green and third is Blue Soul Crystals,
	// ordered by ascending level, from 0 to 13...
	private static final int[] ITEM_IDS =
	{
		4629,
		4630,
		4631,
		4632,
		4633,
		4634,
		4635,
		4636,
		4637,
		4638,
		4639,
		5577,
		5580,
		5908,
		4640,
		4641,
		4642,
		4643,
		4644,
		4645,
		4646,
		4647,
		4648,
		4649,
		4650,
		5578,
		5581,
		5911,
		4651,
		4652,
		4653,
		4654,
		4655,
		4656,
		4657,
		4658,
		4659,
		4660,
		4661,
		5579,
		5582,
		5914
	};
	
	// Our main method, where everything goes on
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2MonsterInstance))
		{
			// Send a System Message to the caster
			SystemMessage sm = new SystemMessage(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(sm);
			sm = null;
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You Cannot Use This While You Are Paralyzed");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// u can use soul crystal only when target hp goes below 50%
		if (((L2MonsterInstance) target).getCurrentHp() > ((L2MonsterInstance) target).getMaxHp() / 2.0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int crystalId = item.getItemId();
		
		// Soul Crystal Casting section
		final L2Skill skill = SkillTable.getInstance().getInfo(2096, 1);
		activeChar.useMagic(skill, false, true);
		// End Soul Crystal Casting section
		
		// Continue execution later
		CrystalFinalizer cf = new CrystalFinalizer(activeChar, target, crystalId);
		ThreadPoolManager.getInstance().scheduleEffect(cf, skill.getHitTime());
		
		cf = null;
		target = null;
		activeChar = null;
	}
	
	static class CrystalFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2Attackable _target;
		private final int _crystalId;
		
		CrystalFinalizer(final L2PcInstance activeChar, final L2Object target, final int crystalId)
		{
			_activeChar = activeChar;
			_target = (L2Attackable) target;
			_crystalId = crystalId;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead() || _target.isDead())
				return;
			_activeChar.enableAllSkills();
			try
			{
				_target.addAbsorber(_activeChar, _crystalId);
				_activeChar.setTarget(_target);
			}
			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
