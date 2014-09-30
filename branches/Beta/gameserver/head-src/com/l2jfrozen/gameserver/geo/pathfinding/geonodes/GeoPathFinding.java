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
package com.l2jfrozen.gameserver.geo.pathfinding.geonodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.pathfinding.PathFinding;
import com.l2jfrozen.gameserver.geo.util.L2Arrays;
import com.l2jfrozen.gameserver.geo.util.LookupTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;


public final class GeoPathFinding extends PathFinding
{
	protected static final Logger _log = Logger.getLogger(GeoPathFinding.class.getName());
	
	private static final class SingletonHolder
	{
		protected static final GeoPathFinding INSTANCE = new GeoPathFinding();
	}
	
	public static GeoPathFinding getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final LookupTable<ByteBuffer> _pathNodes = new LookupTable<ByteBuffer>();
	private final LookupTable<IntBuffer> _pathNodesIndex = new LookupTable<IntBuffer>();
	
	private boolean pathNodesExist(short regionoffset)
	{
		return _pathNodesIndex.get(regionoffset) != null;
	}
	
	@Override
	public Node[] findPath(int x, int y, int z, int tx, int ty, int tz)
	{
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;
		short gz = (short) z;
		int gtx = tx - L2World.MAP_MIN_X >> 4;
		int gty = ty - L2World.MAP_MIN_Y >> 4;
		short gtz = (short) tz;

		Node start = readNode(gx, gy, gz);
		Node end = readNode(gtx, gty, gtz);
		if(start == null || end == null)
			return null;
		if(Math.abs(start.getZ() - z) > 55)
			return null; // not correct layer
		if(Math.abs(end.getZ() - tz) > 55)
			return null; // not correct layer
		if(start.equals(end))
			return null;
		// TODO: Find closest path node we CAN access. Now only checks if we can not reach the closest
		Location temp = GeoData.getInstance().moveCheck(x, y, z, start.getX(), start.getY(), start.getZ());
		if(temp.getX() != start.getX() || temp.getY() != start.getY())
			return null; //   cannot reach closest...

		// TODO: Find closest path node around target, now only checks if final location can be reached
		temp = GeoData.getInstance().moveCheck(tx, ty, tz, end.getX(), end.getY(), end.getZ());
		if(temp.getX() != end.getX() || temp.getY() != end.getY())
			return null; //   cannot reach closest...

		//return searchAStar(start, end);
		return searchByClosest2(start, end);
	}
	
	@Override
	public Node[] readNeighbors(Node n, int idx)
	{
		int node_x = n.getNodeX();
		int node_y = n.getNodeY();
		//short node_z = n.getZ();
			
		short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		ByteBuffer pn = _pathNodes.get(regoffset);

		Node[] Neighbors = new Node[8];
		int index = 0;
		Node newNode;
		short new_node_x, new_node_y;

		//Region for sure will change, we must read from correct file
		byte neighbor = pn.get(idx++); //N
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //NE
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //E
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //SE
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x + 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //S
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) node_x;
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //SW
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y + 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //W
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) node_y;
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		neighbor = pn.get(idx++); //NW
		if(neighbor > 0)
		{
			neighbor--;
			new_node_x = (short) (node_x - 1);
			new_node_y = (short) (node_y - 1);
			newNode = readNode(new_node_x, new_node_y, neighbor);
			if(newNode != null)
			{
				Neighbors[index++] = newNode;
			}
		}
		return L2Arrays.compact(Neighbors);
	}

	//Private

	private Node readNode(short node_x, short node_y, byte layer)
	{
		short regoffset = getRegionOffset(getRegionX(node_x),getRegionY(node_y));
		if(!pathNodesExist(regoffset))
			return null;
		short nbx = getNodeBlock(node_x);
		short nby = getNodeBlock(node_y);
		int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		ByteBuffer pn = _pathNodes.get(regoffset);
		//reading
		byte nodes = pn.get(idx);
		idx += layer * 10 + 1;//byte + layer*10byte
		if(nodes < layer)
		{
			_log.log(Level.WARNING, "SmthWrong!");
		}
		short node_z = pn.getShort(idx);
		idx += 2;
		return new GeoNode(node_x, node_y, node_z, idx);
	}

	private Node readNode(int gx, int gy, short z)
	{
		short node_x = getNodePos(gx);
		short node_y = getNodePos(gy);
		short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
		if(!pathNodesExist(regoffset))
			return null;
		short nbx = getNodeBlock(node_x);
		short nby = getNodeBlock(node_y);
		int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
		ByteBuffer pn = _pathNodes.get(regoffset);
		//reading
		byte nodes = pn.get(idx++);
		int idx2 = 0; //create index to nearlest node by z
		short last_z = Short.MIN_VALUE;
		while(nodes > 0)
		{
			short node_z = pn.getShort(idx);
			if(Math.abs(last_z - z) > Math.abs(node_z - z))
			{
				last_z = node_z;
				idx2 = idx + 2;
			}
			idx += 10; //short + 8 byte
			nodes--;
		}
		return new GeoNode(node_x, node_y, last_z, idx2);
	}

	protected GeoPathFinding()
	{
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			_log.info("PathFinding Engine: - Loading Path Nodes...");
			File Data = new File(Config.DATAPACK_ROOT+"/data/pathnode/pn_index.txt");
			if(!Data.exists())
				return;

			reader = new FileReader(Data);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);

			String line;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0)
					continue;
				StringTokenizer st = new StringTokenizer(line, "_");
				byte rx = Byte.parseByte(st.nextToken());
				byte ry = Byte.parseByte(st.nextToken());
				LoadPathNodeFile(rx, ry);
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
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

	private void LoadPathNodeFile(byte rx, byte ry)
	{
		String fname = Config.DATAPACK_ROOT+"/data/pathnode/" + rx + "_" + ry + ".pn";
		short regionoffset = getRegionOffset(rx, ry);
		_log.info("PathFinding Engine: - Loading: " + fname + " -> region offset: " + regionoffset + "X: " + rx + " Y: " + ry);
		File Pn = new File(fname);
		int node = 0,size, index = 0;
		RandomAccessFile raf = null;
		FileChannel roChannel = null;
		try
		{
			// Create a read-only memory-mapped file
			raf = new RandomAccessFile(Pn, "r");
			roChannel = raf.getChannel();
			size = (int)roChannel.size();
			MappedByteBuffer nodes;
			if(Config.FORCE_GEODATA) //Force O/S to Loads this buffer's content into physical memory.
				//it is not guarantee, because the underlying operating system may have paged out some of the buffer's data
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			else
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);

			// Indexing pathnode files, so we will know where each block starts
			IntBuffer indexs = IntBuffer.allocate(65536);

			while(node < 65536)
			{
				byte layer = nodes.get(index);
				indexs.put(node++, index);
				index += layer * 10 + 1;
			}
			_pathNodesIndex.set(regionoffset, indexs);
			_pathNodes.set(regionoffset, nodes);
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "Failed to Load PathNode File: "+fname+"\n", e);
		}
		finally
		{
			if(roChannel != null)
				try
				{
					roChannel.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(raf != null)
				try
				{
					raf.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			
		}

	}
}
