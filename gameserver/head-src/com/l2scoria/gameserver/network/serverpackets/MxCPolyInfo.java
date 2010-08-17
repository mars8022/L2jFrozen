/*
 * This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;

/**
 * 
 * @author Velvet
 */
public class MxCPolyInfo extends L2GameServerPacket
{
	private L2NpcInstance _activeChar;
	private int _x, _y, _z, _heading;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private float _moveMultiplier;
	private int _maxCp;

	public MxCPolyInfo(L2NpcInstance cha)
	{
		_activeChar = cha;
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_moveMultiplier = _activeChar.getMovementSpeedMultiplier();
		_runSpd = (int) (_activeChar.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_activeChar.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_maxCp = _activeChar.getMaxCp();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x03);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getMxcPoly().getName());
		writeD(_activeChar.getMxcPoly().getRace().ordinal());
		writeD(_activeChar.getMxcPoly().getSex());
		writeD(_activeChar.getMxcPoly().getClassId());
		writeD(_activeChar.getMxcPoly().getHats());
		writeD(_activeChar.getMxcPoly().getHead());
		writeD(_activeChar.getMxcPoly().getWeaponIdRH());
		writeD(_activeChar.getMxcPoly().getWeaponIdLH());
		writeD(_activeChar.getMxcPoly().getGloves());
		writeD(_activeChar.getMxcPoly().getChest());
		writeD(_activeChar.getMxcPoly().getLegs());
		writeD(_activeChar.getMxcPoly().getFeet());
		writeD(_activeChar.getMxcPoly().getHats());
		writeD(_activeChar.getMxcPoly().getWeaponIdRH());
		writeD(_activeChar.getMxcPoly().getHats());
		writeD(_activeChar.getMxcPoly().getFaces());
		write('H', 0, 24);
		writeD(_activeChar.getMxcPoly().getPvpFlag());
		writeD(_activeChar.getMxcPoly().getKarma());
		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getMxcPoly().getPvpFlag());
		writeD(_activeChar.getMxcPoly().getKarma());
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_activeChar.getMovementSpeedMultiplier());
		writeF(_activeChar.getAttackSpeedMultiplier());
		writeF(_activeChar.getCollisionRadius());
		writeF(_activeChar.getCollisionHeight());
		writeD(_activeChar.getMxcPoly().getHair());
		writeD(_activeChar.getMxcPoly().getHairColor());
		writeD(_activeChar.getMxcPoly().getFace());
		writeS(_activeChar.getMxcPoly().getTitle());
		writeD(_activeChar.getMxcPoly().getClan() != null ? _activeChar.getMxcPoly().getClan().getClanId() : 0);
		writeD(_activeChar.getMxcPoly().getClan() != null ? _activeChar.getMxcPoly().getClan().getCrestId() : 0);
		writeD(_activeChar.getMxcPoly().getClan() != null ? _activeChar.getMxcPoly().getClan().getAllyId() : 0);
		writeD(_activeChar.getMxcPoly().getClan() != null ? _activeChar.getMxcPoly().getClan().getAllyCrestId() : 0);
		writeD(0);
		writeC(1);
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		write('C',0,3);
		writeH(0);
		writeC(0x00);
		writeD(_activeChar.getAbnormalEffect());
		writeC(0);
		writeH(_activeChar.getMxcPoly().getRecom());
		writeD(_activeChar.getMxcPoly().getClassId());
		writeD(_maxCp);
		writeD((int) _activeChar.getCurrentCp());
		writeC(_activeChar.getMxcPoly().getWeaponIdEnc());
		writeC(0x00);
		writeD(_activeChar.getMxcPoly().getClan() != null ? _activeChar.getMxcPoly().getClan().getCrestLargeId() : 0);
		writeC(0);
		writeC(_activeChar.getMxcPoly().getIsHero());
		writeC(0);
		write('D',0,3);
		writeD(_activeChar.getMxcPoly().getNameColor());
		writeD(0x00);
		writeD(_activeChar.getMxcPoly().getPledge());
		writeD(0x00);
		writeD(_activeChar.getMxcPoly().getTitleColor());
		writeD(0x00);
	}

	private final void write(char type, int value, int times)
	{
		for(int i = 0; i < times; i++)
		{
			switch(type)
			{
				case 'C':
					writeC(value);
					break;
				case 'D':
					writeD(value);
					break;
				case 'F':
					writeF(value);
					break;
				case 'H':
					writeH(value);
					break;
			}
		}
	}

	@Override
	public String getType()
	{
		return "MxCPolyInfo: 0x03";
	}
}
