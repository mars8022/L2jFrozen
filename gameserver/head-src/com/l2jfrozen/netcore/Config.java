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
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + MMO_CONFIG + " File.");
		}
	}
}