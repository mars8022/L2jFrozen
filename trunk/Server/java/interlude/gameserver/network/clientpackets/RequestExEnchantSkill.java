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
import interlude.gameserver.datatables.SkillTreeTable;
import interlude.gameserver.model.L2EnchantSkillLearn;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2ShortCut;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2FolkInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.base.Experience;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ShortCutRegister;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.gameserver.util.Util;
import interlude.util.Rnd;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 *
 * @author -Wooden-
 */
public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final String _C__D0_07_REQUESTEXENCHANTSKILL = "[C] D0:07 RequestExEnchantSkill";
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		L2FolkInstance trainer = player.getLastFolkNPC();
		if (trainer == null) {
			return;
		}
		int npcid = trainer.getNpcId();
		if ((trainer == null || !player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM()) {
			return;
		}
		if (player.getSkillLevel(_skillId) >= _skillLvl) {
			// skill with this level
			return;
		}
		if (player.getClassId().getId() < 88) {
			// quest completed
			return;
		}
		if (player.getLevel() < 76) {
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		int counts = 0;
		int _requiredSp = 10000000;
		int _requiredExp = 100000;
		byte _rate = 0;
		int _baseLvl = 1;
		L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
		for (L2EnchantSkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null || sk != skill || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid)) {
				continue;
			}
			counts++;
			_requiredSp = s.getSpCost();
			_requiredExp = s.getExp();
			_rate = s.getRate(player);
			_baseLvl = s.getBaseLevel();
		}
		if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.sendMessage("You are trying to learn skill that u can't..");
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", Config.DEFAULT_PUNISH);
			player.closeNetConnection(); // kick
			return;
		}
		if (player.getSp() >= _requiredSp)
		{
			long expAfter = player.getExp() - _requiredExp;
			if (player.getExp() >= _requiredExp && expAfter >= Experience.LEVEL[player.getLevel()])
			{
				if (Config.ES_SP_BOOK_NEEDED && (_skillLvl == 101 || _skillLvl == 141))
				// only first lvl requires book
				{
					int spbId = 6622;
					L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					if (spb == null)// Haven't spellbook
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
						return;
					}
					// ok. Destroy ONE copy of the book
					player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
			return;
		}
		if (Rnd.get(100) <= _rate)
		{
			player.addSkill(skill, true);
			if (Config.DEBUG) {
				_log.fine("Learned skill " + _skillId + " for " + _requiredSp + " SP.");
			}
			player.getStat().removeExpAndSp(_requiredExp, _requiredSp);
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			player.sendPacket(new UserInfo(player));
			SystemMessage ep = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			ep.addNumber(_requiredExp);
			sendPacket(ep);
			SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
			sp.addNumber(_requiredSp);
			sendPacket(sp);
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			if (skill.getLevel() > 100)
			{
				_skillLvl = _baseLvl;
				player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
				player.sendSkillList();
			}
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		trainer.showEnchantSkillList(player, player.getClassId());
		// update all the shortcuts to this skill
		L2ShortCut[] allShortCuts = player.getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_07_REQUESTEXENCHANTSKILL;
	}
}
