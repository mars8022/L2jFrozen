package com.l2jfrozen.gameserver.thread.daemons;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author ProGramMoS
 */

public class PcPoint implements Runnable
{
	Logger LOGGER = Logger.getLogger(PcPoint.class);
	private static PcPoint _instance;
	
	public static PcPoint getInstance()
	{
		if (_instance == null)
		{
			_instance = new PcPoint();
		}
		
		return _instance;
	}
	
	private PcPoint()
	{
		LOGGER.info("PcBang point event started.");
	}
	
	@Override
	public void run()
	{
		
		int score = 0;
		for (L2PcInstance activeChar : L2World.getInstance().getAllPlayers())
		{
			
			if (activeChar.isOnline() == 1 && activeChar.getLevel() > Config.PCB_MIN_LEVEL && !activeChar.isInOfflineMode())
			{
				score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);
				
				if (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT)
				{
					score *= 2;
					
					activeChar.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_GOT_$51_GLASSES_PC);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;
					
					activeChar.updatePcBangWnd(score, true, true);
				}
				else
				{
					activeChar.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_RECEVIED_$51_GLASSES_PC);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;
					
					activeChar.updatePcBangWnd(score, true, false);
				}
			}
			
			activeChar = null;
		}
	}
}
