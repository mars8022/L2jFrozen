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
package interlude.gameserver.handler.itemhandlers;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.handler.IItemHandler;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * @author DaRkRaGe & schursin ;)
 */
public class JackpotSeed implements IItemHandler
{
	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2Spawn spawnedPlant = null;

		public DeSpawnScheduleTimerTask(L2Spawn spawn)
		{
			spawnedPlant = spawn;
		}

		public void run()
		{
			try
			{
				spawnedPlant.getLastSpawn().decayMe();
			}
			catch (Throwable t)
			{
			}
		}
	}

	private static int[] _itemIds = { 6389, // small seed
			6390
	// large seed
	};
	private static int[] _npcIds = { 12774, // Young Pumpkin
			12777
	// Large Young Pumpkin
	};
	private static int[] _npcLifeTime = { 30000, // Young Pumpkin
			40000
	// Large Young Pumpkin
	};

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2NpcTemplate template1 = null;
		int lifeTime = 0;
		int itemId = item.getItemId();
		for (int i = 0; i < _itemIds.length; i++)
		{
			if (_itemIds[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(_npcIds[i]);
				lifeTime = _npcLifeTime[i];
				break;
			}
		}
		if (template1 == null) {
			return;
		}
		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(activeChar.getX());
			spawn.setLocy(activeChar.getY());
			spawn.setLocz(activeChar.getZ());
			L2World.getInstance().storeObject(spawn.spawnOne());
			ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(spawn), lifeTime);
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		catch (Exception e)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Exception in useItem() of JackpotSeed.java");
			activeChar.sendPacket(sm);
		}
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}
}
