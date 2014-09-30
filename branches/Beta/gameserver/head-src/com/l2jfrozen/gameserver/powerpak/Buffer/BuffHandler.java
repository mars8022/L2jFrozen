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
package com.l2jfrozen.gameserver.powerpak.Buffer;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2jfrozen.gameserver.datatables.BufferSkillsTable;
import com.l2jfrozen.gameserver.datatables.CharSchemesTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IBBSHandler;
import com.l2jfrozen.gameserver.handler.ICustomByPassHandler;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;
import com.l2jfrozen.gameserver.powerpak.Buffer.BuffTable.Buff;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author Nick
 */
public class BuffHandler implements IVoicedCommandHandler, ICustomByPassHandler, IBBSHandler
{
	private static final String PARENT_DIR = "data/html/buffer/";
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
		else if(activeChar.isCastingNow()  || activeChar.isCastingPotionNow())
			msg = "Can't use buffer when casting";
		else if(activeChar.isAlikeDead())
			msg = "Can't use buffer while dead";
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
				(activeChar.isInFunEvent()))
			msg = "Buffer is not available in this event";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("TVT") && 
				activeChar._inEventTvT && TvT.is_started() )
			msg = "Buffer is not available in TVT";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("CTF") && 
				activeChar._inEventCTF && CTF.is_started() )
			msg = "Buffer is not available in CTF";
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("DM") && 
				activeChar._inEventDM && DM.is_started() )
			msg = "Buffer is not available in DM";
		
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
			String text = HtmCache.getInstance().getHtm("data/html/default/"+PowerPakConfig.BUFFER_NPC+".htm");
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
	public void handleCommand(String command, final L2PcInstance player,
			String parameters)
	{
		if(player==null)
			return;

		if(!checkAllowed(player))
			return;

		L2NpcInstance buffer = null;
		
		if(!PowerPakConfig.BUFFER_USEBBS && !PowerPakConfig.BUFFER_USECOMMAND){
			
			if(player.getTarget()!=null)
				if(player.getTarget() instanceof L2NpcInstance)
				{
					buffer = (L2NpcInstance)player.getTarget();
					if(buffer.getTemplate().getNpcId()!=PowerPakConfig.BUFFER_NPC)
						buffer=null;
				}
			
			//Possible fix to Buffer - 1
			if (buffer == null)
				return;

			//Possible fix to Buffer - 2
			if (!player.isInsideRadius(buffer, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				return;
			
		}//if buffer is null means that buffer will be applied directly (voice and bbs)
		
		if(parameters.contains("Pet")){
			if(player.getPet()==null){
				return;
			}
		}
		
		StringTokenizer st = new StringTokenizer(parameters, " ");
		
		String currentCommand = st.nextToken();
		
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
			final L2Effect[] effects = player.getAllEffects();
			
			for(L2Effect e : effects)
			{
				if(e.getEffectType()==L2Effect.EffectType.BUFF)
					player.removeEffect(e);
			}
		}
		else if(parameters.compareTo("RemovePetAll")==0)
		{
			
			final L2Effect[] effects = player.getPet().getAllEffects();
			
			for(L2Effect e : effects)
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
			
			chatIndex = "-" + chatIndex;
			String text = HtmCache.getInstance().getHtm("data/html/buffer/buffer" + chatIndex + ".htm");
			
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
							//buffer.doCast(skill);
							skill.getEffects(buffer, player,false,false,false);
							buffer.setBusy(false);
						} else
							skill.getEffects(player, player,false,false,false);
					}
					try
					{
						Thread.sleep(100); // Delay for the packet...
					}
					catch(InterruptedException e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
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
							skill.getEffects(buffer, player.getPet(),false,false,false);
							//buffer.doCast(skill);
							buffer.setBusy(false);
						} else
							skill.getEffects(player, player.getPet(),false,false,false);
					}
					try
					{
						Thread.sleep(100); // Delay for the packet...
					}
					catch(InterruptedException e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
					}
				}
			}
			if(totaladena>0)
				player.reduceAdena("Buff", totaladena, null, true);
			if(_visitedPages.get(player.getObjectId())!=null)
				handleCommand(command,player,"Chat "+_visitedPages.get(player.getObjectId()));
			else 
				useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
			
			
		//SCHEMAS
		}else if (currentCommand.startsWith("menu"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(PARENT_DIR + "menu.htm");
			sendHtmlMessage(player, html);
		}
		// handles giving effects {support player, support pet, givebuffs}
		else if (currentCommand.startsWith("support"))
		{
			String targettype = st.nextToken();
			showGiveBuffsWindow(player, targettype);
		}
		else if (currentCommand.startsWith("givebuffs"))
		{
			String targettype = st.nextToken();
			String scheme_key = st.nextToken();
			int cost = Integer.parseInt(st.nextToken());
			if (cost == 0 || cost <= player.getInventory().getAdena())
			{
				L2Character target = player;
				if (targettype.equalsIgnoreCase("pet"))
					target = player.getPet();
				
				if (target != null)
				{
					for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key))
						if(buffer!=null)
						{
							buffer.setBusy(true);
							buffer.setCurrentMp(buffer.getMaxMp());
							buffer.setTarget(target);
							//buffer.doCast(skill);
							sk.getEffects(buffer, target,false,false,false);
							buffer.setBusy(false);
						} else
							sk.getEffects(target, target,false,false,false);
					
						//sk.getEffects(buffer, target);
					
					player.reduceAdena("NPC Buffer", cost, null, true);
				
				}else
				{
					player.sendMessage("Incorrect Target");
					// go to main menu
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(PARENT_DIR + "menu.htm");
					sendHtmlMessage(player, html);
				}
			}
			else
			{
				player.sendMessage("Not enough adena");
				showGiveBuffsWindow(player, targettype);
			}
		}
		// handles edit schemes {skillselect, skillunselect}
		else if (currentCommand.startsWith("editscheme"))
		{
			String skill_group = st.nextToken();
			String scheme_key = null;
			try
			{
				scheme_key = st.nextToken();
			}
			catch (Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			showEditSchemeWindow(player, skill_group, scheme_key);
		}
		else if (currentCommand.startsWith("skill"))
		{
			String skill_group = st.nextToken();
			String scheme_key = st.nextToken();
			int skill_id = Integer.parseInt(st.nextToken());
			int level = BufferSkillsTable.getInstance().getSkillLevelById(skill_id);
			if (currentCommand.startsWith("skillselect") && !scheme_key.equalsIgnoreCase("unselected"))
			{
				if(CharSchemesTable.getInstance() != null
						&& CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key)!=null
						&& CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).size() < PowerPakConfig.NPCBUFFER_MAX_SKILLS)
					CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).add(SkillTable.getInstance().getInfo(skill_id, level));
				else
					player.sendMessage("This scheme has reached maximun amount of buffs");
			}
			else if (currentCommand.startsWith("skillunselect") && CharSchemesTable.getInstance() != null
					&& CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key)!=null)
				CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).remove(SkillTable.getInstance().getInfo(skill_id, level));
			showEditSchemeWindow(player, skill_group, scheme_key);
		}
		// manage schemes {create, delete, clear}
		else if (currentCommand.startsWith("manageschemes"))
			showManageSchemeWindow(player);
		else if (currentCommand.startsWith("createscheme"))
		{
			if(!st.hasMoreTokens()){
				player.sendMessage("Error: Specify Schema Name!");
				showManageSchemeWindow(player);
				return;
			}
			
			String name = st.nextToken();
			
			if (name.length() > 14)
			{
				player.sendMessage("Error: Scheme's name must contain up to 14 chars without any spaces");
				showManageSchemeWindow(player);
			}
			else if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).size() == PowerPakConfig.NPCBUFFER_MAX_SCHEMES)
			{
				player.sendMessage("Error: Maximun schemes amount reached, please delete one before creating a new one");
				showManageSchemeWindow(player);
			}
			else if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				player.sendMessage("Error: duplicate entry. Please use another name");
				showManageSchemeWindow(player);
			}
			else
			{
				if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null)
					CharSchemesTable.getInstance().getSchemesTable().put(player.getObjectId(), new FastMap<String, FastList<L2Skill>>(PowerPakConfig.NPCBUFFER_MAX_SCHEMES + 1));
				CharSchemesTable.getInstance().setScheme(player.getObjectId(), name.trim(), new FastList<L2Skill>(PowerPakConfig.NPCBUFFER_MAX_SKILLS + 1));
				showManageSchemeWindow(player);
			}
		}
		// handles deletion
		else if (currentCommand.startsWith("deletescheme"))
		{
			if(!st.hasMoreTokens()){
				player.sendMessage("Error: Specify Schema Name!");
				showManageSchemeWindow(player);
				return;
			}
			
			String name = st.nextToken();
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).remove(name);
				showManageSchemeWindow(player);
			}
		}
		// handles cleanning
		else if (currentCommand.startsWith("clearscheme"))
		{
			if(!st.hasMoreTokens()){
				player.sendMessage("Error: Specify Schema Name!");
				showManageSchemeWindow(player);
				return;
			}
			
			String name = st.nextToken();
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).get(name).clear();
				showManageSchemeWindow(player);
			}
		}
		//predefined buffs
		else if (currentCommand.startsWith("fighterbuff") || currentCommand.startsWith("magebuff"))
		{
			
			ArrayList<L2Skill> skills_to_buff = new ArrayList<L2Skill>();
			if(currentCommand.startsWith("magebuff")){
				
				for(int skillId:PowerPakConfig.MAGE_SKILL_LIST.keySet()){
					
					L2Skill skill = SkillTable.getInstance().getInfo(skillId, PowerPakConfig.MAGE_SKILL_LIST.get(skillId));
					if(skill!=null)
					{
						skills_to_buff.add(skill);
					}
					
				}
				
			}else{
				
				for(int skillId:PowerPakConfig.FIGHTER_SKILL_LIST.keySet()){
					
					L2Skill skill = SkillTable.getInstance().getInfo(skillId, PowerPakConfig.FIGHTER_SKILL_LIST.get(skillId));
					if(skill!=null)
					{
						skills_to_buff.add(skill);
					}
					
				}
				
			}
			
			
			String targettype = "";
			if(st.hasMoreTokens())
				targettype = st.nextToken();
			
			int cost = 0;
			if(PowerPakConfig.BUFFER_PRICE>0)
				cost = PowerPakConfig.BUFFER_PRICE*skills_to_buff.size();
			
			if (cost == 0 || cost <= player.getInventory().getAdena())
			{
				L2Character target = player;
				if (targettype.equalsIgnoreCase("pet"))
					target = player.getPet();

				if (target != null)
				{
					for (L2Skill sk : skills_to_buff)
						sk.getEffects(target, target,false,false,false);
					player.reduceAdena("NPC Buffer", cost, null, true);
				}
				else
				{
					player.sendMessage("Incorrect Pet");
				}
			}
			else
			{
				player.sendMessage("Not enough adena");
				
			}
			
		}
	}

	private static String [] _BBSCommand = {"bbsbuff"};
	@Override
	public String[] getBBSCommands()
	{
		return _BBSCommand;
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		//html.replace("%objectId%", String.valueOf(getObjectId()));
		//html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targettype parameter {player, pet}
	 *
	 * @param player
	 * @param targettype
	 */
	private void showGiveBuffsWindow(L2PcInstance player, String targettype)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Buffer - Giving buffs to " + targettype + "</title>");
		tb.append("<body> Here are your defined profiles and their fee, just click on it to receive effects<br>");
		FastMap<String, FastList<L2Skill>> map = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId());
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty())
			tb.append("You have not defined any valid scheme, please go to Manage scheme and create at least one");
		else
		{
			int cost;
			tb.append("<table>");
			for (FastMap.Entry<String, FastList<L2Skill>> e = map.head(), end = map.tail(); (e = e.getNext()) != end;)
			{
				cost = getFee(e.getValue());
				tb.append("<tr><td width=\"90\"><a action=\"bypass -h custom_dobuff givebuffs " + targettype + " " + e.getKey() + " " + String.valueOf(cost) + "\">" + e.getKey() + "</a></td><td>Fee: " + String.valueOf(cost) + "</td></tr>");
			}
			tb.append("</table>");
		}
		tb.append("</body></html>");
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(tb.toString());
		sendHtmlMessage(player, html);
	}

	/**
	 * Sends an html packet to player with Manage scheme menu info. This allows player to create/delete/clear schemes
	 *
	 * @param player
	 */
	private void showManageSchemeWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Buffer - Manage Schemes</title>");
		tb.append("<body><br>");
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty())
			tb.append("<font color=\"LEVEL\">You have not created any scheme</font><br>");
		else
		{
			tb.append("Here is a list of your schemes. To delete one just click on drop button. To create, fill name box and press create. " + "Each scheme must have different name. Name must have up to 14 chars. Spaces (\" \") are not allowed. DO NOT click on Create until you have filled quick box<br>");
			tb.append("<table>");
			for (FastMap.Entry<String, FastList<L2Skill>> e = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).head(), end = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).tail(); (e = e.getNext()) != end;)
			{
				tb.append("<tr><td width=\"140\">" + e.getKey() + " (" + String.valueOf(CharSchemesTable.getInstance().getScheme(player.getObjectId(), e.getKey()).size()) + " skill(s))</td>");
				tb.append("<td width=\"60\"><button value=\"Clear\" action=\"bypass -h custom_dobuff clearscheme " + e.getKey() + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				tb.append("<td width=\"60\"><button value=\"Drop\" action=\"bypass -h custom_dobuff deletescheme " + e.getKey() + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
		}
		tb.append("<br><table width=240>");
		tb.append("<tr><td><edit var=\"name\" width=120 height=15></td><td><button value=\"create\" action=\"bypass -h custom_dobuff createscheme $name\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		tb.append("<br><font color=\"LEVEL\">Max schemes per player: " + String.valueOf(PowerPakConfig.NPCBUFFER_MAX_SCHEMES) + "</font>");
		tb.append("<br><br>");
		tb.append("<a action=\"bypass -h custom_dobuff menu\">Back</a>");
		tb.append("</body></html>");
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(tb.toString());
		sendHtmlMessage(player, html);
	}

	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 *
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 */
	private void showEditSchemeWindow(L2PcInstance player, String skill_group, String scheme_key)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(PARENT_DIR + "schememenu.htm");
		html.replace("%typesframe%", getTypesFrame(scheme_key));
		if (skill_group.equalsIgnoreCase("unselected"))
		{
			html.replace("%schemelistframe%", getPlayerSchemeListFrame(player, skill_group, scheme_key));
			html.replace("%skilllistframe%", getGroupSkillListFrame(player, null, null));
			html.replace("%myschemeframe%", getPlayerSkillListFrame(player, null, null));
		}
		else
		{
			html.replace("%schemelistframe%", getPlayerSchemeListFrame(player, skill_group, scheme_key));
			html.replace("%skilllistframe%", getGroupSkillListFrame(player, skill_group, scheme_key));
			html.replace("%myschemeframe%", getPlayerSkillListFrame(player, skill_group, scheme_key));
		}
		sendHtmlMessage(player, html);
	}

	/**
	 * Returns a table with info about player's scheme list.<br>
	 * If player scheme list is null, it returns a warning message
	 * @param player 
	 * @param skill_group 
	 * @param scheme_key 
	 * @return 
	 */
	private String getPlayerSchemeListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty())
			return "Please create at least one scheme";
		if (skill_group == null)
			skill_group = "def";
		if (scheme_key == null)
			scheme_key = "def";
		TextBuilder tb = new TextBuilder();
		tb.append("<table>");
		int count = 0;
		for (FastMap.Entry<String, FastList<L2Skill>> e = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).head(), end = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).tail(); (e = e.getNext()) != end;)
		{
			if (count == 0)
				tb.append("<tr>");
			tb.append("<td width=\"90\"><a action=\"bypass -h custom_dobuff editschemes " + skill_group + " " + e.getKey() + "\">" + e.getKey() + "</a></td>");
			if (count == 3)
			{
				tb.append("</tr>");
				count = 0;
			}
			count++;
		}
		if (!tb.toString().endsWith("</tr>"))
			tb.append("</tr>");
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 * @return a table with info about skills stored in each skill_group
	 */
	private String getGroupSkillListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (skill_group == null || skill_group == "unselected")
			return "Please, select a valid group of skills";
		else if (scheme_key == null || scheme_key.equalsIgnoreCase("unselected"))
			return "Please, select a valid scheme";
		TextBuilder tb = new TextBuilder();
		tb.append("<table>");
		int count = 0;
		for (L2Skill sk : BufferSkillsTable.getInstance().getSkillsByType(skill_group))
		{
			if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key) != null && !CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).isEmpty() && CharSchemesTable.getInstance().getSchemeContainsSkill(player.getObjectId(), scheme_key, sk.getId()))
				continue;
			if (count == 0)
				tb.append("<tr>");
			tb.append("<td width=\"100\"><a action=\"bypass -h custom_dobuff skillselect " + skill_group + " " + scheme_key + " " + String.valueOf(sk.getId()) + "\">" + sk.getName() + " (" + String.valueOf(sk.getLevel()) + ")</a></td>");
			if (count == 3)
			{
				tb.append("</tr>");
				count = -1;
			}
			count++;
		}
		if (!tb.toString().endsWith("</tr>"))
			tb.append("</tr>");
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 * @return a table with info about selected skills
	 */
	private String getPlayerSkillListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (skill_group == null || skill_group == "unselected")
			return "<br>Please, select a valid group of skills";
		else if (scheme_key == null || scheme_key.equalsIgnoreCase("unselected"))
			return "<br>Please, select a valid scheme";
		if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key) == null)
			return "Please choose your Scheme";
		if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).isEmpty())
			return "Empty Scheme";
		TextBuilder tb = new TextBuilder();
		tb.append("Scheme: " + scheme_key + "<br>");
		tb.append("<table>");
		int count = 0;
		for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key))
		{
			if (count == 0)
				tb.append("<tr>");
			tb.append("<td><a action=\"bypass -h custom_dobuff skillunselect " + skill_group + " " + scheme_key + " " + String.valueOf(sk.getId()) + "\">" + sk.getName() + "</a></td>");
			count++;
			if (count == 3)
			{
				tb.append("</tr>");
				count = 0;
			}
		}
		if (!tb.toString().endsWith("<tr>"))
			tb.append("<tr>");
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param scheme_key
	 * @return an string with skill_groups table.
	 */
	private String getTypesFrame(String scheme_key)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<table>");
		int count = 0;
		if (scheme_key == null)
			scheme_key = "unselected";
		for (String s : BufferSkillsTable.getInstance().getSkillsTypeList())
		{
			if (count == 0)
				tb.append("<tr>");
			tb.append("<td width=\"90\"><a action=\"bypass -h custom_dobuff editscheme " + s + " " + scheme_key + "\">" + s + "</a></td>");
			if (count == 2)
			{
				tb.append("</tr>");
				count = -1;
			}
			count++;
		}
		if (!tb.toString().endsWith("</tr>"))
			tb.append("</tr>");
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param list
	 * @return fee for all skills contained in list.
	 */
	private int getFee(FastList<L2Skill> list)
	{
		int fee = 0;
		if (PowerPakConfig.NPCBUFFER_STATIC_BUFF_COST >= 0)
			return list.size() * PowerPakConfig.NPCBUFFER_STATIC_BUFF_COST;
		for (L2Skill sk : list)
			fee += BufferSkillsTable.getInstance().getSkillFee(sk.getId());
		return fee;
	}
	
	
}
