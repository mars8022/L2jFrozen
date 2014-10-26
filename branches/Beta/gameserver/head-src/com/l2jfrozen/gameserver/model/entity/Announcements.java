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
package com.l2jfrozen.gameserver.model.entity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.script.DateRange;

/**
 * @author ProGramMoS
 * @version 1.6
 */
public class Announcements
{
	private static Logger LOGGER = Logger.getLogger(Announcements.class);
	
	private static Announcements _instance;
	private final List<String> _announcements = new FastList<>();
	private final List<List<Object>> _eventAnnouncements = new FastList<>();
	
	public Announcements()
	{
		loadAnnouncements();
	}
	
	public static Announcements getInstance()
	{
		if (_instance == null)
		{
			_instance = new Announcements();
		}
		
		return _instance;
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		final File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			LOGGER.warn("data/announcements.txt doesn't exist");
		}
		
	}
	
	public void showAnnouncements(final L2PcInstance activeChar)
	{
		for (final String _announcement : _announcements)
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcement.replace("%name%", activeChar.getName()));
			activeChar.sendPacket(cs);
			cs = null;
		}
		
		for (List<Object> entry : _eventAnnouncements)
		{
			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();
			
			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				
				for (final String element : msg)
				{
					sm.addString(element);
				}
				
				activeChar.sendPacket(sm);
				sm = null;
			}
			
			entry = null;
			validDateRange = null;
			msg = null;
			currentDate = null;
		}
	}
	
	public void addEventAnnouncement(final DateRange validDateRange, final String[] msg)
	{
		List<Object> entry = new FastList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
		
		entry = null;
	}
	
	public void listAnnouncements(final L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		TextBuilder replyMSG = new TextBuilder("<br>");
		
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
		
		content = null;
		adminReply = null;
		replyMSG = null;
	}
	
	public void addAnnouncement(final String text)
	{
		_announcements.add(text);
		saveToDisk();
	}
	
	public void delAnnouncement(final int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(final File file)
	{
		LineNumberReader lnr = null;
		FileReader reader = null;
		try
		{
			int i = 0;
			
			String line = null;
			reader = new FileReader(file);
			lnr = new LineNumberReader(reader);
			
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					final String announcement = st.nextToken();
					_announcements.add(announcement);
					
					i++;
				}
			}
			LOGGER.info("Announcements: Loaded " + i + " Announcements.");
		}
		catch (final IOException e1)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e1.printStackTrace();
			
			LOGGER.error("Error reading announcements", e1);
		}
		finally
		{
			if (lnr != null)
				try
				{
					lnr.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			
			if (reader != null)
				try
				{
					reader.close();
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
		}
	}
	
	private void saveToDisk()
	{
		final File file = new File("data/announcements.txt");
		FileWriter save = null;
		
		try
		{
			save = new FileWriter(file);
			for (final String _announcement : _announcements)
			{
				save.write(_announcement);
				save.write("\r\n");
			}
			save.flush();
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("saving the announcements file has failed: " + e);
		}
		finally
		{
			
			if (save != null)
				try
				{
					save.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
		}
		
	}
	
	public void announceToAll(final String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
		
		cs = null;
	}
	
	// Colored Announcements 8D
	public void gameAnnounceToAll(final String text)
	{
		CreatureSay cs = new CreatureSay(0, 18, "", "Announcements: " + text);
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player != null)
				if (player.isOnline() != 0)
					player.sendPacket(cs);
		}
		
		cs = null;
	}
	
	public void announceToAll(final SystemMessage sm)
	{
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}
	
	// Method fo handling announcements from admin
	public void handleAnnounce(final String command, final int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			Announcements.getInstance().announceToAll(text);
			text = null;
		}
		
		// No body cares!
		catch (final StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}
}
