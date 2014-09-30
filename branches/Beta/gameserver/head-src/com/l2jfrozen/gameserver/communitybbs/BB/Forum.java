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
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfrozen.gameserver.communitybbs.Manager.TopicBBSManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class Forum
{
	//type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	//perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;

	private static Logger _log = Logger.getLogger(Forum.class.getName());
	private List<Forum> _children;
	private Map<Integer, Topic> _topic;
	private int _forumId;
	private String _forumName;
	//private int _ForumParent;
	private int _forumType;
	private int _forumPost;
	private int _forumPerm;
	private Forum _fParent;
	private int _ownerID;
	private boolean _loaded = false;

	/**
	 * @param Forumid 
	 * @param FParent 
	 */
	public Forum(int Forumid, Forum FParent)
	{
		_forumId = Forumid;
		_fParent = FParent;
		_children = new FastList<Forum>();
		_topic = new FastMap<Integer, Topic>();
	}

	/**
	 * @param name
	 * @param parent
	 * @param type
	 * @param perm
	 * @param OwnerID 
	 */
	public Forum(String name, Forum parent, int type, int perm, int OwnerID)
	{
		_forumName = name;
		_forumId = ForumsBBSManager.getInstance().getANewID();
		//_ForumParent = parent.getID();
		_forumType = type;
		_forumPost = 0;
		_forumPerm = perm;
		_fParent = parent;
		_ownerID = OwnerID;
		_children = new FastList<Forum>();
		_topic = new FastMap<Integer, Topic>();
		parent._children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		_loaded = true;
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
			statement.setInt(1, _forumId);
			ResultSet result = statement.executeQuery();

			if(result.next())
			{
				_forumName = result.getString("forum_name");
				//_ForumParent = Integer.parseInt(result.getString("forum_parent"));
				_forumPost = Integer.parseInt(result.getString("forum_post"));
				_forumType = Integer.parseInt(result.getString("forum_type"));
				_forumPerm = Integer.parseInt(result.getString("forum_perm"));
				_ownerID = Integer.parseInt(result.getString("forum_owner_id"));
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warning("data error on Forum " + _forumId + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
			statement.setInt(1, _forumId);
			ResultSet result = statement.executeQuery();

			while(result.next())
			{
				Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(result.getString("topic_id")), Integer.parseInt(result.getString("topic_forum_id")), result.getString("topic_name"), Long.parseLong(result.getString("topic_date")), result.getString("topic_ownername"), Integer.parseInt(result.getString("topic_ownerid")), Integer.parseInt(result.getString("topic_type")), Integer.parseInt(result.getString("topic_reply")));
				_topic.put(t.getID(), t);
				if(t.getID() > TopicBBSManager.getInstance().getMaxID(this))
				{
					TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
				t = null;
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warning("data error on Forum " + _forumId + " : " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	private void getChildren()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
			statement.setInt(1, _forumId);
			ResultSet result = statement.executeQuery();

			while(result.next())
			{
				Forum f = new Forum(result.getInt("forum_id"), this);
				_children.add(f);
				ForumsBBSManager.getInstance().addForum(f);
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warning("data error on Forum (children): " + e);
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}

	}

	public int getTopicSize()
	{
		vload();
		return _topic.size();
	}

	public Topic gettopic(int j)
	{
		vload();
		return _topic.get(j);
	}

	public void addtopic(Topic t)
	{
		vload();
		_topic.put(t.getID(), t);
	}

	public int getID()
	{
		return _forumId;
	}

	public String getName()
	{
		vload();
		return _forumName;
	}

	public int getType()
	{
		vload();
		return _forumType;
	}

	/**
	 * @param name
	 * @return
	 */
	public Forum getChildByName(String name)
	{
		vload();

		for(Forum f : _children)
		{
			if(f==null || f.getName() == null){
				continue;
			}
			
			if(f.getName().equals(name))
				return f;
		}
		return null;
	}

	public void rmTopicByID(int id)
	{
		_topic.remove(id);
	}

	public void insertindb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
			statement.setInt(1, _forumId);
			statement.setString(2, _forumName);
			statement.setInt(3, _fParent.getID());
			statement.setInt(4, _forumPost);
			statement.setInt(5, _forumType);
			statement.setInt(6, _forumPerm);
			statement.setInt(7, _ownerID);
			statement.execute();
			statement.close();

			statement = null;

		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("error while saving new Forum to db " + e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}

	public void vload()
	{
		if(!_loaded)
		{
			load();
			getChildren();
			_loaded = true;
		}
	}
}
