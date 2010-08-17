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
package interlude.gameserver.handler.skillhandlers;

import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.instancemanager.FortSiegeManager;
import interlude.gameserver.instancemanager.SiegeManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2SiegeFlagInstance;
import interlude.gameserver.model.entity.Castle;
import interlude.gameserver.model.entity.Fort;


public class SiegeFlag implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.SIEGEFLAG};

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) {
			return;
		}

        L2PcInstance player = (L2PcInstance)activeChar;

        if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) {
			return;
		}

        Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);

		if (castle == null && fort == null) {
			return;
		}

		if (castle != null)
		{
			if (!checkIfOkToPlaceFlag(player, castle, true)) {
				return;
			}
		}
		else
		{
			if (!checkIfOkToPlaceFlag(player, fort, true)) {
				return;
			}
		}

		try
		{
            L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
            flag.setTitle(player.getClan().getName());
            flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
            flag.setHeading(player.getHeading());
            flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
            if (castle != null) {
				castle.getSiege().getFlag(player.getClan()).add(flag);
			} else {
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}

        }
        catch (Exception e)
        {
            player.sendMessage("Error placing flag:" + e);
        }
    }

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
	{
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);

		if (castle == null && fort == null) {
			return false;
		}
		if (castle != null) {
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		} else {
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		}
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance)) {
			return false;
		}

		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;

		if (castle == null || castle.getCastleId() <= 0) {
			text = "You must be on castle ground to place a flag.";
		} else if (!castle.getSiege().getIsInProgress()) {
			text = "You can only place a flag during a siege.";
		} else if (castle.getSiege().getAttackerClan(player.getClan()) == null) {
			text = "You must be an attacker to place a flag.";
		} else if (player.getClan() == null || !player.isClanLeader()) {
			text = "You must be a clan leader to place a flag.";
		} else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount()) {
			text = "You have already placed the maximum number of flags possible.";
		} else {
			return true;
		}

		if (!isCheckOnly) {
			player.sendMessage(text);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Fort fort, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance)) {
			return false;
		}

		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;

		if (fort == null || fort.getFortId() <= 0) {
			text = "You must be on fort ground to place a flag.";
		} else if (!fort.getSiege().getIsInProgress()) {
			text = "You can only place a flag during a siege.";
		} else if (fort.getSiege().getAttackerClan(player.getClan()) == null) {
			text = "You must be an attacker to place a flag.";
		} else if (player.getClan() == null || !player.isClanLeader()) {
			text = "You must be a clan leader to place a flag.";
		} else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount()) {
			text = "You have already placed the maximum number of flags possible.";
		} else {
			return true;
		}

		if (!isCheckOnly) {
			player.sendMessage(text);
		}
		return false;
	}

}
