/*
 * $Header: BlockList.java, 21/11/2005 14:53:53 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/11/2005 14:53:53 $
 * $Revision: 1 $
 * $Log: BlockList.java,v $
 * Revision 1  21/11/2005 14:53:53  luisantonioa
 * Added copyright notice
 *
 *
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
package com.l2jfrozen.gameserver.model;

import java.util.Set;

import javolution.util.FastSet;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class BlockList
{
	private Set<String> _blockSet;
	private boolean _blockAll;
	private L2PcInstance _owner;
	
	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockSet = new FastSet<String>();
		_blockAll = false;
	}

	public void addToBlockList(String character)
	{
		if(character != null)
		{
			_blockSet.add(character);
			
			SystemMessage sm = null;
			
			L2PcInstance target = L2World.getInstance().getPlayer(character);
			if(target != null){
				sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
				sm.addString(_owner.getName());
				target.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
			sm.addString(character);
			_owner.sendPacket(sm);
		}
	}

	public void removeFromBlockList(String character)
	{
		if(character != null)
		{
			_blockSet.remove(character);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
			sm.addString(character);
			_owner.sendPacket(sm);
		}
	}

	public boolean isInBlockList(String character)
	{
		return _blockSet.contains(character);
	}
	
	
	public boolean isBlockAll()
	{
		return _blockAll;
	}
	
	public void setBlockAll(boolean state)
	{
		_blockAll = state;
	}

	public Set<String> getBlockList()
	{
		return _blockSet;
	}
	
	/*
	public static boolean isInBlockList(String ownerName, String targetName) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(ownerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot check blocked list because owner is offline");
		}
		
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetName);
	}
	
	public static void add_ToBlockList(String listOwnerName, String targetCharacter) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(listOwnerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot add character to blocked list because owner is offline");
		}
		
		if (listOwner.getFriendList().contains(targetCharacter))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(targetCharacter);
			listOwner.sendPacket(sm);
			return;
		}
		
		listOwner.getBlockList().addToBlockList(targetCharacter);

		SystemMessage sm = null;
		
		L2PcInstance target = L2World.getInstance().getPlayer(targetCharacter);
		if(target != null){
			sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			target.sendPacket(sm);
		}
		
		sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(targetCharacter);
		listOwner.sendPacket(sm);
	
	}

	public static void removeFromBlockList(String listOwnerName, String targetCharacter) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(listOwnerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot remove character from blocked list because owner is offline");
		}
		
		listOwner.getBlockList().removeFromBlockList(targetCharacter);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(targetCharacter);
		listOwner.sendPacket(sm);
	
	}

	public static boolean isBlockingAll(String listOwnerName) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(listOwnerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot check blocked list because owner is offline");
		}
		
		return listOwner.getBlockList().isBlockAll();
	}

	public static void setBlockAll(String listOwnerName, boolean newValue) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(listOwnerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot set blocked list because owner is offline");
		}
		
		listOwner.getBlockList().setBlockAll(newValue);
	}

	public static void sendListToOwner(String listOwnerName) throws Exception
	{
		L2PcInstance listOwner = L2World.getInstance().getPlayer(listOwnerName);
		if(listOwner == null){
			throw new Exception ("Attention: cannot send blocked list because owner is offline");
		}
		
		for(String playerName : listOwner.getBlockList().getBlockList())
		{
			listOwner.sendPacket(new SystemMessage(SystemMessageId.S1_S2).addString(playerName));
		}
	}
	*/

}
