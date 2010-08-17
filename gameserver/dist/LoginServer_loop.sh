#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -Xmx256m -XX:+UseParallelGC -XX:+AggressiveOpts -cp lib/*:l2scoria-core-3.jar com.l2scoria.loginserver.L2LoginServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
