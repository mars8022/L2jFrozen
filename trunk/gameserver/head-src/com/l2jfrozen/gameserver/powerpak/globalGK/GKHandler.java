package com.l2jfrozen.gameserver.powerpak.globalGK;

import com.l2jfrozen.gameserver.GameTimeController;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2jfrozen.gameserver.datatables.sql.TeleportLocationTable;
import com.l2jfrozen.gameserver.handler.IBBSHandler;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Broadcast;

public class GKHandler implements IVoicedCommandHandler,ICustomByPassHandler, IBBSHandler
{
	private class EscapeFinalizer implements Runnable
	{
		L2PcInstance _player;
		L2TeleportLocation _tp;
		public EscapeFinalizer(L2PcInstance player, L2TeleportLocation loc )
		{
			_player = player;
			_tp = loc;
		}
		public void run()
		{
			_player.enableAllSkills();
			_player.teleToLocation(_tp.getLocX(),_tp.getLocY(),_tp.getLocZ());
		}

	}
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] {PowerPakConfig.GLOBALGK_COMMAND};
	}
	private boolean checkAllowed(L2PcInstance activeChar)
	{
		String msg = null;
		if(activeChar.isSitting())
			msg = "Гейткипер не доступен когда вы сидите";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("ALL"))
			msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = "Гейткипер не доступен с проклятым оружием"; 
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = "Гейткипер не доступен во время боя";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = "Гейткипер не доступен в катакомбах и некрополисах";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
				msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("OLYMPIAD") && activeChar.isInOlympiadMode())
			msg = "Гейткипер не доступен в данной зоне";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("EVENT") && 
				activeChar._inEvent )
			msg = "Гейткипер не доступен на эвенте";

		if(msg!=null)
			activeChar.sendMessage(msg);

		return msg==null;
	}

	@Override
	public boolean useVoicedCommand(String cmd, L2PcInstance player, String params)
	{
		if(player==null)
			return false;

		if(!checkAllowed(player))
			return false;

		String text = HtmCache.getInstance().getHtm("data/html/gatekeeper/70023.htm");
		player.sendPacket(new NpcHtmlMessage(5,text));
		return false;
	}

	@Override
	public String[] getByPassCommands()
	{
		// TODO Auto-generated method stub
		return new String [] {"dotele" };
	}

	@Override
	public void handleCommand(String command, L2PcInstance player,
			String parameters)
	{
		if(player==null)
			return;

		if(!checkAllowed(player))
			return;

		String htm = "70023";
		if(parameters.startsWith("goto"))
		{
			try
			{
				if(PowerPakConfig.GLOBALGK_PRICE>0)
				{
					if(player.getAdena() < PowerPakConfig.GLOBALGK_PRICE)
					{
						player.sendMessage("У вас не хватает аден для оплаты услуги");
						return;
					}
					player.reduceAdena("teleport", PowerPakConfig.GLOBALGK_PRICE, null, true);
				}
 				int locId = Integer.parseInt(parameters.substring(parameters.indexOf(" ") + 1).trim());
				L2TeleportLocation tpPoint = TeleportLocationTable.getInstance().getTemplate(locId); 
				if(tpPoint!=null)
				{
					if(PowerPakConfig.GLOBALGK_PRICE==-1)
					{
						if(player.getAdena()< tpPoint.getPrice())
						{
							player.sendMessage("У вас не хватает аден для оплаты услуги");
							return;
						}
						player.reduceAdena("teleport", tpPoint.getPrice(), null, true);
					}
					int unstuckTimer = PowerPakConfig.GLOBALGK_TIMEOUT*1000; 
					player.setTarget(player);
					player.disableAllSkills();
					MagicSkillUser u = new MagicSkillUser(player, 1050, 1, unstuckTimer, 0);
					Broadcast.toSelfAndKnownPlayersInRadius(player, u, 810000/*900*/);
					SetupGauge sg = new SetupGauge(0, unstuckTimer);
					player.sendPacket(sg);
					EscapeFinalizer e = new EscapeFinalizer(player,tpPoint);
					player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(e, unstuckTimer));
					player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer/GameTimeController.MILLIS_IN_TICK);
					return;
				}
				else 
					player.sendMessage("Телепорта с ID  "+locId+" не существует в базе");
			}
			catch(Exception e)
			{
				player.sendMessage("Ошибка... возможно вы читер..");
			}
		} 
		else if(parameters.startsWith("Chat"))
		{
			htm = htm + "-" + parameters.substring(parameters.indexOf(" ") + 1).trim();
		}

		if(htm.contains("-0"))
			htm = "70023";
		String text = HtmCache.getInstance().getHtm("data/html/gatekeeper/" + htm + ".htm");

		if(command.startsWith("bbs"))
		{
			text = text.replace("-h custom_do", "bbs_bbs");
			BaseBBSManager.separateAndSend(text, player);
		}
		else
			player.sendPacket(new NpcHtmlMessage(5,text));
		return;

	}

	@Override
	public String[] getBBSCommands()
	{
		return new String [] {"bbstele" };
	}

}
