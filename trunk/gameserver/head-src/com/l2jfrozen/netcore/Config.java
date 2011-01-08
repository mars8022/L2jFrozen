package com.l2jfrozen.netcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javolution.text.TypeFormat;

public class Config
{
	
	public short MMOCORE_IP_TOS;
	public boolean MMOCORE_TCP_NO_DELAY;
	public boolean MMOCORE_KEEP_ALIVE;
	public int MMOCORE_BACKLOG;
	public int MMOCORE_SO_TIMEOUT;
	public boolean MMOCORE_SO_REUSEADDR;
	public int NETWORK_READ_BUFFER_SIZE;
	public int NETWORK_WRITE_BUFFER_SIZE;
	public int NETWORK_HELPER_BUFFER_SIZE;
	public int NETWORK_HELPER_BUFFER_COUNT;
	public static boolean PACKET_HANDLER_DEBUG;

	/** MMO settings */
	public static int MMO_SELECTOR_SLEEP_TIME			= 20;		// default 20
	public static int MMO_MAX_SEND_PER_PASS				= 12;		// default 12
	public static int MMO_MAX_READ_PER_PASS				= 12;		// default 12
	public static int MMO_HELPER_BUFFER_COUNT			= 20;		// default 20
	
	/** Client Packets Queue settings */
	public static int CLIENT_PACKET_QUEUE_SIZE								= 14;	// default MMO_MAX_READ_PER_PASS + 2
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE					= 13;	// default MMO_MAX_READ_PER_PASS + 1
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND			= 80;	// default 80
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL					= 5;	// default 5
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND	= 40;	// default 40
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN				= 2;	// default 2
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN				= 1;	// default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN			= 1;	// default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN				= 5;	// default 5
	
    
	//============================================================
	private static Config _instance;
	public static Config getInstance()
	{
		if(_instance==null)
			_instance = new Config();
		return _instance;
	}
	
	private Config()
	{
		final String MMO_CONFIG = "./config/protected/mmocore.properties";

		try
		{
			Properties mmoSetting = new Properties();
			InputStream is = new FileInputStream(new File(MMO_CONFIG));
			mmoSetting.load(is);
			is.close();

			MMOCORE_BACKLOG = TypeFormat.parseInt(mmoSetting.getProperty("NetworkBackLog", "50"));
			MMOCORE_IP_TOS = TypeFormat.parseShort(mmoSetting.getProperty("NetworkIpTOS", "0"));
			MMOCORE_TCP_NO_DELAY = TypeFormat.parseBoolean(mmoSetting.getProperty("NetworkTcpNoDelay", "false"));
			MMOCORE_KEEP_ALIVE = TypeFormat.parseBoolean(mmoSetting.getProperty("NetworkKeepAlive", "false"));
			MMOCORE_SO_TIMEOUT = TypeFormat.parseInt(mmoSetting.getProperty("NetworkSoTimeOut", "0"));
			MMOCORE_SO_REUSEADDR = TypeFormat.parseBoolean(mmoSetting.getProperty("NetworkSoReuseAddr", "true"));

			// Buffer options
			NETWORK_READ_BUFFER_SIZE = TypeFormat.parseInt(mmoSetting.getProperty("NetworkReadBufferSize", "64"));
			NETWORK_WRITE_BUFFER_SIZE = TypeFormat.parseInt(mmoSetting.getProperty("NetworkWriteBufferSize", "64"));
			NETWORK_HELPER_BUFFER_SIZE = TypeFormat.parseInt(mmoSetting.getProperty("NetworkHelperBufferSize", "64"));
			NETWORK_HELPER_BUFFER_COUNT = TypeFormat.parseInt(mmoSetting.getProperty("NetworkHelperBufferCount", "20"));
			
			PACKET_HANDLER_DEBUG 		= Boolean.parseBoolean(mmoSetting.getProperty("PacketHandlerDebug", "False"));
            
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + MMO_CONFIG + " File.");
		}
	}
}