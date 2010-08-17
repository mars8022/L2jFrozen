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
import javolution.util.FastList;
import javolution.util.FastMap;
import interlude.Config;
import interlude.gameserver.datatables.BufferSkillsTable;
import interlude.gameserver.datatables.CharSchemesTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.network.L2GameClient;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;

/**
 * <b><font size=3>NPC Buffer instance handler</font></b><br>
 * <br>
 * This class contains some methods that can be sorted by different types and functions:<br>
 * <br>
 * - Methods that overrides to superclass' (L2FolkInstance): <li>onAction <li>onBypassFeedback <li>onActionShift <br>
 * <br>
 * - Methods to show html windows: <li>showGiveBuffsWindow <li>showManageSchemeWindow <li>showEditSchemeWindow <br>
 * </br> - Methods to get and build info (Strings, future html content) from character schemes, state, etc. <li>getPlayerSchemeListFrame: Returns a table with player's schemes names <li>getGroupSkillListFrame: Returns a table with skills available in the skill_group <li>getPlayerSkillListFrame: Returns a table with skills already in player's scheme (scheme_key) <br>
 * <br>
 *
 * @author House
 */
public class L2BufferInstance extends L2FolkInstance
{
	private static final String PARENT_DIR = "data/html/mods/buffer/";

	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();
		// initial menu
		if (currentCommand.startsWith("menu"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(PARENT_DIR + "menu.htm");
			sendHtmlMessage(player, html);
		}
		// handles giving effects {support player, support pet, givebuffs}
		else if (currentCommand.startsWith("support"))
		{
			String targettype = st.nextToken();
			showGiveBuffsWindow(player, targettype);
		}
		else if (currentCommand.startsWith("givebuffs"))
		{
			String targettype = st.nextToken();
			String scheme_key = st.nextToken();
			int cost = Integer.parseInt(st.nextToken());
			if (cost == 0 || cost <= player.getInventory().getAdena())
			{
				L2Character target = player;
				if (targettype.equalsIgnoreCase("pet")) {
					target = player.getPet();
				}

				if (target != null)
				{
					for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key))
					{
						sk.getEffects(this, target);
					}
					player.reduceAdena("NPC Buffer", cost, this, true);
				}
				else
				{
					player.sendMessage("Incorrect Pet");
					// go to main menu
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(PARENT_DIR + "menu.htm");
					sendHtmlMessage(player, html);
				}
			}
			else
			{
				player.sendMessage("Not enough adena");
				showGiveBuffsWindow(player, targettype);
			}
		}
		// handles edit schemes {skillselect, skillunselect}
		else if (currentCommand.startsWith("editscheme"))
		{
			String skill_group = st.nextToken();
			String scheme_key = null;
			try
			{
				scheme_key = st.nextToken();
			}
			catch (Exception e)
			{
				// ignored...
			}
			showEditSchemeWindow(player, skill_group, scheme_key);
		}
		else if (currentCommand.startsWith("skill"))
		{
			String skill_group = st.nextToken();
			String scheme_key = st.nextToken();
			int skill_id = Integer.parseInt(st.nextToken());
			int level = BufferSkillsTable.getInstance().getSkillLevelById(skill_id);
			if (currentCommand.startsWith("skillselect") && !scheme_key.equalsIgnoreCase("unselected"))
			{
				if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).size() < Config.NPCBUFFER_MAX_SKILLS) {
					CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).add(SkillTable.getInstance().getInfo(skill_id, level));
				} else {
					player.sendMessage("This scheme has reached maximun amount of buffs");
				}
			}
			else if (currentCommand.startsWith("skillunselect"))
			{
				CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).remove(SkillTable.getInstance().getInfo(skill_id, level));
			}
			showEditSchemeWindow(player, skill_group, scheme_key);
		}
		// manage schemes {create, delete, clear}
		else if (currentCommand.startsWith("manageschemes"))
		{
			showManageSchemeWindow(player);
		}
		// handles creation
		else if (currentCommand.startsWith("createscheme"))
		{
			if(!st.hasMoreTokens()){
				return;
			}
			
			String name = st.nextToken();
			if (name.length() > 14)
			{
				player.sendMessage("Error: Scheme's name must contain up to 14 chars without any spaces");
				showManageSchemeWindow(player);
			}
			else if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).size() == Config.NPCBUFFER_MAX_SCHEMES)
			{
				player.sendMessage("Error: Maximun schemes amount reached, please delete one before creating a new one");
				showManageSchemeWindow(player);
			}
			else if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				player.sendMessage("Error: duplicate entry. Please use another name");
				showManageSchemeWindow(player);
			}
			else
			{
				if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null) {
					CharSchemesTable.getInstance().getSchemesTable().put(player.getObjectId(), new FastMap<String, FastList<L2Skill>>(Config.NPCBUFFER_MAX_SCHEMES + 1));
				}
				CharSchemesTable.getInstance().setScheme(player.getObjectId(), name.trim(), new FastList<L2Skill>(Config.NPCBUFFER_MAX_SKILLS + 1));
				showManageSchemeWindow(player);
			}
		}
		// handles deletion
		else if (currentCommand.startsWith("deletescheme"))
		{
			String name = st.nextToken();
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).remove(name);
				showManageSchemeWindow(player);
			}
		}
		// handles cleanning
		else if (currentCommand.startsWith("clearscheme"))
		{
			String name = st.nextToken();
			if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null && CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name))
			{
				CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).get(name).clear();
				showManageSchemeWindow(player);
			}
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);
		if (!canTarget(player)) {
			return;
		}
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				// note: commented out so the player must stand close
				// player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(PARENT_DIR + "menu.htm");
				sendHtmlMessage(player, html);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null) {
			return;
		}
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			TextBuilder tb = new TextBuilder();
			tb.append("<html><title>NPC Buffer - Admin</title>");
			tb.append("<body>Changing buffs feature is not implemented yet. :)<br>");
			tb.append("<br>Please report any bug/impression/suggestion/etc at http://l2jserver.com/forum. " + "<br>Contact <font color=\"00FF00\">House</font></body></html>");
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(tb.toString());
			sendHtmlMessage(player, html);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}

	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targettype parameter {player, pet}
	 *
	 * @param player
	 * @param targettype
	 */
	private void showGiveBuffsWindow(L2PcInstance player, String targettype)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Buffer - Giving buffs to " + targettype + "</title>");
		tb.append("<body> Here are your defined profiles and their fee, just click on it to receive effects<br>");
		FastMap<String, FastList<L2Skill>> map = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId());
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty()) {
			tb.append("You have not defined any valid scheme, please go to Manage scheme and create at least one");
		} else
		{
			int cost;
			tb.append("<table>");
			for (FastMap.Entry<String, FastList<L2Skill>> e = map.head(), end = map.tail(); (e = e.getNext()) != end;)
			{
				cost = getFee(e.getValue());
				tb.append("<tr><td width=\"90\"><a action=\"bypass -h npc_%objectId%_givebuffs " + targettype + " " + e.getKey() + " " + String.valueOf(cost) + "\">" + e.getKey() + "</a></td><td>Fee: " + String.valueOf(cost) + "</td></tr>");
			}
			tb.append("</table>");
		}
		tb.append("</body></html>");
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(tb.toString());
		sendHtmlMessage(player, html);
	}

	/**
	 * Sends an html packet to player with Manage scheme menu info. This allows player to create/delete/clear schemes
	 *
	 * @param player
	 */
	private void showManageSchemeWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Buffer - Manage Schemes</title>");
		tb.append("<body><br>");
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty())
		{
			tb.append("<font color=\"LEVEL\">You have not created any scheme</font><br>");
		}
		else
		{
			tb.append("Here is a list of your schemes. To delete one just click on drop button. To create, fill name box and press create. " + "Each scheme must have different name. Name must have up to 14 chars. Spaces (\" \") are not allowed. DO NOT click on Create until you have filled quick box<br>");
			tb.append("<table>");
			for (FastMap.Entry<String, FastList<L2Skill>> e = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).head(), end = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).tail(); (e = e.getNext()) != end;)
			{
				tb.append("<tr><td width=\"140\">" + e.getKey() + " (" + String.valueOf(CharSchemesTable.getInstance().getScheme(player.getObjectId(), e.getKey()).size()) + " skill(s))</td>");
				tb.append("<td width=\"60\"><button value=\"Clear\" action=\"bypass -h npc_%objectId%_clearscheme " + e.getKey() + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				tb.append("<td width=\"60\"><button value=\"Drop\" action=\"bypass -h npc_%objectId%_deletescheme " + e.getKey() + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
		}
		tb.append("<br><table width=240>");
		tb.append("<tr><td><edit var=\"name\" width=120 height=15></td><td><button value=\"create\" action=\"bypass -h npc_%objectId%_createscheme $name\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		tb.append("<br><font color=\"LEVEL\">Max schemes per player: " + String.valueOf(Config.NPCBUFFER_MAX_SCHEMES) + "</font>");
		tb.append("<br><br>");
		tb.append("<a action=\"bypass -h npc_%objectId%_menu\">Back</a>");
		tb.append("</body></html>");
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(tb.toString());
		sendHtmlMessage(player, html);
	}

	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 *
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 */
	private void showEditSchemeWindow(L2PcInstance player, String skill_group, String scheme_key)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(PARENT_DIR + "schememenu.htm");
		html.replace("%typesframe%", getTypesFrame(scheme_key));
		if (skill_group.equalsIgnoreCase("unselected"))
		{
			html.replace("%schemelistframe%", getPlayerSchemeListFrame(player, skill_group, scheme_key));
			html.replace("%skilllistframe%", getGroupSkillListFrame(player, null, null));
			html.replace("%myschemeframe%", getPlayerSkillListFrame(player, null, null));
		}
		else
		{
			html.replace("%schemelistframe%", getPlayerSchemeListFrame(player, skill_group, scheme_key));
			html.replace("%skilllistframe%", getGroupSkillListFrame(player, skill_group, scheme_key));
			html.replace("%myschemeframe%", getPlayerSkillListFrame(player, skill_group, scheme_key));
		}
		sendHtmlMessage(player, html);
	}

	/**
	 * Returns a table with info about player's scheme list.<br>
	 * If player scheme list is null, it returns a warning message
	 */
	private String getPlayerSchemeListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null || CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).isEmpty()) {
			return "Please create at least one scheme";
		} else
		{
			if (skill_group == null) {
				skill_group = "def";
			}
			if (scheme_key == null) {
				scheme_key = "def";
			}
			TextBuilder tb = new TextBuilder();
			tb.append("<table>");
			int count = 0;
			for (FastMap.Entry<String, FastList<L2Skill>> e = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).head(), end = CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).tail(); (e = e.getNext()) != end;)
			{
				if (count == 0) {
					tb.append("<tr>");
				}
				tb.append("<td width=\"90\"><a action=\"bypass -h npc_%objectId%_editschemes " + skill_group + " " + e.getKey() + "\">" + e.getKey() + "</a></td>");
				if (count == 3)
				{
					tb.append("</tr>");
					count = 0;
				}
				count++;
			}
			if (!tb.toString().endsWith("</tr>")) {
				tb.append("</tr>");
			}
			tb.append("</table>");
			return tb.toString();
		}
	}

	/**
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 * @return a table with info about skills stored in each skill_group
	 */
	private String getGroupSkillListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (skill_group == null || skill_group == "unselected") {
			return "Please, select a valid group of skills";
		} else if (scheme_key == null || scheme_key == "unselected") {
			return "Please, select a valid scheme";
		}
		TextBuilder tb = new TextBuilder();
		tb.append("<table>");
		int count = 0;
		for (L2Skill sk : BufferSkillsTable.getInstance().getSkillsByType(skill_group))
		{
			if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key) != null && !CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).isEmpty() && CharSchemesTable.getInstance().getSchemeContainsSkill(player.getObjectId(), scheme_key, sk.getId()))
			{
				continue;
			}
			if (count == 0) {
				tb.append("<tr>");
			}
			tb.append("<td width=\"100\"><a action=\"bypass -h npc_%objectId%_skillselect " + skill_group + " " + scheme_key + " " + String.valueOf(sk.getId()) + "\">" + sk.getName() + " (" + String.valueOf(sk.getLevel()) + ")</a></td>");
			if (count == 3)
			{
				tb.append("</tr>");
				count = -1;
			}
			count++;
		}
		if (!tb.toString().endsWith("</tr>")) {
			tb.append("</tr>");
		}
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param player
	 * @param skill_group
	 * @param scheme_key
	 * @return a table with info about selected skills
	 */
	private String getPlayerSkillListFrame(L2PcInstance player, String skill_group, String scheme_key)
	{
		if (skill_group == null || skill_group == "unselected") {
			return "<br>Please, select a valid group of skills";
		} else if (scheme_key == null || scheme_key == "unselected") {
			return "<br>Please, select a valid scheme";
		}
		if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key) == null) {
			return "Please choose your Scheme";
		}
		if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key).isEmpty()) {
			return "Empty Scheme";
		}
		TextBuilder tb = new TextBuilder();
		tb.append("Scheme: " + scheme_key + "<br>");
		tb.append("<table>");
		int count = 0;
		for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), scheme_key))
		{
			if (count == 0) {
				tb.append("<tr>");
			}
			tb.append("<td><a action=\"bypass -h npc_%objectId%_skillunselect " + skill_group + " " + scheme_key + " " + String.valueOf(sk.getId()) + "\">" + sk.getName() + "</a></td>");
			count++;
			if (count == 3)
			{
				tb.append("</tr>");
				count = 0;
			}
		}
		if (!tb.toString().endsWith("<tr>")) {
			tb.append("<tr>");
		}
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param scheme_key
	 * @return an string with skill_groups table.
	 */
	private String getTypesFrame(String scheme_key)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<table>");
		int count = 0;
		if (scheme_key == null) {
			scheme_key = "unselected";
		}
		for (String s : BufferSkillsTable.getInstance().getSkillsTypeList())
		{
			if (count == 0) {
				tb.append("<tr>");
			}
			tb.append("<td width=\"90\"><a action=\"bypass -h npc_%objectId%_editscheme " + s + " " + scheme_key + "\">" + s + "</a></td>");
			if (count == 2)
			{
				tb.append("</tr>");
				count = -1;
			}
			count++;
		}
		if (!tb.toString().endsWith("</tr>")) {
			tb.append("</tr>");
		}
		tb.append("</table>");
		return tb.toString();
	}

	/**
	 * @param list
	 * @return fee for all skills contained in list.
	 */
	private int getFee(FastList<L2Skill> list)
	{
		int fee = 0;
		if (Config.NPCBUFFER_STATIC_BUFF_COST >= 0) {
			return list.size() * Config.NPCBUFFER_STATIC_BUFF_COST;
		} else
		{
			for (L2Skill sk : list)
			{
				fee += BufferSkillsTable.getInstance().getSkillFee(sk.getId());
			}
			return fee;
		}
	}
}