@echo off
setlocal
title Distributed Chat - SERVER (PC3)

SET "LIB_PATH=..\mysql-connector-j-9.7.0 (3).jar"
SET "BIN_PATH=bin"

cls
echo ===================================================
echo       Distributed Chat System - SERVER (PC3)
echo ===================================================
echo.
echo  A popup will appear asking for:
echo    1. Database IP (PC1 IP address)
echo    2. This Server's own IP (PC3 IP address)
echo.
echo ===================================================
echo  Checking if port 1099 is already in use...
echo ===================================================
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":1099 " ^| findstr "LISTENING"') do (
    echo [!] Port 1099 in use by PID %%p - killing it...
    taskkill /PID %%p /F >nul 2>&1
    echo [+] Port freed.
)
echo.
echo ===================================================
echo  Starting Server... (A dialog will pop up)
echo ===================================================
echo.

java -cp "%BIN_PATH%;%LIB_PATH%" server.ServerMain

echo.
echo [!] Server has stopped.
pause
