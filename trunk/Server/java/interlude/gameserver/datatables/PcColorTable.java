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
/**
 * @author DaRkRaGe
 */
package interlude.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javolution.util.FastMap;
import interlude.L2DatabaseFactory;
import interlude.gameserver.model.actor.instance.L2PcInstance;

public class PcColorTable
{
	/** The one and only instance of this class */
	public static PcColorTable _instance = null;
	/** List of names and color values container */
	private FastMap<String, PcColorContainer> _pcColors = new FastMap<String, PcColorContainer>();

	PcColorTable()
	{
		System.out.print("Pc Color Table: ");
		java.sql.Connection con = null;
		try
		{
			Vector<String> deleteNames = new Vector<String>();
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_colors`");
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				long regTime = rs.getLong("reg_time"), time = rs.getLong("time");
				String charName = rs.getString("char_name");
				int color = rs.getInt("color");
				if (time == 0 || regTime + time > System.currentTimeMillis())
				{
					_pcColors.put(charName, new PcColorContainer(color, regTime, time));
				} else {
					deleteNames.add(charName);
				}
			}
			ps.close();
			rs.close();
			for (String deleteName : deleteNames)
			{
				PreparedStatement psDel = con.prepareStatement("DELETE FROM `character_colors` WHERE `char_name`=?");
				psDel.setString(1, deleteName);
				psDel.executeUpdate();
				psDel.close();
			}
			System.out.println(_pcColors.size() + " loaded. " + deleteNames.size() + " expired deleted!");
			deleteNames.clear();
		}
		catch (Exception e)
		{
			System.out.println("Error while loading data from DB!");
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

	/**
	 * Returns the instance of this class, assign a new object to _instance if it's null
	 *
	 * @return PcColorTable
	 */
	public static PcColorTable getInstance()
	{
		if (_instance == null) {
			_instance = new PcColorTable();
		}
		return _instance;
	}

	/**
	 * Sets the name color of the L2PcInstance if it name is on the list
	 *
	 * @param activeChar
	 */
	public synchronized void process(L2PcInstance activeChar)
	{
		PcColorContainer colorContainer = _pcColors.get(activeChar.getName());
		if (colorContainer == null) {
			return;
		}
		long time = colorContainer.getTime();
		if (time == 0 || colorContainer.getRegTime() + time > System.currentTimeMillis()) {
			activeChar.getAppearance().setNameColor(colorContainer.getColor());
		} else {
			delete(activeChar.getName());
		}
	}

	/**
	 * Adds the name of the L2PcInstance to the list with the color values
	 *
	 * @param activeChar
	 * @param color
	 * @param regTime
	 * @param time
	 */
	public synchronized void add(L2PcInstance activeChar, int color, long regTime, long time)
	{
		String charName = activeChar.getName();
		PcColorContainer colorContainer = _pcColors.get(charName);
		if (colorContainer != null)
		{
			if (!delete(charName)) {
				return;
			}
		}
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psIns = con.prepareStatement("INSERT INTO `character_colors` VALUES (?,?,?,?)");
			psIns.setString(1, charName);
			psIns.setInt(2, color);
			psIns.setLong(3, regTime);
			psIns.setLong(4, time);
			psIns.executeUpdate();
			psIns.close();
			_pcColors.put(activeChar.getName(), new PcColorContainer(color, regTime, time));
			activeChar.getAppearance().setNameColor(color);
			activeChar.broadcastUserInfo();
			activeChar.sendMessage("Your name color has been changed by a GM!");
		}
		catch (Exception e)
		{
			System.out.println("Pc Color Table: Error while add " + charName + "'s color to DB!");
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

	/**
	 * Returns true if the name is deleted successfully from list, otherwise false Deletes the name from the list
	 *
	 * @param charName
	 * @return boolean
	 */
	public synchronized boolean delete(String charName)
	{
		PcColorContainer colorContainer = _pcColors.get(charName);
		if (colorContainer == null) {
			return false;
		}
		colorContainer = null;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement psDel = con.prepareStatement("DELETE FROM `character_colors` WHERE `char_name`=?");
			psDel.setString(1, charName);
			psDel.executeUpdate();
			psDel.close();
			_pcColors.remove(charName);
		}
		catch (Exception e)
		{
			System.out.println("Pc Color Table: Error while delete " + charName + "'s color from DB!");
			return false;
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
		return true;
	}
}