/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.crypt.nProtect;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author zabbix Lets drink to code! Unknown Packet:ca 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00
 *         E....7..........
 */

// By Azagthtot РќР°С‡РЅРµРј СЂРµР°Р»РёР·Р°С†РёСЋ РєР»РёРµРЅС‚СЃРєРѕР№ Р·Р°С‰РёС‚С‹.... С…РµС…

public class GameGuardReply extends L2GameClientPacket
{
	private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";
	//private static final Logger _log = Logger.getLogger(GameGuardReply.class.getName());
	private int[] _reply = new int[4];

	@Override
	protected void readImpl()
	{
		_reply[0] = readD(); // 32 Р±РёС‚Р° РґР»СЏ РєР»СЋС‡Р° СѓРЅРёРєР°Р»СЊРЅРѕСЃС‚Рё СЃРµСЂРІРµСЂР° РЅР°Рј С…РІР°С‚РёС‚
		_reply[1] = readD(); // РЎРѕР»РµРЅРёРµ... Р·Р°Р±РёРІР°РµРј СЌС‚РѕС‚ int С‡РµРј СѓРіРѕРґРЅРѕ, СЃРєСЂС‹РІР°РµРј РїР°РєРµС‚С‹
		_reply[2] = readD(); // РЈРќРёРєР°Р»СЊРЅС‹Р№ РёРґРµРЅС‚РёС„РёРєР°С‚РѕСЂ РєР»РёРµРЅС‚Р° 
		_reply[3] = readD(); // РџРѕСЃРѕР»РµРЅРѕРµ РїРѕР»Рµ, РЅРѕ 4-С‹Р№ Р±РёС‚ РІ 1 РѕР·РЅР°С‡РµС‚ С‡С‚Рѕ Р·Р°РїСѓС‰РµРЅ РЅРµС…РѕСЂРѕС€РёРє Рё РєР»РёРµРЅС‚Р° РЅР°РґРѕ РєР°Р·РЅРёС‚СЊ
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!nProtect.getInstance().checkGameGuardRepy(getClient(), _reply))
			return;
		getClient().setGameGuardOk(true);
	}

	@Override
	public String getType()
	{
		return _C__CA_GAMEGUARDREPLY;
	}

}
