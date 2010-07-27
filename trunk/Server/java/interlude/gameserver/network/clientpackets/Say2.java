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
package interlude.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.handler.ChatHandler;
import interlude.gameserver.handler.IChatHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.ActionFailed;

/**
 * This class is describes Say2 packet
 *
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public final class Say2 extends L2GameClientPacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";
	private static Logger _log = Logger.getLogger(Say2.class.getName());
	private static Logger _logChat = Logger.getLogger("chat");
	public final static int ALL = 0;
	public final static int SHOUT = 1; // !
	public final static int TELL = 2;
	public final static int PARTY = 3; // #
	public final static int CLAN = 4; // @
	public final static int GM = 5; // gmchat
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; // used for petition
	public final static int TRADE = 8; // +
	public final static int ALLIANCE = 9; // $
	public final static int ANNOUNCEMENT = 10; // announce
	public final static int PARTYROOM_ALL = 16; // (Red)
	public final static int PARTYROOM_COMMANDER = 15; // (Yellow)
	public final static int HERO_VOICE = 17;
	private final static String[] CHAT_NAMES = { "ALL  ", "SHOUT", "TELL ", "PARTY", "CLAN ", "GM   ", "PETITION_PLAYER", "PETITION_GM", "TRADE", "ALLIANCE", "ANNOUNCEMENT", "WILLCRASHCLIENT:)", "FAKEALL?", "FAKEALL?", "FAKEALL?", "PARTYROOM_ALL", "PARTYROOM_COMMANDER", "HERO_VOICE" };
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
		catch (BufferUnderflowException e)
		{
			_type = CHAT_NAMES.length;
		}
		_target = _type == TELL ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			_log.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");

		// is message type valid?
		if (_type < 0 || _type >= CHAT_NAMES.length)
		{
			_log.warning("Say2: Invalid type: " + _type);
			return;
		}
		// getting char instance
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null && activeChar instanceof L2PcInstance)
		{
			if (!Config.ENABLE_FP && !activeChar.getFloodProtectors().getChat().tryPerformAction("chat"))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		if (_text.length() >= 100)
		{
			_log.warning("Max input limit exceeded.");
			activeChar.sendMessage("You Cannot Input More Than 100 Characters");
			return;
		}
		// words from nowere?
		if (activeChar == null)
		{
			_log.warning("[Say2.java] Active Character is null.");
			return;
		}
		if (_text.length() >= 100)
		{
			_log.warning("Say2: Max input exceeded.");
			return;
		}
        // Say Filter implementation
        if (Config.USE_SAY_FILTER)
            checkText(activeChar);

		// player chat banned?
		if (activeChar.isChatBanned() && !activeChar.isGM())
		{
			activeChar.checkBanChat(true);
			return;
		}
		// player jailed?
		if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if (_type == TELL || _type == SHOUT || _type == TRADE || _type == HERO_VOICE)
			{
				activeChar.sendMessage("You Have been Chat Banned");
				return;
			}
		}
		// is it GM petition?
		if (_type == PETITION_PLAYER && activeChar.isGM())
			_type = PETITION_GM;
		
		if (_text.length() > 100)
		{
			_log.info("Say2: Msg Type = '" + _type + "' Text length more than " + 100 + " truncate them.");
			_text = _text.substring(0, 100);
		}  
		// must we log chat text?
		if (Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");
			if (_type == TELL)
				record.setParameters(new Object[] { CHAT_NAMES[_type], "[" + activeChar.getName() + " to " + _target + "]" });
			else
				record.setParameters(new Object[] { CHAT_NAMES[_type], "[" + activeChar.getName() + "]" });
			_logChat.log(record);
		}
		// prepare packet
		IChatHandler handler = ChatHandler.getInstance().getChatHandler(_type);
		if (handler != null)
			handler.handleChat(_type, activeChar, _target, _text);
	}
	private void checkText(L2PcInstance activeChar)
	{
		if (Config.USE_SAY_FILTER)
		{
			String filteredText = _text;

			for (String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}

			if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail") && _text != filteredText)
			{
				int punishmentLength = 0;
				if (Config.CHAT_FILTER_PUNISHMENT_PARAM2 == 0)
					punishmentLength = Config.CHAT_FILTER_PUNISHMENT_PARAM1;
				else
				{
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement;

						statement = con.prepareStatement("SELECT value FROM account_data WHERE (account_name=?) AND (var='jail_time')");
						statement.setString(1, activeChar.getAccountName());
						ResultSet rset = statement.executeQuery();

						if (!rset.next())
						{
							punishmentLength = Config.CHAT_FILTER_PUNISHMENT_PARAM1;
							PreparedStatement statement1;
							statement1 = con.prepareStatement("INSERT INTO account_data (account_name, var, value) VALUES (?, 'jail_time', ?)");
							statement1.setString(1, activeChar.getAccountName());
							statement1.setInt(2, punishmentLength);
							statement1.executeUpdate();
							statement1.close();
						}
						else
						{
							punishmentLength = rset.getInt("value") + Config.CHAT_FILTER_PUNISHMENT_PARAM2;
							PreparedStatement statement1;
							statement1 = con.prepareStatement("UPDATE account_data SET value=? WHERE (account_name=?) AND (var='jail_time')");
							statement1.setInt(1, punishmentLength);
							statement1.setString(2, activeChar.getAccountName());
							statement1.executeUpdate();
							statement1.close();
						}
						rset.close();
						statement.close();
	 	           }
	 	           catch (SQLException e)
	 	           {
	 	        	   _log.warning("Say2: Could not check character for chat filter punishment data: " + e);
	 	           }
	 	           finally
	 	           {
	 	        	   try
	 	        	   {
	 	        		   con.close();
	 	        	   }
	 	        	   catch (Exception e)
	 	        	   {
	 	        		   _log.warning("Say2: Error, While Trying to Check Text (Say2.checkText()");
	 	        	   }
	 	           }
				}
				activeChar.setPunishLevel(L2PcInstance.PunishLevel.JAIL, punishmentLength);
			}
			_text = filteredText;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}
}
