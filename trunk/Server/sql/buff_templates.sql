--
-- Table structure for buff_templates
--
CREATE TABLE IF NOT EXISTS `buff_templates` (
  `id` int(11) unsigned NOT NULL,
  `name` varchar(35) NOT NULL default '',
  `skill_id` int(10) unsigned NOT NULL,
  `skill_name` varchar(35) default NULL,
  `skill_level` int(10) unsigned NOT NULL default '1',
  `skill_force` int(1) NOT NULL default '1',
  `skill_order` int(10) unsigned NOT NULL,
  `char_min_level` int(10) unsigned NOT NULL default '0',
  `char_max_level` int(10) unsigned NOT NULL default '0',
  `char_race` int(1) unsigned NOT NULL default '0',
  `char_class` int(1) NOT NULL default '0',
  `char_faction` int(10) unsigned NOT NULL default '0',
  `price_adena` int(10) unsigned NOT NULL default '0',
  `price_points` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`,`name`,`skill_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1033', 'Resist Poison', '3', '1', '1', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1032', 'Invigor', '3', '0', '2', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1040', 'Shield', '3', '0', '3', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1035', 'Mental Shield', '4', '0', '4', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1259', 'Resist Shock', '4', '0', '5', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1204', 'Wind Walk', '2', '0', '6', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1257', 'Decrease Weight', '3', '0', '7', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1045', 'Bless the Body', '6', '0', '8', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1078', 'Concentration', '6', '0', '9', '1', '40', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1303', 'Wild Magic', '2', '0', '10', '1', '40', '0', '2', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1048', 'Bless the Soul', '6', '0', '11', '1', '40', '0', '2', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1085', 'Acumen', '3', '0', '12', '1', '40', '0', '2', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1059', 'Empower', '3', '0', '13', '1', '40', '0', '2', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1010', 'Soul Shield', '3', '0', '14', '1', '40', '0', '2', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1068', 'Might', '3', '0', '15', '1', '40', '0', '1', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1086', 'Haste', '2', '0', '16', '1', '40', '0', '1', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1077', 'Focus', '3', '0', '17', '1', '40', '0', '1', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1242', 'Death Whisper', '3', '0', '18', '1', '40', '0', '1', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('1', 'Newbie', '1268', 'Vampiric Rage', '4', '0', '19', '1', '40', '0', '1', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1033', 'Resist Poison', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1032', 'Invigor', '3', '0', '2', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1352', 'Elemental Protection', '1', '0', '3', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1392', 'Holy Resistance', '3', '0', '4', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1393', 'Unholy Resistance', '3', '0', '5', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1035', 'Mental Shield', '4', '0', '6', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1259', 'Resist Shock', '4', '0', '7', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('2', 'ResistBuffs', '1354', 'Arcane Protection', '1', '0', '8', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1204', 'Wind Walk', '2', '0', '1', '1', '80', '0', '0', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1257', 'Decrease Weight', '3', '0', '2', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1073', 'Kiss of Eva', '2', '0', '3', '1', '80', '0', '0', '0', '20000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1040', 'Shield', '3', '0', '4', '1', '80', '0', '0', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1068', 'Might', '3', '0', '5', '1', '80', '0', '1', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1086', 'Haste', '2', '0', '6', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1077', 'Focus', '3', '0', '7', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1242', 'Death Whisper', '3', '0', '8', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1045', 'Bless the Body', '6', '0', '9', '1', '80', '0', '0', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1044', 'Regeneration', '3', '0', '10', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1240', 'Guidance', '3', '0', '11', '1', '80', '0', '1', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1087', 'Agility', '3', '0', '12', '1', '80', '0', '1', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1010', 'Soul Shield', '3', '0', '13', '1', '80', '0', '1', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1036', 'Magic Barrier', '2', '0', '14', '1', '80', '0', '1', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1243', 'Bless Shield', '6', '0', '15', '1', '80', '0', '1', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1268', 'Vampiric Rage', '4', '0', '16', '1', '80', '0', '1', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1303', 'Wild Magic', '2', '0', '17', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1048', 'Bless the Soul', '6', '0', '18', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1078', 'Concentration', '6', '0', '19', '1', '80', '0', '2', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1085', 'Acumen', '3', '0', '20', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1059', 'Empower', '3', '0', '21', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '1062', 'Berserker Spirit', '2', '0', '22', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '275', 'Dance of Fury', '1', '0', '23', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '274', 'Dance of Fire', '1', '0', '24', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '271', 'Dance of Warrior', '1', '0', '25', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '264', 'Song of Earth', '1', '0', '26', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '269', 'Song of Hunter', '1', '0', '27', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '267', 'Song of Warding', '1', '0', '28', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '276', 'Dance of Concentration', '1', '0', '29', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '273', 'Dance of Mystic', '1', '0', '30', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '311', 'Dance of Protection', '1', '0', '31', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '265', 'Song of Life', '1', '0', '32', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '363', 'Song of Meditation', '1', '0', '33', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('3', 'DeluxeBuffs', '349', 'Song of Renewal', '1', '0', '34', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1040', 'Shield', '3', '0', '1', '1', '80', '0', '0', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1036', 'Magic Barrier', '2', '0', '2', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1045', 'Bless the Body', '6', '0', '3', '1', '80', '0', '0', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1048', 'Bless the Soul', '6', '0', '4', '1', '80', '0', '0', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1243', 'Bless Shield', '6', '0', '5', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1352', 'Elemental Protection', '1', '0', '6', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1204', 'Wind Walk', '2', '0', '7', '1', '80', '0', '0', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1062', 'Berserker Spirit', '2', '0', '8', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1032', 'Invigor', '3', '0', '9', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1035', 'Mental Shield', '4', '0', '10', '1', '80', '0', '0', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1259', 'Resist Shock', '4', '0', '11', '1', '80', '0', '0', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1044', 'Regeneration', '3', '0', '12', '1', '80', '0', '0', '0', '100000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1068', 'Might', '3', '0', '13', '1', '80', '0', '1', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1086', 'Haste', '2', '0', '14', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1077', 'Focus', '3', '0', '15', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1242', 'Death Whisper', '3', '0', '16', '1', '80', '0', '1', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1268', 'Vampiric Rage', '4', '0', '17', '1', '80', '0', '1', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1087', 'Agility', '3', '0', '18', '1', '80', '0', '1', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1240', 'Guidance', '3', '0', '19', '1', '80', '0', '1', '0', '150000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1059', 'Empower', '3', '0', '20', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1078', 'Concentration', '6', '0', '21', '1', '80', '0', '2', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('4', 'Prophet', '1085', 'Acumen', '3', '0', '22', '1', '80', '0', '2', '0', '250000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '364', 'Song of Champion', '1', '0', '1', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '264', 'Song of Earth', '1', '0', '2', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '306', 'Song of Flame Guard', '1', '0', '3', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '308', 'Song of Storm Guard', '1', '0', '4', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '270', 'Song of Invocation', '1', '0', '5', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '265', 'Song of Life', '1', '0', '6', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '349', 'Sonf of Renewal', '1', '0', '7', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '267', 'Song of Warding', '1', '0', '8', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '266', 'Song of Water', '1', '0', '9', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '268', 'Song of Wind', '1', '0', '10', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '305', 'Song of Vengance', '1', '0', '11', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '269', 'Song of Hunter', '1', '0', '12', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('5', 'Songs', '363', 'Song of Meditation', '1', '0', '13', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '307', 'Dance of Aqua Guard', '1', '0', '1', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '309', 'Dance of Earth Guard', '1', '0', '2', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '311', 'Dance of Protection', '1', '0', '3', '1', '80', '0', '0', '0', '750000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '274', 'Dance of Fire', '1', '0', '4', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '275', 'Dance of Fury', '1', '0', '5', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '272', 'Dance of Inspiration', '1', '0', '6', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '277', 'Dance of Light', '1', '0', '7', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '310', 'Dance of Vampire', '1', '0', '8', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '271', 'Dance of Warrior', '1', '0', '9', '1', '80', '0', '1', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '276', 'Dance of Concentration', '1', '0', '10', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('6', 'Dances', '273', 'Dance of Mystic', '1', '0', '11', '1', '80', '0', '2', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('7', 'WindWalk', '1204', 'Wind Walk', '2', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('8', 'DWeight', '1257', 'Decrease Weight', '1', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('9', 'Shield', '1040', 'Shield', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('10', 'Might', '1068', 'Might', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('11', 'MShield', '1035', 'Mental Shield', '4', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('12', 'BTB', '1045', 'Bless the Body', '6', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('13', 'BTS', '1048', 'Bless the Soul', '6', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('14', 'MBarrier', '1036', 'Magic Barrier', '2', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('15', 'RShock', '1259', 'Resist Shock', '4', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('16', 'Concentration', '1078', 'Concentration', '6', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('17', 'BerserkerSpirit', '1062', 'Berserker Spirit', '2', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('18', 'BTShield', '1243', 'Bless Shield', '6', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('19', 'VRage', '1268', 'Vampiric Rage', '4', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('20', 'Acumen', '1085', 'Acumen', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('21', 'Empower', '1059', 'Empower', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('22', 'Haste', '1086', 'Haste', '2', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('23', 'Guidance', '1240', 'Guidance', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('24', 'Focus', '1077', 'Focus', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('25', 'DeathWhisper', '1242', 'Death Whisper', '3', '0', '1', '1', '80', '0', '0', '0', '200000', '0');
INSERT INTO `buff_templates` VALUES ('26', 'DWarrior', '271', 'Dance of Warrior', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('27', 'DInspiration', '272', 'Dance of Inspiration', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('28', 'DMystic', '273', 'Dance of Mystic', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('29', 'DFire', '274', 'Dance of Fire', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('30', 'DFury', '275', 'Dance of Fury', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('31', 'DConecntration', '276', 'Dance of Concentration', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('32', 'DLight', '277', 'Dance of Light', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('33', 'DAqua', '307', 'Dance of Aqua Guard', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('34', 'DEarth', '309', 'Dance of Earth Guard', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('35', 'DVampire', '310', 'Dance of Vampire', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('36', 'DProtection', '311', 'Dance of Protection', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('37', 'SEarth', '264', 'Song of Earth', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('38', 'SLife', '265', 'Song of Life', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('39', 'SWater', '266', 'Song of Water', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('40', 'SWarding', '267', 'Song of Warding', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('41', 'SWind', '268', 'Song of Wind', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('42', 'SHunter', '269', 'Song of Hunter', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('43', 'SInvocation', '270', 'Song of Invocation', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('44', 'SVengance', '305', 'Song of Vengance', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('45', 'SFlameGuard', '306', 'Song of Flame Guard', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('46', 'SStormGuard', '308', 'Song of Storm Guard', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('47', 'SRenewal', '349', 'Song of Renewal', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('48', 'SChampion', '364', 'Song of Champion', '1', '0', '1', '1', '80', '0', '0', '0', '1000000', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1204', 'Wind Walk', '2', '0', '1', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1257', 'Decrease Weight', '3', '0', '2', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1045', 'Bless The Body', '6', '0', '3', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1048', 'Bess The Soul', '6', '1', '4', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1085', 'Acumen', '3', '1', '5', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1059', 'Empower', '3', '1', '6', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1068', 'Might', '3', '1', '7', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1086', 'Haste', '2', '1', '8', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1268', 'Vampiric Rage', '4', '1', '9', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1040', 'Shield', '3', '1', '10', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1036', 'Magic Barrier', '2', '1', '11', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('49', 'Event1', '1035', 'Mental Shield', '4', '1', '12', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1204', 'Wind Walk', '1', '1', '1', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1257', 'Decrease Weight', '2', '1', '2', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1045', 'Bless The Body', '3', '1', '3', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1048', 'Bess The Soul', '3', '1', '4', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1085', 'Acumen', '2', '1', '5', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1059', 'Empower', '1', '1', '6', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1068', 'Might', '2', '1', '7', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1086', 'Haste', '1', '1', '8', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1268', 'Vampiric Rage', '2', '1', '9', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1040', 'Shield', '2', '1', '10', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1036', 'Magic Barrier', '1', '1', '11', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('50', 'Event2', '1035', 'Mental Shield', '2', '1', '12', '1', '80', '0', '0', '0', '0', '0');
INSERT INTO `buff_templates` VALUES ('51', 'SongOfVitality', '304', 'Song of Vitality', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('52', 'POF', '1356', 'Prophecy of Fire', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('53', 'GMight', '1388', 'Greater Might', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('54', 'GShield', '1389', 'Greater Shield', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('55', 'ChOV', '1363', 'Chant of Victory', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('56', 'ChOS', '1362', 'Chant of Spirit', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('57', 'ChOF', '1002', 'Chant of Flame', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('58', 'SirensDance', '365', 'Siren\'s Dance', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('59', 'WildMagic', '1303', 'Wild Magic', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('60', 'POW', '1355', 'Prophecy of Water', '1', '1', '1', '1', '80', '0', '0', '0', '0', '2');
INSERT INTO `buff_templates` VALUES ('61', 'GiftOfQueen', '4700', 'Gift Of Queen', '1', '1', '1', '1', '80', '0', '0', '0', '0', '3');
INSERT INTO `buff_templates` VALUES ('62', 'BlessingofQueen', '4699', 'Blessing of Queen', '1', '1', '1', '1', '80', '0', '0', '0', '0', '3');