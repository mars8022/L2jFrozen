/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddc
	// ddddddddddddddddddffffdddcccccSSddd dddddccffd
	
	/** The Constant _S__22_NPCINFO. */
	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	
	/** The _active char. */
	private L2Character _activeChar;
	
	/** The _heading. */
	private int _x, _y, _z, _heading;
	
	/** The _id template. */
	private int _idTemplate;
	
	/** The _is summoned. */
	private boolean _isAttackable, _isSummoned;
	
	/** The _p atk spd. */
	private int _mAtkSpd, _pAtkSpd;
	
	/** The _fly walk spd. */
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	
	/** The _lhand. */
	private int _rhand, _lhand;
	
	/** The _collision radius. */
	private int _collisionHeight, _collisionRadius;
	
	/** The _name. */
	private String _name = "";
	
	/** The _title. */
	private String _title = "";
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(final L2NpcInstance cha, final L2Character attacker)
	{
		/*
		 * if(cha.getMxcPoly() != null) { attacker.sendPacket(new MxCPolyInfo(cha)); return; }
		 */
		if (cha.getCustomNpcInstance() != null)
		{
			attacker.sendPacket(new CustomNpcInfo(cha));
			attacker.broadcastPacket(new FinishRotation(cha));
			return;
		}
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_isSummoned = false;
		_collisionHeight = cha.getCollisionHeight();
		_collisionRadius = cha.getCollisionRadius();
		if (cha.getTemplate().serverSideName)
		{
			_name = cha.getTemplate().name;
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			_title = Config.L2JMOD_CHAMP_TITLE;
		}
		else if (cha.getTemplate().serverSideTitle)
		{
			_title = cha.getTemplate().title;
		}
		else
		{
			_title = cha.getTitle();
		}
		
		if (Config.SHOW_NPC_LVL && _activeChar instanceof L2MonsterInstance)
		{
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (_title != null)
			{
				t += " " + _title;
			}
			
			_title = t;
		}
		
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(final L2Summon cha, final L2Character attacker)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker); // (cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = cha.isShowSummonAnimation();
		_collisionHeight = _activeChar.getTemplate().collisionHeight;
		_collisionRadius = _activeChar.getTemplate().collisionRadius;
		if (cha.getTemplate().serverSideName || cha instanceof L2PetInstance || cha instanceof L2SummonInstance)
		{
			_name = _activeChar.getName();
			_title = cha.getTitle();
		}
		
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;
		
		if (_activeChar instanceof L2Summon)
			if (((L2Summon) _activeChar).getOwner() != null && ((L2Summon) _activeChar).getOwner().getAppearance().getInvisible())
				return;
		writeC(0x16);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate + 1000000); // npctype id
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/* 0x32 */); // swimspeed
		writeD(_swimWalkSpd/* 0x32 */); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1.1/* _activeChar.getProperMultiplier() */);
		// writeF(1/*_activeChar.getAttackSpeedMultiplier()*/);
		writeF(_pAtkSpd / 277.478340719);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		
		if (_activeChar instanceof L2Summon)
		{
			writeD(0x01);// Title color 0=client default
			writeD(((L2Summon) _activeChar).getPvpFlag());
			writeD(((L2Summon) _activeChar).getKarma());
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}
		
		writeD(_activeChar.getAbnormalEffect()); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
		
		writeC(0x00); // C3 team circle 1-blue, 2-red
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(0x00); // C4
		writeD(0x00); // C6
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}
