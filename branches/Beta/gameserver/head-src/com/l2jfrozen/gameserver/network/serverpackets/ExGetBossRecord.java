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

import java.util.Map;

/**
 * Format: ch ddd [ddd].
 *
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket
{
	
	/** The Constant _S__FE_33_EXGETBOSSRECORD. */
	private static final String _S__FE_33_EXGETBOSSRECORD = "[S] FE:33 ExGetBossRecord";
	
	/** The _boss record info. */
	private final Map<Integer, Integer>	_bossRecordInfo;
	
	/** The _ranking. */
	private final int						_ranking;
	
	/** The _total points. */
	private final int						_totalPoints;

	/**
	 * Instantiates a new ex get boss record.
	 *
	 * @param ranking the ranking
	 * @param totalScore the total score
	 * @param list the list
	 */
	public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = list;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x33);
		writeD(_ranking);
		writeD(_totalPoints);
		if(_bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(_bossRecordInfo.size());
			for (int bossId : _bossRecordInfo.keySet())
			{
				writeD(bossId);
				writeD(_bossRecordInfo.get(bossId));
				writeD(0x00); //??
			}
		}
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return _S__FE_33_EXGETBOSSRECORD;
	}
}
