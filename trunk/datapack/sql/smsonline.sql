create table smsonline (
 smstimestamp decimal(12,2) not null,	
 user_phone varchar(32) not null,
 service varchar(16) not null,
 char_name varchar(64) not null,
 primary key (smstimestamp,user_phone)
 );