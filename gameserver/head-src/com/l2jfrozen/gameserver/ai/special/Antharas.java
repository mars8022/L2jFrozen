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

// import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.State;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.serverpackets.Earthquake;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SpecialCamera;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * AI РґР»СЏ РµРїРёРє Р±РѕСЃР° "Antharas"
 * 
 * @author qwerty
 */

public class Antharas extends Quest implements Runnable
{
	private static final int ANTHARAS = 29019;
	private static final int HEART = 13001;

	private static final int STONE = 3865;

	private static final byte DORMANT = 0; //Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1; //Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	//before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2; //Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3; //Antharas has been killed. Entry is locked

	private static long _LastAction = 0;
	private static L2BossZone _Zone;

	L2GrandBossInstance antharas = null;

	//	private static final Logger _log = Logger.getLogger(Antharas.class.getName());

	public Antharas(int id, String name, String descr)
	{
		super(id, name, descr);

		setInitialState(new State("Start", this));

		addEventId(ANTHARAS, Quest.QuestEventType.ON_KILL);
		addEventId(ANTHARAS, Quest.QuestEventType.ON_ATTACK);

		addEventId(HEART, Quest.QuestEventType.QUEST_START);
		addEventId(HEART, Quest.QuestEventType.QUEST_TALK);

		_Zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARAS);
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARAS);
		if(status == DEAD)
		{
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if(temp > 0)
			{
				startQuestTimer("antharas_unlock", temp, null, null);
			}
			else
			{
				antharas = (L2GrandBossInstance) addSpawn(ANTHARAS, 185708, 114298, -8221, 32768, false, 0);
				GrandBossManager.getInstance().setBossStatus(ANTHARAS, DORMANT);
				antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
				GrandBossManager.getInstance().addBoss(antharas);
			}
		}
		else if(status == FIGHTING)
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			antharas = (L2GrandBossInstance) addSpawn(ANTHARAS, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss(antharas);
			antharas.setCurrentHpMp(hp, mp);
			_LastAction = System.currentTimeMillis();
			// Start repeating timer to check for inactivity
			startQuestTimer("antharas_despawn", 60000, antharas, null);
		}
		else
		{
			antharas = (L2GrandBossInstance) addSpawn(ANTHARAS, 185708, 114298, -8221, 32768, false, 0);
			antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
			GrandBossManager.getInstance().addBoss(antharas);
			if(status == WAITING)
			{
				// Start timer to lock entry after 20 minutes
				startQuestTimer("waiting", Config.ANTHARAS_CLOSE * 1000, antharas, null);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if(npc != null)
		{
			long temp = 0;
			if(event.equalsIgnoreCase("waiting"))
			{
				npc.teleToLocation(185452, 114835, -8221);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(181911, 114835, -7678, 0));
				startQuestTimer("antharas_has_arrived", 2000, npc, null);
				npc.broadcastPacket(new PlaySound(1, "BS02_A", 1, npc.getObjectId(), 185452, 114835, -8221));
				GrandBossManager.getInstance().setBossStatus(ANTHARAS, FIGHTING);
			}
			else if(event.equalsIgnoreCase("camera_1"))
			{
				startQuestTimer("camera_2", 3000, npc, null);
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, -19, 0, 20000));
			}
			else if(event.equalsIgnoreCase("camera_2"))
			{
				startQuestTimer("camera_3", 10000, npc, null);
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, 0, 6000, 20000));
			}
			else if(event.equalsIgnoreCase("camera_3"))
			{
				startQuestTimer("camera_4", 200, npc, null);
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 3700, 0, -3, 0, 10000));
			}
			else if(event.equalsIgnoreCase("camera_4"))
			{
				startQuestTimer("camera_5", 10800, npc, null);
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 22000, 30000));
			}
			else if(event.equalsIgnoreCase("camera_5"))
			{
				startQuestTimer("antharas_despawn", 60000, npc, null);
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 300, 7000));
				_LastAction = System.currentTimeMillis();
			}
			else if(event.equalsIgnoreCase("antharas_despawn"))
			{
				startQuestTimer("antharas_despawn", 60000, npc, null);
				temp = System.currentTimeMillis() - _LastAction;
				if(temp > Config.ANTHARAS_SLEEP * 1000)
				{
					npc.teleToLocation(185708, 114298, -8221);
					npc.getSpawn().setLocx(185708);
					npc.getSpawn().setLocy(114298);
					npc.getSpawn().setLocz(-8221);
					GrandBossManager.getInstance().setBossStatus(ANTHARAS, DORMANT);
					npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
					_Zone.oustAllPlayers();
					cancelQuestTimer("antharas_despawn", npc, null);
				}
			}
			else if(event.equalsIgnoreCase("antharas_has_arrived"))
			{
				int dx = Math.abs(npc.getX() - 181911);
				int dy = Math.abs(npc.getY() - 114835);
				if(dx <= 50 && dy <= 50)
				{
					startQuestTimer("camera_1", 2000, npc, null);
					npc.getSpawn().setLocx(181911);
					npc.getSpawn().setLocy(114835);
					npc.getSpawn().setLocz(-7678);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					cancelQuestTimer("antharas_has_arrived", npc, null);
				}
				else
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(181911, 114835, -7678, 0));
					startQuestTimer("antharas_has_arrived", 2000, npc, null);
				}
			}
			else if(event.equalsIgnoreCase("spawn_cubes"))
			{
				addSpawn(31859, 177615, 114941, -7709, 0, false, 900000);
				int radius = 1500;
				for(int i = 0; i < 20; i++)
				{
					int x = (int) (radius * Math.cos(i * .331)); //.331~2pi/19
					int y = (int) (radius * Math.sin(i * .331));
					addSpawn(31859, 177615 + x, 114941 + y, -7709, 0, false, 900000);
				}
				cancelQuestTimer("antharas_despawn", npc, null);
				startQuestTimer("remove_players", 900000, null, null);
			}
		}
		else
		{
			if(event.equalsIgnoreCase("antharas_unlock"))
			{
				antharas = (L2GrandBossInstance) addSpawn(ANTHARAS, 185708, 114298, -8221, 32768, false, 0);
				GrandBossManager.getInstance().addBoss(antharas);
				GrandBossManager.getInstance().setBossStatus(ANTHARAS, DORMANT);
				antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
			}
			else if(event.equalsIgnoreCase("remove_players"))
			{
				_Zone.oustAllPlayers();
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		_LastAction = System.currentTimeMillis();
		if(GrandBossManager.getInstance().getBossStatus(ANTHARAS) != FIGHTING)
		{
			_Zone.oustAllPlayers();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		startQuestTimer("spawn_cubes", 10000, npc, null);
		GrandBossManager.getInstance().setBossStatus(ANTHARAS, DEAD);
		long respawnTime = (Config.ANTHARAS_RESP_FIRST + Rnd.get(Config.ANTHARAS_RESP_SECOND)) * 3600000;
		startQuestTimer("antharas_unlock", respawnTime, null, null);
		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARAS);
		info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
		GrandBossManager.getInstance().setStatsSet(ANTHARAS, info);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		if(player.getQuestState("antharas") == null)
		{
			newQuestState(player);
		}

		if(npc.getNpcId() == HEART)
		{
			int status = GrandBossManager.getInstance().getBossStatus(ANTHARAS);

			if(status != FIGHTING && status != DEAD) // РјРѕР¶РЅРѕ Р»Рё Рє С‚Р°СЂР°СЃСѓ РІ РіРѕСЃС‚Рё?
			{
				if(player.getInventory().getItemByItemId(STONE) != null && player.getInventory().getItemByItemId(STONE).getCount() >= 1) //РїСЂРѕРІРµСЂРєР°: РµСЃС‚СЊ Portal Stone?
				{
					if(status == DORMANT) //РїСЂРѕРІРµСЂРєР°: РІС‹Р·РІР°Р»Рё РђРЅС‚Р°СЂР°СЃР°? Р°РєС‚РёРІРёСЂСѓРµРј "РІС‹Р·С‹РІР°Р»РєСѓ РђРЅС‚Р°СЂР°СЃР°"
					{
						player.getQuestState("antharas").takeItems(STONE, 1);
						GrandBossManager.getInstance().getZone(177615, 114941, -7709).allowPlayerEntry(player, 30);
						player.teleToLocation(177615, 114941, -7709);
						GrandBossManager.getInstance().setBossStatus(ANTHARAS, WAITING);
						startQuestTimer("waiting", Config.ANTHARAS_CLOSE * 1000, antharas, null); //Р’С‹Р·С‹РІР°Р»РєР° РђРЅС‚Р°СЂР°СЃР°: РѕС‚СЃС‡РµС‚ 20 РјРёРЅСѓС‚
					}
					else if(status == WAITING)
					{
						player.getQuestState("antharas").takeItems(STONE, 1);
						GrandBossManager.getInstance().getZone(177615, 114941, -7709).allowPlayerEntry(player, 30);
						player.teleToLocation(177615, 114941, -7709);
					}
				}
				else
					return "<html><body><tr><td>You hear something...</td></tr><br>You need <font color=LEVEL>Portal stone</font> to enter...</body></html>";
			}
			else
				return "<html><body><tr><td>You hear something...</td></tr><br><font color=LEVEL>Antharas was killed...</font><br>Try another time.</body></html>";
		}
		return super.onTalk(npc, player);
	}

	@Override
	public void run()
	{}
}
