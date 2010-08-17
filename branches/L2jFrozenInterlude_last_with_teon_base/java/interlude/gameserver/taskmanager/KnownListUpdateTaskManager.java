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
package interlude.gameserver.taskmanager;

import java.util.logging.Logger;

import interlude.Config;
import interlude.gameserver.ThreadPoolManager;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.L2Object;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.L2WorldRegion;
import interlude.gameserver.model.actor.instance.L2GuardInstance;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;


public class KnownListUpdateTaskManager
{
    protected static final Logger _log = Logger.getLogger(KnownListUpdateTaskManager.class.getName());

    private final static int FULL_UPDATE_TIMER = 100;
    // Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
    public static int _fullUpdateTimer = FULL_UPDATE_TIMER;
    public static boolean updatePass = true;

    private KnownListUpdateTaskManager()
    {
        ThreadPoolManager.getInstance().scheduleAi(new KnownListUpdate(),1000);
    }

    public static KnownListUpdateTaskManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private class KnownListUpdate implements Runnable
    {
    	protected KnownListUpdate()
    	{
        }

        public void run()
        {
        	try
            {
            	for (L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
            	{
            		for (L2WorldRegion r : regions) // go through all world regions
            		{
                        // avoid stopping update if something went wrong in updateRegion()
            			try
            			{
	            			if (r.isActive()) // and check only if the region is active
	            			{
	        					updateRegion(r, (_fullUpdateTimer == FULL_UPDATE_TIMER), updatePass);
	            			}
            			}
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
            		}
            	}
            }
            catch (Exception e)
            {
            	_log.warning(e.toString());
			}
            updatePass = !updatePass;
            if (_fullUpdateTimer > 0) {
				_fullUpdateTimer--;
			} else {
				_fullUpdateTimer = FULL_UPDATE_TIMER;
			}
            ThreadPoolManager.getInstance().scheduleAi(new KnownListUpdate(), Config.KNOWNLIST_UPDATE_INTERVAL);
        }
    }

    public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
    {
    	for (L2Object object : region.getVisibleObjects()) // and for all members in region
		{
        	if (object == null || !object.isVisible()) {
				continue;   // skip dying objects
			}
        	if (forgetObjects)
        	{
        		object.getKnownList().forgetObjects((object instanceof L2PlayableInstance || Config.GUARD_ATTACK_AGGRO_MOB && object instanceof L2GuardInstance || fullUpdate));
                continue;
        	}
        	if (object instanceof L2PlayableInstance || Config.GUARD_ATTACK_AGGRO_MOB && object instanceof L2GuardInstance || fullUpdate)
        	{
        		for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
        		{
        			for (L2Object _object : regi.getVisibleObjects())
        			{
        				if (_object != object)
        				{
        					object.getKnownList().addKnownObject(_object);
        				}
        			}
        		}
        	}
        	else if (object instanceof L2Character)
        	{
        		for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
        		{
        			if (regi.isActive()) {
						for (L2Object _object : regi.getVisiblePlayable())
						{
							if (_object != object)
							{
								object.getKnownList().addKnownObject(_object);
							}
						}
					}
        		}
        	}
		}
    }
    private static final class SingletonHolder
    {
        protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
    }
}
