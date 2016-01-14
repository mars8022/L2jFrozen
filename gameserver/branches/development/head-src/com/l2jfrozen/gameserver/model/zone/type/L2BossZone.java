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
package com.l2jfrozen.gameserver.model.zone.type;

import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.util.L2FastList;

/**
 * @author DaRkRaGe
 */
public class L2BossZone extends L2ZoneType
{
	private String _zoneName;
	private int _timeInvade;
	private boolean _enabled = true; // default value, unless overridden by xml...
	private boolean _IsFlyingEnable = true; // default value, unless overridden by xml...

	// track the times that players got disconnected. Players are allowed
	// to log back into the zone as long as their log-out was within _timeInvade
	// time...
	// <player objectId, expiration time in milliseconds>
	private FastMap<Integer, Long> _playerAllowedReEntryTimes;

	// track the players admitted to the zone who should be allowed back in 
	// after reboot/server downtime (outside of their control), within 30 
	// of server restart
	private List<Integer> _playersAllowed;

	private int _bossId;
	
	public L2BossZone(int id, int boss_id)
	{
		super(id);
		_bossId = boss_id;
		_playerAllowedReEntryTimes = new FastMap<Integer, Long>();
		_playersAllowed = new L2FastList<Integer>();
	}
	
	

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("name"))
		{
			_zoneName = value;
		}
		else if(name.equals("InvadeTime"))
		{
			_timeInvade = Integer.parseInt(value);
		}
		else if(name.equals("EnabledByDefault"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if(name.equals("flying"))
		{
			_IsFlyingEnable = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	/**
	 * Boss zones have special behaviors for player characters. Players are
	 * automatically teleported out when the attempt to enter these zones,
	 * except if the time at which they enter the zone is prior to the entry
	 * expiration time set for that player. Entry expiration times are set by
	 * any one of the following: 1) A player logs out while in a zone
	 * (Expiration gets set to logoutTime + _timeInvade) 2) An external source
	 * (such as a quest or AI of NPC) set up the player for entry.
	 * 
	 * There exists one more case in which the player will be allowed to enter.
	 * That is if the server recently rebooted (boot-up time more recent than
	 * currentTime - _timeInvade) AND the player was in the zone prior to reboot.
	 */
	protected void onEnter(L2Character character)
	{
		if (_enabled)
		{
			if (character instanceof L2PcInstance)
			{
				// Thread.dumpStack();
				L2PcInstance player = (L2PcInstance) character;
				
				if (player.isGM() || Config.ALLOW_DIRECT_TP_TO_BOSS_ROOM)
				{
					player.sendMessage("You entered " + _zoneName);
					return;
				}
				
				// Ignore the check for Van Halter zone id 12014 if player got marks
				if (getId() == 12014)
				{
					L2ItemInstance VisitorsMark = player.getInventory().getItemByItemId(8064);
					L2ItemInstance FadedVisitorsMark = player.getInventory().getItemByItemId(8065);
					L2ItemInstance PagansMark = player.getInventory().getItemByItemId(8067);
					
					long mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
					long mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
					long mark3 = PagansMark == null ? 0 : PagansMark.getCount();
					
					if (mark1 != 0 || mark2 != 0 || mark3 != 0)
						return;				
				}
				
				if (!player.isGM() && player.isFlying() && !_IsFlyingEnable)
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					return;
				}
				
				// if player has been (previously) cleared by npc/ai for entry and the zone is
				// set to receive players (aka not waiting for boss to respawn)
				if (_playersAllowed.contains(character.getObjectId()))
				{
					// Get the information about this player's last logout-exit from this zone.
					Long expirationTime = _playerAllowedReEntryTimes.get(character.getObjectId());
					
					// with legal entries, do nothing.
					if (expirationTime == null) // legal null expirationTime entries
					{
						long serverStartTime = GameServer.dateTimeServerStarted.getTimeInMillis();
						
						if (serverStartTime > System.currentTimeMillis() - _timeInvade)
							return;
					}
					else
					{
						// legal non-null logoutTime entries
						_playerAllowedReEntryTimes.remove(character.getObjectId());
						
						if (expirationTime.longValue() > System.currentTimeMillis())
							return;
					}
					_playersAllowed.remove(_playersAllowed.indexOf(character.getObjectId()));
				}
				
				// teleport out all players who attempt "illegal" (re-)entry
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				player = null;
			}
		}
	}
	
	/**
	 * Some GrandBosses send all players in zone to a specific part of the zone,
	 * rather than just removing them all. If this is the case, this command should
	 * be used. If this is no the case, then use oustAllPlayers().
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList.isEmpty())
			return;
		
		for (L2Character character : _characterList.values())
		{
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline() == 1)
					player.teleToLocation(x, y, z);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(_enabled)
		{
			if(character instanceof L2PcInstance)
			{
				//Thread.dumpStack();
				L2PcInstance player = (L2PcInstance) character;

				if(player.isGM())
				{
					player.sendMessage("You left " + _zoneName);
					return;
				}

				// if the player just got disconnected/logged out, store the dc time so that
				// decisions can be made later about allowing or not the player to log into the zone
				if(player.isOnline() == 0 && _playersAllowed.contains(character.getObjectId()))
				{
					// mark the time that the player left the zone
					_playerAllowedReEntryTimes.put(character.getObjectId(), System.currentTimeMillis() + _timeInvade);
				}

				player = null;
			}
		}
	}

	public void setZoneEnabled(boolean flag)
	{
		if(_enabled != flag)
		{
			oustAllPlayers();
		}

		_enabled = flag;
	}

	public String getZoneName()
	{
		return _zoneName;
	}

	public int getTimeInvade()
	{
		return _timeInvade;
	}

	public void setAllowedPlayers(List<Integer> list)
	{
		if(list != null)
		{
			_playersAllowed = list;
		}
	}

	public List<Integer> getAllowedPlayers()
	{
		return _playersAllowed;
	}

	public boolean isPlayerAllowed(L2PcInstance player)
	{
		if(player.isGM())
			return true;
		else if(_playersAllowed.contains(player.getObjectId()))
			return true;
		else
		{
			player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			return false;
		}
	}

	/**
	 * Occasionally, all players need to be sent out of the zone (for example, if the players are just running around
	 * without fighting for too long, or if all players die, etc). This call sends all online players to town and marks
	 * offline players to be teleported (by clearing their relog expiration times) when they log back in (no real need
	 * for off-line teleport).
	 */
	public void oustAllPlayers()
	{
		if(_characterList == null)
			return;

		if(_characterList.isEmpty())
			return;

		for(L2Character character : _characterList.values())
		{
			if(character == null)
			{
				continue;
			}

			if(character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;

				if(player.isOnline() == 1)
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}

				player = null;
			}
		}
		_playerAllowedReEntryTimes.clear();
		_playersAllowed.clear();
	}

	/**
	 * This function is to be used by external sources, such as quests and AI in order to allow a player for entry into
	 * the zone for some time. Naturally if the player does not enter within the allowed time, he/she will be teleported
	 * out again...
	 * 
	 * @param player reference to the player we wish to allow
	 * @param durationInSec amount of time in seconds during which entry is valid.
	 */
	public void allowPlayerEntry(L2PcInstance player, int durationInSec)
	{
		if(!player.isGM())
		{
			if(!_playersAllowed.contains(player.getObjectId())){
				_playersAllowed.add(player.getObjectId());
			}
			_playerAllowedReEntryTimes.put(player.getObjectId(), System.currentTimeMillis() + durationInSec * 1000);
		}
	}

	@Override
	protected void onDieInside(L2Character character)
	{}

	@Override
	protected void onReviveInside(L2Character character)
	{}
	
	public void updateKnownList(L2NpcInstance npc)
	{
		if (_characterList == null || _characterList.isEmpty())
			return;
		
		Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
		for (L2Character character : _characterList.values())
		{
			if (character == null)
				continue;
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline()==1 && !player.isOffline())
					npcKnownPlayers.put(player.getObjectId(), player);
			}
		}
		return;
	}
	
	public int getBossId(){
		return _bossId;
	}
}
