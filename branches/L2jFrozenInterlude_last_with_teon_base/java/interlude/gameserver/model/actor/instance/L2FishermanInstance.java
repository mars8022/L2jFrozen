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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import interlude.Config;
import interlude.gameserver.TradeController;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.SkillTreeTable;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2SkillLearn;
import interlude.gameserver.model.L2TradeList;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.AquireSkillList;
import interlude.gameserver.network.serverpackets.BuyList;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SellList;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2NpcTemplate;

public class L2FishermanInstance extends L2FolkInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		return "data/html/fisherman/" + pom + ".htm";
	}

	private void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;
		if (getIsInTown()) {
			taxRate = getCastle().getTaxRate();
		}
		player.tempInvetoryDisable();
		if (Config.DEBUG) {
			_log.fine("Showing buylist");
		}
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		if (list != null && list.getNpcId().equals(String.valueOf(getNpcId())))
		{
			BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
			_log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warning("buylist id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showSellWindow(L2PcInstance player)
	{
		if (Config.DEBUG) {
			_log.fine("Showing selllist");
		}
		player.sendPacket(new SellList(player));
		if (Config.DEBUG) {
			_log.fine("Showing sell window");
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showSkillList(player);
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String command2 = st.nextToken();
		if (command2.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1) {
				return;
			}
			int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (command2.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public void showSkillList(L2PcInstance player)
	{
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Fishing);
		int counts = 0;
		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null) {
				continue;
			}
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}
		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
