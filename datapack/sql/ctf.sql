-- --------------------------------
-- Table structure for table `ctf`
-- Created by SqueezeD & Serpent for l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `ctf`;
CREATE TABLE `ctf` (
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
  `maxPlayers` int(4) NOT NULL default '0'
) DEFAULT CHARSET=utf8;