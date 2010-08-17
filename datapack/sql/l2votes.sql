CREATE TABLE `l2votes` (                                          
           `votedate` datetime NOT NULL,                                   
           `charName` varchar(32) character set utf8 NOT NULL default '',  
           KEY `votedate_idx` (`votedate`)                                 
         ) ENGINE=MyISAM;
