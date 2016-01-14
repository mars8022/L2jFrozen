package com.l2jfrozen.gameserver.powerpak.globalGK;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.sql.TeleportLocationTable;
import com.l2jfrozen.gameserver.handler.IBBSHandler;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2TeleportLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
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
		@Override
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
			msg = "Can't use Gatekeeper when sitting";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("ALL"))
			msg = "Gatekeeper is not available in this area";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = "Can't use Gatekeeper with Cursed Weapon"; 
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = "Gatekeeper is not available during the battle";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = "Gatekeeper is not available in the catacombs and necropolis";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
			msg = "Gatekeeper is not available in this area";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = "Gatekeeper is not available in this area";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = "Gatekeeper is not available in this area";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = "Gatekeeper is not available in this area";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("OLYMPIAD") && activeChar.isInOlympiadMode())
			msg = "Gatekeeper is not available in Olympiad";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("TVT") && 
				activeChar._inEventTvT && TvT.is_started() )
			msg = "Gatekeeper is not available in TVT";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("CTF") && 
				activeChar._inEventCTF && CTF.is_started() )
			msg = "Gatekeeper is not available in CTF";
		else if(PowerPakConfig.GLOBALGK_EXCLUDE_ON.contains("DM") && 
				activeChar._inEventDM && DM.is_started() )
			msg = "Gatekeeper is not available in DM";
		

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

		if(cmd.compareTo(PowerPakConfig.GLOBALGK_COMMAND)==0)
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
			String text = HtmCache.getInstance().getHtm("data/html/gatekeeper/70023.htm");
			htm.setHtml(text);
			player.sendPacket(htm);
		}
		
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

		if(!PowerPakConfig.GLOBALGK_USEBBS && !PowerPakConfig.GLOBALGK_USECOMMAND){
			
			L2NpcInstance gknpc = null; 
			
			if(player.getTarget()!=null)
				if(player.getTarget() instanceof L2NpcInstance)
				{
					gknpc = (L2NpcInstance)player.getTarget();
					if(gknpc.getTemplate().getNpcId()!=PowerPakConfig.GLOBALGK_NPC)
						gknpc=null;
				}
			
			//Possible fix to Buffer - 1
			if (gknpc == null)
				return;

			//Possible fix to Buffer - 2
			if (!player.isInsideRadius(gknpc, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				return;
			
		}//else (voice and bbs)
		
		String htm = "70023";
		if(parameters.startsWith("goto"))
		{
			try
			{
				if(PowerPakConfig.GLOBALGK_PRICE>0)
				{
					if(player.getAdena() < PowerPakConfig.GLOBALGK_PRICE)
					{
						player.sendMessage("You do not have enough adena to pay for services");
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
							player.sendMessage("You do not have enough adena to pay for services");
							return;
						}
						player.reduceAdena("teleport", tpPoint.getPrice(), null, true);
					}
					int unstuckTimer = PowerPakConfig.GLOBALGK_TIMEOUT*1000; 
					player.setTarget(player);
					player.disableAllSkills();
					MagicSkillUser u = new MagicSkillUser(player, 1050, 1, unstuckTimer, 0);
					Broadcast.toSelfAndKnownPlayersInRadius(player, u, 810000);
					SetupGauge sg = new SetupGauge(0, unstuckTimer);
					player.sendPacket(sg);
					EscapeFinalizer e = new EscapeFinalizer(player,tpPoint);
					player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(e, unstuckTimer));
					player.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer/GameTimeController.MILLIS_IN_TICK);
					return;
				}
				player.sendMessage("Teleport, with ID "+locId+" does not exist in the database");
			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				player.sendMessage("Error... maybe you cheat..");
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
