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
package com.l2jfrozen.gameserver.model;

import java.util.List;

import javolution.util.FastList;

import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public class PartyMatchRoom
{
	private int _id;
	private String _title;
	private int _loot;
	private int _location;
	private int _minlvl;
	private int _maxlvl;
	private int _maxmem;
	private final List<L2PcInstance> _members = new FastList<L2PcInstance>();
	
	public PartyMatchRoom(int id, String title, int loot, int minlvl, int maxlvl, int maxmem, L2PcInstance owner)
	{
		_id = id;
		_title = title;
		_loot = loot;
		_location = TownManager.getClosestLocation(owner);
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_maxmem = maxmem;
		_members.add(owner);
	}
	
	public List<L2PcInstance> getPartyMembers()
	{
		return _members;
	}
	
	public void addMember(L2PcInstance player)
	{
		_members.add(player);
	}
	
	public void deleteMember(L2PcInstance player)
	{
		if (player != getOwner())
		{
			_members.remove(player);
			notifyMembersAboutExit(player);
		}
		else if (_members.size() == 1)
		{
			PartyMatchRoomList.getInstance().deleteRoom(_id);
		}
		else
		{
			changeLeader(_members.get(1));
			deleteMember(player);
		}
	}
	
	public void notifyMembersAboutExit(L2PcInstance player)
	{
		for (L2PcInstance _member : getPartyMembers())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM);
			sm.addString(player.getName());
			_member.sendPacket(sm);
			_member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}
	
	public void changeLeader(L2PcInstance newLeader)
	{
		// Get current leader
		L2PcInstance oldLeader = _members.get(0);
		// Remove new leader
		_members.remove(newLeader);
		// Move him to first position
		_members.set(0,newLeader);
		// Add old leader as normal member
		_members.add(oldLeader);
		// Broadcast change
		for(L2PcInstance member : getPartyMembers())
		{
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_LEADER_CHANGED));
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public L2PcInstance getOwner()
	{
		return _members.get(0);
	}
	
	public int getMembers()
	{
		return _members.size();
	}
	
	public int getLootType() { return _loot; }
	public void setLootType(int loot) { _loot = loot; }
	
	public int getMinLvl() { return _minlvl; }
	public void setMinLvl(int minlvl) { _minlvl = minlvl; }
	
	public int getMaxLvl() { return _maxlvl; }
	public void setMaxLvl(int maxlvl) { _maxlvl = maxlvl; }
	
	public int getLocation() { return _location; }
	public void setLocation(int loc) { _location = loc; }
	
	public int getMaxMembers() { return _maxmem; }
	public void setMaxMembers(int maxmem) { _maxmem = maxmem; }
	
	public String getTitle() { return _title; }
	public void setTitle(String title) { _title = title; }
}