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
package com.l2jfrozen.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.l2jfrozen.Config;

/**
 * @author programmos
 */
public abstract class XmlEngine
{
	protected static final Logger _log = Logger.getLogger(XmlEngine.class.getName());
	
	private File _file;
	
	XmlEngine(File f)
	{
		_file = f;
		parseFile();
	}
	
	public void parseFile()
	{
		Document document = null;
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			document = factory.newDocumentBuilder().parse(_file);
		}
		catch(ParserConfigurationException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error loading configure XML: " + _file.getName(), e);
		}
		catch(SAXException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error loading file: " + _file.getName(), e);
		}
		
		try
		{
			parseDocument(document);
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error in file: " + _file.getName(), e);
		}
	}
	
	public abstract void parseDocument(Document document) throws Exception;
	
	public List<Node> parseHeadStandart(Document doc)
	{
		List<Node> temp = new FastList<Node>();
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("record".equalsIgnoreCase(d.getNodeName()))
					{
						for(Node e = d.getFirstChild(); e != null; e = n.getNextSibling())
						{
							if("value".equalsIgnoreCase(n.getNodeName()))
							{
								temp.add(d);
							}
						}
					}
				}
			}
		}
		
		return temp;
	}

}
