SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `max_poly`
-- ----------------------------
DROP TABLE IF EXISTS `max_poly`;
CREATE TABLE `max_poly` (
  `name` varchar(22) default NULL,
  `title` varchar(22) default NULL,
  `sex` decimal(22,0) NOT NULL default '0',
  `hair` decimal(22,0) NOT NULL default '0',
  `hairColor` decimal(22,0) NOT NULL default '0',
  `face` decimal(22,0) NOT NULL default '0',
  `classId` decimal(22,0) NOT NULL default '0',
  `npcId` decimal(22,0) NOT NULL default '0',
  `weaponIdRH` decimal(22,0) NOT NULL default '0',
  `weaponIdLH` decimal(22,0) NOT NULL default '0',
  `weaponIdEnc` decimal(22,0) NOT NULL default '0',
  `armorId` decimal(22,0) NOT NULL default '0',
  `head` decimal(22,0) NOT NULL default '0',
  `hats` decimal(22,0) NOT NULL default '0',
  `faces` decimal(22,0) NOT NULL default '0',
  `chest` decimal(22,0) NOT NULL default '0',
  `legs` decimal(22,0) NOT NULL default '0',
  `gloves` decimal(22,0) NOT NULL default '0',
  `feet` decimal(22,0) NOT NULL default '0',
  `abnormalEffect` decimal(22,0) NOT NULL default '0',
  `pvpFlag` decimal(22,0) NOT NULL default '0',
  `karma` decimal(22,0) NOT NULL default '0',
  `recom` decimal(22,0) NOT NULL default '0',
  `clan` decimal(22,0) NOT NULL default '0',
  `isHero` decimal(22,0) NOT NULL default '0',
  `pledge` decimal(22,0) NOT NULL default '0',
  `nameColor` decimal(22,0) NOT NULL default '0',
  `titleColor` decimal(22,0) NOT NULL default '0',
  PRIMARY KEY  (`classId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of max_poly
-- ----------------------------
INSERT INTO `max_poly` VALUES ('Alex', 'Captain', '1', '0', '0', '0', '31', '30291', '0', '0', '0', '49', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0');