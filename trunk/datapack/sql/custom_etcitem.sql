-- 
-- Table structure for table `etcitem`
-- 

DROP TABLE IF EXISTS `custom_etcitem`;
CREATE TABLE `custom_etcitem` (
  `item_id` decimal(11,0) NOT NULL default '0',
  `name` varchar(100) default NULL,
  `crystallizable` varchar(5) default NULL,
  `item_type` varchar(12) default NULL,
  `weight` decimal(4,0) default NULL,
  `consume_type` varchar(9) default NULL,
  `crystal_type` ENUM("none","d","c","b","a","s") NOT NULL default 'none',
  `duration` decimal(3,0) default NULL,
  `price` decimal(11,0) default NULL,
  `crystal_count` int(4) default NULL,
  `sellable` varchar(5) default NULL,
  `dropable` varchar(5) default NULL,
  `destroyable` varchar(5) default NULL,
  `tradeable` varchar(5) default NULL,
  `oldname` varchar(100) NOT NULL default '',
  `oldtype` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`item_id`)
) TYPE=MyISAM;

-- 
-- Dumping data for table `etcitem`
-- 
INSERT INTO `custom_etcitem` VALUES
(20001, 'Earth Sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20002, 'Burning Heart', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20003, 'Wind sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20004, 'Seal the Spirit', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20005, 'Water sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20006, 'Chalice to Harmonies', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none');