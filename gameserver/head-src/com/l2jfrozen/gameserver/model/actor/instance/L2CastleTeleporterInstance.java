package com.l2jfrozen.gameserver.model.actor.instance;
/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;


/**
 * @author Kerberos
 *
 */
public final class L2CastleTeleporterInstance extends L2NpcInstance
{
	public static final Logger _log = Logger.getLogger(L2CastleTeleporterInstance.class.getName());
	
	private boolean _currentTask = false;
	
	/**
	 * @param template
	 */
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("tele"))
		{
			int delay;
			if (!getTask())
			{
				if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
					delay = 480000;
				else
					delay = 30000;
				
				setTask(true);
				ThreadPoolManager.getInstance().scheduleGeneral(new oustAllPlayers(), delay );
			}
			
			String filename = "data/html/castleteleporter/MassGK-1.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			player.sendPacket(html);
			return;
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename;
		if (!getTask())
		{
			if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
				filename = "data/html/castleteleporter/MassGK-2.htm";
			else
				filename = "data/html/castleteleporter/MassGK.htm";
		}
		else
			filename = "data/html/castleteleporter/MassGK-1.htm";
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}
	
	class oustAllPlayers implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				/*
				CreatureSay cs = new CreatureSay(getObjectId(), 1, getName(), 1000443); // The defenders of $s1 castle will be teleported to the inner castle.
				cs.addStringParameter(getCastle().getName());
				int region = MapRegionTable.getInstance().getMapRegion(getX(), getY());
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
				//synchronized (L2World.getInstance().getAllPlayers())
				{
					for (L2PcInstance player : pls)
					{
						if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY()))
							player.sendPacket(cs);
					}
				}*/
				oustAllPlayers();
				setTask(false);
			}
			catch (NullPointerException e)
			{
				_log.log(Level.WARNING, "" + e.getMessage(), e);
			}
		}
	}
	
	public boolean getTask()
	{
		return _currentTask;
	}
	
	public void setTask(boolean state)
	{
		_currentTask = state;
	}

}