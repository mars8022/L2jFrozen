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
package com.l2jfrozen.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.BoatKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.OnVehicleCheckLocation;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleDeparture;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleInfo;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author eX1steam, l2jfrozen
 */
public class L2BoatInstance extends L2Character
{
	protected static final Logger LOGGER = Logger.getLogger(L2BoatInstance.class);
	
	public float boatSpeed;
	
	private class L2BoatTrajet
	{
		private Map<Integer, L2BoatPoint> _path;
		
		public int idWaypoint1;
		public int idWTicket1;
		public int ntx1;
		public int nty1;
		public int ntz1;
		public int max;
		public String boatName;
		public String npc1;
		public String sysmess10_1;
		public String sysmess5_1;
		public String sysmess1_1;
		public String sysmessb_1;
		public String sysmess0_1;
		
		protected class L2BoatPoint
		{
			public int speed1;
			public int speed2;
			public int x;
			public int y;
			public int z;
			public int time;
		}
		
		/**
		 * @param pIdWaypoint1
		 * @param pIdWTicket1
		 * @param pNtx1
		 * @param pNty1
		 * @param pNtz1
		 * @param pNpc1
		 * @param pSysmess10_1
		 * @param pSysmess5_1
		 * @param pSysmess1_1
		 * @param pSysmess0_1
		 * @param pSysmessb_1
		 * @param pBoatname
		 */
		public L2BoatTrajet(final int pIdWaypoint1, final int pIdWTicket1, final int pNtx1, final int pNty1, final int pNtz1, final String pNpc1, final String pSysmess10_1, final String pSysmess5_1, final String pSysmess1_1, final String pSysmess0_1, final String pSysmessb_1, final String pBoatname)
		{
			idWaypoint1 = pIdWaypoint1;
			idWTicket1 = pIdWTicket1;
			ntx1 = pNtx1;
			nty1 = pNty1;
			ntz1 = pNtz1;
			npc1 = pNpc1;
			sysmess10_1 = pSysmess10_1;
			sysmess5_1 = pSysmess5_1;
			sysmess1_1 = pSysmess1_1;
			sysmessb_1 = pSysmessb_1;
			sysmess0_1 = pSysmess0_1;
			boatName = pBoatname;
			loadBoatPath();
		}
		
		/**
		 * @param line
		 */
		public void parseLine(final String line)
		{
			// L2BoatPath bp = new L2BoatPath();
			_path = new FastMap<>();
			StringTokenizer st = new StringTokenizer(line, ";");
			Integer.parseInt(st.nextToken());
			max = Integer.parseInt(st.nextToken());
			for (int i = 0; i < max; i++)
			{
				final L2BoatPoint bp = new L2BoatPoint();
				bp.speed1 = Integer.parseInt(st.nextToken());
				bp.speed2 = Integer.parseInt(st.nextToken());
				bp.x = Integer.parseInt(st.nextToken());
				bp.y = Integer.parseInt(st.nextToken());
				bp.z = Integer.parseInt(st.nextToken());
				bp.time = Integer.parseInt(st.nextToken());
				_path.put(i, bp);
			}
			st = null;
			return;
		}
		
		protected void loadBoatPath()
		{
			FileReader reader = null;
			BufferedReader buff = null;
			LineNumberReader lnr = null;
			
			try
			{
				final File boatpath = new File(Config.DATAPACK_ROOT, "data/boatpath.csv");
				
				reader = new FileReader(boatpath);
				buff = new BufferedReader(reader);
				lnr = new LineNumberReader(buff);
				
				boolean token = false;
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().length() == 0 || !line.startsWith(idWaypoint1 + ";"))
					{
						continue;
					}
					parseLine(line);
					token = true;
					break;
				}
				if (!token)
					LOGGER.warn("No path for boat " + boatName + " !!!");
			}
			catch (final FileNotFoundException e)
			{
				LOGGER.error("boatpath.csv is missing in data folder", e);
			}
			catch (final Exception e)
			{
				LOGGER.error("Error while creating boat table ", e);
			}
			finally
			{
				if (lnr != null)
					try
					{
						lnr.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
				if (buff != null)
					try
					{
						buff.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
				if (reader != null)
					try
					{
						reader.close();
					}
					catch (final Exception e1)
					{
						e1.printStackTrace();
					}
				
			}
		}
		
		/**
		 * @param state
		 * @param _boat
		 * @return
		 */
		public int state(final int state, final L2BoatInstance _boat)
		{
			if (state < max)
			{
				final L2BoatPoint bp = _path.get(state);
				final double dx = _boat.getX() - bp.x;
				final double dy = _boat.getY() - bp.y;
				final double distance = Math.sqrt(dx * dx + dy * dy);
				final double cos = dx / distance;
				final double sin = dy / distance;
				
				_boat.getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
				
				_boat._vd = new VehicleDeparture(_boat, bp.speed1, bp.speed2, bp.x, bp.y, bp.z);
				// _boat.getTemplate().baseRunSpd = bp.speed1;
				boatSpeed = bp.speed1;
				_boat.moveToLocation(bp.x, bp.y, bp.z, (float) bp.speed1);
				Collection<L2PcInstance> knownPlayers = _boat.getKnownList().getKnownPlayers().values();
				if (knownPlayers == null || knownPlayers.isEmpty())
					return bp.time;
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(_boat._vd);
				}
				knownPlayers = null;
				
				if (bp.time == 0)
				{
					bp.time = 1;
				}
				
				return bp.time;
			}
			return 0;
		}
	}
	
	private final String _name;
	protected L2BoatTrajet _t1;
	protected L2BoatTrajet _t2;
	protected int _cycle = 0;
	protected VehicleDeparture _vd = null;
	private Map<Integer, L2PcInstance> _inboat;
	
	public L2BoatInstance(final int objectId, final L2CharTemplate template, final String name)
	{
		super(objectId, template);
		super.setKnownList(new BoatKnownList(this));
		/*
		 * super.setStat(new DoorStat(new L2DoorInstance[] {this})); super.setStatus(new DoorStatus(new L2DoorInstance[] {this}));
		 */
		_name = name;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param speed
	 */
	public void moveToLocation(final int x, final int y, final int z, final float speed)
	{
		final int curX = getX();
		final int curY = getY();
		// final int curZ = getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		final int dx = x - curX;
		final int dy = y - curY;
		final double distance = Math.sqrt(dx * dx + dy * dy);
		
		/*
		 * if(Config.DEBUG) { _logBoat.fine("distance to target:" + distance); }
		 */
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		final double cos = dx / distance;
		final double sin = dy / distance;
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Caclulate the Nb of ticks between the current position and the destination
		// int ticksToMove = (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		
		// Calculate and set the heading of the L2Character
		getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
		
		/*
		 * if(Config.DEBUG) { _logBoat.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getPosition().getHeading()); }
		 */
		
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		m._heading = 0;
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m._moveStartTime = GameTimeController.getGameTicks();
		
		/*
		 * if(Config.DEBUG) { _logBoat.fine("time to target:" + ticksToMove); }
		 */
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		m = null;
	}
	
	class BoatCaptain implements Runnable
	{
		private final int _state;
		private final L2BoatInstance _boat;
		
		/**
		 * @param i
		 * @param instance
		 */
		public BoatCaptain(final int i, final L2BoatInstance instance)
		{
			_state = i;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			BoatCaptain bc;
			switch (_state)
			{
				case 1:
					_boat.say(5);
					bc = new BoatCaptain(2, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 240000);
					break;
				case 2:
					_boat.say(1);
					bc = new BoatCaptain(3, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 40000);
					break;
				case 3:
					_boat.say(0);
					bc = new BoatCaptain(4, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 20000);
					break;
				case 4:
					_boat.say(-1);
					_boat.begin();
					break;
			}
		}
	}
	
	class Boatrun implements Runnable
	{
		private int _state;
		private final L2BoatInstance _boat;
		
		/**
		 * @param i
		 * @param instance
		 */
		public Boatrun(final int i, final L2BoatInstance instance)
		{
			_state = i;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			if (!_inCycle)
				return;
			
			_boat._vd = null;
			_boat.needOnVehicleCheckLocation = false;
			
			if (_boat._cycle == 1)
			{
				final int time = _boat._t1.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					Boatrun bc = new Boatrun(_state, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
					bc = null;
				}
				else if (time == 0)
				{
					_boat._cycle = 2;
					_boat.say(10);
					BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
					bc = null;
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat._runstate = _state;
				}
			}
			else if (_boat._cycle == 2)
			{
				final int time = _boat._t2.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					Boatrun bc = new Boatrun(_state, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
					bc = null;
				}
				else if (time == 0)
				{
					_boat._cycle = 1;
					_boat.say(10);
					BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
					bc = null;
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat._runstate = _state;
				}
			}
		}
	}
	
	public int _runstate = 0;
	
	/**
	 *
	 */
	public void evtArrived()
	{
		
		if (_runstate != 0)
		{
			// _runstate++;
			Boatrun bc = new Boatrun(_runstate, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 10);
			_runstate = 0;
			bc = null;
		}
	}
	
	/**
	 * @param activeChar
	 */
	public void sendVehicleDeparture(final L2PcInstance activeChar)
	{
		if (_vd != null)
		{
			activeChar.sendPacket(_vd);
		}
	}
	
	public VehicleDeparture getVehicleDeparture()
	{
		return _vd;
	}
	
	public void beginCycle()
	{
		say(10);
		BoatCaptain bc = new BoatCaptain(1, this);
		ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
		bc = null;
	}
	
	private int lastx = -1;
	private int lasty = -1;
	protected boolean needOnVehicleCheckLocation = false;
	protected boolean _inCycle = true;
	private int _id;
	
	public void updatePeopleInTheBoat(final int x, final int y, final int z)
	{
		
		if (_inboat != null)
		{
			boolean check = false;
			if (lastx == -1 || lasty == -1)
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			else if ((x - lastx) * (x - lastx) + (y - lasty) * (y - lasty) > 2250000) // 1500 * 1500 = 2250000
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			for (int i = 0; i < _inboat.size(); i++)
			{
				final L2PcInstance player = _inboat.get(i);
				if (player != null && player.isInBoat())
				{
					if (player.getBoat() == this)
					{
						// player.getKnownList().addKnownObject(this);
						player.getPosition().setXYZ(x, y, z);
						player.revalidateZone(false);
					}
				}
				
				if (check && needOnVehicleCheckLocation && (player != null))
				{
					player.sendPacket(new OnVehicleCheckLocation(this, x, y, z));
				}
			}
		}
		
	}
	
	public void begin()
	{
		if (!_inCycle)
			return;
		
		if (_cycle == 1)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				_inboat = new FastMap<>();
				int i = 0;
				for (final L2PcInstance player : knownPlayers)
				{
					if (player.isInBoat() && player.getBoat() == this)
					{
						L2ItemInstance it;
						it = player.getInventory().getItemByItemId(_t1.idWTicket1);
						if (it != null && it.getCount() >= 1)
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if (it == null && _t1.idWTicket1 == 0)
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t1.ntx1, _t1.nty1, _t1.ntz1, false);
						}
					}
				}
				knownPlayers = null;
			}
			Boatrun bc = new Boatrun(0, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0);
			bc = null;
		}
		else if (_cycle == 2)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				_inboat = new FastMap<>();
				int i = 0;
				for (final L2PcInstance player : knownPlayers)
				{
					if (player.isInBoat() && player.getBoat() == this)
					{
						L2ItemInstance it;
						it = player.getInventory().getItemByItemId(_t2.idWTicket1);
						if (it != null && it.getCount() >= 1)
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if (it == null && _t2.idWTicket1 == 0)
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t2.ntx1, _t2.nty1, _t2.ntz1, false);
						}
					}
				}
				knownPlayers = null;
			}
			Boatrun bc = new Boatrun(0, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0);
			bc = null;
		}
	}
	
	/**
	 * @param i
	 */
	public void say(final int i)
	{
		
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		CreatureSay sm;
		PlaySound ps;
		switch (i)
		{
			case 10:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess10_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess10_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
					return;
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 5:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess5_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess5_1);
				}
				ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
					return;
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 1:
				
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess1_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess1_1);
				}
				ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
					return;
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 0:
				
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess0_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess0_1);
				}
				if (knownPlayers == null || knownPlayers.isEmpty())
					return;
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					// player.sendPacket(ps);
				}
				break;
			case -1:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmessb_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmessb_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
		}
		knownPlayers = null;
		sm = null;
		ps = null;
	}
	
	//
	/**
	 *
	 */
	public void spawn()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		_cycle = 1;
		beginCycle();
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;
		final VehicleInfo vi = new VehicleInfo(this);
		for (final L2PcInstance player : knownPlayers)
		{
			player.sendPacket(vi);
		}
		knownPlayers = null;
	}
	
	/**
	 * @param idWaypoint1
	 * @param idWTicket1
	 * @param ntx1
	 * @param nty1
	 * @param ntz1
	 * @param idnpc1
	 * @param sysmess10_1
	 * @param sysmess5_1
	 * @param sysmess1_1
	 * @param sysmess0_1
	 * @param sysmessb_1
	 */
	public void setTrajet1(final int idWaypoint1, final int idWTicket1, final int ntx1, final int nty1, final int ntz1, final String idnpc1, final String sysmess10_1, final String sysmess5_1, final String sysmess1_1, final String sysmess0_1, final String sysmessb_1)
	{
		_t1 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, _name);
	}
	
	public void setTrajet2(final int idWaypoint1, final int idWTicket1, final int ntx1, final int nty1, final int ntz1, final String idnpc1, final String sysmess10_1, final String sysmess5_1, final String sysmess1_1, final String sysmess0_1, final String sysmessb_1)
	{
		_t2 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, _name);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getLevel()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isInCycle()
	{
		return _inCycle;
	}
	
	public void stopCycle()
	{
		_inCycle = false;
		stopMove(new L2CharPosition(getX(), getY(), getZ(), getPosition().getHeading()));
	}
	
	public void startCycle()
	{
		_inCycle = true;
		_cycle = 1;
		beginCycle();
	}
	
	public void reloadPath()
	{
		_t1.loadBoatPath();
		_t2.loadBoatPath();
		_cycle = 0;
		stopCycle();
		startCycle();
	}
	
	public String getBoatName()
	{
		return _name;
	}
	
	public int getSizeInside()
	{
		return _inboat == null ? 0 : _inboat.size();
	}
	
	public int getCycle()
	{
		return _cycle;
	}
	
	/**
	 * @return
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @param id
	 */
	public void setId(final int id)
	{
		_id = id;
	}
}
