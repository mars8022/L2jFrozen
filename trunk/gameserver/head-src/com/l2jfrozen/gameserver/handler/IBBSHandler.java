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
package com.l2jfrozen.gameserver.handler;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * РљР°СЃС‚РѕРјРЅС‹Р№ РѕР±СЂР°Р±РѕС‚С‡РёРє Р±РѕСЂРґС‹<br>
 * 
 * @author Azagthtot
 */
public interface IBBSHandler
{
	/**
	 * @return as String [] - РЎРїРёСЃРѕРє РѕР±СЂР°Р±Р°С‚С‹РІР°РµРјС‹С… РєРѕРјРјР°РЅРґ (Р±РµР· РїСЂРµС„РёРєСЃР° _bbs)
	 */
	public String[] getBBSCommands();

	/**
	 * РћР±СЂР°Р±РѕС‚РєР° РєРѕРјРјР°РЅРґС‹<br>
	 * <br>
	 * 
	 * @param command as String - РєРѕРјРјР°РЅРґР° Р±РµР· РїСЂРµС„РёРєСЃР° _bbs<br>
	 * @param activeChar as L2PcInstance - С‚РѕС‚ СѓСЂРѕРґ, С‡С‚Рѕ РЅР°Р¶Р°Р» Alt+B<br>
	 * @param params as String - РїР°СЂР°РјРµС‚СЂС‹, РѕРїСЂРµРґРµР»РµРЅРЅС‹Рµ РїРѕСЃР»Рµ РїСЂРѕР±РµР»Р°
	 */
	public void handleCommand(String command, L2PcInstance activeChar, String params);
}
