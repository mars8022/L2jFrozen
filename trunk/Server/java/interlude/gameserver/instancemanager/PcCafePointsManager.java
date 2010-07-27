/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javolution.text.TypeFormat;
import interlude.Config;
import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ExPCCafePointInfo;
import interlude.gameserver.network.serverpackets.SystemMessage;
import interlude.util.Rnd;

public class PcCafePointsManager
{
    protected static Log _log = LogFactory.getLog(PcCafePointsManager.class);

    private static PcCafePointsManager _instance;
    public static boolean _enabled;
    static int _acquisitionPoints = 1;
    static int _doubleAcquisitionPointsChance = 1;
    static double _acquisitionPointsRate = 1.0;
    static boolean _acquisitionPointsRandom = false;
    static boolean _enableDoubleAcquisitionPoints = false;

    public static PcCafePointsManager getInstance()
    {
        if (_instance == null)
            _instance = new PcCafePointsManager();

        return _instance;
    }

    public PcCafePointsManager()
    {
        loadConfig();
    }

    public void givePcCafePoint(final L2PcInstance player, final long givedexp)
    {
        if (!_enabled) return;

        if (player.isInsideZone(L2Character.ZONE_PEACE)
                || player.isInsideZone(L2Character.ZONE_PVP)
                || player.isInsideZone(L2Character.ZONE_SIEGE)
                || player.isOnline() == 0 || player.isInJail())
            return;

        int _points = (int) (givedexp * 0.0001 * _acquisitionPointsRate);

        if (_acquisitionPointsRandom) _points = Rnd.get(_points / 2, _points);

        if (_enableDoubleAcquisitionPoints && Rnd.get(100) < _doubleAcquisitionPointsChance)
        {
            _points *= 2;
            player.setPcBangPoints(player.getPcBangPoints() + _points);
            final SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
            sm.addNumber(_points);
            player.sendPacket(sm);
            player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, true, 1));
        }
        else
        {
            player.setPcBangPoints(player.getPcBangPoints() + _points);
            player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, false, 1));
        }
    }

    public boolean loadConfig()
    {
        try
        {
            final InputStream is = new FileInputStream(new File(Config.GENERAL_CONFIG_FILE));
            final Properties pccaffeSettings = new Properties();
            pccaffeSettings.load(is);
            is.close();

            _enabled = TypeFormat.parseBoolean(pccaffeSettings.getProperty("Enabled", "false"));
            _acquisitionPointsRandom = TypeFormat.parseBoolean(pccaffeSettings.getProperty("AcquisitionPointsRandom", "false"));
            _enableDoubleAcquisitionPoints = TypeFormat.parseBoolean(pccaffeSettings.getProperty("DoublingAcquisitionPoints", "false"));
            _doubleAcquisitionPointsChance = Integer.decode(pccaffeSettings.getProperty("DoublingAcquisitionPointsChance", "1"));
            _acquisitionPointsRate = Double.parseDouble(pccaffeSettings.getProperty("AcquisitionPointsRate", "1.0"));
            return _enabled;
        }
        catch (final Exception e)
        {
            //_initialized = false;
            _log.error("Error while loading pc cafe data.", e);
            return false;
        }
    }
}
