-- Note for L2JFree users : Luxury Gatekeeper is already in table `custom_npc`

DELETE FROM `custom_npc` WHERE `id`='7077';

INSERT INTO `custom_npc` VALUES
(7077, 31862, 'Global GK', 1, 'L2Frozen', 1, 'NPC.broadcasting_tower', 7.00, 35.00, 70, 'etc', 'L2Teleporter', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 490, 10, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 55, 132, '', 0, 1, 0, 'LAST_HIT', 0, 0, 0, 'fighter');