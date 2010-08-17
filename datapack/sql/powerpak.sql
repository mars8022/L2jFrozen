/*Table structure for table `buff_templates` */

DROP TABLE IF EXISTS `buff_templates`;

CREATE TABLE `buff_templates` (
  `id` int(11) unsigned NOT NULL,
  `name` varchar(35) NOT NULL default '',
  `skill_id` int(10) unsigned NOT NULL,
  `skill_name` varchar(35) default NULL,
  `skill_level` int(10) unsigned NOT NULL default '1',
  `skill_force` int(1) NOT NULL default '1',
  `skill_order` int(10) unsigned NOT NULL,
  `char_min_level` int(10) unsigned NOT NULL default '0',
  `char_max_level` int(10) unsigned NOT NULL default '0',
  `price_adena` decimal(10,0) NOT NULL default '-1',
  PRIMARY KEY  (`id`,`name`,`skill_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `buff_templates` */

insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (150,'FullbuffbyUsweer',1204,'Wind Walk',2,1,1,0,80,'-1'),(150,'FullbuffbyUsweer',1032,'Invigore',3,1,2,0,80,'-1'),(150,'FullbuffbyUsweer',1035,'Mental Shield',4,1,3,0,80,'-1'),(150,'FullbuffbyUsweer',1036,'Magic Barrier',2,1,4,0,80,'-1'),(150,'FullbuffbyUsweer',1040,'Shield',3,1,5,0,80,'-1'),(150,'FullbuffbyUsweer',1043,'Hily Weapon',1,1,6,0,80,'-1'),(150,'FullbuffbyUsweer',1044,'Regeneration',3,1,7,0,80,'-1'),(150,'FullbuffbyUsweer',1045,'Blessed Body',6,1,8,0,80,'-1'),(150,'FullbuffbyUsweer',1048,'Blessed Soul',6,1,9,0,80,'-1'),(150,'FullbuffbyUsweer',1062,'Berserker Spirit',2,1,10,0,80,'-1'),(150,'FullbuffbyUsweer',1068,'Might',3,1,11,0,80,'-1'),(150,'FullbuffbyUsweer',1077,'Focus',3,1,12,0,80,'-1'),(150,'FullbuffbyUsweer',1078,'Concentration',6,1,13,0,80,'-1'),(150,'FullbuffbyUsweer',1085,'Acumen',3,1,14,0,80,'-1'),(150,'FullbuffbyUsweer',1086,'Haste',2,1,15,0,80,'-1'),(150,'FullbuffbyUsweer',1087,'Agility',3,1,16,0,80,'-1'),(150,'FullbuffbyUsweer',1182,'Resist Aqua',3,1,17,0,80,'-1'),(150,'FullbuffbyUsweer',1189,'Resist Wind',3,1,18,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (150,'FullbuffbyUsweer',1191,'Resist Fire',3,1,19,0,80,'-1'),(150,'FullbuffbyUsweer',1033,'Resist Potion',3,1,20,0,80,'-1'),(150,'FullbuffbyUsweer',1240,'Guidance',3,1,21,0,80,'-1'),(150,'FullbuffbyUsweer',1242,'Death Whisper',3,1,22,0,80,'-1'),(150,'FullbuffbyUsweer',1243,'Bless Shield',6,1,23,0,80,'-1'),(150,'FullbuffbyUsweer',1257,'Decrease Weight',3,1,24,0,80,'-1'),(150,'FullbuffbyUsweer',1259,'Resist Shock',4,1,25,0,80,'-1'),(150,'FullbuffbyUsweer',1303,'Wild Magic',2,1,26,0,80,'-1'),(150,'FullbuffbyUsweer',1304,'Advanced Block',3,1,27,0,80,'-1'),(150,'FullbuffbyUsweer',1352,'Elemental Protection',1,1,28,0,80,'-1'),(150,'FullbuffbyUsweer',1353,'Divine Protection',1,1,29,0,80,'-1'),(150,'FullbuffbyUsweer',1354,'Arcane Protection',1,1,30,0,80,'-1'),(150,'FullbuffbyUsweer',1392,'Holy Resistance',1,1,31,0,80,'-1'),(150,'FullbuffbyUsweer',1393,'UnHoly Resistance',1,1,32,0,80,'-1'),(150,'FullbuffbyUsweer',1397,'Clarity',3,1,33,0,80,'-1'),(150,'FullbuffbyUsweer',1059,'Greater Empower',3,1,34,0,80,'-1'),(150,'FullbuffbyUsweer',1268,'Vampiric Rage',4,1,35,0,80,'-1'),(150,'FullbuffbyUsweer',1073,'Kiss of Eve',2,1,36,0,80,'-1'),(151,'FulldancebyUsweer',271,'Dance of Warior',1,1,1,0,80,'-1'),(151,'FulldancebyUsweer',272,'Dance of Inspiration',1,1,2,0,80,'-1'),(151,'FulldancebyUsweer',273,'Dance of Mystic',1,1,3,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (151,'FulldancebyUsweer',274,'Dance of Fire',1,1,4,0,80,'-1'),(151,'FulldancebyUsweer',275,'Dance of Fury',1,1,5,0,80,'-1'),(151,'FulldancebyUsweer',276,'Dance of Concentration',1,1,6,0,80,'-1'),(151,'FulldancebyUsweer',277,'Dance of Light',1,1,7,0,80,'-1'),(151,'FulldancebyUsweer',307,'Dance of Aqua Guard',1,1,8,0,80,'-1'),(151,'FulldancebyUsweer',309,'Dance of Earth Guard',1,1,9,0,80,'-1'),(151,'FulldancebyUsweer',310,'Dance of the Vampire',1,1,10,0,80,'-1'),(151,'FulldancebyUsweer',311,'Dance of Protection',1,1,11,0,80,'-1'),(151,'FulldancebyUsweer',365,'Sirens Dance',1,1,12,0,80,'-1'),(151,'FulldancebyUsweer',366,'Dance of Shadow',1,1,13,0,80,'-1'),(152,'FullsongbyUsweer',264,'Song of Earth',1,1,1,0,80,'-1'),(152,'FullsongbyUsweer',265,'Song of Life',1,1,2,0,80,'-1'),(152,'FullsongbyUsweer',266,'Song of Water',1,1,3,0,80,'-1'),(152,'FullsongbyUsweer',267,'Song of Warding',1,1,4,0,80,'-1'),(152,'FullsongbyUsweer',268,'Song of Wind',1,1,5,0,80,'-1'),(152,'FullsongbyUsweer',269,'Song of Hunter',1,1,6,0,80,'-1'),(152,'FullsongbyUsweer',270,'Song of Invocation',1,1,7,0,80,'-1'),(152,'FullsongbyUsweer',304,'Song of Vitality',1,1,8,0,80,'-1'),(152,'FullsongbyUsweer',305,'Song of Vengeance',1,1,9,0,80,'-1'),(152,'FullsongbyUsweer',306,'Song of Flame Guard',1,1,10,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (152,'FullsongbyUsweer',308,'Song of Storm Guard',1,1,11,0,80,'-1'),(152,'FullsongbyUsweer',349,'Song of Renewal',1,1,12,0,80,'-1'),(152,'FullsongbyUsweer',363,'Song of Meditation',1,1,13,0,80,'-1'),(152,'FullsongbyUsweer',364,'Song of Champion',1,1,14,0,80,'-1'),(153,'FulldominatorbyUsweer',1003,'Pa agrian Gift',3,1,1,0,80,'-1'),(153,'FulldominatorbyUsweer',1005,'Blessing of Pa agrio',3,1,2,0,80,'-1'),(153,'FulldominatorbyUsweer',1004,'The Wisdom of Pa agrio',3,1,3,0,80,'-1'),(153,'FulldominatorbyUsweer',1008,'The Glory  of Pa agrio',3,1,4,0,80,'-1'),(153,'FulldominatorbyUsweer',1249,'The Vision of Pa agrio',3,1,5,0,80,'-1'),(153,'FulldominatorbyUsweer',1250,'Under The Protection  of Pa agrio',3,1,6,0,80,'-1'),(153,'FulldominatorbyUsweer',1260,'The Tact  of Pa agrio',3,1,7,0,80,'-1'),(153,'FulldominatorbyUsweer',1261,'The Rage  of Pa agrio',2,1,8,0,80,'-1'),(153,'FulldominatorbyUsweer',1282,' Pa agrio Haste',2,1,9,0,80,'-1'),(153,'FulldominatorbyUsweer',1364,'The Eye of Pa agrio',1,1,10,0,80,'-1'),(153,'FulldominatorbyUsweer',1365,'The Soul of Pa agrio',1,1,11,0,80,'-1'),(153,'FulldominatorbyUsweer',1414,'Victories  of Pa agrio',1,1,12,0,80,'-1'),(153,'FulldominatorbyUsweer',1416,' Pa agrio Fist',1,1,13,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1002,'Flame Chant',3,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (154,'FulldoomcryerbyUsweer',1006,'Chant of Fire',3,1,2,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1007,'Chant of Battle',3,1,3,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1009,'Chant of Shielding',3,1,4,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1251,'Chant of Fury',2,1,5,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1252,'Chant of Evasion',3,1,6,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1253,'Chant of Rage',3,1,7,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1284,'Chant of Revenge',3,1,8,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1308,'Chant of Predator',3,1,9,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1309,'Chant of Eagle',3,1,10,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1310,'Chant of Vampire',4,1,11,0,80,'-1'),(154,'FulldoomcryerbyUsweer',1362,'Chant of Spirit',1,1,12,0,80,'-1'),(155,'WindWalkbyUsweer',1204,'Wind Walk',2,1,1,0,80,'-1'),(156,'InvigorebyUsweer',1032,'Invigore',3,1,1,0,80,'-1'),(157,'MentalShieldbyUsweer',1035,'Mental Shield',4,1,1,0,80,'-1'),(158,'MagicBarrierbyUsweer',1036,'Magic Barrier',2,1,1,0,80,'-1'),(159,'ShieldbyUsweer',1040,'Shield',3,1,1,0,80,'-1'),(160,'HilyWeaponbyUsweer',1043,'Hily Weapon',1,1,1,0,80,'-1'),(161,'RegenerationbyUsweer',1044,'Regeneration',3,1,1,0,80,'-1'),(162,'BlessedBodybyUsweer',1045,'Blessed Body',6,1,1,0,80,'-1'),(163,'BlessedSoulbyUsweer',1048,'Blessed Soul',6,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (164,'BerserkerSpiritbyUsweer',1062,'Berserker Spirit',2,1,1,0,80,'-1'),(165,'MightbyUsweer',1068,'Might',3,1,1,0,80,'-1'),(166,'FocusbyUsweer',1077,'Focus',3,1,1,0,80,'-1'),(167,'ConcentrationbyUsweer',1078,'Concentration',6,1,1,0,80,'-1'),(168,'AcumenbyUsweer',1085,'Acumen',3,0,1,0,80,'-1'),(169,'HastebyUsweer',1086,'Haste',2,1,1,0,80,'-1'),(170,'AgilitybyUsweer',1087,'Agility',3,1,1,0,80,'-1'),(171,'ResistAquabyUsweer',1182,'Resist Aqua',3,1,1,0,80,'-1'),(172,'ResistWindbyUsweer',1189,'Resist Wind',3,1,1,0,80,'-1'),(173,'ResistFirebyUsweer',1191,'Resist Fire',3,1,1,0,80,'-1'),(174,'ResistPotionbyUsweer',1033,'Resist Potion',3,1,1,0,80,'-1'),(175,'GuidancebyUsweer',1240,'Guidance',3,1,1,0,80,'-1'),(176,'DeathWhisperbyUsweer',1242,'Death Whisper',3,1,1,0,80,'-1'),(177,'BlessShieldbyUsweer',1243,'Bless Shield',6,1,1,0,80,'-1'),(178,'DecreaseWeightbyUsweer',1257,'Decrease Weight',3,1,1,0,80,'-1'),(179,'ResistShockbyUsweer',1259,'Resist Shock',4,1,1,0,80,'-1'),(180,'WildMagicbyUsweer',1303,'Wild Magic',2,1,1,0,80,'-1'),(181,'AdvancedBlockbyUsweer',1304,'Advanced Block',3,1,1,0,80,'-1'),(182,'ElementalProtectionbyUsweer',1352,'Elemental Protection',1,1,1,0,80,'-1'),(183,'DivineProtectionbyUsweer',1353,'Divine Protection',1,1,1,0,80,'-1'),(184,'ArcaneProtectionbyUsweer',1354,'Arcane Protection',1,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (185,'HolyResistancebyUsweer',1392,'Holy Resistance',1,1,1,0,80,'-1'),(186,'UnHolyResistancebyUsweer',1393,'UnHoly Resistance',1,1,1,0,80,'-1'),(187,'ClaritybyUsweer',1397,'Clarity',3,1,1,0,80,'-1'),(188,'GreaterEmpowerbyUsweer',1059,'Greater Empower',3,1,1,0,80,'-1'),(189,'VampiricRagebyUsweer',1268,'Vampiric Rage',4,1,1,0,80,'-1'),(190,'KissofEvebyUsweer',1073,'Kiss of Eve',2,1,1,0,80,'-1'),(191,'DanceofWariorbyUsweer',271,'Dance of Warior',1,1,1,0,80,'-1'),(192,'DanceofInspirationbyUsweer',272,'Dance of Inspiration',1,1,1,0,80,'-1'),(193,'DanceofMysticbyUsweer',273,'Dance of Mystic',1,1,1,0,80,'-1'),(194,'DanceofFirebyUsweer',274,'Dance of Fire',1,1,1,0,80,'-1'),(195,'DanceofFurybyUsweer',275,'Dance of Fury',1,1,1,0,80,'-1'),(196,'DanceofConcentrationbyUsweer',276,'Dance of Concentration',1,1,1,0,80,'-1'),(197,'DanceofLightbyUsweer',277,'Dance of Light',1,1,1,0,80,'-1'),(198,'DanceofAquaGuardbyUsweer',307,'Dance of Aqua Guard',1,1,1,0,80,'-1'),(199,'DanceofEarthGuardbyUsweer',309,'Dance of Earth Guard',1,1,1,0,80,'-1'),(200,'DanceoftheVampirebyUsweer',310,'Dance of the Vampire',1,1,1,0,80,'-1'),(201,'DanceofProtectionbyUsweer',311,'Dance of Protection',1,1,1,0,80,'-1'),(202,'SirensDancebyUsweer',365,'Sirens Dance',1,1,1,0,80,'-1'),(203,'DanceofShadowbyUsweer',366,'Dance of Shadow',1,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (204,'SongofEarthbyUsweer',264,'Song of Earth',1,1,1,0,80,'-1'),(205,'SongofLifebyUsweer',265,'Song of Life',1,1,1,0,80,'-1'),(206,'SongofWaterbyUsweer',266,'Song of Water',1,1,1,0,80,'-1'),(207,'SongofWardingbyUsweer',267,'Song of Warding',1,1,1,0,80,'-1'),(208,'SongofWindbyUsweer',268,'Song of Wind',1,1,1,0,80,'-1'),(209,'SongofHunterbyUsweer',269,'Song of Hunter',1,1,1,0,80,'-1'),(210,'SongofInvocationbyUsweer',270,'Song of Invocation',1,1,1,0,80,'-1'),(211,'SongofVitalitybyUsweer',304,'Song of Vitality',1,1,1,0,80,'-1'),(212,'SongofVengeancebyUsweer',305,'Song of Vengeance',1,1,1,0,80,'-1'),(213,'SongofFlameGuardbyUsweer',306,'Song of Flame Guard',1,1,1,0,80,'-1'),(214,'SongofStormGuardbyUsweer',308,'Song of Storm Guard',1,1,1,0,80,'-1'),(215,'SongofRenewalbyUsweer',349,'Song of Renewal',1,1,1,0,80,'-1'),(216,'SongofMeditationbyUsweer',363,'Song of Meditation',1,1,1,0,80,'-1'),(217,'SongofChampionbyUsweer',364,'Song of Champion',1,1,1,0,80,'-1'),(218,'PaagrianGiftbyUsweer',1003,'Pa agrian Gift',3,1,1,0,80,'-1'),(219,'BlessingofPaagriobyUsweer',1005,'Blessing of Pa agrio',3,1,1,0,80,'-1'),(220,'TheWisdomofPaagriobyUsweer',1004,'The Wisdom of Pa agrio',3,1,1,0,80,'-1'),(221,'TheGloryofPaagriobyUsweer',1008,'The Glory  of Pa agrio',3,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (222,'TheVisionofPaagriobyUsweer',1249,'The Vision of Pa agrio',3,1,1,0,80,'-1'),(223,'UnderTheProtectionofPaagriobyUsweer',1250,'Under The Protection  of Pa agrio',3,1,1,0,80,'-1'),(224,'TheTactofPaagriobyUsweer',1260,'The Tact  of Pa agrio',3,1,1,0,80,'-1'),(225,'TheRageofPaagriobyUsweer',1261,'The Rage  of Pa agrio',2,1,1,0,80,'-1'),(226,'PaagrioHastebyUsweer',1282,' Pa agrio Haste',2,1,1,0,80,'-1'),(227,'TheEyeofPaagriobyUsweer',1364,'The Eye of Pa agrio',1,1,1,0,80,'-1'),(228,'TheSoulofPaagriobyUsweer',1365,'The Soul of Pa agrio',1,1,1,0,80,'-1'),(229,'VictoriesofPaagriobyUsweer',1414,'Victories  of Pa agrio',1,1,1,0,80,'-1'),(230,'PaagrioFistbyUsweer',1416,' Pa agrio Fist',1,1,1,0,80,'-1'),(231,'FlameChantbyUsweer',1002,'Flame Chant',3,1,1,0,80,'-1'),(232,'ChantofFirebyUsweer',1006,'Chant of Fire',3,1,1,0,80,'-1'),(233,'ChantofBattlebyUsweer',1007,'Chant of Battle',3,1,1,0,80,'-1'),(234,'ChantofShieldingbyUsweer',1009,'Chant of Shielding',3,1,1,0,80,'-1'),(235,'ChantofFurybyUsweer',1251,'Chant of Fury',2,1,1,0,80,'-1'),(236,'ChantofEvasionbyUsweer',1252,'Chant of Evasion',3,1,1,0,80,'-1'),(237,'ChantofRagebyUsweer',1253,'Chant of Rage',3,1,1,0,80,'-1'),(238,'ChantofRevengebyUsweer',1284,'Chant of Revenge',3,1,1,0,80,'-1'),(239,'ChantofPredatorbyUsweer',1308,'Chant of Predator',3,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (240,'ChantofEaglebyUsweer',1309,'Chant of Eagle',3,1,1,0,80,'-1'),(241,'ChantofVampirebyUsweer',1310,'Chant of Vampire',4,1,1,0,80,'-1'),(242,'ChantofSpiritbyUsweer',1362,'Chant of Spirit',1,1,1,0,80,'-1'),(243,'ProphecyofFirebyUsweer',1356,'Prophecy of Fire',1,1,1,0,80,'-1'),(244,'ProphecyofWaterbyUsweer',1355,'Prophecy of Water',1,1,1,0,80,'-1'),(245,'ProphecyofWindbyUsweer',1357,'Prophecy of Wind',1,1,1,0,80,'-1'),(246,'ChantofVictorybyUsweer',1363,'Chant of Victory',1,1,1,0,80,'-1'),(247,'GreateMightbyUsweer',1388,'Greate Might',3,1,1,0,80,'-1'),(248,'GreateShieldbyUsweer',1389,'Greate Shield',3,1,1,0,80,'-1'),(249,'WarChantbyUsweer',1390,'War Chant',3,1,1,0,80,'-1'),(250,'EarthChantbyUsweer',1391,'Earth Chant',3,1,1,0,80,'-1'),(251,'BlessingofSeraphimbyUsweer',4702,'Blessing of Seraphim',8,1,1,0,80,'-1'),(252,'GiftofSeraphimbyUsweer',4703,'Gift of Seraphim',8,1,1,0,80,'-1'),(253,'BlessingofQueenbyUsweer',4699,'Blessing of Queen',8,1,1,0,80,'-1'),(254,'GiftofQueenbyUsweer',4700,'Gift of Queen',8,1,1,0,80,'-1'),(255,'SummonLifebyUsweer',67,'Summon Life',7,1,1,0,80,'-1'),(256,'SummonAquabyUsweer',1280,'Summon Aqua',9,1,1,0,80,'-1'),(257,'SummonStormbyUsweer',1328,'Summon Storm',8,1,1,0,80,'-1'),(258,'SummonBindingbyUsweer',1279,'Summon Binding',9,1,1,0,80,'-1');
insert into `buff_templates` (`id`,`name`,`skill_id`,`skill_name`,`skill_level`,`skill_force`,`skill_order`,`char_min_level`,`char_max_level`,`price_adena`) values (259,'SummonSparkbyUsweer',1281,'Summon Spark',9,1,1,0,80,'-1'),(260,'SummonPhantombyUsweer',33,'Summon Phantom',8,1,1,0,80,'-1');

insert into `custom_npc` (`id`,`idTemplate`,`name`,`serverSideName`,`title`,`serverSideTitle`,`class`,`collision_radius`,`collision_height`,`level`,`sex`,`type`,`attackrange`,`hp`,`mp`,`hpreg`,`mpreg`,`str`,`con`,`dex`,`int`,`wit`,`men`,`exp`,`sp`,`patk`,`pdef`,`matk`,`mdef`,`atkspd`,`aggro`,`matkspd`,`rhand`,`lhand`,`armor`,`walkspd`,`runspd`,`faction_id`,`faction_range`,`isUndead`,`absorb_level`,`absorb_type`) values ('50018',30499,'Balin',1,'Engraver',1,'NPC.e_smith_master_MDwarf','8.00','20.50','80','male','L2Npc',40,'8888','8888',NULL,NULL,'40','43','30','21','20','10','0','0','1314','470','780','382','278','0','333','0','0','0','10','132',NULL,'0',0,'0','LAST_HIT'),('50019',31360,'Helga',1,'Buffer',1,'NPC.a_casino_FDarkElf','8.00','20.50','80','female','L2Npc',40,'8888','99999',NULL,NULL,'40','43','30','21','20','10','0','0','1314','470','780','382','278','0','3000','0','0','0','10','132',NULL,'0',0,'0','LAST_HIT');

DROP TABLE IF EXISTS `engraved_items`;

CREATE TABLE `engraved_items` (
  `object_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `engraver_id` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `engraved_items` */


/*Table structure for table `engraved_log` */

DROP TABLE IF EXISTS `engraved_log`;

CREATE TABLE `engraved_log` (
  `object_id` int(11) NOT NULL,
  `actiondate` decimal(12,0) NOT NULL,
  `process` varchar(64) NOT NULL,
  `itemName` varchar(64) NOT NULL,
  `form_char` varchar(64) NOT NULL,
  `to_char` varchar(64) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*Data for the table `engraved_log` */

INSERT INTO `teleport` VALUES ('ForbiddenPath', '2022', '112893', '84529', '-6541', '0', '0');
INSERT INTO `teleport` VALUES ('DarkOmen', '2021', '-20706', '13484', '-4901', '0', '0');
INSERT INTO `teleport` VALUES ('Witch', '2020', '139339', '79682', '-5429', '0', '0');
INSERT INTO `teleport` VALUES ('Apostate', '2019', '76549', '78429', '-5124', '0', '0');
INSERT INTO `teleport` VALUES ('Branded', '2018', '44953', '170285', '-4981', '0', '0');
INSERT INTO `teleport` VALUES ('Heretics', '2017', '41763', '143930', '-5407', '0', '0');
INSERT INTO `teleport` VALUES ('Dark Elven', '6012', '12090', '16698', '-4585', '0', '0');
INSERT INTO `teleport` VALUES ('Dwarven', '2501', '115332', '-178232', '-929', '0', '0');
INSERT INTO `teleport` VALUES ('Orc', '2503', '-44856', '-112381', '-240', '0', '0');
INSERT INTO `teleport` VALUES ('Elven', '2502', '47012', '50700', '-2996', '0', '0');
INSERT INTO `teleport` VALUES ('Talkong Island', '2504', '-83862', '243471', '-3730', '0', '0');
INSERT INTO `teleport` VALUES ('Rune', '2521', '43833', '-48346', '-797', '0', '0');
INSERT INTO `teleport` VALUES ('Oren', '2815', '82755', '53583', '-1522', '0', '0');
INSERT INTO `teleport` VALUES ('Goddard', '2520', '147728', '-55395', '-2759', '0', '0');
INSERT INTO `teleport` VALUES ('Aden', '2814', '147447', '25748', '-2013', '0', '0');
INSERT INTO `teleport` VALUES ('Innadril', '2816', '111396', '218917', '-3569', '0', '0');
INSERT INTO `teleport` VALUES ('Gludio', '7556', '-12683', '122716', '-3143', '0', '0');
INSERT INTO `teleport` VALUES ('Giran', '2808', '83026', '148631', '-3469', '0', '0');
INSERT INTO `teleport` VALUES ('Dion', '2505', '15641', '142961', '-2732', '0', '0');
INSERT INTO `teleport` VALUES ('Pavel Ruins', '5002', '88275', '-125690', '-3815', '0', '0');
INSERT INTO `teleport` VALUES ('Monastery of Silence', '5008', '106467', '-87805', '-2950', '0', '0');
INSERT INTO `teleport` VALUES ('Ice Queens Castle', '5000', '102492', '-124255', '-2771', '0', '0');
INSERT INTO `teleport` VALUES ('Town of Schuttgar', '5001', '87363', '-143348', '-1293', '0', '0');
INSERT INTO `teleport` VALUES ('Carons Dungeon', '5003', '69762', '-111260', '-1807', '0', '0');
INSERT INTO `teleport` VALUES ('Crypts of Disgrace', '5004', '56095', '-118952', '-3290', '0', '0');
INSERT INTO `teleport` VALUES ('Ice Merchant Cabin', '5005', '113453', '-109965', '-866', '0', '0');
INSERT INTO `teleport` VALUES ('Stakatonest', '5007', '89689', '-44691', '-2142', '0', '0');
INSERT INTO `teleport` VALUES ('Core', '5015', '17622', '111042', '-6655', '0', '0');
INSERT INTO `teleport` VALUES ('Quent Ant', '5017', '-21547', '180058', '-5834', '0', '0');
INSERT INTO `teleport` VALUES ('Orfen', '5016', '55467', '17289', '-5541', '0', '0');
INSERT INTO `teleport` VALUES ('Zaken', '1000006', '55553', '219381', '-3225', '0', '0');
INSERT INTO `teleport` VALUES ('Baium', '5013', '114034', '15437', '10077', '0', '0');
INSERT INTO `teleport` VALUES ('Antharas', '5012', '180981', '114858', '-7703', '0', '0');
INSERT INTO `teleport` VALUES ('Valakas', '5011', '210476', '-113600', '-138', '0', '0');
INSERT INTO `teleport` VALUES ('swamp', '2933', '-30742', '49730', '-3568', '0', '0');
INSERT INTO `teleport` VALUES ('Temple', '2934', '10845', '14502', '-4242', '0', '0');
INSERT INTO `teleport` VALUES ('school', '2931', '-49180', '49426', '-5916', '0', '0');
INSERT INTO `teleport` VALUES ('elvenFortess', '2946', '45873', '49288', '-3064', '0', '0');
INSERT INTO `teleport` VALUES ('irisLake', '2947', '48118', '72491', '-3481', '0', '0');
INSERT INTO `teleport` VALUES ('obtliVictory', '2877', '-99666', '237569', '-3562', '0', '0');
INSERT INTO `teleport` VALUES ('elvenruins', '2875', '-113342', '235325', '-3648', '0', '0');
INSERT INTO `teleport` VALUES ('talkinharbor', '2878', '-96804', '258705', '-3613', '0', '0');
INSERT INTO `teleport` VALUES ('savetrials', '2940', '9349', '-112499', '-2531', '0', '0');
INSERT INTO `teleport` VALUES ('frozen', '2941', '7615', '-138852', '-915', '0', '0');
INSERT INTO `teleport` VALUES ('mithril', '2942', '171963', '-173368', '3453', '0', '0');
INSERT INTO `teleport` VALUES ('abondoned', '2937', '139734', '-177458', '-1531', '0', '0');
INSERT INTO `teleport` VALUES ('floran', '2506', '16763', '170007', '-3493', '0', '0');
INSERT INTO `teleport` VALUES ('cruma', '2825', '17176', '114201', '-3440', '0', '0');
INSERT INTO `teleport` VALUES ('cruma1', '2826', '17737', '114119', '-11673', '0', '0');
INSERT INTO `teleport` VALUES ('cruma2', '2827', '17725', '108327', '-9058', '0', '0');
INSERT INTO `teleport` VALUES ('AtharasLair', '2822', '131426', '114452', '-3721', '0', '0');
INSERT INTO `teleport` VALUES ('DEvilIsle', '2954', '43403', '206864', '-3747', '0', '0');
INSERT INTO `teleport` VALUES ('giranharbor', '2953', '47921', '186665', '-3486', '0', '0');
INSERT INTO `teleport` VALUES ('hardins', '2952', '105891', '109761', '-3207', '0', '0');
INSERT INTO `teleport` VALUES ('breaka', '2949', '79794', '130617', '-3672', '0', '0');
INSERT INTO `teleport` VALUES ('ruinsArgony', '2965', '-42485', '120050', '-3514', '0', '0');
INSERT INTO `teleport` VALUES ('ruinsDespair', '2966', '-20047', '137622', '-33892', '0', '0');
INSERT INTO `teleport` VALUES ('AntNest', '2879', '-9983', '176472', '-4177', '0', '0');
INSERT INTO `teleport` VALUES ('Aligators', '2993', '126460', '174784', '-3074', '0', '0');
INSERT INTO `teleport` VALUES ('Filesolence', '2996', '82664', '183571', '-3592', '0', '0');
INSERT INTO `teleport` VALUES ('FileWhispers', '2997', '91203', '217092', '-3644', '0', '0');
INSERT INTO `teleport` VALUES ('GardenEva', '2817', '86008', '231050', '-3595', '0', '0');
INSERT INTO `teleport` VALUES ('Blazing', '2983', '159458', '-12933', '-2867', '0', '0');
INSERT INTO `teleport` VALUES ('Hunters', '2982', '116451', '76069', '-2730', '0', '0');
INSERT INTO `teleport` VALUES ('Coliseum', '2991', '146499', '46735', '-3435', '0', '0');
INSERT INTO `teleport` VALUES ('TOI', '2819', '120189', '16114', '-5135', '0', '0');
INSERT INTO `teleport` VALUES ('wallAgros', '2524', '165056', '-47846', '-3555', '0', '0');
INSERT INTO `teleport` VALUES ('varka', '2522', '125517', '-41246', '-3688', '0', '0');
INSERT INTO `teleport` VALUES ('HotSpring', '2532', '149063', '-112408', '-2076', '0', '0');
INSERT INTO `teleport` VALUES ('ketra', '2528', '147005', '-67119', '-3650', '0', '0');
INSERT INTO `teleport` VALUES ('forgenGods', '2530', '168995', '-116279', '-2449', '0', '0');
INSERT INTO `teleport` VALUES ('ivory', '2867', '85332', '16153', '-3694', '0', '0');
INSERT INTO `teleport` VALUES ('seaSpores', '2870', '64320', '26795', '-3763', '0', '0');
INSERT INTO `teleport` VALUES ('ValeySaints', '2527', '67985', '-72039', '-3748', '0', '0');
INSERT INTO `teleport` VALUES ('Forest', '2000', '52107', '-53940', '-3154', '0', '0');
INSERT INTO `teleport` VALUES ('SwampScreams', '2525', '69988', '-49910', '-3246', '0', '0');
INSERT INTO `teleport` VALUES ('RuneHarbor', '2001', '38014', '-38309', '-3609', '0', '0');


