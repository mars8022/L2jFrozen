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
package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Vector;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.SystemMessageId;

public final class SystemMessage extends L2GameServerPacket
{
	// Packets d d (d S/d d/d dd) -> 0 - String 1-number 2-textref npcname (1000000-1002655) 3-textref itemname 4-textref skills 5-??
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private final int _messageId;
	private final Vector<Integer> _types = new Vector<>();
	private final Vector<Object> _values = new Vector<>();
	private int _skillLvL = 1;
	
	public SystemMessage(final SystemMessageId messageId)
	{
		if (Config.DEBUG && messageId == SystemMessageId.TARGET_IS_INCORRECT)
		{
			Thread.dumpStack();
		}
		_messageId = messageId.getId();
	}
	
	@Deprecated
	public SystemMessage(final int messageId)
	{
		_messageId = messageId;
	}
	
	public static SystemMessage sendString(final String msg)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		sm.addString(msg);
		
		return sm;
	}
	
	public SystemMessage addString(final String text)
	{
		_types.add(new Integer(TYPE_TEXT));
		_values.add(text);
		
		return this;
	}
	
	public SystemMessage addNumber(final int number)
	{
		_types.add(new Integer(TYPE_NUMBER));
		_values.add(new Integer(number));
		return this;
	}
	
	public SystemMessage addNpcName(final int id)
	{
		_types.add(new Integer(TYPE_NPC_NAME));
		_values.add(new Integer(1000000 + id));
		
		return this;
	}
	
	public SystemMessage addItemName(final int id)
	{
		_types.add(new Integer(TYPE_ITEM_NAME));
		_values.add(new Integer(id));
		
		return this;
	}
	
	public SystemMessage addZoneName(final int x, final int y, final int z)
	{
		_types.add(new Integer(TYPE_ZONE_NAME));
		final int[] coord =
		{
			x,
			y,
			z
		};
		_values.add(coord);
		
		return this;
	}
	
	public SystemMessage addSkillName(final int id)
	{
		return addSkillName(id, 1);
	}
	
	public SystemMessage addSkillName(final int id, final int lvl)
	{
		_types.add(new Integer(TYPE_SKILL_NAME));
		_values.add(new Integer(id));
		_skillLvL = lvl;
		
		return this;
	}
	
	public void addSkillName(final L2Skill skill)
	{
	} // Check this
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x64);
		
		writeD(_messageId);
		writeD(_types.size());
		
		for (int i = 0; i < _types.size(); i++)
		{
			final int t = _types.get(i).intValue();
			
			writeD(t);
			
			switch (t)
			{
				case TYPE_TEXT:
				{
					writeS((String) _values.get(i));
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					final int t1 = ((Integer) _values.get(i)).intValue();
					writeD(t1);
					break;
				}
				case TYPE_SKILL_NAME:
				{
					final int t1 = ((Integer) _values.get(i)).intValue();
					writeD(t1); // Skill Id
					writeD(_skillLvL); // Skill lvl
					break;
				}
				case TYPE_ZONE_NAME:
				{
					final int t1 = ((int[]) _values.get(i))[0];
					final int t2 = ((int[]) _values.get(i))[1];
					final int t3 = ((int[]) _values.get(i))[2];
					writeD(t1);
					writeD(t2);
					writeD(t3);
					break;
				}
			}
		}
	}
	
	public int getMessageID()
	{
		return _messageId;
	}
	
	public static SystemMessage getSystemMessage(final SystemMessageId smId)
	{
		final SystemMessage sm = new SystemMessage(smId);
		return sm;
	}
	
	@Override
	public String getType()
	{
		return "[S] 64 SystemMessage";
	}
}