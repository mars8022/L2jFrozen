--
-- Table structure for grandboss_intervallist
--
CREATE TABLE IF NOT EXISTS `grandboss_intervallist` (
  `bossId` int(11) NOT NULL,
  `respawnDate` decimal(20,0) NOT NULL,
  `state` int(11) NOT NULL,
  PRIMARY KEY  (`bossId`)
) ENGINE=MyISAM DEFAULT CHARSET=sjis;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT IGNORE INTO `grandboss_intervallist` (`bossId`, `respawnDate`, `state`)
VALUES
(29019,0,0),
(29020,0,0),
(29028,0,0),
(29062,0,0),
(29065,0,0);
