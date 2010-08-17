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
package interlude.gameserver.model.zone.type;

import javolution.util.FastList;
import interlude.Config;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.util.Rnd;

import org.w3c.dom.Node;

/**
 * A Town zone
 *
 * @author durgus
 */
public class L2TownZone extends L2ZoneType
{
	private String _townName;
	private int _townId;
	private int _redirectTownId;
	private int _taxById;
	private FastList<int[]> _spawnLocs;
	private boolean _noPeace;

	public L2TownZone(int id)
	{
		super(id);
		_taxById = 0;
		_spawnLocs = new FastList<int[]>();
		_redirectTownId = 9;
		_noPeace = false;
	}

	@Override
	public void setParameter(String s, String s1)
	{
		if (s.equals("name")) {
			_townName = s1;
		} else if (s.equals("townId")) {
			_townId = Integer.parseInt(s1);
		} else if (s.equals("redirectTownId")) {
			_redirectTownId = Integer.parseInt(s1);
		} else if (s.equals("taxById")) {
			_taxById = Integer.parseInt(s1);
		} else if (s.equals("isPeaceZone")) {
			_noPeace = Boolean.parseBoolean(s1);
		} else {
			super.setParameter(s, s1);
		}
	}

	@Override
	public void setSpawnLocs(Node node)
	{
		int ai[] = new int[3];
		Node node1 = node.getAttributes().getNamedItem("X");
		if (node1 != null) {
			ai[0] = Integer.parseInt(node1.getNodeValue());
		}
		node1 = node.getAttributes().getNamedItem("Y");
		if (node1 != null) {
			ai[1] = Integer.parseInt(node1.getNodeValue());
		}
		node1 = node.getAttributes().getNamedItem("Z");
		if (node1 != null) {
			ai[2] = Integer.parseInt(node1.getNodeValue());
		}
		if (ai != null) {
			_spawnLocs.add(ai);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && ((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1) {
			return;
		}
		if (!_noPeace && Config.ZONE_TOWN != 2) {
			character.setInsideZone(2, true);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (!_noPeace) {
			character.setInsideZone(2, false);
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public String getName()
	{
		return _townName;
	}

	public int getTownId()
	{
		return _townId;
	}

	public int getRedirectTownId()
	{
		return _redirectTownId;
	}

	public final int[] getSpawnLoc()
	{
		int ai[] = new int[3];
		ai = _spawnLocs.get(Rnd.get(_spawnLocs.size()));
		return ai;
	}

	public final int getTaxById()
	{
		return _taxById;
	}

	public final boolean isPeaceZone()
	{
		return _noPeace;
	}
}
