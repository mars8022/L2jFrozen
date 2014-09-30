insert ignore into droplist select * from saved_droplist;
delete from saved_droplist;
delete from custom_droplist;
insert ignore into npc select * from saved_npc;
delete from saved_npc;
delete from custom_npc;