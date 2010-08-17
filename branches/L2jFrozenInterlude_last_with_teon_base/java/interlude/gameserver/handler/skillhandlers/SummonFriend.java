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

import interlude.Config;
import interlude.gameserver.SevenSigns;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.util.Util;

/**
 * @authors BiTi, Sami
 */
public class SummonFriend implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(SummonFriend.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.SUMMON_FRIEND };

	public static boolean checkSummonerStatus(L2PcInstance summonerChar)
	{
		if (summonerChar == null) {
			return false;
		}
		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		// Check to see if the current player is in TvT CTF or DM events.
		if (summonerChar._inEventCTF || summonerChar._inEventTvT || summonerChar._inEventDM)
		{
			summonerChar.sendMessage("You cannot use this skill in a Event.");
			return false;
		}
		if (summonerChar.inObserverMode())
		{
			return false;
		}
		if (summonerChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}

	public static boolean checkTargetStatus(L2PcInstance targetChar, L2PcInstance summonerChar)
	{
		if (targetChar == null) {
			return false;
		}
		if (targetChar.isAlikeDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			return false;
		}
		// Check to see if the current player is in TvT CTF or DM events.
		if (targetChar._inEventCTF || targetChar._inEventTvT || targetChar._inEventDM)
		{
			targetChar.sendMessage("You cannot use this skill in a Event.");
			return false;
		}
		if (targetChar.isInStoreMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			return false;
		}
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			return false;
		}
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
			return false;
		}
		if (targetChar.isFestivalParticipant())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		if (targetChar.inObserverMode())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			return false;
		}
		// on retail character can enter 7s dungeon with summon friend,
		// but will be teleported away by mobs
		// because currently this is not working in L2J we do not allowing summoning
		if (summonerChar.isIn7sDungeon())
		{
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(targetChar);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
					return false;
				}
			}
		}
		return true;
	}

	public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null) {
			return;
		}
		if (!checkSummonerStatus(summonerChar)) {
			return;
		}
		if (!checkTargetStatus(targetChar, summonerChar)) {
			return;
		}
		int itemConsumeId = summonSkill.getTargetConsumeId();
		int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			String ItemName = ItemTable.getInstance().getTemplate(itemConsumeId).getName();
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addString(ItemName);
				targetChar.sendPacket(sm);
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addString(ItemName);
			targetChar.sendPacket(sm);
		}
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
	}

	/**
	 * @see interlude.gameserver.handler.ISkillHandler#useSkill(interlude.gameserver.model.L2Character, interlude.gameserver.model.L2Skill, interlude.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance)) {
			return; // currently not implemented for others
		}
		L2PcInstance activePlayer = (L2PcInstance) activeChar;
		if (!checkSummonerStatus(activePlayer)) {
			return;
		}
		try
		{
			for (L2Character target : (L2Character[]) targets)
			{
				if (activeChar == target) {
					continue;
				}
				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetPlayer = (L2PcInstance) target;
					if (!checkTargetStatus(targetPlayer, activePlayer)) {
						continue;
					}
					if (!Util.checkIfInRange(0, activeChar, target, false))
					{
						if (!targetPlayer.teleportRequest(activePlayer, skill))
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							activePlayer.sendPacket(sm);
							continue;
						}
						/*
						 * if (skill.getId() == 1403) // summon friend { // Send message ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId()); confirm.addString(activeChar.getName()); confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ()); confirm.addTime(30000); confirm.addRequesterId(activePlayer.getCharId());
						 * target.sendPacket(confirm); }
						 */
						else
						{
							teleToTarget(targetPlayer, activePlayer, skill);
							targetPlayer.teleportRequest(null, null);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see interlude.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
