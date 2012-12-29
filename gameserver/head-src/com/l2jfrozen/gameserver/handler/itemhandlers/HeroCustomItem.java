// Hero Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   HeroCustomItem.java
// Modded by programmos, sword dev

package com.l2jfrozen.gameserver.handler.itemhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class HeroCustomItem implements IItemHandler
{

	public HeroCustomItem()
	{
	//null
	}

	protected static final Logger _log = Logger.getLogger(HeroCustomItem.class.getName());
	
	String INSERT_DATA = "REPLACE INTO characters_custom_data (obj_Id, char_name, hero, noble, donator, hero_end_date) VALUES (?,?,?,?,?,?)";

	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(Config.HERO_CUSTOM_ITEMS)
		{
			if(!(playable instanceof L2PcInstance))
				return;

			L2PcInstance activeChar = (L2PcInstance) playable;

			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
			}

			if(activeChar.isHero())
			{
				activeChar.sendMessage("You Are Already A Hero!.");
			}
			else
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.setHero(true);
				updateDatabase(activeChar, Config.HERO_CUSTOM_DAY * 24L * 60L * 60L * 1000L);
				activeChar.sendMessage("You Are Now a Hero,You Are Granted With Hero Status , Skills ,Aura.");
				activeChar.broadcastUserInfo();
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.getInventory().addItem("Wings", 6842, 1, activeChar, null);
			}
			activeChar = null;
		}
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	private void updateDatabase(L2PcInstance player, long heroTime)
	{
		Connection con = null;
		try
		{
			if(player == null)
				return;

			con = L2DatabaseFactory.getInstance().getConnection(false);
			PreparedStatement stmt = con.prepareStatement(INSERT_DATA);

			stmt.setInt(1, player.getObjectId());
			stmt.setString(2, player.getName());
			stmt.setInt(3, 1);
			stmt.setInt(4, player.isNoble() ? 1 : 0);
			stmt.setInt(5, player.isDonator() ? 1 : 0);
			stmt.setLong(6, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
			stmt.execute();
			stmt.close();
			stmt = null;
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error: could not update database: ", e);
		}
		finally
		{
			CloseUtil.close(con);
			
			con = null;
		}
	}

	private static final int ITEM_IDS[] =
	{
		Config.HERO_CUSTOM_ITEM_ID
	};

}
