package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.gameserver.model.olympiad.Olympiad;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

public class shop implements IVoicedCommandHandler
{
    private static String[]    VOICED_COMMANDS    =
    {
    	"shop"
    };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        String htmFile = "data/html/custom/shop.htm";
        String htmContent = HtmCache.getInstance().getHtm(htmFile);

        if (activeChar.isInOlympiadMode()|| Olympiad.getInstance().isRegistered(activeChar))
		{
			activeChar.sendMessage("You cant use shop in olympiad mode");
			return false;
		}
        if (activeChar.isDead())
		{
			activeChar.sendMessage("You cant use shop in dead mode");
			return false;
		}

        if (htmContent != null)
        {
            NpcHtmlMessage Html = new NpcHtmlMessage(1);

            Html.setHtml(htmContent);
            activeChar.sendPacket(Html);
        }
        else
        {
            activeChar.sendMessage("Missing " + htmFile +  " !");
        }

        return true;
    }

    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}

