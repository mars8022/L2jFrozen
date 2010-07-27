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
package interlude.gameserver.model.zone.type;

import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.actor.instance.L2SummonInstance;
import interlude.gameserver.model.zone.L2ZoneType;

public class L2SkillZone extends L2ZoneType
{
	public L2SkillZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("skillId")) {
			_skillId = Integer.parseInt(value);
		} else if (name.equals("skillLvl")) {
			_skillLvl = Integer.parseInt(value);
		} else if (name.equals("onSiege")) {
			_onSiege = Boolean.parseBoolean(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if ((character instanceof L2PcInstance || character instanceof L2SummonInstance) && (!_onSiege || _onSiege && character.isInsideZone(4)))
		{
			if (character instanceof L2PcInstance) {
				((L2PcInstance) character).enterDangerArea();
			}
			SkillTable.getInstance().getInfo(_skillId, _skillLvl).getEffects(character, character);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance || character instanceof L2SummonInstance)
		{
			character.stopSkillEffects(_skillId);
			if (character instanceof L2PcInstance) {
				((L2PcInstance) character).exitDangerArea();
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		onExit(character);
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}

	private int _skillId;
	private int _skillLvl;
	private boolean _onSiege;
}