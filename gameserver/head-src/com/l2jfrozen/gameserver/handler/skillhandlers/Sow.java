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
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
	protected static final Logger LOGGER = Logger.getLogger(Sow.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SOW
	};
	
	private L2PcInstance _activeChar;
	private L2MonsterInstance _target;
	private int _seedId;
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		_activeChar = (L2PcInstance) activeChar;
		
		final L2Object[] targetList = skill.getTargetList(activeChar);
		if (targetList == null)
		{
			return;
		}
		
		if (Config.DEBUG)
			LOGGER.info("Casting sow");
		
		for (int index = 0; index < targetList.length; index++)
		{
			if (!(targetList[0] instanceof L2MonsterInstance))
				continue;
			
			_target = (L2MonsterInstance) targetList[0];
			if (_target.isSeeded())
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.isDead())
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.getSeeder() != _activeChar)
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			_seedId = _target.getSeedType();
			if (_seedId == 0)
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			L2ItemInstance item = _activeChar.getInventory().getItemByItemId(_seedId);
			if (item == null)
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			// Consuming used seed
			_activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			item = null;
			
			SystemMessage sm = null;
			if (calcSuccess())
			{
				_activeChar.sendPacket(new PlaySound("Itemsound.quest_itemget"));
				_target.setSeeded();
				sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}
			
			if (_activeChar.getParty() == null)
			{
				_activeChar.sendPacket(sm);
			}
			else
			{
				_activeChar.getParty().broadcastToPartyMembers(sm);
			}
			sm = null;
			// TODO: Mob should not agro on player, this way doesn't work really nice
			_target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
		}
		
	}
	
	private boolean calcSuccess()
	{
		if (_activeChar == null || _target == null)
			return false;
		
		// TODO: check all the chances
		int basicSuccess = (L2Manor.getInstance().isAlternative(_seedId) ? 20 : 90);
		int minlevelSeed = 0;
		int maxlevelSeed = 0;
		minlevelSeed = L2Manor.getInstance().getSeedMinLevel(_seedId);
		maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(_seedId);
		
		final int levelPlayer = _activeChar.getLevel(); // Attacker Level
		final int levelTarget = _target.getLevel(); // taret Level
		
		// 5% decrease in chance if player level
		// is more then +/- 5 levels to _seed's_ level
		if (levelTarget < minlevelSeed)
			basicSuccess -= 5;
		if (levelTarget > maxlevelSeed)
			basicSuccess -= 5;
		
		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		
		if (diff > 5)
			basicSuccess -= 5 * (diff - 5);
		
		// chance can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		final int rate = Rnd.nextInt(99);
		
		return (rate < basicSuccess);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
