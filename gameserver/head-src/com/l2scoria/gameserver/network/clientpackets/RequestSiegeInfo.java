package com.l2scoria.gameserver.network.clientpackets;

/*
 * l2scoria dev
 */
public class RequestSiegeInfo extends L2GameClientPacket
{

	@Override
	public String getType()
	{
		return "[C] 0x47 RequestSiegeInfo";
	}

	@Override
	protected void readImpl()
	{
	// trigger 
	}

	@Override
	protected void runImpl()
	{
	// TODO this
	}

}
