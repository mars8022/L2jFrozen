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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPledgeInfo.class.getName());

	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		if(Config.DEBUG)
		{
			_log.fine("infos for clan " + _clanId + " requested");
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		L2Clan clan = ClanTable.getInstance().getClan(_clanId);

		if (activeChar == null)
			return;
		
		if(clan == null)
		{   if (Config.DEVELOPER){ 
			_log.warning("Clan data for clanId "+_clanId+" is missing for player "+activeChar.getName());}
			return; // we have no clan data ?!? should not happen
		}
		activeChar.sendPacket(new PledgeInfo(clan));
	}

	@Override
	public String getType()
	{
		return "[C] 66 RequestPledgeInfo";
	}
}
