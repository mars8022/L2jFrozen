/*
 *Porfavor, no intenten copiar el codigo y poner sus creditos cuesta su trabajo escribilo
 *Agradecer a eliteadmins por el apoyo dado en todo mi trabajo.
 */

package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.Config;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.InventoryUpdate;

/**
 * @author Rayder
 **/

public class locrb implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS =
    {
        "antharas",
        "valakas",
        "baium",
        "queenant",
        "zaken",
        "orfen",
        "core"
    };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
       {
            if(activeChar.atEvent)
                    {
                        activeChar.sendMessage("You can't teleport while you are in the event.");
                        return false;
                    }
            else if (activeChar.isInDangerArea())
                    {
                          activeChar.sendMessage("You in DangerArea or no have money");
                          return false;
                    }
            else if (activeChar.isFestivalParticipant())
                    {
            	       activeChar.sendMessage("You can't use this command while participating in the Festival!");
                       return false;
                    }
            else if(activeChar.isInJail())
                    {
                        activeChar.sendMessage("You can't teleport while you are in jail.");
                        return false;
                    }
            else if(activeChar.isDead())
                    {
                        activeChar.sendMessage("You can't teleport while you are dead.");
                        return false;
                    }
            else if(activeChar.isInCombat())
                    {
                        activeChar.sendMessage("You can't teleport while you are in combat.");
                        return false;
                    }
            else if (activeChar.isInDuel())
                    {
                        activeChar.sendMessage("You can't teleport while you are doing a duel.");
                        return false;
                    }
            else if (activeChar.isInOlympiadMode())
                    {
                        activeChar.sendMessage("You can't teleport while you are in olympiad");
                        return false;
                    }
            else if (activeChar.inObserverMode())
                    {
                        activeChar.sendMessage("You can't teleport while you are in observer mode");
                        return false;
                    }

            if (Config.ALLOW_LOC_RB && !activeChar.inObserverMode() && !activeChar.isInOlympiadMode() && !activeChar.isInDuel() && !activeChar.isInCombat() && !activeChar.isDead() && !activeChar.isInJail())
            {
                if (activeChar.getInventory().getItemByItemId(Config.LOCRB_ITEM_ID) != null && activeChar.getInventory().getItemByItemId(Config.LOCRB_ITEM_ID).getCount() >= Config.LOCRB_ITEM_COUNT)
                      {
            InventoryUpdate iu = new InventoryUpdate();
            activeChar.getInventory().destroyItemByItemId("Rec", Config.LOCRB_ITEM_ID, Config.LOCRB_ITEM_COUNT, activeChar, activeChar.getTarget());
            activeChar.sendMessage("You lost " +Config.LOCRB_ITEM_COUNT+" "+Config.LOCRB_ITEM_NAME+"");
            activeChar.getInventory().updateDatabase();
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
             if(command.startsWith("valakas"))
            {
                activeChar.teleToLocation(213604, -114795, -1636);
            }
            else if(command.startsWith("baium"))
            {
                activeChar.teleToLocation(115213, 16623, 10080);
            }
            else if(command.startsWith("queenant"))
            {
                activeChar.teleToLocation(-21610, 181594, -5734);
            }
            else if(command.startsWith("zaken"))
            {
                activeChar.teleToLocation(55312, 219168, -3223);
            }
            else if(command.startsWith("orfen"))
            {
                activeChar.teleToLocation(43021, 17378, -4394);
            }
            else if(command.startsWith("core"))
            {
                activeChar.teleToLocation(17682, 111439, -6584);
            }
            else if(command.startsWith("antharas"))
            {
                activeChar.teleToLocation(185708, 114298, -8221);
            }
            }
                else
            {
               	activeChar.sendMessage("You don't have "+Config.LOCRB_ITEM_COUNT+" "+Config.LOCRB_ITEM_NAME+"");
                return false;
            }
         }


       }
        return true;
    }
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}