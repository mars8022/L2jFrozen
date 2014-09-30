/*
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
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles following admin commands: - delete = deletes target
 * 
 * @version $Revision: 1.1.2.6.2.3 $ $Date: 2005/04/11 10:05:59 $
 */

public class AdminRepairChar implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminRepairChar.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
			"admin_restore", "admin_repair"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){
			return false;
		}
		
		if(Config.GMAUDIT)
		{
			Logger _logAudit = Logger.getLogger("gmaudit");
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}
		*/

		handleRepair(command);

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleRepair(String command)
	{
		String[] parts = command.split(" ");

		if(parts.length != 2)
			return;

		String cmd = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";
		Connection connection = null;

		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement statement = connection.prepareStatement(cmd);
			statement.setString(1, parts[1]);
			statement.execute();
			statement.close();
			statement = null;

			statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
			statement.setString(1, parts[1]);
			ResultSet rset = statement.executeQuery();

			int objId = 0;

			if(rset.next())
			{
				objId = rset.getInt(1);
			}

			rset.close();
			statement.close();
			rset = null;
			statement = null;

			if(objId == 0)
			{
				CloseUtil.close(connection);
				return;
			}

			statement = connection.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = null;

			statement = connection.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.WARNING, "Could not repair char:", e);
		}
		finally
		{
			CloseUtil.close(connection);
			connection = null;
			cmd = null;
			parts = null;
		}
	}
}
