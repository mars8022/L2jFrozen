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
package com.l2jfrozen.gameserver.handler.custom;

import java.util.Map;

import javolution.util.FastMap;

import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.L2Rebirth;

/**
 * This 'Bypass Handler' is a handy tool indeed!<br>
 * Basically, you can send any custom bypass commmands to it from ANY npc and it will call the appropriate function.<br>
 * <strong>Example:</strong><br>
 * <button value=" Request Rebirth " action="bypass -h custom_rebirth_confirmrequest" width=110 height=36
 * back="L2UI_ct1.button_df" fore="L2UI_ct1.button_df">
 * 
 * @author JStar
 */
public class CustomBypassHandler
{
	private static CustomBypassHandler _instance = null;
	private Map<String, ICustomByPassHandler> _handlers;

	private CustomBypassHandler()
	{
		_handlers = new FastMap<String, ICustomByPassHandler>();

		// By Azagthtot РћР±СЂР°Р±РѕС‚РєР° multi-extract Р°Р№С‚РµРјРѕРІ.
		registerCustomBypassHandler(new ExtractableByPassHandler());
		//Do Nothing ^_-
	}

	/** Receives the non-static instance of the RebirthManager. */
	public static CustomBypassHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new CustomBypassHandler();
		}

		return _instance;
	}

	/**
	 * Azagthtot Р РµРіРёСЃС‚СЂР°С†РёСЏ РѕР±СЂР°Р±РѕС‚С‡РёРєР° custom_xxx
	 * 
	 * @param handler as ICustomByPassHandler- РѕР±СЂР°Р±РѕС‚С‡РёРє xxx bypass РєРѕРјР°РЅРґС‹.
	 */
	public void registerCustomBypassHandler(ICustomByPassHandler handler)
	{
		for(String s : handler.getByPassCommands())
		{
			_handlers.put(s, handler);
		}
	}

	/** Handles player's Bypass request to the Custom Content. */
	public void handleBypass(L2PcInstance player, String command)
	{
		//Rebirth Manager and Engine Caller

		String cmd = "";
		String params = "";
		int iPos = command.indexOf(" ");
		if(iPos != -1)
		{
			cmd = command.substring(7, iPos);
			params = command.substring(iPos + 1);
		}
		else
		{
			cmd = command.substring(7);
		}
		ICustomByPassHandler ch = _handlers.get(cmd);
		if(ch != null)
		{
			ch.handleCommand(cmd, player, params);
		}
		else
		{
			if(command.startsWith("custom_rebirth"))
			{
				L2Rebirth.getInstance().handleCommand(player, command);
			}
		}
	}
}
