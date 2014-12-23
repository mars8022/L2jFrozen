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
package com.l2jfrozen.gameserver.scripting;

import java.io.File;

import javax.script.ScriptException;

import com.l2jfrozen.Config;

/**
 * Abstract class for classes that are meant to be implemented by scripts.
 * @author KenM
 */
public abstract class ManagedScript
{
	private final File _scriptFile;
	private long _lastLoadTime;
	private boolean _isActive;
	
	public ManagedScript()
	{
		_scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadScript();
		setLastLoadTime(System.currentTimeMillis());
	}
	
	/**
	 * Attempts to reload this script and to refresh the necessary bindings with it ScriptControler.<BR>
	 * Subclasses of this class should override this method to properly refresh their bindings when necessary.
	 * @return true if and only if the script was reloaded, false otherwise.
	 */
	public boolean reload()
	{
		try
		{
			L2ScriptEngineManager.getInstance().executeScript(getScriptFile());
			return true;
		}
		catch (final ScriptException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			return false;
		}
	}
	
	public abstract boolean unload();
	
	public void setActive(final boolean status)
	{
		_isActive = status;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	/**
	 * @return Returns the scriptFile.
	 */
	public File getScriptFile()
	{
		return _scriptFile;
	}
	
	/**
	 * @param lastLoadTime The lastLoadTime to set.
	 */
	protected void setLastLoadTime(final long lastLoadTime)
	{
		_lastLoadTime = lastLoadTime;
	}
	
	/**
	 * @return Returns the lastLoadTime.
	 */
	protected long getLastLoadTime()
	{
		return _lastLoadTime;
	}
	
	public abstract String getScriptName();
	
	public abstract ScriptManager<?> getScriptManager();
}
