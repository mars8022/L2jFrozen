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
package com.l2jfrozen.gameserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Р›РѕР°РґРµСЂ РєР»Р°СЃСЃРѕРІ.<br>
 * Р’РєР»СЋС‡РµРЅ С…РµС€ РєР»Р°СЃСЃРѕРІ Рё РјРµС‚РѕРґРѕРІ.
 * 
 * @version 1.2
 * @author programmos
 * @author Azagthtot
 */
public class Loader extends ClassLoader
{
	private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

	private static Loader _instance;

	public static Loader getInstance()
	{
		if(_instance == null)
		{
			_instance = new Loader("config/start.bin");
		}
		return _instance;
	}

	public Loader(String nameZip)
	{
		try
		{
			ZipFile file = new ZipFile(nameZip);

			if(file == null)
				throw new IOException();

			byte[] data = null;
			Enumeration<?> list = file.entries();

			String name = "";

			while(list.hasMoreElements())
			{
				ZipEntry element = (ZipEntry) list.nextElement();

				//С‚.Рє. Сѓ РЅР°СЃ РёРјСЏ РґРёСЂРµРєС‚РѕСЂРёРё СЃРѕРѕС‚РІРµС‚СЃС‚РІСѓРµС‚ РјР°РёРЅ РєР»Р°СЃСЃСѓ, С‚Рѕ
				//Р·Р°РїРёСЃС‹РІР°РµРј РµРµ РЅР°Р·РІР°РЅРёРµ РґР»СЏ РґР°Р»СЊРЅРµР№С€РµРіРѕ СЃСЂР°РІРЅРµРЅРёСЏ
				if(element.isDirectory())
				{
					name = element.getName().replace("/", "");
				}
				else if(element.getName().startsWith(name))
				{
					byte[] dataAnon = getBytes(file, element);
					// Р¤РѕСЂРјРёСЂСѓРµРј РёРјСЏ РєР»Р°СЃСЃР° РёР· РєР°С‚Р°Р»РѕРі/С„Р°Р№Р».class
					String ClassName = element.getName().substring(name.length() + 1).replace(".class", "");
					// Р•СЃР»Рё РєР»Р°СЃСЃ == РєР°С‚Р°Р»РѕРі - Р·Р°РїРѕРјРЅР°РµРј, РѕСЃРЅРѕРІРЅРѕР№ РєР»Р°СЃСЃ Р’РЎР•Р“Р”Рђ РїРѕСЃР»РµРґРЅРёР№  
					if(ClassName.compareTo(name) == 0)
					{
						data = dataAnon;
					}
					else
					{
						@SuppressWarnings("unused")
						Class<?> clazz = defineClass(ClassName, dataAnon, 0, dataAnon.length);
						System.out.println("Loading " + ClassName);
					}

					dataAnon = null;
				}

				element = null;
				if(data != null)
				{ // Р“СЂСѓР·РёРј РІ РєР°СЂС‚Сѓ РћРЎРќРћР’РќРћР™ РєР»Р°СЃСЃ
					classes.put(name, defineClass(name, data, 0, data.length));
					System.out.println("Loading " + name);
				}
				data = null;
			}

			name = null;
			list = null;
			file = null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * РСЃРїРѕР»РЅРµРЅРёРµ РјРµС‚РѕРґР°.
	 * 
	 * @param className РёРјСЏ РєР»Р°СЃСЃР° (РІРјРµСЃС‚Рµ СЃ РїР°РєР°РіРѕРј, С‚РёРї String)
	 * @param methodName РёРјСЏ РјРµС‚РѕРґР° (С‚РёРї String)
	 * @param params РїРµСЂРµРґР°С‡Р° РІ РєР»Р°СЃСЃ (С‚РёРї Object)
	 * @throws Exception not found X
	 */
	// Р’СЃРµ С‚Р°РєРё РёСЃРїРѕР»РЅРµРЅРёРµ, Р° РЅРµ Р·Р°РіСЂСѓР·РєР°... invoke Р±РѕР»РµРµ СЂР°Р·СѓРјРЅРѕ...
	public void invokeMethod(String className, String methodName, Object... params) throws Exception
	{
		Class<?> clazz = classes.get(className);

		if(clazz == null)
			throw new Exception("Not found class.");

		Method method;
		//РѕР±СЂР°Р±Р°С‚С‹РІР°РµРј Р°СЂРіСѓРјРµРЅС‚С‹
		Class<?>[] paramClass = new Class<?>[params.length];
		int i = 0;
		for(Object param : params)
		{
			paramClass[i++] = param.getClass();
		}
		method = clazz.getMethod(methodName, paramClass);

		paramClass = null;

		if(method == null) //РјРµС‚РѕРґ РЅРµ РЅР°Р№РґРµРЅ
			throw new Exception("Not found custom method.");

		method.invoke(null, params); //Р·Р°РїСѓСЃРєР°РµРј РјРµС‚РѕРґ

		method = null;
	}

	/**
	 * РџРѕР»СѓС‡РµРЅРёРµ Р±Р°Р№С‚РѕРІ РёР· С„Р°Р№Р»Р°.
	 * 
	 * @param file zip-file
	 * @param element file in zip
	 * @return Р±Р°Р№С‚С‹ (С‚РёРї byte[])
	 * @throws IOException files
	 */
	private byte[] getBytes(ZipFile file, ZipEntry element) throws IOException
	{
		InputStream is;
		//РїСЂРѕРІРµСЂРєР° СЂР°Р·РјРµСЂР° С„Р°Р№Р»Р°
		if(element.getSize() >= Integer.MAX_VALUE)
			throw new IOException();

		byte[] data = new byte[(int) element.getSize()];

		is = file.getInputStream(element);

		is.read(data);

		is.close();
		is = null;

		return data;
	}
}
