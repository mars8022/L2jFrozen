
package interlude.gameserver.network.clientpackets;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.cache.HtmCache;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.communitybbs.CommunityBoard;
import interlude.gameserver.handler.AdminCommandHandler;
import interlude.gameserver.handler.IAdminCommandHandler;
import interlude.gameserver.model.L2CharPosition;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2Multisell;
import interlude.gameserver.model.actor.instance.L2ClassMasterInstance;
import interlude.gameserver.model.actor.instance.L2NpcInstance;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.entity.L2Event;
import interlude.gameserver.model.entity.L2OpenEvents.CTF;
import interlude.gameserver.model.entity.L2OpenEvents.DM;
import interlude.gameserver.model.entity.L2OpenEvents.FortressSiege;
import interlude.gameserver.model.entity.L2OpenEvents.TvT;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	// S
	private String _command;

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (!Config.ENABLE_FP && !activeChar.getFloodProtectors().getServerBypass().tryPerformAction("_command"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar == null)
		{
			return;
		}
		try
		{
			if (_command.startsWith("admin_")) // &&
			// activeChar.getAccessLevel()
			// >= Config.GM_ACCESSLEVEL)
			{
				if (Config.ALT_PRIVILEGES_ADMIN && !AdminCommandHandler.getInstance().checkPrivileges(activeChar, _command))
				{
					_log.info("<GM>" + activeChar + " does not have sufficient privileges for command '" + _command + "'.");
					return;
				}
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);
				if (ach != null)
				{
					ach.useAdminCommand(_command, activeChar);
				}
				else
				{
					_log.warning("No handler registered for bypass '" + _command + "'");
				}
			}
			else if (_command.equals("come_here") && activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				comeHere(activeChar);
			}
            else if (_command.startsWith("menu_multisell"))
            {
                menu_multisell(activeChar, _command);
            }
			else if (_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
            else if (_command.startsWith("menu_buff"))
			{
				menu_buff(activeChar, _command);
			}
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					if (_command.substring(endOfId + 1).startsWith("event_participate"))
					{
						L2Event.inscribePlayer(activeChar);
					}
					else if (_command.substring(endOfId + 1).startsWith("tvt_player_join "))
					{
						String teamName = _command.substring(endOfId + 1).substring(16);
						if (TvT._joining) {
							TvT.addPlayer(activeChar, teamName);
						} else {
							activeChar.sendMessage("The event is already started. You can not join now!");
						}
					}
					else if (_command.substring(endOfId + 1).startsWith("tvt_player_leave"))
					{
						if (TvT._joining) {
							TvT.removePlayer(activeChar);
						} else {
							activeChar.sendMessage("The event is already started. You can not leave now!");
						}
					}
					else if (_command.substring(endOfId + 1).startsWith("dmevent_player_join"))
					{
						if (DM._joining) {
							DM.addPlayer(activeChar);
						} else {
							activeChar.sendMessage("The event is already started. You can not join now!");
						}
					}
					else if (_command.substring(endOfId + 1).startsWith("dmevent_player_leave"))
					{
						if (DM._joining) {
							DM.removePlayer(activeChar);
						} else {
							activeChar.sendMessage("The event is already started. You can not leave now!");
						}
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
					{
						String teamName = _command.substring(endOfId + 1).substring(16);
						if (CTF._joining) {
							CTF.addPlayer(activeChar, teamName);
						} else {
							activeChar.sendMessage("The event is already started. You can not join now!");
						}
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
					{
						if (CTF._joining) {
							CTF.removePlayer(activeChar);
						} else {
							activeChar.sendMessage("The event is already started. You can not leave now!");
						}
					}
                    else if (_command.substring(endOfId+1).startsWith("fos_player_join "))
                    {
                        String teamName = _command.substring(endOfId+1).substring(16);

                        if (FortressSiege._joining) {
							FortressSiege.addPlayer(activeChar, teamName);
						} else {
							activeChar.sendMessage("The event has already begun. You can not join now!");
						}
                    }

                    else if (_command.substring(endOfId+1).startsWith("fos_player_leave")){
                        if (FortressSiege._joining) {
							FortressSiege.removePlayer(activeChar);
						} else {
							activeChar.sendMessage("The event has already begun. You can not withdraw your participation now!");
						}
                    }
					if (Config.ALLOW_REMOTE_CLASS_MASTERS && object instanceof L2ClassMasterInstance || object != null && object instanceof L2NpcInstance && endOfId > 0 && activeChar.isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
					{
						((L2NpcInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			// Draw a Symbol
			else if (_command.equals("menu_select?ask=-16&reply=1"))
			{
				L2Object object = activeChar.getTarget();
				if (object instanceof L2NpcInstance)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if (_command.equals("menu_select?ask=-16&reply=2"))
			{
				L2Object object = activeChar.getTarget();
				if (object instanceof L2NpcInstance)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if (_command.startsWith("manor_menu_select?"))
			{
				L2Object object = activeChar.getTarget();
				if (object instanceof L2NpcInstance)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if (_command.startsWith("bbs_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
				{
					return;
				}
				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
				{
					player.processQuestEvent(p, "");
				}
				else
				{
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
		}
		// finally
		// {
		// activeChar.clearBypass();
		// }
	}
          private void menu_multisell(L2PcInstance player, String command)
          {
              StringTokenizer st = new StringTokenizer(command, " ");
              st.nextToken();
              int val = Integer.parseInt(st.nextToken());
              L2Multisell.getInstance().SeparateAndSend(val, player, false, 0D);
          }
	/**
	 * @param client
	 */
	private void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj instanceof L2NpcInstance)
		{
			L2NpcInstance temp = (L2NpcInstance) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
			// temp.moveTo(player.getX(),player.getY(), player.getZ(), 0 );
		}
	}

	private void playerHelp(L2PcInstance activeChar, String path)
	{
		if (path.indexOf("..") != -1)
		{
			return;
		}
		String filename = "data/html/help/" + path;
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}

	/*
	 * (non-Javadoc)
	 * @see interlude.gameserver.network.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
private boolean menu_buff(L2PcInstance activeChar, String command)
	{
		if (activeChar.isMovementDisabled() || activeChar.isAlikeDead())
			return false;

		if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You can`t buff, if you have cursed weapon");
			return false;
		}
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You can`t buff in duel");
			return false;
		}
		if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You can`t buff in dim. rift!");
			return false;
		}
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You can`t buff in Event!");
			return false;
		}
		if (activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() != -1)
		{
			activeChar.sendMessage("You can`t buff in Olympiad!");
			return false;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can`t buff in Observer Mode!");
			return false;
		}
        		String htmFile = "data/html/Buff.htm";
        		String htmContent = HtmCache.getInstance().getHtm(htmFile);

            		NpcHtmlMessage Html = new NpcHtmlMessage(1);

            		Html.setHtml(htmContent);
            		activeChar.sendPacket(Html);

			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			while (st.hasMoreTokens())
        {
            String[] s = st.nextToken().split(";");

            int skillid=Integer.parseInt(s[0]);
            int lvl=Integer.parseInt(s[1]);

            L2Skill skill;
            skill = SkillTable.getInstance().getInfo(skillid,lvl);
            skill.getEffects(activeChar, activeChar);
        }
			return true;
	}
}
