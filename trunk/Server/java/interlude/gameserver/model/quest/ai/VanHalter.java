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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import interlude.Config;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.DoorTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.instancemanager.GrandBossManager;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.actor.instance.L2DoorInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2RaidBossInstance;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.SpecialCamera;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.gameserver.templates.StatsSet;
import interlude.gameserver.ThreadPoolManager;
import interlude.L2DatabaseFactory;
import interlude.util.Rnd;

/**
 * This class ...
 * control for sequence of fight against "High Priestess van Halter".
 * @version $Revision: $ $Date: $
 * @author L2J_JP SANDMAN
**/
public class VanHalter extends Quest implements Runnable
{
	private static final Logger _log = Logger.getLogger(VanHalter.class.getName());

	// List of intruders.
	protected Map<Integer, List<L2PcInstance>>	_BleedingPlayers		= new FastMap<Integer, List<L2PcInstance>>();

	// Spawn data of monsters.
	protected List<L2Spawn>					_RoyalGuardSpawn			= new FastList<L2Spawn>();
	protected List<L2Spawn>					_RoyalGuardCaptainSpawn		= new FastList<L2Spawn>();
	protected List<L2Spawn>					_RoyalGuardHelperSpawn		= new FastList<L2Spawn>();
	protected List<L2Spawn>					_TriolRevelationSpawn		= new FastList<L2Spawn>();
	protected List<L2Spawn>					_TriolRevelationAlive		= new FastList<L2Spawn>();
	protected List<L2Spawn>					_GuardOfAltarSpawn			= new FastList<L2Spawn>();
	protected Map<Integer, L2Spawn>			_CameraMarkerSpawn			= new FastMap<Integer, L2Spawn>();
	protected L2Spawn						_RitualOfferingSpawn		= null;
	protected L2Spawn						_RitualSacrificeSpawn		= null;
	protected L2Spawn						_VanHalterSpawn				= null;

	// Instance of monsters.
	protected List<L2NpcInstance>			_RoyalGuard					= new FastList<L2NpcInstance>();
	protected List<L2NpcInstance>			_RoyalGuardCaptain			= new FastList<L2NpcInstance>();
	protected List<L2NpcInstance>			_RoyalGuardHepler			= new FastList<L2NpcInstance>();
	protected List<L2NpcInstance>			_TriolRevelation			= new FastList<L2NpcInstance>();
	protected List<L2NpcInstance>			_GuardOfAltar				= new FastList<L2NpcInstance>();
	protected Map<Integer, L2NpcInstance>	_CameraMarker				= new FastMap<Integer, L2NpcInstance>();
	protected List<L2DoorInstance>			_DoorOfAltar				= new FastList<L2DoorInstance>();
	protected List<L2DoorInstance>			_DoorOfSacrifice			= new FastList<L2DoorInstance>();
	protected L2NpcInstance					_RitualOffering				= null;
	protected L2NpcInstance					_RitualSacrifice			= null;
	protected L2RaidBossInstance			_VanHalter					= null;

	// Task
	protected ScheduledFuture<?>			_MovieTask					= null;
	protected ScheduledFuture<?>			_CloseDoorOfAltarTask		= null;
	protected ScheduledFuture<?>			_OpenDoorOfAltarTask		= null;
	protected ScheduledFuture<?>			_LockUpDoorOfAltarTask		= null;
	protected ScheduledFuture<?>			_CallRoyalGuardHelperTask	= null;
	protected ScheduledFuture<?>			_TimeUpTask					= null;
	protected ScheduledFuture<?>			_IntervalTask				= null;
	protected ScheduledFuture<?>			_HalterEscapeTask			= null;
	protected ScheduledFuture<?>			_SetBleedTask				= null;

	// State of High Priestess van Halter
	boolean									_isLocked					= false;
	boolean									_isHalterSpawned			= false;
	boolean									_isSacrificeSpawned			= false;
	boolean									_isCaptainSpawned			= false;
	boolean									_isHelperCalled				= false;
	
	//VanHalter Status Tracking :
	private static final byte INTERVAL = 0;
	private static final byte NOTSPAWN = 1;
	private static final byte ALIVE = 2;

	// Initialize
	public VanHalter(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {29062, 22188, 32058, 32059, 32060, 32061, 32062, 32063, 32064, 32065, 32066};
		this.addEventId(29062, Quest.QuestEventType.ON_ATTACK);
        for (int mob : mobs)
        {
            this.addEventId(mob, Quest.QuestEventType.ON_KILL);
        }

		//GrandBossManager.getInstance().addBoss(29062);
		// Clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		// Setting door state.
		_DoorOfAltar.add(DoorTable.getInstance().getDoor(19160014));
		_DoorOfAltar.add(DoorTable.getInstance().getDoor(19160015));
		openDoorOfAltar(true);
		_DoorOfSacrifice.add(DoorTable.getInstance().getDoor(19160016));
		_DoorOfSacrifice.add(DoorTable.getInstance().getDoor(19160017));
		closeDoorOfSacrifice();

		// Load spawn data of monsters.
		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();

		// Spawn monsters.
		spawnRoyalGuard();
		spawnTriolRevelation();
		spawnVanHalter();
		spawnRitualOffering();

		// Setting spawn data of Dummy camera marker.
		_CameraMarkerSpawn.clear();
		try
		{
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(13014); // Dummy npc
			L2Spawn tempSpawn;

			// Dummy camera marker.
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_CameraMarkerSpawn.put(1, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_CameraMarkerSpawn.put(2, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_CameraMarkerSpawn.put(3, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_CameraMarkerSpawn.put(4, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55197);
			tempSpawn.setLocz(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_CameraMarkerSpawn.put(5, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager : " + e.getMessage() + " :" + e);
		}

		// Set time up.
		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 21600);

		// Set bleeding to palyers.
		if (_SetBleedTask != null)
			_SetBleedTask.cancel(false);
		_SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);

		int status = GrandBossManager.getInstance().getBossStatus(29062);
		if (status == INTERVAL)
			enterInterval();
		else
			GrandBossManager.getInstance().setBossStatus(29062,NOTSPAWN);
	}

	// Load Royal Guard.
	protected void loadRoyalGuard()
	{
		_RoyalGuardSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 22175);
			statement.setInt(2, 22176);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_RoyalGuardSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuard: Loaded " + _RoyalGuardSpawn.size() + " Royal Guard spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnRoyalGuard()
	{
		if (!_RoyalGuard.isEmpty())
			deleteRoyalGuard();

		for (L2Spawn rgs : _RoyalGuardSpawn)
		{
			rgs.startRespawn();
			_RoyalGuard.add(rgs.doSpawn());
		}
	}

	protected void deleteRoyalGuard()
	{
		for (L2NpcInstance rg : _RoyalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}

		_RoyalGuard.clear();
	}

	// Load Triol's Revelation.
	protected void loadTriolRevelation()
	{
		_TriolRevelationSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con
					.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 32058);
			statement.setInt(2, 32068);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_TriolRevelationSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadTriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadTriolRevelation: Loaded " + _TriolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadTriolRevelation: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnTriolRevelation()
	{
		if (!_TriolRevelation.isEmpty())
			deleteTriolRevelation();

		for (L2Spawn trs : _TriolRevelationSpawn)
		{
			trs.startRespawn();
			_TriolRevelation.add(trs.doSpawn());
			if (trs.getNpcid() != 32067 && trs.getNpcid() != 32068)
				_TriolRevelationAlive.add(trs);
		}
	}

	protected void deleteTriolRevelation()
	{
		for (L2NpcInstance tr : _TriolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_TriolRevelation.clear();
		_BleedingPlayers.clear();
	}

	// Load Royal Guard Captain.
	protected void loadRoyalGuardCaptain()
	{
		_RoyalGuardCaptainSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con
					.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_RoyalGuardCaptainSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuardCaptain: Loaded " + _RoyalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnRoyalGuardCaptain()
	{
		if (!_RoyalGuardCaptain.isEmpty())
			deleteRoyalGuardCaptain();

		for (L2Spawn trs : _RoyalGuardCaptainSpawn)
		{
			trs.startRespawn();
			_RoyalGuardCaptain.add(trs.doSpawn());
		}
		_isCaptainSpawned = true;
	}

	protected void deleteRoyalGuardCaptain()
	{
		for (L2NpcInstance tr : _RoyalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_RoyalGuardCaptain.clear();
	}

	// Load Royal Guard Helper.
	protected void loadRoyalGuardHelper()
	{
		_RoyalGuardHelperSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_RoyalGuardHelperSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuardHelper: Loaded " + _RoyalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnRoyalGuardHepler()
	{
		for (L2Spawn trs : _RoyalGuardHelperSpawn)
		{
			trs.startRespawn();
			_RoyalGuardHepler.add(trs.doSpawn());
		}
	}

	protected void deleteRoyalGuardHepler()
	{
		for (L2NpcInstance tr : _RoyalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_RoyalGuardHepler.clear();
	}

	// Load Guard Of Altar
	protected void loadGuardOfAltar()
	{
		_GuardOfAltarSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_GuardOfAltarSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadGuardOfAltar: Loaded " + _GuardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnGuardOfAltar()
	{
		if (!_GuardOfAltar.isEmpty())
			deleteGuardOfAltar();

		for (L2Spawn trs : _GuardOfAltarSpawn)
		{
			trs.startRespawn();
			_GuardOfAltar.add(trs.doSpawn());
		}
	}

	protected void deleteGuardOfAltar()
	{
		for (L2NpcInstance tr : _GuardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_GuardOfAltar.clear();
	}

	// Load High Priestess van Halter.
	protected void loadVanHalter()
	{
		_VanHalterSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_VanHalterSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadVanHalter: Loaded High Priestess van Halter spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnVanHalter()
	{
		_VanHalter = (L2RaidBossInstance) _VanHalterSpawn.doSpawn();
		//_vanHalter.setIsImmobilized(true);
		_VanHalter.setIsInvul(true);
		_isHalterSpawned = true;
	}

	protected void deleteVanHalter()
	{
		//_vanHalter.setIsImmobilized(false);
		_VanHalter.setIsInvul(false);
		_VanHalter.getSpawn().stopRespawn();
		_VanHalter.deleteMe();
	}

	// Load Ritual Offering.
	protected void loadRitualOffering()
	{
		_RitualOfferingSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_RitualOfferingSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRitualOffering: Loaded Ritual Offering spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnRitualOffering()
	{
		_RitualOffering = _RitualOfferingSpawn.doSpawn();
		//_ritualOffering.setIsImmobilized(true);
		_RitualOffering.setIsInvul(true);
		_RitualOffering.setIsParalyzed(true);
	}

	protected void deleteRitualOffering()
	{
		//_ritualOffering.setIsImmobilized(false);
		_RitualOffering.setIsInvul(false);
		_RitualOffering.setIsParalyzed(false);
		_RitualOffering.getSpawn().stopRespawn();
		_RitualOffering.deleteMe();
	}

	// Load Ritual Sacrifice.
	protected void loadRitualSacrifice()
	{
		_RitualSacrificeSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_RitualSacrificeSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRitualSacrifice: Loaded Ritual Sacrifice spawn locations.");
			}
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			_log.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
		}
		finally
		{
			try
			{
				con.close();
				con = null;
			} 
			catch (Exception e)
			{
				//null;
			}
		}
	}

	protected void spawnRitualSacrifice()
	{
		_RitualSacrifice = _RitualSacrificeSpawn.doSpawn();
		//_ritualSacrifice.setIsImmobilized(true);
		_RitualSacrifice.setIsInvul(true);
		_isSacrificeSpawned = true;
	}

	protected void deleteRitualSacrifice()
	{
		if (!_isSacrificeSpawned)
			return;

		_RitualSacrifice.getSpawn().stopRespawn();
		_RitualSacrifice.deleteMe();
		_isSacrificeSpawned = false;
	}

	protected void spawnCameraMarker()
	{
		_CameraMarker.clear();
		for (int i = 1; i <= _CameraMarkerSpawn.size(); i++)
		{
			_CameraMarker.put(i, _CameraMarkerSpawn.get(i).doSpawn());
			_CameraMarker.get(i).getSpawn().stopRespawn();
			//_cameraMarker.get(i).setIsImmobilized(true);
		}
	}

	protected void deleteCameraMarker()
	{
		if (_CameraMarker.isEmpty()) return;

		for (int i = 1; i <= _CameraMarker.size(); i++)
		{
			_CameraMarker.get(i).deleteMe();
		}
		_CameraMarker.clear();
	}

	// Door control.
	/**
	 * @param intruder
	 */
	public void intruderDetection(L2PcInstance intruder)
	{
		if (_LockUpDoorOfAltarTask == null && !_isLocked && _isCaptainSpawned)
		{
			_LockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(), 180);
		}
	}

	private class LockUpDoorOfAltar implements Runnable
	{
		public void run()
		{
			closeDoorOfAltar(false);
			_isLocked = true;
			_LockUpDoorOfAltarTask = null;
		}
	}

	protected void openDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _DoorOfAltar)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage() + " :" + e);
			}
		}

		if (loop)
		{
			_isLocked = false;

			if (_CloseDoorOfAltarTask != null)
				_CloseDoorOfAltarTask.cancel(false);
			_CloseDoorOfAltarTask = null;
			_CloseDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), 5400);
		}
		else
		{
			if (_CloseDoorOfAltarTask != null)
				_CloseDoorOfAltarTask.cancel(false);
			_CloseDoorOfAltarTask = null;
		}
	}

	private class OpenDoorOfAltar implements Runnable
	{
		public void run()
		{
			openDoorOfAltar(true);
		}
	}

	protected void closeDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _DoorOfAltar)
		{
			door.closeMe();
		}

		if (loop)
		{
			if (_OpenDoorOfAltarTask != null)
				_OpenDoorOfAltarTask.cancel(false);
			_OpenDoorOfAltarTask = null;
			_OpenDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), 5400);
		}
		else
		{
			if (_OpenDoorOfAltarTask != null)
				_OpenDoorOfAltarTask.cancel(false);
			_OpenDoorOfAltarTask = null;
		}
	}

	private class CloseDoorOfAltar implements Runnable
	{
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}

	protected void openDoorOfSacrifice()
	{
		for (L2DoorInstance door : _DoorOfSacrifice)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage() + " :" + e);
			}
		}
	}

	protected void closeDoorOfSacrifice()
	{
		for (L2DoorInstance door : _DoorOfSacrifice)
		{
			try
			{
				door.closeMe();
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage() + " :" + e);
			}
		}
	}

	// event
	public void checkTriolRevelationDestroy()
	{
		if (_isCaptainSpawned) return;

		boolean isTriolRevelationDestroyed = true;
		for (L2Spawn tra : _TriolRevelationAlive)
		{
			if (!tra.getLastSpawn().isDead())
				isTriolRevelationDestroyed = false;
		}

		if (isTriolRevelationDestroyed)
			spawnRoyalGuardCaptain();
	}

	public void checkRoyalGuardCaptainDestroy()
	{
		if (!_isHalterSpawned) return;

		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();

		//_vanHalter.setIsImmobilized(true);
		_VanHalter.setIsInvul(true);
		spawnCameraMarker();

		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = null;

		_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), 20);
	}

	// Start fight against High Priestess van Halter.
	protected void combatBeginning()
	{
		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 20);

		Map<Integer, L2PcInstance> _targets = new FastMap<Integer, L2PcInstance>();
		int i = 0;

		for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
		{
			i++;
			_targets.put(i, pc);
		}

		_VanHalter.reduceCurrentHp(1, _targets.get(Rnd.get(1, i)));
	}

	// Call Royal Guard Helper and escape from player.
	public void callRoyalGuardHelper()
	{
		if (!_isHelperCalled)
		{
			_isHelperCalled = true;
			_HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500);
			_CallRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000);
		}
	}

	private class CallRoyalGuardHelper implements Runnable
	{
		public void run()
		{
			spawnRoyalGuardHepler();

			if (_RoyalGuardHepler.size() <= 6 && !_VanHalter.isDead())
			{
				if (_CallRoyalGuardHelperTask != null)
					_CallRoyalGuardHelperTask.cancel(false);
				_CallRoyalGuardHelperTask = ThreadPoolManager.getInstance()
						.scheduleGeneral(new CallRoyalGuardHelper(), 10);
			}
			else
			{
				if (_CallRoyalGuardHelperTask != null)
					_CallRoyalGuardHelperTask.cancel(false);
				_CallRoyalGuardHelperTask = null;
			}
		}
	}

	private class HalterEscape implements Runnable
	{
		public void run()
		{
			if (_RoyalGuardHepler.size() <= 6 && !_VanHalter.isDead())
			{
				if (_VanHalter.isAfraid())
				{
					_VanHalter.stopEffects(L2Effect.EffectType.FEAR);
					_VanHalter.setIsAfraid(false);
					_VanHalter.updateAbnormalEffect();
				}
				else
				{
					_VanHalter.startFear();
					if (_VanHalter.getZ() >= -10476)
					{
						L2CharPosition pos = new L2CharPosition(-16397, -53308, -10448, 0);
						if (_VanHalter.getX() == pos.x && _VanHalter.getY() == pos.y)
						{
							_VanHalter.stopEffects(L2Effect.EffectType.FEAR);
							_VanHalter.setIsAfraid(false);
							_VanHalter.updateAbnormalEffect();
						}
						else
						{
							_VanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
						}
					}
					else if (_VanHalter.getX() >= -16397)
					{
						L2CharPosition pos = new L2CharPosition(-15548, -54830, -10475, 0);
						_VanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else
					{
						L2CharPosition pos = new L2CharPosition(-17248, -54830, -10475, 0);
						_VanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
				}
				if (_HalterEscapeTask != null)
					_HalterEscapeTask.cancel(false);
				_HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 5000);
			}
			else
			{
				_VanHalter.stopEffects(L2Effect.EffectType.FEAR);
				_VanHalter.setIsAfraid(false);
				_VanHalter.updateAbnormalEffect();
				if (_HalterEscapeTask != null)
					_HalterEscapeTask.cancel(false);
				_HalterEscapeTask = null;
			}
		}
	}

	// Check bleeding player.
	protected void addBleeding()
	{
		L2Skill bleed = SkillTable.getInstance().getInfo(4615, 12);

		for (L2NpcInstance tr : _TriolRevelation)
		{
			if (!tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).iterator().hasNext() || tr.isDead())
				continue;

			List<L2PcInstance> bpc = new FastList<L2PcInstance>();

			for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUser(tr, pc, bleed.getId(), 12, 1, 1));
				}

				bpc.add(pc);
			}
			_BleedingPlayers.remove(tr.getNpcId());
			_BleedingPlayers.put(tr.getNpcId(), bpc);
		}
	}

	public void removeBleeding(int npcId)
	{
		if (_BleedingPlayers.get(npcId) == null)
			return;
		for (L2PcInstance pc : (FastList<L2PcInstance>) _BleedingPlayers.get(npcId))
		{
			if (pc.getFirstEffect(L2Effect.EffectType.DMG_OVER_TIME) != null)
				pc.stopEffects(L2Effect.EffectType.DMG_OVER_TIME);
		}
		_BleedingPlayers.remove(npcId);
	}

	private class Bleeding implements Runnable
	{
		public void run()
		{
			addBleeding();

			if (_SetBleedTask != null)
				_SetBleedTask.cancel(false);
			_SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		}
	}

	// High Priestess van Halter dead or time up.
	public void enterInterval()
	{
		// Cancel all task
		if (_CallRoyalGuardHelperTask != null)
			_CallRoyalGuardHelperTask.cancel(false);
		_CallRoyalGuardHelperTask = null;

		if (_CloseDoorOfAltarTask != null)
			_CloseDoorOfAltarTask.cancel(false);
		_CloseDoorOfAltarTask = null;

		if (_HalterEscapeTask != null)
			_HalterEscapeTask.cancel(false);
		_HalterEscapeTask = null;

		if (_IntervalTask != null)
			_IntervalTask.cancel(false);
		_IntervalTask = null;

		if (_LockUpDoorOfAltarTask != null)
			_LockUpDoorOfAltarTask.cancel(false);
		_LockUpDoorOfAltarTask = null;

		if (_MovieTask != null)
			_MovieTask.cancel(false);
		_MovieTask = null;

		if (_OpenDoorOfAltarTask != null)
			_OpenDoorOfAltarTask.cancel(false);
		_OpenDoorOfAltarTask = null;

		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = null;

		// Delete monsters
		if (_VanHalter.isDead())
			_VanHalter.getSpawn().stopRespawn();
		else
			deleteVanHalter();

		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();

		// Set interval end.
		if (_IntervalTask != null)
			_IntervalTask.cancel(false);
			
		int status = GrandBossManager.getInstance().getBossStatus(29062);

		if (status !=INTERVAL)
		{
			long interval = Rnd.get(172800000, 172800000 + 8640000) * 3600000;
			StatsSet info = GrandBossManager.getInstance().getStatsSet(29062);
			info.set("respawn_time",(System.currentTimeMillis() + interval));
			GrandBossManager.getInstance().setStatsSet(29062,info);
			GrandBossManager.getInstance().setBossStatus(29062,INTERVAL);
		}

		StatsSet info = GrandBossManager.getInstance().getStatsSet(29062);
		long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
		_IntervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), temp);
	}

	// Interval.
	private class Interval implements Runnable
	{
		public void run()
		{
			setupAltar();
		}
	}

	// Interval end.
	public void setupAltar()
	{
		// Cancel all task
		if (_CallRoyalGuardHelperTask != null)
			_CallRoyalGuardHelperTask.cancel(false);
		_CallRoyalGuardHelperTask = null;

		if (_CloseDoorOfAltarTask != null)
			_CloseDoorOfAltarTask.cancel(false);
		_CloseDoorOfAltarTask = null;

		if (_HalterEscapeTask != null)
			_HalterEscapeTask.cancel(false);
		_HalterEscapeTask = null;

		if (_IntervalTask != null)
			_IntervalTask.cancel(false);
		_IntervalTask = null;

		if (_LockUpDoorOfAltarTask != null)
			_LockUpDoorOfAltarTask.cancel(false);
		_LockUpDoorOfAltarTask = null;

		if (_MovieTask != null)
			_MovieTask.cancel(false);
		_MovieTask = null;

		if (_OpenDoorOfAltarTask != null)
			_OpenDoorOfAltarTask.cancel(false);
		_OpenDoorOfAltarTask = null;

		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = null;

		// Delete all monsters
		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();

		// Clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		// Set door state
		closeDoorOfSacrifice();
		openDoorOfAltar(true);

		// Respawn monsters.
		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();

		GrandBossManager.getInstance().setBossStatus(29062,NOTSPAWN);

		// Set time up.
		if (_TimeUpTask != null)
			_TimeUpTask.cancel(false);
		_TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 21600);
	}

	// Time up.
	private class TimeUp implements Runnable
	{
		public void run()
		{
			enterInterval();
		}
	}

	// Appearance movie.
	private class Movie implements Runnable
	{
		private final int					_distance	= 6502500;
		private final int					_taskId;

		public Movie(int taskId)
		{
			_taskId = taskId;
		}

		public void run()
		{
			_VanHalter.setHeading(16384);
			_VanHalter.setTarget(_RitualOffering);

			switch (_taskId)
			{
			case 1:
				GrandBossManager.getInstance().setBossStatus(29062,ALIVE);

				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_VanHalter) <= _distance)
					{
						_VanHalter.broadcastPacket(new SpecialCamera(_VanHalter.getObjectId(), 50, 90, 0, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);

				break;

			case 2:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(5)) <= _distance)
					{
						_CameraMarker.get(5).broadcastPacket(new SpecialCamera(_CameraMarker.get(5).getObjectId(), 1842, 100, -3, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);

				break;

			case 3:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(5)) <= _distance)
					{
						_CameraMarker.get(5).broadcastPacket(new SpecialCamera(_CameraMarker.get(5).getObjectId(), 1861, 97, -10, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);

				break;

			case 4:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(4)) <= _distance)
					{
						_CameraMarker.get(4).broadcastPacket(new SpecialCamera(_CameraMarker.get(4).getObjectId(), 1876, 97, 12, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);

				break;

			case 5:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(4)) <= _distance)
					{
						_CameraMarker.get(4).broadcastPacket(new SpecialCamera(_CameraMarker.get(4).getObjectId(), 1839, 94, 0, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);

				break;

			case 6:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(3)) <= _distance)
					{
						_CameraMarker.get(3).broadcastPacket(new SpecialCamera(_CameraMarker.get(3).getObjectId(), 1872, 94, 15, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);

				break;

			case 7:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(3)) <= _distance)
					{
						_CameraMarker.get(3).broadcastPacket(new SpecialCamera(_CameraMarker.get(3).getObjectId(), 1839, 92, 0, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);

				break;

			case 8:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(2)) <= _distance)
					{
						_CameraMarker.get(2).broadcastPacket(new SpecialCamera(_CameraMarker.get(2).getObjectId(), 1872, 92, 15, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);

				break;

			case 9:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(2)) <= _distance)
					{
						_CameraMarker.get(2).broadcastPacket(new SpecialCamera(_CameraMarker.get(2).getObjectId(), 1839, 90, 5, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);

				break;

			case 10:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(1)) <= _distance)
					{
						_CameraMarker.get(1).broadcastPacket(new SpecialCamera(_CameraMarker.get(1).getObjectId(), 1872, 90, 5, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);

				break;

			case 11:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_CameraMarker.get(1)) <= _distance)
					{
						_CameraMarker.get(1).broadcastPacket(new SpecialCamera(_CameraMarker.get(1).getObjectId(), 2002, 90, 2, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);

				break;

			case 12:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_VanHalter) <= _distance)
					{
						_VanHalter.broadcastPacket(new SpecialCamera(_VanHalter.getObjectId(), 50, 90, 10, 0, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);

				break;

			case 13:
				// High Priestess van Halter uses the skill to kill Ritual Offering.
				L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
				_RitualOffering.setIsInvul(false);
				_VanHalter.setTarget(_RitualOffering);
				//_vanHalter.setIsImmobilized(false);
				_VanHalter.doCast(skill);
				//_vanHalter.setIsImmobilized(true);

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);

				break;

			case 14:
				_RitualOffering.setIsInvul(false);
				_RitualOffering.reduceCurrentHp(_RitualOffering.getMaxHp() + 1, _VanHalter);

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);

				break;

			case 15:
				spawnRitualSacrifice();
				deleteRitualOffering();

				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_VanHalter) <= _distance)
					{
						_VanHalter.broadcastPacket(new SpecialCamera(_VanHalter.getObjectId(), 100, 90, 15, 1500, 15000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);

				break;

			case 16:
				// Set camera.
				for (L2PcInstance pc : _VanHalter.getKnownList().getKnownPlayers().values())
				{
					if (pc.getPlanDistanceSq(_VanHalter) <= _distance)
					{
						_VanHalter.broadcastPacket(new SpecialCamera(_VanHalter.getObjectId(), 5200, 90, -10, 9500, 6000));
					}
				}

				// Set next task.
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);

				break;

			case 17:
				deleteRitualSacrifice();
				deleteCameraMarker();
				//_vanHalter.setIsImmobilized(false);
				_VanHalter.setIsInvul(false);

				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
				_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);

				break;

			case 18:
				combatBeginning();
				if (_MovieTask != null)
					_MovieTask.cancel(false);
				_MovieTask = null;
			}
		}
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 29062)
		{
			if ((int)(npc.getStatus().getCurrentHp() / npc.getMaxHp()) * 100 <= 20)
				callRoyalGuardHelper();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == 32058 || npcId == 32059 || npcId == 32060 || npcId == 32061 || npcId == 32062|| npcId == 32063 || npcId == 32064 || npcId == 32065 || npcId == 32066)
			removeBleeding(npcId);
			checkTriolRevelationDestroy();
		if (npcId == 22188)
			checkRoyalGuardCaptainDestroy();
		if (npcId == 29062)
			enterInterval();
		return super.onKill(npc,killer,isPet);
	}

	public void run()
	{
		//new VanHalter(-1, "vanhalter", "ai");
	}
}
