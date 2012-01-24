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

package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.taskmanager.DecayTaskManager;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public final class L2GourdInstance extends L2MonsterInstance
{
	//private static Logger _log = Logger.getLogger(L2GourdInstance.class.getName());

	private String _name;
	private byte _nectar = 0;
	private byte _good = 0;

	public L2GourdInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		DecayTaskManager.getInstance().addDecayTask(this, 180000);
	}

	public void setOwner(String name)
	{
		_name = name;
	}

	public String getOwner()
	{
		return _name;
	}

	public void addNectar()
	{
		_nectar++;
	}

	public byte getNectar()
	{
		return _nectar;
	}

	public void addGood()
	{
		_good++;
	}

	public byte getGood()
	{
		return _good;
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if(!attacker.getName().equalsIgnoreCase(getOwner()))
		{
			damage = 0;
		}
		if(getTemplate().npcId == 12778 || getTemplate().npcId == 12779)
			if(attacker.getActiveWeaponInstance().getItemId() == 4202 || attacker.getActiveWeaponInstance().getItemId() == 5133 || attacker.getActiveWeaponInstance().getItemId() == 5817 || attacker.getActiveWeaponInstance().getItemId() == 7058)
			{
				super.reduceCurrentHp(damage, attacker, awake);
			}
			else if(damage > 0)
			{
				damage = 0;
			}
		super.reduceCurrentHp(damage, attacker, awake);
	}
}
