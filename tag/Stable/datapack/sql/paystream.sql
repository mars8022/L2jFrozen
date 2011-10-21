CREATE TABLE `paystream` (                              
             `msgid` varchar(32) NOT NULL,                         
             `paymentdate` datetime NOT NULL,                      
             `number_from` varchar(32) NOT NULL,                   
             `numebr_to` varchar(32) NOT NULL,                     
             `char_name` varchar(32) character set utf8 NOT NULL,  
             `summ` decimal(12,2) NOT NULL,                        
             `currency` varchar(6) NOT NULL,                       
             PRIMARY KEY  (`msgid`)                                
           ) ENGINE=MyISAM;
