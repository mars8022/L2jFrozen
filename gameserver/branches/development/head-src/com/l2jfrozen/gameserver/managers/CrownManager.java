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
package com.l2jfrozen.gameserver.managers;

import java.util.logging.Logger;

import com.l2jfrozen.gameserver.datatables.CrownTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * @author evill33t Reworked by NB4L1
 */
public class CrownManager
{
	protected static final Logger _log = Logger.getLogger(CrownManager.class.getName());
	private static CrownManager _instance;

	public static final CrownManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new CrownManager();
		}
		return _instance;
	}

	public CrownManager()
	{
		_log.info("CrownManager: initialized");
	}

	public void checkCrowns(L2Clan clan)
	{
		if(clan == null)
			return;

		for(L2ClanMember member : clan.getMembers())
		{
			if(member != null && member.isOnline() && member.getPlayerInstance() != null)
			{
				checkCrowns(member.getPlayerInstance());
			}
		}
	}

	public void checkCrowns(L2PcInstance activeChar)
	{
		if(activeChar == null)
			return;

		boolean isLeader = false;
		int crownId = -1;

		L2Clan activeCharClan = activeChar.getClan();
		L2ClanMember activeCharClanLeader;

		if(activeCharClan != null)
		{
			activeCharClanLeader = activeChar.getClan().getLeader();
		}
		else
		{
			activeCharClanLeader = null;
		}

		if(activeCharClan != null)
		{
			Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);

			if(activeCharCastle != null)
			{
				crownId = CrownTable.getCrownId(activeCharCastle.getCastleId());
			}

			activeCharCastle = null;

			if(activeCharClanLeader != null && activeCharClanLeader.getObjectId() == activeChar.getObjectId())
			{
				isLeader = true;
			}
		}

		activeCharClan = null;
		activeCharClanLeader = null;

		if(crownId > 0)
		{
			if(isLeader && activeChar.getInventory().getItemByItemId(6841) == null)
			{
				activeChar.addItem("Crown", 6841, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}

			if(activeChar.getInventory().getItemByItemId(crownId) == null)
			{
				activeChar.addItem("Crown", crownId, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}

		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;

		for(L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if(CrownTable.getCrownList().contains(item.getItemId()))
			{
				if(crownId > 0)
				{
					if(item.getItemId() == crownId)
					{
						if(!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if(item.getItemId() == 6841 && isLeader)
					{
						if(!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}

				activeChar.destroyItem("Removing Crown", item, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
	}
}
