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

package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2GourdInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author l2jfrozen dev.
 */

public class JackpotSeed implements IItemHandler
{
	private L2GourdInstance _gourd = null;

	private static int[] _itemIds =
	{
			6389, // small seed
			6390
	// large seed
	};

	private static int[] _npcIds =
	{
			12774, //Young Pumpkin
			12777
	//Large Young Pumpkin
	};

	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2NpcTemplate template1 = null;
		int itemId = item.getItemId();
		for(int i = 0; i < _itemIds.length; i++)
		{
			if(_itemIds[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(_npcIds[i]);
				break;
			}
		}

		if(template1 == null)
			return;

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(activeChar.getX());
			spawn.setLocy(activeChar.getY());
			spawn.setLocz(activeChar.getZ());
			_gourd = (L2GourdInstance) spawn.spawnOne();
			L2World.getInstance().storeObject(_gourd);
			_gourd.setOwner(activeChar.getName());
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Created " + template1.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
			activeChar.sendPacket(sm);
			sm = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Target is not ingame.");
			activeChar.sendPacket(sm);
			sm = null;
		}
		activeChar = null;
		template1 = null;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
