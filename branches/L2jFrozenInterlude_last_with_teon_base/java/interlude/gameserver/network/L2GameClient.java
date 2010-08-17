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
package interlude.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.LoginServerThread;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.LoginServerThread.SessionKey;
import interlude.gameserver.communitybbs.Manager.RegionBBSManager;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.model.CharSelectInfoPackage;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.L2Event;
import interlude.gameserver.network.serverpackets.L2GameServerPacket;
import interlude.gameserver.network.serverpackets.UserInfo;
import interlude.util.EventData;

import interlude.netcore.MMOClient;
import interlude.netcore.MMOConnection;

/**
 * Represents a client connected on Game Server
 *
 * @author KenM
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());

	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesnt has character attached to it yet IN_GAME - client has selected a char and is in game
	 *
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED, AUTHED, IN_GAME
	};

	public GameClientState state;
	// Info
	public String accountName;
	public SessionKey sessionId;
	public L2PcInstance activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();
	@SuppressWarnings("unused")
	private boolean _isAuthedGG;
	private long _connectionStartTime;
	private List<Integer> _charSlotMapping = new FastList<Integer>();
	// Task
	@SuppressWarnings("unchecked")
	protected ScheduledFuture _autoSaveInDB;
	// Crypt
	public GameCrypt crypt;
	// Flood protection
	public byte packetsSentInSec = 0;
	public int packetsSentStartTick = 0;
	// UnknownPacket protection
	private int unknownPacketCount = 0;

	protected ScheduledFuture<?> _cleanupTask = null;
	//offline_shop
	private boolean _isDetached = false;
	
	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		crypt = new GameCrypt();
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, 900000L);
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		crypt.setKey(key);
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
		crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		crypt.encrypt(buf.array(), buf.position(), size);
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
		if (activeChar != null)
			L2World.getInstance().storeObject(getActiveChar());
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
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
		if (_isDetached) return;
		
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	public boolean isDetached(){
		return _isDetached;
	}
	
	public void isDetached(boolean b){
		_isDetached = b;
	}
	
	public void closeNow(){
		super.getConnection().close(null);
		cleanMe(true);
	}
	
	public L2PcInstance markToDeleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk(getActiveChar()); if (Config.DEBUG) { _log.fine("active Char saved"); } this.setActiveChar(null); }
		 */
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on update delete time of char: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	public L2PcInstance deleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if (Config.DEBUG) _log.fine("active Char saved"); this.setActiveChar(null); }
		 */
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		deleteCharByObjId(objid);
		return null;
	}

	/**
	 * Save the L2PcInstance to the database.
	 */
	public static void saveCharToDisk(L2PcInstance cha)
	{
		try
		{
			cha.store();
		}
		catch (Exception e)
		{
			_log.severe("Error saving player character: " + e);
		}
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if (Config.DEBUG) _log.fine("active Char saved"); this.setActiveChar(null); }
		 */
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.severe("Data error on restoring char: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
			return;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on deleting char: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));
		if (character != null)
		{
			// restoreInventory(character);
			// restoreSkills(character);
			// character.restoreSkills();
			// restoreShortCuts(character);
			// restoreWarehouse(character);
			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default
			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.refreshMasteryPenality();
			character.sendPacket(new UserInfo(character));
			character.broadcastKarma();
			character.setOnlineStatus(true);
		}
		else
		{
			_log.severe("could not restore in slot: " + charslot);
		}
		// setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();
		for (int i = 0; i < chars.length; i++)
		{
			int objectId = chars[i].getObjectId();
			_charSlotMapping.add(new Integer(objectId));
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if (charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		Integer objectId = _charSlotMapping.get(charslot);
		return objectId.intValue();
	}

	@Override
	protected void onForcedDisconnection()
	{
		_log.info("Client " + toString() + " disconnected abnormally.");
	}

	@Override
	protected void onDisconnection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
		}
		catch (RejectedExecutionException e)
		{
			// server is closing
		}
	}

	public boolean checkUnknownPackets() // TODO flood protector rework.
	{
		if (this.getActiveChar() != null && !this.getActiveChar().getFloodProtectors().getWnk().tryPerformAction("unknownPacketCount"))
		{
			unknownPacketCount++;
			if (unknownPacketCount >= Config.MAX_UNKNOWN_PACKETS)
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

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
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
			
			boolean fast = true;
						
			try{
				L2PcInstance player = L2GameClient.this.getActiveChar();
				isDetached(true);
					
				if (player != null){
						
					//_log.info("called disconnect task on player "+player.getName());
						
					if (!player.isInOlympiadMode() /*&& player.isInsideZone(L2Character.ZONE_PEACE)*/ && !player.isInDuel() &&
						(player.getParty()==null || !player.getParty().isInDimensionalRift()) && !player.isFestivalParticipant() &&
				    	!player.atEvent && !player.isInJail()){
				   			
						//_log.info("checking offline mode");
							
						if (!player.offline_shop_enabled && (player.isInStoreMode() && Config.ALLOW_OFFLINE_TRADE) || (player.isInCraftMode() && Config.ALLOW_OFFLINE_CRAFT)){
				   			
								
							player.leaveParty();
				   				
							if (Config.OFFLINE_TARGET_COLOR){
				   					player.getAppearance().setNameColor(Config.OFFLINE_COLOR);
				    				player.broadcastUserInfo();
				    		}
								
							//_log.info("player in offline mode");
								
							player.offline_shop_enabled = true;
								
							//if is in offline mode, dnt call the clean me
				   			return;
				    			
				    	}else if (player.offline_shop_enabled){
							//player.offline_shop_enabled = false;
							isDetached(false);
						}
				    }
						
					if (player.isInCombat()){
				    	fast = false;
					}
						
				}
			  	
				cleanMe(fast);
					
					
			}catch (Exception e1){
			  	e1.printStackTrace();
			  	_log.warning("Error while disconnecting client.");
			}
		}
	}
				
    public void cleanMe(boolean fast){
					
		try{
			synchronized(this){
				if (_cleanupTask == null){
				   	_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
					
		}catch (Exception e1){
			_log.warning("Error during cleanup.");
		}
	}

				
    class CleanupTask implements Runnable{
			
		public void run(){
						
			try
			{
				// Update BBS
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				// we are going to mannually save the char bellow thus we can
				// force the cancel
				_autoSaveInDB.cancel(true);
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null) // this should only happen on connection loss
				{
					// we store all data from players who are disconnected while
					// in an event in order to restore it in the next login
					if (player.atEvent)
					{
						EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
						L2Event.connectionLossData.put(player.getName(), data);
					}
					if (player.isFlying())
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));

					// notify the world about our disconnect
					player.deleteMe();
					try
					{
						if(isDetached()){
							isDetached(false);
						}else{
							// notify the world about our disconnect
							player.deleteMe();	
							//_log.info("called deleteme");
						}
						
						saveCharToDisk(player);
					}
					catch (Exception e2) { }
				}
				L2GameClient.this.setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "error while disconnecting client", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(L2GameClient.this.getAccountName());
			}
		}
	}

	class AutoSaveTask implements Runnable
	{
		public void run()
		{
			try
			{
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null)
					saveCharToDisk(player);
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
}
