@echo off
setlocal
title Distributed Chat System - Management Tool

SET "LIB_PATH=..\mysql-connector-j-9.7.0 (3).jar"
SET "BIN_PATH=bin"
SET "SRC_PATH=src"

REM === HARDCODED SERVER IP ===
SET "DEFAULT_IP=10.175.104.60"

cls
echo ===================================================
echo       Distributed Chat System - Main Menu
echo ===================================================
echo.
echo   THIS PC = Server + Database (MySQL)
echo   OTHER PC = Client only
echo.
echo ---------------------------------------------------
echo   1. Compile All Files         (do this first!)
echo   2. Run SERVER on This PC     (server + DB here)
echo   3. Run CLIENT on This PC     (for testing)
echo ---------------------------------------------------
echo   4. Recompile + Run Server
echo   5. Recompile + Run Client
echo ---------------------------------------------------
echo   6. Show This PC's IP         (share with clients)
echo   7. Exit
echo.
echo ===================================================
set /p choice="Select an option (1-7): "

if "%choice%"=="1" goto compile_only
if "%choice%"=="2" goto run_server
if "%choice%"=="3" goto run_client
if "%choice%"=="4" goto compile_and_server
if "%choice%"=="5" goto compile_and_client
if "%choice%"=="6" goto show_ip
if "%choice%"=="7" exit
goto menu

:: -----------------------------------------------
:compile_only
call :do_compile
pause
goto menu

:compile_and_server
call :do_compile
if %ERRORLEVEL% neq 0 (
    pause
    goto menu
)
goto run_server

:compile_and_client
call :do_compile
if %ERRORLEVEL% neq 0 (
    pause
    goto menu
)
goto run_client

:: -----------------------------------------------
:do_compile
echo.
echo [*] Compiling shared classes...
if not exist "%BIN_PATH%" mkdir "%BIN_PATH%"
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%" %SRC_PATH%\shared\*.java
if %ERRORLEVEL% neq 0 ( echo [-] Shared compile failed. & exit /b 1 )
echo [+] Shared OK.

echo [*] Compiling server classes...
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%;%BIN_PATH%" %SRC_PATH%\server\*.java
if %ERRORLEVEL% neq 0 ( echo [-] Server compile failed. & exit /b 1 )
echo [+] Server OK.

echo [*] Compiling client classes...
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%;%BIN_PATH%" %SRC_PATH%\client\*.java
if %ERRORLEVEL% neq 0 ( echo [-] Client compile failed. & exit /b 1 )
echo [+] Client OK.

echo.
echo [SUCCESS] All compiled successfully!
exit /b 0

:: -----------------------------------------------
:run_server
echo.
echo ===================================================
echo       Distributed Chat System - SERVER
echo       (This PC = Server + Database)
echo ===================================================
echo.
echo  Default Server IP: %DEFAULT_IP%
echo.
set /p SERVER_IP="Press Enter to use [%DEFAULT_IP%] or type a new IP: "
if "%SERVER_IP%"=="" set "SERVER_IP=%DEFAULT_IP%"

echo.
echo  Server IP : %SERVER_IP%
echo  Database  : localhost
echo  Port      : 1156
echo.
echo  >> Tell clients to connect to: %SERVER_IP%
echo.
echo [*] Checking if port 1156 is already in use...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":1156 " ^| findstr "LISTENING"') do (
    echo [!] Port 1156 in use by PID %%p - killing it...
    taskkill /PID %%p /F >nul 2>&1
    echo [+] Port freed.
)
echo.
echo [Starting server... Ctrl+C to stop]
echo.
java -cp "%BIN_PATH%;%LIB_PATH%" -Djava.rmi.server.hostname=%SERVER_IP% server.ServerMain %SERVER_IP% localhost
echo.
echo [!] Server stopped.
pause
goto menu

:: -----------------------------------------------
:run_client
echo.
echo ===================================================
echo       Distributed Chat System - CLIENT
echo ===================================================
echo.
echo  Default Server IP: %DEFAULT_IP%
echo.
set /p SERVER_IP="Press Enter to use [%DEFAULT_IP%] or type a new IP: "
if "%SERVER_IP%"=="" set "SERVER_IP=%DEFAULT_IP%"
echo.
echo [Connecting to %SERVER_IP%:1156 ...]
echo.
java -cp "%BIN_PATH%;%LIB_PATH%" client.ClientApp %SERVER_IP%
echo.
echo [!] Client disconnected.
pause
goto menu

:: -----------------------------------------------
:show_ip
echo.
echo ===================================================
echo   This PC's IP Address (share with clients):
echo ===================================================
ipconfig | findstr /i "IPv4"
echo.
echo ===================================================
pause
goto menu
