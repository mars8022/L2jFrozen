/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfrozen.gameserver.util;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance.PunishLevel;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.util.StringUtil;


/**
 * Flood protector implementation.
 * 
 * @author fordfrog
 */
public final class FloodProtectorAction
{
	/**
	 * Logger
	 */
	private static final Logger _log = Logger.getLogger(FloodProtectorAction.class.getName());
	/**
	 * Client for this instance of flood protector.
	 */
	private final L2GameClient _client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig _config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile float _nextGameTick = GameTimeController.getGameTicks();
	/**
	 * Request counter.
	 */
	private AtomicInteger _count = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply
	 * punisment multiple times (flooding).
	 */
	private volatile boolean _punishmentInProgress;
	
	/**
	 * Creates new instance of FloodProtectorAction.
	 * 
	 * @param client for which flood protection is being created
	 * @param config flood protector configuration
	 */
	public FloodProtectorAction(final L2GameClient client, final FloodProtectorConfig config)
	{
		super();
		_client = client;
		_config = config;
	}

	private Hashtable<String, AtomicInteger> received_commands_actions = new Hashtable<String, AtomicInteger>();
	
	/**
	 * Checks whether the request is flood protected or not.
	 * 
	 * @param command
	 *            command issued or short command description
	 * 
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(final String command)
	{
		if(!_config.ALTERNATIVE_METHOD){
			
			final int curTick = GameTimeController.getGameTicks();
			
			if (curTick < _nextGameTick || _punishmentInProgress)
			{
				if (_config.LOG_FLOODING && !_logged && _log.isLoggable(Level.WARNING))
				{
					log(" called command ", command, " ~", String.valueOf((_config.FLOOD_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK), " ms after previous command");
					_logged = true;
				}
				
				_count.incrementAndGet();
				
				if (!_punishmentInProgress && _config.PUNISHMENT_LIMIT > 0 && _count.get() >= _config.PUNISHMENT_LIMIT && _config.PUNISHMENT_TYPE != null)
				{
					_punishmentInProgress = true;
					
					if ("kick".equals(_config.PUNISHMENT_TYPE))
					{
						kickPlayer();
					}
					else if ("ban".equals(_config.PUNISHMENT_TYPE))
					{
						banAccount();
					}
					else if ("jail".equals(_config.PUNISHMENT_TYPE))
					{
						jailChar();
					}
					else if ("banchat".equals(_config.PUNISHMENT_TYPE))
					{
						banChat();
					}
					
					_punishmentInProgress = false;
				}
				
				return false;
			}
			
			if (_count.get() > 0)
			{
				if (_config.LOG_FLOODING && _log.isLoggable(Level.WARNING))
				{
					log(" issued ", String.valueOf(_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK), " ms");
				}
			}
			
			_nextGameTick = curTick + _config.FLOOD_PROTECTION_INTERVAL;
			_logged = false;
			_count.set(0);
			
			return true;
			
		}
		
		final int curTick = GameTimeController.getGameTicks();
		
		if (curTick < _nextGameTick || _punishmentInProgress)
		{
			if (_config.LOG_FLOODING && !_logged && _log.isLoggable(Level.WARNING))
			{
				log(" called command ", command, " ~", String.valueOf((_config.FLOOD_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK), " ms after previous command");
				_logged = true;
			}
			
			AtomicInteger command_count = null;
			if((command_count = received_commands_actions.get(command))==null){
				command_count = new AtomicInteger(0);
				//received_commands_actions.put(command, command_count);
			}
			
			int count = command_count.incrementAndGet();
			received_commands_actions.put(command, command_count);
			
			//_count.incrementAndGet();
			
			if (!_punishmentInProgress && _config.PUNISHMENT_LIMIT > 0 && count >= _config.PUNISHMENT_LIMIT && _config.PUNISHMENT_TYPE != null)
			{
				_punishmentInProgress = true;
				
				if ("kick".equals(_config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if ("ban".equals(_config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if ("jail".equals(_config.PUNISHMENT_TYPE))
				{
					jailChar();
				}
				
				_punishmentInProgress = false;
			}
			
			return false;
		}
		
		AtomicInteger command_count = null;
		if((command_count = received_commands_actions.get(command))==null){
			command_count = new AtomicInteger(0);
			received_commands_actions.put(command, command_count);
		}
		
		if (command_count.get() > 0)
		{
			if (_config.LOG_FLOODING && _log.isLoggable(Level.WARNING))
			{
				log(" issued ", String.valueOf(command_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK), " ms");
			}
		}
		
		_nextGameTick = curTick + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		command_count.set(0);
		received_commands_actions.put(command, command_count);
		
		return true;
		
	}

	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer()
	{
		if (_client.getActiveChar() != null)
			_client.getActiveChar().logout();
		else
			_client.closeNow();

		log("Client "+_client.toString()+" kicked for flooding");
		
	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void banChat()
	{
		if (_client.getActiveChar() != null){
			
			final L2PcInstance activeChar = _client.getActiveChar();
			
			long newChatBanTime = 60000; //1 minute
			if (activeChar.getPunishLevel() == PunishLevel.CHAT)
			{
				if(activeChar.getPunishTimer() <= (60000*3)){ //if less then 3 minutes (MAX CHAT BAN TIME), add 1 minute
					newChatBanTime+=activeChar.getPunishTimer();
				}else
					newChatBanTime=activeChar.getPunishTimer();
				
			}
			
			activeChar.setPunishLevel(PunishLevel.CHAT, newChatBanTime);
			
		}
		
	}

	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		if (_client.getActiveChar() != null)
		{
			_client.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.ACC, _config.PUNISHMENT_TIME);

			if (_log.isLoggable(Level.WARNING))
			{
				log(" banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
			}

			_client.getActiveChar().logout();
		}
		else
			log(" unable to ban account: no active player");
	}
	
	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		if (_client.getActiveChar() != null)
		{
			_client.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.JAIL, _config.PUNISHMENT_TIME);
			
			if (_log.isLoggable(Level.WARNING))
			{
				log(" jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
			}
		}
		else
			log(" unable to jail: no active player");
	}

	private void log(String... lines)
	{
		final StringBuilder output = StringUtil.startAppend(100, _config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if (!_client.isDetached())
				address = _client.getConnection().getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
		}

		switch (_client.getState())
		{
			case IN_GAME:
				if (_client.getActiveChar() != null)
				{
					StringUtil.append(output, _client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(_client.getActiveChar().getObjectId()),") ");
				}
			case AUTHED:
				if (_client.getAccountName() != null)
					StringUtil.append(output, _client.getAccountName()," ");
			case CONNECTED:
				if (address != null)
					StringUtil.append(output, address);
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}

		StringUtil.append(output, lines);
		_log.warning(output.toString());
	}
}