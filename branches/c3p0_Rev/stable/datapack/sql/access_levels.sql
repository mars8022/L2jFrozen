-- How to configure the SQL based access level system :
--
-- There are two tables.
--
-- First one is named acess_levels and it's meant to define the different possible
-- groups a GM can belong to.
--
-- You can see in this table six predefined GM groups. Each group has a different
-- accessLevel, and GM's access_level in the characters table should match with
-- one of these. You could define as many groups as needed and give them whatever
-- number you wanted to, from 1 to 255. Nevertheless please note the fact that
-- there is one group that will be reserved for allmighty administrators, and this
-- group is bound to the following rules:
--
--    * There's no need/way to restrict the commands this group is able to run, its
--      members will be able to perform ANY admin_command.
--
--    * One number must be reserved for this group, and by default it is set to 127.
--
--    * In order to change this default group number or its name/title colors,
--      you should look at the Character.properties configuration file and
--      change the value of MasterAccessLevel, MasterNameColor and MasterTitleColor
--      respectively.
--
--    * You should better not use this group as a part of any childs hierarchy.
--
-- In our predefined set of examples, access_level=1 is for the highest admin,
-- and access_level=3 is for Event GMs.
--
-- The rest of the access_levels table columns are expected to be self explanatory.
--
-- And there is a second table named admin_command_access_rights and in this table
-- administrators should add every command they wanted GMs to use.
--
-- We left just one query here to show how commands should be added to the table:
--
-- INSERT IGNORE INTO `admin_command_access_rights` VALUES ('admin_admin','6');
--
-- If an administrator wanted to grant his GMs from group 4 the usage of the //para
-- command, he should just copy our example and replace values like this:
--
-- INSERT IGNORE INTO `admin_command_access_rights` VALUES ('admin_para','4');
--
-- So on, for each command there should be a record in this table. And it would be
-- advisable to use one query per command to avoid messups ;)

-- ---------------------------------
-- Table structure for access_levels
-- ---------------------------------
CREATE TABLE IF NOT EXISTS `access_levels` (
  `accessLevel` MEDIUMINT(9) NOT NULL,
  `name` VARCHAR(255) NOT NULL DEFAULT '',
  `nameColor` CHAR(6) NOT NULL DEFAULT 'FFFFFF',
  `useNameColor` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `titleColor` CHAR(6) NOT NULL DEFAULT 'FFFFFF',
  `useTitleColor` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `isGm` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `allowPeaceAttack` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `allowFixedRes` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `allowTransaction` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `allowAltg` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `giveDamage` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `takeAggro` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `gainExp` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  `canDisableGmStatus` TINYINT(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`accessLevel`)
) DEFAULT CHARSET=utf8;
-- -------------------------------
-- Records for table access_levels
-- -------------------------------

INSERT IGNORE INTO `access_levels` VALUES 
(1, 'Master Access', '0099FF', 1, '0099FF', 0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
(2, 'Head GM', '00FFFF', 1, '00FFFF', 0, 1, 1, 1, 1, 1, 1, 1, 1, 1),
(3, 'Event GM', '00FFFF', 1, '00FFFF', 0, 1, 1, 1, 0, 1, 0, 0, 0, 0),
(4, 'Support GM', '00FFFF', 1, '00FFFF', 0, 1, 0, 1, 0, 1, 0, 0, 0, 0),
(5, 'General GM', '00FFFF', 1, '00FFFF', 0, 1, 0, 1, 0, 1, 0, 0, 0, 0),
(6, 'Test GM', 'FFFFFF', 1, 'FFFFFF', 0, 0, 0, 1, 0, 1, 0, 0, 0, 0);