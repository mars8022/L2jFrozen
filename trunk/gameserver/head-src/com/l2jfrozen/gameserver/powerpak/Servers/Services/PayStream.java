package com.l2jfrozen.gameserver.powerpak.Servers.Services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.L2Properties;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.L2Utils;
import com.l2jfrozen.gameserver.util.sql.SQLQuery;
import com.l2jfrozen.gameserver.util.sql.SQLQueue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PayStream implements HttpHandler
{
	protected static final Logger _log = Logger.getLogger(PayStream.class.getName());
	// An indication that all options are read, and the handler can be created

	// Security Key - defined in the study Lisnoe paystream
	private static String _KEY;
	// Prefix SMS
	private static String _SMSPrefix;
	// ID object, given by a reward
	protected static int _item;
	// The list of numbers and quantities awards for a number. Format: count [; number: the number ...]
	private static Map<Integer, Integer> _bonuses = new FastMap<Integer, Integer>();

	private static String _MessageOK;
	private static String _MessageFail;
	protected static String _UserMessage;

	public PayStream() throws Exception
	{
		// Parse the config, which lies in the config / powerpak / webservices / paystream.properties
		L2Properties p = new L2Properties("./config/powerpak/webservices/paystream.properties");
		_KEY = p.getProperty("SecurityKey", "");
		_item = Integer.parseInt(p.getProperty("RewardItem", "0"));
		_MessageFail = L2Utils.loadMessage(p.getProperty("MessageFail", "Character %s does not exists"));
		_MessageOK = L2Utils.loadMessage(p.getProperty("MessageOK", "%d CoL added for %s"));
		_SMSPrefix = p.getProperty("SMSPrefix", "");
		_UserMessage = L2Utils.loadMessage(p.getProperty("MessageForUser", ""));
		
		if(ItemTable.getInstance().getTemplate(_item) == null)
			throw new Exception("Reward item (" + _item + ") does not exits");
		
		StringTokenizer st = new StringTokenizer(p.getProperty("AmountForNumbers", ""), ";");
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			try
			{
				int iPos = token.indexOf(":");
				if(iPos != 0)
				{
					int number = Integer.parseInt(token.substring(0, iPos));
					int reward = Integer.parseInt(token.substring(iPos + 1));
					_bonuses.put(number, reward);
				}
			}
			catch(NumberFormatException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();

				continue;
			}
		}
	}

	private class PaymentUpdate implements SQLQuery
	{

		private String _charName;
		private L2PcInstance _char;
		private int _count;
		private Map<String, String> _params;

		public PaymentUpdate(String charName, int count, Map<String, String> params, L2PcInstance player)
		{
			_charName = charName;
			_count = count;
			_params = params;
			_char = player;
		}

		@Override
		public void execute(Connection con)
		{
			try
			{
				// Stores the result in the database, at the same time check to see whether we re pulling peystrim.
				PreparedStatement stm = con.prepareStatement("insert into paystream select ?,?,?,?,?,?,? from " + " characters where not exists (select * from  paystream where msgid=?) limit 1");
				stm.setString(1, _params.get("smsid"));
				stm.setString(8, _params.get("smsid"));
				stm.setTimestamp(2, Timestamp.valueOf(_params.get("date")));
				stm.setString(3, _params.get("user_id"));
				stm.setString(4, _params.get("num"));
				stm.setString(5, _charName);
				stm.setFloat(6, Float.parseFloat(_params.get("cost")));
				stm.setString(7, _params.get("currency"));
				boolean isOk = stm.executeUpdate() > 0; // Yes, I must add a user to aytemov.
				if(isOk && _char != null)
				{
					_char.addItem("donate", _item, _count, null, true);
					if(_char.isOnline() != 0 && _UserMessage.length() > 0)
						_char.sendMessage(_UserMessage);

					_char.store();
				}
				stm.close();

			}
			catch(Exception e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();

				_log.log(Level.WARNING, "WebServer: Paystream can't update data : " + e);
			}

		}

	}

	@Override
	public void handle(HttpExchange params) throws IOException
	{
		/* !! WARNING!
		* Paystream supports prefixes beginning with a +
		* HttpServer already decodes from UrlEncode getQuery (), however,
		* It does not decode + as space. The result is a "curve" line
		+ + Perfiks imya_chara
		* This is not true, because Line dodzhna be + prefix imya_chara
		*
		*/
		if(params.getRequestMethod().equalsIgnoreCase("GET"))
		{
			FastMap<String, String> query = new FastMap<String, String>();
			StringBuffer response = new StringBuffer();
			// Parse the parameters passed in the GET request
			StringTokenizer st = new StringTokenizer(params.getRequestURI().getQuery(), "&");
			while(st.hasMoreTokens())
			{
				String token = st.nextToken();
				int iPos = token.indexOf("=");
				if(iPos != -1)
				{
					String param = token.substring(0, iPos).toLowerCase();
					String value = token.substring(iPos + 1);

					// That is the replacement of which is written above..
					if(value != null && value.length() > 0)
					{
						if(value.charAt(0) == '+')
							value = "+" + URLDecoder.decode(value.substring(1), "UTF-8");
						else
							value = URLDecoder.decode(value, "UTF-8");
					}
					query.put(param, value);
				}
			}

			// Check whether there is a secret key and our Do it as well as other required parameters.
			if(query.get("skey") != null && query.get("skey").equals(_KEY) && query.get("num") != null)
			{
				String SMSText = query.get("msg"); // Take the text of the SMS
				if(SMSText != null)
				{
					int iPos = SMSText.indexOf(" ");
					if(iPos != -1)
					{
						String prefix = SMSText.substring(0, iPos).trim();
						String charName = SMSText.substring(iPos + 1).trim();
						boolean prefixOk = _SMSPrefix.length() > 0 ? prefix.equals(_SMSPrefix) : true;
						if(prefixOk)
						{ // Checked for a match prefix (just in case)
							try
							{
								Integer amount = _bonuses.get(Integer.parseInt(query.get("num")));
								if(amount == null) // Indicate whether our number is on the list?
									amount = 1; // No, the issue one aytemov
								response.append("status: reply\n\n"); // Header that the request should be returned to PayStream
								L2PcInstance pc = L2World.getInstance().getPlayer(charName);
								if(pc == null)
									pc = L2Utils.loadPlayer(charName);
								SQLQueue.getInstance().add(new PaymentUpdate(charName, amount, query, pc));
								if(pc != null)
									response.append(String.format(_MessageOK, amount, charName));
								else
									response.append(String.format(_MessageFail, charName));
								_log.info("WebServer: Paystream accepted payment from " + query.get("user_id") + " for character " + charName);
							}
							catch(NumberFormatException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
			params.sendResponseHeaders(200, response.length());
			OutputStream os = params.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}
}
