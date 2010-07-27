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
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	// private static Logger _log =
	// Logger.getLogger(Recall.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.RECALL };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar instanceof L2PcInstance)
		{
			if (((L2PcInstance) activeChar).isInOlympiadMode())
			{
				((L2PcInstance) activeChar).sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				return;
			}
		}
		try
		{
			for (int index = 0; index < targets.length; index++)
			{
				if (!(targets[index] instanceof L2Character))
				{
					continue;
				}
				L2Character target = (L2Character) targets[index];
				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;
					// Check to see if the current player target is in a
					// festival.
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
						continue;
					}
					// Check to see if the current player target is in TvT, CTF or DM events.
					if (targetChar._inEventCTF || targetChar._inEventTvT || targetChar._inEventDM)
					{
						targetChar.sendMessage("You may not use an escape skill in a Event.");
						continue;
					}
					// Check to see if player is in jail
					if (targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
						continue;
					}
					// Check to see if player is in a duel
					if (targetChar.isInDuel())
					{
						targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
						continue;
					}
				}
				target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		catch (Throwable e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}