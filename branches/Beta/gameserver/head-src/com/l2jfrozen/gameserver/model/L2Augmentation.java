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

package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.xml.AugmentationData;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.funcs.FuncAdd;
import com.l2jfrozen.gameserver.skills.funcs.LambdaConst;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Used to store an augmentation and its boni
 * 
 * @author durgus
 */
public final class L2Augmentation
{
	private static final Logger _log = Logger.getLogger(L2Augmentation.class.getName());

	private L2ItemInstance _item;
	private int _effectsId = 0;
	private augmentationStatBoni _boni = null;
	private L2Skill _skill = null;

	public L2Augmentation(L2ItemInstance item, int effects, L2Skill skill, boolean save)
	{
		_item = item;
		_effectsId = effects;
		_boni = new augmentationStatBoni(_effectsId);
		_skill = skill;

		// write to DB if save is true
		if(save)
		{
			saveAugmentationData();
		}
	}

	public L2Augmentation(L2ItemInstance item, int effects, int skill, int skillLevel, boolean save)
	{
		this(item, effects, SkillTable.getInstance().getInfo(skill, skillLevel), save);
	}

	// =========================================================
	// Nested Class

	public class augmentationStatBoni
	{
		private Stats _stats[];
		private float _values[];
		private boolean _active;

		public augmentationStatBoni(int augmentationId)
		{
			_active = false;
			FastList<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);

			_stats = new Stats[as.size()];
			_values = new float[as.size()];

			int i = 0;
			for(AugmentationData.AugStat aStat : as)
			{
				_stats[i] = aStat.getStat();
				_values[i] = aStat.getValue();
				i++;
			}

			as = null;
		}

		public void applyBoni(L2PcInstance player)
		{
			// make sure the boni are not applyed twice..
			if(_active)
				return;

			for(int i = 0; i < _stats.length; i++)
			{
				player.addStatFunc(new FuncAdd(_stats[i], 0x40, this, new LambdaConst(_values[i])));
			}

			_active = true;
		}

		public void removeBoni(L2PcInstance player)
		{
			// make sure the boni is not removed twice
			if(!_active)
				return;

			player.removeStatsOwner(this);

			_active = false;
		}
	}

	private void saveAugmentationData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);

			PreparedStatement statement = con.prepareStatement("INSERT INTO augmentations (item_id,attributes,skill,level) VALUES (?,?,?,?)");
			statement.setInt(1, _item.getObjectId());
			statement.setInt(2, _effectsId);

			if(_skill != null)
			{
				statement.setInt(3, _skill.getId());
				statement.setInt(4, _skill.getLevel());
			}
			else
			{
				statement.setInt(3, 0);
				statement.setInt(4, 0);
			}

			statement.executeUpdate();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not save augmentation for item: " + _item.getObjectId() + " from DB:", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	public void deleteAugmentationData()
	{
		if(!_item.isAugmented())
			return;

		// delete the augmentation from the database
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id=?");
			statement.setInt(1, _item.getObjectId());
			statement.executeUpdate();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete augmentation for item: " + _item.getObjectId() + " from DB:", e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	/**
	 * Get the augmentation "id" used in serverpackets.
	 * 
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return _effectsId;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	/**
	 * Applys the boni to the player.
	 * 
	 * @param player
	 */
	public void applyBoni(L2PcInstance player)
	{
		_boni.applyBoni(player);

		// add the skill if any
		if(_skill != null)
		{
			
			player.addSkill(_skill);
			
			if(_skill.isActive() && Config.ACTIVE_AUGMENTS_START_REUSE_TIME>0){
				player.disableSkill(_skill.getId(), Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
				player.addTimeStamp(_skill.getId(), Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
			}
			
			player.sendSkillList();
		}
	}

	/**
	 * Removes the augmentation boni from the player.
	 * 
	 * @param player
	 */
	public void removeBoni(L2PcInstance player)
	{
		_boni.removeBoni(player);

		// remove the skill if any
		if(_skill != null)
		{
			if(_skill.isPassive())
			{
				player.removeSkill(_skill);
			}
			else
			{
				player.removeSkill(_skill, false);
			}
			
			if((_skill.isPassive() && Config.DELETE_AUGM_PASSIVE_ON_CHANGE) || (_skill.isActive() && Config.DELETE_AUGM_ACTIVE_ON_CHANGE)){
				
				// Iterate through all effects currently on the character.
				final L2Effect[] effects = player.getAllEffects();
				
				for(L2Effect currenteffect : effects)
				{
					L2Skill effectSkill = currenteffect.getSkill();

					if(effectSkill.getId() == _skill.getId())
					{
						player.sendMessage("You feel the power of " + effectSkill.getName() + " leaving yourself.");
						currenteffect.exit(false);
					}
				}
				
			}

			player.sendSkillList();

			
		}
	}
}
