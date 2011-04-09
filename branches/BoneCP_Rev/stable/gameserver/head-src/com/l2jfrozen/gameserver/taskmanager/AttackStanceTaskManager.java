/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
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
package com.l2jfrozen.gameserver.taskmanager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 * @author Luca Baldi
 */
public class AttackStanceTaskManager
{
	private static final Logger _log = Logger.getLogger(AttackStanceTaskManager.class.getName());
	private final BlockingQueue<L2Character> attackStanceTasks = new LinkedBlockingQueue<L2Character>();

	private static AttackStanceTaskManager _instance;

	public AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}

	public static AttackStanceTaskManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new AttackStanceTaskManager();
		}

		return _instance;
	}

	public void addAttackStanceTask(L2Character actor)
	{
		attackStanceTasks.add(actor);
		actor.updateAttackStance();
	}

	public void removeAttackStanceTask(L2Character actor)
	{
		attackStanceTasks.remove(actor);
	}

	public boolean getAttackStanceTask(L2Character actor)
	{
		return attackStanceTasks.contains(actor);
	}

	private class FightModeScheduler implements Runnable {
		@Override public void run() {
			long currentTime = System.currentTimeMillis();
			final L2Character[] actors = attackStanceTasks.toArray(new L2Character[attackStanceTasks.size()]);
			for(L2Character actor : actors)
				if(currentTime - actor.getAttackStance() > 15000) {
					attackStanceTasks.remove(actor);
					actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
					actor.getAI().setAutoAttacking(false);
				}
		}
	}
}
