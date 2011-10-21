-- this script add raid tokens to all raids > 70 and grand bosses

delete from custom_droplist where itemId=11115;
insert into custom_droplist (mobId,itemId,min,max,category,chance) SELECT n.id,'11115','2','4','3','1000000' FROM npc n where n.`title` like '%raid boss%' and n.`level` between 70 and 90;

insert into custom_droplist values (100200,'11115','5','6','3','1000000');
insert into custom_droplist values (25338,'11115','5','6','3','1000000');
insert into custom_droplist values (29019,'11115','5','6','3','1000000');
insert into custom_droplist values (29020,'11115','5','6','3','1000000');
insert into custom_droplist values (29028,'11115','5','6','3','1000000');
insert into custom_droplist values (29045,'11115','5','6','3','1000000');
insert into custom_droplist values (29046,'11115','5','6','3','1000000');
insert into custom_droplist values (29047,'11115','5','6','3','1000000');
insert into custom_droplist values (29054,'11115','5','6','3','1000000');

-- orfen zaken core queenant
insert into custom_droplist values (29014,'11115','5','6','3','1000000');
insert into custom_droplist values (29022,'11115','5','6','3','1000000');
insert into custom_droplist values (29006,'11115','5','6','3','1000000');
insert into custom_droplist values (29001,'11115','5','6','3','1000000');

-- tyrannosaurus
insert into custom_droplist values (22215,'11115','1','1','3','1000000');
insert into custom_droplist values (22216,'11115','1','1','3','1000000');
insert into custom_droplist values (22217,'11115','1','1','3','1000000');