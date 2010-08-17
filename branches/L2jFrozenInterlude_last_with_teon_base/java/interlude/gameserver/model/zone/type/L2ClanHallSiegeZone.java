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

import javolution.util.FastList;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.instancemanager.clanhallsiege.BanditStrongholdSiege;
import interlude.gameserver.instancemanager.clanhallsiege.DevastatedCastleManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortResistSiegeManager;
import interlude.gameserver.instancemanager.clanhallsiege.FortressofTheDeadManager;
import interlude.gameserver.instancemanager.clanhallsiege.WildBeastFarmSiege;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Maxi
 */
public class L2ClanHallSiegeZone extends L2ZoneType
{
	private String _zoneName;

	public L2ClanHallSiegeZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
			_zoneName = value;
		else
			super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && onZoneSiege())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));			
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance && onZoneSiege())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));			
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

	public void banishForeigners(int owningClanId)
	{
		for (L2Character temp : _characterList.values())
		{
			if (!(temp instanceof L2PcInstance)) continue;
			if (((L2PcInstance) temp).getClanId() == owningClanId) continue;
			((L2PcInstance) temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	public void announceToPlayers(String message)
	{
		for (L2Character temp : _characterList.values())
		{
			if (temp instanceof L2PcInstance)
				((L2PcInstance) temp).sendMessage(message);
		}
	}

	public FastList<L2PcInstance> getAllPlayers()
	{
		FastList<L2PcInstance> players = new FastList<L2PcInstance>();
		for (L2Character temp : _characterList.values())
		{
			if (temp instanceof L2PcInstance)
				players.add((L2PcInstance) temp);
		}
		return players;
	}

	public String getZoneName()
	{
		return _zoneName;
	}

	public void updateSiegeStatus()
	{
		if (_zoneName.equalsIgnoreCase("Bandit Stronghold") && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				} catch (Exception e) { }
			}	
		}
		else if (_zoneName.equalsIgnoreCase("Beast Farm") && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				} catch (Exception e) { }
			}	
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					character.setInsideZone(L2Character.ZONE_PVP, false);
					character.setInsideZone(L2Character.ZONE_SIEGE, false);
					character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

					if (character instanceof L2PcInstance)
						character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				} catch (Exception e) { }
			}			
		}
	}

	public void updateSiegeStatus(int val)
	{
		if (val==1)
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					if (_zoneName.equalsIgnoreCase("Fortress of Resistance"))
					{
						onEnter(character);
						character.setInsideZone(L2Character.ZONE_PVP, true);
						character.setInsideZone(L2Character.ZONE_SIEGE, true);
						character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
						((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
					} 
				} catch (Exception e) { }
			}	
		}
		else
		{
			if (val==2)
			{
				for (L2Character character : _characterList.values())
				{
					try
					{
						character.setInsideZone(L2Character.ZONE_PVP, false);
						character.setInsideZone(L2Character.ZONE_SIEGE, false);
						character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
						((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
					} catch (Exception e) { }
				}
			}		
		}
	}

	public boolean onZoneSiege()
	{
		return _zoneName.equalsIgnoreCase("Fortress of Resistance") && FortResistSiegeManager.getInstance().getIsInProgress()
		|| _zoneName.equalsIgnoreCase("Devastated Castle") && DevastatedCastleManager.getInstance().getIsInProgress()
		|| _zoneName.equalsIgnoreCase("Bandit Stronghold") && BanditStrongholdSiege.getInstance().getIsInProgress()
		|| _zoneName.equalsIgnoreCase("Beast Farm") && WildBeastFarmSiege.getInstance().getIsInProgress()
		|| _zoneName.equalsIgnoreCase("Fortress of the Dead") && FortressofTheDeadManager.getInstance().getIsInProgress();
	}
}

