/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShowBoard;

public class TopBBSManager extends BaseBBSManager
{
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			
			separateAndSend(content, activeChar);
			content = null;
		}
		else if (command.equals("_bbshome"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			
			separateAndSend(content, activeChar);
			content = null;
		}
		else if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			final int idp = Integer.parseInt(st.nextToken());
			
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + idp + ".htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/" + idp + ".htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
			st = null;
			content = null;
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			sb = null;
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final L2PcInstance activeChar)
	{
		//
	}
	
	private static TopBBSManager _instance = new TopBBSManager();
	
	/**
	 * @return
	 */
	public static TopBBSManager getInstance()
	{
		return _instance;
	}
}
