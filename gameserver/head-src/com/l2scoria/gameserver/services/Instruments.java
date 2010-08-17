package com.l2scoria.gameserver.services;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javolution.util.FastList;

/**
 * @author ProGramMoS
 */
public class Instruments extends Properties
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String val = getProperty(key);

		if(val == null)
			return defaultValue;
		else
			return val;
	}

	public List<String> getStringList(String key, String defaultValue, String divisor)
	{
		List<String> _list = new FastList<String>();
		String string = getProperty(key, defaultValue);

		if(string.equals(""))
			return _list;
		else
		{
			String tokens[] = string.split(divisor);
			_list.addAll(Arrays.asList(tokens));
			return _list;
		}
	}

}
