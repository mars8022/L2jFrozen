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
package com.l2jfrozen.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.GameGuardQuery;

/**
 * The main "engine" of protection ...
 * 
 * @author Nick
 */
public class nProtect
{
	private static Logger _log = Logger.getLogger("nProtect");
	
	public static enum RestrictionType
	{
		RESTRICT_ENTER,RESTRICT_EVENT,RESTRICT_OLYMPIAD,RESTRICT_SIEGE
	}
	public class nProtectAccessor
	{
		public nProtectAccessor() {}
		public void setCheckGameGuardQuery(Method m)
		{
			nProtect.this._checkGameGuardQuery = m;
		}

		public void setStartTask(Method m)
		{
			nProtect.this._startTask = m;
		}

		public void setCheckRestriction(Method m)
		{
			nProtect.this._checkRestriction = m;
		}

		public void setSendRequest(Method m)
		{
			nProtect.this._sendRequest = m;
		}

		public void setCloseSession(Method m)
		{
			nProtect.this._closeSession = m;
		}

		public void setSendGGQuery(Method m)
		{
			nProtect.this._sendGGQuery = m;
		}

	}
	protected Method _checkGameGuardQuery = null;
	protected Method _startTask = null;
	protected Method _checkRestriction = null;
	protected Method _sendRequest = null;
	protected Method _closeSession = null;
	protected Method _sendGGQuery = null;
	private static nProtect _instance = null;
	
	private static boolean enabled = false;
	
	public static nProtect getInstance()
	{
		if(_instance == null)
			_instance = new nProtect();
		return _instance;
	}

	private nProtect()
	{
		Class<?> clazz=null;
		try
		{
			clazz = Class.forName("com.l2jfrozen.protection.main");
			
			if(clazz!=null)
			{
				Method m = clazz.getMethod("init", nProtectAccessor.class);
				if(m!=null){
					m.invoke(null, new nProtectAccessor());
					enabled = true;
				}
			}
		} 
		catch(ClassNotFoundException e)
		{
			if(Config.DEBUG)
				_log.warning("nProtect System will be not loaded due to ClassNotFoundException of 'com.l2jfrozen.protection.main' class" );
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
		

	}
	
	public void sendGameGuardQuery(GameGuardQuery pkt)
	{
		try
		{
			if(_sendGGQuery!=null)
				_sendGGQuery.invoke(pkt);
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public boolean checkGameGuardRepy(L2GameClient cl, int [] reply)
	{
		try
		{
			if(_checkGameGuardQuery!=null)
				return (Boolean)_checkGameGuardQuery.invoke(null, cl,reply);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	public ScheduledFuture<?> startTask(L2GameClient client)
	{
		try
		{
			if(_startTask != null)
				return (ScheduledFuture<?>)_startTask.invoke(null, client);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void sendRequest(L2GameClient cl)
	{
		if(_sendRequest!=null)
			try
			{
				_sendRequest.invoke(null, cl);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public void closeSession(L2GameClient cl)
	{
			if(_closeSession!=null)
				try
				{
					_closeSession.invoke(null, cl);
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
				}
	}

	public boolean checkRestriction(L2PcInstance player, RestrictionType type, Object... params)
	{
		try
		{
			if(_checkRestriction!=null)
				return (Boolean)_checkRestriction.invoke(null,player,type,params);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @return the enabled
	 */
	public static boolean isEnabled()
	{
		return enabled;
	}
	
	
}
