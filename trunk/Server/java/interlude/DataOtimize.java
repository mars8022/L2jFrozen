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
package interlude;

import java.sql.PreparedStatement;

/**
 * @author ProGramMoS (created the source)
 * @author Aleff (fixed the source)
 * @version 2.0
 */
public class DataOtimize
{
	public static void OptimizeGame()
	{
		String GAME_QUICK_OPTIMIZE =
		"OPTIMIZE TABLE account_data, armor, armorsets, auction, auction_bid, auction_watch, augmentations, auto_announcements, auto_chat, auto_chat_text, boxaccess, boxes, buff_templates, castle, castle_door, castle_doorupgrade, castle_manor_procure, castle_manor_production, castle_siege_guards, char_templates, character_blocklist, character_colors, character_friends, character_hennas, character_macroses, character_quests, character_raid_points, character_recipebook, character_recommends, character_shortcuts, character_skills, character_skills_save, character_subclasses, characters, clan_data, clan_notices, clan_privs, clan_skills, clan_subpledges, clan_wars, clanhall, clanhall_functions, clanhall_siege, class_list, couples, ctf, ctf_teams, cursed_weapons, custom_armor, custom_armorsets, custom_droplist, custom_etcitem, custom_merchant_buylists, custom_merchant_shopids, custom_npc, custom_spawnlist, custom_teleport, custom_weapon, dimensional_rift, dm, droplist, enchant_skill_trees, etcitem, fish, fishing_skill_trees, fort, fortress_siege, fort_door, fort_doorupgrade, fort_functions, fort_spawnlist, fort_siege_guards, fort_staticobjects, fortsiege_clans, forums, four_sepulchers_spawnlist, games, global_tasks, gm_edit, grandboss_data, grandboss_list, helper_buff_list, henna, henna_trees, heroes, items, itemsonground, lastimperialtomb_spawnlist, locations, lvlupgain, mapregion, merchant_areas_list, merchant_buylists, merchant_lease, merchant_shopids, merchants, minions, mods_buffer_schemes, mods_buffer_skills, mods_wedding, nospawnlist, npc, npc_buffer, npcskills, olympiad_nobles, pets, pets_objects, pets_stats, pkkills, pledge_skill_trees, posts, quest_global_data, raid_event_spawnlist, raid_prizes, raidboss_spawnlist, random_spawn, random_spawn_loc, seven_signs, seven_signs_festival, seven_signs_status, siege_clans, skill_learn, skill_spellbooks, skill_trees, spawnlist, teleport, topic, tvt, tvt_teams, vanhalter_spawnlist, walker_routes, weapon, zone, zone_vertices";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Optimization GameServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GAME_QUICK_OPTIMIZE);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Optimization Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				GAME_QUICK_OPTIMIZE = null;

			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void OptimizeLogin()
	{
		String LOGIN_QUICK_OPTIMIZE =
			"OPTIMIZE TABLE accounts, gameservers";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Optimization LoginServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOGIN_QUICK_OPTIMIZE);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Optimization Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				LOGIN_QUICK_OPTIMIZE = null;
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void RepairGame()
	{
		String GAME_QUICK_REPAIR =
		"REPAIR TABLE account_data, armor, armorsets, auction, auction_bid, auction_watch, augmentations, auto_announcements, auto_chat, auto_chat_text, boxaccess, boxes, buff_templates, castle, castle_door, castle_doorupgrade, castle_manor_procure, castle_manor_production, castle_siege_guards, char_templates, character_blocklist, character_colors, character_friends, character_hennas, character_macroses, character_quests, character_raid_points, character_recipebook, character_recommends, character_shortcuts, character_skills, character_skills_save, character_subclasses, characters, clan_data, clan_notices, clan_privs, clan_skills, clan_subpledges, clan_wars, clanhall, clanhall_functions, clanhall_siege, class_list, couples, ctf, ctf_teams, cursed_weapons, custom_armor, custom_armorsets, custom_droplist, custom_etcitem, custom_merchant_buylists, custom_merchant_shopids, custom_npc, custom_spawnlist, custom_teleport, custom_weapon, dimensional_rift, dm, droplist, enchant_skill_trees, etcitem, fish, fishing_skill_trees, fort, fortress_siege, fort_door, fort_doorupgrade, fort_functions, fort_spawnlist, fort_siege_guards, fort_staticobjects, fortsiege_clans, forums, four_sepulchers_spawnlist, games, global_tasks, gm_edit, grandboss_data, grandboss_list, helper_buff_list, henna, henna_trees, heroes, items, itemsonground, lastimperialtomb_spawnlist, locations, lvlupgain, mapregion, merchant_areas_list, merchant_buylists, merchant_lease, merchant_shopids, merchants, minions, mods_buffer_schemes, mods_buffer_skills, mods_wedding, nospawnlist, npc, npc_buffer, npcskills, olympiad_nobles, pets, pets_objects, pets_stats, pkkills, pledge_skill_trees, posts, quest_global_data, raid_event_spawnlist, raid_prizes, raidboss_spawnlist, random_spawn, random_spawn_loc, seven_signs, seven_signs_festival, seven_signs_status, siege_clans, skill_learn, skill_spellbooks, skill_trees, spawnlist, teleport, topic, tvt, tvt_teams, vanhalter_spawnlist, walker_routes, weapon, zone, zone_vertices EXTENDED";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Repair GameServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GAME_QUICK_REPAIR);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Repair Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				GAME_QUICK_REPAIR = null;

			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void RepairLogin()
	{
		String LOGIN_QUICK_REPAIR =
			"REPAIR TABLE accounts, gameservers EXTENDED";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Repair LoginServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOGIN_QUICK_REPAIR);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Repair Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				LOGIN_QUICK_REPAIR = null;
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void CheckGame()
	{
		String GAME_QUICK_CHECK =
		"CHECK TABLE account_data, armor, armorsets, auction, auction_bid, auction_watch, augmentations, auto_announcements, auto_chat, auto_chat_text, boxaccess, boxes, buff_templates, castle, castle_door, castle_doorupgrade, castle_manor_procure, castle_manor_production, castle_siege_guards, char_templates, character_blocklist, character_colors, character_friends, character_hennas, character_macroses, character_quests, character_raid_points, character_recipebook, character_recommends, character_shortcuts, character_skills, character_skills_save, character_subclasses, characters, clan_data, clan_notices, clan_privs, clan_skills, clan_subpledges, clan_wars, clanhall, clanhall_functions, clanhall_siege, class_list, couples, ctf, ctf_teams, cursed_weapons, custom_armor, custom_armorsets, custom_droplist, custom_etcitem, custom_merchant_buylists, custom_merchant_shopids, custom_npc, custom_spawnlist, custom_teleport, custom_weapon, dimensional_rift, dm, droplist, enchant_skill_trees, etcitem, fish, fishing_skill_trees, fort, fortress_siege, fort_door, fort_doorupgrade, fort_functions, fort_spawnlist, fort_siege_guards, fort_staticobjects, fortsiege_clans, forums, four_sepulchers_spawnlist, games, global_tasks, gm_edit, grandboss_data, grandboss_list, helper_buff_list, henna, henna_trees, heroes, items, itemsonground, lastimperialtomb_spawnlist, locations, lvlupgain, mapregion, merchant_areas_list, merchant_buylists, merchant_lease, merchant_shopids, merchants, minions, mods_buffer_schemes, mods_buffer_skills, mods_wedding, nospawnlist, npc, npc_buffer, npcskills, olympiad_nobles, pets, pets_objects, pets_stats, pkkills, pledge_skill_trees, posts, quest_global_data, raid_event_spawnlist, raid_prizes, raidboss_spawnlist, random_spawn, random_spawn_loc, seven_signs, seven_signs_festival, seven_signs_status, siege_clans, skill_learn, skill_spellbooks, skill_trees, spawnlist, teleport, topic, tvt, tvt_teams, vanhalter_spawnlist, walker_routes, weapon, zone, zone_vertices EXTENDED";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Check GameServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GAME_QUICK_CHECK);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Check Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				GAME_QUICK_CHECK = null;

			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void CheckLogin()
	{
		String LOGIN_QUICK_CHECK =
			"CHECK TABLE accounts, gameservers EXTENDED";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Check LoginServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOGIN_QUICK_CHECK);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Check Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				LOGIN_QUICK_CHECK = null;
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void AnalyzeGame()
	{
		String GAME_QUICK_ANALYZE =
		"ANALYZE TABLE account_data, armor, armorsets, auction, auction_bid, auction_watch, augmentations, auto_announcements, auto_chat, auto_chat_text, boxaccess, boxes, buff_templates, castle, castle_door, castle_doorupgrade, castle_manor_procure, castle_manor_production, castle_siege_guards, char_templates, character_blocklist, character_colors, character_friends, character_hennas, character_macroses, character_quests, character_raid_points, character_recipebook, character_recommends, character_shortcuts, character_skills, character_skills_save, character_subclasses, characters, clan_data, clan_notices, clan_privs, clan_skills, clan_subpledges, clan_wars, clanhall, clanhall_functions, clanhall_siege, class_list, couples, ctf, ctf_teams, cursed_weapons, custom_armor, custom_armorsets, custom_droplist, custom_etcitem, custom_merchant_buylists, custom_merchant_shopids, custom_npc, custom_spawnlist, custom_teleport, custom_weapon, dimensional_rift, dm, droplist, enchant_skill_trees, etcitem, fish, fishing_skill_trees, fort, fortress_siege, fort_door, fort_doorupgrade, fort_functions, fort_spawnlist, fort_siege_guards, fort_staticobjects, fortsiege_clans, forums, four_sepulchers_spawnlist, games, global_tasks, gm_edit, grandboss_data, grandboss_list, helper_buff_list, henna, henna_trees, heroes, items, itemsonground, lastimperialtomb_spawnlist, locations, lvlupgain, mapregion, merchant_areas_list, merchant_buylists, merchant_lease, merchant_shopids, merchants, minions, mods_buffer_schemes, mods_buffer_skills, mods_wedding, nospawnlist, npc, npc_buffer, npcskills, olympiad_nobles, pets, pets_objects, pets_stats, pkkills, pledge_skill_trees, posts, quest_global_data, raid_event_spawnlist, raid_prizes, raidboss_spawnlist, random_spawn, random_spawn_loc, seven_signs, seven_signs_festival, seven_signs_status, siege_clans, skill_learn, skill_spellbooks, skill_trees, spawnlist, teleport, topic, tvt, tvt_teams, vanhalter_spawnlist, walker_routes, weapon, zone, zone_vertices";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Analyze GameServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GAME_QUICK_ANALYZE);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Analyze Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				GAME_QUICK_ANALYZE = null;

			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

	public static void AnalyzeLogin()
	{
		String LOGIN_QUICK_ANALYZE =
			"ANALYZE TABLE accounts, gameservers";

		java.sql.Connection con = null;
		PreparedStatement statement = null;

		try
		{
			System.out.println("Analyze LoginServer Tables...");
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOGIN_QUICK_ANALYZE);
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("Analyze Failed");
		}
		finally
		{
			try
			{
				con.close();
				statement.close();
				con = null;
				statement = null;
				LOGIN_QUICK_ANALYZE = null;
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}
}
