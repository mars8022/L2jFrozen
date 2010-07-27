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
package interlude.gameserver.model.quest.ai;

import java.util.logging.Logger;

import interlude.gameserver.ThreadPoolManager;

public class AILoader
{
	private static final Logger _log = Logger.getLogger(AILoader.class.getName());
	
	public static void init()
	{
		_log.info("AI load:");
//		_log.info(" - Van Halter");
//		ThreadPoolManager.getInstance().scheduleAi(new VanHalter(-1, "vanhalter", "ai"), 500);
//		_log.info(" - Monastery");
//		ThreadPoolManager.getInstance().scheduleAi(new Monastery(-1, "monastery", "ai"), 700);
//		_log.info(" - Transform");
//		ThreadPoolManager.getInstance().scheduleAi(new Transform(-1, "transform", "ai"), 800);
//		_log.info(" - Fairy Trees");
//		ThreadPoolManager.getInstance().scheduleAi(new FairyTrees(-1, "FairyTrees", "ai"), 900);
//		_log.info(" - Summon Minions");
//		ThreadPoolManager.getInstance().scheduleAi(new SummonMinions(-1, "SummonMinions", "ai"), 1000);
		_log.info(" - Zombie Gatekeepers");
		ThreadPoolManager.getInstance().scheduleAi(new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai"), 1100);
		_log.info(" - Golkonda");
		ThreadPoolManager.getInstance().scheduleAi(new Golkonda(-1, "Golkonda", "ai"), 1300);
		_log.info(" - Hallate");
		ThreadPoolManager.getInstance().scheduleAi(new Hallate(-1, "Hallate", "ai"), 1400);
		_log.info(" - Kernon");
		ThreadPoolManager.getInstance().scheduleAi(new Kernon(-1, "Kernon", "ai"), 1500);
		_log.info(" - Varka Ketra Ally");
//		ThreadPoolManager.getInstance().scheduleAi(new VarkaKetraAlly(-1, "Varka Ketra Ally", "ai"), 1600);
//		_log.info(" - Barakiel");
		ThreadPoolManager.getInstance().scheduleAi(new Barakiel(-1, "Barakiel", "ai"), 1700);
	}
}