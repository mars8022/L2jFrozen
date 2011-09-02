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

package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.SummonItemsData;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2SummonItem;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		//if(activeChar._inEventTvT && TvT._started && !Config.TVT_ALLOW_SUMMON)
		if(activeChar._inEventTvT && TvT.is_started() && !Config.TVT_ALLOW_SUMMON)
			{
			ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}

		//if(activeChar._inEventDM && DM._started && !Config.DM_ALLOW_SUMMON)
		if(activeChar._inEventDM && DM.is_started() && !Config.DM_ALLOW_SUMMON)
		{
			ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}

		//if(activeChar._inEventCTF && CTF._started && !Config.CTF_ALLOW_SUMMON)
		if(activeChar._inEventCTF && CTF.is_started() && !Config.CTF_ALLOW_SUMMON)
		{
			ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}

		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if(activeChar.isParalyzed())
		{
			activeChar.sendMessage("You Cannot Use This While You Are Paralyzed");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(activeChar.inObserverMode())
			return;

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}

		if(activeChar.isAttackingNow())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		if(activeChar.isCursedWeaponEquiped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return;
		}

		int npcID = sitem.getNpcId();

		if(npcID == 0)
			return;

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

		if(npcTemplate == null)
			return;

		switch(sitem.getType())
		{
			case 0: // static summons (like christmas tree)
				try
				{
					L2Spawn spawn = new L2Spawn(npcTemplate);

					//if(spawn == null)
					//	return;

					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					L2World.getInstance().storeObject(spawn.spawnOne());
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
					spawn = null;
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					activeChar.sendMessage("Target is not ingame.");
				}

				break;
			case 1: // pet summons
				L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);

				if(petSummon == null)
				{
					break;
				}

				petSummon.setTitle(activeChar.getName());

				if(!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if(!petSummon.isRespawned())
				{
					petSummon.store();
				}

				activeChar.setPet(petSummon);

				activeChar.sendPacket(new MagicSkillUser(activeChar, 2046, 1, 1000, 600000));
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));
				L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(activeChar.getX() + Rnd.get(40)-20, activeChar.getY() + Rnd.get(40)-20, activeChar.getZ());
				activeChar.sendPacket(new PetInfo(petSummon));
				petSummon.startFeed(false);
				item.setEnchantLevel(petSummon.getLevel());

				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, petSummon), 900);

				if(petSummon.getCurrentFed() <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed(false);
				}

				petSummon = null;

				break;
			case 2: // wyvern
				if(!activeChar.disarmWeapons())
					return;

				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				activeChar.sendPacket(mount);
				activeChar.broadcastPacket(mount);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(item.getObjectId());
		}

		activeChar = null;
		sitem = null;
		npcTemplate = null;
	}

	static class PetSummonFeedWait implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		@Override
		public void run()
		{
			try
			{
				if(_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_activeChar);
				}
				else
				{
					_petSummon.startFeed(false);
				}
			}
			catch(Throwable e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
	}

	static class PetSummonFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
				_petSummon.setShowSummonAnimation(false);
			}
			catch(Throwable e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
		}
	}

	@Override
	public int[] getItemIds()
	{
		return SummonItemsData.getInstance().itemIDs();
	}
}
