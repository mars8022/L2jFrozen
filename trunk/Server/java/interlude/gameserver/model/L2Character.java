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
package interlude.gameserver.model;

import static interlude.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static interlude.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.python.modules.math;

import interlude.gameserver.model.Location;
import interlude.gameserver.pathfinding.PathFinding;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import interlude.Config;
import interlude.gameserver.GameTimeController;
import interlude.gameserver.GeoData;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlEvent;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.ai.L2AttackableAI;
import interlude.gameserver.ai.L2CharacterAI;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.datatables.MapRegionTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.MapRegionTable.TeleportWhereType;
import interlude.gameserver.handler.ISkillHandler;
import interlude.gameserver.handler.SkillHandler;
import interlude.gameserver.instancemanager.DimensionalRiftManager;
import interlude.gameserver.instancemanager.TownManager;
import interlude.gameserver.model.L2Skill.SkillTargetType;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.model.actor.instance.L2ArtefactInstance;
import interlude.gameserver.model.actor.instance.L2BoatInstance;
import interlude.gameserver.model.actor.instance.L2ControlTowerInstance;
import interlude.gameserver.model.actor.instance.L2CubicInstance;
import interlude.gameserver.model.actor.instance.L2DecoInstance;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2EffectPointInstance;
import interlude.gameserver.model.actor.instance.L2GuardInstance;
import interlude.gameserver.model.actor.instance.L2GuardNoHTMLInstance;
import interlude.gameserver.model.actor.instance.L2MonsterInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2NpcWalkerInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PetInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.model.actor.instance.L2RiftInvaderInstance;
import interlude.gameserver.model.actor.instance.L2SiegeFlagInstance;
import interlude.gameserver.model.actor.instance.L2SiegeGuardInstance;
import interlude.gameserver.model.actor.instance.L2SiegeSummonInstance;
import interlude.gameserver.model.actor.instance.L2SummonInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import interlude.gameserver.model.actor.knownlist.CharKnownList;
import interlude.gameserver.model.actor.stat.CharStat;
import interlude.gameserver.model.actor.status.CharStatus;
import interlude.gameserver.model.entity.Duel;
import interlude.gameserver.model.entity.L2OpenEvents.CTF;
import interlude.gameserver.model.entity.L2OpenEvents.DM;
import interlude.gameserver.model.entity.L2OpenEvents.FortressSiege;
import interlude.gameserver.model.entity.L2OpenEvents.TvT;
import interlude.gameserver.model.item.Inventory;
import interlude.gameserver.model.olympiad.Olympiad;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.Attack;
import interlude.gameserver.network.serverpackets.ChangeMoveType;
import interlude.gameserver.network.serverpackets.ChangeWaitType;
import interlude.gameserver.network.serverpackets.CharInfo;
import interlude.gameserver.network.serverpackets.CharMoveToLocation;
import interlude.gameserver.network.serverpackets.EtcStatusUpdate;
import interlude.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import interlude.gameserver.network.serverpackets.L2GameServerPacket;
import interlude.gameserver.network.serverpackets.MagicEffectIcons;
import interlude.gameserver.network.serverpackets.MagicSkillCanceld;
import interlude.gameserver.network.serverpackets.MagicSkillLaunched;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.NpcInfo;
import interlude.gameserver.network.serverpackets.PartySpelled;
import interlude.gameserver.network.serverpackets.PetInfo;
import interlude.gameserver.network.serverpackets.RelationChanged;
import interlude.gameserver.network.serverpackets.Revive;
import interlude.gameserver.network.serverpackets.SetupGauge;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.network.serverpackets.StatusUpdate;
import interlude.gameserver.network.serverpackets.StopMove;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.network.serverpackets.TargetUnselected;
import interlude.gameserver.network.serverpackets.TeleportToLocation;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.gameserver.pathfinding.AbstractNodeLoc;
import interlude.gameserver.pathfinding.geonodes.GeoPathFinding;
import interlude.gameserver.skills.Calculator;
import interlude.gameserver.skills.Formulas;
import interlude.gameserver.skills.Stats;
import interlude.gameserver.skills.effects.EffectChanceSkillTrigger;
import interlude.gameserver.skills.funcs.Func;
import interlude.gameserver.skills.l2skills.L2SkillSummon;
import interlude.gameserver.templates.L2CharTemplate;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.gameserver.templates.L2Weapon;
import interlude.gameserver.templates.L2WeaponType;
import interlude.gameserver.util.Util;
import interlude.util.Point3D;
import interlude.util.Rnd;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2CastleGuardInstance</li> <li>L2DoorInstance</li> <li>L2NpcInstance</li> <li>L2PlayableInstance</li> <BR>
 * <BR>
 * <B><U> Concept of L2CharTemplate</U> :</B><BR>
 * <BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is spawned, server just create a link between the instance and the template. This link is
 * stored in <B>_template</B><BR>
 * <BR>
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	// =========================================================
	// Data Field
	private List<L2Character> _attackByList;
	@SuppressWarnings("unused")
	private volatile boolean _isCastingNow = false;
	private L2Skill 	_lastSkillCast;
	private boolean 	_isFallsdown;
	private boolean 	_isAfraid 					= false; // Flee in a random direction
	private boolean 	_isConfused 				= false; // Attack anyone randomly
	private boolean 	_isFakeDeath 				= false; // Fake death
	private boolean 	_isFlying 					= false; // Is flying Wyvern?
	private boolean 	_isRaid 					= false;
	private boolean 	_isMuted 					= false; // Cannot use magic
	private boolean 	_isPsychicalMuted 			= false; // Cannot use psychical skills
	private boolean 	_isKilledAlready			= false;
	private boolean		_isImmobilized 				= false;
	private boolean		_isOverloaded 				= false; // the char is carrying too much
	private boolean		_isParalyzed 				= false;
	private boolean		_isPetrified 				= false;
	private boolean		_isRiding 					= false; // Is Riding strider?
	private boolean		_isPendingRevive 			= false;
	private boolean		_isRooted 					= false; // Cannot move until root timed out
	private boolean		_isRunning 					= false;
	private boolean		_isSleeping 				= false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean 	_isStunned 					= false; // Cannot move/attack until stun timed out
	private boolean 	_isImmobileUntilAttacked 	= false;
	private boolean 	_isBetrayed 				= false; // Betrayed by own summon
	private boolean 	_buffImmunity 				= false; // Immunity to buff/debuffs
	protected boolean 	_showSummonAnimation 		= false;
	protected boolean 	_isTeleporting 				= false;
	protected boolean 	_isInvul 					= false;
	private boolean 	_AIdisabled 				= false;
	private boolean 	_isMinion 					= false;
	private int 		_lastHealAmount 			= 0;
	private L2Character _lastBuffer 				= null;
	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template; // The link on the L2CharTemplate object
	// containing generic and static
	// properties of this L2Character type
	// (ex : Max HP, Speed...)
	private String _title;
	private String _aiClass = "default";
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _champion = false;
	/** Table of Calculators containing all used calculator */
	private Calculator[] _calculators;
	/** FastMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, L2Skill> _skills;
	/** FastMap containing the active chance skills on this character */
	protected ChanceSkillList _chanceSkills;
    /** Current force buff this caster is casting to a target */
    protected ForceBuff _forceBuff;
	/** Zone system */
	public static final int ZONE_PVP = 1;
	public static final int ZONE_PEACE = 2;
	public static final int ZONE_SIEGE = 4;
	public static final int ZONE_MOTHERTREE = 8;
	public static final int ZONE_CLANHALL = 16;
	public static final int ZONE_UNUSED = 32;
	public static final int ZONE_NOLANDING = 64;
	public static final int ZONE_WATER = 128;
	public static final int ZONE_JAIL = 256;
	public static final int ZONE_MONSTERTRACK = 512;
	public static final int ZONE_CASTLE = 1024;
	public static final int ZONE_SWAMP = 2048;
	public static final int ZONE_NOSUMMONFRIEND = 4096;
	public static final int ZONE_FORT = 8192;
	public static final int ZONE_SCRIPT = 16384;
	private int _currentZones = 0;

	// Augmentation Skills by schursin
	private int _augmentationSkillId = -1;
	private int _augmentationSkillLevel = -1;
	private int _augmentationSkillType = -1; // 1 - physical; 2 - magic; 0 - both

	public void setAugmentationSkillInfo(int skillId, int skillLevel, int skillType)
	{
		_augmentationSkillId = skillId;
		_augmentationSkillLevel = skillLevel;
		_augmentationSkillType = skillType;
	}

	public void clearAugmentationSkillInfo()
	{
		_augmentationSkillId = -1;
		_augmentationSkillType = -1;
	}

	private int getAugmentationSkillId()
	{
		return _augmentationSkillId;
	}

	private int getAugmentationSkillLevel()
	{
		return _augmentationSkillLevel;
	}

	private int getAugmentationSkillType()
	{
		return _augmentationSkillType;
	}

	public boolean isInsideZone(int zone)
	{
		return (_currentZones & zone) != 0;
	}

	public void setInsideZone(int zone, boolean state)
	{
		if (state)
		{
			_currentZones |= zone;
		}
		else if (isInsideZone(zone)) // zone overlap possible
		{
			_currentZones ^= zone;
		}
	}

	// =========================================================
	// Constructor
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is spawned, server just create a link between the instance and the template This link is
	 * stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _template of the L2Character</li> <li>Set _overloaded to false (the charcater can take more items)</li> <BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li> <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li> <BR>
	 * <BR>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li> <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li> <BR>
	 * <BR>
	 *
	 * @param objectId
	 *            Identifier of the object to initialized
	 * @param template
	 *            The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		getKnownList();
		// Set its template to the new L2Character
		_template = template;
		if (template != null && this instanceof L2NpcInstance)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
			/* Copy the skills of the L2NPCInstance from its template to the L2Character Instance The skills list can be affected by spell effects so it's necessary to make a copy to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.*/
			_skills = ((L2NpcTemplate) template).getSkills();
			if (_skills != null)
			{
				for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet()) {
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
				}
			}
		}
		else
		{
			// Initialize the FastMap _skills to null
			_skills = new FastMap<Integer, L2Skill>().shared();
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}
		if (!(this instanceof L2PcInstance) && !(this instanceof L2MonsterInstance) && !(this instanceof L2GuardInstance) && !(this instanceof L2SiegeGuardInstance) && !(this instanceof L2ControlTowerInstance) && !(this instanceof L2DoorInstance) && !(this instanceof L2DecoInstance) && !(this instanceof L2SiegeSummonInstance) && !(this instanceof L2PetInstance)
				&& !(this instanceof L2SummonInstance) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2GuardNoHTMLInstance) && !(this instanceof L2EffectPointInstance)) {
			setIsInvul(true);
		}
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}

	// =========================================================
	// Event - Public
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		if (reg != null) {
			reg.removeFromZones(this);
		}
		decayMe();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone();
	}

	public void onTeleported()
	{
		if (!isTeleporting()) {
			return;
		}
		spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		setIsTeleporting(false);
		if (_isPendingRevive)
		{
			doRevive();
		}
		// Modify the position of the pet if necessary
		if (getPet() != null)
		{
			getPet().setFollowStatus(false);
			getPet().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
			getPet().setFollowStatus(true);
		}
	}

	// =========================================================
	// Method - Public
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player)) {
			return;
		}
		getAttackByList().add(player);
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 */
	public final void broadcastPacket(L2GameServerPacket mov)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		// if (Config.DEBUG) _log.fine("players to notify:" +
		// knownPlayers.size() + " packet:"+mov.getType());
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			try
			{
				player.sendPacket(mov);
				if (mov instanceof CharInfo && this instanceof L2PcInstance)
				{
					int relation = ((L2PcInstance) this).getRelation(player);
					if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) this, relation, player.isAutoAttackable(this)));
					}
				}
				// if(Config.DEVELOPER && !isInsideRadius(player, 3500, false,
				// false)) _log.warning("broadcastPacket: Too far player see
				// event!");
			}
			catch (NullPointerException e)
			{
			}
		}
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 */
	public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		// if (Config.DEBUG) _log.fine("players to notify:" +
		// knownPlayers.size() + " packet:"+mov.getType());
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			try
			{
				if (!isInsideRadius(player, radiusInKnownlist, false, false))
				{
					continue;
				}
				player.sendPacket(mov);
				if (mov instanceof CharInfo && this instanceof L2PcInstance)
				{
					int relation = ((L2PcInstance) this).getRelation(player);
					if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) this, relation, player.isAutoAttackable(this)));
					}
				}
			}
			catch (NullPointerException e)
			{
			}
		}
	}

	/**
	 * Returns true if hp update should be done, false if not
	 *
	 * @return boolean
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		if (currentHp <= 1.0 || getMaxHp() < barPixels)
		{
			return true;
		}
		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}
		return false;
	}

	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li> <li>Send the Server->Client packet StatusUpdate with current HP and MP to all L2Character called _statusListener that must be informed of HP/MP updates of this L2Character</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li> <BR>
	 * <BR>
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
		{
			return;
		}
		if (!needHpUpdate(352))
		{
			return;
		}
		if (Config.DEBUG)
		{
			_log.fine("Broadcast Status Update for " + getObjectId() + "(" + getName() + "). HP: " + getCurrentHp());
		}
		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		synchronized (getStatus().getStatusListener())
		{
			for (L2Character temp : getStatus().getStatusListener())
			{
				try
				{
					temp.sendPacket(su);
				}
				catch (NullPointerException e)
				{
				}
			}
		}
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the movement of the L2Character</li> <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li> <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li> <li>Modify the position of the pet if necessary</li> <BR>
	 * <BR>
	 */
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();
		setIsTeleporting(true);
		setTarget(null);

        // Remove from world regions zones
        if (getWorldRegion() != null) {
			getWorldRegion().removeFromZones(this);
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		z += 5;
		if (Config.DEBUG) {
			_log.fine("Teleporting to: " + x + ", " + y + ", " + z);
		}

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);

		decayMe();

		if (!(this instanceof L2PcInstance)) {
			onTeleported();
		} else if (FortressSiege._started && this instanceof L2PcInstance && ((L2PcInstance)this)._inEventFOS) {
			FortressSiege.setTitleSiegeFlags((L2PcInstance)this);
		}
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, false);
	}

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		if (this instanceof L2PcInstance && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) // true -> ignore waiting room :)
		{
			L2PcInstance player = (L2PcInstance) this;
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		teleToLocation(x, y, z, allowRandomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	// =========================================================
	// Use Augmentation Skill
	public void useAugmentationSkill()
	{
		if (getAugmentationSkillId() != -1)
		{
			try
			{
				L2Object[] targets = new L2Character[] { (L2Character) getTarget() };
				L2Skill tempSkill = SkillTable.getInstance().getInfo(getAugmentationSkillId(), getAugmentationSkillLevel());
				ISkillHandler IHand = SkillHandler.getInstance().getSkillHandler(tempSkill.getSkillType());
				if (IHand != null)
				{
					IHand.useSkill(this, tempSkill, targets);
				}
			}
			catch (Exception e)
			{
				_log.warning("L2Character: Exception during using augmentation skill.");
			}
		}
	}

	// =========================================================
	// Method - Private
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the active weapon (always equiped in the right hand)</li> <BR>
	 * <BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li> <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li> <BR>
	 * <BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)</li> <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li> <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of
	 * the L2Character</li> <li>Notify AI with EVT_READY_TO_ACT</li> <BR>
	 * <BR>
	 *
	 * @param target
	 *            The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if (Config.DEBUG)
		{
			_log.fine(getName() + " doAttack: target=" + target);
		}
        if (target instanceof L2PlayableInstance && this instanceof L2PlayableInstance){
        	L2Object[] o;
        	if (FortressSiege._started) {
				o = eventBlocker(new L2Object[] {target},true); // This adds another check for protected zone in fortress sieges.
			} else {
				o = eventBlocker(new L2Object[] {target},false); // A Filter to take out any players not in events.
			}
        	if (o!=null && o[0]!=null && o[0] instanceof L2Character) {
				target = (L2Character)o[0];
			} else {
				target = null;
			}
        }
		if (isAlikeDead() || target == null || this instanceof L2NpcInstance 
				&& target.isAlikeDead() || this instanceof L2PcInstance 
				&& target.isDead() && !target.isFakeDeath() || !getKnownList().knowsObject(target) || this instanceof L2PcInstance && isDead() || target instanceof L2PcInstance && ((L2PcInstance) target).getDuelState() == Duel.DUELSTATE_DEAD)
		{
			// If L2PcInstance is dead or the target is dead, the action is
			// stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (isAttackingDisabled())
		{
			return;
		}
		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).inObserverMode())
			{
				sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isCursedWeaponEquiped() && ((L2PcInstance) this).getLevel() <= 20)
				{
					((L2PcInstance) this).sendMessage("Can't attack a cursed player when under level 21.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (((L2PcInstance) this).isCursedWeaponEquiped() && ((L2PcInstance) target).getLevel() <= 20)
				{
					((L2PcInstance) this).sendMessage("Can't attack a newbie player using a cursed weapon.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			if (target instanceof L2NpcInstance && Config.DISABLE_ATTACK_NPC_TYPE)
			{
				String mobtype = ((L2NpcInstance) target).getTemplate().type;
				if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Npc Protection No Attack Allowed!");
					((L2PcInstance) this).sendPacket(sm);
					((L2PcInstance) this).sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			// Checking if target has moved to peace zone
			if (target.isInsidePeaceZone((L2PcInstance) this))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		// Get the active weapon item corresponding to the active weapon
		// instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
		{
			// You can't make an attack with a fishing pole.
			((L2PcInstance) this).sendPacket(new SystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			ActionFailed af = ActionFailed.STATIC_PACKET;
			sendPacket(af);
			return;
		}
		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Check for a bow
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.BOW)
		{
			// Check for arrows and MP
			if (this instanceof L2PcInstance)
			{
				// Verify if the bow can be use
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					// Verify if L2PcInstance owns enough MP
					int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
					if (getCurrentMp() < mpConsume)
					{
						// If L2PcInstance doesn't have enough MP, stop the
						// attack
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					// If L2PcInstance have enough MP, the bow consummes it
					getStatus().reduceMp(mpConsume);
					// Set the period of bow non re-use
					_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at
					// this
					// moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				// Equip arrows needed in left hand and send a Server->Client
				// packet ItemList to the L2PcINstance then return true
				if (!checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no
					// arrow
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					sendPacket(ActionFailed.STATIC_PACKET);
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if (this instanceof L2NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
				{
					return;
				}
			}
		}
		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}
		// Recharge any active auto soulshot tasks for player (or player's
		// summon if one exists).
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}
		// Verify if soulshots are charged.
		boolean wasSSCharged;
        if (this instanceof L2NpcInstance) {
			wasSSCharged = ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
		} else if (this instanceof L2Summon && !(this instanceof L2PetInstance))
		{
			wasSSCharged = ((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE;
		}
		else
		{
			wasSSCharged = weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE;
		}
		// Get the Attack Speed of the L2Character (delay (in milliseconds)
		// before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need
		// further tuning e.g. in bow case
		int timeToHit = timeAtk / 2;
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += timeAtk / GameTimeController.MILLIS_IN_TICK;
		_attackEndTime -= 1;
		int ssGrade = 0;
		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
		}
		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged, ssGrade);
		boolean hitted;
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		// Select the type of attack to start
		if (weaponItem == null)
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		else if (this instanceof L2Summon)
		{
			player = ((L2Summon) this).getOwner();
		}
		if (player != null)
		{
			player.updatePvPStatus(target);
		}
		// Check if hit isn't missed
		if (!hitted)
		{
			// Abort the attack of the L2Character and send Server->Client
			// ActionFailed packet
			abortAttack();
		}
		else
		{
			/*
			 * ADDED BY nexus - 2006-08-17 As soon as we know that our hit landed, we must discharge any active soulshots. This must be done so to avoid unwanted soulshot consumption.
			 */
			// If we didn't miss the hit, discharge the shoulshots, if any
			if (this instanceof L2Summon && !(this instanceof L2PetInstance))
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst != null)
			{
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
			if (player != null)
			{
				if (player.isCursedWeaponEquiped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
					{
						// If a cursed weapon is hitted by a Hero, Cp is
						// reduced to 0
						target.setCurrentCp(0);
					}
				}
			}
		}
		// If the Server->Client packet Attack contains at least 1 hit, send the
		// Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of
		// the L2Character
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}

	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li> <li>Consumme arrows</li> <li>If hit isn't missed, calculate if shield defense is efficient</li> <li>If hit isn't missed, calculate if hit is critical</li> <li>If hit isn't missed, calculate physical damages</li> <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge</li> <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li> <li>Add this hit to the Server-Client packet Attack</li> <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @param sAtk
	 *            The Attack Speed of the attacker
	 * @return true if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		// Consumme arrows
		reduceArrowCount();
		_move = null;
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			// Calculate physical damages
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}
		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		// Calculate and set the disable delay of the bow in function of the
		// Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hits are missed or not</li> <li>If hits aren't missed, calculate if shield defense is efficient</li> <li>If hits aren't missed, calculate if hit is critical</li> <li>If hits aren't missed, calculate physical damages</li> <li>Create 2 new hit tasks with Medium priority</li> <li>Add those hits to the Server-Client packet Attack</li> <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @return true if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			// Calculate if hit 1 is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.getInstance().calcShldUse(this, target);
			// Calculate if hit 2 is critical
			crit2 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
		// Create a new hit task with Medium priority for hit 2 with a higher
		// delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2;
	}

	/**
	 * Launch a Pole attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get all visible objects in a spheric area near the L2Character to obtain possible targets</li> <li>If possible target is the L2Character targeted, launch a simple attack against it</li> <li>If possible target isn't the L2Character targeted but is attakable, launch a simple attack against it</li> <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @return true if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, int sAtk)
	{
		boolean hitted = false;
		double angleChar, angleTarget;
		int maxRadius = (int) getStat().calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		if (getTarget() == null)
		{
			return false;
		}
		if (Config.DEBUG)
		{
			_log.info("doAttackHitByPole: Max radius = " + maxRadius);
			_log.info("doAttackHitByPole: Max angle = " + maxAngleDiff);
		}
		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy,
		// dx))
		//
		// o2
		//
		// o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2 / (heading)
		// /
		// o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north
		// east)
		// Degree of o2 in respect to 01 = -40 (320) degree
		// ===========================================================
		// Make sure that char is facing selected target
		angleTarget = Util.calculateAngleFrom(this, getTarget());
		setHeading((int) (angleTarget / 9.0 * 1610.0)); // =
		// this.setHeading((int)((angleTarget / 360.0) * 64400.0));
		// Update char's heading degree
		angleChar = Util.convertHeadingToDegree(getHeading());
		double attackpercent = 85;
		int attackcountmax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3, null, null);
		int attackcount = 0;
		if (angleChar <= 0)
		{
			angleChar += 360;
			// ===========================================================
		}
		L2Character target;
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			// Check if the L2Object is a L2Character
			if (obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance && this instanceof L2PcInstance && ((L2PetInstance) obj).getOwner() == (L2PcInstance) this)
				{
					continue;
				}
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				// otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
				if (Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}
				if (!isFacing(obj, maxAngleDiff))
				{
					continue;
				}
				angleTarget = Util.calculateAngleFrom(this, obj);
				if (Math.abs(angleChar - angleTarget) > maxAngleDiff && Math.abs(angleChar + 360 - angleTarget) > maxAngleDiff && // Example: char is at 1 degree and target is at 359 degree
						Math.abs(angleChar - (angleTarget + 360)) > maxAngleDiff) // Example: target is at 1 degree and char is at 359 degree)
				{
					continue;
				}
				target = (L2Character) obj;
				// Launch a simple attack against the L2Character targeted
				if (!target.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackcountmax)
					{
						if (target == getAI().getAttackTarget() || target.isAutoAttackable(this))
						{
							hitted |= doAttackHitSimple(attack, target, attackpercent, sAtk);
							attackpercent /= 1.15;
						}
					}
				}
			}
		}
		// Return true if one hit isn't missed
		return hitted;
	}

	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li> <li>If hit isn't missed, calculate if shield defense is efficient</li> <li>If hit isn't missed, calculate if hit is critical</li> <li>If hit isn't missed, calculate physical damages</li> <li>Create a new hit task with Medium priority</li> <li>Add this hit to the Server-Client packet Attack</li> <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @return true if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			// Calculate physical damages
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
			if (attackpercent != 100)
			{
				damage1 = (int) (damage1 * attackpercent / 100);
			}
		}
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li> <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li> <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li> <li>Send a Server->Client packet MagicSkillUser (to diplay casting
	 * animation), a packet SetupGauge (to display casting bar) and a system message</li> <li>Disable all skills during the casting time (create a task EnableAllSkills)</li> <li>Disable the skill during the re-use delay (create a task EnableSkill)</li> <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li> <BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			setIsCastingNow(false);
			return;
		}
		if (isSkillDisabled(skill.getId()))
		{
			if (this instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(), skill.getLevel());
				sendPacket(sm);
			}
			return;
		}
		// Check if the skill is a magic spell and if the L2Character is not muted
		if (skill.isMagic() && isMuted() && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			setIsCastingNow(false);
			return;
		}
		// Check if the skill is psychical and if the L2Character is not psychical_muted
		if (!skill.isMagic() && isPsychicalMuted() && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			setIsCastingNow(false);
			return;
		}
		// Can't use Hero and resurrect skills during Olympiad
		if (this instanceof L2PcInstance && ((L2PcInstance) this).isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == SkillType.RESURRECT))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			sendPacket(sm);
			return;
		}
		// prevent casting signets to peace zone
		if (skill.getSkillType() == L2Skill.SkillType.SIGNET || skill.getSkillType() == L2Skill.SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
				return;

			boolean canCast = true;
			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
					canCast = false;
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
				canCast = false;

			if (!canCast)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
				return;
			}
		}
		// Check if the caster own the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Recharge AutoSoulShot
		if (skill.useSoulShot())
		{
            if (this instanceof L2NpcInstance)
				((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
			else if (this instanceof L2PcInstance)
				((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
			else if (this instanceof L2Summon)
				((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}
		else if (skill.useSpiritShot())
		{
			if (this instanceof L2PcInstance)
				((L2PcInstance) this).rechargeAutoSoulShot(false, true, false);
			else if (this instanceof L2Summon)
				((L2Summon) this).getOwner().rechargeAutoSoulShot(false, true, true);
		}
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);
        if (FortressSiege._started && this instanceof L2PcInstance 
        		&& ((L2PcInstance)this)._inEventFOS 
        		&& (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN 
        				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY							
        				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_ALLY
						|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ENEMY_ALLY))
			targets = getFOSTargets(skill);

        if (this instanceof L2PlayableInstance)
        {
        	if (FortressSiege._started && skill.isOffensive())
				targets = eventBlocker(targets,true); // Special check made here, to not allow players in protected zone to be hit by players IN THE EVENT (a.k.a. safe zone)
			else
				targets = eventBlocker(targets,false); // A Filter to take out any playables not in events or vice versa.
        }
		if (targets == null || targets.length == 0)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = (L2Character) targets[0];
		if (skill.getSkillType() == SkillType.BUFF 
				|| skill.getSkillType() == SkillType.HEAL 
				|| skill.getSkillType() == SkillType.COMBATPOINTHEAL 
				|| skill.getSkillType() == SkillType.MANAHEAL 
				|| skill.getSkillType() == SkillType.REFLECT 
				|| skill.getSkillType() == SkillType.SEED 
				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF
				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET 
				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY 
				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN 
				|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY)
		{
			if (this instanceof L2PcInstance && target instanceof L2PcInstance && target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (skill.getSkillType() == SkillType.BUFF 
						|| skill.getSkillType() == SkillType.HOT 
						|| skill.getSkillType() == SkillType.HEAL 
						|| skill.getSkillType() == SkillType.HEAL_PERCENT 
						|| skill.getSkillType() == SkillType.MANAHEAL 
						|| skill.getSkillType() == SkillType.MANAHEAL_PERCENT 
						|| skill.getSkillType() == SkillType.BALANCE_LIFE)
					target.setLastBuffer(this);
				if (((L2PcInstance) this).isInParty() 
						&& skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
				{
					for (L2PcInstance member : ((L2PcInstance) this).getParty().getPartyMembers())
					{
						member.setLastBuffer(this);
					}
				}
			}
		}
		else
		{
			target = (L2Character) getTarget();
			// Prevent usage of skills on NPCs that shouldn't allow this
			// (teleporters, merchants, trainers, etc)
			if (target instanceof L2NpcInstance)
			{
				String mobtype = ((L2NpcInstance) target).getTemplate().type;
				if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Npc Protection No Skills Allowed!");
					((L2PcInstance) this).sendPacket(sm);
					((L2PcInstance) this).sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		// AURA skills should always be using caster as target
		if (skill.getTargetType() == SkillTargetType.TARGET_AURA 
				|| skill.getTargetType() == SkillTargetType.TARGET_GROUND)
			target = this;

		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			setIsCastingNow(false);
			return;
		}
		setLastSkillCast(skill);
		// Get the Identifier of the skill
		int magicId = skill.getId();
		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();
		// Get the level of the skill
		int level = skill.getLevel();
		if (level < 1)
			level = 1;

		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		boolean effectWhileCasting = skill.getSkillType() == SkillType.FORCE_BUFF || skill.getSkillType() == SkillType.SIGNET_CASTTIME;
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for
		// those skills represent the buff time.
		if (!effectWhileCasting)
		{
			hitTime = Formulas.getInstance().calcMAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
				coolTime = Formulas.getInstance().calcMAtkSpd(this, skill, coolTime);
		}
		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null && skill.isMagic() 
				&& !effectWhileCasting && skill.getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT 
					|| weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				// Only takes 70% of the time to cast a BSpS/SpS cast
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				// Because the following are magic skills that do not actively 'eat' BSpS/SpS,
				// I must 'eat' them here so players don't take advantage of // infinite speed increase
				switch (skill.getSkillType())
				{
					case BUFF:
					case MANAHEAL:
					case RESURRECT:
					case RECALL:
					case DOT:
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						break;
				}
			}
		}
        else if (this instanceof L2NpcInstance && skill.useSpiritShot() && !effectWhileCasting)
        {
            if(((L2NpcInstance)this).rechargeAutoSoulShot(false, true))
            {
                hitTime = (int)(0.70 * hitTime);
                coolTime = (int)(0.70 * coolTime);
            }
        }
		// Set the _castEndTime and _castInterruptTim. +10 ticks for lag
		// situations, will be reseted in onMagicFinalizer
		setIsCastingNow(true);
		_castEndTime = 10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = -2 + GameTimeController.getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
		// Init the reuse time of the skill
		int reuseDelay = (int) (skill.getReuseDelay() * getStat().getMReuseRate(skill));
		reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() / Config.SK_MAG : getPAtkSpd() / Config.SK_FIG);
		// Skill reuse check
		if (reuseDelay > 30000)
			addTimeStamp(skill.getId(), reuseDelay);

		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE, initmpcons, null, null));
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
			disableSkill(skill.getId(), reuseDelay);

		// Make sure that char is facing selected target
		setHeading(Util.calculateHeadingFrom(this, target));
		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());

			if (skill.getSkillType() == SkillType.FORCE_BUFF)
				startForceBuff(target, skill);
			else
				callSkill(skill, targets);
		}
		// Potions Reuse
		if (skill.isPotion())
		{
			if (skill.getId() >= 2287 && skill.getId() <= 2289)
				reuseDelay = Config.ELIXIRS_REUSE_DELAY;
			if (reuseDelay > 0)
				disableSkill(skill.getId(), reuseDelay);
		}
		// Send a Server->Client packet MagicSkillUser with target, displayId,
		// level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of
		// the L2Character
		broadcastPacket(new MagicSkillUser(this, target, displayId, level, hitTime, reuseDelay));
		// Send a system message USE_S1 to the L2Character
		if (this instanceof L2PcInstance && magicId != 1312)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
			sm.addSkillName(magicId, skill.getLevel());
			sendPacket(sm);
		}
		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the
			// gauge and the casting time
			if (this instanceof L2PcInstance && !effectWhileCasting)
			{
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, hitTime);
				sendPacket(sg);
			}
			// Disable all skills during the casting
			disableAllSkills();
			
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			// Create a task MagicUseTask to launch the MagicSkill at the
			// end of the casting time (hitTime)
			// For client animation reasons (party buffs especially) 200 ms
			// before!
			if (effectWhileCasting)
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
			else
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
		} else
			onMagicLaunchedTimer(targets, skill, coolTime, true);
	}

    /**
	    *Since in this event we create 2 clans, all ally,clan skills should be directed to the event participators
	    *@param L2Skill skill that is casted
	    *@return L2Object[] target list of the event participators
	    **/
	    private L2Object[] getFOSTargets(L2Skill skill)
	    {
	    	L2Skill.SkillTargetType type = skill.getTargetType();
	    	FastTable<L2PcInstance> targetList = new FastTable<L2PcInstance>();
	    	if (type == L2Skill.SkillTargetType.TARGET_CLAN || type == L2Skill.SkillTargetType.TARGET_ALLY)
	    	{
				for(L2PcInstance member : FortressSiege._players)
				{
					if (member._teamNameFOS.equals(((L2PcInstance)this)._teamNameFOS) 
							&& member.isInsideRadius(this, skill.getSkillRadius(), true, false))
						targetList.add(member);
					else if (type == L2Skill.SkillTargetType.TARGET_ENEMY_ALLY)
					{
						for(L2PcInstance target : FortressSiege._players)
						{
							if (!target._teamNameFOS.equals(((L2PcInstance)this)._teamNameFOS) 
									&& target.isInsideRadius(this, skill.getSkillRadius(), true, false))
								targetList.add(target);
						}
					}
				}
			}
	    	return targetList.toArray(new L2Object[targetList.size()]);
	    }

	    /**
	     * Function is used to filter out targets during an event.
	     * @param targets - Target list L2Object[] received
	     * @param offensive - true to check Offensive skills or hits from players IN THE EVENT as well. false to check ALL interference
	     * @return L2Object[] - New target list, with event participants excluded
	     */
	    private L2Object[] eventBlocker(L2Object[] targets, boolean offensive)
	    {
	        try
	        {
	        	//If events are not running skip this check. Code efficiency.
	        	if (!TvT._started && !CTF._started && !DM._started && !FortressSiege._started)
					return targets;

	        	//Let's find the caster:
	        	L2PcInstance caster = null;
	        	if (this instanceof L2PcInstance)
					caster = (L2PcInstance)this;
				else if (this instanceof L2Summon)
					caster = ((L2Summon)this).getOwner();

	        	if (caster == null)
					return targets;

	        	//Check the targets:
	        	for (int x=0 ; x<targets.length ; x++)
	        	{
	            	if (targets[x] == null || !(targets[x] instanceof L2PlayableInstance))
						continue; // Don't check mobs

	            	//Find the player to check if he is in event confronting him with the caster:
	            	L2PcInstance target = null;
	            	if (targets[x] instanceof L2PcInstance)
						target = (L2PcInstance)targets[x];
					else if (targets[x] instanceof L2Summon)
						target = ((L2Summon)targets[x]).getOwner();

	            	if (target == null)
						continue;

	            	//Run the check, change off limit targets to null:
	            	if (TvT._started && !Config.TVT_ALLOW_INTERFERENCE 
	            			|| CTF._started && !Config.CTF_ALLOW_INTERFERENCE 
	            			|| DM._started && !Config.DM_ALLOW_INTERFERENCE 
	            			|| FortressSiege._started && !Config.FortressSiege_ALLOW_INTERFERENCE)
	            	{
	                    if (target._inEventTvT && !caster._inEventTvT 
	                    		|| !target._inEventTvT && caster._inEventTvT)
							targets[x]=null;
						else if (target._inEventCTF && !caster._inEventCTF 
								|| !target._inEventCTF && caster._inEventCTF)
							targets[x]=null;
						else if (target._inEventDM && !caster._inEventDM 
								|| !target._inEventDM && caster._inEventDM)
							targets[x]=null;
						else if (target._inEventFOS && !caster._inEventFOS 
								|| !target._inEventFOS && caster._inEventFOS)
							targets[x]=null;
						else if (FortressSiege._started && offensive)
						{
							if (target._inEventFOS && FortressSiege.inProtectedZone((L2Character)targets[x],caster) 
									|| caster._inEventFOS && FortressSiege.inProtectedZone(this,caster))
								targets[x]=null;
						}
	            	}
	        	}
	        	//At this point we have an array of targets which some are null, let's clean this array:
	        	return removeAllNullTargets(targets);
	        }
	        catch (Throwable t)
	        {
	        	return removeAllNullTargets(targets);
	        }
	    }

		/**
		* @param targets a target list with null targets
		* @return a target list without null targets
		*/
	    public L2Object[] removeAllNullTargets(L2Object[] targets)
	    {
			if (targets == null)
				return null;

			int size = 0;
			for (L2Object target : targets)
			{
				if (target!=null)
					size++;
			}
			if (size == 0)
				return null;

			L2Object[] newTargets = new L2Object[size];
			size = 0;
			for (L2Object target : targets)
			{
				if (target!=null){
					newTargets[size] = target;
					size++;
				}
			}
			return newTargets;
		}

	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 *
	 * @param skill
	 *            id
	 * @param reuse
	 *            delay <BR>
	 *            <B>Overriden in :</B> (L2PcInstance)
	 */
	public void addTimeStamp(int s, int r)
	{
	}

	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 *
	 * @param skill
	 *            id <BR>
	 *            <B>Overriden in :</B> (L2PcInstance)
	 */
	public void removeTimeStamp(int s)
	{
		/***/
	}

	/**
	 * Starts a force buff on target.<br>
	 * <br>
	 *
	 * @param caster
	 * @param force
	 *            type <BR>
	 *            <B>Overriden in :</B> (L2PcInstance)
	 */
	public void startForceBuff(L2Character caster, L2Skill skill)
	{
        if (skill.getSkillType() != SkillType.FORCE_BUFF) {
			return;
		}

        if (_forceBuff == null) {
			_forceBuff = new ForceBuff(this, caster, skill);
		}
	}

	/**
	 * Kill the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set target to null and cancel Attack or Cast</li> <li>Stop movement</li> <li>Stop HP/MP/CP Regeneration task</li> <li>Stop all active skills effects in progress on the L2Character</li> <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li> <li>Notify L2Character AI</li> <BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds</li> <li>L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine</li> <li>L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop</li> <BR>
	 * <BR>
	 *
	 * @param killer
	 *            The L2Character who killed it
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isKilledAlready())
				return false;
			setIsKilledAlready(true);
		}
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		// Stop movement
		stopMove(null);
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		// Stop all active skills effects in progress on the L2Character, if the Character isn't a Noblesse Blessed L2PlayableInstance
		if (this instanceof L2PlayableInstance && (((L2PlayableInstance) this).isSoulOfThePhoenix() || this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isSalvation()))
		{
			((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, false);
			if (((L2PlayableInstance) this).isSoulOfThePhoenix() && ((L2PcInstance) this)._reviveRequested == 0)
				((L2PlayableInstance) this).stopSoulOfThePhoenix(null);
			if (((L2PlayableInstance) this).isSalvation() && ((L2PcInstance) this)._reviveRequested == 0)
				((L2PlayableInstance) this).stopSalvation(null);
		}
		else if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isNoblesseBlessed())
		{
			((L2PlayableInstance) this).stopNoblesseBlessing(null);
			if (((L2PlayableInstance) this).getCharmOfLuck()) // remove Lucky Charm if player have Nobless blessing buff
				((L2PlayableInstance) this).stopCharmOfLuck(null);
			if (((L2PlayableInstance) this).isSoulOfThePhoenix() || ((L2PlayableInstance) this).isSalvation())
			{
				((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, false);
				if (((L2PlayableInstance) this).isSoulOfThePhoenix() && ((L2PcInstance) this)._reviveRequested == 0)
					((L2PlayableInstance) this).stopSoulOfThePhoenix(null);
				if (((L2PlayableInstance) this).isSalvation() && ((L2PcInstance) this)._reviveRequested == 0)
					((L2PlayableInstance) this).stopSalvation(null);
			}
		}
		else if(Config.REM_BUFF)
        {
			stopAllEffects();
        }

		calculateRewards(killer);
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		if (getWorldRegion() != null)
			getWorldRegion().onDeath(this);

		getAttackByList().clear();
		return true;
	}

	protected void calculateRewards(L2Character killer)
	{
	}

	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		//if (!isDead()) return;

		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			// Check if player is in faction or in jail
			if (this instanceof L2PcInstance && ((L2PcInstance) this).isInJail())
				teleToLocation(-114597, -249447, -2986);
			else if (this instanceof L2PcInstance && ((L2PcInstance) this).isKoof() 
					&& Config.ENABLE_FACTION_KOOFS_NOOBS)
				teleToLocation(146334, 25767, -2013);

			if (this instanceof L2PcInstance && ((L2PcInstance) this).isNoob() 
					&& Config.ENABLE_FACTION_KOOFS_NOOBS)
				teleToLocation(59669, -42221, -2992);

			if (this instanceof L2PcInstance && (((L2PcInstance) this).isImmobilized() || ((L2PcInstance) this).isParalyzed() || ((L2PcInstance) this).isPetrified() || ((L2PcInstance) this).isRooted() || ((L2PcInstance) this).isSleeping() || ((L2PcInstance) this).isStunned()))
			{
				stopEffects(L2Effect.EffectType.PARALYZE);
				stopEffects(L2Effect.EffectType.PETRIFICATION);
				stopEffects(L2Effect.EffectType.ROOT);
				stopEffects(L2Effect.EffectType.SLEEP);
				stopEffects(L2Effect.EffectType.STUN);
				stopEffects(L2Effect.EffectType.STUN_SELF);
			}
			if (this instanceof L2PlayableInstance && (((L2PlayableInstance) this).isSoulOfThePhoenix() || ((L2PlayableInstance) this).isSalvation()))
			{
				((L2PlayableInstance) this).stopSoulOfThePhoenix(null);
				((L2PlayableInstance) this).stopSalvation(null);
				_status.setCurrentCp(getMaxCp());
				_status.setCurrentHp(getMaxHp());
				_status.setCurrentMp(getMaxMp());
			}
			else
			{
				_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				// _Status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}
			// Start broadcast status
			broadcastPacket(new Revive(this));
			if (getWorldRegion() != null)
				getWorldRegion().onRevive(this);
		}
		else
			setIsPendingRevive(true);
	}

	/** Revives the L2Character using skill. */
	public void doRevive(double revivePower)
	{
		doRevive();
	}

	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the L2Character can cast (ex : not sleeping...)</li> <li>Check if the target is correct</li> <li>Notify the AI with AI_INTENTION_CAST and target</li> <BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 */
	protected void useMagic(L2Skill skill)
	{
		if (skill == null || isDead()) return;

		// Check if the L2Character can cast
		if (isAllSkillsDisabled())
			// must be checked by caller
			return;

		// Ignore the passive skill request. why does the client send it anyway ??
		if (skill.isPassive()) return;

		// Get the target for the skill
		L2Object target = null;
		switch (skill.getTargetType())
		{
			case TARGET_AURA: // AURA, SELF should be cast even if no target
			case TARGET_GROUND:
				// has been found
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	// =========================================================
	// Property - Public
	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.
	 */
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2CharacterAI(new AIAccessor());
			}
		}
		return _ai;
	}

	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
			((L2AttackableAI) oldAI).stopAITask();

		_ai = newAI;
	}

	/** Return true if the L2Character has a L2CharacterAI. */
	public boolean hasAI()
	{
		return _ai != null;
	}

	/** Return true if the L2Character is RaidBoss or his minion. */
	public boolean isRaid()
	{
		return _isRaid;
	}

	/**
	 * Set this Npc as a Raid instance.<BR>
	 * <BR>
	 *
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}

	/** Return a list of L2Character that attacked. */
	public final List<L2Character> getAttackByList()
	{
		if (_attackByList == null)
			_attackByList = new FastList<L2Character>();

		return _attackByList;
	}

	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}

	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}

	public final boolean isAfraid()
	{
		return _isAfraid;
	}

	public final void setIsAfraid(boolean value)
	{
		_isAfraid = value;
	}

	/** Return true if the L2Character is dead or use fake death. */
	public final boolean isAlikeDead()
	{
		return isFakeDeath() || !(getCurrentHp() > 0.5);
	}

	/**
	 * Return true if the L2Character can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isImmobileUntilAttacked() || isStunned() || isSleeping() || isParalyzed() || isPetrified();
	}

	public final boolean isPotionsDisabled()
	{
		return isStunned() || isSleeping() || isParalyzed();
	}

	/** 
	 * Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse, attackMute). 
	 **/
	public boolean isAttackingDisabled()
	{
		return isStunned() 
		|| isImmobileUntilAttacked() 
		|| isSleeping() 
		|| _attackEndTime > GameTimeController.getGameTicks() 
		|| isFakeDeath() 
		|| isParalyzed() 
		|| isPetrified() 
		|| isFallsdown() 
		|| isCoreAIDisabled();
	}

	public final Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final boolean isConfused()
	{
		return _isConfused;
	}

	public final void setIsConfused(boolean value)
	{
		_isConfused = value;
	}

	/** Return True if the L2Character is dead. */
	public final boolean isDead()
	{
		return !isFakeDeath() && !(getCurrentHp() > 0.5);
	}

	public final boolean isKilledAlready()
	{
		return _isKilledAlready;
	}
	public final void setIsKilledAlready(boolean value)
	{
		_isKilledAlready = value;
	}

	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}

	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}

	public final boolean isFallsdown()
	{
		return _isFallsdown;
	}

	public final void setIsFallsdown(boolean value)
	{
		_isFallsdown = value;
	}

	/** Return true if the L2Character is flying. */
	public final boolean isFlying()
	{
		return _isFlying;
	}

	/** Set the L2Character flying mode to true. */
	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}

	public boolean isImmobilized()
	{
		return _isImmobilized;
	}

	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}

	public final boolean isMuted()
	{
		return _isMuted;
	}

	public final void setIsMuted(boolean value)
	{
		_isMuted = value;
	}

	public final boolean isPsychicalMuted()
	{
		return _isPsychicalMuted;
	}

	public final void setIsPsychicalMuted(boolean value)
	{
		_isPsychicalMuted = value;
	}

	/**
	 * Return true if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 */
	public boolean isMovementDisabled()
	{
		return isStunned() || isRooted() || isSleeping() 
		|| isOverloaded() || isParalyzed() || isImmobilized() 
		|| isFakeDeath() || isPetrified() || isFallsdown();
	}

	/**
	 * Return true if the L2Character can be controlled by the player (confused, afraid).
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}

	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}

	/**
	 * Set the overloaded status of the L2Character is overloaded (if true, the L2PcInstance can't take more item).
	 */
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}

	public final boolean isParalyzed()
	{
		return _isParalyzed;
	}

	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}

	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}

	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	/**
	 * Return the L2Summon of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public L2Summon getPet()
	{
		return null;
	}

	/** Return true if the L2Character is ridding. */
	public final boolean isRiding()
	{
		return _isRiding;
	}

	/** Set the L2Character riding mode to true. */
	public final void setIsRiding(boolean mode)
	{
		_isRiding = mode;
	}

	public final boolean isRooted()
	{
		return _isRooted;
	}

	public final void setIsRooted(boolean value)
	{
		_isRooted = value;
	}

	/** Return true if the L2Character is running. */
	public final boolean isRunning()
	{
		return _isRunning;
	}

	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		broadcastPacket(new ChangeMoveType(this));
	}

	/**
	 * Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setRunning()
	{
		if (!isRunning())
			setIsRunning(true);
	}

	public final boolean isSleeping()
	{
		return _isSleeping;
	}

	public final void setIsSleeping(boolean value)
	{
		_isSleeping = value;
	}

	public final void setIsImmobileUntilAttacked(boolean value)
	{
		_isImmobileUntilAttacked = value;
	}

	public final boolean isImmobileUntilAttacked()
	{
		return _isImmobileUntilAttacked;
	}

	public final boolean isStunned()
	{
		return _isStunned;
	}

	public final void setIsStunned(boolean value)
	{
		_isStunned = value;
	}

	public final boolean isPetrified()
	{
		return _isPetrified;
	}

	public final void setIsPetrified(boolean value)
	{
		_isPetrified = value;
	}

	public final boolean isBetrayed()
	{
		return _isBetrayed;
	}

	public final void setIsBetrayed(boolean value)
	{
		_isBetrayed = value;
	}

	// buffs, debuffs Immunity
	public final boolean getBuffImmunity()
	{
		return _buffImmunity;
	}

	public final void setBuffImmunity(boolean value)
	{
		_buffImmunity = value;
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}

	public boolean isUndead()
	{
		return _template.isUndead;
	}

	@Override
	public CharKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof CharKnownList))
			setKnownList(new CharKnownList(this));

		return (CharKnownList) super.getKnownList();
	}

	public CharStat getStat()
	{
		if (_stat == null)
			_stat = new CharStat(this);

		return _stat;
	}

	public final void setStat(CharStat value)
	{
		_stat = value;
	}

	public CharStatus getStatus()
	{
		if (_status == null) {
			_status = new CharStatus(this);
		}
		return _status;
	}

	public final void setStatus(CharStatus value)
	{
		_status = value;
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	/**
	 * Set the template of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is spawned, server just create a link between the instance and the template This link is
	 * stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2Character</li> <BR>
	 * <BR
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	/** Return the Title of the L2Character. */
	public final String getTitle()
	{
		return _title;
	}

	/** Set the Title of the L2Character. */
	public final void setTitle(String value)
	{
		_title = value;
	}

	/**
	 * Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setWalking()
	{
		if (isRunning()) {
			setIsRunning(false);
		}
	}

	/** Task lauching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int _skillId;

		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}

		public void run()
		{
			try
			{
				enableSkill(_skillId);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * Task lauching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li> <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li> <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection
	 * damage to reduce HP of attacker if necessary</li> <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li> <BR>
	 * <BR>
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
				// check augmentation skill effect
				if (getAugmentationSkillType() == 0 || getAugmentationSkillType() == 1) {
					useAugmentationSkill();
				}
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}

	/** Task lauching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		int _coolTime;
		int _phase;

		public MagicUseTask(L2Object[] targets, L2Skill skill, int coolTime, int phase)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
		}

		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
						break;
					case 2:
						onMagicHitTimer(_targets, _skill, _coolTime, false);
						break;
					case 3:
						onMagicFinalizer(_skill);
						break;
					default:
						break;
				}
				// check augmentation skill effect
				if (getAugmentationSkillType() == 0 || getAugmentationSkillType() == 2)
				{
					useAugmentationSkill();
				}
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
				enableAllSkills();
			}
		}
	}

	/** Task lauching the function useMagic() */
	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;

		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}

		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "", t);
			}
		}
	}

	/** Task lauching the function stopPvPFlag() */
	class PvPFlag implements Runnable
	{
		public PvPFlag()
		{
		}

		public void run()
		{
			try
			{
				// _log.fine("Checking pvp time: " + getlastPvpAttack());
				// "lastattack: " _lastAttackTime "currenttime: "
				// System.currentTimeMillis());
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					// _log.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > getPvpFlagLasts() - 5000)
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					// checkPvPFlag();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "error in pvp flag task:", e);
			}
		}
	}

	// =========================================================
	// =========================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS
	// COMPLETE
	// Data Field
	/** Map 32 bits (0x0000) containing all abnormal effect in progress */
	private int _AbnormalEffects;
	/**
	 * FastTable containing all active skills effects in progress of a L2Character.
	 */
	private FastTable<L2Effect> _effects;
	/**
	 * The table containing the List of all stacked effect in progress for each Stack group Identifier
	 */
	protected Map<String, List<L2Effect>> _stackedEffects;
	protected Map<Integer, L2CubicInstance> _cubics = new FastMap<Integer, L2CubicInstance>();
	/**
	 * Table EMPTY_EFFECTS shared by all L2Character without effects in progress
	 */
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	public static final int ABNORMAL_EFFECT_BLEEDING = 0x000001;
	public static final int ABNORMAL_EFFECT_POISON = 0x000002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3 = 0x000004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4 = 0x000008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5 = 0x000010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6 = 0x000020;
	public static final int ABNORMAL_EFFECT_STUN = 0x000040;
	public static final int ABNORMAL_EFFECT_SLEEP = 0x000080;
	public static final int ABNORMAL_EFFECT_MUTED = 0x000100;
	public static final int ABNORMAL_EFFECT_ROOT = 0x000200;
	public static final int ABNORMAL_EFFECT_HOLD_1 = 0x000400;
	public static final int ABNORMAL_EFFECT_HOLD_2 = 0x000800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x001000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD = 0x002000;
	public static final int ABNORMAL_EFFECT_FLAME = 0x004000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x008000;
	public static final int ABNORMAL_EFFECT_GROW = 0x010000;
	public static final int ABNORMAL_EFFECT_FLOATING_ROOT = 0x020000;
	public static final int ABNORMAL_EFFECT_DANCE_STUNNED = 0x040000;
	public static final int ABNORMAL_EFFECT_FIREROOT_STUN = 0x080000;
	public static final int ABNORMAL_EFFECT_STEALTH = 0x100000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_1 = 0x200000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_2 = 0x400000;
	public static final int ABNORMAL_EFFECT_MAGIC_CIRCLE = 0x800000;
	// XXX TEMP HACKS (get the proper mask for these effects)
	public static final int ABNORMAL_EFFECT_CONFUSED = 0x0020;
	public static final int ABNORMAL_EFFECT_AFRAID = 0x0010;

	// Method - Public
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the L2Effect to the L2Character _effects</li> <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li> <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li> <li>If this effect has
	 * NOT higher priority in its Stack Group, set the effect to Not In Use</li> <li>Update active skills in progress icones on player client</li> <BR>
	 */
	public final void addEffect(L2Effect newEffect)
	{
		if (newEffect == null) {
			return;
		}
		synchronized (this)
		{
			if (_effects == null) {
				_effects = new FastTable<L2Effect>();
			}
			if (_stackedEffects == null) {
				_stackedEffects = new FastMap<String, List<L2Effect>>();
			}
		}
		synchronized (_effects)
		{
			// check buff immunity
			if (getBuffImmunity() && newEffect.getEffectType() != L2Effect.EffectType.BUFFIMMUNITY)
			{
				// we have immunity to buff / debuffs
				SkillType typeSkill = newEffect.getSkill().getSkillType();
				if (typeSkill == L2Skill.SkillType.BUFF || typeSkill == L2Skill.SkillType.DEBUFF) {
					return;
				}
			}
			L2Effect tempEffect = null;
            // Make sure there's no same effect previously
			for (int i = 0; i < _effects.size(); i++)
			{
                if (_effects.get(i).getSkill().getId() == newEffect.getSkill().getId() && _effects.get(i).getEffectType() == newEffect.getEffectType())
                {
					// Started scheduled timer needs to be canceled. There could be a nicer fix...
					newEffect.stopEffectTask();
					return;
                }
			}
			// Remove first Buff if number of buffs > 19
			L2Skill tempskill = newEffect.getSkill();
			if (getBuffCount() > getMaxBuffCount() && !doesStack(tempskill) && (tempskill.getSkillType() == L2Skill.SkillType.BUFF || tempskill.getSkillType() == L2Skill.SkillType.DEBUFF || tempskill.getSkillType() == L2Skill.SkillType.REFLECT || tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT || tempskill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)
					&& !(tempskill.getId() > 4360 && tempskill.getId() < 4367))
			{
				// if max buffs, no herb effects are used, even if they would replace one old
				if (newEffect.isHerbEffect())
				{
					newEffect.stopEffectTask();
					return;
				}
				removeFirstBuff(tempskill.getId());
			}
			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isToggle())
			{
				int pos = 0;
				for (int i = 0; i < _effects.size(); i++)
				{
					if (_effects.get(i) != null)
					{
						int skillid = _effects.get(i).getSkill().getId();
						if (!_effects.get(i).getSkill().isToggle() && !(skillid > 4360 && skillid < 4367))
						{
							pos++;
						}
					}
					else
					{
						break;
					}
				}
				_effects.add(pos, newEffect);
			}
			else
			{
				_effects.addLast(newEffect);
			}
			// Check if a stack group is defined for this effect
			if (newEffect.getStackType().equals("none"))
			{
				// Set this L2Effect to In Use
				newEffect.setInUse(true);
				// Add Funcs of this effect to the Calculator set of the
				// L2Character
				addStatFuncs(newEffect.getStatFuncs());
				// Update active skills in progress icones on player client
				updateEffectIcons();
				return;
			}
			// Get the list of all stacked effects corresponding to the
			// stack
			// type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());
			if (stackQueue == null)
			{
				stackQueue = new FastList<L2Effect>();
			}
			if (stackQueue.size() > 0)
			{
				// Get the first stacked effect of the Stack group selected
				tempEffect = null;
				for (int i = 0; i < _effects.size(); i++)
				{
					if (_effects.get(i) == stackQueue.get(0))
					{
						tempEffect = _effects.get(i);
						break;
					}
				}
				if (tempEffect != null)
				{
					// Remove all Func objects corresponding to this stacked
					// effect from the Calculator set of the L2Character
					removeStatsOwner(tempEffect);
					// Set the L2Effect to Not In Use
					tempEffect.setInUse(false);
				}
			}
			// Add the new effect to the stack group selected at its
			// position
			stackQueue = effectQueueInsert(newEffect, stackQueue);
			if (stackQueue == null)
			{
				return;
			}
			// Update the Stack Group table _stackedEffects of the
			// L2Character
			_stackedEffects.put(newEffect.getStackType(), stackQueue);
			// Get the first stacked effect of the Stack group selected
			tempEffect = null;
			for (int i = 0; i < _effects.size(); i++)
			{
				if (_effects.get(i) == stackQueue.get(0))
				{
					tempEffect = _effects.get(i);
					break;
				}
			}
			// Set this L2Effect to In Use
			tempEffect.setInUse(true);
			// Add all Func objects corresponding to this stacked effect to
			// the
			// Calculator set of the L2Character
			addStatFuncs(tempEffect.getStatFuncs());
		}
		// Update active skills in progress (In Use and Not In Use because
		// stacked) icones on client
		updateEffectIcons();
	}

	/**
	 * Insert an effect at the specified position in a Stack Group.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 *
	 * @param id
	 *            The identifier of the stacked effect to add to the Stack Group
	 * @param stackOrder
	 *            The position of the effect in the Stack Group
	 * @param stackQueue
	 *            The Stack Group in wich the effect must be added
	 */
	private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
	{
		// Get the L2Effect corresponding to the Effect Identifier from the
		// L2Character _effects
		if (_effects == null)
		{
			return null;
		}
		// Create an Iterator to go through the list of stacked effects in
		// progress on the L2Character
		Iterator<L2Effect> queueIterator = stackQueue.iterator();
		int i = 0;
		while (queueIterator.hasNext())
		{
			L2Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
			{
				i++;
			}
			else
			{
				break;
			}
		}
		// Add the new effect to the Stack list in function of its position in
		// the Stack group
		stackQueue.add(i, newStackedEffect);
		// skill.exit() could be used, if the users don't wish to see "effect
		// removed" always when a timer goes off, even if the buff isn't active
		// any more (has been replaced). but then check e.g. npc hold and raid
		// petrify.
		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && stackQueue.size() > 1)
		{
			// only keep the current effect, cancel other effects
			for (int n = 0; n < _effects.size(); n++)
			{
				if (_effects.get(n) == stackQueue.get(1))
				{
					_effects.remove(n);
					break;
				}
			}
			stackQueue.remove(1);
		}
		return stackQueue;
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li> <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li> <li>Remove the L2Effect from _effects of the L2Character</li> <li>Update active skills in progress icones on player client</li> <BR>
	 */
	public final void removeEffect(L2Effect effect)
	{
		if (effect == null || _effects == null) {
			return;
		}
		synchronized (_effects)
		{
			if (effect.getStackType() == "none") {
				// Remove Func added by this effect from the L2Character Calculator
				removeStatsOwner(effect);
			} else
			{
				if (_stackedEffects == null) {
					return;
				}
				// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
				List<L2Effect> stackQueue = _stackedEffects.get(effect.getStackType());
				if (stackQueue == null || stackQueue.size() < 1) {
					return;
				}
				// Get the Identifier of the first stacked effect of the Stack group selected
				L2Effect frontEffect = stackQueue.get(0);
				// Remove the effect from the Stack Group
				boolean removed = stackQueue.remove(effect);
				if (removed)
				{
					// Check if the first stacked effect was the effect to remove
					if (frontEffect == effect)
					{
						// Remove all its Func objects from the L2Character calculator set
						removeStatsOwner(effect);
						// Check if there's another effect in the Stack Group
						if (stackQueue.size() > 0)
						{
							// Add its list of Funcs to the Calculator set of the L2Character
							for (int i = 0; i < _effects.size(); i++)
							{
								if (_effects.get(i) == stackQueue.get(0))
								{
									// Add its list of Funcs to the Calculator set of the L2Character
									addStatFuncs(_effects.get(i).getStatFuncs());
									// Set the effect to In Use
									_effects.get(i).setInUse(true);
									break;
								}
							}
						}
					}
					if (stackQueue.isEmpty())
					{
						_stackedEffects.remove(effect.getStackType());
					}
					else
					{
						// Update the Stack Group table _stackedEffects of the
						// L2Character
						_stackedEffects.put(effect.getStackType(), stackQueue);
					}
				}
			}
			// Remove the active skill L2effect from _effects of the L2Character The Integer key of _effects is the L2Skill Identifier that has created the effect
			for (int i = 0; i < _effects.size(); i++)
			{
				if (_effects.get(i) == effect)
				{
					_effects.remove(i);
					break;
				}
			}
		}
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}

	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFakeDeath()
	{
		setIsFallsdown(true);
		if (Config.FAILD_FAKEDEATH)
		{
			// It fails in Fake Death at the probability
			setIsFakeDeath(true);
			if (getAttackByList() != null && !getAttackByList().isEmpty())
			{
				boolean isRaid = false;
				int highestLevel = this.getLevel();
				for (L2Character atkChar : getAttackByList())
				{
					if (atkChar.isRaid())
					{
						isRaid = true;
						break;
					}
					if (atkChar != null && atkChar.getLevel() > highestLevel)
						highestLevel = atkChar.getLevel();
				}
				// If L2RaidBoss in getAttackByList(), Fake Death will have failed.
				if (isRaid)
					setIsFakeDeath(false);
				else
				{
					int _diff = highestLevel - this.getLevel();
					switch (_diff)
					{
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
							if (Rnd.get(100) >= 95)
								setIsFakeDeath(false);
							break;
						case 6:
							if (Rnd.get(100) >= 90)
								setIsFakeDeath(false);
							break;
						case 7:
							if (Rnd.get(100) >= 85)
								setIsFakeDeath(false);
							break;
						case 8:
							if (Rnd.get(100) >= 80)
								setIsFakeDeath(false);
							break;
						case 9:
							if (Rnd.get(100) >= 75)
								setIsFakeDeath(false);
							break;
						default:
							if (_diff > 9)
							{
								if (Rnd.get(100) >= 50)
									setIsFakeDeath(false);
							}
							else
								setIsFakeDeath(true);
					}
				}
			}
			else
			{
				// attacked from aggressive monster
				if (Rnd.get(100) >= 75)
					setIsFakeDeath(false);
			}
		}
		else
			setIsFakeDeath(true);
		// [L2J_JP ADD END]
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startMuted()
	{
		setIsMuted(true);
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startPsychicalMuted()
	{
		setIsPsychicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startRooted()
	{
		setIsRooted(true);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startSleeping()
	{
		setIsSleeping(true);
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li> <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li> <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li> <BR>
	 * <BR>
	 */
	public final void startStunning()
	{
		setIsStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}

	public final void startParalyze()
	{
		setIsParalyzed(true);
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED, null);
		updateAbnormalEffect();
	}

	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}

	public final void stopBetray()
	{
		stopEffects(L2Effect.EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR>
	 * <BR>
	 */
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}

	/**
	 * Stop all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;

		// Go through all active skills effects
		for (L2Effect e : effects)
		{
			if (e != null)
				e.exit(true);
		}
		if (this instanceof L2PcInstance)
			((L2PcInstance) this).updateAndBroadcastStatus(2);
	}

	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _confused to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);

		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 *
	 * @param effectId
	 *            The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopSkillEffects(int skillId)
	{
		// Get all skills effects on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;

		for (L2Effect e : effects)
		{
			if (e.getSkill().getId() == skillId)
				e.exit();
		}
	}

	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li> <li>Remove the L2Effect from _effects of the L2Character</li> <li>Update active skills in progress icones on player client</li> <BR>
	 * <BR>
	 *
	 * @param type
	 *            The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2Effect.EffectType type)
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;

		// Go through all active skills effects
		for (L2Effect e : effects)
		{
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type)
				e.exit();
		}
	}

	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _fake_death to false</li> <li>Notify the L2Character AI</li> <BR>
	 * <BR>
	 */
	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);

		setIsFakeDeath(false);
		// [L2J_JP_ADD]
		setIsFallsdown(false);
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof L2PcInstance)
			((L2PcInstance) this).setRecentFakeDeath(true);

		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _afraid to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);

		setIsAfraid(false);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _muted to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);

		setIsMuted(false);
		updateAbnormalEffect();
	}

	public final void stopPsychicalMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PSYCHICAL_MUTE);
		else
			removeEffect(effect);

		setIsPsychicalMuted(false);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _rooted to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);

		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _sleeping to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);

		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopImmobileUntilAttacked(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.IMMOBILEUNTILATTACKED);
		else
			removeEffect(effect);

		stopSkillEffects(effect.getSkill().getNegateId());
		setIsImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icone</li> <li>Set the abnormal effect flag _stuned to false</li> <li>Notify the L2Character AI</li> <li>Send Server->Client UserInfo/CharInfo packet</li> <BR>
	 * <BR>
	 */
	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);

		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopParalyze(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PARALYZE);
		else
			removeEffect(effect);

		setIsParalyzed(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in</U> :</B><BR>
	 * <BR>
	 * <li>L2NPCInstance</li> <li>L2PcInstance</li> <li>L2Summon</li> <li>L2DoorInstance</li> <BR>
	 * <BR>
	 */
	public abstract void updateAbnormalEffect();

	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR>
	 * <BR>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}

	public final void updateEffectIcons(boolean partyOnly)
	{
		// Create a L2PcInstance of this if needed
		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
			player = (L2PcInstance) this;

		// Create a L2Summon of this if needed
		L2Summon summon = null;
		if (this instanceof L2Summon)
		{
			summon = (L2Summon) this;
			player = summon.getOwner();
		}
		// Create the main packet if needed
		MagicEffectIcons mi = null;
		if (!partyOnly)
			mi = new MagicEffectIcons();

		// Create the party packet if needed
		PartySpelled ps = null;
		if (summon != null)
			ps = new PartySpelled(summon);

		else if (player != null && player.isInParty())
			ps = new PartySpelled(player);

		// Create the olympiad spectator packet if needed
		ExOlympiadSpelledInfo os = null;
		if (player != null && player.isInOlympiadMode())
			os = new ExOlympiadSpelledInfo(player);

		if (mi == null && ps == null && os == null)
			return; // nothing to do (should not happen)

		// Go through all effects if any
		L2Effect[] effects = getAllEffects();
		if (effects != null && effects.length > 0)
		{
			for (L2Effect effect : effects)
			{
				if (effect == null || !effect.getShowIcon())
					continue;

				if (effect.getEffectType() == L2Effect.EffectType.CHARGE && player != null)
					// handled by EtcStatusUpdate
					continue;

				if (effect.getInUse())
				{
					if (mi != null)
						effect.addIcon(mi);

					if (ps != null)
						effect.addPartySpelledIcon(ps);

					if (os != null)
						effect.addOlympiadSpelledIcon(os);
				}
			}
		}
		// Send the packets if needed
		if (mi != null)
			sendPacket(mi);

		if (ps != null && player != null)
		{
			// summon info only needs to go to the owner, not to the whole party player info: if in party, send to all party members except one's self. if not in party, send to self.
			if (player.isInParty() && summon == null)
				player.getParty().broadcastToPartyMembers(player, ps);
			else
				player.sendPacket(ps);
		}
		if (player != null)
			player.sendPacket(new EtcStatusUpdate(player));

		if (os != null)
		{
			if (Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null)
			{
				for (L2PcInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
				{
					if (spectator == null)
						continue;
					spectator.sendPacket(os);
				}
			}
		}
	}

	// Property - Public
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li> <BR>
	 * <BR>
	 */
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (isStunned())
			ae |= ABNORMAL_EFFECT_STUN;

		if (isRooted())
			ae |= ABNORMAL_EFFECT_ROOT;

		if (isSleeping())
			ae |= ABNORMAL_EFFECT_SLEEP;

		if (isConfused())
			ae |= ABNORMAL_EFFECT_CONFUSED;

		if (isMuted())
			ae |= ABNORMAL_EFFECT_MUTED;

		if (isAfraid())
			ae |= ABNORMAL_EFFECT_AFRAID;

		if (isPsychicalMuted())
			ae |= ABNORMAL_EFFECT_MUTED;

		return ae;
	}

	/**
	 * Return all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR>
	 * <BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Effect[] getAllEffects()
	{
		// Create a copy of the effects set
		FastTable<L2Effect> effects = _effects;
		// If no effect found, return EMPTY_EFFECTS
		if (effects == null || effects.isEmpty())
			return EMPTY_EFFECTS;

		// Return all effects in progress in a table
		int ArraySize = effects.size();
		L2Effect[] effectArray = new L2Effect[ArraySize];
		for (int i = 0; i < ArraySize; i++)
		{
			if (i >= effects.size() || effects.get(i) == null)
				break;
			effectArray[i] = effects.get(i);
		}
		return effectArray;
	}

	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param index
	 *            The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(int index)
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null)
			return null;

		L2Effect e;
		L2Effect eventNotInUse = null;
		for (int i = 0; i < effects.size(); i++)
		{
			e = effects.get(i);
			if (e.getSkill().getId() == index)
			{
				if (e.getInUse())
					return e;
				else
					eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}

	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skill
	 *            The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null)
			return null;

		L2Effect e;
		L2Effect eventNotInUse = null;
		for (int i = 0; i < effects.size(); i++)
		{
			e = effects.get(i);
			if (e.getSkill() == skill)
			{
				if (e.getInUse())
					return e;
				else
					eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}

	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 *
	 * @param tp
	 *            The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(L2Effect.EffectType tp)
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null)
			return null;

		L2Effect e;
		L2Effect eventNotInUse = null;
		for (int i = 0; i < effects.size(); i++)
		{
			e = effects.get(i);
			if (e.getEffectType() == tp)
			{
				if (e.getInUse())
					return e;
				else
					eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}

	// =========================================================
	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/**
	 * This class permit to the L2Character AI to obtain informations and uses L2Character method
	 */
	public class AIAccessor
	{
		public AIAccessor()
		{
		}

		/**
		 * Return the L2Character managed by this Accessor AI.<BR>
		 * <BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR>
		 * <BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR>
		 * <BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR>
		 * <BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR>
		 * <BR>
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR>
		 * <BR>
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}

		/**
		 * Create a NotifyAITask.<BR>
		 * <BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR>
		 * <BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}

	/**
	 * This class group all mouvement data.<BR>
	 * <BR>
	 * <B><U> Data</U> :</B><BR>
	 * <BR>
	 * <li>_moveTimestamp : Last time position update</li> <li>_xDestination, _yDestination, _zDestination : Position of the destination</li> <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li> <li>_moveStartTime : Start time of the movement</li> <li>_ticksToMove : Nb of ticks between the start and the destination</li> <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li> <BR>
	 * <BR>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveTimestamp;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public int _xMoveFrom;
		public int _yMoveFrom;
		public int _zMoveFrom;
		public int _heading;
		public int _moveStartTime;
		public int _ticksToMove;
		public float _xSpeedTicks;
		public float _ySpeedTicks;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}

	/** Table containing all skillId that are disabled */
	protected List<Integer> _disabledSkills;
	private boolean _allSkillsDisabled;
	/** Movement data of this L2Character */
	protected MoveData _move;
	/** Orientation of the L2Character */
	private int _heading;
	/** L2Charcater targeted by the L2Character */
	private L2Object _target;
	// setted by the start of casting, in game ticks
	private int _castEndTime;
	private int _castInterruptTime;
	// setted by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	/**
	 * Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE
	 */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
	}
	protected L2CharacterAI _ai;
	/** Future Skill Cast */
	protected Future<?> _skillCast;
	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;

	/**
	 * Add a Func to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li> <li>Add the Func object to _calculators</li> <BR>
	 * <BR>
	 *
	 * @param f
	 *            The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null) return;

		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (_calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			for (int i = 0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		if (_calculators[stat] == null)
			_calculators[stat] = new Calculator();

		// Add the Func to the calculator corresponding to the state
		_calculators[stat].addFunc(f);
	}

	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Equip an item from inventory</li> <li>Learn a new passive skill</li> <li>Use an active skill</li> <BR>
	 * <BR>
	 *
	 * @param funcs
	 *            The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		FastList<Stats> modifiedStats = new FastList<Stats>();
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}

	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the Func object from _calculators</li> <BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li> <BR>
	 * <BR>
	 *
	 * @param f
	 *            The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null) return;

		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		if (_calculators[stat] == null)
			return;

		// Remove the Func object from the Calculator
		_calculators[stat].removeFunc(f);
		if (_calculators[stat].size() == 0)
			_calculators[stat] = null;

		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}

	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li> <li>Stop an active skill</li> <BR>
	 * <BR>
	 *
	 * @param funcs
	 *            The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		FastList<Stats> modifiedStats = new FastList<Stats>();
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}

	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li> <BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li> <BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li> <li>Stop an active skill</li> <BR>
	 * <BR>
	 *
	 * @param owner
	 *            The Object(Skill, Item...) that has created the effect
	 */
	public final synchronized void removeStatsOwner(Object owner)
	{
		FastList<Stats> modifiedStats = null;
		// Go through the Calculator set
		for (int i = 0; i < _calculators.length; i++)
		{
			if (_calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				if (modifiedStats != null)
					modifiedStats.addAll(_calculators[i].removeOwner(owner));
				else
					modifiedStats = _calculators[i].removeOwner(owner);
				if (_calculators[i].size() == 0)
					_calculators[i] = null;
			}
		}
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
		if (owner instanceof L2Effect && !((L2Effect) owner).preventExitUpdate)
			broadcastModifiedStats(modifiedStats);
	}

	private void broadcastModifiedStats(FastList<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
			return;

		boolean broadcastFull = false;
		boolean otherStats = false;
		StatusUpdate su = null;
		for (Stats stat : stats)
		{
			if (stat == Stats.POWER_ATTACK_SPEED)
			{
				if (su == null)
					su = new StatusUpdate(getObjectId());

				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat == Stats.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
					su = new StatusUpdate(getObjectId());

				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
/*			 else if (stat == Stats.MAX_HP)
			 { 
				 // TODO: self only and add more stats... 
				 if (su == null) 
					 su = new StatusUpdate(getObjectId()); 
				 su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				 }
			 }*/
			else if (stat == Stats.MAX_CP)
			{
				if (this instanceof L2PcInstance)
				{
					if (su == null)
						su = new StatusUpdate(getObjectId());

					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}
/*			else if (stat==Stats.MAX_MP)
			{
				if (su == null) su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			}*/
			else if (stat == Stats.RUN_SPEED)
				broadcastFull = true;
			else
				otherStats = true;
		}
		if (this instanceof L2PcInstance)
		{
			if (broadcastFull)
				((L2PcInstance) this).updateAndBroadcastStatus(2);
			else
			{
				if (otherStats)
				{
					((L2PcInstance) this).updateAndBroadcastStatus(1);
					if (su != null)
					{
						for (L2PcInstance player : getKnownList().getKnownPlayers().values())
						{
							try
							{
								player.sendPacket(su);
							}
							catch (NullPointerException e)
							{
							}
						}
					}
				}
				else if (su != null)
					broadcastPacket(su);
			}
		}
		else if (this instanceof L2NpcInstance)
		{
			if (broadcastFull)
			{
				for (L2PcInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
						player.sendPacket(new NpcInfo((L2NpcInstance) this, player));
				}
			}
			else if (su != null)
				broadcastPacket(su);
		}
		else if (this instanceof L2Summon)
		{
			if (broadcastFull)
			{
				for (L2PcInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
						player.sendPacket(new NpcInfo((L2Summon) this, player));
				}
			}
			else if (su != null)
				broadcastPacket(su);
		}
		else if (su != null)
			broadcastPacket(su);
	}

	/**
	 * Return the orientation of the L2Character.<BR>
	 * <BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR>
	 * <BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR>
	 * <BR>
	 */
	public final int getClientX()
	{
		return _clientX;
	}

	public final int getClientY()
	{
		return _clientY;
	}

	public final int getClientZ()
	{
		return _clientZ;
	}

	public final int getClientHeading()
	{
		return _clientHeading;
	}

	public final void setClientX(int val)
	{
		_clientX = val;
	}

	public final void setClientY(int val)
	{
		_clientY = val;
	}

	public final void setClientZ(int val)
	{
		_clientZ = val;
	}

	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}

	public final int getXdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._xDestination;

		return getX();
	}

	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR>
	 * <BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._yDestination;

		return getY();
	}

	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR>
	 * <BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._zDestination;

		return getZ();
	}

	/**
	 * Return true if the L2Character is in combat.<BR>
	 * <BR>
	 */
	public final boolean isInCombat()
	{
		return getAI().getAttackTarget() != null;
	}

	/**
	 * Return true if the L2Character is moving.<BR>
	 * <BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * Return true if the L2Character is travelling a calculated path.<BR>
	 * <BR>
	 */
	public final boolean isOnGeodataPath()
	{
		if (_move == null)
			return false;

		try
		{
			if (_move.onGeodataPathIndex == -1)
				return false;

			if (_move.onGeodataPathIndex == _move.geoPath.size() - 1)
				return false;
		}
		catch (NullPointerException e)
		{
			return false;
		}
		return true;
	}

	/**
	 * Return true if the L2Character is casting.<BR>
	 * <BR>
	 */
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return true if the L2Character is casting.<BR>
	 * <BR>
	 */
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}

	/**
	 * Return true if the cast of the L2Character can be aborted.<BR>
	 * <BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character is attacking.<BR>
	 * <BR>
	 */
	public final boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return true if the L2Character has aborted its attack.<BR>
	 * <BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}

	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			if (_cubics.size() > 0)
			{
				for (L2CubicInstance cubic : _cubics.values())
				{
					cubic.stopAction();
				}
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			if (getForceBuff() != null)
				getForceBuff().onCastAbort();

			L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();

			if (_cubics.size() > 0)
			{
				for (L2CubicInstance cubic : _cubics.values())
				{
					cubic.stopAction();
				}
			}
			// cancels the skill hit scheduled task
			enableAllSkills(); // re-enables the skills
			if (this instanceof L2PcInstance)
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention

			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}

	/**
	 * Update the position of the L2Character during a movement and return true if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on
	 * the server. But, it's always the server position that is used in range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR>
	 * <BR>
	 *
	 * @param gameTicks
	 *            Nb of ticks since the server start
	 * @return true if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;
		if (m == null) return true;

		if (!isVisible())
		{
			_move = null;
			return true;
		}
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == gameTicks) return false;

		// Calculate the time between the beginning of the deplacement and now
		int elapsed = gameTicks - m._moveStartTime;
		// If estimated time needed to achieve the destination is passed,
		// the L2Character is positionned to the destination position
		if (elapsed >= m._ticksToMove)
		{
			// Set the timer of last position update to now
			m._moveTimestamp = gameTicks;
			// Set the position of the L2Character to the destination
			if (this instanceof L2BoatInstance)
			{
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
				((L2BoatInstance) this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
			else
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);

			return true;
		}
		// Estimate the position of the L2Character dureing the movement
		// according to its _xSpeedTicks and _ySpeedTicks
		// The Z position is obtained from the client
		if (this instanceof L2BoatInstance)
		{
			super.getPosition().setXYZ(m._xMoveFrom + (int) (elapsed * m._xSpeedTicks), m._yMoveFrom + (int) (elapsed * m._ySpeedTicks), super.getZ());
			((L2BoatInstance) this).updatePeopleInTheBoat(m._xMoveFrom + (int) (elapsed * m._xSpeedTicks), m._yMoveFrom + (int) (elapsed * m._ySpeedTicks), super.getZ());
		}
		else
		{
			super.getPosition().setXYZ(m._xMoveFrom + (int) (elapsed * m._xSpeedTicks), m._yMoveFrom + (int) (elapsed * m._ySpeedTicks), super.getZ());
			if (this instanceof L2PcInstance)
				((L2PcInstance) this).revalidateZone(false);
			else
				revalidateZone();
		}
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		return false;
	}

	public void revalidateZone()
	{
		if (getWorldRegion() == null)
			return;

		getWorldRegion().revalidateZones(this);
	}

	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete movement data of the L2Character</li> <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading</li> <li>Remove the L2Object object from _gmList** of GmListTable</li> <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR>
	 * <BR>
	 */
	public void stopMove(L2CharPosition pos)
	{
		stopMove(pos, true);
	}

	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;
		// if (getAI() != null)
		// getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// Set the current position (x,y,z), its current L2WorldRegion if
		// necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			if (this instanceof L2PcInstance)
				((L2PcInstance) this).revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
        if (Config.MOVE_BASED_KNOWNLIST)
        	this.getKnownList().findObjects();
	}

	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _target of L2Character to L2Object</li> <li>If necessary, add L2Object to _knownObject of the L2Character</li> <li>If necessary, add L2Character to _KnownObject of the L2Object</li> <li>If object==null, cancel Attak or Cast</li> <BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li> <BR>
	 * <BR>
	 *
	 * @param object
	 *            L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;

		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		// If object==null, Cancel Attak or Cast
		if (object == null)
		{
			if (_target != null)
				broadcastPacket(new TargetUnselected(this));
		}
		_target = object;
	}

	/**
	 * Return the identifier of the L2Object targeted or -1.<BR>
	 * <BR>
	 */
	public final int getTargetId()
	{
		if (_target != null)
			return _target.getObjectId();

		return -1;
	}

	/**
	 * Return the L2Object targeted or null.<BR>
	 * <BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}

	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get current position of the L2Character</li> <li>Calculate distance (dx,dy) between current position and destination including offset</li> <li>Create and Init a MoveData object</li> <li>Set the L2Character _move object to MoveData object</li> <li>Add the L2Character to movingObjects of the GameTimeController</li> <li>Create a task to notify the AI that L2Character arrives at a check
	 * point of the movement</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object)</li> <li>FollowTask</li> <BR>
	 * <BR>
	 *
	 * @param x
	 *            The X position of the destination
	 * @param y
	 *            The Y position of the destination
	 * @param z
	 *            The Y position of the destination
	 * @param offset
	 *            The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
			return;

		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = x - curX;
		double dy = y - curY;
		double dz = z - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if (Config.GEODATA > 0 && isInsideZone(ZONE_WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = x - curX;
			dy = y - curY;
			dz = z - curZ;
			distance = Math.sqrt(dx * dx + dy * dy);
		}
		/*if (Config.DEBUG) {
			_log.fine("distance to target:" + distance);
		}*/
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		double cos;
		double sin;
		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;
				if (Config.DEBUG)
				{
					_log.fine("already in range, no movement needed.");
				}
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			distance -= offset - 5; // due to rounding error, we have to
			// move a
			// bit closer to be in range
			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		if (Config.GEODATA > 0 && !this.isFlying()
		// && !this.isInsideZone(ZONE_WATER) // TODO: change geodata to return correct Z and check if exploiting possible
				&& !(this instanceof L2NpcWalkerInstance)) // currently flying characters not checked
		// if ((Config.GEODATA > 0) && !isFlying()) // currently flying characters not checked
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = originalX - L2World.MAP_MIN_X >> 4;
			int gty = originalY - L2World.MAP_MIN_Y >> 4;
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning
			// home
			// (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance
			// only
			if (Config.GEODATA == 2 && !(this instanceof L2Attackable && ((L2Attackable) this).isReturningToSpawnPoint()) || this instanceof L2PcInstance || this instanceof L2Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW) || isAfraid() || this instanceof L2RiftInvaderInstance)
			{
				if (isOnGeodataPath())
				{
					if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
						return;
					else
						_move.onGeodataPathIndex = -1; // Set not on geodata path
				}
				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y || curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					_log.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
						((L2PcInstance) this).deleteMe();
					else
						onDecay();
					return;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z, getInstanceId());
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
			}
			// Pathfinding checks. Only when geodata setting is 2, the LoS
			// check
			// gives shorter result
			// than the original movement was and the LoS gives a shorter
			// distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (Config.GEODATA == 2 && originalDistance - distance > 100 && distance < 2000 && !this.isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if (this instanceof L2PlayableInstance || isInCombat())
				{
					int gx = curX - L2World.MAP_MIN_X >> 4;
					int gy = curY - L2World.MAP_MIN_Y >> 4;
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId(), this instanceof L2PcInstance);
                	if (m.geoPath == null || m.geoPath.size() < 2) // No
					// path
					// found
					{
						// Even though there's no path found (remember geonodes
						// aren't perfect),
						// the mob is attacking and right now we set it so that
						// the mob will go
						// after target anyway, is dz is small enough. Summons
						// will follow their masters no matter what.
						if (this instanceof L2PcInstance || !(this instanceof L2PlayableInstance) && Math.abs(z - curZ) > 140 || this instanceof L2Summon && !((L2Summon) this).getFollowStatus())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						else
						{
							x = originalX;
							y = originalY;
							z = originalZ;
							distance = originalDistance;
						}
					}
					else
					{
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;
						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();
						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z, getInstanceId()))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						for (int i = 0; i < m.geoPath.size() - 1; i++)
						{
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1), getInstanceId()))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						// not in use: final check if we can indeed reach first
						// path node (path nodes sometimes aren't accurate
						// enough)
						// but if the node is very far, then a shorter check
						// (like 3 blocks) would be enough
						// something similar might be needed for end
						dx = x - curX;
						dy = y - curY;
						distance = Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if (distance < 1 && (Config.GEODATA == 2 || this instanceof L2PlayableInstance || this.isAfraid() || this instanceof L2RiftInvaderInstance))
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;
				if (this instanceof L2Summon)
					((L2Summon) this).setFollowStatus(false);

				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE); // needed?
				return;
			}
		}
		// Caclulate the Nb of ticks between the current position and the
		// destination
		// One tick added for rounding reasons
		m._ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		// Calculate the xspeed and yspeed in unit/ticks in function of the
		// movement speed
		m._xSpeedTicks = (float) (cos * speed / GameTimeController.TICKS_PER_SECOND);
		m._ySpeedTicks = (float) (sin * speed / GameTimeController.TICKS_PER_SECOND);
		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		heading += 32768;
		setHeading(heading);
		/*if (Config.DEBUG)
			_log.fine("dist:" + distance + "speed:" + speed + " ttt:" + m._ticksToMove + " dx:" + (int) m._xSpeedTicks + " dy:" + (int) m._ySpeedTicks + " heading:" + heading);
		 */
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		m._heading = 0;
		m._moveStartTime = GameTimeController.getGameTicks();
		m._xMoveFrom = curX;
		m._yMoveFrom = curY;
		m._zMoveFrom = curZ;
		/*if (Config.DEBUG)
			_log.fine("time to target:" + m._ticksToMove);
		*/
		// Set the L2Character _move object to MoveData object
		_move = m;
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		int tm = m._ticksToMove * GameTimeController.MILLIS_IN_TICK;
		// Create a task to notify the AI that L2Character arrives at a check
		// point of the movement
		if (tm > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive to destination by GameTimeController
	}

	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		// Update MoveData object
		m.onGeodataPathIndex = _move.onGeodataPathIndex + 1; // next segment
		m.geoPath = _move.geoPath;
		m.geoPathGtx = _move.geoPathGtx;
		m.geoPathGty = _move.geoPathGty;
		m.geoPathAccurateTx = _move.geoPathAccurateTx;
		m.geoPathAccurateTy = _move.geoPathAccurateTy;
		// Get current position of the L2Character
		m._xMoveFrom = super.getX();
		m._yMoveFrom = super.getY();
		m._zMoveFrom = super.getZ();
		if (_move.onGeodataPathIndex == _move.geoPath.size() - 2)
		{
			m._xDestination = _move.geoPathAccurateTx;
			m._yDestination = _move.geoPathAccurateTy;
			m._zDestination = _move.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = _move.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = _move.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = _move.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = m._xDestination - m._xMoveFrom;
		double dy = m._yDestination - m._yMoveFrom;
		double distance = Math.sqrt(dx * dx + dy * dy);
		double sin = dy / distance;
		double cos = dx / distance;
		// Caclulate the Nb of ticks between the current position and the
		// destination
		// One tick added for rounding reasons
		m._ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		// Calculate the xspeed and yspeed in unit/ticks in function of the
		// movement speed
		m._xSpeedTicks = (float) (cos * speed / GameTimeController.TICKS_PER_SECOND);
		m._ySpeedTicks = (float) (sin * speed / GameTimeController.TICKS_PER_SECOND);
		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		heading += 32768;
		setHeading(heading);
		m._heading = 0; // ?
		m._moveStartTime = GameTimeController.getGameTicks();
		if (Config.DEBUG)
			_log.fine("time to target:" + m._ticksToMove);

		// Set the L2Character _move object to MoveData object
		_move = m;
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		int tm = m._ticksToMove * GameTimeController.MILLIS_IN_TICK;
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (tm > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive to destination by GameTimeController
		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		CharMoveToLocation msg = new CharMoveToLocation(this);
		broadcastPacket(msg);
		return true;
	}

	public boolean validateMovementHeading(int heading)
	{
		MoveData md = _move;
		if (md == null)
			return true;

		boolean result = true;
		if (md._heading != heading)
		{
			result = md._heading == 0;
			md._heading = heading;
		}
		return result;
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given object.<BR>
	 * <BR>
	 *
	 * @param object
	 *            L2Object
	 * @return the squared distance
	 */
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param z
	 *            Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 *
	 * @param object
	 *            L2Object
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.
	 * (check only x and y, not z)
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		return dx * dx + dy * dy;
	}

	public final double getRangeToTarget(L2Object par)
	{
		return math.sqrt(getPlanDistanceSq(par));
	}

	/**
	 * Check if this object is inside the given radius around the given object.
	 * @param object the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 * @see interlude.gameserver.model.L2Character.isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	 */
	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}

	/**
	 * Check if this object is inside the given plan radius around the given point.<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param radius
	 *            the radius around the target
	 * @param strictCheck
	 *            true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}

	/**
	 * Check if this object is inside the given radius around the given point.<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param z
	 *            Z position of the target
	 * @param radius
	 *            the radius around the target
	 * @param checkZ
	 *            should we check Z axis also
	 * @param strictCheck
	 *            true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		if (strictCheck)
		{
			if (checkZ)
				return dx * dx + dy * dy + dz * dz < radius * radius;
			else
				return dx * dx + dy * dy < radius * radius;
		}
		else
		{
			if (checkZ)
				return dx * dx + dy * dy + dz * dz <= radius * radius;
			else
				return dx * dx + dy * dy <= radius * radius;
		}
	}

	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR>
	 * <BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * Retun true if arrows are available.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <li>L2PetInstance</li> <BR>
	 * <BR>
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	public final void startImmobileUntilAttacked()
	{
		setIsImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public abstract L2Weapon getSecondaryWeaponItem();

	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li> <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li> <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection
	 * damage to reduce HP of attacker if necessary</li> <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li> <BR>
	 * <BR>
	 *
	 * @param target
	 *            The L2Character targeted
	 * @param damage
	 *            Nb of HP to reduce
	 * @param crit
	 *            true if hit is critical
	 * @param miss
	 *            true if hit is missed
	 * @param soulshot
	 *            true if SoulShot are charged
	 * @param shld
	 *            true if shield is efficient
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() || this instanceof L2NpcInstance && ((L2NpcInstance) this).isEventMob)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		if (this instanceof L2NpcInstance && target.isAlikeDead() || target.isDead() 
				|| !getKnownList().knowsObject(target) 
				&& !(this instanceof L2DoorInstance))
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (miss)
		{
        	// ON_EVADED_HIT
         	if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(this);

			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);
				if (this instanceof L2Summon)
				{
					int mobId = ((L2Summon) this).getTemplate().npcId;
					sm.addNpcName(mobId);
				}
				else
					sm.addString(getName());

				((L2PcInstance) target).sendPacket(sm);
			}
		}
		// If attack isn't aborted, send a message system (critical hit,
		// missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack Character will be petrified if attacking a raid that's more than 8 levels lower
			if (target.isRaid())
			{
				int level = 0;
				if (this instanceof L2PcInstance)
					level = getLevel();
				else if (this instanceof L2Summon)
					level = ((L2Summon) this).getOwner().getLevel();
				if (level > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);
					if (skill != null)
						skill.getEffects(target, this);
					else
						_log.warning("Skill 4515 at level 1 is missing in DP.");

					damage = 0; // prevents messing up drop calculation
				}
			}
			sendDamageMessage(target, damage, false, crit, miss);
			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				// Check if shield is efficient
				if (shld)
				{
					enemy.sendPacket(new SystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
					// else if (!miss && damage < 1)
					// enemy.sendMessage("You hit the target's armor.");
				}
			}
			else if (target instanceof L2Summon)
			{
				L2Summon activeSummon = (L2Summon) target;
				SystemMessage sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
				sm.addString(getName());
				sm.addNumber(damage);
				activeSummon.getOwner().sendPacket(sm);
			}
			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isBow = weapon != null && weapon.getItemType().toString().equalsIgnoreCase("Bow");
				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Reduce HP of the target and calculate reflection damage
					// to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					if (reflectPercent > 0)
					{
						int reflectedDamage = (int) (reflectPercent / 100. * damage);
						damage -= reflectedDamage;
						if (reflectedDamage > target.getMaxHp())
							reflectedDamage = target.getMaxHp();

						getStatus().reduceHp(reflectedDamage, target, true);
					}
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);
						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp

						if (absorbDamage > 0)
							setCurrentHp(getCurrentHp() + absorbDamage);
					}
				}
				target.reduceCurrentHp(damage, this);
				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				if (target.getForceBuff() != null)
					target.abortCast();

				// Manage attack or cast break of the target (calculating rate,
				// sending message...)
				if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				// Maybe launch chance skills on us
				if (_chanceSkills != null)
					_chanceSkills.onHit(target, false, crit);

				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
					target.getChanceSkills().onHit(this, true, crit);
			}
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
			if (activeWeapon != null)
				activeWeapon.getSkillEffects(this, target, crit);
			return;
		}
		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}

	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			if (this instanceof L2PcInstance)
			{
				// TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(ActionFailed.STATIC_PACKET);
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}

	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			if (this instanceof L2PcInstance)
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.CASTING_INTERRUPTED));
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}

	/**
	 * Manage Forced attack (shift + select target).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed</li> <li>If target is confused, send a Server->Client packet ActionFailed</li> <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed</li> <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with
	 * AI_INTENTION_ATTACK</li> <BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system
			// message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.isInOlympiadMode() && player.getTarget() != null)
		{
			L2PcInstance target;
			if (player.getTarget() instanceof L2Summon)
				target = ((L2Summon) player.getTarget()).getOwner();
			else
				target = (L2PcInstance) player.getTarget();

			if (target.isInOlympiadMode() && !player.isOlympiadStart() && player.getOlympiadGameId() != target.getOlympiadGameId())
			{
				// if L2PcInstance is in Olympia and the match isn't already
				// start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else if (player.getTarget() != null && !player.getTarget().isAttackable() 
				&& player.getAccessLevel() < Config.GM_PEACEATTACK)
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
		else if (player.isConfused())
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
		else if (this instanceof L2ArtefactInstance)
			// If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
		else
		{
			// GeoData Los Check or dz > 1000
			if (!GeoData.getInstance().canSeeTarget(player, this))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			// Notify AI with AI_INTENTION_ATTACK
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
	}

	/**
	 * Return true if inside peace zone.<BR>
	 * <BR>
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}

	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return attacker.getAccessLevel() < Config.GM_PEACEATTACK && isInsidePeaceZone((L2Object) attacker, target);
	}

	public static boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
			return false;

		if (target instanceof L2MonsterInstance || attacker instanceof L2MonsterInstance)
			return false;

		if (target instanceof L2NpcInstance || attacker instanceof L2NpcInstance)
			return false;

		if (target instanceof L2GuardInstance || attacker instanceof L2GuardInstance)
			return false;

		if (target instanceof L2PlayableInstance && attacker instanceof L2PlayableInstance)
		{
			L2PcInstance targetPlayer = null;
			L2PcInstance attackingPlayer = null;
			if (target instanceof L2Summon)
				targetPlayer = ((L2Summon) target).getOwner();
			else
				targetPlayer = (L2PcInstance) target;

			if (attacker instanceof L2Summon)
				attackingPlayer = ((L2Summon) attacker).getOwner();
			else
				attackingPlayer = (L2PcInstance) attacker;

			if (targetPlayer.getLevel() < Config.PLAYER_PROTECTION_SYSTEM && attackingPlayer.getLevel() >= Config.PLAYER_PROTECTION_SYSTEM || attackingPlayer.getLevel() < Config.PLAYER_PROTECTION_SYSTEM && targetPlayer.getLevel() >= Config.PLAYER_PROTECTION_SYSTEM)
			{
				attackingPlayer.sendMessage("Due to a player protection system in use, players above and under protection level " + String.valueOf(Config.PLAYER_PROTECTION_SYSTEM) + " cannot fight with each other.");
				return true;
			}
		}
		// allows red to be attacked and red to attack flagged players
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return false;

			if (target instanceof L2Summon && ((L2Summon) target).getOwner().getKarma() > 0)
				return false;

			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
					return false;

				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
					return false;
			}
			if (attacker instanceof L2Summon && ((L2Summon) attacker).getOwner().getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
					return false;

				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
					return false;
			}
		}
		// right now only L2PcInstance has update zone status...
		// TODO: ZONETODO: Are there things < L2Characters in peace zones that
		// can be attacked? If not this could be cleaned up
		if (attacker instanceof L2Character && target instanceof L2Character)
			return ((L2Character) target).isInsideZone(ZONE_PEACE) 
			|| ((L2Character) attacker).isInsideZone(ZONE_PEACE);

		if (attacker instanceof L2Character)
			return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null 
			|| ((L2Character) attacker).isInsideZone(ZONE_PEACE);

		return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null;
	}

	/**
	 * return true if this character is inside an active grid.
	 */
	public Boolean isInActiveRegion()
	{
		try
		{
			L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
			return region != null && region.isActive();
		}
		catch (Exception e)
		{
			if (this instanceof L2PcInstance)
			{
				_log.warning("Player " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				((L2PcInstance) this).sendMessage("Error with your coordinates! Please reboot your game fully!");
				((L2PcInstance) this).teleToLocation(80753, 145481, -3532, false); // Near Giran luxury shop
			}
			else
			{
				_log.warning("Object " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				decayMe();
			}
			return false;
		}
	}

	/**
	 * Return true if the L2Character has a Party in progress.<BR>
	 * <BR>
	 */
	public boolean isInParty()
	{
		return false;
	}

	/**
	 * Return the L2Party object of the L2Character.<BR>
	 * <BR>
	 */
	public L2Party getParty()
	{
		return null;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR>
	 * <BR>
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1500 * 345 / atkSpd);
				case DAGGER:
					atkSpd = getStat().getPAtkSpd();
					// atkSpd /= 1.15;
					break;
				default:
					atkSpd = getStat().getPAtkSpd();
			}
		} else
			atkSpd = getPAtkSpd();

		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null)
			return 0;

		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
			return 0;

		// else if (reuse < 10) reuse = 1500;
		reuse *= getStat().getReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
				return (int) (reuse * 345 / atkSpd);
			default:
				return (int) (reuse * 312 / atkSpd);
		}
	}

	/**
	 * Return true if the L2Character use a dual weapon.<BR>
	 * <BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li> <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li> <li>Add Func objects of newSkill to the calculator set of the L2Character</li> <BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li> <BR>
	 * <BR>
	 *
	 * @param newSkill
	 *            The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill    = null;

		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
		    {
				// if skill came with another one, we should delete the other one too.
				if(oldSkill.triggerAnotherSkill())
					removeSkill(oldSkill.getTriggeredId(),true);
				removeStatsOwner(oldSkill);
		    }
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));

			if (oldSkill != null && _chanceSkills != null)
				removeChanceSkill(oldSkill.getId());
			if (newSkill.isChance())
				addChanceSkill(newSkill);
		}
		return oldSkill;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
			return null;

		return removeSkill(skill.getId(), true);
	}

	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		if (skill == null)
			return null;

		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId(), cancelEffect);
	}

	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}

	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			//this is just a fail-safe againts buggers and gm dummies...
			if(oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
				removeSkill(oldSkill.getTriggeredId(), true);

			if (cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Effect e = getFirstEffect(oldSkill);
				if (e == null)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}

			if (oldSkill.isChance() && _chanceSkills != null)
				removeChanceSkill(oldSkill.getId());

			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 
					&& this instanceof L2PcInstance && ((L2PcInstance)this).getPet() != null 
					&& ((L2PcInstance)this).getPet().getNpcId() == 14870)
				((L2PcInstance)this).getPet().unSummon(((L2PcInstance)this));
		}
		return oldSkill;
	}

	public synchronized void addChanceSkill(L2Skill skill)
	{
		if (_chanceSkills == null)
			_chanceSkills = new ChanceSkillList(this);
		_chanceSkills.put(skill, skill.getChanceCondition());
	}

	public synchronized void removeChanceSkill(int id)
	{
		if (_chanceSkills == null) return;

		for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
		{
			if (!(trigger instanceof L2Skill)) continue;

			L2Skill skill = (L2Skill)trigger;
			if (skill.getId() == id)
				_chanceSkills.remove(skill);
		}
		if (_chanceSkills.isEmpty())
			_chanceSkills = null;
	}

	public synchronized void addChanceEffect(EffectChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
			_chanceSkills = new ChanceSkillList(this);
		_chanceSkills.put(effect, effect.getTriggeredChanceCondition());
	}

	public synchronized void removeChanceEffect(EffectChanceSkillTrigger effect)
	{
		if (_chanceSkills == null) return;
		_chanceSkills.remove(effect);
		if (_chanceSkills.isEmpty())
			_chanceSkills = null;
	}

	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR>
	 * <BR>
	 */
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	/**
	 * Return the level of a skill owned by the L2Character.<BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	public int getSkillLevel(int skillId)
	{
		if (_skills == null)
			return -1;

		L2Skill skill = _skills.get(skillId);
		if (skill == null)
			return -1;

		return skill.getLevel();
	}

	/**
	 * Return true if the skill is known by the L2Character.<BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to check the knowledge
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
			return null;

		return _skills.get(skillId);
	}

	/**
	 * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this L2Character.<BR>
	 * <BR>
	 *
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		L2Effect[] effects = getAllEffects();
		int numBuffs = 0;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if ((e.getShowIcon()
							&& e.getSkill().getSkillType() == L2Skill.SkillType.BUFF
							|| e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF
							|| e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT
							|| e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT
							|| e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)
							&& !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367))
					{ // 7s buffs
						numBuffs++;
					}
				}
			}
		}
		return numBuffs;
	}

	/**
	 * Removes the first Buff of this L2Character.<BR>
	 * <BR>
	 *
	 * @param preferSkill
	 *            If != 0 the given skill Id will be removed instead of first
	 */
	public void removeFirstBuff(int preferSkill)
	{
		L2Effect[] effects = getAllEffects();
		L2Effect removeMe = null;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF || e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF || e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT || e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)
							&& !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367))
					{
						if (preferSkill == 0)
						{
							removeMe = e;
							break;
						}
						else if (e.getSkill().getId() == preferSkill)
						{
							removeMe = e;
							break;
						}
						else if (removeMe == null)
							removeMe = e;
					}
				}
			}
		}
		if (removeMe != null)
			removeMe.exit();
	}

	public int getDanceCount()
	{
		int danceCount = 0;
		L2Effect[] effects = getAllEffects();
		for (L2Effect effect : effects)
		{
			if (effect == null)
				continue;

			if (effect.getSkill().isDance() && effect.getInUse())
				danceCount++;
		}
		return danceCount;
	}

	/**
	 * Checks if the given skill stacks with an existing one.<BR>
	 * <BR>
	 *
	 * @param checkSkill
	 *            the skill to be checked
	 * @return Returns whether or not this skill will stack
	 */
	public boolean doesStack(L2Skill checkSkill)
	{
		if (_effects == null || _effects.size() < 1 
				|| checkSkill._effectTemplates == null 
				|| checkSkill._effectTemplates.length < 1 
				|| checkSkill._effectTemplates[0].stackType == null)
			return false;

		String stackType = checkSkill._effectTemplates[0].stackType;
		if (stackType.equals("none"))
			return false;

		for (int i = 0; i < _effects.size(); i++)
		{
			if (_effects.get(i).getStackType() != null && _effects.get(i).getStackType().equals(stackType))
				return true;
		}
		return false;
	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li> <li>Consumme MP, HP and Item if necessary</li> <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li> <li>Launch the magic skill in order to calculate its effects</li> <li>If the skill type is PDAM, notify the AI of
	 * the target with AI_INTENTION_ATTACK</li> <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 */
	public void onMagicLaunchedTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
	{
		if (skill == null || targets == null || targets.length <= 0)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		// Escaping from under skill's radius and peace zone check. First
		// version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();

		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();

		if (escapeRange > 0)
		{
			List<L2Character> targetList = new FastList<L2Character>();
			for (int i = 0; i < targets.length; i++)
			{
				if (targets[i] instanceof L2Character)
				{
					if (!Util.checkIfInRange(escapeRange, this, targets[i], true))
						continue;

					if (skill.isOffensive())
					{
						if (this instanceof L2PcInstance)
						{
							if (((L2Character) targets[i]).isInsidePeaceZone((L2PcInstance) this))
								continue;
						}
						else
						{
							if (L2Character.isInsidePeaceZone(this, targets[i]))
								continue;
						}
					}
					targetList.add((L2Character) targets[i]);
				}
			}
			if (targetList.isEmpty())
			{
				abortCast();
				return;
			}
			else
				targets = targetList.toArray(new L2Character[targetList.size()]);
		}
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (!isCastingNow() || isAlikeDead() && !skill.isPotion())
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}
		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();
		// Get the level of the skill
		int level = getSkillLevel(skill.getId());
		if (level < 1)
			level = 1;

		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isPotion())
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));

		if (instant)
			onMagicHitTimer(targets, skill, coolTime, true);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), 200);
	}

	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
	{
		if (skill == null || targets == null || targets.length <= 0)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		if (getForceBuff() != null)
		{
			_skillCast = null;
			enableAllSkills();
			getForceBuff().onCastAbort();
			return;
		}
		L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			_skillCast = null;
			enableAllSkills();
			mog.exit();
			return;
		}
		try
		{
			// Go through targets table
			for (L2Object target2 : targets)
			{
				if (target2 instanceof L2PlayableInstance)
				{
					L2Character target = (L2Character) target2;
					if (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.SEED)
					{
						SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addString(skill.getName());
						target.sendPacket(smsg);
					}
					if (this instanceof L2PcInstance && target instanceof L2Summon)
					{
						((L2Summon) target).getOwner().sendPacket(new PetInfo((L2Summon) target));
						sendPacket(new NpcInfo((L2Summon) target, this));
						// The PetInfo packet wipes the PartySpelled (list of
						// active spells' icons). Re-add them
						((L2Summon) target).updateEffectIcons(true);
					}
				}
			}
			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			// Consume MP of the L2Character and Send the Server->Client
			// packet StatusUpdate with current HP and MP to all other
			// L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);
			if (mpConsume > 0)
			{
				getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE, mpConsume, null, null));
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			// Consume HP if necessary and Send the Server->Client packet
			// StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if (consumeHp + 1 >= getCurrentHp())
					consumeHp = getCurrentHp() - 1.0;

				getStatus().reduceHp(consumeHp, this);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			// Send a Server->Client packet StatusUpdate with MP
			// modification to the L2PcInstance
			if (isSendStatus)
				sendPacket(su);

			// Consume Items if necessary and Send the Server->Client packet
			// InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());

			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);
		}
		catch (NullPointerException e)
		{
		}
		if (instant || coolTime == 0)
			onMagicFinalizer(skill);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
	}

	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(L2Skill skill)
	{
		_skillCast = null;
		_castEndTime = 0;
		_castInterruptTime = 0;
		enableAllSkills();
		// if the skill has changed the character's state to something other
		// than STATE_CASTING
		// then just leave it that way, otherwise switch back to STATE_IDLE.
		// if(isCastingNow())
		// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target with AI_INTENTION_ATTACK
		switch (skill.getSkillType())
		{
		case PDAM:
		case BLOW:
		case DRAIN_SOUL:
		case SOW:
		case SPOIL:
			if (getTarget() != null && getTarget() instanceof L2Character)
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
			break;
		}
		if (skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) 
				&& !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK))
			getAI().clientStartAutoAttack();

		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		if (this instanceof L2PcInstance)
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();
			currPlayer.setCurrentSkill(null, false, false);
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(),
				// queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}

	/**
	 * Reduce the item number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li> <BR>
	 * <BR>
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).
	 *  Concept :
	 * All skills disabled are identified by their skillId in _disabledSkills of the L2Character 
	 * @param skillId The identifier of the L2Skill to enable
	 */
	public void enableSkill(int skillId)
	{
		if (_disabledSkills == null) return;

		_disabledSkills.remove(new Integer(skillId));
		if (this instanceof L2PcInstance)
			removeTimeStamp(skillId);
	}

	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to disable
	 */
	public void disableSkill(int skillId)
	{
		if (_disabledSkills == null)
			_disabledSkills = Collections.synchronizedList(new FastList<Integer>());

		_disabledSkills.add(skillId);
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skillId
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(int skillId, long delay)
	{
		disableSkill(skillId);
		if (delay > 10)
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
	}

	/**
	 * Check if a skill is disabled.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to disable
	 */
	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;

		if (_disabledSkills == null)
			return false;

		return _disabledSkills.contains(skillId);
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to true).<BR>
	 * <BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG)
			_log.fine("Character: "+this.getName()+" - all skills disabled");

		_allSkillsDisabled = true;
	}

	/**
	 * Enable all skills (set _allSkillsDisabled to false).<BR>
	 * <BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG)
			_log.fine("Character: "+this.getName()+" - all skills enabled");

		_allSkillsDisabled = false;
		setIsCastingNow(false);
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 *
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();

			// Check if the toggle skill effects are already in progress on the L2Character
			if(skill.isToggle() && getFirstEffect(skill.getId()) != null) return;

			// Initial checks
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character target = (L2Character) trg;

					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if (target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if (target.isRaid() && getLevel() > target.getLevel() + 8 || !skill.isOffensive() 
					&& targetsAttackTarget != null && targetsAttackTarget.isRaid() 
					&& targetsAttackTarget.getAttackByList().contains(target) // has attacked raid
					&& getLevel() > targetsAttackTarget.getLevel() + 8 || !skill.isOffensive() 
					&& targetsCastTarget != null && targetsCastTarget.isRaid()
					&& targetsCastTarget.getAttackByList().contains(target) // has attacked raid
					&& getLevel() > targetsCastTarget.getLevel() + 8)
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
							if(tempSkill != null)
								tempSkill.getEffects(target, this);
							else
								_log.warning("Skill 4215 at level 1 is missing in DP.");
						}
						else
						{
							L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
							if(tempSkill != null)
								tempSkill.getEffects(target, this);
							else
								_log.warning("Skill 4515 at level 1 is missing in DP.");
						}
						return;
					}

					 // Check if over-hit is possible
		            if(skill.isOverhit())
		            {
		            	if(target instanceof L2Attackable)
							((L2Attackable)target).overhitEnabled(true);
		            }

		            // crafting does not trigger any chance skills
		            // possibly should be unhardcoded
					switch (skill.getSkillType())
					{
						case COMMON_CRAFT:
						case DWARVEN_CRAFT:
							break;
						default:
							// Launch weapon Special ability skill effect if available
							if (activeWeapon != null && !target.isDead())
							{
								if (activeWeapon.getSkillEffects(this, target, skill).length > 0 && this instanceof L2PcInstance)
									sendPacket(SystemMessage.sendString("Target affected by weapon special ability!"));
							}

							// Maybe launch chance skills on us
							if (_chanceSkills != null)
								_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());

							// Maybe launch chance skills on target
							if (target.getChanceSkills() != null)
								target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
					}
				}
			}

			// Launch the magic skill and calculate its effects
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);

			L2PcInstance player = null;
			if (this instanceof L2PcInstance)
				player = (L2PcInstance) this;
			else if (this instanceof L2Summon)
				player = ((L2Summon) this).getOwner();

            if (skill.getTargetType() == SkillTargetType.TARGET_HOLY && this instanceof L2PcInstance && FortressSiege.checkIfOkToCastSealOfRule((L2PcInstance)this))
            {
            	FortressSiege.Announcements(getName() + " finished casting Seal Of Ruler. " + ((L2PcInstance)this)._teamNameFOS + " has taken " + FortressSiege._eventName + "!");
            	if (!((L2PcInstance)this).isHero())
            	{
            		((L2PcInstance)this).sendMessage("You have been rewarded with a Hero status from this event");
            		((L2PcInstance)this).setHero(true);
            		((L2PcInstance)this).setFakeHero(true); // Since it's only for the event, we don't want him to get Hero gear from the event manager.
            	}
            	((L2PcInstance)this).getAppearance().setTitleColor(0, 0, 255);
            	((L2PcInstance)this).sendPacket(new SocialAction(getObjectId(), 16));
            	FortressSiege.doSwap();
            }

			if (player != null)
			{
				for (L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof L2Character)
					{
						if (skill.isOffensive())
						{
							if (target instanceof L2PcInstance || target instanceof L2Summon)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != SkillType.SIGNET && skill.getSkillType() != SkillType.SIGNET_CASTTIME)
								{
									if (skill.getSkillType() != SkillType.AGGREDUCE
											&& skill.getSkillType() != SkillType.AGGREDUCE_CHAR
											&& skill.getSkillType() != SkillType.AGGREMOVE)
										// notify target AI about the attack
										((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
									if (target instanceof L2PcInstance)
										((L2PcInstance)target).getAI().clientStartAutoAttack();
									else if (target instanceof L2Summon)
									{
										L2PcInstance owner =((L2Summon)target).getOwner();
										if (owner != null)
											owner.getAI().clientStartAutoAttack();
									}
									if (!(target instanceof L2Summon) || player.getPet() != target)
										player.updatePvPStatus((L2Character)target);
								}
							}
							else if (target instanceof L2Attackable)
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										switch (skill.getId())
										{
											case 51: // Lure
											case 511: // Temptation
												break;
											default:
												// add attacker into list
												((L2Character)target).addAttackerToAttackByList(this);
										}
										// notify target AI about the attack
										((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
										break;
								}
							}
						}
						else
						{
							if (target instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) 
										&& (((L2PcInstance)target).getPvpFlag() > 0 
												|| ((L2PcInstance)target).getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target instanceof L2Attackable
									&& !(skill.getSkillType() == SkillType.SUMMON)
									&& !(skill.getSkillType() == SkillType.BEAST_FEED)
									&& !(skill.getSkillType() == SkillType.UNLOCK)
									&& !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK)
									&& (!(target instanceof L2Summon) || player.getPet() != target))
								player.updatePvPStatus();
						}
					}
				}
				if (this instanceof L2PcInstance || this instanceof L2Summon)
				{
					L2PcInstance caster = this instanceof L2PcInstance ? (L2PcInstance) this : ((L2Summon) this).getOwner();
					for (L2Object target : targets)
					{
						if (target instanceof L2NpcInstance)
						{
							L2NpcInstance npcMob = (L2NpcInstance) target;
							if (npcMob.isInsideRadius(caster, 1000, true, true) && npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
							{
								for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
								{
									quest.notifySkillSee(npcMob, caster, skill, targets, this instanceof L2Summon);
								}
							}
						}
					}
					if (skill.getAggroPoints() > 0)
					{
						for (L2Object spMob : caster.getKnownList().getKnownObjects().values())
						{
							if (spMob instanceof L2NpcInstance)
							{
								L2NpcInstance npcMob = (L2NpcInstance) spMob;
								if (npcMob.isInsideRadius(caster, 1000, true, true) && npcMob.hasAI() && npcMob.getAI().getIntention() == AI_INTENTION_ATTACK)
								{
									L2Object npcTarget = npcMob.getTarget();
									for (L2Object target : targets) {
										if (npcTarget == target || npcMob == target)
											npcMob.seeSpell(caster, target, skill);
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}

	public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill)
	{
		if (this instanceof L2Attackable)
			((L2Attackable) this).addDamageHate(caster, 0, - skill.getAggroPoints());
	}
	/**
	 * Return true if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;
		if (target == null)
			return false;

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= -360 + maxAngleDiff)
				angleDiff += 360;

			if (angleDiff >= 360 - maxAngleDiff)
				angleDiff -= 360;

			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (Config.DEBUG)
					_log.info("Char " + getName() + " is behind " + target.getName());

				return true;
			}
		}
		else
			_log.fine("isBehindTarget's target not an L2 Character.");

		return false;
	}

	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}

	/**
	 * Return true if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 */
	public boolean isFront(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;
		if (target == null)
			return false;

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= -180 + maxAngleDiff)
				angleDiff += 180;

			if (angleDiff >= 180 - maxAngleDiff)
				angleDiff -= 180;

			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (Config.DEBUG)
					_log.info("Char " + getName() + " is side " + target.getName());

				return true;
			}
		}
		else
			_log.fine("isSideTarget's target not an L2 Character.");

		return false;
	}

	/** Returns true if target is in front of L2Character (shield def etc) */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
			return false;

		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(this.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;

		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;

		if (Math.abs(angleDiff) <= maxAngleDiff)
			return true;

		return false;
	}

	public boolean isFrontTarget()
	{
		return isFront(getTarget());
	}

	/**
	 * Return 1.<BR>
	 * <BR>
	 */
	public double getLevelMod()
	{
		return 1;
	}

	@SuppressWarnings("unchecked")
	public final void setSkillCast(Future newSkillCast)
	{
		_skillCast = newSkillCast;
	}

	public final void setSkillCastEndTime(int newSkillCastEndTime)
	{
		setIsCastingNow(true);
		_castEndTime = newSkillCastEndTime;
		// for interrupt -12 ticks; first removing the extra second and then -200 ms
		_castInterruptTime = newSkillCastEndTime - 12;
	}

	@SuppressWarnings("unchecked")
	private Future _PvPRegTask;
	private long _pvpFlagLasts;

	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	public void startPvPFlag()
	{
		updatePvPFlag(1);
		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}

	public void stopPvpRegTask()
	{
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(true);
		}
	}

	public void stopPvPFlag()
	{
		stopPvpRegTask();
		updatePvPFlag(0);
		_PvPRegTask = null;
	}

	public void updatePvPFlag(int value)
	{
		if (!(this instanceof L2PcInstance))
		{
			return;
		}
		L2PcInstance player = (L2PcInstance) this;
		if (player.getPvpFlag() == value)
		{
			return;
		}
		player.setPvpFlag(value);
		player.sendPacket(new UserInfo(player));
		for (L2PcInstance target : getKnownList().getKnownPlayers().values())
		{
			target.sendPacket(new RelationChanged(player, player.getRelation(player), player.isAutoAttackable(target)));
		}
	}

	// public void checkPvPFlag()
	// {
	// if (Config.DEBUG) _log.fine("Checking PvpFlag");
	// _PvPRegTask = ThreadPoolManager.getInstance().scheduleLowAtFixedRate(
	// new PvPFlag(), 1000, 5000);
	// _PvPRegActive = true;
	// // _log.fine("PvP recheck");
	// }
	//
	/**
	 * Return a Random Damage in function of the weapon.<BR>
	 * <BR>
	 */
	public final int getRandomDamage(L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return 1;
		}
		return weaponItem.getRandomDamage();
	}

	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}

	public int getAttackEndTime()
	{
		return _attackEndTime;
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 */
	public abstract int getLevel();

	// =========================================================
	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}

	// Property - Public
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}

	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}

	public int getCON()
	{
		return getStat().getCON();
	}

	public int getDEX()
	{
		return getStat().getDEX();
	}

	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public int getINT()
	{
		return getStat().getINT();
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}

	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}

	public int getMAtkSps(L2Character target, L2Skill skill)
	{
		int matk = (int) calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				matk *= 4;
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				matk *= 2;
			}
		}
		return matk;
	}

	public int getMAtkSpd()
	{
		return (int) (getStat().getMAtkSpd() * Config.CP_MAG);
	}

	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}

	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public int getMEN()
	{
		return getStat().getMEN();
	}

	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}

	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}

	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}

	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}

	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}

	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}

	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}

	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}

	public int getPAtkSpd()
	{
		return (int) (getStat().getPAtkSpd() * Config.AP_FIG);
	}

	public double getPAtkUndead(L2Character target)
	{
		return getStat().getPAtkUndead(target);
	}

	public double getPDefUndead(L2Character target)
	{
		return getStat().getPDefUndead(target);
	}

	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}

	public final int getShldDef()
	{
		return getStat().getShldDef();
	}

	public int getSTR()
	{
		return getStat().getSTR();
	}

	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}

	public int getWIT()
	{
		return getStat().getWIT();
	}

	// =========================================================
	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}

	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if (this instanceof L2NpcInstance)
		{
			if (Config.INVUL_NPC_LIST.contains(Integer.valueOf(((L2NpcInstance) this).getNpcId())))
				return;
		}
		if (Config.CHAMPION_ENABLE && isChampion() && Config.CHAMPION_HP != 0)
		{
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake);
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake);
		}
	}

	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}

	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}

	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}

	// Property - Public
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}

	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}

	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}

	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}

	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}

	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}

	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}

	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}

	// =========================================================
	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}

	public String getAiClass()
	{
		return _aiClass;
	}

	public L2Character getLastBuffer()
	{
		return _lastBuffer;
	}

	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	public boolean isChampion()
	{
		return _champion;
	}
	
	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}

	public void setLastBuffer(L2Character buffer)
	{
		_lastBuffer = buffer;
	}

	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}

	/**
	 * Check if character reflected skill
	 *
	 * @param skill
	 * @return
	 */
	public boolean reflectSkill(L2Skill skill)
	{
		double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, null);
		if (Rnd.get(100) < reflect)
			return true;

		return false;
	}

	/**
	 * Check player max buff count
	 *
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}

	/**
	 * Send system message about damage.
	 *  Overriden in  :
	 * L2PcInstance L2SummonInstance L2PetInstance
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}

	public ForceBuff getForceBuff()
	{
        return _forceBuff;
	}

    public void setForceBuff(ForceBuff fb)
    {
        _forceBuff = fb;
    }

	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}

	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}

	public boolean isRaidMinion()
	{
		return _isMinion;
	}

	/**
	 * Set this Npc as a Minion instance.
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isMinion = val;
	}

	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}

	/**
	 * @param showSummonAnimation
	 *            The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}

	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}
}
