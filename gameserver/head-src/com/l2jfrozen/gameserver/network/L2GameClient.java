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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.OfflineTradeTable;
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
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.ServerClose;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.EventData;
import com.l2jfrozen.gameserver.util.FloodProtectors;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.netcore.MMOClient;
import com.l2jfrozen.netcore.MMOConnection;
import com.l2jfrozen.netcore.ReceivablePacket;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author L2JFrozen dev
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());

	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesn't has character attached to it yet
	 * IN_GAME - client has selected a char and is in game
	 * 
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}

	// floodprotectors
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);

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
	private ScheduledFuture<?> _guardCheckTask = null;

	protected ScheduledFuture<?> _cleanupTask = null;
	
	private ClientStats _stats;
	
	// Crypt
	public GameCrypt crypt;

	// Flood protection
	public long packetsNextSendTick = 0;

	//unknownPacket protection  
	private int unknownPacketCount = 0;
	
	protected boolean _closenow = true;
	private boolean _isDetached = false;

	protected boolean _forcedToClose = false;
	
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	private ReentrantLock _queueLock = new ReentrantLock();
	
	private long _last_received_packet_action_time = 0;
	
	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		crypt = new GameCrypt();
		_stats = new ClientStats();
		_packetQueue = new ArrayBlockingQueue<ReceivablePacket<L2GameClient>>(com.l2jfrozen.netcore.Config.getInstance().CLIENT_PACKET_QUEUE_SIZE);
		
		_guardCheckTask = nProtect.getInstance().startTask(this);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
			@Override
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
		if (state != pState)
		{
			state = pState;
			_packetQueue.clear();
		}
	}

	public ClientStats getStats()
	{
		return _stats;
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
			L2World.getInstance().storeObject(getActiveChar());
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
		if (_isDetached) 
			return;
		
		if(getConnection()!=null){
			
			if(Config.DEBUG_PACKETS){
				
				Log.add("[ServerPacket] SendingGameServerPacket, Client: "+this.toString()+" Packet:"+gsp.getType(), "GameServerPacketsLog");
				
			}
			
			getConnection().sendPacket(gsp);
			gsp.runImpl();
		}
	}

	public boolean isDetached()
	{
		return _isDetached;
	}
		
	public void setDetached(boolean b)
	{
		_isDetached = b;
	}
	
	/**
	 * Method to handle character deletion
	 * @param charslot 
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
		byte answer = -1;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();

			rs.next();

			int clanId = rs.getInt(1);

			answer = 0;
			
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

			
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("Data error on update delete time of char: " + e);
			
			answer = -1;
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		return answer;
	}

	public void markRestoredChar(int charslot)
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
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.severe("Data error on restoring char: " + e);
		}
		finally
		{
			CloseUtil.close(con);
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
			con = L2DatabaseFactory.getInstance().getConnection(false);
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

			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
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
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("Data error on deleting char: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		//L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
			return null;

		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null)
		{
			// exploit prevention, should not happens in normal way
			if(Config.DEBUG)
				_log.severe("Attempt of double login: " + character.getName()+"("+objId+") "+getAccountName());
			
			if (character.getClient() != null)
				character.getClient().closeNow();
			else{
				character.deleteMe();
				
				try
				{
					character.store();
				}
				catch(Exception e2)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e2.printStackTrace();
				}
				
			}
			
			//return null;
		}
		
		character = L2PcInstance.load(objId);
//		if(character != null)
//		{
//			//restoreInventory(character);
//			//restoreSkills(character);
//			//character.restoreSkills();
//			//restoreShortCuts(character);
//			//restoreWarehouse(character);
//
//			// preinit some values for each login
//			character.setRunning(); // running is default
//			character.standUp(); // standing is default
//
//			character.refreshOverloaded();
//			character.refreshExpertisePenalty();
//			character.refreshMasteryPenality();
//			character.refreshMasteryWeapPenality();
//			
//			character.sendPacket(new UserInfo(character));
//			character.broadcastKarma();
//			character.setOnlineStatus(true);
//		}
//		if(character == null)
//		{
//			_log.severe("could not restore in slot: " + charslot);
//		}

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
	public void onForcedDisconnection(boolean critical)
	{
		_forcedToClose = true;
		
		if(critical)
			_log.log(Level.WARNING, "Client " + toString() + " disconnected abnormally.");
		
		//the force operation will allow to not save client position to prevent again criticals
		//and stuck
		closeNow();
		
		
		/*
		L2PcInstance player = null;
		if((player = getActiveChar()) !=null){
			
			if(critical){
				_log.log(Level.WARNING, "Client " + toString() + " disconnected abnormally.");
				_log.log(Level.WARNING, "Character disconnected at Loc X:"+player.getX()+" Y:"+player.getY()+" Z:"+player.getZ());
				_log.log(Level.WARNING, "Character disconnected in (closest) zone: "+MapRegionTable.getInstance().getClosestTownName(player));
			}
			
			if(player.isFlying())
				player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			
			if(player.isAway())
			{
				AwayManager.getInstance().extraBack(player);
			}
			if(player.isInParty())
			{
				player.getParty().removePartyMember(player);
			}

			//Decrease boxes number
			if(player._active_boxes!=-1)
				player.decreaseBoxes();
			
			if(player._inEventCTF){
				CTF.onDisconnect(player);
			}else if(player._inEventDM){
				DM.onDisconnect(player);
			}else if(player._inEventTvT){
				TvT.onDisconnect(player);
			}else if(player._inEventVIP){
				VIP.onDisconnect(player);
			}
			
			if(Olympiad.getInstance().isRegistered(player)){
				Olympiad.getInstance().unRegisterNoble(player);
			}
			
			player.deleteMe();

			try
			{
				player.store(true);
			}
			catch(Exception e2)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e2.printStackTrace();
			}
			L2World.getInstance().removeFromAllPlayers(player);
			setActiveChar(null);
			LoginServerThread.getInstance().sendLogout(getAccountName());
			
		}
		stopGuardTask();
		nProtect.getInstance().closeSession(this);
		*/
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
	public void onDisconnection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());

		}
		catch(RejectedExecutionException e)
		{
			// server is closing
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
		}
	}

	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		close(0);
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 * @param delay 
	 */
	public void close(int delay)
	{
		
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
				cancelCleanup();
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), delay); //delayed
		}
		stopGuardTask();
		nProtect.getInstance().closeSession(this);
	}
	
    
	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
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
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
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

//				// we are going to manually save the char below thus we can force the cancel
//				if (_autoSaveInDB != null)
//					_autoSaveInDB.cancel(true);
//				
				
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null) // this should only happen on connection loss
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
					
					if(Olympiad.getInstance().isRegistered(player)){
						Olympiad.getInstance().unRegisterNoble(player);
					}

					//Decrease boxes number
					if(player._active_boxes!=-1)
						player.decreaseBoxes();
					
					// prevent closing again
					player.setClient(null);
					
					player.deleteMe();
					
					try
					{
						player.store(_forcedToClose);
					}
					catch(Exception e2)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e2.printStackTrace();
					}
					
				}
				
				L2GameClient.this.setActiveChar(null);
			}
			catch (Exception e1)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e1.printStackTrace();
				
				_log.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(L2GameClient.this.getAccountName());
			}
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
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

//				// we are going to manually save the char bellow thus we can force the cancel
//				if(_autoSaveInDB != null)
//					_autoSaveInDB.cancel(true);

				L2PcInstance player = L2GameClient.this.getActiveChar();
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
					
					if(Olympiad.getInstance().isRegistered(player)){
						Olympiad.getInstance().unRegisterNoble(player);
					}

					//Decrease boxes number
					if(player._active_boxes!=-1)
						player.decreaseBoxes();
					
					
					if(!player.isKicked() && !Olympiad.getInstance().isRegistered(player) 
							&& !player.isInOlympiadMode() 
							&& !player.isInFunEvent() 
							&& ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) 
							|| (player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE)))
					{
						player.setOffline(true);
						player.leaveParty();
						player.store();
						
						if(Config.OFFLINE_SET_NAME_COLOR)
						{
							player._originalNameColorOffline=player.getAppearance().getNameColor();
							player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							player.broadcastUserInfo();
						}
						
						if (player.getOfflineStartTime() == 0)
							player.setOfflineStartTime(System.currentTimeMillis());
						
						OfflineTradeTable.storeOffliner(player);
						
						return;
					}

					// notify the world about our disconnect
					player.deleteMe();
					
					//store operation
					try
					{
						player.store();
					}
					catch(Exception e2)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e2.printStackTrace();
					}
					
				}

				L2GameClient.this.setActiveChar(null);

				player = null;
			}
			catch(Exception e1)
			{
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e1.printStackTrace();
				
				_log.log(Level.WARNING, "error while disconnecting client", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(L2GameClient.this.getAccountName());
			}
		}
	}

	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	public boolean checkUnknownPackets()
	{
		
		L2PcInstance player = getActiveChar();
		
		if(player != null && _floodProtectors!=null && _floodProtectors.getUnknownPackets()!=null && !_floodProtectors.getUnknownPackets().tryPerformAction("check packet"))
		{
			unknownPacketCount++;
			
			if(unknownPacketCount >= Config.MAX_UNKNOWN_PACKETS)
				return true;
			return false;
		}
		unknownPacketCount = 0;
		return false;
	}
	
	
	private boolean cancelCleanup()
	{
		Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	/**
	 * Returns false if client can receive packets.
	 * True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 * @return 
	 */
	public boolean dropPacket()
	{
		if (_isDetached) // detached clients can't receive any packets
			return true;
		
		// flood protection
		if (getStats().countPacket(_packetQueue.size()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return getStats().dropPacket();
	}
	
	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			_log.severe("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		if (state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			_log.severe("Client " + toString() + " - Disconnected, too many buffer underflows in non-authed state.");
			closeNow();
		}
	}
	
	/**
	 * Add packet to the queue and start worker thread if needed
	 * @param packet 
	 */
	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			_log.severe("Client " + toString() + " - Disconnected, too many floods:"+getStats().longFloods+" long and "+getStats().shortFloods+" short.");
			closeNow();
			return;
		}
		
		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				_log.severe("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
				sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (_queueLock.isLocked()) // already processing
			return;
		
		//save last action time
		_last_received_packet_action_time = System.currentTimeMillis();
		//_log.severe("Client " + toString() + " - updated last action state "+_last_received_packet_action_time);
						
		try
		{
			if (state == GameClientState.CONNECTED)
			{
				if (getStats().processedPackets > 3)
				{
					_log.severe("Client " + toString() + " - Disconnected, too many packets in non-authed state.");
					closeNow();
					return;
				}
				
				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
				ThreadPoolManager.getInstance().executePacket(this);
		}
		catch (RejectedExecutionException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			// if the server is shutdown we ignore
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.severe("Failed executing: "+packet.getClass().getSimpleName()+" for Client: "+toString());
			}
		}
	}
	
	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
			return;
		
		try
		{
			int count = 0;
			while (true)
			{
				final ReceivablePacket<L2GameClient> packet = _packetQueue.poll();
				if (packet == null) // queue is empty
					return;
				
				if (_isDetached) // clear queue immediately after detach
				{
					_packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					
					_log.severe("Exception during execution "+packet.getClass().getSimpleName()+", client: "+toString()+","+e.getMessage());
				}
				
				count++;
				if (getStats().countBurst(count))
					return;
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}

	/**
	 * @return the _forcedToClose
	 */
	public boolean is_forcedToClose()
	{
		return _forcedToClose;
	}
	
	public boolean isConnectionAlive()
	{
		//if last received packet time is higher then Config.CHECK_CONNECTION_INACTIVITY_TIME --> check connection
		if(System.currentTimeMillis() - _last_received_packet_action_time > Config.CHECK_CONNECTION_INACTIVITY_TIME){
			
			_last_received_packet_action_time = System.currentTimeMillis();
			
			return getConnection().isConnected()
					&& !getConnection().isClosed();
			
		}
		
		return true;
	}
	
}
