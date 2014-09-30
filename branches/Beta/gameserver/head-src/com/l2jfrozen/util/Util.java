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
package com.l2jfrozen.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @author luisantonioa
 */
public class Util
{
	protected static final Logger _log = Logger.getLogger(Util.class.getName());
	
	public static boolean isInternalIP(String ipAddress)
	{
		return ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("127.0.0.1");
	}

	public static String printData(byte[] data, int len)
	{
		TextBuilder result = new TextBuilder();

		int counter = 0;

		for(int i = 0; i < len; i++)
		{
			if(counter % 16 == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}

			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if(counter == 16)
			{
				result.append("   ");

				int charpoint = i - 15;
				for(int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if(t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}

				result.append('\n');
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if(rest > 0)
		{
			for(int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}

			int charpoint = data.length - rest;
			for(int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if(t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append('\n');
		}

		return result.toString();
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for(int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}

		return number;
	}

	/**
	 * @param s
	 */

	public static void printSection(String s)
	{
		int maxlength = 79;
		s = "-[ " + s + " ]";
		int slen = s.length();
		if(slen > maxlength)
		{
			_log.info(s);
			return;
		}
		int i;
		for(i = 0; i < maxlength - slen; i++)
		{
			s = "=" + s;
		}
		_log.info(s);
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	/**
	 * returns how many processors are installed on this system.
	 */
	private static void printCpuInfo()
	{
		_log.info("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		_log.info("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns the operational system server is running on it.
	 */
	private static void printOSInfo()
	{
		_log.info("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		_log.info("OS Arch: " + System.getProperty("os.arch"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns JAVA Runtime Enviroment properties
	 */
	private static void printJreInfo()
	{
		_log.info("Java Platform Information");
		_log.info("Java Runtime  Name: " + System.getProperty("java.runtime.name"));
		_log.info("Java Version: " + System.getProperty("java.version"));
		_log.info("Java Class Version: " + System.getProperty("java.class.version"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns general infos related to machine
	 */
	private static void printRuntimeInfo()
	{
		_log.info("Runtime Information");
		_log.info("Current Free Heap Size: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb");
		_log.info("Current Heap Size: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb");
		_log.info("Maximum Heap Size: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb");
		_log.info("..................................................");
		_log.info("..................................................");

	}

	/**
	 * calls time service to get system time.
	 */
	private static void printSystemTime()
	{
		// instanciates Date Objec
		Date dateInfo = new Date();

		//generates a simple date format
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");

		//generates String that will get the formater info with values
		String dayInfo = df.format(dateInfo);

		_log.info("..................................................");
		_log.info("System Time: " + dayInfo);
		_log.info("..................................................");
	}

	/**
	 * gets system JVM properties.
	 */
	private static void printJvmInfo()
	{
		_log.info("Virtual Machine Information (JVM)");
		_log.info("JVM Name: " + System.getProperty("java.vm.name"));
		_log.info("JVM installation directory: " + System.getProperty("java.home"));
		_log.info("JVM version: " + System.getProperty("java.vm.version"));
		_log.info("JVM Vendor: " + System.getProperty("java.vm.vendor"));
		_log.info("JVM Info: " + System.getProperty("java.vm.info"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * prints all other methods.
	 */
	public static void printGeneralSystemInfo()
	{
		printSystemTime();
		printOSInfo();
		printCpuInfo();
		printRuntimeInfo();
		printJreInfo();
		printJvmInfo();
	}

	/**
	 * converts a given time from minutes -> miliseconds
	 * @param minutesToConvert 
	 * @return
	 */
	public static int convertMinutesToMiliseconds(int minutesToConvert)
	{
		return minutesToConvert * 60000;
	}

	public static int getAvailableProcessors()
	{
		Runtime rt = Runtime.getRuntime();
		return rt.availableProcessors();
	}

	public static String getOSName()
	{
		return System.getProperty("os.name");
	}

	public static String getOSVersion()
	{
		return System.getProperty("os.version");
	}

	public static String getOSArch()
	{
		return System.getProperty("os.arch");
	}
}
