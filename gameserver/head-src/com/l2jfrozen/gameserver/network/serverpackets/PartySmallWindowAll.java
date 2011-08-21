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
package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * sample 63 01 00 00 00 count c1 b2 e0 4a object id 54 00 75 00 65 00 73 00 64 00 61 00 79 00 00 00 name 5a 01 00 00 hp
 * 5a 01 00 00 hp max 89 00 00 00 mp 89 00 00 00 mp max 0e 00 00 00 level 12 00 00 00 class 00 00 00 00 01 00 00 00
 * format d (dSdddddddd)
 * 
 * @version $Revision: 1.6.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowAll extends L2GameServerPacket
{
	private static final String _S__63_PARTYSMALLWINDOWALL = "[S] 4e PartySmallWindowAll";
	private L2Party _party; 
 	private L2PcInstance _exclude; 
 	private int _dist, _LeaderOID;

 	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
 		_exclude = exclude; 
 	 	_party = party; 
 	 	_LeaderOID = _party.getPartyLeaderOID(); 
 	 	_dist = _party.getLootDistribution(); 
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(_LeaderOID); 
	 	writeD(_dist); 
	 	writeD(_party.getMemberCount() - 1);

	 	for (L2PcInstance member : _party.getPartyMembers())
		{
	 		if ((member != null) && (member != _exclude))
			{
				writeD(member.getObjectId());
				writeS(member.getName());

				writeD((int) member.getCurrentCp()); //c4
				writeD(member.getMaxCp()); //c4

				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0);//writeD(0x01); ??
				writeD(member.getRace().ordinal());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__63_PARTYSMALLWINDOWALL;
	}
}
