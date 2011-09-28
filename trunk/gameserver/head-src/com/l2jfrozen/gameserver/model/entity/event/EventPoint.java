package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class EventPoint
{
	private final L2PcInstance _activeChar;
	private Integer _points = 0;

	public EventPoint(L2PcInstance player)
	{
		_activeChar = player;
		loadFromDB();
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public void savePoints()
	{
		saveToDb();
	}

	private void loadFromDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement st = con.prepareStatement("Select * From char_points where charId = ?");
			st.setInt(1, getActiveChar().getObjectId());
			ResultSet rst = st.executeQuery();

			while(rst.next())
			{
				_points = rst.getInt("points");
			}

			rst.close();
			st.close();
		}
		catch(Exception ex)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				ex.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	private void saveToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement st = con.prepareStatement("Update char_points Set points = ? Where charId = ?");
			st.setInt(1, _points);
			st.setInt(2, getActiveChar().getObjectId());
			st.execute();
			st.close();
		}
		catch(Exception ex)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				ex.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	public Integer getPoints()
	{
		return _points;
	}

	public void setPoints(Integer points)
	{
		_points = points;
	}

	public void addPoints(Integer points)
	{
		_points += points;
	}

	public void removePoints(Integer points)
	{
		//Don't know , do the calc or return. it's up to you
		if(_points - points < 0)
			return;

		_points -= points;
	}

	public boolean canSpend(Integer value)
	{
		return _points - value >= 0;
	}

}
