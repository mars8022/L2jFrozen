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
package com.l2scoria.gameserver.powerpak.Buffer;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.powerpak.Buffer.BuffTable.Buff;

import javolution.util.FastList;

public class L2BufferInstance
{
	static L2PcInstance selfBuffer;
	static L2NpcInstance npcBuffer;

	/**
	 * Apply Buffs onto a player.
	 *
	 * @param player
	 * @param _templateId
	 * @param efector
	 * @param paymentRequired
	 */
	public static void makeBuffs(L2PcInstance player, int _templateId, L2Object efector, boolean paymentRequired)
	{
		if (player == null) {
			return;
		}
		
		L2NpcInstance buffer = null;
		if(player.getTarget()!=null)
			if(player.getTarget() instanceof L2NpcInstance)
			{
				buffer = (L2NpcInstance) getbufferType(efector);
			}
		//getbufferType(efector).setTarget(player);
		
		buffer.setTarget(player);
		
		ArrayList<Buff> _templateBuffs = new ArrayList<Buff>();
		_templateBuffs = BuffTable.getInstance().getBuffsForID(_templateId);
		if (_templateBuffs == null || _templateBuffs.size() == 0) {
			return;
		}
		int _priceTotal = 0;
		for (Buff _buff : _templateBuffs)
		{
			if (paymentRequired)
			{
				if (!_buff.checkPrice(player))
				{
					player.sendMessage("Not enough Adena");
					return;
				}else{
					
					_priceTotal += _buff._price;
					
				}
				
			}
			
			if (_buff._force || player.getFirstEffect(_buff._skill) == null)
			{
				buffer.setBusy(true);
				//MagicSkillUser msu = new MagicSkillUser(buffer, player, _buff.getSkill().getId(), _buff.getSkill().getLevel(), 100, 0);
				//player.broadcastPacket(msu);
				buffer.setCurrentMp(buffer.getMaxMp());
				buffer.setTarget(player);
				buffer.doCast(_buff._skill);
				buffer.setBusy(false);
			} else{
				_buff._skill.getEffects(player, player);
			}
			/*
			if (_buff.checkPrice(player))
			{
				if (player.getInventory().getAdena() >= _priceTotal + _buff._price)
				{
					_priceTotal += _buff._price;
					
					//L2Skill skill = SkillTable.getInstance().getInfo(_buff._skillId, _buff._skillLevel);
					//if(skill!=null){
					
						
						
						
						/*
						// regeneration ^^
						player.setCurrentHpMp(player.getMaxHp() + 5000, player.getMaxMp() + 5000);
						
						//Mensaje informativo al cliente sobre los buffs dados.
						 
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(_buff.getSkill().getId());
						player.sendPacket(sm);
						sm = null;
						if (_buff.getSkill().getTargetType() == SkillTargetType.TARGET_SELF)
						{
							// Ignora el tiempo de casteo del skill, hay
							// unos
							// 100ms de animacion de casteo
							MagicSkillUser msu = new MagicSkillUser(player, player, _buff.getSkill().getId(), _buff.getSkill().getLevel(), 100, 0);
							player.broadcastPacket(msu);
							for (L2Effect effect : _buff.getSkill().getEffectsSelf(player))
							{
								player.addEffect(effect);
							}
							// newbie summons
							if (_buff.getSkill().getSkillType() == SkillType.SUMMON)
							{
								player.doCast(_buff.getSkill());
							}
						}
						else
						{ // Ignora el tiempo de casteo del skill,
							// hay unos
							// 5ms de animacion de casteo
							MagicSkillUser msu = new MagicSkillUser(getbufferType(efector), player, _buff.getSkill().getId(), _buff.getSkill().getLevel(), 5, 0);
							player.broadcastPacket(msu);
						}
						
						_buff.getSkill().getEffects(getbufferType(efector), player);
						//System.out.println("**** "+;
						
						for (L2Effect effect : _buff.getSkill().getEffects(getbufferType(efector), player))
						{
							player.addEffect(effect);
						}
						*/
						/*try
						{
							Thread.sleep(50);
						}
						catch (Exception e)
						{
						}*/
					//}
			//	}
			//}
		}
		if (paymentRequired && (_priceTotal > 0))
		{
			if (_priceTotal > 0) {
				player.reduceAdena("NpcBuffer", _priceTotal, player.getLastFolkNPC(), true);
			}
		}
	}

	private static L2Character getbufferType(L2Object efector)
	{
		if (efector instanceof L2PcInstance)
		{
			selfBuffer = (L2PcInstance) efector;
			efector = selfBuffer;
		}
		if (efector instanceof L2NpcInstance)
		{
			npcBuffer = (L2NpcInstance) efector;
			efector = npcBuffer;
		}
		return (L2Character) efector;
	}

	static Logger _log = Logger.getLogger(Config.class.getName());
}