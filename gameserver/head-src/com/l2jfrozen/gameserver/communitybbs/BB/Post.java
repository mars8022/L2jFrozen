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
package com.l2jfrozen.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.PostBBSManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Maktakien
 */
public class Post
{
	private static Logger LOGGER = Logger.getLogger(Post.class);
	
	public class CPost
	{
		public int postId;
		public String postOwner;
		public int postOwnerId;
		public long postDate;
		public int postTopicId;
		public int postForumId;
		public String postTxt;
	}
	
	private final List<CPost> _post;
	
	// public enum ConstructorType {REPLY, CREATE };
	
	/**
	 * @param _PostOwner
	 * @param _PostOwnerID
	 * @param date
	 * @param tid
	 * @param _PostForumID
	 * @param txt
	 */
	public Post(final String _PostOwner, final int _PostOwnerID, final long date, final int tid, final int _PostForumID, final String txt)
	{
		_post = new FastList<>();
		CPost cp = new CPost();
		cp.postId = 0;
		cp.postOwner = _PostOwner;
		cp.postOwnerId = _PostOwnerID;
		cp.postDate = date;
		cp.postTopicId = tid;
		cp.postForumId = _PostForumID;
		cp.postTxt = txt;
		_post.add(cp);
		insertindb(cp);
		cp = null;
		
	}
	
	public void insertindb(final CPost cp)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)");
			statement.setInt(1, cp.postId);
			statement.setString(2, cp.postOwner);
			statement.setInt(3, cp.postOwnerId);
			statement.setLong(4, cp.postDate);
			statement.setInt(5, cp.postTopicId);
			statement.setInt(6, cp.postForumId);
			statement.setString(7, cp.postTxt);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("error while saving new Post to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
	}
	
	public Post(final Topic t)
	{
		_post = new FastList<>();
		load(t);
	}
	
	public CPost getCPost(final int id)
	{
		int i = 0;
		
		for (final CPost cp : _post)
		{
			if (i++ == id)
				return cp;
		}
		
		return null;
	}
	
	public void deleteme(final Topic t)
	{
		PostBBSManager.getInstance().delPostByTopic(t);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * @param t
	 */
	private void load(final Topic t)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				CPost cp = new CPost();
				cp.postId = Integer.parseInt(result.getString("post_id"));
				cp.postOwner = result.getString("post_owner_name");
				cp.postOwnerId = Integer.parseInt(result.getString("post_ownerid"));
				cp.postDate = Long.parseLong(result.getString("post_date"));
				cp.postTopicId = Integer.parseInt(result.getString("post_topic_id"));
				cp.postForumId = Integer.parseInt(result.getString("post_forum_id"));
				cp.postTxt = result.getString("post_txt");
				_post.add(cp);
				cp = null;
			}
			result.close();
			DatabaseUtils.close(statement);
			
			result = null;
			statement = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("data error on Post " + t.getForumID() + "/" + t.getID() + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	/**
	 * @param i
	 */
	public void updatetxt(final int i)
	{
		Connection con = null;
		try
		{
			CPost cp = getCPost(i);
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp.postTxt);
			statement.setInt(2, cp.postId);
			statement.setInt(3, cp.postTopicId);
			statement.setInt(4, cp.postForumId);
			statement.execute();
			DatabaseUtils.close(statement);
			
			cp = null;
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			LOGGER.warn("error while saving new Post to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}