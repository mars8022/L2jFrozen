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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillSpellbookTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.model.L2PledgeSkillLearn;
import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2VillageMasterInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public class RequestAquireSkill extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());

	private int _id;

	private int _level;

	private int _skillType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}

	@Override
	protected void runImpl()
	{

		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
			return;

		L2FolkInstance trainer = player.getLastFolkNPC();

		if(trainer == null)
			return;

		int npcid = trainer.getNpcId();

		if(!player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;

		if(!Config.ALT_GAME_SKILL_LEARN)
		{
			player.setSkillLearningClassId(player.getClassId());
		}

		if(player.getSkillLevel(_id) >= _level)
			// already knows the skill with this level
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

		int counts = 0;
		int _requiredSp = 10000000;

		if(_skillType == 0)
		{

			L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());

			for(L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || sk != skill || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
				{
					continue;
				}
				counts++;
				_requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
			}

			if(counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}

			if(player.getSp() >= _requiredSp)
			{
				int spbId = -1;
				// divine inspiration require book for each level
				if(Config.DIVINE_SP_BOOK_NEEDED && skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, _level);
				}
				else if(Config.SP_BOOK_NEEDED && skill.getLevel() == 1)
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
				}

				// spellbook required
				if(spbId > -1)
				{
					L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);

					if(spb == null)
					{
						// Haven't spellbook
						player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return;
					}

					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);

				return;
			}
		}
		else if(_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);

			for(L2SkillLearn s : skillsc)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if(sk == null || sk != skill)
				{
					continue;
				}

				counts++;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				_requiredSp = s.getSpCost();
			}

			if(counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}

			if(player.getSp() >= _requiredSp)
			{
				if(!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
				{
					// Haven't spellbook
					player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
					return;
				}

				SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else if(_skillType == 2) //pledgeskills TODO: Find appropriate system messages.
		{
			if(!player.isClanLeader())
			{
				// TODO: Find and add system msg
				player.sendMessage("This feature is available only for the clan leader");
				return;
			}

			int itemId = 0;
			int repCost = 100000000;
			// Skill Learn bug Fix
			L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);

			for(L2PledgeSkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if(sk == null || sk != skill)
				{
					continue;
				}

				counts++;
				itemId = s.getItemId();
				repCost = s.getRepCost();
			}

			if(counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}

			if(player.getClan().getReputationScore() >= repCost)
			{
				if(Config.LIFE_CRYSTAL_NEEDED)
				{
					if(!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
					{
						// Haven't spellbook
						player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return;
					}

					SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
					sm.addItemName(itemId);
					sm.addNumber(1);
					sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
				player.sendPacket(sm);
				return;
			}
			player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
			player.getClan().addNewSkill(skill);

			if(Config.DEBUG)
			{
				_log.fine("Learned pledge skill " + _id + " for " + _requiredSp + " SP.");
			}

			SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(repCost);
			player.sendPacket(cr);
			SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(_id);
			player.sendPacket(sm);

			player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));

			for(L2PcInstance member : player.getClan().getOnlineMembers(""))
			{
				member.sendSkillList();
			}
			
			if(trainer instanceof L2VillageMasterInstance){
				((L2VillageMasterInstance) trainer).showPledgeSkillList(player); 
			}
			
			return;
		}

		else
		{
			_log.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
			return;
		}

		player.addSkill(skill, true);

		if(Config.DEBUG)
		{
			_log.fine("Learned skill " + _id + " for " + _requiredSp + " SP.");
		}

		player.setSp(player.getSp() - _requiredSp);

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);

		SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
		sp.addNumber(_requiredSp);
		sendPacket(sp);

		SystemMessage sm = new SystemMessage(SystemMessageId.LEARNED_SKILL_S1);
		sm.addSkillName(_id);
		player.sendPacket(sm);

		// update all the shortcuts to this skill
		if(_level > 1)
		{
			L2ShortCut[] allShortCuts = player.getAllShortCuts();

			for(L2ShortCut sc : allShortCuts)
			{
				if(sc.getId() == _id && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}

		if(trainer instanceof L2FishermanInstance)
		{
			((L2FishermanInstance) trainer).showSkillList(player);
		}
		else
		{
			trainer.showSkillList(player, player.getSkillLearningClassId());
		}

		if(_id >= 1368 && _id <= 1372) // if skill is expand sendpacket :)
		{
			ExStorageMaxCount esmc = new ExStorageMaxCount(player);
			player.sendPacket(esmc);
		}
		
		player.sendSkillList();
	}

	@Override
	public String getType()
	{
		return "[C] 6C RequestAquireSkill";
	}
}
