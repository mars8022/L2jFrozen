-- -----------------------------------------------
-- Table structure for admin_command_access_rights
-- -----------------------------------------------
DROP TABLE IF EXISTS admin_command_access_rights;
CREATE TABLE IF NOT EXISTS `admin_command_access_rights` (
  `adminCommand` varchar(255) NOT NULL DEFAULT 'admin_',
  `accessLevels` varchar(255) NOT NULL,
  PRIMARY KEY  (`adminCommand`)
) DEFAULT CHARSET=utf8;
-- ---------------------------------------------
-- Records for table admin_command_access_rights
-- ---------------------------------------------

INSERT IGNORE INTO `admin_command_access_rights` VALUES 
-- Section: Admin
('admin_admin','3'),
('admin_admin1','3'),
('admin_admin2','3'),
('admin_admin3','3'),
('admin_admin4','3'),
('admin_admin5','3'),
('admin_gmliston','3'),
('admin_gmlistoff','3'),
('admin_silence','3'),
('admin_diet','3'), -- means that player dnt take weight penalty
('admin_set','1'), -- Config.setParameterValue(pName, pValue)
('admin_set_menu','1'), -- Not Implemented
('admin_set_mod','3'),
('admin_saveolymp','2'),
('admin_manualhero','2'),

-- Section: Announcements
('admin_list_announcements','3'),
('admin_reload_announcements','3'),
('admin_announce_announcements','3'),
('admin_add_announcement','3'),
('admin_del_announcement','3'),
('admin_announce','3'),
('admin_critannounce','1'),
('admin_announce_menu','3'),
('admin_list_autoannouncements','3'),
('admin_add_autoannouncement','3'),
('admin_del_autoannouncement','3'),
('admin_autoannounce','3'),

-- Section: Ban
('admin_ban','2'),
('admin_unban','2'),
('admin_jail','3'),
('admin_unjail','3'),

-- Section: BanChat
('admin_banchat','3'),
('admin_unbanchat','3'),

-- Section: BBS
('admin_bbs','3'), -- Not Implemented

-- Section: Boat
('admin_boat','3'),

-- Section: Buffs
('admin_getbuffs','3'), -- show all player buffs
('admin_stopbuff','3'), -- cancel just 1 buff
('admin_stopallbuffs','3'), -- cancel all player buffs
('admin_areacancel','3'), -- Cancel all area players buffs

-- Section: Cache
('admin_cache_htm_rebuild','1'),
('admin_cache_htm_reload','1'), -- reload all htmls cache
('admin_cache_reload_path','1'), -- reload just 1 html path
('admin_cache_reload_file','1'), -- reload just 1 html file
('admin_cache_crest_rebuild','1'), -- CrestCache.getInstance().reload();
('admin_cache_crest_reload','1'), -- CrestCache.getInstance().reload();
('admin_cache_crest_fix','1'), -- CrestCache.getInstance().convertOldPedgeFiles();

-- Section: ChangeLevel
('admin_changelvl','2'),

-- Section: Christmas
('admin_christmas_start','3'),
('admin_christmas_end','3'),

-- Section: CreateItem
('admin_itemcreate','3'), -- itemcreation.htm
('admin_create_item','3'), -- lvl 3: item just on yourself, lvl 2-1: item on other too

-- Section: CTF
('admin_ctf','3'),
('admin_ctf_name','3'),
('admin_ctf_desc','3'),
('admin_ctf_join_loc','3'),
('admin_ctf_edit','3'),
('admin_ctf_control','3'),
('admin_ctf_minlvl','3'),
('admin_ctf_maxlvl','3'),
('admin_ctf_tele_npc','3'),
('admin_ctf_tele_team','3'),
('admin_ctf_tele_flag','3'),
('admin_ctf_npc','3'),
('admin_ctf_npc_pos','3'),
('admin_ctf_reward','3'),
('admin_ctf_reward_amount','3'),
('admin_ctf_team_add','3'),
('admin_ctf_team_remove','3'),
('admin_ctf_team_pos','3'),
('admin_ctf_team_color','3'),
('admin_ctf_team_flag','3'),
('admin_ctf_join','3'),
('admin_ctf_teleport','3'),
('admin_ctf_start','3'),
('admin_ctf_abort','3'),
('admin_ctf_finish','3'),
('admin_ctf_sit','3'),
('admin_ctf_dump','3'),
('admin_ctf_save','3'),
('admin_ctf_load','3'),
('admin_ctf_jointime','3'),
('admin_ctf_eventtime','3'),
('admin_ctf_autoevent','3'),
('admin_ctf_minplayers','3'),
('admin_ctf_maxplayers','3'),
('admin_ctf_interval','3'),

-- Section: CursedWeapons
('admin_cw_info','2'),
('admin_cw_remove','2'),
('admin_cw_goto','2'),
('admin_cw_reload','2'),
('admin_cw_add','2'),
('admin_cw_info_menu','3'),

-- Section: Delete
('admin_delete','3'),

-- Section: Disconnect
('admin_character_disconnect','2'),

-- Section: DMEngine
('admin_dmevent','3'),
('admin_dmevent_name','3'),
('admin_dmevent_desc','3'),
('admin_dmevent_join_loc','3'),
('admin_dmevent_minlvl','3'),
('admin_dmevent_maxlvl','3'),
('admin_dmevent_npc','3'),
('admin_dmevent_npc_pos','3'),
('admin_dmevent_reward','3'),
('admin_dmevent_reward_amount','3'),
('admin_dmevent_spawnpos','3'),
('admin_dmevent_color','3'),
('admin_dmevent_join','3'),
('admin_dmevent_teleport','3'),
('admin_dmevent_start','3'),
('admin_dmevent_abort','3'),
('admin_dmevent_finish','3'),
('admin_dmevent_sit','3'),
('admin_dmevent_dump','3'),
('admin_dmevent_save','3'),
('admin_dmevent_load','3'),

-- Section: Donator
('admin_setdonator','1'),

-- Section: DoorControl
('admin_open','3'),
('admin_close','3'),
('admin_openall','3'),
('admin_closeall','3'),

-- Section: EditChar
('admin_changename','2'),
('admin_edit_character','2'),
('admin_current_player','3'),
('admin_nokarma','2'),
('admin_setkarma','2'),
('admin_character_list','3'),
('admin_character_info','3'),
('admin_show_characters','3'),
('admin_find_character','3'),
('admin_find_dualbox','3'),
('admin_find_ip','3'),
('admin_find_account','3'),
('admin_save_modifications','2'),
('admin_rec','2'),
('admin_setclass','2'),
('admin_settitle','2'),
('admin_setsex','2'),
('admin_setcolor','2'),
('admin_fullfood','2'),
('admin_remclanwait','2'),
('admin_setcp','2'),
('admin_sethp','2'),
('admin_setmp','2'),
('admin_setchar_cp','2'),
('admin_setchar_hp','2'),
('admin_setchar_mp','2'),

-- Section: EditChar
('admin_edit_npc','2'),
('admin_save_npc','2'),
('admin_show_droplist','3'),
('admin_edit_drop','2'),
('admin_add_drop','2'),
('admin_del_drop','2'),
('admin_showShop','3'),
('admin_showShopList','3'),
('admin_addShopItem','2'),
('admin_delShopItem','2'),
('admin_box_access','2'),
('admin_editShopItem','2'),
('admin_close_window','3'),

-- Section: Char Supervision
('admin_start_monitor_char','3'),
('admin_stop_monitor_char','3'),
('admin_block_char_packet','3'),
('admin_restore_char_packet','3'),

-- Section: Effects
('admin_invis','3'),
('admin_invisible','3'),
('admin_vis','3'),
('admin_visible','3'),
('admin_invis_menu','3'),
('admin_earthquake','3'),
('admin_earthquake_menu','3'),
('admin_bighead','3'),
('admin_shrinkhead','3'),
('admin_gmspeed','3'),
('admin_gmspeed_menu','3'),
('admin_unpara_all','3'),
('admin_para_all','3'),
('admin_unpara','3'),
('admin_para','3'),
('admin_unpara_all_menu','3'),
('admin_para_all_menu','3'),
('admin_unpara_menu','3'),
('admin_para_menu','3'),
('admin_polyself','3'),
('admin_unpolyself','3'),
('admin_polyself_menu','3'),
('admin_unpolyself_menu','3'),
('admin_clearteams','3'),
('admin_setteam_close','3'), -- set all Gm close players to val team
('admin_setteam','3'),
('admin_social','3'),
('admin_effect','3'),
('admin_social_menu','3'),
('admin_effect_menu','3'),
('admin_abnormal','3'),
('admin_abnormal_menu','3'),
('admin_play_sounds','3'),
('admin_play_sound','3'),
('admin_shrinkhead','3'),
('admin_atmosphere','3'),
('admin_atmosphere_menu','3'),
('admin_npc_say','3'),
('admin_debuff','3'),

-- Section: Enchant
('admin_seteh','2'),
('admin_setec','2'),
('admin_seteg','2'),
('admin_setel','2'),
('admin_seteb','2'),
('admin_setew','2'),
('admin_setes','2'),
('admin_setle','2'),
('admin_setre','2'),
('admin_setlf','2'),
('admin_setrf','2'),
('admin_seten','2'),
('admin_setun','2'),
('admin_setba','2'),
('admin_enchant','2'),

-- Section: EventEngine
('admin_event','3'),
('admin_event_new','3'),
('admin_event_choose','3'),
('admin_event_store','3'),
('admin_event_set','3'),
('admin_event_change_teams_number','3'),
('admin_event_announce','3'),
('admin_event_panel','3'),
('admin_event_control_begin','3'),
('admin_event_control_teleport','3'),
('admin_add','3'),
('admin_event_see','3'),
('admin_event_del','3'),
('admin_delete_buffer','3'),
('admin_event_control_sit','3'),
('admin_event_name','3'),
('admin_event_control_kill','3'),
('admin_event_control_res','3'),
('admin_event_control_poly','3'),
('admin_event_control_unpoly','3'),
('admin_event_control_prize','3'),
('admin_event_control_chatban','3'),
('admin_event_control_finish','3'),

-- Section: ExpSp
('admin_add_exp_sp_to_character','2'),
('admin_add_exp_sp','2'),
('admin_remove_exp_sp','2'),

-- Section: FightCalculator
('admin_fight_calculator','3'),
('admin_fight_calculator_show','3'),
('admin_fcs','3'),

-- Section: FortSiege
('admin_fortsiege','2'),
('admin_add_fortattacker','2'),
('admin_add_fortdefender','2'),
('admin_add_fortguard','2'),
('admin_list_fortsiege_clans','3'),
('admin_clear_fortsiege_list','2'),
('admin_move_fortdefenders','2'),
('admin_spawn_fortdoors','2'),
('admin_endfortsiege','2'),
('admin_startfortsiege','2'),
('admin_setfort','2'),
('admin_removefort','2'),

-- Section: Geodata
('admin_geo_z','3'),
('admin_geo_type','3'),
('admin_geo_nswe','3'),
('admin_geo_los','3'),
('admin_geo_position','3'),
('admin_geo_bug','3'),
('admin_geo_load','3'),
('admin_geo_unload','3'),			

-- Section: Gm
('admin_gm','1'),

-- Section: GmChat
('admin_gmchat','3'),
('admin_snoop','3'),
('admin_gmchat_menu','3'),

-- Section: Heal
('admin_heal','2'),

-- Section: HelpPage
('admin_help','3'),

-- Section: Hero
('admin_sethero','2'),

-- Section: Invul
('admin_invul','3'),
('admin_setinvul','2'),

-- Section: Kick
('admin_kick','2'),
('admin_kick_non_gm','1'),

-- Section: Kill
('admin_kill','3'),
('admin_kill_monster','2'),

-- Section: Level
('admin_add_level','2'),
('admin_set_level','2'),

-- Section: Login
('admin_server_gm_only','1'),
('admin_server_all','1'),
('admin_server_max_player','1'),
('admin_server_list_clock','1'),
('admin_server_login','1'),

-- Section: Mammon
('admin_mammon_find','2'),
('admin_mammon_respawn','2'),
('admin_list_spawns','2'),
('admin_msg','2'),

-- Section: Manor
('admin_manor','2'),
('admin_manor_reset','2'),
('admin_manor_save','2'),
('admin_manor_disable','2'),

-- Section: MassControl
('admin_masskill','3'),
('admin_massress','3'),

-- Section: MassRecall
('admin_recallclan','3'),
('admin_recallparty','3'),
('admin_recallally','3'),

-- Section: Menu
('admin_char_manage','2'),
('admin_teleport_character_to_menu','3'),
('admin_recall_char_menu','3'),
('admin_recall_party_menu','3'),
('admin_recall_clan_menu','3'),
('admin_goto_char_menu','3'),
('admin_kick_menu','2'),
('admin_kill_menu','3'),
('admin_ban_menu','2'),
('admin_unban_menu','2'),

-- Section: MobMenu
('admin_mobmenu','2'),
('admin_mobgroup_list','2'),
('admin_mobgroup_create','2'),
('admin_mobgroup_remove','2'),
('admin_mobgroup_delete','2'),
('admin_mobgroup_spawn','2'),
('admin_mobgroup_unspawn','2'),
('admin_mobgroup_kill','2'),
('admin_mobgroup_idle','2'),
('admin_mobgroup_attack','2'),
('admin_mobgroup_rnd','2'),
('admin_mobgroup_return','2'),
('admin_mobgroup_follow','2'),
('admin_mobgroup_casting','2'),
('admin_mobgroup_nomove','2'),
('admin_mobgroup_attackgrp','2'),
('admin_mobgroup_invul','2'),
('admin_mobinst','2'),

-- Section: MonsterRace
('admin_mons','2'),

-- Section: Noble
('admin_setnoble','2'),

-- Section: Petitions
('admin_view_petitions','3'),
('admin_view_petition','3'),
('admin_accept_petition','3'),
('admin_reject_petition','3'),
('admin_reset_petitions','3'),

-- Section: PForge -- forge packets
('admin_forge','1'),
('admin_forge2','1'),
('admin_forge3','1'),

-- Section: Pledge
('admin_pledge','2'), -- Manage Clan

-- Section: Polymorph
('admin_polymorph','2'),
('admin_unpolymorph','2'),
('admin_polymorph_menu','2'),
('admin_unpolymorph_menu','2'),

-- Section: Quest
('admin_quest_reload','2'),

-- Section: Reload
('admin_reload','2'),

-- Section: RepairChar
('admin_restore','2'),
('admin_repair','2'),

-- Section: Res
('admin_res','3'),
('admin_res_monster','3'),

-- Section: RideWyvern
('admin_ride_wyvern','3'),
('admin_ride_strider','3'),
('admin_unride_wyvern','3'),
('admin_unride_strider','3'),
('admin_unride','3'),

-- Section: Script
('admin_load_script','2'),

-- Section: Shop -- Must be modified the Trade option..
('admin_buy','3'),
('admin_gmshop','3'),

-- Section: Shutdown
('admin_server_shutdown','2'),
('admin_server_restart','2'),
('admin_server_abort','2'),

-- Section: Siege 
('admin_siege','2'),
('admin_add_attacker','2'),
('admin_add_defender','2'),
('admin_add_guard','2'),
('admin_list_siege_clans','2'),
('admin_clear_siege_list','2'),
('admin_move_defenders','2'),
('admin_spawn_doors','2'),
('admin_endsiege','2'),
('admin_startsiege','2'),
('admin_setcastle','2'),
('admin_removecastle','2'),
('admin_clanhall','2'),
('admin_clanhallset','2'),
('admin_clanhalldel','2'),
('admin_clanhallopendoors','2'),
('admin_clanhallclosedoors','2'),
('admin_clanhallteleportself','2'),

-- Section: Skills
('admin_show_skills','3'),
('admin_remove_skills','3'),
('admin_skill_list','3'),
('admin_skill_index','3'),
('admin_add_skill','3'),
('admin_remove_skill','3'),
('admin_get_skills','3'),
('admin_reset_skills','3'),
('admin_give_all_skills','3'),
('admin_remove_all_skills','3'),
('admin_add_clan_skill','3'),

-- Section: Spawn 
('admin_show_spawns','3'),
('admin_spawn','3'),
('admin_spawn_monster','3'),
('admin_spawn_index','3'),
('admin_unspawnall','3'),
('admin_respawnall','3'),
('admin_spawn_reload','3'),
('admin_npc_index','3'),
('admin_spawn_once','3'),
('admin_show_npcs','3'),
('admin_teleport_reload','3'),
('admin_spawnnight','3'),
('admin_spawnday','3'),

-- Section: Target 
('admin_target','3'),

-- Section: Teleport 
('admin_show_moves','3'),
('admin_show_moves_other','3'),
('admin_show_teleport','3'),
('admin_teleport_to_character','3'),
('admin_teleportto','3'),
('admin_move_to','3'),
('admin_teleport_character','3'),
('admin_recall','3'),
('admin_walk','3'),
('admin_recall_npc','3'),
('admin_gonorth','3'),
('admin_gosouth','3'),
('admin_goeast','3'),
('admin_gowest','3'),
('admin_goup','3'),
('admin_godown','3'),
('admin_tele','3'),
('admin_teleto','3'),

-- Section: Teleport 
('admin_test','3'),
('admin_stats','3'),
('admin_skill_test','3'),
('admin_st','3'),
('admin_mp','3'),
('admin_known','3'),

-- Section: TownWar 
('admin_townwar_start','3'),
('admin_townwar_end','3'),

-- Section: TvTEngine 
('admin_tvt','3'),
('admin_tvt_name','3'),
('admin_tvt_desc','3'),
('admin_tvt_join_loc','3'),
('admin_tvt_minlvl','3'),
('admin_tvt_maxlvl','3'),
('admin_tvt_npc','3'),
('admin_tvt_npc_pos','3'),
('admin_tvt_reward','3'),
('admin_tvt_reward_amount','3'),
('admin_tvt_team_add','3'),
('admin_tvt_team_remove','3'),
('admin_tvt_team_pos','3'),
('admin_tvt_team_color','3'),
('admin_tvt_join','3'),
('admin_tvt_teleport','3'),
('admin_tvt_start','3'),
('admin_tvt_abort','3'),
('admin_tvt_finish','3'),
('admin_tvt_sit','3'),
('admin_tvt_dump','3'),
('admin_tvt_save','3'),
('admin_tvt_load','3'),
('admin_tvt_jointime','3'),
('admin_tvt_eventtime','3'),
('admin_tvt_autoevent','3'),
('admin_tvt_minplayers','3'),
('admin_tvt_maxplayers','3'),
('admin_tvtkick','3'),

-- Section: UnblockIp 
('admin_unblockip','2'),

-- Section: VIPEngine 
('admin_vip','3'),
('admin_vip_setteam','3'),
('admin_vip_randomteam','3'),
('admin_vip_settime','3'),
('admin_vip_endnpc','3'),
('admin_vip_setdelay','3'),
('admin_vip_joininit','3'),
('admin_vip_joinnpc','3'),
('admin_vip_joinlocxyz','3'),
('admin_vip_setarea','3'),
('admin_vip_vipreward','3'),
('admin_vip_viprewardamount','3'),
('admin_vip_thevipreward','3'),
('admin_vip_theviprewardamount','3'),
('admin_vip_notvipreward','3'),
('admin_vip_notviprewardamount','3'),

-- Section: Walker 
('admin_walker_setmessage','3'),
('admin_walker_menu','3'),
('admin_walker_setnpc','3'),
('admin_walker_setpoint','3'),
('admin_walker_setmode','3'),
('admin_walker_addpoint','3'),

-- Section: Zone 
('admin_zone_check','2'),
('admin_zone_reload','2'),

-- Section: AIO
('admin_setaio','2'),
('admin_removeaio','2');
