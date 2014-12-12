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
package com.l2jfrozen.gameserver.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Layane
 */
public class CrestCache
{
	private static Logger LOGGER = Logger.getLogger(CrestCache.class);
	
	private static CrestCache _instance;
	
	private final FastMRUCache<Integer, byte[]> _cachePledge = new FastMRUCache<>();
	
	private final FastMRUCache<Integer, byte[]> _cachePledgeLarge = new FastMRUCache<>();
	
	private final FastMRUCache<Integer, byte[]> _cacheAlly = new FastMRUCache<>();
	
	private int _loadedFiles;
	
	private long _bytesBuffLen;
	
	public static CrestCache getInstance()
	{
		if (_instance == null)
		{
			_instance = new CrestCache();
		}
		
		return _instance;
	}
	
	public CrestCache()
	{
		convertOldPedgeFiles();
		reload();
	}
	
	public void reload()
	{
		final FileFilter filter = new BmpFilter();
		
		final File dir = new File(Config.DATAPACK_ROOT, "data/crests/");
		
		final File[] files = dir.listFiles(filter);
		byte[] content;
		synchronized (this)
		{
			_loadedFiles = 0;
			_bytesBuffLen = 0;
			
			_cachePledge.clear();
			_cachePledgeLarge.clear();
			_cacheAlly.clear();
		}
		
		final FastMap<Integer, byte[]> _mapPledge = _cachePledge.getContentMap();
		final FastMap<Integer, byte[]> _mapPledgeLarge = _cachePledgeLarge.getContentMap();
		final FastMap<Integer, byte[]> _mapAlly = _cacheAlly.getContentMap();
		
		for (final File file : files)
		{
			RandomAccessFile f = null;
			synchronized (this)
			{
				try
				{
					f = new RandomAccessFile(file, "r");
					content = new byte[(int) f.length()];
					f.readFully(content);
					
					if (file.getName().startsWith("Crest_Large_"))
					{
						_mapPledgeLarge.put(Integer.valueOf(file.getName().substring(12, file.getName().length() - 4)), content);
					}
					else if (file.getName().startsWith("Crest_"))
					{
						_mapPledge.put(Integer.valueOf(file.getName().substring(6, file.getName().length() - 4)), content);
					}
					else if (file.getName().startsWith("AllyCrest_"))
					{
						_mapAlly.put(Integer.valueOf(file.getName().substring(10, file.getName().length() - 4)), content);
					}
					
					_loadedFiles++;
					_bytesBuffLen += content.length;
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					LOGGER.warn("problem with crest bmp file " + e);
				}
				finally
				{
					if (f != null)
						try
						{
							f.close();
						}
						catch (final Exception e1)
						{
							e1.printStackTrace();
						}
				}
			}
		}
		
		LOGGER.info("Cache[Crest]: " + String.format("%.3f", getMemoryUsage()) + "MB on " + getLoadedFiles() + " files loaded. (Forget Time: " + _cachePledge.getForgetTime() / 1000 + "s , Capacity: " + _cachePledge.capacity() + ")");
	}
	
	public void convertOldPedgeFiles()
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/crests/");
		
		File[] files = dir.listFiles(new OldPledgeFilter());
		
		if (files == null)
		{
			LOGGER.info("No old crest files found in \"data/crests/\"!!! May be you deleted them?");
			return;
		}
		
		for (final File file : files)
		{
			final int clanId = Integer.parseInt(file.getName().substring(7, file.getName().length() - 4));
			
			LOGGER.info("Found old crest file \"" + file.getName() + "\" for clanId " + clanId);
			
			final int newId = IdFactory.getInstance().getNextId();
			
			L2Clan clan = ClanTable.getInstance().getClan(clanId);
			
			if (clan != null)
			{
				removeOldPledgeCrest(clan.getCrestId());
				
				file.renameTo(new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp"));
				LOGGER.info("Renamed Clan crest to new format: Crest_" + newId + ".bmp");
				
				Connection con = null;
				
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(false);
					PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
					statement.setInt(1, newId);
					statement.setInt(2, clan.getClanId());
					statement.executeUpdate();
					DatabaseUtils.close(statement);
					statement = null;
				}
				catch (final SQLException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					LOGGER.warn("could not update the crest id:" + e.getMessage());
				}
				finally
				{
					CloseUtil.close(con);
				}
				
				clan.setCrestId(newId);
				clan.setHasCrest(true);
			}
			else
			{
				LOGGER.info("Clan Id: " + clanId + " does not exist in table.. deleting.");
				file.delete();
			}
			
			clan = null;
		}
		
		files = null;
		dir = null;
	}
	
	public float getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}
	
	public int getLoadedFiles()
	{
		return _loadedFiles;
	}
	
	public byte[] getPledgeCrest(final int id)
	{
		return _cachePledge.get(id);
	}
	
	public byte[] getPledgeCrestLarge(final int id)
	{
		return _cachePledgeLarge.get(id);
	}
	
	public byte[] getAllyCrest(final int id)
	{
		return _cacheAlly.get(id);
	}
	
	public void removePledgeCrest(final int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + id + ".bmp");
		_cachePledge.remove(id);
		
		try
		{
			crestFile.delete();
			crestFile = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
	}
	
	public void removePledgeCrestLarge(final int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + id + ".bmp");
		_cachePledgeLarge.remove(id);
		
		try
		{
			crestFile.delete();
			crestFile = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
	}
	
	public void removeOldPledgeCrest(final int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Pledge_" + id + ".bmp");
		
		try
		{
			crestFile.delete();
			crestFile = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
	}
	
	public void removeAllyCrest(final int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + id + ".bmp");
		_cacheAlly.remove(id);
		
		try
		{
			crestFile.delete();
			crestFile = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}
	}
	
	public boolean savePledgeCrest(final int newId, final byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp");
		
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledge.getContentMap().put(newId, data);
			
			output = true;
		}
		catch (final IOException e)
		{
			LOGGER.error("Error saving pledge crest" + crestFile, e);
		}
		finally
		{
			
			if (out != null)
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			
		}
		
		return output;
	}
	
	public boolean savePledgeCrestLarge(final int newId, final byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledgeLarge.getContentMap().put(newId, data);
			
			output = true;
		}
		catch (final IOException e)
		{
			LOGGER.error("Error saving Large pledge crest" + crestFile, e);
			
		}
		finally
		{
			
			if (out != null)
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			
		}
		
		return output;
	}
	
	public boolean saveAllyCrest(final int newId, final byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cacheAlly.getContentMap().put(newId, data);
			
			output = true;
		}
		catch (final IOException e)
		{
			LOGGER.error("Error saving ally crest" + crestFile, e);
			
		}
		finally
		{
			
			if (out != null)
				try
				{
					out.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			
		}
		
		return output;
	}
	
	class BmpFilter implements FileFilter
	{
		@Override
		public boolean accept(final File file)
		{
			return file.getName().endsWith(".bmp");
		}
	}
	
	class OldPledgeFilter implements FileFilter
	{
		@Override
		public boolean accept(final File file)
		{
			return file.getName().startsWith("Pledge_");
		}
	}
}
