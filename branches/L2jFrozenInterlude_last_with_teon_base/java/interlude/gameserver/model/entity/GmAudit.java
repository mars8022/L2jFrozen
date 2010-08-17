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
import java.util.logging.Logger;

import interlude.Config;
import interlude.L2DatabaseFactory;

/**
 * GM EditManager. Will insert a SQL message upon call.
 *
 * @author DaRkRaGe
 */
public class GmAudit
{
	/**
	 * Inserts the comment into the DataBase.
	 *
	 * @param GmName
	 * @param GmId
	 * @param Target
	 * @param Action
	 */
	public GmAudit(String GmName, int GmId, String Target, String Action)
	{
		if (Config.GMAUDIT)
		{
			java.sql.Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO `gm_edit` (`id`,`GM_Name`,`GM_ID`,`Edited_Char`,`Action`) " + "VALUES (NULL,'" + GmName + "','" + GmId + "','" + Target + "','" + Action + "')");
				// Information about GM actions.
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning("Error in the informative sentence in GmAudit.java");
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (Exception e)
				{
					// meh
				}
			}
		}
	}

	private static Logger _log = Logger.getLogger(Config.class.getName());
}