-- Not implemented yet, this is imperial script copy

/*
CREATE TABLE IF NOT EXISTS `saved_droplist` (
  `mobId` int(11) NOT NULL DEFAULT '0',
  `itemId` int(11) NOT NULL DEFAULT '0',
  `min` int(11) NOT NULL DEFAULT '0',
  `max` int(11) NOT NULL DEFAULT '0',
  `category` int(11) NOT NULL DEFAULT '0',
  `chance` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`mobId`,`itemId`,`category`),
  KEY `key_mobId` (`mobId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- Add 5* Adena to All Imperial Tomb Mobs
delete from custom_droplist where itemId = 57 and mobId in (SELECT distinct d.mobId mo FROM droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%");
insert ignore into saved_droplist (mobId,itemId,min,max,category,chance) SELECT distinct d.mobId mo, 57, d.min mi,d.max ma,d.category ca,d.chance ch FROM droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%";
insert ignore into custom_droplist (mobId,itemId,min,max,category,chance) SELECT distinct d.mobId mo, 57, d.min*5 mi,d.max*5 ma,0 ca,1000000 FROM droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%";

-- Add Gold tokens to All Imperial Mobs (lower chance then monastery but more items)
delete from custom_droplist where itemId = 11112 and mobId in (SELECT distinct d.mobId mo FROM droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%");
insert ignore into custom_droplist (mobId,itemId,min,max,category,chance) SELECT distinct d.mobId mo, 11112, 1 mi,5 ma,-1 ca,150000 FROM droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%";

-- add lifestones
delete from custom_droplist where itemId = 8752 and mobId = 21424;
delete from custom_droplist where itemId = 8762 and mobId = 21427;
insert into custom_droplist values (21424, 8752, 1, 3, -1, 500000);
insert into custom_droplist values (21427, 8762, 1, 3, -1, 500000);

-- Delete previous Adena Drops
delete from droplist where itemId = 57 and mobId in (SELECT distinct d.mobId mo FROM custom_droplist d,npc n,spawnlist s where d.itemId = 57 and n.id = d.mobid and n.id = s.npc_templateid and s.location like "%godard14_2515%");


CREATE TABLE IF NOT EXISTS `saved_npc` (
  `id` decimal(11,0) NOT NULL DEFAULT '0',
  `idTemplate` int(11) NOT NULL DEFAULT '0',
  `name` varchar(200) DEFAULT NULL,
  `serverSideName` int(1) DEFAULT '0',
  `title` varchar(45) DEFAULT '',
  `serverSideTitle` int(1) DEFAULT '0',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(5,2) DEFAULT NULL,
  `collision_height` decimal(5,2) DEFAULT NULL,
  `level` decimal(2,0) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `attackrange` int(11) DEFAULT NULL,
  `hp` decimal(8,0) DEFAULT NULL,
  `mp` decimal(5,0) DEFAULT NULL,
  `hpreg` decimal(8,2) DEFAULT NULL,
  `mpreg` decimal(5,2) DEFAULT NULL,
  `str` decimal(7,0) DEFAULT NULL,
  `con` decimal(7,0) DEFAULT NULL,
  `dex` decimal(7,0) DEFAULT NULL,
  `int` decimal(7,0) DEFAULT NULL,
  `wit` decimal(7,0) DEFAULT NULL,
  `men` decimal(7,0) DEFAULT NULL,
  `exp` decimal(9,0) DEFAULT NULL,
  `sp` decimal(8,0) DEFAULT NULL,
  `patk` decimal(5,0) DEFAULT NULL,
  `pdef` decimal(5,0) DEFAULT NULL,
  `matk` decimal(5,0) DEFAULT NULL,
  `mdef` decimal(5,0) DEFAULT NULL,
  `atkspd` decimal(3,0) DEFAULT NULL,
  `aggro` decimal(6,0) DEFAULT NULL,
  `matkspd` decimal(4,0) DEFAULT NULL,
  `rhand` decimal(4,0) DEFAULT NULL,
  `lhand` decimal(4,0) DEFAULT NULL,
  `armor` decimal(1,0) DEFAULT NULL,
  `walkspd` decimal(3,0) DEFAULT NULL,
  `runspd` decimal(3,0) DEFAULT NULL,
  `faction_id` varchar(40) DEFAULT NULL,
  `faction_range` decimal(4,0) DEFAULT NULL,
  `isUndead` int(11) DEFAULT '0',
  `absorb_level` decimal(2,0) DEFAULT '0',
  `absorb_type` enum('FULL_PARTY','LAST_HIT','PARTY_ONE_RANDOM') NOT NULL DEFAULT 'LAST_HIT',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

delete from custom_npc where custom_npc.id in (select distinct npc.id from npc,spawnlist where spawnlist.location like "%godard14_2515%" and spawnlist.npc_templateid=npc.id);
insert into custom_npc select distinct npc.id, npc.idTemplate, npc.name, npc.serverSideName, 'L2Frozen' , 1, npc.class, npc.collision_radius, npc.collision_height, 90, npc.sex, npc.type, npc.attackrange, npc.hp*2, npc.mp*2, npc.hpreg, npc.mpreg, npc.str, npc.con, npc.dex, npc.int, npc.wit, npc.men, npc.exp*2, npc.sp*2, npc.patk*2.0, npc.pdef*2.0, npc.matk*2.0, npc.mdef*2.0, npc.atkspd*2.0, npc.aggro, npc.matkspd*2.0, npc.rhand, npc.lhand, npc.armor, npc.walkspd*1.5, npc.runspd*1.5, npc.faction_id, npc.faction_range, npc.isUndead, npc.absorb_level, npc.absorb_type from npc,spawnlist where spawnlist.location like "%godard14_2515%" and spawnlist.npc_templateid=npc.id;
insert into saved_npc select distinct npc.* from npc,spawnlist where spawnlist.location like "%godard14_2515%" and spawnlist.npc_templateid=npc.id;
delete from npc where npc.id in (select distinct saved_npc.id from saved_npc,spawnlist where spawnlist.location like "%godard14_2515%" and spawnlist.npc_templateid=saved_npc.id);

*/