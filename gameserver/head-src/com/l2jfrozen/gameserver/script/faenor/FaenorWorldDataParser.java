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
package com.l2jfrozen.gameserver.script.faenor;

import java.util.Map;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import javolution.util.FastMap;

import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.IntList;
import com.l2jfrozen.gameserver.script.Parser;
import com.l2jfrozen.gameserver.script.ParserFactory;
import com.l2jfrozen.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorWorldDataParser extends FaenorParser
{
	static Logger _log = Logger.getLogger(FaenorWorldDataParser.class.getName());
	//Script Types
	private final static String PET_DATA = "PetData";

	@Override
	public void parseScript(Node eventNode, ScriptContext context)
	{
		if(Config.DEBUG)
		{
			_log.info("Parsing WorldData");
		}

		for(Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{

			if(isNodeName(node, PET_DATA))
			{
				parsePetData(node, context);
			}
		}
	}

	public class PetData
	{
		public int petId;
		public int levelStart;
		public int levelEnd;
		Map<String, String> statValues;

		public PetData()
		{
			statValues = new FastMap<String, String>();
		}
	}

	private void parsePetData(Node petNode, ScriptContext context)
	{
		//if (Config.DEBUG) _log.info("Parsing PetData.");

		PetData petData = new PetData();

		try
		{
			petData.petId = getInt(attribute(petNode, "ID"));
			int[] levelRange = IntList.parse(attribute(petNode, "Levels"));
			petData.levelStart = levelRange[0];
			petData.levelEnd = levelRange[1];

			for(Node node = petNode.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if(isNodeName(node, "Stat"))
				{
					parseStat(node, petData);
				}
			}
			_bridge.addPetData(context, petData.petId, petData.levelStart, petData.levelEnd, petData.statValues);
		}
		catch(Exception e)
		{
			petData.petId = -1;
			_log.warning("Error in pet Data parser.");
			e.printStackTrace();
		}
	}

	private void parseStat(Node stat, PetData petData)
	{
		//if (Config.DEBUG) _log.info("Parsing Pet Statistic.");

		try
		{
			String statName = attribute(stat, "Name");

			for(Node node = stat.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if(isNodeName(node, "Formula"))
				{
					String formula = parseForumla(node);
					petData.statValues.put(statName, formula);
				}
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			petData.petId = -1;
			_log.warning("ERROR(parseStat):" + e.getMessage());
		}
	}

	private String parseForumla(Node formulaNode)
	{
		return formulaNode.getTextContent().trim();
	}

	static class FaenorWorldDataParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return new FaenorWorldDataParser();
		}
	}

	static
	{
		ScriptEngine.parserFactories.put(getParserName("WorldData"), new FaenorWorldDataParserFactory());
	}
}
