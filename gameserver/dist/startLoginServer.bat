@echo off
title L2J-Frozen: Login Server Console
:start

java -Dfile.encoding=UTF8 -Xmx128m -XX:+UseParallelGC -XX:+AggressiveOpts -cp ./lib/*;l2jfrozen-core.jar com.l2jfrozen.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted ...
echo.
goto start
:error
echo.
echo LoginServer terminated abnormaly
echo.
:end
echo.
echo LoginServer terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
