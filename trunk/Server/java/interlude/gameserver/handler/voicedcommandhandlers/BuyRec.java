package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.Config;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.InventoryUpdate;

/**
 * @author Rayder, Edit MeGaPk
 */
public class BuyRec implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = {"buyrec"};

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("buyrec"))
		{
            if(activeChar.getInventory().getItemByItemId(Config.REC_ITEM_ID) != null && activeChar.getInventory().getItemByItemId(Config.REC_ITEM_ID).getCount() >= Config.REC_ITEM_COUNT)
            {
            	InventoryUpdate iu = new InventoryUpdate();
            	activeChar.getInventory().destroyItemByItemId("Rec", Config.REC_ITEM_ID, Config.REC_ITEM_COUNT, activeChar, activeChar.getTarget());
            	activeChar.setRecomHave(activeChar.getRecomHave() + Config.REC_REWARD);
                activeChar.sendMessage("You Have Earned "+Config.REC_REWARD+" Recomends.");
				activeChar.getInventory().updateDatabase();
				activeChar.sendPacket(iu);
				activeChar.broadcastUserInfo();
              }
            else
            {
               	activeChar.sendMessage("You don't have enought items");
                return true;
            }
		}
		return false;
		}
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}