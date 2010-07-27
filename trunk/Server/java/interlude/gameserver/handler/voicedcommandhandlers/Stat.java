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
package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;


public class Stat implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { "stat" };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if (command.equalsIgnoreCase("stat"))
        {
            if (activeChar.getTarget()==null)
            {
                activeChar.sendMessage("You have no one targeted.");
                return false;
            }
            if (!(activeChar.getTarget() instanceof L2PcInstance))
            {
                activeChar.sendMessage("You can only get the info of a player.");

                return false;
            }

            L2PcInstance targetp = (L2PcInstance)activeChar.getTarget();



           activeChar.sendMessage("========="+ targetp.getName() +"=========");
            activeChar.sendMessage("Level: " + targetp.getLevel());
            if (targetp.getClan() != null)
            {
               activeChar.sendMessage("Clan: " + targetp.getClan().getName());
               activeChar.sendMessage("Alliance: " + targetp.getClan().getAllyName());
            }
            else
            {
               activeChar.sendMessage("Alliance: None");
               activeChar.sendMessage("Clan: None");
            }

           activeChar.sendMessage("Adena: " + targetp.getAdena());

          if(activeChar.getInventory().getItemByItemId(6393) == null)
       {
          activeChar.sendMessage("Medals : 0");
       }
          else
          {
             activeChar.sendMessage("Medals : " + targetp.getInventory().getItemByItemId(6393).getCount());
          }

          if(activeChar.getInventory().getItemByItemId(3470) == null)
       {
          activeChar.sendMessage("Gold Bars : 0");
       }
          else
          {
             activeChar.sendMessage("Gold Bars : " + targetp.getInventory().getItemByItemId(3470).getCount());
          }

            activeChar.sendMessage("PvP Kills: " + targetp.getPvpKills());
            activeChar.sendMessage("PvP Flags: " + targetp.getPvpFlag());
            activeChar.sendMessage("PK Kills: " + targetp.getPkKills());
            activeChar.sendMessage("HP, CP, MP: " + targetp.getMaxHp() + ", " +targetp.getMaxCp() + ", " + targetp.getMaxMp());
        }
       return true;
    }
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}