/* L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.taskmanager.tasks;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jfrozen.gameserver.taskmanager.Task;
import com.l2jfrozen.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public class TaskJython extends Task
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskJython.class);
	public static final String NAME = "jython";
	
	private final PythonInterpreter _python = new PythonInterpreter();
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(final ExecutedTask task)
	{
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile("data/scripts/cron/" + task.getParams()[2]);
		LOGGER.info("[GlobalTask] Python Cleanup launched.");
	}
	
}