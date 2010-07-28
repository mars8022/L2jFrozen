SET FOREIGN_KEY_CHECKS=0;
--
-- Table structure for raid_prizes
--
CREATE TABLE IF NOT EXISTS `raid_prizes` (
  `prize_package_id` int(11) NOT NULL,
  `first_prize_id` int(11) NOT NULL,
  `first_prize_ammount` int(11) NOT NULL,
  `second_prize_id` int(11) NOT NULL,
  `second_prize_ammount` int(11) NOT NULL,
  `event_points_ammount` int(11) NOT NULL,
  PRIMARY KEY  (`prize_package_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `raid_prizes` VALUES ('1', '3470', '15', '6393', '20', '3');
INSERT INTO `raid_prizes` VALUES ('2', '3470', '25', '6393', '35', '4');
INSERT INTO `raid_prizes` VALUES ('3', '3470', '40', '6393', '40', '5');
INSERT INTO `raid_prizes` VALUES ('4', '3470', '50', '6393', '50', '10');
INSERT INTO `raid_prizes` VALUES ('5', '3470', '75', '6393', '75', '15');
INSERT INTO `raid_prizes` VALUES ('6', '3470', '150', '6393', '150', '20');
INSERT INTO `raid_prizes` VALUES ('7', '3470', '3', '6393', '10', '1');
INSERT INTO `raid_prizes` VALUES ('8', '3470', '6', '6393', '15', '2');
INSERT INTO `raid_prizes` VALUES ('9', '3470', '8', '6393', '18', '2');
INSERT INTO `raid_prizes` VALUES ('10', '3470', '20', '6393', '30', '3');
INSERT INTO `raid_prizes` VALUES ('11', '3470', '30', '6393', '40', '5');