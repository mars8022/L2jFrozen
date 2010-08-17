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
package interlude.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.model.L2World;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.MyTargetSelected;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;
import interlude.gameserver.network.serverpackets.SocialAction;
import interlude.gameserver.network.serverpackets.ValidateLocation;
import interlude.gameserver.templates.L2NpcTemplate;
import interlude.util.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author DaRkRaGe [L2JOneo]
 */
public class L2FactionInstance extends L2FolkInstance
{
	public L2FactionInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private final static Log _log = LogFactory.getLog(L2FactionInstance.class.getName());

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		if (actualCommand.equalsIgnoreCase("delevel"))
		{
			setTarget(player);
			int lvl = player.getLevel();
			if (lvl > 40)
			{
				if (val.equalsIgnoreCase("1"))
				{
					long delexp = 0;
					delexp = player.getStat().getExp() - player.getStat().getExpForLevel(lvl - 1);
					player.getStat().addExp(-delexp);
					player.broadcastKarma();
					player.sendMessage("Delevel was Successfull.");
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/Faction/delevel.htm");
					html.replace("%lvl%", String.valueOf(player.getLevel()));
					sendHtmlMessage(player, html);
				}
			}
			else
			{
				player.sendMessage("You have to be at least level 40 to use the delevel faction.");
			}
			return;
		}
		else if (actualCommand.equalsIgnoreCase("levelup"))
		{
			setTarget(player);
			int lvl = player.getLevel();
			if (val.equalsIgnoreCase("1"))
			{
				long addexp = 0;
				addexp = player.getStat().getExpForLevel(lvl + 1) - player.getStat().getExp();
				player.getStat().addExp(addexp);
				player.broadcastKarma();
				player.sendMessage("Level Up was Successfull.");
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/mods/Faction/levelup.htm");
				html.replace("%lvl%", String.valueOf(player.getLevel()));
				sendHtmlMessage(player, html);
			}
		}
		else if (actualCommand.equalsIgnoreCase("setnobless"))
		{
			setTarget(player);
			if (player.isNoble())
			{
				player.sendMessage("You have allready nobless status.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.setNoble(true);
				Connection connection = null;
				try
				{
					connection = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
					statement.setString(1, player.getName());
					ResultSet rset = statement.executeQuery();
					int objId = 0;
					if (rset.next())
					{
						objId = rset.getInt(1);
					}
					rset.close();
					statement.close();
					if (objId == 0)
					{
						connection.close();
						return;
					}
					statement = connection.prepareStatement("UPDATE characters SET nobless=1 WHERE obj_id=?");
					statement.setInt(1, objId);
					statement.execute();
					statement.close();
					connection.close();
				}
				catch (Exception e)
				{
					_log.warn("Could not set Nobless status of char.");
				}
				finally
				{
					try
					{
						connection.close();
					}
					catch (Exception e)
					{
					}
				}
				System.out.println("##KvN Engine## : Player " + player.getName() + " has gain noble status");
				player.sendMessage("You Gaine Nobless Status.");
			}
		}
		else if (actualCommand.equalsIgnoreCase("setkoof"))
		{
			setTarget(player);
			if (player.isKoof())
			{
				player.sendMessage("You are allready a " + Config.KOOFS_NAME_TEAM + " faction.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (player.isNoob())
				{
					player.sendMessage("You Cant Change Faction.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					// int getnoobs = L2World.getInstance().getAllnoobPlayers().size();
					// int getkoofs = L2World.getInstance().getAllkoofPlayers().size();
					// if (getkoofs > getnoobs)
					{
						// player.sendMessage("It is more " + Config.KOOF_NAME_TEAM + " members online.");
						// player.sendPacket(ActionFailed.STATIC_PACKET);
						// } else
						// {
						player.setKoof(true);
						Connection connection = null;
						try
						{
							connection = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
							statement.setString(1, player.getName());
							ResultSet rset = statement.executeQuery();
							int objId = 0;
							if (rset.next())
							{
								objId = rset.getInt(1);
							}
							rset.close();
							statement.close();
							if (objId == 0)
							{
								connection.close();
								return;
							}
							statement = connection.prepareStatement("UPDATE characters SET koof=1 WHERE obj_id=?");
							statement.setInt(1, objId);
							statement.execute();
							statement.close();
							connection.close();
						}
						catch (Exception e)
						{
							_log.warn("Could not set koof status of char:");
						}
						finally
						{
							try
							{
								connection.close();
							}
							catch (Exception e)
							{
							}
						}
						System.out.println("##KvN Engine## : Player " + player.getName() + " has choose " + Config.KOOFS_NAME_TEAM + " faction");
						if (player.isKoof() == true)
						{
							player.broadcastUserInfo();
							player.sendMessage("You are fighiting now for " + Config.KOOFS_NAME_TEAM + " faction ");
							player.getAppearance().setNameColor(Config.KOOFS_NAME_COLOR);
							player.setTitle(Config.KOOFS_NAME_TEAM);
							player.teleToLocation(146334, 25767, -2013);
						}
					}
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("setnoob"))
		{
			setTarget(player);
			if (player.isNoob())
			{
				player.sendMessage("You are allready a " + Config.NOOBS_NAME_TEAM + " faction.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (player.isKoof())
				{
					player.sendMessage("You cant change faction.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					@SuppressWarnings("unused")
					int getnoobs = L2World.getInstance().getAllnoobPlayers().size();
					@SuppressWarnings("unused")
					int getkoofs = L2World.getInstance().getAllkoofPlayers().size();
					// if (getnoobs > getkoofs)
					{
						// player.sendMessage("It is more " + Config.NOOB_NAME_TEAM + " members online.");
						// player.sendPacket(ActionFailed.STATIC_PACKET);
						// } else
						// {
						player.setNoob(true);
						Connection connection = null;
						try
						{
							connection = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
							statement.setString(1, player.getName());
							ResultSet rset = statement.executeQuery();
							int objId = 0;
							if (rset.next())
							{
								objId = rset.getInt(1);
							}
							rset.close();
							statement.close();
							if (objId == 0)
							{
								connection.close();
								return;
							}
							statement = connection.prepareStatement("UPDATE characters SET noob=1 WHERE obj_id=?");
							statement.setInt(1, objId);
							statement.execute();
							statement.close();
							connection.close();
						}
						catch (Exception e)
						{
							_log.warn("Could not set noob status of char:");
						}
						finally
						{
							try
							{
								connection.close();
							}
							catch (Exception e)
							{
							}
						}
						System.out.println("##KvN Engine## : player " + player.getName() + " Has choose " + Config.NOOBS_NAME_TEAM + " Faction");
						if (player.isNoob() == true)
						{
							player.broadcastUserInfo();
							player.sendMessage("You are fighiting now for " + Config.NOOBS_NAME_TEAM + " faction ");
							player.getAppearance().setNameColor(Config.NOOBS_NAME_COLOR);
							player.setTitle(Config.NOOBS_NAME_TEAM);
							player.teleToLocation(59669, -42221, -2992);
						}
					}
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
			broadcastPacket(sa);
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/Faction/faction.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
}