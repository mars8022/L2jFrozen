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
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;

	@SuppressWarnings("unused")
	private int _originX;

	@SuppressWarnings("unused")
	private int _originY;

	@SuppressWarnings("unused")
	private int _originZ;

	@SuppressWarnings("unused")
	private int _attackId;

	private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click   1 for shift-click
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		// avoid using expensive operations if not needed
		L2Object target;

		if(activeChar.getTargetId() == _objectId)
		{
			target = activeChar.getTarget();
		}
		else
		{
			target = L2World.getInstance().findObject(_objectId);
		}

		if(target == null)
			return;

		
		//during teleport phase, players cant do any attack
		if((TvT.is_teleport() && activeChar._inEventTvT) || (CTF.is_teleport() && activeChar._inEventCTF) || (DM.is_teleport() && activeChar._inEventDM)){
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
		}
		
		//No attacks to same team in Event
		if (TvT.is_started())
	    {
			if(target instanceof L2PcInstance){
	            if ((activeChar._inEventTvT && ((L2PcInstance)target)._inEventTvT) && activeChar._teamNameTvT.equals(((L2PcInstance)target)._teamNameTvT))
	            {
	            	activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	                return;
	            }
			}else if(target instanceof L2SummonInstance){
				if ((activeChar._inEventTvT && ((L2SummonInstance)target).getOwner()._inEventTvT) && activeChar._teamNameTvT.equals(((L2SummonInstance)target).getOwner()._teamNameTvT))
	            {
	            	activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	                return;
	            }
			}
        }
		
		//No attacks to same team in Event
		if (CTF.is_started())
	    {
			if(target instanceof L2PcInstance){
	            if ((activeChar._inEventCTF && ((L2PcInstance)target)._inEventCTF) && activeChar._teamNameCTF.equals(((L2PcInstance)target)._teamNameCTF))
	            {
	            	activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	                return;
	            }
			}else if(target instanceof L2SummonInstance){
				if ((activeChar._inEventCTF && ((L2SummonInstance)target).getOwner()._inEventCTF) && activeChar._teamNameCTF.equals(((L2SummonInstance)target).getOwner()._teamNameCTF))
	            {
	            	activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	                return;
	            }
			}
        }
		
		if(activeChar.getTarget() != target)
		{
			target.onAction(activeChar);
		}
		else
		{
			if(target.getObjectId() != activeChar.getObjectId() & activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null)
			{
				//_log.config("Starting ForcedAttack");
				target.onForcedAttack(activeChar);
				//_log.config("Ending ForcedAttack");
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}
