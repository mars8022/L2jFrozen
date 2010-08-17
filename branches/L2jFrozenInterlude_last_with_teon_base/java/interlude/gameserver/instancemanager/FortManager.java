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
package interlude.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import interlude.L2DatabaseFactory;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.entity.Fort;

/**
 * @author Vice [L2JOneo]
 */
public class FortManager
{
	// =========================================================
	private static FortManager _instance;

	public static final FortManager getInstance()
	{
		if (_instance == null)
		{
			System.out.println("Initializing FortManager");
			_instance = new FortManager();
			_instance.load();
		}
		return _instance;
	}

	// =========================================================
	// =========================================================
	// Data Field
	private List<Fort> _forts;

	// =========================================================
	// Constructor
	public FortManager()
	{
	}

	// =========================================================
	// Method - Public
	public final int findNearestFortIndex(L2Object obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Fort fort;
			for (int i = 0; i < getForts().size(); i++)
			{
				fort = getForts().get(i);
				if (fort == null) {
					continue;
				}
				distance = fort.getDistance(obj);
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = i;
				}
			}
		}
		return index;
	}

	// =========================================================
	// Method - Private
	private final void load()
	{
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("Select id from fort order by id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}
			statement.close();
			System.out.println("Loaded: " + getForts().size() + " forts");
		}
		catch (Exception e)
		{
			System.out.println("Exception: loadFortData(): " + e.getMessage());
			e.printStackTrace();
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

	// =========================================================
	// Property - Public
	public final Fort getFortById(int fortId)
	{
		for (Fort temp : getForts())
		{
			if (temp.getFortId() == fortId) {
				return temp;
			}
		}
		return null;
	}

	public final Fort getFortByOwner(L2Clan clan)
	{
		for (Fort temp : getForts())
		{
			if (temp.getOwnerId() == clan.getClanId()) {
				return temp;
			}
		}
		return null;
	}

	public final Fort getFort(String name)
	{
		for (Fort temp : getForts())
		{
			if (temp.getName().equalsIgnoreCase(name.trim())) {
				return temp;
			}
		}
		return null;
	}

	public final Fort getFort(int x, int y, int z)
	{
		for (Fort temp : getForts())
		{
			if (temp.checkIfInZone(x, y, z)) {
				return temp;
			}
		}
		return null;
	}

	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.getFortId() == fortId) {
				return i;
			}
		}
		return -1;
	}

	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z)) {
				return i;
			}
		}
		return -1;
	}

	public final List<Fort> getForts()
	{
		if (_forts == null) {
			_forts = new FastList<Fort>();
		}
		return _forts;
	}

	int _fortId = 1; // from this fort
}
