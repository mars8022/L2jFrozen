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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.l2jfrozen.gameserver.network.L2GameClient;

/**
 * @author KenM
 * @param <T> 
 */
public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable
{
	NioNetStringBuffer _sbuf;
	
	protected ReceivablePacket()
	{
		
	}
	
	protected abstract boolean read();
	
	@Override
	public abstract void run();
	
	protected final void readB(final byte[] dst)
	{
		try{
			
			_buf.get(dst);
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}
		
	}
	
	protected final void readB(final byte[] dst, final int offset, final int len)
	{
		try{
			
			_buf.get(dst, offset, len);
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		
	}
	
	protected final int readC()
	{
		try{
			
			return _buf.get() & 0xFF;
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return -1;
		
	}
	
	protected final int readH()
	{
		
		try{
			
			return _buf.getShort() & 0xFFFF;
			
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return -1;
	}
	
	protected final int readD()
	{
		
		try{
			
			return _buf.getInt();
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return -1;
	}
	
	protected final long readQ()
	{
		
		try{
			
			return _buf.getLong();
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return -1;
	}
	
	protected final double readF()
	{
		try{
			
			return _buf.getDouble();
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return -1;
	}
	
	protected final String readS()
	{
		_sbuf.clear();
		
		try{
			
			char ch;
			while ((ch = _buf.getChar()) != 0)
			{
				_sbuf.append(ch);
			}
			
		}catch(BufferUnderflowException e){
			
			if(Config.getInstance().ENABLE_MMOCORE_EXCEPTIONS)
				e.printStackTrace();
			
			if(getClient() instanceof L2GameClient){
				((L2GameClient)getClient()).onBufferUnderflow();
			}
			
		}

		return _sbuf.toString();
	}
	
	/**
	 * packet forge purpose
	 * @param data
	 * @param client
	 * @param sBuffer
	 */
	public void setBuffers(ByteBuffer data, T client, NioNetStringBuffer sBuffer)
	{
		_buf = data;
		_client = client;
		_sbuf = sBuffer;
	}
}
