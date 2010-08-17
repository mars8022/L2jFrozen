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
package interlude.gameserver.network.clientpackets;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import interlude.Config;
import interlude.gameserver.datatables.CharNameTable;
import interlude.gameserver.datatables.CharTemplateTable;
import interlude.gameserver.datatables.ItemTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.datatables.SkillTreeTable;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.instancemanager.QuestManager;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2ShortCut;
import interlude.gameserver.model.L2SkillLearn;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.item.PcInventory;
import interlude.gameserver.model.quest.Quest;
import interlude.gameserver.model.quest.QuestState;
import interlude.gameserver.network.L2GameClient;
import interlude.gameserver.network.serverpackets.CharCreateFail;
import interlude.gameserver.network.serverpackets.CharCreateOk;
import interlude.gameserver.network.serverpackets.CharSelectInfo;
import interlude.gameserver.templates.L2Item;
import interlude.gameserver.templates.L2PcTemplate;
import interlude.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		if (_name.length() < 3 || _name.length() > 16 || !Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
				_log.fine("charname: " + _name + " is invalid. creation failed.");

			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		synchronized (CharNameTable.getInstance())
		{
			if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				if (Config.DEBUG)
					_log.fine("Max number of characters reached. Creation failed.");

				CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
				sendPacket(ccf);
				return;
			}
			else if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (Config.DEBUG)
					_log.fine("charname: " + _name + " already exists. creation failed.");

				CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
				sendPacket(ccf);
				return;
			}
			template = CharTemplateTable.getInstance().getTemplate(_classId);
			if (Config.DEBUG)
				_log.fine("charname: " + _name + " classId: " + _classId + " template: " + template);

			if (template == null || template.classBaseLevel > 1)
			{
				CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
				sendPacket(ccf);
				return;
			}
			int objectId = IdFactory.getInstance().getNextId();
			newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);
		}
		newChar.setCurrentHp(template.baseHpMax);
		newChar.setCurrentCp(template.baseCpMax);
		newChar.setCurrentMp(template.baseMpMax);
		// newChar.setMaxLoad(template.baseLoad);
		// send acknowledgement
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);
		initNewChar(getClient(), newChar);
	}

	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
			result = false;

		return result;
	}

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
			_log.fine("Character init start");

		L2World.getInstance().storeObject(newChar);
		L2PcTemplate template = newChar.getTemplate();
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		newChar.addAncientAdena("Init", Config.STARTING_AA, null, false);
		if (Config.STARTING_LEVEL > 1)
			newChar.getStat().addLevel(--Config.STARTING_LEVEL);

		for (int[] startingItems : Config.CUSTOM_STARTER_ITEMS)
		{
			if (newChar == null)
				continue;

			PcInventory inv = newChar.getInventory();
			if (ItemTable.getInstance().createDummyItem(startingItems[0]).isStackable())
				inv.addItem("Starter Items", startingItems[0], startingItems[1], newChar, null);
			else
			{
				for (int i = 0; i < startingItems[1]; i++)
				{
					inv.addItem("Starter Items", startingItems[0], 1, newChar, null);
				}
			}
		}
		if (Config.SPAWN_CHAR)
			newChar.setXYZInvisible(Config.SPAWN_X, Config.SPAWN_Y, Config.SPAWN_Z);
		else
			newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);

		if (Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		L2ShortCut shortcut;
		// add attack shortcut
		shortcut = new L2ShortCut(0, 0, 3, 2, -1, 1);
		newChar.registerShortCut(shortcut);
		// add take shortcut
		shortcut = new L2ShortCut(3, 0, 3, 5, -1, 1);
		newChar.registerShortCut(shortcut);
		// add sit shortcut
		shortcut = new L2ShortCut(10, 0, 3, 0, -1, 1);
		newChar.registerShortCut(shortcut);
		if (!Config.DISABLE_OFFICIAL_STARTER_ITEMS)
		{
			ItemTable itemTable = ItemTable.getInstance();
			L2Item[] items = template.getItems();
			for (L2Item item2 : items)
			{
				L2ItemInstance item = newChar.getInventory().addItem("Init", item2.getItemId(), 1, newChar, null);
				if (item.getItemId() == 5588)
				{
					// add tutbook shortcut
					shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1);
					newChar.registerShortCut(shortcut);
				}
				if (item.isEquipable())
				{
					if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
						newChar.getInventory().equipItemAndRecord(item);
				}
			}
		}
		L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (L2SkillLearn startSkill : startSkills) {
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkill.getId(), startSkill.getLevel()), true);
			if (startSkill.getId() == 1001 || startSkill.getId() == 1177)
			{
				shortcut = new L2ShortCut(1, 0, 2, startSkill.getId(), 1, 1);
				newChar.registerShortCut(shortcut);
			}
			if (startSkill.getId() == 1216)
			{
				shortcut = new L2ShortCut(10, 0, 2, startSkill.getId(), 1, 1);
				newChar.registerShortCut(shortcut);
			}
			if (Config.DEBUG)
				_log.fine("adding starter skill:" + startSkill.getId() + " / " + startSkill.getLevel());

		}
		L2GameClient.saveCharToDisk(newChar);
		newChar.deleteMe(); // release the world of this character and it's inventory send char list
		L2World.getInstance().removeObject(newChar);
		startTutorialQuest(newChar);
		CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		if (Config.DEBUG)
			_log.fine("Character init end");
	}

	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		if (qs == null)
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		if (q != null)
			q.newQuestState(player);
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}