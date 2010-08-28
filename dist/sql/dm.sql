-- --------------------------------
-- Table structure for table `dm`
-- Created by SqueezeD from l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `dm`;
CREATE TABLE `dm` (
  `eventName` varchar(255) NOT NULL default '',
  `eventDesc` varchar(255) NOT NULL default '',
  `joiningLocation` varchar(255) NOT NULL default '',
  `minlvl` int(4) NOT NULL default '0',
  `maxlvl` int(4) NOT NULL default '0',
  `npcId` int(8) NOT NULL default '0',
  `npcX` int(11) NOT NULL default '0',
  `npcY` int(11) NOT NULL default '0',
  `npcZ` int(11) NOT NULL default '0',
  `rewardId` int(11) NOT NULL default '0',
  `rewardAmount` int(11) NOT NULL default '0',
  `color` int(11) NOT NULL default '0',
  `playerX` int(11) NOT NULL default '0',
  `playerY` int(11) NOT NULL default '0',
  `playerZ` int(11) NOT NULL default '0'  
) DEFAULT CHARSET=utf8;