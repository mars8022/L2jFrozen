/* L2jFrozen Project - www.l2jfrozen.com 
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

import java.util.Vector;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public final class L2Radar
{
	private final L2PcInstance _player;
	private final Vector<RadarMarker> _markers;
	
	public L2Radar(final L2PcInstance player)
	{
		_player = player;
		_markers = new Vector<>();
	}
	
	// Add a marker to player's radar
	public void addMarker(final int x, final int y, final int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		_markers.add(newMarker);
		_player.sendPacket(new RadarControl(0, 1, x, y, z));
		
		newMarker = null;
	}
	
	// Remove a marker from player's radar
	public void removeMarker(final int x, final int y, final int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		_markers.remove(newMarker);
		_player.sendPacket(new RadarControl(1, 1, x, y, z));
		
		newMarker = null;
	}
	
	public void removeAllMarkers()
	{
		// TODO: Need method to remove all markers from radar at once
		for (final RadarMarker tempMarker : _markers)
		{
			_player.sendPacket(new RadarControl(1, tempMarker._type, tempMarker._x, tempMarker._y, tempMarker._z));
		}
		
		_markers.removeAllElements();
	}
	
	public void loadMarkers()
	{
		// TODO: Need method to re-send radar markers after load/teleport/death
		// etc.
	}
	
	private static class RadarMarker
	{
		// Simple class to model radar points.
		public int _type, _x, _y, _z;
		
		@SuppressWarnings("unused")
		public RadarMarker(final int type, final int x, final int y, final int z)
		{
			_type = type;
			_x = x;
			_y = y;
			_z = z;
		}
		
		public RadarMarker(final int x, final int y, final int z)
		{
			_type = 1;
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			try
			{
				RadarMarker temp = (RadarMarker) obj;
				
				if (temp._x == _x && temp._y == _y && temp._z == _z && temp._type == _type)
					return true;
				
				temp = null;
				
				return false;
			}
			catch (final Exception e)
			{
				return false;
			}
		}
	}
	
	public class RadarOnPlayer implements Runnable
	{
		private final L2PcInstance _myTarget, _me;
		
		public RadarOnPlayer(final L2PcInstance target, final L2PcInstance me)
		{
			_me = me;
			_myTarget = target;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_me == null || _me.isOnline() == 0)
					return;
				_me.sendPacket(new RadarControl(1, 1, _me.getX(), _me.getY(), _me.getZ()));
				if (_myTarget == null || _myTarget.isOnline() == 0 || !_myTarget._haveFlagCTF)
				{
					return;
				}
				_me.sendPacket(new RadarControl(0, 1, _myTarget.getX(), _myTarget.getY(), _myTarget.getZ()));
				ThreadPoolManager.getInstance().scheduleGeneral(new RadarOnPlayer(_myTarget, _me), 15000);
			}
			catch (final Throwable t)
			{
			}
		}
	}
}
