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
package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.AccessLevel;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author FBIagent<br>
 */
public class AdminCommandAccessRights
{
	/** The logger<br> */
	protected static final Logger _log = Logger.getLogger(AdminCommandAccessRights.class.getName());
	
	/** The one and only instance of this class, retriveable by getInstance()<br> */
	private static AdminCommandAccessRights _instance = null;

	/** The access rights<br> */
	private final Map<String, Integer> _adminCommandAccessRights = new FastMap<String, Integer>();

	/**
	 * Loads admin command access rights from database<br>
	 */
	private AdminCommandAccessRights()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			final PreparedStatement stmt = con.prepareStatement("SELECT * FROM admin_command_access_rights");
			final ResultSet rset = stmt.executeQuery();
			String adminCommand = null;
			int accessLevels = 1;

			while(rset.next())
			{
				adminCommand = rset.getString("adminCommand");
				accessLevels = rset.getInt("accessLevels");
				_adminCommandAccessRights.put(adminCommand, accessLevels);
			}
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			_log.severe("Admin Access Rights: Error loading from database"+" "+ e);
		}
		finally
		{
			CloseUtil.close(con);
		}

		_log.finest("Admin Access Rights: Loaded {} Access Rigths from database."+" "+ _adminCommandAccessRights.size());
	}

	/**
	 * Returns the one and only instance of this class<br>
	 * <br>
	 * 
	 * @return AdminCommandAccessRights: the one and only instance of this class<br>
	 */
	public static AdminCommandAccessRights getInstance()
	{
		return _instance == null ? (_instance = new AdminCommandAccessRights()) : _instance;
	}
	
	public static void reload(){
		_instance = null;
		getInstance();
	}

	public int accessRightForCommand(String command){
		int out = -1;
		
		if(_adminCommandAccessRights.containsKey(command)){
			out = _adminCommandAccessRights.get(command);
		}
		
		return out;
	}
	
	public boolean hasAccess(String adminCommand, AccessLevel accessLevel)
	{
		if(accessLevel.getLevel() <= 0)
			return false;

		if(!accessLevel.isGm())
			return false;

		if(accessLevel.getLevel() == Config.MASTERACCESS_LEVEL)
			return true;

		//L2EMU_ADD - Visor123  need parse command before check
		String command = adminCommand;
		if(adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		//L2EMU_ADD

		int acar = 0;
		if(_adminCommandAccessRights.get(command) != null)
		{
			acar = _adminCommandAccessRights.get(command);
		}

		if(acar == 0)
		{
			_log.warning("Admin Access Rights: No rights defined for admin command {}."+" "+ command);
			return false;
		}
		else if(acar >= accessLevel.getLevel())
			return true;
		else
			return false;
	}
}
