INSERT IGNORE INTO `grandboss_data` VALUES 
(29066, 185708, 114298, -8221,32768, 0, 11186000, 1998000, 0), -- Antharas Weak (85)
(29067, 185708, 114298, -8221,32768, 0, 14518000, 1998000, 0), -- Antharas Normal (85)
(29068, 185708, 114298, -8221,32768, 0, 17850000, 1998000, 0); -- Antharas Strong (85)

delete from npc where id = 29066;
delete from npc where id = 29067;
delete from npc where id = 29068;

INSERT INTO `npc` VALUES ('29066', '29066', 'Antharas', '0', '', '0', 'Monster.antaras', '300.00', '300.00', '79', 'male', 'L2GrandBoss', '40', '400000', '9999', '13.43', '3.09', '40', '43', '30', '21', '20', '10', '0', '0', '9000', '5000', '6000', '6000', '300', '0', '333', '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `npc` VALUES ('29067', '29067', 'Antharas', '0', '', '0', 'Monster.antaras', '300.00', '300.00', '99', 'male', 'L2GrandBoss', '40', '400000', '9999', '13.43', '3.09', '40', '43', '30', '21', '20', '10', '0', '0', '9000', '5000', '6000', '6000', '300', '0', '333', '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT');
INSERT INTO `npc` VALUES ('29068', '29068', 'Antharas', '0', '', '0', 'Monster.antaras', '300.00', '300.00', '99', 'male', 'L2GrandBoss', '40', '400000', '9999', '13.43', '3.09', '40', '43', '30', '21', '20', '10', '0', '0', '9000', '5000', '6000', '6000', '300', '0', '333', '0', '0', '0', '88', '132', null, '0', '0', '0', 'LAST_HIT');

INSERT IGNORE INTO `droplist` VALUES
(29066,80,1,2,0,250000), -- Tallum Blade
(29066,98,1,2,0,250000), -- Halberd
(29066,269,1,2,0,250000), -- Blood Tornado
(29066,2504,1,2,0,250000), -- Meteor Shower
(29066,150,1,2,1,250000), -- Elemental Sword
(29066,212,1,2,1,250000), -- Dasparion\'s Staff
(29066,235,1,2,1,250000), -- Bloody Orchid
(29066,288,1,2,1,250000), -- Carnage Bow
(29066,81,1,2,2,250000), -- Dragon Slayer
(29066,151,1,2,2,250000), -- Sword of Miracles 
(29066,164,1,2,2,250000), -- Elysian
(29066,2500,1,2,2,250000), -- Dark Legion\'s Edge
(29066,213,1,2,3,200000), -- Branch of The Mother Tree
(29066,236,1,2,3,200000), -- Soul Separator
(29066,270,1,2,3,200000), -- Dragon Grinder
(29066,289,1,2,3,200000), -- Soul Bow
(29066,305,1,2,3,200000), -- Tallum Glaive
(29066,6364,1,1,4,170000), -- Forgotten Blade
(29066,6365,1,1,4,170000), -- Basalt Battlehammer
(29066,6369,1,1,4,170000), -- Dragon Hunter Axe
(29066,6370,1,1,4,170000), -- Saint Spear
(29066,6372,1,1,4,160000), -- Heaven\'s Divider
(29066,6580,1,1,4,160000), -- Tallum Blade*Dark Legion\'s Edge
(29066,6366,1,1,5,200000), -- Imperial Staff
(29066,6367,1,1,5,200000), -- Angel Slayer
(29066,6371,1,1,5,200000), -- Demon Splinter
(29066,6579,1,1,5,200000), -- Arcana Mace
(29066,7575,1,1,5,200000), -- Draconic Bow
(29066,6326,1,5,6,300000), -- Sealed Majestic Necklace
(29066,6327,1,5,6,350000), -- Sealed Majestic Earring
(29066,6328,1,5,6,350000), -- Sealed Majestic Ring
(29066,6323,1,5,7,300000), -- Sealed Phoenix Necklace
(29066,6324,1,5,7,350000), -- Sealed Phoenix Earring
(29066,6325,1,5,7,350000), -- Sealed Phoenix Ring
(29066,6724,1,1,8,350000), -- Sealed Tateossian Earring 
(29066,6725,1,1,8,350000), -- Sealed Tateossian Ring
(29066,6726,1,1,8,300000), -- Sealed Tateossian Necklace
(29066,5287,1,3,9,500000), -- Sealed Dark Crystal breastplate
(29066,5297,1,3,9,250000), -- Sealed Dark Crystal Leather Armor
(29066,5304,1,3,9,250000), -- Sealed Tallum Tunic
(29066,5311,1,3,10,350000), -- Sealed Armor of Nightmare
(29066,5320,1,3,10,350000), -- Sealed Leather Armor of Nightmare
(29066,5326,1,3,10,300000), -- Sealed Robe of Nightmare
(29066,5293,1,3,11,250000), -- Sealed Tallum Plate Armor
(29066,5301,1,3,11,250000), -- Sealed Tallum Leather Armor
(29066,5308,1,3,11,250000), -- Sealed Dark Crystal Robe
(29066,5316,1,3,11,80000), -- Sealed Majestic Plate Armor
(29066,5323,1,3,11,80000), -- Sealed Majestic Leather Armor
(29066,5329,1,3,11,90000), -- Sealed Majestic Robe
(29066,5288,1,3,12,350000), -- Sealed Dark Crystal Gaiters
(29066,5298,1,3,12,350000), -- Sealed Dark Crystalline Leggings
(29066,5305,1,3,12,300000), -- Sealed Tallum Stockings
(29066,6674,1,1,13,150000), -- Sealed Imperial Crusader Breastplate
(29066,6675,1,1,13,150000), -- Sealed Imperial Crusader Gaiters
(29066,6680,1,1,13,350000), -- Sealed Draconic Leather Armor
(29066,6684,1,1,13,350000), -- Sealed Major Arcana Robe
(29066,5289,1,3,14,250000), -- Sealed Dark Crystal Helmet
(29066,5294,1,3,14,250000), -- Sealed Tallum Helmet
(29066,5312,1,3,14,250000), -- Sealed Helm of Nightmare
(29066,5317,1,3,14,250000), -- Sealed Majestic Circlet
(29066,5291,1,3,15,250000), -- Sealed Dark Crystal Boots
(29066,5296,1,3,15,250000), -- Sealed Tallum Boots
(29066,5314,1,3,15,250000), -- Sealed Boots of Nightmare
(29066,5319,1,3,15,250000), -- Sealed Majestic Boots
(29066,5290,1,3,16,250000), -- Sealed Dark Crystal Gloves
(29066,5295,1,3,16,250000), -- Sealed Tallum Gloves
(29066,5313,1,3,16,250000), -- Sealed Gloves of Nightmare
(29066,5318,1,3,16,250000), -- Sealed Majestic Gloves
(29066,5292,1,3,17,400000), -- Sealed Dark Crystal Shield
(29066,5315,1,3,17,400000), -- Sealed Shield of Nightmare
(29066,6678,1,1,17,200000), -- Sealed Imperial Crusader Shield
(29066,6676,1,1,18,340000), -- Sealed Imperial Crusader Gauntlet
(29066,6681,1,1,18,330000), -- Sealed Draconic Leather Glove
(29066,6685,1,1,18,330000), -- Sealed Major Arcana Glove
(29066,6677,1,1,19,340000), -- Sealed Imperial Crusader Boots
(29066,6682,1,1,19,330000), -- Sealed Draconic Leather Boots
(29066,6686,1,1,19,330000), -- Sealed Major Arcana Boots
(29066,6679,1,1,20,340000), -- Sealed Imperial Crusader Helmet
(29066,6683,1,1,20,330000), -- Sealed Draconic Leather Helmet
(29066,6687,1,1,20,330000), -- Sealed Major Arcana Circlet
(29066,729,1,19,21,1000000), -- Scroll: Enchant Weapon (Grade A)
(29066,1538,1,59,22,1000000), -- Blessed Scroll of Escape
(29066,3936,1,39,23,1000000), -- Blessed Scroll of Resurrection
(29066,57,14000000,18000000,24,1000000), -- Adena
(29066,57,14000000,18000000,25,1000000), -- Adena
(29066,57,14000000,18000000,26,1000000), -- Adena
(29066,57,14000000,18000000,27,1000000), -- Adena
(29066,57,14000000,18000000,28,1000000), -- Adena
(29066,57,14000000,18000000,29,1000000), -- Adena
(29066,57,9000000,13000000,30,1000000), -- Adena
(29066,6656,1,1,31,1000000); -- Earring of Antharas

INSERT IGNORE INTO `droplist` VALUES
(29067,80,1,2,0,250000), -- Tallum Blade
(29067,98,1,2,0,250000), -- Halberd
(29067,269,1,2,0,250000), -- Blood Tornado
(29067,2504,1,2,0,250000), -- Meteor Shower
(29067,150,1,2,1,250000), -- Elemental Sword
(29067,212,1,2,1,250000), -- Dasparion\'s Staff
(29067,235,1,2,1,250000), -- Bloody Orchid
(29067,288,1,2,1,250000), -- Carnage Bow
(29067,81,1,2,2,250000), -- Dragon Slayer
(29067,151,1,2,2,250000), -- Sword of Miracles 
(29067,164,1,2,2,250000), -- Elysian
(29067,2500,1,2,2,250000), -- Dark Legion\'s Edge
(29067,213,1,2,3,200000), -- Branch of The Mother Tree
(29067,236,1,2,3,200000), -- Soul Separator
(29067,270,1,2,3,200000), -- Dragon Grinder
(29067,289,1,2,3,200000), -- Soul Bow
(29067,305,1,2,3,200000), -- Tallum Glaive
(29067,6364,1,1,4,170000), -- Forgotten Blade
(29067,6365,1,1,4,170000), -- Basalt Battlehammer
(29067,6369,1,1,4,170000), -- Dragon Hunter Axe
(29067,6370,1,1,4,170000), -- Saint Spear
(29067,6372,1,1,4,160000), -- Heaven\'s Divider
(29067,6580,1,1,4,160000), -- Tallum Blade*Dark Legion\'s Edge
(29067,6366,1,1,5,200000), -- Imperial Staff
(29067,6367,1,1,5,200000), -- Angel Slayer
(29067,6371,1,1,5,200000), -- Demon Splinter
(29067,6579,1,1,5,200000), -- Arcana Mace
(29067,7575,1,1,5,200000), -- Draconic Bow
(29067,6326,1,5,6,300000), -- Sealed Majestic Necklace
(29067,6327,1,5,6,350000), -- Sealed Majestic Earring
(29067,6328,1,5,6,350000), -- Sealed Majestic Ring
(29067,6323,1,5,7,300000), -- Sealed Phoenix Necklace
(29067,6324,1,5,7,350000), -- Sealed Phoenix Earring
(29067,6325,1,5,7,350000), -- Sealed Phoenix Ring
(29067,6724,1,1,8,350000), -- Sealed Tateossian Earring 
(29067,6725,1,1,8,350000), -- Sealed Tateossian Ring
(29067,6726,1,1,8,300000), -- Sealed Tateossian Necklace
(29067,5287,1,3,9,500000), -- Sealed Dark Crystal breastplate
(29067,5297,1,3,9,250000), -- Sealed Dark Crystal Leather Armor
(29067,5304,1,3,9,250000), -- Sealed Tallum Tunic
(29067,5311,1,3,10,350000), -- Sealed Armor of Nightmare
(29067,5320,1,3,10,350000), -- Sealed Leather Armor of Nightmare
(29067,5326,1,3,10,300000), -- Sealed Robe of Nightmare
(29067,5293,1,3,11,250000), -- Sealed Tallum Plate Armor
(29067,5301,1,3,11,250000), -- Sealed Tallum Leather Armor
(29067,5308,1,3,11,250000), -- Sealed Dark Crystal Robe
(29067,5316,1,3,11,80000), -- Sealed Majestic Plate Armor
(29067,5323,1,3,11,80000), -- Sealed Majestic Leather Armor
(29067,5329,1,3,11,90000), -- Sealed Majestic Robe
(29067,5288,1,3,12,350000), -- Sealed Dark Crystal Gaiters
(29067,5298,1,3,12,350000), -- Sealed Dark Crystalline Leggings
(29067,5305,1,3,12,300000), -- Sealed Tallum Stockings
(29067,6674,1,1,13,150000), -- Sealed Imperial Crusader Breastplate
(29067,6675,1,1,13,150000), -- Sealed Imperial Crusader Gaiters
(29067,6680,1,1,13,350000), -- Sealed Draconic Leather Armor
(29067,6684,1,1,13,350000), -- Sealed Major Arcana Robe
(29067,5289,1,3,14,250000), -- Sealed Dark Crystal Helmet
(29067,5294,1,3,14,250000), -- Sealed Tallum Helmet
(29067,5312,1,3,14,250000), -- Sealed Helm of Nightmare
(29067,5317,1,3,14,250000), -- Sealed Majestic Circlet
(29067,5291,1,3,15,250000), -- Sealed Dark Crystal Boots
(29067,5296,1,3,15,250000), -- Sealed Tallum Boots
(29067,5314,1,3,15,250000), -- Sealed Boots of Nightmare
(29067,5319,1,3,15,250000), -- Sealed Majestic Boots
(29067,5290,1,3,16,250000), -- Sealed Dark Crystal Gloves
(29067,5295,1,3,16,250000), -- Sealed Tallum Gloves
(29067,5313,1,3,16,250000), -- Sealed Gloves of Nightmare
(29067,5318,1,3,16,250000), -- Sealed Majestic Gloves
(29067,5292,1,3,17,400000), -- Sealed Dark Crystal Shield
(29067,5315,1,3,17,400000), -- Sealed Shield of Nightmare
(29067,6678,1,1,17,200000), -- Sealed Imperial Crusader Shield
(29067,6676,1,1,18,340000), -- Sealed Imperial Crusader Gauntlet
(29067,6681,1,1,18,330000), -- Sealed Draconic Leather Glove
(29067,6685,1,1,18,330000), -- Sealed Major Arcana Glove
(29067,6677,1,1,19,340000), -- Sealed Imperial Crusader Boots
(29067,6682,1,1,19,330000), -- Sealed Draconic Leather Boots
(29067,6686,1,1,19,330000), -- Sealed Major Arcana Boots
(29067,6679,1,1,20,340000), -- Sealed Imperial Crusader Helmet
(29067,6683,1,1,20,330000), -- Sealed Draconic Leather Helmet
(29067,6687,1,1,20,330000), -- Sealed Major Arcana Circlet
(29067,729,1,19,21,1000000), -- Scroll: Enchant Weapon (Grade A)
(29067,1538,1,59,22,1000000), -- Blessed Scroll of Escape
(29067,3936,1,39,23,1000000), -- Blessed Scroll of Resurrection
(29067,57,14000000,18000000,24,1000000), -- Adena
(29067,57,14000000,18000000,25,1000000), -- Adena
(29067,57,14000000,18000000,26,1000000), -- Adena
(29067,57,14000000,18000000,27,1000000), -- Adena
(29067,57,14000000,18000000,28,1000000), -- Adena
(29067,57,14000000,18000000,29,1000000), -- Adena
(29067,57,9000000,13000000,30,1000000), -- Adena
(29067,6656,1,1,31,1000000); -- Earring of Antharas

INSERT IGNORE INTO `droplist` VALUES
(29068,80,1,2,0,250000), -- Tallum Blade
(29068,98,1,2,0,250000), -- Halberd
(29068,269,1,2,0,250000), -- Blood Tornado
(29068,2504,1,2,0,250000), -- Meteor Shower
(29068,150,1,2,1,250000), -- Elemental Sword
(29068,212,1,2,1,250000), -- Dasparion\'s Staff
(29068,235,1,2,1,250000), -- Bloody Orchid
(29068,288,1,2,1,250000), -- Carnage Bow
(29068,81,1,2,2,250000), -- Dragon Slayer
(29068,151,1,2,2,250000), -- Sword of Miracles 
(29068,164,1,2,2,250000), -- Elysian
(29068,2500,1,2,2,250000), -- Dark Legion\'s Edge
(29068,213,1,2,3,200000), -- Branch of The Mother Tree
(29068,236,1,2,3,200000), -- Soul Separator
(29068,270,1,2,3,200000), -- Dragon Grinder
(29068,289,1,2,3,200000), -- Soul Bow
(29068,305,1,2,3,200000), -- Tallum Glaive
(29068,6364,1,1,4,170000), -- Forgotten Blade
(29068,6365,1,1,4,170000), -- Basalt Battlehammer
(29068,6369,1,1,4,170000), -- Dragon Hunter Axe
(29068,6370,1,1,4,170000), -- Saint Spear
(29068,6372,1,1,4,160000), -- Heaven\'s Divider
(29068,6580,1,1,4,160000), -- Tallum Blade*Dark Legion\'s Edge
(29068,6366,1,1,5,200000), -- Imperial Staff
(29068,6367,1,1,5,200000), -- Angel Slayer
(29068,6371,1,1,5,200000), -- Demon Splinter
(29068,6579,1,1,5,200000), -- Arcana Mace
(29068,7575,1,1,5,200000), -- Draconic Bow
(29068,6326,1,5,6,300000), -- Sealed Majestic Necklace
(29068,6327,1,5,6,350000), -- Sealed Majestic Earring
(29068,6328,1,5,6,350000), -- Sealed Majestic Ring
(29068,6323,1,5,7,300000), -- Sealed Phoenix Necklace
(29068,6324,1,5,7,350000), -- Sealed Phoenix Earring
(29068,6325,1,5,7,350000), -- Sealed Phoenix Ring
(29068,6724,1,1,8,350000), -- Sealed Tateossian Earring 
(29068,6725,1,1,8,350000), -- Sealed Tateossian Ring
(29068,6726,1,1,8,300000), -- Sealed Tateossian Necklace
(29068,5287,1,3,9,500000), -- Sealed Dark Crystal breastplate
(29068,5297,1,3,9,250000), -- Sealed Dark Crystal Leather Armor
(29068,5304,1,3,9,250000), -- Sealed Tallum Tunic
(29068,5311,1,3,10,350000), -- Sealed Armor of Nightmare
(29068,5320,1,3,10,350000), -- Sealed Leather Armor of Nightmare
(29068,5326,1,3,10,300000), -- Sealed Robe of Nightmare
(29068,5293,1,3,11,250000), -- Sealed Tallum Plate Armor
(29068,5301,1,3,11,250000), -- Sealed Tallum Leather Armor
(29068,5308,1,3,11,250000), -- Sealed Dark Crystal Robe
(29068,5316,1,3,11,80000), -- Sealed Majestic Plate Armor
(29068,5323,1,3,11,80000), -- Sealed Majestic Leather Armor
(29068,5329,1,3,11,90000), -- Sealed Majestic Robe
(29068,5288,1,3,12,350000), -- Sealed Dark Crystal Gaiters
(29068,5298,1,3,12,350000), -- Sealed Dark Crystalline Leggings
(29068,5305,1,3,12,300000), -- Sealed Tallum Stockings
(29068,6674,1,1,13,150000), -- Sealed Imperial Crusader Breastplate
(29068,6675,1,1,13,150000), -- Sealed Imperial Crusader Gaiters
(29068,6680,1,1,13,350000), -- Sealed Draconic Leather Armor
(29068,6684,1,1,13,350000), -- Sealed Major Arcana Robe
(29068,5289,1,3,14,250000), -- Sealed Dark Crystal Helmet
(29068,5294,1,3,14,250000), -- Sealed Tallum Helmet
(29068,5312,1,3,14,250000), -- Sealed Helm of Nightmare
(29068,5317,1,3,14,250000), -- Sealed Majestic Circlet
(29068,5291,1,3,15,250000), -- Sealed Dark Crystal Boots
(29068,5296,1,3,15,250000), -- Sealed Tallum Boots
(29068,5314,1,3,15,250000), -- Sealed Boots of Nightmare
(29068,5319,1,3,15,250000), -- Sealed Majestic Boots
(29068,5290,1,3,16,250000), -- Sealed Dark Crystal Gloves
(29068,5295,1,3,16,250000), -- Sealed Tallum Gloves
(29068,5313,1,3,16,250000), -- Sealed Gloves of Nightmare
(29068,5318,1,3,16,250000), -- Sealed Majestic Gloves
(29068,5292,1,3,17,400000), -- Sealed Dark Crystal Shield
(29068,5315,1,3,17,400000), -- Sealed Shield of Nightmare
(29068,6678,1,1,17,200000), -- Sealed Imperial Crusader Shield
(29068,6676,1,1,18,340000), -- Sealed Imperial Crusader Gauntlet
(29068,6681,1,1,18,330000), -- Sealed Draconic Leather Glove
(29068,6685,1,1,18,330000), -- Sealed Major Arcana Glove
(29068,6677,1,1,19,340000), -- Sealed Imperial Crusader Boots
(29068,6682,1,1,19,330000), -- Sealed Draconic Leather Boots
(29068,6686,1,1,19,330000), -- Sealed Major Arcana Boots
(29068,6679,1,1,20,340000), -- Sealed Imperial Crusader Helmet
(29068,6683,1,1,20,330000), -- Sealed Draconic Leather Helmet
(29068,6687,1,1,20,330000), -- Sealed Major Arcana Circlet
(29068,729,1,19,21,1000000), -- Scroll: Enchant Weapon (Grade A)
(29068,1538,1,59,22,1000000), -- Blessed Scroll of Escape
(29068,3936,1,39,23,1000000), -- Blessed Scroll of Resurrection
(29068,57,14000000,18000000,24,1000000), -- Adena
(29068,57,14000000,18000000,25,1000000), -- Adena
(29068,57,14000000,18000000,26,1000000), -- Adena
(29068,57,14000000,18000000,27,1000000), -- Adena
(29068,57,14000000,18000000,28,1000000), -- Adena
(29068,57,14000000,18000000,29,1000000), -- Adena
(29068,57,9000000,13000000,30,1000000), -- Adena
(29068,6656,1,1,31,1000000); -- Earring of Antharas