-- 
-- Dumping data for table `etcitem`
-- 
delete from custom_etcitem where item_id in (20001,20002,20003,20004,20005,20006);
INSERT ignore INTO `custom_etcitem` VALUES
(20001, 'Earth Sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20002, 'Burning Heart', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20003, 'Wind sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20004, 'Seal the Spirit', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20005, 'Water sphere', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none'),
(20006, 'Chalice to Harmonies', 'false', 'quest', 0, 'stackable', 'none', -1, 0, 0, 'false', 'false', 'false', 'false', 'C4Item', 'none');

delete from custom_etcitem where item_id=11117;
INSERT ignore INTO `custom_etcitem` (`item_id`,`name`,`crystallizable`,`item_type`,`weight`,`consume_type`,`crystal_type`,`duration`,`price`,`crystal_count`,`sellable`,`dropable`,`destroyable`,`tradeable`,`oldname`,`oldtype`) VALUES
 ('11117', 'Frozen Adena', 'false', 'none', 0, 'asset',  'none', -1, 0, 0, 'true', 'true', 'true', 'true', 'C4Item', 'none');

delete from custom_etcitem where item_id in (11112,11113,11114,11115);
INSERT ignore INTO `custom_etcitem` (`item_id`,`name`,`crystallizable`,`item_type`,`weight`,`consume_type`,`crystal_type`,`duration`,`price`,`crystal_count`,`sellable`,`dropable`,`destroyable`,`tradeable`,`oldname`,`oldtype`) VALUES
 ('11112', 'Gold Token', 'false', 'none', 1, 'stackable',  'none', -1, 2000000000, 0, 'true', 'true', 'true', 'true', 'InterludeItem', 'none'),
 ('11113', 'Epic Token', 'false', 'none', 1, 'stackable',  'none', -1, 2000000000, 0, 'true', 'true', 'true', 'true', 'InterludeItem', 'none'),
 ('11114', 'Frozen Token', 'false', 'none', 1, 'stackable',  'none', -1, 2000000000, 0, 'true', 'true', 'true', 'true', 'InterludeItem', 'none'),
 ('11115', 'Raid Token', 'false', 'none', 1, 'stackable',  'none', -1, 20000000, 0, 'true', 'true', 'true', 'true', 'InterludeItem', 'none');