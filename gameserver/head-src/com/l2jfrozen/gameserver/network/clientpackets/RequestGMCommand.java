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

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewItemList;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewQuestList;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public final class RequestGMCommand extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestGMCommand.class.getName());

	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		//_unknown  = readD();
	}

	@Override
	protected void runImpl()
	{

		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);

		// prevent non gm or low level GMs from vieweing player stuff
		if(player == null || !getClient().getActiveChar().getAccessLevel().allowAltG())
			return;

		switch(_command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2: // player clan
			{
				if(player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				break;
			}
			case 6: // player warehouse
			{
				// gm warehouse view to be implemented
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}

		}
	}

	@Override
	public String getType()
	{
		return "[C] 6e RequestGMCommand";
	}
}
