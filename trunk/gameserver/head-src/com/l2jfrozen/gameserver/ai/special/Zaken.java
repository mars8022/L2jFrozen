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
package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameTimeController;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * 
 * 
 * @author Enzo
 */
public class Zaken extends Quest implements Runnable
{

	private static int ZAKEN = 29022;
	private static final byte LIVE = 0;
	private static final byte DEAD = 1;
	
	enum Event{
		ZAKEN_SPAWN,ZAKEN_TP_CHAR,ZAKEN_OPEN_DOOR,ZAKEN_CLOSE_DOOR
	}

	L2GrandBossInstance zaken = null;
	
	public Zaken(int id, String name, String descr){
		
		super(id, name, descr);
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ZAKEN);
        int status = GrandBossManager.getInstance().getBossStatus(ZAKEN);
        
        addEventId(ZAKEN, Quest.QuestEventType.ON_KILL);
		addEventId(ZAKEN, Quest.QuestEventType.ON_ATTACK);
		
		switch(status){
        	case DEAD:{
        		
        		long temp = info.getLong("respawn_time") - System.currentTimeMillis();
    			if(temp > 0)
    			{
    				startQuestTimer("ZAKEN_SPAWN", temp, null, null);
    			}
    			else
    			{
    				deleteGlobalQuestVar("underattack");
    				zaken = (L2GrandBossInstance) addSpawn(ZAKEN, 55312, 219168, -3223,0, false, 0);
    				GrandBossManager.getInstance().setBossStatus(ZAKEN, LIVE);
    				GrandBossManager.getInstance().addBoss(zaken);
    				startQuestTimer("ZAKEN_TP_CHAR",600000,zaken,null);
    			}
        	}
        	break;
        	case LIVE:{
        		
        		int loc_x = info.getInteger("loc_x");
    			int loc_y = info.getInteger("loc_y");
    			int loc_z = info.getInteger("loc_z");
    			int heading = info.getInteger("heading");
    			int hp = info.getInteger("currentHP");
    			int mp = info.getInteger("currentMP");
    			zaken = (L2GrandBossInstance) addSpawn(ZAKEN, loc_x, loc_y, loc_z, heading, false, 0);
    			GrandBossManager.getInstance().addBoss(zaken);
    			zaken.setCurrentHpMp(hp, mp);
    			startQuestTimer("ZAKEN_TP_CHAR",600000,zaken,null);
        	}
        	break;
        	default:{
        		
        		deleteGlobalQuestVar("underattack");
        		zaken = (L2GrandBossInstance) addSpawn(ZAKEN, 55312, 219168, -3223,0, false, 0);
        		GrandBossManager.getInstance().setBossStatus(ZAKEN, LIVE);
        		GrandBossManager.getInstance().addBoss(zaken);
        		startQuestTimer("ZAKEN_TP_CHAR",600000,zaken,null);
    		}
        }
        
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		Event event_enum = Event.valueOf(event);
		
		switch(event_enum){

			case ZAKEN_SPAWN:{

				deleteGlobalQuestVar("underattack");
				zaken = (L2GrandBossInstance) addSpawn(ZAKEN,55606,218755,-3251,30000,false,0);
				GrandBossManager.getInstance().setBossStatus(ZAKEN, LIVE);
				GrandBossManager.getInstance().addBoss(zaken);
				startQuestTimer("ZAKEN_TP_CHAR",600000,zaken,null);
				cancelQuestTimer("ZAKEN_SPAWN",npc,null);

			}
			break;
			case ZAKEN_TP_CHAR:{

				String underatak = loadGlobalQuestVar("underattack");
				if (underatak.equals("")){
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4222,1));
					startQuestTimer("ZAKEN_TP_CHAR",600000,npc,null);
				}else{
					deleteGlobalQuestVar("underattack");
					startQuestTimer("ZAKEN_TP_CHAR",600000,npc,null);
				} 

			}
			break;
			case ZAKEN_OPEN_DOOR:{

				int time = GameTimeController.getInstance().getGameTime();
				int hour = (time/60)%24;
				if( hour == 0){
					DoorTable.getInstance().getDoor(21240006).openMe();
					startQuestTimer("ZAKEN_OPEN_DOOR",1800000,null,null);
					startQuestTimer("ZAKEN_CLOSE_DOOR",300000,null,null);
				}else {
					startQuestTimer("ZAKEN_OPEN_DOOR",30000,null,null);
				}

			}
			break;
			case ZAKEN_CLOSE_DOOR:{

				DoorTable.getInstance().getDoor(21240006).closeMe();

			}
			break;
			default:{
				System.out.println("ZAKEN: Not defined event: "+event+"!");
			}
		}

		return super.onAdvEvent(event, npc, player);
		
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet)
	{
		saveGlobalQuestVar("underattack", "1");
		int maxHp = npc.getMaxHp();
		int nowHp = (int) npc.getCurrentHp();

		if( nowHp > 50){
			if (Rnd.get(15) < 1){
				int ch = Rnd.get(15*15);
				if( ch < 1){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4216,1));
				}else if( ch < 2){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4217,1));
				}else if( ch < 4){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4219,1));
				}else if( ch < 8){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4218,1));
				}else if( ch < 15){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4221,1));
				}

				if (Rnd.get(2) < 1){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4220,1));
				}

			}else if (Rnd.get(10) < 1){
				int ch = Rnd.get(15*15);
				if( ch < 1){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4216,1));
				}else if( ch < 2){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4217,1));
				}else if( ch < 4){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4219,1));
				}else if (ch < 8){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4218,1));
				}else if( ch < 15){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4221,1));
				}

				if( Rnd.get(2) < 1){
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4220,1));
				}

			}else if(nowHp < (maxHp*0.25)){
				int chh = (Rnd.get(20));
				if (chh < 1){
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4222,1));
				}
			}
		}

		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
		
		return super.onAttack(npc, player, damage, isPet);
		
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == ZAKEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			GrandBossManager.getInstance().setBossStatus(ZAKEN, DEAD);

			long respawnTime = (long) (Config.ZAKEN_RESP_FIRST + Rnd.get(Config.ZAKEN_RESP_SECOND)) * 3600000;
			
			cancelQuestTimer("ZAKEN_TP_CHAR", npc, null);
			startQuestTimer("ZAKEN_SPAWN", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ZAKEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(ZAKEN, info);
			
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	
	
	@Override
	public void run()
	{}

}
