--
-- Table structure for `character_blocklist`
--
CREATE TABLE IF NOT EXISTS `character_blocklist` (
  `blocker` varchar(35) NOT NULL,
  `blocked` varchar(35) NOT NULL
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;