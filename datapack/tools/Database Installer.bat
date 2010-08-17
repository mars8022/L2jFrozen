@echo off

REM ###############################################
REM ## Configurate Database Connections please!  ##
REM ###############################################
REM Please, type here you dir to mysql directory \bin. Example : C:\Program Files\MySQL\MySQL Server 5.1\bin
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.1\bin

set DateT=%date%

REM Configurate database connection loginserver
set lsuser=root
set lspass=root
set lsdb=loginserver_beta
set lshost=localhost

REM Configurate database connection Gameserver
set gsuser=root
set gspass=root
set gsdb=gameserver_beta
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"


:Step1
cls
echo. ---------------------------------------------------------------------
echo.
echo.   L2-Frozen Team - Database Login Server`s 
echo. _____________________________________________________________________
echo.
echo.   1 - Full install database loginserver`s.
echo.   2 - Skip install loginserver db, go to install gamserver databases
echo.   3 - Spawn in town GMshop and other custom NPC
echo.   4 - Exit from installer
echo. ---------------------------------------------------------------------

set Step1prompt=x
set /p Step1prompt= Please enter values :
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto addinstall
if /i %Step1prompt%==4 goto fullend
goto Step1


:LoginInstall
@cls
echo Clear database : %lsdb% and install database loginserver`s.
echo.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo Update table accounts.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo Update table gameservers.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo.
echo Database login server will be installer whis no-error!!
pause
goto :Step2

:Step2
@cls
echo. ---------------------------------------------------------------------
echo.
echo.   L2-Frozen Team - database operation about gameserver
echo. _____________________________________________________________________
echo.
echo.   1 - Full Install Gameserver Database`s.
echo.   2 - Exit.
echo. ---------------------------------------------------------------------

set Step2prompt=x
set /p Step2prompt= Please, put value:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo Drop and clean old gamserver database`s.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
set title=installed
goto CreateTables

:CreateTables
@cls
echo Now be %title%ed database gameserver`s.
pause
@cls
echo *** Sucesfull 1 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/access_levels.sql
@cls
echo *** Sucesfull 2 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armorsets.sql
@cls
echo *** Sucesfull 4 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql
@cls
echo *** Sucesfull 5 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql
@cls
echo *** Sucesfull 9 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql
@cls
echo *** Sucesfull 7 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql
@cls
echo *** Sucesfull 8 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql
@cls
echo *** Sucesfull 9 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql
@cls
echo *** Sucesfull 10 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxaccess.sql
@cls
echo *** Sucesfull 11 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxes.sql
@cls
echo *** Sucesfull 12 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql
@cls
echo *** Sucesfull 13 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql
@cls
echo *** Sucesfull 14 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql
@cls
echo *** Sucesfull 15 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql
@cls
echo *** Sucesfull 16 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql
@cls
echo *** Sucesfull 17 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql
@cls
echo *** Sucesfull 18 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql
@cls
echo *** Sucesfull 19 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql
@cls
echo *** Sucesfull 20 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql
@cls
echo *** Sucesfull 21 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_raid_points.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
@cls
echo *** Sucesfull 22 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql
@cls
echo *** Sucesfull 23 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql
@cls
echo *** Sucesfull 24 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql
@cls
echo *** Sucesfull 25 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql
@cls
echo *** Sucesfull 26 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql
@cls
echo *** Sucesfull 27 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql
@cls
echo *** Sucesfull 28 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql
@cls
echo *** Sucesfull 29 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql
@cls
echo *** Sucesfull 30 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql
@cls
echo *** Sucesfull 31 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql
@cls
echo *** Sucesfull 32 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_notices.sql
@cls
echo ****** Sucesfull 33 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql
@cls
echo *** Sucesfull 34 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql
@cls
echo *** Sucesfull 35 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql
@cls
echo *** Sucesfull 36 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql
@cls
echo *** Sucesfull 37 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql
@cls
echo *** Sucesfull 38 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql
@cls
echo *** Sucesfull 39 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql
@cls
echo *** Sucesfull 40 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql
@cls
echo *** Sucesfull 41 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql
@cls
echo *** Sucesfull 42 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql
@cls
echo *** Sucesfull 43 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql
@cls
echo *** Sucesfull 44 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql
@cls
echo *** Sucesfull 45 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql
@cls
echo *** Sucesfull 46 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql
@cls
echo *** Sucesfull 47 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql
@cls
echo *** Sucesfull 48 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql
@cls
echo *** Sucesfull 49 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql
@cls
echo *** Sucesfull 50 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql
@cls
echo *** Sucesfull 51 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql
@cls
echo *** Sucesfull 52 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql
@cls
echo *** Sucesfull 53 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql
@cls
echo *** Sucesfull 54 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql
@cls
echo *** Sucesfull 55 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql
@cls
echo *** Sucesfull 56 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql
@cls
echo *** Sucesfull 57 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql
@cls
echo *** Sucesfull 58 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql
@cls
echo *** Sucesfull 59 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql
@cls
echo *** Sucesfull 60 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql
@cls
echo *** Sucesfull 61 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql
@cls
echo *** Sucesfull 62 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql
@cls
echo *** Sucesfull 63 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql
@cls
echo *** Sucesfull 64 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql
@cls
echo *** Sucesfull 65 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql
@cls
echo *** Sucesfull 66 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql
@cls
echo *** Sucesfull 67 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql
@cls
echo *** Sucesfull 68 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql
@cls
echo *** Sucesfull 69 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql
@cls
echo *** Sucesfull 70 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql
@cls
echo *** Sucesfull 71 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql
@cls
echo *** Sucesfull 72 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql
@cls
echo *** Sucesfull 73 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql
@cls
echo *** Sucesfull 74 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql
@cls
echo *** Sucesfull 75 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql
@cls
echo *** Sucesfull 76 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql
@cls
echo *** Sucesfull 77 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql
@cls
echo *** Sucesfull 78 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql
@cls
echo *** Sucesfull 79 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql
@cls
echo *** Sucesfull 80 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql
@cls
echo *** Sucesfull 81 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql
@cls
echo *** Sucesfull 83 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql
@cls
echo *** Sucesfull 84 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql
@cls
echo *** Sucesfull 85 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql
@cls
echo *** Sucesfull 87 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql
@cls
echo *** Sucesfull 88 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql
@cls
echo *** Sucesfull 89 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_weapon.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_teleport.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armor.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armorsets.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_contestable_clanhalls.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_shopids.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_droplist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_buylists.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_etcitem.sql
@cls
echo *** Sucesfull 90 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/rebirth_manager.sql
@cls
echo *** Sucesfull 91 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/remove_unneeded_spawns.sql
@cls
echo *** Sucesfull 92 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/admin_command_access_rights.sql
@cls
echo *** Sucesfull 93 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pkkills.sql
@cls
echo *** Sucesfull 94 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters_custom_data.sql
@cls
echo *** Sucesfull 95 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt_teams.sql
@cls
echo *** Sucesfull 97 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_door.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_doorupgrade.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fortsiege_clans.sql
@cls
echo *** Sucessfull 99 percents. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist_clear.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_siege.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/powerpak.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/paystream.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/smsonline.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/max_poly.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vip.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dm.sql
@cls
echo *** Sucesfull 100 percents. ***
echo.
echo GameServer Database %title%.
pause
goto :Step1

:addinstall
@cls
echo.
echo Put in database spawn GmShop, Classmaster and other goods...
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist.sql
echo GmShop and other goods spawned whis no-error. Greet!!!
pause
:end
echo.
echo Installed Sucessfull.
echo.
pause
goto :Step1

:end
echo.
echo Installing sucessfull.
echo.
pause

:fullend
