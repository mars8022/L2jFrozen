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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.CrestCache;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestSetPledgeCrest.class);
	
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length < 0 || _length > 256)
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS));
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 256)
		{
			activeChar.sendMessage("The clan crest file size was too big (max 256 bytes).");
			return;
		}
		
		if (_length == 0 || _data.length == 0)
		{
			CrestCache.getInstance().removePledgeCrest(clan.getCrestId());
			
			clan.setHasCrest(false);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED));
			
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
			
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (clan.getLevel() < 3)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST));
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			
			final int newId = IdFactory.getInstance().getNextId();
			
			if (clan.hasCrest())
			{
				crestCache.removePledgeCrest(newId);
			}
			
			if (!crestCache.savePledgeCrest(newId, _data))
			{
				LOGGER.warn("Error loading crest of clan:" + clan.getName());
				return;
			}
			
			Connection con = null;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
				statement.setInt(1, newId);
				statement.setInt(2, clan.getClanId());
				statement.executeUpdate();
				DatabaseUtils.close(statement);
				
				statement = null;
			}
			catch (final SQLException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.warn("could not update the crest id:" + e.getMessage());
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
			
			clan.setCrestId(newId);
			clan.setHasCrest(true);
			
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
			
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 53 RequestSetPledgeCrest";
	}
}
