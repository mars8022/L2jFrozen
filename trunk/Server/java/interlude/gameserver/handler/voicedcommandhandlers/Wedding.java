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
package interlude.gameserver.handler.voicedcommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import interlude.Config;
import interlude.L2DatabaseFactory;
import interlude.gameserver.GameTimeController;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.ai.CtrlIntention;
import interlude.gameserver.datatables.SkillTable;
import interlude.gameserver.handler.IVoicedCommandHandler;
import interlude.gameserver.instancemanager.CastleManager;
import interlude.gameserver.instancemanager.CoupleManager;
import interlude.gameserver.instancemanager.GrandBossManager;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ConfirmDlg;
import interlude.gameserver.network.serverpackets.MagicSkillUser;
import interlude.gameserver.network.serverpackets.SetupGauge;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.gameserver.util.Broadcast;

/**
 * @author evill33t reworked schursin
 */
public class Wedding implements IVoicedCommandHandler
{
	static final Logger _log = Logger.getLogger(Wedding.class.getName());
	private static final String[] _voicedCommands = { "divorce", "engage", "gotolove" };

	/**
	 * @see interlude.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, interlude.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("engage")) {
			return engage(activeChar);
		} else if (command.startsWith("divorce")) {
			return divorce(activeChar);
		} else if (command.startsWith("gotolove")) {
			return gotolove(activeChar);
		}
		return false;
	}

	public boolean divorce(L2PcInstance playerA)
	{
		// no partner?!
		if (playerA.getPartnerId() == 0)
		{
			return false;
		}
		// try to found partner
		L2PcInstance playerB = (L2PcInstance) L2World.getInstance().findObject(playerA.getPartnerId());
		// divorce or couple broke?
		if (playerA.isMaried())
		{
			playerA.sendMessage("You are now devorced.");
			playerA.getInventory().reduceAdena("Wedding", Config.WEDDING_DIVORCE_COSTS, playerA, null);
		}
		else
		{
			playerA.sendMessage("You have broken up as a couple.");
		}
		// partner found?
		if (playerB != null)
		{
			playerB.setPartnerId(0);
			// divorce or couple broke?
			if (playerB.isMaried())
			{
				playerB.sendMessage("Your spouse has decided to break up with you.");
				playerB.addAdena("WEDDING", Config.WEDDING_DIVORCE_COSTS, null, false);
			}
			else
			{
				playerB.sendMessage("Your fiance has decided to break the engagement with you.");
			}
		}
		// delete couple
		CoupleManager.getInstance().deleteCouple(playerA.getCoupleId());
		return true;
	}

	public boolean engage(L2PcInstance playerA)
	{
		// check target
		if (playerA.getTarget() == null)
		{
			playerA.sendMessage("Please target your partner.");
			return false;
		}
		// check if target is a L2PcInstance
		if (!(playerA.getTarget() instanceof L2PcInstance))
		{
			playerA.sendMessage("You can only ask another player to engage you.");
			return false;
		}
		// check if player is already engaged
		if (playerA.getPartnerId() != 0)
		{
			playerA.sendMessage("You are already engaged.");
			// try to tell about it to partner
			L2PcInstance playerB = (L2PcInstance) L2World.getInstance().findObject(playerA.getPartnerId());
			if (playerB != null)
			{
				playerB.sendMessage("--> Your partner has tried to engage with another player! <--");
			}
			// any punishment?
			if (Config.WEDDING_PUNISH_INFIDELITY)
			{
				// give player a big head
				playerA.startAbnormalEffect((short) 0x2000);
				// lets recycle the sevensigns debuffs
				int skillId = playerA.isMageClass() ? 4361 : 4362;
				int skillLevel = playerA.getLevel() > 40 ? 2 : 1;
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				// get effect
				if (playerA.getFirstEffect(skill) == null)
				{
					skill.getEffects(playerA, playerA);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skillId);
					playerA.sendPacket(sm);
				}
			}
			return false;
		}
		L2PcInstance playerB = (L2PcInstance) playerA.getTarget();
		// check if player target himself
		if (playerA.getObjectId() == playerB.getObjectId())
		{
			playerA.sendMessage("Is there something wrong with you? Are you trying to go out with youself?");
			return false;
		}
		if (playerB.isMaried())
		{
			playerA.sendMessage("You are already married.");
			return false;
		}
		if (playerA.isEngageRequest())
		{
			playerB.sendMessage("This player is already in a relationship.");
			return false;
		}
		if (playerB.isNoob() && playerA.isKoof())
		{
			playerB.sendMessage("Faction Protection Cant proceed Request.");
			return false;
		}
		if (playerB.isKoof() && playerA.isNoob())
		{
			playerB.sendMessage("Faction Protection Cant proceed Request.");
			return false;
		}
		if (playerB.getPartnerId() != 0)
		{
			playerA.sendMessage("This player is already in a relationship.");
			return false;
		}
		if (!Config.WEDDING_SAMESEX)
		{
			if (playerA.getAppearance().getSex() == playerB.getAppearance().getSex())
			{
				playerA.sendMessage("Homosexual marriage is not allowed on this server!");
				return false;
			}
		}
		// check if target has player on friendlist
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT COUNT(*) as 'count' FROM character_friends WHERE friend_id=? AND char_id=?");
			statement.setInt(1, playerA.getObjectId());
			statement.setInt(2, playerB.getObjectId());
			ResultSet rset = statement.executeQuery();
			rset.next();
			if (rset.getInt("count") < 1)
			{
				playerA.sendMessage("The player you want to ask is not on your friends list. Before entering a relationship, you should become friends with the player. (To become friends /friendinvite [name])");
				return false;
			}
		}
		catch (Exception e)
		{
			_log.warning("Could not read friend data:" + e);
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
		// Since all looks fine we can engage them.
		playerB.setEngageRequest(true, playerA.getObjectId());
		ConfirmDlg dlg = new ConfirmDlg(1983).addString(playerA.getName() + " is asking your hand in marriage. Would you like to accept the request and start a new relationship?");
		playerB.sendPacket(dlg);
		// playerB.sendPacket(new ConfirmDlg(614, playerA.getName() + " is asking your hand in marriage. Would you like to accept the request and start a new relationship?"));
		return true;
	}

	public boolean gotolove(L2PcInstance playerA)
	{
		// is player maried?
		if (!playerA.isMaried())
		{
			playerA.sendMessage("You are not married.");
			return false;
		}
		if (GrandBossManager.getInstance().getZone(playerA) != null)
		{
			playerA.sendMessage("You are inside a Boss Zone.");
			return false;
		}
		// is have a partner?
		if (playerA.getPartnerId() == 0)
		{
			playerA.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
			_log.severe("Married but couldn't find parter for " + playerA.getName());
			return false;
		}
		// try to found partner
		L2PcInstance playerB = (L2PcInstance) L2World.getInstance().findObject(playerA.getPartnerId());
		// checking conditions
		if (playerB == null)
		{
			playerA.sendMessage("Your partner is not online.");
			return false;
		}
		else if (playerB.isInJail())
		{
			playerA.sendMessage("Your partner is in jail.");
			return false;
		}
		else if (GrandBossManager.getInstance().getZone(playerA) != null)
		{
			playerA.sendMessage("Your partner is inside a Boss Zone.");
			return false;
		}
		else if (playerB.getClan() != null && CastleManager.getInstance().getCastleByOwner(playerB.getClan()) != null && CastleManager.getInstance().getCastleByOwner(playerB.getClan()).getSiege().getIsInProgress())
		{
			playerA.sendMessage("Your partner is in siege, you can't go to your partner at this time.");
			return false;
		}
		else if (playerB.isInOlympiadMode())
		{
			playerA.sendMessage("Your partner is in the Olympiad now.");
			return false;
		}
		else if (playerB.inObserverMode())
		{
			playerA.sendMessage("Your partnet is in Obsverve mode.");
			return false;
		}
		else if (playerB.isInParty() && playerB.getParty().isInDimensionalRift())
		{
			playerA.sendMessage("Your partner is in dimensional rift.");
			return false;
		}
		else if (playerA.isInDuel())
		{
			playerA.sendMessage("You are in a duel.");
			return false;
		}
		else if (playerA.isFestivalParticipant())
		{
			playerA.sendMessage("You are in a festival.");
			return false;
		}
		else if (playerA.getClan() != null && CastleManager.getInstance().getCastleByOwner(playerA.getClan()) != null && CastleManager.getInstance().getCastleByOwner(playerA.getClan()).getSiege().getIsInProgress())
		{
			playerA.sendMessage("You are in siege, you can't go to your partner.");
			return false;
		}
		else if (playerA.isInJail())
		{
			playerA.sendMessage("You are in jail!");
			return false;
		}
		else if (playerA.isInOlympiadMode())
		{
			playerA.sendMessage("You are in the Olympiad.");
			return false;
		}
		else if (playerA.inObserverMode())
		{
			playerA.sendMessage("You are in Obsverve mode.");
			return false;
		}
		else if (playerA.isInParty() && playerA.getParty().isInDimensionalRift())
		{
			playerA.sendMessage("You are in the dimensional rift.");
			return false;
		}
		else if (playerA.isCursedWeaponEquiped())
		{
			playerA.sendMessage("You Cannot Teleport To Your Partner When You Have a Cursed Weapon Equipped.");
			return false;
		}
		// get teleport time
		int _portTimer = Config.WEDDING_TELEPORT_INTERVAL * 1000;
		// send message & get payment
		playerA.sendMessage("After " + Config.WEDDING_TELEPORT_INTERVAL + " sec. you will be teleported to your fiance.");
		playerA.getInventory().reduceAdena("Wedding", Config.WEDDING_TELEPORT_PRICE, playerA, null);
		playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// soe animation
		playerA.setTarget(playerA);
		playerA.disableAllSkills();
		Broadcast.toSelfAndKnownPlayersInRadius(playerA, new MagicSkillUser(playerA, 1050, 1, _portTimer, 0), 810000);
		playerA.sendPacket(new SetupGauge(0, _portTimer));
		// thread for escape
		EscapeFinalizer ef = new EscapeFinalizer(playerA, playerB.getX(), playerB.getY(), playerB.getZ(), playerB.isIn7sDungeon());
		playerA.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, _portTimer));
		playerA.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + _portTimer / GameTimeController.MILLIS_IN_TICK);
		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private int _partnerx;
		private int _partnery;
		private int _partnerz;
		private boolean _to7sDungeon;

		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_activeChar = activeChar;
			_to7sDungeon = to7sDungeon;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
		}

		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			_activeChar.setIsIn7sDungeon(_to7sDungeon);
			_activeChar.enableAllSkills();
			try
			{
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * @see interlude.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
