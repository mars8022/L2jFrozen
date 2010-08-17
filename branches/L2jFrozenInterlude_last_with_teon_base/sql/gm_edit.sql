--
-- Table structure for gm_edit
--
CREATE TABLE IF NOT EXISTS `gm_edit` (
  `id` int(11) NOT NULL auto_increment,
  `GM_Name` varchar(40) default NULL,
  `GM_ID` int(11) default NULL,
  `Edited_Char` varchar(40) default NULL,
  `Action` varchar(120) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
