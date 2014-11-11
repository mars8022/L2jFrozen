@echo off
title L2J-Frozen: Game Server Restart
echo L2J-Frozen: Game Server Restart
echo ATTENTION: It needs XMLRPC Server Enabled in Powerpak in order to work

REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF8 -cp ./lib/*;l2jfrozen-core.jar com.l2jfrozen.gameserver.powerpak.xmlrpc.XMLRPCClient_ManagementTester
REM
