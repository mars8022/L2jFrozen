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
package interlude.gameserver.instancemanager.clanhallsiege;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javolution.util.FastList;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.instancemanager.EventsDropManager;
import interlude.gameserver.instancemanager.EventsDropManager.ruleType;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Party;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2WorldRegion;
import interlude.gameserver.model.actor.instance.L2ChestInstance;
import interlude.gameserver.model.actor.instance.L2HotSpringSquashInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.model.entity.ClanHallSiege;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.clientpackets.Say2;
import interlude.gameserver.network.serverpackets.CreatureSay;
import interlude.gameserver.network.serverpackets.ItemList;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.taskmanager.ExclusiveTask;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Author: L2EmuRT, All credits L2EmuRT
 */
public class RainbowSpringSiegeManager extends ClanHallSiege
{
	protected static Log							_log						= LogFactory.getLog(RainbowSpringSiegeManager.class.getName());
	private boolean									_registrationPeriod			= false;
	public ClanHall 								clanhall 					= ClanHallManager.getInstance().getClanHallById(62);
	private Map<Integer, clanPlayersInfo>			_clansInfo					= new HashMap<Integer, clanPlayersInfo>();
	private L2NpcInstance[]							eti 						= new L2NpcInstance[] {null, null, null, null };
	private L2HotSpringSquashInstance[]				squash 						= new L2HotSpringSquashInstance[] { null, null, null, null};
	
	private FastList<L2ChestInstance> 				arena1chests 				= new FastList<L2ChestInstance>();
	private FastList<L2ChestInstance> 				arena2chests 				= new FastList<L2ChestInstance>();
	private FastList<L2ChestInstance> 				arena3chests 				= new FastList<L2ChestInstance>();
	private FastList<L2ChestInstance> 				arena4chests 				= new FastList<L2ChestInstance>();

	private int[] 									arenaChestsCnt 				= {0, 0, 0, 0};
	private FastList<Integer> 						_playersOnArena 			= new FastList<Integer>();
	private int 									currArena;
	private L2NpcInstance 							teleporter;
	private static RainbowSpringSiegeManager		_instance;

	public static final RainbowSpringSiegeManager getInstance()
	{
		if (_instance == null)
			_instance = new RainbowSpringSiegeManager();
		return _instance;
	}	
	
	private RainbowSpringSiegeManager()
	{
		_log.info("ClanHallSiege: Rainbow Springs Chateau");
		long siegeDate = restoreSiegeDate(62);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate,62,22);
		// Schedule siege auto start
		_startSiegeTask.schedule(1000);
	}
	public void startSiege()
	{
		if(_startSiegeTask.isScheduled())
			_startSiegeTask.cancel();
		if(_clansInfo.size() == 0)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() > 4)
		{
			for (int x = 1;x < _clansInfo.size()-4; x++)
			{
				clanPlayersInfo minClan=null;
				int minVal = Integer.MAX_VALUE;
				for (clanPlayersInfo cl: _clansInfo.values())
				{
					if(cl._decreeCnt < minVal)
					{
						minVal = cl._decreeCnt;
						minClan = cl;
					}
				}
				_clansInfo.remove(minClan);
			}
		}
		else if (_clansInfo.size() < 2)
		{
			shutdown();
			anonce("Attention! Clan Hall, Palace Rainbow Sources did not receive new Owner");			
			endSiege(false);
			return;
		}
		for (L2Spawn sp : SpawnTable.getInstance().getAllTemplates().values())
		{
			if (sp.getTemplate().getNpcId() == 35603)
				teleporter = sp.getLastSpawn();
		}
		currArena = 0;
		setIsInProgress(true);		
		anonce("Attention! competition for the clan hall, the Palace Rainbow Sources will start 5 minutes.");
		anonce("Attention! representatives of the clan must enter the arena.");
		for (clanPlayersInfo cl: _clansInfo.values())
		{
			L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
			L2PcInstance clanLeader = clan.getLeader().getPlayerInstance();
			if (clanLeader != null)
				clanLeader.sendMessage("Your clan takes part in the competition. go to the arena.");
		}
		_firstStepSiegeTask.schedule(60000*5);
		
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 65);
		_endSiegeTask.schedule(1000);
	}
	
	public void startFirstStep()
	{
		L2NpcTemplate template;
		template = NpcTable.getInstance().getTemplate(35596);
		for (int x = 0;x <= 3; x++)
		{
			eti[x] = new L2NpcInstance(IdFactory.getInstance().getNextId(),template);
			eti[x].getStatus().setCurrentHpMp(eti[x].getMaxHp(), eti[x].getMaxMp());			
		}
		eti[0].spawnMe(153129, -125337, -2221);
		eti[1].spawnMe(153884, -127534, -2221);
		eti[2].spawnMe(151560, -127075, -2221);
		eti[3].spawnMe(156657, -125753, -2221);
		template = NpcTable.getInstance().getTemplate(35588);
		for (int x = 0;x <= 3; x++)
		{
			squash[x] = new L2HotSpringSquashInstance(IdFactory.getInstance().getNextId(),template);
			squash[x].getStatus().setCurrentHpMp(squash[x].getMaxHp(), squash[x].getMaxMp());			
		}
		squash[0].spawnMe(153129+50, -125337+50, -2221);
		squash[1].spawnMe(153884+50, -127534+50, -2221);
		squash[2].spawnMe(151560+50, -127075+50, -2221);
		squash[3].spawnMe(156657+50, -125753+50, -2221);
		
		int mobs[] = {35593};
		int item[] = {8035, 8037, 8039, 8040, 8046, 8047, 8050, 8051, 8052, 8053, 8054};
		int cnt[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		int chance[] = { 400, 400, 400, 400, 400, 400, 400, 400, 400, 400, 400};
		EventsDropManager.getInstance().addRule("RainbowSpring", ruleType.BY_NPCID, mobs, item, cnt, chance, false);		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ChestsSpawn(), 5000, 5000);
	}
	
	public void chestDie(L2Character killer, L2ChestInstance chest)
	{
		if (arena1chests.contains(chest))
		{
			arenaChestsCnt[0]--;
			arena1chests.remove(chest);
		}
		if (arena2chests.contains(chest))
		{
			arenaChestsCnt[1]--;
			arena2chests.remove(chest);
		}
		if (arena3chests.contains(chest))
		{
			arenaChestsCnt[2]--;
			arena3chests.remove(chest);
		}
		if (arena4chests.contains(chest))
		{
			arenaChestsCnt[3]--;
			arena4chests.remove(chest);
		}
	}

	public void exchangeItem(L2PcInstance player,int val)
	{
		if (val == 1) //WATERS
		{
			if (player.destroyItemByItemId("Quest", 8054, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8035, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8052, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8039, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8050, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8051, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8032, 1, player, player.getTarget());
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				player.sendPacket(smsg);
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage("Is not enough quest items");
				return;
			}
		}
		if (val == 2) //WATERS
		{
			if (player.destroyItemByItemId("Quest", 8054, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8035, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8052, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8039, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8050, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8051, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8031, 1, player, player.getTarget());
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				player.sendPacket(smsg);
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage("Is not enough quest items");
				return;
			}
		}

		if (val == 3) //NECTAR
		{
			if (player.destroyItemByItemId("Quest", 8047, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8039, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8037, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8052, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8035, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8050, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8030, 1, player, player.getTarget());
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				player.sendPacket(smsg);
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage("Is not enough quest items");
				return;
			}
		}
		if (val == 4) //SULFUR
		{
			if (player.destroyItemByItemId("Quest", 8051, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8053, 2, player, true) &&
					player.destroyItemByItemId("Quest", 8046, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8040, 1, player, true) &&
					player.destroyItemByItemId("Quest", 8050, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8033, 1, player, player.getTarget());
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				player.sendPacket(smsg);
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage("Is not enough quest items");
				return;
			}
		}
	}	
	
	public boolean usePotion(L2PlayableInstance activeChar,int potionId)
	{
		if (activeChar instanceof L2PcInstance && isPlayerInArena((L2PcInstance)activeChar) && activeChar.getTarget() instanceof L2NpcInstance
				&& ((L2NpcInstance)activeChar.getTarget()).getTemplate().getNpcId() == 35596)
			return true;

		return false;
	}
	
	private final class ChestsSpawn implements Runnable
	{
		public void run()
		{
			if (arenaChestsCnt[0] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(153129 + Rnd.get(-400, 400), -125337 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();				
				arena1chests.add(newChest);
				arenaChestsCnt[0]++;
			}
			if (arenaChestsCnt[1] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(153884 + Rnd.get(-400, 400), -127534 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();				
				arena2chests.add(newChest);
				arenaChestsCnt[1]++;
			}
			if (arenaChestsCnt[2] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(151560 + Rnd.get(-400, 400), -127075 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();				
				arena3chests.add(newChest);
				arenaChestsCnt[2]++;
			}
			if (arenaChestsCnt[3] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(155657 + Rnd.get(-400, 400), -125753 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();				
				arena4chests.add(newChest);
				arenaChestsCnt[3]++;
			}
			
		}		
	}	
	
	public void endSiege(boolean par)
	{
		setIsInProgress(false);
		_clansInfo.clear();
		for (int id : _playersOnArena)
		{
			L2PcInstance pl = L2World.getInstance().findPlayer(id);
			if (pl != null)
				pl.teleToLocation(150717, -124818, -2355);
		}
		_playersOnArena = new FastList<Integer>();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(),62,22);
		_startSiegeTask.schedule(1000);
	}	
	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}
	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}
	public boolean isClanOnSiege(L2Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
			return true;

		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
			return false;

		return true;
	}
	public synchronized int registerClanOnSiege(L2PcInstance player,L2Clan playerClan)
	{
		L2ItemInstance item = player.getInventory().getItemByItemId(8034);
		int itemCnt = 0;
		if (item != null)
		{
			itemCnt = item.getCount();
			if (player.destroyItem("RegOnSiege", item.getObjectId(), itemCnt, player, true))
			{
				_log.info("Rainbow Springs Chateau: registered clan " + playerClan.getName() + " get: " + itemCnt + " decree.");
				clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
				if (regPlayers == null)
				{
					regPlayers = new clanPlayersInfo();
					regPlayers._clanName = playerClan.getName();
					regPlayers._decreeCnt = itemCnt;
					_clansInfo.put(playerClan.getClanId(), regPlayers);
				}
			}
		}
		else
			return 0;
		return itemCnt;
	}
	
	public boolean isPlayerInArena(L2PcInstance pl)
	{
		if (_playersOnArena.contains(pl.getObjectId()))
			return true;
		return false;
	}
	
	public void removeFromArena(L2PcInstance pl)
	{
		if (_playersOnArena.contains(pl.getObjectId()))
			pl.teleToLocation(150717, -124818, -2355);
	}
	
	public synchronized boolean enterOnArena(L2PcInstance pl)
	{
		L2Clan clan = pl.getClan();
		L2Party party = pl.getParty(); 
		if (clan == null || party == null)
			return false;

		if (!isClanOnSiege(clan) || !getIsInProgress()
				|| currArena > 3 || !pl.isClanLeader()
				|| party.getMemberCount() < 5)
			return false;
		
		clanPlayersInfo ci = _clansInfo.get(clan.getClanId());
		if (ci == null)
			return false;

		for (L2PcInstance pm : party.getPartyMembers())
		{
			if (pm == null || pm.getRangeToTarget(teleporter) > 500)
				return false;
		}
			
		ci._arenaNumber = currArena;
		currArena++;
		
		for (L2PcInstance pm : party.getPartyMembers())
		{
			if(pm.getPet() != null)
				pm.getPet().unSummon(pm);
			_playersOnArena.add(pm.getObjectId());
			
			switch (ci._arenaNumber)
			{
			case 0:
				pm.teleToLocation(153129 + Rnd.get(-400, 400), -125337 + Rnd.get(-400, 400), -2221);
				break;
			case 1:
				pm.teleToLocation(153884 + Rnd.get(-400, 400), -127534 + Rnd.get(-400, 400), -2221);				
				break;
			case 2:
				pm.teleToLocation(151560 + Rnd.get(-400, 400), -127075 + Rnd.get(-400, 400), -2221);				
				break;
			case 3:
				pm.teleToLocation(155657 + Rnd.get(-400, 400), -125753 + Rnd.get(-400, 400), -2221);				
				break;
			}
		}
		return true;
	}
	
	public synchronized boolean unRegisterClan(L2PcInstance player)
	{
		L2Clan playerClan = player.getClan();
		if(_clansInfo.containsKey(playerClan.getClanId()))
		{
			int decreeCnt = _clansInfo.get(playerClan.getClanId())._decreeCnt / 2;
			if (decreeCnt > 0)
			{
				L2ItemInstance item = player.getInventory().addItem("UnRegOnSiege", 8034, decreeCnt, player, player.getTarget());
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				player.sendPacket(smsg);
				player.sendPacket(new ItemList(player, false));
			}	
			return true;
		}
		return false;		
	}

	public void anonce(String text)
	{
			CreatureSay cs = new CreatureSay(0, Say2.SHOUT, "Messenger", text);
			L2WorldRegion region = L2World.getInstance().getRegion(143944, -119196);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (region == L2World.getInstance().getRegion(player.getX(), player.getY()) 
						&& player.getInstanceId() == 0)
					player.sendPacket(cs);
			}
	}

	public void shutdown()
	{
		if (isRegistrationPeriod())
		{
			for(clanPlayersInfo cl:_clansInfo.values())
			{
				L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
				if (clan != null && cl._decreeCnt>0)
				{
					L2PcInstance pl = L2World.getInstance().getPlayer(clan.getLeaderName());
					if (pl != null)
						pl.sendMessage("In the repository Klan returned evidenced by participation in the War of the clan hall hot spring");
					clan.getWarehouse().addItem("revert", 8034, cl._decreeCnt, null, null);
				}
			}
		}
		for (int id : _playersOnArena)
		{
			L2PcInstance pl = L2World.getInstance().findPlayer(id);
			if (pl != null)
				pl.teleToLocation(150717, -124818, -2355);
		}
	}

	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			Calendar siegeStart = Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining = registerTimeRemaining;
			if (registerTimeRemaining <= 0)
			{
				if (!isRegistrationPeriod())
				{
					setRegistrationPeriod(true);
					anonce("Attention! A period of registration at the siege of the clan hall, the Palace Rainbow sources.");
					anonce("Attention! Battle of the clan hall, the Palace Rainbow Sources begin an hour later.");
					remaining = siegeTimeRemaining;
				}
			}
			if (siegeTimeRemaining <= 0)
			{
				setRegistrationPeriod(false);
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);			
		}
	};
	private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};
	private final ExclusiveTask _firstStepSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			startFirstStep();
		}
	};
	private class clanPlayersInfo
	{
		public String _clanName;
		public int _decreeCnt;
		public int _arenaNumber;
	}	
}