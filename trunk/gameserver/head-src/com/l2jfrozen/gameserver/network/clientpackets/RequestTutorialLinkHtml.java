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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialCloseHtml;

/**
 * @author ProGramMoS
 */

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private static final String _C__7B_REQUESTTUTORIALLINKHTML = "[C] 7b RequestTutorialLinkHtml";
	String _bypass;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		try
		{
			int Id = Integer.parseInt(_bypass);
			if(Config.BOT_PROTECTOR && Id > 100000 && Id < 100006)
			{
				player.checkAnswer(Id);
				player.sendPacket(new TutorialCloseHtml());
				return;
			}
		}
		catch(Exception e) {
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}

		QuestState qs = player.getQuestState("255_Tutorial");
		if(qs != null)
		{
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}

	@Override
	public String getType()
	{
		return _C__7B_REQUESTTUTORIALLINKHTML;
	}
}
