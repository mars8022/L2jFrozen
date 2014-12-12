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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Give / Take Status Aio to Player Changes name color and title color if enabled Uses: setaio [<player_name>] [<time_duration in days>] removeaio [<player_name>] If <player_name> is not specified, the current target player is used.
 * @author KhayrusS
 */
public class AdminAio implements IAdminCommandHandler
{
	private final static Logger LOGGER = Logger.getLogger(AdminAio.class);
	
	private static String[] _adminCommands =
	{
		"admin_setaio",
		"admin_removeaio"
	};
	
	private enum CommandEnum
	{
		admin_setaio,
		admin_removeaio
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
			return false;
		
		switch (comm)
		{
			case admin_setaio:
			{
				
				boolean no_token = false;
				
				if (st.hasMoreTokens())
				{ // char_name not specified
				
					final String char_name = st.nextToken();
					
					final L2PcInstance player = L2World.getInstance().getPlayer(char_name);
					
					if (player != null)
					{
						
						if (st.hasMoreTokens()) // time
						{
							final String time = st.nextToken();
							
							try
							{
								final int value = Integer.parseInt(time);
								
								if (value > 0)
								{
									
									doAio(activeChar, player, char_name, time);
									
									if (player.isAio())
										return true;
									
								}
								else
								{
									activeChar.sendMessage("Time must be bigger then 0!");
									return false;
								}
								
							}
							catch (final NumberFormatException e)
							{
								activeChar.sendMessage("Time must be a number!");
								return false;
							}
							
						}
						else
						{
							no_token = true;
						}
						
					}
					else
					{
						activeChar.sendMessage("Player must be online to set AIO status");
						no_token = true;
					}
					
				}
				else
				{
					
					no_token = true;
					
				}
				
				if (no_token)
				{
					activeChar.sendMessage("Usage: //setaio <char_name> [time](in days)");
					return false;
				}
				
			}
			case admin_removeaio:
			{
				
				boolean no_token = false;
				
				if (st.hasMoreTokens())
				{ // char_name
				
					final String char_name = st.nextToken();
					
					final L2PcInstance player = L2World.getInstance().getPlayer(char_name);
					
					if (player != null)
					{
						
						removeAio(activeChar, player, char_name);
						
						if (!player.isAio())
							return true;
						
					}
					else
					{
						
						activeChar.sendMessage("Player must be online to remove AIO status");
						no_token = true;
					}
					
				}
				else
				{
					no_token = true;
				}
				
				if (no_token)
				{
					activeChar.sendMessage("Usage: //removeaio <char_name>");
					return false;
				}
				
			}
		}
		
		return true;
		
	}
	
	public void doAio(final L2PcInstance activeChar, final L2PcInstance _player, final String _playername, final String _time)
	{
		final int days = Integer.parseInt(_time);
		if (_player == null)
		{
			activeChar.sendMessage("not found char" + _playername);
			return;
		}
		
		if (days > 0)
		{
			_player.setAio(true);
			_player.setEndTime("aio", days);
			_player.getStat().addExp(_player.getStat().getExpForLevel(81));
			
			Connection connection = null;
			try
			{
				connection = L2DatabaseFactory.getInstance().getConnection(false);
				
				final PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE obj_id=?");
				statement.setLong(1, _player.getAioEndTime());
				statement.setInt(2, _player.getObjectId());
				statement.execute();
				DatabaseUtils.close(statement);
				connection.close();
				
				if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
					_player.getAppearance().setNameColor(Config.AIO_NCOLOR);
				
				if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
					_player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
				
				_player.rewardAioSkills();
				_player.broadcastUserInfo();
				_player.sendPacket(new EtcStatusUpdate(_player));
				_player.sendSkillList();
				GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " set Aio stat for player " + _playername + " for " + _time + " day(s)");
				_player.sendMessage("You are now an Aio, Congratulations!");
				_player.broadcastUserInfo();
			}
			catch (final Exception e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
				
				LOGGER.warn("could not set Aio stats to char:", e);
			}
			finally
			{
				CloseUtil.close(connection);
			}
		}
		else
		{
			removeAio(activeChar, _player, _playername);
		}
	}
	
	public void removeAio(final L2PcInstance activeChar, final L2PcInstance _player, final String _playername)
	{
		_player.setAio(false);
		_player.setAioEndTime(0);
		
		Connection connection = null;
		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection(false);
			
			final PreparedStatement statement = connection.prepareStatement("UPDATE characters SET Aio=0, Aio_end=0 WHERE obj_id=?");
			statement.setInt(1, _player.getObjectId());
			statement.execute();
			DatabaseUtils.close(statement);
			connection.close();
			
			_player.lostAioSkills();
			_player.getAppearance().setNameColor(0xFFFFFF);
			_player.getAppearance().setTitleColor(0xFFFFFF);
			_player.broadcastUserInfo();
			_player.sendPacket(new EtcStatusUpdate(_player));
			_player.sendSkillList();
			GmListTable.broadcastMessageToGMs("GM " + activeChar.getName() + " remove Aio stat of player " + _playername);
			_player.sendMessage("Now You are not an Aio..");
			_player.broadcastUserInfo();
		}
		catch (final Exception e)
		{
			if (Config.DEBUG)
				e.printStackTrace();
			
			LOGGER.warn("could not remove Aio stats of char:", e);
		}
		finally
		{
			CloseUtil.close(connection);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}