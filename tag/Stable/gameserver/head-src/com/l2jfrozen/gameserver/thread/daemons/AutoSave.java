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
package com.l2jfrozen.gameserver.thread.daemons;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * <b>L2GameClient</b> only
 * 
 * @author programmos
 */
public class AutoSave implements Runnable
{
	protected static final Logger _log = Logger.getLogger(AutoSave.class.getName());
	
	private L2PcInstance _player;
	
	private static AutoSave _instance;
	public static AutoSave getInstance()
	{
		if(_instance==null)
			_instance = new AutoSave();
		
		return _instance;
	}
	
	public AutoSave(L2PcInstance player)
	{
		_player = player;
	}
	
	private AutoSave() //local
	{
		_log.info("Start auto save daemon.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			if(_player != null)
			{
				try
				{
					_player.store();
				}
				catch(Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					_log.log(Level.SEVERE, "Error saving player character: " + _player.getName(), e);
				}
			}

			_player = null;
		}
		catch(Throwable e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, e.getMessage());
		}
	}

}
