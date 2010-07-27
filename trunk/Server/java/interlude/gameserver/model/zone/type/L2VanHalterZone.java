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

import interlude.gameserver.instancemanager.VanHalterManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.zone.L2ZoneType;

/**
 * @author DaRkRaGe
 */
public class L2VanHalterZone extends L2ZoneType
{
	private String _zoneName;

	public L2VanHalterZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_zoneName = value;
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
			if (player.isGM())
			{
				player.sendMessage("You entered " + _zoneName);
				return;
			}
			if (_zoneName.equalsIgnoreCase("Altar of Sacrifice")) {
				VanHalterManager.getInstance().intruderDetection((L2PcInstance) character);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
			if (player.isGM())
			{
				player.sendMessage("You left " + _zoneName);
				return;
			}
		}
	}

	public String getZoneName()
	{
		return _zoneName;
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
