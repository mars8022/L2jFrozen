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
package interlude.gameserver.model.quest.jython;

import interlude.gameserver.model.quest.Quest;

public abstract class QuestJython extends Quest
{
	/**
	 * Constructor used in jython files.
	 *
	 * @param questId
	 *            : int designating the ID of the quest
	 * @param name
	 *            : String designating the name of the quest
	 * @param descr
	 *            : String designating the description of the quest
	 */
	public QuestJython(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
}