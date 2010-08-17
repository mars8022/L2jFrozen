package com.l2jfrozen.gameserver.powerpak.Servers.Services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static Log _log = LogFactory.getLog("webServer");
	// Признак, что все параметры прочитаны, и хэндлер может быть создан

	// Ключ безопасности - определяется в лисном кабинете paystream
	private static String _KEY;
	// Префикс SMS
	private static String _SMSPrefix;
	// ID предмета, даваемого в качестве награды
	private static int _item;
	// Список номеров и количества награды для номера. Формат номер:количество[;номер:количество...]
	private static Map<Integer, Integer> _bonuses = new FastMap<Integer, Integer>();

	private static String _MessageOK;
	private static String _MessageFail;
	private static String _UserMessage;

	public PayStream() throws Exception {
		// Разбираем конфиг, который лежит в config/powerpak/webservices/paystream.properties
			L2Properties p = new L2Properties("./config/powerpak/webservices/paystream.properties");
			_KEY = p.getProperty("SecurityKey","");
			_item = Integer.parseInt(p.getProperty("RewardItem","0"));
			_MessageFail = L2Utils.loadMessage(p.getProperty("MessageFail","Character %s does not exists"));
			_MessageOK = L2Utils.loadMessage(p.getProperty("MessageOK","%d CoL added for %s"));
			_SMSPrefix = p.getProperty("SMSPrefix","");
			_UserMessage = L2Utils.loadMessage(p.getProperty("MessageForUser",""));
			if(ItemTable.getInstance().getTemplate(_item)==null) 
				throw new Exception("Reward item ("+_item+") does not exits"); 
			StringTokenizer st = new StringTokenizer(p.getProperty("AmountForNumbers",""),";");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				try
				{
					int iPos = token.indexOf(":"); 
					if(iPos!=0)
					{
						int number = Integer.parseInt(token.substring(0,iPos));
						int reward = Integer.parseInt(token.substring(iPos+1));
						_bonuses.put(number, reward);
					}
				}
				catch(NumberFormatException e)
				{
					continue;
				}
			}
	}

	private class PaymentUpdate implements SQLQuery {
		
		private String _charName;
		private L2PcInstance _char;
		private int _count;
		private Map<String,String> _params;
		public PaymentUpdate(String charName, int count, Map<String,String> params, L2PcInstance player) {
			_charName = charName;
			_count = count;
			_params = params;
			_char = player;
		}
		@Override
		public void execute(Connection con) {
			try
			{
				// Сохраняем результат в БД, заодно проверяем не повторно ли нас дергает пэйстрим.
				PreparedStatement stm = con.prepareStatement("insert into paystream select ?,?,?,?,?,?,? from "+
															 " characters where not exists (select * from  paystream where msgid=?) limit 1");
				stm.setString(1, _params.get("smsid"));
				stm.setString(8, _params.get("smsid"));
				stm.setTimestamp(2, Timestamp.valueOf(_params.get("date")));
				stm.setString(3, _params.get("user_id"));
				stm.setString(4,_params.get("num"));
				stm.setString(5, _charName);
				stm.setFloat(6,Float.parseFloat(_params.get("cost")));
				stm.setString(7, _params.get("currency"));
				boolean isOk = stm.executeUpdate() > 0; // Да, надо добавить айтем юзеру.
				if(isOk && _char!=null) {
					_char.addItem("donate", _item, _count, null, true);
					if(_char.isOnline()!=0 && _UserMessage.length()>0)
						_char.sendMessage(_UserMessage);

					_char.store();
				}
				stm.close();


			}
			catch(Exception e)
			{
				_log.warn("WebServer: Paystream can't update data : "+e);
			}

		}

	}
	@Override
	public void handle(HttpExchange params) throws IOException {
		/* !!!! ВНИМАНИЕ !!!!
		 * Paystream поддерживает префиксы начинающиеся с символа +
		 * HttpServer уже декодирует из UrlEncode getQuery(), однако,
		 * он НЕ декодирует + как пробел. В результате получается "кривая" строка
		 * +перфикс+имя_чара
		 * Это не верно, т.к. строка доджна быть +префикс имя_чара
		 * 
		*/
		if(params.getRequestMethod().equalsIgnoreCase("GET")) {
			FastMap<String, String> query = new FastMap<String, String>();
			StringBuffer response = new StringBuffer();
			// Разбираем параметры, переданные в GET запросе.
			StringTokenizer st = new StringTokenizer(params.getRequestURI().getQuery(),"&");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				int iPos = token.indexOf("=");
				if(iPos!=-1) {
					String param = token.substring(0,iPos).toLowerCase();
					String value = token.substring(iPos+1);

					// Та самая замена, о которой написано выше.
					if(value!=null && value.length()>0) {
						if(value.charAt(0)=='+')
							value = "+"+URLDecoder.decode(value.substring(1),"UTF-8");
						else 
							value = URLDecoder.decode(value,"UTF-8");
					}
					query.put(param,value);
				}
			}

			// Проверяем есть ли секретный ключ и наш ли он а так же другие обязательные параметры
			if(query.get("skey")!=null && query.get("skey").equals(_KEY) && query.get("num")!=null) {
				String SMSText = query.get("msg"); // Берем текст SMS-ки
				if(SMSText!=null) {
					int iPos = SMSText.indexOf(" ");
					if(iPos !=-1) {
						String prefix = SMSText.substring(0,iPos).trim();
						String charName = SMSText.substring(iPos+1).trim();
						boolean prefixOk = _SMSPrefix.length()>0?prefix.equals(_SMSPrefix):true;
						if(prefixOk)
						{ // Проверяем на совпадение префикс (на всякий случай)
							try 
							{
								Integer amount = _bonuses.get(Integer.parseInt(query.get("num")));
								if(amount==null)  // Указан ли наш номер в списке?
									amount = 1;   // Нет, выдаем один айтем
								response.append("status: reply\n\n"); // Заголовок, который по требованию PayStream надо вернуть
								L2PcInstance pc = L2World.getInstance().getPlayer(charName);
								if(pc==null )
									pc = L2Utils.loadPlayer(charName);
								SQLQueue.getInstance().add(new PaymentUpdate(charName,amount,query,pc));
								if(pc!=null)
									response.append(String.format(_MessageOK, amount,charName));
								else
									response.append(String.format(_MessageFail,charName));
								_log.info("WebServer: Paystream accepted payment from "+query.get("user_id")+" for character "+charName);
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
