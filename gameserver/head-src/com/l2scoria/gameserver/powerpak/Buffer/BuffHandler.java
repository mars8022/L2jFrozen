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
package com.l2scoria.gameserver.powerpak.Buffer;

import java.util.ArrayList;
import java.util.Map;

import javolution.util.FastMap;

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.handler.IBBSHandler;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import com.l2scoria.gameserver.powerpak.Buffer.BuffTable.Buff;
import com.l2scoria.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * 
 * 
 * @author Nick
 */
public class BuffHandler implements IVoicedCommandHandler, ICustomByPassHandler, IBBSHandler
{

	private Map<Integer,ArrayList<Buff>> _buffs;
	private Map<Integer, String> _visitedPages;
	private ArrayList<Buff> getOwnBuffs(int objectId)
	{
		
		if(_buffs.get(objectId)==null)
			synchronized(_buffs)
			{
				_buffs.put(objectId,new ArrayList<Buff>());
			}
		return _buffs.get(objectId);
	}

	public BuffHandler()
	{
		_buffs = new FastMap<Integer,ArrayList<Buff>>();
		_visitedPages = new FastMap<Integer,String>();
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] {PowerPakConfig.BUFFER_COMMAND};
	}

	private boolean checkAllowed(L2PcInstance activeChar)
	{
		String msg = null;
		if(activeChar.isSitting())
			msg = "Can't use buffer when sitting";
		else if(activeChar.isCastingNow())
			msg = "Can't use buffer when casting";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("ALL"))
			msg = "Buffer is not available in this area";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = "Can't use Buffer with Cursed Weapon"; 
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = "Buffer is not available during the battle";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = "Buffer is not available in the catacombs and necropolis";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
			msg = "Buffer is not available in this area";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = "Buffer is not available in this area";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = "Buffer is not available in this area";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = "Buffer is not available in this area";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("OLYMPIAD") && (activeChar.isInOlympiadMode() ||
				activeChar.isInsideZone(L2Character.ZONE_OLY) || Olympiad.getInstance().isRegistered(activeChar) ||
				Olympiad.getInstance().isRegisteredInComp(activeChar))) 
			msg = "Buffer is not available in Olympiad";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("EVENT") && 
				(activeChar._inEvent))
			msg = "Buffer is not available in this event";
		
		if(msg!=null)
			activeChar.sendMessage(msg);

		return msg==null;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar,
			String target)
	{
		if(activeChar == null)
			return  false;

		if(!checkAllowed(activeChar))
			return false;

		if(command.compareTo(PowerPakConfig.BUFFER_COMMAND)==0)
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
			String text = HtmCache.getInstance().getHtm("data/html/default/50019.htm");
			htm.setHtml(text);
			activeChar.sendPacket(htm);
		}
		return false;
	}

	private static final String [] _BYPASSCMD = {"dobuff"};
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player,
			String parameters)
	{
		if(player==null)
			return;

		if(!checkAllowed(player))
			return;

		L2NpcInstance buffer = null;
		if(player.getTarget()!=null)
			if(player.getTarget() instanceof L2NpcInstance)
			{
				buffer = (L2NpcInstance)player.getTarget();
				if(buffer.getTemplate().getNpcId()!=50018)
					buffer=null;
			}

		if(parameters.contains("Pet")){
			if(player.getPet()==null){
				return;
			}
		}
		
		if(parameters.compareTo("ClearBuffs")==0)
		{
			getOwnBuffs(player.getObjectId()).clear();
			player.sendMessage("Buff set cleared");
		}
		else if(parameters.compareTo("ClearPetBuffs")==0)
		{
			getOwnBuffs(player.getPet().getObjectId()).clear();
			player.sendMessage("Pet Buff set cleared");
		}
		else if(parameters.compareTo("RemoveAll")==0)
		{
			for(L2Effect e : player.getAllEffects())
			{
				if(e.getEffectType()==L2Effect.EffectType.BUFF)
					player.removeEffect(e);
			}
		}
		else if(parameters.compareTo("RemovePetAll")==0)
		{
			for(L2Effect e : player.getPet().getAllEffects())
			{
				if(e.getEffectType()==L2Effect.EffectType.BUFF)
					player.getPet().removeEffect(e);
			}
		}
		else if(parameters.startsWith("Chat"))
		{
			String chatIndex = parameters.substring(4).trim();
			synchronized(_visitedPages)
			{
				_visitedPages.put(player.getObjectId(), chatIndex);
			}
			/*
			if(chatIndex.length()>0)
				if(chatIndex.compareTo("0")==0)
					chatIndex = "";
				else
			*/		
			chatIndex = "-" + chatIndex;
			String text;
			//if(chatIndex.length()>0)
				text = HtmCache.getInstance().getHtm("data/html/buffer/buffer" + chatIndex + ".htm");
			/*else 
				text = HtmCache.getInstance().getHtm("data/html/default/50019.htm");
			*/
			if(command.startsWith("bbsbuff"))
			{
				text = text.replace("-h custom_do", "bbs_bbs");
				BaseBBSManager.separateAndSend(text, player);
			}
			else
			{
				NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
				htm.setHtml(text);
				player.sendPacket(htm);
			}
		}
		else if(parameters.startsWith("RestoreAll")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE*3) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getStatus().setCurrentCp(player.getMaxCp());
			player.getStatus().setCurrentMp(player.getMaxMp());
			player.getStatus().setCurrentHp(player.getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE*3, null, true);
		}
		else if(parameters.startsWith("RestorePetAll")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE*3) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getPet().getStatus().setCurrentMp(player.getPet().getMaxMp());
			player.getPet().getStatus().setCurrentHp(player.getPet().getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE*3, null, true);
		}
		else if(parameters.startsWith("RestoreCP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getStatus().setCurrentCp(player.getMaxCp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestoreMP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getStatus().setCurrentMp(player.getMaxMp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestorePetMP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getPet().getStatus().setCurrentMp(player.getPet().getMaxMp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestoreHP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getStatus().setCurrentHp(player.getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestorePetHP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("You don't have enough adena");
				return;
			}
			player.getPet().getStatus().setCurrentHp(player.getPet().getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("MakeBuffs") || parameters.startsWith("RestoreBuffs"))
		{
			String buffName = parameters.substring(9).trim();
			int totaladena = 0;
			ArrayList<Buff> buffs = null;
			if(parameters.startsWith("RestoreBuffs"))
				buffs = getOwnBuffs(player.getObjectId());
			else	
				buffs = BuffTable.getInstance().getBuffsForName(buffName);
			if(buffs!=null && buffs.size()==1)
			{
				if(!getOwnBuffs(player.getObjectId()).contains(buffs.get(0)))
					getOwnBuffs(player.getObjectId()).add(buffs.get(0));
			}
			if(buffs==null || buffs.size()==0){
				player.sendMessage("Your buff set is missing");
				return;
			}
			for(Buff buff: buffs)
			{
				
				L2Skill skill = SkillTable.getInstance().getInfo(buff._skillId, buff._skillLevel);
				if(skill!=null)
				{
					if(player.getLevel()>= buff._minLevel && player.getLevel()<=buff._maxLevel)
					{
						if(buff._price>0)
						{
							totaladena+=buff._price;
							if(player.getAdena()<totaladena)
							{
								player.sendMessage("You don't have enough adena");
								break;
							} 
						}
						if(!buff._force && buffer!=null)
						{
							buffer.setBusy(true);
							buffer.setCurrentMp(buffer.getMaxMp());
							buffer.setTarget(player);
							buffer.doCast(skill);
							buffer.setBusy(false);
						} else
							skill.getEffects(player, player);
					}
					try
					{
						Thread.sleep(100); // Задержко что бы пакетами не зафлудить..
					}
					catch(InterruptedException e)
					{
						//null
					}
				}
			}
			if(totaladena>0)
				player.reduceAdena("Buff", totaladena, null, true);
			if(_visitedPages.get(player.getObjectId())!=null)
				handleCommand(command,player,"Chat "+_visitedPages.get(player.getObjectId()));
			else 
				useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
		}else if(parameters.startsWith("MakePetBuffs") || parameters.startsWith("RestorePetBuffs"))
		{
			if(player.getPet()==null){
				player.sendMessage("You have not a summoned pet");
				return;
			}
			
			String buffName = parameters.substring(12).trim();
			
			int totaladena = 0;
			ArrayList<Buff> buffs = null;
			if(parameters.startsWith("RestorePetBuffs"))
				buffs = getOwnBuffs(player.getPet().getObjectId());
			else	
				buffs = BuffTable.getInstance().getBuffsForName(buffName);
			
			if(buffs!=null && buffs.size()==1)
			{
				
				if(!getOwnBuffs(player.getPet().getObjectId()).contains(buffs.get(0))){
					getOwnBuffs(player.getPet().getObjectId()).add(buffs.get(0));
				}
			}
			if(buffs==null || buffs.size()==0){
				player.sendMessage("Your pet buff set is missing");
				return;
			}
			for(Buff buff: buffs)
			{
				
				L2Skill skill = SkillTable.getInstance().getInfo(buff._skillId, buff._skillLevel);
				if(skill!=null)
				{
					if(player.getLevel()>= buff._minLevel && player.getLevel()<=buff._maxLevel)
					{
						if(buff._price>0)
						{
							totaladena+=buff._price;
							if(player.getAdena()<totaladena)
							{
								player.sendMessage("You don't have enough adena");
								break;
							} 
						}
						if(!buff._force && buffer!=null)
						{
							buffer.setBusy(true);
							buffer.setCurrentMp(buffer.getMaxMp());
							buffer.setTarget(player.getPet());
							buffer.doCast(skill);
							buffer.setBusy(false);
						} else
							skill.getEffects(player.getPet(), player.getPet());
					}
					try
					{
						Thread.sleep(100); // Задержко что бы пакетами не зафлудить..
					}
					catch(InterruptedException e)
					{
						//null
					}
				}
			}
			if(totaladena>0)
				player.reduceAdena("Buff", totaladena, null, true);
			if(_visitedPages.get(player.getObjectId())!=null)
				handleCommand(command,player,"Chat "+_visitedPages.get(player.getObjectId()));
			else 
				useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
		}
	}

	private static String [] _BBSCommand = {"bbsbuff"};
	@Override
	public String[] getBBSCommands()
	{
		return _BBSCommand;
	}

}
