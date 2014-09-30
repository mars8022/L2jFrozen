#!/bin/bash
date=`date '+%m-%d-%y'`
###############################################
## Configurate Database Connections please!!!!
###############################################
## Please, type here you dir to mysql directory bin. Example: /home/my_user/MySQL5/bin
mysqlPath="/home/user/mysql-5.1/bin"
## Please, type here you dir to save backup directory. Example: /home/my_user/backup
backupPath="/home/user/backup"
user="root"
pass="password"
db="l2jdb"

echo "Start backuping"
$mysqlPath/mysqldump -u $user -p $pass $db>$backupPath/$db.$date.sql
gzip $backupPath/$db.$date.sql
rm $backupPath/$db.$date.sql