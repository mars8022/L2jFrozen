--
-- Table structure for lastimperialtomb_spawnlist
--
CREATE TABLE IF NOT EXISTS `lastimperialtomb_spawnlist` (
  `id` int(11) NOT NULL auto_increment,
  `count` int(9) NOT NULL default '0',
  `npc_templateid` int(9) NOT NULL default '0',
  `locx` int(9) NOT NULL default '0',
  `locy` int(9) NOT NULL default '0',
  `locz` int(9) NOT NULL default '0',
  `randomx` int(9) NOT NULL default '0',
  `randomy` int(9) NOT NULL default '0',
  `heading` int(9) NOT NULL default '0',
  `respawn_delay` int(9) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `key_npc_templateid` (`npc_templateid`)
) ENGINE=MyISAM;

-- ----------------------------
-- Records
-- ----------------------------
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '173777', '-76650', '-5107', '0', '0', '12588', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '173584', '-76386', '-5107', '0', '0', '3041', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '173481', '-76043', '-5107', '0', '0', '61312', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '173539', '-75678', '-5107', '0', '0', '59524', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '173773', '-75420', '-5107', '0', '0', '51115', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '174623', '-75571', '-5107', '0', '0', '40141', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '174769', '-75895', '-5107', '0', '0', '29572', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '174744', '-76240', '-5107', '0', '0', '29202', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18329', '174585', '-76510', '-5107', '0', '0', '21704', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18330', '174365', '-76745', '-5107', '0', '0', '22424', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18330', '174613', '-76179', '-5107', '0', '0', '31471', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18330', '174570', '-75584', '-5107', '0', '0', '31968', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18330', '173498', '-75724', '-5107', '0', '0', '58498', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18330', '173489', '-76227', '-5134', '0', '0', '63565', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '173696', '-76675', '-5107', '0', '0', '6757', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '173515', '-76184', '-5107', '0', '0', '6971', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '173516', '-75790', '-5134', '0', '0', '3142', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '173766', '-75502', '-5134', '0', '0', '60827', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '174473', '-75321', '-5107', '0', '0', '37147', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '174568', '-75654', '-5134', '0', '0', '41661', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '174584', '-76263', '-5107', '0', '0', '31729', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18331', '174493', '-76505', '-5107', '0', '0', '34503', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '173823', '-76688', '-5107', '0', '0', '2411', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '173630', '-76340', '-5107', '0', '0', '62454', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '173620', '-75981', '-5107', '0', '0', '4588', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '173755', '-75613', '-5107', '0', '0', '57892', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '174000', '-75411', '-5107', '0', '0', '54718', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '174487', '-75555', '-5107', '0', '0', '33861', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '174600', '-75841', '-5134', '0', '0', '35927', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '174576', '-76122', '-5107', '0', '0', '31176', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18332', '174517', '-76471', '-5107', '0', '0', '21893', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '174460', '-76355', '-5107', '0', '0', '27311', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '174483', '-76041', '-5107', '0', '0', '30947', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '174422', '-75689', '-5107', '0', '0', '42878', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '173898', '-75668', '-5107', '0', '0', '51856', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '173861', '-76011', '-5107', '0', '0', '383', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18333', '173872', '-76461', '-5107', '0', '0', '8041', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18339', '174128', '-81805', '-5150', '0', '0', '21495', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18339', '173958', '-81820', '-5123', '0', '0', '7459', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18339', '173892', '-81592', '-5123', '0', '0', '50849', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18339', '174245', '-81566', '-5123', '0', '0', '41760', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '173264', '-81529', '-5072', '0', '0', '1646', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '173265', '-81656', '-5072', '0', '0', '441', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '173267', '-81889', '-5072', '0', '0', '0', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '173271', '-82015', '-5072', '0', '0', '65382', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '174869', '-81485', '-5073', '0', '0', '32315', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '174867', '-81655', '-5073', '0', '0', '32537', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '174868', '-81890', '-5073', '0', '0', '32768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18334', '174871', '-82017', '-5073', '0', '0', '33007', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173493', '-80683', '-5107', '0', '0', '0', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173524', '-80627', '-5134', '0', '0', '65027', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173527', '-80569', '-5134', '0', '0', '65062', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173497', '-80510', '-5134', '0', '0', '417', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173526', '-80452', '-5107', '0', '0', '64735', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173465', '-80453', '-5107', '0', '0', '174', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173435', '-80512', '-5107', '0', '0', '65215', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173469', '-80570', '-5107', '0', '0', '65353', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173469', '-80628', '-5107', '0', '0', '166', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173492', '-83121', '-5107', '0', '0', '394', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173521', '-83063', '-5107', '0', '0', '316', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173524', '-83007', '-5107', '0', '0', '0', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173499', '-82947', '-5107', '0', '0', '0', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173523', '-82889', '-5107', '0', '0', '128', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173468', '-82889', '-5107', '0', '0', '316', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173440', '-82948', '-5107', '0', '0', '417', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173465', '-83006', '-5107', '0', '0', '2604', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173463', '-83064', '-5107', '0', '0', '286', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '173443', '-83120', '-5107', '0', '0', '1094', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '173181', '-82544', '-5107', '0', '0', '65135', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173147', '-82602', '-5107', '0', '0', '51316', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '173128', '-82702', '-5107', '0', '0', '5345', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173014', '-82628', '-5107', '0', '0', '11874', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173095', '-82520', '-5107', '0', '0', '49152', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '173191', '-80981', '-5107', '0', '0', '6947', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173144', '-80894', '-5107', '0', '0', '5345', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '173074', '-80817', '-5107', '0', '0', '8353', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173033', '-80920', '-5107', '0', '0', '10425', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '173115', '-80986', '-5107', '0', '0', '9611', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172916', '-81033', '-5107', '0', '0', '7099', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172992', '-81091', '-5107', '0', '0', '9438', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '173064', '-81125', '-5107', '0', '0', '5827', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172971', '-81198', '-5107', '0', '0', '14768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172867', '-81123', '-5107', '0', '0', '64055', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172883', '-82495', '-5107', '0', '0', '64764', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172946', '-82435', '-5107', '0', '0', '58038', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '173032', '-82365', '-5107', '0', '0', '59041', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172940', '-82325', '-5107', '0', '0', '58998', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '172837', '-82382', '-5107', '0', '0', '58363', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174635', '-82893', '-5107', '0', '0', '33594', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174604', '-82949', '-5107', '0', '0', '33184', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174639', '-83008', '-5107', '0', '0', '33057', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174632', '-83066', '-5107', '0', '0', '32768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174602', '-83122', '-5107', '0', '0', '33104', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174661', '-83121', '-5107', '0', '0', '32768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174691', '-83066', '-5107', '0', '0', '32961', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174687', '-83008', '-5107', '0', '0', '32520', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174663', '-82948', '-5107', '0', '0', '32768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174693', '-82889', '-5107', '0', '0', '32622', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174636', '-80456', '-5107', '0', '0', '32065', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174692', '-80455', '-5107', '0', '0', '33202', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174609', '-80514', '-5107', '0', '0', '33234', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174660', '-80512', '-5107', '0', '0', '33057', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174632', '-80570', '-5107', '0', '0', '32896', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174692', '-80571', '-5107', '0', '0', '32768', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174629', '-80627', '-5107', '0', '0', '33346', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174693', '-80630', '-5107', '0', '0', '32994', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174609', '-80684', '-5107', '0', '0', '32851', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18336', '174664', '-80685', '-5107', '0', '0', '32676', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '174859', '-80889', '-5134', '0', '0', '24103', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '174912', '-80825', '-5107', '0', '0', '24270', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '174947', '-80733', '-5107', '0', '0', '22449', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '175041', '-80834', '-5107', '0', '0', '25420', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '174935', '-80899', '-5107', '0', '0', '18061', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175014', '-81173', '-5107', '0', '0', '26398', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175096', '-81080', '-5107', '0', '0', '24719', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175172', '-80972', '-5107', '0', '0', '32315', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175249', '-81075', '-5107', '0', '0', '28435', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175197', '-81157', '-5107', '0', '0', '27617', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '175096', '-82724', '-5107', '0', '0', '42205', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '175016', '-82697', '-5107', '0', '0', '39533', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18335', '174924', '-82666', '-5107', '0', '0', '38710', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '175071', '-82549', '-5107', '0', '0', '39163', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18338', '175154', '-82619', '-5107', '0', '0', '36345', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175061', '-82374', '-5107', '0', '0', '43290', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175169', '-82453', '-5107', '0', '0', '37672', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175245', '-82547', '-5107', '0', '0', '40275', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175292', '-82432', '-5107', '0', '0', '42225', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18337', '175174', '-82328', '-5107', '0', '0', '41760', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18328', '174095', '-77279', '-5107', '0', '0', '16216', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18328', '175344', '-76042', '-5107', '0', '0', '32847', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18328', '174111', '-74833', '-5107', '0', '0', '49043', '180');
INSERT INTO `lastimperialtomb_spawnlist` VALUES (null, '1', '18328', '172894', '-76019', '-5107', '0', '0', '243', '180');
