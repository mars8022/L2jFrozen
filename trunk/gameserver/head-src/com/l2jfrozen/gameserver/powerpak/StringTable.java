package com.l2jfrozen.gameserver.powerpak;

/**
 * L2JFrozen
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import javolution.util.FastMap;

import com.l2jfrozen.Config;

public final class StringTable
{
	private Map<String, String> _messagetable = new FastMap<String, String>();
	
	public StringTable(String name)
	{
		if(!name.startsWith(".") && !name.startsWith("/"))
			name = Config.DATAPACK_ROOT.getPath()+"/data/messages/"+name;
		
		File f = new File(name);
		load(f);
		
	}
	
	public void load(File f)
	{
		if(!f.exists())
			return;
		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		LineNumberReader lnr = null;
		
		try
		{
			
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis,"UTF-8");
			lnr = new LineNumberReader(isr);
			
			String line;
			while((line=lnr.readLine())!=null)
			{
				if(line.length()>1 && 	line.getBytes()[0] == (byte)0x3F ) {
					line = line.substring(1);
				}
				int iPos = line.indexOf("#");
				if(iPos!=-1) line = line.substring(0,iPos);
				iPos = line.indexOf("=");
				if(iPos!=-1)
				{
					_messagetable.put(line.substring(0,iPos).trim(),line.substring(iPos+1));
				}
			}
		}
		catch(IOException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
		}finally{
			
			if(lnr != null)
				try
				{
					lnr.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(isr != null)
				try
				{
					isr.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(fis != null)
				try
				{
					fis.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
		}
	
	}
	
	public String Message(String MsgId) {
		if(_messagetable.containsKey(MsgId))
			return _messagetable.get(MsgId);
		return MsgId;
	}
	
	public String format(String MsgId,Object... params)
	{
		String msg = Message(MsgId);
		return String.format(msg, params);
	}
	
}
