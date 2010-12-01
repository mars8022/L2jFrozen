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
package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.services.FService;

/**
 * 
 * 
 * @author Shyla
 */
public class ClassDamageManager
{
	private static final Logger _log = Logger.getLogger(ClassDamageManager.class.getName());
	
	private static Hashtable<Integer, Double> damage_to_mage = new Hashtable<Integer, Double>();
	private static Hashtable<Integer, Double> damage_to_fighter = new Hashtable<Integer, Double>();
	private static Hashtable<Integer, Double> damage_by_mage = new Hashtable<Integer, Double>();
	private static Hashtable<Integer, Double> damage_by_fighter = new Hashtable<Integer, Double>();

	private static Hashtable<Integer, String> id_to_name = new Hashtable<Integer, String>();
	private static Hashtable<String, Integer> name_to_id = new Hashtable<String, Integer>();
	
	public static void loadConfig()
	{
		final String SCRIPT = FService.CLASS_DAMAGES_FILE;

		try
		{
			Properties scriptSetting = new Properties();
			InputStream is = new FileInputStream(new File(SCRIPT));
			scriptSetting.load(is);
			is.close();

			Set<Object> key_set = scriptSetting.keySet();
			
			for(Object key : key_set){
				
				String key_string = (String) key;
				
				String[] class_and_type = key_string.split("__");
				
				String class_name = class_and_type[0].replace("_", " ");
				
				if(class_name.equals("Eva s Saint"))
					class_name = "Eva's Saint";
				
				String type = class_and_type[1];
				
				Integer class_id = CharTemplateTable.getClassIdByName(class_name)-1;
				
				id_to_name.put(class_id, class_name);
				name_to_id.put(class_name, class_id);
				
				if(type.equals("ToFighter")){
					damage_to_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string, "1.0")));
				}else if(type.equals("ToMage")){
					damage_to_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string, "1.0")));
				}else if(type.equals("ByFighter")){
					damage_by_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string, "1.0")));
				}else if(type.equals("ByMage")){
					damage_by_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string, "1.0")));
				}
				
			}
			
			_log.info("Loaded "+id_to_name.size()+" classes Damages configurations");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SCRIPT + " File.");
		}
		
	}
	
	public static void main(String[] args){
		ClassDamageManager.loadConfig();
	}
	
	public static double getClassDamageToMage(int id){
		double multiplier = damage_to_mage.get(id);
		return multiplier;
	}
	
	public static double getClassDamageToFighter(int id){
		double multiplier = damage_to_fighter.get(id);
		return multiplier;
	}
	
	public static double getClassDamageByMage(int id){
		double multiplier = damage_by_mage.get(id);
		return multiplier;
	}
	
	public static double getClassDamageByFighter(int id){
		double multiplier = damage_by_fighter.get(id);
		return multiplier;
	}
	
	public static int getIdByName(String name){
		return name_to_id.get(name);
	}
	
	public static String getNameById(int id){
		return id_to_name.get(id);
	}
	
	/**
	 * return the product between the attackerMultiplier and attackedMultiplier
	 * configured into the classDamages.properties
	 * @param attacker
	 * @param attacked
	 * @return output = attackerMulti*attackedMulti
	 */
	public static double getDamageMultiplier(L2PcInstance attacker, L2PcInstance attacked){
		
		if(attacker==null || attacked==null)
			return 1;
		
		double attackerMulti = 1;
		
		if(attacked.isMageClass())
			attackerMulti = getClassDamageToMage(attacker.getClassId().getId());
		else
			attackerMulti = getClassDamageToFighter(attacker.getClassId().getId());
		
		double attackedMulti = 1;
		
		if(attacker.isMageClass())
			attackedMulti = getClassDamageByMage(attacked.getClassId().getId());
		else
			attackerMulti = getClassDamageByFighter(attacked.getClassId().getId());
		
		double output = attackerMulti*attackedMulti;
		
		return output < 1 ? 1. : output;
		
	}
	
}
