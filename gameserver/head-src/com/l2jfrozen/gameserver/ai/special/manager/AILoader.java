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
package com.l2jfrozen.gameserver.ai.special.manager;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.ai.special.Antharas;
import com.l2jfrozen.gameserver.ai.special.Baium;
import com.l2jfrozen.gameserver.ai.special.Barakiel;
import com.l2jfrozen.gameserver.ai.special.Core;
import com.l2jfrozen.gameserver.ai.special.FairyTrees;
import com.l2jfrozen.gameserver.ai.special.Frintezza;
import com.l2jfrozen.gameserver.ai.special.Golkonda;
import com.l2jfrozen.gameserver.ai.special.Gordon;
import com.l2jfrozen.gameserver.ai.special.Hallate;
import com.l2jfrozen.gameserver.ai.special.IceFairySirra;
import com.l2jfrozen.gameserver.ai.special.Kernon;
import com.l2jfrozen.gameserver.ai.special.Monastery;
import com.l2jfrozen.gameserver.ai.special.Orfen;
import com.l2jfrozen.gameserver.ai.special.QueenAnt;
import com.l2jfrozen.gameserver.ai.special.SummonMinions;
import com.l2jfrozen.gameserver.ai.special.Transform;
import com.l2jfrozen.gameserver.ai.special.Valakas;
import com.l2jfrozen.gameserver.ai.special.VanHalter;
import com.l2jfrozen.gameserver.ai.special.VarkaKetraAlly;
import com.l2jfrozen.gameserver.ai.special.Zaken;
import com.l2jfrozen.gameserver.ai.special.ZombieGatekeepers;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author qwerty
 */

public class AILoader
{
	private static final Logger LOGGER = Logger.getLogger(AILoader.class);
	
	public static void init()
	{
		LOGGER.info("AI load:");
		
		LOGGER.info(" - Antharas_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Antharas(-1, "antharas", "ai"), 100);
		
		LOGGER.info(" - Baium_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Baium(-1, "baium", "ai"), 200);
		
		LOGGER.info(" - Core");
		ThreadPoolManager.getInstance().scheduleAi(new Core(-1, "core", "ai"), 300);
		
		LOGGER.info(" - Queen Ant");
		ThreadPoolManager.getInstance().scheduleAi(new QueenAnt(-1, "queen_ant", "ai"), 400);
		
		LOGGER.info(" - Van Halter");
		ThreadPoolManager.getInstance().scheduleAi(new VanHalter(-1, "vanhalter", "ai"), 500);
		LOGGER.info(" - Gordon");
		ThreadPoolManager.getInstance().scheduleAi(new Gordon(-1, "Gordon", "ai"), 600);
		
		LOGGER.info(" - Monastery_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Monastery(-1, "monastery", "ai"), 700);
		
		LOGGER.info(" - Transform");
		ThreadPoolManager.getInstance().scheduleAi(new Transform(-1, "transform", "ai"), 800);
		LOGGER.info(" - Fairy Trees");
		ThreadPoolManager.getInstance().scheduleAi(new FairyTrees(-1, "FairyTrees", "ai"), 900);
		LOGGER.info(" - Summon Minions");
		ThreadPoolManager.getInstance().scheduleAi(new SummonMinions(-1, "SummonMinions", "ai"), 1000);
		LOGGER.info(" - Zombie Gatekeepers");
		ThreadPoolManager.getInstance().scheduleAi(new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai"), 1100);
		LOGGER.info(" - Ice Fairy Sirra");
		ThreadPoolManager.getInstance().scheduleAi(new IceFairySirra(-1, "IceFairySirra", "ai"), 1200);
		LOGGER.info(" - Golkonda");
		ThreadPoolManager.getInstance().scheduleAi(new Golkonda(-1, "Golkonda", "ai"), 1300);
		LOGGER.info(" - Hallate");
		ThreadPoolManager.getInstance().scheduleAi(new Hallate(-1, "Hallate", "ai"), 1400);
		LOGGER.info(" - Kernon");
		ThreadPoolManager.getInstance().scheduleAi(new Kernon(-1, "Kernon", "ai"), 1500);
		LOGGER.info(" - Varka/Ketra Ally");
		ThreadPoolManager.getInstance().scheduleAi(new VarkaKetraAlly(-1, "Varka Ketra Ally", "ai"), 1600);
		LOGGER.info(" - Barakiel");
		ThreadPoolManager.getInstance().scheduleAi(new Barakiel(-1, "Barakiel", "ai"), 1700);
		
		LOGGER.info(" - Orfen");
		ThreadPoolManager.getInstance().scheduleAi(new Orfen(-1, "Orfen", "ai"), 1800);
		
		LOGGER.info(" - Zaken_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Zaken(-1, "Zaken", "ai"), 1900);
		
		LOGGER.info(" - Frintezza_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Frintezza(-1, "Frintezza", "ai"), 2000);
		
		LOGGER.info(" - Valakas_l2j");
		ThreadPoolManager.getInstance().scheduleAi(new Valakas(-1, "valakas", "ai"), 2100);
		
	}
}
