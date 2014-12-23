/* L2jFrozen Project - www.l2jfrozen.com 
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
package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Drunkard Zabb0x Lets drink2code!
 */
public class L2XmassTreeInstance extends L2NpcInstance
{
	private final ScheduledFuture<?> _aiTask;
	
	class XmassAI implements Runnable
	{
		private final L2XmassTreeInstance _caster;
		
		protected XmassAI(final L2XmassTreeInstance caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				final int i = Rnd.nextInt(3);
				handleCast(player, (4262 + i));
			}
		}
		
		private boolean handleCast(final L2PcInstance player, final int skillId)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			
			if (player.getFirstEffect(skill) == null)
			{
				setTarget(player);
				doCast(skill);
				
				MagicSkillUser msu = new MagicSkillUser(_caster, player, skill.getId(), 1, skill.getHitTime(), 0);
				broadcastPacket(msu);
				skill = null;
				msu = null;
				return true;
			}
			skill = null;
			return false;
		}
		
	}
	
	public L2XmassTreeInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		return 900;
	}
	
	/**
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
	
}
