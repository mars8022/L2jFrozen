/*
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

import javolution.util.FastList;

/**
 * @author Dezmond_snz Format: cdddsdd
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private static final String _S__ED_CONFIRMDLG = "[S] ed ConfirmDlg";
	private int _requestId;
	///////
	private FastList<Object> _S = new FastList<Object>();

	public ConfirmDlg(int requestId)
	{
		_requestId = requestId;
	}

	public ConfirmDlg addString(String S)
	{
		_S.add(new String(S));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		/*
		 * "Nemu is making an attempt at resurrection. Do you want to continue with this resurrection?"
		 * ED //C
		 * E6 05 00 00 - Number of system messages int 1510 "$s1 is making an attempt at resurrection. Do you want to continue with this resurrection?"
		 * 02 00 00 00 00 00 00 - Size "attachments" ($S1, $S2, $S3, ...)
		 * 00 - unknown
		 * 00 4E 00 65 00 6D 00 75 00 00 - $S1 (custom string), in this case Nemu
		 * 00 06 00 00 - The response time for dialogue, you must specify in the constructor
		 * 00 - id, required to specify in the constructor assignment
		 */
		writeC(0xed); //ED
		writeD(_requestId); //id system message
		writeD(_S.size()); // size custom
		writeD(0x00); // unknown
		for(int i = 0; i < _S.size(); i++)
		{
			writeS((String) _S.get(i));
		}
		writeD(0x6000); // time
		writeD(0x00); // id?
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__ED_CONFIRMDLG;
	}
}
