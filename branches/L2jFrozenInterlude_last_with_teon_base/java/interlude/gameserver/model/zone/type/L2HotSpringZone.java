/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.model.zone.type;

import java.util.Collection;
import java.util.concurrent.Future;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.util.Rnd;

/**
 * another type of damage zone with skills
 * @author maxi
 */
public class L2HotSpringZone extends L2ZoneType
{
	private int _skillId;
	private Future<?> _task;

	private static final int BIGHEAD_EFFECT = 0x2000;
	private static final int SKILL = 4559;
	private static final int EFFECT_DURATION = 1200000; // 20 mins
	//60min, 60000 1min-60sec/g, 360000 5min, 1800000 30min

	public L2HotSpringZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
			super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_task == null && Rnd.get(100) < 100)
		{
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(), 60, 10000);
		}
	}
	public int getSkillId()
	{
		return _skillId;
	}

	protected Collection<L2Character> getCharacterList()
	{
		return _characterList.values();
	}
	
	class ApplySkill implements Runnable
	{
		ApplySkill() { }

		public void run()
		{
			L2PlayableInstance playable = null;
				if (!(playable instanceof L2PcInstance)) return;
					L2PcInstance activeChar = (L2PcInstance) playable;
				MagicSkillUser MSU = new MagicSkillUser(playable, playable, SKILL, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			activeChar.startAbnormalEffect(BIGHEAD_EFFECT);
			}
		}

	@Override
	protected void onExit(L2Character character)
	{
		L2PlayableInstance playable = null;
		EffectStop ep = new EffectStop(playable);
		ThreadPoolManager.getInstance().scheduleEffect(ep, EFFECT_DURATION);
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public class EffectStop implements Runnable
	{
		private L2PlayableInstance _playable;

		public EffectStop(L2PlayableInstance playable)
		{
			_playable = playable;
		}

		public void run()
		{
			try
			{
				if (!(_playable instanceof L2PcInstance)) return;
				((L2PcInstance) _playable).stopAbnormalEffect(BIGHEAD_EFFECT);
			} catch (Throwable t) { }
		}
	}
}
