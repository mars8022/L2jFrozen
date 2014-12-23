/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private static Logger LOGGER = Logger.getLogger(ClanTable.class);
	
	private static ClanTable _instance;
	
	private final Map<Integer, L2Clan> _clans;
	
	public static ClanTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanTable();
		}
		
		return _instance;
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
	
	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}
	
	public int getTopRate(final int clan_id)
	{
		L2Clan clan = getClan(clan_id);
		if (clan.getLevel() < 3)
		{
			return 0;
		}
		int i = 1;
		for (final L2Clan clans : getClans())
		{
			if (clan != clans)
			{
				if (clan.getLevel() < clans.getLevel())
				{
					i++;
				}
				else if (clan.getLevel() == clans.getLevel())
				{
					if (clan.getReputationScore() <= clans.getReputationScore())
					{
						i++;
					}
				}
			}
		}
		clan = null;
		return i;
	}
	
	private ClanTable()
	{
		_clans = new FastMap<>();
		L2Clan clan;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			final ResultSet result = statement.executeQuery();
			
			// Count the clans
			int clanCount = 0;
			
			while (result.next())
			{
				_clans.put(Integer.parseInt(result.getString("clan_id")), new L2Clan(Integer.parseInt(result.getString("clan_id"))));
				clan = getClan(Integer.parseInt(result.getString("clan_id")));
				if (clan.getDissolvingExpiryTime() != 0)
				{
					if (clan.getDissolvingExpiryTime() < System.currentTimeMillis())
					{
						destroyClan(clan.getClanId());
					}
					else
					{
						scheduleRemoveClan(clan.getClanId());
					}
				}
				clanCount++;
			}
			result.close();
			DatabaseUtils.close(statement);
			
			LOGGER.info("Restored " + clanCount + " clans from the database.");
		}
		catch (final Exception e)
		{
			LOGGER.error("Data error on ClanTable", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		
		restorewars();
	}
	
	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(final int clanId)
	{
		
		return _clans.get(clanId);
	}
	
	public L2Clan getClanByName(final String clanName)
	{
		for (final L2Clan clan : getClans())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
				return clan;
		}
		
		return null;
	}
	
	/**
	 * Creates a new clan and store clan info to database
	 * @param player
	 * @param clanName
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(final L2PcInstance player, final String clanName)
	{
		if (null == player)
			return null;
		
		LOGGER.debug("{" + player.getObjectId() + "}({" + player.getName() + "}) requested a clan creation.");
		
		if (10 > player.getLevel())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN));
			return null;
		}
		
		if (0 != player.getClanId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_CREATE_CLAN));
			return null;
		}
		
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN));
			return null;
		}
		
		if (!isValidCalnName(player, clanName))
			return null;
		
		final L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		final L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);
		
		LOGGER.debug("New clan created: {" + clan.getClanId() + "} {" + clan.getName() + "}");
		
		_clans.put(new Integer(clan.getClanId()), clan);
		
		// should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREATED));
		
		return clan;
	}
	
	public boolean isValidCalnName(final L2PcInstance player, final String clanName)
	{
		if (!Util.isAlphaNumeric(clanName) || clanName.length() < 2)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}
		
		if (clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return false;
		}
		
		if (getClanByName(clanName) != null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			return false;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.warn("ERROR: Clan name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher match = pattern.matcher(clanName);
		
		if (!match.matches())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}
		
		return true;
	}
	
	public synchronized void destroyClan(final int clanId)
	{
		final L2Clan clan = getClan(clanId);
		
		if (clan == null)
			return;
		
		L2PcInstance leader = null;
		if (clan.getLeader() != null && (leader = clan.getLeader().getPlayerInstance()) != null)
		{
			
			if (Config.CLAN_LEADER_COLOR_ENABLED && clan.getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
			{
				
				if (Config.CLAN_LEADER_COLORED == 1)
				{
					leader.getAppearance().setNameColor(0x000000);
				}
				else
				{
					leader.getAppearance().setTitleColor(0xFFFF77);
				}
				
			}
			
			// remove clan leader skills
			leader.addClanLeaderSkills(false);
		}
		
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		
		final int castleId = clan.getHasCastle();
		
		if (castleId == 0)
		{
			for (final Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}
		
		final int fortId = clan.getHasFort();
		
		if (fortId == 0)
		{
			for (final FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}
		
		final L2ClanMember leaderMember = clan.getLeader();
		
		if (leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		}
		
		for (final L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getName(), 0);
		}
		
		final int leaderId = clan.getLeaderId();
		final int clanLvl = clan.getLevel();
		
		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			
			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			
			if (leader == null && leaderId != 0 && Config.CLAN_LEADER_COLOR_ENABLED && clanLvl >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
			{
				String query;
				if (Config.CLAN_LEADER_COLORED == 1)
				{
					query = "UPDATE characters SET name_color = '000000' WHERE obj_Id = ?";
				}
				else
				{
					query = "UPDATE characters SET title_color = 'FFFF77' WHERE obj_Id = ?";
				}
				statement = con.prepareStatement(query);
				statement.setInt(1, leaderId);
				statement.execute();
			}
			
			if (castleId != 0)
			{
				statement = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
				statement.setInt(1, castleId);
				statement.execute();
			}
			
			if (fortId != 0)
			{
				final Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					final L2Clan owner = fort.getOwnerClan();
					if (clan == owner)
					{
						fort.removeOwner(clan);
					}
				}
			}
			
			LOGGER.debug("Clan removed in db: {}" + " " + clanId);
			
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.error("Error while removing clan in db", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
	
	public void scheduleRemoveClan(final int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (getClan(clanId) == null)
					return;
				
				if (getClan(clanId).getDissolvingExpiryTime() != 0)
				{
					destroyClan(clanId);
				}
			}
		}, getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis());
	}
	
	public boolean isAllyExists(final String allyName)
	{
		for (final L2Clan clan : getClans())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
				return true;
		}
		return false;
	}
	
	public void storeclanswars(final int clanId1, final int clanId2)
	{
		final L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		final L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not store clans wars data", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		// SystemMessage msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		
		SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		// msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		// msg.addString(clan1.getName());
		// clan2.broadcastToOnlineMembers(msg);
		// clan1 declared clan war.
		
		msg = new SystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void deleteclanswars(final int clanId1, final int clanId2)
	{
		final L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		final L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		
		// for(L2ClanMember player: clan1.getMembers())
		// {
		// if(player.getPlayerInstance()!=null)
		// player.getPlayerInstance().setWantsPeace(0);
		// }
		// for(L2ClanMember player: clan2.getMembers())
		// {
		// if(player.getPlayerInstance()!=null)
		// player.getPlayerInstance().setWantsPeace(0);
		// }
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.execute();
			
			// statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			// statement.setInt(1,clanId2);
			// statement.setInt(2,clanId1);
			// statement.execute();
			
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not restore clans wars data", e);
		}
		finally
		{
			CloseUtil.close(con);
		}
		// SystemMessage msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		
		SystemMessage msg = new SystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		
		msg = new SystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
		
		// msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		// msg.addString(clan1.getName());
		// clan2.broadcastToOnlineMembers(msg);
		// msg = null;
	}
	
	public void checkSurrender(final L2Clan clan1, final L2Clan clan2)
	{
		int count = 0;
		
		for (final L2ClanMember player : clan1.getMembers())
		{
			if (player != null && player.getPlayerInstance().getWantsPeace() == 1)
			{
				count++;
			}
		}
		
		if (count == clan1.getMembers().length - 1)
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteclanswars(clan1.getClanId(), clan2.getClanId());
		}
	}
	
	private void restorewars()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars");
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				getClan(rset.getInt("clan1")).setEnemyClan(rset.getInt("clan2"));
				getClan(rset.getInt("clan2")).setAttackerClan(rset.getInt("clan1"));
			}
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not restore clan wars data:");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}
