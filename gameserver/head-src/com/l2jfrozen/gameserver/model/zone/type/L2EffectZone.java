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
package com.l2jfrozen.gameserver.model.zone.type;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.StringUtil;
import com.l2jfrozen.util.random.Rnd;

/**
 * another type of damage zone with skills
 * @author kerberos
 */
public class L2EffectZone extends L2ZoneType
{
	public static final Logger LOGGER = Logger.getLogger(L2EffectZone.class);
	
	private int _chance;
	private int _initialDelay;
	private int _reuse;
	private boolean _enabled;
	private boolean _isShowDangerIcon;
	private volatile Future<?> _task;
	protected volatile FastMap<Integer, Integer> _skills;
	
	public L2EffectZone(final int id)
	{
		super(id);
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		_isShowDangerIcon = true;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		switch (name)
		{
			case "chance":
				_chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				_initialDelay = Integer.parseInt(value);
				break;
			case "defaultStatus":
				_enabled = Boolean.parseBoolean(value);
				break;
			case "reuse":
				_reuse = Integer.parseInt(value);
				break;
			case "skillIdLvl":
				final String[] propertySplit = value.split(";");
				_skills = new FastMap<>(propertySplit.length);
				for (final String skill : propertySplit)
				{
					final String[] skillSplit = skill.split("-");
					if (skillSplit.length != 2)
						LOGGER.warn(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skill, "\""));
					else
					{
						try
						{
							_skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (final NumberFormatException nfe)
						{
							if (!skill.isEmpty())
								LOGGER.warn(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"", skillSplit[1]));
						}
					}
				}
				break;
			case "showDangerIcon":
				_isShowDangerIcon = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (_skills != null)
		{
			if (_task == null)
			{
				synchronized (this)
				{
					if (_task == null)
						_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(), _initialDelay, _reuse);
				}
			}
		}
		
		if (character instanceof L2PcInstance && _isShowDangerIcon)
		{
			character.setInsideZone(L2Character.ZONE_DANGERAREA, true);
			character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2PcInstance && _isShowDangerIcon)
		{
			character.setInsideZone(L2Character.ZONE_DANGERAREA, false);
			if (!character.isInsideZone(L2Character.ZONE_DANGERAREA))
				character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
		
		if (_characterList.isEmpty() && _task != null)
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	protected L2Skill getSkill(final int skillId, final int skillLvl)
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void addSkill(final int skillId, final int skillLvL)
	{
		if (skillLvL < 1) // remove skill
		{
			removeSkill(skillId);
			return;
		}
		
		if (_skills == null)
		{
			synchronized (this)
			{
				if (_skills == null)
					_skills = new FastMap<Integer, Integer>(3).shared();
			}
		}
		_skills.put(skillId, skillLvL);
	}
	
	public void removeSkill(final int skillId)
	{
		if (_skills != null)
			_skills.remove(skillId);
	}
	
	public void clearSkills()
	{
		if (_skills != null)
			_skills.clear();
	}
	
	public int getSkillLevel(final int skillId)
	{
		if (_skills == null || !_skills.containsKey(skillId))
			return 0;
		return _skills.get(skillId);
	}
	
	public void setZoneEnabled(final boolean val)
	{
		_enabled = val;
	}
	
	protected Collection<L2Character> getCharacterList()
	{
		return _characterList.values();
	}
	
	class ApplySkill implements Runnable
	{
		ApplySkill()
		{
			if (_skills == null)
				throw new IllegalStateException("No skills defined.");
		}
		
		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (final L2Character temp : L2EffectZone.this.getCharacterList())
				{
					
					if (temp != null && !temp.isDead())
					{
						if (!(temp instanceof L2PlayableInstance)) // effect on zones are just applied to Playable Instances
							continue;
						
						if (Rnd.get(100) < getChance())
						{
							for (final Entry<Integer, Integer> e : _skills.entrySet())
							{
								final L2Skill skill = getSkill(e.getKey(), e.getValue());
								
								if (skill == null)
								{
									LOGGER.warn("ATTENTION: Skill " + e.getKey() + " cannot be loaded.. Verify Skill definition into data/stats/skill folder...");
									continue;
								}
								
								if (skill.checkCondition(temp, temp, false))
									if (temp.getFirstEffect(e.getKey()) == null)
									{
										
										skill.getEffects(temp, temp);
										
									}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
}