@echo off
title GameServer Console
:start
echo Starting L2J-Scoria Core Game Server.
echo Official website : http://la2.100nt.ru
echo Enjoy by server core. Bee happy!
echo ------------------------------
echo.
REM ----------- Set Class Paths and Calls setenv.bat -----------------
SET OLDCLASSPATH=%CLASSPATH%
call setgame.bat
REM ------------------------------------------------------------------

REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF8 -server -Xmx1024m -Xms512m -Xmn256m -XX:PermSize=128m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts com.l2scoria.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 7 goto telldown
if ERRORLEVEL 6 goto tellrestart
if ERRORLEVEL 5 goto taskrestart
if ERRORLEVEL 4 goto taskdown
REM 3 - abort
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:tellrestart
echo.
echo Telnet server Restart ...
echo Send you bug to : http://la2.100nt.ru
echo.
goto start
:taskrestart
echo.
echo Auto Task Restart ...
echo Send you bug to : http://la2.100nt.ru
echo.
goto start
:restart
echo.
echo Admin Restart ...
echo Send you bug to : http://la2.100nt.ru
echo.
goto start
:taskdown
echo .
echo Server terminated (Auto task)
echo Send you bug to : http://la2.100nt.ru
echo .
:telldown
echo .
echo Server terminated (Telnet)
echo Send you bug to : http://la2.100nt.ru
echo .
:error
echo.
echo Server terminated abnormaly
echo Send you bug to : http://la2.100nt.ru
echo.
:end
echo.
echo server terminated
echo Send you bug to : http://la2.100nt.ru
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
