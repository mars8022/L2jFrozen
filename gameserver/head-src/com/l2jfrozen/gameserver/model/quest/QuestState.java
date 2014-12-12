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
package com.l2jfrozen.gameserver.model.quest;

import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowQuestMark;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialCloseHtml;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialEnableClientEvent;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Luis Arias
 */
public final class QuestState
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class);
	
	/** Quest associated to the QuestState */
	private final String _questName;
	
	/** Player who engaged the quest */
	private final L2PcInstance _player;
	
	/** State of the quest */
	private State _state;
	
	/** Boolean representing the completion of the quest */
	private boolean _isCompleted;
	
	/** List of couples (variable for quest,value of the variable for quest) */
	private Map<String, String> _vars;
	
	/** Boolean flag letting QuestStateManager know to exit quest when cleaning up */
	private boolean _isExitQuestOnCleanUp = false;
	
	/**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/>
	 * <BR/>
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI> <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI> <LI>Add drops gotten by the quest</LI> <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : L2PcInstance pointing out the player
	 * @param state : state of the quest
	 * @param completed : boolean for completion of the quest
	 */
	QuestState(final Quest quest, final L2PcInstance player, final State state, final boolean completed)
	{
		_questName = quest.getName();
		_player = player;
		
		_isCompleted = completed;
		// set the state of the quest
		_state = state;
	}
	
	public String getQuestName()
	{
		return _questName;
	}
	
	/**
	 * Return the quest
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	/**
	 * Return the L2PcInstance
	 * @return L2PcInstance
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	/**
	 * Return the state of the quest
	 * @return State
	 */
	public State getState()
	{
		return _state;
	}
	
	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted()
	{
		return _isCompleted;
	}
	
	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted()
	{
		if (getStateId().equals("Start") || getStateId().equals("Completed"))
			return false;
		
		return true;
	}
	
	/**
	 * Return state of the quest after its initialization.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U> <LI>Remove drops from previous state</LI> <LI>Set new state of the quest</LI> <LI>Add drop for new state</LI> <LI>Update information in database</LI> <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(final State state)
	{
		// set new state
		_state = state;
		if (state == null)
			return null;
		if (getStateId().equals("Completed"))
			_isCompleted = true;
		else
			_isCompleted = false;
		Quest.updateQuestInDb(this);
		final QuestList ql = new QuestList();
		getPlayer().sendPacket(ql);
		return state;
	}
	
	/**
	 * Return ID of the state of the quest
	 * @return String
	 */
	public String getStateId()
	{
		if (getState() != null)
			return getState().getName();
		return "Created";
	}
	
	/**
	 * Add parameter used in quests.
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	String setInternal(final String var, String val)
	{
		if (_vars == null)
		{
			_vars = new FastMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		_vars.put(var, val);
		
		return val;
	}
	
	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI> <LI>Initialize parameter "val" if is null</LI> <LI>Add/Update couple (var,val) in class variable FastMap "vars"</LI> <LI>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database. The key is
	 * known as existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 * @param var : String indicating the name of the variable for quest
	 * @param val : String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(final String var, String val)
	{
		if (_vars == null)
		{
			_vars = new FastMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		// FastMap.put() returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, val);
		
		int previousVal = 0;
		
		if (old != null && !old.equals(""))
		{
			try
			{
				previousVal = Integer.parseInt(old);
			}
			catch (final NumberFormatException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				// LOGGER.info(getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e);
				
			}
			
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			previousVal = 0;
			Quest.createQuestVarInDb(this, var, val);
			
		}
		
		if (var.equalsIgnoreCase("cond"))
		{
			if (!val.equals(""))
			{
				try
				{
					final int value = Integer.parseInt(val);
					setCond(value, previousVal);
				}
				catch (final NumberFormatException e)
				{
					LOGGER.info(getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent... ");
					e.printStackTrace();
				}
				
			}
			else
			{
				
				LOGGER.info(getPlayer().getName() + ", " + getQuestName() + " cond [null] is not an integer.  Value stored, but no packet was sent... ");
				
			}
			
		}
		
		old = null;
		
		return val;
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI> <LI>If not, check if quest progress steps have been skipped</LI> <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI> <LI>If no steps were skipped,
	 * flags do not need to be prepared...</LI> <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step For more info on the variable communicating the progress steps to the client, please see
	 */
	private void setCond(final int cond, final int old)
	{
		int completedStateFlags = 0; // initializing...
		
		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
			return;
		
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if (cond < 3 || cond > 31)
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything. If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...
			
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > old + 1)
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= (1 << old) - 1;
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= 1 << cond - 1;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if (cond < old)
			{
				completedStateFlags &= (1 << cond) - 1; // note, this also unsets the flag indicating that there exist skips
				
				// now, check if this resulted in no steps being skipped any more
				if (completedStateFlags == (1 << cond) - 1)
				{
					unset("__compltdStateFlags");
				}
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= 1 << cond - 1;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		QuestList ql = new QuestList();
		getPlayer().sendPacket(ql);
		ql = null;
		
		final int questId = getQuest().getQuestIntId();
		
		if (questId > 0 && questId < 999 && cond > 0)
		{
			getPlayer().sendPacket(new ExShowQuestMark(questId));
		}
	}
	
	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U> Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(final String var)
	{
		if (_vars == null)
			return null;
		
		final String old = _vars.remove(var);
		
		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		
		return old;
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public Object get(final String var)
	{
		if (_vars == null)
			return null;
		
		return _vars.get(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(final String var)
	{
		int varint = 0;
		
		String var_value = "";
		if (_vars != null && (var_value = _vars.get(var)) != null)
		{
			try
			{
				
				varint = Integer.parseInt(var_value);
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				LOGGER.info(getPlayer().getName() + ": variable " + var + " isn't an integer: returned value will be " + varint + e);
				
				if (Config.AUTODELETE_INVALID_QUEST_DATA)
				{
					exitQuest(true);
				}
			}
		}
		
		return varint;
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param character : L2Character of the character to get notification of death
	 */
	public void addNotifyOfDeath(final L2Character character)
	{
		if (character == null)
			return;
		
		character.addNotifyQuestOfDeath(this);
	}
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return int
	 */
	public int getQuestItemsCount(final int itemId)
	{
		int count = 0;
		
		if (getPlayer() != null && getPlayer().getInventory() != null && getPlayer().getInventory().getItems() != null)
		{
			
			for (final L2ItemInstance item : getPlayer().getInventory().getItems())
				if (item != null && item.getItemId() == itemId)
				{
					count += item.getCount();
				}
			
		}
		
		return count;
	}
	
	/**
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId : ID of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(final int itemId)
	{
		final L2ItemInstance enchanteditem = getPlayer().getInventory().getItemByItemId(itemId);
		
		if (enchanteditem == null)
			return 0;
		
		return enchanteditem.getEnchantLevel();
	}
	
	/**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public synchronized void giveItems(final int itemId, final int count)
	{
		giveItems(itemId, count, 0);
	}
	
	public synchronized void giveItems(final int itemId, int count, final int enchantlevel)
	{
		if (count <= 0)
			return;
		
		final int questId = getQuest().getQuestIntId();
		
		// If item for reward is gold (ID=57), modify count with rate for quest reward
		if (itemId == 57 && !(questId >= 217 && questId <= 233) && !(questId >= 401 && questId <= 418))
		{
			count = (int) (count * Config.RATE_QUESTS_REWARD);
		}
		
		// Set quantity of item
		// Add items to player's inventory
		final L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());
		
		if (item == null)
			return;
		
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		// If item for reward is gold, send message of gold reward to client
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addNumber(count);
			getPlayer().sendPacket(smsg);
			smsg = null;
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item.getItemId());
				smsg.addNumber(count);
				getPlayer().sendPacket(smsg);
				smsg = null;
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item.getItemId());
				getPlayer().sendPacket(smsg);
				smsg = null;
			}
		}
		getPlayer().sendPacket(new ItemList(getPlayer(), false));
		
		StatusUpdate su = new StatusUpdate(getPlayer().getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
		
		// on quests, always refresh inventory
		// InventoryUpdate u = new InventoryUpdate();
		// u.addItem(item);
		// getPlayer().sendPacket(u);
		
		su = null;
	}
	
	/**
	 * Drop Quest item using Config.RATE_DROP_QUEST
	 * @param itemId int Item Identifier of the item to be dropped
	 * @param count (minCount, maxCount) : int Quantity of items to be dropped
	 * @param neededCount Quantity of items needed for quest
	 * @param dropChance int Base chance of drop, same as in droplist
	 * @param sound boolean indicating whether to play sound
	 * @return boolean indicating whether player has requested number of items
	 */
	public boolean dropQuestItems(final int itemId, final int count, final int neededCount, final int dropChance, final boolean sound)
	{
		return dropQuestItems(itemId, count, count, neededCount, dropChance, sound);
	}
	
	public boolean dropQuestItems(final int itemId, final int minCount, final int maxCount, final int neededCount, int dropChance, final boolean sound)
	{
		dropChance *= Config.RATE_DROP_QUEST / (getPlayer().getParty() != null ? getPlayer().getParty().getMemberCount() : 1);
		
		final int currentCount = getQuestItemsCount(itemId);
		
		if (neededCount > 0 && currentCount >= neededCount)
			return true;
		
		if (currentCount >= neededCount)
			return true;
		
		int itemCount = 0;
		final int random = Rnd.get(L2DropData.MAX_CHANCE);
		
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			// if over neededCount, just fill the gap
			if (neededCount > 0 && currentCount + itemCount > neededCount)
			{
				itemCount = neededCount - currentCount;
			}
			
			// Inventory slot check
			if (!getPlayer().getInventory().validateCapacityByItemId(itemId))
				return false;
			
			// just wait 3-5 seconds before the drop
			try
			{
				Thread.sleep(Rnd.get(3, 5) * 1000);
			}
			catch (final InterruptedException e)
			{
			}
			
			// Give the item to Player
			getPlayer().addItem("Quest", itemId, itemCount, getPlayer().getTarget(), true);
			
			if (sound)
			{
				playSound(currentCount + itemCount < neededCount ? "Itemsound.quest_itemget" : "Itemsound.quest_middle");
			}
		}
		
		return neededCount > 0 && currentCount + itemCount >= neededCount;
	}
	
	// TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	public void addRadar(final int x, final int y, final int z)
	{
		getPlayer().getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(final int x, final int y, final int z)
	{
		getPlayer().getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		getPlayer().getRadar().removeAllMarkers();
	}
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U> <LI>Destroy quantity of items wanted</LI> <LI>Send new inventory list to player</LI>
	 * @param itemId : Identifier of the item
	 * @param count : Quantity of items to destroy
	 */
	public void takeItems(final int itemId, int count)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = getPlayer().getInventory().getItemByItemId(itemId);
		
		if (item == null)
			return;
		
		if (getPlayer().isProcessingTransaction())
		{
			getPlayer().cancelActiveTrade();
		}
		
		// Tests on count value in order not to have negative value
		if (count < 0 || count > item.getCount())
		{
			count = item.getCount();
		}
		
		// Destroy the quantity of items wanted
		if (itemId == 57)
		{
			getPlayer().reduceAdena("Quest", count, getPlayer(), true);
		}
		else
		{
			// Fix for destroyed quest items
			if (item.isEquipped())
				getPlayer().getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			
			getPlayer().destroyItemByItemId("Quest", itemId, count, getPlayer(), true);
		}
		
		// on quests, always refresh inventory
		final InventoryUpdate u = new InventoryUpdate();
		u.addItem(item);
		getPlayer().sendPacket(u);
		
		item = null;
	}
	
	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(final String sound)
	{
		getPlayer().sendPacket(new PlaySound(sound));
	}
	
	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(final int exp, final int sp)
	{
		getPlayer().addExpAndSp((int) getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), (int) getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
	}
	
	/**
	 * Return random value
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(final int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * @param loc
	 * @return number of ticks from GameTimeController.
	 */
	public int getItemEquipped(final int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}
	
	/**
	 * Return the number of ticks from the GameTimeController
	 * @return int
	 */
	public int getGameTicks()
	{
		return GameTimeController.getGameTicks();
	}
	
	/**
	 * Return true if quest is to exited on clean up by QuestStateManager
	 * @return boolean
	 */
	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}
	
	/**
	 * @param isExitQuestOnCleanUp
	 */
	public void setIsExitQuestOnCleanUp(final boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}
	
	/**
	 * Start a timer for quest.<BR>
	 * <BR>
	 * @param name The name of the timer. Will also be the value for event of onEvent
	 * @param time The millisecond value the timer will elapse
	 */
	public void startQuestTimer(final String name, final long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer());
	}
	
	public void startQuestTimer(final String name, final long time, final L2NpcInstance npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer());
	}
	
	/**
	 * Return the QuestTimer object with the specified name
	 * @param name
	 * @return QuestTimer<BR>
	 *         Return null if name does not exist
	 */
	public final QuestTimer getQuestTimer(final String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param npcId
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, 0);
	}
	
	public L2NpcInstance addSpawn(final int npcId, final int despawnDelay)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, despawnDelay);
	}
	
	public L2NpcInstance addSpawn(final int npcId, final int x, final int y, final int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Uses player's coords and heading. Adds a little randomization in the x y coords Return object id of newly spawned npc
	 * @param npcId
	 * @param cha
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, final L2Character cha)
	{
		return addSpawn(npcId, cha, true, 0);
	}
	
	public L2NpcInstance addSpawn(final int npcId, final L2Character cha, final int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Return object id of newly spawned npc
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param despawnDelay
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Inherits coords and heading from specified L2Character instance. It could be either the player, or any killed/attacked mob Return object id of newly spawned npc
	 * @param npcId
	 * @param cha
	 * @param randomOffset
	 * @param despawnDelay
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, final L2Character cha, final boolean randomOffset, final int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset
	 * @param despawnDelay
	 * @return
	 */
	public L2NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final boolean randomOffset, final int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}
	
	public String showHtmlFile(final String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitQuest(final boolean repeatable)
	{
		if (isCompleted())
			return this;
		
		// Say quest is completed
		_isCompleted = true;
		
		// Clean registered quest items
		FastList<Integer> itemIdList = getQuest().getRegisteredItemIds();
		if (itemIdList != null)
		{
			for (FastList.Node<Integer> n = itemIdList.head(), end = itemIdList.tail(); (n = n.getNext()) != end;)
			{
				takeItems(n.getValue().intValue(), -1);
			}
		}
		
		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable)
		{
			getPlayer().delQuestState(getQuestName());
			Quest.deleteQuestInDb(this);
			
			_vars = null;
		}
		else
		{
			checkNewbieQuests();
			// Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			if (_vars != null)
			{
				for (final String var : _vars.keySet())
				{
					unset(var);
				}
			}
			
			Quest.updateQuestInDb(this);
		}
		
		itemIdList = null;
		
		return this;
	}
	
	public void showQuestionMark(final int number)
	{
		getPlayer().sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(final String voice)
	{
		getPlayer().sendPacket(new PlaySound(2, voice, 0, 0, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ()));
	}
	
	public void showTutorialHTML(final String html)
	{
		String text = HtmCache.getInstance().getHtm("data/scripts/quests/255_Tutorial/" + html);
		
		if (text == null)
		{
			LOGGER.warn("missing html page data/scripts/quests/255_Tutorial/" + html);
			text = "<html><body>File data/scripts/quests/255_Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		
		getPlayer().sendPacket(new TutorialShowHtml(text));
		text = null;
	}
	
	public void closeTutorialHtml()
	{
		getPlayer().sendPacket(new TutorialCloseHtml());
	}
	
	public void onTutorialClientEvent(final int number)
	{
		getPlayer().sendPacket(new TutorialEnableClientEvent(number));
	}
	
	public void dropItem(final L2MonsterInstance npc, final L2PcInstance player, final int itemId, final int count)
	{
		npc.DropItem(player, itemId, count);
	}
	
	public L2NpcInstance getNpc()
	{
		
		if (getPlayer().getTarget() instanceof L2NpcInstance)
		{
			
			return (L2NpcInstance) getPlayer().getTarget();
		}
		return null;
	}
	
	public void checkNewbieQuests()
	{
		final int questId = getQuest().getQuestIntId();
		
		if (questId == 1 || questId == 2 || questId == 4 || questId == 5 || questId == 166 || questId == 174)
		{
			if (_player != null)
			{
				final QuestState st = _player.getQuestState("7003_NewbieHelper");
				if (st != null && st.getInt("cond") <= 1)
				{
					_player.sendPacket(new ExShowScreenMessage("Quest completed. Find the Newbie Helper.", 4000));
					st.set("cond", "2");
				}
			}
		}
		if (questId == 257 || questId == 293 || questId == 260 || questId == 265 || questId == 273 || questId == 281)
		{
			if (_player != null)
			{
				final QuestState st = _player.getQuestState("7003_NewbieHelper");
				if (st != null && st.getInt("cond") == 4)
				{
					if (_player.getClassId().isMage())
						_player.sendPacket(new ExShowScreenMessage("You earned a spiritshots for beginners. Find the Newbie Helper.", 4000));
					else
						_player.sendPacket(new ExShowScreenMessage("You earned a soulshots for beginners. Find the Newbie Helper.", 4000));
					st.set("cond", "5");
				}
			}
		}
		if (questId == 104 || questId == 101 || questId == 105 || questId == 107 || questId == 175 || questId == 106 || questId == 103 || questId == 108)
		{
			if (_player != null)
			{
				final QuestState st = _player.getQuestState("7003_NewbieHelper");
				if (st != null && st.getInt("cond") == 6)
				{
					_player.sendPacket(new ExShowScreenMessage("You earned a new weapon. Go to the Newbie Helper.", 4000));
					st.set("cond", "7");
				}
			}
		}
		if (questId == 151 || questId == 296 || questId == 169 || questId == 261 || questId == 276 || questId == 283)
		{
			if (_player != null)
			{
				final QuestState st = _player.getQuestState("7003_NewbieHelper");
				if (st != null && st.getInt("cond") == 8)
				{
					_player.sendPacket(new ExShowScreenMessage("Last stage completed. Go to the Newbie Helper.", 4000));
					st.set("cond", "9");
				}
			}
		}
	}
}
