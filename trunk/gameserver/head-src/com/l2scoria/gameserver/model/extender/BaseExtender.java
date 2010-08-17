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
package com.l2scoria.gameserver.model.extender;

import com.l2scoria.gameserver.model.L2Object;

/**
 * @author Azagthtot BaseExtender - Р±Р°Р·РѕРІС‹Р№ РєР»Р°СЃСЃ СЂР°СЃС€РёСЂРµРЅРёСЏ РІРѕР·РјРѕР¶РЅРѕСЃС‚РµР№<BR>
 * <BR>
 */
public class BaseExtender
{
	// Р‘Р°Р·РѕРІС‹Рµ С‚РёРїС‹ СЌРІРµРЅС‚РѕРІ. Р’ РєРѕРјРјРµРЅС‚Р°СЂРёСЏС… - РїР°СЂР°РјРµС‚СЂС‹
	public enum EventType
	{
		LOAD("load"), // null
		STORE("store"), // null
		CAST("cast"), // L2Skill , L2Character, L2Character[]
		ATTACK("attack"), // L2Character
		CRAFT("craft"),
		ENCHANT("enchant"),
		SPAWN("spawn"), // null
		DELETE("delete"), // null
		SETOWNER("setwoner"), // int, String
		DROP("drop"), // null		
		DIE("die"), // L2Character
		REVIVE("revive"), // null
		SETINTENTION("setintention"); // CtrlIntention
		public final String name;

		EventType(String name)
		{
			this.name = name;
		}
	}

	/**
	 * РњРѕР¶РµС‚ Р»Рё РґР°РЅРЅС‹Р№ СЌРєСЃС‚РµРЅРґРµСЂ Р±С‹С‚СЊ СЃРѕР·РґР°РЅ РґР»СЏ РѕР±СЉРµРєС‚Р°<br>
	 * 
	 * @param object as L2Object РѕР±СЉРµРєС‚, РґР»СЏ РєРѕС‚РѕСЂРѕРіРѕ РёРґРµС‚ Р·Р°РїСЂРѕСЃ<br>
	 * @return as boolean<br>
	 *         Р’РќРРњРђРќРР•! Р”Р°РЅРЅС‹Р№ РјРµС‚РѕРґ РІС‹Р·С‹РІР°РµС‚СЃСЏ РІ РєРѕРЅСЃС‚СЂСѓРєС‚РѕСЂРµ L2Object!!!<br>
	 *         Р’СЃРµ С‡С‚Рѕ РјС‹ РёРјРµРµРј - СЌС‚Рѕ РєРѕСЂСЂРµРєС‚РЅС‹Р№ ObjectId
	 */
	public static boolean canCreateFor(L2Object object)
	{
		return true;
	}

	protected L2Object _owner;
	private BaseExtender _next = null;

	/**
	 * РљРѕРЅСЃС‚СЂСѓРєС‚РѕСЂ РїРѕ СѓРјРѕР»С‡Р°РЅРёСЋ<br>
	 * <br>
	 * 
	 * @param owner - L2Object РґР»СЏ РєРѕС‚РѕСЂРѕРіРѕ СЃРѕР·РґР°РЅ СЌРєСЃС‚РµРЅРґРµСЂ
	 */
	public BaseExtender(L2Object owner)
	{
		_owner = owner;
	}

	/**
	 * РџРѕР»СѓС‡РёС‚СЊ РІР»Р°РґРµР»СЊС†Р° СЂР°СЃС€РёСЂРµРЅРёСЏ<br>
	 * <br>
	 * 
	 * @return as Object - РІР»Р°РґРµР»РµС† СЂР°СЃС€РёСЂРµРЅРёСЏ
	 */
	public L2Object getOwner()
	{
		return _owner;
	}

	/**
	 * onEvent - РњРµС‚РѕРґ, РІС‹Р·С‹РІР°РµРјС‹Р№ РїСЂРё РІРѕР·РЅРёРєРЅРѕРІРµРЅРёРё СЃРѕР±С‹С‚РёСЏ<br>
	 * Р’РЎР•Р“Р”Рђ РІС‹Р·С‹РІР°Р№С‚Рµ super.onEvent(event,params);<BR>
	 * <BR>
	 * 
	 * @param event as String - РёРґРµРЅС‚РёС„РёРєР°С‚РѕСЂ СЃРѕР±С‹С‚РёСЏ<br>
	 * @param params as Object[]- РїР°СЂР°РјРµС‚СЂС‹, РїРµСЂРµРґР°РІР°РµРјС‹Рµ РІ СЃРѕР±С‹С‚РёРµ<br>
	 * @return as Object - РµСЃР»Рё СЌРІРµРЅС‚ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ РІ РєР°С‡РµСЃС‚РІРµ С…РµРЅРґР»РµСЂР° С‚Рѕ РїСЂРё
	 *         РІРѕР·РІСЂР°С‚Рµ<br>
	 *         РЅРµ null СЃС‡РёС‚Р°РµС‚СЃСЏ РґРµР№СЃС‚РІРёРµ РїРµСЂРµС…РІР°С‡РµРЅРЅС‹Рј
	 */
	public Object onEvent(final String event, Object... params)
	{
		if(_next == null)
			return null;
		else
			return _next.onEvent(event, params);
	}

	/**
	 * РџРѕРёСЃРє СЌРєСЃС‚РµРЅРґРµСЂР° РїРѕ РєРѕСЂС‚РѕРєРѕРјСѓ РёРјРµРЅРё РєР»Р°СЃСЃР°<br>
	 * <br>
	 * 
	 * @param simpleClassName as String - РёРјСЏ РєР»Р°СЃСЃР°-СЂР°СЃС€РёСЂРµРЅРёСЏ<br>
	 * @return as BaseExtender - СЂР°СЃС€РёСЂРµРЅРёРµ РёР»Рё null
	 */
	public BaseExtender getExtender(final String simpleClassName)
	{
		if(this.getClass().getSimpleName().compareTo(simpleClassName) == 0)
			return this;
		else if(_next != null)
			return _next.getExtender(simpleClassName);
		else
			return null;
	}

	public void removeExtender(BaseExtender ext) {
		if(_next!=null)
			if(_next==ext)
				_next = _next._next;
			else
				_next.removeExtender(ext);
	}
	public BaseExtender getNextExtender() {
		return _next;
	}
	/**
	 * Р”РѕР±Р°РІРёС‚СЊ СЌРєСЃС‚РµРЅРґРµСЂ РІ С†РµРїРѕС‡РєСѓ<br>
	 * РќРµ РїСЂРµРґРЅР°Р·РЅР°С‡РµРЅ РґР»СЏ РїСЂСЏРјРѕРіРѕ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёСЏ, СЃРј. L2Object.addExtender()<br>
	 * <br>
	 * 
	 * @param newExtender as BaseExtender - РЅРѕРІС‹Р№ СЌРєСЃС‚РµРЅРґРµСЂ
	 */
	public void addExtender(BaseExtender newExtender)
	{
		if(_next == null)
		{
			_next = newExtender;
		}
		else
		{
			_next.addExtender(newExtender);
		}
	}
}
