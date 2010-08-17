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
package com.l2scoria.gameserver.model;

import com.l2scoria.gameserver.datatables.sql.ArmorSetsTable;
import com.l2scoria.gameserver.datatables.sql.ArmorSetsTable.ArmorDummy;
import com.l2scoria.gameserver.datatables.sql.CharTemplateTable;
import com.l2scoria.gameserver.datatables.sql.ClanTable;
import com.l2scoria.gameserver.model.base.Race;
import com.l2scoria.gameserver.templates.StatsSet;

/**
 *
 * @author Velvet
 */
public class L2MaxPolyModel
{
	// Base
	private String _name;
	private String _title;
	private Race _race;
	private int _sex;
	private int _hair;
	private int _hairColor;
	private int _face;
	private int _classId;
	private int _npcId;

	// Item related
	private int _weaponIdRH;
	private int _weaponIdLH;
	private int _weaponIdEnc;
	private int _armorId; // all others p_dolls will be set auto if the value is a valid armor set id
	private int _head; // not seen
	private int _hats; 
	private int _faces;
	private int _chest;
	private int _legs;
	private int _gloves;
	private int _feet;

	// Misc
	private int _abnormalEffect;
	private int _pvpFlag;
	private int _karma;
	private int _recom;
	private L2Clan _clan;
	private int _isHero;
	private int _pledge;
	private int _nameColor = 0xFFFFFF;
	private int _titleColor = 0xFFFF77;

	public L2MaxPolyModel(StatsSet data)
	{
		_name = data.getString("name");
		_title = data.getString("title");
		_sex = data.getInteger("sex");
		_hair = data.getInteger("hair");
		_hairColor = data.getInteger("hairColor");
		_face = data.getInteger("face");
		_classId = data.getInteger("classId");
		_npcId = data.getInteger("npcId");
		_weaponIdRH = data.getInteger("weaponIdRH");
		_weaponIdLH = data.getInteger("weaponIdLH");
		_weaponIdEnc = data.getInteger("weaponIdEnc");
		_armorId = data.getInteger("armorId");
		_head = data.getInteger("head");
		_hats = data.getInteger("hats");
		_faces = data.getInteger("faces");
		_chest = data.getInteger("chest");
		_legs = data.getInteger("legs");
		_gloves = data.getInteger("gloves");
		_feet = data.getInteger("feet");
		_abnormalEffect = data.getInteger("abnormalEffect");
		_pvpFlag = data.getInteger("pvpFlag");
		_karma = data.getInteger("karma");
		_recom = data.getInteger("recom");
		_clan = ClanTable.getInstance().getClan(data.getInteger("clan"));
		_isHero = data.getInteger("isHero");
		_pledge = data.getInteger("pledge");

		if(data.getInteger("nameColor") > 0)
			_nameColor = data.getInteger("nameColor");
		if(data.getInteger("titleColor") > 0)
			_titleColor = data.getInteger("titleColor");

		_race = CharTemplateTable.getInstance().getTemplate(_classId).race;
		
		ArmorDummy armor = ArmorSetsTable.getInstance().getCusArmorSets(_armorId);

		if(armor != null)
		{
			_head = armor.getHead();
			_chest = armor.getChest();
			_legs = armor.getLegs();
			_gloves = armor.getGloves();
			_feet = armor.getFeet();
		}
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle()
	{
		return _title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title)
	{
		_title = title;
	}
	/**
	 * @return Returns the sex.
	 */
	public int getSex()
	{
		return _sex;
	}
	/**
	 * @param sex The sex to set.
	 */
	public void setSex(int sex)
	{
		_sex = sex;
	}
	/**
	 * @return Returns the hair.
	 */
	public int getHair()
	{
		return _hair;
	}
	/**
	 * @param hair The hair to set.
	 */
	public void setHair(int hair)
	{
		_hair = hair;
	}
	/**
	 * @return Returns the hairColor.
	 */
	public int getHairColor()
	{
		return _hairColor;
	}
	/**
	 * @param hairColor The hairColor to set.
	 */
	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}
	/**
	 * @return Returns the face.
	 */
	public int getFace()
	{
		return _face;
	}
	/**
	 * @param face The face to set.
	 */
	public void setFace(int face)
	{
		_face = face;
	}
	/**
	 * @return Returns the classId.
	 */
	public int getClassId()
	{
		return _classId;
	}
	/**
	 * @param classId The classId to set.
	 */
	public void setClassId(int classId)
	{
		_classId = classId;
	}

	/**
	 * @return Returns the weaponIdRH.
	 */
	public int getWeaponIdRH()
	{
		return _weaponIdRH;
	}

	/**
	 * @return Returns the weaponIdRH.
	 */
	public int getWeaponIdLH()
	{
		return _weaponIdLH;
	}

	/**
	 * @param weaponIdRH The weaponIdRH to set.
	 */
	public void setWeaponIdRH(int weaponIdRH)
	{
		_weaponIdRH = weaponIdRH;
	}

	public void setWeaponIdLH(int weaponIdLH)
	{
		_weaponIdLH = weaponIdLH;
	}

	/**
	 * @return Returns the weaponIdEnc.
	 */
	public int getWeaponIdEnc()
	{
		return _weaponIdEnc;
	}
	/**
	 * @param weaponIdEnc The weaponIdEnc to set.
	 */
	public void setWeaponIdEnc(int weaponIdEnc)
	{
		_weaponIdEnc = weaponIdEnc;
	}
	/**
	 * @return Returns the armorId.
	 */
	public int getArmorId()
	{
		return _armorId;
	}
	/**
	 * @param armorId The armorId to set.
	 */
	public void setArmorId(int armorId)
	{
		_armorId = armorId;
	}
	/**
	 * @return Returns the head.
	 */
	public int getHead()
	{
		return _head;
	}
	/**
	 * @param head The head to set.
	 */
	public void setHead(int head)
	{
		_head = head;
	}
	/**
	 * @return Returns the hats.
	 */
	public int getHats()
	{
		return _hats;
	}
	/**
	 * @param hats The hats to set.
	 */
	public void setHats(int hats)
	{
		_hats = hats;
	}
	/**
	 * @return Returns the faces.
	 */
	public int getFaces()
	{
		return _faces;
	}
	/**
	 * @param faces The faces to set.
	 */
	public void setFaces(int faces)
	{
		_faces = faces;
	}
	/**
	 * @return Returns the chest.
	 */
	public int getChest()
	{
		return _chest;
	}
	/**
	 * @param chest The chest to set.
	 */
	public void setChest(int chest)
	{
		_chest = chest;
	}
	/**
	 * @return Returns the legs.
	 */
	public int getLegs()
	{
		return _legs;
	}
	/**
	 * @param legs The legs to set.
	 */
	public void setLegs(int legs)
	{
		_legs = legs;
	}
	/**
	 * @return Returns the gloves.
	 */
	public int getGloves()
	{
		return _gloves;
	}
	/**
	 * @param gloves The gloves to set.
	 */
	public void setGloves(int gloves)
	{
		_gloves = gloves;
	}
	/**
	 * @return Returns the feet.
	 */
	public int getFeet()
	{
		return _feet;
	}
	/**
	 * @param feet The feet to set.
	 */
	public void setFeet(int feet)
	{
		_feet = feet;
	}
	/**
	 * @return Returns the abnormalEffect.
	 */
	public int getAbnormalEffect()
	{
		return _abnormalEffect;
	}
	/**
	 * @param abnormalEffect The abnormalEffect to set.
	 */
	public void setAbnormalEffect(int abnormalEffect)
	{
		_abnormalEffect = abnormalEffect;
	}
	/**
	 * @return Returns the pvpFlag.
	 */
	public int getPvpFlag()
	{
		return _pvpFlag;
	}
	/**
	 * @param pvpFlag The pvpFlag to set.
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}
	/**
	 * @return Returns the karma.
	 */
	public int getKarma()
	{
		return _karma;
	}
	/**
	 * @param karma The karma to set.
	 */
	public void setKarma(int karma)
	{
		_karma = karma;
	}
	/**
	 * @return Returns the recom.
	 */
	public int getRecom()
	{
		return _recom;
	}
	/**
	 * @param recom The recom to set.
	 */
	public void setRecom(int recom)
	{
		_recom = recom;
	}
	/**
	 * @return Returns the clan.
	 */
	public L2Clan getClan()
	{
		return _clan;
	}
	/**
	 * @param clan The clan to set.
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
	}
	/**
	 * @return Returns the isHero.
	 */
	public int getIsHero()
	{
		return _isHero;
	}
	/**
	 * @param isHero The isHero to set.
	 */
	public void setIsHero(int isHero)
	{
		_isHero = isHero;
	}
	/**
	 * @return Returns the pledge.
	 */
	public int getPledge()
	{
		return _pledge;
	}
	/**
	 * @param pledge The pledge to set.
	 */
	public void setPledge(int pledge)
	{
		_pledge = pledge;
	}
	/**
	 * @return Returns the nameColor.
	 */
	public int getNameColor()
	{
		return _nameColor;
	}
	/**
	 * @param nameColor The nameColor to set.
	 */
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	/**
	 * @return Returns the titleColor.
	 */
	public int getTitleColor()
	{
		return _titleColor;
	}
	/**
	 * @param titleColor The titleColor to set.
	 */
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}

	/**
	 * @param npcId The npcId to set.
	 */
	public void setNpcId(int npcId)
	{
		_npcId = npcId;
	}

	/**
	 * @return Returns the npcId.
	 */
	public int getNpcId()
	{
		return _npcId;
	}

	public Race getRace()
	{
		return _race;
	}

}
