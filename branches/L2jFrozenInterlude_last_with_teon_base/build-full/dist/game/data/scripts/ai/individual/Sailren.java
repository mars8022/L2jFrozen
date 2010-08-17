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
package ai.individual;

import ai.group_template.L2AttackableAIScript;
import java.util.logging.Logger;
import java.util.concurrent.ScheduledFuture;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javolution.util.FastList;

import interlude.ExternalConfig;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.instancemanager.GrandBossManager;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.gameserver.templates.StatsSet;
import interlude.gameserver.util.Util;
import interlude.util.Rnd;

/**
 * @author SANDMAN
 * adapted by Maxi
 */
public class Sailren extends L2AttackableAIScript
{
    protected static Logger _log = Logger.getLogger(Sailren.class.getName());

    private final int _SailrenCubeLocation[][] =
    	{
    		{27734,-6838,-1982,0}
    	};

    protected List<L2Spawn> _SailrenCubeSpawn = new FastList<L2Spawn>();
    protected List<L2NpcInstance> _SailrenCube = new FastList<L2NpcInstance>();

    protected List<L2PcInstance> _PlayersInSailrenLair = new FastList<L2PcInstance>();

    protected L2Spawn _VelociraptorSpawn;
    protected L2Spawn _PterosaurSpawn;
    protected L2Spawn _TyrannoSpawn;
    protected L2Spawn _SailrenSapwn;

    protected L2NpcInstance _Velociraptor;
    protected L2NpcInstance _Pterosaur;
    protected L2NpcInstance _Tyranno;
    protected L2NpcInstance _Sailren;

    protected ScheduledFuture<?> _CubeSpawnTask = null;
    protected ScheduledFuture<?> _SailrenSpawnTask = null;
    protected ScheduledFuture<?> _IntervalEndTask = null;
    protected ScheduledFuture<?> _ActivityTimeEndTask = null;
    protected ScheduledFuture<?> _OnPartyAnnihilatedTask = null;
    protected ScheduledFuture<?> _SocialTask = null;

    protected String _ZoneType;
    protected String _QuestName;
    protected boolean _IsAlreadyEnteredOtherParty = false;
    protected StatsSet _StateSet;
    protected int _Alive;
    protected int _BossId = 29065;

	private final int GAZKH = 8784;

	public static final int NOTSPAWN = 0;
	public static final int ALIVE = 1;
	public static final int DEAD = 2;
	public static final int INTERVAL = 3;

    public Sailren(int id,String name,String descr)
    {
        super(id,name,descr);
        int[] mobs = {22218,22199,22217,29065,32107,32109};
        this.registerMobs(mobs);
	addStartNpc(32109);
	addTalkId(32109);

    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
        _ZoneType = "Lair of Sailren";
        _QuestName = "sailren";
        _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
        _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

        try
        {
            L2NpcTemplate template1;

            template1 = NpcTable.getInstance().getTemplate(22218); //Velociraptor
            _VelociraptorSpawn = new L2Spawn(template1);
            _VelociraptorSpawn.setLocx(27852);
            _VelociraptorSpawn.setLocy(-5536);
            _VelociraptorSpawn.setLocz(-1983);
            _VelociraptorSpawn.setHeading(44732);
            _VelociraptorSpawn.setAmount(1);
            _VelociraptorSpawn.setRespawnDelay(ExternalConfig.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_VelociraptorSpawn, false);

            template1 = NpcTable.getInstance().getTemplate(22199); //Pterosaur
            _PterosaurSpawn = new L2Spawn(template1);
            _PterosaurSpawn.setLocx(27852);
            _PterosaurSpawn.setLocy(-5536);
            _PterosaurSpawn.setLocz(-1983);
            _PterosaurSpawn.setHeading(44732);
            _PterosaurSpawn.setAmount(1);
            _PterosaurSpawn.setRespawnDelay(ExternalConfig.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_PterosaurSpawn, false);

            template1 = NpcTable.getInstance().getTemplate(22217); //Tyrannosaurus
            _TyrannoSpawn = new L2Spawn(template1);
            _TyrannoSpawn.setLocx(27852);
            _TyrannoSpawn.setLocy(-5536);
            _TyrannoSpawn.setLocz(-1983);
            _TyrannoSpawn.setHeading(44732);
            _TyrannoSpawn.setAmount(1);
            _TyrannoSpawn.setRespawnDelay(ExternalConfig.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_TyrannoSpawn, false);

            template1 = NpcTable.getInstance().getTemplate(29065); //Sailren
            _SailrenSapwn = new L2Spawn(template1);
            _SailrenSapwn.setLocx(27810);
            _SailrenSapwn.setLocy(-5655);
            _SailrenSapwn.setLocz(-1983);
            _SailrenSapwn.setHeading(44732);
            _SailrenSapwn.setAmount(1);
            _SailrenSapwn.setRespawnDelay(ExternalConfig.FWS_ACTIVITYTIMEOFMOBS * 2);
            SpawnTable.getInstance().addNewSpawn(_SailrenSapwn, false);

        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        try
        {
            L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(32107);
            L2Spawn spawnDat;

            for(int i = 0;i < _SailrenCubeLocation.length; i++)
            {
                spawnDat = new L2Spawn(Cube);
                spawnDat.setAmount(1);
                spawnDat.setLocx(_SailrenCubeLocation[i][0]);
                spawnDat.setLocy(_SailrenCubeLocation[i][1]);
                spawnDat.setLocz(_SailrenCubeLocation[i][2]);
                spawnDat.setHeading(_SailrenCubeLocation[i][3]);
                spawnDat.setRespawnDelay(60);
                spawnDat.setLocation(0);
                SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                _SailrenCubeSpawn.add(spawnDat);
            }
        }
        catch (Exception e)
        {
            _log.warning(e.getMessage());
        }

        _log.info("Sailren : State of Sailren is " + _Alive + ".");
        if (_Alive != NOTSPAWN)
        	setInetrvalEndTask();

		Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("Sailren : Next spawn date of Sailren is " + dt + ".");
        _log.info("Sailren : Init Sailren.");

    }

    public List<L2PcInstance> getPlayersInLair()
	{
		return _PlayersInSailrenLair;
	}

    public int canIntoSailrenLair(L2PcInstance pc)
    {
    	if (ExternalConfig.FWS_ENABLESINGLEPLAYER == false && pc.getParty() == null) return 4;
    	else if (_IsAlreadyEnteredOtherParty) return 2;
    	else if (_Alive == NOTSPAWN) return 0;
    	else if (_Alive == ALIVE || _Alive != DEAD) return 1;
    	else if (_Alive == INTERVAL) return 3;
    	else return 0;
    }

    public void setSailrenSpawnTask(int NpcId)
    {
    	if (NpcId == 22218 && _PlayersInSailrenLair.size() >= 1) return;

    	if (_SailrenSpawnTask == null)
        {
        	_SailrenSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(
            		new SailrenSpawn(NpcId),ExternalConfig.FWS_INTERVALOFNEXTMONSTER);
        }
    }

    public void addPlayerToSailrenLair(L2PcInstance pc)
    {
        if (!_PlayersInSailrenLair.contains(pc)) _PlayersInSailrenLair.add(pc);
    }

    public void entryToSailrenLair(L2PcInstance pc)
    {
		int driftx;
		int drifty;

		if(canIntoSailrenLair(pc) != 0)
		{
			pc.sendMessage("...");
			_IsAlreadyEnteredOtherParty = false;
			return;
		}

		if(pc.getParty() == null)
		{
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			pc.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
			addPlayerToSailrenLair(pc);
		}
		else
		{
			List<L2PcInstance> members = new FastList<L2PcInstance>();
			for (L2PcInstance mem : pc.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, pc, mem, true))
				{
					members.add(mem);
				}
			}
			for (L2PcInstance mem : members)
			{
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(27734 + driftx,-6938 + drifty,-1982);
				addPlayerToSailrenLair(mem);
			}
		}
		_IsAlreadyEnteredOtherParty = true;
    }

    public void checkAnnihilated(L2PcInstance pc)
    {
    	if(isPartyAnnihilated(pc))
    	{
    		_OnPartyAnnihilatedTask =
				ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(pc),5000);
    	}
    }

    public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
    {
		if(pc.getParty() != null)
		{
			for(L2PcInstance mem:pc.getParty().getPartyMembers())
			{
				if(!mem.isDead() && GrandBossManager.getInstance().checkIfInZone("Lair of Sailren", pc))
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return true;
		}
    }

    public void banishesPlayers()
    {
    	for(L2PcInstance pc : _PlayersInSailrenLair)
    	{
    		if(pc.getQuestState("sailren") != null) pc.getQuestState("sailren").exitQuest(true);
    		if(GrandBossManager.getInstance().checkIfInZone("Lair of Sailren", pc))
    		{
        		int driftX = Rnd.get(-80,80);
        		int driftY = Rnd.get(-80,80);
        		pc.teleToLocation(10468 + driftX,-24569 + driftY,-3650);
    		}
    	}
    	_PlayersInSailrenLair.clear();
    	_IsAlreadyEnteredOtherParty = false;
    }

    public void setUnspawn()
	{
    	banishesPlayers();

		for (L2NpcInstance cube : _SailrenCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_SailrenCube.clear();

		if(_CubeSpawnTask != null)
		{
			_CubeSpawnTask.cancel(true);
			_CubeSpawnTask = null;
		}
		if(_SailrenSpawnTask != null)
		{
			_SailrenSpawnTask.cancel(true);
			_SailrenSpawnTask = null;
		}
		if(_IntervalEndTask != null)
		{
			_IntervalEndTask.cancel(true);
			_IntervalEndTask = null;
		}
		if(_ActivityTimeEndTask != null)
		{
			_ActivityTimeEndTask.cancel(true);
			_ActivityTimeEndTask = null;
		}

		_Velociraptor = null;
		_Pterosaur = null;
		_Tyranno = null;
		_Sailren = null;

		setInetrvalEndTask();
	}

    public void spawnCube()
    {
		for (L2Spawn spawnDat : _SailrenCubeSpawn)
		{
			_SailrenCube.add(spawnDat.doSpawn());
		}
    }

    public void setCubeSpawn()
    {
    	_Alive = DEAD;
    	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(ExternalConfig.FWS_FIXINTERVALOFSAILRENSPAWN,ExternalConfig.FWS_FIXINTERVALOFSAILRENSPAWN + ExternalConfig.FWS_RANDOMINTERVALOFSAILRENSPAWN));
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);

    	_CubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(),10000);

    	Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("Sailren : Sailren is dead.");
        _log.info("Sailren : Next spawn date of Sailren is " + dt + ".");
    }

    public void setInetrvalEndTask()
    {
    	if (_Alive != INTERVAL)
    	{
        	_Alive = INTERVAL;
        	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	}

    	_IntervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(),GrandBossManager.getInstance().getInterval(_BossId));
    	_log.info("Sailren : Interval START.");
    }

    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _PlayersInSailrenLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
    }

    private class SailrenSpawn implements Runnable
    {
    	int _NpcId;
    	L2CharPosition _pos = new L2CharPosition(27628,-6109,-1982,44732);
    	public SailrenSpawn(int NpcId)
    	{
    		_NpcId = NpcId;
    	}

        public void run()
        {
        	switch (_NpcId)
            {
            	case 22218:
            		_Velociraptor = _VelociraptorSpawn.doSpawn();
            		_Velociraptor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Velociraptor,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Velociraptor),ExternalConfig.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 22199:
            		_VelociraptorSpawn.stopRespawn();
            		_Pterosaur = _PterosaurSpawn.doSpawn();
            		_Pterosaur.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Pterosaur,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Pterosaur),ExternalConfig.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 22217:
            		_PterosaurSpawn.stopRespawn();
            		_Tyranno = _TyrannoSpawn.doSpawn();
            		_Tyranno.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Tyranno,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Tyranno),ExternalConfig.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	case 29065:
            		_TyrannoSpawn.stopRespawn();
            		_Sailren = _SailrenSapwn.doSpawn();

	            	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(ExternalConfig.FWS_FIXINTERVALOFSAILRENSPAWN,ExternalConfig.FWS_FIXINTERVALOFSAILRENSPAWN + ExternalConfig.FWS_RANDOMINTERVALOFSAILRENSPAWN) + ExternalConfig.FWS_ACTIVITYTIMEOFMOBS);
	            	_Alive = ALIVE;
	            	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
	            	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
	            	_log.info("Sailren : Spawn Sailren.");

            		_Sailren.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,_pos);
            		if(_SocialTask != null)
            		{
            			_SocialTask.cancel(true);
            			_SocialTask = null;
            		}
            		_SocialTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new Social(_Sailren,2),6000);
            		if(_ActivityTimeEndTask != null)
            		{
            			_ActivityTimeEndTask.cancel(true);
            			_ActivityTimeEndTask = null;
            		}
            		_ActivityTimeEndTask =
                        ThreadPoolManager.getInstance().scheduleGeneral(
                        		new ActivityTimeEnd(_Sailren),ExternalConfig.FWS_ACTIVITYTIMEOFMOBS);
            		break;
            	default:
            		break;
            }

            if(_SailrenSpawnTask != null)
            {
            	_SailrenSpawnTask.cancel(true);
            	_SailrenSpawnTask = null;
            }
        }
    }

    private class CubeSpawn implements Runnable
    {
    	public CubeSpawn()
    	{
    	}

        public void run()
        {
        	spawnCube();
        }
    }

    private class ActivityTimeEnd implements Runnable
    {
    	L2NpcInstance _Mob;
    	public ActivityTimeEnd(L2NpcInstance npc)
    	{
    		_Mob = npc;
    	}

    	public void run()
    	{
    		if(!_Mob.isDead())
    		{
    			_Mob.deleteMe();
    			_Mob.getSpawn().stopRespawn();
    			_Mob = null;
    		}
    		setUnspawn();
    	}
    }

    private class IntervalEnd implements Runnable
    {
    	public IntervalEnd()
    	{
    	}

    	public void run()
    	{
    		doIntervalEnd();
    	}
    }

    protected void doIntervalEnd()
    {
		_PlayersInSailrenLair.clear();
    	_Alive = NOTSPAWN;
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	_log.info("Sailren : Interval END.");
    }

	private class OnPartyAnnihilatedTask implements Runnable
	{
		L2PcInstance _player;

		public OnPartyAnnihilatedTask(L2PcInstance player)
		{
			_player = player;
		}

		public void run()
		{
			setUnspawn();
		}
	}

    private class Social implements Runnable
    {
        private int _action;
        private L2NpcInstance _npc;

        public Social(L2NpcInstance npc,int actionId)
        {
        	_npc = npc;
            _action = actionId;
        }

        public void run()
        {

        	updateKnownList(_npc);

    		SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);
        }
    }

	/**
	 * 1) The sailren is very powerful now. It is not possible to enter the inside.
	 * 2) Another adventurers have already fought against the sailren. Do not obstruct them.
	 * 3) is necessary for seal the sailren.
	 * 4) Please seal the sailren by your ability.
	 * 5) You may not enter while flying a wyvern
	 */

	public String onTalk (L2NpcInstance npc, L2PcInstance player)
	{
	String htmltext = "";
	if (GrandBossManager.getInstance().getBossStatus(_BossId) == NOTSPAWN || GrandBossManager.getInstance().getBossStatus(_BossId) == ALIVE)
		{
			if (player.isFlying())
			{
				htmltext = "<html><body>Shilen's Stone Statue:<br>You may not enter while flying a wyvern</body></html>";
			}
			else if (player.getQuestState("sailren").getQuestItemsCount(GAZKH) > 0)
			{
				player.getQuestState("sailren").takeItems(GAZKH,1);
				player.teleToLocation(27734 + Rnd.get(-80, 80),-6938 + Rnd.get(-80, 80),-1982);
          			setSailrenSpawnTask(22218);
          			entryToSailrenLair(player);
				htmltext = "";
				if (GrandBossManager.getInstance().getBossStatus(_BossId) == NOTSPAWN)
				{
					GrandBossManager.getInstance().setBossStatus(_BossId,ALIVE);
				}
			}
			else
				htmltext = "<html><body>Shilen's Stone Statue:<br><font color=LEVEL>Gazkh</font> is necessary for seal the sailren.</body></html>";
		}
	else if (GrandBossManager.getInstance().getBossStatus(_BossId) == INTERVAL)
		htmltext = "<html><body>Shilen's Stone Statue:<br><font color=\"LEVEL\">Another adventurers have already fought against the sailren. Do not obstruct them.</font></body></html>";
	else
		htmltext = "<html><body>Shilen's Stone Statue:<br><font color=\"LEVEL\">Please seal the sailren by your ability.</font></body></html>";
	return htmltext;
	}
		//canIntoSailrenLair(player);

    public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
    {
    	int npcId = npc.getNpcId();
    	if (npcId == 22218)
    	{
    		setSailrenSpawnTask(22199);
    	}
    	else if (npcId == 22199)
    	{
    		setSailrenSpawnTask(22217);
    	}
    	else if (npcId == 22217)
    	{
    		setSailrenSpawnTask(29065);
    	}
    	else if (npcId == 29065)
    	{
    		setCubeSpawn();
    	}
        return super.onKill(npc,killer,isPet);
    }

    public static void main(String[] args)
    {
    	// now call the constructor (starts up the ai)
    	new Sailren(-1,"sailren","ai");
    }
}
