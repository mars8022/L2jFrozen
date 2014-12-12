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
package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2DoorAI;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Territory;
import com.l2jfrozen.gameserver.model.actor.knownlist.DoorKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.actor.stat.DoorStat;
import com.l2jfrozen.gameserver.model.actor.status.DoorStatus;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
	/** The Constant LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(L2DoorInstance.class);
	
	/** The castle index in the array of L2Castle this L2NpcInstance belongs to. */
	private int _castleIndex = -2;
	
	/** The _map region. */
	private int _mapRegion = -1;
	
	/** fort index in array L2Fort -> L2NpcInstance. */
	private int _fortIndex = -2;
	
	// when door is closed, the dimensions are
	/** The _range x min. */
	private int _rangeXMin = 0;
	
	/** The _range y min. */
	private int _rangeYMin = 0;
	
	/** The _range z min. */
	private int _rangeZMin = 0;
	
	/** The _range x max. */
	private int _rangeXMax = 0;
	
	/** The _range y max. */
	private int _rangeYMax = 0;
	
	/** The _range z max. */
	private int _rangeZMax = 0;
	
	/** The _ a. */
	private int _A = 0;
	
	/** The _ b. */
	private int _B = 0;
	
	/** The _ c. */
	private int _C = 0;
	
	/** The _ d. */
	private int _D = 0;
	
	/** The _door id. */
	protected final int _doorId;
	
	/** The _name. */
	protected final String _name;
	
	/** The _open. */
	private boolean _open;
	
	/** The _unlockable. */
	private final boolean _unlockable;
	
	/** The _clan hall. */
	private ClanHall _clanHall;
	
	/** The _auto action delay. */
	protected int _autoActionDelay = -1;
	
	/** The _auto action task. */
	private ScheduledFuture<?> _autoActionTask;
	
	/** The pos. */
	public final L2Territory pos;
	
	/**
	 * This class may be created only by L2Character and only for AI.
	 */
	public class AIAccessor extends L2Character.AIAccessor
	{
		
		/**
		 * Instantiates a new aI accessor.
		 */
		protected AIAccessor()
		{
			// null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#getActor()
		 */
		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#moveTo(int, int, int, int)
		 */
		@Override
		public void moveTo(final int x, final int y, final int z, final int offset)
		{
			// null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#moveTo(int, int, int)
		 */
		@Override
		public void moveTo(final int x, final int y, final int z)
		{
			// null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#stopMove(com.l2jfrozen.gameserver.model.actor.position.L2CharPosition)
		 */
		@Override
		public void stopMove(final L2CharPosition pos)
		{
			// null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.l2jfrozen.gameserver.model.L2Character.AIAccessor#doAttack(com.l2jfrozen.gameserver.model.L2Character)
		 */
		@Override
		public void doAttack(final L2Character target)
		{
			// null;
		}
		
		@Override
		public void doCast(final L2Skill skill)
		{
			// null;
		}
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2DoorAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#hasAI()
	 */
	@Override
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * The Class CloseTask.
	 */
	class CloseTask implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				onClose();
			}
			catch (final Throwable e)
			{
				LOGGER.error(e);
			}
		}
	}
	
	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				String doorAction;
				
				if (!getOpen())
				{
					doorAction = "opened";
					openMe();
				}
				else
				{
					doorAction = "closed";
					closeMe();
				}
				
				if (Config.DEBUG)
				{
					LOGGER.info("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + _autoActionDelay / 60000 + " minute(s).");
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.warn("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
			}
		}
	}
	
	/**
	 * Instantiates a new l2 door instance.
	 * @param objectId the object id
	 * @param template the template
	 * @param doorId the door id
	 * @param name the name
	 * @param unlockable the unlockable
	 */
	public L2DoorInstance(final int objectId, final L2CharTemplate template, final int doorId, final String name, final boolean unlockable)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
		pos = new L2Territory(/* "door_" + doorId */);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getKnownList()
	 */
	@Override
	public final DoorKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof DoorKnownList))
		{
			setKnownList(new DoorKnownList(this));
		}
		
		return (DoorKnownList) super.getKnownList();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getStat()
	 */
	@Override
	public final DoorStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}
		
		return (DoorStat) super.getStat();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getStatus()
	 */
	@Override
	public final DoorStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}
		
		return (DoorStatus) super.getStatus();
	}
	
	/**
	 * Checks if is unlockable.
	 * @return true, if is unlockable
	 */
	public final boolean isUnlockable()
	{
		return _unlockable;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getLevel()
	 */
	@Override
	public final int getLevel()
	{
		return 1;
	}
	
	/**
	 * Gets the door id.
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}
	
	/**
	 * Gets the open.
	 * @return Returns the open.
	 */
	public boolean getOpen()
	{
		return _open;
	}
	
	/**
	 * Sets the open.
	 * @param open The open to set.
	 */
	public void setOpen(final boolean open)
	{
		_open = open;
	}
	
	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door instance. <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 * @param actionDelay the new auto action delay
	 */
	public void setAutoActionDelay(final int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
			return;
		
		if (actionDelay > -1)
		{
			AutoOpenClose ao = new AutoOpenClose();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
			ao = null;
		}
		else
		{
			if (_autoActionTask != null)
			{
				_autoActionTask.cancel(false);
			}
		}
		
		_autoActionDelay = actionDelay;
	}
	
	/**
	 * Gets the damage.
	 * @return the damage
	 */
	public int getDamage()
	{
		final int dmg = 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}
	
	/**
	 * Gets the castle.
	 * @return the castle
	 */
	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		
		if (_castleIndex < 0)
			return null;
		
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	/**
	 * Gets the fort.
	 * @return the fort
	 */
	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		}
		
		if (_fortIndex < 0)
			return null;
		
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	/**
	 * Sets the clan hall.
	 * @param clanhall the new clan hall
	 */
	public void setClanHall(final ClanHall clanhall)
	{
		_clanHall = clanhall;
	}
	
	/**
	 * Gets the clan hall.
	 * @return the clan hall
	 */
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	/**
	 * Checks if is enemy of.
	 * @param cha the cha
	 * @return true, if is enemy of
	 */
	public boolean isEnemyOf(final L2Character cha)
	{
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#isAutoAttackable(com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		if (isUnlockable())
			return true;
		
		// Doors can`t be attacked by NPCs
		if (attacker == null || !(attacker instanceof L2PlayableInstance))
			return false;
		
		// Attackable during siege by attacker only
		
		L2PcInstance player = null;
		if (attacker instanceof L2PcInstance)
		{
			player = (L2PcInstance) attacker;
		}
		else if (attacker instanceof L2SummonInstance)
		{
			player = ((L2SummonInstance) attacker).getOwner();
		}
		else if (attacker instanceof L2PetInstance)
		{
			player = ((L2PetInstance) attacker).getOwner();
		}
		
		if (player == null)
		{
			return false;
		}
		
		final L2Clan clan = player.getClan();
		final boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(clan);
		final boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && getFort().getSiege().checkIsAttacker(clan);
		if (isFort)
		{
			if (clan != null && clan == getFort().getOwnerClan())
			{
				return false;
			}
		}
		else if (isCastle)
		{
			if (clan != null && clan.getClanId() == getCastle().getOwnerId())
			{
				return false;
			}
		}
		return isCastle || isFort || DevastatedCastle.getInstance().getIsInProgress();
	}
	
	/**
	 * Checks if is attackable.
	 * @param attacker the attacker
	 * @return true, if is attackable
	 */
	public boolean isAttackable(final L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	/**
	 * Gets the distance to watch object.
	 * @param object the object
	 * @return the distance to watch object
	 */
	public int getDistanceToWatchObject(final L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;
		return 2000;
	}
	
	/**
	 * Return the distance after which the object must be remove from _knownObject according to the type of the object.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is a L2PcInstance : 4000</li> <li>object is not a L2PcInstance : 0</li><BR>
	 * <BR>
	 * @param object the object
	 * @return the distance to forget object
	 */
	public int getDistanceToForgetObject(final L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;
		
		return 4000;
	}
	
	/**
	 * Return null.<BR>
	 * <BR>
	 * @return the active weapon instance
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getActiveWeaponItem()
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getSecondaryWeaponInstance()
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#getSecondaryWeaponItem()
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#onAction(com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (Config.DEBUG)
		{
			LOGGER.info("player " + player.getObjectId());
			LOGGER.info("Door " + getObjectId());
			LOGGER.info("player clan " + player.getClan());
			if (player.getClan() != null)
			{
				LOGGER.info("player clanid " + player.getClanId());
				LOGGER.info("player clanleaderid " + player.getClan().getLeaderId());
			}
			LOGGER.info("clanhall " + getClanHall());
			if (getClanHall() != null)
			{
				LOGGER.info("clanhallID " + getClanHall().getId());
				LOGGER.info("clanhallOwner " + getClanHall().getOwnerId());
				for (final L2DoorInstance door : getClanHall().getDoors())
				{
					LOGGER.info("clanhallDoor " + door.getObjectId());
				}
			}
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// if (isAutoAttackable(player))
			// {
			DoorStatusUpdate su = new DoorStatusUpdate(this);
			player.sendPacket(su);
			su = null;
			// }
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			// player.sendPacket(my);
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			else if (player.getClan() != null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
			{
				if (!isInsideRadius(player, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Like L2OFF Clanhall's doors get request to be closed/opened
					player.gatesRequest(this);
					if (!this.getOpen())
						player.sendPacket(new ConfirmDlg(1140));
					else
						player.sendPacket(new ConfirmDlg(1141));
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Object#onActionShift(com.l2jfrozen.gameserver.network.L2GameClient)
	 */
	@Override
	public void onActionShift(final L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null)
			return;
		
		if (Config.DEBUG)
		{
			LOGGER.info("player " + player.getObjectId());
			LOGGER.info("Door " + getObjectId());
			LOGGER.info("player clan " + player.getClan());
			if (player.getClan() != null)
			{
				LOGGER.info("player clanid " + player.getClanId());
				LOGGER.info("player clanleaderid " + player.getClan().getLeaderId());
			}
			LOGGER.info("clanhall " + getClanHall());
			if (getClanHall() != null)
			{
				LOGGER.info("clanhallID " + getClanHall().getId());
				LOGGER.info("clanhallOwner " + getClanHall().getOwnerId());
				for (final L2DoorInstance door : getClanHall().getDoors())
				{
					LOGGER.info("clanhallDoor " + door.getObjectId());
				}
			}
		}
		
		if (player.getAccessLevel().isGm())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			my = null;
			
			if (isAutoAttackable(player))
			{
				DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
				su = null;
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>Current HP  " + getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP       " + getMaxHp() + "</td></tr>");
			
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID: " + getDoorId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			
			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");
			
			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("</tr></table></body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
			html1 = null;
			html = null;
			
			// openMe();
		}
		else
		{
			// ATTACK the mob without moving?
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			my = null;
			
			if (isAutoAttackable(player))
			{
				DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
				su = null;
			}
			
			final NpcHtmlMessage reply = new NpcHtmlMessage(5);
			final TextBuilder replyMsg = new TextBuilder("<html><body>You cannot use this action.");
			replyMsg.append("</body></html>");
			reply.setHtml(replyMsg.toString());
			player.sendPacket(reply);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#broadcastStatusUpdate()
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		final Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;
		
		final DoorStatusUpdate su = new DoorStatusUpdate(this);
		
		for (final L2PcInstance player : knownPlayers)
		{
			player.sendPacket(su);
		}
	}
	
	/**
	 * On open.
	 */
	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}
	
	/**
	 * On close.
	 */
	public void onClose()
	{
		closeMe();
	}
	
	/**
	 * Close me.
	 */
	public final void closeMe()
	{
		synchronized (this)
		{
			if (!getOpen())
				return;
			
			setOpen(false);
		}
		
		broadcastStatusUpdate();
	}
	
	/**
	 * Open me.
	 */
	public final void openMe()
	{
		synchronized (this)
		{
			if (getOpen())
				return;
			setOpen(true);
		}
		
		broadcastStatusUpdate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#toString()
	 */
	@Override
	public String toString()
	{
		return "door " + _doorId;
	}
	
	/**
	 * Gets the door name.
	 * @return the door name
	 */
	public String getDoorName()
	{
		return _name;
	}
	
	/**
	 * Gets the x min.
	 * @return the x min
	 */
	public int getXMin()
	{
		return _rangeXMin;
	}
	
	/**
	 * Gets the y min.
	 * @return the y min
	 */
	public int getYMin()
	{
		return _rangeYMin;
	}
	
	/**
	 * Gets the z min.
	 * @return the z min
	 */
	public int getZMin()
	{
		return _rangeZMin;
	}
	
	/**
	 * Gets the x max.
	 * @return the x max
	 */
	public int getXMax()
	{
		return _rangeXMax;
	}
	
	/**
	 * Gets the y max.
	 * @return the y max
	 */
	public int getYMax()
	{
		return _rangeYMax;
	}
	
	/**
	 * Gets the z max.
	 * @return the z max
	 */
	public int getZMax()
	{
		return _rangeZMax;
	}
	
	/**
	 * Sets the range.
	 * @param xMin the x min
	 * @param yMin the y min
	 * @param zMin the z min
	 * @param xMax the x max
	 * @param yMax the y max
	 * @param zMax the z max
	 */
	public void setRange(final int xMin, final int yMin, final int zMin, final int xMax, final int yMax, final int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;
		
		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;
		
		_A = _rangeYMax * (_rangeZMax - _rangeZMin) + _rangeYMin * (_rangeZMin - _rangeZMax);
		_B = _rangeZMin * (_rangeXMax - _rangeXMin) + _rangeZMax * (_rangeXMin - _rangeXMax);
		_C = _rangeXMin * (_rangeYMax - _rangeYMin) + _rangeXMin * (_rangeYMin - _rangeYMax);
		_D = -1 * (_rangeXMin * (_rangeYMax * _rangeZMax - _rangeYMin * _rangeZMax) + _rangeXMax * (_rangeYMin * _rangeZMin - _rangeYMin * _rangeZMax) + _rangeXMin * (_rangeYMin * _rangeZMax - _rangeYMax * _rangeZMin));
	}
	
	/**
	 * Gets the a.
	 * @return the a
	 */
	public int getA()
	{
		return _A;
	}
	
	/**
	 * Gets the b.
	 * @return the b
	 */
	public int getB()
	{
		return _B;
	}
	
	/**
	 * Gets the c.
	 * @return the c
	 */
	public int getC()
	{
		return _C;
	}
	
	/**
	 * Gets the d.
	 * @return the d
	 */
	public int getD()
	{
		return _D;
	}
	
	/**
	 * Gets the map region.
	 * @return the map region
	 */
	public int getMapRegion()
	{
		return _mapRegion;
	}
	
	/**
	 * Sets the map region.
	 * @param region the new map region
	 */
	public void setMapRegion(final int region)
	{
		_mapRegion = region;
	}
	
	/**
	 * Gets the known siege guards.
	 * @return the known siege guards
	 */
	public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
	{
		final FastList<L2SiegeGuardInstance> result = new FastList<>();
		
		for (final L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2SiegeGuardInstance)
			{
				result.add((L2SiegeGuardInstance) obj);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the known fort siege guards.
	 * @return the known fort siege guards
	 */
	public Collection<L2FortSiegeGuardInstance> getKnownFortSiegeGuards()
	{
		final FastList<L2FortSiegeGuardInstance> result = new FastList<>();
		
		final Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		// synchronized (getKnownList().getKnownObjects())
		{
			for (final L2Object obj : objs)
			{
				if (obj instanceof L2FortSiegeGuardInstance)
				{
					result.add((L2FortSiegeGuardInstance) obj);
				}
			}
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#reduceCurrentHp(double, com.l2jfrozen.gameserver.model.L2Character, boolean)
	 */
	@Override
	public void reduceCurrentHp(final double damage, final L2Character attacker, final boolean awake)
	{
		if (this.isAutoAttackable(attacker) || (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isGM()))
		{
			super.reduceCurrentHp(damage, attacker, awake);
		}
		else
		{
			super.reduceCurrentHp(0, attacker, awake);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.L2Character#doDie(com.l2jfrozen.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		final boolean isFort = (getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress());
		final boolean isCastle = (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());
		
		if (isFort || isCastle)
			broadcastPacket(SystemMessage.sendString("The castle gate has been broken down"));
		return true;
	}
}
