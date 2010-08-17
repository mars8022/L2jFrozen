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

import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.FortManager;
import interlude.gameserver.model.entity.Castle;
import interlude.gameserver.model.entity.Fort;
import interlude.gameserver.network.serverpackets.FortSiegeDefenderList;
import interlude.gameserver.network.serverpackets.SiegeDefenderList;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestSiegeDefenderList extends L2GameClientPacket
{
	private static final String _C__a3_RequestSiegeDefenderList = "[C] a3 RequestSiegeDefenderList";
	// private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	private int _castleId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
	}

	@Override
	protected void runImpl()
	{
		if (_castleId < 100)
		{
			Castle castle = CastleManager.getInstance().getCastleById(_castleId);
			if (castle == null)
				return;

			SiegeDefenderList sdl = new SiegeDefenderList(castle);
			sendPacket(sdl);
		}
		else
		{
			Fort fort = FortManager.getInstance().getFortById(_castleId);
			if (fort == null)
				return;

			FortSiegeDefenderList sdl = new FortSiegeDefenderList(fort);
			sendPacket(sdl);
		}
	}

	@Override
	public String getType()
	{
		return _C__a3_RequestSiegeDefenderList;
	}
}
