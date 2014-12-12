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

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0x00)(type)(speaker)(name)
 */

public class Snoop extends L2GameServerPacket
{
	private static final String _S__D5_SNOOP = "[S] D5 Snoop";
	private final L2PcInstance _snooped;
	private final int _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(final L2PcInstance snooped, final int type, final String speaker, final String msg)
	{
		_snooped = snooped;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xd5);
		writeD(_snooped.getObjectId());
		writeS(_snooped.getName());
		writeD(0); // ??
		writeD(_type);
		writeS(_speaker);
		writeS(_msg);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__D5_SNOOP;
	}
}