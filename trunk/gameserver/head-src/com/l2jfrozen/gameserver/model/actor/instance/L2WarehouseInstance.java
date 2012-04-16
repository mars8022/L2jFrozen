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
package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.PcFreight;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EnchantResult;
import com.l2jfrozen.gameserver.network.serverpackets.PackageToList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2jfrozen.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.10 $ $Date: 2005/04/06 16:13:41 $
 */
public final class L2WarehouseInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2WarehouseInstance.class.getName());

	/**
	 * Instantiates a new l2 warehouse instance.
	 *
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance#getHtmlPath(int, int)
	 */
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if(val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/warehouse/" + pom + ".htm";
	}

	/**
	 * Show retrieve window.
	 *
	 * @param player the player
	 */
	private void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if(player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}

		if(Config.DEBUG)
		{
			_log.fine("Showing stored items");
		}
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}

	/**
	 * Show deposit window.
	 *
	 * @param player the player
	 */
	private void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInvetoryDisable();
		if(Config.DEBUG)
		{
			_log.fine("Showing items to deposit");
		}

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}

	/**
	 * Show deposit window clan.
	 *
	 * @param player the player
	 */
	private void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if(player.getClan() != null)
		{
			if(player.getClan().getLevel() == 0)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInvetoryDisable();
				if(Config.DEBUG)
				{
					_log.fine("Showing items to deposit - clan");
				}

				WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
				player.sendPacket(dl);
				dl = null;
			}
		}
	}

	/**
	 * Show withdraw window clan.
	 *
	 * @param player the player
	 */
	private void showWithdrawWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
			return;
		}
		
		if(player.getClan().getLevel() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			if(Config.DEBUG)
			{
				_log.fine("Showing items to deposit - clan");
			}
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
	}

	/**
	 * Show withdraw window freight.
	 *
	 * @param player the player
	 */
	private void showWithdrawWindowFreight(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if(Config.DEBUG)
		{
			_log.fine("Showing freightened items");
		}

		PcFreight freight = player.getFreight();

		if(freight != null)
		{
			if(freight.getSize() > 0)
			{
				if(Config.ALT_GAME_FREIGHTS)
				{
					freight.setActiveLocation(0);
				}
				else
				{
					freight.setActiveLocation(getWorldRegion().hashCode());
				}
				player.setActiveWarehouse(freight);
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			}
		}
		else
		{
			if(Config.DEBUG)
			{
				_log.fine("no items freightened");
			}
		}
		freight = null;
	}

	/**
	 * Show deposit window freight.
	 *
	 * @param player the player
	 */
	private void showDepositWindowFreight(L2PcInstance player)
	{
		// No other chars in the account of this player
		if(player.getAccountChars().size() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
		}
		// One or more chars other than this player for this account
		else
		{

			Map<Integer, String> chars = player.getAccountChars();

			if(chars.size() < 1)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			player.sendPacket(new PackageToList(chars));
			chars = null;

			if(Config.DEBUG)
			{
				_log.fine("Showing destination chars to freight - char src: " + player.getName());
			}
		}
	}

	/**
	 * Show deposit window freight.
	 *
	 * @param player the player
	 * @param obj_Id the obj_ id
	 */
	private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		L2PcInstance destChar = L2PcInstance.load(obj_Id);
		if(destChar == null)
		{
			// Something went wrong!
			if(Config.DEBUG)
			{
				_log.warning("Error retrieving a target object for char " + player.getName() + " - using freight.");
			}
			return;
		}

		PcFreight freight = destChar.getFreight();
		if(Config.ALT_GAME_FREIGHTS)
		{
			freight.setActiveLocation(0);
		}
		else
		{
			freight.setActiveLocation(getWorldRegion().hashCode());
		}
		player.setActiveWarehouse(freight);
		player.tempInvetoryDisable();
		destChar.deleteMe();
		destChar = null;
		freight = null;

		if(Config.DEBUG)
		{
			_log.fine("Showing items to freight");
		}

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.FREIGHT));
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance#onBypassFeedback(com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// Like L2OFF if you have enchant window opened it will be closed
		if(player.getActiveEnchantItem() != null)
			player.sendPacket(new EnchantResult(0));

		if(command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if(command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if(command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if(command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if(command.startsWith("WithdrawF"))
		{
			if(Config.ALLOW_FREIGHT)
			{
				showWithdrawWindowFreight(player);
			}
		}
		else if(command.startsWith("DepositF"))
		{
			if(Config.ALLOW_FREIGHT)
			{
				showDepositWindowFreight(player);
			}
		}
		else if(command.startsWith("FreightChar"))
		{
			if(Config.ALLOW_FREIGHT)
			{
				int startOfId = command.lastIndexOf("_") + 1;
				String id = command.substring(startOfId);
				showDepositWindowFreight(player, Integer.parseInt(id));
			}
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}
}
