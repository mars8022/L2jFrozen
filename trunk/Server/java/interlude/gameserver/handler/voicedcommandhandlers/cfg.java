package interlude.gameserver.handler.voicedcommandhandlers;

import interlude.gameserver.model.olympiad.Olympiad;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

public class cfg implements IVoicedCommandHandler
{
    private static String[]    VOICED_COMMANDS    =
    {
    	"cfg"
    };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        String htmFile = "data/html/custom/cfg.htm";
        String htmContent = HtmCache.getInstance().getHtm(htmFile);

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

