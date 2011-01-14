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
package com.l2jfrozen.gameserver.geo.geoeditorcon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfrozen.Config;


public class GeoEditorListener extends Thread
{
	private static final Log _log = LogFactory.getLog(GeoEditorListener.class);
	
	private static final int PORT = Config.GEOEDITOR_PORT;
	
	private static final class SingletonHolder
	{
		private static final GeoEditorListener INSTANCE = new GeoEditorListener();
	}
	
	public static GeoEditorListener getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private ServerSocket _serverSocket;
	private GeoEditorThread _geoEditor;
	
	private GeoEditorListener()
	{
		try
		{
			_serverSocket = new ServerSocket(PORT);
		}
		catch(IOException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.fatal("Error creating geoeditor listener! ", e);
			System.exit(1);
		}
		start();
		_log.info("GeoEditorListener Initialized.");
	}

	public GeoEditorThread getThread()
	{
		return _geoEditor;
	}

	public String getStatus()
	{
		if(_geoEditor != null && _geoEditor.isWorking())
		{
			return "Geoeditor connected.";
		}
		return "Geoeditor not connected.";
	}

	@Override
	public void run()
	{
		Socket connection = null;
		try
		{
			while(true)
			{
				connection = _serverSocket.accept();
				if(_geoEditor != null && _geoEditor.isWorking())
				{
					_log.warn("Geoeditor already connected!");
					connection.close();
					continue;
				}
				_log.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
				_geoEditor = new GeoEditorThread(connection);
				_geoEditor.start();
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.info("GeoEditorListener: ", e);
			try
			{
				if(connection != null) connection.close();
			}
			catch(Exception e2)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e2.printStackTrace();
				
			}
		}
		finally
		{
			try
			{
				_serverSocket.close();
			}
			catch(IOException io)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					io.printStackTrace();
				
				_log.info("", io);
			}
			_log.warn("GeoEditorListener Closed!");
		}
	}
}