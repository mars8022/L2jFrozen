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
package com.l2jfrozen.gameserver.model.actor.position;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.Point3D;

/**
 * The Class ObjectPosition.
 */
public class ObjectPosition
{
	
	/** The Constant _log. */
	private static final Logger _log = Logger.getLogger(ObjectPosition.class.getName());

	// =========================================================
	// Data Field
	/** The _active object. */
	private L2Object _activeObject;
	
	/** The _heading. */
	private int _heading = 0;
	
	/** The _world position. */
	private Point3D _worldPosition;
	
	/** The _world region. */
	private L2WorldRegion _worldRegion; // Object localization : Used for items/chars that are seen in the world

	/** The _changing region. */
	private Boolean _changingRegion = false;
	
	// =========================================================
	// Constructor
	/**
	 * Instantiates a new object position.
	 *
	 * @param activeObject the active object
	 */
	public ObjectPosition(L2Object activeObject)
	{
		_activeObject = activeObject;
		setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
	}

	// =========================================================
	// Method - Public
	/**
	 * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.<BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion != null</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Update position during and after movement, or after teleport</li><BR>
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setXYZ(int x, int y, int z)
	{
		if(Config.ASSERT)
		{
			assert getWorldRegion() != null;
		}

		setWorldPosition(x, y, z);

		try
		{
			if(L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
			{
				updateWorldRegion();
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.warning("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");

			if(getActiveObject() instanceof L2PcInstance)
			{
				//((L2PcInstance)obj).deleteMe();
				((L2PcInstance) getActiveObject()).teleToLocation(0, 0, 0, false);
				((L2PcInstance) getActiveObject()).sendMessage("Error with your coords, Please ask a GM for help!");
			
			}else if(getActiveObject() instanceof L2Character)
			{
				getActiveObject().decayMe();
			}
			
		}
	}

	/**
	 * Set the x,y,z position of the L2Object and make it invisible.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldregion==null <I>(L2Object is invisible)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Create a Door</li> <li>Restore L2PcInstance</li><BR>
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setXYZInvisible(int x, int y, int z)
	{
		if(Config.ASSERT)
		{
			assert getWorldRegion() == null;
		}
		if(x > L2World.MAP_MAX_X)
		{
			x = L2World.MAP_MAX_X - 5000;
		}

		if(x < L2World.MAP_MIN_X)
		{
			x = L2World.MAP_MIN_X + 5000;
		}

		if(y > L2World.MAP_MAX_Y)
		{
			y = L2World.MAP_MAX_Y - 5000;
		}

		if(y < L2World.MAP_MIN_Y)
		{
			y = L2World.MAP_MIN_Y + 5000;
		}

		setWorldPosition(x, y, z);
		getActiveObject().setIsVisible(false);
	}

	/**
	 * checks if current object changed its region, if so, update referencies.
	 */
	public void updateWorldRegion()
	{
		if(!getActiveObject().isVisible())
			return;

		L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
		if(newRegion != getWorldRegion())
		{
			getWorldRegion().removeVisibleObject(getActiveObject());

			setWorldRegion(newRegion);

			// Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			getWorldRegion().addVisibleObject(getActiveObject());
		}

		newRegion = null;
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	/**
	 * Gets the active object.
	 *
	 * @return the active object
	 */
	public final L2Object getActiveObject()
	{
		return _activeObject;
	}

	/**
	 * Gets the heading.
	 *
	 * @return the heading
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Sets the heading.
	 *
	 * @param value the new heading
	 */
	public final void setHeading(int value)
	{
		_heading = value;
	}

	/**
	 * Return the x position of the L2Object.
	 *
	 * @return the x
	 */
	public final int getX()
	{
		return getWorldPosition().getX();
	}

	/**
	 * Sets the x.
	 *
	 * @param value the new x
	 */
	public final void setX(int value)
	{
		getWorldPosition().setX(value);
	}

	/**
	 * Return the y position of the L2Object.
	 *
	 * @return the y
	 */
	public final int getY()
	{
		return getWorldPosition().getY();
	}

	/**
	 * Sets the y.
	 *
	 * @param value the new y
	 */
	public final void setY(int value)
	{
		getWorldPosition().setY(value);
	}

	/**
	 * Return the z position of the L2Object.
	 *
	 * @return the z
	 */
	public final int getZ()
	{
		return getWorldPosition().getZ();
	}

	/**
	 * Sets the z.
	 *
	 * @param value the new z
	 */
	public final void setZ(int value)
	{
		getWorldPosition().setZ(value);
	}

	/**
	 * Gets the world position.
	 *
	 * @return the world position
	 */
	public final Point3D getWorldPosition()
	{
		if(_worldPosition == null)
		{
			_worldPosition = new Point3D(0, 0, 0);
		}

		return _worldPosition;
	}

	/**
	 * Sets the world position.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setWorldPosition(int x, int y, int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}

	/**
	 * Sets the world position.
	 *
	 * @param newPosition the new world position
	 */
	public final void setWorldPosition(Point3D newPosition)
	{
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}

	/**
	 * Gets the world region.
	 *
	 * @return the world region
	 */
	public final L2WorldRegion getWorldRegion()
	{
		synchronized(_changingRegion){
			_changingRegion = false;
			return _worldRegion;
		}
	}

	/**
	 * Sets the world region.
	 *
	 * @param value the new world region
	 */
	public final void setWorldRegion(L2WorldRegion value)
	{
		synchronized(_changingRegion){
			_changingRegion = true;
			_worldRegion = value;
		}
	}
}
