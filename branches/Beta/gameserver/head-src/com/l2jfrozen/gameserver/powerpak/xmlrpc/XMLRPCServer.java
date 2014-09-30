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
package com.l2jfrozen.gameserver.powerpak.xmlrpc;

import java.net.InetAddress;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.l2jfrozen.Config;
import com.l2jfrozen.L2Properties;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;

/**
 * @author L2JFrozen
 */
public class XMLRPCServer
{
	private static XMLRPCServer _instance = null;
	private WebServer _server;

	public static XMLRPCServer getInstance()
	{
		if(_instance == null)
		{
			_instance = new XMLRPCServer();
		}
		return _instance;
	}

	private XMLRPCServer()
	{
		System.out.println("XMLRPCServer:");
		try
		{
			_server = new WebServer(PowerPakConfig.XMLRPC_PORT, InetAddress.getByName(PowerPakConfig.XMLRPC_HOST));
			XmlRpcServer xmlServer = _server.getXmlRpcServer();
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			xmlServer.setHandlerMapping(phm);
			int numServices = 0;
			try
			{
				L2Properties p = new L2Properties("config/powerpak/xmlrpc.service");
				for(Object s : p.keySet())
				{
					String service = p.getProperty(s.toString());
					Class<?> clazz = Class.forName(service);
					if(clazz != null)
					{
						numServices++;
						phm.addHandler(s.toString(), clazz);
					}

				}
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}

			if(numServices > 0)
			{
				XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlServer.getConfig();
				serverConfig.setEnabledForExtensions(true);
				serverConfig.setContentLengthOptional(false);
				_server.start();
				System.out.println("...Listen on " + PowerPakConfig.XMLRPC_HOST + ":" + PowerPakConfig.XMLRPC_PORT + ", " + numServices + " service(s) avaliable");
			}
			else
			{
				System.out.println("...No services defined");
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			System.out.println("...Error while starting " + e);
		}
	}

	public void sthutdown()
	{
		_server.shutdown();
		System.out.println("XMLRPCServer: Stopped");
	}

}
