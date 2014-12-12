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
package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author eX1steam L2JFrozen
 */
public class ConditionTargetPvp extends Condition
{
	
	private final int _pvp;
	
	public ConditionTargetPvp(final int pvp)
	{
		_pvp = pvp;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final L2Character target = env.target;
		if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() != 0)
			return ((L2PcInstance) target).getPvpFlag() == _pvp;
		return false;
	}
}
