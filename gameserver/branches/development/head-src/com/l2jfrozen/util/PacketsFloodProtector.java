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
package com.l2jfrozen.util;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.loginserver.LoginController;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.netcore.Config;
import com.l2jfrozen.netcore.MMOClient;

/**
 * @author Enzo
 */
public class PacketsFloodProtector
{
	private final static int MAX_CONCURRENT_ACTIONS_PER_PLAYER = 10;
	
	private static Hashtable<String, AtomicInteger> clients_concurrent_actions = new Hashtable<String, AtomicInteger>();
	
	private static final Logger _log = Logger.getLogger(PacketsFloodProtector.class.getName());
	
	private static Hashtable<String, Hashtable<Integer, AtomicInteger>> clients_actions = new Hashtable<String, Hashtable<Integer, AtomicInteger>>();
	
	private static Hashtable<String, Hashtable<Integer, Integer>> clients_nextGameTick = new Hashtable<String, Hashtable<Integer, Integer>>();
	
	private static Hashtable<String, Boolean> punishes_in_progress = new Hashtable<String, Boolean>();
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param opcode
	 * @param opcode2
	 * @param client
	 * @return true if action is allowed, otherwise false
	 */
	public static boolean tryPerformAction(final int opcode, final int opcode2, MMOClient<?> client)
	{
		if (Config.getInstance().DISABLE_FULL_PACKETS_FLOOD_PROTECTOR)
			return true;
		
		// filter on opcodes
		if (!isOpCodeToBeTested(opcode, opcode2, client instanceof L2LoginClient))
			return true;
		
		String account = "";
		
		if (client instanceof L2LoginClient)
		{			
			L2LoginClient login_cl = (L2LoginClient) client;
			account = login_cl.getAccount();			
		}
		else if (client instanceof L2GameClient)
		{			
			L2GameClient game_cl = (L2GameClient) client;
			account = game_cl.accountName;			
		}
		
		if (account == null)
			return true;
		
		// get actual concurrent actions number for account
		AtomicInteger actions_per_account = clients_concurrent_actions.get(account);
		if (actions_per_account == null)
		{
			actions_per_account = new AtomicInteger(0);
		}
		if (actions_per_account.get() < MAX_CONCURRENT_ACTIONS_PER_PLAYER)
		{			
			int actions = actions_per_account.incrementAndGet();
			
			if (Config.getInstance().ENABLE_MMOCORE_DEBUG)
			{
				_log.info(" -- account " + account + " has performed " + actions + " concurrent actions until now");
			}
			
			clients_concurrent_actions.put(account, actions_per_account);
		}
		else
			return false;
		
		final int curTick = GameTimeController.getGameTicks();
		
		Hashtable<Integer, Integer> account_nextGameTicks = clients_nextGameTick.get(account);
		if (account_nextGameTicks == null)
		{
			account_nextGameTicks = new Hashtable<Integer, Integer>();
		}
		Integer _nextGameTick = account_nextGameTicks.get(opcode);
		if (_nextGameTick == null)
		{
			_nextGameTick = curTick;
			account_nextGameTicks.put(opcode, _nextGameTick);
		}
		clients_nextGameTick.put(account, account_nextGameTicks);
		
		Boolean _punishmentInProgress = punishes_in_progress.get(account);
		if (_punishmentInProgress == null)
		{
			_punishmentInProgress = false;
		}
		else if (_punishmentInProgress)
		{
			AtomicInteger actions = clients_concurrent_actions.get(account);
			actions.decrementAndGet();
			clients_concurrent_actions.put(account, actions);
			return false;
		}
		punishes_in_progress.put(account, _punishmentInProgress);
		
		Hashtable<Integer, AtomicInteger> received_commands_actions = clients_actions.get(account);
		if (received_commands_actions == null)
		{
			received_commands_actions = new Hashtable<Integer, AtomicInteger>();
		}
		AtomicInteger command_count = null;
		if ((command_count = received_commands_actions.get(opcode)) == null)
		{
			command_count = new AtomicInteger(0);
			received_commands_actions.put(opcode, command_count);
		}
		clients_actions.put(account, received_commands_actions);
		
		if (curTick <= _nextGameTick && !_punishmentInProgress) // time to check operations
		{
			command_count.incrementAndGet();
			clients_actions.get(account).put(opcode, command_count);
			
			if (Config.getInstance().ENABLE_MMOCORE_DEBUG)
			{
				_log.info("-- called OpCode " + Integer.toHexString(opcode) + " ~" + String.valueOf((Config.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK) + " ms after first command...");
				_log.info("   total received packets with OpCode " + Integer.toHexString(opcode) + " into the Interval: " + command_count.get());
			}
			
			if (Config.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT > 0 && command_count.get() >= Config.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT && Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE != null)
			{
				punishes_in_progress.put(account, true);
				
				if (!isOpCodeToBeTested(opcode, opcode2, client instanceof L2LoginClient))
				{					
					if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
						_log.warning("ATTENTION: Account " + account + " is flooding the server...");
					
					if ("kick".equals(Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
					{
						if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
							_log.warning(" ------- kicking account " + account);
						kickPlayer(client, opcode);
					}
					else if ("ban".equals(Config.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
					{
						if (Config.getInstance().LOG_PACKET_FLOODING && _log.isLoggable(Level.WARNING))
							_log.warning(" ------- banning account " + account);
						banAccount(client, opcode);
					}					
				}				
				// clear already punished account
				punishes_in_progress.remove(account);
				clients_nextGameTick.remove(account);
				clients_actions.remove(account);
				clients_concurrent_actions.remove(account);
				
				return false;				
			}
			
			if (curTick == _nextGameTick)
			{ // if is the first time, just calculate the next game tick
				_nextGameTick = curTick + Config.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL;
				clients_nextGameTick.get(account).put(opcode, _nextGameTick);
			}
			
			AtomicInteger actions = clients_concurrent_actions.get(account);
			actions.decrementAndGet();
			clients_concurrent_actions.put(account, actions);
			
			return true;			
		}
		punishes_in_progress.put(account, false);
		clients_nextGameTick.get(account).remove(opcode);
		clients_actions.get(account).remove(opcode);
		
		AtomicInteger actions = clients_concurrent_actions.get(account);
		actions.decrementAndGet();
		clients_concurrent_actions.put(account, actions);
		
		return true;		
	}
	
	private static boolean isOpCodeToBeTested(int opcode, int opcode2, boolean loginclient)
	{	
		if (loginclient)
		{			
			return !Config.getInstance().LS_LIST_PROTECTED_OPCODES.contains(opcode);			
		}
		
		if (opcode == 0xd0)
		{			
			if (Config.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode))
			{				
				return !Config.getInstance().GS_LIST_PROTECTED_OPCODES2.contains(opcode2);				
			}
			return true;
			
		}
		return !Config.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode);
	}
	
	/**
	 * Kick player from game (close network connection).
	 * @param _client
	 * @param opcode
	 */
	private static void kickPlayer(MMOClient<?> _client, int opcode)
	{
		if (_client instanceof L2LoginClient)
		{		
			L2LoginClient login_cl = (L2LoginClient) _client;
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			_log.warning("Player with account " + login_cl.getAccount() + " kicked for flooding with packet " + Integer.toHexString(opcode));			
		}
		else if (_client instanceof L2GameClient)
		{		
			L2GameClient game_cl = (L2GameClient) _client;
			game_cl.closeNow();
			
			_log.warning("Player with account " + game_cl.accountName + " kicked for flooding with packet " + Integer.toHexString(opcode));			
		}		
	}
	
	/**
	 * Bans char account and logs out the char.
	 * @param _client
	 * @param opcode
	 */
	private static void banAccount(MMOClient<?> _client, int opcode)
	{		
		if (_client instanceof L2LoginClient)
		{			
			L2LoginClient login_cl = (L2LoginClient) _client;
			LoginController.getInstance().setAccountAccessLevel(login_cl.getAccount(), -100);
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			_log.warning("Player with account " + login_cl.getAccount() + " banned for flooding forever with packet " + Integer.toHexString(opcode));			
		}
		else if (_client instanceof L2GameClient)
		{			
			L2GameClient game_cl = (L2GameClient) _client;
			
			if (game_cl.getActiveChar() != null)
			{
				game_cl.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.ACC, 0);			
				_log.warning("Player " + game_cl.getActiveChar() + " of account " + game_cl.accountName + " banned forever for flooding with packet " + Integer.toHexString(opcode));				
				game_cl.getActiveChar().logout();
			}
			
			game_cl.closeNow();
			_log.warning("Player with account " + game_cl.accountName + " kicked for flooding with packet " + Integer.toHexString(opcode));			
		}		
	}	
}