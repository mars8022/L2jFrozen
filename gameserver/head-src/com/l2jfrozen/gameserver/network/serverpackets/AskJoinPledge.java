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
package com.l2jfrozen.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
	private int _requestorObjId;
	private String _pledgeName;

	public AskJoinPledge(int requestorObjId, String pledgeName)
	{
		_requestorObjId = requestorObjId;
		_pledgeName = pledgeName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x32);
		writeD(_requestorObjId);
		writeS(_pledgeName);
	}

	@Override
	public String getType()
	{
		return "[S] 32 AskJoinPledge";
	}
}