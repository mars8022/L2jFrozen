package com.l2jfrozen.gameserver.powerpak.vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.L2Utils;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.sql.SQLQuery;
import com.l2jfrozen.gameserver.util.sql.SQLQueue;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

public class L2TopDeamon implements Runnable
{
	private static L2TopDeamon _instance = null;
	public static L2TopDeamon getInstance()
	{
		if(_instance == null)
			_instance = new L2TopDeamon();
		return _instance;
	}
	private static final Logger _log = Logger.getLogger(L2TopDeamon.class.getName());
    private ScheduledFuture<?> _task;
	private Timestamp _lastVote;
	private boolean _firstRun = false;

	private class Terminator extends Thread
	{
		@Override
		public void run()
		{
			System.out.println("L2TopDeamon: stopped");
			try
			{
				if(L2TopDeamon.getInstance()._task!=null)
				{
					L2TopDeamon.getInstance()._task.cancel(true);
				}
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
		}
	}
	private L2TopDeamon()
	{
		_lastVote = null;
		if(PowerPakConfig.L2TOPDEMON_ENABLED) try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("select max(votedate) from l2votes");
			ResultSet r = stm.executeQuery();
			if(r.next())
				_lastVote = r.getTimestamp(1);
			if(_lastVote==null) {
				_firstRun = true;
				_lastVote =  new Timestamp(0);
			}
			r.close();
			stm.close();
			try
			{
				con.close();
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
			}
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000, PowerPakConfig.L2TOPDEMON_POLLINTERVAL*60000);
			Runtime.getRuntime().addShutdownHook(new Terminator());
			_log.info("L2TopDeamon: Started with poll interval "+PowerPakConfig.L2TOPDEMON_POLLINTERVAL+" minute(s)");
		}
		catch(SQLException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.info("L2TopDeamon: Error connection to database: "+ e.getMessage());
		}
	}
	private class VotesUpdate implements SQLQuery
	{
		private Timestamp _votedate; 
		private String _charName;
		private boolean _fr;
		public VotesUpdate(Timestamp votedate, String charName, boolean fr)
		{
			_votedate = votedate;
			_charName = charName;
			_fr = fr;
		}
		@Override
		public void execute(Connection con)
		{
			try
			{
				PreparedStatement stm = con.prepareStatement("insert into l2votes select ?,? from characters where not exists(select * from l2votes where votedate=? and charName =?) limit 1");
				stm.setTimestamp(1, _votedate);
				stm.setTimestamp(3, _votedate);
				stm.setString(2, _charName);
				stm.setString(4, _charName);
				boolean sendPrize  = stm.executeUpdate()>0; 
				stm.close();
				if(_fr && PowerPakConfig.L2TOPDEMON_IGNOREFIRST)
					sendPrize = false;
				if(sendPrize)
				{
					L2PcInstance player = L2Utils.loadPlayer(_charName);
					if(player!=null)
					{
						int numItems = PowerPakConfig.L2TOPDEMON_MIN;
						if(PowerPakConfig.L2TOPDEMON_MAX > PowerPakConfig.L2TOPDEMON_MIN )
							numItems += Rnd.get(PowerPakConfig.L2TOPDEMON_MAX-PowerPakConfig.L2TOPDEMON_MIN);
						player.addItem("l2top", PowerPakConfig.L2TOPDEMON_ITEM, numItems, null, true);
						if(player.isOnline()!=0 && PowerPakConfig.L2TOPDEMON_MESSAGE != null && PowerPakConfig.L2TOPDEMON_MESSAGE.length() >0)
							player.sendMessage(PowerPakConfig.L2TOPDEMON_MESSAGE);
						player.store();
					}
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean checkVotes()
	{
		try
		{
			_log.info("L2TopDeamon: Checking l2top.ru....");
			int nVotes = 0;
			URL url = new URL(PowerPakConfig.L2TOPDEMON_URL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			if(reader!=null)
			{
				String line;
				Timestamp last = _lastVote;
				while((line= reader.readLine())!=null)
				{
					if(line.indexOf("\t")!=-1)
					{
						Timestamp voteDate = Timestamp.valueOf(line.substring(0,line.indexOf("\t")).trim());
						if(voteDate.after(_lastVote))
						{
							if(voteDate.after(last))
								last = voteDate;
							String charName = line.substring(line.indexOf("\t")+1).toLowerCase();
							if(PowerPakConfig.L2TOPDEMON_PREFIX!=null && PowerPakConfig.L2TOPDEMON_PREFIX.length()>0)
								if(charName.startsWith(PowerPakConfig.L2TOPDEMON_PREFIX)) {
									charName = charName.substring(PowerPakConfig.L2TOPDEMON_PREFIX.length());
								}
								else
									continue;
							SQLQueue.getInstance().add(new VotesUpdate(voteDate,charName,_firstRun));
							nVotes++;
						}
					}
				}
				_lastVote = last;
				_log.info("L2TopDeamon: "+nVotes+" vote(s) parsed");
				return true;
			}
		} catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.info("L2TopDeamon: Error while reading data"+ e);
		}
		return false;
	}
	@Override
	public void run()
	{
		checkVotes();
		_firstRun = false;
		
	}
}
