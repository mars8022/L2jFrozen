@echo off
color 0E
title L2Open: Game Server Console
:start
echo Starting L2Open Game Server.
echo.
REM ------------------------------------------------------------------------
REM #======================================================================#
REM # You need to set here your JDK/JRE params in case of x64 bits System. #
REM # Remove the "REM" after set PATH variable                             #
REM # If you're not a x64 system user just leave                           # 
REM #======================================================================#
REM set PATH="type here your path to java jdk/jre (including bin folder)"
REM ------------------------------------------------------------------------

REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF-8 -Xmx1024m -cp ./libs/*;l2open-game.jar interlude.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Administrator Restarted ...
echo.
goto start
:error
echo.
echo GameServer Terminated Abnormaly, Please Verify Your Files.
echo.
:end
echo.
echo GameServer Terminated.
echo.
pause
