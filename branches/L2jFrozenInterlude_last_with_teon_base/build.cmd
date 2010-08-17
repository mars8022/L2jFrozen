@echo off
color 0F

REM Autor: Ramzessuss.
REM For Heaven Team.


:Step1
cls
echo.    #######################################################################
echo.    #  /  /  /  /                                              \   \   \  #
echo.    # /  /  /  /           .:: Heaven Compilator ::.            \   \   \ #
echo.    #/  /  /  /                                                  \   \   \#
echo.    #######################################################################
echo.
echo.      1 - Full compilation server.
echo.      2 - Compilation DataPack.
echo.      3 - Compilation GameServer.
echo.      4 - Compilation CORE only.
echo.      5 - Exit from installer
echo. -----------------------------------------------------------------------

set Step1prompt=x
set /p Step1prompt= Please enter values :
if /i %Step1prompt%==1 goto FullCompil
if /i %Step1prompt%==2 goto DPCompil
if /i %Step1prompt%==3 goto GSCompil
if /i %Step1prompt%==4 goto CoreCmpil
if /i %Step1prompt%==5 goto fullend
goto Step1


:FullCompil
@cls
title FullServer Compiler
color 0A
echo.
echo Compilation process. Please wait...
ant -f build-full.xml -l compile-full.log
echo Compilation successful!!!
pause

:DPCompil
@cls
title DataPack Compiler
color 0B
echo.
echo Compilation process. Please wait...
ant -f build-dp.xml -l compile-dp.log
echo Compilation successful!!!
pause

:GSCompil
@cls
title GameServer Compiler
color 0E
echo.
echo Compilation process. Please wait...
ant -f build-gs.xml -l compile-gs.log
echo Compilation successful!!!
pause

:CoreCmpil
@cls
title Core only Compiler
color 0C
echo.
echo Compilation process. Please wait...
ant -f build-core.xml -l compile-core.log
echo Compilation successful!!!
pause

:fullend
