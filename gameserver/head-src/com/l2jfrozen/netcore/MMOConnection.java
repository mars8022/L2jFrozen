/* L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.netcore;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T>
 * @author KenM
 */
public class MMOConnection<T extends MMOClient<?>>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MMOConnection.class);
	private final SelectorThread<T> _selectorThread;
	
	private final Socket _socket;
	
	private final InputStream _socket_is;
	
	private final InetAddress _address;
	
	private final ReadableByteChannel _readableByteChannel;
	
	private final WritableByteChannel _writableByteChannel;
	
	private final int _port;
	
	private final NioNetStackList<SendablePacket<T>> _sendQueue;
	
	private final SelectionKey _selectionKey;
	
	// private SendablePacket<T> _closePacket;
	
	private ByteBuffer _readBuffer;
	
	private ByteBuffer _primaryWriteBuffer;
	
	private ByteBuffer _secondaryWriteBuffer;
	
	private volatile boolean _pendingClose;
	
	private T _client;
	
	public MMOConnection(final SelectorThread<T> selectorThread, final Socket socket, final SelectionKey key) throws IOException
	{
		_selectorThread = selectorThread;
		_socket = socket;
		_address = socket.getInetAddress();
		_readableByteChannel = socket.getChannel();
		_writableByteChannel = socket.getChannel();
		
		_socket_is = socket.getInputStream();
		
		_port = socket.getPort();
		_selectionKey = key;
		
		_sendQueue = new NioNetStackList<>();
	}
	
	final void setClient(final T client)
	{
		_client = client;
	}
	
	public final T getClient()
	{
		return _client;
	}
	
	public final void sendPacket(final SendablePacket<T> sp)
	{
		sp._client = _client;
		
		if (_pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			_sendQueue.addLast(sp);
		}
		
		if (!_sendQueue.isEmpty() && _selectionKey.isValid())
		{
			try
			{
				_selectionKey.interestOps(_selectionKey.interestOps() | SelectionKey.OP_WRITE);
			}
			catch (final CancelledKeyException e)
			{
				LOGGER.warn("", e);
				
			}
		}
	}
	
	final SelectionKey getSelectionKey()
	{
		return _selectionKey;
	}
	
	public final InetAddress getInetAddress()
	{
		return _address;
	}
	
	public final int getPort()
	{
		return _port;
	}
	
	final void close() throws IOException
	{
		_socket.close();
	}
	
	final int read(final ByteBuffer buf) throws IOException
	{
		if (/*
			 * !isClosed() &&
			 */_readableByteChannel != null && _readableByteChannel.isOpen() && !_socket.isInputShutdown())
		// && !_socket.isOutputShutdown())
		{
			return _readableByteChannel.read(buf);
		}
		return -1;
	}
	
	final int write(final ByteBuffer buf) throws IOException
	{
		if (_writableByteChannel != null && _writableByteChannel.isOpen() && !_socket.isOutputShutdown())
		{
			return _writableByteChannel.write(buf);
		}
		return -1;
	}
	
	final void createWriteBuffer(final ByteBuffer buf)
	{
		if (_primaryWriteBuffer == null)
		{
			_primaryWriteBuffer = _selectorThread.getPooledBuffer();
			_primaryWriteBuffer.put(buf);
		}
		else
		{
			final ByteBuffer temp = _selectorThread.getPooledBuffer();
			temp.put(buf);
			
			final int remaining = temp.remaining();
			_primaryWriteBuffer.flip();
			final int limit = _primaryWriteBuffer.limit();
			
			if (remaining >= _primaryWriteBuffer.remaining())
			{
				temp.put(_primaryWriteBuffer);
				_selectorThread.recycleBuffer(_primaryWriteBuffer);
				_primaryWriteBuffer = temp;
			}
			else
			{
				_primaryWriteBuffer.limit(remaining);
				temp.put(_primaryWriteBuffer);
				_primaryWriteBuffer.limit(limit);
				_primaryWriteBuffer.compact();
				_secondaryWriteBuffer = _primaryWriteBuffer;
				_primaryWriteBuffer = temp;
			}
		}
	}
	
	final boolean hasPendingWriteBuffer()
	{
		return _primaryWriteBuffer != null;
	}
	
	final void movePendingWriteBufferTo(final ByteBuffer dest)
	{
		_primaryWriteBuffer.flip();
		dest.put(_primaryWriteBuffer);
		_selectorThread.recycleBuffer(_primaryWriteBuffer);
		_primaryWriteBuffer = _secondaryWriteBuffer;
		_secondaryWriteBuffer = null;
	}
	
	final void setReadBuffer(final ByteBuffer buf)
	{
		_readBuffer = buf;
	}
	
	final ByteBuffer getReadBuffer()
	{
		return _readBuffer;
	}
	
	public final boolean isConnected()
	{
		return !_socket.isClosed() && _socket.isConnected();
	}
	
	public final boolean isChannelConnected()
	{
		boolean output = false;
		
		if (!_socket.isClosed() && _socket.getChannel() != null && _socket.getChannel().isConnected() && _socket.getChannel().isOpen() && !_socket.isInputShutdown())
		{
			
			try
			{
				if (_socket_is.available() > 0)
				{
					output = true;
				}
			}
			catch (final IOException e)
			{
				LOGGER.error("unhandled exception", e);
			}
			
		}
		return output;
	}
	
	public final boolean isClosed()
	{
		return _pendingClose;
	}
	
	final NioNetStackList<SendablePacket<T>> getSendQueue()
	{
		return _sendQueue;
	}
	
	/*
	 * final SendablePacket<T> getClosePacket() { return _closePacket; }
	 */
	
	@SuppressWarnings("unchecked")
	public final void close(final SendablePacket<T> sp)
	{
		close(new SendablePacket[]
		{
			sp
		});
	}
	
	public final void close(final SendablePacket<T>[] closeList)
	{
		if (_pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			if (!_pendingClose)
			{
				_pendingClose = true;
				_sendQueue.clear();
				for (final SendablePacket<T> sp : closeList)
				{
					_sendQueue.addLast(sp);
				}
			}
		}
		
		if (_selectionKey.isValid())
		{
			
			try
			{
				_selectionKey.interestOps(_selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
			}
			catch (final CancelledKeyException e)
			{
				// not useful LOGGER
			}
			
		}
		
		if (NetcoreConfig.getInstance().DUMP_CLOSE_CONNECTIONS)
		{
			Thread.dumpStack();
		}
		// _closePacket = sp;
		_selectorThread.closeConnection(this);
	}
	
	final void releaseBuffers()
	{
		if (_primaryWriteBuffer != null)
		{
			_selectorThread.recycleBuffer(_primaryWriteBuffer);
			_primaryWriteBuffer = null;
			
			if (_secondaryWriteBuffer != null)
			{
				_selectorThread.recycleBuffer(_secondaryWriteBuffer);
				_secondaryWriteBuffer = null;
			}
		}
		
		if (_readBuffer != null)
		{
			_selectorThread.recycleBuffer(_readBuffer);
			_readBuffer = null;
		}
	}
}
