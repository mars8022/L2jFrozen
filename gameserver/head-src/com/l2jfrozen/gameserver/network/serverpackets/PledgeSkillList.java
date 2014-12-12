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
package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * Format: (ch) d [dd].
 * @author -Wooden-
 */
public class PledgeSkillList extends L2GameServerPacket
{
	/** The Constant _S__FE_39_PLEDGESKILLLIST. */
	private static final String _S__FE_39_PLEDGESKILLLIST = "[S] FE:39 PledgeSkillList";
	
	/** The _clan. */
	private final L2Clan _clan;
	
	/**
	 * Instantiates a new pledge skill list.
	 * @param clan the clan
	 */
	public PledgeSkillList(final L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		final L2Skill[] skills = _clan.getAllSkills();
		
		writeC(0xfe);
		writeH(0x39);
		writeD(skills.length);
		for (final L2Skill sk : skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return _S__FE_39_PLEDGESKILLLIST;
	}
}
