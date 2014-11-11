package com.l2jfrozen.gameserver.powerpak.vote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.L2Utils;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.sql.SQLQuery;
import com.l2jfrozen.gameserver.util.sql.SQLQueue;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

public class L2TopDeamon implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(L2TopDeamon.class);
	protected ScheduledFuture<?> _task;
	private Timestamp _lastVote;
	private boolean _firstRun = false;
	
	protected class Terminator extends Thread
	{
		@Override
		public void run()
		{
			LOGGER.info("L2TopDeamon: stopped");
			try
			{
				if (L2TopDeamon.getInstance()._task != null)
				{
					L2TopDeamon.getInstance()._task.cancel(true);
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private L2TopDeamon()
	{
		_lastVote = null;
		if (PowerPakConfig.L2TOPDEMON_ENABLED)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				final PreparedStatement stm = con.prepareStatement("select max(votedate) from l2votes");
				final ResultSet r = stm.executeQuery();
				if (r.next())
				{
					_lastVote = r.getTimestamp(1);
				}
				if (_lastVote == null)
				{
					_firstRun = true;
					_lastVote = new Timestamp(0);
				}
				r.close();
				stm.close();
				
				_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000, PowerPakConfig.L2TOPDEMON_POLLINTERVAL * 60000);
				Runtime.getRuntime().addShutdownHook(new Terminator());
				LOGGER.info("L2TopDeamon: Started with poll interval " + PowerPakConfig.L2TOPDEMON_POLLINTERVAL + " minute(s)");
			}
			catch (final SQLException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.info("L2TopDeamon: Error connection to database: " + e.getMessage());
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
		}
	}
	
	private class VotesUpdate implements SQLQuery
	{
		private final Timestamp _votedate;
		private final String _charName;
		private final boolean _fr;
		
		public VotesUpdate(final Timestamp votedate, final String charName, final boolean fr)
		{
			_votedate = votedate;
			_charName = charName;
			_fr = fr;
		}
		
		@Override
		public void execute(final Connection con)
		{
			try
			{
				final PreparedStatement stm = con.prepareStatement("insert into l2votes select ?,? from characters where not exists(select * from l2votes where votedate=? and charName =?) limit 1");
				stm.setTimestamp(1, _votedate);
				stm.setTimestamp(3, _votedate);
				stm.setString(2, _charName);
				stm.setString(4, _charName);
				boolean sendPrize = stm.executeUpdate() > 0;
				stm.close();
				if (_fr && PowerPakConfig.L2TOPDEMON_IGNOREFIRST)
				{
					sendPrize = false;
				}
				if (sendPrize)
				{
					final L2PcInstance player = L2Utils.loadPlayer(_charName);
					if (player != null)
					{
						int numItems = PowerPakConfig.L2TOPDEMON_MIN;
						if (PowerPakConfig.L2TOPDEMON_MAX > PowerPakConfig.L2TOPDEMON_MIN)
						{
							numItems += Rnd.get(PowerPakConfig.L2TOPDEMON_MAX - PowerPakConfig.L2TOPDEMON_MIN);
						}
						player.addItem("l2top", PowerPakConfig.L2TOPDEMON_ITEM, numItems, null, true);
						if ((player.isOnline() != 0) && (PowerPakConfig.L2TOPDEMON_MESSAGE != null) && (PowerPakConfig.L2TOPDEMON_MESSAGE.length() > 0))
						{
							player.sendMessage(PowerPakConfig.L2TOPDEMON_MESSAGE);
						}
						player.store();
					}
				}
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private boolean checkVotes()
	{
		boolean output = false;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try
		{
			LOGGER.info("L2TopDeamon: Checking l2top.ru....");
			int nVotes = 0;
			final URL url = new URL(PowerPakConfig.L2TOPDEMON_URL);
			is = url.openStream();
			isr = new InputStreamReader(is);
			reader = new BufferedReader(isr);
			String line;
			Timestamp last = _lastVote;
			while ((line = reader.readLine()) != null)
			{
				if (line.contains("\t"))
				{
					final Timestamp voteDate = Timestamp.valueOf(line.substring(0, line.indexOf("\t")).trim());
					if (voteDate.after(_lastVote))
					{
						if (voteDate.after(last))
						{
							last = voteDate;
						}
						String charName = line.substring(line.indexOf("\t") + 1).toLowerCase();
						if ((PowerPakConfig.L2TOPDEMON_PREFIX != null) && (PowerPakConfig.L2TOPDEMON_PREFIX.length() > 0))
						{
							if (charName.startsWith(PowerPakConfig.L2TOPDEMON_PREFIX))
							{
								charName = charName.substring(PowerPakConfig.L2TOPDEMON_PREFIX.length());
							}
							else
							{
								continue;
							}
						}
						SQLQueue.getInstance().add(new VotesUpdate(voteDate, charName, _firstRun));
						nVotes++;
					}
				}
			}
			_lastVote = last;
			LOGGER.info("L2TopDeamon: " + nVotes + " vote(s) parsed");
			output = true;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("L2TopDeamon: Error while reading data" + e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (isr != null)
			{
				try
				{
					isr.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return output;
	}
	
	@Override
	public void run()
	{
		checkVotes();
		_firstRun = false;
	}
	
	public static L2TopDeamon getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final L2TopDeamon _instance = new L2TopDeamon();
	}
}
