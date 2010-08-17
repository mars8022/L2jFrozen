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
package interlude.gameserver.model.actor.instance;

import interlude.Config;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.ai.L2AttackableAI;
import interlude.gameserver.model.L2Attackable;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2WorldRegion;
import interlude.gameserver.model.actor.knownlist.TownPetKnownList;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

public final class L2TownPetInstance extends L2Attackable
{
	private boolean _isInvul;
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private static final int RETURN_INTERVAL = 1;

	public L2TownPetInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.nextInt(1));
	}

	public class ReturnTask implements Runnable
	{
		public void run()
		{
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				returnHome();
			}
		}
	}

	@Override
	public final TownPetKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof TownPetKnownList))
		{
			setKnownList(new TownPetKnownList(this));
		}
		return (TownPetKnownList) super.getKnownList();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	/**
	 * Sets home location of townpet. pet will always try to return to this location.
	 */
	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		if (Config.DEBUG)
		{
			_log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
		}
	}

	public int getHomeX()
	{
		return _homeX;
	}

	public int getHomeY()
	{
		return _homeY;
	}

	@Override
	public void returnHome()
	{
		if (!isInsideRadius(_homeX, _homeY, 2, false))
		{
			if (Config.DEBUG)
			{
				_log.fine(getObjectId() + ": moving hometo" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
		}
	}

	@Override
	public void onSpawn()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		if (Config.DEBUG)
		{
			_log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
		}
		// check the region where this mob is, do not activate the AI if region
		// is inactive.
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive())
		{
			((L2AttackableAI) getAI()).stopAITask();
		}
	}

	@Override
	public boolean isAggressive()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return Config.MAX_NPC_ANIMATION > 0;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			player.sendPacket(new ValidateLocation(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
