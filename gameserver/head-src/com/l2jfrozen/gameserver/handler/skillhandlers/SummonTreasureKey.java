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

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author evill33t
 */
public class SummonTreasureKey implements ISkillHandler
{
	static Logger LOGGER = Logger.getLogger(SummonTreasureKey.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SUMMON_TREASURE_KEY
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		try
		{
			int item_id = 0;
			
			switch (skill.getLevel())
			{
				case 1:
				{
					item_id = Rnd.get(6667, 6669);
					break;
				}
				case 2:
				{
					item_id = Rnd.get(6668, 6670);
					break;
				}
				case 3:
				{
					item_id = Rnd.get(6669, 6671);
					break;
				}
				case 4:
				{
					item_id = Rnd.get(6670, 6672);
					break;
				}
			}
			player.addItem("Skill", item_id, Rnd.get(2, 3), player, false);
			
			player = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("Error using skill summon Treasure Key:" + e);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
}
