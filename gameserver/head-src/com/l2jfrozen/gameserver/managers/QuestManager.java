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
package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.scripting.L2ScriptEngineManager;
import com.l2jfrozen.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
	private Map<String, Quest> _quests = new FastMap<String, Quest>();
	private static QuestManager _instance;

	public static QuestManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new QuestManager();
		}
		return _instance;
	}

	public QuestManager()
	{
		_log.info("Initializing QuestManager");
	}

	public final boolean reload(String questFolder)
	{
		Quest q = getQuest(questFolder);
		if(q == null)
			return false;
		return q.reload();
	}

	/**
	 * Reloads a the quest given by questId.<BR>
	 * <B>NOTICE: Will only work if the quest name is equal the quest folder name</B>
	 * 
	 * @param questId The id of the quest to be reloaded
	 * @return true if reload was succesful, false otherwise
	 */
	public final boolean reload(int questId)
	{
		Quest q = this.getQuest(questId);
		if(q == null)
			return false;
		return q.reload();
	}

	public final void reloadAllQuests()
	{
		_log.info("Reloading Server Scripts");
		// unload all scripts
		for(Quest quest : _quests.values())
		{
			if(quest != null)
			{
				quest.unload();
			}
		}
		// now load all scripts
		File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
		L2ScriptEngineManager.getInstance().executeScriptsList(scripts);
		QuestManager.getInstance().report();
	}

	public final void report()
	{
		_log.info("Loaded: " + _quests.size() + " quests");
	}

	public final void save()
	{
		for(Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}

	// =========================================================
	// Property - Public
	public final Quest getQuest(String name)
	{
		return getQuests().get(name);
	}

	public final Quest getQuest(int questId)
	{
		for(Quest q : getQuests().values())
		{
			if(q.getQuestIntId() == questId)
				return q;
		}
		return null;
	}

	public final void addQuest(Quest newQuest)
	{
		if(getQuests().containsKey(newQuest.getName()))
		{
			_log.info("Replaced: " + newQuest.getName() + " with a new version");
		}

		// Note: FastMap will replace the old value if the key already exists
		// so there is no need to explicitly try to remove the old reference.
		getQuests().put(newQuest.getName(), newQuest);
	}

	public final FastMap<String, Quest> getQuests()
	{
		if(_quests == null)
		{
			_quests = new FastMap<String, Quest>();
		}

		return (FastMap<String, Quest>) _quests;
	}

	/**
	 * This will reload quests
	 */
	public static void reload()
	{
		_instance = new QuestManager();
	}

	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}

	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}

	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}

	public final boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
}
