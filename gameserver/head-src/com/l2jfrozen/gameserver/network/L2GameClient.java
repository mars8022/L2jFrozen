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
package com.l2jfrozen.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.AwayManager;
import com.l2jfrozen.gameserver.model.CharSelectInfoPackage;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.event.VIP;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;
import com.l2jfrozen.gameserver.thread.daemons.AutoSave;
import com.l2jfrozen.gameserver.util.EventData;
import com.l2jfrozen.gameserver.util.FloodProtector;
import com.l2jfrozen.netcore.MMOClient;
import com.l2jfrozen.netcore.MMOConnection;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author l2jfrozen dev
 */

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());

	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesnt has character attached to it yet
	 * IN_GAME - client has selected a char and is in game
	 * 
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	};

	public GameClientState state;

	// Info
	public String accountName;
	public SessionKey sessionId;
	public L2PcInstance activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();

	private boolean _isAuthedGG;
	private long _connectionStartTime;
	private List<Integer> _charSlotMapping = new FastList<Integer>();

	// Task
	protected/*final*/ScheduledFuture<?> _autoSaveInDB;
	private ScheduledFuture<?> _guardCheckTask = null;

	// Crypt
	public GameCrypt crypt;

	// Flood protection
	public byte packetsSentInSec = 0;
	public int packetsSentStartTick = 0;
	public long packetsNextSendTick = 0;

	//unknownPacket protection  
	private int unknownPacketCount = 0;
	
	private boolean _closenow = true;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		crypt = new GameCrypt();
		if(Config.AUTOSAVE_INITIAL_TIME > 0)
			_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSave(activeChar), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		_guardCheckTask = nProtect.getInstance().startTask(this);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
			public void run() {
				if(_closenow)
					close(new LeaveWorld());
			}
		}, 4000);

	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		GameCrypt.setKey(key, crypt);
		return key;
	}

	public GameClientState getState()
	{
		return state;
	}

	public void setState(GameClientState pState)
	{
		state = pState;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_closenow = false;
		GameCrypt.decrypt(buf.array(), buf.position(), size, crypt);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		GameCrypt.encrypt(buf.array(), buf.position(), size, crypt);
		buf.position(buf.position() + size);
		return true;
	}

	public L2PcInstance getActiveChar()
	{
		return activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		activeChar = pActiveChar;
		if(activeChar != null)
		{
			L2World.storeObject(getActiveChar());
		}
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public void setAccountName(String pAccountName)
	{
		accountName = pAccountName;
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setSessionId(SessionKey sk)
	{
		sessionId = sk;
	}

	public SessionKey getSessionId()
	{
		return sessionId;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if(getConnection()!=null)
			getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	/**
	 * Method to handle character deletion
	 * 
	 * @return a byte: <li>-1: Error: No char was found for such charslot, caught exception, etc... <li>0: character is
	 *         not member of any clan, proceed with deletion <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{

		int objid = getObjectIdForSlot(charslot);

		if(objid < 0)
			return -1;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();

			rs.next();

			int clanId = rs.getInt(1);

			byte answer = 0;

			if(clanId != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanId);

				if(clan == null)
				{
					answer = 0; // jeezes!
				}
				else if(clan.getLeaderId() == objid)
				{
					answer = 2;
				}
				else
				{
					answer = 1;
				}

				clan = null;
			}

			// Setting delete time
			if(answer == 0)
			{
				if(Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objid);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_Id=?");
					statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
					statement.setInt(2, objid);
					statement.execute();
					statement.close();
					rs.close();
					statement = null;
					rs = null;
				}
			}
			else
			{
				statement.close();
				rs.close();
				statement = null;
				rs = null;
			}

			return answer;
		}
		catch(Exception e)
		{
			_log.warning("Data error on update delete time of char: " + e);
			return -1;
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		/*if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.fine("active Char saved");
			this.setActiveChar(null);
		}*/

		int objid = getObjectIdForSlot(charslot);

		if(objid < 0)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.severe("Data error on restoring char: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public static void deleteCharByObjId(int objid)
	{
		if(objid < 0)
			return;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.warning("Data error on deleting char: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

		if(character != null)
		{
			//restoreInventory(character);
			//restoreSkills(character);
			//character.restoreSkills();
			//restoreShortCuts(character);
			//restoreWarehouse(character);

			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.getMasteryPenalty();
			character.sendPacket(new UserInfo(character));
			character.broadcastKarma();
			character.setOnlineStatus(true);
		}
		else
		{
			_log.severe("could not restore in slot: " + charslot);
		}

		//setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for(CharSelectInfoPackage c : chars)
		{
			int objectId = c.getObjectId();

			_charSlotMapping.add(new Integer(objectId));
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		if(getConnection()!=null)
			getConnection().close(gsp);

	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if(charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}

		Integer objectId = _charSlotMapping.get(charslot);

		return objectId.intValue();
	}

	@Override
	public void onForcedDisconnection()
	{
		_log.log(Level.WARNING, "Client " + toString() + " disconnected abnormally.");
		L2PcInstance player = getActiveChar();
		if(player != null) {
			if(player.isFlying())
				player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			
			player.deleteMe();

			try {
				player.store();
			} catch(Exception e2) {}
			L2World.getInstance().removeFromAllPlayers(player);
			setActiveChar(null);
			LoginServerThread.getInstance().sendLogout(getAccountName());
		}
		stopGuardTask();
		nProtect.getInstance().closeSession(this);
	}

	public void stopGuardTask()
	{
		if(_guardCheckTask != null)
		{
			_guardCheckTask.cancel(true);
			_guardCheckTask = null;
		}

	}

	@Override
	public void onDisconection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());

		}
		catch(RejectedExecutionException e)
		{
			// server is closing
		}
	}

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getSocketChannel().socket().getInetAddress();
			String ip = "N/A";

			if(address == null)
			{
				ip = "disconnected";
			}
			else
			{
				ip = address.getHostAddress();
			}

			switch(getState())
			{
				case CONNECTED:
					return "[IP: " + ip + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + ip + "]";
				case IN_GAME:
					address = null;
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + ip + "]";
				default:
					address = null;
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch(NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	class DisconnectTask implements Runnable
	{

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			try
			{
				// Update BBS
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				// we are going to mannually save the char bellow thus we can force the cancel
				if(_autoSaveInDB != null)
					_autoSaveInDB.cancel(true);

				L2PcInstance player = getActiveChar();
				if(player != null) // this should only happen on connection loss
				{

					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if(player.atEvent)
					{
						EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventKarma, player.eventPvpKills, player.eventPkKills, player.eventTitle, player.kills, player.eventSitForced);

						L2Event.connectionLossData.put(player.getName(), data);
						data = null;
					}else{
						
						if(player._inEventCTF){
							CTF.onDisconnect(player);
						}else if(player._inEventDM){
							DM.onDisconnect(player);
						}else if(player._inEventTvT){
							TvT.onDisconnect(player);
						}else if(player._inEventVIP){
							VIP.onDisconnect(player);
						}
						
					}

					if(player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}

					if(player.isAway())
					{
						AwayManager.getInstance().extraBack(player);
					}

					if(player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE || player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE)
					{
						player.setOffline(true);
						player.leaveParty();
						if(Config.OFFLINE_SET_NAME_COLOR)
						{
							player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							player.broadcastUserInfo();
						}
						
						if (player.getOfflineStartTime() == 0)
							player.setOfflineStartTime(System.currentTimeMillis());
						
						return;
					}

					// notify the world about our disconnect
					player.deleteMe();
					player.store();

					try
					{
						player.store();
					}
					catch(Exception e2)
					{
						/* ignore any problems here */
					}
				}

				setActiveChar(null);

				player = null;
			}
			catch(Exception e1)
			{
				_log.log(Level.WARNING, "error while disconnecting client", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}

	//TODO
	public boolean checkUnknownPackets()
	{
		if(getActiveChar() != null && !FloodProtector.getInstance().tryPerformAction(getActiveChar().getObjectId(), FloodProtector.PROTECTED_UNKNOWNPACKET))
		{
			unknownPacketCount++;
			
			if(unknownPacketCount >= Config.MAX_UNKNOWN_PACKETS)
				return true;
			else
				return false;
		}
		else
		{
			unknownPacketCount = 0;
			return false;
		}
	}
}
