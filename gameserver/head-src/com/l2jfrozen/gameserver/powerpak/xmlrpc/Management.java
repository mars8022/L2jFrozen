/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.powerpak.xmlrpc;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.l2jfrozen.gameserver.Shutdown;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.util.Memory;

/**
 * @author Shyla
 */
public class Management
{
	
	public void restartServer(final int seconds)
	{
		
		Shutdown.getInstance().startShutdown(null, seconds, true);
		
	}
	
	public void abortServerShutdown()
	{
		
		Shutdown.getInstance().abort(null);
		
	}
	
	public void shutdownServer(final int seconds)
	{
		
		Shutdown.getInstance().startShutdown(null, seconds, false);
		
	}
	
	public String getServerStats()
	{
		
		final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
		final StringBuilder sb = new StringBuilder();
		sb.append("Server Time: " + fmt.format(new Date(System.currentTimeMillis())));
		sb.append("Players Online: " + L2World.getInstance().getAllPlayers().size());
		sb.append("Threads: " + Thread.activeCount());
		sb.append("Free Memory: " + Memory.getFreeMemory() + " MB");
		sb.append("Used memory: " + Memory.getUsedMemory() + " MB");
		
		return sb.toString();
		
	}
	
}
