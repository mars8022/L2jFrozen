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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Give / Take Status Aio to Player
 * Changes name color and title color if enabled
 *
 * Uses:
 * setaio [<player_name>] [<time_duration in days>]
 * removeaio [<player_name>]
 *
 * If <player_name> is not specified, the current target player is used.
 *
 *
 * @author KhayrusS
 *
 */
public class AdminAio implements IAdminCommandHandler
{   
private static String[] _adminCommands =
{
   "admin_setaio", "admin_removeaio"
};
private final static Logger _log = Logger.getLogger(AdminAio.class.getName());

public boolean useAdminCommand(String command, L2PcInstance activeChar)
{   
   if (command.startsWith("admin_setaio"))
   {
      StringTokenizer str = new StringTokenizer(command);
      L2Object target = activeChar.getTarget();
      
      L2PcInstance player = null;
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      
      if (target != null && target instanceof L2PcInstance)
         player = (L2PcInstance)target;
      else
         player = activeChar;
                    
      try
      {
         str.nextToken();
         String time = str.nextToken();
         if (str.hasMoreTokens())
         {
            String playername = time;
            time = str.nextToken();
            player = L2World.getInstance().getPlayer(playername);
            doAio(activeChar, player, playername, time);
         }
         else
         {
            String playername = player.getName();
            doAio(activeChar, player, playername, time);
         }
         if(!time.equals("0"))
         {
            sm.addString("You are now a Aio, Congratulations!");
            player.sendPacket(sm);
         }
      }
      catch(Exception e)
      {
         activeChar.sendMessage("Usage: //setaio <char_name> [time](in days)");
      }
      
      player.broadcastUserInfo();
      if(player.isAio())
         return true;
   }
   else if(command.startsWith("admin_removeaio"))
   {
      StringTokenizer str = new StringTokenizer(command);
      L2Object target = activeChar.getTarget();
      
      L2PcInstance player = null;
      
      if (target != null && target instanceof L2PcInstance)
         player = (L2PcInstance)target;
      else
         player = activeChar;
      
      try
      {
         str.nextToken();
         if (str.hasMoreTokens())
         {
            String playername = str.nextToken();
            player = L2World.getInstance().getPlayer(playername);
            removeAio(activeChar, player, playername);
         }
         else
         {
            String playername = player.getName();
            removeAio(activeChar, player, playername);
         }
      }
      catch(Exception e)
      {
         activeChar.sendMessage("Usage: //removeaio <char_name>");
      }
      player.broadcastUserInfo();
      if(!player.isAio())
         return true;   
   }
   return false;
}

public void doAio(L2PcInstance activeChar, L2PcInstance _player, String _playername, String _time)
{
   int days = Integer.parseInt(_time);
   if (_player == null)
   {
      activeChar.sendMessage("not found char" + _playername);
      return;
   }
   
   if(days > 0)
   {
      _player.setAio(true);
      _player.setEndTime("aio", days);
      _player.getStat().addExp(_player.getStat().getExpForLevel(81));
      
      Connection connection = null;
      try
      {
         connection = L2DatabaseFactory.getInstance().getConnection();               
         
         PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE obj_id=?");
         statement.setLong(1, _player.getAioEndTime());
         statement.setInt(2, _player.getObjectId());
         statement.execute();
         statement.close();
         connection.close();
         
         if(Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
            _player.getAppearance().setNameColor(Config.AIO_NCOLOR);
       
         if(Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
            _player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
         
         _player.rewardAioSkills();
         _player.broadcastUserInfo();
         _player.sendPacket(new EtcStatusUpdate(_player));
         _player.sendSkillList();
         GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " set Aio stat for player "+ _playername + " for " + _time + " day(s)");
      }
      catch (Exception e)
      {
         _log.log(Level.WARNING,"could not set Aio stats to char:", e);
      }
      finally
      {
         L2DatabaseFactory.close(connection);
      }
   }
   else
   {
      removeAio(activeChar, _player, _playername);
   }
}

public void removeAio(L2PcInstance activeChar, L2PcInstance _player, String _playername)
{
   _player.setAio(false);
   _player.setAioEndTime(0);
   
   Connection connection = null;
   try
   {
      connection = L2DatabaseFactory.getInstance().getConnection();               
      
      PreparedStatement statement = connection.prepareStatement("UPDATE characters SET Aio=0, Aio_end=0 WHERE obj_id=?");
      statement.setInt(1, _player.getObjectId());
      statement.execute();
      statement.close();
      connection.close();
      
      _player.lostAioSkills();
      _player.getAppearance().setNameColor(0xFFFFFF);
      _player.getAppearance().setTitleColor(0xFFFFFF);
      _player.broadcastUserInfo();
      _player.sendPacket(new EtcStatusUpdate(_player));
      _player.sendSkillList();
      GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" remove Aio stat of player "+ _playername);
   }
   catch (Exception e)
   {
      _log.log(Level.WARNING,"could not remove Aio stats of char:", e);
   }
   finally
   {
      L2DatabaseFactory.close(connection);
   }
}

public String[] getAdminCommandList()
{
   return _adminCommands;
}
}