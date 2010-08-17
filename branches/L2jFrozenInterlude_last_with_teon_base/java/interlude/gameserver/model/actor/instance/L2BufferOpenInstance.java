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

import java.util.logging.Logger;

import javolution.util.FastList;
import interlude.Config;
import interlude.gameserver.datatables.BuffTemplateTable;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2Skill.SkillTargetType;
import interlude.gameserver.model.L2Skill.SkillType;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.templates.L2BuffTemplate;

public class L2BufferOpenInstance
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
		getbufferType(efector).setTarget(player);
		FastList<L2BuffTemplate> _templateBuffs = new FastList<L2BuffTemplate>();
		_templateBuffs = BuffTemplateTable.getInstance().getBuffTemplate(_templateId);
		if (_templateBuffs == null || _templateBuffs.size() == 0) {
			return;
		}
		int _priceTotal = 0;
		int _pricePoints = 0;
		for (L2BuffTemplate _buff : _templateBuffs)
		{
			if (paymentRequired)
			{
				if (!_buff.checkPrice(player))
				{
					player.sendMessage("Not enough Adena");
					return;
				}
				if (!_buff.checkPoints(player))
				{
					player.sendMessage("Not enough Event Points");
					return;
				}
			}
			getbufferType(efector).setTarget(player);
			if (_buff.checkPlayer(player) && _buff.checkPrice(player))
			{
				if (player.getInventory().getAdena() >= _priceTotal + _buff.getAdenaPrice() && player.getEventPoints() >= _buff.getPointsPrice())
				{
					_priceTotal += _buff.getAdenaPrice();
					_pricePoints += _buff.getPointsPrice();
					if (_buff.forceCast() || player.getFirstEffect(_buff.getSkill()) == null)
					{
						// regeneration ^^
						player.setCurrentHpMp(player.getMaxHp() + 5000, player.getMaxMp() + 5000);
						/*
						 * Mensaje informativo al cliente sobre los buffs dados.
						 */
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
						for (L2Effect effect : _buff.getSkill().getEffects(getbufferType(efector), player))
						{
							player.addEffect(effect);
						}
						try
						{
							Thread.sleep(50);
						}
						catch (Exception e)
						{
						}
					}
				}
			}
		}
		if (paymentRequired && (_pricePoints > 0 || _priceTotal > 0))
		{
			if (_pricePoints > 0)
			{
				int previousPoints = player.getEventPoints();
				player.setEventPoints(player.getEventPoints() - _pricePoints);
				player.sendMessage("You had " + previousPoints + " Event Points, and now you have " + player.getEventPoints() + " Event Points.");
			}
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