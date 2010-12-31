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
package com.l2jfrozen.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.handler.VoicedCommandHandler;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.model.BlockList;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.powerpak.PowerPak;
import com.l2jfrozen.gameserver.util.FloodProtector;

/**
 * This class ...
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public final class Say2 extends L2GameClientPacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";
	private static Logger _log = Logger.getLogger(Say2.class.getName());
	private static Logger _logChat = Logger.getLogger("chat");

	public final static int ALL = 0;
	public final static int SHOUT = 1; //!
	public final static int TELL = 2;
	public final static int PARTY = 3; //#
	public final static int CLAN = 4; //@
	public final static int GM = 5; ////gmchat
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10; ////announce
	public final static int PARTYROOM_ALL = 16; //(Red)
	public final static int PARTYROOM_COMMANDER = 15; //(Yellow)
	public final static int HERO_VOICE = 17; //%

	private final static String[] CHAT_NAMES =
	{
			"ALL  ", "SHOUT", "TELL ", "PARTY", "CLAN ", "GM   ", "PETITION_PLAYER", "PETITION_GM", "TRADE", "ALLIANCE", "ANNOUNCEMENT", //10
			"WILLCRASHCLIENT:)",
			"FAKEALL?",
			"FAKEALL?",
			"FAKEALL?",
			"PARTYROOM_ALL",
			"PARTYROOM_COMMANDER",
			"HERO_VOICE"
	};

	private String _text;
	private int _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS();
		try
		{
			_type = readD();
		}
		catch(BufferUnderflowException e)
		{
			_type = CHAT_NAMES.length;
		}
		_target = _type == TELL ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		if(Config.DEBUG)
		{
			_log.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");
		}

		if(_type < 0 || _type >= CHAT_NAMES.length)
		{
			_log.warning("Say2: Invalid type: " + _type);
			return;
		}
		L2PcInstance activeChar = getClient().getActiveChar();
		
				if ((_text.equalsIgnoreCase("hello") 
								|| _text.equalsIgnoreCase("hey") 
								|| _text.equalsIgnoreCase("aloha") 
								|| _text.equalsIgnoreCase("alo") 
								|| _text.equalsIgnoreCase("ciao")
								|| _text.equalsIgnoreCase("hi"))
								&& (!activeChar.isRunning() 
										|| !activeChar.isAttackingNow() 
										|| !activeChar.isCastingNow()
										|| !activeChar.isCastingPotionNow()))
							activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 2));
						
						if ((_text.equalsIgnoreCase("lol") 
								|| _text.equalsIgnoreCase("haha") 
								|| _text.equalsIgnoreCase("xaxa") 
								|| _text.equalsIgnoreCase("ghgh")
								|| _text.equalsIgnoreCase("jaja"))
								&& (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow() || !activeChar.isCastingPotionNow()))
							activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 10));
							
						if ((_text.equalsIgnoreCase("yes") 
								|| _text.equalsIgnoreCase("si")
								|| _text.equalsIgnoreCase("yep"))
								&& (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow()|| !activeChar.isCastingPotionNow()))
							activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 6));
						
						if ((_text.equalsIgnoreCase("no") 
								|| _text.equalsIgnoreCase("nop") 
								|| _text.equalsIgnoreCase("nope"))
								&& (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow()|| !activeChar.isCastingPotionNow()))
							activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 5));
					
		
		

		if(activeChar == null)
		{
			_log.warning("[Say2.java] Active Character is null.");
			return;
		}

		if(activeChar.isCursedWeaponEquiped() && (_type == TRADE || _type == SHOUT))
		{
			activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
			return;
		}

		if(activeChar.isChatBanned() && !activeChar.isGM())
		{
			//if (_type == ALL || _type == SHOUT || _type == TRADE || _type == HERO_VOICE)
			//{
			activeChar.sendMessage("You may not chat while a chat ban is in effect.");
			return;
			//}
		}

		if(activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if(_type == TELL || _type == SHOUT || _type == TRADE || _type == HERO_VOICE)
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}

		if(_type == PETITION_PLAYER && activeChar.isGM())
		{
			_type = PETITION_GM;
		}

		if(_text.length() > Config.MAX_CHAT_LENGTH)
		{
			activeChar.setChatBanned(true, 1800 * 1000L);
			if(Config.DEBUG)
			{
				_log.info("Say2: Msg Type = '" + _type + "' Text length more than " + Config.MAX_CHAT_LENGTH + " truncate them.");
			}
			_text = _text.substring(0, Config.MAX_CHAT_LENGTH);
			return;
		}

		if(Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");

			if(_type == TELL)
			{
				record.setParameters(new Object[]
				{
						CHAT_NAMES[_type], "[" + activeChar.getName() + " to " + _target + "]"
				});
			}
			else
			{
				record.setParameters(new Object[]
				{
						CHAT_NAMES[_type], "[" + activeChar.getName() + "]"
				});
			}

			_logChat.log(record);
		}

		_text = _text.replaceAll("\\\\n", "");

		// Say Filter implementation
		if(Config.USE_SAY_FILTER)
		{
			checkText(activeChar);
		}
		// by Azagthtot РћР±СЂР°Р±РѕС‚РєР° С‡Р°С‚Р° РґР»СЏ РІРµР±-СЃР°Р№С‚Р°
		PowerPak.getInstance().chatHandler(activeChar, _type, _text);
		//CreatureSay cs = new CreatureSay(activeChar.getObjectId(),_type, activeChar.getName(), _text);

		L2Object saymode = activeChar.getSayMode();
		if(saymode != null)
		{
			String name = saymode.getName();
			int actor = saymode.getObjectId();
			_type = 0;
			Collection<L2Object> list = saymode.getKnownList().getKnownObjects().values();

			CreatureSay cs = new CreatureSay(actor, _type, name, _text);
			for(L2Object obj : list)
			{
				if(obj == null || !(obj instanceof L2Character))
				{
					continue;
				}
				L2Character chara = (L2Character) obj;
				chara.sendPacket(cs);
			}
			return;
		}
		else
		{
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);

			switch(_type)
			{
				case TELL:
					L2PcInstance receiver = L2World.getInstance().getPlayer(_target);

					if(receiver != null && !BlockList.isBlocked(receiver, activeChar) || activeChar.isGM() && receiver != null)
					{
						if(receiver.isAway())
						{
							activeChar.sendMessage("Player is Away try again later.");
						}

						if(Config.JAIL_DISABLE_CHAT && receiver.isInJail())
						{
							activeChar.sendMessage("Player is in jail.");
							return;
						}

						if(receiver.isChatBanned())
						{
							activeChar.sendMessage("Player is chat banned.");
							return;
						}

						if(!receiver.getMessageRefusal())
						{
							receiver.sendPacket(cs);
							activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text));
						}
						else
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
						}
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
						sm.addString(_target);
						activeChar.sendPacket(sm);
						sm = null;
					}
					break;
				case SHOUT:
					if(Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
					{
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for(L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if(region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								player.sendPacket(cs);
							}
						}
					}
					else if(Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
					{
						for(L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							player.sendPacket(cs);
						}
					}
					break;
				case TRADE:
					if(Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
					{
						if(Config.TRADE_CHAT_IS_NOOBLE)
						{
							if(!activeChar.isNoble() && !activeChar.isGM())
							{
								activeChar.sendMessage("Only nobles can post in trade mass chat");
								return;
							}
							for(L2PcInstance player : L2World.getInstance().getAllPlayers())
							{
								player.sendPacket(cs);
							}
						}
						else
						{
							for(L2PcInstance player : L2World.getInstance().getAllPlayers())
							{
								player.sendPacket(cs);
							}
						}
					}
					else if(Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
					{
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for(L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if(region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								player.sendPacket(cs);
							}
						}
					}
					break;
				case ALL:
					if(_text.startsWith("."))
					{
						StringTokenizer st = new StringTokenizer(_text);
						IVoicedCommandHandler vch;
						String command = "";
						String target = "";

						if(st.countTokens() > 1)
						{
							command = st.nextToken().substring(1);
							target = _text.substring(command.length() + 2);
							vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
						}
						else
						{
							command = _text.substring(1);
							if(Config.DEBUG)
							{
								_log.info("Command: " + command);
							}
							vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
						}

						if(vch != null)
						{
							vch.useVoicedCommand(command, activeChar, target);
						}
						else
						{
							if(Config.DEBUG)
							{
								_log.warning("No handler registered for bypass '" + command + "'");
							}
						}
					}
					else
					{
						for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
						{
							if(player != null && activeChar.isInsideRadius(player, 1250, false, true))
							{
								player.sendPacket(cs);
							}
						}
						activeChar.sendPacket(cs);
					}
					break;
				case CLAN:
					if(activeChar.getClan() != null)
					{
						activeChar.getClan().broadcastToOnlineMembers(cs);
					}
					break;
				case ALLIANCE:
					if(activeChar.getClan() != null)
					{
						activeChar.getClan().broadcastToOnlineAllyMembers(cs);
					}
					break;
				case PARTY:
					if(activeChar.isInParty())
					{
						activeChar.getParty().broadcastToPartyMembers(cs);
					}
					break;
				case PETITION_PLAYER:
				case PETITION_GM:
					if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT));
						break;
					}

					PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
					break;
				case PARTYROOM_ALL:
					if(activeChar.isInParty())
					{
						if(activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
						{
							activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
						}
					}
					break;
				case PARTYROOM_COMMANDER:
					if(activeChar.isInParty())
					{
						if(activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
						{
							activeChar.getParty().getCommandChannel().broadcastToChannelMembers(cs);
						}
					}
					break;
				case HERO_VOICE:
					if(activeChar.isGM())
					{
						for(L2PcInstance player : L2World.getInstance().getAllPlayers())
							if(!BlockList.isBlocked(player, activeChar))
							{
								player.sendPacket(cs);
							}
					}
					else if(activeChar.isHero())
					{
						if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_HEROVOICE))
						{
							activeChar.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
							return;
						}
						for(L2PcInstance player : L2World.getInstance().getAllPlayers())
							if(!BlockList.isBlocked(player, activeChar))
							{
								player.sendPacket(cs);
							}
					}
					break;
			}
		}
	}

	private void checkText(L2PcInstance activeChar)
	{
		if(Config.USE_SAY_FILTER)
		{
			String filteredText = _text.toLowerCase();

			for(String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}

			if(!filteredText.equalsIgnoreCase(_text))
			{
				if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("chat"))
				{
					activeChar.setChatBanned(true, Config.CHAT_FILTER_PUNISHMENT_PARAM1 * 60 * 1000L);
					activeChar.sendMessage("Administrator banned you chat from " + Config.CHAT_FILTER_PUNISHMENT_PARAM1 + " minutes");
				}
				else if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("karma"))
				{
					activeChar.setKarma(Config.CHAT_FILTER_PUNISHMENT_PARAM2);
					activeChar.sendMessage("You have get " + Config.CHAT_FILTER_PUNISHMENT_PARAM2 + " karma for bad words");
				}
				else if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail"))
				{
					activeChar.setInJail(true, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}
				_text = filteredText;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}
}
