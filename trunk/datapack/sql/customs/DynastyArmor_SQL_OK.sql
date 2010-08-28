delete from custom_armor where item_id in (9416,9421,9422,9423,9424,9425,9428,9429,9430,9431,9432,9437,9438,9439,9440);

INSERT ignore INTO `custom_armor` VALUES
('9416', 'Dynasty Plate', 'chest', 'false', 'heavy', '7620', 's', '0', '-1', '219', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9421', 'Dynasty Gaiters', 'legs', 'false', 'heavy', '3260',  's', '0', '-1', '137', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9422', 'Dynasty Helm', 'head', 'false', 'none', '550',  's', '0', '-1', '89', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9423', 'Dynasty Gloves', 'gloves', 'false', 'none', '540',  's', '0', '-1', '59', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9424', 'Dynasty Boots', 'feet', 'false', 'none', '1110',  's', '0', '-1', '59', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9425', 'Dynasty Leather', 'chest', 'false', 'light', '7620',  's', '0', '-1', '219', '0', '0', '0',   '0','true', 'true', 'true', 'true', '0', '0'),
('9428', 'Dynasty Leather Pants', 'legs', 'false', 'light', '3260',  's', '0', '-1', '137', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9429', 'Dynasty Leather Helm', 'head', 'false', 'none', '550',  's', '0', '-1', '89', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9430', 'Dynasty Leather Gloves', 'gloves', 'false', 'none', '540', 's', '0', '-1', '59', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9431', 'Dynasty Leather Shoes', 'feet', 'false', 'none', '1110', 's', '0', '-1', '59', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9432', 'Dynasty Robe', 'chest', 'false', 'magic', '7620', 's', '0', '-1', '219', '0', '257', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9437', 'Dynasty Pants', 'legs', 'false', 'magic', '3260', 's', '0', '-1', '137', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9438', 'Dynasty Cap', 'head', 'false', 'none', '550', 's', '0', '-1', '89', '0', '0', '0', 'true',  '0','true', 'true', 'true', '0', '0'),
('9439', 'Dynasty Mittens', 'gloves', 'false', 'none', '540', 's', '0', '-1', '59', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0'),
('9440', 'Dynasty Slippers', 'feet', 'false', 'none', '1110', 's', '0', '-1', '59', '0', '0', '0',  '0','true', 'true', 'true', 'true', '0', '0');

delete from custom_armorsets where id in (52,53,54);

INSERT ignore INTO `custom_armorsets` VALUES
('52', '9416', '9421', '9422', '9423', '9424', '9051', '0', '0', '3623'),
('53', '9425', '9428', '9429', '9430', '9431', '9050', '0', '0', '3624'),
('54', '9432', '9437', '9438', '9439', '9440', '9052', '0', '0', '3625');