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
package interlude.gameserver.network.clientpackets;

import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

/**
 * Format:(ch) h
 * @author chris_00
 * @author -Wooden-
 */
public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private static final String _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO = "[C] D0:26 RequestExMPCCShowPartyMembersInfo";
	private int _partyLeaderId;

	@Override
	protected void readImpl()
	{
		_partyLeaderId = readD();
	}

	/**
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2Object player = L2World.getInstance().findObject(_partyLeaderId);
		if (player != null && player instanceof L2PcInstance 
				&& ((L2PcInstance) player).getParty() != null)
			getClient().getActiveChar().sendPacket(new ExMPCCShowPartyMemberInfo(((L2PcInstance) player).getParty()));
	}

	/**
	 * @see interlude.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO;
	}
}
