#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/backup/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/backup/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -Xms64m -Xmx128m -cp ./libs/*:l2open-login.jar interlude.loginserver.L2LoginServer > log/stdout.log 2>&1
 	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
