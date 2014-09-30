-- 
-- Dumping data for table `npc`
-- 
INSERT ignore INTO custom_npc (`id`, `idTemplate`, `name`, `serverSideName`, `title`, `serverSideTitle`, `class`, `collision_radius`, `collision_height`, `level`, `sex`, `type`, `attackrange`, `hp`, `mp`, `hpreg`, `mpreg`, `str`, `con`, `dex`, `int`, `wit`, `men`, `exp`, `sp`, `patk`, `pdef`, `matk`, `mdef`, `atkspd`, `aggro`, `matkspd`, `rhand`, `lhand`, `armor`, `walkspd`, `runspd`, `faction_id`, `faction_range`, `isUndead`, `absorb_level`, `absorb_type`)
VALUES
(50007, 31324, 'Andromeda', '1', 'Wedding Manager', '1', 'NPC.a_casino_FDarkElf', '8.00', '23.00', '70', 'female', 'L2WeddingManager', '40', '3862', '1493', '500', '500', '40', '43', '30', '21', '20', '10', '0', '0', '9999', '9999', '999', '999', '278', '0', '333', '316', '0', '0', '55', '132', null, '0', '1', '0', 'LAST_HIT'),
(50017, 31854, 'Protector', '1', 'PVP/PK Manager', '1', 'NPC.a_maidA_FHuman', '8.00', '20.50', '80', 'female', 'L2Protector', '40', '99999', '9999', null, null, '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '10000', '382', '278', '0', '3000', '0', '0', '0', '55', '132', null, '0', '0', '0', 'LAST_HIT'),
('50018',30499,'Balin',1,'Engraver',1,'NPC.e_smith_master_MDwarf','8.00','20.50','80','male','L2Npc',40,'8888','8888',NULL,NULL,'40','43','30','21','20','10','0','0','1314','470','780','382','278','0','333','0','0','0','10','132',NULL,'0',0,'0','LAST_HIT'),
(55555, 22124, 'Totor', '1', 'Rebirth Manager', '1', 'NPC.a_fighterguild_master_FHuman', '11.00', '27.00', '83', 'male', 'L2Merchant', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT'),
(70010, 31606, 'Catrina', '1', 'TvT Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT'),
(70011, 31606, 'Catretta', '1', 'CTF Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT'),
(70012, 31606, 'Catrosa', '1', 'VIP Join Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT'),
(70013, 31606, 'Catrigna', '1', 'VIP End Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT'),
(70014, 31606, 'Catrieta', '1', 'DM Event Manager', '1', 'Monster2.queen_of_cat', '8.00', '15.00', '70', 'female', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '0', '0', '0', '28', '132', null, '0', '0', '0', 'LAST_HIT');

insert ignore into `custom_npc` (`id`, `idTemplate`, `name`, `serverSideName`, `title`, `serverSideTitle`, `class`, `collision_radius`, `collision_height`, `level`, `sex`, `type`, `attackrange`, `hp`, `mp`, `hpreg`, `mpreg`, `str`, `con`, `dex`, `int`, `wit`, `men`, `exp`, `sp`, `patk`, `pdef`, `matk`, `mdef`, `atkspd`, `aggro`, `matkspd`, `rhand`, `lhand`, `armor`, `walkspd`, `runspd`, `faction_id`, `faction_range`, `isUndead`, `absorb_level`) 
values
('66666',32130, 'Looney the Cat', 1, 'Nobless Trader', 1, 'Monster3.king_of_cat', 6.00, 16.01, 70, 'male', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 88, 132, '', 0, 0, 0);

insert ignore into `custom_npc` (`id`, `idTemplate`, `name`, `serverSideName`, `title`, `serverSideTitle`, `class`, `collision_radius`, `collision_height`, `level`, `sex`, `type`, `attackrange`, `hp`, `mp`, `hpreg`, `mpreg`, `str`, `con`, `dex`, `int`, `wit`, `men`, `exp`, `sp`, `patk`, `pdef`, `matk`, `mdef`, `atkspd`, `aggro`, `matkspd`, `rhand`, `lhand`, `armor`, `walkspd`, `runspd`, `faction_id`, `faction_range`, `isUndead`, `absorb_level`) 
values
('66667',32130, 'Fragola the Cat', 1, 'Clan Manager', 1, 'Monster3.king_of_cat', 6.00, 16.01, 70, 'male', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 88, 132, '', 0, 0, 0);

INSERT ignore INTO custom_npc
values
(53, 20830, 'Angelic Shop', 1, 'L2 Frozen', 1, 'Monster.angel', 13.50, 36.50, 72, 'male', 'L2Merchant', 40, 4013, 1565, 53.72, 3.09, 40, 43, 30, 21, 20, 10, 0, 0, 1710, 406, 849, 399, 278, 500, 333, 148, 0, 0, 33, 187, 'tower_guard_clan', 400, 0, 10, 'LAST_HIT'), 
(7077, 31862, 'Vortex Gatekeeper', 1, 'L2 Frozen', 1, 'NPC.broadcasting_tower', 7.00, 35.00, 70, 'etc', 'L2Teleporter', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 490, 10, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 55, 132, '', 0, 1, 0, 'LAST_HIT'),
(50019, 27214, 'Guardian Buffer', 1, 'L2 Frozen', 1, 'Monster2.apostle_warrior', 8.00, 30.00, 76, 'female', 'L2Npc', 40, 4297, 1710, 13.43, 3.09, 40, 43, 30, 21, 20, 10, 0, 0, 2242, 534, 994, 433, 200, 0, 333, 80, 0, 0, 55, 198, 'guard_of_secrets_clan', 300, 0, 0, 'LAST_HIT');

INSERT INTO `custom_npc` VALUES 
(93000, 30705, 'Boss Manager', 1, 'Raid Info', 1, 'NPC.e_fighterguild_teacher_MOrc', 8.00, 28.50, 70, 'male', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 55, 132, '', 0, 1, 0, 'LAST_HIT');

INSERT INTO `custom_npc` VALUES 
(54, 21654, 'Necromancer of Destruction', 1, 'Equip Manager', 1, 'Monster.vale_master_20_bi', 14.50, 48.00, 80, 'male', 'L2Merchant', 40, 4608, 1896, 67.15, 3.09, 40, 43, 30, 21, 20, 10, 8042, 913, 1863, 587, 1182, 477, 278, 150, 333, 0, 0, 0, 77, 154, 'fire_clan', 300, 0, 0, 'LAST_HIT');

insert into custom_npc values 
(50020, 30298, 'Augmenter', 1, 'L2Frozen', 1, 'NPC.a_smith_MDwarf', 7.00, 16.50, 70, 'male', 'L2Trainer', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 89, 0, 0, 55, 132, '', 0, 1, 0, 'LAST_HIT');