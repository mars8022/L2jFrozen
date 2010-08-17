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
package interlude.loginserver;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import interlude.loginserver.serverpackets.Init;

import interlude.netcore.IAcceptFilter;
import interlude.netcore.IClientFactory;
import interlude.netcore.IMMOExecutor;
import interlude.netcore.MMOConnection;
import interlude.netcore.ReceivablePacket;

/**
 * @author KenM
 */
public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	private ThreadPoolExecutor _generalPacketsThreadPool;

	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	/**
	 * @see interlude.netcore.IMMOExecutor#execute(interlude.netcore.ReceivablePacket)
	 */
	public void execute(ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}

	/**
	 * @see interlude.netcore.IClientFactory#create(interlude.netcore.MMOConnection)
	 */
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}

	/**
	 * @see interlude.netcore.IAcceptFilter#accept(java.nio.channels.SocketChannel)
	 */
	public boolean accept(SocketChannel sc)
	{
		return !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}
}
