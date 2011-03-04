ALTER TABLE characters DROP banchat_time;

Alter table characters change in_jail punish_level TINYINT UNSIGNED NOT NULL DEFAULT 0;
Alter table characters change jail_timer punish_timer INT UNSIGNED NOT NULL DEFAULT 0;
