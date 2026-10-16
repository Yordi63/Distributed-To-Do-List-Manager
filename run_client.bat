@echo off
setlocal
title Distributed Chat - CLIENT (PC2)

SET "LIB_PATH=..\mysql-connector-j-9.7.0 (3).jar"
SET "BIN_PATH=bin"

cls
echo ===================================================
echo       Distributed Chat System - CLIENT (PC2)
echo ===================================================
echo.
echo  NOTE: The server (PC3) must already be running
echo        before you connect.
echo.
echo  A popup will appear asking for the Server IP.
echo.
echo ===================================================
echo  Starting Client... (A dialog will pop up)
echo ===================================================
echo.

java -cp "%BIN_PATH%;%LIB_PATH%" client.ClientApp

echo.
echo [!] Client has disconnected.
pause
