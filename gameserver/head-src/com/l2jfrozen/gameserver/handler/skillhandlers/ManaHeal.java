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
package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class ManaHeal implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(ManaHeal.class);
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IItemHandler#useItem(com.l2jfrozen.gameserver.model.L2PcInstance, com.l2jfrozen.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.MANAHEAL,
		SkillType.MANARECHARGE,
		SkillType.MANAHEAL_PERCENT
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.handler.IItemHandler#useItem(com.l2jfrozen.gameserver.model.L2PcInstance, com.l2jfrozen.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(final L2Character actChar, final L2Skill skill, final L2Object[] targets)
	{
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target == null || target.isDead() || target.isInvul())
				continue;
			
			double mp = skill.getPower();
			if (skill.getSkillType() == SkillType.MANAHEAL_PERCENT)
			{
				// double mp = skill.getPower();
				mp = target.getMaxMp() * mp / 100.0;
			}
			else
			{
				mp = (skill.getSkillType() == SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
			}
			
			// if ((target.getCurrentMp() + mp) >= target.getMaxMp())
			// {
			// mp = target.getMaxMp() - target.getCurrentMp();
			// }
			target.setLastHealAmount((int) mp);
			target.setCurrentMp(mp + target.getCurrentMp());
			final StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);
			
			if (actChar instanceof L2PcInstance && actChar != target)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1);
				sm.addString(actChar.getName());
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_MP_RESTORED);
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			
		}
		
		if (skill.isMagic() && skill.useSpiritShot())
		{
			if (actChar.checkBss())
				actChar.removeBss();
			if (actChar.checkSps())
				actChar.removeSps();
		}
		else if (skill.useSoulShot())
		{
			if (actChar.checkSs())
				actChar.removeSs();
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
