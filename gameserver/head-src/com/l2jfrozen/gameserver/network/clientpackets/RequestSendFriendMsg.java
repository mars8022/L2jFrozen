/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.FriendRecvMsg;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author L2JFrozen
 */
public final class RequestSendFriendMsg extends L2GameClientPacket
{
	private static java.util.logging.Logger _logChat = java.util.logging.Logger.getLogger("chat");
	
	private String _message;
	private String _reciever;
	
	@Override
	protected void readImpl()
	{
		_message = readS();
		_reciever = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
		if (targetPlayer == null || !targetPlayer.getFriendList().contains(activeChar.getName()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, _message);
			record.setLoggerName("chat");
			record.setParameters(new Object[]
			{
				"PRIV_MSG",
				"[" + activeChar.getName() + " to " + _reciever + "]"
			});
			
			_logChat.log(record);
		}
		
		final FriendRecvMsg frm = new FriendRecvMsg(activeChar.getName(), _reciever, _message);
		targetPlayer.sendPacket(frm);
	}
	
	@Override
	public String getType()
	{
		return "[C] CC RequestSendMsg";
	}
}
