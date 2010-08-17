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

import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.NpcBufferSkillIdsTable;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.skills.Formulas;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

/**
 * L2NpcBufferInstance
 *
 * @author KidZor & VaGo
 * @Special Thx to FbiAgent
 */
public class L2NpcBufferInstance extends L2NpcInstance
{
	private class BuffTask implements Runnable
	{
		private Boolean _buffing = false;
		private L2NpcBufferInstance _me = null;
		private Vector<L2PcInstance> _playerInstances = new Vector<L2PcInstance>();
		private Vector<Integer> _skillIds = new Vector<Integer>();
		private Vector<Integer> _skillLevels = new Vector<Integer>();
		@SuppressWarnings("unchecked")
		private ScheduledFuture _task = null;

		public BuffTask(L2NpcBufferInstance me)
		{
			_me = me;
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 100, 100);
		}

		public void addBuff(L2PcInstance playerInstance, int skillId, int skillLevel)
		{
			synchronized (_playerInstances)
			{
				_playerInstances.add(playerInstance);
				_skillIds.add(skillId);
				_skillLevels.add(skillLevel);
			}
		}

		public void run()
		{
			boolean abort = false;
			synchronized (_buffing)
			{
				abort = _buffing;
				_buffing = true;
			}
			if (abort) {
				return;
			}
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException ie)
			{
			}
			int index = -1;
			L2PcInstance playerInstance = null;
			int skillId = 0;
			int skillLevel = 0;
			synchronized (_playerInstances)
			{
				index = _skillIds.size() - 1;
				if (index != -1)
				{
					index = Rnd.get(_skillIds.size());
					playerInstance = _playerInstances.get(index);
					skillId = _skillIds.get(index);
					skillLevel = _skillLevels.get(index);
				}
			}
			if (index == -1) {
				return;
			}
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (playerInstance != null && skill != null)
			{
				// if (_me.isInsideRadius(playerInstance.getX(), playerInstance.getY(), skill.getCastRange(), true))
				// continue;
				int skillTime = Formulas.getInstance().calcMAtkSpd(_me, skill, skill.getHitTime());
				if (skill.isDance()) {
					_me.broadcastPacket(new MagicSkillUser(_me, _me, skillId, skillLevel, skillTime, 0));
				} else {
					_me.broadcastPacket(new MagicSkillUser(_me, playerInstance, skillId, skillLevel, skillTime, 0));
				}
				long continueTime = System.currentTimeMillis() + skillTime;
				while (continueTime >= System.currentTimeMillis())
				{
					try
					{
						Thread.sleep(1);
					}
					catch (InterruptedException ie)
					{
					}
				}
				L2Effect[] effects = playerInstance.getAllEffects();
				if (effects != null)
				{
					for (L2Effect e : effects)
					{
						if (e != null && skill != null)
						{
							if (e.getSkill().getId() == skill.getId()) {
								e.exit();
							}
						}
					}
				}
				skill.getEffects(playerInstance, playerInstance);
			} else {
				System.out.println("NpcBuffer warning(" + getNpcId() + " at " + getX() + ", " + getY() + ", " + getZ() + "): Skill or Player null!");
			}
			synchronized (_playerInstances)
			{
				_playerInstances.remove(index);
				_skillIds.remove(index);
				_skillLevels.remove(index);
			}
			synchronized (_buffing)
			{
				_buffing = false;
			}
		}

		protected void stopTask()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}
		}
	}

	private BuffTask _buffTaskInstance = null;

	public L2NpcBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_buffTaskInstance = new BuffTask(this);
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
		return "data/html/mods/buffer/" + pom + ".htm";
	}

	@Override
	public void deleteMe()
	{
		_buffTaskInstance.stopTask();
		_buffTaskInstance = null;
		super.deleteMe();
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		if (playerInstance == null) {
			return;
		}
		int npcId = getNpcId();
		if (command.startsWith("npc_buffer_heal"))
		{
			if (!playerInstance.getFloodProtectors().getBuffer().tryPerformAction("npc_buffer_heal"))
			{
				playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (playerInstance.getCurrentHp() == 0 || playerInstance.getPvpFlag() > 0)
			{
				playerInstance.sendMessage("You can't do that in combat!!!");
			}
			else
			{
				playerInstance.setCurrentCp(playerInstance.getMaxCp());
				playerInstance.setCurrentHp(playerInstance.getMaxHp());
				playerInstance.setCurrentMp(playerInstance.getMaxMp());
			}
		}
		if (command.startsWith("npc_buffer_cancel"))
		{
			if (!playerInstance.getFloodProtectors().getBuffer().tryPerformAction("npc_buffer_cancel"))
			{
				playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (playerInstance.getCurrentHp() == 0 || playerInstance.getPvpFlag() > 0)
			{
				playerInstance.sendMessage("You can't do that!!!");
			}
			else
			{
				removeAllBuffs(playerInstance);
			}
		}
		if (command.startsWith("npc_buffer_buff"))
		{
			if (!playerInstance.getFloodProtectors().getBuffer().tryPerformAction("npc_buffer_buff"))
			{
				playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			String[] params = command.split(" ");
			int skillId = Integer.parseInt(params[1]);
			int[] skillInfos = NpcBufferSkillIdsTable.getInstance().getSkillInfo(npcId, skillId);
			if (skillInfos == null)
			{
				System.out.println("NpcBuffer warning(" + npcId + " at " + getX() + ", " + getY() + ", " + getZ() + "): Player " + playerInstance.getName() + " tried to use skill(" + skillId + ") not assigned to npc buffer!");
				return;
			}
			int skillLevel = skillInfos[0];
			int skillFeeId = skillInfos[1];
			int skillFeeAmount = skillInfos[2];
			if (skillFeeId != 0) // take some item?
			{
				if (skillFeeAmount == 0)
				{
					System.out.println("NpcBuffer warning(" + npcId + " at " + getX() + ", " + getY() + ", " + getZ() + "): Fee amount of skill(" + skillId + ") fee id(" + skillFeeId + ") is 0!");
					return;
				}
				L2ItemInstance itemInstance = playerInstance.getInventory().getItemByItemId(skillFeeId);
				if (itemInstance == null || !itemInstance.isStackable() && playerInstance.getInventory().getInventoryItemCount(skillFeeId, -1) < skillFeeAmount)
				{
					playerInstance.sendMessage("You do not have enought items!");
					return;
				}
				if (itemInstance.isStackable())
				{
					if (!playerInstance.destroyItemByItemId("Npc Buffer", skillFeeId, skillFeeAmount, playerInstance.getTarget(), true))
					{
						playerInstance.sendMessage("You do not have enought items!");
						return;
					}
				}
				else
				{
					for (int i = 0; i < skillFeeAmount; i++) {
						playerInstance.destroyItemByItemId("Npc Buffer", skillFeeId, 1, playerInstance.getTarget(), true);
					}
				}
			}
			L2Skill skill;
			skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			skill.getEffects(playerInstance, playerInstance);
		}
		showChatWindow(playerInstance, 0);
	}

	@SuppressWarnings("null")
	private void removeAllBuffs(L2PcInstance player)
	{
		if (player != null)
		{
			player.stopAllEffects();
			player.sendMessage("Your buffs were removed.");
		}
		else
		{
			player.sendMessage("Your buffs could not be removed.");
		}
	}
}