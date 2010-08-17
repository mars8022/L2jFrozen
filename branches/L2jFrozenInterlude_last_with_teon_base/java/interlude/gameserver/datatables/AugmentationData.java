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
package interlude.gameserver.datatables;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import interlude.Config;
import interlude.gameserver.model.L2Augmentation;
import interlude.gameserver.model.L2ItemInstance;
import interlude.gameserver.model.L2Skill;
import interlude.gameserver.skills.Stats;
import interlude.util.Rnd;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AugmentationData
{
	private static final Logger _log = Logger.getLogger(AugmentationData.class.getName());

	public static AugmentationData getInstance()
	{
		return SingletonHolder._instance;
	}

	private static final int STAT_START = 1;
	private static final int STAT_END = 14560;
	private static final int STAT_BLOCKSIZE = 3640;
	private static final int STAT_SUBBLOCKSIZE = 91;

    private static final int PURPLE_START = 14578;
    private static final int RED_START = 14685;

	private static final int BASESTAT_STR = 16341;
	private static final int BASESTAT_CON = 16342;
	private static final int BASESTAT_INT = 16343;
	private static final int BASESTAT_MEN = 16344;

	@SuppressWarnings("unchecked")
	private FastList _augmentationStats[];
    private FastList<augmentationSkill> _blueSkills;
    private FastList<augmentationSkill> _purpleSkills;
    private FastList<augmentationSkill> _redSkills;
    private int _skillsCount;

	public AugmentationData()
	{
		_log.info("Initializing AugmentationData.");

		_augmentationStats = new FastList[4];
		_augmentationStats[0] = new FastList<augmentationStat>();
		_augmentationStats[1] = new FastList<augmentationStat>();
		_augmentationStats[2] = new FastList<augmentationStat>();
		_augmentationStats[3] = new FastList<augmentationStat>();

        _blueSkills = new FastList<augmentationSkill>();
        _purpleSkills = new FastList<augmentationSkill>();
        _redSkills = new FastList<augmentationSkill>();

		load();

		_log.info("AugmentationData: Loaded: " + _augmentationStats[0].size() * 4 + " augmentation stats.");
        _log.info("AugmentationData: Loaded: " + _blueSkills.size() + " blue, " + _purpleSkills.size() + " purple and " + _redSkills.size() + " red skills");
        }

	public class augmentationSkill
	{
		private int _skillId;
		private int _maxSkillLevel;
		private int _augmentationSkillId;

		public augmentationSkill(int skillId, int maxSkillLevel, int augmentationSkillId)
		{
			_skillId = skillId;
			_maxSkillLevel = maxSkillLevel;
			_augmentationSkillId = augmentationSkillId;
		}

		public L2Skill getSkill(int level)
		{
			if (level > _maxSkillLevel)
				return SkillTable.getInstance().getInfo(_skillId, _maxSkillLevel);

			return SkillTable.getInstance().getInfo(_skillId, level);
		}

		public int getAugmentationSkillId()
		{
			return _augmentationSkillId;
		}
	}

	public class augmentationStat
	{
		private Stats _stat;
		private int _singleSize;
		private int _combinedSize;
		private float _singleValues[];
		private float _combinedValues[];

		public augmentationStat(Stats stat, float sValues[], float cValues[])
		{
			_stat = stat;
			_singleSize = sValues.length;
			_singleValues = sValues;
			_combinedSize = cValues.length;
			_combinedValues = cValues;
		}

		public int getSingleStatSize()
		{
			return _singleSize;
		}
		public int getCombinedStatSize()
		{
			return _combinedSize;
		}

		public float getSingleStatValue(int i)
		{
			if (i >= _singleSize || i < 0)
				return _singleValues[_singleSize-1];

			return _singleValues[i];
		}
		public float getCombinedStatValue(int i)
		{
			if (i >= _combinedSize || i < 0)
				return _combinedValues[_combinedSize-1];

			return _combinedValues[i];
		}

		public Stats getStat()
		{
			return _stat;
		}
	}

	@SuppressWarnings("unchecked")
	private final void load()
	{
		try
		{
			SkillTable st = SkillTable.getInstance();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File(Config.DATAPACK_ROOT + "/data/stats/augmentation/augmentation_skillmap.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
					System.out.println("The augmentation skillmap file is missing.");

				return;
			}

			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("augmentation".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int skillId=0, augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

							for (Node cd=d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("skillId".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
                            		skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								}

							}

						    if (augmentationId < PURPLE_START)
								_blueSkills.add(new augmentationSkill(skillId, st.getMaxLevel(skillId, 1), augmentationId));
							else if (augmentationId < RED_START)
								_purpleSkills.add(new augmentationSkill(skillId, st.getMaxLevel(skillId, 1), augmentationId));
							else
								_redSkills.add(new augmentationSkill(skillId, st.getMaxLevel(skillId, 1), augmentationId));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error parsing augmentation_skillmap.xml.", e);
			return ;
		}

		for (int i=1; i<5; i++)
		{
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);

				File file = new File(Config.DATAPACK_ROOT + "/data/stats/augmentation/augmentation_stats" + i + ".xml");
				if (!file.exists())
				{
					if (Config.DEBUG)
						System.out.println("The augmentation stat data file " + i + " is missing.");
					return;
				}

				Document doc = factory.newDocumentBuilder().parse(file);

				for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								String statName = attrs.getNamedItem("name").getNodeValue();
								float soloValues[]=null, combinedValues[]=null;

								for (Node cd=d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("table".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										String tableName = attrs.getNamedItem("name").getNodeValue();

										StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
										List<Float> array = new FastList<Float>();
										while (data.hasMoreTokens()) {
											array.add(Float.parseFloat(data.nextToken()));
										}

										if (tableName.equalsIgnoreCase("#soloValues"))
										{
											soloValues = new float[array.size()];
											int x=0;
											for (float value : array)
											{
												soloValues[x++] = value;
											}
										}
										else
										{
											combinedValues = new float[array.size()];
											int x=0;
											for (float value : array)
											{
												combinedValues[x++] = value;
											}
										}
									}
								}
								((FastList<augmentationStat>)
										_augmentationStats[(i-1)]).add(new augmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error parsing augmentation_stats"+i+".xml.", e);
				return ;
			}
		}
	}

	public L2Augmentation generateRandomAugmentation(L2ItemInstance item, int lifeStoneLevel, int lifeStoneGrade)
	{
        int skill_Chance = 0;
        int stat34 = 0;
        boolean generateSkill = false;
        int resultColor = 0;
        boolean generateGlow = false;
        switch (lifeStoneGrade)
        {
        	case 0:
        			skill_Chance = Config.AUGMENTATION_NG_SKILL_CHANCE;
        				if (Rnd.get(1,100) <= Config.AUGMENTATION_NG_GLOW_CHANCE)
        					generateGlow = true;
        				break;
        	case 1:
        			skill_Chance = Config.AUGMENTATION_MID_SKILL_CHANCE;
        				if (Rnd.get(1,100) <= Config.AUGMENTATION_MID_GLOW_CHANCE)
        					generateGlow = true;
        				break;
        	case 2:
        			skill_Chance = Config.AUGMENTATION_HIGH_SKILL_CHANCE;
        				if (Rnd.get(1,100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE)
        					generateGlow = true;
        				break;
        	case 3:
        			skill_Chance = Config.AUGMENTATION_TOP_SKILL_CHANCE;
        				if (Rnd.get(1,100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE)
        					generateGlow = true;
        }
        if (Rnd.get(1, 100) <= skill_Chance)
        	generateSkill = true;
        else
        	if (Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE)
        		stat34 = Rnd.get(BASESTAT_STR, BASESTAT_MEN);
        if (stat34 == 0 && !generateSkill)
        {
        	resultColor = Rnd.get(0, 100);
        	if (resultColor <= 15 * lifeStoneGrade + 40)
        		resultColor = 1;
        	else
        		resultColor = 0;
        	}
        else
        {
        	resultColor = Rnd.get(0, 100);
        	if (resultColor <= 10 * lifeStoneGrade + 5 || stat34 != 0)
        		resultColor = 3;
        	else if (resultColor <= 10 * lifeStoneGrade + 10)
        		resultColor = 1;
        	else
        		resultColor = 2;
        	}
        int stat12 = 0;
        if (stat34 == 0 && !generateSkill)
        {
        	int temp = Rnd.get(2,3);
        	int colorOffset = resultColor * 10 * STAT_SUBBLOCKSIZE + temp * STAT_BLOCKSIZE + 1;
        	int offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + colorOffset;

            stat34 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
            if (generateGlow && lifeStoneGrade >= 2)
            	offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade * 10 * STAT_SUBBLOCKSIZE + 1;
			else
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + Rnd.get(0, 1) * 10 * STAT_SUBBLOCKSIZE + 1;
            stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
            }
        else
        {
        	int offset;
        	if (!generateGlow)
        		offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + 1;
        	else
        		offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * 10 * STAT_SUBBLOCKSIZE+ 1;
        	stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
        	}
        L2Skill skill = null;
        if (generateSkill)
        {
        	augmentationSkill temp=null;
            switch (resultColor)
            {
                case 1:
                    temp = _blueSkills.get(Rnd.get(0, _blueSkills.size() - 1));
                    skill = temp.getSkill(lifeStoneLevel);
                    stat34 = temp.getAugmentationSkillId() + (lifeStoneLevel - 1) * _skillsCount;
					break;
                case 2:
                    temp = _purpleSkills.get(Rnd.get(0, _purpleSkills.size() - 1));
                    skill = temp.getSkill(lifeStoneLevel);
                    stat34 = temp.getAugmentationSkillId() + (lifeStoneLevel - 1) * _skillsCount;
                    break;
                case 3:
                    temp = _redSkills.get(Rnd.get(0, _redSkills.size() - 1));
                    skill = temp.getSkill(lifeStoneLevel);
                    stat34 = temp.getAugmentationSkillId() + (lifeStoneLevel - 1) * _skillsCount;
                    break;
                    }
            }
        if (Config.DEBUG)
        	_log.info("Augmentation success: stat12=" + stat12 + "; stat34=" + stat34 + "; resultColor=" + resultColor + "; level=" + lifeStoneLevel + "; grade=" + lifeStoneGrade);

        return new L2Augmentation(item, ((stat34<<16)+stat12), skill, true);
        }

	public class AugStat
	{
		private Stats _stat;
		private float _value;
		public AugStat(Stats stat, float value)
		{
			_stat = stat; _value = value;
		}
		public Stats getStat()
		{
			return _stat;
		}
		public float getValue()
		{
			return _value;
		}
	}

	public FastList<AugStat> getAugStatsById(int augmentationId)
	{
		FastList <AugStat> temp = new FastList<AugStat>();
		int stats[] = new int[2];
		stats[0] = 0x0000FFFF&augmentationId;
		stats[1] = augmentationId>>16;

		for (int i=0; i<2; i++)
		{
			if (stats[i] >= STAT_START && stats[i] <= STAT_END)
			{
				int block=0;
				while (stats[i] > STAT_BLOCKSIZE)
				{
					stats[i]-=STAT_BLOCKSIZE;
					block++;
				}
				int subblock=0;
				while (stats[i] > STAT_SUBBLOCKSIZE)
				{
					stats[i]-=STAT_SUBBLOCKSIZE;
					subblock++;
				}
				if (stats[i] < 14)
				{
					augmentationStat as = (augmentationStat)_augmentationStats[block].get((stats[i]-1));
					temp.add(new AugStat(as.getStat(), as.getSingleStatValue(subblock)));
				}
				else
				{
					stats[i]-=13;
					int x = 12;
					int rescales = 0;

					while (stats[i] > x)
					{
						stats[i]-=x;
						x--;
						rescales++;
					}

					augmentationStat as = (augmentationStat)_augmentationStats[block].get(rescales);
					if (rescales == 0)
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					else
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock*2+1)));

					as = (augmentationStat)_augmentationStats[block].get(rescales+stats[i]);
					if (as.getStat() == Stats.CRITICAL_DAMAGE)
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					else
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock*2)));
				}
			}

			else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN)
			{
				switch (stats[i])
				{
					case BASESTAT_STR:
						temp.add(new AugStat(Stats.STAT_STR, 1.0f));
						break;
					case BASESTAT_CON:
						temp.add(new AugStat(Stats.STAT_CON, 1.0f));
						break;
					case BASESTAT_INT:
						temp.add(new AugStat(Stats.STAT_INT, 1.0f));
						break;
					case BASESTAT_MEN:
						temp.add(new AugStat(Stats.STAT_MEN, 1.0f));
						break;
				}
			}
		}
		return temp;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AugmentationData _instance = new AugmentationData();
	}
}
