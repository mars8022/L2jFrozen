/* This program is free software; you can redistribute it and/or modify
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

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.knownlist.PlayableKnownList;
import com.l2jfrozen.gameserver.model.actor.stat.PlayableStat;
import com.l2jfrozen.gameserver.model.actor.status.PlayableStatus;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;

/**
 * This class represents all Playable characters in the world.<BR>
 * <BR>
 * L2PlayableInstance :<BR>
 * <BR>
 * <li>L2PcInstance</li> <li>L2Summon</li><BR>
 */
public abstract class L2PlayableInstance extends L2Character
{

	/** The _is noblesse blessed. */
	private boolean _isNoblesseBlessed = false; // for Noblesse Blessing skill, restores buffs after death
	
	/** The _get charm of luck. */
	private boolean _getCharmOfLuck = false; // Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	
	/** The _is phoenix blessed. */
	private boolean _isPhoenixBlessed = false; // for Soul of The Phoenix or Salvation buffs
	
	/** The _ protection blessing. */
	private boolean _ProtectionBlessing = false;

	/**
	 * Constructor of L2PlayableInstance (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this
	 * L2PlayableInstance</li><BR>
	 * <BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2PlayableInstance
	 */
	public L2PlayableInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getKnownList()
	 */
	@Override
	public PlayableKnownList getKnownList()
	{
		if(super.getKnownList() == null || !(super.getKnownList() instanceof PlayableKnownList))
		{
			setKnownList(new PlayableKnownList(this));
		}
		return (PlayableKnownList) super.getKnownList();
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getStat()
	 */
	@Override
	public PlayableStat getStat()
	{
		if(super.getStat() == null || !(super.getStat() instanceof PlayableStat))
		{
			setStat(new PlayableStat(this));
		}
		return (PlayableStat) super.getStat();
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getStatus()
	 */
	@Override
	public PlayableStatus getStatus()
	{
		if(super.getStatus() == null || !(super.getStatus() instanceof PlayableStatus))
		{
			setStatus(new PlayableStatus(this));
		}
		return (PlayableStatus) super.getStatus();
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#doDie(com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
			return false;

		if(killer != null)
		{
			L2PcInstance player = null;
			if(killer instanceof L2PcInstance)
			{
				player = (L2PcInstance) killer;
			}
			else if(killer instanceof L2Summon)
			{
				player = ((L2Summon) killer).getOwner();
			}

			if(player != null)
			{
				player.onKillUpdatePvPKarma(this);
				player = null;
			}
		}
		return true;
	}

	/**
	 * Check if pvp.
	 *
	 * @param target the target
	 * @return true, if successful
	 */
	public boolean checkIfPvP(L2Character target)
	{
		if(target == null)
			return false; // Target is null
		if(target == this)
			return false; // Target is self
		if(!(target instanceof L2PlayableInstance))
			return false; // Target is not a L2PlayableInstance

		L2PcInstance player = null;
		if(this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		else if(this instanceof L2Summon)
		{
			player = ((L2Summon) this).getOwner();
		}

		if(player == null)
			return false; // Active player is null
		if(player.getKarma() != 0)
			return false; // Active player has karma

		L2PcInstance targetPlayer = null;
		if(target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if(target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}

		if(targetPlayer == null)
			return false; // Target player is null
		if(targetPlayer == this)
			return false; // Target player is self
		if(targetPlayer.getKarma() != 0)
			return false; // Target player has karma
		if(targetPlayer.getPvpFlag() == 0)
			return false;

		player = null;
		targetPlayer = null;

		return true;
		/*  Even at war, there should be PvP flag
		if(
		        player.getClan() == null ||
		        targetPlayer.getClan() == null ||
		        (
		                !targetPlayer.getClan().isAtWarWith(player.getClanId()) &&
		                targetPlayer.getWantsPeace() == 0 &&
		                player.getWantsPeace() == 0
		        )
		    )
		{
		    return true;
		}

		return false;
		*/
	}

	//==================================== DONT EVEN THINK!! ====================================//

	/**
	 * Return True.<BR>
	 * <BR>
	 *
	 * @return true, if is attackable
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}

	private L2Effect _lastNoblessEffect = null;
	
	// Support for Noblesse Blessing skill, where buffs are retained
	// after resurrect
	/**
	 * Checks if is noblesse blessed.
	 *
	 * @return true, if is noblesse blessed
	 */
	public final boolean isNoblesseBlessed()
	{
		return _isNoblesseBlessed;
	}

	/**
	 * Sets the checks if is noblesse blessed.
	 *
	 * @param value the new checks if is noblesse blessed
	 */
	public final void setIsNoblesseBlessed(boolean value)
	{
		_isNoblesseBlessed = value;
	}

	/**
	 * Start noblesse blessing.
	 * @param effect 
	 */
	public final void startNoblesseBlessing(L2Effect effect)
	{
		_lastNoblessEffect = effect;
		setIsNoblesseBlessed(true);
		updateAbnormalEffect();
	}

	/**
	 * Stop noblesse blessing.
	 *
	 * @param effect the effect
	 */
	public final void stopNoblesseBlessing(L2Effect effect)
	{
		//to avoid multiple buffs effects removal
		if(effect!= null &&
			_lastNoblessEffect != effect){
			return;
		}
		
		if(effect == null)
		{
			stopEffects(L2Effect.EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setIsNoblesseBlessed(false);
		updateAbnormalEffect();
		_lastNoblessEffect = null;
		
	}
	
	private L2Effect _lastProtectionBlessingEffect = null;
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	/**
	 * Gets the protection blessing.
	 *
	 * @return the protection blessing
	 */
	public final boolean getProtectionBlessing()
	{
		return _ProtectionBlessing;
	}

	/**
	 * Sets the protection blessing.
	 *
	 * @param value the new protection blessing
	 */
	public final void setProtectionBlessing(boolean value)
	{
		_ProtectionBlessing = value;
	}

	/**
	 * Start protection blessing.
	 * @param effect 
	 */
	public void startProtectionBlessing(L2Effect effect)
	{
		_lastProtectionBlessingEffect = effect;
		setProtectionBlessing(true);
		updateAbnormalEffect();
	}

	/**
	 * Stop protection blessing.
	 *
	 * @param effect the effect
	 */
	public void stopProtectionBlessing(L2Effect effect)
	{
		if(effect!=null
			&& _lastProtectionBlessingEffect != effect){
			return;
		}
		
		if(effect == null)
		{
			stopEffects(L2Effect.EffectType.PROTECTION_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setProtectionBlessing(false);
		updateAbnormalEffect();
		_lastProtectionBlessingEffect = null;
	}

	private L2Effect _lastPhoenixBlessedEffect = null;
	
	// Support for Soul of the Phoenix and Salvation skills
	/**
	 * Checks if is phoenix blessed.
	 *
	 * @return true, if is phoenix blessed
	 */
	public final boolean isPhoenixBlessed()
	{
		return _isPhoenixBlessed;
	}

	/**
	 * Sets the checks if is phoenix blessed.
	 *
	 * @param value the new checks if is phoenix blessed
	 */
	public final void setIsPhoenixBlessed(boolean value)
	{
		_isPhoenixBlessed = value;
	}

	/**
	 * Start phoenix blessing.
	 * @param effect 
	 */
	public final void startPhoenixBlessing(L2Effect effect)
	{
		_lastPhoenixBlessedEffect = effect;
		setIsPhoenixBlessed(true);
		updateAbnormalEffect();
	}

	/**
	 * Stop phoenix blessing.
	 *
	 * @param effect the effect
	 */
	public final void stopPhoenixBlessing(L2Effect effect)
	{
		if(effect != null
			&& _lastPhoenixBlessedEffect != effect){
			return;
		}
		
		if(effect == null)
		{
			stopEffects(L2Effect.EffectType.PHOENIX_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setIsPhoenixBlessed(false);
		updateAbnormalEffect();
		_lastPhoenixBlessedEffect = null;
		
	}

	

	/**
	 * Destroy item by item id.
	 *
	 * @param process the process
	 * @param itemId the item id
	 * @param count the count
	 * @param reference the reference
	 * @param sendMessage the send message
	 * @return true, if successful
	 */
	public abstract boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage);

	/**
	 * Destroy item.
	 *
	 * @param process the process
	 * @param objectId the object id
	 * @param count the count
	 * @param reference the reference
	 * @param sendMessage the send message
	 * @return true, if successful
	 */
	public abstract boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage);

	//Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	private L2Effect _lastCharmOfLuckEffect = null;
	
	/**
	 * Gets the charm of luck.
	 *
	 * @return the charm of luck
	 */
	public final boolean getCharmOfLuck()
	{
		return _getCharmOfLuck;
	}

	/**
	 * Sets the charm of luck.
	 *
	 * @param value the new charm of luck
	 */
	public final void setCharmOfLuck(boolean value)
	{
		_getCharmOfLuck = value;
	}

	/**
	 * Start charm of luck.
	 * @param effect 
	 */
	public final void startCharmOfLuck(L2Effect effect)
	{
		setCharmOfLuck(true);
		updateAbnormalEffect();
		_lastCharmOfLuckEffect = effect;
	}

	/**
	 * Stop charm of luck.
	 *
	 * @param effect the effect
	 */
	public final void stopCharmOfLuck(L2Effect effect)
	{
		if(effect != null
			&& _lastCharmOfLuckEffect != effect){
			return;
		}
		
		if(effect == null)
		{
			stopEffects(L2Effect.EffectType.CHARM_OF_LUCK);
		}
		else
		{
			removeEffect(effect);
		}

		setCharmOfLuck(false);
		updateAbnormalEffect();
		_lastCharmOfLuckEffect = null;
		
	}
	
	/**
	 * Checks if is in fun event.
	 *
	 * @return true, if is in fun event
	 */
	public boolean isInFunEvent()
	{
		L2PcInstance player = getActingPlayer();

		return player == null ? false : player.isInFunEvent();
	}

	/**
	 * Gets the acting player.
	 *
	 * @return the acting player
	 */
	@Override
	public L2PcInstance getActingPlayer()
	{
		if(this instanceof L2PcInstance){
			return (L2PcInstance) this;
		}
		return null;
	}
}
