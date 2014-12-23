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
package com.l2jfrozen.gameserver.script.faenor;

import javax.script.ScriptContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.Parser;
import com.l2jfrozen.gameserver.script.ParserFactory;
import com.l2jfrozen.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorQuestParser extends FaenorParser
{
	protected static final Logger LOGGER = Logger.getLogger(FaenorQuestParser.class);
	
	@Override
	public void parseScript(final Node questNode, final ScriptContext context)
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Quest.");
		}
		
		final String questID = attribute(questNode, "ID");
		
		for (Node node = questNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DROPLIST"))
			{
				parseQuestDropList(node.cloneNode(true), questID);
			}
			else if (isNodeName(node, "DIALOG WINDOWS"))
			{
				// parseDialogWindows(node.cloneNode(true));
			}
			else if (isNodeName(node, "INITIATOR"))
			{
				// parseInitiator(node.cloneNode(true));
			}
			else if (isNodeName(node, "STATE"))
			{
				// parseState(node.cloneNode(true));
			}
		}
	}
	
	private void parseQuestDropList(final Node dropList, final String questID) throws NullPointerException
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Droplist.");
		}
		
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DROP"))
			{
				parseQuestDrop(node.cloneNode(true), questID);
			}
		}
	}
	
	private void parseQuestDrop(final Node drop, final String questID)// throws NullPointerException
	{
		if (DEBUG)
		{
			LOGGER.info("Parsing Drop.");
		}
		
		int npcID;
		int itemID;
		int min;
		int max;
		int chance;
		String[] states;
		try
		{
			npcID = getInt(attribute(drop, "NpcID"));
			itemID = getInt(attribute(drop, "ItemID"));
			min = getInt(attribute(drop, "Min"));
			max = getInt(attribute(drop, "Max"));
			chance = getInt(attribute(drop, "Chance"));
			states = attribute(drop, "States").split(",");
		}
		catch (final NullPointerException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			throw new NullPointerException("Incorrect Drop Data");
		}
		
		if (DEBUG)
		{
			LOGGER.info("Adding Drop to NpcID: " + npcID);
		}
		
		_bridge.addQuestDrop(npcID, itemID, min, max, chance, questID, states);
	}
	
	static class FaenorQuestParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return new FaenorQuestParser();
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Quest"), new FaenorQuestParserFactory());
	}
}
