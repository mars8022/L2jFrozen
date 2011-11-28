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
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestQuestAbort extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestQuestAbort.class.getName());

	private int _questId;

	@Override
	protected void readImpl()
	{
		_questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Quest qe = null;
		if(!Config.ALT_DEV_NO_QUESTS)
			qe = QuestManager.getInstance().getQuest(_questId);
		
		if(qe != null)
		{
			QuestState qs = activeChar.getQuestState(qe.getName());
			if(qs != null)
			{
				qs.exitQuest(true);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Quest aborted.");
				activeChar.sendPacket(sm);
				sm = null;
				QuestList ql = new QuestList();
				activeChar.sendPacket(ql);
			}
			else
			{
				if(Config.DEBUG)
				{
					_log.info("Player '" + activeChar.getName() + "' try to abort quest " + qe.getName() + " but he didn't have it started.");
				}
			}
		}
		else
		{
			if(Config.DEBUG)
			{
				_log.warning("Quest (id='" + _questId + "') not found.");
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 64 RequestQuestAbort";
	}
}
