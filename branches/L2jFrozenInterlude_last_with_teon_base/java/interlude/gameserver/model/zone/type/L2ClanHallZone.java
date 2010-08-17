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
package interlude.gameserver.model.zone.type;

import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.clanhallsiege.BanditStrongholdSiege;
import interlude.gameserver.instancemanager.clanhallsiege.WildBeastFarmSiege;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.Location;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ClanHallDecoration;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * A clan hall zone
 *
 * @author durgus
 */
public class L2ClanHallZone extends L2ZoneType
{
	private int _clanHallId;
	private int[] _spawnLoc;

	public L2ClanHallZone(int id)
	{
		super(id);
		_spawnLoc = new int[3];
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			_clanHallId = Integer.parseInt(value);
			// Register self to the correct clan hall
			ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
		}
		else if (name.equals("spawnX"))
		{
			_spawnLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("spawnY"))
		{
			_spawnLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("spawnZ"))
		{
			_spawnLoc[2] = Integer.parseInt(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_clanHallId == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));		
		}
		if (_clanHallId == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));			
		}
		if (character instanceof L2PcInstance)
		{
			// Set as in clan hall
			character.setInsideZone(L2Character.ZONE_CLANHALL, true);
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (clanHall == null)
				return;
			// Send decoration packet
			ClanHallDecoration deco = new ClanHallDecoration(clanHall);
			((L2PcInstance) character).sendPacket(deco);
		}
	}

	public void updateSiegeStatus()
	{
		if (_clanHallId == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (Exception e)
				{
				}
			}	
		}
		else if (_clanHallId == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (Exception e)
				{
				}
			}	
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					character.setInsideZone(L2Character.ZONE_PVP, false);

					if (character instanceof L2PcInstance)
						character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				}
				catch (Exception e)
				{
				}
			}			
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_clanHallId == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));			
		}
		if (_clanHallId == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));			
		}
		if (character instanceof L2PcInstance)
		{
			// Unset clanhall zone
			character.setInsideZone(L2Character.ZONE_CLANHALL, false);
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * Removes all foreigners from the clan hall
	 *
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		for (L2Character temp : _characterList.values())
		{
			if (!(temp instanceof L2PcInstance)) {
				continue;
			}
			if (((L2PcInstance) temp).getClanId() == owningClanId) {
				continue;
			}
			((L2PcInstance) temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	/**
	 * Get the clan hall's spawn
	 *
	 * @return
	 */
	public Location getSpawn()
	{
		return new Location(_spawnLoc[0], _spawnLoc[1], _spawnLoc[2]);
	}
}
