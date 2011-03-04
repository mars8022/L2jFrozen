/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.netcore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.util.FloodProtectorAction;
import com.l2jfrozen.gameserver.util.FloodProtectorConfig;
import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.loginserver.LoginController;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;

import javolution.util.FastList;

/**
 * @author KenM<BR>
 *         Parts of design based on networkcore from WoodenGil
 */
public final class SelectorThread<T extends MMOClient<?>> extends Thread
{
	// default BYTE_ORDER
	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	// default HEADER_SIZE
	private static final int HEADER_SIZE = 2;
	// Selector
	private final Selector _selector;
	// Implementations
	private final IPacketHandler<T> _packetHandler;
	private final IMMOExecutor<T> _executor;
	private final IClientFactory<T> _clientFactory;
	private final IAcceptFilter _acceptFilter;
	// Configurations
	private final int HELPER_BUFFER_SIZE;
	private final int HELPER_BUFFER_COUNT;
	private final int MAX_SEND_PER_PASS;
	private final int MAX_READ_PER_PASS;
	private final long SLEEP_TIME;
	// Main Buffers
	private final ByteBuffer DIRECT_WRITE_BUFFER;
	private final ByteBuffer WRITE_BUFFER;
	private final ByteBuffer READ_BUFFER;
	// String Buffer
	private final NioNetStringBuffer STRING_BUFFER;
	// ByteBuffers General Purpose Pool
	private final FastList<ByteBuffer> _bufferPool;
	// Pending Close
	private final NioNetStackList<MMOConnection<T>> _pendingClose;
	
	private boolean _shutdown;
	
	public SelectorThread(final SelectorConfig sc, final IMMOExecutor<T> executor, final IPacketHandler<T> packetHandler, final IClientFactory<T> clientFactory, final IAcceptFilter acceptFilter) throws IOException
	{
		super.setName("SelectorThread-" + super.getId());
		
		HELPER_BUFFER_SIZE = sc.HELPER_BUFFER_SIZE;
		HELPER_BUFFER_COUNT = sc.HELPER_BUFFER_COUNT;
		MAX_SEND_PER_PASS = sc.MAX_SEND_PER_PASS;
		MAX_READ_PER_PASS = sc.MAX_READ_PER_PASS;
		
		SLEEP_TIME = sc.SLEEP_TIME;
		
		DIRECT_WRITE_BUFFER = ByteBuffer.allocateDirect(sc.WRITE_BUFFER_SIZE).order(BYTE_ORDER);
		WRITE_BUFFER = ByteBuffer.wrap(new byte[sc.WRITE_BUFFER_SIZE]).order(BYTE_ORDER);
		READ_BUFFER = ByteBuffer.wrap(new byte[sc.READ_BUFFER_SIZE]).order(BYTE_ORDER);
		
		STRING_BUFFER = new NioNetStringBuffer(64 * 1024);
		
		_pendingClose = new NioNetStackList<MMOConnection<T>>();
		_bufferPool = new FastList<ByteBuffer>();
		
		for (int i = 0; i < HELPER_BUFFER_COUNT; i++)
		{
			_bufferPool.addLast(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER));
		}
		
		_acceptFilter = acceptFilter;
		_packetHandler = packetHandler;
		_clientFactory = clientFactory;
		_executor = executor;
		_selector = Selector.open();
	}
	
	public final void openServerSocket(InetAddress address, int tcpPort) throws IOException
	{
		ServerSocketChannel selectable = ServerSocketChannel.open();
		selectable.configureBlocking(false);
		
		ServerSocket ss = selectable.socket();
		
		if (address == null)
			ss.bind(new InetSocketAddress(tcpPort));
		else
			ss.bind(new InetSocketAddress(address, tcpPort));
		
		selectable.register(_selector, SelectionKey.OP_ACCEPT);
	}
	
	final ByteBuffer getPooledBuffer()
	{
		if (_bufferPool.isEmpty())
			return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
		
		return _bufferPool.removeFirst();
	}
	
	final void recycleBuffer(final ByteBuffer buf)
	{
		if (_bufferPool.size() < HELPER_BUFFER_COUNT)
		{
			buf.clear();
			_bufferPool.addLast(buf);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void run()
	{
		int selectedKeysCount = 0;
		
		SelectionKey key;
		MMOConnection<T> con;
		
		Iterator<SelectionKey> selectedKeys;
		
		while (!_shutdown)
		{
			try
			{
				selectedKeysCount = _selector.selectNow();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if (selectedKeysCount > 0)
			{
				selectedKeys = _selector.selectedKeys().iterator();
				
				while (selectedKeys.hasNext())
				{
					key = selectedKeys.next();
					selectedKeys.remove();
					
					con = (MMOConnection<T>) key.attachment();
					
					switch (key.readyOps())
					{
						case SelectionKey.OP_CONNECT:
							finishConnection(key, con);
							break;
						case SelectionKey.OP_ACCEPT:
							acceptConnection(key, con);
							break;
						case SelectionKey.OP_READ:
							readPacket(key, con);
							break;
						case SelectionKey.OP_WRITE:
							writePacket(key, con);
							break;
						case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
							writePacket(key, con);
							if (key.isValid())
								readPacket(key, con);
							break;
					}
				}
			}
			
			synchronized (_pendingClose)
			{
				while (!_pendingClose.isEmpty())
				{
					con = _pendingClose.removeFirst();
					writeClosePacket(con);
					closeConnectionImpl(con.getSelectionKey(), con);
				}
			}
			
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		closeSelectorThread();
	}
	
	private final void finishConnection(final SelectionKey key, final MMOConnection<T> con)
	{
		try
		{
			((SocketChannel) key.channel()).finishConnect();
		}
		catch (IOException e)
		{
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			con.getClient().onForcedDisconnection();
			closeConnectionImpl(key, con);
		}
		
		// key might have been invalidated on finishConnect()
		if (key.isValid())
		{
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
		}
	}
	
	private final void acceptConnection(final SelectionKey key, MMOConnection<T> con)
	{
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc;
		
		try
		{
			while ((sc = ssc.accept()) != null)
			{
				if (_acceptFilter == null || _acceptFilter.accept(sc))
				{
					sc.configureBlocking(false);
					SelectionKey clientKey = sc.register(_selector, SelectionKey.OP_READ);
					con = new MMOConnection<T>(this, sc.socket(), clientKey);
					con.setClient(_clientFactory.create(con));
					clientKey.attach(con);
				}
				else
					sc.socket().close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final void readPacket(final SelectionKey key, final MMOConnection<T> con)
	{
		if (!con.isClosed())
		{
			ByteBuffer buf;
			if ((buf = con.getReadBuffer()) == null)
			{
				buf = READ_BUFFER;
			}
			
			// if we try to to do a read with no space in the buffer it will
			// read 0 bytes
			// going into infinite loop
			if (buf.position() == buf.limit())
				System.exit(0);
			
			int result = -2;
			
			try
			{
				result = con.read(buf);
			}
			catch (IOException e)
			{
				// error handling goes bellow
				if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
					e.printStackTrace();
				
			}
			
			if (result > 0)
			{
				buf.flip();
				
				final T client = con.getClient();
				
				for (int i = 0; i < MAX_READ_PER_PASS; i++)
				{
					if (!tryReadPacket(key, client, buf, con))
						return;
				}
				
				// only reachable if MAX_READ_PER_PASS has been reached
				// check if there are some more bytes in buffer
				// and allocate/compact to prevent content lose.
				if (buf.remaining() > 0)
				{
					// did we use the READ_BUFFER ?
					if (buf == READ_BUFFER)
						// move the pending byte to the connections READ_BUFFER
						allocateReadBuffer(con);
					else
						// move the first byte to the beginning :)
						buf.compact();
				}
			}
			else
			{
				switch (result)
				{
					case 0:
					case -1:
						closeConnectionImpl(key, con);
						break;
					case -2:
						con.getClient().onForcedDisconnection();
						closeConnectionImpl(key, con);
						break;
				}
			}
		}
	}
	
	private final boolean tryReadPacket(final SelectionKey key, final T client, final ByteBuffer buf, final MMOConnection<T> con)
	{
		switch (buf.remaining())
		{
			case 0:
				// buffer is full
				// nothing to read
				return false;
			case 1:
				// we don`t have enough data for header so we need to read
				key.interestOps(key.interestOps() | SelectionKey.OP_READ);
				
				// did we use the READ_BUFFER ?
				if (buf == READ_BUFFER)
					// move the pending byte to the connections READ_BUFFER
					allocateReadBuffer(con);
				else
					// move the first byte to the beginning :)
					buf.compact();
				return false;
			default:
				// data size excluding header size :>
				final int dataPending = (buf.getShort() & 0xFFFF) - HEADER_SIZE;
				
				// do we got enough bytes for the packet?
				if (dataPending <= buf.remaining())
				{
					boolean read = true;
					
					// avoid parsing dummy packets (packets without body)
					if (dataPending > 0)
					{
						final int pos = buf.position();
						
						
						if(!parseClientPacket(pos, buf, dataPending, client)){
							read = false;
						}
							
						buf.position(pos + dataPending);
						
					}
					
					// if we are done with this buffer
					if (!buf.hasRemaining())
					{
						if (buf != READ_BUFFER)
						{
							con.setReadBuffer(null);
							recycleBuffer(buf);
						}
						else
						{
							READ_BUFFER.clear();
						}
						return false;
					}
					
					return read;
					
				}
				else
				{
					// we don`t have enough bytes for the dataPacket so we need
					// to read
					key.interestOps(key.interestOps() | SelectionKey.OP_READ);
					
					// did we use the READ_BUFFER ?
					if (buf == READ_BUFFER)
					{
						// move it`s position
						buf.position(buf.position() - HEADER_SIZE);
						// move the pending byte to the connections READ_BUFFER
						allocateReadBuffer(con);
					}
					else
					{
						buf.position(buf.position() - HEADER_SIZE);
						buf.compact();
					}
					return false;
				}
		}
	}
	
	private final void allocateReadBuffer(final MMOConnection<T> con)
	{
		con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
		READ_BUFFER.clear();
	}
	
	private final boolean parseClientPacket(final int pos, final ByteBuffer buf, final int dataSize, final T client)
	{
		final boolean ret = client.decrypt(buf, dataSize);
		
		if (ret && buf.hasRemaining())
		{
			// apply limit
			final int limit = buf.limit();
			buf.limit(pos + dataSize);
			
			
			//check for flood action
			int opcode = buf.get() & 0xFF;
			int opcode2 = -1;
			
			if(opcode == 0xd0){
				
				if(buf.remaining() >= 2)
				{
					opcode2 = buf.getShort() & 0xffff;
				}
				
			}
			
			if(!tryPerformAction(opcode,opcode2,client)){
				
				return false;
			}
			
			final ReceivablePacket<T> cp = _packetHandler.handlePacket(opcode, opcode2, buf, client);
			
			if (cp != null)
			{
				cp._buf = buf;
				cp._sbuf = STRING_BUFFER;
				cp._client = client;
				
				if (cp.read())
					_executor.execute(cp);
				
				cp._buf = null;
				cp._sbuf = null;
			}
			buf.limit(limit);
		}
		
		return true;
	}
	
	/**
	 * Logger
	 */
	private static final Logger _log = Logger.getLogger(FloodProtectorAction.class.getName());
	/**
	 * Next game tick when new request is allowed.
	 */
	//private static int _nextGameTick = GameTimeController.getGameTicks();
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	//private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply
	 * punisment multiple times (flooding).
	 */
	//private volatile boolean _punishmentInProgress;
	
	private Hashtable<String, Hashtable<Integer, AtomicInteger>> clients_actions = 
		new Hashtable<String, Hashtable<Integer, AtomicInteger>>();
	
	private Hashtable<String, Hashtable<Integer, Integer>> clients_nextGameTick = 
		new Hashtable<String, Hashtable<Integer, Integer>>();

	private Hashtable<String, Boolean> punishes_in_progress = 
		new Hashtable<String, Boolean>();
	/*
	private Hashtable<Integer, AtomicInteger> received_commands_actions = 
		new Hashtable<Integer, AtomicInteger>();
	*/
	
	/**
	 * Checks whether the request is flood protected or not.
	 * 
	 * @param command
	 *            command issued or short command description
	 * 
	 * @return true if action is allowed, otherwise false
	 */
	private boolean tryPerformAction(final int opcode, final int opcode2, T client)
	{
		//filter on opcodes
		if(!isOpCodeToBeTested(opcode, opcode2, client instanceof L2LoginClient)){
			return true;
		}
		
		String account = "";
		
		if(client instanceof L2LoginClient){
			
			L2LoginClient login_cl = (L2LoginClient) client;
			account = login_cl.getAccount();
			
		}else if(client instanceof L2GameClient){
			
			L2GameClient game_cl = (L2GameClient) client;
			account = game_cl.accountName;
			
		}

		if(account==null)
			return true;
			
		final int curTick = GameTimeController.getGameTicks();
		
		Hashtable<Integer, Integer> account_nextGameTicks = clients_nextGameTick.get(account);
		if(account_nextGameTicks == null){
			account_nextGameTicks = new Hashtable<Integer, Integer>();
		}
		Integer _nextGameTick = account_nextGameTicks.get(opcode);
		if(_nextGameTick == null){
			_nextGameTick = curTick;
			account_nextGameTicks.put(opcode,_nextGameTick);
		}
		clients_nextGameTick.put(account, account_nextGameTicks);
		
		Boolean _punishmentInProgress = punishes_in_progress.get(account);
		if(_punishmentInProgress == null){
			_punishmentInProgress = false;
		}
		punishes_in_progress.put(account,_punishmentInProgress);
		
		Hashtable<Integer, AtomicInteger> received_commands_actions = clients_actions.get(account);
		if(received_commands_actions == null){
			received_commands_actions = new Hashtable<Integer, AtomicInteger>();
		}
		AtomicInteger command_count = null;
		if((command_count = received_commands_actions.get(opcode))==null){
			command_count = new AtomicInteger(0);
			received_commands_actions.put(opcode, command_count);
		}
		clients_actions.put(account,received_commands_actions);
		
		if (curTick <= _nextGameTick && !_punishmentInProgress) //time to check operations
		{
			command_count.incrementAndGet();
			clients_actions.get(account).put(opcode, command_count);
			
			if (Config.getInstance().ENABLE_MMOCORE_DEBUG)
			{
				_log.info("-- called OpCode "+ opcode+ " ~"+ String.valueOf((Config.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK)+ " ms after previous command...");
				_log.info("   total received packets with OpCode "+opcode+" into the Interval: "+command_count.get());
			}
			
			if (Config.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT > 0 && command_count.get() >= Config.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT  && Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE != null)
			{
				punishes_in_progress.put(account, true);
				
				if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
					_log.warning("ATTENTION: Account "+account+" is flooding the server...");
				
				
				if ("kick".equals(Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
				{
					if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
						_log.warning(" ------- kicking account "+account);
					kickPlayer(client);
				}
				else if ("ban".equals(Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
				{
					if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
						_log.warning(" ------- banning account "+account);
					banAccount(client);
				}
				
				//clear already punished account
				punishes_in_progress.remove(account);
				clients_nextGameTick.remove(account);
				clients_actions.remove(account);
				
				return false;
				
			}
			
			_nextGameTick = curTick + Config.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL;
			clients_nextGameTick.get(account).put(opcode, _nextGameTick);
			return true;
			
		}else{ //cur time more then checked one  --> restart opcode
			
			punishes_in_progress.put(account, false);
			clients_nextGameTick.get(account).remove(opcode);
			clients_actions.get(account).remove(opcode);
			
			return true;
			
		}
		
	}
	
	private boolean isOpCodeToBeTested(int opcode, int opcode2, boolean loginclient){
		
		if(loginclient){
			
			return !Config.getInstance().LS_LIST_PROTECTED_OPCODES.contains(opcode);
			
		}else{
			
			if(opcode == 0xd0){
				
				if(Config.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode)){
					
					return !Config.getInstance().GS_LIST_PROTECTED_OPCODES2.contains(opcode2);
					
				}else
					return true;
				
			}else{
				
				return !Config.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode);
				
			}
			
		}

	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer(T _client)
	{
		if(_client instanceof L2LoginClient){
			
			L2LoginClient login_cl = (L2LoginClient) _client;
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			_log.warning(login_cl.getAccount() +" kicked for flooding");
			
		}else if(_client instanceof L2GameClient){
			
			L2GameClient game_cl = (L2GameClient) _client;
			game_cl.closeNow();
			
			_log.warning(game_cl.accountName +" kicked for flooding");
			
		}
		
	}

	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount(T _client)
	{
		
		if(_client instanceof L2LoginClient){
			
			L2LoginClient login_cl = (L2LoginClient) _client;
			LoginController.getInstance().setAccountAccessLevel(login_cl.getAccount(), -100);
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			_log.warning(login_cl.getAccount() +" banned for flooding forever");
			
		}else if(_client instanceof L2GameClient){
			
			L2GameClient game_cl = (L2GameClient) _client;
			
			if (game_cl.getActiveChar() != null)
			{
				game_cl.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.ACC, 0);

				_log.warning(game_cl.accountName +" banned for flooding forever");

				game_cl.getActiveChar().logout();
			}
			
			game_cl.closeNow();
			
		}

		
	}

	
	private final void writeClosePacket(final MMOConnection<T> con)
	{
		SendablePacket<T> sp;
		synchronized (con.getSendQueue())
		{
			if (con.getSendQueue().isEmpty())
				return;
			
			while ((sp = con.getSendQueue().removeFirst()) != null)
			{
				WRITE_BUFFER.clear();
				
				putPacketIntoWriteBuffer(con.getClient(), sp);
				
				WRITE_BUFFER.flip();
				
				try
				{
					con.write(WRITE_BUFFER);
				}
				catch (IOException e)
				{
					// error handling goes on the if bellow
					if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
						e.printStackTrace();
					
				}
			}
		}
	}
	
	protected final void writePacket(final SelectionKey key, final MMOConnection<T> con)
	{
		if (!prepareWriteBuffer(con))
		{
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
			return;
		}
		
		DIRECT_WRITE_BUFFER.flip();
		
		final int size = DIRECT_WRITE_BUFFER.remaining();
		
		int result = -1;
		
		try
		{
			result = con.write(DIRECT_WRITE_BUFFER);
		}
		catch (IOException e)
		{
			// error handling goes on the if bellow
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
		}
		
		// check if no error happened
		if (result >= 0)
		{
			// check if we written everything
			if (result == size)
			{
				// complete write
				synchronized (con.getSendQueue())
				{
					if (con.getSendQueue().isEmpty() && !con.hasPendingWriteBuffer())
					{
						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
					}
				}
			}
			else
				// incomplete write
				con.createWriteBuffer(DIRECT_WRITE_BUFFER);
		}
		else
		{
			con.getClient().onForcedDisconnection();
			closeConnectionImpl(key, con);
		}
	}
	
	private final boolean prepareWriteBuffer(final MMOConnection<T> con)
	{
		boolean hasPending = false;
		DIRECT_WRITE_BUFFER.clear();
		
		// if there is pending content add it
		if (con.hasPendingWriteBuffer())
		{
			con.movePendingWriteBufferTo(DIRECT_WRITE_BUFFER);
			hasPending = true;
		}
		
		if (DIRECT_WRITE_BUFFER.remaining() > 1 && !con.hasPendingWriteBuffer())
		{
			final NioNetStackList<SendablePacket<T>> sendQueue = con.getSendQueue();
			final T client = con.getClient();
			SendablePacket<T> sp;
			
			for (int i = 0; i < MAX_SEND_PER_PASS; i++)
			{
				synchronized (con.getSendQueue())
				{
					if (sendQueue.isEmpty())
						sp = null;
					else
						sp = sendQueue.removeFirst();
				}
				
				if (sp == null)
					break;
				
				hasPending = true;
				
				// put into WriteBuffer
				putPacketIntoWriteBuffer(client, sp);
				
				WRITE_BUFFER.flip();
				
				if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit())
					DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
				else
				{
					con.createWriteBuffer(WRITE_BUFFER);
					break;
				}
			}
		}
		return hasPending;
	}
	
	private final void putPacketIntoWriteBuffer(final T client, final SendablePacket<T> sp)
	{
		WRITE_BUFFER.clear();
		
		// reserve space for the size
		final int headerPos = WRITE_BUFFER.position();
		final int dataPos = headerPos + HEADER_SIZE;
		WRITE_BUFFER.position(dataPos);
		
		// set the write buffer
		sp._buf = WRITE_BUFFER;
		// write content to buffer
		sp.write();
		// delete the write buffer
		sp._buf = null;
		
		// size (inclusive header)
		int dataSize = WRITE_BUFFER.position() - dataPos;
		WRITE_BUFFER.position(dataPos);
		client.encrypt(WRITE_BUFFER, dataSize);
		
		// recalculate size after encryption
		dataSize = WRITE_BUFFER.position() - dataPos;
		
		WRITE_BUFFER.position(headerPos);
		// write header
		WRITE_BUFFER.putShort((short) (dataSize + HEADER_SIZE));
		WRITE_BUFFER.position(dataPos + dataSize);
	}
	
	final void closeConnection(final MMOConnection<T> con)
	{
		synchronized (_pendingClose)
		{
			_pendingClose.addLast(con);
		}
	}
	
	private final void closeConnectionImpl(final SelectionKey key, final MMOConnection<T> con)
	{
		try
		{
			// notify connection
			con.getClient().onDisconnection();
		}
		finally
		{
			try
			{
				// close socket and the SocketChannel
				con.close();
			}
			catch (IOException e)
			{
				// ignore, we are closing anyway
				if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
					e.printStackTrace();
				
			}
			finally
			{
				con.releaseBuffers();
				// clear attachment
				key.attach(null);
				// cancel key
				key.cancel();
			}
		}
	}
	
	public final void shutdown()
	{
		_shutdown = true;
	}
	
	protected void closeSelectorThread()
	{
		for (final SelectionKey key : _selector.keys())
		{
			try
			{
				key.channel().close();
			}
			catch (IOException e)
			{
				if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
					e.printStackTrace();
				
			}
		}
		
		try
		{
			_selector.close();
		}
		catch (IOException e)
		{
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
		}
	}
}
