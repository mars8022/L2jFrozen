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
package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class Voting implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
			"vote", "votePoints", "getVoteReward", "voteTime"
	};

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		//flood protection
		long currentTime = System.currentTimeMillis();
		if(activeChar.getVoteTimestamp() > currentTime)
		{
			activeChar.sendMessage("You can't use Voting system soo fast!");
		}else
		{
			currentTime += 1000;
			activeChar.setVoteTimestamp(currentTime);
		}

		if(command.equalsIgnoreCase("vote"))
		{
			activeChar.sendMessage(".votePoints - tells how many points has been accumulated.");
			activeChar.sendMessage(".getVoteReward - converts vote points to a reward.");
			activeChar.sendMessage(".voteTime - tells will you be able to vote next time.");
		}
		else if(command.equalsIgnoreCase("votePoints"))
		{
			int votePoints = activeChar.getVotePoints();
			activeChar.sendMessage("You've collected "+ votePoints + ".");
			
		}
		else if(command.equalsIgnoreCase("voteTime"))
		{
			int voteTime = activeChar.getVoteTime();
			currentTime /= 1000;
			if((voteTime + 43200) > currentTime)
			{
				int secLeft = (int) ((voteTime+43200) - currentTime);
				int minutesLeft = secLeft / 60;
				secLeft %= 60;
				int hoursLeft = minutesLeft / 60;
				minutesLeft %= 60;
				
				activeChar.sendMessage("You'll be able to vote in "+hoursLeft+" hour(s) and "+minutesLeft+" minute(s).");
				
			}else
			{
				activeChar.sendMessage("You can vote now.");
			}			
		}
		else if(command.equalsIgnoreCase("getVoteReward"))
		{
			int votePoints = activeChar.getVotePoints();
			if(votePoints > 0)
			{
				activeChar.setVotePoints(0);
				activeChar.addItem("VoteReward", 4356, votePoints, activeChar, true);
			}else
			{
				activeChar.sendMessage("You've got not enough vote points.");
			}
			
		}
		return true;
	}

	/**
	 * @see com.l2jfrozen.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}