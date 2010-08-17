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
package interlude.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import interlude.L2DatabaseFactory;
import interlude.gameserver.datatables.NpcTable;
import interlude.gameserver.datatables.SpawnTable;
import interlude.gameserver.model.L2Spawn;
import interlude.gameserver.model.actor.instance.L2GrandBossInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.templates.L2NpcTemplate;

/*
 * Author: MHard
 * Author: Maxi(update)
 */
public abstract class ClanHallSiege
{
	protected static Logger _log = Logger.getLogger(ClanHallSiege.class.getName());
	private Calendar _siegeDate;
	public Calendar _siegeEndDate;
	private boolean _isInProgress = false;

	protected List<L2Spawn> _DCSiegeGuardSpawn = new FastList<L2Spawn>();
	protected List<L2Spawn> _FOTDSiegeGuardSpawn = new FastList<L2Spawn>();
	protected L2Spawn _lidiaHellmannSpawn = null;
	protected L2Spawn _gustavSpawn = null;

	protected List<L2NpcInstance> _DCSiegeGuard = new FastList<L2NpcInstance>();
	protected List<L2NpcInstance> _FOTDSiegeGuard = new FastList<L2NpcInstance>();
	protected L2GrandBossInstance _lidiaHellmann = null;
	protected L2GrandBossInstance _gustav = null;

	private static final int GUSTAV = 35410;
	private static final int GMINION1 = 35408;
	private static final int GMINION2 = 35409;

	private static final int LIDIA = 35629;
	private static final int MINION1 = 35630;
	private static final int MINION2 = 35631;

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
			_log.warning("Exception: can't get clan hall siege date: " + e);
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
				_log.warning("Exception: can't save clanhall siege date: " + e.getMessage());
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

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public final void setSiegeDate(Calendar par)
	{
		_siegeDate = par;
	}

	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}

	public final void setIsInProgress(boolean par)
	{
		_isInProgress = par;
	}
	protected void loadFOTDSiegeGuards()
	{
		_lidiaHellmannSpawn = null;
		_FOTDSiegeGuardSpawn.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM cch_guards Where npc_templateid between ? and ? ORDER BY id");
			/*statement.setInt(1, 35633);
			statement.setInt(2, 35634);
			statement.setInt(3, 35635);
			statement.setInt(4, 35636);
			statement.setInt(5, 35637);
			statement.setInt(6, LIDIA);
			statement.setInt(7, MINION1);
			statement.setInt(8, MINION2);*/
			statement.setInt(1, 35629);
			statement.setInt(2, 35637);
			ResultSet rset = statement.executeQuery();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_FOTDSiegeGuardSpawn.add(spawnDat);
					_lidiaHellmannSpawn = spawnDat;
				}
				else
					_log.warning("Fortress of The Dead Siege Guards: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			rset.close();
			statement.close();
			_log.info("Fortress of The Dead Siege Guards: Loaded " + _FOTDSiegeGuardSpawn.size() + " Siege Guards spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("Fortress of The Dead Siege Guards: Spawn could not be initialized: " + e);
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

	protected void spawnFOTDGuards()
	{
		if (!_FOTDSiegeGuard.isEmpty())
			deleteFOTDGuards();

		for (L2Spawn s : _FOTDSiegeGuardSpawn)
		{
			s.startRespawn();
			_FOTDSiegeGuard.add((L2NpcInstance) s.doSpawn());
		}
		_lidiaHellmann = (L2GrandBossInstance) _lidiaHellmannSpawn.doSpawn();
		_lidiaHellmann.setIsImmobilized(false);
		_lidiaHellmann.setIsInvul(false);
	}

	protected void deleteFOTDGuards()
	{
		for (L2NpcInstance rg : _FOTDSiegeGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}
		if (_lidiaHellmann != null)
		{
			_lidiaHellmann.setIsImmobilized(false);
			_lidiaHellmann.setIsInvul(false);
			_lidiaHellmann.getSpawn().stopRespawn();
			_lidiaHellmann.deleteMe();
		}
		_lidiaHellmann.setIsImmobilized(false);
		_lidiaHellmann.setIsInvul(false);
		_FOTDSiegeGuard.clear();
	}

	protected void spawnDCSiegeGuards()
	{
		_gustav = (L2GrandBossInstance) _gustavSpawn.doSpawn();
		_gustav.setIsImmobilized(false);
		_gustav.setIsInvul(false);
		if (!_DCSiegeGuard.isEmpty())
			deleteDCSiegeGuards();

		for (L2Spawn s : _DCSiegeGuardSpawn)
		{
			s.startRespawn();
			_DCSiegeGuard.add((L2NpcInstance) s.doSpawn());
		}
	}

	protected void deleteDCSiegeGuards()
	{
		for (L2NpcInstance rg : _DCSiegeGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}
		if (_gustav != null)
		{
			_gustav.setIsImmobilized(false);
			_gustav.setIsInvul(false);
			_gustav.getSpawn().stopRespawn();
			_gustav.deleteMe();
		}
		_gustav.setIsImmobilized(false);
		_gustav.setIsInvul(false);
		_gustav.getSpawn().stopRespawn();
		_gustav.deleteMe();
		_DCSiegeGuard.clear();
	}

	protected void loadDCSiegeGuards()
	{
		_gustavSpawn = null;
		_DCSiegeGuardSpawn.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM cch_guards Where npc_templateid between ? and ? ORDER BY id");
			/*statement.setInt(1, 35411);
			statement.setInt(2, 35412);
			statement.setInt(3, 35413);
			statement.setInt(4, 35414);
			statement.setInt(5, 35415);
			statement.setInt(6, 35416);
			statement.setInt(1, GUSTAV);
			statement.setInt(2, GMINION1);
			statement.setInt(3, GMINION2);*/
			statement.setInt(1, 35409);
			statement.setInt(2, 35416);
			ResultSet rset = statement.executeQuery();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_DCSiegeGuardSpawn.add(spawnDat);
					_gustavSpawn = spawnDat;
				}
				else
					_log.warning("Devastated Castle Siege Guards: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			rset.close();
			statement.close();
			_log.info("Devastated Castle Siege Guards: Loaded " + _DCSiegeGuardSpawn.size() + " Siege Guards spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("Devastated Castle Siege Guards: Spawn could not be initialized: " + e);
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
}