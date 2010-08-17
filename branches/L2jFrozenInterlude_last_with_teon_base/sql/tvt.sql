SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for tvt
-- ----------------------------
CREATE TABLE `tvt` (
`eventName` varchar(255) NOT NULL default '',
`eventDesc` varchar(255) NOT NULL default '',
`joiningLocation` varchar(255) NOT NULL default '',
`minlvl` int(4) NOT NULL default '0',
`maxlvl` int(4) NOT NULL default '0',
`npcId` int(8) NOT NULL default '0',
`npcX` int(11) NOT NULL default '0',
`npcY` int(11) NOT NULL default '0',
`npcZ` int(11) NOT NULL default '0',
`npcHeading` int(11) NOT NULL default '0',
`rewardId` int(11) NOT NULL default '0',
`rewardAmount` int(11) NOT NULL default '0',
`teamsCount` int(4) NOT NULL default '0',
`joinTime` int(11) NOT NULL default '0',
`eventTime` int(11) NOT NULL default '0',
`minPlayers` int(4) NOT NULL default '0',
`maxPlayers` int(4) NOT NULL default '0',
`delayForNextEvent` bigint(20) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records
-- ----------------------------
INSERT INTO `tvt` VALUES ('TVT', 'TeamvsTeam', 'Giran', '70', '80', '70010', '83476', '148615', '-3431', '0', '5574', '2500', '2', '15', '10', '2', '50', '3600000');