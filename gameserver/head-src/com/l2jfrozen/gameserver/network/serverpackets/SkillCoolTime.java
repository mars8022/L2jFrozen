package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Iterator;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;

public class SkillCoolTime extends L2GameServerPacket
{
	
	@SuppressWarnings("rawtypes")
	public Collection _reuseTimeStamps;
	
	public SkillCoolTime(L2PcInstance cha)
	{
		_reuseTimeStamps = cha.getReuseTimeStamps();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected final void writeImpl()
	{
		@SuppressWarnings("cast")
		L2PcInstance activeChar = ((L2GameClient) getClient()).getActiveChar();
		if (activeChar == null)
			return;
		writeC(193);
		writeD(_reuseTimeStamps.size());
		L2PcInstance.TimeStamp ts;
		for (Iterator i$ = _reuseTimeStamps.iterator(); i$.hasNext(); writeD((int) ts.getRemaining() / 1000))
		{
			ts = (L2PcInstance.TimeStamp) i$.next();
			writeD(ts.getSkill());
			writeD(0);
			writeD((int) ts.getReuse() / 1000);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] c1 SkillCoolTime";
	}
}