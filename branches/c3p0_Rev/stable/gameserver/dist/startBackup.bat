@echo off
title Frozen Backup Database

REM ###############################################
REM ## Configurate Database Connections please!!!!
REM ###############################################
REM Please, type here you dir to mysql directory \bin. Example: C:\Program Files\MySQL\Mysql 5.0\bin
set mysqlPath=C:\Program Files\MySql\MySQL 5.1\bin
set backupPath=C:\server\backup
set user=root
set pass=db_password
set db=l2jdb

echo Start backuping
%mysqlPath%\mysqldump.exe -u %user% -p %pass% %db%>%backupPath%/%db%.%date%.sql
exit