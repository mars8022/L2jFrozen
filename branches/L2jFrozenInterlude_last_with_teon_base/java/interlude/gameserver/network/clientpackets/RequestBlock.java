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
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

public final class RequestBlock extends L2GameClientPacket
{
	private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
	private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;
	private String _name;
	private int _type;
	private L2PcInstance _target;

	@Override
	protected void readImpl()
	{
		_type = readD(); // 0x00 - block, 0x01 - unblock, 0x03 -
		// allblock, 0x04 - allunblock
		if (_type == BLOCK || _type == UNBLOCK)
		{
			_name = readS();
			_target = L2World.getInstance().getPlayer(_name);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		switch (_type)
		{
			case BLOCK:
				if (_target == null)
				{
					int acl = 0;
					try
					{
						acl = BlockList.getOfflineCharacterACL(_name);
					}
					catch (IllegalArgumentException iae)
					{
						// Incorrect player name.
						activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
						return;
					}
					if (acl >= Config.GM_MIN)
					{
						// Cannot block a GM character.
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM));
						return;
					}
					BlockList.addToBlockList(activeChar, _name);
				}
				else
				{
					if (_target.isGM())
					{
						// Cannot block a GM character.
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM));
						return;
					}
					BlockList.addToBlockList(activeChar, _target);
				}
				break;
			case UNBLOCK:
				if (_target == null)
				{
					if (BlockList.isInBlockList(activeChar, _name))
					{
						BlockList.removeFromBlockList(activeChar, _name);
					}
				}
				else
				{
					if (BlockList.isInBlockList(activeChar, _target))
					{
						BlockList.removeFromBlockList(activeChar, _target);
					}
				}
				break;
			case BLOCKLIST:
				BlockList.sendListToOwner(activeChar);
				break;
			case ALLBLOCK:
				if (!activeChar.getMessageRefusal())
				{
					activeChar.setMessageRefusal(true);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
				}
				break;
			case ALLUNBLOCK:
				if (activeChar.getMessageRefusal())
				{
					activeChar.setMessageRefusal(false);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
				}
				break;
			default:
				_log.info("Unknown 0x0a block type: " + _type);
		}
	}

	@Override
	public String getType()
	{
		return _C__A0_REQUESTBLOCK;
	}
}
