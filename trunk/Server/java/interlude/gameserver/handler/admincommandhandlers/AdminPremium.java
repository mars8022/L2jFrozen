package interlude.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Calendar;
import java.util.logging.Logger;

import interlude.L2DatabaseFactory;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.actor.instance.L2PcInstance;

public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_premium_menu", "admin_premium_add1", "admin_premium_add2", "admin_premium_add3" };

private static final String UPDATE_PREMIUMSERVICE = "UPDATE character_premium SET premium_service=?,enddate=? WHERE account_name=?";
private static final Logger _log = Logger.getLogger(AdminPremium.class.getName());

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
		{
			AdminHelpPage.showHelpPage(activeChar, "premium_menu.htm");
		}
		else if (command.startsWith("admin_premium_add1"))
		{
			try
            {
                String val = command.substring(19);
                addPremiumServices(1, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
		else if(command.startsWith("admin_premium_add2"))
        {
            try
            {
                String val = command.substring(19);
                addPremiumServices(2, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
		else if(command.startsWith("admin_premium_add3"))
        {
            try
            {
                String val = command.substring(19);
                addPremiumServices(3, val);
            }
            catch(StringIndexOutOfBoundsException e)
            {
                activeChar.sendMessage("Err");
            }
        }
        return true;
    }

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void addPremiumServices(int Months,String AccName)
	{
		Connection con = null;
		try
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.MONTH, Months);

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, AccName);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService:  Could not increase data");
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
