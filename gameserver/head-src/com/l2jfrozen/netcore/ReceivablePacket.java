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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T>
 * @author KenM
 */
public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable
{
	NioNetStringBuffer _sbuf;
	protected static final Logger LOGGER = LoggerFactory.getLogger(ReceivablePacket.class);
	
	protected ReceivablePacket()
	{
		
	}
	
	protected abstract boolean read();
	
	@Override
	public abstract void run();
	
	protected final void readB(final byte[] dst)
	{
		try
		{
			
			_buf.get(dst);
			
		}
		catch (final BufferUnderflowException e)
		{
			
			LOGGER.warn("", e);
			
		}
		
	}
	
	protected final void readB(final byte[] dst, final int offset, final int len)
	{
		try
		{
			
			_buf.get(dst, offset, len);
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
	}
	
	protected final int readC()
	{
		try
		{
			
			return _buf.get() & 0xFF;
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
		
	}
	
	protected final int readH()
	{
		
		try
		{
			
			return _buf.getShort() & 0xFFFF;
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final int readD()
	{
		
		try
		{
			
			return _buf.getInt();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final long readQ()
	{
		
		try
		{
			
			return _buf.getLong();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final double readF()
	{
		try
		{
			
			return _buf.getDouble();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final String readS()
	{
		_sbuf.clear();
		
		try
		{
			
			char ch;
			while ((ch = _buf.getChar()) != 0)
			{
				_sbuf.append(ch);
			}
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return _sbuf.toString();
	}
	
	/**
	 * packet forge purpose
	 * @param data
	 * @param client
	 * @param sBuffer
	 */
	public void setBuffers(final ByteBuffer data, final T client, final NioNetStringBuffer sBuffer)
	{
		_buf = data;
		_client = client;
		_sbuf = sBuffer;
	}
}
