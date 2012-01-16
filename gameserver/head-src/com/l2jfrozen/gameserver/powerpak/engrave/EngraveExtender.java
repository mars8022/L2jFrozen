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
package com.l2jfrozen.gameserver.powerpak.engrave;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.extender.BaseExtender;
import com.l2jfrozen.gameserver.powerpak.PowerPakConfig;

/**
 * @author L2JFrozen
 */
public class EngraveExtender extends BaseExtender
{
	private L2ItemInstance _item;

	public static boolean canCreateFor(L2Object object)
	{
		if(EngraveManager.getInstance().isEngraved(object.getObjectId()))
			return true;
		return false;
	}

	public EngraveExtender(L2Object owner)
	{
		super(owner);
		_item = (L2ItemInstance) owner;
	}

	@Override
	public Object onEvent(final String event, Object... params)
	{
		if(event.compareTo(BaseExtender.EventType.SETOWNER.name) == 0)
		{
			L2Character reference = (L2Character) L2World.getInstance().findObject((Integer) params[1]);
			L2Character owner = (L2Character) L2World.getInstance().findObject(_item.getOwnerId());
			EngraveManager.getInstance().logAction(_item, reference, owner, params[0].toString());
		}
		else if(event.compareTo("DESTROY") == 0 || event.compareTo("CRYSTALLIZE") == 0 || event.compareTo("MULTISELL") == 0)
		{
			L2PcInstance owner = (L2PcInstance) L2World.getInstance().findObject(_item.getOwnerId());
			if(EngraveManager.getInstance().getEngraver(_item.getObjectId()) != _item.getOwnerId())
			{
				if(!PowerPakConfig.ENGRAVE_ALLOW_DESTROY)
				{
					if(owner != null)
					{
						owner.sendMessage("You can not destroy the object you are not engraved.");
					}
					return true;
				}
			}
			EngraveManager.getInstance().logAction(_item, owner, null, "Destroy");
		}

		return super.onEvent(event, params);
	}

}
