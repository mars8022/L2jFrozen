@echo off
color 0E
title L2Open: Login Server Console
:start
echo Starting L2Open Login Server.
echo.
java -Dfile.encoding=UTF-8 -Xmx64m -cp ./libs/*;l2open-login.jar interlude.loginserver.L2LoginServer
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
pause
