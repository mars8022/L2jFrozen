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
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;

public final class AuthLogin extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(AuthLogin.class.getName());

	// loginName + keys must match what the loginserver used.
	private String _loginName;
	/*private final long _key1;
	private final long _key2;
	private final long _key3;
	private final long _key4;*/
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}

	@Override
	protected void runImpl()
	{
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);

		if(Config.DEBUG)
		{
			_log.info("user:" + _loginName);
			_log.info("key:" + key);
		}

		L2GameClient client = getClient();

		// avoid potential exploits
		if(client.getAccountName() == null)
		{
			client.setAccountName(_loginName);
			LoginServerThread.getInstance().addGameServerLogin(_loginName, client);
			LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 08 AuthLogin";
	}
}
