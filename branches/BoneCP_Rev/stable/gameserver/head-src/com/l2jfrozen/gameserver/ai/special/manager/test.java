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
package com.l2jfrozen.gameserver.ai.special.manager;

/**
 * TODO: test class AI extends...
 * 
 * @author programmos
 */
public class test extends AIExtend
{
	private static final int[] ID_MOB =
	{
			1, 2, 3, 4, 5
	};

	public test()
	{
		for(int Mob : ID_MOB)
		{
			super.addAI(Mob);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.ai.special.manager.AIExtend#run()
	 */
	@Override
	public void run()
	{
	// TODO Auto-generated method stub

	}

}
