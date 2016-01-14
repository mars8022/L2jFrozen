delete from npc where id = 29021;
INSERT INTO `npc` VALUES (29021, 29021,'Archangel',0,'',0,'Monster.archangel','15.00','80.00',74,'male','L2Monster',80,'186915','1342','158','3.00',40,43,30,21,20,20,6571,343,'2255','8326','606','2500',230,500,3819,93,0, 0,117,176, null, '0', '0', '12', 'FULL_PARTY');

-- Van Halter Drops
INSERT INTO `droplist` VALUES ('29062', '7579', '1', '1', '0', '233500');
INSERT INTO `droplist` VALUES ('29062', '960', '1', '1', '2', '320001');
INSERT INTO `droplist` VALUES ('29062', '959', '1', '1', '3', '32001');
INSERT INTO `droplist` VALUES ('29062', '6711', '12', '36', '4', '306300');
INSERT INTO `droplist` VALUES ('29062', '7575', '1', '1', '5', '10050');
INSERT INTO `droplist` VALUES ('29062', '8921', '1', '1', '6', '320001');
INSERT INTO `droplist` VALUES ('29062', '6684', '1', '1', '7', '10000');
INSERT INTO `droplist` VALUES ('29062', '6578', '1', '1', '9', '1000');

delete from npc where id = 29062;
INSERT INTO `npc` VALUES (29062,29062,'High Priestess van Halter',0,'Raid Boss',0,'Monster3.heretic_priest_110p','9.00','21.30',80,'male','L2RaidBoss',40,'115708','1866',  '364',  '3.21'   ,60   ,57   ,73  ,76   ,70    ,80   ,2915973,2380617,'5253',   '5343','2095',  '889'   ,230,  500   ,333, '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT');
