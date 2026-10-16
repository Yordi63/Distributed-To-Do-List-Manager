@echo off
setlocal
title Compiling - Distributed Chat System

SET "LIB_PATH=..\mysql-connector-j-9.7.0 (3).jar"
SET "BIN_PATH=bin"
SET "SRC_PATH=src"

cls
echo ===================================================
echo        Distributed Chat System - Compiler
echo ===================================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo         Please install Java JDK and try again.
    pause
    exit /b 1
)

REM Check if MySQL connector exists
IF NOT EXIST "%LIB_PATH%" (
    echo [ERROR] MySQL connector JAR not found at: %LIB_PATH%
    echo         Make sure the JAR file is in the parent folder.
    pause
    exit /b 1
)

REM Create bin directory if it doesn't exist
IF NOT EXIST "%BIN_PATH%" (
    mkdir "%BIN_PATH%"
    echo [*] Created output directory: %BIN_PATH%
)

echo [*] Compiling shared classes...
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%" %SRC_PATH%\shared\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo [-] Failed to compile shared classes.
    pause
    exit /b 1
)
echo [+] Shared classes OK.

echo [*] Compiling server classes...
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%;%BIN_PATH%" %SRC_PATH%\server\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo [-] Failed to compile server classes.
    pause
    exit /b 1
)
echo [+] Server classes OK.

echo [*] Compiling client classes...
javac -d "%BIN_PATH%" -cp ".;%LIB_PATH%;%BIN_PATH%" %SRC_PATH%\client\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo [-] Failed to compile client classes.
    pause
    exit /b 1
)
echo [+] Client classes OK.

echo.
echo ===================================================
echo  [SUCCESS] All files compiled successfully!
echo  Classes are in the '%BIN_PATH%' directory.
echo ===================================================
echo.
pause
