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
package interlude.gameserver.model.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.actor.instance.L2BuffInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

public class Npcbuffer
{
	public class BuffGroup
	{
		@SuppressWarnings("unchecked")
		public void addSkill(int t, int f)
		{
			entries.add(new int[] { t, f });
		}

		public void setCost(int t, int f)
		{
			itemId = t;
			itemCount = f;
		}

		public int nId;
		public int itemId;
		public int itemCount;
		@SuppressWarnings("unchecked")
		public List entries;

		@SuppressWarnings("unchecked")
		public BuffGroup(int id)
		{
			nId = id;
			entries = new FastList();
		}
	}

	public Npcbuffer()
	{
		bInitialized = false;
	}

	public static Npcbuffer getInstance()
	{
		if (i == null) {
			i = new Npcbuffer();
		}
		return i;
	}

	@SuppressWarnings("unchecked")
	public static Map buffs()
	{
		return buffs;
	}

	public void engineInit()
	{
		loadBuffs();
	}

	public void reload(L2PcInstance client)
	{
		loadBuffs();
		client.sendMessage(new StringBuilder("Buffer reload: entries ").append(buffs().size()).append(", muls ").append(buffs_mul.size()).toString());
	}

	public void useRestore(L2BuffInstance jj, L2PcInstance client, String type, String after)
	{
		if (type.equalsIgnoreCase("mp"))
		{
			if (client.getAdena() < mp_restore)
			{
				jj.showChatWnd(client, after, mp_restore, 57);
				return;
			}
			client.setCurrentMp(client.getMaxMp());
			client.reduceAdena("getrestore", mp_restore, jj, true);
		}
		if (type.equalsIgnoreCase("hp"))
		{
			if (client.getAdena() < hp_restore)
			{
				jj.showChatWnd(client, after, hp_restore, 57);
				return;
			}
			client.setCurrentHp(client.getMaxHp());
			client.reduceAdena("getrestore", hp_restore, jj, true);
		}
		if (type.equalsIgnoreCase("cp"))
		{
			if (client.getAdena() < cp_restore)
			{
				jj.showChatWnd(client, after, cp_restore, 57);
				return;
			}
			client.setCurrentCp(client.getMaxCp());
			client.reduceAdena("getrestore", cp_restore, jj, true);
		}
		jj.showChatWnd(client, after);
	}

	@SuppressWarnings("unchecked")
	public void useBuff(L2BuffInstance jj, L2PcInstance client, String st, String after)
	{
		if (!bInitialized)
		{
			jj.showChatErrWnd(client, after, client.isGM() ? "buffer is not initialized." : "Come back later.");
			return;
		}
		BuffGroup buff = (BuffGroup) buffs().get(Integer.valueOf(Integer.parseInt(st)));
		if (buff == null)
		{
			jj.showChatErrWnd(client, after, client.isGM() ? new StringBuilder("template ").append(st).append(" is null.").toString() : "Come back later.");
			return;
		}
		L2ItemInstance item = client.getInventory().getItemByItemId(buff.itemId);
		if (item == null || item.getCount() < buff.itemCount)
		{
			jj.showChatWnd(client, after, buff.itemCount, buff.itemId);
			return;
		}
		if (buff.itemId == 57) {
			client.reduceAdena("getbuff", buff.itemCount, jj, true);
		} else {
			client.destroyItem("getbuff", item.getObjectId(), buff.itemCount, jj, true);
		}
		int ef[];
		for (Iterator iterator = buff.entries.iterator(); iterator.hasNext(); affect(jj, client, ef)) {
			ef = (int[]) iterator.next();
		}
		client.updateEffectIcons();
		jj.showChatWnd(client, after);
	}

	public void affect(L2BuffInstance jj, L2PcInstance client, int effect[])
	{
		L2Skill skill = SkillTable.getInstance().getInfo(effect[0], effect[1]);
		if (skill != null)
		{
			cae(client, skill);
			skill.getEffects(jj, client);
			client.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(effect[0], effect[1]));
		}
		else
		{
			client.sendMessage(client.isGM() ? new StringBuilder("null skill ").append(effect[0]).append(" lv").append(effect[1]).toString() : "You are not able to receive this effect.");
		}
	}

	private void cae(L2PcInstance client, L2Skill skill)
	{
		L2Effect al2effect[];
		int k = (al2effect = client.getAllEffects()).length;
		for (int j = 0; j < k; j++)
		{
			L2Effect ef = al2effect[j];
			if (ef.getSkill().getId() == skill.getId()) {
				ef.exit();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadBuffs()
	{
		LineNumberReader lnr;
		BuffGroup buffGroup;
		buffs = new FastMap();
		buffs_mul = new FastMap();
		mp_restore = 1001;
		cp_restore = 1001;
		hp_restore = 1001;
		String ln = null;
		lnr = null;
		buffGroup = null;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("config/custom/NpcBuffer.ini"))));
			while ((ln = lnr.readLine()) != null)
			{
				if (ln.trim().length() == 0 || ln.startsWith("//")) {
					continue;
				}
				if (ln.startsWith("@param"))
				{
					String h[] = ln.split(" ");
					if (h[1].equalsIgnoreCase("cp_restore")) {
						cp_restore = Integer.parseInt(h[2]);
					}
					if (h[1].equalsIgnoreCase("hp_restore")) {
						hp_restore = Integer.parseInt(h[2]);
					}
					if (h[1].equalsIgnoreCase("mp_restore")) {
						mp_restore = Integer.parseInt(h[2]);
					}
					continue;
				}
				if (ln.contains("//"))
				{
					ln = ln.split("//")[0];
					ln = ln.replaceAll(" ", "");
					if (ln.trim().length() < 1) {
						continue;
					}
					ln = ln.replaceAll("\t", "");
				}
				String t[] = ln.split(";");
				for (String e : t) {
					if (e.contains("="))
					{
						if (e.split("=")[0].contains("entry"))
						{
							int entryId = Integer.parseInt(e.split("=")[1]);
							buffGroup = new BuffGroup(entryId);
						}
						if (e.split("=")[0].contains("cost"))
						{
							String t2 = e.split("=")[1];
							t2 = t2.substring(1, t2.length() - 1);
							buffGroup.setCost(Integer.parseInt(t2.split(",")[0]), Integer.parseInt(t2.split(",")[1]));
						}
					}
					else
					{
						buffGroup.addSkill(Integer.parseInt(e.split(",")[0]), Integer.parseInt(e.split(",")[1]));
					}
				}
				if (buffGroup != null) {
					buffs.put(Integer.valueOf(buffGroup.nId), buffGroup);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			bInitialized = false;
		}
		bInitialized = true;
		System.out.println(new StringBuilder("Buffer reload: entries ").append(buffs().size()).append(", muls ").append(buffs_mul.size()).toString());
		return;
	}

	@SuppressWarnings("unchecked")
	private static Map buffs;
	@SuppressWarnings("unchecked")
	private Map buffs_mul;
	private int mp_restore;
	private int cp_restore;
	private int hp_restore;
	private static Npcbuffer i;
	private boolean bInitialized;
}
