/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.Point3D;

/**
 * Fromat:(ch) dddddc
 */
public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private static final String _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND = "[C] D0:2F RequestExMagicSkillUseGround";

	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private int _ctrlPressed;
	private int _shiftPressed;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_skillId = readD();
		_ctrlPressed = readD();
		_shiftPressed = readC();
	}

	/**
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Get the level of the used skill
		int level = activeChar.getSkillLevel(_skillId);
		if(level <= 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the L2Skill template corresponding to the skillID received from the client
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

		if(skill != null)
		{
			activeChar.setCurrentSkillWorldPosition(new Point3D(_x, _y, _z));

			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x , _y));
			activeChar.broadcastPacket(new ValidateLocation(activeChar));
			activeChar.useMagic(skill, _ctrlPressed == 1 ? true : false, _shiftPressed == 1 ? true : false);
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * @see com.l2jfrozen.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND;
	}
}
