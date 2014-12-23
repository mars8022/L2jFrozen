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
package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.communitybbs.BB.Forum;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger LOGGER = Logger.getLogger(ForumsBBSManager.class);
	private final List<Forum> _table;
	private static ForumsBBSManager _instance;
	private int _lastid = 1;
	
	/**
	 * @return
	 */
	public static ForumsBBSManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ForumsBBSManager();
		}
		return _instance;
	}
	
	public ForumsBBSManager()
	{
		_table = new FastList<>();
		load();
	}
	
	public void addForum(final Forum ff)
	{
		if (ff == null)
			return;
		
		_table.add(ff);
		
		if (ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}
	
	/**
	 *
	 */
	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final Forum f = new Forum(result.getInt("forum_id"), null);
				addForum(f);
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Forum (root): " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void initRoot()
	{
		for (final Forum f : _table)
			f.vload();
		LOGGER.info("Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}
	
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		//
	}
	
	/**
	 * @param Name
	 * @return
	 */
	public Forum getForumByName(final String Name)
	{
		for (final Forum f : _table)
		{
			if (f.getName().equals(Name))
				return f;
		}
		
		return null;
	}
	
	/**
	 * @param name
	 * @param parent
	 * @param type
	 * @param perm
	 * @param oid
	 * @return
	 */
	public Forum createNewForum(final String name, final Forum parent, final int type, final int perm, final int oid)
	{
		final Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertindb();
		return forum;
	}
	
	/**
	 * @return
	 */
	public int getANewID()
	{
		return ++_lastid;
	}
	
	/**
	 * @param idf
	 * @return
	 */
	public Forum getForumByID(final int idf)
	{
		for (final Forum f : _table)
		{
			if (f.getID() == idf)
				return f;
		}
		return null;
	}
	
	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final L2PcInstance activeChar)
	{
		//
	}
}
