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
package interlude.gameserver.instancemanager.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import interlude.L2DatabaseFactory;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.instancemanager.ClanHallManager;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.entity.ClanHall;
import interlude.gameserver.model.entity.ClanHallSiege;
import interlude.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Author: MHard
 * Author: Maxi(modified)
 */
public class FortResistSiegeManager extends ClanHallSiege
{
	protected static Log 						_log 						= LogFactory.getLog(FortResistSiegeManager.class.getName());
	public ClanHall 							_clanhall					= ClanHallManager.getInstance().getClanHallById(21);
	private Map<Integer, DamageInfo> 			_clansDamageInfo 			= new HashMap<Integer, DamageInfo>();
	protected L2Spawn 							_BrakelSpawn				= null;
	protected L2NpcInstance 					_Brakel 					= null;
	protected ScheduledFuture<?> 				_scheduledStartSiegeTask 	= null;
	private Calendar 							_siegeEndDate;
	private ScheduledFuture<?> 					_Nurka;
	private static FortResistSiegeManager		_instance;
	private static final int NURKA = 35368;					// RaidBoss loc x(44525) y(108867) z(-2020).
	private static final int REPUTATIONSCORE = 600;			// Reputacion para el clan ganador del Clan Hall Fortress of Resistance.

	private class DamageInfo
	{
		public L2Clan _clan;
		public long _damage;
	}

	public static final FortResistSiegeManager getInstance()
	{
		if (_instance == null)
			_instance = new FortResistSiegeManager();
		return _instance;
	}

	private FortResistSiegeManager()
	{
		setIsInProgress(false);
		_log.info("ClanHallSiege: Fortress Of Resistence");
		long siegeDate = restoreSiegeDate(21);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 21, 22);
		_clansDamageInfo = new HashMap<Integer, DamageInfo>();
		// Schedule siege auto start
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(), 1000);
	}

	public void startSiege()
	{
		BossSpawn();
		setIsInProgress(true);
		if (!_clansDamageInfo.isEmpty())
			_clansDamageInfo.clear();
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 30);
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(), 1000);
			if (!ClanHallManager.getInstance().isFree(_clanhall.getId()))
			{
				ClanTable.getInstance().getClan(_clanhall.getOwnerId()).broadcastClanStatus();
				ClanHallManager.getInstance().setFree(_clanhall.getId());
				_clanhall.banishForeigners();
			}
	}

	public void endSiege(boolean type)
	{
		setIsInProgress(false);
		_Nurka.cancel(true);
		if (type = true)
		{
			L2Clan clanIdMaxDamage = null;
			long tempMaxDamage = 0;
			for (DamageInfo damageInfo : _clansDamageInfo.values())
			{
				if (damageInfo != null)
				{
					if (damageInfo._damage > tempMaxDamage)
					{
						tempMaxDamage = damageInfo._damage;
						clanIdMaxDamage = damageInfo._clan;
					}
				}
			}
			if (clanIdMaxDamage != null)
			{
				ClanHallManager.getInstance().setOwner(21 , clanIdMaxDamage);
				clanIdMaxDamage.setReputationScore(clanIdMaxDamage.getReputationScore() + REPUTATIONSCORE, true);
				_clanhall.banishForeigners();
			}
			_log.info("the siege of Fortress of Resistance to finish");
		}
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 21, 22);
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(), 1000);
	}

	public long restoreSiegeDate(int ClanHallId)
	{
		long res = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT siege_data FROM clanhall_siege WHERE id=?");
			statement.setInt(1, ClanHallId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
				res = rs.getLong("siege_data");
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Exception: can't get clan hall siege date: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}

	public void setNewSiegeDate(long siegeDate, int ClanHallId, int hour)
	{
		Calendar tmpDate = Calendar.getInstance();
		if (siegeDate <= System.currentTimeMillis())
		{
			tmpDate.setTimeInMillis(System.currentTimeMillis());
			tmpDate.add(Calendar.DAY_OF_MONTH, 3);
			tmpDate.set(Calendar.DAY_OF_WEEK, 6);
			tmpDate.set(Calendar.HOUR_OF_DAY, hour);
			tmpDate.set(Calendar.MINUTE, 0);
			tmpDate.set(Calendar.SECOND, 0);
			setSiegeDate(tmpDate);
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clanhall_siege SET siege_data=? WHERE id = ?");
				statement.setLong(1, getSiegeDate().getTimeInMillis());
				statement.setInt(2, ClanHallId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Exception: can't save clan hall siege date: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					if (con != null)
						con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void addSiegeDamage(L2Clan clan,double damage)
	{
		DamageInfo clanDamage=_clansDamageInfo.get(clan.getClanId());
		if (clanDamage != null)
			clanDamage._damage += damage;
		else
		{
			clanDamage = new DamageInfo();
			clanDamage._clan=clan;
			clanDamage._damage += damage;

			_clansDamageInfo.put(clan.getClanId(), clanDamage);
		}
	}

	public class ScheduleEndSiegeTask implements Runnable
	{
		public ScheduleEndSiegeTask()
		{
		}
		public void run()
		{
			if (!getIsInProgress()) return;

			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
				if (timeRemaining <= 0)
				{
					endSiege(true);
					return;
			}
		}
	}

	public class ScheduleStartSiegeTask implements Runnable
	{
		public ScheduleStartSiegeTask()
		{
		}

		public void run()
		{
			_scheduledStartSiegeTask.cancel(false);
			if (getIsInProgress()) return;

			final long timeRemaining = getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				startSiege();
				return;
			}
		}
	}
	

	public void BossSpawn()
    {
		if (!_clansDamageInfo.isEmpty())
			_clansDamageInfo.clear();

		L2NpcInstance result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(NURKA);

			L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(44525);
			spawn.setLocy(108867);
			spawn.setLocz(-2020);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			template = null;
		} catch(Exception e)
		{
			e.printStackTrace();
		}
			_log.info("Fortress of Resistanse: Bloody Lord Nurka spawned!");
			_Nurka = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), _siegeEndDate.getTimeInMillis() - System.currentTimeMillis());
		}
	
	protected class DeSpawnTimer implements Runnable
    {
        L2NpcInstance _npc = null;

        public DeSpawnTimer(L2NpcInstance npc)
        {
        	_npc = npc;
        }

        public void run()
        {
        	_npc.onDecay();
        }
    }
}
