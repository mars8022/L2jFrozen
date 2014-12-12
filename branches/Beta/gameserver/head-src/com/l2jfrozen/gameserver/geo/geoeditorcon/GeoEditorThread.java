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
package com.l2jfrozen.gameserver.geo.geoeditorcon;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class GeoEditorThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(GeoEditorThread.class);
	
	private boolean _working = false;
	
	private int _mode = 0; // 0 - don't send coords, 1 - send each
	
	// validateposition from client, 2 - send in
	// intervals of _sendDelay ms.
	private int _sendDelay = 1000; // default - once in second
	
	private final Socket _geSocket;
	
	private OutputStream _out;
	
	private final FastList<L2PcInstance> _gms;
	
	public GeoEditorThread(final Socket ge)
	{
		_geSocket = ge;
		_working = true;
		_gms = new FastList<>();
	}
	
	@Override
	public void interrupt()
	{
		try
		{
			_geSocket.close();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
		super.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			_out = _geSocket.getOutputStream();
			int timer = 0;
			
			while (_working)
			{
				if (!isConnected())
				{
					_working = false;
				}
				
				if (_mode == 2 && timer > _sendDelay)
				{
					for (final L2PcInstance gm : _gms)
					{
						if (gm.isOnline() == 1)
						{
							sendGmPosition(gm);
						}
						else
						{
							_gms.remove(gm);
						}
					}
					timer = 0;
				}
				
				try
				{
					sleep(100);
					if (_mode == 2)
					{
						timer += 100;
					}
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
				}
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("GeoEditor disconnected. ", e);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				_geSocket.close();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			_working = false;
		}
	}
	
	public void sendGmPosition(final int gx, final int gy, final short z)
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (_out)
			{
				writeC(0x0b); // length 11 bytes!
				writeC(0x01); // Cmd = save cell;
				writeD(gx); // Global coord X;
				writeD(gy); // Global coord Y;
				writeH(z); // Coord Z;
				_out.flush();
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("GeoEditor disconnected. ", e);
			_working = false;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.error(e.getMessage(), e);
			try
			{
				_geSocket.close();
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			_working = false;
		}
	}
	
	public void sendGmPosition(final L2PcInstance _gm)
	{
		sendGmPosition(_gm.getX(), _gm.getY(), (short) _gm.getZ());
	}
	
	public void sendPing()
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (_out)
			{
				writeC(0x01); // length 1 byte!
				writeC(0x02); // Cmd = ping (dummy packet for connection test);
				_out.flush();
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("GeoEditor disconnected. ", e);
			_working = false;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.error(e.getMessage(), e);
			try
			{
				_geSocket.close();
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					ex.printStackTrace();
				
			}
			_working = false;
		}
	}
	
	private void writeD(final int value) throws IOException
	{
		_out.write(value & 0xff);
		_out.write(value >> 8 & 0xff);
		_out.write(value >> 16 & 0xff);
		_out.write(value >> 24 & 0xff);
	}
	
	private void writeH(final int value) throws IOException
	{
		_out.write(value & 0xff);
		_out.write(value >> 8 & 0xff);
	}
	
	private void writeC(final int value) throws IOException
	{
		_out.write(value & 0xff);
	}
	
	public void setMode(final int value)
	{
		_mode = value;
	}
	
	public void setTimer(final int value)
	{
		if (value < 500)
		{
			_sendDelay = 500; // maximum - 2 times per second!
		}
		else if (value > 60000)
		{
			_sendDelay = 60000; // Minimum - 1 time per minute.
		}
		else
		{
			_sendDelay = value;
		}
	}
	
	public void addGM(final L2PcInstance gm)
	{
		if (!_gms.contains(gm))
		{
			_gms.add(gm);
		}
	}
	
	public void removeGM(final L2PcInstance gm)
	{
		if (_gms.contains(gm))
		{
			_gms.remove(gm);
		}
	}
	
	public boolean isSend(final L2PcInstance gm)
	{
		return _mode == 1 && _gms.contains(gm);
	}
	
	private boolean isConnected()
	{
		return _geSocket.isConnected() && !_geSocket.isClosed();
	}
	
	public boolean isWorking()
	{
		sendPing();
		return _working;
	}
	
	public int getMode()
	{
		return _mode;
	}
}