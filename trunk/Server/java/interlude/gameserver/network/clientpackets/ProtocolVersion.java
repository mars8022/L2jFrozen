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
import interlude.gameserver.network.serverpackets.KeyPacket;
import interlude.gameserver.network.serverpackets.SendStatus;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 * @reworked Z3r0C00L
 */
public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
	private int _version;

	@Override
	protected void readImpl()
	{
		_version = readD();
	}

	@Override
	protected void runImpl()
	{
		// this packet is never encrypted
		if (_version == 65534 || _version == -2)
		{
			if (Config.DEBUG) 
			{
				_log.info("Ping received");
			}
			// this is just a ping attempt from the new C2 client
			getClient().getConnection().close(null);
		}
		else if(_version == 65533 || _version == -3) //RWHO
		{
			if(Config.RWHO_LOG)
			{
				_log.info(getClient().toString() + " RWHO received");
			}
			getClient().getConnection().close(new SendStatus());
		}
			
		else if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.info("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			_log.warning("Wrong Protocol Version " + _version);
			getClient().getConnection().close(null);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.fine("Client Protocol Revision is ok: " + _version);
			}
			KeyPacket pk = new KeyPacket(getClient().enableCrypt());
			getClient().sendPacket(pk);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}