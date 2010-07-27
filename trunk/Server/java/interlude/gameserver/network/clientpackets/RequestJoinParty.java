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
package interlude.gameserver.network.clientpackets;

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.model.BlockList;
import interlude.gameserver.model.L2Party;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.AskJoinParty;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 29 42 00 00 10 01 00 00 00 format cdd
 *
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestJoinParty extends L2GameClientPacket
{
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
	private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	private String _name;
	private int _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);
		if (requestor == null) {
			return;
		}
		if (target == null)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		if (target.isInParty())
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		if (BlockList.isBlocked(target, requestor))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		if (target == requestor)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (target.isCursedWeaponEquiped() || requestor.isCursedWeaponEquiped())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (target.isInJail() || requestor.isInJail())
		{
			SystemMessage sm = SystemMessage.sendString("Player is in Jail");
			requestor.sendPacket(sm);
			return;
		}
		// Protect Again Same Factions Koofs or Noobs
		if (target.isKoof() && requestor.isNoob())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		// Protect Again Same Factions Koofs or Noobs
		if (target.isNoob() && requestor.isKoof())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
                 
	        if (target.getClient().isDetached()){ 
            requestor.sendMessage("Player is in offline mode."); 
	        return; 
	    } 
	                 
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode()) {
			return;
		}
		if (target.isInDuel() || requestor.isInDuel()) {
			return;
		}
		if (!requestor.isInParty()) // Asker has no party
		{
			createNewParty(target, requestor);
		}
		else
		// Asker is in party
		{
			if (requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
			}
			else
			{
				addTargetToParty(target, requestor);
			}
		}
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;
		// summary of ppl already in party and ppl that get invitation
		if (requestor.getParty().getMemberCount() + requestor.getParty().getPendingInvitationNumber() >= 9)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEADER_CAN_INVITE));
			return;
		}
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().increasePendingInvitationNumber();
			if (Config.DEBUG) {
				_log.fine("sent out a party invitation to:" + target.getName());
			}
			msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			requestor.sendPacket(msg);
			if (Config.DEBUG) {
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
		msg = null;
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().increasePendingInvitationNumber();
			if (Config.DEBUG) {
				_log.fine("sent out a party invitation to:" + target.getName());
			}
			msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			if (Config.DEBUG) {
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
