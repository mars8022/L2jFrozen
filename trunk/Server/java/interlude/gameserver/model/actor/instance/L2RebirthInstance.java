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

import interlude.Config;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author DaRkRaGe [L2JOneo]
 */
public class L2RebirthInstance extends L2FolkInstance
{
	public L2RebirthInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@SuppressWarnings("unused")
	private final static Log _log = LogFactory.getLog(L2RebirthInstance.class.getName());

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		if (actualCommand.equalsIgnoreCase("rebirth"))
		{
			setTarget(player);
			int lvl = player.getLevel();
			if (lvl >= 80)
			{
				if (val.equalsIgnoreCase("79"))
				{
					long delexp = 0;
					delexp = player.getStat().getExp() - player.getStat().getExpForLevel(lvl - 79);
					player.getStat().addExp(-delexp);
					player.broadcastKarma();
					player.sendMessage("Rebirth Accepted.");
					int itemReward = 1;
					player.addItem("Loot", Config.REBIRTH_ITEM, itemReward, player, true);
					player.sendMessage("You have win " + itemReward + " Rebirth Item");
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/Rebirth/Rebirth.htm");
					html.replace("%lvl%", String.valueOf(player.getLevel()));
					sendHtmlMessage(player, html);
				}
			}
			else
			{
				player.sendMessage("You have to be at least level 80 to use Rebirth Engine.");
			}
			return;
		}
		else if (actualCommand.equalsIgnoreCase("Reward"))
		{
			setTarget(player);
			L2ItemInstance invItem = player.getInventory().getItemByItemId(Config.REBIRTH_ITEM);
			{
				if (invItem != null)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/Rebirth/reward.htm");
					html.replace("%lvl%", String.valueOf(player.getLevel()));
					sendHtmlMessage(player, html);
				}
				else
				{
					player.sendMessage("You Need Rebirth Book.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("skill1"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL1, Config.REBIRTH_SKILL1_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill2"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL2, Config.REBIRTH_SKILL2_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill3"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL3, Config.REBIRTH_SKILL3_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill4"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL4, Config.REBIRTH_SKILL4_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill5"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL5, Config.REBIRTH_SKILL5_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill6"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL6, Config.REBIRTH_SKILL6_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill7"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL7, Config.REBIRTH_SKILL7_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill8"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL8, Config.REBIRTH_SKILL8_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill9"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL9, Config.REBIRTH_SKILL9_LVL);
			player.addSkill(skill, true);
		}
		else if (actualCommand.equalsIgnoreCase("skill10"))
		{
			setTarget(player);
			player.destroyItem("Consume", Config.REBIRTH_ITEM, 1, null, false);
			L2Skill skill = SkillTable.getInstance().getInfo(Config.REBIRTH_SKILL10, Config.REBIRTH_SKILL10_LVL);
			player.addSkill(skill, true);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
			broadcastPacket(sa);
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/Rebirth/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
}