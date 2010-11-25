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
package com.l2jfrozen.gameserver.util;

import java.util.logging.Logger;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameTimeController;

/**
 * Flood protector
 * 
 * @author programmos
 */
public class FloodProtector
{
	private static final Logger _log = Logger.getLogger(FloodProtector.class.getName());
	private static FloodProtector _instance;

	public static final FloodProtector getInstance()
	{
		if(_instance == null)
		{
			_instance = new FloodProtector();
		}

		return _instance;
	}

	// =========================================================
	// Data Field
	private static FastMap<Integer, Integer[]> _floodClient;
	// =========================================================
	// reuse delays for protected actions (in game ticks 1 tick = 100ms)
	private static final int[] REUSEDELAY = new int[]
	{
			//4, use item
			//42, rolldice
			//42, fireworks
			//16, item pet summon
			//100, hero voice
			//100, multisell
			//100, subclass
			//100, packets
			//100, buff
			//0, chat
			//100, action
			//80, party
			//50, drop
			//50, banking system
			//50, werehouse
			//50, craft

			Config.PROTECTED_BYPASS_C,
			42, //rolldice
			42, //fireworks
			Config.PROTECTED_HEROVOICE_C,
			Config.PROTECTED_MULTISELL_C,
			Config.PROTECTED_SUBCLASS_C,
			Config.PROTECTED_UNKNOWNPACKET_C,
			Config.GLOBAL_CHAT_DELAY,
			Config.PROTECTED_PARTY_ADD_MEMBER_C,
			Config.PROTECTED_DROP_C,
			Config.PROTECTED_ENCHANT_C,
			Config.PROTECTED_BANKING_SYSTEM_C,
			Config.PROTECTED_WEREHOUSE_C,
			Config.PROTECTED_CRAFT_C,
			////////////////////
			// packet protect //
			////////////////////
			Config.PROTECTED_ACTIVE_PACK_RETURN, // > return
			Config.PROTECTED_ACTIVE_PACK_FAILED, // < action failed
			Config.PROTECTED_USE_ITEM_C
	};

	// =============================================================
	public static final int PROTECTED_BYPASS = 0;
	public static final int PROTECTED_ROLLDICE = 1;
	public static final int PROTECTED_FIREWORK = 2;
	public static final int PROTECTED_HEROVOICE = 3;
	public static final int PROTECTED_MULTISELL = 4;
	public static final int PROTECTED_SUBCLASS = 5;
	public static final int PROTECTED_UNKNOWNPACKET = 6;
	public static final int PROTECTED_GLOBALCHAT = 7;
	public static final int PROTECTED_PARTY_ADD_MEMBER = 8;
	public static final int PROTECTED_DROP = 9;
	public static final int PROTECTED_ENCHANT = 10;
	public static final int PROTECTED_BANKING_SYSTEM = 11;
	public static final int PROTECTED_WEREHOUSE = 12;
	public static final int PROTECTED_CRAFT = 13;
	// =============================================================
	public static final int PROTECTED_ACTIVE_PACKETS = 14;
	public static final int PROTECTED_ACTIVE_PACKETS2 = 15;
	// =============================================================
	public static final int PROTECTED_USE_ITEM = 16;

	// =============================================================
	// Constructor
	private FloodProtector()
	{
		_log.info("Initializing Flood Protector");
		_floodClient = new FastMap<Integer, Integer[]>(Config.FLOODPROTECTOR_INITIALSIZE).setShared(true);
	}

	/**
	 * Add a new player to the flood protector (should be done for all players when they enter the world)
	 * 
	 * @param playerObjId
	 */
	public void registerNewPlayer(int playerObjId)
	{
		// create a new array
		Integer[] array = new Integer[REUSEDELAY.length];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = 0;
		}

		// register the player with an empty array
		_floodClient.put(playerObjId, array);
	}

	/**
	 * Remove a player from the flood protector (should be done if player loggs off)
	 * 
	 * @param playerObjId
	 */
	public void removePlayer(int playerObjId)
	{
		_floodClient.remove(playerObjId);
	}

	/**
	 * Return the size of the flood protector
	 * 
	 * @return size
	 */
	public int getSize()
	{
		return _floodClient.size();
	}

	/**
	 * Try to perform the requested action
	 * 
	 * @param playerObjId
	 * @param action
	 * @return true if the action may be performed
	 */
	public boolean tryPerformAction(int playerObjId, int action)
	{
		Entry<Integer, Integer[]> entry = _floodClient.getEntry(playerObjId);

		if(entry==null){
			this.registerNewPlayer(playerObjId);
			entry = _floodClient.getEntry(playerObjId);
		}
		
		if(entry != null) // in some case "entry" equal to "Null", but i dont know why and when...
		{
			Integer[] value = entry.getValue();

			if(value[action] < GameTimeController.getGameTicks())
			{
				value[action] = GameTimeController.getGameTicks() + REUSEDELAY[action];
				entry.setValue(value);

				_floodClient.put(entry.getKey(),entry.getValue());
				
				return true;
			}
		}
		return false;
	}
}
