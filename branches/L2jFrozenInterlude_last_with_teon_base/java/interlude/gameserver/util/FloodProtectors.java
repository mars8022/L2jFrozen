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
package interlude.gameserver.util;

import interlude.Config;
import interlude.gameserver.model.actor.instance.L2PcInstance;

/**
 * Collection of flood protectors for single player.
 *
 * @author fordfrog edit Danielmwx
 */
public final class FloodProtectors
{
	/**
	 * Use-item flood protector.
	 */
	private final FloodProtectorAction _useItem;
	/**
	 * Roll-dice flood protector.
	 */
	private final FloodProtectorAction _rollDice;
	/**
	 * Firework flood protector.
	 */
	private final FloodProtectorAction _firework;
	/**
	 * Item-pet-summon flood protector.
	 */
	private final FloodProtectorAction _itemPetSummon;
	/**
	 * Hero-voice flood protector.
	 */
	private final FloodProtectorAction _heroVoice;
	/**
	 * Subclass flood protector.
	 */
	private final FloodProtectorAction _subclass;
	/**
	 * Drop-item flood protector.
	 */
	private final FloodProtectorAction _dropItem;
	/**
	 * Wnk flood protector.
	 */
	private final FloodProtectorAction _wnk;
	/**
	 * Server-bypass flood protector.
	 */
	private final FloodProtectorAction _serverBypass;
	/**
	 * Buffer flood protector.
	 */
	private final FloodProtectorAction _buffer;
	/**
	 * Craft flood protector.
	 */
	private final FloodProtectorAction _craft;
	/**
	 * Multisell flood protector.
	 */
	private final FloodProtectorAction _multisell;
	/**
	 * Banking System Flood Protector.
	 */
	private final FloodProtectorAction _BankingSystem;
	/**
	 * Werehouse Flood Protector.
	 */
	private final FloodProtectorAction _werehouse;
	/**
	 * Misc Flood Protector.
	 */
	private final FloodProtectorAction _misc;
	/**
	 * Chat Flood Protector.
	 */
	private final FloodProtectorAction _chat;
	/**
	 * Chat Global Flood Protector.
	 */
	private final FloodProtectorAction _global;
	/**
	 * Chat trade Flood Protector.
	 */
	private final FloodProtectorAction _trade;
	/**
	 * Potion Flood Protector.
	 */
	private final FloodProtectorAction _potion;

	/**
	 * Creates new instance of FloodProtectors.
	 *
	 * @param player
	 *            player for which the collection of flood protectors is being created.
	 */
	public FloodProtectors(final L2PcInstance player)
	{
		super();
		_useItem = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_USE_ITEM);
		_rollDice = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_ROLL_DICE);
		_firework = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_FIREWORK);
		_itemPetSummon = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		_heroVoice = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_HERO_VOICE);
		_subclass = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_SUBCLASS);
		_dropItem = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_DROP_ITEM);
		_wnk = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_UNK_PACKETS);
		_serverBypass = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		_buffer = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_BUFFER);
		_craft = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_CRAFT);
		_multisell = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_MULTISELL);
		_BankingSystem = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_BANKING_SYSTEM);
		_werehouse = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_WEREHOUSE);
		_misc = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_MISC);
		_chat = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_CHAT);
		_global = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_GLOBAL);
		_trade = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_TRADE);
		_potion = new FloodProtectorAction(player, Config.FLOOD_PROTECTOR_POTION);
	}

	/**
	 * Returns {@link #_useItem}.
	 *
	 * @return {@link #_useItem}
	 */
	public FloodProtectorAction getUseItem()
	{
		return _useItem;
	}

	/**
	 * Returns {@link #_rollDice}.
	 *
	 * @return {@link #_rollDice}
	 */
	public FloodProtectorAction getRollDice()
	{
		return _rollDice;
	}

	/**
	 * Returns {@link #_firework}.
	 *
	 * @return {@link #_firework}
	 */
	public FloodProtectorAction getFirework()
	{
		return _firework;
	}

	/**
	 * Returns {@link #_itemPetSummon}.
	 *
	 * @return {@link #_itemPetSummon}
	 */
	public FloodProtectorAction getItemPetSummon()
	{
		return _itemPetSummon;
	}

	/**
	 * Returns {@link #_heroVoice}.
	 *
	 * @return {@link #_heroVoice}
	 */
	public FloodProtectorAction getHeroVoice()
	{
		return _heroVoice;
	}

	/**
	 * Returns {@link #_subclass}.
	 *
	 * @return {@link #_subclass}
	 */
	public FloodProtectorAction getSubclass()
	{
		return _subclass;
	}

	/**
	 * Returns {@link #_dropItem}.
	 *
	 * @return {@link #_dropItem}
	 */
	public FloodProtectorAction getDropItem()
	{
		return _dropItem;
	}

	/**
	 * Returns {@link #_wnk}.
	 *
	 * @return {@link #_wnk}
	 */
	public FloodProtectorAction getWnk()
	{
		return _wnk;
	}

	/**
	 * Returns {@link #_serverBypass}.
	 *
	 * @return {@link #_serverBypass}
	 */
	public FloodProtectorAction getServerBypass()
	{
		return _serverBypass;
	}

	/**
	 * Returns {@link #_buffer}.
	 *
	 * @return {@link #_buffer}
	 */
	public FloodProtectorAction getBuffer()
	{
		return _buffer;
	}

	/**
	 * Returns {@link #_craft}.
	 *
	 * @return {@link #_craft}
	 */
	public FloodProtectorAction getCraft()
	{
		return _craft;
	}

	/**
	 * Returns {@link #_multisell}.
	 *
	 * @return {@link #_multisell}
	 */
	public FloodProtectorAction getMultisell()
	{
		return _multisell;
	}

	/**
	 * Returns {@link #_BankingSystem}.
	 *
	 * @return {@link #_BankingSystem}
	 */
	public FloodProtectorAction getBankingSystem()
	{
		return _BankingSystem;
	}

	/**
	 * Returns {@link #_werehouse}.
	 *
	 * @return {@link #_werehouse}
	 */
	public FloodProtectorAction getWerehouse()
	{
		return _werehouse;
	}

	/**
	 * Returns {@link #_misc}.
	 *
	 * @return {@link #_misc}
	 */
	public FloodProtectorAction getMisc()
	{
		return _misc;
	}

	/**
	 * Returns {@link #_chat}.
	 *
	 * @return {@link #_chat}
	 */
	public FloodProtectorAction getChat()
	{
		return _chat;
	}

	/**
	 * Returns {@link #_global}.
	 *
	 * @return {@link #_global}
	 */
	public FloodProtectorAction getGlobal()
	{
		return _global;
	}

	/**
	 * Returns {@link #_trade}.
	 *
	 * @return {@link #_trade}
	 */
	public FloodProtectorAction getTrade()
	{
		return _trade;
	}

	/**
	 * Returns {@link #_potion}.
	 *
	 * @return {@link #_potion}
	 */
	public FloodProtectorAction getPotion()
	{
		return _potion;
	}

    public static FloodProtectorAction getInstance() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
