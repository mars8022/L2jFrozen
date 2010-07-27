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
package interlude.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Multisell;
import interlude.gameserver.model.entity.Npcbuffer;
import interlude.gameserver.network.L2GameClient;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.skills.Stats;
import interlude.gameserver.templates.L2NpcTemplate;

public class L2BuffInstance extends L2NpcInstance
{
	public L2BuffInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance client)
	{
		if (this != client.getTarget())
		{
			client.setTarget(this);
			client.sendPacket(new MyTargetSelected(getObjectId(), 0));
			client.sendPacket(new ValidateLocation(this));
		}
		else
		{
			client.sendPacket(new MyTargetSelected(getObjectId(), 0));
			client.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			if (!isInsideRadius(client, 150, false, false))
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(new StringBuilder("data/html/mods/core_buffer/main.htm").toString());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				client.sendPacket(html);
			}
		}
	}

	@Override
	public void onBypassFeedback(L2PcInstance client, String command)
	{
		boolean bFail = true;
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		if (cmd.equalsIgnoreCase("chat"))
		{
			showChatWnd(client, st.nextToken());
			bFail = false;
		}
		if (cmd.equalsIgnoreCase("main"))
		{
			showChatWnd(client, "-1");
			bFail = false;
		}
		if (cmd.equalsIgnoreCase("buff"))
		{
			Npcbuffer.getInstance().useBuff(this, client, st.nextToken(), st.nextToken());
			bFail = false;
		}
		if (cmd.equalsIgnoreCase("restore"))
		{
			Npcbuffer.getInstance().useRestore(this, client, st.nextToken(), st.nextToken());
			bFail = false;
		}
		if (cmd.equalsIgnoreCase("reload"))
		{
			Npcbuffer.getInstance().reload(client);
			bFail = false;
		}
		if (cmd.equalsIgnoreCase("multisell"))
		{
			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(st.nextToken()), client, false, 0.0D);
			bFail = false;
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			if (quest.length() == 0) {
				showQuestWindow(client);
			} else {
				showQuestWindow(client, quest);
			}
		}
		if (bFail) {
			client.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onActionShift(L2GameClient game)
	{
		L2PcInstance client = game.getActiveChar();
		if (client.isGM())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">Buffer Information</font></center>");
			html1.append("<br><br><br>");
			html1.append(new StringBuilder("<br1><a action=\"bypass -h npc_").append(getObjectId()).append("_reload\">Reload buff.txt</a>").toString());
			html1.append(new StringBuilder("<br1><a action=\"bypass -h npc_").append(getObjectId()).append("_storefavs\">Force to save fav.txt</a><br>").toString());
			html1.append("<br1><a action=\"bypass -h admin_kill\">Kill</a>");
			html1.append("<br1><a action=\"bypass -h admin_delete\">Delete</a>");
			html1.append("Respawn Time: " + (getSpawn() != null ? getSpawn().getRespawnDelay() / 1000 + "  Seconds<br>" : "?  Seconds<br>"));
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().npcId + "</td></tr>");
			html1.append("<tr><td>Castle</td><td>" + getCastle().getCastleId() + "</td><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");
			html1.append("</table><br>");
			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + getCurrentHp() + "</td><td>Current MP</td><td>" + getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("</table><br>");
			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table>");
			html1.append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><br1></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("</table></center><br>");
			html1.append("</body></html>");
			html.setHtml(html1.toString());
			client.sendPacket(html);
			client.setTarget(this);
			client.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			onAction(client);
		}
	}

	@Override
	public void reduceCurrentHp(double d, L2Character l2character, boolean flag)
	{
	}

	@SuppressWarnings("unchecked")
	public void showChatWnd(L2PcInstance client, String id)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(new StringBuilder("data/html/mods/core_buffer/").append(id.equals("-1") ? "main" : new StringBuilder("main-").append(id).toString()).append(".htm").toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%after%", id);
		if (id.endsWith("_repl"))
		{
			interlude.gameserver.model.entity.Npcbuffer.BuffGroup bg;
			for (Iterator iterator = Npcbuffer.buffs().values().iterator(); iterator.hasNext(); html.replace(new StringBuilder("%e").append(bg.nId).append("i%").toString(), new StringBuilder("&#").append(bg.itemId).append(";").toString()))
			{
				bg = (interlude.gameserver.model.entity.Npcbuffer.BuffGroup) iterator.next();
				html.replace(new StringBuilder("%e").append(bg.nId).append("c%").toString(), String.valueOf(bg.itemCount));
			}
		}
		client.sendPacket(html);
	}

	public void showChatWnd(L2PcInstance client, String id, int count, int item)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(new StringBuilder("data/html/mods/core_buffer/main-notenought.htm").toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%after%", id);
		html.replace("%count%", String.valueOf(count));
		html.replace("%item%", new StringBuilder("&#").append(item).append(";").toString());
		client.sendPacket(html);
	}

	public void showChatErrWnd(L2PcInstance client, String after, String text)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(new StringBuilder("data/html/mods/core_buffer/main-err.htm").toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%text%", text);
		html.replace("%after%", after);
		client.sendPacket(html);
	}
}
