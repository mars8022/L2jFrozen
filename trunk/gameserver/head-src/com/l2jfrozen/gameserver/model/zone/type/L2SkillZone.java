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

package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

public class L2SkillZone extends L2ZoneType
{
	private int _skillId;
	private int _skillLvl;
	private boolean _onSiege;

	public L2SkillZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("skillId"))
		{
			_skillId = Integer.parseInt(value);
		}
		else if(name.equals("skillLvl"))
		{
			_skillLvl = Integer.parseInt(value);
		}
		else if(name.equals("onSiege"))
		{
			_onSiege = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if((character instanceof L2PcInstance || character instanceof L2SummonInstance) && (!_onSiege || _onSiege && character.isInsideZone(4)))
		{
			if(character instanceof L2PcInstance)
			{
				((L2PcInstance) character).enterDangerArea();
			}

			SkillTable.getInstance().getInfo(_skillId, _skillLvl).getEffects(character, character,false,false,false);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance || character instanceof L2SummonInstance)
		{
			character.stopSkillEffects(_skillId);

			if(character instanceof L2PcInstance)
			{
				((L2PcInstance) character).exitDangerArea();
			}
		}
	}

	@Override
	protected void onDieInside(L2Character character)
	{
		onExit(character);
	}

	@Override
	protected void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
}
