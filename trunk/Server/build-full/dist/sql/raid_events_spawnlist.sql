SET FOREIGN_KEY_CHECKS=0;
--
-- Table structure for raid_events_spawnlist
--
CREATE TABLE IF NOT EXISTS `raid_event_spawnlist` (
  `id` int(11) NOT NULL auto_increment,
  `location` varchar(40) NOT NULL default '',
  `raid_locX` int(9) NOT NULL,
  `raid_locY` int(9) NOT NULL,
  `raid_locZ` int(9) NOT NULL,
  `player_locX` int(9) NOT NULL,
  `player_locY` int(9) NOT NULL,
  `player_locZ` int(9) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `raid_event_spawnlist` VALUES ('1', 'Test', '-93287', '-251026', '-3336', '-94347', '-251026', '-3136');
INSERT INTO `raid_event_spawnlist` VALUES ('2', 'Test', '-87131', '-257755', '-3336', '-88020', '-257755', '-3136');
INSERT INTO `raid_event_spawnlist` VALUES ('3', 'Test', '174167', '-75329', '-5107', '174085', '-76703', '-5007');
INSERT INTO `raid_event_spawnlist` VALUES ('4', 'Test', '174252', '-88483', '-5139', '174242', '-86548', '-5007');
INSERT INTO `raid_event_spawnlist` VALUES ('5', 'Test', '174091', '-82305', '-5123', '174103', '-80650', '-5007');