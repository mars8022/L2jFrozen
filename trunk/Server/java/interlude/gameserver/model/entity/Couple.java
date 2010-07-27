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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import interlude.L2DatabaseFactory;
import interlude.gameserver.idfactory.IdFactory;
import interlude.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author evill33t
 */
public class Couple
{
	private static final Log _log = LogFactory.getLog(Couple.class.getName());
	// =========================================================
	// Data Field
	private int _Id = 0;
	private int _player1Id = 0;
	private int _player2Id = 0;
	private boolean _maried = false;
	private Calendar _affiancedDate;
	private Calendar _weddingDate;

	// =========================================================
	// Constructor
	public Couple(int coupleId)
	{
		_Id = coupleId;
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Select * from couples where id = ?");
			statement.setInt(1, _Id);
			rs = statement.executeQuery();
			while (rs.next())
			{
				_player1Id = rs.getInt("player1Id");
				_player2Id = rs.getInt("player2Id");
				_maried = rs.getBoolean("maried");
				_affiancedDate = Calendar.getInstance();
				_affiancedDate.setTimeInMillis(rs.getLong("affiancedDate"));
				_weddingDate = Calendar.getInstance();
				_weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
			}
			statement.close();
			this._maried = true;
		}
		catch (Exception e)
		{
			_log.error("Exception: Couple.load(): " + e.getMessage(), e);
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

	public Couple(L2PcInstance player1, L2PcInstance player2)
	{
		int _tempPlayer1Id = player1.getObjectId();
		int _tempPlayer2Id = player2.getObjectId();
		_player1Id = _tempPlayer1Id;
		_player2Id = _tempPlayer2Id;
		_affiancedDate = Calendar.getInstance();
		_affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		_weddingDate = Calendar.getInstance();
		_weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			_Id = IdFactory.getInstance().getNextId();
			statement = con.prepareStatement("INSERT INTO couples (id, player1Id, player2Id, maried, affiancedDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _Id);
			statement.setInt(2, _player1Id);
			statement.setInt(3, _player2Id);
			statement.setBoolean(4, false);
			statement.setLong(5, _affiancedDate.getTimeInMillis());
			statement.setLong(6, _weddingDate.getTimeInMillis());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("", e);
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

	public void marry()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE couples set maried = ?, weddingDate = ? where id = ?");
			statement.setBoolean(1, true);
			_weddingDate = Calendar.getInstance();
			statement.setLong(2, _weddingDate.getTimeInMillis());
			statement.setInt(3, _Id);
			statement.execute();
			statement.close();
			_maried = true;
		}
		catch (Exception e)
		{
			_log.error("", e);
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

	public void divorce()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM couples WHERE id=?");
			statement.setInt(1, _Id);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("Exception: Couple.divorce(): " + e.getMessage(), e);
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

	public final int getId()
	{
		return _Id;
	}

	public final int getPlayer1Id()
	{
		return _player1Id;
	}

	public final int getPlayer2Id()
	{
		return _player2Id;
	}

	public final boolean getMaried()
	{
		return _maried;
	}

	public final Calendar getAffiancedDate()
	{
		return _affiancedDate;
	}

	public final Calendar getWeddingDate()
	{
		return _weddingDate;
	}
}
