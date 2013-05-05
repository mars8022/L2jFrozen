/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfrozen.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import com.l2jfrozen.gameserver.taskmanager.Task;
import com.l2jfrozen.gameserver.taskmanager.TaskManager;
import com.l2jfrozen.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jfrozen.gameserver.taskmanager.TaskTypes;

/**
 * @author Tempy
 */
public final class TaskCleanUp extends Task
{
	private static final Logger _log = Logger.getLogger(TaskCleanUp.class.getName());
	public static final String NAME = "CleanUp";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		System.runFinalization();
		System.gc();
		_log.info("Java Memory Cleanup Global Task: launched.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "3600000", "");
	}
}