package com.l2scoria.gameserver.powerpak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import javolution.util.FastMap;

import com.l2scoria.Config;

public final class StringTable
{
	private static final long serialVersionUID = -4599023842346938325L;
	private Map<String, String> _messagetable = new FastMap<String, String>();
	public StringTable(String name)
	{
		if(!name.startsWith(".") && !name.startsWith("/"))
			name = Config.DATAPACK_ROOT.getPath()+"/data/messages/"+name;
		try
		{
			File f = new File(name);
			if(f.exists())
				load(new FileInputStream(f));
		}
		catch(IOException e)
		{
		}
	}
	public void load(FileInputStream inStream) throws IOException
	{
		try
		{
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inStream,"UTF-8"));
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
		finally
		{
			inStream.close();
		}
	}
	public String Message(String MsgId) {
		if(_messagetable.containsKey(MsgId))
			return _messagetable.get(MsgId);
		else
			return MsgId;
	}
	public String format(String MsgId,Object... params)
	{
		String msg = Message(MsgId);
		return String.format(msg, params);
	}
}
