/* L2jFrozen Project - www.l2jfrozen.com 
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

/**
 * Format: ch Sd.
 * @author KenM
 */
public class ExDuelAskStart extends L2GameServerPacket
{
	
	/** The Constant _S__FE_4B_EXDUELASKSTART. */
	private static final String _S__FE_4B_EXDUELASKSTART = "[S] FE:4B ExDuelAskStart";
	
	/** The _requestor name. */
	private final String _requestorName;
	
	/** The _party duel. */
	private final int _partyDuel;
	
	/**
	 * Instantiates a new ex duel ask start.
	 * @param requestor the requestor
	 * @param partyDuel the party duel
	 */
	public ExDuelAskStart(final String requestor, final int partyDuel)
	{
		_requestorName = requestor;
		_partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4b);
		
		writeS(_requestorName);
		writeD(_partyDuel);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return _S__FE_4B_EXDUELASKSTART;
	}
}
