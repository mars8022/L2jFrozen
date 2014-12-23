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
package com.l2jfrozen.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.GameGuardQuery;

/**
 * The main "engine" of protection ...
 * @author Nick
 */
public class nProtect
{
	private static Logger LOGGER = Logger.getLogger(nProtect.class);
	
	public static enum RestrictionType
	{
		RESTRICT_ENTER,
		RESTRICT_EVENT,
		RESTRICT_OLYMPIAD,
		RESTRICT_SIEGE
	}
	
	public class nProtectAccessor
	{
		public nProtectAccessor()
		{
		}
		
		public void setCheckGameGuardQuery(final Method m)
		{
			nProtect.this._checkGameGuardQuery = m;
		}
		
		public void setStartTask(final Method m)
		{
			nProtect.this._startTask = m;
		}
		
		public void setCheckRestriction(final Method m)
		{
			nProtect.this._checkRestriction = m;
		}
		
		public void setSendRequest(final Method m)
		{
			nProtect.this._sendRequest = m;
		}
		
		public void setCloseSession(final Method m)
		{
			nProtect.this._closeSession = m;
		}
		
		public void setSendGGQuery(final Method m)
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
		if (_instance == null)
			_instance = new nProtect();
		return _instance;
	}
	
	private nProtect()
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName("com.l2jfrozen.protection.main");
			
			if (clazz != null)
			{
				final Method m = clazz.getMethod("init", nProtectAccessor.class);
				if (m != null)
				{
					m.invoke(null, new nProtectAccessor());
					enabled = true;
				}
			}
		}
		catch (final ClassNotFoundException e)
		{
			if (Config.DEBUG)
				LOGGER.warn("nProtect System will be not loaded due to ClassNotFoundException of 'com.l2jfrozen.protection.main' class");
		}
		catch (SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void sendGameGuardQuery(final GameGuardQuery pkt)
	{
		try
		{
			if (_sendGGQuery != null)
				_sendGGQuery.invoke(pkt);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkGameGuardRepy(final L2GameClient cl, final int[] reply)
	{
		try
		{
			if (_checkGameGuardQuery != null)
				return (Boolean) _checkGameGuardQuery.invoke(null, cl, reply);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public ScheduledFuture<?> startTask(final L2GameClient client)
	{
		try
		{
			if (_startTask != null)
				return (ScheduledFuture<?>) _startTask.invoke(null, client);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void sendRequest(final L2GameClient cl)
	{
		if (_sendRequest != null)
			try
			{
				_sendRequest.invoke(null, cl);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
	}
	
	public void closeSession(final L2GameClient cl)
	{
		if (_closeSession != null)
			try
			{
				_closeSession.invoke(null, cl);
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
	}
	
	public boolean checkRestriction(final L2PcInstance player, final RestrictionType type, final Object... params)
	{
		try
		{
			if (_checkRestriction != null)
				return (Boolean) _checkRestriction.invoke(null, player, type, params);
		}
		catch (final Exception e)
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
