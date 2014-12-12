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
package com.l2jfrozen.gameserver.network;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Beetle
 */
public class L2IrcClient extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(L2IrcClient.class);
	protected static final Logger _logChat = Logger.getLogger("irc");
	
	protected IRCConnection conn;
	protected String channel;
	protected String nickname;
	
	public L2IrcClient(final String host, final int port, final String pass, final String nick, final String user, final String name, final boolean ssl, final String Chan)
	{
		if (!ssl)
		{
			conn = new IRCConnection(host, new int[]
			{
				port
			}, pass, nick, user, name);
		}
		else
		{
			conn = new SSLIRCConnection(host, new int[]
			{
				port
			}, pass, nick, user, name);
			((SSLIRCConnection) conn).addTrustManager(new TrustManager());
		}
		channel = Chan;
		nickname = nick;
		conn.addIRCEventListener(new Listener());
		conn.setEncoding("UTF-8");
		conn.setPong(true);
		conn.setDaemon(false);
		conn.setColors(false);
		start();
	}
	
	public void connect() throws IOException
	{
		if (!conn.isConnected())
		{
			conn.connect();
			conn.send("JOIN " + channel);
			setDaemon(true);
		}
	}
	
	public void disconnect()
	{
		if (conn.isConnected())
		{
			conn.close();
			conn.setDaemon(false);
		}
	}
	
	public void send(final String Text)
	{
		if (checkConnection())
			conn.send(Text);
	}
	
	public void send(final String target, final String Text)
	{
		if (checkConnection())
			conn.doPrivmsg(target, Text);
	}
	
	public void sendChan(final String Text)
	{
		if (checkConnection())
		{
			conn.doPrivmsg(channel, Text);
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + channel + "> text");
		}
	}
	
	public boolean checkConnection()
	{
		if (!conn.isConnected())
		{
			try
			{
				conn.close();
				connect();
			}
			catch (final Exception exc)
			{
				exc.printStackTrace();
			}
		}
		return conn.isConnected();
	}
	
	public class TrustManager implements SSLTrustManager
	{
		private X509Certificate[] chain;
		
		@Override
		public X509Certificate[] getAcceptedIssuers()
		{
			return chain != null ? chain : new X509Certificate[0];
		}
		
		@Override
		public boolean isTrusted(final X509Certificate[] chain)
		{
			this.chain = chain;
			return true;
		}
		
	}
	
	/**
	 * Treats IRC events.
	 */
	public class Listener implements IRCEventListener
	{
		
		private boolean isconnected;
		
		@Override
		public void onRegistered()
		{
			LOGGER.info("IRC: Connected");
			
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Connected");
			
			if (!Config.IRC_LOGIN_COMMAND.trim().equals(""))
				send(Config.IRC_LOGIN_COMMAND.trim());
			
			if (Config.IRC_NICKSERV)
				send(Config.IRC_NICKSERV_NAME, Config.IRC_NICKSERV_COMMAND);
			
			isconnected = true;
		}
		
		@Override
		public void onDisconnected()
		{
			LOGGER.info("IRC: Disconnected");
			
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Disconnected");
			
			isconnected = false;
			conn.close();
		}
		
		@Override
		public void onError(final String msg)
		{
			LOGGER.info("IRC: Error: " + msg);
			
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Error: " + msg);
		}
		
		@Override
		public void onError(final int num, final String msg)
		{
			LOGGER.info("IRC: Error #" + num + ": " + msg);
			
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Error #" + num + ": " + msg);
			
			// nickname already in use
			if (num == 433)
			{
				final Integer random = Rnd.get(999);
				send("NICK " + nickname + random);
				send("JOIN " + channel);
			}
		}
		
		@Override
		public void onInvite(final String chan, final IRCUser u, final String nickPass)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " invites " + nickPass);
		}
		
		@Override
		public void onJoin(final String chan, final IRCUser u)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " joins");
		}
		
		@Override
		public void onKick(final String chan, final IRCUser u, final String nickPass, final String msg)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " kicks " + nickPass);
		}
		
		@Override
		public void onMode(final IRCUser u, final String nickPass, final String mode)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass);
		}
		
		@Override
		public void onMode(final String chan, final IRCUser u, final IRCModeParser mp)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " sets mode: " + mp.getLine());
		}
		
		@Override
		public void onNick(final IRCUser u, final String nickNew)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC Nick: " + u.getNick() + " is now known as " + nickNew);
		}
		
		@Override
		public void onNotice(final String target, final IRCUser u, final String msg)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("Irc " + target + "> " + u.getNick() + " (notice): " + msg);
		}
		
		@Override
		public void onPart(final String chan, final IRCUser u, final String msg)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " parts");
		}
		
		@Override
		public void onPrivmsg(final String chan, final IRCUser u, final String msg)
		{
			
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + ": " + msg);
			
			if (chan.equalsIgnoreCase(channel))
			{
				if (Config.IRC_TO_GAME_TYPE.equals("global") || Config.IRC_TO_GAME_TYPE.equals("special"))
				{
					if (Config.IRC_TO_GAME_TYPE.equals("global") || (Config.IRC_TO_GAME_TYPE.equals("special") && msg.substring(0, 1).equals(Config.IRC_TO_GAME_SPECIAL_CHAR) && msg.length() >= 2))
					{
						
						String sendmsg;
						if (Config.IRC_TO_GAME_TYPE.equals("special"))
							sendmsg = msg.substring(1, msg.length());
						else
							sendmsg = msg;
						
						Integer ChatType = 1;
						
						if (Config.IRC_TO_GAME_DISPLAY.equals("trade"))
							ChatType = 8;
						if (Config.IRC_TO_GAME_DISPLAY.equals("hero"))
							ChatType = 17;
						
						final CreatureSay cs = new CreatureSay(0, ChatType, "[IRC] " + u.getNick(), sendmsg);
						
						for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							player.sendPacket(cs);
						}
					}
				}
			}
			
			if (msg.equals("!online"))
			{
				sendChan("Online Players: " + L2World.getAllPlayersCount() + " / " + LoginServerThread.getInstance().getMaxPlayer());
			}
			
			if (msg.equals("!gmlist"))
			{
				if (GmListTable.getInstance().getAllGms(false).size() == 0)
					sendChan(Config.IRC_NO_GM_MSG);
				else
					for (final L2PcInstance gm : GmListTable.getInstance().getAllGms(false))
						sendChan(gm.getName());
			}
			if (msg.equals("!rates"))
			{
				sendChan("XP Rate: " + Config.RATE_XP + " | " + "SP Rate: " + Config.RATE_SP + " | " + "Spoil Rate: " + Config.RATE_DROP_SPOIL + " | " + "Adena Rate: " + Config.RATE_DROP_ADENA + " | " + "Drop Rate: " + Config.RATE_DROP_ITEMS + " | " + "Party XP Rate: " + Config.RATE_PARTY_XP + " | " + "Party SP Rate: " + Config.RATE_PARTY_SP);
			}
			
			if (msg.equals("!showon"))
			{
				if (L2World.getInstance().getAllPlayers().size() == 0)
					sendChan(Config.IRC_NO_PLAYER_ONLINE);
				else
				{
					String _onlineNames = Config.IRC_PLAYER_ONLINE + "";
					boolean _isFirst = true;
					for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						_onlineNames = _onlineNames + (_isFirst ? " " : ", ") + player.getName();
						_isFirst = false;
					}
					sendChan(_onlineNames);
				}
			}
			
		}
		
		@Override
		public void onQuit(final IRCUser u, final String msg)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC Quit: " + u.getNick());
		}
		
		@Override
		public void onReply(final int num, final String value, final String msg)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC REPLY #" + num + ": " + value + " " + msg);
		}
		
		@Override
		public void onTopic(final String chan, final IRCUser u, final String topic)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: " + chan + "> " + u.getNick() + " changes topic into: " + topic);
		}
		
		@Override
		public void onPing(final String p)
		{
			if (Config.IRC_LOG_CHAT)
				_logChat.info("IRC: Ping Pong");
			
			// keep connection alive
			conn.doPong(p);
		}
		
		@Override
		public void unknown(final String a, final String b, final String c, final String d)
		{
			LOGGER.warn("IRC UNKNOWN: " + a + " b " + c + " " + d);
		}
		
		public boolean isConnected()
		{
			return isconnected;
		}
	}
}
