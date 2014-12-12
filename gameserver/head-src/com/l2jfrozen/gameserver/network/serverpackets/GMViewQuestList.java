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

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;

/**
 * Sh (dd) h (dddd)
 * @author Tempy
 */
public class GMViewQuestList extends L2GameServerPacket
{
	private static final String _S__AC_GMVIEWQUESTLIST = "[S] ac GMViewQuestList";
	
	private final L2PcInstance _activeChar;
	
	public GMViewQuestList(final L2PcInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x93);
		writeS(_activeChar.getName());
		
		final Quest[] questList = _activeChar.getAllActiveQuests();
		
		if (questList.length == 0)
		{
			writeC(0);
			writeH(0);
			writeH(0);
			return;
		}
		
		writeH(questList.length); // quest count
		
		for (final Quest q : questList)
		{
			writeD(q.getQuestIntId());
			
			final QuestState qs = _activeChar.getQuestState(q.getName());
			
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			writeD(qs.getInt("cond")); // stage of quest progress
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__AC_GMVIEWQUESTLIST;
	}
}
