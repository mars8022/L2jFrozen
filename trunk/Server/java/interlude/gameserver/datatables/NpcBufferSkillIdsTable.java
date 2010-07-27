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
package interlude.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;
import interlude.L2DatabaseFactory;

public class NpcBufferSkillIdsTable
{
	private static NpcBufferSkillIdsTable _instance = null;
	private Map<Integer, NpcBufferSkills> _buffers = new FastMap<Integer, NpcBufferSkills>();

	private NpcBufferSkillIdsTable()
	{
		Connection con = null;
		int skillCount = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT `npc_id`,`skill_id`,`skill_level`,`skill_fee_id`,`skill_fee_amount` FROM `npc_buffer` ORDER BY `npc_id` ASC");
			ResultSet rset = statement.executeQuery();
			int lastNpcId = 0;
			NpcBufferSkills skills = null;
			while (rset.next())
			{
				int npcId = rset.getInt("npc_id");
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				int skillFeeId = rset.getInt("skill_fee_id");
				int skillFeeAmount = rset.getInt("skill_fee_amount");
				if (npcId != lastNpcId)
				{
					if (lastNpcId != 0) {
						_buffers.put(lastNpcId, skills);
					}
					skills = new NpcBufferSkills(npcId);
					skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount);
				} else {
					skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount);
				}
				lastNpcId = npcId;
				skillCount++;
			}
			_buffers.put(lastNpcId, skills);
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("NpcBufferSkillIdsTable: Error reading npc_buffer_skill_ids table: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		System.out.println("NpcBufferSkillIdsTable: Loaded " + _buffers.size() + " buffers and " + skillCount + " skills.");
	}

	public static NpcBufferSkillIdsTable getInstance()
	{
		if (_instance == null) {
			_instance = new NpcBufferSkillIdsTable();
		}
		return _instance;
	}

	/** Reloads npc buffer **/
	public static void reload()
	{
		_instance = new NpcBufferSkillIdsTable();
	}

	public int[] getSkillInfo(int npcId, int skillId)
	{
		NpcBufferSkills skills = _buffers.get(npcId);
		if (skills == null) {
			return null;
		}
		return skills.getSkillInfo(skillId);
	}
}
