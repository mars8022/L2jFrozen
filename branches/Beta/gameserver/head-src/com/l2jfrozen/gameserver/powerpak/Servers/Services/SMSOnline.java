package com.l2jfrozen.gameserver.powerpak.Servers.Services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.L2Properties;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.powerpak.L2Utils;
import com.l2jfrozen.gameserver.util.sql.SQLQuery;
import com.l2jfrozen.gameserver.util.sql.SQLQueue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SMSOnline implements HttpHandler
{
	protected static final Logger _log = Logger.getLogger(SMSOnline.class.getName());
	
	private class DBUpdater implements SQLQuery {
	private L2PcInstance _pc;
	private int _count;
	private float _tid;
	private String _phone;
	private String _service;
	private String _charName;

		public DBUpdater(String charName, int count, String tid, String phone, String service, L2PcInstance player)
		{
			_pc = player;
			_count = count;
			_tid = Float.parseFloat(tid);
			_phone = phone;
			_service = service;
			_charName = charName;
		}
		@Override
		public void execute(Connection con)
		{
			boolean doAdd = _pc!=null;
			try
			{
				PreparedStatement stm = con.prepareStatement(" insert into smsonline select "+
															 "?,?,?,? from characters where not exists ( "+
															 "select * from smsonline where smstimestamp = ? and user_phone = ?) limit 1 ");
				stm.setFloat(1, _tid);
				stm.setFloat(5, _tid);
				stm.setString(2, _phone);
				stm.setString(6, _phone);
				stm.setString(3, _service);
				stm.setString(4, _charName);
				doAdd = doAdd && stm.executeUpdate() > 0;
				stm.close();
			}
			catch(SQLException e)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
				
				_log.log(Level.WARNING, "WebServices: SMSOnline error updating database",e);
				return;
			}
			if(doAdd)
			{
				_pc.addItem("SMSCoin", _RewardID, _count, null, true);
				if(_pc.isOnline()!=0 && _PlayerMessage.length()>0)
					_pc.sendMessage(_PlayerMessage);
				else
					_pc.store();
			}
		}

	}
	private static Map<String, Integer> _rewards = new FastMap<String, Integer>();
	private String _MsgOk;
	private String _MsgFail;
	private String _prefix;
	protected int _RewardID;
	protected String _PlayerMessage;
	public SMSOnline()
	{
		_rewards.clear();
		try
		{
			L2Properties p = new L2Properties("./config/powerpak/webservices/smsonline.properties");
			_RewardID = Integer.parseInt(p.getProperty("RewardID","4037"));
			_prefix = p.getProperty("Prefix","");
			_MsgOk = L2Utils.loadMessage(p.getProperty("SMSOk","%d CoL dobavleno dlya %s"));
			_MsgFail = L2Utils.loadMessage(p.getProperty("SMSFail","%s ne zaregistrirovan na servere"));
			_PlayerMessage = L2Utils.loadMessage(p.getProperty("MessageForPlayer",""));
			StringTokenizer st = new StringTokenizer(p.getProperty("AmountForNumbers",""),";");
			while(st.hasMoreTokens())
			{
				try
				{
					String  s =st.nextToken();
					int iPos = s.indexOf(":");
					if(iPos!=-1) 
						_rewards.put(s.substring(0,iPos).trim(), Integer.parseInt(s.substring(iPos+1).trim()));
				}
				catch(NumberFormatException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					continue;
				}
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "WebService: SMSOnline error reading config :",e);
		}
	}
	@Override
	public void handle(HttpExchange params) throws IOException {
		if(params.getRequestMethod().equalsIgnoreCase("GET")) {
			FastMap<String, String> query = new FastMap<String, String>();
			StringBuffer response = new StringBuffer();
			// Parse the parameters passed in the GET request.
			StringTokenizer st = new StringTokenizer(params.getRequestURI().getQuery(),"&");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				int iPos = token.indexOf("=");
				if(iPos!=-1) {
					String param = token.substring(0,iPos).toLowerCase();
					String value = token.substring(iPos+1);

					// That is the replacement of which is written above.
					if(value!=null && value.length()>0) {
						if(value.charAt(0)=='+')
							value = "+"+URLDecoder.decode(value.substring(1),"UTF-8");
						else 
							value = URLDecoder.decode(value,"UTF-8");
					}
					query.put(param,value);
				}

			}
			if(query.get("pref")!=null && query.get("txt")!=null && query.get("op")!=null &&
					query.get("phone")!=null && query.get("sn")!=null && query.get("tid")!=null) {
				boolean isOk = _prefix.length()>0?_prefix.equals(query.get("pref")):true;
				if(isOk) {
					L2PcInstance pc = L2Utils.loadPlayer(query.get("txt"));
					int count = _rewards.containsKey(query.get("sn"))?_rewards.get(query.get("sn")):1;
					SQLQueue.getInstance().add(new DBUpdater(query.get("txt"),count,query.get("tid"),query.get("phone"), 
															 query.get("sn"),pc));
					if(pc!=null)
						response.append("sms="+String.format(_MsgOk, count,pc.getName()));
					else 
						response.append("sms="+String.format(_MsgFail, query.get("txt")));
				}
				else 
					response.append("sms=Invalid prefix");
			}
			else
			response.append("");
			params.sendResponseHeaders(200, response.length());
			OutputStream os = params.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}
}
