@echo off
title Build Client Package - For Other PCs

SET "BIN_PATH=bin"
SET "LIB_JAR=..\mysql-connector-j-9.7.0 (3).jar"
SET "OUT_DIR=client_package"

REM === HARDCODED SERVER IP ===
SET "SERVER_IP=10.175.104.60"

cls
echo ===================================================
echo    Distributed Chat - Build Client Package
echo    Creates a folder to copy to other PCs
echo    Server IP: %SERVER_IP%
echo ===================================================
echo.

REM Check if bin folder exists (must compile first)
IF NOT EXIST "%BIN_PATH%" (
    echo [ERROR] 'bin' folder not found!
    echo         Please run compile.bat first.
    echo.
    pause
    exit /b 1
)

REM Check if compiled client classes exist
IF NOT EXIST "%BIN_PATH%\client" (
    echo [ERROR] Client classes not found in 'bin'!
    echo         Please run compile.bat first.
    echo.
    pause
    exit /b 1
)

REM Remove old package if exists
IF EXIST "%OUT_DIR%" (
    echo [*] Removing old client_package folder...
    rmdir /s /q "%OUT_DIR%"
)

echo [*] Creating client package folder...
mkdir "%OUT_DIR%"
mkdir "%OUT_DIR%\bin"

REM Copy compiled class files
echo [*] Copying compiled classes...
xcopy /e /q "%BIN_PATH%\client"   "%OUT_DIR%\bin\client\"
xcopy /e /q "%BIN_PATH%\shared"   "%OUT_DIR%\bin\shared\"

REM Copy MySQL connector JAR
echo [*] Copying MySQL connector JAR...
IF EXIST "%LIB_JAR%" (
    copy /y "%LIB_JAR%" "%OUT_DIR%\"
    echo [+] JAR copied.
) ELSE (
    echo [!] WARNING: MySQL JAR not found at %LIB_JAR%
    echo     Copy it manually into the client_package folder.
)

REM Create a ready-to-use run_client.bat inside the package
echo [*] Creating run_client.bat for client PCs...
(
echo @echo off
echo setlocal
echo title Distributed Chat - CLIENT
echo.
echo SET "BIN_PATH=bin"
echo SET "LIB_PATH=mysql-connector-j-9.7.0 ^(3^).jar"
echo.
echo REM === Server IP is pre-configured ===
echo SET "DEFAULT_SERVER_IP=%SERVER_IP%"
echo.
echo cls
echo echo ===================================================
echo echo       Distributed Chat System - CLIENT
echo echo ===================================================
echo echo.
echo echo  Server IP: %%DEFAULT_SERVER_IP%%
echo echo.
echo set /p SVRIP="Press Enter to connect to [%%DEFAULT_SERVER_IP%%] or type new IP: "
echo IF "%%SVRIP%%"=="" SET "SVRIP=%%DEFAULT_SERVER_IP%%"
echo echo.
echo echo [Connecting to %%SVRIP%%:1156 ...]
echo echo.
echo java -cp "%%BIN_PATH%%;%%LIB_PATH%%" client.ClientApp %%SVRIP%%
echo echo.
echo echo [!] Disconnected.
echo pause
) > "%OUT_DIR%\run_client.bat"

echo.
echo ===================================================
echo  [SUCCESS] Client package ready: .\%OUT_DIR%\
echo ===================================================
echo.
echo  Contents:
echo    bin\client\     - Client class files
echo    bin\shared\     - Shared interface classes
echo    mysql-connector-j-9.7.0 (3).jar
echo    run_client.bat  - Pre-configured with IP: %SERVER_IP%
echo.
echo ===================================================
echo  HOW TO SEND TO OTHER PCs:
echo ===================================================
echo  1. ZIP or copy 'client_package' folder via USB
echo     or shared folder to the other PC
echo  2. Make sure Java JDK/JRE is installed on that PC
echo  3. Double-click run_client.bat on the other PC
echo  4. Press Enter (IP %SERVER_IP% is pre-filled)
echo ===================================================
echo.
pause
