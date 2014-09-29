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
package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2NpcWalkerNode;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * 
 * @author Rayan RPG for L2Emu Project
 * @author ProGramMoS
 * @since 927
 */
public class NpcWalkerRoutesTable
{
	protected static final Logger _log = Logger.getLogger(NpcWalkerRoutesTable.class.getName());
	
	private static NpcWalkerRoutesTable _instance;

	private FastList<L2NpcWalkerNode> _routes;

	public static NpcWalkerRoutesTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new NpcWalkerRoutesTable();
			_log.info("Initializing Walkers Routes Table.");
		}

		return _instance;
	}

	private NpcWalkerRoutesTable()
	{
	//not here
	}

	public void load()
	{
		_routes = new FastList<L2NpcWalkerNode>();
		//java.sql.Connection con = null;

		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			File fileData = new File(Config.DATAPACK_ROOT+"/data/csv/walker_routes.csv");
			
			reader = new FileReader(fileData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			L2NpcWalkerNode route;
			String line = null;

			//format:
			//  route_id;npc_id;move_point;chatText;move_x;move_y;move_z;delay;running
			while((line = lnr.readLine()) != null)
			{
				//ignore comments
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				route = new L2NpcWalkerNode();
				StringTokenizer st = new StringTokenizer(line, ";");

				int route_id = Integer.parseInt(st.nextToken());
				int npc_id = Integer.parseInt(st.nextToken());
				String move_point = st.nextToken();
				String chatText = st.nextToken();
				int move_x = Integer.parseInt(st.nextToken());
				int move_y = Integer.parseInt(st.nextToken());
				int move_z = Integer.parseInt(st.nextToken());
				int delay = Integer.parseInt(st.nextToken());
				boolean running = Boolean.parseBoolean(st.nextToken());

				route.setRouteId(route_id);
				route.setNpcId(npc_id);
				route.setMovePoint(move_point);
				route.setChatText(chatText);
				route.setMoveX(move_x);
				route.setMoveY(move_y);
				route.setMoveZ(move_z);
				route.setDelay(delay);
				route.setRunning(running);

				_routes.add(route);
				route = null;
			}

			_log.info("WalkerRoutesTable: Loaded " + _routes.size() + " Npc Walker Routes.");

		}
		catch(FileNotFoundException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING,"walker_routes.csv is missing in data folder");
		}
		catch(IOException e0)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e0.printStackTrace();
			
			_log.log(Level.WARNING,"Error while creating table: " + e0.getMessage() + "\n" + e0);
		}
		finally
		{
			if(lnr != null)
				try
				{
					lnr.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(buff != null)
				try
				{
					buff.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(reader != null)
				try
				{
					reader.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
		}

	}

	public FastList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		FastList<L2NpcWalkerNode> _return = new FastList<L2NpcWalkerNode>();

		for(FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;)
		{
			if(n.getValue().getNpcId() == id)
			{
				_return.add(n.getValue());
			}
		}

		return _return;
	}
}
