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
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz Format: cddd
 */
public final class DlgAnswer extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(DlgAnswer.class.getName());

	private int _messageId, _answer, _requestId;

	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requestId = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(Config.DEBUG)
		{
			_log.fine(getType() + ": Answer acepted. Message ID " + _messageId + ", asnwer " + _answer + ", unknown field " + _requestId);
		}

		if(_messageId == SystemMessageId.RESSURECTION_REQUEST.getId())
		{
			activeChar.reviveAnswer(_answer);
		}
		else if(_messageId == 614 && Config.L2JMOD_ALLOW_WEDDING)
		{
			activeChar.EngageAnswer(_answer);
		}
		else if(_messageId == SystemMessageId.S1.getId())
			if(activeChar.dialog != null)
			{
				activeChar.dialog.onDlgAnswer(activeChar);
				activeChar.dialog = null;
			}
	}

	@Override
	public String getType()
	{
		return "[C] C5 DlgAnswer";
	}
}
