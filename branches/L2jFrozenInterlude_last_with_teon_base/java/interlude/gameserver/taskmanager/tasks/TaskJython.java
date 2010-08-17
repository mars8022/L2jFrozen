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
package interlude.gameserver.taskmanager.tasks;

import interlude.gameserver.taskmanager.Task;
import interlude.gameserver.taskmanager.TaskManager.ExecutedTask;

import org.python.util.PythonInterpreter;

/**
 * @author Layane
 */
public class TaskJython extends Task
{
	public static final String NAME = "jython";
	private final PythonInterpreter _python = new PythonInterpreter();

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.taskmanager.Task#onTimeElapsed(interlude.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile("data/scripts/cron/" + task.getParams()[2]);
	}
}
