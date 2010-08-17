--
-- Table structure for fort
--
CREATE TABLE IF NOT EXISTS `fort` (
  `id` int(11) NOT NULL default '0',
  `name` varchar(55) NOT NULL,
  `taxPercent` int(11) NOT NULL default '15',
  `treasury` int(11) NOT NULL default '0',
  `siegeDate` decimal(20,0) NOT NULL default '0',
  `siegeDayOfWeek` int(11) NOT NULL default '7',
  `siegeHourOfDay` int(11) NOT NULL default '20',
  `owner` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `id` (`id`)
) ;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `fort` VALUES ('101', 'Shanty', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('102', 'Southern', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('103', 'Hive', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('104', 'Valley', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('105', 'Ivory', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('106', 'Narsell', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('107', 'Basin', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('108', 'WhiteSands', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('109', 'Borderland', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('110', 'Marshland', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('111', 'Archaic', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('112', 'Floran', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('113', 'Cloud Mountain', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('114', 'Tanor', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('115', 'Dragonspine', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('116', 'LandDragon', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('117', 'WesternGuard', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('118', 'Hunters', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('119', 'Aaru', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('120', 'Demon', '0', '0', '0', '0', '0', '0');
INSERT INTO `fort` VALUES ('121', 'Monastic', '0', '0', '0', '0', '0', '0');
