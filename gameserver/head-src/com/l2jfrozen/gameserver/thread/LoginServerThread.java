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
package com.l2jfrozen.gameserver.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.NewCrypt;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jfrozen.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jfrozen.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jfrozen.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jfrozen.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jfrozen.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jfrozen.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jfrozen.gameserver.network.loginserverpackets.InitLS;
import com.l2jfrozen.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jfrozen.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jfrozen.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jfrozen.gameserver.network.serverpackets.AuthLoginFail;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jfrozen.util.Util;
import com.l2jfrozen.util.random.Rnd;

public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class);
	
	/** The LoginServerThread singleton */
	private static LoginServerThread _instance;
	
	private static final int REVISION = 0x0102;
	private RSAPublicKey _publicKey;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final List<WaitingClient> _waitingClients;
	private final Map<String, L2GameClient> _accountsInGameServer;
	private int _status;
	private String _serverName;
	private final String _gameExternalHost;
	private final String _gameInternalHost;
	
	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new FastList<>();
		_accountsInGameServer = new FastMap<String, L2GameClient>().shared();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	public static LoginServerThread getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginServerThread();
		}
		return _instance;
	}
	
	@Override
	public void run()
	{
		while (!_interrupted)
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				LOGGER.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				
				if (_out != null)
					synchronized (_out) // avoids tow threads writing in the mean time
					{
						_out = new BufferedOutputStream(_loginSocket.getOutputStream());
					}
				else
					_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				
				// init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!_interrupted)
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = lengthHi * 256 + lengthLo;
					
					if (lengthHi < 0)
					{
						LOGGER.info("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					final byte[] incoming = new byte[length];
					incoming[0] = (byte) lengthLo;
					incoming[1] = (byte) lengthHi;
					
					int receivedBytes = 0;
					int newBytes = 0;
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, 2, length - 2);
						receivedBytes = receivedBytes + newBytes;
					}
					
					if (receivedBytes != length - 2)
					{
						LOGGER.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					byte[] decrypt = new byte[length - 2];
					System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
					// decrypt if we have a key
					decrypt = _blowfish.decrypt(decrypt);
					checksumOk = NewCrypt.verifyChecksum(decrypt);
					
					if (!checksumOk)
					{
						LOGGER.warn("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					if (Config.DEBUG)
					{
						LOGGER.warn("[C]\n" + Util.printData(decrypt));
					}
					
					final int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 00:
							final InitLS init = new InitLS(decrypt);
							if (Config.DEBUG)
							{
								LOGGER.info("Init received");
							}
							if (init.getRevision() != REVISION)
							{
								// TODO: revision mismatch
								LOGGER.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if (Config.DEBUG)
								{
									LOGGER.info("RSA key set up");
								}
							}
							
							catch (final GeneralSecurityException e)
							{
								if (Config.ENABLE_ALL_EXCEPTIONS)
									e.printStackTrace();
								
								LOGGER.warn("Troubles while init the public key send by login");
								break;
							}
							// send the blowfish key through the rsa encryption
							final BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);
							if (Config.DEBUG)
							{
								LOGGER.info("Sent new blowfish key");
							}
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							if (Config.DEBUG)
							{
								LOGGER.info("Changed blowfish key");
							}
							final AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							if (Config.DEBUG)
							{
								LOGGER.info("Sent AuthRequest to login");
							}
							break;
						case 01:
							final LoginServerFail lsf = new LoginServerFail(decrypt);
							LOGGER.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 02:
							final AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							LOGGER.info("Registered on login as Server " + _serverID + " : " + _serverName);
							final ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							sendPacket(st);
							if (L2World.getAllPlayersCount() > 0)
							{
								final FastList<String> playerList = new FastList<>();
								for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
								{
									playerList.add(player.getAccountName());
								}
								final PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						case 03:
							final PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (final WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									if (Config.DEBUG)
									{
										LOGGER.info("Login accepted player " + wcToRemove.account + " waited(" + (GameTimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
									}
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									pig = null;
									
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									
									// before the char selection, check shutdown status
									if (GameServer.getSelectorThread().isShutdown())
									{
										wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
										wcToRemove.gameClient.closeNow();
									}
									else
									{
										CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
										wcToRemove.gameClient.getConnection().sendPacket(cl);
										wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
										cl = null;
									}
									
								}
								else
								{
									LOGGER.warn("session key is not correct. closing connection");
									wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
									wcToRemove.gameClient.closeNow();
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						case 04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							kp = null;
							break;
					}
				}
			}
			catch (final UnknownHostException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info("Deconnected from Login, Trying to reconnect:");
				LOGGER.info(e.toString());
			}
			catch (final IOException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info("Deconnected from Login, Trying to reconnect..");
				// LOGGER.info(e.toString());
			}
			finally
			{
				if (_out != null)
					synchronized (_out) // avoids tow threads writing in the mean time
					{
						try
						{
							_loginSocket.close();
						}
						catch (final Exception e)
						{
							if (Config.ENABLE_ALL_EXCEPTIONS)
								e.printStackTrace();
						}
					}
				else
					try
					{
						_loginSocket.close();
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
					}
				
			}
			
			try
			{
				Thread.sleep(5000); // 5 seconds
			}
			catch (final InterruptedException e)
			{
				// no need exception LOGGER
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(final String acc, final L2GameClient client, final SessionKey key)
	{
		if (Config.DEBUG)
		{
			LOGGER.info(key);
		}
		
		final WaitingClient wc = new WaitingClient(acc, client, key);
		
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		
		try
		{
			sendPacket(par);
		}
		catch (final IOException e)
		{
			LOGGER.warn("Error while sending player auth request");
			
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		
		par = null;
	}
	
	public void removeWaitingClient(final L2GameClient client)
	{
		WaitingClient toRemove = null;
		
		synchronized (_waitingClients)
		{
			for (final WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
		
		toRemove = null;
	}
	
	public void sendLogout(final String account)
	{
		if (account == null)
			return;
		PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (final IOException e)
		{
			LOGGER.warn("Error while sending logout packet to login");
			
			e.printStackTrace();
		}
		
		pl = null;
	}
	
	/*
	 * public void addGameServerLogin(String account, L2GameClient client) { _accountsInGameServer.put(account, client); }
	 */
	
	public boolean addGameServerLogin(final String account, final L2GameClient client)
	{
		
		final L2GameClient savedClient = _accountsInGameServer.get(account);
		
		if (savedClient != null)
		{
			if (savedClient.isDetached())
			{
				if (Config.DEBUG)
					LOGGER.info("Old Client was disconnected: Offline or OfflineMode --> Login Again");
				_accountsInGameServer.put(account, client);
				
				return true;
			}
			if (Config.DEBUG)
				LOGGER.info("Old Client was online --> Close Old Client Connection");
			
			savedClient.closeNow();
			_accountsInGameServer.remove(account);
			return false;
		}
		
		if (Config.DEBUG)
			LOGGER.info("Client was not online --> New Client Connection");
		
		_accountsInGameServer.put(account, client);
		
		return true;
	}
	
	public void sendAccessLevel(final String account, final int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		
		cal = null;
	}
	
	private String hexToString(final byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(final String account)
	{
		if (_accountsInGameServer.get(account) != null)
		{
			_accountsInGameServer.get(account).closeNow();
			LoginServerThread.getInstance().sendLogout(account);
			
			if (Config.DEBUG)
				LOGGER.debug("called [doKickPlayer], closing connection");
			
		}
	}
	
	public static byte[] generateHex(final int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		if (Config.DEBUG)
		{
			LOGGER.debug("Generated random String:  \"" + array + "\"");
		}
		return array;
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(final GameServerBasePacket sl) throws IOException
	{
		if (_interrupted)
		{
			return;
		}
		
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG)
		{
			LOGGER.debug("[S]\n" + Util.printData(data));
		}
		data = _blowfish.crypt(data);
		
		final int len = data.length + 2;
		
		if (_out != null && !_loginSocket.isClosed() && _loginSocket.isConnected())
			synchronized (_out) // avoids tow threads writing in the mean time
			{
				_out.write(len & 0xff);
				_out.write(len >> 8 & 0xff);
				_out.write(data);
				_out.flush();
			}
	}
	
	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(final int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	/**
	 * @param id
	 * @param value
	 */
	public void sendServerStatus(final int id, final int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
		
		ss = null;
	}
	
	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}
	
	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}
	
	public void setServerStatus(final int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey = -1; // by Azagthtot
		
		public SessionKey(final int loginOK1, final int loginOK2, final int playOK1, final int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(final String acc, final L2GameClient client, final SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}
	
	private boolean _interrupted = false;
	
	@Override
	public void interrupt()
	{
		_interrupted = true;
		super.interrupt();
	}
	
	@Override
	public boolean isInterrupted()
	{
		return _interrupted;
	}
}
