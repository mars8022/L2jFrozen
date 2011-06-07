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
package com.l2jfrozen.gameserver.handler.skillhandlers;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.event.VIP;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @authors L2JFrozen
 */
public class SummonFriend implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(SummonFriend.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.SUMMON_FRIEND };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance)) // currently not implemented for others
			return;

		L2PcInstance activePlayer = (L2PcInstance) activeChar;
		if(activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (activePlayer._inEvent) {
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;			
		}
		if (activePlayer._inEventCTF && CTF.is_started()) {
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;			
		}
		if (activePlayer._inEventDM && DM.is_started()) {
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;			
		}
		if (activePlayer._inEventTvT && TvT.is_started()) {
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;			
		}
		if (activePlayer._inEventVIP && VIP._started) {
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;			
		}
		
		// Checks summoner not in siege zone
		if(activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			((L2PcInstance) activeChar).sendMessage("You cannot summon in siege zone.");
			return;
		}
		
		// Checks summoner not in arenas, siege zones, jail
		if(activePlayer.isInsideZone(L2Character.ZONE_PVP))
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		if(GrandBossManager.getInstance().getZone(activePlayer) != null && !activePlayer.isGM())
		{
			activePlayer.sendMessage("You may not use Summon Friend Skill inside a Boss Zone.");
			return;
		}

		// check for summoner not in raid areas
		FastList<L2Object> objects = L2World.getInstance().getVisibleObjects(activeChar, 5000);
		if(objects != null)
		{
			for(L2Object object : objects)
			{
				if(object instanceof L2RaidBossInstance || object instanceof L2GrandBossInstance)
				{
					activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
					return;
				}
			}
		}

		objects = null;

		try
		{
			for(int index = 0; index < targets.length; index++)
			{
				if(!(targets[index] instanceof L2Character))
					continue;

				L2Character target = (L2Character) targets[index];
				if(activeChar == target)
					continue;

				if(target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;

					// CHECK TARGET CONDITIONS

					//This message naturally doesn't bring up a box...
					//$s1 wishes to summon you from $s2. Do you accept?
					//SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT);
					//sm2.addString(activeChar.getName());
					//String nearestTown = MapRegionTable.getInstance().getClosestTownName(activeChar);
					//sm2.addString(nearestTown);
					//targetChar.sendPacket(sm2);

					// is in same party (not necessary any more)
					// if (!(targetChar.getParty() != null && targetChar.getParty().getPartyMembers().contains(activeChar)))
					//	continue;

					if(targetChar.isAlikeDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}
					
					if (targetChar._inEvent)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventCTF)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventDM)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventTvT)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventVIP)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}

					if(targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}

					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
					if(targetChar.isRooted() || targetChar.isInCombat())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}

					if(GrandBossManager.getInstance().getZone(targetChar) != null && !targetChar.isGM())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					// Check for the the target's festival status
					if(targetChar.isInOlympiadMode())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
						continue;
					}

					// Check for the the target's festival status
					if(targetChar.isFestivalParticipant())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}

					// Check for the target's jail status, arenas and siege zones
					if(targetChar.isInsideZone(L2Character.ZONE_PVP))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}

					activePlayer = null;

					// Requires a Summoning Crystal
					/* if (targetChar.getInventory().getItemByItemId(8615) == null) */
					if((targetChar.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) //KidZor
					{
						((L2PcInstance) activeChar).sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal");
						targetChar.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal");
						continue;
					}

					if(!Util.checkIfInRange(0, activeChar, target, false))
					{
						if(skill.getId() == 1429)
						{
							targetChar.sendPacket(SystemMessage.sendString("You are summoned to a party member."));
							targetChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true);
						}
						else
						{
							targetChar.getInventory().destroyItemByItemId("Consume", 8615, 1, targetChar, activeChar);
							targetChar.sendPacket(SystemMessage.sendString("You are summoned to a party member."));
							targetChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true);
						}
					}

					target = null;
					targetChar = null;
				}
			}
		}
		catch(Throwable e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
