/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfrozen.gameserver.powerpak.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;

/**
 * @author Shyla
 */
public class XMLRPCClient_ManagementTester
{
	
	public static void main(String[] args)
	{
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try
		{
			config.setServerURL(new URL("http://" + PowerPakConfig.XMLRPC_HOST + ":" + PowerPakConfig.XMLRPC_PORT + "/Management"));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			Object[] params =
			{
				60
			};
			
			client.execute("Management.restartServer", params);
			
		}
		catch (MalformedURLException localMalformedURLException)
		{
			localMalformedURLException.printStackTrace();
		}
		catch (XmlRpcException localXmlRpcException)
		{
			localXmlRpcException.printStackTrace();
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
		
	}
	
}
