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
package com.l2jfrozen.gameserver.handler;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Azagthtot
 */
public interface IBBSHandler
{
	/**
	 * @return as String [] _bbs)
	 */
	public String[] getBBSCommands();

	/**
	 * @param command as String - _bbs<br>
	 * @param activeChar as L2PcInstance - Alt+B<br>
	 * @param params as String -
	 */
	public void handleCommand(String command, L2PcInstance activeChar, String params);
}
